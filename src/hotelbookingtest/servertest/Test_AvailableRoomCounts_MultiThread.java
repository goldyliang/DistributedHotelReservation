package hotelbookingtest.servertest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import hotelbooking.miscutil.SimpleDate;
import hotelbooking.server.AvailableRoomCounts;

public class Test_AvailableRoomCounts_MultiThread {
	SimpleDate startDate = new SimpleDate();
	
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
	        assertTrue("Timeout initializing threads!", 
	        		allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
	        
	        // start all test runners
	        afterInitBlocker.countDown();
	        assertTrue(message +" timeout! More than" + maxTimeoutSeconds + "seconds", 
	        		allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
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
	
	enum OperationType {INCREASE, DECREASE, QUERY, DELETE_FRONT};
	
	static class Operation {
		OperationType type;
		
		SimpleDate inDate, outDate; // for INCREASE/DECREASE/QUERY
		SimpleDate startDate; // for DELETE_FRONT
		
		int queryResult;
		boolean success;
		
		static Operation IncOper (SimpleDate inDate, SimpleDate outDate) {
			Operation oper = new Operation();
			oper.type = OperationType.INCREASE;
			oper.inDate = inDate;
			oper.outDate = outDate;
			return oper;
		}
		
		static Operation DecOper (SimpleDate inDate, SimpleDate outDate) {
			Operation oper = new Operation();
			oper.type = OperationType.DECREASE;
			oper.inDate = inDate;
			oper.outDate = outDate;
			return oper;
		}
		
		static Operation DeleteFrontOper (SimpleDate startDate) {
			Operation oper = new Operation();
			oper.type = OperationType.DELETE_FRONT;
			oper.startDate = startDate;
			return oper;
		}
		
		static Operation QryOper (SimpleDate inDate, SimpleDate outDate) {
			Operation oper = new Operation();
			oper.type = OperationType.QUERY;
			oper.inDate = inDate;
			oper.outDate = outDate;
			return oper;
		}
		
		@Override
		public String toString () {
			StringBuilder builder = new StringBuilder();
			builder.append(type.toString() + ":");
			
			switch (type) {
			case INCREASE:
			case DECREASE:
				builder.append(" ");
				builder.append(inDate);
				builder.append("-");
				builder.append(outDate);
				builder.append(" Return:");
				builder.append(success);
				break;
			case QUERY:
				builder.append(" ");
				builder.append(inDate);
				builder.append("-");
				builder.append(outDate);
				builder.append(" Result:");
				builder.append(queryResult);
				break;
			case DELETE_FRONT:
				builder.append(" ");
				builder.append(startDate);
				builder.append(" Return:");
				builder.append(success);
				break;
			default:
			}
			
			return builder.toString();
		}
	}
	
	static class MultiThreadTester {
		
		final AvailableRoomCounts roomCnt;
		
		class BookingThread extends Thread {
			List <Operation> operations;
			
			int thread_num;
			ArrayList <int[]> dec_history = new ArrayList<int[]>();
			Random rand = new Random();
			
			public BookingThread (int thread_num, List<Operation> operations) {
				this.thread_num = thread_num;
				this.operations = operations;
			}

			public void run() {
				List <Operation> decreHistory = new ArrayList <Operation> ();
				Random rand = new Random();
								
				//int cnt = 0;
				
				for (Operation oper: operations) {
					
					//if ( (cnt++) % 500 ==0)
					//	System.out.println("Thread:" + thread_num + "-" + cnt);
					 
					boolean r = false;
					
					SimpleDate inDate = oper.inDate;
					SimpleDate outDate = oper.outDate;
					
					switch (oper.type) {
					case INCREASE:
						if (inDate == null) {
							if (decreHistory.size()>0) {
								// look for a random success decrease history
								int h = rand.nextInt(decreHistory.size());
								Operation operDecr = decreHistory.get(h);
								inDate = operDecr.inDate;
								outDate = operDecr.outDate;
								
								oper.inDate = operDecr.inDate;
								oper.outDate = operDecr.outDate;
								
								decreHistory.remove(h);
								
								r=roomCnt.increaseDayRange(
										oper.inDate,
										oper.outDate);
							} else
								r=false;
						} else {
							//use the date range from the booking table
							r=roomCnt.increaseDayRange(inDate, outDate);
						}
						
						break;
					case DECREASE:
						r=roomCnt.decreaseDayRange(inDate, outDate);
						if (r) {
							decreHistory.add(oper);
						}
						break;
					case QUERY:
						oper.queryResult = roomCnt.queryCount(inDate, outDate);
						r = true;
						break;
					case DELETE_FRONT:
						r = roomCnt.deleteCountersTillDate(oper.startDate);
						break;
					}
					
					oper.success = r;
					
					//System.out.println("Thread:" + thread_num + "-" + oper );

				}
				

			}
		}
		
		int numThreads;
		List < List <Operation> > operationsPerThread; // operations list per thread.
		
		List<BookingThread> runnables= new ArrayList<BookingThread> ();
		

		
		MultiThreadTester ( AvailableRoomCounts cnt, List < List <Operation>> operationsPerThread) {
			this.roomCnt = cnt;
			this.operationsPerThread = operationsPerThread;
			numThreads = operationsPerThread.size();
			
			// Threads simulating booking operation
			for (int i=0;i < numThreads;i++)
				runnables.add ( new BookingThread(i, operationsPerThread.get(i)));		

		}
		
		void verifyResult () {
			//Verify final status
			
			System.out.println("Verifying result");
			
			SimpleDate startDate = roomCnt.getStartDate();
			SimpleDate endDate = roomCnt.getLastAllocatedDate();

			
			int len = SimpleDate.dateDiff(startDate, endDate) + 1;
			
			// counter for the simulation result. Only care about the counters in the same day ranges
			// of current roomCnt
			int[] check = new int[len]; //(focus_days_end>initial_days?focus_days_end:initial_days)];
			for (int i=0;i<len;i++) {
				check [i] = roomCnt.getTotalRoomCount();
			}
			
			SimpleDate expectedStartDate = startDate; // also get the expected start date from operation list
			
			
			for (int thread = 0; thread < operationsPerThread.size(); thread++) {
				List <Operation> operations = operationsPerThread.get(thread);
			
				for (Operation oper : operations) {

					//System.out.println("Thread:" + thread + "-" + oper);
					
					switch (oper.type) {
					case DECREASE:
					case INCREASE:
						if (!oper.success) break;
						
						// Only cares about the date >= startDate
						SimpleDate inDate = (oper.inDate.before(startDate) ? startDate : oper.inDate);
						SimpleDate outDate =(oper.outDate.before(startDate) ? startDate : oper.outDate);
						
						
						SimpleDate dt = inDate.clone();
						
						while (dt.before(outDate) && !dt.after(endDate)) {
							int k = SimpleDate.dateDiff(startDate, dt);

							if (oper.type == OperationType.INCREASE)
								check[k]++;
							else
								check[k]--;
							
							dt.nextDay();
						}
							
						break;
						
					case QUERY:
						// Just check the result is in a valid range
						assertTrue ("Query result not valid", 
								(oper.queryResult>=0 && oper.queryResult <= roomCnt.getTotalRoomCount()));
						break;
						
					case DELETE_FRONT:
						// In this test DELETE_FRONT operation may return false
						// Which is still normal
						//assertTrue ("Delete front not success", oper.success);
						
						if (oper.success &&
						    oper.startDate.after(expectedStartDate))
							expectedStartDate = oper.startDate;
					}
				}
			}
			
			assertEquals (expectedStartDate, startDate);
			
			SimpleDate dt = startDate.clone();
			
			for (int i=0;i<len;i++)  { 
				//System.out.println("Date:" + i + ":" + check[i]);
				assertEquals ("Date #"+i, check[i], 
				        roomCnt.queryCount(dt));
				dt.nextDay();
			}
			
			System.out.println("Verification passed");
			
		}
			
		void runTest () {
			try {
				long time = System.currentTimeMillis();
				
				assertConcurrent( "Concurrent room count operation",
						runnables, 20000);
				
				long time_end = System.currentTimeMillis();
				System.out.println ("Total time used:" + (time_end - time));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			verifyResult(); 
		}
	}
	
	/*
	 * Randomly generate the test vector and invoke in multi-thread testing
	 * The random test vector is generated in a way simulating real HotelServer application
	 */
	@Test public void multiThreadTest_AvailableRoomCounts_Random () {
		
		final int initial_days = 60;
		SimpleDate startDate = new SimpleDate();
		
        final int num_threads=4;

		final int num_oprs = 50000;
		int focus_days_start = 0;
		int focus_days_end = 35; 
		final int inc_possibility_factor = 15; // 15%
		final int delete_possibility_factor = inc_possibility_factor;// + 1; // 1%

		final AvailableRoomCounts cnt = new AvailableRoomCounts(5, startDate, initial_days);
		
		List <List <Operation> > operationsPerThread = 
				new ArrayList <List<Operation>> ();
		
		Random rand = new Random(0);
		for (int i=0;i<num_threads;i++) {
			
			operationsPerThread.add( new ArrayList <Operation> () );
		}
		

		for (int j=0;j<num_oprs;j++)
		{
			for (int i=0;i<num_threads;i++) {

				List <Operation> operations = operationsPerThread.get(i);
				
				Operation oper = new Operation();
				int r = rand.nextInt(100);
				if (r <= inc_possibility_factor)
				{
					oper.type = OperationType.INCREASE;
					oper.inDate = oper.outDate = null; // let tester choose a previous success decrease record
				} else if (r <= delete_possibility_factor) {
					oper.type = OperationType.DELETE_FRONT;
					
					int moveDays = rand.nextInt(5); // move forward 0~5 days
					
					startDate.forwardDay(moveDays);
					focus_days_start += moveDays;
					focus_days_end += moveDays;
					
					oper.startDate = startDate.clone();
					
				} else {
					// decrease (booking)
					oper.type = OperationType.DECREASE;
			
					int inDate = focus_days_start + rand.nextInt(focus_days_end - focus_days_start); 
					int outDate = rand.nextInt(focus_days_end-inDate) + inDate + 1;
					
					oper.inDate = SimpleDate.getDateByDelta(startDate, inDate);
					oper.outDate = SimpleDate.getDateByDelta(startDate, outDate); 
				}
				
				operations.add(oper);
			}
			
//				System.out.println(oper);
		}
		
		MultiThreadTester tester = new MultiThreadTester (cnt, operationsPerThread);
		
		tester.runTest();
	}
	
	/*
	 * Manual test vector of add/decrease with high probability of data conflict
	 */
	@Test 
	public void multiThreadTest_AvailableRoomCounts_High_Counter_Conflict () {
				
		final int initial_days = 30;
		final AvailableRoomCounts cnt = new AvailableRoomCounts(5,new SimpleDate(), initial_days);
		SimpleDate startDate = new SimpleDate();
		
		List <List <Operation> > operationsPerThread = 
				new ArrayList <List<Operation>> ();
		
		// Test vector to be added to the operations
		final int [][][] testVector= {
			{ {-1,3,5}, {1,2,4}, {1,2,6}, {-1,3,6} } ,
			{ {1,2,4}, {-1,3,5}, {-1,2,5}, {1,4,7} } ,
			{ {-1,2,4}, {1,3,5}, {1,2,5}, {-1,4,7} } ,
			{ {1,2,4}, {-1,3,5}, {-1,2,5}, {1,4,7} } ,
			{ {-1,2,4}, {1,3,5}, {1,2,5}, {-1,4,7} } 
		};
		
		for (int th = 0; th < testVector.length; th++) {
			List <Operation> operations = new ArrayList <Operation> ();
			
			for (int i = 0; i < testVector[th].length; i++) {
				SimpleDate inDate = SimpleDate.getDateByDelta(startDate, testVector[th][i][1]);
				SimpleDate outDate = SimpleDate.getDateByDelta(startDate, testVector[th][i][2]);
				
				if (testVector[th][i][0] < 0)
					operations.add(Operation.DecOper(inDate, outDate));
				else
					operations.add(Operation.IncOper(inDate, outDate));
			}
			
			operationsPerThread.add (operations);
		}
		
		MultiThreadTester tester = new MultiThreadTester (cnt, operationsPerThread);
		
		tester.runTest();
		
		
/*      // Test #2: manual data targeting possible contention of structure change
		final int focus_days_end = 40;   // change to 40 to test size increase interaction

		final int [][][] booking= {
			{ {-1,3,5,0}, {-1,27,35,0}, {-1,34,35,0} } ,
			{ {-1,2,4,0}, {-1,28,33,0}, {-1,32,36,0} } ,
		}; /* */
		

	}
	
	/*
	 * Manual test vector of add/decrease with high probability of data conflict
	 * between decrease/increase and deleteFront
	 */
	@Test 
	public void multiThreadTest_AvailableRoomCounts_Conflict_Delete () {
				
		final int initial_days = 30;
		final AvailableRoomCounts cnt = new AvailableRoomCounts(5,new SimpleDate(), initial_days);
		SimpleDate startDate = new SimpleDate();
		
		List <List <Operation> > operationsPerThread = 
				new ArrayList <List<Operation>> ();
		
		// Test vector to be added to the operations
		// deleteFront concurrent with decrease (with counter expansion)
		final int [][][] testVector= {
			{ {-1,3,5}, {0,2,0}, {1,2,6}, {-1,60,66} } ,
			{ {1,2,4}, {-1,3,5}, {-1,2,5}, {1,4,7} } ,
			{ {-1,2,4}, {1,3,5}, {1,2,5}, {0,4,0} } ,
			{ {1,2,4}, {-1,33,35}, {-1,2,5}, {1,4,7} } ,
			{ {-1,2,4}, {1,3,5}, {1,2,5}, {-1,4,7} } 
		};
		
		for (int th = 0; th < testVector.length; th++) {
			List <Operation> operations = new ArrayList <Operation> ();
			
			for (int i = 0; i < testVector[th].length; i++) {
				SimpleDate inDate = SimpleDate.getDateByDelta(startDate, testVector[th][i][1]);
				SimpleDate outDate = SimpleDate.getDateByDelta(startDate, testVector[th][i][2]);
				
				if (testVector[th][i][0] < 0)
					operations.add(Operation.DecOper(inDate, outDate));
				else if (testVector[th][i][0] > 0)
					operations.add(Operation.IncOper(inDate, outDate));
				else
					operations.add(Operation.DeleteFrontOper(inDate));
			}
			
			operationsPerThread.add (operations);
		}
		
		MultiThreadTester tester = new MultiThreadTester (cnt, operationsPerThread);
		
		tester.runTest();
		
		
/*      // Test #2: manual data targeting possible contention of structure change
		final int focus_days_end = 40;   // change to 40 to test size increase interaction

		final int [][][] booking= {
			{ {-1,3,5,0}, {-1,27,35,0}, {-1,34,35,0} } ,
			{ {-1,2,4,0}, {-1,28,33,0}, {-1,32,36,0} } ,
		}; /* */
		

	}
		
}
