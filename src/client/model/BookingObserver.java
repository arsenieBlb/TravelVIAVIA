package client.model;

public interface BookingObserver
{
    void onBookingStateChanged(Booking booking, String oldState, String newState);
}
