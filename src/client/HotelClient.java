package client;


import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;


//import HotelServer.HotelServer;
//import HotelServerInterface.*;
import common.ErrorAndLogMsg;
//import common.HotelServerTypes.*;
import common.Utilities;
import common.ErrorAndLogMsg.ErrorCode;
import common.ErrorAndLogMsg.MsgType;
import HotelServerInterface.IHotelServer.*;
import client.webservice.Availability;
import client.webservice.GeneralReturn;
import client.webservice.HotelProfile;
import client.webservice.HotelServerWS;
import client.webservice.HotelServerWSService;
import client.webservice.ManagerReturn;
import client.webservice.Record;
import client.webservice.RoomType;
import client.webservice.SimpleDate;


public class HotelClient {

	// TODO : service discovery
    String[][] urlServers = {
    		{"Gordon", "http://localhost:8050/Gordon"},
    		{"Motel", "http://localhost:8051/Motel"},
    		{"Star", "http://localhost:8052/Star"}
    };
    
    public class HotelServerWrapper {
        //public IHotelServer server;
    	public HotelServerWS server;
       // public HotelServerManagerWS serverMgr;
        public HotelProfile prof;
        public String url;
        public long loginToken = -1; // >0 if a valid login is there
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
    
    public HotelServerWrapper[] getServers() {
        return servers.values().toArray(new HotelServerWrapper[0]);
    }
    
