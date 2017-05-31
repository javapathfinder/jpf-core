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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.Transition;

import java.io.PrintWriter;
import java.util.Formatter;

/**
 * From already visited states, estimates the total number of states by the branching factor.
 */

public class StateCountEstimator extends ListenerAdapter
{
   private final PrintWriter   m_out;
   private final StringBuilder m_buffer    = new StringBuilder();
   private final Formatter     m_formatter = new Formatter(m_buffer);
   private final int           m_logPeriod;
   private       double        m_lastPercent;
   private       long          m_nextLog;
   private       long          m_startTime;

   public StateCountEstimator(Config config)
   {
      m_out       = new PrintWriter(System.out, true);
      m_logPeriod = config.getInt("jpf.state_count_estimator.log_period", 0);
   }

   @Override
   public void searchStarted(Search search)
   {
      m_nextLog     = 0;
      m_lastPercent = 0.0;
      m_startTime   = System.currentTimeMillis();
   }

   @Override
   public void searchFinished(Search search)
   {
      log(search);
   }

   @Override
   public void stateProcessed(Search search)
   {
      if (m_nextLog > System.currentTimeMillis())
         return;
      
      if (log(search))
         m_nextLog = m_logPeriod + System.currentTimeMillis();
   }

   private boolean log(Search search)
   {
      VM vm;
      Path path;
      Transition trans;
      ChoiceGenerator cg;
      double percent, delta;
      long currentState, expectedState, currentTime, expectedTime;
      int i, size, processed;

      vm       = search.getVM();
      path      = vm.getPath();
      size      = path.size();
      percent   = 0.0;
      delta     = 1.0;
      processed = 0;
      
      for (i = 0; i < size; i++)
      {
         trans      = path.get(i);
         cg         = trans.getChoiceGenerator();
         delta     /= cg.getTotalNumberOfChoices();
         processed  = cg.getProcessedNumberOfChoices() - 1;
         percent   += delta * processed;
      }
      
      if (size == 0)
         percent = 1.0;
      
      if (m_lastPercent > percent)  // Sometimes a state is declared as processed but doesn't show up in the path so the percentage appears to go backwards.
         return(false);
      
      m_lastPercent = percent;
         
      currentState  = vm.getStateCount();
      expectedState = (long) (currentState / percent);
      
      currentTime   = System.currentTimeMillis() - m_startTime;
      expectedTime  = (long) (currentTime / percent);
      
      m_formatter.format("State:  %,d / %,d (%g%%)    Time:  ", currentState, expectedState, 100.0 * percent);
      
      formatTime(expectedTime);
      m_buffer.append(" - ");
      formatTime(currentTime);
      m_buffer.append(" = ");
      formatTime(expectedTime - currentTime);

      m_out.println(m_buffer.toString());
      m_buffer.setLength(0);
      
      return(true);
   }

   private void formatTime(long time)
   {
      long days, hours, minutes, seconds;
      boolean commit;

      seconds = time / 1000;
      minutes = seconds / 60;
      hours   = minutes / 60;
      days    = hours / 24;

      seconds %= 60;
      minutes %= 60;
      hours   %= 24;

      commit = false;

      if ((commit) || (days != 0))
      {
         commit = true;
         m_buffer.append(days);
         m_buffer.append(' ');
      }

      if ((commit) || (hours != 0))
      {
         if ((commit) && (hours < 10))
            m_buffer.append('0');

         m_buffer.append(hours);
         m_buffer.append(':');
         commit = true;
      }

      if ((commit) || (minutes != 0))
      {
         if ((commit) && (minutes < 10))
            m_buffer.append('0');

         m_buffer.append(minutes);
         m_buffer.append(':');
         commit = true;
      }

      if ((commit) && (seconds < 10))
         m_buffer.append('0');

      m_buffer.append(seconds);
   }
}
