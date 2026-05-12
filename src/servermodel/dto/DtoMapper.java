package servermodel.dto;

import model.ActiveState;
import model.Admin;
import model.Booking;
import model.BusinessClass;
import model.Carrier;
import model.City;
import model.ConnectingFlight;
import model.Customer;
import model.EconomyClass;
import model.Flight;
import model.FlightSearchService;
import model.InactiveState;
import model.LuggageType;
import model.MaintenanceState;
import model.Passenger;
import model.PassengerLuggage;
import model.Plane;
import model.PlaneState;
import model.PlaneType;
import model.SearchCriteria;
import model.Seat;
import model.SeatAssignment;
import model.SeatClass;
import model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DtoMapper
{
  private DtoMapper()
  {
  }

  public static CityDto toDto(City city)
  {
    if (city == null)
    {
      return null;
    }
    CityDto dto = new CityDto();
    dto.cityId = city.getCityId();
    dto.cityName = city.getCityName();
    dto.country = city.getCountry();
    return dto;
  }

  public static City fromDto(CityDto dto)
  {
    if (dto == null)
    {
      return null;
    }
    return new City(dto.cityId, dto.cityName, dto.country);
  }

  public static CarrierDto toDto(Carrier carrier)
  {
    if (carrier == null)
    {
      return null;
    }
    CarrierDto dto = new CarrierDto();
    dto.carrierId = carrier.getCarrierId();
    dto.name = carrier.getName();
    return dto;
  }

  public static Carrier fromDto(CarrierDto dto)
  {
    if (dto == null)
    {
      return null;
    }
    return new Carrier(dto.carrierId, dto.name);
  }

  public static LuggageTypeDto toDto(LuggageType luggageType)
  {
    if (luggageType == null)
    {
      return null;
    }
    LuggageTypeDto dto = new LuggageTypeDto();
    dto.luggageTypeId = luggageType.getLuggageTypeId();
    dto.name = luggageType.getName();
    dto.description = luggageType.getDescription();
    dto.extraPrice = luggageType.getExtraPrice();
    return dto;
  }

  public static LuggageType fromDto(LuggageTypeDto dto)
  {
    if (dto == null)
    {
      return null;
    }
    return new LuggageType(dto.luggageTypeId, dto.name, dto.description,
        dto.extraPrice);
  }

  public static SeatDto toDto(Seat seat)
  {
    if (seat == null)
    {
      return null;
    }
    SeatDto dto = new SeatDto();
    dto.seatId = seat.getSeatId();
    dto.seatNumber = seat.getSeatNumber();
    dto.rowNumber = seat.getRowNumber();
    dto.seatClass = seat.getSeatClass().getClassName();
    return dto;
  }

  public static Seat fromDto(SeatDto dto)
  {
    if (dto == null)
    {
      return null;
    }
    return new Seat(dto.seatId, dto.seatNumber, dto.rowNumber,
        seatClassFromName(dto.seatClass));
  }

  public static PlaneDto toDto(Plane plane)
  {
    if (plane == null)
    {
      return null;
    }
    PlaneDto dto = new PlaneDto();
    dto.planeId = plane.getPlaneId();
    dto.registrationNumber = plane.getRegistrationNumber();
    dto.status = plane.getStatusName();
    dto.carrier = toDto(plane.getCarrier());

    PlaneType planeType = plane.getPlaneType();
    dto.planeTypeId = planeType.getPlaneTypeId();
    dto.typeName = planeType.getTypeName();
    dto.model = planeType.getModel();
    dto.numberOfColumns = planeType.getNumberOfColumns();
    dto.numberOfEconomySeats = planeType.getNumberOfEconomySeats();
    dto.numberOfBusinessSeats = planeType.getNumberOfBusinessSeats();

    for (Seat seat : plane.getSeats())
    {
      dto.seats.add(toDto(seat));
    }
    return dto;
  }

  public static Plane fromDto(PlaneDto dto)
  {
    return fromDto(dto, dto == null ? null : fromDto(dto.carrier));
  }

  private static Plane fromDto(PlaneDto dto, Carrier carrier)
  {
    if (dto == null)
    {
      return null;
    }
    PlaneType planeType = new PlaneType(dto.planeTypeId, dto.typeName,
        dto.model, dto.numberOfColumns, dto.numberOfEconomySeats,
        dto.numberOfBusinessSeats);
    Plane plane = new Plane(dto.planeId, dto.registrationNumber,
        planeStateFromName(dto.status), planeType, carrier);
    if (dto.seats != null)
    {
      for (SeatDto seatDto : dto.seats)
      {
        plane.addSeat(fromDto(seatDto));
      }
    }
    return plane;
  }

  public static FlightDto toDto(Flight flight)
  {
    if (flight == null)
    {
      return null;
    }
    FlightDto dto = new FlightDto();
    dto.flightId = flight.getFlightId();
    dto.flightNumber = flight.getFlightNumber();
    dto.departureTime = flight.getDepartureTime().toString();
    dto.arrivalTime = flight.getArrivalTime().toString();
    dto.basePrice = flight.getBasePrice();
    dto.carrier = toDto(flight.getCarrier());
    dto.plane = toDto(flight.getPlane());
    dto.departureCity = toDto(flight.getDepartureCity());
    dto.arrivalCity = toDto(flight.getArrivalCity());

    for (Seat seat : flight.getAvailableSeats())
    {
      dto.availableSeatIds.add(seat.getSeatId());
    }

    if (flight instanceof ConnectingFlight connectingFlight)
    {
      dto.connecting = true;
      dto.firstSegment = toDto(connectingFlight.getFirstSegment());
      dto.secondSegment = toDto(connectingFlight.getSecondSegment());
    }
    return dto;
  }

  public static Flight fromDto(FlightDto dto)
  {
    if (dto == null)
    {
      return null;
    }
    if (dto.connecting && dto.firstSegment != null && dto.secondSegment != null)
    {
      return new ConnectingFlight(fromDto(dto.firstSegment),
          fromDto(dto.secondSegment));
    }

    Carrier carrier = fromDto(dto.carrier);
    Plane plane = fromDto(dto.plane, carrier);
    Flight flight = new Flight(dto.flightId, dto.flightNumber,
        LocalDateTime.parse(dto.departureTime),
        LocalDateTime.parse(dto.arrivalTime), dto.basePrice, carrier, plane,
        fromDto(dto.departureCity), fromDto(dto.arrivalCity));
    markOccupiedSeats(flight, dto.availableSeatIds);
    return flight;
  }

  public static UserDto toDto(User user)
  {
    if (user == null)
    {
      return null;
    }
    UserDto dto = new UserDto();
    dto.userId = user.getUserId();
    dto.email = user.getEmail();
    dto.password = user.getPassword();
    dto.loggedIn = user.isLoggedIn();
    if (user instanceof Customer customer)
    {
      dto.type = "Customer";
      dto.firstName = customer.getFirstName();
      dto.lastName = customer.getLastName();
    }
    else if (user instanceof Admin)
    {
      dto.type = "Admin";
    }
    else
    {
      dto.type = "User";
    }
    return dto;
  }

  public static User fromDto(UserDto dto)
  {
    if (dto == null)
    {
      return null;
    }
    if ("Admin".equalsIgnoreCase(dto.type))
    {
      return new Admin(dto.userId, dto.email, dto.password,
          new FlightSearchService());
    }
    return customerFromDto(dto);
  }

  public static BookingDto toDto(Booking booking)
  {
    if (booking == null)
    {
      return null;
    }
    BookingDto dto = new BookingDto();
    dto.bookingId = booking.getBookingId();
    dto.bookingDate = booking.getBookingDate().toString();
    dto.totalPrice = booking.getTotalPrice();
    dto.customer = toDto(booking.getCustomer());
    dto.flight = toDto(booking.getFlight());
    dto.cancelled = booking.isCancelled();
    for (Passenger passenger : booking.getPassengers())
    {
      dto.passengers.add(toDto(passenger));
    }
    return dto;
  }

  public static Booking fromDto(BookingDto dto)
  {
    if (dto == null)
    {
      return null;
    }
    Customer customer = customerFromDto(dto.customer);
    Flight flight = fromDto(dto.flight);
    List<Passenger> passengers = passengersFromDtos(dto.passengers);
    Booking booking = new Booking(dto.bookingId,
        parseDateTime(dto.bookingDate), customer, flight, passengers);
    applySeatAssignments(booking, dto.passengers);
    return booking;
  }

  public static PassengerDto toDto(Passenger passenger)
  {
    if (passenger == null)
    {
      return null;
    }
    PassengerDto dto = new PassengerDto();
    dto.passengerId = passenger.getPassengerId();
    dto.firstName = passenger.getFirstName();
    dto.lastName = passenger.getLastName();

    for (PassengerLuggage luggage : passenger.getPassengerLuggage())
    {
      PassengerDto.LuggageDto luggageDto = new PassengerDto.LuggageDto();
      luggageDto.passengerLuggageId = luggage.getPassengerLuggageId();
      luggageDto.quantity = luggage.getQuantity();
      luggageDto.luggageType = toDto(luggage.getLuggageType());
      dto.luggage.add(luggageDto);
    }

    for (SeatAssignment seatAssignment : passenger.getSeatAssignments())
    {
      PassengerDto.SeatAssignmentDto assignmentDto =
          new PassengerDto.SeatAssignmentDto();
      assignmentDto.flightId = seatAssignment.getFlight().getFlightId();
      assignmentDto.seat = toDto(seatAssignment.getSeat());
      dto.seatAssignments.add(assignmentDto);
    }
    return dto;
  }

  public static Passenger fromDto(PassengerDto dto)
  {
    return fromDto(dto, 1);
  }

  public static Passenger fromDto(PassengerDto dto, int fallbackId)
  {
    if (dto == null)
    {
      return null;
    }
    Passenger passenger = new Passenger(positive(dto.passengerId, fallbackId),
        dto.firstName, dto.lastName);
    if (dto.luggage != null)
    {
      int fallbackLuggageId = 1;
      for (PassengerDto.LuggageDto luggageDto : dto.luggage)
      {
        if (luggageDto != null && luggageDto.luggageType != null
            && luggageDto.quantity > 0)
        {
          passenger.addPassengerLuggage(new PassengerLuggage(
              positive(luggageDto.passengerLuggageId, fallbackLuggageId++),
              luggageDto.quantity, fromDto(luggageDto.luggageType)));
        }
      }
    }
    return passenger;
  }

  public static SearchFlightsRequest toDto(SearchCriteria criteria)
  {
    Integer departureCityId = criteria.getDepartureCity() == null ? null
        : criteria.getDepartureCity().getCityId();
    Integer arrivalCityId = criteria.getArrivalCity() == null ? null
        : criteria.getArrivalCity().getCityId();
    String departureDate = criteria.getDepartureDate() == null ? null
        : criteria.getDepartureDate().toString();
    String seatClass = criteria.getSeatClass() == null ? "Economy"
        : criteria.getSeatClass().getClassName();
    return new SearchFlightsRequest(departureCityId, arrivalCityId,
        departureDate, criteria.getPassengerCount(), seatClass);
  }

  public static SearchCriteria toCriteria(SearchFlightsRequest request,
      List<City> cities)
  {
    SearchCriteria criteria = new SearchCriteria();
    if (request == null)
    {
      return criteria;
    }
    criteria.setDepartureCity(findCity(cities, request.departureCityId));
    criteria.setArrivalCity(findCity(cities, request.arrivalCityId));
    if (request.departureDate != null && !request.departureDate.isBlank())
    {
      criteria.setDepartureDate(LocalDate.parse(request.departureDate));
    }
    criteria.setPassengerCount(request.passengerCount <= 0 ? 1
        : request.passengerCount);
    criteria.setSeatClass(seatClassFromName(request.seatClass));
    return criteria;
  }

  public static List<CityDto> cityDtos(List<City> cities)
  {
    List<CityDto> dtos = new ArrayList<>();
    if (cities != null)
    {
      for (City city : cities)
      {
        dtos.add(toDto(city));
      }
    }
    return dtos;
  }

  public static List<City> citiesFromDtos(List<CityDto> dtos)
  {
    List<City> cities = new ArrayList<>();
    if (dtos != null)
    {
      for (CityDto dto : dtos)
      {
        cities.add(fromDto(dto));
      }
    }
    return cities;
  }

  public static List<FlightDto> flightDtos(List<Flight> flights)
  {
    List<FlightDto> dtos = new ArrayList<>();
    if (flights != null)
    {
      for (Flight flight : flights)
      {
        dtos.add(toDto(flight));
      }
    }
    return dtos;
  }

  public static List<Flight> flightsFromDtos(List<FlightDto> dtos)
  {
    List<Flight> flights = new ArrayList<>();
    if (dtos != null)
    {
      for (FlightDto dto : dtos)
      {
        flights.add(fromDto(dto));
      }
    }
    return flights;
  }

  public static List<BookingDto> bookingDtos(List<Booking> bookings)
  {
    List<BookingDto> dtos = new ArrayList<>();
    if (bookings != null)
    {
      for (Booking booking : bookings)
      {
        dtos.add(toDto(booking));
      }
    }
    return dtos;
  }

  public static List<Booking> bookingsFromDtos(List<BookingDto> dtos)
  {
    List<Booking> bookings = new ArrayList<>();
    if (dtos != null)
    {
      for (BookingDto dto : dtos)
      {
        bookings.add(fromDto(dto));
      }
    }
    return bookings;
  }

  public static List<PassengerDto> passengerDtos(List<Passenger> passengers)
  {
    List<PassengerDto> dtos = new ArrayList<>();
    if (passengers != null)
    {
      for (Passenger passenger : passengers)
      {
        dtos.add(toDto(passenger));
      }
    }
    return dtos;
  }

  public static List<Passenger> passengersFromDtos(List<PassengerDto> dtos)
  {
    List<Passenger> passengers = new ArrayList<>();
    if (dtos != null)
    {
      int fallbackId = 1000;
      for (PassengerDto dto : dtos)
      {
        passengers.add(fromDto(dto, fallbackId++));
      }
    }
    return passengers;
  }

  public static List<SeatDto> seatDtos(List<Seat> seats)
  {
    List<SeatDto> dtos = new ArrayList<>();
    if (seats != null)
    {
      for (Seat seat : seats)
      {
        dtos.add(toDto(seat));
      }
    }
    return dtos;
  }

  public static List<Seat> seatsFromDtos(List<SeatDto> dtos)
  {
    List<Seat> seats = new ArrayList<>();
    if (dtos != null)
    {
      for (SeatDto dto : dtos)
      {
        seats.add(fromDto(dto));
      }
    }
    return seats;
  }

  public static List<LuggageTypeDto> luggageTypeDtos(
      List<LuggageType> luggageTypes)
  {
    List<LuggageTypeDto> dtos = new ArrayList<>();
    if (luggageTypes != null)
    {
      for (LuggageType luggageType : luggageTypes)
      {
        dtos.add(toDto(luggageType));
      }
    }
    return dtos;
  }

  public static List<LuggageType> luggageTypesFromDtos(
      List<LuggageTypeDto> dtos)
  {
    List<LuggageType> luggageTypes = new ArrayList<>();
    if (dtos != null)
    {
      for (LuggageTypeDto dto : dtos)
      {
        luggageTypes.add(fromDto(dto));
      }
    }
    return luggageTypes;
  }

  public static List<PlaneDto> planeDtos(List<Plane> planes)
  {
    List<PlaneDto> dtos = new ArrayList<>();
    if (planes != null)
    {
      for (Plane plane : planes)
      {
        dtos.add(toDto(plane));
      }
    }
    return dtos;
  }

  public static List<Plane> planesFromDtos(List<PlaneDto> dtos)
  {
    List<Plane> planes = new ArrayList<>();
    if (dtos != null)
    {
      for (PlaneDto dto : dtos)
      {
        planes.add(fromDto(dto));
      }
    }
    return planes;
  }

  public static List<CarrierDto> carrierDtos(List<Carrier> carriers)
  {
    List<CarrierDto> dtos = new ArrayList<>();
    if (carriers != null)
    {
      for (Carrier carrier : carriers)
      {
        dtos.add(toDto(carrier));
      }
    }
    return dtos;
  }

  public static List<Carrier> carriersFromDtos(List<CarrierDto> dtos)
  {
    List<Carrier> carriers = new ArrayList<>();
    if (dtos != null)
    {
      for (CarrierDto dto : dtos)
      {
        carriers.add(fromDto(dto));
      }
    }
    return carriers;
  }

  private static Customer customerFromDto(UserDto dto)
  {
    if (dto == null)
    {
      return new Customer(1, "unknown@example.com", "unknown", "Unknown",
          "Customer", new FlightSearchService());
    }
    String firstName = dto.firstName == null || dto.firstName.isBlank()
        ? "Unknown" : dto.firstName;
    String lastName = dto.lastName == null || dto.lastName.isBlank()
        ? "Customer" : dto.lastName;
    return new Customer(dto.userId, dto.email, dto.password, firstName,
        lastName, new FlightSearchService());
  }

  private static void applySeatAssignments(Booking booking,
      List<PassengerDto> passengerDtos)
  {
    if (booking == null || passengerDtos == null)
    {
      return;
    }
    List<Flight> segments = getSegments(booking.getFlight());
    List<Passenger> passengers = booking.getPassengers();
    for (int i = 0; i < passengerDtos.size() && i < passengers.size(); i++)
    {
      PassengerDto passengerDto = passengerDtos.get(i);
      Passenger passenger = passengers.get(i);
      if (passengerDto.seatAssignments == null)
      {
        continue;
      }
      for (PassengerDto.SeatAssignmentDto assignmentDto :
          passengerDto.seatAssignments)
      {
        Flight segment = findFlight(segments, assignmentDto.flightId);
        Seat seat = findSeat(segment, assignmentDto.seat == null ? 0
            : assignmentDto.seat.seatId);
        if (segment != null && seat != null)
        {
          SeatAssignment assignment = new SeatAssignment(segment, seat,
              passenger);
          passenger.addSeatAssignment(assignment);
          segment.markSeatOccupied(seat);
        }
      }
    }
  }

  private static void markOccupiedSeats(Flight flight,
      List<Integer> availableSeatIds)
  {
    if (flight == null || availableSeatIds == null)
    {
      return;
    }
    Set<Integer> availableIds = new HashSet<>(availableSeatIds);
    for (Seat seat : flight.getPlane().getSeats())
    {
      if (!availableIds.contains(seat.getSeatId()))
      {
        flight.markSeatOccupied(seat);
      }
    }
  }

  private static List<Flight> getSegments(Flight flight)
  {
    if (flight == null)
    {
      return Collections.emptyList();
    }
    List<Flight> segments = new ArrayList<>();
    if (flight instanceof ConnectingFlight connectingFlight)
    {
      segments.add(connectingFlight.getFirstSegment());
      segments.add(connectingFlight.getSecondSegment());
    }
    else
    {
      segments.add(flight);
    }
    return segments;
  }

  private static Flight findFlight(List<Flight> flights, int flightId)
  {
    for (Flight flight : flights)
    {
      if (flight.getFlightId() == flightId)
      {
        return flight;
      }
    }
    return null;
  }

  private static Seat findSeat(Flight flight, int seatId)
  {
    if (flight == null)
    {
      return null;
    }
    for (Seat seat : flight.getPlane().getSeats())
    {
      if (seat.getSeatId() == seatId)
      {
        return seat;
      }
    }
    return null;
  }

  private static City findCity(List<City> cities, Integer cityId)
  {
    if (cities == null || cityId == null)
    {
      return null;
    }
    for (City city : cities)
    {
      if (city.getCityId() == cityId)
      {
        return city;
      }
    }
    return null;
  }

  private static SeatClass seatClassFromName(String className)
  {
    if ("Business".equalsIgnoreCase(className))
    {
      return new BusinessClass();
    }
    return new EconomyClass();
  }

  private static PlaneState planeStateFromName(String status)
  {
    if ("Inactive".equalsIgnoreCase(status))
    {
      return new InactiveState();
    }
    if ("Maintenance".equalsIgnoreCase(status))
    {
      return new MaintenanceState();
    }
    return new ActiveState();
  }

  private static LocalDateTime parseDateTime(String value)
  {
    if (value == null || value.isBlank())
    {
      return LocalDateTime.now();
    }
    return LocalDateTime.parse(value);
  }

  private static int positive(int value, int fallback)
  {
    return value > 0 ? value : fallback;
  }
}
