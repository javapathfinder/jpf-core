/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 */
package java.util.logging;

/**
 * MJI model class for java.util.logging.Logger
 */
public class Logger {

  private String name;
  private String resourceBundleName;

  protected Logger(String name, String resourceBundleName) {
    this.name = name;
    this.resourceBundleName = resourceBundleName;
  }

  public static Logger getLogger(String name) {
    return getLogger(name, null);
  }
  
  public static Logger getLogger(String name, String resourceBundleName) {
    LogManager manager = LogManager.getLogManager();
    return manager.demandLogger(name, resourceBundleName, null);
  }

  public static Logger getAnonymousLogger() {
    return new Logger("", null);
  }
  
  public static Logger getAnonymousLogger(String resourceBundleName) {
    return new Logger("", resourceBundleName);
  }

  public String getName() {
    return name;
  }
  
  public String getResourceBundleName() {
    return resourceBundleName;
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
  
  public void config(String msg) {
    log(Level.CONFIG, msg);
  }
  
  public void fine(String msg) {
    log(Level.FINE, msg);
  }
  
  public void finer(String msg) {
    log(Level.FINER, msg);
  }
  
  public void finest(String msg) {
    log(Level.FINEST, msg);
  }
  
  public void log(Level level, String msg) {
    System.out.println("[" + level.getName() + "] " + name + ": " + msg);
  }
}
