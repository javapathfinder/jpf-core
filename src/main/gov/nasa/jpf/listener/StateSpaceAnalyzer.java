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
import gov.nasa.jpf.jvm.bytecode.JVMFieldInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.MONITORENTER;
import gov.nasa.jpf.jvm.bytecode.MONITOREXIT;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.BooleanChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DoubleChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.IntChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * a listener that collects information about ChoiceGenerators, choices and
 * where they are used. The purpose is to find out what causes the state space
 * size, and to give hints of how to reduce it.
 * The interesting part is that this is a listener that doesn't work off traces,
 * but needs to collect info up to a point where we want it to report. That's
 * state space or resource related, i.e. a combination of
 *
 *  - number of transitions
 *  - memory consumption
 *  - elapsed time
 *
 * once the limit is reached, we stop the search and report.
 *
 * There are two parts we are interested in:
 *
 *  - what CGs do we have
 *  - what creates those CGs (thread,insn,source) = last step insn
 */
public class StateSpaceAnalyzer extends ListenerAdapter implements PublisherExtension {
  // Search termination conditions

  private final long m_maxTime;
  private final long m_maxMemory;
  private final int m_maxStates;
  private final int m_maxChoices;
  private final ArrayList<CGGrouper> m_groupers = new ArrayList<CGGrouper>();
  private final int m_maxOutputLines; // how many detail lines do we display in the report
  private long m_terminateTime;
  private int m_choiceCount;

  public StateSpaceAnalyzer(Config config, JPF jpf) {
    m_maxStates = config.getInt("ssa.max_states", -1);
    m_maxTime = config.getDuration("ssa.max_time", -1);
    m_maxMemory = config.getMemorySize("ssa.max_memory", -1);
    m_maxChoices = config.getInt("ssa.max_choices", -1);
    m_maxOutputLines = config.getInt("ssa.max_output_lines", 10);

    initGroupers(config);

    jpf.addPublisherExtension(ConsolePublisher.class, this);
  }

  private void initGroupers(Config config) {
    HashMap<String, CGAccessor> accessors;
    CGGrouper grouper;
    int i;

    if (config.getStringArray("ssa.sort_order") == null) {
      config.setProperty("ssa.sort_order", "type");
      config.setProperty("ssa.sort_order2", "package,class,method,instruction,type");
    }

    accessors = new HashMap<String, CGAccessor>(5);
    accessors.put("package", new CGPackageAccessor());
    accessors.put("class", new CGClassAccessor());
    accessors.put("method", new CGMethodAccessor());
    accessors.put("instruction", new CGInstructionAccessor());
    accessors.put("type", new CGTypeAccessor());

    m_groupers.add(initGrouper(config, "ssa.sort_order", accessors));

    for (i = 2; true; i++) {
      grouper = initGrouper(config, "ssa.sort_order" + i, accessors);
      if (grouper == null) {
        break;
      }

      m_groupers.add(grouper);
    }
  }

  private CGGrouper initGrouper(Config config, String parameter, Map<String, CGAccessor> accessors) {
    CGGrouper grouper;
    CGAccessor list[];
    StringBuilder name;
    String key, sortOrder[];
    int i;

    sortOrder = config.getStringArray(parameter);
    if ((sortOrder == null) || (sortOrder.length <= 0)) {
      return (null);
    }

    name = new StringBuilder();
    list = new CGAccessor[sortOrder.length];

    for (i = 0; i < sortOrder.length; i++) {
      key = sortOrder[i].toLowerCase();
      name.append(key);
      name.append(", ");

      list[i] = accessors.get(key);

      if (list[i] == null) {
        config.exception("Unknown sort key: " + sortOrder[i] + ".  Found in parameter: " + parameter);
      }
    }

    name.setLength(name.length() - 2);
    grouper = new CGGrouper(list, name.toString());

    return (grouper);
  }

