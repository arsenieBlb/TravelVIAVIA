package client.view;

import client.model.Booking;
import client.model.ConnectingFlight;
import client.model.Customer;
import client.model.Flight;
import client.model.Passenger;
import client.model.PassengerLuggage;
import client.model.SeatAssignment;
import client.viewmodel.BookingAdminViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingAdminViewController
{
  @FXML private TextField bookingIdSearchField;
  @FXML private TextField emailSearchField;
  @FXML private Button viewBookingButton;
  @FXML private TableView<Booking> bookingsTable;
  @FXML private TableColumn<Booking, String> bookingIdColumn;
  @FXML private TableColumn<Booking, Number> passengersColumn;
  @FXML private TableColumn<Booking, String> routeColumn;
  @FXML private TableColumn<Booking, String> typeColumn;
  @FXML private TableColumn<Booking, String> emailColumn;
  @FXML private TableColumn<Booking, String> totalColumn;
  @FXML private Label bookingsEmptyLabel;

  private BookingAdminViewModel viewModel;
  private SortedList<Booking> sortedBookings;
  private boolean initialized;

  public void init(BookingAdminViewModel viewModel)
  {
    this.viewModel = viewModel;

    if (initialized)
    {
      refresh();
      return;
    }

    setupTable();
    setupBindings();
    setupActions();
    initialized = true;
    refresh();
  }

  private void setupTable()
  {
    bookingIdColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(viewModel.getBookingCode(data.getValue())));
    passengersColumn.setCellValueFactory(data ->
        new ReadOnlyObjectWrapper<Number>(
            data.getValue().getPassengers().size()));
    routeColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(viewModel.getRoute(data.getValue())));
    typeColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(viewModel.getTripType(data.getValue())));
    emailColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(viewModel.getCustomerEmail(data.getValue())));
    totalColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(viewModel.getFormattedTotal(data.getValue())));

    bookingIdColumn.setCellFactory(column -> createTextCell(
        "booking-id-cell"));
    passengersColumn.setCellFactory(column -> createIntegerCell());
    totalColumn.setCellFactory(column -> createTextCell("total-cell"));

    sortedBookings = new SortedList<>(viewModel.getFilteredBookings());
    sortedBookings.comparatorProperty().bind(
        bookingsTable.comparatorProperty());
    bookingsTable.setItems(sortedBookings);

    bookingsEmptyLabel.visibleProperty().bind(Bindings.isEmpty(sortedBookings));
    bookingsEmptyLabel.managedProperty().bind(
        bookingsEmptyLabel.visibleProperty());

    bookingsTable.setRowFactory(table -> {
      TableRow<Booking> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty())
        {
          showBookingDetails(row.getItem());
        }
      });
      return row;
    });

    setDefaultSortDirection();
    bookingsTable.getSortOrder().clear();
  }

  private void setupBindings()
  {
    bookingIdSearchField.textProperty().bindBidirectional(
        viewModel.bookingIdFilterProperty());
    emailSearchField.textProperty().bindBidirectional(
        viewModel.emailFilterProperty());
    viewBookingButton.disableProperty().bind(
        bookingsTable.getSelectionModel().selectedItemProperty().isNull());
  }

  private void setupActions()
  {
    viewBookingButton.setOnAction(event -> {
      Booking booking = bookingsTable.getSelectionModel().getSelectedItem();
      if (booking != null)
      {
        showBookingDetails(booking);
      }
    });
  }

  private void setDefaultSortDirection()
  {
    bookingIdColumn.setSortType(TableColumn.SortType.ASCENDING);
    passengersColumn.setSortType(TableColumn.SortType.ASCENDING);
    routeColumn.setSortType(TableColumn.SortType.ASCENDING);
    typeColumn.setSortType(TableColumn.SortType.ASCENDING);
    emailColumn.setSortType(TableColumn.SortType.ASCENDING);
    totalColumn.setSortType(TableColumn.SortType.ASCENDING);
  }

  public void refresh()
  {
    if (viewModel != null)
    {
      viewModel.refresh();
      bookingsTable.sort();
    }
  }

  public void clearFilters()
  {
    if (viewModel != null)
    {
      viewModel.clearFilters();
    }
  }

  private TableCell<Booking, String> createTextCell(String styleClass)
  {
    TableCell<Booking, String> cell = new TableCell<>()
    {
      @Override protected void updateItem(String item, boolean empty)
      {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item);
      }
    };

    if (styleClass != null)
    {
      cell.getStyleClass().add(styleClass);
    }
    return cell;
  }

  private TableCell<Booking, Number> createIntegerCell()
  {
    return new TableCell<>()
    {
      @Override protected void updateItem(Number item, boolean empty)
      {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : String.valueOf(item.intValue()));
      }
    };
  }

  private void showBookingDetails(Booking booking)
  {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Booking Details");
    dialog.getDialogPane().getStylesheets().add(
        getClass().getResource("admin_theme.css").toExternalForm());
    dialog.getDialogPane().getButtonTypes().add(
        new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

    ScrollPane scrollPane = new ScrollPane(createDetailsContent(booking));
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefViewportWidth(720);
    scrollPane.setPrefViewportHeight(560);
    dialog.getDialogPane().setContent(scrollPane);
    dialog.showAndWait();
  }

  private VBox createDetailsContent(Booking booking)
  {
    VBox content = new VBox(14);
    content.getStyleClass().add("booking-details-content");
    content.getChildren().add(createHeaderBlock(booking));
    content.getChildren().add(createFlightBlock("OUTBOUND ITINERARY",
        getSegments(booking.getFlight())));
    if (booking.getReturnFlight() != null)
    {
      content.getChildren().add(createFlightBlock("RETURN ITINERARY",
          getSegments(booking.getReturnFlight())));
    }
    content.getChildren().add(createPassengersBlock(booking));
    content.getChildren().add(createTotalBlock(booking));
    return content;
  }

  private VBox createHeaderBlock(Booking booking)
  {
    Customer customer = booking.getCustomer();
    VBox block = new VBox(6);
    block.getStyleClass().add("booking-flight-panel");
    block.getChildren().add(createValueRow("Booking reference code",
        viewModel.getBookingCode(booking)));
    block.getChildren().add(createValueRow("Trip type",
        viewModel.getTripType(booking)));
    block.getChildren().add(createValueRow("Customer account",
        customer == null ? "Unknown" : customer.getFullName()));
    block.getChildren().add(createValueRow("Customer email",
        viewModel.getCustomerEmail(booking)));
    block.getChildren().add(createValueRow("Customer ID",
        customer == null ? "N/A" : String.valueOf(customer.getUserId())));
    return block;
  }

  private VBox createFlightBlock(String title, List<Flight> segments)
  {
    VBox block = new VBox(8);
    block.getStyleClass().add("booking-flight-panel");
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("booking-section-title");
    block.getChildren().add(titleLabel);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "MMM dd, yyyy HH:mm");
    for (int i = 0; i < segments.size(); i++)
    {
      Flight segment = segments.get(i);
      String route = segment.getDepartureCity().getCityName() + " -> "
          + segment.getArrivalCity().getCityName();
      String value = segment.getFlightNumber() + " | " + route + " | "
          + segment.getDepartureTime().format(formatter) + " - "
          + segment.getArrivalTime().format(formatter);
      block.getChildren().add(createValueRow("Segment " + (i + 1), value));
    }
    return block;
  }

  private VBox createPassengersBlock(Booking booking)
  {
    VBox block = new VBox(10);
    block.getStyleClass().add("booking-flight-panel");
    Label title = new Label("PASSENGERS");
    title.getStyleClass().add("booking-section-title");
    block.getChildren().add(title);

    for (Passenger passenger : booking.getPassengers())
    {
      VBox passengerBlock = new VBox(4);
      passengerBlock.getStyleClass().add("booking-segment-block");
      passengerBlock.getChildren().add(createPlainValue(
          passenger.getFullName()));

      for (SeatAssignment assignment : passenger.getSeatAssignments())
      {
        String route = assignment.getFlight().getDepartureCity().getCityName()
            + " -> " + assignment.getFlight().getArrivalCity().getCityName();
        String value = route + " | Seat "
            + assignment.getSeat().getSeatNumber() + " | "
            + assignment.getSeat().getSeatClass();
        passengerBlock.getChildren().add(createSubtleValue(value));
      }

      if (passenger.getPassengerLuggage().isEmpty())
      {
        passengerBlock.getChildren().add(createSubtleValue("Baggage: none"));
      }
      for (PassengerLuggage luggage : passenger.getPassengerLuggage())
      {
        passengerBlock.getChildren().add(createSubtleValue(
            luggage.getQuantity() + "x "
                + luggage.getLuggageType().getName()));
      }
      block.getChildren().add(passengerBlock);
    }
    return block;
  }

  private VBox createTotalBlock(Booking booking)
  {
    VBox block = new VBox(6);
    block.getStyleClass().add("booking-fare-panel");
    block.getChildren().add(createValueRow("Total price",
        viewModel.getFormattedTotal(booking)));
    return block;
  }

  private HBox createValueRow(String labelText, String valueText)
  {
    Label label = new Label(labelText + ":");
    label.getStyleClass().add("booking-info-label");
    Label value = new Label(valueText);
    value.getStyleClass().add("booking-info-value");
    value.setWrapText(true);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    return new HBox(10, label, spacer, value);
  }

  private Label createPlainValue(String text)
  {
    Label label = new Label(text);
    label.getStyleClass().add("booking-info-value");
    return label;
  }

  private Label createSubtleValue(String text)
  {
    Label label = new Label(text);
    label.getStyleClass().add("subtle-body");
    label.setWrapText(true);
    return label;
  }

  private List<Flight> getSegments(Flight flight)
  {
    List<Flight> segments = new ArrayList<>();
    if (flight instanceof ConnectingFlight connectingFlight)
    {
      segments.add(connectingFlight.getFirstSegment());
      segments.add(connectingFlight.getSecondSegment());
    }
    else if (flight != null)
    {
      segments.add(flight);
    }
    return segments;
  }
}
