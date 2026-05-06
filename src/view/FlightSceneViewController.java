package view;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import model.Booking;
import viewmodel.ViewModelFactory;

public class FlightSceneViewController
{
  @FXML private Label pageTitleLabel;
  @FXML private Label authStatusLabel;
  @FXML private Button authButton;
  @FXML private Button bookTabButton;
  @FXML private Button bookingsTabButton;
  @FXML private StackPane bookViewWrapper;
  @FXML private StackPane bookingsViewWrapper;
  @FXML private StackPane passengerViewWrapper;
  @FXML private StackPane seatMapDialogWrapper;
  @FXML private StackPane addBookingDialogWrapper;
  @FXML private StackPane bookingDetailsDialogWrapper;

  @FXML private BookFlightViewController bookViewControllerController;
  @FXML private MyBookingsViewController bookingsViewControllerController;
  @FXML private PassengerDetailsViewController passengerViewControllerController;
  @FXML private SeatMapViewController seatMapDialogController;
  @FXML private AddBookingDialogController addBookingDialogController;
  @FXML private BookingDetailsDialogController bookingDetailsDialogController;

  private Region root;
  private ViewHandler viewHandler;

  public void init(Region root, ViewHandler viewHandler,
      ViewModelFactory viewModelFactory)
  {
    this.root = root;
    this.viewHandler = viewHandler;

    bookViewControllerController.init(viewModelFactory.getBookFlightViewModel(),
        root, viewHandler);
    passengerViewControllerController.init(
        viewModelFactory.getPassengerDetailsViewModel(), root, viewHandler);
    bookingsViewControllerController.init(viewModelFactory.getMyBookingsViewModel(),
        root, viewHandler);
    seatMapDialogController.init(root, viewHandler,
        viewModelFactory.getSeatMapViewModel(), seatMapDialogWrapper);
    addBookingDialogController.init(viewModelFactory.getMyBookingsViewModel(),
        viewHandler, addBookingDialogWrapper);
    bookingDetailsDialogController.init(viewModelFactory.getMyBookingsViewModel(),
        viewHandler, bookingDetailsDialogWrapper);

    authStatusLabel.textProperty().bind(Bindings.concat("Customer: ",
        viewModelFactory.getMyBookingsViewModel().customerNameProperty()));
    authButton.setText("Customer");
    authButton.setDisable(true);

    bookTabButton.setOnAction(event -> showBookFlight());
    bookingsTabButton.setOnAction(event -> showMyBookings());

    hideOverlayDialogs();
    showBookFlight();
  }

  public Region getRoot()
  {
    return root;
  }

  public void reset()
  {
    showBookFlight();
  }

  public void showBookFlight()
  {
    showOnly(bookViewWrapper);
    setActiveTab(bookTabButton);
    pageTitleLabel.setText("Travel via VIA");
  }

  public void showPassengerDetails()
  {
    passengerViewControllerController.refresh();
    showOnly(passengerViewWrapper);
    setActiveTab(bookTabButton);
    pageTitleLabel.setText("Passenger details");
  }

  public void showMyBookings()
  {
    bookingsViewControllerController.refresh();
    showOnly(bookingsViewWrapper);
    setActiveTab(bookingsTabButton);
    pageTitleLabel.setText("My Bookings");
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

  private void showOnly(StackPane visibleWrapper)
  {
    setWrapperVisible(bookViewWrapper, visibleWrapper == bookViewWrapper);
    setWrapperVisible(bookingsViewWrapper,
        visibleWrapper == bookingsViewWrapper);
    setWrapperVisible(passengerViewWrapper,
        visibleWrapper == passengerViewWrapper);
  }

  private void setWrapperVisible(StackPane wrapper, boolean visible)
  {
    wrapper.setVisible(visible);
    wrapper.setManaged(visible);
  }

  private void setActiveTab(Button activeButton)
  {
    bookTabButton.getStyleClass().remove("tab-btn-active");
    bookingsTabButton.getStyleClass().remove("tab-btn-active");
    activeButton.getStyleClass().add("tab-btn-active");
  }

  private void hideOverlayDialogs()
  {
    setWrapperVisible(seatMapDialogWrapper, false);
    setWrapperVisible(addBookingDialogWrapper, false);
    setWrapperVisible(bookingDetailsDialogWrapper, false);
  }

  @FXML public void loginButton()
  {
    // The project is intentionally pinned to the demo customer for now.
  }
}
