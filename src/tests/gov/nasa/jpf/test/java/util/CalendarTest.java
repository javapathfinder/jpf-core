package gov.nasa.jpf.test.java.util;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Assert;
import org.junit.Test;
import java.util.Calendar;

public class CalendarTest extends TestJPF {

    @Test
    public void testCalendarHourSet(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        Assert.assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testCalendarHourSetAfterYear(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        Assert.assertEquals(2014, cal.get(Calendar.YEAR));
        Assert.assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
    }

}
