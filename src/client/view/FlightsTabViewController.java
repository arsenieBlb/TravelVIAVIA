package client.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import client.model.BusinessClass;
import client.model.Carrier;
import client.model.City;
import client.model.ConnectingFlight;
import client.model.EconomyClass;
import client.model.Flight;
import client.model.Plane;
import client.model.Seat;
import client.viewmodel.FlightsTabViewModel;
import client.viewmodel.NavigationAdminViewModel;
import client.viewmodel.NavigationAdminViewModel.NavigationTab;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FlightsTabViewController {
    @FXML private Button flightsNavButton, dashboardNavButton, bookingsNavButton;
    @FXML private Button editFlightButton, removeFlightButton;
    @FXML private Region flightsView, bookingsView, dashboardContainer;
    @FXML private StackPane contentHost;
    @FXML private BookingAdminViewController bookingAdminViewController;
    @FXML private DatePicker dateFilterPicker;

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightIdColumn, carrierColumn, aircraftColumn, routeColumn, departureColumn, arrivalColumn;
    @FXML private TextField originFilterField, destinationFilterField, carrierFilterField;
    @FXML private ComboBox<String> aircraftFilterCombo;
    @FXML private Label authStatusLabel;
    @FXML private DashboardViewController dashboardViewController;

    private NavigationAdminViewModel navViewModel;
    private FlightsTabViewModel flightsViewModel;
    private ViewHandler viewHandler;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter editFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    private Region root;
    private StackPane activeFlightOverlay;

    public void init(ViewHandler viewHandler, Region root, NavigationAdminViewModel navViewModel) {
        this.viewHandler = viewHandler;
        this.navViewModel = navViewModel;
        this.flightsViewModel = navViewModel.getFlightsTabViewModel();
        this.root = root;

        setupTable();
        setupBindings();
        setupBookingsView();
        setupNavigation();

        if (dashboardViewController != null)
        {
            dashboardViewController.init(navViewModel.getDashboardViewModel(), root, viewHandler);
        }

        ObservableList<String> aircraftModels = FXCollections.observableArrayList();
        aircraftModels.add("All");
        aircraftModels.addAll(navViewModel.getUniqueAircraftModels());
        aircraftFilterCombo.setItems(aircraftModels);
        aircraftFilterCombo.getSelectionModel().select("All");

        if (flightsViewModel.aircraftFilterProperty().get() == null ||
                flightsViewModel.aircraftFilterProperty().get().isEmpty()) {
            aircraftFilterCombo.getSelectionModel().select("All");
        }
    }

    private void setupTable() {
        flightIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty("FL-" + data.getValue().getFlightId()));

        carrierColumn.setCellValueFactory(data -> {
            String name = (data.getValue().getCarrier() != null) ? data.getValue().getCarrier().getName() : "Unknown";
            return new SimpleStringProperty(name);
        });

        aircraftColumn.setCellValueFactory(data -> {
            if (data.getValue().getPlane() != null && data.getValue().getPlane().getPlaneType() != null) {
                return new SimpleStringProperty(data.getValue().getPlane().getPlaneType().getModel());
            }
            return new SimpleStringProperty("N/A");
        });

        routeColumn.setCellValueFactory(data -> {
            String origin = (data.getValue().getDepartureCity() != null) ? data.getValue().getDepartureCity().getCityName() : "???";
            String dest = (data.getValue().getArrivalCity() != null) ? data.getValue().getArrivalCity().getCityName() : "???";
            return new SimpleStringProperty(origin + " → " + dest);
        });

        departureColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartureTime().format(formatter)));

        arrivalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getArrivalTime().format(formatter)));

        flightsTable.setItems(flightsViewModel.getFilteredFlights());
        flightsTable.setRowFactory(table -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty())
                {
                    Flight flight = row.getItem();
                    Platform.runLater(() -> showFlightDetailsOverlay(flight));
                }
            });
            return row;
        });
    }

    private void setupBindings() {
        dateFilterPicker.valueProperty().bindBidirectional(flightsViewModel.dateFilterProperty());
        originFilterField.textProperty().bindBidirectional(flightsViewModel.originFilterProperty());
        destinationFilterField.textProperty().bindBidirectional(flightsViewModel.destinationFilterProperty());
        carrierFilterField.textProperty().bindBidirectional(flightsViewModel.carrierFilterProperty());
        aircraftFilterCombo.valueProperty().bindBidirectional(flightsViewModel.aircraftFilterProperty());
        hideToolbarButton(editFlightButton);
        hideToolbarButton(removeFlightButton);
        if (authStatusLabel != null)
        {
            authStatusLabel.textProperty().bind(navViewModel.authStatusProperty());
        }
    }

    private void hideToolbarButton(Button button)
    {
        if (button == null)
        {
            return;
        }
        button.setVisible(false);
        button.setManaged(false);
        button.setDisable(true);
    }

    private void setupNavigation() {
        flightsNavButton.setOnAction(e -> showTab(NavigationTab.FLIGHTS));
        bookingsNavButton.setOnAction(e -> showTab(NavigationTab.BOOKINGS));
        dashboardNavButton.setOnAction(e -> showTab(NavigationTab.DASHBOARD));
        showTab(NavigationTab.FLIGHTS);
    }

    private void setupBookingsView() {
        if (bookingAdminViewController != null) {
            bookingAdminViewController.init(
                    navViewModel.getBookingAdminViewModel());
        }
    }

    private void showTab(NavigationTab tab) {
        navViewModel.navigateTo(tab);

        boolean showingFlights = tab == NavigationTab.FLIGHTS;
        boolean showingBookings = tab == NavigationTab.BOOKINGS;
        boolean showingDashboard = tab == NavigationTab.DASHBOARD;

        flightsView.setVisible(showingFlights);
        flightsView.setManaged(showingFlights);
        bookingsView.setVisible(showingBookings);
        bookingsView.setManaged(showingBookings);

        if (dashboardContainer != null) {
            dashboardContainer.setVisible(showingDashboard);
            dashboardContainer.setManaged(showingDashboard);
        }

        setActiveNavButton(flightsNavButton, showingFlights);
        setActiveNavButton(bookingsNavButton, showingBookings);
        setActiveNavButton(dashboardNavButton, showingDashboard);

        if (showingBookings && bookingAdminViewController != null) {
            bookingAdminViewController.refresh();
        }
    }


    private void setActiveNavButton(Button button, boolean active) {
        if (active && !button.getStyleClass().contains("sidebar-link-active")) {
            button.getStyleClass().add("sidebar-link-active");
        } else if (!active) {
            button.getStyleClass().remove("sidebar-link-active");
        }
    }

    public Region getRoot() {
        return root;
    }

    public void clear() {
        if (flightsViewModel != null) {
            flightsViewModel.originFilterProperty().set("");
            flightsViewModel.destinationFilterProperty().set("");
            flightsViewModel.carrierFilterProperty().set("");
            flightsViewModel.aircraftFilterProperty().set("All");
        }
        if (bookingAdminViewController != null) {
            bookingAdminViewController.clearFilters();
        }
    }

    @FXML
    public void clearAllFilters() {
        flightsViewModel.originFilterProperty().set("");
        flightsViewModel.destinationFilterProperty().set("");
        flightsViewModel.carrierFilterProperty().set("");
        flightsViewModel.aircraftFilterProperty().set("All");

        if (flightsViewModel.dateFilterProperty() != null) {
            flightsViewModel.dateFilterProperty().set(null);
        }

        aircraftFilterCombo.getSelectionModel().select("All");
    }

    @FXML
    private void onAddFlightClick()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("add_flight_tab.fxml"));
            Region addFlightRoot = loader.load();

            AddFlightTabController controller = loader.getController();
            controller.init(navViewModel.getAddFlightTabViewModel(), this::refreshTable);

            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            if (flightsTable.getScene() != null)
            {
                dialog.initOwner(flightsTable.getScene().getWindow());
            }
            dialog.setTitle("Add Flight");
            dialog.setScene(new Scene(addFlightRoot));
            dialog.setOnHidden(event -> refreshTable());
            dialog.showAndWait();
        }
        catch (IOException e)
        {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not open add flight view: " + e.getMessage());
        }
    }

    public void refreshTable()
    {
        if (flightsViewModel != null)
        {
            flightsViewModel.refreshFromModel();
        }
        flightsTable.refresh();
    }

    @FXML
    private void onLogoutClick(MouseEvent event)
    {
        if (navViewModel != null)
        {
            navViewModel.logout();
            authStatusLabel.textProperty().unbind();
            authStatusLabel.setText("Not signed in");
        }

        if (viewHandler != null)
        {
            viewHandler.openView("flightScene");
        }
    }

    @FXML
    private void onRemoveFlightClick()
    {
        Flight selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a flight to remove.");
            return;
        }

        showFlightDetailsOverlay(selected);
    }

    @FXML
    private void onEditFlightClick()
    {
        Flight selected = flightsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a flight to edit.");
            return;
        }
        showFlightDetailsOverlay(selected);
    }

    private void showFlightDetailsOverlay(Flight flight)
    {
        StackPane sceneRoot = getSceneStackRoot();
        if (sceneRoot == null)
        {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not open flight details.");
            return;
        }

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("modal-overlay");
        showFlightDetailsContent(overlay, flight);
        if (activeFlightOverlay != null)
        {
            closeOverlay(activeFlightOverlay);
        }
        activeFlightOverlay = overlay;
        sceneRoot.getChildren().add(overlay);
    }

    private void showFlightDetailsContent(StackPane overlay, Flight flight)
    {
        VBox content = createModalCard();

        Label title = new Label(flight.getFlightNumber());
        title.getStyleClass().add("section-title");
        Label subtitle = new Label(getCarrierAircraftText(flight));
        subtitle.getStyleClass().add("section-subtitle");
        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("close-icon-button");
        closeButton.setOnAction(event -> closeOverlay(overlay));
        HBox header = new HBox(12, new VBox(2, title, subtitle),
            new Region(), closeButton);
        HBox.setHgrow(header.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);

        GridPane details = createTwoColumnGrid();
        details.add(createAvailabilityCard("Route", getRouteText(flight)), 0, 0);
        details.add(createAvailabilityCard("Duration",
            flight.getDurationString()), 1, 0);
        details.add(createAvailabilityCard("Departure",
            flight.getDepartureTime().format(editFormatter)), 0, 1);
        details.add(createAvailabilityCard("Arrival",
            flight.getArrivalTime().format(editFormatter)), 1, 1);

        GridPane availability = createTwoColumnGrid();
        availability.add(createAvailabilityCard("Total seats",
            String.valueOf(countSeats(flight, null, false))), 0, 0);
        availability.add(createAvailabilityCard("Taken seats",
            String.valueOf(countSeats(flight, null, false)
                - countSeats(flight, null, true))), 1, 0);
        availability.add(createAvailabilityCard("Available seats",
            String.valueOf(countSeats(flight, null, true))), 0, 1);
        availability.add(createAvailabilityCard("Economy",
            availabilityText(flight, EconomyClass.class)), 1, 1);
        availability.add(createAvailabilityCard("Business",
            availabilityText(flight, BusinessClass.class)), 0, 2);

        GridPane prices = createTwoColumnGrid();
        prices.add(createAvailabilityCard("Economy price",
            formatMoney(flight.getBasePrice())), 0, 0);
        prices.add(createAvailabilityCard("Business price",
            formatMoney(flight.getBasePrice()
                * new BusinessClass().getPriceMultiplier())), 1, 0);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("outline-button");
        editButton.setOnAction(event -> {
            if (hasTakenSeats(flight))
            {
                showAlert(Alert.AlertType.ERROR, "Cannot Edit Flight",
                    "Flight cannot be edited because it has existing bookings.");
                return;
            }
            showFlightEditContent(overlay, flight);
        });
        Button deleteButton = new Button("Remove");
        deleteButton.getStyleClass().add("outline-button");
        deleteButton.setOnAction(event -> confirmRemoveFlight(flight, overlay));
        Button closeBottomButton = new Button("Close");
        closeBottomButton.getStyleClass().add("primary-button");
        closeBottomButton.setOnAction(event -> closeOverlay(overlay));
        HBox buttons = new HBox(10, editButton, deleteButton, closeBottomButton);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttons.getStyleClass().add("separator-line");

        content.getChildren().addAll(header, details, prices, availability,
            createSegmentBlock(flight), buttons);
        overlay.getChildren().setAll(createModalScroll(content));
    }

    private void showFlightEditContent(StackPane overlay, Flight flight)
    {
        VBox content = createModalCard();
        Label title = new Label("Edit Flight");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Flight ID and seat availability are locked.");
        subtitle.getStyleClass().add("section-subtitle");

        TextField flightIdField = new TextField(flight.getFlightNumber());
        lockField(flightIdField);
        ComboBox<Carrier> carrierCombo = new ComboBox<>();
        carrierCombo.getItems().setAll(flightsViewModel.getCarriers());
        carrierCombo.setValue(flight.getCarrier());
        ComboBox<Plane> aircraftCombo = new ComboBox<>();
        aircraftCombo.getItems().setAll(flightsViewModel.getPlanes());
        aircraftCombo.setValue(flight.getPlane());
        aircraftCombo.valueProperty().addListener((obs, oldPlane, newPlane) -> {
            if (newPlane != null)
            {
                carrierCombo.setValue(newPlane.getCarrier());
            }
        });

        ComboBox<City> originCombo = new ComboBox<>();
        originCombo.getItems().setAll(flightsViewModel.getCities());
        originCombo.setValue(flight.getDepartureCity());
        ComboBox<City> destinationCombo = new ComboBox<>();
        destinationCombo.getItems().setAll(flightsViewModel.getCities());
        destinationCombo.setValue(flight.getArrivalCity());

        TextField departureField = new TextField(
            flight.getDepartureTime().format(editFormatter));
        TextField arrivalField = new TextField(
            flight.getArrivalTime().format(editFormatter));
        TextField economyPriceField = new TextField(
            String.format("%.0f", flight.getBasePrice()));
        TextField businessPriceField = new TextField(formatRawMoney(
            flight.getBasePrice() * new BusinessClass().getPriceMultiplier()));

        GridPane form = createTwoColumnGrid();
        form.add(createFieldBox("Flight ID", flightIdField), 0, 0);
        form.add(createFieldBox("Carrier", carrierCombo), 1, 0);
        form.add(createFieldBox("Aircraft", aircraftCombo), 0, 1);
        form.add(createReadOnlyValue("Total seats",
            String.valueOf(countSeats(flight, null, false))), 1, 1);
        form.add(createFieldBox("Origin", originCombo), 0, 2);
        form.add(createFieldBox("Destination", destinationCombo), 1, 2);
        form.add(createFieldBox("Departure", departureField), 0, 3);
        form.add(createFieldBox("Arrival", arrivalField), 1, 3);
        form.add(createFieldBox("Economy price", economyPriceField), 0, 4);
        form.add(createFieldBox("Business price", businessPriceField), 1, 4);

        GridPane availability = createTwoColumnGrid();
        availability.add(createReadOnlyValue("Economy availability",
            availabilityText(flight, EconomyClass.class)), 0, 0);
        availability.add(createReadOnlyValue("Business availability",
            availabilityText(flight, BusinessClass.class)), 1, 0);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("outline-button");
        backButton.setOnAction(event -> showFlightDetailsContent(overlay, flight));
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("outline-button");
        cancelButton.setOnAction(event -> closeOverlay(overlay));
        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(event -> saveFlightChanges(overlay, flight,
            carrierCombo, aircraftCombo, originCombo, destinationCombo,
            departureField, arrivalField, economyPriceField,
            businessPriceField));
        HBox buttons = new HBox(10, backButton, cancelButton, saveButton);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttons.getStyleClass().add("separator-line");

        content.getChildren().addAll(title, subtitle, form, availability,
            buttons);
        overlay.getChildren().setAll(createModalScroll(content));
    }

    private VBox createAvailabilityCard(String labelText, String valueText)
    {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-grid-item");
        Label label = new Label(labelText);
        label.getStyleClass().add("card-label");
        Label value = new Label(valueText);
        value.getStyleClass().add("card-value");
        card.getChildren().addAll(label, value);
        return card;
    }

    private GridPane createTwoColumnGrid()
    {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setPercentWidth(50);
        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setPercentWidth(50);
        grid.getColumnConstraints().addAll(firstColumn, secondColumn);
        return grid;
    }

    private VBox createModalCard()
    {
        VBox content = new VBox(16);
        content.getStyleClass().add("modal-card");
        content.setMaxWidth(820);
        content.setPrefWidth(760);
        content.setStyle("-fx-padding: 24;");
        return content;
    }

    private ScrollPane createModalScroll(VBox content)
    {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMaxWidth(900);
        scrollPane.getStyleClass().add("modal-scroll");
        return scrollPane;
    }

    private VBox createFieldBox(String labelText, Control field)
    {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("modal-form-label");
        field.getStyleClass().add(field instanceof ComboBox<?>
            ? "modal-combo" : "modal-input");
        field.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(label, field);
        return box;
    }

    private VBox createReadOnlyValue(String labelText, String valueText)
    {
        VBox box = new VBox(6);
        box.getStyleClass().add("capacity-card");
        Label label = new Label(labelText);
        label.getStyleClass().add("capacity-title");
        Label value = new Label(valueText);
        value.getStyleClass().add("capacity-value");
        box.getChildren().addAll(label, value);
        return box;
    }

    private VBox createSegmentBlock(Flight flight)
    {
        VBox block = new VBox(8);
        block.getStyleClass().add("seat-panel");
        Label title = new Label("Flight segments");
        title.getStyleClass().add("section-title-small");
        block.getChildren().add(title);

        List<Flight> segments = getSegments(flight);
        for (int i = 0; i < segments.size(); i++)
        {
            Flight segment = segments.get(i);
            Label row = new Label("Segment " + (i + 1) + ": "
                + getRouteText(segment) + " | "
                + segment.getDepartureTime().format(editFormatter) + " - "
                + segment.getArrivalTime().format(editFormatter));
            row.getStyleClass().add("legend-text");
            block.getChildren().add(row);
        }
        return block;
    }

    private String availabilityText(Flight flight,
        Class<?> seatClassType)
    {
        int total = countSeats(flight, seatClassType, false);
        int available = countSeats(flight, seatClassType, true);
        int taken = total - available;
        return available + " available / " + taken + " taken";
    }

    private int countSeats(Flight flight, Class<?> seatClassType,
        boolean onlyAvailable)
    {
        int count = 0;
        for (Flight segment : getSegments(flight))
        {
            if (segment == null || segment.getPlane() == null)
            {
                continue;
            }
            List<Seat> seats = onlyAvailable ? segment.getAvailableSeats()
                : segment.getPlane().getSeats();
            for (Seat seat : seats)
            {
                if (seatClassType == null
                    || seatClassType.isInstance(seat.getSeatClass()))
                {
                    count++;
                }
            }
        }
        return count;
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

    private void confirmRemoveFlight(Flight selected)
    {
        confirmRemoveFlight(selected, null);
    }

    private void confirmRemoveFlight(Flight selected, StackPane overlay)
    {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Remove flight " + selected.getFlightNumber() + "?\n"
            + "This will mark the flight as unavailable for customers.");
        confirm.setTitle("Confirm Removal");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK)
            {
                boolean removed = removeSelectedFlight(selected);
                if (removed && overlay != null)
                {
                    closeOverlay(overlay);
                }
            }
        });
    }

    private boolean removeSelectedFlight(Flight selected)
    {
        try
        {
            flightsViewModel.removeFlight(selected);
            refreshTable();
            flightsTable.getSelectionModel().clearSelection();
            showAlert(Alert.AlertType.INFORMATION, "Flight Removed",
                "Flight " + selected.getFlightNumber() + " has been removed.");
            return true;
        }
        catch (Exception e)
        {
            String message = e.getMessage() == null
                ? "Could not remove flight." : e.getMessage();
            if (!message.contains("existing bookings"))
            {
                message = "Could not remove flight: " + message;
            }
            showAlert(Alert.AlertType.ERROR, "Error", message);
            return false;
        }
    }

    private void saveFlightChanges(StackPane overlay, Flight originalFlight,
        ComboBox<Carrier> carrierCombo, ComboBox<Plane> aircraftCombo,
        ComboBox<City> originCombo, ComboBox<City> destinationCombo,
        TextField departureField, TextField arrivalField,
        TextField economyPriceField, TextField businessPriceField)
    {
        try
        {
            LocalDateTime departure = LocalDateTime.parse(
                departureField.getText().trim(), editFormatter);
            LocalDateTime arrival = LocalDateTime.parse(
                arrivalField.getText().trim(), editFormatter);
            if (!arrival.isAfter(departure))
            {
                showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Arrival time must be after departure time.");
                return;
            }

            double economyPrice = Double.parseDouble(
                economyPriceField.getText().trim());
            if (economyPrice < 0)
            {
                showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Economy price cannot be negative.");
                return;
            }

            double businessPrice = Double.parseDouble(
                businessPriceField.getText().trim());
            if (businessPrice < 0)
            {
                showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Business price cannot be negative.");
                return;
            }

            if (carrierCombo.getValue() == null
                || aircraftCombo.getValue() == null
                || originCombo.getValue() == null
                || destinationCombo.getValue() == null)
            {
                showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please choose carrier, aircraft, origin, and destination.");
                return;
            }
            if (originCombo.getValue().equals(destinationCombo.getValue()))
            {
                showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Origin and destination must be different.");
                return;
            }

            double businessMultiplier = new BusinessClass().getPriceMultiplier();
            double originalBusinessPrice =
                originalFlight.getBasePrice() * businessMultiplier;
            double savedBasePrice = economyPrice;
            if (Math.abs(businessPrice - originalBusinessPrice) > 0.01)
            {
                savedBasePrice = businessPrice / businessMultiplier;
            }

            Flight updatedFlight = new Flight(originalFlight.getFlightId(),
                originalFlight.getFlightNumber(), departure, arrival,
                savedBasePrice, carrierCombo.getValue(),
                aircraftCombo.getValue(), originCombo.getValue(),
                destinationCombo.getValue());
            flightsViewModel.editFlight(updatedFlight);
            refreshTable();
            closeOverlay(overlay);
            showAlert(Alert.AlertType.INFORMATION, "Flight Updated",
                "Flight " + originalFlight.getFlightNumber()
                    + " has been updated.");
        }
        catch (DateTimeParseException e)
        {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                "Please use the format MM/dd/yyyy hh:mm AM/PM for dates.");
        }
        catch (NumberFormatException e)
        {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                "Economy and business prices must be valid numbers.");
        }
        catch (Exception e)
        {
            String message = e.getMessage() == null
                ? "Could not save changes." : e.getMessage();
            if (message.contains("existing bookings"))
            {
                showAlert(Alert.AlertType.ERROR, "Cannot Edit Flight", message);
                return;
            }
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not save changes: " + message);
        }
    }

    private boolean hasTakenSeats(Flight flight)
    {
        return countSeats(flight, null, false) > countSeats(flight, null, true);
    }

    private StackPane getSceneStackRoot()
    {
        if (contentHost != null)
        {
            return contentHost;
        }

        if (flightsTable.getScene() != null
            && flightsTable.getScene().getRoot() instanceof StackPane stackPane)
        {
            return stackPane;
        }
        return null;
    }

    private void closeOverlay(StackPane overlay)
    {
        if (overlay != null && overlay.getParent() instanceof StackPane parent)
        {
            parent.getChildren().remove(overlay);
        }
        if (overlay == activeFlightOverlay)
        {
            activeFlightOverlay = null;
        }
    }

    private void lockField(TextField field)
    {
        field.setEditable(false);
        field.setFocusTraversable(false);
        field.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #6e6e6e;");
    }

    private String getRouteText(Flight flight)
    {
        return flight.getDepartureCity().getCityName() + " -> "
            + flight.getArrivalCity().getCityName();
    }

    private String getCarrierAircraftText(Flight flight)
    {
        String carrier = flight.getCarrier() == null ? "Unknown carrier"
            : flight.getCarrier().getName();
        String aircraft = flight.getPlane() == null
            || flight.getPlane().getPlaneType() == null ? "Unknown aircraft"
            : flight.getPlane().getPlaneType().getModel();
        return carrier + " - " + aircraft;
    }

    private String formatMoney(double value)
    {
        return "EUR " + String.format("%.0f", value);
    }

    private String formatRawMoney(double value)
    {
        return String.format("%.0f", value);
    }

    private void openEditFlightDialog(Flight flight)
    {
        try
        {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("EditFlightModalView.fxml"));
            javafx.scene.layout.StackPane modalRoot = loader.load();

            EditFlightDialogController editController = loader.getController();
            editController.init(flight, flightsViewModel, this);

            StackPane sceneRoot = getSceneStackRoot();
            if (sceneRoot == null)
            {
                showAlert(Alert.AlertType.ERROR, "Error",
                    "Could not open edit dialog.");
                return;
            }

            sceneRoot.getChildren().add(modalRoot);
        }
        catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Could not open edit dialog: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}


