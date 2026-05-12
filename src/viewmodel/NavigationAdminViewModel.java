package viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import model.Model;

public class NavigationAdminViewModel {

    private final ObjectProperty<NavigationTab> currentTab = new SimpleObjectProperty<>(NavigationTab.DASHBOARD);
    private final Model model;

    private FlightsTabViewModel flightsTabViewModel;
    private BookingAdminViewModel bookingAdminViewModel;

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

    public void navigateTo(NavigationTab tab) {
        currentTab.set(tab);
    }

    public ObjectProperty<NavigationTab> currentTabProperty() {
        return currentTab;
    }

    public ObservableList<String> getUniqueAircraftModels() {
        return model.searchFlights(new model.SearchCriteria()).stream()
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
}
