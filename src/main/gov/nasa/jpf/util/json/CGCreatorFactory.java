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

package gov.nasa.jpf.util.json;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.BooleanChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.choice.DoubleChoiceFromList;
import gov.nasa.jpf.vm.choice.DoubleThresholdGenerator;
import gov.nasa.jpf.vm.choice.IntChoiceFromSet;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;
import gov.nasa.jpf.vm.choice.RandomIntIntervalGenerator;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * Singleton factory for creating CGCreators.
 * @author Ivan Mushketik
 */
public class CGCreatorFactory {

  private static CGCreatorFactory factory;

  ClassLoader loader = CGCreatorFactory.class.getClassLoader();

  // Hash where key is a name that user can use in JSON document to set a
  // ChoiceGenerator and value is creator of ChoiceGenerator that uses Values[]
  // from JSON to creat CG
  private HashMap<String, CGCreator> cgTable = new HashMap<String, CGCreator>() {{
    put("TrueFalse", new BoolCGCreator());
    put("IntSet", new IntFromSetCGCreator());
    put("IntInterval", new IntIntervalCGCreator());
    put("DoubleSet", new DoubleFromSetCGCreator());
    put("DoubleThreshold", new DoubleThresholdGeneratorCGCreator());
    put("RandomIntInerval", new RandomIntIntervalGeneratorCGCreator());
  }};

  private CGCreatorFactory() {
    Config config = VM.getVM().getConfig();
    String[] cgCreators = config.getStringArray("cg-creators");

    // If user specified names for additional CG creators, lets add them
    if (cgCreators != null) {
      for (String creator : cgCreators) {
        String[] nameClassPair = creator.split(":");
        String cgName = nameClassPair[0];
        String cgCreatorClassName = nameClassPair[1];

        CGCreator cgCreator = createCGCreator(cgCreatorClassName);

        addCGCreator(cgName, cgCreator);
      }
    }
  }

  public static CGCreatorFactory getFactory() {
    if (factory == null) {
      factory = new CGCreatorFactory();
    }

    return factory;
  }

  public CGCreator getCGCreator(String key) {
    return cgTable.get(key);
  }

  public void addCGCreator(String cgName, CGCreator cgCreator) {
    if (cgTable.containsKey(cgName)) {
      throw new JPFException("CGCreator with name '" + cgName + "' already exists");
    }

    cgTable.put(cgName, cgCreator);
  }

  private CGCreator createCGCreator(String cgCreatorClassName) {
    try {
      Class cgCreatorClass = loader.loadClass(cgCreatorClassName);
      // We search for a constructor with no parameters
      Constructor ctor = cgCreatorClass.getDeclaredConstructor();
      ctor.setAccessible(true);
      return (CGCreator) ctor.newInstance();
    } catch (Exception ex) {
      throw new JPFException(ex);
    }

  }
}

/**
 * CGCreator that creates instance of BooleanChoiceGenerator
 */
class BoolCGCreator implements CGCreator {

  @Override
  public ChoiceGenerator<?> createCG(String id, Value[] params) {
    return new BooleanChoiceGenerator(id);
  }
}


/**
 * CGCreator that creates IntChoiceFromSet CG 
 */
class IntFromSetCGCreator implements CGCreator {

  // <2do> add support from ctor with no params
  @Override
  public ChoiceGenerator<?> createCG(String id, Value[] params) {
    int[] intSet = new int[params.length];

    for (int i = 0; i < intSet.length; i++) {
      intSet[i] = params[i].getDouble().intValue();
    }

    return new IntChoiceFromSet(id, intSet);
  }
}

class IntIntervalCGCreator implements CGCreator {

  @Override
  public ChoiceGenerator<?> createCG(String id, Value[] params) {
    int min = params[0].getDouble().intValue();
    int max = params[1].getDouble().intValue();
    if (params.length == 2) {
      return new IntIntervalGenerator(id, min, max);
    } else if (params.length == 3) {
      int delta = params[2].getDouble().intValue();
      return new IntIntervalGenerator(id, min, max, delta);
    }

    throw new JPFException("Can't create IntIntevalChoiceGenerator with id " + id);
  }
}

class DoubleFromSetCGCreator implements CGCreator {

  @Override
  public ChoiceGenerator<?> createCG(String id, Value[] params) {
    double[] doubleSet = new double[params.length];

    for (int i = 0; i < doubleSet.length; i++) {
      doubleSet[i] = params[i].getDouble().doubleValue();
    }

    return new DoubleChoiceFromList(id, doubleSet);
  }

}

class DoubleThresholdGeneratorCGCreator implements CGCreator {

  @Override
  public ChoiceGenerator<?> createCG(String id, Value[] params) {
    if (params.length != 0) {
      throw new JPFException("Double threshold generator requires empty parameters list");
    }
    Config config = VM.getVM().getConfig();
    return new DoubleThresholdGenerator(config, id);
  }

}

class RandomIntIntervalGeneratorCGCreator implements CGCreator {

  @Override
  public ChoiceGenerator<?> createCG(String id, Value[] params) {
    int min = params[0].getDouble().intValue();
    int max = params[1].getDouble().intValue();
    int nChoices = params[2].getDouble().intValue();

    if (params.length == 3) {
      return new RandomIntIntervalGenerator(id, min, max, nChoices);
    } else if (params.length == 4) {
      long seed = params[3].getDouble().longValue();

      return new RandomIntIntervalGenerator(id, min, max, nChoices, seed);
    }

    throw new JPFException("Unexpected length of parameters list " + params.length);
  }
  
}

