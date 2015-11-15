package HotelServerInterface;

import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IHotelCentralAdmin extends Remote {

	public boolean registerHotelQuerySocket(int port) throws RemoteException;
	
    public boolean unRegisterHotelQuerySocket(int port) throws RemoteException;
	
	public List<InetSocketAddress> getHotelQuerySockets () throws RemoteException;
}
