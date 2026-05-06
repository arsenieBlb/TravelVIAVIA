package model;

import database.BookingDAO;
import database.DatabaseLoader;
import database.UserDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelManager implements Model
{

    private FlightSearchService flightSearchService;
    private User currentUser;
    private DatabaseLoader databaseLoader;
    private UserDAO userDAO;
    private BookingDAO bookingDAO;
    private int nextSeatAssignmentId = 1;

    public ModelManager()
    {
        this.userDAO = new UserDAO();
        this.bookingDAO = new BookingDAO();
        
        this.databaseLoader = new DatabaseLoader();

        // loads all flights, planes, cities, carriers from the database
        try {
            this.flightSearchService = databaseLoader.loadAll();
        } catch (SQLException e) {
            System.out.println("Database error loading data");
            this.flightSearchService = new FlightSearchService();
        }
        useDemoCustomer();
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
        if (!(currentUser instanceof Customer))
        {
            useDemoCustomer();
        }
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
        return flightSearchService.searchFlights(criteria);
    }

    @Override
    public Flight getFlightDetails(int flightId)
    {
        return flightSearchService.viewFlightDetails(flightId);
    }

    // checks the database for matching email and password
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
            for (int i = 0; i < passengers.size()
                && i < selectedSeats.size(); i++)
            {
                Seat seat = selectedSeats.get(i);
                if (seat != null)
                {
                    new SeatAssignment(nextSeatAssignmentId++,
                        passengers.get(i), seat, flight);
                }
            }
        }

        // saves the booking to the database
        try {
            bookingDAO.saveBooking(booking);
        } catch (SQLException e) {
            System.out.println("Failed to save booking");
            throw new IllegalStateException("Failed to save booking.", e);
        }
        return booking;
    }

    @Override
    public void cancelBooking(Booking booking)
    {
        if (currentUser instanceof Customer customer) {
            customer.cancelBooking(booking);
        } else if (currentUser instanceof Admin admin) {
            booking.cancel();
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
        if (currentUser instanceof Admin admin) {
            admin.createFlight(flight);
        } else {
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
    public List<City> getAllCities() {
        List<City> cities = databaseLoader.getCities();
        return cities == null ? Collections.emptyList() : cities;
    }
}
