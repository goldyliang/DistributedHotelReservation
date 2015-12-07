package CHotelServerInterface;


/**
* CHotelServerInterface/CListAvailabilityHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Sunday, December 6, 2015 9:58:24 PM EST
*/

abstract public class CListAvailabilityHelper
{
  private static String  _id = "IDL:CHotelServerInterface/CListAvailability:1.0";

  public static void insert (org.omg.CORBA.Any a, CHotelServerInterface.CAvailability[] that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static CHotelServerInterface.CAvailability[] extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = CHotelServerInterface.CAvailabilityHelper.type ();
      __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
      __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (CHotelServerInterface.CListAvailabilityHelper.id (), "CListAvailability", __typeCode);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static CHotelServerInterface.CAvailability[] read (org.omg.CORBA.portable.InputStream istream)
  {
    CHotelServerInterface.CAvailability value[] = null;
    int _len0 = istream.read_long ();
    value = new CHotelServerInterface.CAvailability[_len0];
    for (int _o1 = 0;_o1 < value.length; ++_o1)
      value[_o1] = CHotelServerInterface.CAvailabilityHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, CHotelServerInterface.CAvailability[] value)
  {
    ostream.write_long (value.length);
    for (int _i0 = 0;_i0 < value.length; ++_i0)
      CHotelServerInterface.CAvailabilityHelper.write (ostream, value[_i0]);
  }

}
