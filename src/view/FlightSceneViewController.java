package view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import model.Booking;
import viewmodel.ViewModelFactory;

public class FlightSceneViewController
{
  @FXML private StackPane bookViewWrapper;
  @FXML private StackPane bookingsViewWrapper;
  @FXML private StackPane passengerViewWrapper;
  @FXML private StackPane seatMapDialogWrapper;
  @FXML private StackPane addBookingDialogWrapper;
  @FXML private StackPane bookingDetailsDialogWrapper;
  @FXML private Button bookTabButton;
  @FXML private Button bookingsTabButton;

  @FXML private BookFlightViewController bookViewControllerController;
  @FXML private MyBookingsViewController bookingsViewControllerController;
  @FXML private PassengerDetailsViewController passengerViewControllerController;
  @FXML private SeatMapViewController seatMapDialogController;
  @FXML private AddBookingDialogController addBookingDialogController;
  @FXML private BookingDetailsDialogController bookingDetailsDialogController;

  @FXML private Region root;

  private ViewModelFactory viewModelFactory;

  public void init(Region root, ViewHandler viewHandler,
      ViewModelFactory viewModelFactory)
  {
    this.root = root;
    this.viewModelFactory = viewModelFactory;

    bookViewControllerController.init(viewModelFactory.getBookFlightViewModel(),
        bookViewWrapper, viewHandler);
    bookingsViewControllerController.init(viewModelFactory.getMyBookingsViewModel(),
        bookingsViewWrapper, viewHandler);
    passengerViewControllerController.init(
        viewModelFactory.getPassengerDetailsViewModel(), passengerViewWrapper,
        viewHandler);
    seatMapDialogController.init(root, viewHandler,
        viewModelFactory.getSeatMapViewModel(), seatMapDialogWrapper);
    addBookingDialogController.init(viewModelFactory.getMyBookingsViewModel(),
        viewHandler, addBookingDialogWrapper);
    bookingDetailsDialogController.init(viewModelFactory.getMyBookingsViewModel(),
        viewHandler, bookingDetailsDialogWrapper);

    bookTabButton.setOnAction(event -> showBookFlight());
    bookingsTabButton.setOnAction(event -> showMyBookings());
  }

  public Region getRoot()
  {
    return root;
  }

  public void reset()
  {
    viewModelFactory.getBookFlightViewModel().clear();
  }

  public void showBookFlight()
  {
    showContent(bookViewWrapper);
  }

  public void showPassengerDetails()
  {
    showContent(passengerViewWrapper);
    passengerViewControllerController.refresh();
  }

  public void showMyBookings()
  {
    showContent(bookingsViewWrapper);
    bookingsViewControllerController.refresh();
  }

  public void showSeatPicker(int passengerNumber)
  {
    seatMapDialogController.showForPassenger(passengerNumber);
  }

  public void showAddBookingDialog()
  {
    addBookingDialogController.show();
  }

  public void showBookingDetails(Booking booking)
  {
    bookingDetailsDialogController.show(booking);
  }

  public void refreshMyBookings()
  {
    bookingsViewControllerController.refresh();
  }

  private void showContent(StackPane visibleWrapper)
  {
    setVisible(bookViewWrapper, visibleWrapper == bookViewWrapper);
    setVisible(bookingsViewWrapper, visibleWrapper == bookingsViewWrapper);
    setVisible(passengerViewWrapper, visibleWrapper == passengerViewWrapper);

    bookTabButton.getStyleClass().remove("tab-btn-active");
    bookingsTabButton.getStyleClass().remove("tab-btn-active");
    if (visibleWrapper == bookingsViewWrapper)
    {
      bookingsTabButton.getStyleClass().add("tab-btn-active");
    }
    else
    {
      bookTabButton.getStyleClass().add("tab-btn-active");
    }
  }

  private void setVisible(StackPane wrapper, boolean visible)
  {
    wrapper.setVisible(visible);
    wrapper.setManaged(visible);
  }

  @FXML public void loginButton()
  {
    // Dialog flow is not wired yet.
  }
}
