package client.model;

public interface BookingState
{
    void confirm(Booking booking);

    void cancel(Booking booking);

    String getStatus();
}
