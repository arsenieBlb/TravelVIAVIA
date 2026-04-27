package model;

import java.util.List;
import java.util.Objects;

public class Anonymous
{
  private FlightSearchService flightSearchService;

  public Anonymous(FlightSearchService flightSearchService)
  {
    setFlightSearchService(flightSearchService);
  }

  public List<Flight> searchFlights(SearchCriteria criteria)
  {
    return flightSearchService.searchFlights(criteria);
  }

  public Flight viewFlightDetails(int flightId)
  {
    return flightSearchService.viewFlightDetails(flightId);
  }

  public FlightSearchService getFlightSearchService()
  {
    return flightSearchService;
  }

  public void setFlightSearchService(FlightSearchService flightSearchService)
  {
    this.flightSearchService = Objects.requireNonNull(flightSearchService,
        "Flight search service is required.");
  }
  //
}
