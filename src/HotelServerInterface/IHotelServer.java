package HotelServerInterface;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
//import java.util.Date;
import miscutil.SimpleDate;
import java.util.EnumMap;
import java.util.List;
import HotelServerInterface.ErrorAndLogMsg.ErrorCode;

public interface IHotelServer extends Remote {
	
	public enum RoomType {SINGLE, DOUBLE, FAMILY};
	
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
	
	@SuppressWarnings("serial")
	public static class Availability implements Serializable {
		/**
		 * 
		 */
		public String hotelName;
		public int availCount;
		public float rate;
		
		@Override
		public String toString() {
			return hotelName + " Count:" + availCount + "; Rate:" + rate;
		}
	}
	
	// Complete informatoin about a reservation
	@SuppressWarnings("serial")
	public static class Record implements Serializable {
		
		/**
		 * 
		 */
	    public int resID;
		public String guestID; 
		public String shortName;
		public RoomType roomType;
		public SimpleDate checkInDate; 
		public SimpleDate checkOutDate;
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
	
	public static class HotelProfile implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4211485720712591857L;
		public String shortName;
		public String fullName;
		public String descText;
		public EnumMap <RoomType, Integer> totalRooms;
		public int allTotalRooms;
		public EnumMap <RoomType, Float> rates;
	}
	
	public HotelProfile getProfile () throws RemoteException;
	
	public ErrorCode reserveRoom (
			String guestID, RoomType roomType, 
			SimpleDate checkInDate, SimpleDate checkOutDate, int resID) throws RemoteException;
	
	public ErrorCode cancelRoom (
			String guestID, RoomType roomType, 
			SimpleDate checkInDate, SimpleDate checkOutDate) throws RemoteException;
	
	public List<Availability> checkAvailability (
			String guestID, RoomType roomType,
			SimpleDate checkInDate, SimpleDate checkOutDate) throws RemoteException;
	
	public Record[] getReserveRecords (
			String guestID ) throws RemoteException;
	
	// If roomType, checkInDate, checkOutDate, is null, do not validate
	// If yes, validate the fields
	public ErrorCode transferRoom (
	        String guestID, int reservationID,
	        RoomType roomType,
	        SimpleDate checkInDate, SimpleDate checkOutDate,
	        String targetHotel,
	        int newResID);
	

	
	public IHotelServerManager getManagerObject (String guestID, String passWord) throws RemoteException;
}
