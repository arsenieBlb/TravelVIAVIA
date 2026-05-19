package server.network.dto;

import java.util.ArrayList;
import java.util.List;

public class BookingRequest
{
  public FlightDto flight;
  public List<PassengerDto> passengers = new ArrayList<>();
  public List<SeatDto> selectedSeats = new ArrayList<>();
  public FlightDto returnFlight;
  public List<SeatDto> returnSeats = new ArrayList<>();

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

  public BookingRequest(FlightDto flight, List<PassengerDto> passengers,
      List<SeatDto> selectedSeats, FlightDto returnFlight,
      List<SeatDto> returnSeats)
  {
    this(flight, passengers, selectedSeats);
    this.returnFlight = returnFlight;
    if (returnSeats != null)
    {
      this.returnSeats = returnSeats;
    }
  }
}


