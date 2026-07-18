/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 */
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
