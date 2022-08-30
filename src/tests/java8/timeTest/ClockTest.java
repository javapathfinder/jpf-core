package java8.timeTest;
import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ClockTest {

     private Function<String,ZoneId> withZoneId= (timeZoneId -> ZoneId.of(ZoneId.SHORT_IDS.get(timeZoneId)));

    @Test
    public void fixed_clock_test() throws Exception{
        Instant fixedInstant  = Instant.parse("2014-03-06T21:27:31Z");
        Clock fixedClock = Clock.fixed(fixedInstant, withZoneId.apply("EST"));

        assertThat(fixedClock.instant(),is(fixedInstant));

    }

    @Test
    public void switch_clock_time_zone_test() throws Exception {
        ZoneId eastCoastZone = ZoneId.of("America/New_York");

        ZoneId westCoastZone = ZoneId.of("America/Los_Angeles");
        ZonedDateTime eastCoastTime = ZonedDateTime.now(eastCoastZone);
        ZonedDateTime westCoastTime = eastCoastTime.withZoneSameLocal(westCoastZone);
        Duration timeDifference = Duration.between(eastCoastTime,westCoastTime);
        assertThat(timeDifference.toHours(),is(3L));

    }

    @Test
    public void  change_clock_duration_test() throws Exception {
        Clock eastCoastStandardTime = Clock.system(withZoneId.apply("EST"));
        Clock eastCoastDaylightSavingsTime = Clock.offset(eastCoastStandardTime, Duration.of(1,ChronoUnit.HOURS));

        LocalTime est = LocalTime.now(eastCoastStandardTime);
        LocalTime edt = LocalTime.now(eastCoastDaylightSavingsTime);

        long timeDifference = edt.until(est,ChronoUnit.HOURS);
        assertThat(timeDifference,is(-1L));
    }

    @Test
    public void tick_minute_clock_test() throws Exception {
        Clock clock = Clock.tickMinutes(withZoneId.apply("EST"));
        LocalTime lt = LocalTime.now(clock);
        LocalTime lt2 = LocalTime.now(clock);
        long timeDifference = lt.until(lt2,ChronoUnit.SECONDS);
    
        assertThat(timeDifference,is(0L));
    }
}
