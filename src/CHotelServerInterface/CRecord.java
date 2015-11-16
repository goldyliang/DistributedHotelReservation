package CHotelServerInterface;


/**
* CHotelServerInterface/CRecord.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Tuesday, October 27, 2015 12:11:22 PM EDT
*/

public final class CRecord implements org.omg.CORBA.portable.IDLEntity
{
  public int reserveID = (int)0;
  public String guestID = null;
  public String hotelName = null;
  public CHotelServerInterface.CRoomType roomType = null;
  public CHotelServerInterface.CDate checkInDate = null;
  public CHotelServerInterface.CDate checkOutDate = null;
  public float rate = (float)0;

  public CRecord ()
  {
  } // ctor

  public CRecord (int _reserveID, String _guestID, String _hotelName, CHotelServerInterface.CRoomType _roomType, CHotelServerInterface.CDate _checkInDate, CHotelServerInterface.CDate _checkOutDate, float _rate)
  {
    reserveID = _reserveID;
    guestID = _guestID;
    hotelName = _hotelName;
    roomType = _roomType;
    checkInDate = _checkInDate;
    checkOutDate = _checkOutDate;
    rate = _rate;
  } // ctor

} // class CRecord
