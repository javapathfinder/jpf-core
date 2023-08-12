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
package gov.nasa.jpf.test.java.util;

import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FunctionsTest extends TestJPF {

  @Test
  public void test_and_then_function() throws Exception {

    UnaryOperator<Integer> f1 = i -> i + 1;
    UnaryOperator<Integer> f2 = (i -> i + 2);
    UnaryOperator<Integer> f3 = (i -> i + 3);
    UnaryOperator<Integer> f4 = (i -> i * 10);
    Function<Integer, Integer> f = f1.andThen(f2).andThen(f3).andThen(f4);
    assertThat(f.apply(1), is(70));
  }

  @Test
  public void test_before_function() throws Exception {
    UnaryOperator<Integer> cubed = x -> x * x * x;
    Function<Integer, Integer> divideByTwoThenCube = cubed.compose(x -> x / 2);
    assertThat(divideByTwoThenCube.apply(8), is(64));
  }

  @Test
  public void test_before_then_compose() throws Exception {
    UnaryOperator<String> lowercase = String::toLowerCase;
    UnaryOperator<String> replaceVowel = s -> s.replaceAll("[aeiou]", "*");
    UnaryOperator<String> trim = String::trim;
    Function<String, String> formatString = lowercase.compose(trim).andThen(replaceVowel);
    String msg = " HELLO WORLD ";
    assertThat(formatString.apply(msg), is("h*ll* w*rld"));
  }

  @Test
  public void test_partially_applied_functions() throws Exception {
    BiFunction<Integer, Integer, Boolean> modN = (n, x) -> (x % n) == 0;
    Function<Integer,Boolean> modFive = i -> modN.apply(5,i);
    assertThat(modFive.apply(25),is(true));
  }
}
