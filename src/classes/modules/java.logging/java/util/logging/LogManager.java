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

import java.util.HashMap;
import java.util.Map;

/**
 * MJI model class for java.util.logging.LogManager
 *
 * Provides a minimal LogManager that avoids JDK's heavy initialization
 * (configuration file loading, file system access, security manager checks)
 * which causes crashes in JPF. See issue #341.
 */
public class LogManager {

  private static final LogManager manager = new LogManager();
  private final Map<String, Logger> loggers = new HashMap<>();

  protected LogManager() {
  }

  public static LogManager getLogManager() {
    return manager;
  }

  public synchronized Logger getLogger(String name) {
    return loggers.get(name);
  }

  public synchronized boolean addLogger(Logger logger) {
    String name = logger.getName();
    if (loggers.containsKey(name)) {
      return false;
    }
    loggers.put(name, logger);
    return true;
  }

  Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
    Logger result = getLogger(name);
    if (result == null) {
      Logger newLogger = new Logger(name, resourceBundleName);
      addLogger(newLogger);
      result = newLogger;
    }
    return result;
  }
}
