package CHotelServerInterface;

/**
* CHotelServerInterface/CIHotelCentralAdminHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Friday, November 27, 2015 4:40:26 AM EST
*/

public final class CIHotelCentralAdminHolder implements org.omg.CORBA.portable.Streamable
{
  public CHotelServerInterface.CIHotelCentralAdmin value = null;

  public CIHotelCentralAdminHolder ()
  {
  }

  public CIHotelCentralAdminHolder (CHotelServerInterface.CIHotelCentralAdmin initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = CHotelServerInterface.CIHotelCentralAdminHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    CHotelServerInterface.CIHotelCentralAdminHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return CHotelServerInterface.CIHotelCentralAdminHelper.type ();
  }

}