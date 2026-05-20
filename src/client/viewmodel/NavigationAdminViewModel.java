package client.viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import client.model.Model;

public class NavigationAdminViewModel {

    private final ObjectProperty<NavigationTab> currentTab = new SimpleObjectProperty<>(NavigationTab.DASHBOARD);
    private final Model model;
    private final StringProperty authStatus = new SimpleStringProperty("Not signed in");

    private FlightsTabViewModel flightsTabViewModel;
    private BookingAdminViewModel bookingAdminViewModel;
    private DashboardViewModel dashboardViewModel;
    private AddFlightTabViewModel addFlightTabViewModel;

    public enum NavigationTab {
        DASHBOARD, FLIGHTS, BOOKINGS
    }

    public NavigationAdminViewModel(Model model) {
        this.model = model;
        updateAuthStatus();
    }

    public FlightsTabViewModel getFlightsTabViewModel() {
        if (flightsTabViewModel == null) {
            flightsTabViewModel = new FlightsTabViewModel(model);
        }
        return flightsTabViewModel;
    }

    public BookingAdminViewModel getBookingAdminViewModel() {
        if (bookingAdminViewModel == null) {
            bookingAdminViewModel = new BookingAdminViewModel(model);
        }
        return bookingAdminViewModel;
    }

    public AddFlightTabViewModel getAddFlightTabViewModel() {
        if (addFlightTabViewModel == null) {
            addFlightTabViewModel = new AddFlightTabViewModel(model);
        }
        return addFlightTabViewModel;
    }

    public void navigateTo(NavigationTab tab) {
        currentTab.set(tab);
    }

    public ObjectProperty<NavigationTab> currentTabProperty() {
        return currentTab;
    }

    public ObservableList<String> getUniqueAircraftModels() {
        return model.getPlanes().stream()
                .filter(plane -> plane != null && plane.getPlaneType() != null)
                .map(plane -> plane.getPlaneType().getModel())
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(javafx.collections.FXCollections::observableArrayList));
    }

    public void logout()
    {
        model.logout();
        updateAuthStatus();
    }

    public StringProperty authStatusProperty()
    {
        return authStatus;
    }

    private void updateAuthStatus()
    {
        authStatus.set(model.getLoggedInUser() != null
            ? "Logged in as Admin" : "Not signed in");
    }

    public DashboardViewModel getDashboardViewModel()
    {
        if (dashboardViewModel == null) {
            dashboardViewModel = new DashboardViewModel(model);
        }
        return dashboardViewModel;
    }
}


