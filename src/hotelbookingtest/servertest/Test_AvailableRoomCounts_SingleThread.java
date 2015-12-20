package hotelbookingtest.servertest;

import static org.junit.Assert.*;

import org.junit.Test;

import hotelbooking.miscutil.SimpleDate;
import hotelbooking.server.AvailableRoomCounts;

public class Test_AvailableRoomCounts_SingleThread {
		
	static AvailableRoomCounts createNewCntAndVerify (int rooms, SimpleDate startDate, int size) {
		AvailableRoomCounts roomCnt = new AvailableRoomCounts (rooms, startDate, size);
		
		// Verify initial status
		Tester tester = new Tester (roomCnt);
		tester.verifyLen (size);
		tester.verifyCnts (0, size-1, rooms);	
		
		return roomCnt;
	}
	
	static class Tester {
		AvailableRoomCounts roomCnt;
		SimpleDate startDate;
		
		public Tester (AvailableRoomCounts cnt) {
			this.roomCnt = cnt;
			startDate = roomCnt.getStartDate();
		}
		
		static void testIllegalConstructor (int rooms, SimpleDate date, int size) {
			// Illegal parameters, throws Exception
			try {
				new AvailableRoomCounts (rooms, date, size);
				fail ("Exception not throwed");
			} catch (RuntimeException e) {
				
			}
		}
		
		void verifyDecreaseTrue (int dt1, int dt2) {
			assertTrue (roomCnt.decreaseDayRange(
					SimpleDate.getDateByDelta(startDate, dt1), 
					SimpleDate.getDateByDelta(startDate, dt2)));
		}
		
		void verifyDecreaseFalse (int dt1, int dt2) {
			assertFalse (roomCnt.decreaseDayRange(
					SimpleDate.getDateByDelta(startDate, dt1), 
					SimpleDate.getDateByDelta(startDate, dt2)));
		}
		
		void verifyDecreaseException (int dt1, int dt2) {
			SimpleDate d1 = SimpleDate.getDateByDelta(startDate, dt1);
			SimpleDate d2 = SimpleDate.getDateByDelta(startDate, dt2);
			
			try {
				roomCnt.decreaseDayRange(d1, d2);
				fail ("Exception not thrown");
			} catch (RuntimeException e) {
				
			}
		}
		
		void verifyIncreaseTrue (int dt1, int dt2) {
			assertTrue (roomCnt.increaseDayRange(
					SimpleDate.getDateByDelta(startDate, dt1), 
					SimpleDate.getDateByDelta(startDate, dt2)));
		}
		
		void verifyIncreaseFalse (int dt1, int dt2) {
			assertFalse (roomCnt.increaseDayRange(
					SimpleDate.getDateByDelta(startDate, dt1), 
					SimpleDate.getDateByDelta(startDate, dt2)));
		}
		
		void verifyIncreaseException (int dt1, int dt2) {
			SimpleDate d1 = SimpleDate.getDateByDelta(startDate, dt1);
			SimpleDate d2 = SimpleDate.getDateByDelta(startDate, dt2);
			
			try {
				roomCnt.increaseDayRange (d1, d2);
				fail ("Exception not thrown");
			} catch (RuntimeException e) {
				
			}
		}
				
		// Verify the counter from (today+d1) to (today+d2) shall equal to expected
		void verifyCnts (int d1, int d2, int expected) {
			for (int i=d1;i<=d2;i++)
				assertEquals (expected, roomCnt.queryCount(
				        SimpleDate.getDateByDelta(startDate,i), 
				        SimpleDate.getDateByDelta(startDate,i+1)));

		}
		
		// Verify the length of allocated counters shall be >=len
		void verifyLen (int len) throws AssertionError {
			SimpleDate date = SimpleDate.getDateByDelta(startDate, len-1);
			SimpleDate actualLast = roomCnt.getLastAllocatedDate();
			assertTrue (SimpleDate.dateDiff(date, actualLast) >= 0);
		}
		
		// Verify single date query
		public void verifyQuery (int dt, int cnt) {
			SimpleDate date = SimpleDate.getDateByDelta(startDate, dt);
			int n = roomCnt.queryCount(date);
			assertEquals (cnt, n);
		}
		
