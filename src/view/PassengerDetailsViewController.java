package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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

public class PassengerDetailsViewController
{
  @FXML private HBox timelineContainer;
  @FXML private Label timelineTotalTimeLabel;
  @FXML private VBox passengersSection;
  @FXML private Label fareBaseTextLabel;
  @FXML private Label fareBaseLabel;
  @FXML private Label fareBaggageTextLabel;
  @FXML private Label fareBaggageLabel;
  @FXML private Label fareTotalLabel;
  @FXML private Button cancelPassengerDetailsButton;
  @FXML private Button confirmBookingButton;

  private Region root;
  private ViewHandler viewHandler;
  private PassengerDetailsViewModel viewModel;

  public void init(PassengerDetailsViewModel viewModel, Region root,
      ViewHandler viewHandler)
  {
    this.viewModel = viewModel;
    this.root = root;
    this.viewHandler = viewHandler;

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
    Flight flight = viewModel.getSelectedFlight();
    timelineContainer.getChildren().clear();
    timelineContainer.getChildren().add(createTimelineChip(
        flight.getDepartureCity().getCityName()));
    timelineContainer.getChildren().add(createTimelineChip(
        flight.getArrivalCity().getCityName()));
    timelineTotalTimeLabel.setText(flight.getDurationString());
  }

  private Label createTimelineChip(String text)
  {
    Label label = new Label(text);
    label.getStyleClass().add("timeline-chip");
    return label;
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
    VBox card = new VBox(12);
    card.getStyleClass().add("card");

    Label title = new Label("Passenger " + form.getPassengerNumber());
    title.getStyleClass().add("card-title");

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);

    for (int i = 0; i < 4; i++)
    {
      ColumnConstraints column = new ColumnConstraints();
      column.setHgrow(Priority.ALWAYS);
      grid.getColumnConstraints().add(column);
    }

    TextField firstNameField = createTextField("First name");
    firstNameField.textProperty().bindBidirectional(form.firstNameProperty());
    grid.add(createField("FIRST NAME", firstNameField), 0, 0);

    TextField lastNameField = createTextField("Last name");
    lastNameField.textProperty().bindBidirectional(form.lastNameProperty());
    grid.add(createField("LAST NAME", lastNameField), 1, 0);

    ComboBox<SeatClass> seatClassCombo = new ComboBox<>();
    seatClassCombo.getItems().setAll(SeatClass.values());
    seatClassCombo.valueProperty().bindBidirectional(form.seatClassProperty());
    seatClassCombo.getStyleClass().add("field-input");
    seatClassCombo.setMaxWidth(Double.MAX_VALUE);
    grid.add(createField("SEAT CLASS", seatClassCombo), 2, 0);

    Spinner<Integer> baggageSpinner = new Spinner<>();
    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5,
            form.getBaggageQuantity());
    baggageSpinner.setValueFactory(valueFactory);
    baggageSpinner.getStyleClass().add("field-input");
    valueFactory.valueProperty().addListener((obs, oldValue, newValue) -> {
      form.baggageQuantityProperty().set(newValue);
      refreshFareLabels();
    });
    grid.add(createField("CHECKED BAGS", baggageSpinner), 3, 0);

    HBox seatRow = new HBox(10);
    seatRow.setPadding(new Insets(2, 0, 0, 0));
    Label seatLabel = new Label();
    seatLabel.textProperty().bind(form.selectedSeatTextProperty());
    seatLabel.getStyleClass().add("summary-value");
    Button chooseSeatButton = new Button("Choose seat");
    chooseSeatButton.getStyleClass().add("btn-outline");
    chooseSeatButton.setOnAction(event ->
        viewHandler.showSeatPicker(form.getPassengerNumber()));
    seatRow.getChildren().addAll(new Label("Seat:"), seatLabel,
        new Region(), chooseSeatButton);
    HBox.setHgrow(seatRow.getChildren().get(2), Priority.ALWAYS);

    card.getChildren().addAll(title, grid, seatRow);
    return card;
  }

  private TextField createTextField(String promptText)
  {
    TextField textField = new TextField();
    textField.setPromptText(promptText);
    textField.getStyleClass().add("field-input");
    textField.setMaxWidth(Double.MAX_VALUE);
    return textField;
  }

  private VBox createField(String labelText, Region input)
  {
    Label label = new Label(labelText);
    label.getStyleClass().add("field-label");
    VBox box = new VBox(6, label, input);
    input.setMaxWidth(Double.MAX_VALUE);
    return box;
  }

  private void refreshFareLabels()
  {
    int passengerCount = viewModel.getPassengerForms().size();
    int baggageCount = viewModel.getTotalBaggageQuantity();
    double baggageUnitPrice = viewModel.getBaggageUnitPrice();

    fareBaseTextLabel.setText("Base Fare (" + passengerCount
        + (passengerCount == 1 ? " passenger)" : " passengers)"));
    fareBaseLabel.setText(formatCurrency(viewModel.baseFareProperty().get()));
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
