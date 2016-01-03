package server;

import java.util.List;

import server.ErrorAndLogMsg.ErrorCode;
import server.DataTypes.*;

public interface HotelServer {
	
	public HotelProfile getProfile () ;
	
	public ErrorCode reserveRoom (
			String guestID, RoomType roomType, 
			SimpleDate checkInDate, SimpleDate checkOutDate, int[] resID) ;
	
	public ErrorCode cancelRoom (
			String guestID, RoomType roomType, 
			SimpleDate checkInDate, SimpleDate checkOutDate) ;
	
	public List<Availability> checkAvailability (
			String guestID, RoomType roomType,
			SimpleDate checkInDate, SimpleDate checkOutDate) ;
	
	public Record[] getReserveRecords (
			String guestID ) ;
	
	public ErrorCode transferRoom (
	        String guestID, int reservationID,
	        RoomType roomType,
	        SimpleDate checkInDate, SimpleDate checkOutDate,
	        String targetHotel,
	        int[] newResID);
	

    public long loginAsManager (String managerID, String passWord);
    
    public ErrorCode getServiceReport (
    		long token, SimpleDate serviceDate,
    		List <Record> list);
    
    public ErrorCode getStatusReport (
    		long token, SimpleDate serviceDate,
    		List <Record> list);

}
