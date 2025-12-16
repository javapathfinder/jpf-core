package java.util.logging;
/**
 * MJI model class for java.util.logging.Handler.
 */
public abstract class Handler {
  public abstract void publish(LogRecord record);
  public abstract void flush();
  public abstract void close();
}
