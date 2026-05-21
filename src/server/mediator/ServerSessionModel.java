package server.mediator;

import client.model.Booking;
import client.model.Carrier;
import client.model.City;
import client.model.Flight;
import client.model.LuggageType;
import client.model.Model;
import server.model.ModelManager;
import client.model.Passenger;
import client.model.Plane;
import client.model.SearchCriteria;
import client.model.Seat;
import client.model.User;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class ServerSessionModel implements Model
{
  private final ModelManager serverModel;
  private final PropertyChangeSupport support;
  private User currentUser;

  public ServerSessionModel(ModelManager serverModel)
  {
    this.serverModel = serverModel;
    this.support = new PropertyChangeSupport(this);
  }

  @Override public List<Flight> searchFlights(SearchCriteria criteria)
  {
    synchronized (serverModel)
    {
      return serverModel.searchFlights(criteria);
    }
  }

  @Override public Flight getFlightDetails(int flightId)
  {
    synchronized (serverModel)
    {
      return serverModel.getFlightDetails(flightId);
    }
  }

  @Override public boolean login(String email, String password)
  {
    User oldUser = currentUser;
    synchronized (serverModel)
    {
      currentUser = serverModel.authenticate(email, password);
    }
    support.firePropertyChange("currentUser", oldUser, currentUser);
    return currentUser != null;
  }

  @Override public boolean register(String firstName, String lastName,
      String email, String password)
  {
    synchronized (serverModel)
    {
      return serverModel.register(firstName, lastName, email, password);
    }
  }

  @Override public void logout()
  {
    User oldUser = currentUser;
    if (currentUser != null)
    {
      currentUser.logout();
    }
    currentUser = null;
    support.firePropertyChange("currentUser", oldUser, null);
  }

  @Override public User getLoggedInUser()
  {
    return currentUser;
  }

  @Override public Booking createBooking(Flight flight,
      List<Passenger> passengers)
  {
    return createBooking(flight, passengers, List.of());
  }

  @Override public Booking createBooking(Flight flight,
      List<Passenger> passengers, List<Seat> selectedSeats)
  {
    return createBooking(flight, passengers, selectedSeats, null, null);
  }

  @Override public Booking createBooking(Flight flight,
      List<Passenger> passengers, List<Seat> selectedSeats,
      Flight returnFlight, List<Seat> returnSeats)
  {
    synchronized (serverModel)
    {
      return serverModel.createBookingInternal(currentUser, flight, passengers,
          selectedSeats, returnFlight, returnSeats);
    }
  }

  @Override public void cancelBooking(Booking booking)
  {
    synchronized (serverModel)
    {
      serverModel.cancelBooking(currentUser, booking);
    }
  }

  @Override public List<Booking> getAllBookings()
  {
    synchronized (serverModel)
    {
      return serverModel.getAllBookings(currentUser);
    }
  }

  @Override public List<Booking> getUserBookings()
  {
    synchronized (serverModel)
    {
      return serverModel.getUserBookings(currentUser);
    }
  }

  @Override public Booking addBookingToCurrentUserById(int bookingId,
      String passengerLastName)
  {
    synchronized (serverModel)
    {
      return serverModel.addBookingToCurrentUserById(currentUser, bookingId,
          passengerLastName);
    }
  }

  @Override public void removeBookingFromCurrentUser(int bookingId)
  {
    synchronized (serverModel)
    {
      serverModel.removeBookingFromCurrentUser(currentUser, bookingId);
    }
  }

  @Override public void addFlight(Flight flight)
  {
    synchronized (serverModel)
    {
      serverModel.addFlight(currentUser, flight);
    }
  }

  @Override public void removeFlight(Flight flight)
  {
    synchronized (serverModel)
    {
      serverModel.removeFlight(currentUser, flight);
    }
  }

  @Override public void editFlight(Flight flight)
  {
    synchronized (serverModel)
    {
      serverModel.editFlight(currentUser, flight);
    }
  }

  @Override public List<LuggageType> getLuggageTypes()
  {
    synchronized (serverModel)
    {
      return serverModel.getLuggageTypes();
    }
  }

  @Override public List<City> getAllCities()
  {
    synchronized (serverModel)
    {
      return serverModel.getAllCities();
    }
  }

  @Override public List<City> getCities()
  {
    synchronized (serverModel)
    {
      return serverModel.getCities();
    }
  }

  @Override public List<Plane> getPlanes()
  {
    synchronized (serverModel)
    {
      return serverModel.getPlanes();
    }
  }

  @Override public List<Carrier> getCarriers()
  {
    synchronized (serverModel)
    {
      return serverModel.getCarriers();
    }
  }

  @Override public List<Flight> getAllFlights()
  {
    synchronized (serverModel)
    {
      return serverModel.getAllFlights();
    }
  }

  public Flight findLoadedFlightById(int flightId)
  {
    synchronized (serverModel)
    {
      return serverModel.findLoadedFlightById(flightId);
    }
  }

  @Override public boolean isLoggedIn()
  {
    return currentUser != null;
  }

  @Override public void addPropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.addPropertyChangeListener(listener);
  }

  @Override public void removePropertyChangeListener(
      PropertyChangeListener listener)
  {
    support.removePropertyChangeListener(listener);
  }
}


