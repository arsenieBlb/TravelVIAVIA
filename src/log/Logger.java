package log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger
{
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy_MM_dd");
  private static Logger instance;

  private final Object lock = new Object();
  private final File logDirectory;

  private Logger()
  {
    logDirectory = new File(System.getProperty("user.home"), "Downloads");
  }

  public static synchronized Logger getInstance()
  {
    if (instance == null)
    {
      instance = new Logger();
    }
    return instance;
  }

  public void log(String clientId, String request, String result,
      String message)
  {
    String safeClient = clientId == null ? "-" : clientId;
    String safeRequest = request == null ? "-" : request;
    String safeResult = result == null ? "-" : result;
    String safeMessage = message == null ? "" : message;
    String line = "SERVER [" + LocalDateTime.now().format(TIME_FORMATTER)
        + "] client=" + safeClient
        + " request=" + safeRequest
        + " result=" + safeResult
        + " message=" + safeMessage;

    writeLine(line);
  }

  public void logTraffic(String remoteAddress, String direction,
      String message)
  {
    String safeAddress = remoteAddress == null || remoteAddress.isBlank()
        ? "unknown" : remoteAddress;
    String safeDirection = direction == null || direction.isBlank()
        ? "-" : direction;
    String safeMessage = message == null ? "" : message;
    String line = LocalDateTime.now().format(TIME_FORMATTER)
        + " | " + safeAddress
        + " | " + safeDirection
        + " " + safeMessage;

    writeLine(line);
  }

  private void writeLine(String line)
  {
    synchronized (lock)
    {
      System.out.println(line);
      if (!logDirectory.exists() && !logDirectory.mkdirs())
      {
        System.err.println("SERVER logger could not create log directory: "
            + logDirectory.getAbsolutePath());
        return;
      }
      try (FileWriter fileWriter = new FileWriter(getLogFile(), true);
          PrintWriter writer = new PrintWriter(fileWriter))
      {
        writer.println(line);
      }
      catch (IOException e)
      {
        System.err.println("SERVER logger could not write file: "
            + e.getMessage());
      }
    }
  }

  private File getLogFile()
  {
    String fileName = "TravelVIAVIA-Server-Log-"
        + LocalDateTime.now().format(DATE_FORMATTER) + ".txt";
    return new File(logDirectory, fileName);
  }
}
