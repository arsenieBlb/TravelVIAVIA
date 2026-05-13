package client.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import client.model.Booking;
import client.model.Model;

import java.util.List;

public class BookingAdminViewModel
{
  private final Model model;
  private final ObservableList<Booking> allBookings =
      FXCollections.observableArrayList();
  private final FilteredList<Booking> filteredBookings;
  private final StringProperty bookingIdFilter = new SimpleStringProperty("");
  private final StringProperty emailFilter = new SimpleStringProperty("");

  public BookingAdminViewModel(Model model)
  {
    this.model = model;
    this.filteredBookings = new FilteredList<>(allBookings,
        booking -> true);

    bookingIdFilter.addListener((observable, oldValue, newValue) ->
        updatePredicate());
    emailFilter.addListener((observable, oldValue, newValue) ->
        updatePredicate());

    refresh();
  }

  public void refresh()
  {
    List<Booking> bookings = model.getAllBookings();
    allBookings.setAll(bookings);
    updatePredicate();
  }

  public void clearFilters()
  {
    bookingIdFilter.set("");
    emailFilter.set("");
  }

  private void updatePredicate()
  {
    String rawBookingIdQuery = normalize(bookingIdFilter.get());
    String bookingIdQuery = rawBookingIdQuery.replaceAll("[^0-9]", "");
    String emailQuery = normalize(emailFilter.get()).toLowerCase();

    filteredBookings.setPredicate(booking ->
    {
      boolean idMatches = rawBookingIdQuery.isEmpty()
          || (!bookingIdQuery.isEmpty()
          && String.valueOf(booking.getBookingId()).contains(bookingIdQuery));
      boolean emailMatches = emailQuery.isEmpty()
          || getCustomerEmail(booking).toLowerCase().contains(emailQuery);
      return idMatches && emailMatches;
    });
  }

  private String getCustomerEmail(Booking booking)
  {
    if (booking.getCustomer() == null || booking.getCustomer().getEmail() == null)
    {
      return "";
    }
    return booking.getCustomer().getEmail();
  }

  private String normalize(String value)
  {
    return value == null ? "" : value.trim();
  }

  public ObservableList<Booking> getFilteredBookings()
  {
    return filteredBookings;
  }

  public StringProperty bookingIdFilterProperty()
  {
    return bookingIdFilter;
  }

  public StringProperty emailFilterProperty()
  {
    return emailFilter;
  }
}


