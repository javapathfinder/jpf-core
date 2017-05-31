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
import gov.nasa.jpf.jvm.bytecode.GOTO;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassInfoException;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ExceptionHandler;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * a listener to report coverage statistics
 *
 * The idea is to collect per-class/-method/-thread information about
 * executed instructions, and then analyze this deeper when it comes to
 * report time (e.g. branch coverage, basic blocks, ..)
 *
 * Keep in mind that this is potentially a concurrent, model checked program,
 * i.e. there is more to coverage than what hits the eye of a static analyzer
 * (exceptions, data and thread CGs)
 */
public class CoverageAnalyzer extends ListenerAdapter implements PublisherExtension {

  static Logger log = JPF.getLogger("gov.nasa.jpf.listener.CoverageAnalyzer");

  static class Coverage {

    int total;
    int covered;

    Coverage(int total, int covered) {
      this.total = total;
      this.covered = covered;
    }

    public void add(Coverage cov) {
      total += cov.total;
      covered += cov.covered;
    }

    public int percent() {
      if (total > 0) {
        return covered * 100 / total;
      }

      return (Integer.MIN_VALUE);
    }

    public int covered() {
      return covered;
    }

    public int total() {
      return total;
    }

    public boolean isPartiallyCovered() {
      return ((covered > 0) && (covered < total));
    }

    public boolean isNotCovered() {
      return (covered == 0);
    }

    public boolean isFullyCovered() {
      return (covered == total);
    }
  }

  static class MethodCoverage {

    MethodInfo mi;

    // we base everything else on bytecode instruction coverage
    BitSet[] covered;
    BitSet basicBlocks; // set on demand
    BitSet handlers; // set on demand
    BitSet branches; // set on demand
    BitSet branchTrue;
    BitSet branchFalse;

    MethodCoverage(MethodInfo mi) {
      this.mi = mi;
      log.info("add method: " + mi.getUniqueName());
    }

    MethodInfo getMethodInfo() {
      return mi;
    }

    void setExecuted(ThreadInfo ti, Instruction insn) {
      int idx = ti.getId();

      if (covered == null) {
        covered = new BitSet[idx + 1];
      } else if (idx >= covered.length) {
        BitSet[] a = new BitSet[idx + 1];
        System.arraycopy(covered, 0, a, 0, covered.length);
        covered = a;
      }

      if (covered[idx] == null) {
        covered[idx] = new BitSet(mi.getInstructions().length);
      }

      int off = insn.getInstructionIndex();
      covered[idx].set(off);

      if (showBranchCoverage && (insn instanceof IfInstruction)) {
        if (branchTrue == null) {
          branchTrue = new BitSet(mi.getInstructions().length);
          branchFalse = new BitSet(branchTrue.size());
        }
        if (!((IfInstruction) insn).getConditionValue()) {
          branchTrue.set(off);
        } else {
          branchFalse.set(off);
        }
      }
    }

    void setCGed(ThreadInfo ti, Instruction insn) {
      ti = null;  // Remove IDE warning about unused variable.
      // Hmm, we have to store the bb set at this point
      BitSet bb = getBasicBlocks();
      Instruction next = insn.getNext();
      if (next != null) { // insn might be a sync return
        bb.set(next.getInstructionIndex());
      }
    }

    BitSet getExecutedInsn() {
      int nTotal = mi.getInstructions().length;
      BitSet bUnion = new BitSet(nTotal);

      if (covered != null) {
        for (BitSet b : covered) {
          if (b != null) {
            bUnion.or(b);
          }
        }
      }

      return bUnion;
    }

    Coverage getCoveredInsn() {
      int nTotal = mi.getInstructions().length;

      if (excludeHandlers) {
        nTotal -= getHandlers().cardinality();
      }

      if (covered != null) {
        BitSet bExec = getExecutedInsn();
        if (excludeHandlers) {
          bExec.andNot(getHandlers());
        }
        return new Coverage(nTotal, bExec.cardinality());
      } else {
        return new Coverage(nTotal, 0);
      }
    }

