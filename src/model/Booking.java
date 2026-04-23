package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Booking
{
  private static int nextBookingId = 1;

  private int bookingId;
  private LocalDateTime bookingDate;
  private double totalPrice;
  private Customer customer;
  private Flight flight;
  private final List<Passenger> passengers;
  private boolean cancelled;

  public Booking(Customer customer, Flight flight, List<Passenger> passengers)
  {
    this(nextBookingId++, LocalDateTime.now(), customer, flight, passengers);
  }

  public Booking(int bookingId, Customer customer, Flight flight,
      List<Passenger> passengers)
  {
    this(bookingId, LocalDateTime.now(), customer, flight, passengers);
  }

  public Booking(int bookingId, LocalDateTime bookingDate, Customer customer,
      Flight flight, List<Passenger> passengers)
  {
    this.passengers = new ArrayList<>();
    setBookingId(bookingId);
    setBookingDate(bookingDate);
    setCustomer(customer);
    setFlight(flight);
    setPassengers(passengers);
    recalculateTotalPrice();
    this.customer.addBooking(this);
    this.flight.addBooking(this);
  }

  public void cancel()
  {
    if (cancelled)
    {
      return;
    }

    for (Passenger passenger : passengers)
    {
      SeatAssignment seatAssignment = passenger.getSeatAssignment();
      if (seatAssignment != null && seatAssignment.getFlight().equals(flight))
      {
        seatAssignment.release();
      }
    }
    cancelled = true;
  }

  public String getBookingSummary()
  {
    recalculateTotalPrice();

    StringBuilder summary = new StringBuilder();
    summary.append("Booking #").append(bookingId).append(System.lineSeparator());
    summary.append("Cancelled: ").append(cancelled).append(System.lineSeparator());
    summary.append("Customer: ").append(customer.getFullName())
        .append(System.lineSeparator());
    summary.append("Flight: ").append(flight.getFlightNumber()).append(" ")
        .append(flight.getDepartureCity().getCityName()).append(" -> ")
        .append(flight.getArrivalCity().getCityName())
        .append(System.lineSeparator());
    summary.append("Departure time: ").append(flight.getDepartureTime())
        .append(System.lineSeparator());
    summary.append("Arrival time: ").append(flight.getArrivalTime())
        .append(System.lineSeparator());
    summary.append("Booking date: ").append(bookingDate)
        .append(System.lineSeparator());
    summary.append("Passengers:").append(System.lineSeparator());

    for (Passenger passenger : passengers)
    {
      summary.append("- ").append(passenger.getFullName());
      SeatAssignment seatAssignment = passenger.getSeatAssignment();
      if (seatAssignment != null && seatAssignment.getFlight().equals(flight))
      {
        summary.append(", seat ")
            .append(seatAssignment.getSeat().getSeatNumber());
      }

      if (!passenger.getPassengerLuggage().isEmpty())
      {
        summary.append(", luggage: ");
        for (int i = 0; i < passenger.getPassengerLuggage().size(); i++)
        {
          PassengerLuggage luggage = passenger.getPassengerLuggage().get(i);
          if (i > 0)
          {
            summary.append("; ");
          }
          summary.append(luggage.getQuantity()).append(" x ")
              .append(luggage.getLuggageType().getName()).append(" (")
              .append(luggage.getTotalExtraPrice()).append(")");
        }
      }
      summary.append(System.lineSeparator());
    }

    summary.append("Total price: ").append(totalPrice);
    return summary.toString();
  }

  public void recalculateTotalPrice()
  {
    double luggagePrice = 0;
    for (Passenger passenger : passengers)
    {
      for (PassengerLuggage luggage : passenger.getPassengerLuggage())
      {
        luggagePrice += luggage.getTotalExtraPrice();
      }
    }
    totalPrice = (flight.getBasePrice() * passengers.size()) + luggagePrice;
  }

  private void setPassengers(List<Passenger> passengers)
  {
    if (passengers == null || passengers.isEmpty())
    {
      throw new IllegalArgumentException(
          "A booking must include at least one passenger.");
    }

    for (Passenger passenger : passengers)
    {
      Objects.requireNonNull(passenger, "Passenger is required.");
      if (this.passengers.contains(passenger))
      {
        throw new IllegalArgumentException(
            "The same passenger cannot be added to one booking twice.");
      }
      if (passenger.getBooking() != null && passenger.getBooking() != this)
      {
        throw new IllegalArgumentException(
            "Passenger already belongs to another booking.");
      }
      passenger.setBooking(this);
      this.passengers.add(passenger);
    }
  }

  public int getBookingId()
  {
    return bookingId;
  }

  public void setBookingId(int bookingId)
  {
    if (bookingId <= 0)
    {
      throw new IllegalArgumentException("Booking id must be positive.");
    }
    this.bookingId = bookingId;
    if (bookingId >= nextBookingId)
    {
      nextBookingId = bookingId + 1;
    }
  }

  public LocalDateTime getBookingDate()
  {
    return bookingDate;
  }

  public void setBookingDate(LocalDateTime bookingDate)
  {
    this.bookingDate = Objects.requireNonNull(bookingDate,
        "Booking date is required.");
  }

  public double getTotalPrice()
  {
    recalculateTotalPrice();
    return totalPrice;
  }

  public void setTotalPrice(double totalPrice)
  {
    if (totalPrice < 0)
    {
      throw new IllegalArgumentException("Total price cannot be negative.");
    }
    this.totalPrice = totalPrice;
  }

  public Customer getCustomer()
  {
    return customer;
  }

  public void setCustomer(Customer customer)
  {
    this.customer = Objects.requireNonNull(customer, "Customer is required.");
  }

  public Flight getFlight()
  {
    return flight;
  }

  public void setFlight(Flight flight)
  {
    this.flight = Objects.requireNonNull(flight, "Flight is required.");
  }

  public List<Passenger> getPassengers()
  {
    return Collections.unmodifiableList(passengers);
  }

  public boolean isCancelled()
  {
    return cancelled;
  }

  @Override public String toString()
  {
    return "Booking #" + bookingId + " for " + flight.getFlightNumber();
  }
}
