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
package gov.nasa.jpf.vm;

/**
 * Stores the information about an exception handler.
 */
public class ExceptionHandler {
  /**
   * Name of the exception caught. If it is 'null', this means this is an 'any' handler
   */
  private String name;

  /**
   * The first instruction belonging to this handler.
   */
  private int begin;

  /**
   * The last instruction belonging to this handler.
   */
  private int end;

  /**
   * The offset of the handler.
   */
  private int handler;

  /**
   * Creates a new exception handler.
   */
  public ExceptionHandler (String n, int b, int e, int h) {
    name = n;
    begin = b;
    end = e;
    handler = h;
  }

  /**
   * Returns the first instruction in the block.
   */
  public int getBegin () {
    return begin;
  }

  /**
   * Returns the last instruction in the block.
   */
  public int getEnd () {
    return end;
  }

  /**
   * Returns the instruction location for the handler.
   */
  public int getHandler () {
    return handler;
  }

  /**
   * Returns the name of the exception caught.
   */
  public String getName () {
    return name;
  }


  @Override
  public String toString() {
    return "Handler [name="+name+",from="+begin+",to="+end+",target="+handler+"]";
  }
}