    Coverage getCoveredLines() {
      BitSet executable = new BitSet();
      BitSet covered = new BitSet();

      getCoveredLines(executable, covered);

      return new Coverage(executable.cardinality(), covered.cardinality());
    }

    boolean getCoveredLines(BitSet executable, BitSet covered) {
      Instruction inst[] = mi.getInstructions();
      BitSet insn;
      int i, line;

      if (covered == null) {
        return false;
      }

      insn = getExecutedInsn();
      if (excludeHandlers) {
        insn.andNot(getHandlers());
      }

      if (branchTrue != null) {
        for (i = branchTrue.length() - 1; i >= 0; i--) {
          boolean cTrue = branchTrue.get(i);
          boolean cFalse = branchFalse.get(i);

          if ((!cTrue || !cFalse) && (inst[i] instanceof IfInstruction)) {
            insn.clear(i);
          }
        }
      }

      for (i = inst.length - 1; i >= 0; i--) {
        line = inst[i].getLineNumber();
         
        if (line > 0) {
          executable.set(line);
          covered.set(line);
        }
      }

      for (i = inst.length - 1; i >= 0; i--) {
        line = inst[i].getLineNumber();
        if ((!insn.get(i)) && (line > 0)) {         // TODO What do we do with instructions that don't have a line number.  Is this even possible?
          covered.clear(line);
        }
      }

      return true;
    }

    Coverage getCoveredBranches() {
      BitSet b = getBranches();
      int nTotal = b.cardinality();
      int nCovered = 0;

      if (branchTrue != null) {
        int n = branchTrue.size();

        for (int i = 0; i < n; i++) {
          boolean cTrue = branchTrue.get(i);
          boolean cFalse = branchFalse.get(i);

          if (cTrue && cFalse) {
            nCovered++;
          }
        }
      }

      return new Coverage(nTotal, nCovered);
    }

    BitSet getHandlerStarts() {
      BitSet b = new BitSet(mi.getInstructions().length);
      ExceptionHandler[] handler = mi.getExceptions();

      if (handler != null) {
        for (int i = 0; i < handler.length; i++) {
          Instruction hs = mi.getInstructionAt(handler[i].getHandler());
          b.set(hs.getInstructionIndex());
        }
      }

      return b;
    }

    BitSet getHandlers() {
      // this algorithm is a bit subtle - we walk through the code until
      // we hit a forward GOTO (or RETURN). If the insn following the goto is the
      // beginning of a handler, we mark everything up to the jump address
      // as a handler block

      if (handlers == null) {
        BitSet hs = getHandlerStarts();
        Instruction[] code = mi.getInstructions();
        BitSet b = new BitSet(code.length);

        if (!hs.isEmpty()) {
          for (int i = 0; i < code.length; i++) {
            Instruction insn = code[i];
            if (insn instanceof GOTO) {
              GOTO gotoInsn = (GOTO) insn;
              if (!gotoInsn.isBackJump() && hs.get(i + 1)) { // jump around handler
                int handlerEnd = gotoInsn.getTarget().getInstructionIndex();
                for (i++; i < handlerEnd; i++) {
                  b.set(i);
                }
              }
            } else if (insn instanceof JVMReturnInstruction) { // everything else is handler
              for (i++; i < code.length; i++) {
                b.set(i);
              }
            }
          }
        }

        handlers = b;
      }

      return handlers;
    }

    // that's kind of redundant with basic blocks, but not really - here
    // we are interested in the logic behind branches, i.e. we want to know
    // what the condition values were. We are not interested in GOTOs and exceptions
    BitSet getBranches() {
      if (branches == null) {
        Instruction[] code = mi.getInstructions();
        BitSet br = new BitSet(code.length);

        for (int i = 0; i < code.length; i++) {
          Instruction insn = code[i];
          if (insn instanceof IfInstruction) {
            br.set(i);
          }
        }

        branches = br;
      }

      return branches;
    }

