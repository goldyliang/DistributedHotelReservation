package CHotelServerInterface;

/**
* CHotelServerInterface/CIHotelServerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Sunday, December 6, 2015 9:58:24 PM EST
*/

public final class CIHotelServerHolder implements org.omg.CORBA.portable.Streamable
{
  public CHotelServerInterface.CIHotelServer value = null;

  public CIHotelServerHolder ()
  {
  }

  public CIHotelServerHolder (CHotelServerInterface.CIHotelServer initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = CHotelServerInterface.CIHotelServerHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    CHotelServerInterface.CIHotelServerHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return CHotelServerInterface.CIHotelServerHelper.type ();
  }

}
