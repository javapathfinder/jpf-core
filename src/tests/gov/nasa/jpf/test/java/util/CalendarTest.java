package gov.nasa.jpf.test.java.util;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.Calendar;

// To reproduce bad behavior, run these tests on JPF VM: first should pass, second should fail
// On HotSpot both tests pass
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

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(CalendarTest.class);
        System.out.println("Tests run: " + result.getRunCount());
        if (result.wasSuccessful()){
            System.out.println("Success");
        } else {
            for (Failure failure: result.getFailures()){
                System.out.println("Error: "+ failure.getMessage() + "\n  "
                        +failure.getDescription() + "\n  " + failure.getException());
            }
        }
    }

}
