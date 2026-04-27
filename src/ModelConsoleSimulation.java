import model.*;

import java.time.LocalDateTime;
import java.util.List;

public class ModelConsoleSimulation
{
  public static void main(String[] args)
  {
    City copenhagen = new City(1, "Copenhagen", "Denmark");
    City london = new City(2, "London", "United Kingdom");
    System.out.println("1. Cities: " + copenhagen + " and " + london);

    Carrier carrier = new Carrier(1, "Scandinavian Airlines");
    PlaneType planeType = new PlaneType(1, "Airbus", "A320", 8, 4);
    Plane plane = new Plane(1, "OY-SIM", PlaneStatus.Active, planeType,
        carrier);
    addSimulationSeats(plane);
    System.out.println("2. Carrier and plane: " + carrier + ", " + plane);
    System.out.println("   Total seats: " + plane.getTotalSeats());

    LocalDateTime departure = LocalDateTime.now().plusDays(7).withHour(9)
        .withMinute(30).withSecond(0).withNano(0);
    LocalDateTime arrival = departure.plusHours(2);
    Flight flight = new Flight(1, "SK100", departure, arrival, 120.0, carrier,
        plane, copenhagen, london);
    System.out.println("3. Flight created: " + flight);
    System.out.println("   Flight departure date: "
        + flight.getDepartureTime().toLocalDate());
    System.out.println("   Flight departure time: "
        + flight.getDepartureTime().toLocalTime());
    System.out.println("   Flight arrival time: "
        + flight.getArrivalTime().toLocalTime());

    FlightSearchService flightSearchService = new FlightSearchService();
    flightSearchService.registerFlight(flight);

    SearchCriteria criteria = new SearchCriteria(copenhagen, london,
        departure.toLocalDate(), 1, SeatClass.Economy);
    System.out.println("   Search departure date selected by user: "
        + criteria.getDepartureDate());
    System.out.println("   The search compares that date with the flight date: "
        + flight.getDepartureTime().toLocalDate());


    Customer customer = new Customer(1, "Allan@my.via.dk", "IulianSugePula",
        "Artem", "NuIiStiuFamilia", flightSearchService);
    List<Flight> customerResults = customer.searchFlights(criteria);
    System.out.println("4. Customer search found " + customerResults.size()
        + " flight(s).");
    System.out.println("   Customer details: "
        + customer.viewFlightDetails(flight));
    System.out.println("   Economy seats before assignment: "
        + flight.getAvailableSeatsByClass(SeatClass.Economy));

    Passenger passenger = new Passenger(customer.getUserId(),customer.getFirstName(),customer.getLastName());
    Booking booking = customer.createBooking(flight, List.of(passenger));
    Seat selectedSeat = flight.getAvailableSeatsByClass(SeatClass.Economy).get(0);
    SeatAssignment seatAssignment = new SeatAssignment(1, passenger,
        selectedSeat, flight);
    System.out.println("5. Seat assigned: " + seatAssignment);
    System.out.println("   Economy seats after assignment: "
        + flight.getAvailableSeatsByClass(SeatClass.Economy));

    LuggageType checkedBag = new LuggageType(1, "Checked Bag",
        "Checked luggage up to 23 kg", 30.0);
    PassengerLuggage luggage = new PassengerLuggage(1, 2, passenger,
        checkedBag);
    System.out.println("6. Luggage added: " + luggage);
    System.out.println("   Luggage extra price: "
        + luggage.getTotalExtraPrice());

    System.out.println("7. Booking summary:");
    System.out.println(booking.getBookingSummary());

    System.out.println("8. Double-booking validation:");
    try
    {
      Passenger secondPassenger = new Passenger(2, "Sofia", "Larsen");
      customer.createBooking(flight, List.of(secondPassenger));
      new SeatAssignment(2, secondPassenger, selectedSeat, flight);
      System.out.println("   ERROR: Duplicate seat assignment was allowed.");
    }
    catch (IllegalArgumentException exception)
    {
      System.out.println("   Rejected duplicate seat assignment: "
          + exception.getMessage());
    }

    System.out.println("9. Cancellation:");
    customer.cancelBooking(booking);
    System.out.println("   Booking cancelled: " + booking.isCancelled());
    System.out.println("   Passenger seat after cancellation: "
        + passenger.getSeatAssignment());
    System.out.println("   Economy seats after cancellation: "
        + flight.getAvailableSeatsByClass(SeatClass.Economy));
  }

  private static void addSimulationSeats(Plane plane)
  {
    plane.addSeat(new Seat(1, "1A", 1, SeatClass.Business));
    plane.addSeat(new Seat(2, "1B", 1, SeatClass.Business));
    plane.addSeat(new Seat(3, "1C", 1, SeatClass.Business));
    plane.addSeat(new Seat(4, "1D", 1, SeatClass.Business));
    plane.addSeat(new Seat(5, "2A", 2, SeatClass.Economy));
    plane.addSeat(new Seat(6, "2B", 2, SeatClass.Economy));
    plane.addSeat(new Seat(7, "2C", 2, SeatClass.Economy));
    plane.addSeat(new Seat(8, "2D", 2, SeatClass.Economy));
    plane.addSeat(new Seat(9, "3A", 3, SeatClass.Economy));
    plane.addSeat(new Seat(10, "3B", 3, SeatClass.Economy));
    plane.addSeat(new Seat(11, "3C", 3, SeatClass.Economy));
    plane.addSeat(new Seat(12, "3D", 3, SeatClass.Economy));
    //try
  }
}
