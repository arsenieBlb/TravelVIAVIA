package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import viewmodel.ViewModelFactory;

import java.io.IOException;

public class ViewHandler {
    private Stage primaryStage;
    private Scene scene;
    private FlightSceneViewController flightSceneViewController;
    private ViewModelFactory viewModelFactory;
    private BookFlightViewController bookFlightViewController;
    private SeatMapViewController seatMapViewController;

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

            BookFlightViewController bookCtrl = (BookFlightViewController) loader.getNamespace().get("bookViewControllerController");

            if (bookCtrl != null) {
                bookCtrl.init(viewModelFactory.getBookFlightViewModel(), root, this);
            }

            flightSceneViewController.init(root, this,
                    viewModelFactory.getFlightSceneViewModel());
        } else {
            flightSceneViewController.reset();
        }
        return flightSceneViewController.getRoot();
    }

    public Region loadBookFlightView(String fxmlFile) throws IOException
    {
        if (bookFlightViewController == null)
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlFile));
            Region root = loader.load();
            bookFlightViewController = loader.getController();
            bookFlightViewController.init(viewModelFactory.getBookFlightViewModel(), root, this);
        }
        else
        {
            bookFlightViewController.reset();
        }
        return bookFlightViewController.getRoot();
    }

    public Region loadSeatMapView(String fxmlFile) throws IOException
    {
        if (seatMapViewController == null)
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlFile));
            Region root = loader.load();
            seatMapViewController = loader.getController();
            seatMapViewController.init(root, this, viewModelFactory.getSeatMapViewModel());
        }
        else
        {
            seatMapViewController.reset();
        }
        return seatMapViewController.getRoot();
    }
}
