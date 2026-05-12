package model;

public class InactiveState implements PlaneState
{
    @Override
    public void activate(Plane plane)
    {
        plane.setInternalStatus(new ActiveState());
    }

    @Override
    public void sendToMaintenance(Plane plane)
    {
        throw new IllegalStateException("Cannot send a decommissioned plane to maintenance.");
    }

    @Override
    public void decommission(Plane plane)
    {
    }

    @Override
    public String getStatus()
    {
        return "Inactive";
    }
}
