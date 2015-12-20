package hotelbooking.serverinterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import hotelbooking.miscutil.SimpleDate;
import hotelbooking.serverinterface.IHotelServer.Record;

public interface IHotelServerManager extends Remote {
	
	public Record[] getServiceReport (SimpleDate serviceDate) throws RemoteException;
	
	public Record[] getStatusReport (SimpleDate date) throws RemoteException;
	
	public Collection <Record> getReserveRecordSnapshot ();

	
}