    public ErrorAndLogMsg Initialize () {
        
        try {
            servers = new TreeMap<String, HotelServerWrapper> ();
            
            ErrorAndLogMsg.InfoMsg("Hotel Client initialized.").printMsg();
            
            ErrorAndLogMsg m = getHotelServerObjects();
            
            return m;
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, "Client initilization failed");
        }
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
    ErrorAndLogMsg getSingleHotelServerObject (String name, String url, boolean reGet) {
        try {

        	HotelServerWSService service = new HotelServerWSService();
        	
    		HotelServerWS port = service.getHotelServerWSPort();
    		
    		if (port == null)
    			return ErrorAndLogMsg.GeneralErr(ErrorCode.SERVER_CONNECTION_FAILURE,
    					"Server failure: " + url);
    		
        	BindingProvider bindingProvider = (BindingProvider) port;
        	bindingProvider.getRequestContext().put(
        	      BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        	      url);
            
        	
            HotelServerWrapper srv;
            
            synchronized (servers) { srv = servers.get(name); }
            
            if (srv==null || reGet) {
                // new name
                
                //IHotelServer server = new HotelServerProxy (port);
                
                //if (server!=null) {
                    srv = new HotelServerWrapper();
                    srv.server = port; //server;
                    srv.prof = port.getProfile();
                    srv.url = url;
                    
                    synchronized (servers) { servers.put(name, srv); }
                //} 
                
                return ErrorAndLogMsg.LogMsg("Server object retrieved/updated.");
                
            } else {
                // old name
                // Try to get the profile and simply update
                //srv.prof = srv.server.getProfile();
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
        
        
        int new_servers = 0;

        for (String[] serverEntry:urlServers) {
        	
            ErrorAndLogMsg m = getSingleHotelServerObject 
            		(serverEntry[0], // name
            		serverEntry[1],  // url
            		false);
            
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
            SimpleDate checkInDate,
            SimpleDate checkOutDate,
            int[] newID) {
        
        HotelServerWrapper server_wrap = servers.get(hotelName);
        
        if (server_wrap==null) {
            // try to get the server object
            ErrorAndLogMsg m = getSingleHotelServerObject (
            		hotelName, 
            		server_wrap.url,
            		true);
            
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
            
        HotelServerWS server = server_wrap.server;
        
        try {
            
            Record newRec = MiscUtil.createRecord(
            		0,
                    guestID, hotelName, type, 
                    checkInDate, 
                    checkOutDate, 0);
            
            
            int [] idHolder = new int[1];
            
            GeneralReturn ret = server.reserveRoom(
                    guestID, type, checkInDate, checkOutDate );
            
            ErrorCode err = MiscUtil.convertErrorCode(ret.getError());
            
            newRec.setResID(ret.getResID());
            
            String sRec = MiscUtil.getRecordOneLineStr(newRec);
            
            if (err == ErrorCode.SUCCESS) {
                ErrorAndLogMsg.LogMsg("Reserve success: " + sRec).printMsg();
                
                if (newID!=null)
                    newID[0] = newRec.getResID();
                
                return null;
            } else {
                ErrorAndLogMsg.LogMsg("Reserve failure " + err.toString() + ":" + sRec ).printMsg();
                return ErrorAndLogMsg.GeneralErr(err, "Error invoking reserveRoom");
            }
            
        } catch (Exception e) {
            ErrorAndLogMsg m = ErrorAndLogMsg.ExceptionErr(e, "Remote Exception when invoking reserveRoom");
            
            // Try to retrieve server object again and retry
            ErrorAndLogMsg m1 = getSingleHotelServerObject(
            		hotelName,
            		server_wrap.url,
            		true);
            
            if (m1!=null && !m1.anyError())
                return reserveHotel( guestID, hotelName, type, checkInDate, checkOutDate, newID);
            else 
                return m;
        }
    }
    
   public ErrorAndLogMsg cancelHotel (
            String guestID,
            String hotelName,
            RoomType type,
            SimpleDate checkInDate,
            SimpleDate checkOutDate) {
        
        HotelServerWrapper server_wrap = servers.get(hotelName);
        
        if (server_wrap==null) 
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "No hotel server found by name " + hotelName); // Hotel not found
        
        HotelServerWS server = server_wrap.server;
        
        try {
            Record rec = MiscUtil.createRecord(
                    0,guestID, hotelName, type, checkInDate, checkOutDate, 0);

            String sRec = MiscUtil.getRecordOneLineStr(rec);

            GeneralReturn ret = server.cancelRoom(guestID, type, checkInDate, checkOutDate);
            ErrorCode err = MiscUtil.convertErrorCode(ret.getError());
            
            if (err == ErrorCode.SUCCESS) {
                ErrorAndLogMsg.LogMsg("Cancel success: " + sRec).printMsg();
                return null;
            } else {
                ErrorAndLogMsg.LogMsg("Cancel failure " + err.toString() + ":" + sRec).printMsg();
                return ErrorAndLogMsg.GeneralErr(err, "Error invoking cancelRoom");
            }
            
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, "Remote Exception when invoking cancelRoom");
        }
    }
    
    
   public ErrorAndLogMsg checkAvailability (
		   Record rec, Collection <Availability> avails) {
        
        HotelServerWrapper server_wrap = servers.get(rec.getShortName());
        
        if (server_wrap==null) 
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "No hotel server found by name " + rec.getShortName()); // Hotel not found
        
        HotelServerWS server = server_wrap.server;
        
        try {
        	
        	List <Availability> avList = server.checkAvailability(
        			rec.getGuestID(),
        			rec.getRoomType(), 
        			rec.getCheckInDate(), 
        			rec.getCheckOutDate());
            
            
            if (avList==null)
                return ErrorAndLogMsg.GeneralErr(ErrorCode.ROOM_UNAVAILABLE, "No room availabile.");
            
            for (Availability av : avList)
                avails.add(av);
            
            ErrorAndLogMsg.LogMsg("Query avail success:" + 
            		MiscUtil.getRecordOneLineStr(rec)).printMsg();
            
            return null;
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, "Remote Exception when invoking reserveRoom");
        }
    }
    
    public int getServerCnt () {
        return serverCnt;
    }
    
 /*   public ErrorAndLogMsg getReserveRecords (String guestID, Collection <Record> records) {
        
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
            } catch (Exception e) {
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
    } */
    
    // return a token (>0) if success
    // return -1 if not success
    public long loginAsManager ( String hotel, String mgrID, String msgPass) {
        
    
        synchronized (servers) {

            HotelServerWrapper server_wrap = null;

            synchronized (servers) {
                server_wrap = servers.get(hotel);
            }
            
            if (server_wrap==null) {
                ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                        "Hotel not found:" + hotel).printMsg();
                return -1;
            }
            
            HotelServerWS server = server_wrap.server;
                        
        	long token = server.loginAsManager(mgrID,  msgPass);
        	
      		server_wrap.loginToken = token;

            
            return token;
        }

    }
    
    
    public ErrorAndLogMsg getServiceReport (
            String hotel, 
            SimpleDate serviceDate,
            Collection <Record> records) {
        HotelServerWrapper server_wrapper = null;
                
        synchronized (servers) {
            server_wrapper = servers.get(hotel);
        }
        
        if (server_wrapper==null)
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "Hotel not found:" + hotel);
        
