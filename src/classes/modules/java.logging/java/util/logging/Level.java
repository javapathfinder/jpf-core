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
 * MJI model class for java.util.logging.Level
 *
 * Provides predefined logging levels to avoid triggering JDK's heavy
 * Level initialization (which involves resource bundles and locale handling).
 */
public class Level {
  private final String name;
  private final int value;

  protected Level(String name, int value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public final int intValue() {
    return value;
  }

  public static final Level OFF = new Level("OFF", Integer.MAX_VALUE);
  public static final Level SEVERE = new Level("SEVERE", 1000);
  public static final Level WARNING = new Level("WARNING", 900);
  public static final Level INFO = new Level("INFO", 800);
  public static final Level CONFIG = new Level("CONFIG", 700);
  public static final Level FINE = new Level("FINE", 500);
  public static final Level FINER = new Level("FINER", 400);
  public static final Level FINEST = new Level("FINEST", 300);
  public static final Level ALL = new Level("ALL", Integer.MIN_VALUE);

  public String toString() {
    return name;
  }
}
