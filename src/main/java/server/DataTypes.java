package server;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataTypes {

	
	public enum RoomType {
		SINGLE, DOUBLE, FAMILY;
		
		RoomType () {}
		};

	 public static class HotelProfile  {
		 
		public String shortName;
		
		public String fullName;
		
		public String descText;
		
		public EnumMap <RoomType, Integer> totalRooms;
		
		public EnumMap <RoomType, Float> rates;
		
		public int allTotalRooms;
		
		
	}

	/*	public enum ErrorCode {
		SUCCESS,
		HOTEL_NOT_FOUND, // Can not find the hotel by name
		RECORD_NOT_FOUND, // No current record found as requested
		ROOM_UNAVAILABLE, // No available room as requested
		INTERNAL_ERROR, // Error of internal data
		INVALID_DATES,  // Invalid date input
		INVALID_GUEST_ID, // Invalid guest ID
		EXCEPTION_THROWED, // Various Runtime exception throwed (except for the RemoteException
		REGISTRY_CONNECTION_FAILURE,
		SERVER_CONNECTION_FAILURE,
		MGR_LOGIN_FAILURE
	} */
	
	//public final String dateFormat = "yyyy/MM/dd";
	
	//@XmlRootElement
	public static class Availability {
		/**
		 * 
		 */
		public String hotelName;
		public int availCount;
		public float rate;
		
		public Availability() {}
	}

	// Complete informatoin about a reservation
	
	//@XmlRootElement
	public static class Record {
		
		@JsonProperty("resid")
	    public int resID;
		
		@JsonProperty("id")
		public String guestID; 
		
		@JsonProperty("hotel")
		public String shortName;
		
		@JsonProperty("type")
		public RoomType roomType;
		
		@JsonProperty("in")
		public SimpleDate checkInDate; 
		
		@JsonProperty("out")
		public SimpleDate checkOutDate;
		
		@JsonProperty("rate")
		public float rate; // negative if not confirmed
		
		public Record () {
			super();
		}
		
		public Record (
		        int resID,
		        String guestID, 
				String shortName, 
				RoomType roomType,
				SimpleDate checkInDate,
				SimpleDate checkOutDate,
				float rate) {
		    this.resID = resID;
			this.guestID = guestID;
			this.shortName = shortName;
			this.roomType = roomType;
			this.checkInDate = checkInDate;
			this.checkOutDate = checkOutDate;
			this.rate = rate;
		}
		
		public Record (Record r) {
			this (r.resID, r.guestID, r.shortName, r.roomType, r.checkInDate, r.checkOutDate, r.rate);
		}
		
		public String toString() {
			String s = "Reservation ID:" + resID + "\n";
			s = s + "GuestID:" + guestID + "\n";
			s = s + "Hotel Short Name:" + shortName + "\n";
			s = s + "Room Type:" + roomType.toString() + "\n";
						
			s = s + "Check in Date:" + checkInDate + "\n";
			s = s + "Check out Date:" + checkOutDate + "\n";
			
			if (rate>0)
				s = s + "Rate:" + rate + "\n";
			
			return s;
		}
		
		public String toOneLineString() {
			String s = "ResID:" + resID;
			s = s + ";GuestID:" + guestID;
			s = s + ";Hotel:" + shortName;
			s = s + ";Type:" + roomType.toString();
						
			s = s + ";In:" + checkInDate;
			s = s + ";Out:" + checkOutDate;
			
			s = s + ";Rate:" + rate;
			
			return s;			
		}
		
	}

	public static class SimpleDate {
	    public int year;
	    public int month;
	    public int day;
	    
	    private static String dfString = "yyyy/MM/dd";
	    
	    // a thread local DateFormat variable
	    // because DateFormat is not thread-safe
	    private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat> () {
	        @Override
	        protected DateFormat initialValue() {
	            return new SimpleDateFormat (dfString);
	        }
	    };
	    
	    public SimpleDate() {
	        Calendar cal = Calendar.getInstance();
	        set (cal);
	    }
	    
	    public SimpleDate(int year, int month, int day) {
	        set (year, month, day);
	    }
	    
	    public SimpleDate (SimpleDate dt) {
	        set (dt.year, dt.month, dt.day);
	    }
	    
	    public int getYear() { return year; }
	    public int getMonth() { return month;}
	    public int getDay() { return day;}
	    
	    public static void setDateFormat (String fmt) {
	        dfString = fmt;
	        df.set(new SimpleDateFormat (fmt));
	    }
	    
	    public static String getDateFormat() {
	        return dfString;
	    }
	    
	    public void set (int year, int month, int day) {
	        this.year = year;
	        this.month = month;
	        this.day = day;
	    }
	    
	    public void set (Calendar cal) {
	        year = cal.get(Calendar.YEAR);
	        month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero based
	        day = cal.get(Calendar.DAY_OF_MONTH);
	    }
	    
	    public void set (Date dt) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(dt);
	        set (cal);
	    }
	    
	    public String toString() {
	        if (year==0 || month==0 || day==0)
	            return "NULL";
	        
	        Calendar cal = Calendar.getInstance();
	        cal.set(year, month-1, day);
	        return df.get().format( cal.getTime());
	    }
	    
	    public void encodeDate (ByteBuffer bbuf) {
	        bbuf.putShort( (short)year);
	        bbuf.put ( (byte)month); // Calender.MONTH is zero based
	        bbuf.put ( (byte)day);
	    }
	    
	    public static void encodeDate (ByteBuffer bbuf, SimpleDate dt) {
	        if (dt==null) {
	            new SimpleDate(0,0,0).encodeDate(bbuf);  // empty date
	        } else
	            dt.encodeDate(bbuf);
	    }
	    
	    public static SimpleDate decodeDate (ByteBuffer bbuf) {
	        return new SimpleDate (
	            bbuf.getShort(), // year
	            bbuf.get(),  // month
	            bbuf.get()); // day
	    }
	    
	    public Calendar toCalendar() {
	        Calendar cal = Calendar.getInstance();
	        cal.clear();
	        cal.set(year, month-1,day); // Calendar month zero based
	        return cal;
	    }
	    

	    
	    public void forwardDay(int i) {
	        Calendar cal = toCalendar ();
	        cal.add(Calendar.DATE, i);
	        set (cal);        
	    }
	    
	    public void nextDay () {
	        forwardDay (1);
	    }
	    
	    public static SimpleDate getDateByDelta (SimpleDate date, int i) {
	        SimpleDate newDate = new SimpleDate (date);
	        newDate.forwardDay(i);
	        return newDate;
	    }
	    
	    public int compareTo (SimpleDate date1) {
	        Calendar c1 = toCalendar();
	        Calendar c2 = date1.toCalendar();
	        
	        return c1.compareTo(c2);
	    }
	    
	    public boolean after (SimpleDate date1) {
	        return compareTo (date1) > 0;
	    }
	    
	    public boolean before (SimpleDate date1) {
	        return compareTo (date1) < 0;
	    }
	    
	    @Override
	    public boolean equals (Object obj) {
	        if (! (obj instanceof SimpleDate) )
	            return false;
	        
	        SimpleDate d1 = (SimpleDate) obj;
	        
	        return (year == d1.year &&
	                month == d1.month &&
	                day == d1.day);
	    }
	    
	    @Override
	    public SimpleDate clone () {
	        
	        return new SimpleDate (this);
	    }
	    
	    // how many days from d1 to d2
	    // >0 if d2>d1
	    public static int dateDiff (SimpleDate d1, SimpleDate d2) {
	        Calendar dayOne = d1.toCalendar();
	        Calendar dayTwo = d2.toCalendar();

	        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
	            return dayTwo.get(Calendar.DAY_OF_YEAR) - dayOne.get(Calendar.DAY_OF_YEAR);
	        } else {
	            if (dayTwo.get(Calendar.YEAR) < dayOne.get(Calendar.YEAR)) {
	                //swap them
	                Calendar temp = dayOne;
	                dayOne = dayTwo;
	                dayTwo = temp;
	            }
	            
	            int extraDays = dayTwo.get(Calendar.DAY_OF_YEAR);
	            dayTwo.add(Calendar.DATE, - extraDays);
	            
	            while (dayOne.get(Calendar.YEAR) < dayTwo.get(Calendar.YEAR)) {
	                extraDays += dayTwo.getActualMaximum(Calendar.DAY_OF_YEAR);

	                dayTwo.add(Calendar.YEAR, -1);
	                // getActualMaximum() important for leap years
	                // TODO: there is some error here!!!!!
	            }

	            return extraDays - dayOne.get(Calendar.DAY_OF_YEAR) + dayTwo.get(Calendar.DAY_OF_YEAR);
	        }
	    }
	    
	    public static SimpleDate parse (String s) throws ParseException {
	        SimpleDate date = new SimpleDate();
	        date.set(df.get().parse(s));
	        return date;
	    }
	}
	

}

/*	public enum ErrorCode {
	SUCCESS,
	HOTEL_NOT_FOUND, // Can not find the hotel by name
	RECORD_NOT_FOUND, // No current record found as requested
	ROOM_UNAVAILABLE, // No available room as requested
	INTERNAL_ERROR, // Error of internal data
	INVALID_DATES,  // Invalid date input
	INVALID_GUEST_ID, // Invalid guest ID
	EXCEPTION_THROWED, // Various Runtime exception throwed (except for the RemoteException
	REGISTRY_CONNECTION_FAILURE,
	SERVER_CONNECTION_FAILURE,
	MGR_LOGIN_FAILURE
} */

//public final String dateFormat = "yyyy/MM/dd";

