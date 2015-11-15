package HotelServerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

import HotelServerInterface.IHotelServer.Record;

public interface IHotelServerManager extends Remote {
	
	public Record[] getServiceReport (Date serviceDate) throws RemoteException;
	
	public Record[] getStatusReport (Date date) throws RemoteException;
	
}
