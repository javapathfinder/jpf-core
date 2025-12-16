package java.util.logging;
/**
 * MJI model class for java.util.logging.LogRecord.
 */
public class LogRecord {
  private Level level;
  private String message;
  public LogRecord(Level level, String msg) {
    this.level = level;
    this.message = msg;
  }
  public Level getLevel() {
    return level;
  }
  public String getMessage() {
    return message;
  }
}