    BitSet getBasicBlocks() {
      if (basicBlocks == null) {
        Instruction[] code = mi.getInstructions();
        BitSet bb = new BitSet(code.length);

        bb.set(0); // first insn is always a bb start

        // first, look at the insn type
        for (int i = 0; i < code.length; i++) {
          Instruction insn = code[i];
          if (insn instanceof IfInstruction) {
            IfInstruction ifInsn = (IfInstruction) insn;

            Instruction tgt = ifInsn.getTarget();
            bb.set(tgt.getInstructionIndex());

            tgt = ifInsn.getNext();
            bb.set(tgt.getInstructionIndex());
          } else if (insn instanceof GOTO) {
            Instruction tgt = ((GOTO) insn).getTarget();
            bb.set(tgt.getInstructionIndex());
          } else if (insn instanceof JVMInvokeInstruction) {
            // hmm, this might be a bit too conservative, but who says we
            // don't jump out of a caller into a handler, or even that we
            // ever return from the call?
            Instruction tgt = insn.getNext();
            bb.set(tgt.getInstructionIndex());
          }
        }

        // and now look at the handlers (every first insn is a bb start)
        ExceptionHandler[] handlers = mi.getExceptions();
        if (handlers != null) {
          for (int i = 0; i < handlers.length; i++) {
            Instruction tgt = mi.getInstructionAt(handlers[i].getHandler());
            bb.set(tgt.getInstructionIndex());
          }
        }

        basicBlocks = bb;

      /** dump
      System.out.println();
      System.out.println(mi.getFullName());
      for (int i=0; i<code.length; i++) {
      System.out.print(String.format("%1$2d %2$c ",i, bb.get(i) ? '>' : ' '));
      System.out.println(code[i]);
      }
       **/
      }

      return basicBlocks;
    }

    Coverage getCoveredBasicBlocks() {
      BitSet bExec = getExecutedInsn();
      BitSet bb = getBasicBlocks();
      int nCov = 0;

      if (excludeHandlers) {
        BitSet handlers = getHandlers();
        bb.and(handlers);
      }

      if (bExec != null) {
        BitSet bCov = new BitSet(bb.size());
        bCov.or(bb);
        bCov.and(bExec);
        nCov = bCov.cardinality();
      }

      return new Coverage(bb.cardinality(), nCov);
    }
  }

  static class ClassCoverage {

    String className; // need to store since we never might get a ClassInfo
    ClassInfo ci;     // not initialized before the class is loaded
    boolean covered;
    HashMap<MethodInfo, MethodCoverage> methods;

    ClassCoverage(String className) {
      this.className = className;
    }

    void setLoaded(ClassInfo ci) {
      if (methods == null) {
        this.ci = ci;
        covered = true;
        log.info("used class: " + className);

        methods = new HashMap<MethodInfo, MethodCoverage>();
        for (MethodInfo mi : ci.getDeclaredMethodInfos()) {
          // <2do> what about MJI methods? we should report why we don't cover them
          if (!mi.isNative() && !mi.isAbstract()) {
            MethodCoverage mc = new MethodCoverage(mi);
            methods.put(mi, mc);
          }
        }
      }
    }

    boolean isInterface() {
      if (ci == null)           // never loaded
        if (!isCodeLoaded())    // can it be loaded?
          return false;         // will never load

      return ci.isInterface();
    }
    
    boolean isCodeLoaded() {
      if (ci != null)
        return true;

      try {
        ci = ClassLoaderInfo.getCurrentResolvedClassInfo(className);
      } catch (ClassInfoException cie) {
        log.warning("CoverageAnalyzer problem: " + cie);   // Just log the problem but don't fail.  We still want the report to be written.
      }
      
      return ci != null;
    }

    MethodCoverage getMethodCoverage(MethodInfo mi) {
      if (methods == null) {
        setLoaded(ci);
      }
      return methods.get(mi);
    }

    Coverage getCoveredMethods() {
      Coverage cov = new Coverage(0, 0);

      if (methods != null) {
        cov.total = methods.size();

        for (MethodCoverage mc : methods.values()) {
          if (mc.covered != null) {
            cov.covered++;
          }
        }
      }

      return cov;
    }

    Coverage getCoveredInsn() {
      Coverage cov = new Coverage(0, 0);

      if (methods != null) {
        for (MethodCoverage mc : methods.values()) {
          Coverage c = mc.getCoveredInsn();
          cov.total += c.total;
          cov.covered += c.covered;
        }
      }

      return cov;
    }

