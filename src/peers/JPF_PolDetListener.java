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
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.serialize.PolDetSerializer;
import static gov.nasa.jpf.vm.serialize.PolDetSerializer.PolDetPhase;
import java.util.Arrays;


/**
 * Peer class for PolDetListener, implements methods to get pre-state and post-state for a JUnit test
 *
 * @author Pu Yi
 */
public class JPF_PolDetListener extends NativePeer {

  static int[] preState;
  static PolDetSerializer serializer = new PolDetSerializer();

  @MJI
  public static void capturePreState____V (MJIEnv env, int classRef) {
    serializer.attach(env.getVM());
    preState = serializer.getState(PolDetPhase.PRESTATE);
  }

  @MJI
  public static boolean compareStates____Z (MJIEnv env, int classRef) {
    serializer.attach(env.getVM());
    int[] postState = serializer.getState(PolDetPhase.POSTSTATE);
    return Arrays.equals(preState, postState);
  }
}
