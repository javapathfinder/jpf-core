package java.util.logging;

import java.io.IOException;

/**
 * MJI model class for java.util.logging.Handler.
 */
public abstract class Handler {
  public abstract void publish(LogRecord record);
  
  // Mentor requested 'throws IOException'
  public abstract void flush() throws IOException;
  
  public abstract void close();
}
