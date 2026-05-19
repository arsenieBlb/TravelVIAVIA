package server.network.dto;

import java.util.ArrayList;
import java.util.List;

public class BookingDto
{
  public int bookingId;
  public String bookingDate;
  public double totalPrice;
  public UserDto customer;
  public FlightDto flight;
  public List<PassengerDto> passengers = new ArrayList<>();
  public boolean cancelled;

  public BookingDto()
  {
  }
}


