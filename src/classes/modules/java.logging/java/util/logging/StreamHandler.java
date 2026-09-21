/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 */
package java.util.logging;

import java.io.OutputStream;
import java.io.IOException;

/**
 * MJI model class for java.util.logging.StreamHandler.
 */
public class StreamHandler extends Handler {
  private OutputStream out;

  public StreamHandler() {
    this.out = System.out;
  }

  public StreamHandler(OutputStream out, Formatter formatter) {
    this.out = out;
  }

  protected void setOutputStream(OutputStream out) {
    this.out = out;
  }

  @Override
  public void publish(LogRecord record) {
    if (out == null) return;
    try {
      String msg;
      if (record.getLevel() != null) {
          msg = "[" + record.getLevel().getName() + "] " + record.getMessage() + "\n";
      } else {
          msg = "[UNKNOWN] " + record.getMessage() + "\n";
      }
      out.write(msg.getBytes());
      flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void flush() throws IOException {
    if (out != null) {
      out.flush();
    }
  }

  @Override
  public void close() {
    try {
      flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    out = null;
  }
}
