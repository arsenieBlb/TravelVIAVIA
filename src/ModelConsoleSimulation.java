import javafx.application.Application;
import javafx.stage.Stage;
import model.*;
import view.ViewHandler;
import viewmodel.ViewModelFactory;

import java.time.LocalDateTime;
import java.util.List;

public class ModelConsoleSimulation extends Application
{
    @Override
    public void start(Stage primaryStage) {
        Model model = new ModelManager();

        ViewModelFactory viewModelFactory = new ViewModelFactory(null);
        ViewHandler viewHandler = new ViewHandler(viewModelFactory);

        viewHandler.start(primaryStage);
    }
}
