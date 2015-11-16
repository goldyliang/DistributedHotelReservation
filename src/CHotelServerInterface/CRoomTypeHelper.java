package CHotelServerInterface;


/**
* CHotelServerInterface/CRoomTypeHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Tuesday, October 27, 2015 12:11:22 PM EDT
*/


// And a convertion utilitity is designed to convert them back and forth
abstract public class CRoomTypeHelper
{
  private static String  _id = "IDL:CHotelServerInterface/CRoomType:1.0";

  public static void insert (org.omg.CORBA.Any a, CHotelServerInterface.CRoomType that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static CHotelServerInterface.CRoomType extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (CHotelServerInterface.CRoomTypeHelper.id (), "CRoomType", new String[] { "ROOM_SINGLE", "ROOM_DOUBLE", "ROOM_FAMILY"} );
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static CHotelServerInterface.CRoomType read (org.omg.CORBA.portable.InputStream istream)
  {
    return CHotelServerInterface.CRoomType.from_int (istream.read_long ());
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, CHotelServerInterface.CRoomType value)
  {
    ostream.write_long (value.value ());
  }

}