package client.view;

import client.model.Admin;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import client.model.*;
import client.viewmodel.FlightSceneViewModel;
import client.viewmodel.MyBookingsViewModel;
import client.viewmodel.PassengerDetailsViewModel;
import client.viewmodel.SeatMapViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FlightSceneViewController
{

    @FXML private StackPane bookViewWrapper;
    @FXML private StackPane bookingsViewWrapper;
    @FXML private StackPane passengerViewWrapper;
    @FXML private StackPane seatMapDialogWrapper;
    @FXML private StackPane loginDialogWrapper;
    @FXML private StackPane adminLoginDialogWrapper;
    @FXML private StackPane registerDialogWrapper;
    @FXML private StackPane addBookingDialogWrapper;
    @FXML private StackPane bookingDetailsDialogWrapper;

    @FXML private Button bookTabButton;
    @FXML private Button bookingsTabButton;
    @FXML private Button authButton;
    @FXML private Label authStatusLabel;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private ComboBox<String> airlineCombo;

    @FXML private ComboBox<City> originCombo;
    @FXML private ComboBox<City> destinationCombo;
    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> routeColumn;
    @FXML private TableColumn<Flight, String> departureColumn;
    @FXML private TableColumn<Flight, String> durationColumn;
    @FXML private TableColumn<Flight, String> carrierColumn;
    @FXML private TableColumn<Flight, String> typeColumn;
    @FXML private TableColumn<Flight, String> priceColumn;
    @FXML private Label resultCountLabel;
    @FXML private VBox flightSummaryCard;
    @FXML private Label detailRouteLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailInfoLabel;
    @FXML private Label detailPriceLabel;
    @FXML private Label detailAircraftLabel;
    @FXML private Button searchFlightsButton;
    @FXML private Button continueButton;
    @FXML private Label passengerLabel;
    @FXML private Button decreasePassengersButton;
    @FXML private DatePicker travelDatePicker;
    @FXML private CheckBox directOnlyCheck;
    @FXML private RadioButton oneWayRadio;
    @FXML private RadioButton roundTripRadio;
    @FXML private VBox returnDateBox;
    @FXML private DatePicker returnDatePicker;
    @FXML private VBox returnTableSection;
    @FXML private TableView<Flight> returnFlightsTable;
    @FXML private TableColumn<Flight, String> returnRouteColumn;
    @FXML private TableColumn<Flight, String> returnDepartureColumn;
    @FXML private TableColumn<Flight, String> returnDurationColumn;
    @FXML private TableColumn<Flight, String> returnCarrierColumn;
    @FXML private TableColumn<Flight, String> returnTypeColumn;
    @FXML private TableColumn<Flight, String> returnPriceColumn;

    @FXML private MyBookingsViewController bookingsViewController;
    @FXML private PassengerDetailsViewController passengerViewController;
    @FXML private SeatMapViewController seatMapDialogController;
    @FXML private LoginDialogController loginDialogController;
    @FXML private AdminLoginDialogController adminLoginDialogController;
    @FXML private RegisterDialogController registerDialogController;
    @FXML private AddBookingDialogController addBookingDialogController;
    @FXML private BookingDetailsDialogController bookingDetailsDialogController;

    private Region root;
    private ViewHandler viewHandler;
    private FlightSceneViewModel flightSceneViewModel;
    private String pendingCustomerAction;

    public void init(ViewHandler viewHandler, FlightSceneViewModel flightSceneViewModel, Region root)
    {
        this.root = root;
        this.viewHandler = viewHandler;
        this.flightSceneViewModel = flightSceneViewModel;

        MyBookingsViewModel myBookingsViewModel = flightSceneViewModel.getMyBookingsViewModel();
        PassengerDetailsViewModel passengerDetailsViewModel = flightSceneViewModel.getPassengerDetailsViewModel();
        SeatMapViewModel smVM = flightSceneViewModel.getSeatMapViewModel();
        flightSceneViewModel.setSeatMapViewModel(smVM);

        smVM.temporarySelectionProperty().addListener((obs, oldSeat, newSeat) -> {
            flightSceneViewModel.updateTotalPrice();
        });

        setupCityConverter();
        setupFlightTable();

        originCombo.setItems(flightSceneViewModel.getAllCities());
        destinationCombo.setItems(flightSceneViewModel.getFilteredDestinations());

        originCombo.valueProperty().bindBidirectional(flightSceneViewModel.departureCityProperty());
        destinationCombo.valueProperty().bindBidirectional(flightSceneViewModel.arrivalCityProperty());

        flightsTable.setItems(flightSceneViewModel.getFilteredFlights());
        flightsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldFlight, newFlight) -> {
                    flightSceneViewModel.setSelectedFlight(newFlight);
                    updateFlightSummary(newFlight);
                });

        passengerLabel.textProperty().bind(flightSceneViewModel.passengerCountProperty().asString());
        decreasePassengersButton.disableProperty().bind(
                flightSceneViewModel.passengerCountProperty().lessThanOrEqualTo(1)
        );

        travelDatePicker.valueProperty().bindBidirectional(flightSceneViewModel.travelDateProperty());

        // block dates that already passed
        travelDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        resultCountLabel.textProperty().bind(Bindings.size(flightSceneViewModel.getFilteredFlights()).asString());

        setupFilters();
        updateAuthHeader();

        bookTabButton.setOnAction(event -> showBookFlight());
        bookingsTabButton.setOnAction(event -> showMyBookings());
        authButton.setOnAction(event -> handleAuthButton());
        searchFlightsButton.setOnAction(event -> {
            if (flightSceneViewModel.roundTripProperty().get())
            {
                java.time.LocalDate travel = flightSceneViewModel.travelDateProperty().get();
                java.time.LocalDate ret = flightSceneViewModel.returnDateProperty().get();
                if (ret != null && travel != null && ret.isBefore(travel))
                {
                    showAlert(Alert.AlertType.ERROR, "Invalid Date",
                            "Return date must be on or after the departure date.");
                    return;
                }
            }
            flightSceneViewModel.searchFlights();
        });
        continueButton.setOnAction(event -> showPassengerDetails());

        if (loginDialogController != null)
        {
            loginDialogController.init(this);
        }
        if (adminLoginDialogController != null)
        {
            adminLoginDialogController.init(this, adminLoginDialogWrapper);
        }
        if (registerDialogController != null)
        {
            registerDialogController.init(this, registerDialogWrapper);
        }
        if (passengerViewController != null)
        {
            passengerViewController.init(this, passengerDetailsViewModel, passengerViewWrapper);
        }
        if (bookingsViewController != null)
        {
            bookingsViewController.init(this, myBookingsViewModel, bookingsViewWrapper);
        }
        if (bookingDetailsDialogController != null)
        {
            bookingDetailsDialogController.init(myBookingsViewModel, viewHandler, bookingDetailsDialogWrapper);
        }
        if (seatMapDialogController != null)
        {
            seatMapDialogController.init(root, viewHandler, smVM, seatMapDialogWrapper);
        }
        if (addBookingDialogController != null)
        {
            addBookingDialogController.init(myBookingsViewModel, viewHandler, addBookingDialogWrapper);
        }

        directOnlyCheck.selectedProperty().bindBidirectional(flightSceneViewModel.directOnlyProperty());
        directOnlyCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            flightSceneViewModel.searchFlights();
        });

        if (roundTripRadio != null)
        {
            roundTripRadio.selectedProperty().bindBidirectional(flightSceneViewModel.roundTripProperty());
            roundTripRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                returnDateBox.setVisible(isSelected);
                returnDateBox.setManaged(isSelected);
                returnTableSection.setVisible(isSelected);
                returnTableSection.setManaged(isSelected);
                if (!isSelected)
                {
                    flightSceneViewModel.setSelectedReturnFlight(null);
                }
            });
        }
        if (returnDatePicker != null)
        {
            returnDatePicker.valueProperty().bindBidirectional(flightSceneViewModel.returnDateProperty());

            // block dates before the travel date
            returnDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate earliest = travelDatePicker.getValue();
                    if (earliest == null) earliest = LocalDate.now();
                    setDisable(empty || date.isBefore(earliest));
                }
            });
        }

        if (returnFlightsTable != null)
        {
            returnFlightsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            returnFlightsTable.setRowFactory(tv -> {
                TableRow<Flight> row = new TableRow<>();
                row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                    if (!row.isEmpty() && row.isSelected()) {
                        returnFlightsTable.getSelectionModel().clearSelection();
                        flightSceneViewModel.setSelectedReturnFlight(null);
                        updateFlightSummary(flightSceneViewModel.getSelectedFlight());
                        event.consume();
                    }
                });
                return row;
            });
            setupReturnTableColumns();
            returnFlightsTable.setItems(flightSceneViewModel.getReturnFlights());
            returnFlightsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldFlight, newFlight) -> {
                        flightSceneViewModel.setSelectedReturnFlight(newFlight);
                        updateFlightSummary(flightSceneViewModel.getSelectedFlight());
                    });
        }
    }

    private void setupFlightTable()
    {
        flightsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        flightsTable.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!row.isEmpty() && row.isSelected()) {
                    flightsTable.getSelectionModel().clearSelection();
                    flightSceneViewModel.setSelectedFlight(null);
                    updateFlightSummary(null);
                    event.consume();
                }
            });
            return row;
        });
        routeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartureCity().getCityName() + " → "
                        + cellData.getValue().getArrivalCity().getCityName()));
        departureColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        durationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDurationString()));
        carrierColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCarrier().getName() + "\n"
                        + cellData.getValue().getFlightNumber()));
        typeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof ConnectingFlight connectingFlight)
            {
                return new SimpleStringProperty("1 Stop in " + connectingFlight.getFirstSegment().getArrivalCity().getCityName());
            }
            else
            {
                return new SimpleStringProperty("Direct");
            }
        });
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("EUR %.0f", cellData.getValue().getBasePrice())));
    }

    private void updateFlightSummary(Flight flight)
    {
        boolean hasFlight = flight != null;
        flightSummaryCard.setVisible(hasFlight);
        flightSummaryCard.setManaged(hasFlight);
        if (hasFlight)
        {
            detailDateLabel.setText(flight.getDepartureTime()
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")));
            detailPriceLabel.textProperty().bind(
                    flightSceneViewModel.totalPriceProperty().asString("EUR %.2f")
            );

            if (flight instanceof ConnectingFlight connectingFlight)
            {
                detailRouteLabel.setText(connectingFlight.getFirstSegment().getDepartureCity().getCityName() + " → "
                        + connectingFlight.getFirstSegment().getArrivalCity().getCityName() + " → "
                        + connectingFlight.getSecondSegment().getArrivalCity().getCityName());
                detailInfoLabel.setText(flight.getDurationString() + " - 1 Stop");
                detailAircraftLabel.setText(connectingFlight.getFirstSegment().getPlane().getPlaneType().getModel() + " & "
                        + connectingFlight.getSecondSegment().getPlane().getPlaneType().getModel());
            }
            else
            {
                detailRouteLabel.setText(flight.getDepartureCity().getCityName() + " → "
                        + flight.getArrivalCity().getCityName());
                detailInfoLabel.setText(flight.getDurationString() + " - Direct");
                detailAircraftLabel.setText(flight.getPlane().getPlaneType().getModel());
            }

            Flight returnFlight = flightSceneViewModel.getSelectedReturnFlight();
            if (returnFlight != null)
            {
                String returnRoute = returnFlight.getDepartureCity().getCityName() + " → "
                        + returnFlight.getArrivalCity().getCityName();
                detailRouteLabel.setText(detailRouteLabel.getText() + "\n↩ " + returnRoute);
                detailInfoLabel.setText(detailInfoLabel.getText() + "\n↩ " + returnFlight.getDurationString());
            }
        }
    }

    private void setupReturnTableColumns()
    {
        returnRouteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartureCity().getCityName() + " \u2192 "
                        + cellData.getValue().getArrivalCity().getCityName()));
        returnDepartureColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        returnDurationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDurationString()));
        returnCarrierColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCarrier().getName() + "\n"
                        + cellData.getValue().getFlightNumber()));
        returnTypeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof ConnectingFlight connectingFlight)
            {
                return new SimpleStringProperty("1 Stop in " + connectingFlight.getFirstSegment().getArrivalCity().getCityName());
            }
            else
            {
                return new SimpleStringProperty("Direct");
            }
        });
        returnPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("EUR %.0f", cellData.getValue().getBasePrice())));
    }

    private void setupFilters()
    {
        if (sortByCombo != null)
        {
            sortByCombo.getItems().setAll("Price (Low to High)", "Duration (Shortest First)", "Departure (Early First)");
            sortByCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                if (newVal != null)
                {
                    flightSceneViewModel.sortFlights(newVal);
                }
            });
        }

        if (airlineCombo != null)
        {
            airlineCombo.setItems(flightSceneViewModel.getUniqueCarriers());
            if (!airlineCombo.getItems().contains("All Airlines"))
            {
                airlineCombo.getItems().add(0, "All Airlines");
            }
            airlineCombo.getSelectionModel().selectFirst();
            airlineCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                if (newVal != null)
                {
                    flightSceneViewModel.filterByCarrier(newVal);
                }
            });
        }
    }

    @FXML
    private void onResetClick()
    {
        flightSceneViewModel.clear();
        if (sortByCombo != null)
        {
            sortByCombo.getSelectionModel().clearSelection();
        }
        if (airlineCombo != null)
        {
            airlineCombo.getSelectionModel().selectFirst();
        }
        if (oneWayRadio != null)
        {
            oneWayRadio.setSelected(true);
        }
        returnDateBox.setVisible(false);
        returnDateBox.setManaged(false);
        returnTableSection.setVisible(false);
        returnTableSection.setManaged(false);
        updateFlightSummary(null);
        updateAuthHeader();
    }

    private void setupCityConverter()
    {
        StringConverter<City> cityConverter = new StringConverter<>()
        {
            @Override
            public String toString(City city)
            {
                return (city == null) ? "" : city.getCityName();
            }

            @Override
            public City fromString(String string)
            {
                return null;
            }
        };
        originCombo.setConverter(cityConverter);
        destinationCombo.setConverter(cityConverter);
    }

    public void showBookFlight()
    {
        showContent(bookViewWrapper);
    }

    public void showPassengerDetails()
    {
        if (flightSceneViewModel.getSelectedFlight() == null)
        {
            showAlert(Alert.AlertType.ERROR, "Selection Required", "Please select a flight first.");
            return;
        }
        if (flightSceneViewModel.roundTripProperty().get() && flightSceneViewModel.getSelectedReturnFlight() == null)
        {
            showAlert(Alert.AlertType.ERROR, "Selection Required", "Please select a return flight.");
            return;
        }
        if (!isCustomerLoggedIn())
        {
            pendingCustomerAction = "passengerDetails";
            showLoginDialog();
            return;
        }
        showContent(passengerViewWrapper);
        passengerViewController.refresh();
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
        bookingsViewController.refresh();
    }

    private void handleAuthButton()
    {
        if (flightSceneViewModel.getLoggedInUser() == null)
        {
            showLoginDialog();
        }
        else
        {
            flightSceneViewModel.logout();
            pendingCustomerAction = null;
            updateAuthHeader();
            showAlert(Alert.AlertType.INFORMATION, "Logged Out", "You have been logged out.");
            showBookFlight();
        }
    }

    public boolean loginCustomer(String email, String password)
    {
        boolean success = flightSceneViewModel.login(email, password);
        if (success && flightSceneViewModel.getLoggedInUser() instanceof Customer)
        {
            updateAuthHeader();
            runPendingCustomerAction();
            return true;
        }
        flightSceneViewModel.logout();
        updateAuthHeader();
        return false;
    }



    private void runPendingCustomerAction()
    {
        String action = pendingCustomerAction;
        pendingCustomerAction = null;
        if ("passengerDetails".equals(action)) showPassengerDetails();
        else if ("myBookings".equals(action)) showMyBookings();
    }

    private void updateAuthHeader()
    {
        User user = flightSceneViewModel.getLoggedInUser();

        if (user instanceof Customer customer)
        {
            authStatusLabel.setText("Welcome, " + customer.getFirstName());
            authButton.setText("Logout");
        }
        else if (user instanceof Admin admin)
        {
            authStatusLabel.setText("Logged in as Admin");
            authButton.setText("Logout");
        }
        else
        {
            authStatusLabel.setText("Not signed in");
            authButton.setText("Login");
        }
    }

    private void showContent(StackPane visibleWrapper)
    {
        setVisible(bookViewWrapper, visibleWrapper == bookViewWrapper);
        setVisible(bookingsViewWrapper, visibleWrapper == bookingsViewWrapper);
        setVisible(passengerViewWrapper, visibleWrapper == passengerViewWrapper);

        bookTabButton.getStyleClass().remove("tab-btn-active");
        bookingsTabButton.getStyleClass().remove("tab-btn-active");
        if (visibleWrapper == bookingsViewWrapper) bookingsTabButton.getStyleClass().add("tab-btn-active");
        else bookTabButton.getStyleClass().add("tab-btn-active");
    }

    private void setVisible(StackPane wrapper, boolean visible)
    {
        wrapper.setVisible(visible);
        wrapper.setManaged(visible);
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

    public void showSeatPicker(int passengerNumber, int segmentIndex)
    {
        seatMapDialogController.showForPassenger(passengerNumber, segmentIndex);
    }

    public void showBookingDetails(Booking booking)
    {
        bookingDetailsDialogController.show(booking);
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

    public void hideAuthDialogs()
    {
        loginDialogWrapper.setVisible(false);
        loginDialogWrapper.setManaged(false);

        registerDialogWrapper.setVisible(false);
        registerDialogWrapper.setManaged(false);

        adminLoginDialogWrapper.setVisible(false);
        adminLoginDialogWrapper.setManaged(false);
    }

    public boolean loginAdmin(String email, String password)
    {
        if (email == null || password == null) return false;

        if (!flightSceneViewModel.login(email.trim(), password.trim())
                || !(flightSceneViewModel.getLoggedInUser() instanceof Admin))
        {

            flightSceneViewModel.logout();
            updateAuthHeader();
            return false;
        }

        updateAuthHeader();
        viewHandler.openView("admin");
        return true;
    }

    public boolean registerCustomer(String firstName, String lastName, String email, String password)
    {
        return flightSceneViewModel.register(firstName.trim(), lastName.trim(), email.trim(), password.trim());
    }

    private boolean isCustomerLoggedIn()
    {
        return flightSceneViewModel.getLoggedInUser() instanceof Customer;
    }

    public void showLoginDialog()
    {
        hideAuthDialogs();
        loginDialogWrapper.setVisible(true);
        loginDialogWrapper.setManaged(true);
        loginDialogController.show();
    }

    public void reset()
    {
        if (authStatusLabel != null)
        {
            authStatusLabel.setText("Not signed in");
        }
        if (authButton != null)
        {
            authButton.setText("Login");
        }
        if (flightSceneViewModel != null)
        {
            flightSceneViewModel.clear();
        }
    }

    public Region getRoot()
    {
        return root;
    }

    @FXML private void onIncrementClick()
    {
        flightSceneViewModel.incrementPassengers();
    }

    @FXML private void onDecrementClick()
    {
        flightSceneViewModel.decrementPassengers();
    }

    @FXML public void loginButton()
    {
        handleAuthButton();
    }

    @FXML private void onSearchClick()
    {
        flightSceneViewModel.searchFlights();
    }

    @FXML private void onContinueClick()
    {
        showPassengerDetails();
    }

    public MyBookingsViewController getMyBookingsViewController()
    {
        return bookingsViewController;
    }

    private void showAlert(Alert.AlertType type, String title, String message)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
