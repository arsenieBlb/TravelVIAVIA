package server.network.dto;

public class AddBookingByIdRequest
{
  public int bookingId;
  public String passengerLastName;

  public AddBookingByIdRequest()
  {
  }

  public AddBookingByIdRequest(int bookingId, String passengerLastName)
  {
    this.bookingId = bookingId;
    this.passengerLastName = passengerLastName;
  }
}


