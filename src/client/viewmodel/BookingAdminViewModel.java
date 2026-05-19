package client.viewmodel;

import javafx.application.Platform;
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
    model.addPropertyChangeListener(event ->
    {
      if ("bookings".equals(event.getPropertyName()))
      {
        if (Platform.isFxApplicationThread())
        {
          refresh();
        }
        else
        {
          Platform.runLater(this::refresh);
        }
      }
    });

    refresh();
  }

  public void refresh()
  {
    javafx.concurrent.Task<List<Booking>> loadTask = new javafx.concurrent.Task<>()
    {
      @Override
      protected List<Booking> call()
      {
        return model.getAllBookings();
      }
    };

    loadTask.setOnSucceeded(event -> {
      List<Booking> bookings = loadTask.getValue();
      allBookings.setAll(bookings.stream()
          .filter(booking -> !booking.isCancelled())
          .toList());
      updatePredicate();
    });

    loadTask.setOnFailed(event -> {
      Throwable e = loadTask.getException();
      if (e != null) {
          System.err.println("Could not load admin bookings: "
              + e.getMessage());
      }
    });

    Thread thread = new Thread(loadTask);
    thread.setDaemon(true);
    thread.start();
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
          && getBookingCode(booking).replaceAll("[^0-9]", "")
          .contains(bookingIdQuery));
      boolean emailMatches = emailQuery.isEmpty()
          || getCustomerEmail(booking).toLowerCase().contains(emailQuery);
      return idMatches && emailMatches;
    });
  }

  public String getBookingCode(Booking booking)
  {
    return booking == null ? "" : "#" + booking.getBookingId();
  }

  public String getRoute(Booking booking)
  {
    if (booking == null || booking.getFlight() == null)
    {
      return "N/A";
    }
    String route = booking.getFlight().getDepartureCity().getCityName()
        + " -> " + booking.getFlight().getArrivalCity().getCityName();
    if (booking.getReturnFlight() != null)
    {
      route += " / " + booking.getReturnFlight().getDepartureCity()
          .getCityName() + " -> "
          + booking.getReturnFlight().getArrivalCity().getCityName();
    }
    return route;
  }

  public String getTripType(Booking booking)
  {
    return booking != null && booking.getReturnFlight() != null
        ? "Round trip" : "One-way";
  }

  public String getFormattedTotal(Booking booking)
  {
    return booking == null ? "EUR 0.00"
        : String.format("EUR %.2f", booking.getTotalPrice());
  }

  public String getCustomerEmail(Booking booking)
  {
    if (booking == null || booking.getCustomer() == null
        || booking.getCustomer().getEmail() == null)
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