		// Verify the range query
		public void verifyQuery (int dt1, int dt2, int cnt) {
			SimpleDate d1 = SimpleDate.getDateByDelta(startDate, dt1);
			SimpleDate d2 = SimpleDate.getDateByDelta(startDate, dt2);
			int n = roomCnt.queryCount(d1, d2);
			assertEquals ( cnt, n);
		}
		

		
		// Verify exception of single date query
		public void verifyQueryException (int dt) {
			//query invalid date
			SimpleDate date = SimpleDate.getDateByDelta(startDate, dt);
			try {
				roomCnt.queryCount(date);
				fail ("Exception not thrown");
			} catch (RuntimeException e) {
				
			}
		}
		
		// Verify exception of range query
		public void verifyQueryException (int dt1, int dt2) {
			SimpleDate d1 = SimpleDate.getDateByDelta(startDate,  dt1);
			SimpleDate d2 = SimpleDate.getDateByDelta(startDate,  dt2);
			
			try {
				roomCnt.queryCount(d1,d2);
				fail ("Exception not thrown");
			} catch (RuntimeException e) {
				
			}
			
		}
		

	}
	
	@Test
	public void testAvailableRoomCounts () {
		
		SimpleDate startDate = new SimpleDate();
		
		Tester.testIllegalConstructor(-1, startDate, 30);
		Tester.testIllegalConstructor(0, startDate, 30);
		Tester.testIllegalConstructor(5, startDate, 0);
		Tester.testIllegalConstructor(5, startDate, -1);
		
	}
	
