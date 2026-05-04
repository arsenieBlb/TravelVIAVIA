package view.viewnew;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * UI-only controller for the split FXML handoff.
 * Team TODO: connect this controller to FlightSceneViewModel bindings/commands.
 */
public class FlightSceneViewController {

    @FXML private StackPane bookViewWrapper;
    @FXML private StackPane bookingsViewWrapper;
    @FXML private StackPane passengerViewWrapper;

    @FXML private Button bookTabButton;
    @FXML private Button bookingsTabButton;
    @FXML private Button authButton;
    @FXML private Button floatingAddBookingButton;

    @FXML private StackPane addBookingDialogWrapper;
    @FXML private StackPane loginDialogWrapper;
    @FXML private StackPane registerDialogWrapper;
    @FXML private StackPane adminLoginDialogWrapper;
    @FXML private StackPane verificationDialogWrapper;
    @FXML private StackPane cancellationDialogWrapper;
    @FXML private StackPane bookingDetailsDialogWrapper;
    @FXML private StackPane seatMapDialogWrapper;

    @FXML
    private void initialize() {
        showBookTab();
        hideAllDialogs();

        // Optional default wiring to make the shell navigable immediately.
        bookTabButton.setOnAction(event -> showBookTab());
        bookingsTabButton.setOnAction(event -> showBookingsTab());
        authButton.setOnAction(event -> showLoginDialog());
        floatingAddBookingButton.setOnAction(event -> showAddBookingDialog());
    }

    // -------- Screen switching --------
    public void showBookTab() {
        setActiveScreen(true, false, false);
        floatingAddBookingButton.setVisible(false);
        floatingAddBookingButton.setManaged(false);
        setTabState(bookTabButton, true);
        setTabState(bookingsTabButton, false);
    }

    public void showBookingsTab() {
        setActiveScreen(false, true, false);
        floatingAddBookingButton.setVisible(true);
        floatingAddBookingButton.setManaged(true);
        setTabState(bookTabButton, false);
        setTabState(bookingsTabButton, true);
    }

    public void showPassengerDetailsTab() {
        setActiveScreen(false, false, true);
        floatingAddBookingButton.setVisible(false);
        floatingAddBookingButton.setManaged(false);
        setTabState(bookTabButton, false);
        setTabState(bookingsTabButton, false);
    }

    private void setActiveScreen(boolean showBook, boolean showBookings, boolean showPassenger) {
        bookViewWrapper.setVisible(showBook);
        bookViewWrapper.setManaged(showBook);
        bookingsViewWrapper.setVisible(showBookings);
        bookingsViewWrapper.setManaged(showBookings);
        passengerViewWrapper.setVisible(showPassenger);
        passengerViewWrapper.setManaged(showPassenger);
    }

    private void setTabState(Button tab, boolean active) {
        tab.getStyleClass().remove("tab-btn-active");
        if (active) {
            tab.getStyleClass().add("tab-btn-active");
        }
    }

    // -------- Dialog helpers --------
    public void hideAllDialogs() {
        setDialogVisible(addBookingDialogWrapper, false);
        setDialogVisible(loginDialogWrapper, false);
        setDialogVisible(registerDialogWrapper, false);
        setDialogVisible(adminLoginDialogWrapper, false);
        setDialogVisible(verificationDialogWrapper, false);
        setDialogVisible(cancellationDialogWrapper, false);
        setDialogVisible(bookingDetailsDialogWrapper, false);
        setDialogVisible(seatMapDialogWrapper, false);
    }

    public void showAddBookingDialog() {
        hideAllDialogs();
        setDialogVisible(addBookingDialogWrapper, true);
    }

    public void showLoginDialog() {
        hideAllDialogs();
        setDialogVisible(loginDialogWrapper, true);
    }

    public void showRegisterDialog() {
        hideAllDialogs();
        setDialogVisible(registerDialogWrapper, true);
    }

    public void showAdminLoginDialog() {
        hideAllDialogs();
        setDialogVisible(adminLoginDialogWrapper, true);
    }

    public void showVerificationDialog() {
        hideAllDialogs();
        setDialogVisible(verificationDialogWrapper, true);
    }

    public void showCancellationDialog() {
        hideAllDialogs();
        setDialogVisible(cancellationDialogWrapper, true);
    }

    public void showBookingDetailsDialog() {
        hideAllDialogs();
        setDialogVisible(bookingDetailsDialogWrapper, true);
    }

    public void showSeatMapDialog() {
        hideAllDialogs();
        setDialogVisible(seatMapDialogWrapper, true);
    }

    private void setDialogVisible(StackPane wrapper, boolean visible) {
        wrapper.setVisible(visible);
        wrapper.setManaged(visible);
    }
}