        HotelServerWS server = server_wrapper.server;
        long token = server_wrapper.loginToken;
                
        if (token < 0 )
            return ErrorAndLogMsg.GeneralErr(ErrorCode.MGR_LOGIN_FAILURE, 
                    "Hotel " + hotel + "not yet logged in as manager. Login first.");
        
        
        records.clear();
        
        try {
        	ManagerReturn ret = server.getServiceReport(token, serviceDate);
        	
        	List <Record> list = ret.getListRecord();
        	
        	if (list!=null && !list.isEmpty())
                records.addAll(list);
        	
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, 
                    "Exception when getting service records");
        }
        
        ErrorAndLogMsg.LogMsg("Get Service Report success:Hotel:" + hotel + ";Date:" + serviceDate.toString());
        return null;
    } 

    
    public ErrorAndLogMsg getStatusReport (
            String hotel, 
            SimpleDate statusDate,
            Collection <Record> records) {
    	
        HotelServerWrapper server_wrapper = null;

    	
        synchronized (servers) {
            server_wrapper = servers.get(hotel);
        }
        
        if (server_wrapper==null)
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                    "Hotel not found:" + hotel);
        
        HotelServerWS server = server_wrapper.server;
        long token = server_wrapper.loginToken;
                
        if (token < 0 )
            return ErrorAndLogMsg.GeneralErr(ErrorCode.MGR_LOGIN_FAILURE, 
                    "Hotel " + hotel + "not yet logged in as manager. Login first.");
        
        
        records.clear();
        
        try {
        	ManagerReturn ret = server.getStatusReport(token, statusDate);
        	
        	List <Record> list = ret.getListRecord();
        	
        	if (list!=null && !list.isEmpty())
                records.addAll(list);
        	
        } catch (Exception e) {
            return ErrorAndLogMsg.ExceptionErr(e, 
                    "Exception when getting service records");
        }
        
        return null;
    } 
    
    // return resID by filling resID[0]
    public ErrorAndLogMsg transferRoom (
            String guestID,
            int[] resID,
            String hotelName,
            RoomType type,
            SimpleDate checkInDate,
            SimpleDate checkOutDate,
            String targetHotel) {
        
        HotelServerWrapper server_wrap = servers.get(hotelName);
        
        if (server_wrap==null) {
            // can not get the object, return error
            return ErrorAndLogMsg.GeneralErr(ErrorCode.HOTEL_NOT_FOUND, 
                "No hotel server found by name " + hotelName); // Hotel not found
        }
            
        HotelServerWS server = server_wrap.server;
        
        try {
            
            Record newRec = MiscUtil.createRecord(
            		0,
                    guestID, targetHotel, type, 
                    checkInDate, 
                    checkOutDate, 0);
            
            
            int [] idHolder = new int[1];
            
            GeneralReturn ret =  server.transferRoom(
                    guestID, 
                    resID[0],
                    type, checkInDate, checkOutDate,
                    targetHotel);
            
            ErrorCode err = MiscUtil.convertErrorCode(ret.getError());
            idHolder[0] = ret.getResID();
            
            newRec.setResID(idHolder[0]);
            resID[0] = idHolder[0];
            
            String sRec = MiscUtil.getRecordOneLineStr(newRec);
            
            if (err == ErrorCode.SUCCESS) {
                ErrorAndLogMsg.LogMsg("Transfer success, new record: " + sRec).printMsg();
                
                return null;
            } else {
                ErrorAndLogMsg.LogMsg("Reserve failure " + err.toString() + ":" + sRec ).printMsg();
                return ErrorAndLogMsg.GeneralErr(err, "Error invoking transferRoom");
            }
            
        } catch (Exception e) {
            ErrorAndLogMsg m = ErrorAndLogMsg.ExceptionErr(e, "Remote Exception when invoking reserveRoom");
            m.printMsg();
        }
        
        return null;
    }  
    
}