	// Test the query with different input parameters for basic verification only
	// The validation of result will be thoroughly tested together with increase/decrease operation
	@Test public void testQueryCount () {
		
		SimpleDate startDate = new SimpleDate();
		
		AvailableRoomCounts roomCnt = createNewCntAndVerify(5, startDate, 30);

		Tester tester = new Tester (roomCnt);
		
		//query first date
		tester.verifyQuery(0, 5);
		
		//query last date
		int last = SimpleDate.dateDiff(startDate, roomCnt.getLastAllocatedDate());
		tester.verifyQuery(last, 5);
		
		// query date in the middle

		for (int i = 1; i<last; i++) {
			tester.verifyQuery(i, 5);
		}
		
		//query date beyond the last
		tester.verifyQuery(last + 1, 5);
		tester.verifyQuery(last+6, 5);
		
		//query invalid date
		tester.verifyQueryException(-1);
		tester.verifyQueryException(-2);
		
		//query range - whole
		tester.verifyQuery(0, last, 5);
		
		//query range - first one day
		tester.verifyQuery(0, 1, 5);

		//query range - last one day
		tester.verifyQuery(last, last+1, 5);
		
		//query range - subrange in middle
		tester.verifyQuery(5, last-5, 5);

		//query range - begin in range, end out of range
		tester.verifyQuery(last-2, last+2, 5);
		
		//query range - begin and end out of range
		tester.verifyQuery(last+1, last+3, 5);
		
		//query range with invalid dates
		tester.verifyQueryException(5, 5);
		
		tester.verifyQueryException(6,5);
		
	}
	
	
	@Test public void testDecreaseDayRange() {
		
		SimpleDate startDate = new SimpleDate();
		
		AvailableRoomCounts roomCnt = createNewCntAndVerify(5, startDate, 30);

		Tester tester = new Tester (roomCnt);
		
		// verify decrement
		tester.verifyDecreaseTrue (0, 5);
		tester.verifyDecreaseTrue (3, 7);
		tester.verifyDecreaseTrue (7, 8);

		
		tester.verifyCnts(0,2,4);
		tester.verifyCnts(3,4,3);
		tester.verifyCnts(5,7,4);
		tester.verifyCnts(8,31,5);
		
		// decrease till zero
		tester.verifyDecreaseTrue (3, 6);
		tester.verifyDecreaseTrue (3, 6);
		tester.verifyDecreaseTrue (3, 6);
		tester.verifyCnts (0, 2, 4);
		tester.verifyCnts (3, 4, 0);
		tester.verifyCnts (5, 5, 1);
		tester.verifyCnts (6, 7, 4);
		
		// not able to decrease more
		tester.verifyDecreaseFalse (3, 5);
		tester.verifyDecreaseFalse (3, 6);
		tester.verifyDecreaseFalse (1, 4);
		tester.verifyDecreaseFalse (4, 5);
		tester.verifyCnts (0, 2, 4);
		tester.verifyCnts (3, 4, 0);
		tester.verifyCnts (5, 5, 1);
		tester.verifyCnts (6, 7, 4);
		
		// still able to decrease for days next to the zero range
		tester.verifyDecreaseTrue (2, 3);
		tester.verifyDecreaseTrue (5, 6);
		tester.verifyCnts (0, 1, 4);
		tester.verifyCnts (2, 2, 3);
		tester.verifyCnts (3, 5, 0);
		tester.verifyCnts (6, 7 ,4);
		
		// verify range query btw
		tester.verifyQuery (0, 1, 4);
		tester.verifyQuery (0, 2, 4);
		tester.verifyQuery (0, 3, 3);
		tester.verifyQuery (0, 4, 0);
		tester.verifyQuery (4, 5, 0);
		tester.verifyQuery (4, 6, 0);
		tester.verifyQuery (5, 6, 0);
		tester.verifyQuery (5, 7, 0);
		tester.verifyQuery (6, 7, 4);

		
		// verify decrement at the end of allocated dates
		SimpleDate last = roomCnt.getLastAllocatedDate();
		int nLast = SimpleDate.dateDiff(startDate, last);
		
		tester.verifyDecreaseTrue (nLast - 2, nLast);
		tester.verifyDecreaseTrue (nLast - 4, nLast-2);
		tester.verifyDecreaseTrue (nLast - 3, nLast+1);
		tester.verifyCnts (nLast - 4, nLast - 4, 4);
		tester.verifyCnts (nLast - 3, nLast -1, 3);
		tester.verifyCnts (nLast, nLast, 4);

		// last day shall not change till now
		last = roomCnt.getLastAllocatedDate();
		assertEquals (nLast, SimpleDate.dateDiff(startDate, last));
		
		// verify decrement across the end of allocated dates
		tester.verifyDecreaseTrue (nLast - 3, nLast + 5);
		tester.verifyLen (nLast + 4);
		tester.verifyCnts (nLast -3, nLast - 1, 2);
		tester.verifyCnts (nLast, nLast, 3);
		tester.verifyCnts (nLast + 1, nLast + 4, 4);
		
		// verify invalid decrement arguments
		tester.verifyDecreaseFalse (-1, 2);
		tester.verifyDecreaseException (2, 2);
		tester.verifyDecreaseException (3, 2);
		
		// counter shall not change
		tester.verifyCnts (0, 1, 4);
		tester.verifyCnts (2, 2, 3);
		tester.verifyCnts (3, 5, 0);
				
	}
	

	
	@Test public void testIncreaseDayRange() {
		
		SimpleDate startDate = new SimpleDate();
		
		AvailableRoomCounts roomCnt = createNewCntAndVerify(5, startDate, 30);

		Tester tester = new Tester (roomCnt);
		
		// Make some decrease first
		tester.verifyDecreaseTrue (0, 5);
		tester.verifyDecreaseTrue (3, 7);
		tester.verifyDecreaseTrue (7, 8);
		tester.verifyDecreaseTrue (3, 6);
		tester.verifyDecreaseTrue (3, 6);
		tester.verifyDecreaseTrue (3, 6);

		tester.verifyCnts (0, 2, 4);
		tester.verifyCnts (3, 4, 0);
		tester.verifyCnts (5, 5, 1);
		tester.verifyCnts (6, 7, 4);
		
		// Verify increase
		tester.verifyIncreaseTrue (3, 6);
		tester.verifyIncreaseTrue (3, 6);
		tester.verifyIncreaseTrue (3, 6);
		tester.verifyIncreaseTrue (1, 4);
		
		tester.verifyCnts (0, 0, 4);
		tester.verifyCnts (1, 2, 5);
		tester.verifyCnts (3, 3, 4);
		tester.verifyCnts (4, 4, 3);
		tester.verifyCnts (5, 7, 4);
		tester.verifyCnts (8, 30, 5);
		
		// not able to increase more
		tester.verifyIncreaseFalse (0,2);
		tester.verifyIncreaseFalse (0,3);
		tester.verifyIncreaseFalse (2,3);
		tester.verifyIncreaseFalse (5,9);
		tester.verifyIncreaseFalse (8,10);
		
		tester.verifyCnts (0, 0, 4);
		tester.verifyCnts (1, 2, 5);
		tester.verifyCnts (3, 3, 4);
		tester.verifyCnts (4, 4, 3);
		tester.verifyCnts (5, 7, 4);
		tester.verifyCnts (8, 30, 5);
		
		// still able to increase for days besides
		tester.verifyIncreaseTrue (0,1);
		tester.verifyIncreaseTrue (3,4);
		tester.verifyCnts (0, 0, 5);
		tester.verifyCnts (1, 2, 5);
		tester.verifyCnts (3, 3, 5);
		tester.verifyCnts (4, 4, 3);
		
		// verify increment at the end of allocated dates
		SimpleDate last = roomCnt.getLastAllocatedDate();
		int nLast = SimpleDate.dateDiff(startDate, last);
		
		tester.verifyDecreaseTrue (nLast - 2, nLast);
		tester.verifyDecreaseTrue (nLast - 4, nLast-2);
		tester.verifyDecreaseTrue (nLast - 3, nLast+1);
		tester.verifyCnts (nLast - 4, nLast - 4, 4);
		tester.verifyCnts (nLast - 3, nLast -1, 3);
		tester.verifyCnts (nLast, nLast, 4);
		
		tester.verifyIncreaseTrue (nLast - 2, nLast);
		tester.verifyIncreaseTrue (nLast - 1, nLast + 1);
		tester.verifyCnts (nLast - 4, nLast - 4 , 4);
		tester.verifyCnts (nLast - 3, nLast - 3, 3);
		tester.verifyCnts (nLast - 2, nLast - 2, 4);
		tester.verifyCnts (nLast - 1, nLast, 5);

		// last day shall not change till now
		last = roomCnt.getLastAllocatedDate();
		assertEquals (nLast, SimpleDate.dateDiff(startDate, last));
		
		// verify increment across the end of allocated dates (false)
		tester.verifyDecreaseTrue (nLast -1 , nLast + 1);
		tester.verifyIncreaseFalse (nLast - 1, nLast + 2);
		
		tester.verifyLen ( nLast + 1);
		tester.verifyCnts (nLast - 1, nLast, 4);
		tester.verifyCnts (nLast + 1, nLast + 5, 5);
		
		// verify invalid increment arguments
		tester.verifyIncreaseFalse (-1, 2);
		tester.verifyIncreaseException (2, 2);
		tester.verifyIncreaseException (3, 2);
		
		// counter shall not change
		tester.verifyCnts (0, 0, 5);
		tester.verifyCnts (1, 2, 5);
		tester.verifyCnts (3, 3, 5);
		tester.verifyCnts (4, 4, 3);
				
	}
	
