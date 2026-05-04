package view.viewnew;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.FlightSceneViewController;

/**
 * Drop-in view loader for projects that keep FXML/CSS/controllers in src/view.
 *
 * Team TODO:
 * - Inject your ViewModelFactory and set it on FlightSceneViewController.
 * - Add methods for opening extra views if your app grows.
 */
public class ViewHandler {

    private final Stage stage;
    private Scene mainScene;
    private view.FlightSceneViewController flightSceneViewController;

    public ViewHandler() {
        this.stage = null;
    }

    public ViewHandler(Stage stage) {
        this.stage = stage;
    }

    public Scene createMainScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("flight_scene.fxml"));
        Parent root = loader.load();

        this.flightSceneViewController = loader.getController();
        this.mainScene = new Scene(root);
        return mainScene;
    }

    public void start() throws IOException {
        if (stage == null) {
            throw new IllegalStateException("Stage is null. Use ViewHandler(Stage) or call createMainScene() manually.");
        }
        if (mainScene == null) {
            createMainScene();
        }
        stage.setTitle("Travel via VIA");
        stage.setScene(mainScene);
        stage.setMinWidth(1200);
        stage.setMinHeight(780);
        stage.show();
    }

    public FlightSceneViewController getFlightSceneViewController() {
        return flightSceneViewController;
    }

    public Scene getMainScene() {
        return mainScene;
    }
}
