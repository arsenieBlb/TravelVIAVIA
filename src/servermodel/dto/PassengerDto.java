package servermodel.dto;

import java.util.ArrayList;
import java.util.List;

public class PassengerDto
{
  public int passengerId;
  public String firstName;
  public String lastName;
  public List<LuggageDto> luggage = new ArrayList<>();
  public List<SeatAssignmentDto> seatAssignments = new ArrayList<>();

  public PassengerDto()
  {
  }

  public static class LuggageDto
  {
    public int passengerLuggageId;
    public int quantity;
    public LuggageTypeDto luggageType;

    public LuggageDto()
    {
    }
  }

  public static class SeatAssignmentDto
  {
    public int flightId;
    public SeatDto seat;

    public SeatAssignmentDto()
    {
    }
  }
}
