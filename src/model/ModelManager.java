package model;

import database.BookingDAO;
import database.DatabaseLoader;
import database.FlightDAO;
import database.UserDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ModelManager implements Model
{

    private FlightSearchService flightSearchService;
    private User currentUser;
    private DatabaseLoader databaseLoader;
    private UserDAO userDAO;
    private BookingDAO bookingDAO;
    private int nextSeatAssignmentId = 1;
    private List<Flight> allFlights;
    private FlightDAO flightDAO;

    public ModelManager()
    {
        this.userDAO = new UserDAO();
        this.bookingDAO = new BookingDAO();
        this.flightDAO = new FlightDAO();
        this.allFlights = new ArrayList<>();
        this.databaseLoader = new DatabaseLoader();

        try
        {
            this.flightSearchService = databaseLoader.loadAll();

            if (databaseLoader.getFlights() != null)
            {
                this.allFlights.clear();
                this.allFlights.addAll(databaseLoader.getFlights());
            }
        }
        catch (SQLException e)
        {
            System.out.println("Database error loading data: " + e.getMessage());
            this.flightSearchService = new FlightSearchService();
        }
        currentUser = null;
    }

    private void useDemoCustomer()
    {
        try
        {
            Customer demoCustomer = userDAO.getCustomerById(2,
                flightSearchService);
            if (demoCustomer == null)
            {
                demoCustomer = userDAO.getFirstCustomer(flightSearchService);
            }
            if (demoCustomer != null)
            {
                currentUser = demoCustomer;
                return;
            }
        }
        catch (SQLException e)
        {
            System.out.println("Could not load demo customer from database");
        }

        currentUser = new Customer(2, "j.doe@gmail.com", "1234b", "John",
            "Doe", flightSearchService);
    }

    private Customer getActiveCustomer()
    {
        if (currentUser instanceof Customer customer)
        {
            return customer;
        }
        throw new IllegalStateException("A customer account is required.");
    }

    private List<Flight> getLoadedFlights()
    {
        List<Flight> flights = databaseLoader.getFlights();
        return flights == null ? Collections.emptyList() : flights;
    }

    private List<LuggageType> getLoadedLuggageTypes()
    {
        List<LuggageType> luggageTypes = databaseLoader.getLuggageTypes();
        return luggageTypes == null ? Collections.emptyList() : luggageTypes;
    }

    @Override
    public List<Flight> searchFlights(SearchCriteria criteria)
    {
        List<Flight> found = flightSearchService.searchFlights(this.allFlights, criteria);

        return found;
    }

    @Override
    public Flight getFlightDetails(int flightId)
    {
        return flightSearchService.viewFlightDetails(flightId);
    }

    @Override
    public boolean login(String email, String password)
    {
        try {
            User user = userDAO.login(email, password, flightSearchService);
            if (user != null)
            {
                this.currentUser = user;
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Login error connecting to database");
        }
        return false;
    }

    @Override
    public boolean register(String firstName, String lastName, String email, String password) {
        try {
            return userDAO.registerCustomer(firstName, lastName, email, password);
        } catch (SQLException e) {
            System.out.println("Registration error connecting to database");
            return false;
        }
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public User getLoggedInUser()
    {
        return currentUser;
    }

    @Override
    public Booking createBooking(Flight flight, List<Passenger> passengers)
    {
        return createBooking(flight, passengers, new ArrayList<>());
    }

    @Override
    public Booking createBooking(Flight flight, List<Passenger> passengers,
        List<Seat> selectedSeats)
    {
        Customer customer = getActiveCustomer();
        Booking booking = customer.createBooking(flight, passengers);

        if (selectedSeats != null)
        {
            List<Flight> segments = new ArrayList<>();
            if (flight instanceof ConnectingFlight connectingFlight) {
                segments.add(connectingFlight.getFirstSegment());
                segments.add(connectingFlight.getSecondSegment());
            } else {
                segments.add(flight);
            }

            int seatIndex = 0;
            try {
                for (int p = 0; p < passengers.size(); p++) {
                    for (int s = 0; s < segments.size(); s++) {
                        if (seatIndex < selectedSeats.size()) {
                            Seat seat = selectedSeats.get(seatIndex++);
                            if (seat != null) {
                                new SeatAssignment(nextSeatAssignmentId++, passengers.get(p), seat, segments.get(s));
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                booking.cancel();
                customer.removeBooking(booking);
                throw e;
            }
        }

        booking.recalculateTotalPrice();

        // saves the booking to the database
        try {
            bookingDAO.saveBooking(booking);
        } catch (SQLException e) {
            booking.cancel();
            customer.removeBooking(booking);
            System.out.println("Failed to save booking");
            throw new IllegalStateException("Failed to save booking.", e);
        }
        return booking;
    }

    @Override
    public void cancelBooking(Booking booking) {
        if (currentUser instanceof Customer customer) {
            try {
                bookingDAO.removeBooking(booking.getBookingId());
                customer.cancelBooking(booking);
            } catch (SQLException e) {
                System.out.println("Failed to remove booking from database: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Booking> getAllBookings()
    {
        if (!(currentUser instanceof Admin))
        {
            return Collections.emptyList();
        }

        try
        {
            List<Customer> customers = userDAO.getAllCustomers(
                flightSearchService);
            return bookingDAO.getAllBookings(getLoadedFlights(), customers,
                getLoadedLuggageTypes());
        }
        catch (SQLException e)
        {
            System.out.println("Failed to load all bookings from database");
            return Collections.emptyList();
        }
    }

    @Override
    public List<Booking> getUserBookings()
    {
        Customer customer = getActiveCustomer();
        try
        {
            return bookingDAO.getBookingsForCustomer(customer,
                getLoadedFlights(), getLoadedLuggageTypes());
        }
        catch (SQLException e)
        {
            System.out.println("Failed to load bookings from database");
            return new ArrayList<>(customer.viewBookings());
        }
    }

    @Override
    public Booking addBookingToCurrentUserById(int bookingId,
        String passengerLastName)
    {
        Customer customer = getActiveCustomer();
        try
        {
            if (!bookingDAO.bookingExists(bookingId))
            {
                throw new IllegalArgumentException("Booking ID was not found.");
            }
            if (!bookingDAO.bookingHasPassengerLastName(bookingId,
                passengerLastName))
            {
                throw new IllegalArgumentException(
                    "The last name does not match this booking.");
            }

            bookingDAO.linkBookingToCustomer(bookingId, customer.getUserId());
            Booking booking = bookingDAO.getBookingById(bookingId, customer,
                getLoadedFlights(), getLoadedLuggageTypes());
            if (booking == null)
            {
                throw new IllegalArgumentException(
                    "Booking exists, but its flight is not loaded.");
            }
            return booking;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(
                "Could not add booking from the database.", e);
        }
    }

    @Override
    public void addFlight(Flight flight)
    {
        if (currentUser instanceof Admin admin)
        {
            try
            {
                this.flightDAO.saveFlight(flight);

                admin.createFlight(flight);

                if (!allFlights.contains(flight))
                {
                    allFlights.add(flight);
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException("Could not save flight to database", e);
            }
        }
        else
        {
            throw new SecurityException("Only admins can add flights.");
        }
    }

    @Override
    public void removeFlight(Flight flight)
    {
        if (currentUser instanceof Admin admin)
        {
            admin.deleteFlight(flight);
        }
    }
    
    // gives access to everything loaded from the database
    public DatabaseLoader getDatabaseLoader() {
        return databaseLoader;
    }

    public FlightSearchService getFlightSearchService()
    {
        return flightSearchService;
    }

    @Override
    public List<LuggageType> getLuggageTypes() {
        return getLoadedLuggageTypes();
    }

    @Override
    public List<City> getAllCities()
    {
        return Objects.requireNonNullElse(databaseLoader.getCities(), new ArrayList<>());
    }

    @Override
    public List<City> getCities()
    {
        return Objects.requireNonNullElse(databaseLoader.getCities(), new ArrayList<>());
    }

    @Override
    public List<Plane> getPlanes()
    {
        return Objects.requireNonNullElse(databaseLoader.getPlanes(), new ArrayList<>());
    }

    @Override
    public List<Carrier> getCarriers()
    {
        return Objects.requireNonNullElse(databaseLoader.getCarriers(), new ArrayList<>());
    }

    @Override
    public List<Flight> getAllFlights()
    {
        return Objects.requireNonNullElse(allFlights, new ArrayList<>());
    }
}
