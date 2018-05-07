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

package java.text;

/**
 * (incomplete) model class for java.text.Format
 * the reason we model this is that we want to cut off all the inner
 * workings by just delegating to real formatters stored in our
 * native peer
 */
public abstract class Format {
  
  // <?> how does that work with backtracking? my initial guess is that
  // we can safely overwrite an index because after backtracking, the
  // formatter will never be used. It's therefore sufficient if we keep the
  // nInstances counter in the JPF space
  // (just a reminder - we can't use the reference value because it might
  // change -- the ElementInfo invariance sucks!
  static int nInstances;
  private int id = nInstances++; // just for peer implementation purposes 
  
  public String format (Object o) {
    StringBuffer sb = new StringBuffer();
    FieldPosition pos = new FieldPosition(0);
    return format(o, sb, pos).toString();
  }
  
  public abstract StringBuffer format (Object o, StringBuffer sb, FieldPosition pos);

  public abstract Object parseObject (String source, ParsePosition pos);

  public Object parseObject(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Object result = parseObject(source, pos);
    if (pos.index == 0) {
      throw new ParseException("Format.parseObject(String) failed",
        pos.errorIndex);
    }
    return result;
  }
}
