package model;

import java.util.Objects;

public class PassengerLuggage
{
  private int passengerLuggageId;
  private int quantity;
  private Passenger passenger;
  private LuggageType luggageType;

  public PassengerLuggage(int passengerLuggageId, int quantity,
      LuggageType luggageType)
  {
    setPassengerLuggageId(passengerLuggageId);
    setQuantity(quantity);
    setLuggageType(luggageType);
  }

  public PassengerLuggage(int passengerLuggageId, int quantity,
      Passenger passenger, LuggageType luggageType)
  {
    this(passengerLuggageId, quantity, luggageType);
    Objects.requireNonNull(passenger, "Passenger is required.")
        .addPassengerLuggage(this);
  }

  public double getTotalExtraPrice()
  {
    return quantity * luggageType.getExtraPrice();
  }
  //

  void setPassengerInternal(Passenger passenger)
  {
    this.passenger = passenger;
  }

  public int getPassengerLuggageId()
  {
    return passengerLuggageId;
  }

  public void setPassengerLuggageId(int passengerLuggageId)
  {
    if (passengerLuggageId <= 0)
    {
      throw new IllegalArgumentException(
          "Passenger luggage id must be positive.");
    }
    this.passengerLuggageId = passengerLuggageId;
  }

  public int getQuantity()
  {
    return quantity;
  }

  public void setQuantity(int quantity)
  {
    if (quantity <= 0)
    {
      throw new IllegalArgumentException("Luggage quantity must be positive.");
    }
    this.quantity = quantity;
  }

  public Passenger getPassenger()
  {
    return passenger;
  }

  public void setPassenger(Passenger passenger)
  {
    Objects.requireNonNull(passenger, "Passenger is required.")
        .addPassengerLuggage(this);
  }

  public LuggageType getLuggageType()
  {
    return luggageType;
  }

  public void setLuggageType(LuggageType luggageType)
  {
    this.luggageType = Objects.requireNonNull(luggageType,
        "Luggage type is required.");
  }

  @Override public String toString()
  {
    return quantity + " x " + luggageType.getName();
  }
}
