package server.network.dto;

public class AddBookingByIdRequest
{
  public int bookingId;
  public String lastName;

  public AddBookingByIdRequest()
  {
  }

  public AddBookingByIdRequest(int bookingId, String lastName)
  {
    this.bookingId = bookingId;
    this.lastName = lastName;
  }
}


