package model;

import database.BookingDAO;
import database.DatabaseLoader;
import database.UserDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModelManager implements Model
{

    private FlightSearchService flightSearchService;
    private User currentUser;
    private DatabaseLoader databaseLoader;
    private UserDAO userDAO;
    private BookingDAO bookingDAO;

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
        if (currentUser instanceof Customer customer)
        {
            Booking booking = customer.createBooking(flight, passengers);
            
            // saves the booking to the database
            try {
                bookingDAO.saveBooking(booking);
            } catch (SQLException e) {
                System.out.println("Failed to save booking");
            }
            return booking;
        }
        throw new IllegalStateException("Only registered customers can create bookings.");
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
        if (currentUser instanceof Customer customer)
        {
            return customer.viewBookings();
        }
        if (currentUser instanceof Admin admin)
        {
            return admin.viewBookings();
        }
        return new ArrayList<>();
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
}
