import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentCount {
	static final int COUNT = 30000;
	volatile static int count = COUNT;
	volatile static AtomicBoolean lock = new AtomicBoolean(false); 
	static int a = 0;
	static int b = 0;
	
	public static void main(String args[]) {
		
		new Thread() {
			
			@Override
			public void run() {
				while(count > 0) {
					if (lock.get()) continue;
					lock.set(true);
					decreaseCount();
					a++;
					lock.set(false);
					
					
				}
			}
		}.start();
		
        new Thread() {
			
			@Override
			public void run() {
				while(count > 0) {
					if (lock.get()) continue;
					lock.set(true);
					decreaseCount();
					b++;
					lock.set(false);
					
					
				}
			}
		}.start();
		
		while(count > 0);
		//System.out.println("a = " + a);
		//System.out.println("b = " + b);
		//System.out.println("a + b = " + (a + b));
		//System.out.println("count = " + count);

		//assert a + b == COUNT;
	}
	
	private static synchronized void decreaseCount() {
		count--;
	}

}

