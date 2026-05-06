package view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import model.Admin;
import model.Booking;
import model.Customer;
import model.Model;
import model.User;
import viewmodel.ViewModelFactory;

public class FlightSceneViewController
{
  @FXML private StackPane bookViewWrapper;
  @FXML private StackPane bookingsViewWrapper;
  @FXML private StackPane passengerViewWrapper;
  @FXML private StackPane seatMapDialogWrapper;
  @FXML private StackPane addBookingDialogWrapper;
  @FXML private StackPane bookingDetailsDialogWrapper;
  @FXML private StackPane loginDialogWrapper;
  @FXML private StackPane adminLoginDialogWrapper;
  @FXML private StackPane registerDialogWrapper;
  @FXML private Button bookTabButton;
  @FXML private Button bookingsTabButton;
  @FXML private Button authButton;
  @FXML private Label authStatusLabel;

  @FXML private BookFlightViewController bookViewControllerController;
  @FXML private MyBookingsViewController bookingsViewControllerController;
  @FXML private PassengerDetailsViewController passengerViewControllerController;
  @FXML private SeatMapViewController seatMapDialogController;
  @FXML private AddBookingDialogController addBookingDialogController;
  @FXML private BookingDetailsDialogController bookingDetailsDialogController;
  @FXML private LoginDialogController loginDialogController;
  @FXML private AdminLoginDialogController adminLoginDialogController;
  @FXML private RegisterDialogController registerDialogController;

  @FXML private Region root;

  private Model model;
  private ViewModelFactory viewModelFactory;
  private String pendingCustomerAction;

  public void init(Region root, ViewHandler viewHandler,
      ViewModelFactory viewModelFactory)
  {
    this.root = root;
    this.viewModelFactory = viewModelFactory;
    this.model = viewModelFactory.getModel();

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
    loginDialogController.init(this, loginDialogWrapper);
    adminLoginDialogController.init(this, adminLoginDialogWrapper);
    registerDialogController.init(this, registerDialogWrapper);

    bookTabButton.setOnAction(event -> showBookFlight());
    bookingsTabButton.setOnAction(event -> showMyBookings());
    authButton.setOnAction(event -> handleAuthButton());
    updateAuthHeader();
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
    if (!isCustomerLoggedIn())
    {
      pendingCustomerAction = "passengerDetails";
      showLoginDialog();
      return;
    }
    showContent(passengerViewWrapper);
    passengerViewControllerController.refresh();
  }

  public void showMyBookings()
  {
    if (!isCustomerLoggedIn())
    {
      pendingCustomerAction = "myBookings";
      showLoginDialog();
      return;
    }
    showContent(bookingsViewWrapper);
    bookingsViewControllerController.refresh();
  }

  public void showSeatPicker(int passengerNumber)
  {
    seatMapDialogController.showForPassenger(passengerNumber);
  }

  public void showAddBookingDialog()
  {
    if (!isCustomerLoggedIn())
    {
      pendingCustomerAction = "myBookings";
      showLoginDialog();
      return;
    }
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

  public void showLoginDialog()
  {
    hideAuthDialogs();
    loginDialogController.show();
  }

  public void showAdminLoginDialog()
  {
    hideAuthDialogs();
    adminLoginDialogController.show();
  }

  public void showRegisterDialog()
  {
    hideAuthDialogs();
    registerDialogController.show();
  }

  public boolean loginCustomer(String email, String password)
  {
    if (!model.login(trim(email), trim(password))
        || !(model.getLoggedInUser() instanceof Customer))
    {
      model.logout();
      updateAuthHeader();
      return false;
    }

    updateAuthHeader();
    runPendingCustomerAction();
    return true;
  }

  public boolean loginAdmin(String email, String password)
  {
    if (!model.login(trim(email), trim(password))
        || !(model.getLoggedInUser() instanceof Admin))
    {
      model.logout();
      updateAuthHeader();
      return false;
    }

    pendingCustomerAction = null;
    updateAuthHeader();
    showBookFlight();
    return true;
  }

  public boolean registerCustomer(String firstName, String lastName,
      String email, String password)
  {
    return model.register(trim(firstName), trim(lastName), trim(email),
        trim(password));
  }

  private void handleAuthButton()
  {
    if (model.getLoggedInUser() == null)
    {
      showLoginDialog();
      return;
    }

    model.logout();
    pendingCustomerAction = null;
    updateAuthHeader();
    showBookFlight();
  }

  private void runPendingCustomerAction()
  {
    String action = pendingCustomerAction;
    pendingCustomerAction = null;

    if ("passengerDetails".equals(action))
    {
      showPassengerDetails();
    }
    else if ("myBookings".equals(action))
    {
      showMyBookings();
    }
  }

  private void updateAuthHeader()
  {
    User user = model.getLoggedInUser();
    if (user instanceof Customer customer)
    {
      authStatusLabel.setText("Welcome, " + customer.getFirstName());
      authButton.setText("Logout");
    }
    else if (user instanceof Admin)
    {
      authStatusLabel.setText("Admin signed in");
      authButton.setText("Logout");
    }
    else
    {
      authStatusLabel.setText("Not signed in");
      authButton.setText("Login");
    }
  }

  private boolean isCustomerLoggedIn()
  {
    return model.getLoggedInUser() instanceof Customer;
  }

  private void hideAuthDialogs()
  {
    loginDialogController.hide();
    adminLoginDialogController.hide();
    registerDialogController.hide();
  }

  private String trim(String value)
  {
    return value == null ? "" : value.trim();
  }

  @FXML public void loginButton()
  {
    handleAuthButton();
  }
}
