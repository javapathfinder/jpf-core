/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 */
package java.util.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * MJI model class for java.util.logging.Logger
 */
public class Logger {

  private static final Map<String, Logger> loggers = new HashMap<>();
  private String name;
  private final List<Handler> handlers = new ArrayList<>();

  protected Logger(String name, String resourceBundleName) {
    this.name = name;
  }

  public static synchronized Logger getLogger(String name) {
    Logger logger = loggers.get(name);
    if (logger == null) {
      logger = new Logger(name, null);
      loggers.put(name, logger);
    }
    return logger;
  }

  public static Logger getAnonymousLogger() {
    return new Logger("", null);
  }

  public String getName() {
    return name;
  }

  public void addHandler(Handler handler) {
    handlers.add(handler);
  }

  public void removeHandler(Handler handler) {
    handlers.remove(handler);
  }

  public Handler[] getHandlers() {
    return handlers.toArray(new Handler[0]);
  }

  public void log(Level level, String msg) {
    LogRecord record = new LogRecord(level, msg);
    for (Handler h : handlers) {
      h.publish(record);
    }
  }

  public void info(String msg) {
    log(Level.INFO, msg);
  }

  public void warning(String msg) {
    log(Level.WARNING, msg);
  }
  
  public void severe(String msg) {
    log(Level.SEVERE, msg);
  }
}
