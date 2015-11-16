package serverws;


import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import HotelServerInterface.IHotelServer;
import common.ErrorAndLogMsg.ErrorCode;
import common.HotelServerTypes.*;

//Wrapper class for HotelServer Webservice

@WebService
public class HotelServerWS {

    IHotelServer server;
    
    public HotelServerWS (IHotelServer server) {
        this.server = server;
    }
    
    @WebMethod
	public HotelProfile getProfile () {
		return server.getProfile();
	}
	
    @WebMethod
	public GeneralReturn reserveRoom (
			String guestID, RoomType roomType, 
			SimpleDate checkInDate, SimpleDate checkOutDate) {
		
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
	
	
    @WebMethod
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
    
    @WebMethod
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
	
    @WebMethod
	public Record[] getReserveRecords (
			String guestID ) {
    	return null;
    }
	
    @WebMethod
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
    @WebMethod
    public long loginAsManager (String user, String pass) {
    	
    	return server.loginAsManager(user, pass);
    	
    }
    
    /* the token ID is to be compared with the generated token ID
     * return null if login token ID is wrong
     */
    @WebMethod
	public ManagerReturn getServiceReport (long token, SimpleDate serviceDate) {
    	
    	List <Record> list = new ArrayList <Record> ();
    	
    	ErrorCode error = server.getServiceReport(token, serviceDate, list);
    	
    	ManagerReturn ret = new ManagerReturn(error);
    	ret.listRecord = list;
    	
    	return ret;
    }
	
    @WebMethod
	public ManagerReturn getStatusReport (long token, SimpleDate statusDate) {
    	
    	List <Record> list = new ArrayList <Record> ();
    	
    	ErrorCode error = server.getStatusReport(token, statusDate, list);
    	
    	ManagerReturn ret = new ManagerReturn(error);
    	ret.listRecord = list;
    	
    	return ret;
    }
    
	

	
	/*public IHotelServerManager getManagerObject (String guestID, String passWord) ; */
    

}
