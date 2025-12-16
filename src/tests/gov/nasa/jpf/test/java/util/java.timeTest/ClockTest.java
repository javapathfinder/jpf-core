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
package gov.nasa.jpf.test.java.util.java.timeTest;

import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ClockTest extends TestJPF {

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

    ZonedDateTime est = ZonedDateTime.now(eastCoastStandardTime);
    ZonedDateTime edt = ZonedDateTime.now(eastCoastDaylightSavingsTime);

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
