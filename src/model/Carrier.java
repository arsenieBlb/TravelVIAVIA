package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Carrier
{
  private int carrierId;
  private String name;
  private final List<Flight> flights;
  private final List<Plane> planes;

  public Carrier(int carrierId, String name)
  {
    this.flights = new ArrayList<>();
    this.planes = new ArrayList<>();
    setCarrierId(carrierId);
    setName(name);
  }

  public void addFlight(Flight flight)
  {
    Objects.requireNonNull(flight, "Flight is required.");
    if (!flights.contains(flight))
    {
      flights.add(flight);
    }
    if (flight.getCarrier() != this)
    {
      flight.setCarrier(this);
    }
  }

  public void removeFlight(Flight flight)
  {
    flights.remove(flight);
  }

  public void addPlane(Plane plane)
  {
    Objects.requireNonNull(plane, "Plane is required.");
    if (!planes.contains(plane))
    {
      planes.add(plane);
    }
    if (plane.getCarrier() != this)
    {
      plane.setCarrier(this);
    }
  }

  public void removePlane(Plane plane)
  {
    planes.remove(plane);
  }

  public int getCarrierId()
  {
    return carrierId;
  }

  public void setCarrierId(int carrierId)
  {
    if (carrierId <= 0)
    {
      throw new IllegalArgumentException("Carrier id must be positive.");
    }
    this.carrierId = carrierId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    if (name == null || name.isBlank())
    {
      throw new IllegalArgumentException("Carrier name is required.");
    }
    this.name = name;
  }

  public List<Flight> getFlights()
  {
    return Collections.unmodifiableList(flights);
  }

  public List<Plane> getPlanes()
  {
    return Collections.unmodifiableList(planes);
  }

  @Override public String toString()
  {
    return name;
  }
}
