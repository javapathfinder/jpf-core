/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 */
package java.util.logging;
import java.util.HashMap;
import java.util.Map;
/**
 * MJI model class for java.util.logging.LogManager
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