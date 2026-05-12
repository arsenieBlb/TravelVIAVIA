package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Booking;
import model.Flight;
import model.SeatClass;
import viewmodel.PassengerDetailsViewModel;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PassengerDetailsViewController
{
  @FXML private HBox timelineContainer;
  @FXML private Label timelineTotalTimeLabel;
  @FXML private VBox passengersSection;
  @FXML private Label fareBaseTextLabel;
  @FXML private Label fareBaseLabel;
  @FXML private Label fareCarryOnTextLabel;
  @FXML private Label fareCarryOnLabel;
  @FXML private Label fareBaggageTextLabel;
  @FXML private Label fareBaggageLabel;
  @FXML private Label fareTotalLabel;
  @FXML private Button cancelPassengerDetailsButton;
  @FXML private Button confirmBookingButton;

  private Region root;
  private ViewHandler viewHandler;
  private PassengerDetailsViewModel viewModel;

  public void init(ViewHandler viewHandler, PassengerDetailsViewModel viewModel, Region root)
  {
    this.viewHandler = viewHandler;
    this.viewModel = viewModel;
    this.root = root;

    cancelPassengerDetailsButton.setOnAction(event ->
        viewHandler.showBookFlight());
    confirmBookingButton.setOnAction(event -> confirmBooking());
  }

  public void refresh()
  {
    try
    {
      viewModel.prepare();
      renderTimeline();
      renderPassengerForms();
      refreshFareLabels();
    }
    catch (RuntimeException e)
    {
      showError(e.getMessage());
      viewHandler.showBookFlight();
    }
  }

  private void renderTimeline()
  {
    List<Flight> segments = viewModel.getFlightSegments();
    timelineContainer.getChildren().clear();
    if (segments.isEmpty())
    {
      timelineTotalTimeLabel.setText("0h 00m");
      return;
    }

    // render outbound timeline
    renderTimelineSegments(segments);

    // render return timeline if roundtrip
    List<Flight> returnSegments = viewModel.getReturnFlightSegments();
    if (!returnSegments.isEmpty())
    {
      Label returnArrow = new Label("  ↩  ");
      returnArrow.getStyleClass().add("timeline-segment-route");
      timelineContainer.getChildren().add(returnArrow);
      renderTimelineSegments(returnSegments);
    }

    Flight firstFlight = segments.get(0);
    Flight lastOutbound = segments.get(segments.size() - 1);
    Duration totalDuration = Duration.between(firstFlight.getDepartureTime(),
        lastOutbound.getArrivalTime());
    if (!returnSegments.isEmpty()) {
        Flight lastReturn = returnSegments.get(returnSegments.size() - 1);
        totalDuration = totalDuration.plus(
            Duration.between(returnSegments.get(0).getDepartureTime(), lastReturn.getArrivalTime()));
    }
    timelineTotalTimeLabel.setText(formatDuration(totalDuration));
  }

  private void renderTimelineSegments(List<Flight> segments)
  {
    Flight firstFlight = segments.get(0);
    timelineContainer.getChildren().add(createTerminalStop(
        firstFlight.getDepartureCity().getCityName(),
        firstFlight.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"))));

    for (int i = 0; i < segments.size(); i++)
    {
      Flight segment = segments.get(i);
      timelineContainer.getChildren().add(createTimelineLine());
      timelineContainer.getChildren().add(createSegmentCard(segment));
      timelineContainer.getChildren().add(createTimelineLine());

      if (i < segments.size() - 1)
      {
        Flight nextSegment = segments.get(i + 1);
        Duration layover = Duration.between(segment.getArrivalTime(),
            nextSegment.getDepartureTime());
        timelineContainer.getChildren().add(createLayoverStop(
            segment.getArrivalCity().getCityName(), formatDuration(layover)));
      }
      else
      {
        timelineContainer.getChildren().add(createTerminalStop(
            segment.getArrivalCity().getCityName(),
            segment.getArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
      }
    }
  }

  private VBox createTerminalStop(String cityName, String time)
  {
    Region dot = new Region();
    dot.getStyleClass().add("timeline-terminal-dot");
    Label timeLabel = new Label(time);
    timeLabel.getStyleClass().add("timeline-terminal-time");
    Label cityLabel = new Label(cityName);
    cityLabel.getStyleClass().add("timeline-terminal-city");

    VBox stop = new VBox(4, dot, timeLabel, cityLabel);
    stop.setAlignment(Pos.CENTER);
    stop.getStyleClass().add("timeline-terminal-stop");
    return stop;
  }

  private VBox createLayoverStop(String cityName, String duration)
  {
    Region dot = new Region();
    dot.getStyleClass().add("timeline-layover-dot");
    Label cityLabel = new Label(cityName);
    cityLabel.getStyleClass().add("timeline-layover-city");
    Label durationLabel = new Label(duration);
    durationLabel.getStyleClass().add("timeline-layover-time");

    VBox stop = new VBox(4, dot, cityLabel, durationLabel);
    stop.setAlignment(Pos.CENTER);
    stop.getStyleClass().add("timeline-layover-stop");
    return stop;
  }

  private VBox createSegmentCard(Flight segment)
  {
    Label routeLabel = new Label(segment.getDepartureCity().getCityName()
        + " \u2192 " + segment.getArrivalCity().getCityName());
    routeLabel.getStyleClass().add("timeline-segment-route");
    Label durationLabel = new Label(segment.getDurationString());
    durationLabel.getStyleClass().add("timeline-segment-duration");

    VBox card = new VBox(3, routeLabel, durationLabel);
    card.setAlignment(Pos.CENTER);
    card.getStyleClass().add("timeline-segment-card");
    return card;
  }

  private HBox createTimelineLine()
  {
    Region line = new Region();
    line.getStyleClass().add("timeline-line");
    HBox wrapper = new HBox(line);
    wrapper.setAlignment(Pos.CENTER);
    wrapper.setPadding(new Insets(0, 16, 0, 16));
    wrapper.getStyleClass().add("timeline-line-wrapper");
    return wrapper;
  }

  private String formatDuration(Duration duration)
  {
    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    return String.format("%dh %02dm", hours, minutes);
  }

  private void renderPassengerForms()
  {
    passengersSection.getChildren().clear();
    for (PassengerDetailsViewModel.PassengerForm form
        : viewModel.getPassengerForms())
    {
      passengersSection.getChildren().add(createPassengerCard(form));
    }
  }

  private VBox createPassengerCard(
      PassengerDetailsViewModel.PassengerForm form)
  {
    VBox card = new VBox(26);
    card.getStyleClass().add("passenger-form-panel");

    Label title = new Label("Passenger " + form.getPassengerNumber());
    title.getStyleClass().add("passenger-form-title");

    Region divider = new Region();
    divider.getStyleClass().add("passenger-form-divider");

    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(10);

    for (int i = 0; i < 2; i++)
    {
      ColumnConstraints column = new ColumnConstraints();
      column.setHgrow(Priority.ALWAYS);
      grid.getColumnConstraints().add(column);
    }

    TextField firstNameField = createTextField("First name");
    firstNameField.textProperty().bindBidirectional(form.firstNameProperty());
    grid.add(createField("First Name", firstNameField), 0, 0);

    TextField lastNameField = createTextField("Last name");
    lastNameField.textProperty().bindBidirectional(form.lastNameProperty());
    grid.add(createField("Last Name", lastNameField), 1, 0);

    HBox carryOnRow = createBaggageRow("Carry-on Bags",
        createCounter(form.getCarryOnQuantity(),
            () -> updateCarryOnBags(form, -1),
            () -> updateCarryOnBags(form, 1)));
    HBox checkedBaggageRow = createBaggageRow("Checked Baggage",
        createCounter(form.getBaggageQuantity(),
            () -> updateCheckedBaggage(form, -1),
            () -> updateCheckedBaggage(form, 1)));

    card.getChildren().addAll(title, divider, grid, carryOnRow,
        checkedBaggageRow);

    // outbound flight seat sections
    List<Flight> outboundSegments = viewModel.getFlightSegments();
    if (!outboundSegments.isEmpty()) {
        Label outboundLabel = new Label("OUTBOUND FLIGHT");
        outboundLabel.getStyleClass().add("passenger-form-title");
        card.getChildren().add(outboundLabel);
        for (int i = 0; i < outboundSegments.size(); i++) {
            VBox seatSection = createSeatSection(form, outboundSegments.get(i), i);
            card.getChildren().add(seatSection);
        }
    }

    // return flight seat sections
    List<Flight> returnSegments = viewModel.getReturnFlightSegments();
    if (!returnSegments.isEmpty()) {
        Label returnLabel = new Label("RETURN FLIGHT");
        returnLabel.getStyleClass().add("passenger-form-title");
        card.getChildren().add(returnLabel);
        int offset = outboundSegments.size();
        for (int i = 0; i < returnSegments.size(); i++) {
            VBox seatSection = createSeatSection(form, returnSegments.get(i), offset + i);
            card.getChildren().add(seatSection);
        }
    }

    return card;
  }

    private VBox createSeatSection(PassengerDetailsViewModel.PassengerForm form, Flight flight, int segmentIndex)
    {
        String route = flight == null ? "Selected flight"
                : flight.getDepartureCity().getCityName() + " \u2192 "
                + flight.getArrivalCity().getCityName();

        Label title = new Label("SEGMENT " + (segmentIndex + 1) + ": " + route);
        title.getStyleClass().add("passenger-segment-title");

        ComboBox<SeatClass> seatClassCombo = new ComboBox<>();

        seatClassCombo.getItems().setAll(new model.EconomyClass(), new model.BusinessClass());

        seatClassCombo.valueProperty().bindBidirectional(form.seatClassProperty(segmentIndex));

        seatClassCombo.setOnAction(event ->
                javafx.application.Platform.runLater(this::refreshFareLabels));

        seatClassCombo.getStyleClass().add("passenger-seat-control");
        seatClassCombo.setMaxWidth(Double.MAX_VALUE);

        TextField selectedSeatField = new TextField();
        selectedSeatField.textProperty().bind(form.selectedSeatTextProperty(segmentIndex));
        selectedSeatField.setEditable(false);
        selectedSeatField.getStyleClass().add("passenger-seat-control");
        selectedSeatField.setMaxWidth(Double.MAX_VALUE);

        Button chooseSeatButton = new Button("Choose seat");
        chooseSeatButton.getStyleClass().add("passenger-seat-button");
        chooseSeatButton.setMaxWidth(Double.MAX_VALUE);
        chooseSeatButton.setOnAction(event ->
                viewHandler.showSeatPicker(form.getPassengerNumber(), segmentIndex));

        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(16);
        seatGrid.setVgap(8);

        ColumnConstraints classColumn = new ColumnConstraints();
        classColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints seatColumn = new ColumnConstraints();
        seatColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints buttonColumn = new ColumnConstraints();
        buttonColumn.setHgrow(Priority.ALWAYS);

        seatGrid.getColumnConstraints().addAll(classColumn, seatColumn, buttonColumn);

        seatGrid.add(createField("Class", seatClassCombo), 0, 0);
        seatGrid.add(createField("Seat", selectedSeatField), 1, 0);
        seatGrid.add(chooseSeatButton, 2, 0);

        VBox section = new VBox(12, title, seatGrid);
        section.getStyleClass().add("passenger-segment-card");
        return section;
    }

  private TextField createTextField(String promptText)
  {
    TextField textField = new TextField();
    textField.setPromptText(promptText);
    textField.getStyleClass().add("passenger-text-field");
    textField.setMaxWidth(Double.MAX_VALUE);
    return textField;
  }

  private VBox createField(String labelText, Region input)
  {
    Label label = new Label(labelText);
    label.getStyleClass().add("passenger-field-label");
    VBox box = new VBox(6, label, input);
    input.setMaxWidth(Double.MAX_VALUE);
    return box;
  }

  private HBox createBaggageRow(String text, HBox counter)
  {
    Label label = new Label(text);
    label.getStyleClass().add("passenger-bag-label");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox row = new HBox(16, label, spacer, counter);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(4, 0, 0, 0));
    return row;
  }

  private HBox createCounter(int value, Runnable decrement, Runnable increment)
  {
    Label valueLabel = new Label(String.valueOf(value));
    valueLabel.getStyleClass().add("bag-counter-value");

    Button minusButton = new Button("-");
    minusButton.getStyleClass().add("bag-counter-button");
    minusButton.setDisable(decrement == null);
    if (decrement != null)
    {
      minusButton.setOnAction(event -> decrement.run());
    }

    Button plusButton = new Button("+");
    plusButton.getStyleClass().add("bag-counter-button");
    plusButton.setDisable(increment == null);
    if (increment != null)
    {
      plusButton.setOnAction(event -> increment.run());
    }

    HBox counter = new HBox(18, minusButton, valueLabel, plusButton);
    counter.setAlignment(Pos.CENTER);
    counter.getStyleClass().add("bag-counter");
    return counter;
  }

  private void updateCheckedBaggage(PassengerDetailsViewModel.PassengerForm form,
      int delta)
  {
    int nextValue = Math.max(0, Math.min(5,
        form.getBaggageQuantity() + delta));
    form.baggageQuantityProperty().set(nextValue);
    renderPassengerForms();
    refreshFareLabels();
  }

  private void updateCarryOnBags(PassengerDetailsViewModel.PassengerForm form,
      int delta)
  {
    int nextValue = Math.max(0, Math.min(
        PassengerDetailsViewModel.MAX_CARRY_ON_BAGS,
        form.getCarryOnQuantity() + delta));
    form.carryOnQuantityProperty().set(nextValue);
    renderPassengerForms();
    refreshFareLabels();
  }

  private void refreshFareLabels()
  {
    int passengerCount = viewModel.getPassengerForms().size();
    int carryOnCount = viewModel.getTotalCarryOnQuantity();
    int baggageCount = viewModel.getTotalBaggageQuantity();
    double baggageUnitPrice = viewModel.getBaggageUnitPrice();

    fareBaseTextLabel.setText("Base Fare (" + passengerCount
        + (passengerCount == 1 ? " passenger)" : " passengers)"));
    fareBaseLabel.setText(formatCurrency(viewModel.baseFareProperty().get()));
    fareCarryOnTextLabel.setText("Carry-on x" + carryOnCount + " @ "
        + formatCurrency(PassengerDetailsViewModel.CARRY_ON_UNIT_PRICE));
    fareCarryOnLabel.setText(formatCurrency(
        viewModel.carryOnFareProperty().get()));
    fareBaggageTextLabel.setText("Baggage x" + baggageCount + " @ "
        + formatCurrency(baggageUnitPrice));
    fareBaggageLabel.setText(formatCurrency(viewModel.baggageFareProperty().get()));
    fareTotalLabel.setText(formatCurrency(viewModel.totalFareProperty().get()));
  }

  private void confirmBooking()
  {
    try
    {
      Booking booking = viewModel.confirmBooking();
      viewHandler.refreshMyBookings();
      showInformation("Booking confirmed",
          "Booking #" + booking.getBookingId() + " has been saved.");
      viewHandler.showMyBookings();
    }
    catch (RuntimeException e)
    {
      showError(e.getMessage());
      viewModel.prepare();
    }
  }

  private String formatCurrency(double value)
  {
    return String.format("EUR %.0f", value);
  }

  private void showInformation(String title, String message)
  {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showError(String message)
  {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Booking");
    alert.setHeaderText(null);
    alert.setContentText(message == null ? "Something went wrong." : message);
    alert.showAndWait();
  }
}
