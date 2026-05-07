package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import model.Booking;
import viewmodel.FlightsTabViewModel;
import viewmodel.ViewModelFactory;

import java.io.IOException;

public class ViewHandler {
    private Stage primaryStage;
    private Scene scene;
    private FlightSceneViewController flightSceneViewController;
    private ViewModelFactory viewModelFactory;
    private MyBookingsViewController bookViewController;
    private NavigationAdminViewController navigationAdminViewController;

    public ViewHandler(ViewModelFactory viewModelFactory)
    {
        this.viewModelFactory = viewModelFactory;
        this.scene = new Scene(new Region());
    }

    public void start(Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        openView("flightScene");
    }

    public void openView(String id)
    {
        Region root = null;
        try
        {
            if ("flightScene".equals(id))
            {
                root = loadFlightSceneView("flight_scene.fxml");
            }

            else if ("admin".equals(id)) {
                root = loadAdminShellView("flights_tab.fxml");
            }

            scene.setRoot(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(id);
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Region loadAdminShellView(String fxmlFile) throws IOException {
        if (navigationAdminViewController == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlFile));
            Region root = loader.load();
            navigationAdminViewController = loader.getController();

            navigationAdminViewController.init(this, root, viewModelFactory.getNavigationAdminViewModel());
        } else {
            navigationAdminViewController.clear();
        }
        return navigationAdminViewController.getRoot();
    }

    public Region loadFlightSceneView(String fxmlFile) throws IOException {
        if (flightSceneViewController == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlFile));
            Region root = loader.load();
            flightSceneViewController = loader.getController();

            flightSceneViewController.init(root, this, viewModelFactory.getFlightSceneViewModel());
            this.bookViewController = flightSceneViewController.getMyBookingsViewController();
        } else {
            flightSceneViewController.reset();
        }
        return flightSceneViewController.getRoot();
    }


    public void showBookFlight()
    {
        flightSceneViewController.showBookFlight();
    }

    public void showPassengerDetails()
    {
        flightSceneViewController.showPassengerDetails();
    }

    public void showMyBookings()
    {
        flightSceneViewController.showMyBookings();
    }

    public void showSeatPicker(int passengerNumber)
    {
        flightSceneViewController.showSeatPicker(passengerNumber);
    }

    public void showAddBookingDialog()
    {
        flightSceneViewController.showAddBookingDialog();
    }

    public void showBookingDetails(Booking booking)
    {
        flightSceneViewController.showBookingDetails(booking);
    }

    public void refreshMyBookings() {
        if (bookViewController != null) {
            bookViewController.refresh();
        } else {
            System.out.println("DEBUG: bookViewController is null. Attempting re-grab...");

            if (flightSceneViewController != null) {
                this.bookViewController = flightSceneViewController.getMyBookingsViewController();
                if (this.bookViewController != null) {
                    this.bookViewController.refresh();
                } else {
                    System.out.println("ERROR: MyBookingsViewController still null. Check FXML fx:id!");
                }
            }
        }
    }
}
