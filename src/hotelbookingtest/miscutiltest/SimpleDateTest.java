package hotelbookingtest.miscutiltest;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import hotelbooking.miscutil.SimpleDate;

public class SimpleDateTest {
	
	@Test public void testDateDiff() {
	    
	   SimpleDate.setDateFormat("yyyy/MM/dd");
       SimpleDate d1;
       SimpleDate d2;
       
        try {
            d1 = SimpleDate.parse("2015/12/5");
            d2 = SimpleDate.parse("2015/12/10");
            assertEquals (5, SimpleDate.dateDiff(d1,d2));
            assertEquals (-5, SimpleDate.dateDiff(d2, d1));
            
            d1 = SimpleDate.parse("2015/10/29");
            d2 = SimpleDate.parse("2015/11/2");
            assertEquals (4, SimpleDate.dateDiff(d1,d2));
            assertEquals (-4, SimpleDate.dateDiff(d2,d1));

            
            d1 = SimpleDate.parse("2016/1/15");
            d2 = SimpleDate.parse("2016/2/23");
            assertEquals (39, SimpleDate.dateDiff(d1,d2));
            assertEquals (-39, SimpleDate.dateDiff(d2,d1));

            
          //  assertEquals (4, AvailableRoomCounts.dateDiff1(d1,d2));
            
            d1 = SimpleDate.parse("2015/3/1");
            d2 = SimpleDate.parse("2015/3/10");
            assertEquals (9, SimpleDate.dateDiff(d1,d2));
            assertEquals (-9, SimpleDate.dateDiff(d2,d1));

         //   assertEquals (8, AvailableRoomCounts.dateDiff1(d1,d2));
            
            d1 = SimpleDate.parse("2015/10/29");
            d2 = SimpleDate.parse("2016/1/7");
            assertEquals (70, SimpleDate.dateDiff(d1,d2));
            assertEquals (-70, SimpleDate.dateDiff(d2,d1));

      //      assertEquals (-70, AvailableRoomCounts.dateDiff(d2,d1));
            
            d1 = SimpleDate.parse("2015/10/29");
            d2 = SimpleDate.parse("2016/3/17");
            // error here, not going to fix in this program
            assertEquals (140, SimpleDate.dateDiff(d1,d2));
            assertEquals (-140, SimpleDate.dateDiff(d2,d1));


            d1 = SimpleDate.parse("2016/1/30");
            d2 = SimpleDate.parse("2015/12/30");
            assertEquals (-31, SimpleDate.dateDiff(d1,d2));
            assertEquals (31, SimpleDate.dateDiff(d2,d1));
            
            d1 = SimpleDate.parse("2016/3/1");
            d2 = SimpleDate.parse("2015/12/30");
            assertEquals (-62, SimpleDate.dateDiff(d1,d2));
            assertEquals (62, SimpleDate.dateDiff(d2,d1));

            d1 = SimpleDate.parse("2017/1/30");
            d2 = SimpleDate.parse("2016/12/30");
            assertEquals (-31, SimpleDate.dateDiff(d1,d2));
            assertEquals (31, SimpleDate.dateDiff(d2,d1));
            
            d1 = SimpleDate.parse("2017/3/1");
            d2 = SimpleDate.parse("2016/12/30");
            assertEquals (-61, SimpleDate.dateDiff(d1,d2));
            assertEquals (61, SimpleDate.dateDiff(d2,d1));

        } catch (ParseException e) {
            e.printStackTrace();
        } // set to today
	    
	}
}