  @Override
  public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> newCG) {
    int i;

    // NOTE: we get this from SystemState.nextSuccessor, i.e. when the CG
    // is actually used (which doesn't necessarily mean it produces a new state,
    // but it got created from a new state)
    
    // The original code stored each choice generator in an ArrayList.  For long 
    // running tests, this would grow and cause an OutOfMemoryError.  Now, the 
    // generators are dealt with as they are created.  This means a bit more 
    // processing up front but huge memory savings in the long run.  If the 
    // machine has multiple processors, a better solution would be to have a 
    // background thread process the generators.

    m_choiceCount += newCG.getTotalNumberOfChoices();

    for (i = m_groupers.size(); --i >= 0; )
      m_groupers.get(i).add(newCG);
  }

  @Override
  public void searchStarted(Search search) {
    int i;
    
    for (i = m_groupers.size(); --i >= 0; )
      m_groupers.get(i).clear();
    
    m_choiceCount = 0;
    m_terminateTime = m_maxTime + System.currentTimeMillis();
  }

  @Override
  public void stateAdvanced(Search search) {
    if (shouldTerminate(search)) {
      search.terminate();
    }
  }

  private boolean shouldTerminate(Search search) {
    if ((m_maxStates >= 0) && (search.getVM().getStateCount() >= m_maxStates)) {
      return (true);
    }

    if ((m_maxTime >= 0) && (System.currentTimeMillis() >= m_terminateTime)) {
      return (true);
    }

    if ((m_maxMemory >= 0) && (Runtime.getRuntime().totalMemory() >= m_maxMemory)) {
      return (true);
    }

    if ((m_maxChoices >= 0) && (m_choiceCount >= m_maxChoices)) {
      return (true);
    }

    return (false);
  }

  @Override
  public void publishFinished(Publisher publisher) {
    CGGrouper groupers[];

    groupers = new CGGrouper[m_groupers.size()];
    m_groupers.toArray(groupers);

    if (publisher instanceof ConsolePublisher) {
      new PublishConsole((ConsolePublisher) publisher, groupers, m_maxOutputLines).publish();
    }
  }

  private enum CGType {

    FieldAccess,
    ObjectWait,
    ObjectNotify,
    SyncEnter,
    SyncExit,
    ThreadStart,
    ThreadTerminate,
    ThreadSuspend,
    ThreadResume,
    SyncCall,
    SyncReturn,
    AtomicOp,
    DataChoice
  }

  private interface CGAccessor {

    public Object getValue(ChoiceGenerator generator);
  }

  private static class CGPackageAccessor implements CGAccessor {

    @Override
	public Object getValue(ChoiceGenerator generator) {
      ClassInfo ci;
      MethodInfo mi;
      Instruction instruction;

      instruction = generator.getInsn();
      if (instruction == null) {
        return (null);
      }

      mi = instruction.getMethodInfo();
      if (mi == null) {
        return (null);
      }

      ci = mi.getClassInfo();
      if (ci == null) {
        return (null);
      }

      return (ci.getPackageName());
    }
  }

  private static class CGClassAccessor implements CGAccessor {

    @Override
	public Object getValue(ChoiceGenerator generator) {
      ClassInfo ci;
      MethodInfo mi;
      Instruction instruction;

      instruction = generator.getInsn();
      if (instruction == null) {
        return (null);
      }

      mi = instruction.getMethodInfo();
      if (mi == null) {
        return (null);
      }

      ci = mi.getClassInfo();
      if (ci == null) {
        return (null);
      }

      return (ci.getName());
    }
  }

  private static class CGMethodAccessor implements CGAccessor {

    @Override
	public Object getValue(ChoiceGenerator generator) {
      MethodInfo mi;
      Instruction instruction;

      instruction = generator.getInsn();
      if (instruction == null) {
        return (null);
      }

      mi = instruction.getMethodInfo();
      if (mi == null) {
        return (null);
      }

      return (mi.getFullName());
    }
  }

  private static class CGInstructionAccessor implements CGAccessor {

    @Override
	public Object getValue(ChoiceGenerator generator) {
      return (generator.getInsn());
    }
  }

  private static class CGTypeAccessor implements CGAccessor {

    private static final String OBJECT_CLASS_NAME = Object.class.getName();
    private static final String THREAD_CLASS_NAME = Thread.class.getName();

    @Override
	public Object getValue(ChoiceGenerator generator) {
      if (generator instanceof ThreadChoiceGenerator) {
        return (getType((ThreadChoiceGenerator) generator));
      }

      if (generator instanceof BooleanChoiceGenerator) {
        return (CGType.DataChoice);
      }

      if (generator instanceof DoubleChoiceGenerator) {
        return (CGType.DataChoice);
      }

      if (generator instanceof IntChoiceGenerator) {
        return (CGType.DataChoice);
      }

      if (generator instanceof BooleanChoiceGenerator) {
        return (CGType.DataChoice);
      }

      return (null);
    }

    private static CGType getType(ThreadChoiceGenerator generator) {
      Instruction instruction;

      instruction = generator.getInsn();
      if (instruction == null) {
        return (null);
      }

      if (instruction instanceof JVMFieldInstruction) {
        return (CGType.FieldAccess);
      }

      if (instruction instanceof JVMInvokeInstruction) {
        return (getType((JVMInvokeInstruction) instruction));
      }

      if (instruction instanceof JVMReturnInstruction) {
        return (getType(generator, (JVMReturnInstruction) instruction));
      }

      if (instruction instanceof MONITORENTER) {
        return (CGType.SyncEnter);
      }

      if (instruction instanceof MONITOREXIT) {
        return (CGType.SyncExit);
      }

      return (null);
    }

    private static CGType getType(JVMInvokeInstruction instruction) {
      MethodInfo mi;

      if (is(instruction, OBJECT_CLASS_NAME, "wait")) {
        return (CGType.ObjectWait);
      }

      if (is(instruction, OBJECT_CLASS_NAME, "notify")) {
        return (CGType.ObjectNotify);
      }

      if (is(instruction, OBJECT_CLASS_NAME, "notifyAll")) {
        return (CGType.ObjectNotify);
      }

      if (is(instruction, THREAD_CLASS_NAME, "start")) {
        return (CGType.ThreadStart);
      }

      if (is(instruction, THREAD_CLASS_NAME, "suspend")) {
        return (CGType.ThreadSuspend);
      }

      if (is(instruction, THREAD_CLASS_NAME, "resume")) {
        return (CGType.ThreadResume);
      }

      mi = instruction.getInvokedMethod();
      if (mi.getClassName().startsWith("java.util.concurrent.atomic.")) {
        return (CGType.AtomicOp);
      }

      if (mi.isSynchronized()) {
        return (CGType.SyncCall);
      }

      return (null);
    }

    private static boolean is(JVMInvokeInstruction instruction, String className, String methodName) {
      MethodInfo mi;
      ClassInfo ci;

      mi = instruction.getInvokedMethod();
      if (!methodName.equals(mi.getName())) {
        return (false);
      }

      ci = mi.getClassInfo();

      if (!className.equals(ci.getName())) {
        return (false);
      }

      return (true);
    }

    private static CGType getType(ThreadChoiceGenerator generator, JVMReturnInstruction instruction) {
      MethodInfo mi;

      if (generator.getThreadInfo().getStackDepth() <= 1) // The main thread has 0 frames.  Other threads have 1 frame.
      {
        return (CGType.ThreadTerminate);
      }

      mi = instruction.getMethodInfo();
      if (mi.isSynchronized()) {
        return (CGType.SyncReturn);
      }

      return (null);
    }
  }

  private static class TreeNode {

    private final HashMap<Object, TreeNode> m_childNodes;
    private final ArrayList<Object> m_sortedValues;
    private final CGAccessor m_accessors[];
    private final Object m_value;
    private final int m_level;
    private String m_sampleGeneratorClassName;
    private Instruction m_sampleGeneratorInstruction;
    private int m_choiceCount;
    private int m_generatorCount;

    TreeNode(CGAccessor accessors[], int level, Object value) {
      m_accessors = accessors;
      m_level = level;
      m_value = value;

      if (level >= accessors.length) {
        m_childNodes = null;
        m_sortedValues = null;
      } else {
        m_sortedValues = new ArrayList<Object>();
        m_childNodes = new HashMap<Object, TreeNode>();
      }
    }

    public void add(ChoiceGenerator generator) {
      TreeNode child;
      Object value;

      m_generatorCount++;
      m_choiceCount += generator.getTotalNumberOfChoices();

      if (isLeaf()) {
        if (m_sampleGeneratorClassName == null) {
          m_sampleGeneratorClassName = generator.getClass().getName();
          m_sampleGeneratorInstruction = generator.getInsn();
        }

        return;
      }

      value = m_accessors[m_level].getValue(generator);
      child = m_childNodes.get(value);
      if (child == null) {
        child = new TreeNode(m_accessors, m_level + 1, value);
        m_childNodes.put(value, child);
      }

      child.add(generator);
    }

    public int getLevel() {
      return (m_level);
    }

    public Object getValue() {
      return (m_value);
    }

    public int getChoiceCount() {
      return (m_choiceCount);
    }

    public int getGeneratorCount() {
      return (m_generatorCount);
    }

    public String getSampleGeneratorClassName() {
      return (m_sampleGeneratorClassName);
    }
    
    public Instruction getSampleGeneratorInstruction() {
      return (m_sampleGeneratorInstruction);
    }

    public boolean isLeaf() {
      return (m_childNodes == null);
    }

    public void sort() {
      Comparator<Object> comparator;

      if (isLeaf()) {
        return;
      }

      m_sortedValues.clear();
      m_sortedValues.addAll(m_childNodes.keySet());

      comparator = new Comparator<Object>() {

        @Override
		public int compare(Object value1, Object value2) {
          TreeNode node1, node2;

          node1 = m_childNodes.get(value1);
          node2 = m_childNodes.get(value2);

          return (node2.getChoiceCount() - node1.getChoiceCount());  // Sort descending
        }
      };

      Collections.sort(m_sortedValues, comparator);

      for (TreeNode child : m_childNodes.values()) {
        child.sort();
      }
    }

    public List<TreeNode> tour() {
      List<TreeNode> result;

      result = new ArrayList<TreeNode>();
      tour(result);

      return (result);
    }

    public void tour(List<TreeNode> list) {
      TreeNode child;
      Object value;
      int i;

      list.add(this);

      if (isLeaf()) {
        return;
      }

      for (i = 0; i < m_sortedValues.size(); i++) {
        value = m_sortedValues.get(i);
        child = m_childNodes.get(value);
        child.tour(list);
      }
    }

    @Override
	public String toString() {
      StringBuilder result;

      result = new StringBuilder();

      if (m_value == null) {
        result.append("(null)");
      } else {
        result.append(m_value);
      }

      result.append(" - L");
      result.append(m_level);
      result.append(" / C");
      result.append(m_choiceCount);
      result.append(" / G");
      result.append(m_generatorCount);
      result.append(" / N");
      result.append(m_childNodes.size());

      return (result.toString());
    }
  }

  private static class CGGrouper {

    private final CGAccessor m_accessors[];
    private final String m_name;
    private       TreeNode m_root;
    private       boolean m_sorted;

    CGGrouper(CGAccessor accessors[], String name) {
      if (accessors.length <= 0) {
        throw new IllegalArgumentException("accessors.length <= 0");
      }

      if (name == null) {
        throw new NullPointerException("name == null");
      }

      m_accessors = accessors;
      m_name = name;
      
      clear();
    }
    
    public void clear() {
      m_sorted = false;
      m_root = new TreeNode(m_accessors, 0, "All");
    }

    public String getName() {
      return(m_name);
    }

    public int getLevelCount() {
      return(m_accessors.length);
    }

    public TreeNode getTree() {
      if (!m_sorted) {
        m_sorted = true;
        m_root.sort();
      }
      
      return(m_root);
    }
    
    public void add(ChoiceGenerator generator) {
      m_sorted = false;
      m_root.add(generator); 
    }
  }

  private static abstract class Publish {

    protected final Publisher m_publisher;
    protected final CGGrouper m_groupers[];
    protected final int m_maxOutputLines;
    protected PrintWriter m_output;

    Publish(Publisher publisher, CGGrouper groupers[], int maxOutputLines) {
      m_publisher = publisher;
      m_groupers = groupers;
      m_maxOutputLines = maxOutputLines;
    }

    public abstract void publish();
  }

  private static class PublishConsole extends Publish {

    PublishConsole(ConsolePublisher publisher, CGGrouper[] groupers, int maxOutputLines) {
      super(publisher, groupers, maxOutputLines);
      m_output = publisher.getOut();
    }

    @Override
	public void publish() {
      int i;

      for (i = 0; i < m_groupers.length; i++) {
        publishSortedData(m_groupers[i]);
      }
    }

    private void publishSortedData(CGGrouper grouper) {
      List<TreeNode> tour;
      TreeNode node;
      int i, lines, levelCount;

      lines = 0;
      levelCount = grouper.getLevelCount();
      node = grouper.getTree();
      tour = node.tour();

      m_publisher.publishTopicStart("Grouped By: " + grouper.getName());

      for (i = 0; (i < tour.size()) && (lines < m_maxOutputLines); i++) {
        node = tour.get(i);

        publishTreeNode(node);

        if (node.isLeaf()) {
          publishDetails(node, levelCount + 1);
          lines++;
        }
      }

      if (lines >= m_maxOutputLines) {
        m_output.println("...");
      }
    }

    private void publishTreeNode(TreeNode node) {
      Object value;

      // Tree
      publishPadding(node.getLevel());

      value = node.getValue();
      if (value == null) {
        m_output.print("(null)");
      } else {
        m_output.print(value);
      }

      // Choices
      m_output.print("  (choices: ");
      m_output.print(node.getChoiceCount());

      // Generators
      m_output.print(", generators: ");
      m_output.print(node.getGeneratorCount());

      m_output.println(')');
    }

    private void publishDetails(TreeNode node, int levelCount) {
      ChoiceGenerator generator;
      Instruction instruction;

      instruction = node.getSampleGeneratorInstruction();

      // Location
      publishPadding(levelCount);
      m_output.print("Location:  ");
      m_output.println(instruction.getFileLocation());

      // Code
      publishPadding(levelCount);
      m_output.print("Code:  ");
      m_output.println(instruction.getSourceOrLocation().trim());

      // Instruction
      publishPadding(levelCount);
      m_output.print("Instruction:  ");
      m_output.println(instruction.getMnemonic());

      // Position
      publishPadding(levelCount);
      m_output.print("Position:  ");
      m_output.println(instruction.getPosition());

      // Generator Class
      publishPadding(levelCount);
      m_output.print("Generator Class:  ");
      m_output.println(node.getSampleGeneratorClassName());
    }

    private void publishPadding(int levelCount) {
      int i;

      for (i = levelCount; i > 0; i--) {
        m_output.print("   ");
      }
    }
  }
}
