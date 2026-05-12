package servermodel.dto;

import java.util.ArrayList;
import java.util.List;

public class FlightDto
{
  public int flightId;
  public String flightNumber;
  public String departureTime;
  public String arrivalTime;
  public double basePrice;
  public CarrierDto carrier;
  public PlaneDto plane;
  public CityDto departureCity;
  public CityDto arrivalCity;
  public boolean connecting;
  public FlightDto firstSegment;
  public FlightDto secondSegment;
  public List<Integer> availableSeatIds = new ArrayList<>();

  public FlightDto()
  {
  }
}
