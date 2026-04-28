package model;

import java.util.List;

public interface Model
{

    List<Flight> searchFlights(SearchCriteria criteria);

    Flight getFlightDetails(int flightId);

    boolean login(String email, String password);

    void logout();

    User getLoggedInUser();

    Booking createBooking(Flight flight, List<Passenger> passengers);

    void cancelBooking(Booking booking);

    List<Booking> getUserBookings();

    void addFlight(Flight flight);

    void removeFlight(Flight flight);

}