	@Test public void testDeleteCountersTillDate () {
		SimpleDate startDate = new SimpleDate();
		SimpleDate startDateCloned = startDate.clone();
		
		AvailableRoomCounts roomCnt = createNewCntAndVerify(5, startDate, 30);

		Tester tester = new Tester (roomCnt);
		
		// Make some decrease first
		tester.verifyDecreaseTrue (0, 5);
		tester.verifyDecreaseTrue (3, 7);
		tester.verifyDecreaseTrue (7, 8);
		tester.verifyDecreaseTrue (3, 6);
		tester.verifyDecreaseTrue (3, 6);
		tester.verifyDecreaseTrue (3, 6);

		tester.verifyCnts (0, 2, 4);
		tester.verifyCnts (3, 4, 0);
		tester.verifyCnts (5, 5, 1);
		tester.verifyCnts (6, 7, 4);
		
		SimpleDate st1 = roomCnt.getStartDate();
		assertEquals (startDate, st1);
		
		SimpleDate dt = SimpleDate.getDateByDelta(startDate, 5);
		
		roomCnt.deleteCountersTillDate(dt);
		
		// the startDate was passed in constructor
		// Now first days deleted, the original startDate shall not be changed
		assertEquals (startDateCloned, startDate);
		
		assertEquals (dt, roomCnt.getStartDate());
		
		startDate = dt;
		
		tester = new Tester (roomCnt);
		
		tester.verifyCnts (0, 0, 1);
		tester.verifyCnts (1, 2, 4);
		tester.verifyCnts (3, 60, 5);
		
	}


}
