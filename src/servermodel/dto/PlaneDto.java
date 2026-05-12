package servermodel.dto;

import java.util.ArrayList;
import java.util.List;

public class PlaneDto
{
  public int planeId;
  public String registrationNumber;
  public String status;
  public int planeTypeId;
  public String typeName;
  public String model;
  public int numberOfColumns;
  public int numberOfEconomySeats;
  public int numberOfBusinessSeats;
  public CarrierDto carrier;
  public List<SeatDto> seats = new ArrayList<>();

  public PlaneDto()
  {
  }
}
