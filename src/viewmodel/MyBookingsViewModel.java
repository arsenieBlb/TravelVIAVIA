package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Booking;
import model.Customer;
import model.Model;
import model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyBookingsViewModel
{
  private final Model model;
  private final ObservableList<Booking> bookings =
      FXCollections.observableArrayList();
  private final StringProperty customerName = new SimpleStringProperty("");
  private final Set<Integer> cancelledBookingIds = new HashSet<>();

  public MyBookingsViewModel(Model model)
  {
    this.model = model;
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
    List<Booking> currentBookings = model.getUserBookings().stream()
        .filter(booking -> !cancelledBookingIds.contains(booking.getBookingId()))
        .collect(Collectors.toList());
    bookings.setAll(currentBookings);
  }

  public Booking addBookingById(String bookingCode, String lastName)
  {
    int bookingId = parseBookingId(bookingCode);
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

  private int parseBookingId(String bookingCode)
  {
    if (bookingCode == null || bookingCode.isBlank())
    {
      throw new IllegalArgumentException("Booking ID is required.");
    }

    String digitsOnly = bookingCode.replaceAll("[^0-9]", "");
    if (digitsOnly.isBlank())
    {
      throw new IllegalArgumentException("Booking ID must contain a number.");
    }
    return Integer.parseInt(digitsOnly);
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
