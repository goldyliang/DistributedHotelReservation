package CHotelServerInterface;


/**
* CHotelServerInterface/CIHotelCentralAdminOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Sunday, December 6, 2015 9:58:24 PM EST
*/

public interface CIHotelCentralAdminOperations 
{
  int registerServer (CHotelServerInterface.CIHotelServer server, CHotelServerInterface.CINetSocketAddress socket);
  int unregisterServer (CHotelServerInterface.CIHotelServer server);
} // interface CIHotelCentralAdminOperations
