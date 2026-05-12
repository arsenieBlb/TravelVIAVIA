package model;

public class MaintenanceState implements PlaneState
{
    @Override
    public void activate(Plane plane)
    {
        plane.setInternalStatus(new ActiveState());
    }

    @Override
    public void sendToMaintenance(Plane plane)
    {
    }

    @Override
    public void decommission(Plane plane)
    {
        plane.setInternalStatus(new InactiveState());
    }

    @Override
    public String getStatus()
    {
        return "Maintenance";
    }
}