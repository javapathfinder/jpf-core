package java8;
import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LocalDateTimeTest {

    @Test
    public void test_add_minutes() {
        LocalDateTime localDateTime = LocalDateTime.parse("2003-11-18T10:40:00");
        LocalDateTime adjustedDateTime = localDateTime.plusMinutes(30);
        assertThat(adjustedDateTime.toString(), is("2003-11-18T11:10"));
    }

    @Test
    public void test_truncate_minutes_seconds() {
        LocalDateTime localDateTime = LocalDateTime.parse("2009-06-01T11:35:17");
        LocalDateTime truncatedMinutes = localDateTime.truncatedTo(ChronoUnit.HOURS);
        assertThat(truncatedMinutes.toString(), is("2009-06-01T11:00"));
    }

    @Test
    public void test_adjust_local_date_time_zone() {
        LocalDateTime eastCoastTime = LocalDateTime.now(Clock.system(OffsetTime.now().getOffset()));
        LocalDateTime centralTime = LocalDateTime.now(Clock.system(ZoneId.of("America/Chicago")));
        LocalDateTime pacificTime = LocalDateTime.now(Clock.system(ZoneId.of("America/Los_Angeles")));
        assertThat(eastCoastTime.get(ChronoField.CLOCK_HOUR_OF_DAY) - pacificTime.get(ChronoField.CLOCK_HOUR_OF_DAY),is(3));
        assertThat(eastCoastTime.get(ChronoField.CLOCK_HOUR_OF_DAY) - centralTime.get(ChronoField.CLOCK_HOUR_OF_DAY),is(1));

    }
    private Object is(String string) {
        return null;
    }

}