package CHotelServerInterface;

/**
* CHotelServerInterface/CHotelProfileHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Tuesday, October 27, 2015 12:11:22 PM EDT
*/

public final class CHotelProfileHolder implements org.omg.CORBA.portable.Streamable
{
  public CHotelServerInterface.CHotelProfile value = null;

  public CHotelProfileHolder ()
  {
  }

  public CHotelProfileHolder (CHotelServerInterface.CHotelProfile initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = CHotelServerInterface.CHotelProfileHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    CHotelServerInterface.CHotelProfileHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return CHotelServerInterface.CHotelProfileHelper.type ();
  }

}