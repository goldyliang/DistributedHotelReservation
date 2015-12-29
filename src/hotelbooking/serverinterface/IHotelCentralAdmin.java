package hotelbooking.serverinterface;

import java.net.InetSocketAddress;
import java.util.List;

public interface IHotelCentralAdmin{

	public boolean registerHotelQuerySocket(int port);
	
    public boolean unRegisterHotelQuerySocket(int port);
	
	public List<InetSocketAddress> getHotelQuerySockets ();
}
