package view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Booking;
import model.Flight;
import model.Passenger;
import model.PassengerLuggage;
import viewmodel.MyBookingsViewModel;

import java.time.format.DateTimeFormatter;

public class BookingDetailsDialogController
{
  @FXML private StackPane bookingDetailsModal;
  @FXML private Button closeBookingDetailsButton;
  @FXML private VBox bookingDetailsContent;
  @FXML private Button closeBookingDetailsActionButton;
  @FXML private Button cancelBookingFromDetailsButton;

  private StackPane wrapper;
  private ViewHandler viewHandler;
  private MyBookingsViewModel viewModel;
  private Booking currentBooking;

  public void init(MyBookingsViewModel viewModel, ViewHandler viewHandler,
      StackPane wrapper)
  {
    this.viewModel = viewModel;
    this.viewHandler = viewHandler;
    this.wrapper = wrapper;

    closeBookingDetailsButton.setOnAction(event -> hide());
    closeBookingDetailsActionButton.setOnAction(event -> hide());
    cancelBookingFromDetailsButton.setOnAction(event -> cancelBooking());
  }

  public void show(Booking booking)
  {
    currentBooking = booking;
    renderBookingDetails();
    wrapper.setVisible(true);
    wrapper.setManaged(true);
  }

  private void hide()
  {
    wrapper.setVisible(false);
    wrapper.setManaged(false);
  }

  private void renderBookingDetails()
  {
    bookingDetailsContent.getChildren().clear();
    Flight flight = currentBooking.getFlight();

    bookingDetailsContent.getChildren().add(createValue("Booking ID",
        "#" + currentBooking.getBookingId()));
    bookingDetailsContent.getChildren().add(createValue("Route",
        flight.getDepartureCity().getCityName() + " -> "
            + flight.getArrivalCity().getCityName()));
    bookingDetailsContent.getChildren().add(createValue("Departure",
        flight.getDepartureTime()
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm"))));
    bookingDetailsContent.getChildren().add(createValue("Arrival",
        flight.getArrivalTime()
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm"))));
    bookingDetailsContent.getChildren().add(createValue("Carrier",
        flight.getCarrier().getName() + " | " + flight.getFlightNumber()));
    bookingDetailsContent.getChildren().add(createValue("Aircraft",
        flight.getPlane().getPlaneType().getModel()));
    bookingDetailsContent.getChildren().add(createValue("Total",
        String.format("EUR %.0f", currentBooking.getTotalPrice())));

    Label passengerTitle = new Label("Passengers");
    passengerTitle.getStyleClass().add("card-title");
    bookingDetailsContent.getChildren().add(passengerTitle);

    for (Passenger passenger : currentBooking.getPassengers())
    {
      bookingDetailsContent.getChildren().add(createPassengerLine(passenger));
    }
  }

  private VBox createValue(String labelText, String valueText)
  {
    Label label = new Label(labelText);
    label.getStyleClass().add("summary-label");
    Label value = new Label(valueText);
    value.getStyleClass().add("summary-value");
    return new VBox(3, label, value);
  }

  private Label createPassengerLine(Passenger passenger)
  {
    StringBuilder text = new StringBuilder(passenger.getFullName());
    if (!passenger.getPassengerLuggage().isEmpty())
    {
      text.append(" | ");
      for (int i = 0; i < passenger.getPassengerLuggage().size(); i++)
      {
        PassengerLuggage luggage = passenger.getPassengerLuggage().get(i);
        if (i > 0)
        {
          text.append(", ");
        }
        text.append(luggage.getQuantity()).append(" x ")
            .append(luggage.getLuggageType().getName());
      }
    }

    Label label = new Label(text.toString());
    label.getStyleClass().add("subtle-body");
    return label;
  }

  private void cancelBooking()
  {
    if (currentBooking == null)
    {
      return;
    }
    viewModel.cancelBooking(currentBooking);
    viewHandler.refreshMyBookings();
    hide();
  }
}
