package CHotelServerInterface;


/**
* CHotelServerInterface/CIHotelServerManagerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CHotelServerInterface.idl
* Tuesday, October 27, 2015 12:11:22 PM EDT
*/

public interface CIHotelServerManagerOperations 
{
  int getServiceReport (CHotelServerInterface.CDate serviceDate, CHotelServerInterface.CListRecordHolder records);
  int getStatusReport (CHotelServerInterface.CDate serviceDate, CHotelServerInterface.CListRecordHolder records);
} // interface CIHotelServerManagerOperations
