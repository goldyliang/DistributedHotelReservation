package HotelServer;

import static org.junit.Assert.*;

import java.util.Arrays;
//import java.util.Date;

import HotelServer.ReserveRecords;
import HotelServerInterface.IHotelServer.RoomType;
import HotelServerInterface.IHotelServer.Record;
import miscutil.SimpleDate;

import org.junit.Test;

public class ReserveRecordsTest {
	
	@Test
	public void test() {
		
		SimpleDate today = new SimpleDate();
		
		ReserveRecords recs = new ReserveRecords();
		
		System.out.println (recs);
		
		// [idx] [guesID, opr (-1,booking, +1 cancel), typ (0,1,2), inDate, outDate, return_value]
		int [][] booking= {
				{1000,-1,0,3,5,0}, 
				{1020,-1,1,2,4,0},
				{1000,-1,1,3,7,0},
				{1000,-1,0,7,12,0},
				{1000, 1,0,7,8,0},
				{1000, 1,0,4,5,0},
				{1020, 1,0,2,3,0},
				{ 900,-1,2,5,12,0},
				{ 900, 1,2,7,9,0}
			};
		
		int cc=0;
		int resID=0;
		
		for (int [] bk: booking) {
			Record r = new Record (
			        (int)(System.currentTimeMillis() % 10000),
					String.valueOf(bk[0]),
					"Test",
					RoomType.values()[bk[2]],
					SimpleDate.getDateByDelta (today, bk[3]),
					SimpleDate.getDateByDelta (today, bk[4]),
					50);
			
			if (Integer.parseInt(r.guestID) == 1000) {
			    cc ++;
			    if (cc==3)
			        resID = r.resID;
			}
			
			switch (bk[1]) {
			case -1: // reserver
				bk[5] = (recs.makeReservation ( String.valueOf(bk[0]), r)? 1 : 0);
				break;
			case 1:
				bk[5] = (recs.cancelReservation( String.valueOf(bk[0]), r, 
				        (int)(System.currentTimeMillis() % 10000))? 1 : 0);
			}
		}
		
		Record r = recs.findRecord("1000", resID);
		assertNotNull(r);
		
		assertEquals(resID, r.resID);
		assertEquals("1000", r.guestID);
		System.out.println (r);
		
		System.out.println(Arrays.deepToString(booking));
		
		System.out.println (recs);
	}

}
