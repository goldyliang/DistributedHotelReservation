package hotelbooking.CorbaInterface;

/**
* CHotelServerInterface/CDateHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Sunday, December 6, 2015 9:58:24 PM EST
*/

public final class CDateHolder implements org.omg.CORBA.portable.Streamable
{
  public hotelbooking.CorbaInterface.CDate value = null;

  public CDateHolder ()
  {
  }

  public CDateHolder (hotelbooking.CorbaInterface.CDate initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = hotelbooking.CorbaInterface.CDateHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    hotelbooking.CorbaInterface.CDateHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return hotelbooking.CorbaInterface.CDateHelper.type ();
  }

}