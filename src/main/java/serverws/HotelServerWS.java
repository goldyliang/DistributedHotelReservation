package serverws;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import HotelServerInterface.IHotelServer;
import common.ErrorAndLogMsg.ErrorCode;
import common.HotelServerTypes.*;

//Wrapper class for HotelServer Webservice

@RestController
public class HotelServerWS {

	@Autowired
    IHotelServer server;
    
    public HotelServerWS () {
    	System.out.println("Construction");
    }
    
    public HotelServerWS (IHotelServer server) {
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
    		e.printStackTrace();
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
	
	
	public GeneralReturn cancelRoom (
			String guestID, RoomType roomType, 
			SimpleDate checkInDate, SimpleDate checkOutDate) {
    	
    	ErrorCode error = server.cancelRoom(
    			guestID, 
    			roomType, 
    			checkInDate, 
    			checkOutDate);
    	GeneralReturn ret = new GeneralReturn (error,0);
    	return ret;
    }
    
	public Availability[] checkAvailability (
			String guestID, RoomType roomType,
			SimpleDate checkInDate, SimpleDate checkOutDate) {
    	
    	List <Availability> list = server.checkAvailability(
    			guestID, 
    			roomType,
    			checkInDate, 
    			checkOutDate);
		
    	return list.toArray(new Availability[0]);
	}
	
	public Record[] getReserveRecords (
			String guestID ) {
    	return null;
    }
	
	public GeneralReturn transferRoom (
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
    	
    }
    
    /* if successfull login, return a token ID
     * Otherwise, return -1
     */
    @RequestMapping(value="/mgrlogin", method={RequestMethod.POST})
    public long loginAsManager (@RequestParam String user, @RequestParam String pass) {
    	
    	return server.loginAsManager(user, pass);
    	
    }
    
    /* the token ID is to be compared with the generated token ID
     * return null if login token ID is wrong
     */
    
    @RequestMapping(value="/servicereport", method={RequestMethod.GET})
	public ManagerReturn getServiceReport (
			@RequestParam long token, 
			@RequestParam String serviceDate) {
    	
    	SimpleDate date;
		try {
			date = SimpleDate.parse(serviceDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	
    	System.out.println("Token:"+token);
    	System.out.println("date:" + date);
    	List <Record> list = new ArrayList <Record> ();
    	
    	ErrorCode error = server.getServiceReport(token, date, list);
    	
    	ManagerReturn ret = new ManagerReturn(error);
    	ret.listRecord = list;
    	
    	return ret;
    }
	
	public ManagerReturn getStatusReport (long token, SimpleDate statusDate) {
    	
    	List <Record> list = new ArrayList <Record> ();
    	
    	ErrorCode error = server.getStatusReport(token, statusDate, list);
    	
    	ManagerReturn ret = new ManagerReturn(error);
    	ret.listRecord = list;
    	
    	return ret;
    }
    
	

	
	/*public IHotelServerManager getManagerObject (String guestID, String passWord) ; */
    

}
