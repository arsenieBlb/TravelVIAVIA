package client.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import client.model.*;

import java.util.List;

public class MainPageViewModel {

    private Model model;
    private StringProperty authStatus = new SimpleStringProperty("Not signed in");
    private StringProperty authButtonText = new SimpleStringProperty("Login");
    private BooleanProperty isAdmin = new SimpleBooleanProperty(false);
    private ObjectProperty<City> departureCity = new SimpleObjectProperty<>();
    private ObjectProperty<City> arrivalCity = new SimpleObjectProperty<>();
    private ObjectProperty<java.time.LocalDate> departureDate = new SimpleObjectProperty<>(java.time.LocalDate.now());
    private IntegerProperty passengerCount = new SimpleIntegerProperty(1);
    private ObjectProperty<SeatClass> seatClass = new SimpleObjectProperty<>(new EconomyClass());
    private ObservableList<Flight> foundFlights = FXCollections.observableArrayList();
    private ObservableList<Booking> userBookings = FXCollections.observableArrayList();
    private StringProperty pageTitle = new SimpleStringProperty("Travel via VIA");
    private BooleanProperty bookViewVisible = new SimpleBooleanProperty(true);
    private BooleanProperty bookingsViewVisible = new SimpleBooleanProperty(false);
    private BooleanProperty loginDialogVisible = new SimpleBooleanProperty(false);

    public MainPageViewModel(Model model)
    {
        this.model = model;
    }

    public void searchFlights()
    {
        SearchCriteria criteria = new SearchCriteria(
                departureCity.get(),
                arrivalCity.get(),
                departureDate.get(),
                passengerCount.get(),
                seatClass.get()
        );

        List<Flight> results = model.searchFlights(criteria);
        foundFlights.setAll(results);
    }

    public boolean login(String email, String password)
    {
        boolean success = model.login(email, password);
        if (success)
        {
            User user = model.getLoggedInUser();
            updateAuthUI(user);
            loginDialogVisible.set(false);
        }
        return success;
    }

    public void logout()
    {
        model.logout();
        updateAuthUI(null);
        showBookView();
    }

    private void updateAuthUI(User user)
    {
        if (user == null)
        {
            authStatus.set("Not signed in");
            authButtonText.set("Login");
            isAdmin.set(false);
        }
        else
        {
            String name = (user instanceof Customer c) ? c.getFirstName() : "Admin";
            authStatus.set("Welcome, " + name);
            authButtonText.set("Logout");
            isAdmin.set(user instanceof Admin);
        }
    }

    public void showBookView()
    {
        bookViewVisible.set(true);
        bookingsViewVisible.set(false);
        pageTitle.set("Book a Flight");
    }

    public void showMyBookings()
    {
        bookViewVisible.set(false);
        bookingsViewVisible.set(true);
        pageTitle.set("My Bookings");

        userBookings.setAll(model.getUserBookings());
    }

    public StringProperty authStatusProperty() { return authStatus; }
    public StringProperty authButtonTextProperty() { return authButtonText; }
    public StringProperty pageTitleProperty() { return pageTitle; }
    public BooleanProperty isAdminProperty() { return isAdmin; }
    public BooleanProperty bookViewVisibleProperty() { return bookViewVisible; }
    public BooleanProperty bookingsViewVisibleProperty() { return bookingsViewVisible; }
    public BooleanProperty loginDialogVisibleProperty() { return loginDialogVisible; }
    public ObservableList<Flight> getFoundFlights() { return foundFlights; }
    public ObservableList<Booking> getUserBookings() { return userBookings; }
    // Свойства поиска для связки с комбобоксами/полями ввода
    public ObjectProperty<City> departureCityProperty() { return departureCity; }
    public ObjectProperty<City> arrivalCityProperty() { return arrivalCity; }
}

