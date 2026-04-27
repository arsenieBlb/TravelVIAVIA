package model;

import java.time.LocalDate;
import java.util.Objects;

public class SearchCriteria
{
  private City departureCity;
  private City arrivalCity;
  private LocalDate departureDate;
  private int passengerCount;
  private SeatClass seatClass;

  public SearchCriteria()
  {
    passengerCount = 1;
    seatClass = SeatClass.Economy;
  }

  public SearchCriteria(City departureCity, City arrivalCity,
      LocalDate departureDate, int passengerCount, SeatClass seatClass)
  {
    setDepartureCity(departureCity);
    setArrivalCity(arrivalCity);
    setDepartureDate(departureDate);
    setPassengerCount(passengerCount);
    setSeatClass(seatClass);
  }

  public City getDepartureCity()
  {
    return departureCity;
  }

  public void setDepartureCity(City departureCity)
  {
    this.departureCity = departureCity;
  }

  public City getArrivalCity()
  {
    return arrivalCity;
  }

  public void setArrivalCity(City arrivalCity)
  {
    this.arrivalCity = arrivalCity;
  }

  public LocalDate getDepartureDate()
  {
    return departureDate;
  }

  public void setDepartureDate(LocalDate departureDate)
  {
    this.departureDate = departureDate;
  }

  public int getPassengerCount()
  {
    return passengerCount;
  }

  public void setPassengerCount(int passengerCount)
  {
    if (passengerCount <= 0)
    {
      throw new IllegalArgumentException("Passenger count must be positive.");
    }
    this.passengerCount = passengerCount;
  }

  public SeatClass getSeatClass()
  {
    return seatClass;
  }

  public void setSeatClass(SeatClass seatClass)
  {
    this.seatClass = Objects.requireNonNull(seatClass,
        "Seat class is required.");
  }
}
