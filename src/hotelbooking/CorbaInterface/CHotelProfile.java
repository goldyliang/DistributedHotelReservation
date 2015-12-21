package hotelbooking.CorbaInterface;


/**
* CHotelServerInterface/CHotelProfile.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Sunday, December 6, 2015 9:58:24 PM EST
*/

public final class CHotelProfile implements org.omg.CORBA.portable.IDLEntity
{
  public String shortName = null;
  public String fullName = null;
  public String descText = null;
  public short totalRooms[] = null;
  public float rates[] = null;

  public CHotelProfile ()
  {
  } // ctor

  public CHotelProfile (String _shortName, String _fullName, String _descText, short[] _totalRooms, float[] _rates)
  {
    shortName = _shortName;
    fullName = _fullName;
    descText = _descText;
    totalRooms = _totalRooms;
    rates = _rates;
  } // ctor

} // class CHotelProfile