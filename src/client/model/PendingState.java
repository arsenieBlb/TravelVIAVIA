package client.model;

public class PendingState implements BookingState
{
    @Override
    public void confirm(Booking booking)
    {
        booking.setInternalState(new ConfirmedState());
    }

    @Override
    public void cancel(Booking booking)
    {
        booking.setInternalState(new CancelledState());
    }

    @Override
    public String getStatus()
    {
        return "Pending";
    }
}
