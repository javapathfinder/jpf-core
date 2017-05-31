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
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.bytecode.ArrayElementInstruction;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.bytecode.ReadOrWriteInstruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This is a Race Detection Algorithm that is precise in its calculation of races, i.e. no false warnings.
 * It exploits the fact that every thread choice selection point could be due to a possible race. It just runs
 * through all the thread choices and checks whether there are more than one thread trying to read & write to the
 * same field of an object.
 *
 * Current limitation is that it is only sound, i.e. will not miss a race, if the sync-detection is switched off
 * during model checking. This is due to the fact that the sync-detection guesses that an acess is lock-protected
 * when it in reality might not be.
 *
 * The listener also checks races for array elements, but in order to do so you have to set
 * "cg.threads.break_arrays=true" (note that it is false by default because this can cause serious state
 * explosion)
 *
 * This algorithm came out of a discussion with Franck van Breugel and Sergey Kulikov from the University of York.
 * All credits for it goes to Franck and Sergey, all the bugs are mine.
 *
 * NOTE - the PreciseRaceDetector is machine type agnostic
 *
 * Author: Willem Visser
 *
 */

public class PreciseRaceDetector extends PropertyListenerAdapter {

  static class Race {
    Race prev;   // linked list

    ThreadInfo ti1, ti2;
    ReadOrWriteInstruction insn1, insn2;
    ElementInfo ei;
    boolean isRead1, isRead2;

    boolean isRace() {
      return insn2 != null && ti1 != null && ti2 != null && ( ! ti1.equals(ti2) );
    }

    void printOn(PrintWriter pw){
      pw.print("  ");
      pw.print( ti1.getName());
      pw.print(" at ");
      pw.println(insn1.getSourceLocation());
      String line = insn1.getSourceLine();
      if (line != null){
        pw.print("\t\t\"" + line.trim());
      }
      pw.print("\"  ");
      pw.print( insn1.isRead() ? "READ:  " : "WRITE: ");
      pw.println(insn1);

      if (insn2 != null){
        pw.print("  ");
        pw.print(ti2.getName());
        pw.print(" at ");
        pw.println(insn2.getSourceLocation());
        line = insn2.getSourceLine();
        if (line != null){
          pw.print("\t\t\"" + line.trim());
        }
        pw.print("\"  ");
        pw.print( insn2.isRead() ? "READ:  " : "WRITE: ");
        pw.println(insn2);
      }
    }
  }

  static class FieldRace extends Race {
    FieldInfo   fi;

    static Race check (Race prev, ThreadInfo ti,  ReadOrWriteInstruction insn, ElementInfo ei, FieldInfo fi){
      for (Race r = prev; r != null; r = r.prev){
        if (r instanceof FieldRace){
          FieldRace fr = (FieldRace)r;
          if (fr.ei == ei && fr.fi == fi){
            
            if (!((FieldInstruction)fr.insn1).isRead() || !((FieldInstruction)insn).isRead()){
              fr.ti2 = ti;
              fr.insn2 = insn;
              return fr;
            }
          }
        }
      }

      FieldRace fr = new FieldRace();
      fr.ei = ei;
      fr.ti1 = ti;
      fr.insn1 = insn;
      fr.fi = fi;
      fr.prev = prev;
      return fr;
    }

    @Override
	void printOn(PrintWriter pw){
      pw.print("race for field ");
      pw.print(ei);
      pw.print('.');
      pw.println(fi.getName());

      super.printOn(pw);
    }
  }

  static class ArrayElementRace extends Race {
    int idx;

    static Race check (Race prev, ThreadInfo ti, ReadOrWriteInstruction insn, ElementInfo ei, int idx){
      for (Race r = prev; r != null; r = r.prev){
        if (r instanceof ArrayElementRace){
          ArrayElementRace ar = (ArrayElementRace)r;
          if (ar.ei == ei && ar.idx == idx){
            if (!((ArrayElementInstruction)ar.insn1).isRead() || !((ArrayElementInstruction)insn).isRead()){
              ar.ti2 = ti;
              ar.insn2 = insn;
              return ar;
            }
          }
        }
      }

      ArrayElementRace ar = new ArrayElementRace();
      ar.ei = ei;
      ar.ti1 = ti;
      ar.insn1 = insn;
      ar.idx = idx;
      ar.prev = prev;
      return ar;
    }

    @Override
	void printOn(PrintWriter pw){
      pw.print("race for array element ");
      pw.print(ei);
      pw.print('[');
      pw.print(idx);
      pw.println(']');

      super.printOn(pw);
    }
  }

  // this is where we store if we detect one
  protected Race race;


  // our matchers to determine which code we have to check
  protected StringSetMatcher includes = null; //  means all
  protected StringSetMatcher excludes = null; //  means none


  public PreciseRaceDetector (Config conf) {
    includes = StringSetMatcher.getNonEmpty(conf.getStringArray("race.include"));
    excludes = StringSetMatcher.getNonEmpty(conf.getStringArray("race.exclude"));
  }
  
  @Override
  public boolean check(Search search, VM vm) {
    return (race == null);
  }

  @Override
  public void reset() {
    race = null;
  }


  @Override
  public String getErrorMessage () {
    if (race != null){
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      race.printOn(pw);
      pw.flush();
      return sw.toString();
      
    } else {
      return null;
    }
  }

  protected boolean checkRace (ThreadInfo[] threads){
    Race candidate = null;

    for (int i = 0; i < threads.length; i++) {
      ThreadInfo ti = threads[i];
      Instruction insn = ti.getPC();
      MethodInfo mi = insn.getMethodInfo();

      if (StringSetMatcher.isMatch(mi.getBaseName(), includes, excludes)) {
        if (insn instanceof FieldInstruction) {
          FieldInstruction finsn = (FieldInstruction) insn;
          FieldInfo fi = finsn.getFieldInfo();
          ElementInfo ei = finsn.peekElementInfo(ti);

          candidate = FieldRace.check(candidate, ti, finsn, ei, fi);

        } else if (insn instanceof ArrayElementInstruction) {
          ArrayElementInstruction ainsn = (ArrayElementInstruction) insn;
          ElementInfo ei = ainsn.peekArrayElementInfo(ti);

          // these insns have been through their top half since they created CGs, but they haven't
          // removed the operands from the stack
          int idx = ainsn.peekIndex(ti);

          candidate = ArrayElementRace.check(candidate, ti, ainsn, ei, idx);
        }
      }

      if (candidate != null && candidate.isRace()){
        race = candidate;
        return true;
      }
    }

    return false;
  }


  //----------- our VMListener interface

  // All we rely on here is that the scheduler breaks transitions at all
  // insns that could be races. We then just have to look at all currently
  // executed insns and don't rely on any past-exec info, PROVIDED that we only
  // use execution parameters (index or reference values) that are retrieved
  // from the operand stack, and not cached in the insn from a previous exec
  // (all the insns we look at are pre-exec, i.e. don't have their caches
  // updated yet)
  @Override
  public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> newCG) {

    if (newCG instanceof ThreadChoiceFromSet) {
      ThreadInfo[] threads = ((ThreadChoiceFromSet)newCG).getAllThreadChoices();
      checkRace(threads);
    }
  }

  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute) {
    if (race != null) {
      // we're done, report as quickly as possible
      //ti.skipInstruction();
      ti.breakTransition("dataRace");
    }
  }

}