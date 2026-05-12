package servermediator;

import com.google.gson.Gson;
import log.Logger;
import model.Model;
import model.ModelManager;
import servermodel.RequestType;
import servermodel.NetworkPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Server
{
  public static final int PORT = 8800;

  private final List<ClientHandler> activeClients;
  private final AtomicInteger nextClientId;
  private final Logger logger;
  private final Gson gson;
  private final ModelManager sharedModel;
  private boolean running;

  public Server()
  {
    this(new ModelManager());
  }

  public Server(ModelManager sharedModel)
  {
    this.activeClients = new ArrayList<>();
    this.nextClientId = new AtomicInteger(1);
    this.logger = Logger.getInstance();
    this.gson = new Gson();
    this.sharedModel = Objects.requireNonNull(sharedModel, "sharedModel");
  }

  public void start()
  {
    running = true;
    logger.logTraffic("server", "START",
        "TravelVIAVIA server listening on port " + PORT);
    try (ServerSocket listenSocket = new ServerSocket(PORT))
    {
      while (running)
      {
        logger.logTraffic("server", "WAIT", "Waiting for a client");
        Socket socket = listenSocket.accept();
        Model clientModel = new ServerSessionModel(sharedModel);
        ClientHandler clientHandler = new ClientHandler(
            nextClientId.getAndIncrement(), socket, this, clientModel);
        addClient(clientHandler);
        new Thread(clientHandler,
            "TravelVIAVIA-client-" + clientHandler.getClientId()).start();
      }
    }
    catch (IOException e)
    {
      logger.logTraffic("server", "ERR", e.getMessage());
    }
  }

  public void addClient(ClientHandler clientHandler)
  {
    synchronized (activeClients)
    {
      activeClients.add(clientHandler);
    }
    logger.logTraffic(clientHandler.getClientAddress(), "CONNECT",
        "client=" + clientHandler.getClientId()
            + " remote=" + clientHandler.getRemoteAddress());
  }

  public void removeClient(ClientHandler clientHandler)
  {
    synchronized (activeClients)
    {
      activeClients.remove(clientHandler);
    }
    logger.logTraffic(clientHandler.getClientAddress(), "DISCONNECT",
        "client=" + clientHandler.getClientId());
  }

  public void broadcastPropertyChange(String propertyName)
  {
    broadcastPropertyChange(propertyName, "");
  }

  public void broadcastPropertyChange(String propertyName, String changeSummary)
  {
    NetworkPackage networkPackage = new NetworkPackage(0,
        RequestType.BROADCAST_PROPERTY_CHANGE, gson.toJson(propertyName));
    List<ClientHandler> snapshot;
    synchronized (activeClients)
    {
      snapshot = new ArrayList<>(activeClients);
    }
    for (ClientHandler clientHandler : snapshot)
    {
      clientHandler.sendMessage(networkPackage);
      String payload = "payload={property=\"" + propertyName + "\"";
      if (changeSummary != null && !changeSummary.isBlank())
      {
        payload += ", change=" + changeSummary;
      }
      payload += "}";
      logger.logTraffic(clientHandler.getClientAddress(), "TX",
          "broadcast " + RequestType.BROADCAST_PROPERTY_CHANGE
              + " client=" + clientHandler.getClientId()
              + " " + payload);
    }
  }
}
