package client.model;

public class CancelledState implements BookingState
{
    @Override
    public void confirm(Booking booking)
    {
        throw new IllegalStateException("Cannot confirm a cancelled booking.");
    }

    @Override
    public void cancel(Booking booking)
    {
        // already cancelled, nothing to do
    }

    @Override
    public String getStatus()
    {
        return "Cancelled";
    }
}
