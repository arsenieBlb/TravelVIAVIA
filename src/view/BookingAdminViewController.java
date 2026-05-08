package view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Booking;
import model.Flight;
import model.Plane;
import model.PlaneType;
import viewmodel.BookingAdminViewModel;

public class BookingAdminViewController
{
  @FXML private TextField bookingIdSearchField;
  @FXML private TextField emailSearchField;
  @FXML private TableView<Booking> bookingsTable;
  @FXML private TableColumn<Booking, Number> bookingIdColumn;
  @FXML private TableColumn<Booking, Number> passengersColumn;
  @FXML private TableColumn<Booking, String> routeColumn;
  @FXML private TableColumn<Booking, String> typeColumn;
  @FXML private TableColumn<Booking, String> emailColumn;
  @FXML private TableColumn<Booking, Number> totalColumn;
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
    initialized = true;
    refresh();
  }

  private void setupTable()
  {
    bookingIdColumn.setCellValueFactory(data ->
        new ReadOnlyObjectWrapper<Number>(data.getValue().getBookingId()));
    passengersColumn.setCellValueFactory(data ->
        new ReadOnlyObjectWrapper<Number>(
            data.getValue().getPassengers().size()));
    routeColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(getRoute(data.getValue())));
    typeColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(getFlightType(data.getValue())));
    emailColumn.setCellValueFactory(data ->
        new ReadOnlyStringWrapper(getCustomerEmail(data.getValue())));
    totalColumn.setCellValueFactory(data ->
        new ReadOnlyObjectWrapper<Number>(data.getValue().getTotalPrice()));

    bookingIdColumn.setCellFactory(column -> createIntegerCell(
        "booking-id-cell", "#"));
    passengersColumn.setCellFactory(column -> createIntegerCell(null, ""));
    totalColumn.setCellFactory(column -> createTotalCell());

    sortedBookings = new SortedList<>(viewModel.getFilteredBookings());
    sortedBookings.comparatorProperty().bind(
        bookingsTable.comparatorProperty());
    bookingsTable.setItems(sortedBookings);

    bookingsEmptyLabel.visibleProperty().bind(Bindings.isEmpty(sortedBookings));
    bookingsEmptyLabel.managedProperty().bind(
        bookingsEmptyLabel.visibleProperty());

    setDefaultSortDirection();
    bookingsTable.getSortOrder().clear();
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

  private void setupBindings()
  {
    bookingIdSearchField.textProperty().bindBidirectional(
        viewModel.bookingIdFilterProperty());
    emailSearchField.textProperty().bindBidirectional(
        viewModel.emailFilterProperty());
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

  private TableCell<Booking, Number> createIntegerCell(String styleClass,
      String prefix)
  {
    TableCell<Booking, Number> cell = new TableCell<>()
    {
      @Override protected void updateItem(Number item, boolean empty)
      {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : prefix + item.intValue());
      }
    };

    if (styleClass != null)
    {
      cell.getStyleClass().add(styleClass);
    }
    return cell;
  }

  private TableCell<Booking, Number> createTotalCell()
  {
    TableCell<Booking, Number> cell = new TableCell<>()
    {
      @Override protected void updateItem(Number item, boolean empty)
      {
        super.updateItem(item, empty);
        setText(empty || item == null ? null
            : String.format("$%.2f", item.doubleValue()));
      }
    };

    cell.getStyleClass().add("total-cell");
    return cell;
  }

  private String getRoute(Booking booking)
  {
    Flight flight = booking.getFlight();
    if (flight == null)
    {
      return "N/A";
    }

    String origin = flight.getDepartureCity() == null ? "???"
        : flight.getDepartureCity().getCityName();
    String destination = flight.getArrivalCity() == null ? "???"
        : flight.getArrivalCity().getCityName();
    return origin + " -> " + destination;
  }

  private String getFlightType(Booking booking)
  {
    Flight flight = booking.getFlight();
    if (flight == null)
    {
      return "N/A";
    }

    Plane plane = flight.getPlane();
    if (plane == null)
    {
      return "Direct";
    }

    PlaneType planeType = plane.getPlaneType();
    if (planeType == null)
    {
      return "Direct";
    }
    return planeType.getTypeName();
  }

  private String getCustomerEmail(Booking booking)
  {
    if (booking.getCustomer() == null || booking.getCustomer().getEmail() == null)
    {
      return "";
    }
    return booking.getCustomer().getEmail();
  }
}
