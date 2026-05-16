package client.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import client.viewmodel.ViewModelFactory;

import java.io.IOException;

public class ViewHandler
{

    private Scene currentScene;
    private Stage primaryStage;
    private ViewModelFactory viewModelFactory;

    public ViewHandler(ViewModelFactory viewModelFactory)
    {
        this.currentScene = new Scene(new Region());
        this.viewModelFactory = viewModelFactory;
    }

    public void start(Stage primaryStage)
    {
        this.primaryStage = primaryStage;
        openView("flightScene");
    }

    public void openView(String id)
    {
        Region root = null;

        if (id.equals("flightScene"))
        {
            root = loadView("flight_scene.fxml", "client");
        }
        else if (id.equals("admin"))
        {
            root = loadView("flights_tab.fxml", "admin");
        }

        if (root != null)
        {
            currentScene.setRoot(root);
            primaryStage.setScene(currentScene);
            primaryStage.setTitle(id.toUpperCase());
            primaryStage.show();
        }
    }

    private Region loadView(String fxmlFile, String type)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlFile));
            Region root = loader.load();

            if (type.equals("client"))
            {
                FlightSceneViewController controller = loader.getController();
                controller.init(this, viewModelFactory.getFlightSceneViewModel(), root);
            }
            else if (type.equals("admin"))
            {
                FlightsTabViewController controller = loader.getController();
                controller.init(this, root, viewModelFactory.getNavigationAdminViewModel());
            }

            return root;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void closeView()
    {
        primaryStage.close();
    }
}