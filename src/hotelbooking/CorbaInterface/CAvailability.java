package hotelbooking.CorbaInterface;


/**
* CHotelServerInterface/CAvailability.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Sunday, December 6, 2015 9:58:24 PM EST
*/

public final class CAvailability implements org.omg.CORBA.portable.IDLEntity
{
  public String hotelName = null;
  public short availCount = (short)0;
  public float rate = (float)0;

  public CAvailability ()
  {
  } // ctor

  public CAvailability (String _hotelName, short _availCount, float _rate)
  {
    hotelName = _hotelName;
    availCount = _availCount;
    rate = _rate;
  } // ctor

} // class CAvailability