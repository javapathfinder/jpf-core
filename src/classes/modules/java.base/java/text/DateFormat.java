package java.text;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class DateFormat extends Format {

    static int nInstances;
    private int id = nInstances++;
    public static final int FULL = 0;
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int SHORT = 3;
    public static final int DEFAULT = SHORT;
    protected Calendar calendar;

    public DateFormat() {}

    public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
        DateFormat dateFormat = getDT(dateStyle, timeStyle, 3, locale);
        return dateFormat;
    }

    private static DateFormat getDT(int dateStyle, int timeStyle, int flags, Locale locale) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
        return getDateTimeInstance(dateStyle, timeStyle, Locale.getDefault(Locale.Category.FORMAT));
    }

    public void setLenient(boolean lenient)
    {
        if(calendar == null) {
            calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        }
        calendar.setLenient(lenient);
    }

    public void setTimeZone(TimeZone zone)
    {
        if(calendar == null) {
            calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        }
        calendar.setTimeZone(zone);
    }

    public TimeZone getTimeZone() {return calendar.getTimeZone();}

    public static Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }

    public final String format(Date date)
    {
        return format(date, new StringBuffer(),
                DontCareFieldPosition.INSTANCE).toString();
    }

    public Date parse(String source) throws ParseException
    {
        ParsePosition pos = new ParsePosition(0);
        Date result = parse(source, pos);
        if (pos.index == 0)
            throw new ParseException("Unparseable date: \"" + source + "\"" ,
                    pos.errorIndex);
        return result;
    }

    public abstract Date parse(String source, ParsePosition pos);

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return null;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return null;
    }
}