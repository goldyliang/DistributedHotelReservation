package CHotelServerInterface;


/**
* CHotelServerInterface/IHotelCentralAdminOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Monday, October 26, 2015 6:38:21 PM EDT
*/

public interface IHotelCentralAdminOperations 
{
  int registerServer (CHotelServerInterface.CIHotelServer server, CHotelServerInterface.CINetSocketAddress socket);
  int unregisterServer (CHotelServerInterface.CIHotelServer server);
} // interface IHotelCentralAdminOperations