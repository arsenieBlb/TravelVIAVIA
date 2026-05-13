import javafx.application.Application;
import javafx.stage.Stage;
import client.mediator.Client;
import client.model.Model;
import server.mediator.Server;
import client.view.ViewHandler;
import client.viewmodel.ViewModelFactory;

public class ModelConsoleSimulation extends Application
{
    @Override
    public void start(Stage primaryStage) {
        Model model = createClientModel();

        ViewModelFactory viewModelFactory = new ViewModelFactory(model);
        ViewHandler viewHandler = new ViewHandler(viewModelFactory);
        primaryStage.setMaximized(true);
        viewHandler.start(primaryStage);
    }

    private Model createClientModel()
    {
        try
        {
            return new Client("localhost", Server.PORT);
        }
        catch (IllegalStateException e)
        {
            System.out.println("No TravelVIAVIA server found. Starting a local socket server.");
            Thread serverThread = new Thread(() -> new Server().start(),
                    "TravelVIAVIA-local-server");
            serverThread.setDaemon(true);
            serverThread.start();
            return new Client("localhost", Server.PORT);
        }
    }
}


