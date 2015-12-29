package hotelbooking.serverinterface;

import java.util.Collection;

import hotelbooking.miscutil.SimpleDate;
import hotelbooking.serverinterface.IHotelServer.Record;

public interface IHotelServerManager{
	
	public Record[] getServiceReport (SimpleDate serviceDate);
	
	public Record[] getStatusReport (SimpleDate date);
	
	public Collection <Record> getReserveRecordSnapshot ();

	
}
