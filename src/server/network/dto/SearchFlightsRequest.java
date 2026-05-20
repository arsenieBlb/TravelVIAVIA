package server.network.dto;

public class SearchFlightsRequest
{
  public Integer departureCityId;
  public Integer arrivalCityId;
  public String departureDate;
  public int passengerCount;
  public String seatClass;
  public boolean directOnly;

  public SearchFlightsRequest()
  {
  }

  public SearchFlightsRequest(Integer departureCityId, Integer arrivalCityId,
      String departureDate, int passengerCount, String seatClass)
  {
    this(departureCityId, arrivalCityId, departureDate, passengerCount,
        seatClass, false);
  }

  public SearchFlightsRequest(Integer departureCityId, Integer arrivalCityId,
      String departureDate, int passengerCount, String seatClass,
      boolean directOnly)
  {
    this.departureCityId = departureCityId;
    this.arrivalCityId = arrivalCityId;
    this.departureDate = departureDate;
    this.passengerCount = passengerCount;
    this.seatClass = seatClass;
    this.directOnly = directOnly;
  }
}


