package client.model;

public class ConfirmedState implements BookingState
{
    @Override
    public void confirm(Booking booking)
    {
        // already confirmed, nothing to do
    }

    @Override
    public void cancel(Booking booking)
    {
        booking.setInternalState(new CancelledState());
    }

    @Override
    public String getStatus()
    {
        return "Confirmed";
    }
}
