package CHotelServerInterface;

/**
* CHotelServerInterface/CINetSocketAddressHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Tuesday, October 27, 2015 12:11:22 PM EDT
*/

public final class CINetSocketAddressHolder implements org.omg.CORBA.portable.Streamable
{
  public CHotelServerInterface.CINetSocketAddress value = null;

  public CINetSocketAddressHolder ()
  {
  }

  public CINetSocketAddressHolder (CHotelServerInterface.CINetSocketAddress initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = CHotelServerInterface.CINetSocketAddressHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    CHotelServerInterface.CINetSocketAddressHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return CHotelServerInterface.CINetSocketAddressHelper.type ();
  }

}
