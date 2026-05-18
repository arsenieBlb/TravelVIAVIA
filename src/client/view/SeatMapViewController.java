package client.view;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import client.model.Seat;
import client.model.SeatClass;
import client.viewmodel.SeatMapViewModel;
import client.model.BusinessClass;
import client.model.EconomyClass;

import java.util.*;

public class SeatMapViewController {
    private static final int SEAT_COLUMN_WIDTH = 64;
    private static final int AISLE_COLUMN_WIDTH = 44;
    private static final int BUSINESS_SEAT_WIDTH = 78;
    private static final int BUSINESS_SEAT_HEIGHT = 50;
    private static final int BUSINESS_PAIR_GAP = 16;
    private static final int BUSINESS_ROW_WIDTH =
            (SEAT_COLUMN_WIDTH * 6) + AISLE_COLUMN_WIDTH + (6 * 4);
    private static final int BUSINESS_AISLE_GAP =
            BUSINESS_ROW_WIDTH - (BUSINESS_SEAT_WIDTH * 4)
                    - (BUSINESS_PAIR_GAP * 2);

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

        GridPane grid = createSeatGrid(passengerNumber,
                seatMapViewModel.getSeats(),
                seatMapViewModel.temporarySelectionProperty());
        VBox planeBox = new VBox(16, createPlaneNose(), grid);
        planeBox.setAlignment(Pos.TOP_CENTER);
        planeBox.setPrefWidth(540);
        planeBox.setMaxWidth(540);
        planeBox.getStyleClass().add("seat-map");

        seatGridContainer.getChildren().add(planeBox);

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
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setMaxWidth(Double.MAX_VALUE);
        configureSeatGridColumns(gridPane);

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

