package view;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import model.Flight;
import model.Seat;
import model.SeatClass;
import viewmodel.FlightSceneViewModel;
import viewmodel.SeatMapViewModel;

import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.binding.Bindings;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FlightSceneViewController {
    /* OLD FIELDS - Commented out for Arseny to use later as per team chat
    @FXML private TextField passengerOneFirstNameField;
    @FXML private TextField passengerOneLastNameField;
    @FXML private Spinner<Integer> passengerOneBaggageSpinner;
    @FXML private Label passengerOneFlightLabel;
    @FXML private ComboBox<SeatClass> passengerOneSeatClassCombo;
    @FXML private TextField passengerOneSeatField;

    @FXML private TextField passengerTwoFirstNameField;
    @FXML private TextField passengerTwoLastNameField;
    @FXML private Spinner<Integer> passengerTwoBaggageSpinner;
    @FXML private Label passengerTwoFlightLabel;
    @FXML private ComboBox<SeatClass> passengerTwoSeatClassCombo;
    @FXML private TextField passengerTwoSeatField;

    @FXML private Label departureTimeLabel;
    @FXML private Label departureCityLabel;
    @FXML private Label arrivalTimeLabel;
    @FXML private Label arrivalCityLabel;
    @FXML private Label fareTitleLabel;
    */

    @FXML private StackPane seatMapDialogWrapper;
    @FXML private StackPane seatMapDialog;
    @FXML
    private Region root;
    private ViewHandler viewHandler;
    private FlightSceneViewModel flightSceneViewModel;
    private SeatMapViewModel seatMapViewModel;

    public void init(Region root, ViewHandler viewHandler, FlightSceneViewModel flightSceneViewModel, SeatMapViewModel seatMapViewModel)
    {
        this.root = root;
        this.viewHandler = viewHandler;
        this.flightSceneViewModel = flightSceneViewModel;
        this.seatMapViewModel = seatMapViewModel;

        /* OLD BINDINGS - Commented out to prevent NPE because FXML changed
        passengerOneSeatClassCombo.setItems(FXCollections.observableArrayList(
            SeatClass.Economy, SeatClass.Business));
        passengerTwoSeatClassCombo.setItems(FXCollections.observableArrayList(
            SeatClass.Economy, SeatClass.Business));

        departureTimeLabel.textProperty().bind(flightSceneViewModel.departureTimeProperty());
        departureCityLabel.textProperty().bind(flightSceneViewModel.departureCityProperty());
        arrivalTimeLabel.textProperty().bind(flightSceneViewModel.arrivalTimeProperty());
        arrivalCityLabel.textProperty().bind(flightSceneViewModel.arrivalCityProperty());
        passengerOneFlightLabel.textProperty().bind(flightSceneViewModel.routeSummaryProperty());
        passengerTwoFlightLabel.textProperty().bind(flightSceneViewModel.routeSummaryProperty());
        passengerOneFirstNameField.textProperty().bindBidirectional(flightSceneViewModel.passengerOneFirstNameProperty());
        passengerOneLastNameField.textProperty().bindBidirectional(flightSceneViewModel.passengerOneLastNameProperty());
        passengerOneBaggageSpinner.getValueFactory().valueProperty().bindBidirectional(flightSceneViewModel.passengerOneBaggageCountProperty().asObject());
        passengerOneSeatClassCombo.valueProperty().bindBidirectional(flightSceneViewModel.passengerOneSeatClassProperty());
        passengerOneSeatField.textProperty().bind(flightSceneViewModel.passengerOneSeatTextProperty());
        passengerTwoFirstNameField.textProperty().bindBidirectional(flightSceneViewModel.PassengerTwoFirstNameProperty());
        passengerTwoLastNameField.textProperty().bindBidirectional(flightSceneViewModel.PassengerTwoLastNameProperty());
        passengerTwoBaggageSpinner.getValueFactory().valueProperty().bindBidirectional(flightSceneViewModel.PassengerTwoBaggageCountProperty().asObject());
        passengerTwoSeatClassCombo.valueProperty().bindBidirectional(flightSceneViewModel.passengerTwoSeatClassProperty());
        passengerTwoSeatField.textProperty().bind(flightSceneViewModel.passengerTwoSeatTextProperty());
        fareTitleLabel.textProperty().bind(flightSceneViewModel.totalPriceProperty().asString("Total: %.2f Eur"));
        */

        Platform.runLater(() -> {
            TableView<Flight> flightsTable = (TableView<Flight>) root.lookup("#flightsTable");
            System.out.println("Table is: " + flightsTable);
            if (flightsTable != null && flightsTable.getColumns().size() >= 6) {
                TableColumn<Flight, String> routeCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(0);
                TableColumn<Flight, String> depCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(1);
                TableColumn<Flight, String> durCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(2);
                TableColumn<Flight, String> carCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(3);
                TableColumn<Flight, String> typeCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(4);
                TableColumn<Flight, String> priceCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(5);

                routeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getDepartureCity().getCityName() + " -> " + cellData.getValue().getArrivalCity().getCityName()));
                depCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))));
                durCol.setCellValueFactory(cellData -> new SimpleStringProperty("Direct"));
                carCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getPlane().getCarrier().getName()));
                typeCol.setCellValueFactory(cellData -> new SimpleStringProperty("Economy"));
                priceCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                    String.format("%.2f", cellData.getValue().getBasePrice())));

                flightsTable.setItems(flightSceneViewModel.getFilteredFlights());

                flightsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                    if (newSel != null) {
                        flightSceneViewModel.setSelectedFlight(newSel);
                        javafx.scene.Node summaryCard = root.lookup("#flightSummaryCard");
                        if (summaryCard != null) {
                            summaryCard.setVisible(true);
                            summaryCard.setManaged(true);
                        }
                    }
                });
            }

            Button searchFlightsButton = (Button) root.lookup("#searchFlightsButton");
            if (searchFlightsButton != null) {
                searchFlightsButton.setOnAction(e -> flightSceneViewModel.searchFlights());
            }

            // TEMPORARY TEST HOOK for the user
            Button continueButton = (Button) root.lookup("#continueButton");
            if (continueButton != null) {
                continueButton.setOnAction(e -> openSeatPicker(1));
            }

            Label resultCountLabel = (Label) root.lookup("#resultCountLabel");
            if (resultCountLabel != null) {
                resultCountLabel.textProperty().bind(Bindings.size(flightSceneViewModel.getFilteredFlights()).asString());
            }
        });
    }

    public Region getRoot()
    {
        return root;
    }

    public void reset()
    {
        flightSceneViewModel.clear();
    }

    @FXML
    public void passengerOneChooseSeatButton()
    {
        openSeatPicker(1);
    }

    @FXML
    public void passengerTwoChooseSeatButton()
    {
        openSeatPicker(2);
    }

    @FXML
    public void loginButton()
    {
        //will open another view
    }

    private void openSeatPicker(int passengerNumber)
    {
        List<Seat> seats = seatMapViewModel.getSeats();
        if (seats.isEmpty())
        {
            showInformation("Seat selection", "No seats are available for this flight.");
            return;
        }

        seatMapViewModel.startSelection(passengerNumber);

        VBox seatGridContainer = (VBox) seatMapDialog.lookup("#seatGridContainer");
        seatGridContainer.getChildren().clear();
        seatGridContainer.getChildren().add(createSeatGrid(passengerNumber, seats, seatMapViewModel.temporarySelectionProperty()));

        Button closeButton = (Button) seatMapDialog.lookup("#closeSeatModalButton");
        Button cancelButton = (Button) seatMapDialog.lookup("#cancelSeatSelectionButton");
        Button okButton = (Button) seatMapDialog.lookup("#seatOkButton");

        okButton.setDisable(seatMapViewModel.temporarySelectionProperty().get() == null);
        seatMapViewModel.temporarySelectionProperty().addListener((obs, oldV, newV) ->
            okButton.setDisable(newV == null));

        closeButton.setOnAction(e -> closeSeatPicker());
        cancelButton.setOnAction(e -> {
            seatMapViewModel.cancelSelection();
            closeSeatPicker();
        });
        okButton.setOnAction(e -> {
            seatMapViewModel.confirmSelection();
            closeSeatPicker();
        });

        seatMapDialogWrapper.setVisible(true);
        seatMapDialogWrapper.setManaged(true);
    }

    private void closeSeatPicker() {
        seatMapDialogWrapper.setVisible(false);
        seatMapDialogWrapper.setManaged(false);
    }

    private ScrollPane createSeatScrollPane(int passengerNumber, List<Seat> seats,
        ObjectProperty<Seat> temporarySelection)
    {
        GridPane seatGrid = createSeatGrid(passengerNumber, seats,
            temporarySelection);
        ScrollPane scrollPane = new ScrollPane(seatGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(560);
        scrollPane.setPrefViewportHeight(430);
        scrollPane.getStyleClass().add("seat-map-scroll");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        return scrollPane;
    }

    private GridPane createSeatGrid(int passengerNumber, List<Seat> seats,
        ObjectProperty<Seat> temporarySelection)
    {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(8));
        gridPane.getStyleClass().add("seat-map");

        Map<Integer, List<Seat>> seatsByRow = groupSeatsByRow(seats);
        Map<Seat, Button> seatButtons = new LinkedHashMap<>();
        SeatClass previousClass = null;
        int gridRow = 0;

        for (Map.Entry<Integer, List<Seat>> rowEntry : seatsByRow.entrySet())
        {
            List<Seat> rowSeats = rowEntry.getValue();
            rowSeats.sort(Comparator.comparing(Seat::getSeatNumber));
            SeatClass rowClass = rowSeats.get(0).getSeatClass();

            if (previousClass != null && previousClass != rowClass)
            {
                Label divider = new Label("Business / Economy");
                divider.getStyleClass().add("seat-section-divider");
                gridPane.add(divider, 0, gridRow++, 12, 1);
            }
            previousClass = rowClass;

            Label rowLabel = new Label(String.valueOf(rowEntry.getKey()));
            rowLabel.getStyleClass().add("seat-row-label");
            gridPane.add(rowLabel, 0, gridRow);

            int leftSideCount = calculateLeftSideSeatCount(rowClass,
                rowSeats.size());
            int gridColumn = 1;
            for (int i = 0; i < rowSeats.size(); i++)
            {
                if (i == leftSideCount)
                {
                    Region aisle = new Region();
                    aisle.setMinWidth(24);
                    aisle.getStyleClass().add("seat-aisle");
                    gridPane.add(aisle, gridColumn++, gridRow);
                }

                Seat seat = rowSeats.get(i);
                Button seatButton = new Button(seat.getSeatNumber());
                configureSeatButtonSize(seatButton, rowClass);
                seatButton.getStyleClass().add("seat-button");
                seatButton.setOnAction(event -> temporarySelection.set(seat));
                seatButtons.put(seat, seatButton);
                gridPane.add(seatButton, gridColumn++, gridRow);
            }
            gridRow++;
        }

        temporarySelection.addListener((observable, oldSeat, newSeat) ->
            updateSeatButtonStyles(seatButtons, passengerNumber, newSeat));
        updateSeatButtonStyles(seatButtons, passengerNumber,
            temporarySelection.get());

        return gridPane;
    }

    private Map<Integer, List<Seat>> groupSeatsByRow(List<Seat> seats)
    {
        Map<Integer, List<Seat>> seatsByRow = new TreeMap<>();
        for (Seat seat : seats)
        {
            seatsByRow.computeIfAbsent(seat.getRowNumber(),
                row -> new ArrayList<>()).add(seat);
        }
        return seatsByRow;
    }

    private int calculateLeftSideSeatCount(SeatClass rowClass, int seatsInRow)
    {
        if (seatsInRow <= 1)
        {
            return seatsInRow;
        }
        int plannedLeftSide = rowClass == SeatClass.Business ? 2 : 3;
        return Math.min(plannedLeftSide, seatsInRow - 1);
    }

    private void configureSeatButtonSize(Button seatButton, SeatClass rowClass)
    {
        if (rowClass == SeatClass.Business)
        {
            seatButton.setMinSize(76, 44);
            seatButton.setPrefSize(76, 44);
            seatButton.getStyleClass().add("business-seat-button");
        }
        else
        {
            seatButton.setMinSize(58, 38);
            seatButton.setPrefSize(58, 38);
            seatButton.getStyleClass().add("economy-seat-button");
        }
    }

    private void updateSeatButtonStyles(Map<Seat, Button> seatButtons,
        int passengerNumber, Seat selectedSeat)
    {
        SeatClass selectedClass = seatMapViewModel.getSelectedClass();

        for (Map.Entry<Seat, Button> entry : seatButtons.entrySet())
        {
            Seat seat = entry.getKey();
            Button button = entry.getValue();
            button.getStyleClass().removeAll("seat-available", "seat-taken",
                "seat-disabled", "seat-selected");

            boolean wrongClass = seat.getSeatClass() != selectedClass;
            boolean taken = seatMapViewModel.isSeatTaken(seat);
            boolean selectedByOtherPassenger =
                seatMapViewModel.isSeatAlreadySelectedByOtherPassenger(seat);

            button.setDisable(wrongClass || taken || selectedByOtherPassenger);

            if (taken || selectedByOtherPassenger)
            {
                button.getStyleClass().add("seat-taken");
            }
            else if (wrongClass)
            {
                button.getStyleClass().add("seat-disabled");
            }
            else
            {
                button.getStyleClass().add("seat-available");
            }

            if (seat.equals(selectedSeat))
            {
                button.getStyleClass().add("seat-selected");
            }
        }
    }

    private HBox createLegend()
    {
        HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.getChildren().addAll(createLegendItem("Available",
                "legend-available"),
            createLegendItem("Taken", "legend-taken"),
            createLegendItem("Selected", "legend-selected"));
        return legend;
    }

    private HBox createLegendItem(String text, String swatchClass)
    {
        Region swatch = new Region();
        swatch.setPrefSize(18, 18);
        swatch.getStyleClass().addAll("legend-swatch", swatchClass);
        Label label = new Label(text);
        label.getStyleClass().add("seat-legend-label");
        HBox item = new HBox(6, swatch, label);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private VBox createPlaneNose()
    {
        VBox nose = new VBox(2);
        nose.setAlignment(Pos.CENTER);
        Label title = new Label("FRONT OF PLANE");
        Label shape = new Label("________\n/        \\");
        title.getStyleClass().add("plane-front-label");
        shape.getStyleClass().add("plane-nose");
        nose.getChildren().addAll(title, shape);
        return nose;
    }

    private void centerDialogOnOwner(Dialog<Seat> dialog)
    {
        dialog.setOnShown(event -> {
            Window owner = root.getScene().getWindow();
            Window dialogWindow = dialog.getDialogPane().getScene().getWindow();
            dialogWindow.setX(owner.getX()
                + ((owner.getWidth() - dialogWindow.getWidth()) / 2));
            dialogWindow.setY(owner.getY()
                + ((owner.getHeight() - dialogWindow.getHeight()) / 2));
        });
    }

    private void showInformation(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
