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

    private FlightsTabViewModel flightsTabViewModel;
    private BookingAdminViewModel bookingAdminViewModel;
    private DashboardViewModel dashboardViewModel;
    private AddFlightTabViewModel addFlightTabViewModel;

    public enum NavigationTab {
        DASHBOARD, FLIGHTS, BOOKINGS
    }

    public NavigationAdminViewModel(Model model) {
        this.model = model;
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
        return model.searchFlights(new client.model.SearchCriteria()).stream()
                .map(flight -> flight.getPlane().getPlaneType().getModel())
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(javafx.collections.FXCollections::observableArrayList));
    }

    public void logout()
    {
        model.logout();
    }

    public StringProperty authStatusProperty()
    {
        if (model.getLoggedInUser() != null)
        {
            return new SimpleStringProperty("Logged in as Admin");
        }
        else
        {
            return new SimpleStringProperty("Not signed in");
        }
    }

    public DashboardViewModel getDashboardViewModel()
    {
        if (dashboardViewModel == null) {
            dashboardViewModel = new DashboardViewModel(model);
        }
        return dashboardViewModel;
    }
}


