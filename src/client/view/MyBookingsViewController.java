package client.view;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import client.model.Booking;
import client.model.Flight;
import client.model.Passenger;
import client.viewmodel.MyBookingsViewModel;

import java.time.format.DateTimeFormatter;

public class MyBookingsViewController
{
  @FXML private Button addBookingButton;
  @FXML private FlowPane bookingsListPane;
  @FXML private VBox emptyBookingsPane;

  private Region root;
  private ViewHandler viewHandler;
  private MyBookingsViewModel viewModel;

  public void init(MyBookingsViewModel viewModel, Region root,
      ViewHandler viewHandler)
  {
    this.viewModel = viewModel;
    this.root = root;
    this.viewHandler = viewHandler;

    viewModel.customerNameProperty().addListener((obs, oldValue, newValue) ->
        updateSubtitle());
    viewModel.getBookings().addListener(
        (ListChangeListener<Booking>) change -> renderBookings());
    addBookingButton.setOnAction(event -> viewHandler.showAddBookingDialog());
    emptyBookingsPane.setOnMouseClicked(event ->
        viewHandler.showAddBookingDialog());
    refresh();
  }

  public void refresh()
  {
    viewModel.refresh();
    updateSubtitle();
    renderBookings();
  }

  private void updateSubtitle()
  {
    // Kept for future auth text without changing the existing FXML layout.
  }

  private void renderBookings()
  {
    bookingsListPane.getChildren().clear();
    for (Booking booking : viewModel.getBookings())
    {
      bookingsListPane.getChildren().add(createBookingCard(booking));
    }

    boolean hasBookings = !viewModel.getBookings().isEmpty();
    bookingsListPane.setVisible(hasBookings);
    bookingsListPane.setManaged(hasBookings);
    emptyBookingsPane.setVisible(!hasBookings);
    emptyBookingsPane.setManaged(!hasBookings);
  }

  private VBox createBookingCard(Booking booking)
  {
    Flight flight = booking.getFlight();
    VBox card = new VBox(10);
    card.getStyleClass().add("booking-card");
    card.setPrefWidth(360);
    card.setOnMouseClicked(event -> {
      viewHandler.showBookingDetails(booking);
      event.consume();
    });

    HBox header = new HBox(10);
    VBox codeBox = createBookingCardValue("BOOKING CODE",
        "#" + booking.getBookingId(), "booking-card-code");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    Label chevron = new Label(">");
    chevron.getStyleClass().add("booking-card-chevron");
    header.getChildren().addAll(codeBox, spacer, chevron);

    VBox passengerBox = createBookingCardValue("PASSENGER",
        getPassengerDisplayName(booking), "booking-card-value");
    VBox dateBox = createBookingCardValue("DATE ADDED",
        booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
        "booking-card-date");

    String routeText = flight.getDepartureCity().getCityName() + " → "
        + flight.getArrivalCity().getCityName();

    // show return route if roundtrip
    if (booking.getReturnFlight() != null) {
        Flight returnFlight = booking.getReturnFlight();
        routeText += "\n↩ " + returnFlight.getDepartureCity().getCityName()
            + " → " + returnFlight.getArrivalCity().getCityName();
    }

    VBox routeBox = createBookingCardValue("ROUTE", routeText, "booking-card-value");

    card.getChildren().addAll(header, passengerBox, dateBox, routeBox);
    return card;
  }

  private VBox createBookingCardValue(String labelText, String valueText,
      String valueStyleClass)
  {
    Label label = new Label(labelText);
    label.getStyleClass().add("booking-card-label");
    Label value = new Label(valueText);
    value.getStyleClass().add(valueStyleClass);
    return new VBox(3, label, value);
  }

  private String getPassengerDisplayName(Booking booking)
  {
    if (booking.getPassengers().isEmpty())
    {
      return "Passenger";
    }
    Passenger passenger = booking.getPassengers().get(0);
    if (booking.getPassengers().size() == 1)
    {
      return passenger.getFullName();
    }
    return passenger.getFullName() + " +"
        + (booking.getPassengers().size() - 1);
  }

}


