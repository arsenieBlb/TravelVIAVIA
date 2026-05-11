package view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Booking;
import model.Flight;
import model.Passenger;
import model.PassengerLuggage;
import model.SeatAssignment;
import viewmodel.MyBookingsViewModel;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

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

    bookingDetailsContent.getChildren().add(createBookingCodePanel());
    bookingDetailsContent.getChildren().add(createPassengerDateGrid());
    List<Flight> segments = new java.util.ArrayList<>();
    if (flight instanceof model.ConnectingFlight connectingFlight) {
        segments.add(connectingFlight.getFirstSegment());
        segments.add(connectingFlight.getSecondSegment());
    } else {
        segments.add(flight);
    }

    bookingDetailsContent.getChildren().add(createFlightInformationPanel(
        segments));
    bookingDetailsContent.getChildren().add(createFareSummaryPanel());
  }

  private VBox createBookingCodePanel()
  {
    VBox panel = new VBox(4);
    panel.getStyleClass().add("booking-code-panel");
    panel.getChildren().addAll(createDetailsLabel("BOOKING CODE"),
        createDetailsValue("#" + currentBooking.getBookingId(),
            "booking-details-code"));
    return panel;
  }

  private GridPane createPassengerDateGrid()
  {
    GridPane grid = new GridPane();
    grid.setHgap(18);
    grid.setVgap(12);
    grid.add(createDetailsValueBlock("PASSENGER NAME",
        getPassengerDisplayName()), 0, 0);
    grid.add(createDetailsValueBlock("DATE BOOKED",
        currentBooking.getBookingDate().format(
            DateTimeFormatter.ofPattern("MMM dd, yyyy"))), 1, 0);
    return grid;
  }

  private VBox createFlightInformationPanel(List<Flight> segments)
  {
    Flight firstFlight = segments.get(0);
    Flight lastFlight = segments.get(segments.size() - 1);

    VBox panel = new VBox(14);
    panel.getStyleClass().add("booking-flight-panel");
    Label title = new Label("FLIGHT INFORMATION");
    title.getStyleClass().add("booking-section-title");

    VBox summary = new VBox(8);
    summary.getChildren().addAll(
        createInfoRow("Route:", createRouteText(segments)),
        createInfoRow("Flight Date:", firstFlight.getDepartureTime()
            .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))),
        createInfoRow("Total Duration:", formatDuration(Duration.between(
            firstFlight.getDepartureTime(), lastFlight.getArrivalTime()))),
        createInfoRow("Airline:", firstFlight.getCarrier().getName() + " | "
            + firstFlight.getFlightNumber()),
        createInfoRow("Aircraft:", firstFlight.getPlane().getPlaneType().getModel()));

    Region divider = new Region();
    divider.getStyleClass().add("booking-blue-divider");

    Label segmentTitle = new Label("FLIGHT SEGMENTS");
    segmentTitle.getStyleClass().add("booking-segment-heading");

    VBox segmentList = new VBox(10);
    for (int i = 0; i < segments.size(); i++)
    {
      segmentList.getChildren().add(createSegmentBlock(segments.get(i),
          i + 1));
    }

    panel.getChildren().addAll(title, summary, divider, segmentTitle,
        segmentList, createFinalArrivalRow(lastFlight));
    return panel;
  }

  private HBox createInfoRow(String labelText, String valueText)
  {
    Label label = new Label(labelText);
    label.getStyleClass().add("booking-info-label");
    Label value = new Label(valueText);
    value.getStyleClass().add("booking-info-value");
    value.setWrapText(true);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox row = new HBox(10, label, spacer, value);
    return row;
  }

  private VBox createSegmentBlock(Flight flight, int segmentNumber)
  {
    VBox block = new VBox(8);
    block.getStyleClass().add("booking-segment-block");

    HBox header = new HBox(8);
    Label pill = new Label("SEG " + segmentNumber);
    pill.getStyleClass().add("booking-segment-pill");
    Label route = new Label(flight.getDepartureCity().getCityName() + " -> "
        + flight.getArrivalCity().getCityName());
    route.getStyleClass().add("booking-info-value");
    header.getChildren().addAll(pill, route);

    GridPane details = new GridPane();
    details.setHgap(18);
    details.setVgap(4);
    details.getStyleClass().add("booking-segment-details");
    details.add(createSmallDetail("Departure",
        flight.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"))),
        0, 0);
    details.add(createSmallDetail("Duration", flight.getDurationString()), 0,
        1);
    details.add(createSmallDetail("Class", getSeatClassText(flight)), 1, 0);
    details.add(createSmallDetail("Seat", getSeatText(flight)), 1, 1);

    block.getChildren().addAll(header, details);
    return block;
  }

  private HBox createFinalArrivalRow(Flight lastFlight)
  {
    HBox row = new HBox(10);
    row.getStyleClass().add("booking-final-arrival");
    Label label = new Label("Final Arrival:");
    label.getStyleClass().add("booking-info-label");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    VBox value = new VBox(2,
        createDetailsValue(lastFlight.getArrivalCity().getCityName(),
            "booking-info-value"),
        createDetailsValue(lastFlight.getArrivalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm")), "subtle-body"));
    row.getChildren().addAll(label, spacer, value);
    return row;
  }

  private VBox createFareSummaryPanel()
  {
    VBox panel = new VBox(10);
    panel.getStyleClass().add("booking-fare-panel");
    Label title = new Label("FARE SUMMARY");
    title.getStyleClass().add("booking-section-title");

    double baseFare = currentBooking.getFlight().getBasePrice()
        * currentBooking.getPassengers().size();
    double carryOnFare = getLuggageTotal("carry");
    double baggageFare = getLuggageTotal("baggage");

    panel.getChildren().addAll(title,
        createFareRow("Base Fare:", String.format("EUR %.0f", baseFare),
            false),
        createFareRow("Carry-on:", String.format("EUR %.0f", carryOnFare),
            false),
        createFareRow("Baggage:", String.format("EUR %.0f", baggageFare),
            false),
        createFareRow("Total:", String.format("EUR %.0f",
            currentBooking.getTotalPrice()), true));
    return panel;
  }

  private HBox createFareRow(String labelText, String valueText,
      boolean total)
  {
    Label label = new Label(labelText);
    label.getStyleClass().add(total ? "fare-total" : "booking-info-label");
    Label value = new Label(valueText);
    value.getStyleClass().add(total ? "fare-total-price" : "booking-info-value");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox row = new HBox(10, label, spacer, value);
    if (total)
    {
      row.getStyleClass().add("booking-fare-total-row");
    }
    return row;
  }

  private VBox createSmallDetail(String labelText, String valueText)
  {
    Label label = new Label(labelText + ": ");
    label.getStyleClass().add("summary-label");
    Label value = new Label(valueText);
    value.getStyleClass().add("summary-value");
    return new VBox(2, label, value);
  }

  private VBox createDetailsValueBlock(String labelText, String valueText)
  {
    return new VBox(4, createDetailsLabel(labelText),
        createDetailsValue(valueText, "booking-info-value"));
  }

  private Label createDetailsLabel(String text)
  {
    Label label = new Label(text);
    label.getStyleClass().add("summary-label");
    return label;
  }

  private Label createDetailsValue(String text, String styleClass)
  {
    Label value = new Label(text);
    value.getStyleClass().add(styleClass);
    value.setWrapText(true);
    return value;
  }

  private String getPassengerDisplayName()
  {
    if (currentBooking.getPassengers().isEmpty())
    {
      return "Passenger";
    }
    Passenger passenger = currentBooking.getPassengers().get(0);
    if (currentBooking.getPassengers().size() == 1)
    {
      return passenger.getFullName();
    }
    return passenger.getFullName() + " +"
        + (currentBooking.getPassengers().size() - 1);
  }

  private String createRouteText(List<Flight> segments)
  {
    StringBuilder route = new StringBuilder();
    route.append(segments.get(0).getDepartureCity().getCityName());
    for (Flight segment : segments)
    {
      route.append(" -> ").append(segment.getArrivalCity().getCityName());
    }
    return route.toString();
  }

  private String formatDuration(Duration duration)
  {
    return String.format("%dh %02dm", duration.toHours(),
        duration.toMinutesPart());
  }

  private String getSeatClassText(Flight flight)
  {
    for (Passenger passenger : currentBooking.getPassengers())
    {
      for (SeatAssignment seatAssignment : passenger.getSeatAssignments()) {
        if (seatAssignment.getFlight().getFlightId() == flight.getFlightId()) {
          return seatAssignment.getSeat().getSeatClass().toString();
        }
      }
    }
    return "Economy";
  }

  private String getSeatText(Flight flight)
  {
    for (Passenger passenger : currentBooking.getPassengers())
    {
      for (SeatAssignment seatAssignment : passenger.getSeatAssignments()) {
        if (seatAssignment.getFlight().getFlightId() == flight.getFlightId()) {
          return seatAssignment.getSeat().getSeatNumber();
        }
      }
    }
    return "Not selected";
  }

  private double getLuggageTotal(String namePart)
  {
    double total = 0;
    for (Passenger passenger : currentBooking.getPassengers())
    {
      for (PassengerLuggage luggage : passenger.getPassengerLuggage())
      {
        if (luggage.getLuggageType().getName().toLowerCase()
            .contains(namePart))
        {
          total += luggage.getTotalExtraPrice();
        }
      }
    }
    return total;
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
