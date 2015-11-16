package client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import client.webservice.*;

public class MiscUtil {

    private static String dfString = "yyyy/MM/dd";
    
    // a thread local DateFormat variable
    // because DateFormat is not thread-safe
    private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat> () {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat (dfString);
        }
    };

    public static String getDateFormat () {
    	return dfString;
    }
    
    public static SimpleDate parseDate (String txt) throws ParseException {
        SimpleDate date = new SimpleDate();
        Date dt = df.get().parse(txt);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        date.setYear(cal.get(Calendar.YEAR));
        date.setMonth(cal.get(Calendar.MONTH) + 1);
        date.setDay(cal.get(Calendar.DAY_OF_MONTH));
        
        return date;
    }
    
    public static String formatDate (SimpleDate dt) {
    	int year = dt.getYear();
    	int month = dt.getMonth();
    	int day = dt.getDay();
    	
        if (year==0 || month==0 || day==0)
            return "NULL";
        
        Calendar cal = Calendar.getInstance();
        cal.set(year, month-1, day);
        return df.get().format( cal.getTime());
    }
    
	public static Record createRecord (
			        int resID,
			        String guestID, 
					String shortName, 
					RoomType roomType,
					SimpleDate checkInDate,
					SimpleDate checkOutDate,
					float rate) {
		Record r = new Record();
		
	    r.setResID(resID);
		r.setGuestID(guestID);
		r.setShortName(shortName);
		r.setRoomType(roomType);
		r.setCheckInDate(checkInDate);
		r.setCheckOutDate (checkOutDate);
		r.setRate(rate);
		
		return r;
	}
			
	public static String getRecordOneLineStr(Record r) {
		String s = "ResID:" + r.getResID();
		s = s + ";GuestID:" + r.getGuestID();
		s = s + ";Hotel:" + r.getShortName();
		s = s + ";Type:" + r.getRoomType();
					
		s = s + ";In:" +  MiscUtil.formatDate(r.getCheckInDate());
		s = s + ";Out:" + MiscUtil.formatDate(r.getCheckOutDate());
		
		s = s + ";Rate:" + r.getRate();
		
		return s;	
	}
	
	public static String getRecordText(Record r) {
		String s = "Reservation ID:" + r.getResID() + "\n";
		s = s + "GuestID:" + r.getGuestID() + "\n";
		s = s + "Hotel Short Name:" + r.getShortName() + "\n";
		s = s + "Room Type:" + r.getRoomType().toString() + "\n";
					
		s = s + "Check in Date:" + MiscUtil.formatDate(r.getCheckInDate()) + "\n";
		s = s + "Check out Date:" + MiscUtil.formatDate(r.getCheckOutDate()) + "\n";
		
		if (r.getRate()>0)
			s = s + "Rate:" + r.getRate() + "\n";
		
		return s;
		
	}
	
	public static common.ErrorAndLogMsg.ErrorCode convertErrorCode (
			client.webservice.ErrorCode err ) {
		return common.ErrorAndLogMsg.ErrorCode.values()
				[err.ordinal()];
		                                               
	}

	// get a date from input
	// return null if user input empty string
	static SimpleDate inputDate () {
		
		int tryCnt=0;
		
		Scanner keyboard = new Scanner(System.in);
		
		do {
			try {
				System.out.print ("(" + MiscUtil.getDateFormat() + "). Prese <Enter> to Cancel:");
				String s = keyboard.nextLine();
				
				if (s==null || s.isEmpty() ) return null;
				
				return MiscUtil.parseDate(s);//, new ParsePosition(0));
	
			} catch (ParseException e) {
				System.out.println ("Try again...");
			}
		} while (tryCnt++ < 5);
		
		return null;
	
	}

	/*static String dateToString (Date date) {
		return df.format(date);
	} */
	
	static Record inputOneRecord () {
		Scanner keyboard = new Scanner(System.in);

		Record r = new Record();
		
		// Input Guest ID
		System.out.print ("Guest ID:");
		r.setGuestID(keyboard.nextLine());
		if (r.getGuestID ()==null || r.getGuestID().isEmpty())
			return null;
		
		// Input short name
		System.out.print ("Hotel Short Name:");
		r.setShortName(keyboard.nextLine());
		if (r.getShortName()==null || r.getShortName().isEmpty())
			return null;
		
		// Input room type
		System.out.print ("Room type:");
		int i = 1;
		for (RoomType typ: RoomType.values()) {
			System.out.print (" " + (i++) + "-" + typ.toString() );
		}
		
		boolean valid = false;
		do {
			int typ = keyboard.nextInt();
			keyboard.nextLine(); // skip empty line after integer
	
			valid = typ>0 && typ<=RoomType.values().length;
			
			if (valid)
				r.setRoomType( RoomType.values() [typ-1] );
		} while (!valid);
		
		// Input check in date
		System.out.println ("Checkin Date:");
		
		valid = true;
		
		SimpleDate today =  new SimpleDate();
	    
		do {
			r.setCheckInDate (inputDate());
			if (r.getCheckInDate() == null) break;
			System.out.println ("Entered check in date:" + 
					MiscUtil.formatDate(r.getCheckInDate()));
			/*valid = !r.getCheckInDate().before(today);
			if (!valid)
				System.out.println ("Check in date not valid. Try again..\n");*/
		} while (!valid);
		
		if (r.getCheckInDate () ==null)
			return null;
		
		// Input check out date
		System.out.println ("Checkout Date:");
		valid = true;
		do {
			r.setCheckOutDate (inputDate());
			if (r.getCheckOutDate() ==null) break;
			System.out.println ("Entered check out date:" +
					MiscUtil.formatDate(r.getCheckOutDate()));
		} while (!valid);
		
		if (r.getCheckOutDate() == null)
			return null;
		
		System.out.println ("---------------\n");
		System.out.print (MiscUtil.getRecordText(r));
		System.out.println ("---------------\n");
	
		System.out.print ("Confirm (y/n):");
		String s = keyboard.nextLine();
		if (s.charAt(0) != 'y' && s.charAt(0) != 'Y')
			return null;
		
		return r;
	}
		
	public static SimpleDate getDateByDelta (SimpleDate dt, int delta) {
		
		common.HotelServerTypes.SimpleDate sd1 = 
				new common.HotelServerTypes.SimpleDate (
						dt.getYear(), dt.getMonth(), dt.getDay() );
		
		common.HotelServerTypes.SimpleDate d2 = 
				common.HotelServerTypes.SimpleDate.getDateByDelta(sd1, delta);
		
		SimpleDate ret = new SimpleDate ();
		ret.setYear(d2.getYear());
		ret.setMonth(d2.getMonth());
		ret.setDay(d2.getDay());
		
		return ret;
	}
	
	public static int compareDate (SimpleDate d1, SimpleDate d2) {
		common.HotelServerTypes.SimpleDate sd1 = 
				new common.HotelServerTypes.SimpleDate (
						d1.getYear(), d1.getMonth(), d1.getDay() );
		
		common.HotelServerTypes.SimpleDate sd2 = 
				new common.HotelServerTypes.SimpleDate (
						d2.getYear(), d2.getMonth(), d2.getDay() );
		
		return sd1.compareTo(sd2);
	}
	
	public static SimpleDate getToday() {
		SimpleDate today = new SimpleDate();
		Calendar cal = Calendar.getInstance();
		
		today.setYear(cal.get(Calendar.YEAR));
		today.setMonth(cal.get(Calendar.MONTH)+1);
		today.setDay(cal.get(Calendar.DAY_OF_MONTH));
		return today;
	}
}
