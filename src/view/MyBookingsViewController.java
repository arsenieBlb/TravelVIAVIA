package view;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Booking;
import model.Flight;
import viewmodel.MyBookingsViewModel;

import java.time.format.DateTimeFormatter;

public class MyBookingsViewController
{
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
    emptyBookingsPane.setOnMouseClicked(event ->
        viewHandler.showAddBookingDialog());
    bookingsListPane.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2)
      {
        viewHandler.showAddBookingDialog();
      }
    });
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
    card.getStyleClass().add("card");
    card.setPrefWidth(360);

    HBox header = new HBox(10);
    Label idLabel = new Label("Booking #" + booking.getBookingId());
    idLabel.getStyleClass().add("card-title");
    Label statusLabel = new Label("Current");
    statusLabel.getStyleClass().add("result-pill-value");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    header.getChildren().addAll(idLabel, spacer, statusLabel);

    Label routeLabel = new Label(flight.getDepartureCity().getCityName()
        + " -> " + flight.getArrivalCity().getCityName());
    routeLabel.getStyleClass().add("summary-value");

    Label dateLabel = new Label(flight.getDepartureTime()
        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")));
    dateLabel.getStyleClass().add("subtle-body");

    Label carrierLabel = new Label(flight.getCarrier().getName() + " | "
        + flight.getFlightNumber());
    carrierLabel.getStyleClass().add("subtle-body");

    Label passengerLabel = new Label(booking.getPassengers().size()
        + (booking.getPassengers().size() == 1 ? " passenger" : " passengers")
        + " | " + String.format("EUR %.0f", booking.getTotalPrice()));
    passengerLabel.getStyleClass().add("summary-value");

    Button detailsButton = new Button("View details");
    detailsButton.getStyleClass().add("btn-outline");
    detailsButton.setMaxWidth(Double.MAX_VALUE);
    detailsButton.setOnAction(event -> viewHandler.showBookingDetails(booking));

    card.getChildren().addAll(header, routeLabel, dateLabel, carrierLabel,
        passengerLabel, detailsButton);
    return card;
  }
}
