package CHotelServerInterface;


/**
* CHotelServerInterface/CIHotelCentralAdminOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Tuesday, October 27, 2015 12:11:22 PM EDT
*/

public interface CIHotelCentralAdminOperations 
{
  int registerServer (CHotelServerInterface.CIHotelServer server, CHotelServerInterface.CINetSocketAddress socket);
  int unregisterServer (CHotelServerInterface.CIHotelServer server);
} // interface CIHotelCentralAdminOperations