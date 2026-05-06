package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import model.Booking;
import viewmodel.ViewModelFactory;

import java.io.IOException;

public class ViewHandler {
    private Stage primaryStage;
    private Scene scene;
    private FlightSceneViewController flightSceneViewController;
    private ViewModelFactory viewModelFactory;

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

    public Region loadFlightSceneView(String fxmlFile) throws IOException {
        if (flightSceneViewController == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlFile));
            Region root = loader.load();
            flightSceneViewController = loader.getController();

            flightSceneViewController.init(root, this, viewModelFactory);
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

    public void refreshMyBookings()
    {
        flightSceneViewController.refreshMyBookings();
    }
}
