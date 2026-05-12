package servermediator;

import model.Booking;
import model.Carrier;
import model.City;
import model.Flight;
import model.LuggageType;
import model.Model;
import model.ModelManager;
import model.Passenger;
import model.Plane;
import model.SearchCriteria;
import model.Seat;
import model.User;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class ServerSessionModel implements Model
{
  private final ModelManager sharedModel;
  private final PropertyChangeSupport support;
  private User currentUser;

  public ServerSessionModel(ModelManager sharedModel)
  {
    this.sharedModel = sharedModel;
    this.support = new PropertyChangeSupport(this);
  }

  @Override public List<Flight> searchFlights(SearchCriteria criteria)
  {
    synchronized (sharedModel)
    {
      return sharedModel.searchFlights(criteria);
    }
  }

  @Override public Flight getFlightDetails(int flightId)
  {
    synchronized (sharedModel)
    {
      return sharedModel.getFlightDetails(flightId);
    }
  }

  @Override public boolean login(String email, String password)
  {
    User oldUser = currentUser;
    synchronized (sharedModel)
    {
      currentUser = sharedModel.authenticate(email, password);
    }
    support.firePropertyChange("currentUser", oldUser, currentUser);
    return currentUser != null;
  }

  @Override public boolean register(String firstName, String lastName,
      String email, String password)
  {
    synchronized (sharedModel)
    {
      return sharedModel.register(firstName, lastName, email, password);
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
    synchronized (sharedModel)
    {
      return sharedModel.createBooking(currentUser, flight, passengers,
          selectedSeats);
    }
  }

  @Override public void cancelBooking(Booking booking)
  {
    synchronized (sharedModel)
    {
      sharedModel.cancelBooking(currentUser, booking);
    }
  }

  @Override public List<Booking> getAllBookings()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getAllBookings(currentUser);
    }
  }

  @Override public List<Booking> getUserBookings()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getUserBookings(currentUser);
    }
  }

  @Override public Booking addBookingToCurrentUserById(int bookingId,
      String passengerLastName)
  {
    synchronized (sharedModel)
    {
      return sharedModel.addBookingToCurrentUserById(currentUser, bookingId,
          passengerLastName);
    }
  }

  @Override public void addFlight(Flight flight)
  {
    synchronized (sharedModel)
    {
      sharedModel.addFlight(currentUser, flight);
    }
  }

  @Override public void removeFlight(Flight flight)
  {
    synchronized (sharedModel)
    {
      sharedModel.removeFlight(currentUser, flight);
    }
  }

  @Override public List<LuggageType> getLuggageTypes()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getLuggageTypes();
    }
  }

  @Override public List<City> getAllCities()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getAllCities();
    }
  }

  @Override public List<City> getCities()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getCities();
    }
  }

  @Override public List<Plane> getPlanes()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getPlanes();
    }
  }

  @Override public List<Carrier> getCarriers()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getCarriers();
    }
  }

  @Override public List<Flight> getAllFlights()
  {
    synchronized (sharedModel)
    {
      return sharedModel.getAllFlights();
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
