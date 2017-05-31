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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import gov.nasa.jpf.util.FinalBitSet;
import gov.nasa.jpf.util.PrintUtils;

/**
 * stream to write program state info in a readable and diff-able format.
 * This is mostly intended for debugging, but could also at some point be
 * used to restore such states.
 * 
 * Currently supports heap objects, classes (static fields), threads and stack frames
 */
public class JPFOutputStream extends OutputStream {
  
  PrintStream ps;
  
  boolean useSid = false;
  int maxElements = -1;
  
  public JPFOutputStream (OutputStream os){
    ps = new PrintStream(os);
  }
  
  public JPFOutputStream (PrintStream ps){
    this.ps = ps;
  }
  
  public JPFOutputStream (){
    this(System.out);
  }
  
  @Override
  public void close(){
    ps.flush();
    
    if (ps != System.err && ps != System.out){
      ps.close();
    }
  }
  
  public void printCommentLine(String msg){
    ps.print("// ");
    ps.println(msg);
  }
  
  public void print (ElementInfo ei, FieldInfo fi, boolean isFiltered){
    ps.print(fi.getName());
    ps.print(':');

    if (isFiltered){
      ps.print("X");
      
    } else {
      switch (fi.getTypeCode()) {
      case Types.T_BOOLEAN:
        ps.print(ei.getBooleanField(fi));
        break;
      case Types.T_BYTE:
        ps.print(ei.getByteField(fi));
        break;
      case Types.T_CHAR:
        PrintUtils.printCharLiteral(ps, ei.getCharField(fi));
        break;
      case Types.T_SHORT:
        ps.print(ei.getShortField(fi));
        break;
      case Types.T_INT:
        ps.print(ei.getIntField(fi));
        break;
      case Types.T_LONG:
        ps.print(ei.getLongField(fi));
        break;
      case Types.T_FLOAT:
        ps.print(ei.getFloatField(fi));
        break;
      case Types.T_DOUBLE:
        ps.print(ei.getDoubleField(fi));
        break;

      case Types.T_REFERENCE:
      case Types.T_ARRAY:
        PrintUtils.printReference(ps, ei.getReferenceField(fi));
        break;
      }
    }
  }
  
  protected void printFields (ElementInfo ei, FieldInfo[] fields, FinalBitSet filterMask){
    if (fields != null){
      for (int i = 0; i < fields.length; i++) {
        if (i > 0) {
          ps.print(',');
        }
        print(ei, fields[i], (filterMask != null && filterMask.get(i)));
      }
    }
  }
  
  public void print (ElementInfo ei, FinalBitSet filterMask){
    boolean isObject = ei.isObject();
    ClassInfo ci = ei.getClassInfo();
    
    int ref = (useSid) ? ei.getSid() : ei.getObjectRef();
    ps.printf("@%x ", ref);
    
    if (isObject){
      ps.print("object ");
      if (ei.isArray()){
        ps.print( Types.getTypeName(ci.getName()));
      } else {
        ps.print(ci.getName());
      }
    } else {
      ps.print("class ");
      ps.print(ci.getName());
    }
    
    ps.print(':');
    
    if (isObject){
      if (ei.isArray()){
        ps.print('[');
        ei.getArrayFields().printElements(ps, maxElements);
        ps.print(']');
            
      } else {
        ps.print('{');
        printFields(ei, ci.getInstanceFields(), filterMask);
        ps.print('}');
      }
      
    } else {
      ps.print('{');
      printFields( ei, ci.getDeclaredStaticFields(), filterMask);        
      ps.print('}');
    }
  }
  
  public void print (ThreadInfo ti){
    PrintUtils.printReference(ps, ti.getThreadObjectRef());
    ps.print(' ');
    ps.print(ti.getStateDescription());
  }
  
  public void print (StackFrame frame){
    MethodInfo mi = frame.getMethodInfo();
  
    ps.print('@');
    ps.print(frame.getDepth());
    
    ps.print(" frame ");
    ps.print( mi.getFullName());
    ps.print( ":{" );
    
    if (!mi.isStatic()){
      ps.print("this:");
      PrintUtils.printReference(ps, frame.getThis());
      ps.print(',');
    }
    
    ps.print("pc:");
    ps.print(frame.getPC().getInstructionIndex());
    
    ps.print(",slots:[");
    frame.printSlots(ps);
    ps.print(']');
    
    ps.print('}');
  }
  
  public void println(){
    ps.println();
  }
  
  public void print (NativeStateHolder nsh){
    ps.print(nsh);
    ps.print(":");
    ps.print(nsh.getHash());
  }
  
  @Override
  public void write(int b) throws IOException {
    ps.write(b);
  }
}
