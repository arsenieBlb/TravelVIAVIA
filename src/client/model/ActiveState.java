package client.model;

public class ActiveState implements PlaneState
{
    @Override
    public void activate(Plane plane)
    {
    }

    @Override
    public void sendToMaintenance(Plane plane)
    {
        plane.setInternalStatus(new MaintenanceState());
    }

    @Override
    public void decommission(Plane plane)
    {
        plane.setInternalStatus(new InactiveState());
    }

    @Override
    public String getStatus()
    {
        return "Active";
    }
}