    boolean getCoveredLines(BitSet executable, BitSet covered) {
      boolean result = false;

      if (methods == null) {
        return false;
      }

      for (MethodCoverage mc : methods.values()) {
        result = mc.getCoveredLines(executable, covered) || result;
      }

      return result;
    }

    Coverage getCoveredLines() {
      BitSet executable = new BitSet();
      BitSet covered = new BitSet();

      getCoveredLines(executable, covered);

      return new Coverage(executable.cardinality(), covered.cardinality());
    }

    Coverage getCoveredBasicBlocks() {
      Coverage cov = new Coverage(0, 0);

      if (methods != null) {
        for (MethodCoverage mc : methods.values()) {
          Coverage c = mc.getCoveredBasicBlocks();
          cov.total += c.total;
          cov.covered += c.covered;
        }
      }

      return cov;
    }

    Coverage getCoveredBranches() {
      Coverage cov = new Coverage(0, 0);

      if (methods != null) {
        for (MethodCoverage mc : methods.values()) {
          Coverage c = mc.getCoveredBranches();
          cov.total += c.total;
          cov.covered += c.covered;
        }
      }

      return cov;
    }
  }

  StringSetMatcher includes = null; //  means all
  StringSetMatcher excludes = null; //  means none
  StringSetMatcher loaded;
  static boolean loadedOnly; // means we only check loaded classes that are not filtered
  static boolean showMethods;      // do we want to show per-method coverage?
  static boolean showMethodBodies;
  static boolean excludeHandlers;  // do we count the handlers in? (off-nominal CF)
  static boolean showBranchCoverage; // makes only sense with showMethods
  static boolean showRequirements; // report requirements coverage
  HashMap<String, ClassCoverage> classes = new HashMap<String, ClassCoverage>();

  public CoverageAnalyzer(Config conf, JPF jpf) {
    includes = StringSetMatcher.getNonEmpty(conf.getStringArray("coverage.include"));
    excludes = StringSetMatcher.getNonEmpty(conf.getStringArray("coverage.exclude"));

    showMethods = conf.getBoolean("coverage.show_methods", false);
    showMethodBodies = conf.getBoolean("coverage.show_bodies", false);
    excludeHandlers = conf.getBoolean("coverage.exclude_handlers", false);
    showBranchCoverage = conf.getBoolean("coverage.show_branches", true);
    loadedOnly = conf.getBoolean("coverage.loaded_only", true);
    showRequirements = conf.getBoolean("coverage.show_requirements", false);

    if (!loadedOnly) {
      getCoverageCandidates(); // this might take a little while
    }

    jpf.addPublisherExtension(ConsolePublisher.class, this);
  }

  void getCoverageCandidates() {

    // doesn't matter in which order we process this, since we
    // just store one entry per qualified class name (i.e. there won't be
    // multiple entries)
    // NOTE : this doesn't yet deal with ClassLoaders, but that's also true for BCEL
    ClassLoaderInfo cl = ClassLoaderInfo.getCurrentClassLoader();
    for (String s : cl.getClassPathElements()) {
      log.fine("analyzing classpath element: " + s);
      File f = new File(s);
      if (f.exists()) {
        if (f.isDirectory()) {
          traverseDir(f, null);
        } else if (s.endsWith(".jar")) {
          traverseJar(f);
        }
      }
    }
  }

  void addClassEntry(String clsName) {
    ClassCoverage cc = new ClassCoverage(clsName);
    classes.put(clsName, cc);
    log.info("added class candidate: " + clsName);
  }

  boolean isAnalyzedClass(String clsName) {
    return StringSetMatcher.isMatch(clsName, includes, excludes);
  }

