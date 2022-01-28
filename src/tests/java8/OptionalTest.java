package java8;
import bbejeck.collector.CustomCollectors;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import gov.nasa.jpf.util.test.TestJPF;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class OptionalTest {

    private List<String> listWithNulls = Arrays.asList("foo", null, "bar", "baz", null);
    private List<Optional<String>> optionals;

    @Before
    public void setUp() {
        optionals = listWithNulls.stream().map(Optional::ofNullable).collect(Collectors.toList());
    }
    @Test
    public void map_with_nulls_test() {
        List<Optional<String>> upperCased = optionals.stream().map(s -> s.map(String::toUpperCase)).collect(Collectors.toList());
        List<Optional<String>> expectedValues = Arrays.asList(Optional.of("FOO"), Optional.empty(), Optional.of("BAR"), Optional.of("BAZ"), Optional.empty());
        assertThat(upperCased, is(expectedValues));
    }
    @Test
    public void collect_optional_values_test(){

        List<String> upperCasedWords = optionals.stream().map(o -> o.map(String::toUpperCase)).collect(CustomCollectors.optionalToList());
        List<String> expectedWords = Arrays.asList("FOO","BAR","BAZ");

        assertThat(upperCasedWords,is(expectedWords));
    }
    @Test
    public void collect_optional_with_default_values_test(){
        String defaultValue = "MISSING";
        List<String> upperCasedWords = optionals.stream().map(o -> o.map(String::toUpperCase)).collect(CustomCollectors.optionalToList(defaultValue));
        List<String> expectedWords = Arrays.asList("FOO", defaultValue, "BAR", "BAZ", defaultValue);

        assertThat(upperCasedWords,is(expectedWords));
    }
    @Test
    (expected = NullPointerException.class)
    public void optional_of_with_null_test() {
        Optional<String> option = Optional.of(null);
        assertThat(option.isPresent(), is(false));
    }
    @Test
    public void optional_of_nullable_with_null_test() {
        Optional<String> option = Optional.ofNullable(null);
        assertThat(option.isPresent(), is(false));
    }
    @Test
    public void optional_filter_test() {
        Optional<Integer> numberOptional = Optional.of(10);
        Optional<Integer> filteredOut = numberOptional.filter(n -> n > 100);
        Optional<Integer> notFiltered = numberOptional.filter(n -> n < 100);

        assertThat(filteredOut.isPresent(), is(false));
        assertThat(notFiltered.isPresent(), is(true));
    }
    @Test
    public void optional_map_test() {
        Optional<Integer> number = Optional.of(300);
        Optional<Integer> noNumber = Optional.empty();
        Function<Integer, Integer> divideByOneHundred = n -> n / 100;

        Optional<Integer> smallerNumber = number.map(divideByOneHundred);
        Optional<Integer> nothing = noNumber.map(divideByOneHundred);

        assertThat(smallerNumber.get(), is(3));
        assertThat(nothing.isPresent(), is(false));
    }

    @Test
    public void optional_map_substring_test() {
        Optional<String> number = Optional.of("longword");
        Optional<String> noNumber = Optional.empty();

        Optional<String> smallerWord = number.map(s -> s.substring(0,4));
        Optional<String> nothing = noNumber.map(s -> s.substring(0,4));

        assertThat(smallerWord.get(), is("long"));
        assertThat(nothing.isPresent(), is(false));
    }

    @Test
    public void optional_is_present_add_to_list_without_get_test() {
        List<String> words = Lists.newArrayList();

        Optional<String> month = Optional.of("October");
        Optional<String> nothing = Optional.ofNullable(null);

        month.ifPresent(words::add);
        nothing.ifPresent(words::add);

        assertThat(words.size(), is(1));
        assertThat(words.get(0), is("October"));
    }

    @Test
    public void optional_flat_map_test() {
        Function<String, Optional<String>> upperCaseOptionalString = s -> (s == null) ? Optional.empty() : Optional.of(s.toUpperCase());

        Optional<String> word = Optional.of("apple");
        Optional<Optional<String>> optionalOfOptional = word.map(upperCaseOptionalString);
        Optional<String> upperCasedOptional = word.flatMap(upperCaseOptionalString);

        assertThat(optionalOfOptional.get().get(), is("APPLE"));
        assertThat(upperCasedOptional.get(), is("APPLE"));

    }

    @Test
    public void optional_or_else_and_or_else_get_test() {
        String defaultValue = "DEFAULT";

        Supplier<TestObject> testObjectSupplier = () -> {
            String name = "name";
            String category = "justCreated";
            return new TestObject(name, category, new Date());
        };

        Optional<String> emptyOptional = Optional.empty();
        Optional<TestObject> emptyTestObject = Optional.empty();
        assertThat(emptyOptional.orElse(defaultValue), is(defaultValue));
        TestObject testObject = emptyTestObject.orElseGet(testObjectSupplier);
        assertNotNull(testObject);
        assertThat(testObject.category, is("justCreated"));
    }

    @Test
    (expected = IllegalStateException.class)
    public void optional_or_else_throw_test() {
        Optional<String> shouldNotBeEmpty = Optional.empty();
        shouldNotBeEmpty.orElseThrow(() -> new IllegalStateException("This should not happen!!!"));
    }

    private static class TestObject {
        String name;
        String category;
        Date createdAt;

        public TestObject(String name, String category, Date createdAt) {
            this.name = name;
            this.category = category;
            this.createdAt = createdAt;
        }
    }
}