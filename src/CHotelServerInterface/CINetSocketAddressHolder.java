package CHotelServerInterface;

/**
* CHotelServerInterface/CINetSocketAddressHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Friday, November 27, 2015 4:40:26 AM EST
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