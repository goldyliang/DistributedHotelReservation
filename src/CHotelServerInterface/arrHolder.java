package CHotelServerInterface;


/**
* CHotelServerInterface/arrHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Monday, October 26, 2015 3:29:58 PM EDT
*/

public final class arrHolder implements org.omg.CORBA.portable.Streamable
{
  public CHotelServerInterface.CAvailability value[] = null;

  public arrHolder ()
  {
  }

  public arrHolder (CHotelServerInterface.CAvailability[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = CHotelServerInterface.arrHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    CHotelServerInterface.arrHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return CHotelServerInterface.arrHelper.type ();
  }

}