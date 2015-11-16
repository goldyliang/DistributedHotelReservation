package HotelServerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import miscutil.SimpleDate;
import HotelServerInterface.IHotelServer.Record;

public interface IHotelServerManager extends Remote {
	
	public Record[] getServiceReport (SimpleDate serviceDate) throws RemoteException;
	
	public Record[] getStatusReport (SimpleDate date) throws RemoteException;
	
}