            if (rowClass instanceof BusinessClass && rowSeats.size() <= 4)
            {
                addBusinessSeatRow(gridPane, gridRow, rowSeats, seatButtons,
                        temporarySelection);
            }
            else
            {
                addStandardSeatRow(gridPane, gridRow, rowSeats, rowClass,
                        seatButtons, temporarySelection);
            }
            gridRow++;
        }

        temporarySelection.addListener((observable, oldSeat, newSeat) ->
                updateSeatButtonStyles(seatButtons, passengerNumber, newSeat));
        updateSeatButtonStyles(seatButtons, passengerNumber,
                temporarySelection.get());

        return gridPane;
    }

    private void configureSeatGridColumns(GridPane gridPane)
    {
        ColumnConstraints rowLabelColumn = new ColumnConstraints();
        rowLabelColumn.setMinWidth(28);
        rowLabelColumn.setPrefWidth(28);
        rowLabelColumn.setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().add(rowLabelColumn);

        for (int i = 0; i < 3; i++)
        {
            gridPane.getColumnConstraints().add(createSeatColumn());
        }

        ColumnConstraints aisleColumn = new ColumnConstraints();
        aisleColumn.setMinWidth(AISLE_COLUMN_WIDTH);
        aisleColumn.setPrefWidth(AISLE_COLUMN_WIDTH);
        gridPane.getColumnConstraints().add(aisleColumn);

        for (int i = 0; i < 3; i++)
        {
            gridPane.getColumnConstraints().add(createSeatColumn());
        }
    }

    private ColumnConstraints createSeatColumn()
    {
        ColumnConstraints column = new ColumnConstraints();
        column.setMinWidth(SEAT_COLUMN_WIDTH);
        column.setPrefWidth(SEAT_COLUMN_WIDTH);
        column.setHalignment(HPos.CENTER);
        return column;
    }

    private void addBusinessSeatRow(GridPane gridPane, int gridRow,
                                    List<Seat> rowSeats,
                                    Map<Seat, Button> seatButtons,
                                    ObjectProperty<Seat> temporarySelection)
    {
        HBox leftPair = new HBox(BUSINESS_PAIR_GAP);
        leftPair.setAlignment(Pos.CENTER);
        HBox rightPair = new HBox(BUSINESS_PAIR_GAP);
        rightPair.setAlignment(Pos.CENTER);

        for (int i = 0; i < rowSeats.size(); i++)
        {
            Seat seat = rowSeats.get(i);
            Button seatButton = createSeatButton(seat, seat.getSeatClass(),
                    seatButtons, temporarySelection);

            if (i < 2)
            {
                leftPair.getChildren().add(seatButton);
            }
            else
            {
                rightPair.getChildren().add(seatButton);
            }
        }

        Region aisle = new Region();
        aisle.setMinWidth(BUSINESS_AISLE_GAP);
        aisle.setPrefWidth(BUSINESS_AISLE_GAP);
        aisle.getStyleClass().add("seat-aisle");

        HBox businessRow = new HBox(0, leftPair, aisle, rightPair);
        businessRow.setAlignment(Pos.CENTER);
        businessRow.setMinWidth(BUSINESS_ROW_WIDTH);
        businessRow.setPrefWidth(BUSINESS_ROW_WIDTH);
        businessRow.setMaxWidth(BUSINESS_ROW_WIDTH);
        GridPane.setHalignment(businessRow, HPos.LEFT);

        gridPane.add(businessRow, 1, gridRow, 7, 1);
    }

    private void addStandardSeatRow(GridPane gridPane, int gridRow,
                                    List<Seat> rowSeats, SeatClass rowClass,
                                    Map<Seat, Button> seatButtons,
                                    ObjectProperty<Seat> temporarySelection)
    {
        int leftSideCount = calculateLeftSideSeatCount(rowClass,
                rowSeats.size());
        int gridColumn = 1;

        for (int i = 0; i < rowSeats.size(); i++)
        {
            if (i == leftSideCount)
            {
                addAisle(gridPane, gridColumn++, gridRow);
            }

            gridColumn = addSeatButton(gridPane, gridRow, gridColumn,
                    rowSeats.get(i), rowClass, seatButtons,
                    temporarySelection);
        }
    }

    private int addSeatButton(GridPane gridPane, int gridRow, int gridColumn,
                              Seat seat, SeatClass rowClass,
                              Map<Seat, Button> seatButtons,
                              ObjectProperty<Seat> temporarySelection)
    {
        Button seatButton = createSeatButton(seat, rowClass, seatButtons,
                temporarySelection);
        GridPane.setHalignment(seatButton, HPos.CENTER);
        gridPane.add(seatButton, gridColumn, gridRow);
        return gridColumn + 1;
    }

    private Button createSeatButton(Seat seat, SeatClass rowClass,
                                    Map<Seat, Button> seatButtons,
                                    ObjectProperty<Seat> temporarySelection)
    {
        Button seatButton = new Button(seat.getSeatNumber());
        configureSeatButtonSize(seatButton, rowClass);
        seatButton.getStyleClass().add("seat-button");
        seatButton.setOnAction(event -> temporarySelection.set(seat));
        seatButtons.put(seat, seatButton);
        return seatButton;
    }

    private void addAisle(GridPane gridPane, int gridColumn, int gridRow)
    {
        Region aisle = new Region();
        aisle.setMinWidth(AISLE_COLUMN_WIDTH);
        aisle.setPrefWidth(AISLE_COLUMN_WIDTH);
        aisle.getStyleClass().add("seat-aisle");
        gridPane.add(aisle, gridColumn, gridRow);
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
            seatButton.setMinSize(BUSINESS_SEAT_WIDTH, BUSINESS_SEAT_HEIGHT);
            seatButton.setPrefSize(BUSINESS_SEAT_WIDTH, BUSINESS_SEAT_HEIGHT);
            seatButton.setMaxSize(BUSINESS_SEAT_WIDTH, BUSINESS_SEAT_HEIGHT);
            seatButton.getStyleClass().add("business-seat-button");
        }
        else
        {
            seatButton.setMinSize(52, 44);
            seatButton.setPrefSize(52, 44);
            seatButton.setMaxSize(52, 44);
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



