package HotelServer;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import HotelServer.ReserveRecords;
import HotelServerInterface.IHotelServer.RoomType;
import HotelServerInterface.IHotelServer.Record;


import org.junit.Test;

public class ReserveRecordsTest {

	Date getDateByDelta (Date d, int delta)
	{
		if (delta>=0)
			return new Date (d.getTime() + TimeUnit.DAYS.toMillis(delta));
		else
			return new Date (d.getTime() - TimeUnit.DAYS.toMillis(-delta));
				
	}
	
	@Test
	public void test() {
		
		Date today=null;
		
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      
		try {
			today = sdf.parse(sdf.format(new Date()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // set to today
		
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
		
		for (int [] bk: booking) {
			Record r = new Record (
					"1234",
					"Test",
					RoomType.values()[bk[2]],
					getDateByDelta (today, bk[3]),
					getDateByDelta (today, bk[4]),
					50);
			
			switch (bk[1]) {
			case -1: // reserver
				bk[5] = (recs.makeReservation ( String.valueOf(bk[0]), r)? 1 : 0);
				break;
			case 1:
				bk[5] = (recs.cancelReservation( String.valueOf(bk[0]), r)? 1 : 0);
			}
		}
		
		System.out.println(Arrays.deepToString(booking));
		
		System.out.println (recs);
	}

}
