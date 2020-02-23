package gov.nasa.jpf.test.java.util;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.util.Calendar;

// To reproduce bad behavior, run these tests on JPF VM: first should pass, second should fail
// On HotSpot both tests pass
public class CalendarTest extends TestJPF {

    @Test
    public void testCalendarHourSet(){
        if (verifyNoPropertyViolation()){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        }
    }

    @Test
    public void testCalendarHourSetAfterYear(){
        if (verifyNoPropertyViolation()){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2014);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            assertEquals(2014, cal.get(Calendar.YEAR));
            assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
        }
    }

    public static void main(String[] testMethods) {
        runTestsOfThisClass(testMethods);
    }

}
