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
package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;

import java.io.PrintWriter;

/**
 * simple tool to log state changes
 */
public class StateTracker extends ListenerAdapter {

  private final PrintWriter out;
  private final int logPeriod;
  volatile private String operation;
  volatile private String detail;
  volatile private int depth;
  volatile private int id;

  public StateTracker (Config conf, JPF jpf) {
    out = new PrintWriter(System.out, true);
    logPeriod = conf.getInt("jpf.state_tracker.log_period", 0);
    Runnable task = new Runnable() {@Override
	public void run() {logger();}};
    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.setName("StateTracker Logger");
    thread.start();
  }

  private void logger() {
    StringBuilder buffer = new StringBuilder();

    buffer.append("----------------------------------- [");
    int len = buffer.length();

    while (true) {
      try {
        Thread.sleep(logPeriod);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      buffer.append(depth);
      buffer.append(']');
      buffer.append(operation);
      buffer.append(": ");
      buffer.append(id);

      if (detail != null) {
        buffer.append(' ');
        buffer.append(detail);
      }

      out.println(buffer.toString());

      buffer.setLength(len);
    }
  }

  @Override
  public void stateRestored(Search search) {
    id = search.getStateId();
    depth = search.getDepth();
    operation = "restored";
    detail = null;
  }

  //--- the ones we are interested in
  @Override
  public void searchStarted(Search search) {
    out.println("----------------------------------- search started");
  }

  @Override
  public void stateAdvanced(Search search) {
    id = search.getStateId();
    depth = search.getDepth();
    operation = "forward";
    if (search.isNewState()) {
      detail = "new";
    } else {
      detail = "visited";
    }

    if (search.isEndState()) {
      detail += " end";
    }
  }

  @Override
  public void stateBacktracked(Search search) {
    id = search.getStateId();
    depth = search.getDepth();
    operation = "backtrack";
    detail = null;
  }

  @Override
  public void searchFinished(Search search) {
    out.println("----------------------------------- search finished");
  }

}
