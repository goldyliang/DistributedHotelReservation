package CHotelServerInterface;


/**
* CHotelServerInterface/CListRecordHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Tuesday, October 27, 2015 12:11:22 PM EDT
*/

public final class CListRecordHolder implements org.omg.CORBA.portable.Streamable
{
  public CHotelServerInterface.CRecord value[] = null;

  public CListRecordHolder ()
  {
  }

  public CListRecordHolder (CHotelServerInterface.CRecord[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = CHotelServerInterface.CListRecordHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    CHotelServerInterface.CListRecordHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return CHotelServerInterface.CListRecordHelper.type ();
  }

}