package java8;
import org.junit.Test;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import gov.nasa.jpf.util.test.TestJPF;

public class LocalDateTest {
    private LocalDate today = LocalDate.parse("2014-02-27");

    @Test
    public void test_add_to_date() {
        LocalDate oneMonthFromNow = today.plusDays(30);
        assertTrue(oneMonthFromNow.isEqual(LocalDate.parse("2014-03-29")));
        LocalDate nextMonth = today.plusMonths(1);
        assertTrue(nextMonth.isEqual(LocalDate.parse("2014-03-27")));
        LocalDate future = today.plus(4, ChronoUnit.WEEKS);
        assertTrue(future.isEqual(LocalDate.parse("2014-03-27")));
    }

    @Test
    public void  test_create_date() {
        LocalDate bday = LocalDate.of(2014,3,18);
        assertThat(bday.toString(),is("2014-03-18"));
    }

    @Test
    public void test_subtract_from_date() {
        assertThat(today.minusWeeks(1).toString(), is("2014-02-20"));
        assertThat(today.minusMonths(2).toString(), is("2013-12-27"));
        assertThat(today.minusYears(4).toString(), is("2010-02-27"));
        Period twoMonths = Period.ofMonths(2);
        assertThat(today.minus(twoMonths).toString(), is("2013-12-27"));

    }

    @Test
    public void test_get_date_parts() {
        assertThat(today.getDayOfWeek().toString(), is("THURSDAY"));
        assertThat(today.getMonth().toString(), is("FEBRUARY"));
    }

    @Test
    public void test_get_days_between_dates() {
        LocalDate vacationStart = LocalDate.parse("2014-07-04");
        Period timeUntilVacation = today.until(vacationStart);
        assertThat(timeUntilVacation.getMonths(), is(4));
        assertThat(timeUntilVacation.getDays(), is(7));
        assertThat(today.until(vacationStart, ChronoUnit.DAYS), is(127L));
        LocalDate libraryBookDue = LocalDate.parse("2000-03-18");
        assertThat(today.until(libraryBookDue).isNegative(), is(true));
        assertThat(today.until(libraryBookDue, ChronoUnit.DAYS), is(-5094L));
        LocalDate christmas = LocalDate.parse("2014-12-25");
        assertThat(today.until(christmas, ChronoUnit.DAYS), is(301L));
    }
}