  void traverseDir(File dir, String pkgPrefix) {
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) {
        String prefix = f.getName();
        if (pkgPrefix != null) {
          prefix = pkgPrefix + '.' + prefix;
        }
        traverseDir(f, prefix);
      } else {
        String fname = f.getName();
        if (fname.endsWith(".class")) {
          if (f.canRead() && (f.length() > 0)) {
            String clsName = fname.substring(0, fname.length() - 6);
            if (pkgPrefix != null) {
              clsName = pkgPrefix + '.' + clsName;
            }

            if (isAnalyzedClass(clsName)) {
              addClassEntry(clsName);
            }
          } else {
            log.warning("cannot read class file: " + fname);
          }
        }
      }
    }
  }

  void traverseJar(File jar) {
    try {
      JarFile jf = new JarFile(jar);
      for (Enumeration<JarEntry> entries = jf.entries(); entries.hasMoreElements();) {
        JarEntry e = entries.nextElement();
        if (!e.isDirectory()) {
          String eName = e.getName();
          if (eName.endsWith(".class")) {
            if (e.getSize() > 0) {
              String clsName = eName.substring(0, eName.length() - 6);
              clsName = clsName.replace('/', '.');
              if (isAnalyzedClass(clsName)) {
                addClassEntry(clsName);
              }
            } else {
              log.warning("cannot read jar entry: " + eName);
            }
          }
        }
      }
    } catch (IOException iox) {
      iox.printStackTrace();
    }
  }


  HashMap<String, Integer> getGlobalRequirementsMethods() {
    HashMap<String, Integer> map = new HashMap<String, Integer>();

    // <2do> this is extremely braindead, since inefficient
    // it would be much better to do this with Class<?> instead of ClassInfo, but
    // then we need a JPF- ClassLoader, since the path might be different. As a
    // middle ground, we could use BCEL

    for (ClassCoverage cc : classes.values()) {
      ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(cc.className);
      for (MethodInfo mi : ci.getDeclaredMethodInfos()) {
        AnnotationInfo ai = getRequirementsAnnotation(mi);
        if (ai != null) {
          for (String id : ai.getValueAsStringArray()) {
            Integer n = map.get(id);
            if (n == null) {
              map.put(id, 1);
            } else {
              map.put(id, n + 1);
            }
          }
        }
      }
    }

    return map;
  }

  int computeTotalRequirementsMethods(HashMap<String, Integer> map) {
    int n = 0;
    for (Integer i : map.values()) {
      n += i;
    }
    return n;
  }

  private void computeCoverages(String packageFilter, List<Map.Entry<String, ClassCoverage>> clsEntries, Coverage cls, Coverage mth, Coverage branch, Coverage block, Coverage line, Coverage insn) {
    for (Map.Entry<String, ClassCoverage> e : clsEntries) {
      if (e.getKey().startsWith(packageFilter)) {
        ClassCoverage cc = e.getValue();

        if (cc.isInterface()) {
          continue; // no code
        }

        cls.total++;
        
        if (cc.covered) {
          cls.covered++;
        }

        insn.add(cc.getCoveredInsn());
        line.add(cc.getCoveredLines());
        block.add(cc.getCoveredBasicBlocks());
        branch.add(cc.getCoveredBranches());
        mth.add(cc.getCoveredMethods());
      }
    }
  }


  /**
   * print uncovered source ranges
   */

  //-------- the listener interface
  @Override
  public void classLoaded(VM vm, ClassInfo ci) {
    String clsName = ci.getName();

    if (loadedOnly) {
      if (isAnalyzedClass(clsName)) {
        addClassEntry(clsName);
      }
    }

    ClassCoverage cc = classes.get(clsName);
    if (cc != null) {
      cc.setLoaded(ci);
    }
  }
  MethodInfo lastMi = null;
  MethodCoverage lastMc = null;

  MethodCoverage getMethodCoverage(Instruction insn) {

    if (!insn.isExtendedInstruction()) {
      MethodInfo mi = insn.getMethodInfo();
      if (mi != lastMi) {
        lastMc = null;
        lastMi = mi;
        ClassInfo ci = mi.getClassInfo();
        if (ci != null) {
          ClassCoverage cc = classes.get(ci.getName());
          if (cc != null) {
            lastMc = cc.getMethodCoverage(mi);
          }
        }
      }

      return lastMc;
    }

    return null;
  }
  HashMap<String, HashSet<MethodCoverage>> requirements;

  void updateRequirementsCoverage(String[] ids, MethodCoverage mc) {
    if (requirements == null) {
      requirements = new HashMap<String, HashSet<MethodCoverage>>();
    }

    for (String id : ids) {
      HashSet<MethodCoverage> mcs = requirements.get(id);
      if (mcs == null) {
        mcs = new HashSet<MethodCoverage>();
        requirements.put(id, mcs);
      }

      if (!mcs.contains(mc)) {
        mcs.add(mc);
      }
    }
  }

  AnnotationInfo getRequirementsAnnotation(MethodInfo mi) {
    // <2do> should probably look for overridden method annotations
    return mi.getAnnotation("gov.nasa.jpf.Requirement");
  }

  @Override
  public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {
    MethodCoverage mc = getMethodCoverage(executedInsn);

    if (mc != null) {
      mc.setExecuted(ti, executedInsn);

      if (showRequirements) {
        if (executedInsn.getPosition() == 0) { // first insn in method, check for Requirements
          AnnotationInfo ai = getRequirementsAnnotation(mc.getMethodInfo());
          if (ai != null) {
            String[] ids = ai.getValueAsStringArray();
            updateRequirementsCoverage(ids, mc);
          }
        }
      }
    }
  }

  @Override
  public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> newCG) {
    /*** should be an option
    Instruction insn = vm.getLastInstruction();
    MethodCoverage mc = getMethodCoverage(vm);
    mc.setCGed(vm.getLastThreadInfo(),insn);
     ***/
  }

  //-------- the publisher interface
  private Publisher publisher;
  private ArrayList<Map.Entry<String, ClassCoverage>> clsEntries;


  abstract class Publish {
    PrintWriter pw;

    Publish () {}
    Publish (Publisher p){
      pw = p.getOut();
    }

    abstract void publish();
    abstract void printClassCoverages();
    abstract void printRequirementsCoverage();
  }

  class PublishConsole extends Publish {
    PublishConsole (ConsolePublisher p){
      super(p);
    }

    @Override
	void publish() {
      publisher.publishTopicStart("coverage statistics");

      printClassCoverages();

      if (showRequirements) {
        printRequirementsCoverage();
      }

    }

    void printCoverage (Coverage cov){
      int nTotal = cov.total();
      int nCovered = cov.covered();

      String s;
      if (nTotal <= 0) {
        s = " -  ";
      } else {
        s = String.format("%.2f (%d/%d)", ((double) nCovered / nTotal), nCovered, nTotal);
      }
      pw.print(String.format("%1$-18s", s));
    }


    @Override
	void printClassCoverages() {
      String space = "  ";
      Coverage clsCoverage = new Coverage(0, 0);
      Coverage mthCoverage = new Coverage(0, 0);
      Coverage bbCoverage = new Coverage(0, 0);
      Coverage lineCoverage = new Coverage(0, 0);
      Coverage insnCoverage = new Coverage(0, 0);
      Coverage branchCoverage = new Coverage(0, 0);

      computeCoverages("", clsEntries, clsCoverage, mthCoverage, branchCoverage, bbCoverage, lineCoverage, insnCoverage);

      pw.println();
      pw.println("-------------------------------------------- class coverage ------------------------------------------------");
      pw.println("bytecode            line                basic-block         branch              methods             location");
      pw.println("------------------------------------------------------------------------------------------------------------");

      // Write Body
      for (Map.Entry<String, ClassCoverage> e : clsEntries) {
        ClassCoverage cc = e.getValue();

        printCoverage(cc.getCoveredInsn());
        pw.print(space);

        printCoverage(cc.getCoveredLines());
        pw.print(space);

        printCoverage(cc.getCoveredBasicBlocks());
        pw.print(space);

        printCoverage(cc.getCoveredBranches());
        pw.print(space);

        printCoverage(cc.getCoveredMethods());
        pw.print(space);

        pw.println(e.getKey());

        if (showMethods) {
          printMethodCoverages(cc);
        }
      }

      pw.println();
      pw.println("------------------------------------------------------------------------------------------------------------");

      printCoverage(insnCoverage);
      pw.print(space);
      printCoverage(lineCoverage);
      pw.print(space);
      printCoverage(bbCoverage);
      pw.print(space);
      printCoverage(branchCoverage);
      pw.print(space);
      printCoverage(mthCoverage);
      pw.print(space);
      printCoverage(clsCoverage);
      pw.println(" total");

    }

    @Override
	void printRequirementsCoverage() {
      HashMap<String, Integer> reqMethods = getGlobalRequirementsMethods();

      String space = "  ";
      Coverage bbAll = new Coverage(0, 0);
      Coverage insnAll = new Coverage(0, 0);
      Coverage branchAll = new Coverage(0, 0);
      Coverage mthAll = new Coverage(0, 0);
      Coverage reqAll = new Coverage(0, 0);

      reqAll.total = reqMethods.size();
      mthAll.total = computeTotalRequirementsMethods(reqMethods);

      pw.println();
      pw.println();
      pw.println("--------------------------------- requirements coverage -----------------------------------");
      pw.println("bytecode            basic-block         branch              methods             requirement");
      pw.println("-------------------------------------------------------------------------------------------");

      for (String id : Misc.getSortedKeyStrings(reqMethods)) {

        Coverage bbCoverage = new Coverage(0, 0);
        Coverage insnCoverage = new Coverage(0, 0);
        Coverage branchCoverage = new Coverage(0, 0);
        Coverage reqMth = new Coverage(reqMethods.get(id), 0);

        if (requirements != null && requirements.containsKey(id)) {
          reqAll.covered++;
          for (MethodCoverage mc : requirements.get(id)) {
            insnCoverage.add(mc.getCoveredInsn());
            bbCoverage.add(mc.getCoveredBasicBlocks());
            branchCoverage.add(mc.getCoveredBranches());

            mthAll.covered++;
            reqMth.covered++;
          }


          printCoverage(insnCoverage);
          pw.print(space);
          printCoverage(bbCoverage);
          pw.print(space);
          printCoverage(branchCoverage);
          pw.print(space);
          printCoverage(reqMth);
          pw.print("\"" + id + "\"");


          pw.println();

          if (showMethods) {
            for (MethodCoverage mc : requirements.get(id)) {

              pw.print(space);
              printCoverage(mc.getCoveredInsn());
              pw.print(space);
              printCoverage(mc.getCoveredBasicBlocks());
              pw.print(space);
              printCoverage(mc.getCoveredBranches());
              pw.print(space);

              pw.print(mc.getMethodInfo().getFullName());
              pw.println();
            }
          }
        } else { // requirement not covered
          pw.print(" -                   -                   -                  ");

          printCoverage(reqMth);
          pw.print("\"" + id + "\"");
          pw.println();
        }

        insnAll.add(insnCoverage);
        bbAll.add(bbCoverage);
        branchAll.add(branchCoverage);
      }

      pw.println();
      pw.println("------------------------------------------------------------------------------------------");

      printCoverage(insnAll);
      pw.print(space);
      printCoverage(bbAll);
      pw.print(space);
      printCoverage(branchAll);
      pw.print(space);
      printCoverage(mthAll);
      pw.print(space);
      printCoverage(reqAll);
      pw.print(" total");

      pw.println();
    }

    void printMethodCoverages(ClassCoverage cc) {
      String space = "  ";
      boolean result = true;

      if (cc.methods == null) {
        return;
      }

      ArrayList<Map.Entry<MethodInfo, MethodCoverage>> mthEntries =
              Misc.createSortedEntryList(cc.methods, new Comparator<Map.Entry<MethodInfo, MethodCoverage>>() {

        @Override
		public int compare(Map.Entry<MethodInfo, MethodCoverage> o1,
                Map.Entry<MethodInfo, MethodCoverage> o2) {
          int a = o2.getValue().getCoveredInsn().percent();
          int b = o1.getValue().getCoveredInsn().percent();

          if (a == b) {
            return o2.getKey().getUniqueName().compareTo(o1.getKey().getUniqueName());
          } else {
            return a - b;
          }
        }
      });

      Coverage emptyCoverage = new Coverage(0, 0);

      for (Map.Entry<MethodInfo, MethodCoverage> e : mthEntries) {
        MethodCoverage mc = e.getValue();
        MethodInfo mi = mc.getMethodInfo();
        Coverage insnCoverage = mc.getCoveredInsn();
        Coverage lineCoverage = mc.getCoveredLines();
        Coverage branchCoverage = mc.getCoveredBranches();

        result = result && insnCoverage.isFullyCovered();


        pw.print(space);
        printCoverage(insnCoverage);

        pw.print(space);
        printCoverage(lineCoverage);

        pw.print(space);
        printCoverage(mc.getCoveredBasicBlocks());

        pw.print(space);
        printCoverage(branchCoverage);

        pw.print(space);
        printCoverage(emptyCoverage);

        pw.print(space);
        pw.print(mi.getLongName());
        pw.println();

        if (showMethodBodies &&
                (!insnCoverage.isFullyCovered() || !branchCoverage.isFullyCovered())) {
          printBodyCoverage(mc);
        }
      }
    }

    void printBodyCoverage(MethodCoverage mc) {
      MethodInfo mi = mc.getMethodInfo();
      Instruction[] code = mi.getInstructions();
      BitSet cov = mc.getExecutedInsn();
      int i, start = -1;

      BitSet handlers = mc.getHandlers();

      if (excludeHandlers) {
        cov.andNot(handlers);
      }

      for (i = 0; i < code.length; i++) {
        if (!cov.get(i)) { // not covered
          if (start == -1) {
            start = i;
          }
        } else { // covered
          if (start != -1) {
            printSourceRange(code, handlers, start, i - 1, "");
            start = -1;
          }
        }
      }
      if (start != -1) {
        printSourceRange(code, handlers, start, i - 1, "");
      }

      // now print the missing branches
      BitSet branches = mc.getBranches();
      lastStart = -1; // reset in case condition and branch are in same line
      for (i = 0; i < code.length; i++) {
        if (branches.get(i)) {
          String prefix = "";
          BitSet bTrue = mc.branchTrue;
          BitSet bFalse = mc.branchFalse;
          if (bTrue != null) { // means we have condition bit sets
            boolean cTrue = bTrue.get(i);
            boolean cFalse = bFalse.get(i);
            if (cTrue) {
              prefix = cFalse ? "" : "F "; // covered or false missing
            } else {
              prefix = cFalse ? "T " : "N "; // true or both missing
            }
          } else {
            prefix = "N ";                   // not covered at all
          }

          if (prefix != null) {
            printSourceRange(code, handlers, i, i, prefix);
          }
        }
      }
    }

    // there might be several ranges within the same source line
    int lastStart = -1;

    void printSourceRange(Instruction[] code, BitSet handlers,
            int start, int end, String prefix) {

      int line = code[start].getLineNumber();

      if (lastStart == line) {
        return;
      }

      lastStart = line;

      printLocation(prefix, "at", code[start].getSourceLocation(), handlers.get(start) ? "x" : "");

      if (line != code[end].getLineNumber()) {
        printLocation(prefix, "..", code[end].getSourceLocation(), handlers.get(end) ? "x" : "");
      }
    // we need the "(..)" wrapper so that Eclipse parses this
    // as a source location when displaying the report in a console
    }

    private void printLocation(String prefix, String at, String location, String suffix) {

      printBlanks(pw, 84);
      pw.print(prefix);
      pw.print(at);
      pw.print(' ');
      pw.print(location);
      pw.print(' ');
      pw.println(suffix);
    }

    void printBlanks(PrintWriter pw, int n) {
      for (int i = 0; i < n; i++) {
        pw.print(' ');
      }
    }

  }


  @Override
  public void publishFinished(Publisher publisher) {

    if (clsEntries == null) {
      clsEntries = Misc.createSortedEntryList(classes, new Comparator<Map.Entry<String, ClassCoverage>>() {

        @Override
		public int compare(Map.Entry<String, ClassCoverage> o1,
                Map.Entry<String, ClassCoverage> o2) {
          return o2.getKey().compareTo(o1.getKey());
        }
      });
    }

    this.publisher = publisher;

    if (publisher instanceof ConsolePublisher) {
      new PublishConsole((ConsolePublisher) publisher).publish();
    }
  }
}
