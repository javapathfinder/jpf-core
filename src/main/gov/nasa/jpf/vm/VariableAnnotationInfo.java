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
 * type annotation for local vars and resource vars
 */
public class VariableAnnotationInfo extends AbstractTypeAnnotationInfo {
  
  protected long[] scopeEntries;
  
  public VariableAnnotationInfo (AnnotationInfo base, int targetType, short[] typePath, long[] scopeEntries) {
    super( base, targetType, typePath);
    
    this.scopeEntries = scopeEntries;
  }
  
  public int getNumberOfScopeEntries(){
    return scopeEntries.length;
  }
  
  public int getStartPC (int idx){
    return (int)(scopeEntries[idx] >> 32) & 0xffff;
  }
  
  public int getLength (int idx){
    return (int)(scopeEntries[idx] >> 16) & 0xffff;
  }
  
  public int getEndPC (int idx){
    long e = scopeEntries[idx];
    
    int startPC = (int)(e >> 32) & 0xffff;
    int len = (int)(e >> 16) & 0xffff;
    
    return startPC + len;
  }
  
  public int getSlotIndex (int idx){
    return (int)scopeEntries[idx] & 0xffff;    
  }
  
  
  @Override
  protected void addArgs(StringBuilder sb){
    sb.append(",scope:");
    for (int i=0; i<scopeEntries.length;i++){
      long e = scopeEntries[i];
      int slotIndex = (int)(e & 0xffff);
      int length = (int)((e >> 16) & 0xffff);
      int startPc = (int)((e >> 32) & 0xffff);
      
      if (i>0){
        sb.append(',');
      }
      
      sb.append('[');
      sb.append( Integer.toString(startPc));
      sb.append("..");
      sb.append( Integer.toString(startPc + length-1));
      sb.append("]#");
      sb.append(slotIndex);
    }
  }
  
  // 2do - perhaps we should map to LocalVarInfos here (in case we have them), but
  // this would probably belong to LocalVarInfo (turning them into full InfoObjects)
}