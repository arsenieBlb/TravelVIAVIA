package model;

import java.util.ArrayList;
import java.util.List;

public class ModelManager implements Model
{

    private FlightSearchService flightSearchService;
    private User currentUser;

    public ModelManager()
    {
        this.flightSearchService = new FlightSearchService();
        this.currentUser = null;
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

    @Override
    public boolean login(String email, String password)
    {
        if ("admin@via.com".equals(email) && "password".equals(password))
        {
            this.currentUser = new Admin(1, email, password, flightSearchService);
            return true;
        }
        return false;

        // !!! Guys, this needs to be redone, it's a placeholder. !!!
    }

    @Override
    public void logout()
    {
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
            return customer.createBooking(flight, passengers);
        }
        throw new IllegalStateException("Only registered customers can create bookings.");
    }

    @Override
    public void cancelBooking(Booking booking)
    {
        if (currentUser instanceof Customer customer)
        {
            customer.cancelBooking(booking);
        }
        else if (currentUser instanceof Admin admin)
        {
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
        if (currentUser instanceof Admin admin)
        {
            admin.createFlight(flight);
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
}
