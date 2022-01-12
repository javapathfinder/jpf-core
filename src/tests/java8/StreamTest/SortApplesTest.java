public class SortApplesTest {
    import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SortApplesTest {
    private List<Apple> apples = new ArrayList<Apple>();

    @Before
    public void setUp() {
        // make sure we always have an empty list for future tests
        apples.clear();

        // add some colorful apples
        apples.add(new Apple("green"));
        apples.add(new Apple("green"));
        apples.add(new Apple("red"));
        apples.add(new Apple("yellow"));
    }

    @Test
    public void sortGreenApples() {
        // get only green apples from the list, using a Lambda!
        // also, using Collectors.toList() to get all the results into a List at the end
        final List<Apple> greenApples = apples.stream().filter(a -> a.color.equals("green")).collect(Collectors.toList());
        assertEquals(greenApples.size(), 2);
    }
}
}
