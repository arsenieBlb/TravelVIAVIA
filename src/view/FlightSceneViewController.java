package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import model.Flight;
import viewmodel.FlightSceneViewModel;

import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.binding.Bindings;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.application.Platform;

public class FlightSceneViewController {

    @FXML private StackPane seatMapDialogWrapper;
    @FXML private StackPane seatMapDialog;
    @FXML Button bookTabButton;

    @FXML private Region root;

    private ViewHandler viewHandler;
    private FlightSceneViewModel flightSceneViewModel;

    public void init(Region root, ViewHandler viewHandler, FlightSceneViewModel flightSceneViewModel) {
        this.root = root;
        this.viewHandler = viewHandler;
        this.flightSceneViewModel = flightSceneViewModel;

        Platform.runLater(() -> {
            TableView<Flight> flightsTable = (TableView<Flight>) root.lookup("#flightsTable");

            if (flightsTable != null) {
                flightsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                if (flightsTable.getColumns().size() >= 6) {
                    TableColumn<Flight, String> routeCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(0);
                    TableColumn<Flight, String> depCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(1);
                    TableColumn<Flight, String> durCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(2);
                    TableColumn<Flight, String> carCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(3);
                    TableColumn<Flight, String> typeCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(4);
                    TableColumn<Flight, String> priceCol = (TableColumn<Flight, String>) flightsTable.getColumns().get(5);

                    routeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                            cellData.getValue().getDepartureCity().getCityName() + " → " +
                                    cellData.getValue().getArrivalCity().getCityName()));

                    depCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                            cellData.getValue().getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"))));

                    depCol.setSortable(false);
                    durCol.setCellValueFactory(cellData ->
                            new SimpleStringProperty(cellData.getValue().getDurationString())
                    );

                    carCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                            cellData.getValue().getPlane().getCarrier().getName() + "\n" +
                                    cellData.getValue().getFlightNumber()));

                    typeCol.setCellValueFactory(cellData -> new SimpleStringProperty("Direct"));
                    priceCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                            String.format("€%.0f", cellData.getValue().getBasePrice())));

                    flightsTable.setItems(flightSceneViewModel.getFilteredFlights());

                    flightsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                        if (newSel != null) {
                            flightSceneViewModel.setSelectedFlight(newSel);

                            javafx.scene.Node summaryCard = root.lookup("#flightSummaryCard");
                            if (summaryCard != null) {
                                summaryCard.setVisible(true);
                                summaryCard.setManaged(true);

                                Label routeLabel = (Label) root.lookup("#detailRouteLabel");
                                Label dateLabel = (Label) root.lookup("#detailDateLabel");
                                Label detailsLabel = (Label) root.lookup("#detailInfoLabel");
                                Label priceLabel = (Label) root.lookup("#detailPriceLabel");
                                Label aircraftLabel = (Label) root.lookup("#detailAircraftLabel");

                                if (routeLabel != null) routeLabel.setText(newSel.getDepartureCity().getCityName() + " → " + newSel.getArrivalCity().getCityName());
                                if (dateLabel != null) dateLabel.setText(newSel.getDepartureTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")));
                                if (detailsLabel != null) detailsLabel.setText(newSel.getDurationString() + " - Direct");
                                if (priceLabel != null) priceLabel.setText(String.format("EUR %.0f", newSel.getBasePrice()));
                                if (aircraftLabel != null) aircraftLabel.setText(newSel.getPlane().getPlaneType().getModel());
                            }
                        }
                    });

                    ComboBox<String> sortCombo = (ComboBox<String>) root.lookup("#sortByCombo");

                    if (sortCombo != null) {
                        sortCombo.getItems().setAll(
                                "Price (Low to High)",
                                "Duration (Shortest First)",
                                "Departure (Early First)"
                        );
                        sortCombo.getSelectionModel().selectFirst();

                        sortCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                flightSceneViewModel.sortFlights(newVal);
                            }
                        });
                    }

                    ComboBox<String> airlineCombo = (ComboBox<String>) root.lookup("#airlineCombo");

                    if (airlineCombo != null) {
                        airlineCombo.getItems().setAll(flightSceneViewModel.getUniqueCarriers());

                        airlineCombo.getItems().add(0, "All Airlines");
                        airlineCombo.getSelectionModel().selectFirst();

                        airlineCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                            flightSceneViewModel.filterByCarrier(newVal);
                        });
                    }

                    Label resultCountLabel = (Label) root.lookup("#resultCountLabel");
                    if (resultCountLabel != null) {
                        resultCountLabel.textProperty().bind(Bindings.size(flightSceneViewModel.getFilteredFlights()).asString());
                    }
                }
            }
        });
    }

    public Region getRoot() { return root; }
    public void reset() { flightSceneViewModel.clear(); }

    @FXML public void loginButton() { /*  */ }
}