package server.network;

public class NetworkPackage
{
  public int requestId;
  public String type;
  public String contentJson;
  public String errorMessage;

  public NetworkPackage()
  {
  }

  public NetworkPackage(int requestId, String type, String contentJson)
  {
    this.requestId = requestId;
    this.type = type;
    this.contentJson = contentJson;
  }

  public static NetworkPackage reply(int requestId, String type,
      String contentJson)
  {
    return new NetworkPackage(requestId, type, contentJson);
  }

  public static NetworkPackage error(int requestId, String type,
      String errorMessage)
  {
    NetworkPackage networkPackage = new NetworkPackage(requestId, type, null);
    networkPackage.errorMessage = errorMessage;
    return networkPackage;
  }

  public boolean hasError()
  {
    return errorMessage != null && !errorMessage.isBlank();
  }

  @Override public String toString()
  {
    return "NetworkPackage{requestId=" + requestId + ", type='" + type
        + "', error='" + errorMessage + "'}";
  }
}


