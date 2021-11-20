import java.sql.Date;
import java.text.SimpleDateFormat;

class threadSafeFormatter{
    public static ThreadLocal<SimpleDateFormat> df = ThreadLocal.withInitial(() 
        -> new SimpleDateFormat("yyyy-MM-dd"));
}

public class App  {
    public static void main(String[] args) throws Exception {
        threadSafeFormatter tf = new threadSafeFormatter();
        Thread t1 = new Thread();
        t1.start();
    }

    public static String birthDate(int userId){
        Date birthdDate = new Date(userId);
        final SimpleDateFormat df = threadSafeFormatter.df.get();
        return df.format(birthdDate);
    }
}
