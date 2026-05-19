package server.network.dto;

import java.util.ArrayList;
import java.util.List;

public class BookingRequest
{
  public FlightDto flight;
  public List<PassengerDto> passengers = new ArrayList<>();
  public List<SeatDto> selectedSeats = new ArrayList<>();

  public BookingRequest()
  {
  }

  public BookingRequest(FlightDto flight, List<PassengerDto> passengers,
      List<SeatDto> selectedSeats)
  {
    this.flight = flight;
    if (passengers != null)
    {
      this.passengers = passengers;
    }
    if (selectedSeats != null)
    {
      this.selectedSeats = selectedSeats;
    }
  }
}


