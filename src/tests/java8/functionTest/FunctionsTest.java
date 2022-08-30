package java8.functionTest;
import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FunctionsTest {

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
