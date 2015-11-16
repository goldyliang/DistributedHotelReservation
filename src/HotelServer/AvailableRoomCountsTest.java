package HotelServer;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import miscutil.SimpleDate;

import org.junit.Test;

public class AvailableRoomCountsTest {

	//AvailableRoomCounts cnt; // = new AvailableRoomCounts(5, 30);

	SimpleDate today = new SimpleDate();// = new Date();
	
	
	void verifyCnts (AvailableRoomCounts cnt, int d1, int d2, int expected) {
		for (int i=d1;i<=d2;i++)
			assertEquals (expected, cnt.query(
			        SimpleDate.getDateByDelta(today,i), 
			        SimpleDate.getDateByDelta(today,i+1)));

	}
	
	void verifyLen (AvailableRoomCounts cnt, int len) {
	    SimpleDate end = SimpleDate.getDateByDelta (today, len-1); //new Date ( today.getTime() + (long)30 * 24 * 3600* 1000);
		assertEquals (end, cnt.getBookingEndDate());
	}
	
	@Test
	public void testDate() {
	    
       SimpleDate d1;
       SimpleDate d2;
       
        try {
            d1 = SimpleDate.parse("2015/12/5");
            d2 = SimpleDate.parse("2015/12/10");
            assertEquals (5, SimpleDate.dateDiff(d1,d2));
            
            d1 = SimpleDate.parse("2015/10/29");
            d2 = SimpleDate.parse("2015/11/2");
            assertEquals (4, SimpleDate.dateDiff(d1,d2));
            
          //  assertEquals (4, AvailableRoomCounts.dateDiff1(d1,d2));
            
            d1 = SimpleDate.parse("2015/3/1");
            d2 = SimpleDate.parse("2015/3/10");
            assertEquals (9, SimpleDate.dateDiff(d1,d2));
         //   assertEquals (8, AvailableRoomCounts.dateDiff1(d1,d2));
            
            d1 = SimpleDate.parse("2015/10/29");
            d2 = SimpleDate.parse("2016/1/7");
            assertEquals (70, SimpleDate.dateDiff(d1,d2));
      //      assertEquals (-70, AvailableRoomCounts.dateDiff(d2,d1));
            
            d1 = SimpleDate.parse("2015/10/29");
            d2 = SimpleDate.parse("2016/3/17");
            // error here, not going to fix in this program
            assertEquals (140, SimpleDate.dateDiff(d1,d2));



        } catch (ParseException e) {
            e.printStackTrace();
        } // set to today
	    
	}
	
	@Test
	public void singleThreadTest() {
	
		AvailableRoomCounts cnt = new AvailableRoomCounts(5, 30);
		
		// Verify initial status
		verifyLen (cnt, 30);
		verifyCnts (cnt, 0,29, 5);
		
		// verify an query across days
		int n = cnt.query(today, SimpleDate.getDateByDelta(today,7));
		assertEquals (5, n);
		verifyLen (cnt, 30);
		
		// verify an query resulting increase size of the counts
		n = cnt.query(today, SimpleDate.getDateByDelta(today,35));
		assertEquals(5,n);
		verifyLen (cnt, 35);
		
		// verify decrement
		assertTrue ("Should be true", cnt.decrementDays(today, SimpleDate.getDateByDelta(today, 5)));
		assertTrue (cnt.decrementDays(SimpleDate.getDateByDelta(today,3), SimpleDate.getDateByDelta(today, 7)));
		assertTrue (cnt.decrementDays(SimpleDate.getDateByDelta(today,7), SimpleDate.getDateByDelta(today, 8)));
		assertTrue (cnt.decrementDays(SimpleDate.getDateByDelta(today,32), SimpleDate.getDateByDelta(today, 35)));
		
		verifyCnts(cnt, 0,2,4);
		verifyCnts(cnt, 3,4,3);
		verifyCnts(cnt, 5,7,4);
		verifyCnts(cnt, 8,31,5);
		verifyCnts(cnt, 32,34,4);
		verifyLen(cnt, 35);
		
		// decrement with extended days
		assertTrue (cnt.decrementDays(SimpleDate.getDateByDelta(today,33), SimpleDate.getDateByDelta(today,37)));
		verifyCnts (cnt, 33,34,3);
		verifyCnts (cnt, 32,32,4);
		verifyCnts (cnt, 35,36,4);
		verifyCnts (cnt, 37,37,5);
		verifyLen (cnt, 38);
		
		// query with different number in the day range
		n = cnt.query(SimpleDate.getDateByDelta(today, 2), SimpleDate.getDateByDelta(today, 6));
		assertEquals (3, n);
		
		// increment test
		assertTrue (cnt.incrementDays(SimpleDate.getDateByDelta(today,4), SimpleDate.getDateByDelta(today,8)));
		verifyCnts (cnt, 0,2,4);
		verifyCnts (cnt, 3,3,3);
		verifyCnts (cnt, 4,4,4);
		verifyCnts (cnt, 5,7,5);
		verifyCnts (cnt, 8,31,5);
		
		//increment test with invalid status
		assertFalse (cnt.incrementDays(SimpleDate.getDateByDelta(today,4), SimpleDate.getDateByDelta(today,6)));
		verifyCnts (cnt, 0,2,4);
		verifyCnts (cnt, 3,3,3);
		verifyCnts (cnt, 4,4,4);
		verifyCnts (cnt, 5,7,5);
		verifyCnts (cnt, 8,31,5);
		
		//query with no room
		assertTrue (cnt.decrementDays(SimpleDate.getDateByDelta(today,3), SimpleDate.getDateByDelta(today,4)));
		assertTrue (cnt.decrementDays(SimpleDate.getDateByDelta(today,3), SimpleDate.getDateByDelta(today,4)));
		assertTrue (cnt.decrementDays(SimpleDate.getDateByDelta(today,3), SimpleDate.getDateByDelta(today,4)));

		assertFalse (cnt.decrementDays(SimpleDate.getDateByDelta(today,0), SimpleDate.getDateByDelta(today,5)));
		verifyCnts (cnt, 0,2,4);
		verifyCnts (cnt, 3,3,0);
		verifyCnts (cnt, 4,4,4);
		verifyCnts (cnt, 5,7,5);
		verifyCnts (cnt, 8,31,5);
	}
	
