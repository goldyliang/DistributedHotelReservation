package serverws;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import server.ErrorAndLogMsg;
import server.HotelServer;
import server.ErrorAndLogMsg.ErrorCode;
import server.DataTypes.*;

//Wrapper class for HotelServer Webservice

@RestController
public class HotelServerWS {

    HotelServer server;
    
    public HotelServerWS () {
    	System.out.println("Construction");
    }
    
	@Autowired
    public HotelServerWS (HotelServer server) {
        this.server = server;
    }
    
    @RequestMapping(value="/profile", method={RequestMethod.GET})
	public HotelProfile getProfile () {
    	HotelProfile profile = server.getProfile();
    	
		return profile;
				//server.getProfile();
	}
	
    @RequestMapping(value="/reserve", method={RequestMethod.POST})
	public GeneralReturn reserveRoom (
			@RequestBody Record input) {
    	
    	String guestID;
    	RoomType roomType;
    	SimpleDate checkInDate, checkOutDate;
    	
    	try {
	    	guestID = input.guestID;
	    	roomType = input.roomType;
	    	checkInDate = input.checkInDate;
	    	checkOutDate = input.checkOutDate;
	
	    	System.out.println("GuestID" + guestID);
	    	System.out.println("RoomType" + roomType);
	    	System.out.println("In:" + checkInDate);
	    	System.out.println("Oout:" + checkOutDate);
    	} catch (Exception e) {
    		//e.printStackTrace();
    		return new GeneralReturn (ErrorCode.INVALID_REQUEST, 0);
    	}
    	
    	int [] id = new int[1];
    	
    	ErrorCode error = server.reserveRoom(
    			guestID, 
    			roomType, 
    			checkInDate, 
    			checkOutDate, 
    			id);
    	
    	GeneralReturn ret = new GeneralReturn (error, id[0]);


    	return ret; 
	}
	
    @RequestMapping(value="/cancel", method={RequestMethod.POST})
	public GeneralReturn cancelRoom (
			@RequestBody Record input) {
    	String guestID;
    	RoomType roomType;
    	SimpleDate checkInDate, checkOutDate;
    	
    	try {
	    	guestID = input.guestID;
	    	roomType = input.roomType;
	    	checkInDate = input.checkInDate;
	    	checkOutDate = input.checkOutDate;
    	} catch (Exception e) {
    		//e.printStackTrace();
    		return new GeneralReturn (ErrorCode.INVALID_REQUEST, 0);
    	}
    	
    	ErrorCode error = server.cancelRoom(
    			guestID, 
    			roomType, 
    			checkInDate, 
    			checkOutDate);
    	GeneralReturn ret = new GeneralReturn (error,0);
    	return ret;
    }
    
    @RequestMapping(value="/avail", method={RequestMethod.GET})
	public AvailabilityReturn checkAvailability (
			//String guestID, 
			@RequestParam("type") RoomType roomType,
			@RequestParam("in") String sCheckInDate, 
			@RequestParam("out") String sCheckOutDate) {
    	
    	SimpleDate checkInDate, checkOutDate ;
    	AvailabilityReturn ret = new AvailabilityReturn();

		try {
			checkInDate = SimpleDate.parse(sCheckInDate);
			checkOutDate = SimpleDate.parse(sCheckOutDate);
		} catch (ParseException e) {
			ErrorAndLogMsg.ExceptionErr(e, "Wrong dates.").printMsg();
			
	    	ret.error = ErrorCode.INVALID_DATES;

			return ret;
		}
    	
    	List <Availability> list = server.checkAvailability(
    			"", 
    			roomType,
    			checkInDate, 
    			checkOutDate);
		
    	ret.error = ErrorCode.SUCCESS;
    	ret.avails = list;
    	
    	return ret;
	}
	
    @RequestMapping(value="/records", method={RequestMethod.GET})
	public ListRecordReturn getReserveRecords (
			@RequestParam("id") String guestID ) {
    	
    	Record[] records = server.getReserveRecords(guestID);
    	ListRecordReturn ret = new ListRecordReturn();
    	ret.error = ErrorCode.SUCCESS;
    	ret.listRecord = new ArrayList <Record> ();
    	ret.listRecord.addAll(Arrays.asList(records));
    	
    	return ret;
    }

	// TODO: not supported for now
	/*public GeneralReturn transferRoom (
	        String guestID, int reservationID,
	        RoomType roomType,
	        SimpleDate checkInDate, SimpleDate checkOutDate,
	        String targetHotel ) {
    	
    	int[] newResID = new int[1];
    	
    	ErrorCode error = server.transferRoom(
    			guestID, 
    			reservationID, 
    			roomType, 
    			checkInDate, 
    			checkOutDate, 
    			targetHotel, 
    			newResID);
    	
    	GeneralReturn ret = new GeneralReturn (error,newResID[0]);

    	return ret;
    	
    } */
    
    /* if successfull login, return a token ID
     * Otherwise, return -1
     */
    @RequestMapping(value="/mgrlogin", method={RequestMethod.POST})
    public long loginAsManager (@RequestParam String user, @RequestParam String pass) {
    	
    	long res = server.loginAsManager(user, pass);
    	return res;
    	
    }
    
    /* the token ID is to be compared with the generated token ID
     * return null if login token ID is wrong
     */
    @RequestMapping(value="/servicereport", method={RequestMethod.GET})
	public ListRecordReturn getServiceReport (
			//@RequestParam long token, 
			@RequestParam("date") String serviceDate) {
    	
    	SimpleDate date;
		try {
			date = SimpleDate.parse(serviceDate);
		} catch (ParseException e) {
			ErrorAndLogMsg.ExceptionErr(e, "Wrong date.").printMsg();
			
	    	ListRecordReturn ret = new ListRecordReturn(ErrorCode.INVALID_DATES);

			return ret;
		}
    	
    	List <Record> list = new ArrayList <Record> ();
    	
    	ErrorCode error = server.getServiceReport(0, date, list);
    	
    	ListRecordReturn ret = new ListRecordReturn(error);
    	ret.listRecord = list;
    	
    	return ret;
    }
	
    @RequestMapping(value="/statusreport", method={RequestMethod.GET})
	public ListRecordReturn getStatusReport (
			//long token,
			@RequestParam("date") String statusDate) {

    	SimpleDate date;
		try {
			date = SimpleDate.parse(statusDate);
		} catch (ParseException e) {
			ErrorAndLogMsg.ExceptionErr(e, "Wrong date.").printMsg();
			
	    	ListRecordReturn ret = new ListRecordReturn(ErrorCode.INVALID_DATES);

			return ret;
		}
		
    	List <Record> list = new ArrayList <Record> ();
    	
    	ErrorCode error = server.getStatusReport(0, date, list);
    	
    	ListRecordReturn ret = new ListRecordReturn(error);
    	ret.listRecord = list;
    	
    	return ret;
    }
    
	

	
	/*public IHotelServerManager getManagerObject (String guestID, String passWord) ; */
    

}
