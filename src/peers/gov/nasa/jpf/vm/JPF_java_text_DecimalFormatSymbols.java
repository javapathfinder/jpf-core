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

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.text.DecimalFormatSymbols;

/**
 * MJI NativePeer class for java.text.DecimalFormatSymbols library abstraction
 * 
 * we need to intercept the initialization because it is requires
 * file io (properties) based on the Locale
 */
public class JPF_java_text_DecimalFormatSymbols extends NativePeer {
  @MJI
  public void initialize__Ljava_util_Locale_2__V (MJIEnv env, int objRef, int localeRef) {
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    
    env.setCharField(objRef,"patternSeparator", dfs.getPatternSeparator());
    env.setCharField(objRef,"percent", dfs.getPercent());
    env.setCharField(objRef,"digit", dfs.getDigit());
    env.setCharField(objRef,"minusSign", dfs.getMinusSign());
    env.setCharField(objRef,"perMill", dfs.getPerMill());
    env.setReferenceField(objRef,"infinity", env.newString(dfs.getInfinity()));
    env.setReferenceField(objRef,"NaN", env.newString(dfs.getNaN()));
    env.setReferenceField(objRef,"currencySymbol", env.newString(dfs.getCurrencySymbol()));
    env.setCharField(objRef,"monetarySeparator", dfs.getMonetaryDecimalSeparator());

    env.setCharField(objRef,"decimalSeparator", dfs.getDecimalSeparator());
    env.setCharField(objRef,"groupingSeparator", dfs.getGroupingSeparator());
    env.setCharField(objRef,"exponential", 'E'); // getExponentialSymbol() is not public
  }
}

