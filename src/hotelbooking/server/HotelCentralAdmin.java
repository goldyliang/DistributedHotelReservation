package hotelbooking.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import hotelbooking.serverinterface.IHotelCentralAdmin;

public class HotelCentralAdmin implements IHotelCentralAdmin {
	
	private ArrayList<InetSocketAddress> addresses;
	
	public HotelCentralAdmin() {
		addresses = new ArrayList<InetSocketAddress> (20);
	}
	

	
	//Export server to register server host:port, with name
	public void exportServer (String host, int port, String name) throws RemoteException {
		
		Remote obj = UnicastRemoteObject.exportObject(this,0);
		
		Registry r = LocateRegistry.getRegistry(host, port);
		
		r.rebind(name, obj);
	}
	
	@Override
	public boolean registerHotelQuerySocket(int port) throws RemoteException {
		
		String host = null;
		
		try {
			host = RemoteServer.getClientHost();
			
			InetAddress addr = InetAddress.getByName(host);
			
			InetSocketAddress sockAddr = new InetSocketAddress (addr, port);
			
			synchronized (addresses) {
			    boolean duplicated = false;
			    for (InetSocketAddress a: addresses) {
			        // compare to see whether it is duplicated
			        if (a.equals(sockAddr)) {
			            duplicated = true;
			            break;
			        }
			    }
				
			    if (!duplicated) addresses.add(sockAddr);
			}
			
			System.out.println("Server registered at query address " + host + ":" + port);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
   @Override
    public boolean unRegisterHotelQuerySocket(int port) throws RemoteException {
        
        String host = null;
        
        try {
            host = RemoteServer.getClientHost();
            
            InetAddress addr = InetAddress.getByName(host);
            
            InetSocketAddress sockAddr = new InetSocketAddress (addr, port);
            
            boolean found =false;
            synchronized (addresses) {
                Iterator<InetSocketAddress> it = addresses.iterator();
                
                while (it.hasNext()) {
                    InetSocketAddress a = it.next();
                    if (a.equals(sockAddr)) {
                        //found it. delete
                        it.remove();
                        found = true;
                        break;
                    }
                }
            }
            
            if (found)
                System.out.println("Server un-registered at query address " + host + ":" + port);
            else
                System.out.println("Un-register request invalid, address unfound " + host + ":" + port);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
        
    }
	
	@Override
	public List<InetSocketAddress> getHotelQuerySockets () throws RemoteException {
		
		String host = null;
		
		try {
			host = RemoteServer.getClientHost();
			
			System.out.println ("Server soket addresses sent to host " + host);
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		return addresses;
	}
	
	// for internal usage
	public List<InetSocketAddress> getSockets() {
		return addresses;
	}
	
	//Arg#1 The port number of the registry to start
	//Arg#2 The name of the admin object to export
	public static void main (String args[]) {
		
		if (args.length<=1)  {
			System.out.println ("Arg #1: port number of the registry to start");
			System.out.println ("Arg #2: the name that the admin object to export");
			return;
		}
		
		int port=0;
		
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println ("Arg #1: port number of the registry to start");
			System.out.println ("Arg #2: the name that the admin object to export");

			return;
		}
		
		HotelCentralAdmin admin = null;
		
		try {
		
			Registry reg = LocateRegistry.createRegistry( port );
			
			admin = new HotelCentralAdmin();
			Remote obj = UnicastRemoteObject.exportObject(admin,0);
			
			reg.rebind(args[1], obj);
			
		} catch (Exception e) {
			System.out.println ("Admin server started failure.");
			e.printStackTrace();
			return;
		}
		
		System.out.println ("Admin server started.");
		
		Scanner keyboard = new Scanner(System.in);

		while (true) {
			System.out.println ("Input p<Enter> to print registered server sockets.");
			System.out.println ("Input q<Enter> to quit:");
			
			String s = keyboard.nextLine();
			
			if (s.equals("q")) {
				keyboard.close();
				
				try {
				    UnicastRemoteObject.unexportObject(admin, true);
				} catch (NoSuchObjectException e) {
				    e.printStackTrace();
				}
				
				return;
			} else if (s.equals("p")) {
				System.out.println ("Registered query sockets:");
				for (InetSocketAddress addr:admin.getSockets()) {
					System.out.println (addr);
				}
			}
		}
	}
	
}
