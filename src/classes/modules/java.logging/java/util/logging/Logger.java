/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.util.logging;

/**
 * MJI model class for java.util.logging.Logger
 *
 * Provides a lightweight Logger that bypasses JDK's heavy LogManager
 * initialization (config file loading, file system access, security checks,
 * Thread/AccessController calls) which cause crashes in JPF.
 * See issue #341.
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

  public void setLevel(Level level) { }

  public Level getLevel() {
    return Level.INFO;
  }

  public void addHandler(Handler h) { }

  public void removeHandler(Handler h) { }

  public void setUseParentHandlers(boolean use) { }

  public boolean getUseParentHandlers() {
    return true;
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
    // no-op in verification mode to avoid state space expansion
  }

  public boolean isLoggable(Level level) {
    return false;
  }
}
