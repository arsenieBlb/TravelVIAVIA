package view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.*;
import viewmodel.FlightSceneViewModel;
import viewmodel.MyBookingsViewModel;
import viewmodel.PassengerDetailsViewModel;
import viewmodel.SeatMapViewModel;

import java.time.format.DateTimeFormatter;

public class FlightSceneViewController {

    @FXML private StackPane bookViewWrapper;
    @FXML private StackPane bookingsViewWrapper;
    @FXML private StackPane passengerViewWrapper;
    @FXML private StackPane seatMapDialogWrapper;
    @FXML private StackPane loginDialogWrapper;
    @FXML private StackPane adminLoginDialogWrapper;
    @FXML private StackPane registerDialogWrapper;
    @FXML private StackPane addBookingDialogWrapper;
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
    @FXML private PassengerDetailsViewModel passengerDetailsViewModel;
    @FXML private MyBookingsViewModel myBookingsViewModel;
    @FXML private SeatMapViewController seatMapViewController;

    @FXML
    private Region root;
    private ViewHandler viewHandler;
    private FlightSceneViewModel flightSceneViewModel;
    private String pendingCustomerAction;
    @FXML private StackPane bookingDetailsDialogWrapper;

    public void init(Region root, ViewHandler viewHandler, FlightSceneViewModel flightSceneViewModel) {
        this.root = root;
        this.viewHandler = viewHandler;
        this.flightSceneViewModel = flightSceneViewModel;

        this.myBookingsViewModel = flightSceneViewModel.getMyBookingsViewModel();
        this.passengerDetailsViewModel = flightSceneViewModel.getPassengerDetailsViewModel();
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
        resultCountLabel.textProperty().bind(Bindings.size(flightSceneViewModel.getFilteredFlights()).asString());

        setupFilters();
        updateAuthHeader();

        bookTabButton.setOnAction(event -> showBookFlight());
        bookingsTabButton.setOnAction(event -> showMyBookings());
        authButton.setOnAction(event -> handleAuthButton());
        searchFlightsButton.setOnAction(event -> flightSceneViewModel.searchFlights());
        continueButton.setOnAction(event -> showPassengerDetails());

        loginDialogController.init(this);
        adminLoginDialogController.init(this, adminLoginDialogWrapper);
        registerDialogController.init(this, registerDialogWrapper);
        passengerViewController.init(viewHandler, passengerDetailsViewModel, passengerViewWrapper);

        if (bookingsViewController != null) {
            bookingsViewController.init(myBookingsViewModel, bookingsViewWrapper, viewHandler);
        }

        if (bookingDetailsDialogController != null) {
            bookingDetailsDialogController.init(
                    myBookingsViewModel,
                    viewHandler,
                    bookingDetailsDialogWrapper
            );
        }

        directOnlyCheck.selectedProperty().bindBidirectional(flightSceneViewModel.directOnlyProperty());
        directOnlyCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            flightSceneViewModel.searchFlights();
        });

        // roundtrip toggle
        if (roundTripRadio != null) {
            roundTripRadio.selectedProperty().bindBidirectional(flightSceneViewModel.roundTripProperty());
            roundTripRadio.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                returnDateBox.setVisible(isSelected);
                returnDateBox.setManaged(isSelected);
                returnTableSection.setVisible(isSelected);
                returnTableSection.setManaged(isSelected);
                if (!isSelected) {
                    flightSceneViewModel.setSelectedReturnFlight(null);
                }
            });
        }
        if (returnDatePicker != null) {
            returnDatePicker.valueProperty().bindBidirectional(flightSceneViewModel.returnDateProperty());
        }

        // setup the return flights table
        if (returnFlightsTable != null) {
            returnFlightsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            setupReturnTableColumns();
            returnFlightsTable.setItems(flightSceneViewModel.getReturnFlights());
            returnFlightsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldFlight, newFlight) -> {
                    flightSceneViewModel.setSelectedReturnFlight(newFlight);
                    updateFlightSummary(flightSceneViewModel.getSelectedFlight());
                });
        }

        if (seatMapDialogController != null) {
            seatMapDialogController.init(root, viewHandler, smVM, seatMapDialogWrapper);
        }

        if (addBookingDialogController != null) {
            this.myBookingsViewModel = flightSceneViewModel.getMyBookingsViewModel();

            addBookingDialogController.init(
                    myBookingsViewModel,
                    viewHandler,
                    addBookingDialogWrapper
            );
        }
    }

    private void setupFlightTable() {
        flightsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
            // checks if we should say direct or 1 stop
            if (cellData.getValue() instanceof ConnectingFlight connectingFlight) {
                return new SimpleStringProperty("1 Stop in " + connectingFlight.getFirstSegment().getArrivalCity().getCityName());
            } else {
                return new SimpleStringProperty("Direct");
            }
        });
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("EUR %.0f", cellData.getValue().getBasePrice())));
    }

    private void updateFlightSummary(Flight flight) {
        boolean hasFlight = flight != null;
        flightSummaryCard.setVisible(hasFlight);
        flightSummaryCard.setManaged(hasFlight);
        if (hasFlight) {
            detailDateLabel.setText(flight.getDepartureTime()
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")));
            detailPriceLabel.textProperty().bind(
                    flightSceneViewModel.totalPriceProperty().asString("EUR %.2f")
            );

            // update the label to show if it has a stop
            if (flight instanceof ConnectingFlight connectingFlight) {
                detailRouteLabel.setText(connectingFlight.getFirstSegment().getDepartureCity().getCityName() + " → "
                        + connectingFlight.getFirstSegment().getArrivalCity().getCityName() + " → "
                        + connectingFlight.getSecondSegment().getArrivalCity().getCityName());
                detailInfoLabel.setText(flight.getDurationString() + " - 1 Stop");
                detailAircraftLabel.setText(connectingFlight.getFirstSegment().getPlane().getPlaneType().getModel() + " & "
                        + connectingFlight.getSecondSegment().getPlane().getPlaneType().getModel());
            } else {
                detailRouteLabel.setText(flight.getDepartureCity().getCityName() + " → "
                        + flight.getArrivalCity().getCityName());
                detailInfoLabel.setText(flight.getDurationString() + " - Direct");
                detailAircraftLabel.setText(flight.getPlane().getPlaneType().getModel());
            }

            // append return flight info if roundtrip
            Flight returnFlight = flightSceneViewModel.getSelectedReturnFlight();
            if (returnFlight != null) {
                String returnRoute = returnFlight.getDepartureCity().getCityName() + " → "
                        + returnFlight.getArrivalCity().getCityName();
                detailRouteLabel.setText(detailRouteLabel.getText() + "\n↩ " + returnRoute);
                detailInfoLabel.setText(detailInfoLabel.getText() + "\n↩ " + returnFlight.getDurationString());
            }
        }
    }

    private void setupReturnTableColumns() {
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
            if (cellData.getValue() instanceof ConnectingFlight connectingFlight) {
                return new SimpleStringProperty("1 Stop in " + connectingFlight.getFirstSegment().getArrivalCity().getCityName());
            } else {
                return new SimpleStringProperty("Direct");
            }
        });
        returnPriceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("EUR %.0f", cellData.getValue().getBasePrice())));
    }

    private void setupFilters() {
        if (sortByCombo != null) {
            sortByCombo.getItems().setAll("Price (Low to High)", "Duration (Shortest First)", "Departure (Early First)");
            sortByCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    flightSceneViewModel.sortFlights(newVal);
                }
            });
        }

        if (airlineCombo != null) {
            airlineCombo.setItems(flightSceneViewModel.getUniqueCarriers());
            if (!airlineCombo.getItems().contains("All Airlines")) {
                airlineCombo.getItems().add(0, "All Airlines");
            }
            airlineCombo.getSelectionModel().selectFirst();
            airlineCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    flightSceneViewModel.filterByCarrier(newVal);
                }
            });
        }
    }

    @FXML
    private void onResetClick() {
        flightSceneViewModel.clear();
        if (sortByCombo != null) {
            sortByCombo.getSelectionModel().clearSelection();
        }
        if (airlineCombo != null) {
            airlineCombo.getSelectionModel().selectFirst();
        }
        if (oneWayRadio != null) {
            oneWayRadio.setSelected(true);
        }
        returnDateBox.setVisible(false);
        returnDateBox.setManaged(false);
        returnTableSection.setVisible(false);
        returnTableSection.setManaged(false);
        updateFlightSummary(null);
        updateAuthHeader();
    }

    private void setupCityConverter() {
        StringConverter<City> cityConverter = new StringConverter<>() {
            @Override
            public String toString(City city) { return (city == null) ? "" : city.getCityName(); }
            @Override
            public City fromString(String string) { return null; }
        };
        originCombo.setConverter(cityConverter);
        destinationCombo.setConverter(cityConverter);
    }

    public void showBookFlight() { showContent(bookViewWrapper); }

    public void showPassengerDetails() {
        if (flightSceneViewModel.getSelectedFlight() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a flight first.");
            alert.showAndWait();
            return;
        }
        if (flightSceneViewModel.roundTripProperty().get() && flightSceneViewModel.getSelectedReturnFlight() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a return flight.");
            alert.showAndWait();
            return;
        }
        if (!isCustomerLoggedIn()) {
            pendingCustomerAction = "passengerDetails";
            showLoginDialog();
            return;
        }
        showContent(passengerViewWrapper);
        passengerViewController.refresh();
    }

    public void showMyBookings() {
        if (!isCustomerLoggedIn()) {
            pendingCustomerAction = "myBookings";
            showLoginDialog();
            return;
        }
        showContent(bookingsViewWrapper);
        bookingsViewController.refresh();
    }

    private void handleAuthButton() {
        if (flightSceneViewModel.getLoggedInUser() == null) {
            showLoginDialog();
        } else {
            flightSceneViewModel.logout();
            pendingCustomerAction = null;
            updateAuthHeader();
            showBookFlight();
        }
    }

    public boolean loginCustomer(String email, String password) {
        boolean success = flightSceneViewModel.login(email, password);
        if (success && flightSceneViewModel.getLoggedInUser() instanceof Customer) {
            updateAuthHeader();
            runPendingCustomerAction();
            return true;
        }
        flightSceneViewModel.logout();
        updateAuthHeader();
        return false;
    }

    private void runPendingCustomerAction() {
        String action = pendingCustomerAction;
        pendingCustomerAction = null;
        if ("passengerDetails".equals(action)) showPassengerDetails();
        else if ("myBookings".equals(action)) showMyBookings();
    }

    private void updateAuthHeader() {
        User user = flightSceneViewModel.getLoggedInUser();

        if (user instanceof Customer customer) {
            authStatusLabel.setText("Welcome, " + customer.getFirstName());
            authButton.setText("Logout");
        }
        else if (user instanceof Admin admin) {
            authStatusLabel.setText("Logged in as Admin");
            authButton.setText("Logout");
        }
        else {
            authStatusLabel.setText("Not signed in");
            authButton.setText("Login");
        }
    }

    private void showContent(StackPane visibleWrapper) {
        setVisible(bookViewWrapper, visibleWrapper == bookViewWrapper);
        setVisible(bookingsViewWrapper, visibleWrapper == bookingsViewWrapper);
        setVisible(passengerViewWrapper, visibleWrapper == passengerViewWrapper);

        bookTabButton.getStyleClass().remove("tab-btn-active");
        bookingsTabButton.getStyleClass().remove("tab-btn-active");
        if (visibleWrapper == bookingsViewWrapper) bookingsTabButton.getStyleClass().add("tab-btn-active");
        else bookTabButton.getStyleClass().add("tab-btn-active");
    }

    private void setVisible(StackPane wrapper, boolean visible) {
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

    public void showSeatPicker(int passengerNumber, int segmentIndex) {
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

    public boolean loginAdmin(String email, String password) {
        if (email == null || password == null) return false;

        if (!flightSceneViewModel.login(email.trim(), password.trim())
                || !(flightSceneViewModel.getLoggedInUser() instanceof Admin)) {

            flightSceneViewModel.logout();
            updateAuthHeader();
            return false;
        }

        updateAuthHeader();
        viewHandler.openView("admin");

        return true;
    }

    public boolean registerCustomer(String firstName, String lastName, String email, String password) {
        return flightSceneViewModel.register(firstName.trim(), lastName.trim(), email.trim(), password.trim());
    }

    private boolean isCustomerLoggedIn() { return flightSceneViewModel.getLoggedInUser() instanceof Customer; }
    public void showLoginDialog() {
        hideAuthDialogs();
        loginDialogWrapper.setVisible(true);
        loginDialogWrapper.setManaged(true);
        loginDialogController.show();
    }
    public void reset() { flightSceneViewModel.clear(); }
    public Region getRoot() { return root; }

    @FXML private void onIncrementClick() { flightSceneViewModel.incrementPassengers(); }
    @FXML private void onDecrementClick() { flightSceneViewModel.decrementPassengers(); }
    @FXML public void loginButton() { handleAuthButton(); }

    public void refresh() {

    }

    @FXML
    private void onSearchClick() {
        flightSceneViewModel.searchFlights();
    }

    @FXML
    private void onContinueClick() {
        showPassengerDetails();
    }

    public MyBookingsViewController getMyBookingsViewController() {
        return bookingsViewController;
    }
}