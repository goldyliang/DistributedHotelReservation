package Client;

import java.io.PrintStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;

import HotelServer.HotelServer;
import HotelServerInterface.*;
import HotelServerInterface.ErrorAndLogMsg.MsgType;
import HotelServerInterface.IHotelServer.*;
import HotelServerInterface.ErrorAndLogMsg.ErrorCode;


public class HotelClient {

    Registry reg;
    
    
    public class HotelServerWrapper {
        public IHotelServer server;
        public IHotelServerManager serverMgr;
        public HotelProfile prof;
    }
        
    private Map <String, HotelServerWrapper> servers;
    
    Thread thread=null;
    private final static int HOTEL_SERVER_SCAN_PERIOD = 2;
    
    int serverCnt=0;
    
    //host:port, the rmiregistry url
    public HotelClient () {
        super();
    }
    
    public Iterator <HotelServerWrapper> getServerIterator () {
        return servers.values().iterator();
    }
    
    public ErrorAndLogMsg Initialize (String host, int port) {
        
        try {
            reg = LocateRegistry.getRegistry(host, port);
        } catch (RemoteException e) {
            return ErrorAndLogMsg.ExceptionErr (e, 
                    "Remote RMI registry " + host + ":" + port + " could not be achieved.");
        }
        
        servers = new TreeMap<String, HotelServerWrapper> ();
        
        ErrorAndLogMsg.InfoMsg("Hotel Client initialized.").printMsg();
        
        ErrorAndLogMsg m = getHotelServerObjects();
        
        return m;
    }
    
    //work silently (a new thread) in background to periodically scan and retrieve new
    //server objects, or delete unregistered server objects
    //return info describing the changes (if any), or return error info
    ErrorAndLogMsg startHotelServerBackgroundScan () {
        
        try {
            thread =  new Thread() {
                public void run() {
                    
                    while (true) {
                        if (this.isInterrupted())
                            return;
                        
                        ErrorAndLogMsg m = getHotelServerObjects ();
                        m.printMsg();

                        
                        try {
                            Thread.sleep( HOTEL_SERVER_SCAN_PERIOD * 60 * 1000); // Update per 15 minutes
                        } catch (InterruptedException e) {
                            return;
                        }
                        
                        
                    }
                }

            };
            
            thread.start();
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, 
                    "HotelServer scanner start error.");
        }
        
