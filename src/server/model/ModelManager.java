package server.model;

import server.database.BookingDAO;
import server.database.DatabaseLoader;
import server.database.FlightDAO;
import server.database.UserDAO;
import server.logging.ServerLogger;
import client.model.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    private PropertyChangeSupport support;
    private ServerLogger logger;

    public ModelManager()
    {
        this.support = new PropertyChangeSupport(this);
        this.userDAO = new UserDAO();
        this.bookingDAO = new BookingDAO();
        this.flightDAO = new FlightDAO();
        this.allFlights = new ArrayList<>();
        this.databaseLoader = new DatabaseLoader();
        this.logger = ServerLogger.getInstance();

        try
        {
            this.flightSearchService = databaseLoader.loadAll();

            if (databaseLoader.getFlights() != null)
            {
                this.allFlights.clear();
                // generate recurring flights so each route flies multiple times per week
                List<Flight> recurring = FlightSearchService.generateRecurringFlights(
                    databaseLoader.getFlights());
                this.allFlights.addAll(recurring);
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
        return getActiveCustomer(currentUser);
    }

    private Customer getActiveCustomer(User user)
    {
        if (user instanceof Customer customer)
        {
            return customer;
        }
        throw new IllegalStateException("A customer account is required.");
    }

    private List<Flight> getLoadedFlights()
    {
        List<Flight> flights = allFlights;
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
        User oldUser = this.currentUser;
        User user = authenticate(email, password);
        if (user != null)
        {
            this.currentUser = user;
            support.firePropertyChange("currentUser", oldUser, currentUser);
            return true;
        }
        return false;
    }

    public User authenticate(String email, String password)
    {
        try
        {
            User user = userDAO.login(email, password, flightSearchService);
            if (user != null)
            {
                user.login(email, password);
            }
            return user;
        }
        catch (SQLException e)
        {
            logger.log("model", "LOGIN", "ERROR",
                "Login error: " + e.getMessage());
            System.out.println("Login error");
            return null;
        }
    }

    @Override
    public boolean register(String firstName, String lastName, String email, String password) {
        try {
            boolean registered = userDAO.registerCustomer(firstName, lastName,
                email, password);
            logger.log("model", "REGISTER", registered ? "OK" : "ERROR",
                "email=" + email);
            return registered;
        } catch (SQLException e) {
            logger.log("model", "REGISTER", "ERROR",
                "Registration error: " + e.getMessage());
            System.out.println("Registration error connecting to database");
            return false;
        }
    }

    @Override
    public void logout()
    {
        User oldUser = this.currentUser;
        if (oldUser != null)
        {
            oldUser.logout();
        }
        this.currentUser = null;
        support.firePropertyChange("currentUser", oldUser, null);
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
        return createBooking(flight, passengers, selectedSeats, null, null);
    }

    @Override
    public Booking createBooking(Flight flight, List<Passenger> passengers,
        List<Seat> selectedSeats, Flight returnFlight, List<Seat> returnSeats)
    {
        return createBookingInternal(currentUser, flight, passengers,
            selectedSeats, returnFlight, returnSeats);
    }

    public Booking createBookingInternal(User user, Flight flight,
        List<Passenger> passengers, List<Seat> selectedSeats,
        Flight returnFlight, List<Seat> returnSeats)
    {
        Customer customer = getActiveCustomer(user);
        Booking booking = customer.createBooking(flight, passengers);

        // assign outbound seats
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

        // assign return flight seats if this is a roundtrip booking
        if (returnFlight != null) {
            booking.setReturnFlight(returnFlight);

            if (returnSeats != null) {
                List<Flight> returnSegments = new ArrayList<>();
                if (returnFlight instanceof ConnectingFlight connReturn) {
                    returnSegments.add(connReturn.getFirstSegment());
                    returnSegments.add(connReturn.getSecondSegment());
                } else {
                    returnSegments.add(returnFlight);
                }

                int seatIndex = 0;
                try {
                    for (int p = 0; p < passengers.size(); p++) {
                        for (int s = 0; s < returnSegments.size(); s++) {
                            if (seatIndex < returnSeats.size()) {
                                Seat seat = returnSeats.get(seatIndex++);
                                if (seat != null) {
                                    new SeatAssignment(nextSeatAssignmentId++, passengers.get(p), seat, returnSegments.get(s));
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
        }

        booking.recalculateTotalPrice();

        // saves the booking to the database
        try {
            bookingDAO.saveBooking(booking);
            booking.confirmBooking();
            logger.log("model", "CREATE_BOOKING", "OK",
                "bookingId=" + booking.getBookingId()
                    + ", customer=" + customer.getEmail());
        } catch (SQLException e) {
            booking.cancel();
            customer.removeBooking(booking);
            logger.log("model", "CREATE_BOOKING", "ERROR",
                "Failed to save booking: " + e.getMessage());
            System.out.println("Failed to save booking");
            throw new IllegalStateException("Failed to save booking.", e);
        }
        support.firePropertyChange("bookings", null, booking);
        return booking;
    }

    @Override
    public void cancelBooking(Booking booking)
    {
        cancelBooking(currentUser, booking);
    }

    public void cancelBooking(User user, Booking booking)
    {
        if (user instanceof Customer customer)
        {
            try
            {
                for (Passenger passenger : booking.getPassengers())
                {
                    for (SeatAssignment assignment :
                        new ArrayList<>(passenger.getSeatAssignments()))
                    {
                        if (assignment.getSeat() != null)
                        {
                            assignment.getSeat().setOccupied(false);
                        }
                        assignment.release();
                    }
                }
                bookingDAO.removeBooking(booking.getBookingId());
                customer.cancelBooking(booking);
                logger.log("model", "CANCEL_BOOKING", "OK",
                    "bookingId=" + booking.getBookingId()
                        + ", customer=" + customer.getEmail());
                support.firePropertyChange("bookings", booking, null);
            }
            catch (SQLException e)
            {
                logger.log("model", "CANCEL_BOOKING", "ERROR",
                    "Failed to remove booking: " + e.getMessage());
                System.out.println("Failed to remove booking: " + e.getMessage());
                throw new RuntimeException("Database error during cancellation.");
            }
        }
    }

    @Override
    public List<Booking> getAllBookings()
    {
        return getAllBookings(currentUser);
    }

    public List<Booking> getAllBookings(User user)
    {
        if (!(user instanceof Admin))
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
        return getUserBookings(currentUser);
    }

    public List<Booking> getUserBookings(User user)
    {
        Customer customer = getActiveCustomer(user);
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
        return addBookingToCurrentUserById(currentUser, bookingId,
            passengerLastName);
    }

    public Booking addBookingToCurrentUserById(User user, int bookingId,
        String passengerLastName)
    {
        Customer customer = getActiveCustomer(user);
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
            logger.log("model", "ADD_BOOKING_TO_CURRENT_USER_BY_ID", "OK",
                "bookingId=" + bookingId + ", customer="
                    + customer.getEmail());
            support.firePropertyChange("bookings", null, booking);
            return booking;
        }
        catch (SQLException e)
        {
            logger.log("model", "ADD_BOOKING_TO_CURRENT_USER_BY_ID", "ERROR",
                "Could not link booking: " + e.getMessage());
            throw new IllegalStateException(
                "Could not add booking from the database.", e);
        }
    }

    @Override
    public void addFlight(Flight flight)
    {
        addFlight(currentUser, flight);
    }

    public void addFlight(User user, Flight flight)
    {
        if (user instanceof Admin admin)
        {
            try
            {
                this.flightDAO.saveFlight(flight);
                admin.createFlight(flight);

                if (!allFlights.contains(flight))
                {
                    List<Flight> oldFlights = new ArrayList<>(allFlights);
                    allFlights.add(flight);
                    support.firePropertyChange("allFlights", oldFlights, allFlights);
                }
                logger.log("model", "ADD_FLIGHT", "OK",
                    "flightId=" + flight.getFlightId()
                        + ", admin=" + admin.getEmail());
            }
            catch (SQLException e)
            {
                logger.log("model", "ADD_FLIGHT", "ERROR",
                    "Could not save flight: " + e.getMessage());
                throw new RuntimeException("Could not save flight", e);
            }
        }
    }

    @Override
    public void removeFlight(Flight flight)
    {
        removeFlight(currentUser, flight);
    }

    public void removeFlight(User user, Flight flight)
    {
        if (user instanceof Admin admin)
        {
            try
            {
                flightDAO.removeFlight(flight.getFlightId());
                List<Flight> oldFlights = new ArrayList<>(allFlights);
                admin.deleteFlight(flight);
                allFlights.remove(flight);
                logger.log("model", "REMOVE_FLIGHT", "OK",
                    "flightId=" + flight.getFlightId()
                        + ", admin=" + admin.getEmail());
                support.firePropertyChange("allFlights", oldFlights, allFlights);
            }
            catch (SQLException e)
            {
                logger.log("model", "REMOVE_FLIGHT", "ERROR",
                    "Could not remove flight: " + e.getMessage());
                throw new RuntimeException("Could not remove flight", e);
            }
        }
    }

    @Override
    public void editFlight(Flight flight)
    {
        editFlight(currentUser, flight);
    }

    public void editFlight(User user, Flight flight)
    {
        if (user instanceof Admin admin)
        {
            try
            {
                flightDAO.updateFlight(flight);

                // replace the old flight in the in-memory list
                for (int i = 0; i < allFlights.size(); i++)
                {
                    if (allFlights.get(i).getFlightId() == flight.getFlightId())
                    {
                        allFlights.set(i, flight);
                        break;
                    }
                }

                admin.updateFlight(flight);
                logger.log("model", "EDIT_FLIGHT", "OK",
                    "flightId=" + flight.getFlightId()
                        + ", admin=" + admin.getEmail());
                support.firePropertyChange("allFlights", null, allFlights);
            }
            catch (SQLException e)
            {
                logger.log("model", "EDIT_FLIGHT", "ERROR",
                    "Could not edit flight: " + e.getMessage());
                throw new RuntimeException("Could not edit flight", e);
            }
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

    @Override
    public boolean isLoggedIn()
    {
        return currentUser != null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}



