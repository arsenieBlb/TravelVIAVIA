package client.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import client.model.Booking;
import client.model.BookingObserver;
import client.model.Customer;
import client.model.Model;
import client.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyBookingsViewModel implements BookingObserver
{
  private final Model model;
  private final ObservableList<Booking> bookings =
      FXCollections.observableArrayList();
  private final StringProperty customerName = new SimpleStringProperty("");
  private final Set<Integer> cancelledBookingIds = new HashSet<>();

  public MyBookingsViewModel(Model model)
  {
    this.model = model;
    this.model.addPropertyChangeListener(event -> {
      if ("bookings".equals(event.getPropertyName()))
      {
        javafx.application.Platform.runLater(this::refresh);
      }
    });
    refresh();
  }

  public void refresh()
  {
    User user = model.getLoggedInUser();
    if (user instanceof Customer customer)
    {
      customerName.set(customer.getFullName());
    }
    else
    {
      customerName.set("Customer");
      bookings.clear();
      return;
    }
    javafx.concurrent.Task<List<Booking>> loadTask = new javafx.concurrent.Task<>()
    {
      @Override
      protected List<Booking> call()
      {
        return model.getUserBookings();
      }
    };

    loadTask.setOnSucceeded(event -> {
      List<Booking> currentBookings = loadTask.getValue().stream()
          .filter(booking -> !cancelledBookingIds.contains(booking.getBookingId()))
          .collect(Collectors.toList());
      bookings.setAll(currentBookings);

      // register as observer on each booking so we get state change updates
      for (Booking booking : bookings)
      {
        booking.addObserver(this);
      }
    });

    loadTask.setOnFailed(event -> {
      Throwable e = loadTask.getException();
      if (e != null) {
          System.err.println("Could not load bookings: " + e.getMessage());
      }
    });

    Thread thread = new Thread(loadTask);
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public void onBookingStateChanged(Booking booking, String oldState, String newState)
  {
    javafx.application.Platform.runLater(this::refresh);
  }

  public Booking addBookingById(String bookingCode, String passengerLastName)
  {
    int bookingId = parseBookingId(bookingCode);
    String lastName = parsePassengerLastName(passengerLastName);
    Booking booking = model.addBookingToCurrentUserById(bookingId, lastName);
    refresh();
    return booking;
  }

  public void cancelBooking(Booking booking)
  {
    model.cancelBooking(booking);
    cancelledBookingIds.add(booking.getBookingId());
    refresh();
  }

  public void removeBookingFromCurrentUser(Booking booking)
  {
    if (booking == null)
    {
      return;
    }
    model.removeBookingFromCurrentUser(booking.getBookingId());
    bookings.remove(booking);
    refresh();
  }

  public String getCustomerLastName()
  {
    User user = model.getLoggedInUser();
    if (user instanceof Customer customer)
    {
      return customer.getLastName();
    }
    return "";
  }

  private int parseBookingId(String bookingCode)
  {
    if (bookingCode == null || bookingCode.isBlank())
    {
      throw new IllegalArgumentException("Booking code is required.");
    }

    String digitsOnly = bookingCode.replaceAll("[^0-9]", "");
    if (digitsOnly.isBlank())
    {
      throw new IllegalArgumentException(
          "Booking code must contain a number.");
    }
    return Integer.parseInt(digitsOnly);
  }

  private String parsePassengerLastName(String passengerLastName)
  {
    if (passengerLastName == null || passengerLastName.isBlank())
    {
      throw new IllegalArgumentException("Passenger last name is required.");
    }
    return passengerLastName.trim();
  }

  public ObservableList<Booking> getBookings()
  {
    return bookings;
  }

  public StringProperty customerNameProperty()
  {
    return customerName;
  }
}


