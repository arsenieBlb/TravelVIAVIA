package model;

public interface PlaneState
{
    void activate(Plane plane);

    void sendToMaintenance(Plane plane);

    void decommission(Plane plane);

    String getStatus();
}