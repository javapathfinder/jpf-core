/*
 * Copyright (C) 2021 Pu Yi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
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
