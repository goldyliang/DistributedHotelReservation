package CHotelServerInterface;


/**
* CHotelServerInterface/CListAvailabilityHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Friday, November 27, 2015 4:40:26 AM EST
*/

public final class CListAvailabilityHolder implements org.omg.CORBA.portable.Streamable
{
  public CHotelServerInterface.CAvailability value[] = null;

  public CListAvailabilityHolder ()
  {
  }

  public CListAvailabilityHolder (CHotelServerInterface.CAvailability[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = CHotelServerInterface.CListAvailabilityHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    CHotelServerInterface.CListAvailabilityHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return CHotelServerInterface.CListAvailabilityHelper.type ();
  }

}