        return ErrorAndLogMsg.InfoMsg("Background thread of Server update created.");
    }
    
    ErrorAndLogMsg stopHotelServerBackgroundScan () {
        if (thread!=null)
            thread.interrupt();
        
        return null;
    }

    
    // return null if the object is already valid and no update required
    // return Log if a server is updated/added
    // return Err if any error
    ErrorAndLogMsg getSingleHotelServerObject (String name, boolean reGet) {
        try {
            
            HotelServerWrapper srv;
            
            synchronized (servers) { srv = servers.get(name); }
            
            if (srv==null || reGet) {
                // new name
                Remote stb = reg.lookup(name);
                
                IHotelServer server = null;
                
                if (stb instanceof IHotelServer) {
                
                    try {
                        server = (IHotelServer) stb;
                    } catch (ClassCastException e) {
                        // not a right class, just ignore it
                    }
                    
                    if (server!=null) {
                        srv = new HotelServerWrapper();
                        srv.server = server;
                        srv.prof = server.getProfile();                        
                        
                        synchronized (servers) { servers.put(name, srv); }
                    } 
                }
                
                return ErrorAndLogMsg.LogMsg("Server object retrieved/updated.");
                
            } else {
                // old name
                // Try to get the profile and simply update
                srv.prof = srv.server.getProfile();
                return null;
            }
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, 
                    "Error when instance and remote object or get update of an remote object.");
        }
    }
    
    //Query rmiregistry to get all remote server objects
    //If server objects already exist, this method update and add all new objects,
    //and delete those do not exist any more.
    //return number of new servers
    ErrorAndLogMsg getHotelServerObjects () {
        
/*        try {
            Context ictx = new InitialContext();
            Context registryCtx = (Context)ictx.lookup("rmi://localhost:1099");
            
            NamespaceChangeListener listener = new NamespaceChangeListener () {
                public void objectAdded(NamingEvent evt) {
                    System.out.println (evt.getOldBinding().toString());
                    System.out.println (evt.getNewBinding().toString());
                }
                
                public void objectRemoved (NamingEvent evt) {
                    System.out.println (evt.getOldBinding().toString());
                    System.out.println (evt.getNewBinding().toString());
                }
                
                public void objectRenamed (NamingEvent evt) {
                    System.out.println (evt.getOldBinding().toString());
                    System.out.println (evt.getNewBinding().toString());
                }

                @Override
                public void namingExceptionThrown(NamingExceptionEvent evt) {
                    // TODO Auto-generated method stub
                    evt.getException().printStackTrace();
                }
            };
            
            EventContext evCtx = (EventContext) registryCtx; // new EventContext();
            evCtx.addNamingListener(registryCtx.getNameInNamespace(), EventContext.ONELEVEL_SCOPE, listener);
            
            NamingEnumeration<Binding> bindings = registryCtx.listBindings(registryCtx.getNameInNamespace());
            
            while (bindings.hasMore()) {
                Binding b = bindings.next();
                
                System.out.println (b.getName());
                System.out.println (b.getClassName());
                
            }
        NamingEnumeration<NameClassPair> name_classes = registryCtx.list( registryCtx.getNameInNamespace());
            
            while (name_classes.hasMore()) {
                NameClassPair name_class = name_classes.next();
                System.out.println (name_class.getName());
                System.out.println (name_class.getClassName());
            } 
        } catch (NamingException e) {
            return ErrorAndLogMsg.ExceptionErr(e, "Exception when getting/updatting hotel server objects");
        }

        return null; */
        
        String[] nameList = null;
        
        try {
            nameList = reg.list();
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, 
                    "Registry name list failure.");
        }                
    
        int new_servers = 0;
        
        for (String name:nameList) {
            
            ErrorAndLogMsg m = getSingleHotelServerObject (name, false); // no re-get
            
            if (m!=null && !m.anyError())
                new_servers ++;
        }
                    
        serverCnt += new_servers;
        
        return ErrorAndLogMsg.InfoMsg("Added servers:" + new_servers); 
    }
    
    public ErrorAndLogMsg reserveHotel (
            String guestID,
            String hotelName,
            RoomType type,
            Date checkInDate,
            Date checkOutDate) {
        
        HotelServerWrapper server_wrap = servers.get(hotelName);
        
        if (server_wrap==null) {
            // try to get the server object
            ErrorAndLogMsg m = getSingleHotelServerObject (hotelName, true);
            
            if (m==null || !m.anyError()) {
                server_wrap = servers.get(hotelName);
                
                if (server_wrap != null)
                    ErrorAndLogMsg.LogMsg("New server retrieved: " + hotelName).printMsg();
            }
        }
        
        if (server_wrap ==null) {
            // can not get the object, return error
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                "No hotel server found by name " + hotelName); // Hotel not found
        }
            
        IHotelServer server = server_wrap.server;
        
        try {
            
            String sRec = new Record(guestID, hotelName, type, checkInDate, checkOutDate, 0).toOneLineString();
            
            ErrorCode err = server.reserveRoom(guestID, type, checkInDate, checkOutDate);
            
            if (err == ErrorCode.SUCCESS) {
                ErrorAndLogMsg.LogMsg("Reserve success: " + sRec).printMsg();
                return null;
            } else {
                ErrorAndLogMsg.LogMsg("Reserve failure " + err.toString() + ":" + sRec ).printMsg();
                return ErrorAndLogMsg.GeneralErr(err, "Error invoking reserveRoom");
            }
            
        } catch (RemoteException e) {
            ErrorAndLogMsg m = ErrorAndLogMsg.ExceptionErr(e, "Remote Exception when invoking reserveRoom");
            m.printMsg();
            
            // Try to retrieve server object again and retry
            ErrorAndLogMsg m1 = getSingleHotelServerObject(hotelName, true);
            
            if (m1!=null && !m1.anyError())
                return reserveHotel( guestID, hotelName, type, checkInDate, checkOutDate);
            else 
                return m;
        }
    }
    
    public ErrorAndLogMsg cancelHotel (
            String guestID,
            String hotelName,
            RoomType type,
            Date checkInDate,
            Date checkOutDate) {
        
        HotelServerWrapper server_wrap = servers.get(hotelName);
        
        if (server_wrap==null) 
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "No hotel server found by name " + hotelName); // Hotel not found
        
        IHotelServer server = server_wrap.server;
        
        try {
            
            String sRec = new Record(guestID, hotelName, type, checkInDate, checkOutDate, 0).toOneLineString();

            ErrorCode err = server.cancelRoom(guestID, type, checkInDate, checkOutDate);
            
            if (err == ErrorCode.SUCCESS) {
                ErrorAndLogMsg.LogMsg("Cancel success: " + sRec).printMsg();
                return null;
            } else {
                ErrorAndLogMsg.LogMsg("Cancel failure " + err.toString() + ":" + sRec).printMsg();
                return ErrorAndLogMsg.GeneralErr(err, "Error invoking cancelRoom");
            }
            
        } catch (RemoteException e) {
            return ErrorAndLogMsg.ExceptionErr(e, "Remote Exception when invoking cancelRoom");
        }
    }
    
    public ErrorAndLogMsg checkAvailability (Record rec, Collection <Availability> avails) {
        
        HotelServerWrapper server_wrap = servers.get(rec.shortName);
        
        if (server_wrap==null) 
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "No hotel server found by name " + rec.shortName); // Hotel not found
        
        IHotelServer server = server_wrap.server;
        
        try {
            List <Availability> avList = server.checkAvailability(
                    rec.guestID,rec.roomType, rec.checkInDate, rec.checkOutDate);
            
            for (Availability av : avList)
                avails.add(av);
            
            ErrorAndLogMsg.LogMsg("Query avail success:" + rec.toOneLineString()).printMsg();
            
            return null;
        } catch (RemoteException e) {
            return ErrorAndLogMsg.ExceptionErr(e, "Remote Exception when invoking reserveRoom");
        }
    }
    
    public int getServerCnt () {
        return serverCnt;
    }
    
    public ErrorAndLogMsg getReserveRecords (String guestID, Collection <Record> records) {
        
        Collection <HotelServerWrapper> server_wrappers;
        
        records.clear();
        
        synchronized (servers) {
            server_wrappers = servers.values();
        }
        
        Iterator <HotelServerWrapper> iter = server_wrappers.iterator();
        
        while (iter.hasNext()) {
            HotelServerWrapper srv_wrapper = iter.next();
            IHotelServer srv = srv_wrapper.server;
            Record [] recs;
            
            try {
                recs = srv.getReserveRecords(guestID);
            } catch (RemoteException e) {
                return ErrorAndLogMsg.ExceptionErr(e,
                        "Exception when retrieving reserve records from server " + 
                        srv_wrapper.prof.shortName);
            }
            
            if (recs!=null) {
                records.addAll( Arrays.asList(recs));
            
                ErrorAndLogMsg.LogMsg("Query record success: " + "GuestID:" + guestID + "NumRecords:" + recs.length);
            } else
                ErrorAndLogMsg.LogMsg("Query record success: " + "GuestID:" + guestID + "NumRecords:" + 0);
        }
        
        return null;
    }
    
    public ErrorAndLogMsg loginAsManager ( String hotel, String mgrID, String msgPass) {
        
    
        synchronized (servers) {

            HotelServerWrapper server_wrap = null;

            synchronized (servers) {
                server_wrap = servers.get(hotel);
            }
            
            if (server_wrap==null)
                return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                        "Hotel not found:" + hotel);
            
            IHotelServer server = server_wrap.server;
            
            server_wrap.serverMgr = null; // TODO, how to explicily release the remote object?
            
            try {
                server_wrap.serverMgr = server.getManagerObject(mgrID, msgPass);
                
                if (server_wrap.serverMgr == null)
                    return ErrorAndLogMsg.GeneralErr(ErrorCode.MGR_LOGIN_FAILURE, 
                            "Manager login failure to Hotel:" + server.getProfile().shortName);
                else {
                    ErrorAndLogMsg.LogMsg("Retrieved manager object:" + "Hotel:" + hotel);
                    return null;
                }
            } catch (RemoteException e) {
                return ErrorAndLogMsg.ExceptionErr(e, "Exception when getting manager object");
            }
        }
    }
    
    
    public ErrorAndLogMsg getServiceReport (
            String hotel, 
            Date serviceDate,
            Collection <Record> records) {
        HotelServerWrapper server_wrapper = null;
                
        synchronized (servers) {
            server_wrapper = servers.get(hotel);
        }
        
        if (server_wrapper==null)
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "Hotel not found:" + hotel);
        
        IHotelServer server = server_wrapper.server;
        IHotelServerManager mgr = server_wrapper.serverMgr;
        
        if (mgr==null) 
            return ErrorAndLogMsg.GeneralErr(ErrorCode.MGR_LOGIN_FAILURE, 
                    "Hotel " + hotel + "not yet logged in as manager. Login first.");
        
        
        records.clear();
        
        try {
            Record[] recs = mgr.getServiceReport(serviceDate);
            records.addAll( Arrays.asList(recs ));
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, 
                    "Exception when getting service records");
        }
        
        ErrorAndLogMsg.LogMsg("Get Service Report success:Hotel:" + hotel + ";Date:" + serviceDate.toString());
        return null;
    } 

    
    public ErrorAndLogMsg getStatusReport (
            String hotel, 
            Date date,
            Collection <Record> records) {
        HotelServerWrapper server_wrapper = null;
                
        synchronized (servers) {
            server_wrapper = servers.get(hotel);
        }
        
        if (server_wrapper==null)
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "Hotel not found:" + hotel);
        
        IHotelServer server = server_wrapper.server;
        IHotelServerManager mgr = server_wrapper.serverMgr;
        
        if (mgr==null) 
            return ErrorAndLogMsg.GeneralErr(ErrorCode.MGR_LOGIN_FAILURE, 
                    "Hotel " + hotel + "not yet logged in as manager. Login first.");
        
        
        records.clear();
        
        try {
            Record[] recs = mgr.getStatusReport(date);
            records.addAll( Arrays.asList(recs ));
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, 
                    "Exception when getting service records");
        }
        
        ErrorAndLogMsg.LogMsg("Get Service Report success:Hotel:" + hotel + ";Date:" + date.toString());
        
        return null;
    } 
    
    
}
