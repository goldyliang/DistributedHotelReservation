package hotelbooking.CorbaInterface;


/**
* CHotelServerInterface/CIHotelCentralAdminHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Sunday, December 6, 2015 9:58:24 PM EST
*/

abstract public class CIHotelCentralAdminHelper
{
  private static String  _id = "IDL:CHotelServerInterface/CIHotelCentralAdmin:1.0";

  public static void insert (org.omg.CORBA.Any a, hotelbooking.CorbaInterface.CIHotelCentralAdmin that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static hotelbooking.CorbaInterface.CIHotelCentralAdmin extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (hotelbooking.CorbaInterface.CIHotelCentralAdminHelper.id (), "CIHotelCentralAdmin");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static hotelbooking.CorbaInterface.CIHotelCentralAdmin read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_CIHotelCentralAdminStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, hotelbooking.CorbaInterface.CIHotelCentralAdmin value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static hotelbooking.CorbaInterface.CIHotelCentralAdmin narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof hotelbooking.CorbaInterface.CIHotelCentralAdmin)
      return (hotelbooking.CorbaInterface.CIHotelCentralAdmin)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      hotelbooking.CorbaInterface._CIHotelCentralAdminStub stub = new hotelbooking.CorbaInterface._CIHotelCentralAdminStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static hotelbooking.CorbaInterface.CIHotelCentralAdmin unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof hotelbooking.CorbaInterface.CIHotelCentralAdmin)
      return (hotelbooking.CorbaInterface.CIHotelCentralAdmin)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      hotelbooking.CorbaInterface._CIHotelCentralAdminStub stub = new hotelbooking.CorbaInterface._CIHotelCentralAdminStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}