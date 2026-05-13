package view;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import model.Seat;
import model.SeatClass;
import viewmodel.SeatMapViewModel;
import model.BusinessClass;
import model.EconomyClass;

import java.util.*;

public class SeatMapViewController {
    @FXML private StackPane seatModal;
    @FXML private VBox seatGridContainer;
    @FXML private Label seatModalTitleLabel;
    @FXML private Button closeSeatModalButton;
    @FXML private Button cancelSeatSelectionButton;
    @FXML private Button seatOkButton;

    private Region root;
    private StackPane dialogWrapper;
    private ViewHandler viewHandler;
    private SeatMapViewModel seatMapViewModel;

    public void init(Region root, ViewHandler viewHandler,
                     SeatMapViewModel seatMapViewModel,
                     StackPane dialogWrapper)
    {
        this.root = root;
        this.viewHandler = viewHandler;
        this.seatMapViewModel = seatMapViewModel;
        this.dialogWrapper = dialogWrapper;

        seatOkButton.disableProperty().bind(seatMapViewModel.temporarySelectionProperty().isNull());

        closeSeatModalButton.setOnAction(e -> closeSeatPicker());

        cancelSeatSelectionButton.setOnAction(e ->
        {
            seatMapViewModel.cancelSelection();
            closeSeatPicker();
        });

        seatOkButton.setOnAction(e -> {
            try
            {
                seatMapViewModel.confirmSelection();
                closeSeatPicker();
            }
            catch (IllegalArgumentException ex) {
                showError("Selection Error", ex.getMessage());
            }
            catch (Exception ex) {
                showError("System Error", "An unexpected error occurred.");
            }
        });
    }

    private void showError(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("card");

        alert.showAndWait();
    }

    public void showForPassenger(int passengerNumber, int segmentIndex) {
        seatMapViewModel.startSelection(passengerNumber, segmentIndex);
        seatModalTitleLabel.setText("Select a seat ("
                + seatMapViewModel.getSelectedClass() + ")");

        seatGridContainer.getChildren().clear();
        seatGridContainer.setAlignment(Pos.TOP_CENTER);

        VBox nose = createPlaneNose();
        GridPane grid = createSeatGrid(passengerNumber,
                seatMapViewModel.getSeats(),
                seatMapViewModel.temporarySelectionProperty());

        seatGridContainer.getChildren().addAll(nose, grid);

        dialogWrapper.setVisible(true);
        dialogWrapper.setManaged(true);
    }

    private void closeSeatPicker() {
        dialogWrapper.setVisible(false);
        dialogWrapper.setManaged(false);
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
        gridPane.setHgap(4);
        gridPane.setVgap(4);
        gridPane.setPadding(new Insets(6));
        gridPane.getStyleClass().add("seat-map");
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setMaxWidth(Double.MAX_VALUE);

        Map<Integer, List<Seat>> seatsByRow = groupSeatsByRow(seats);
        Map<Seat, Button> seatButtons = new LinkedHashMap<>();
        SeatClass previousClass = null;
        int gridRow = 0;

        for (Map.Entry<Integer, List<Seat>> rowEntry : seatsByRow.entrySet())
        {
            List<Seat> rowSeats = rowEntry.getValue();
            rowSeats.sort(Comparator.comparing(Seat::getSeatNumber));
            SeatClass rowClass = rowSeats.get(0).getSeatClass();

            if (previousClass != null && !previousClass.getClass().equals(rowClass.getClass()))
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
                    aisle.setMinWidth(16);
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
        int plannedLeftSide = (rowClass instanceof BusinessClass) ? 2 : 3;
        return Math.min(plannedLeftSide, seatsInRow - 1);
    }

    private void configureSeatButtonSize(Button seatButton, SeatClass rowClass)
    {
        if (rowClass instanceof BusinessClass)
        {
            seatButton.setMinSize(48, 32);
            seatButton.setPrefSize(48, 32);
            seatButton.setMaxSize(48, 32);
            seatButton.getStyleClass().add("business-seat-button");
        }
        else
        {
            seatButton.setMinSize(40, 28);
            seatButton.setPrefSize(40, 28);
            seatButton.setMaxSize(40, 28);
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

            button.getStyleClass().removeAll("seat-available", "seat-taken", "seat-disabled", "seat-selected");

            boolean isWrongClass = !seat.getSeatClass().getClass().equals(selectedClass.getClass());
            boolean isTakenInDb = seatMapViewModel.isSeatTaken(seat);
            boolean isSelectedByOther = seatMapViewModel.isSeatAlreadySelectedByOtherPassenger(seat);

            button.setDisable(isWrongClass || isTakenInDb || isSelectedByOther);

            if (isTakenInDb || isSelectedByOther) {
                button.getStyleClass().add("seat-taken");
            } else if (isWrongClass) {
                button.getStyleClass().add("seat-disabled");
            } else {
                button.getStyleClass().add("seat-available");
            }
            if (seat.equals(selectedSeat)) {
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

    public Region getRoot()
    {
        return root;
    }

    public void reset()
    {
        seatMapViewModel.clear();
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