	static void assertConcurrent(
		final String message, 
		final List<? extends Runnable> runnables, 
		final int maxTimeoutSeconds) throws InterruptedException {
	
		    final int numThreads = runnables.size();
		    final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
		    final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		    try {
		        final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
		        final CountDownLatch afterInitBlocker = new CountDownLatch(1);
		        final CountDownLatch allDone = new CountDownLatch(numThreads);
		        for (final Runnable submittedTestRunnable : runnables) {
		            threadPool.submit(new Runnable() {
		                public void run() {
		                    allExecutorThreadsReady.countDown();
		                    try {
		                        afterInitBlocker.await();
		                        submittedTestRunnable.run();
		                    } catch (final Throwable e) {
		                        exceptions.add(e);
		                    } finally {
		                        allDone.countDown();
		                    }
		                }
		            });
		        }
		        // wait until all threads are ready
		        assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent", allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
		        // start all test runners
		        afterInitBlocker.countDown();
		        assertTrue(message +" timeout! More than" + maxTimeoutSeconds + "seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
		    } finally {
		        threadPool.shutdownNow();
		    }
		    
		    if (!exceptions.isEmpty()) {
		    	for (Throwable e:exceptions) {
		    		e.printStackTrace();
		    	}
		    }
		    
		    assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
	}
	
	//@Test
	public void repeatMultiThreadTest() {
		int times=10;
		
		while (times-->0)
			multiThreadTest();
	}
	
	@Test
	public void multiThreadTest() {
		
		List<Runnable> runnables= new ArrayList<Runnable> ();
		
		final int initial_days = 30;
		final AvailableRoomCounts cnt = new AvailableRoomCounts(5,initial_days);
		
		// Definition of concurrent booking requests
		// [thread][request num][increment (1) / decement (-1), in, out, return_bool]
		
/*      // Test #1: manual data targeting the possible contention of counter inconsistency
		final int focus_days_end = 40; 

		final int [][][] booking= {

		{ {-1,3,5,0}, {-1,2,4,0}, {-1,2,6,0}, {1,3,6,0} } ,
		{ {-1,2,4,0}, {-1,3,5,0}, {-1,2,5,0}, {1,4,7,0} } ,
		{ {-1,2,4,0}, {-1,3,5,0}, {-1,2,5,0}, {1,4,7,0} } ,
		{ {-1,2,4,0}, {-1,3,5,0}, {-1,2,5,0}, {1,4,7,0} } ,
		{ {-1,2,4,0}, {-1,3,5,0}, {-1,2,5,0}, {1,4,7,0} } 
		}; /* */
		
/*      // Test #2: manual data targeting possible contention of structure change
		final int focus_days_end = 40;   // change to 40 to test size increase interaction

		final int [][][] booking= {
			{ {-1,3,5,0}, {-1,27,35,0}, {-1,34,35,0} } ,
			{ {-1,2,4,0}, {-1,28,33,0}, {-1,32,36,0} } ,
		}; /* */
		

		// Test #3: random data targeting unforseen contentions
        final int num_threads=6;

		final int num_oprs = 1000;
		final int focus_days_start = 28; // change to 28 to test size increase interaction
		final int focus_days_end = 35;   // change to 40 to test size increase interaction
		final int inc_possiblity_factor = 5;
		final int [][][] booking = new int [num_threads][num_oprs][4];
		
		Random rand = new Random();
		for (int i=0;i<num_threads;i++)
			for (int j=0;j<num_oprs;j++)
			{
				int r = rand.nextInt(inc_possiblity_factor);
				if (r==0 && j>0) // 1 out of inc_possiblity_factor chance of increase (cancel booking)
				{
					booking[i][j][0] = 1;
					booking[i][j][1] = -1;
					booking[i][j][2] = -1;
					booking[i][j][3] = 0;
				} else
				{
					// decrease (booking)
					booking[i][j][0] = -1;
					int inDate = focus_days_start + rand.nextInt(focus_days_end - focus_days_start); 
					int outDate = rand.nextInt(focus_days_end-inDate) + inDate + 1;
					booking[i][j][1] = inDate;
					booking[i][j][2] = outDate;
					booking[i][j][3] = 0; 
				}
			}  /* */
		
		class BookingThread extends Thread {
			public BookingThread (int thread_num) {
				this.thread_num = thread_num;
			}
			
			int thread_num;
			ArrayList <int[]> dec_history = new ArrayList<int[]>();
			Random rand = new Random();
			
			public void run() {
				for (int i=0; i<booking[thread_num].length; i++) {
					
					boolean r;
					
					int inDate = booking[thread_num][i][1];
					int outDate = booking[thread_num][i][2];
					int opr = booking[thread_num][i][0];
					
					if (opr==1) { //increment
						if (inDate<0) {
							if (dec_history.size()>0) {
								// look for a random success decr history
								int h = rand.nextInt(dec_history.size());
								inDate = dec_history.get(h)[0];
								outDate = dec_history.get(h)[1];
								booking[thread_num][i][1] = inDate;
								booking[thread_num][i][2] = outDate;
								dec_history.remove(h);
								
								r=cnt.incrementDays(SimpleDate.getDateByDelta(today,inDate), 
							            SimpleDate.getDateByDelta(today,outDate));
							} else
								r=false;
						} else {
							//use the date range from the bookign table
							r=cnt.incrementDays(SimpleDate.getDateByDelta(today,inDate), 
						            SimpleDate.getDateByDelta(today,outDate));
						}
						
					}
					else { //decrement
						r=cnt.decrementDays(SimpleDate.getDateByDelta(today,inDate), 
					            SimpleDate.getDateByDelta(today,outDate));
						
						if (r)
							dec_history.add(new int[] {inDate,outDate});
					}
					
					booking[thread_num][i][3] = (r?1:0);
				}
			}
		};
		
		// Threads simulating booking operation
		for (int i=0;i<booking.length;i++)
			runnables.add ( new BookingThread(i));		
		
		try {
			assertConcurrent( "Concurrent room count operation",
					runnables, 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Verify final status
		int len = cnt.getSize();
		int[] check = new int[len]; //(focus_days_end>initial_days?focus_days_end:initial_days)];
		for (int i=0;i<len;i++) {check[i]=5;}
		
		for (int i=0;i<booking.length;i++)
			for (int j=0;j<booking[i].length;j++)
				if (booking[i][j][3]==1) //success indication
				{
					int opr = booking[i][j][0];
					int inDate = booking[i][j][1];
					int outDate = booking[i][j][2];
					
					for (int k=inDate;k<outDate;k++)
						if (opr==1) // increase
							check[k]++;
						else
							check[k]--;
				}
		
		//System.out.println ("End size of counts:" + cnt.availCounts.size());

		
		System.out.println(Arrays.deepToString(booking));
		
		int i=0;
/*		for (int[] n:cnt.availCounts) {
			System.out.print ("#" + (i++) + ":");
			System.out.println (n[0]);
		} */
		
		SimpleDate dt = new SimpleDate();
		for (i=0;i<len;i++)  { 
			assertEquals ("Date #"+i, check[i], 
			        cnt.getCount(dt));
			dt.nextDay();
		}
		
	//	1System.out.println (cnt.availCounts);
	}
	

}
