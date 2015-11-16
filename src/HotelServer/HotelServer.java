package HotelServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.ws.Endpoint;

import HotelServer.ReserveRecords.IRecordOperation;
import HotelServerInterface.IHotelServer;
import common.ErrorAndLogMsg;
import common.ErrorAndLogMsg.ErrorCode;
import common.ErrorAndLogMsg.MsgType;
import common.HotelServerTypes.*;
import serverws.HotelServerWS;


public class HotelServer implements IHotelServer, Runnable {

    final static String DEFAULT_LOG_FILE_PATH = "ServerLog.txt";
    
    static boolean logQueries = false;
    
    // Count of available rooms per room type, across the days
    private EnumMap <RoomType, AvailableRoomCounts> roomCounts;
    
    private long managerLoginToken = -1;
    
    // Reservation records for different guests and all room types
    ReserveRecords resRecords;
    
    HotelProfile prof;
    
        
    HotelServerWS hotelServerWS; // wrapper of server for Web Service
    Endpoint      endpointWS; 

    
    private static int MAX_RESERVATION_ID = 10000;
    // reservation ID is the short code of the hotel(unique), 
    // plus an incremental number which last value is as below
    // increment until MAX_RESERVATION_ID then go back to zero
    int lastReservationID = 0;
    
/*    public static class AddressPort {
        private InetAddress add;
        private int port;
        AddressPort (InetAddress add, int port) {
            this.add = add;
            this.port =port;
        }
    }*/
    
    //List<InetSocketAddress> srvSocketAddresses;
    Map <String, InetSocketAddress> srvSocketAddresses;
    
    int queryPort;
    final static int DEFAULT_LISTEN_PORT = 5000;

    public static final int SOCKET_TIMEOUT = 3000;
    
    // how many counts we have done.  
    // Update the server query port information every 10 times of query
    int queryCount = 0;
    final int  QUERIES_PER_SERVER_UPDATE = 0; // 0 indicating only query for the first time;
    
    private String managerID, managerPassword;
    
    private Thread thread = null;
    
    DatagramSocket listenSocket = null;

    
    public HotelServer ( HotelProfile prof, int queryPort) {
        
        this.prof = prof;
        
        roomCounts = new EnumMap <RoomType, AvailableRoomCounts> (RoomType.class);
        for (RoomType t:RoomType.values()) {
            roomCounts.put(t, 
                    new AvailableRoomCounts ( prof.totalRooms.get(t), 30 ) );
        }
        
        resRecords = new ReserveRecords();
        
        srvSocketAddresses = new TreeMap<String, InetSocketAddress> ();
        
        this.queryPort = queryPort;
        
    }
    

    
    @Override
    public HotelProfile getProfile ()  {
        return prof;
    }
    
    private String getClientHostLog() {
        // CORBA does not provide a convenient way to get the client address
        // just ignore
        //try {
            return ";From: N/A"; //+ RemoteServer.getClientHost();
        //} catch (ServerNotActiveException e) {
       //     e.printStackTrace(); // abnormal error, just print
        //    return "";
       // }
    }

    // TODO : serverID may not guarantee unique here
    private int getNewReserveID () {
        int id = Math.abs(this.hashCode() % 1000);
        
        
        id = id  * MAX_RESERVATION_ID + (this.lastReservationID);
        
        lastReservationID = (lastReservationID + 1);
        if (lastReservationID == MAX_RESERVATION_ID)
            lastReservationID = 0;//% 100;
        
        return id;
    }
    
    @Override
    public ErrorCode reserveRoom (
            String guestID, RoomType roomType, 
            SimpleDate checkInDate, SimpleDate checkOutDate, int[] resID)  {
        
        Record newRec = new Record (
                getNewReserveID(),
                guestID, 
                prof.shortName, 
                roomType, 
                checkInDate, checkOutDate, 
                prof.rates.get(roomType));
        
        resID[0] = newRec.resID;
        
        String logStr =  newRec.toOneLineString();
        logStr = logStr + getClientHostLog();

        
        AvailableRoomCounts counts = roomCounts.get(roomType);
        
        //First do "pre-server" operation by checking and decreasing the available room counters
        boolean r = counts.decrementDays(checkInDate, checkOutDate);
        
        if (r) {
            // Successfuly pre-servered, now add the reservation records

            
            try {
                r = resRecords.makeReservation(guestID, newRec);
            } catch (Exception e) {
                ErrorAndLogMsg.ExceptionErr(e, "Exception making reservation: " + logStr);
                return ErrorCode.EXCEPTION_THROWED;
            }
            
            if (r) {
                ErrorAndLogMsg.LogMsg("Reservation success: " + logStr);
                return ErrorCode.SUCCESS;
            }
            else {
                //some error happens when adding reserve record, 
                //need to revert the counters of pre-servation
                counts.incrementDays(checkInDate, checkOutDate);    
                
                ErrorAndLogMsg.GeneralErr(ErrorCode.INTERNAL_ERROR, "Internal error making reservation: " + logStr);
                return ErrorCode.INTERNAL_ERROR;
            }
        } else {
            ErrorAndLogMsg.LogMsg("Reservation failure, room unavailable: " + logStr);
            return ErrorCode.ROOM_UNAVAILABLE;
        }
        
    }
            
    
    @Override
    public ErrorCode cancelRoom (
            String guestID, RoomType roomType, 
            SimpleDate checkInDate, SimpleDate checkOutDate)  {
        


        
        AvailableRoomCounts counts = roomCounts.get(roomType);
        Record cancelRec = new Record (
                0, // do not provide, just match the dates
                guestID, 
                prof.shortName, 
                roomType, 
                checkInDate, 
                checkOutDate,
                prof.rates.get(roomType));
        
        String logStr = cancelRec.toOneLineString();
        logStr = logStr + getClientHostLog();
        
        //First to see if we can find the reservation record and delete/update it
        boolean r = resRecords.cancelReservation(guestID, cancelRec, 
                getNewReserveID()  // provide a new reseravation ID in case a split is needed
                );
        
        if (r) {
            // Now we found the record and delete/update already.
            // Then we need to update the Room availability counters
            
            r = counts.incrementDays(checkInDate, checkOutDate);
            
            if (!r) {
                ErrorAndLogMsg.GeneralErr(ErrorCode.INTERNAL_ERROR, "Internal error canceling: " + logStr);
                
                // To better maintain integrity, add the reservation record back
                resRecords.makeReservation(guestID, cancelRec);
                
                return ErrorCode.INTERNAL_ERROR;
            } else {
                ErrorAndLogMsg.LogMsg("Cancelation success: " + logStr);

                return ErrorCode.SUCCESS;
            }
        } else {
            ErrorAndLogMsg.LogMsg("Cancelation failure, record not found: " + logStr);

            return ErrorCode.RECORD_NOT_FOUND;
        }
        
    }


    
    @Override
    public Record[] getReserveRecords (
            String guestID )  {
        
        Record [] records = resRecords.getRecords(guestID);
        
        if (logQueries) {
            int len = 0;
            if (records !=null) 
                len = records.length;
            
            ErrorAndLogMsg.LogMsg("Get reserve records: GuestID:" + guestID + 
                    ";NumRecords:" + len +
                    getClientHostLog());
        }
        
        return records;
    }
    
   public long loginAsManager (String managerID, String passWord) {
    	
    	final Random rand = new Random();
    	
        String logStr = getClientHostLog();
        
        if (managerID.equals(managerID) &&
            passWord.equals(managerPassword))
        {
            ErrorAndLogMsg.LogMsg("Server object returned: ManagerID:" + managerID + logStr);
            long token = rand.nextLong();
            if (token < 0) token = - token;
            if (token ==0 ) token = 10;
            
            managerLoginToken = token;
            
            return token;
        }
        else {
            ErrorAndLogMsg.LogMsg("Server object NOT returned, auth failure: ManagerID:" + managerID + logStr);
            return -1;
        }
    }
    
    @Override
    public ErrorCode getServiceReport (
    		long token, SimpleDate serviceDate,
    		List <Record> list)  {
        
    	final SimpleDate date = serviceDate;
    	final List <Record> resultList = list;
    	
        // check manager login token first
    	if (token != managerLoginToken) {
    		list.clear();
    		return ErrorCode.MGR_LOGIN_FAILURE;
    	}
        
        resRecords.traverseAllRecords(
                new IRecordOperation() {
                    public void doOperation(Record r) {
                        if (r.checkOutDate.equals(date)) 
                        	resultList.add(r);                    
                    }
                } );
        
        if (logQueries) {
            ErrorAndLogMsg.LogMsg("Get service report: Date:" + serviceDate.toString() +
                    getClientHostLog());
        }
        
        return ErrorCode.SUCCESS;
    }
    
    public ErrorCode getStatusReport (
    		long token,
    		final SimpleDate statusDate,
    		List <Record> list)  {
        
        
    	final SimpleDate date = statusDate;
    	final List <Record> resultList = list;
    	
        // check manager login token first
    	if (token != managerLoginToken) {
    		list.clear();
    		return ErrorCode.MGR_LOGIN_FAILURE;
    	}
        
        resRecords.traverseAllRecords(
                new IRecordOperation() {
                    public void doOperation(Record r) {
                        if (!r.checkInDate.after(date) &&
                             r.checkOutDate.after(date))
                        	resultList.add(r);    
                    }
                } );
        
        if (logQueries) {
            ErrorAndLogMsg.LogMsg("Get status report: Date:" + statusDate.toString() +
                    getClientHostLog());
        }
        
        return ErrorCode.SUCCESS;

    }

    
    // Start web service
    public void startWebService (
            int port,     // port of HTTP
            int portMan,
            String name,  // sub-domain name
            String adminName // sub-domain name of Hotel Admin
            ) {
        
        try {
    
        	// TODO: Deploy Webservice
            
        	String urlHS = "http://localhost:" + port + "/" + prof.shortName;
        	String urlHSMan = "http://localhost:" + portMan + "/" + prof.shortName;
        	
        	hotelServerWS = new HotelServerWS (this);
        	
        	if (hotelServerWS == null)
        		ErrorAndLogMsg.GeneralErr(ErrorCode.INTERNAL_ERROR, "Hotel Web Service creation failure.");

            endpointWS = Endpoint.publish(
           		 urlHS,
           		hotelServerWS);  
            
            if (endpointWS == null)
            	throw new RuntimeException ("Web service publish failure.");
            
            System.out.println("Web service for HotelServer created: " + urlHS);
            

        } catch (Exception e) {
            ErrorAndLogMsg.ExceptionErr(e, "Server registration failed");
        }
        
    }
    
    public void stopWebServce () {
    	endpointWS.stop();
    }
    
    public void printCounterTable (EnumMap <RoomType, ArrayList<int[]>> counts) {
        
        for (RoomType typ: RoomType.values()) {
            System.out.println ("\n\nCounters for type:" + typ.toString());
            
            ArrayList<int[]> ct = counts.get(typ);
            
           
            for (int n=0; n<10; n++) {
                System.out.print ( String.format("%3d",n));
            }
            System.out.println ("");
            for (int n=0;n<10;n++) System.out.print("+--");
            System.out.println ("");
            
            int idx = 0;
            while (idx < ct.size())  {
                
                int n = 0;
                while (n<10 && idx < ct.size()) {
                    int cnt = ct.get(idx)[0];
                    System.out.print (String.format("%3d",cnt));
                    n++;
                    
                    idx++;
                //    dt.setTime(dt.getTime() + 24*60*60*1000);
                }
                System.out.println ("");
            }
        }
    }
    
    public void printInternalDatas () {
        for (RoomType typ: RoomType.values()) {
            System.out.println ("\n\nCounters for type:" + typ.toString());
            
            AvailableRoomCounts ct = roomCounts.get(typ);
            
            SimpleDate dt= new SimpleDate(); //today
            
            SimpleDate dt_end = ct.getBookingEndDate();
            
            for (int n=0; n<10; n++) {
                System.out.print ( String.format("%3d",n));
            }
            System.out.println ("");
            for (int n=0;n<10;n++) System.out.print("+--");
            System.out.println ("");
            
            while (!dt_end.before(dt))  {
                
                int n = 0;
                while (n<10 && !dt.after(dt_end)) {
                    int cnt = ct.getCount(dt);
                    System.out.print (String.format("%3d",cnt));
                    n++;
                    
                    dt.nextDay();
                //    dt.setTime(dt.getTime() + 24*60*60*1000);
                }
                System.out.println ("");
            }
        }
        
        System.out.println ("Reservation records:");
        
        resRecords.traverseAllRecords( new IRecordOperation () {
            @Override
            public void doOperation(Record r) {
                System.out.println (r.toOneLineString());
            }
        });
    }
    
    //Arg#1 Property file of the server
    //      If missing, just look into the current dir of "config.properties".
    public static void main (String args[]) {
        
        //String regHost;
        //int regPort;
        int httpPort, httpManPort, queryPort;
        String name;
        
        FileInputStream input=null;
        HotelProfile prof = new HotelProfile();
        String mgrID, mgrPass, centralAdminName;
        
        String logFilePath = null;

        try {
        

            if (args.length>0)
                input = new FileInputStream(args[0]);
            else
                input = new FileInputStream("config.properties");
            
            // load a properties file
            Properties prop = new Properties();
            prop.load(input);

            // get the registry and naming properties
            //regHost = prop.getProperty("RegistryHost");
            //regPort = Integer.parseInt(prop.getProperty("RegistryPort"));
            httpPort = Integer.parseInt(prop.getProperty("HTTPPort"));
            httpManPort = Integer.parseInt(prop.getProperty("HTTPManPort"));
            name = prop.getProperty("BindName");
            centralAdminName = prop.getProperty("AdminName");
            

            prof.shortName = name;
            prof.fullName = prop.getProperty("FullName");
            prof.descText = prop.getProperty("Description");
            
            prof.totalRooms = new EnumMap <RoomType, Integer> (RoomType.class);
            prof.rates = new EnumMap <RoomType, Float> (RoomType.class);
            
            String sQueryPort = prop.getProperty("QueryPort");
            if (sQueryPort==null || sQueryPort.isEmpty())
                queryPort = DEFAULT_LISTEN_PORT;
            else
                queryPort = Integer.parseInt(sQueryPort);
            
            prof.allTotalRooms = 0;
            
            for (RoomType typ:RoomType.values()) {
                int cnt = Integer.parseInt(prop.getProperty("RoomCnt-" + typ.toString()) );
                prof.totalRooms.put(typ, cnt);
                
                prof.allTotalRooms += cnt;
                
                float rate = Float.parseFloat(prop.getProperty("Rate-" + typ.toString() ));
                prof.rates.put(typ, rate);
            }
            
            mgrID = prop.getProperty("ManagerID");
            mgrPass = prop.getProperty("ManagerPassword");
            
            logFilePath = prop.getProperty("LogFile");
            logQueries = prop.getProperty("LogQueries").equals("1");
            
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //Open the log file and append contents
        if (logFilePath==null || logFilePath.isEmpty()) {
            logFilePath = DEFAULT_LOG_FILE_PATH;
        }
        

        File logFile = new File (logFilePath);

        PrintStream logStream = null;

        try {
            logStream = new PrintStream(logFile);
        } catch (IOException e) {
            System.out.println ("Log file open and creation error.");
            e.printStackTrace();
        }
        
        ErrorAndLogMsg.addStream(MsgType.ERR, System.err);
        ErrorAndLogMsg.addStream(MsgType.WARN, System.out);
        ErrorAndLogMsg.addStream(MsgType.INFO, System.out);

        
        if (logStream!=null) {
            // every thing put to the log file
            ErrorAndLogMsg.addStream(MsgType.LOG, logStream ); // the log fine
            ErrorAndLogMsg.addStream(MsgType.ERR, logStream );
            ErrorAndLogMsg.addStream(MsgType.WARN, logStream);
            ErrorAndLogMsg.addStream(MsgType.INFO, logStream);
        }
        
        ErrorAndLogMsg.setAutoPrint(true);

        //logStream.println("Log started");
        ErrorAndLogMsg.LogMsg("Logging started.").printMsg();
        
        
        // Now we can create the server object
        HotelServer server = new HotelServer(prof, queryPort);
        
        server.managerID = mgrID;
        server.managerPassword = mgrPass;
        
        server.loadServerAddPorts();
        
        server.startQueryListeningThread(); // throw exception if thread start failed
        
        try {
            // Export the server
            server.startWebService (httpPort, httpManPort, name, centralAdminName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        System.out.println("Server running!");
        
        Scanner keyboard = new Scanner(System.in);
        // Allow the user to display the counters and records for debug and verify purpose
        while (true) {
            System.out.println ("\n\n\nPress p<Enter> to display internal data (debug and verify purpose):");
            System.out.println ("Press v<Enter> to verify the integrity of the datas.");
            System.out.println ("Press q<Enter> to quit.");
            
            String ln = keyboard.nextLine();
            
            if (ln.equals("p")) {
                // print out internal datas
                
                server.printInternalDatas();
                
            } else if (ln.equals("v")) {
                
                boolean match = server.integrityCheck();
                
                if (match) 
                    System.out.println("Integrity check no issue.");
                else
                    System.out.println("Mismatch found during integrity check.");
            }
            else if (ln.equals("q")) {
                
                /*
                try {
                    UnicastRemoteObject.unexportObject(server, true);
                } catch (NoSuchObjectException e) {
                    e.printStackTrace();
                } */
                
                server.stopQueryListeningThread();
                
                server.stopWebServce();
                
                return;
            } 
        }
    }
    
    
    private void loadServerAddPorts () {

        // TODO: add code to invoke admin object
        /*
        if (adminObj != null) {
            
            List<InetSocketAddress> newAddr = null;
            
            try {
                newAddr = adminObj.getHotelQuerySockets();
            } catch (RemoteException e) {
                
                ErrorAndLogMsg.ExceptionErr(e,  "Exception when getting query sockets.");
                return;
            }
            
            if (newAddr!=null) {
                srvSocketAddresses = newAddr;
                ErrorAndLogMsg.InfoMsg("Server query sockets updated.");
            } else
                ErrorAndLogMsg.InfoMsg("Server query sockets update failure.");
        } */
        
        // work-around solution, hard code socket addresses
        
        srvSocketAddresses = new TreeMap <String,InetSocketAddress> ();
        
        srvSocketAddresses.put("Gordon", new InetSocketAddress("localhost", 5000));
        srvSocketAddresses.put("Star", new InetSocketAddress("localhost", 5001));
        srvSocketAddresses.put("Motel", new InetSocketAddress("localhost", 5002));
    }
    
    void startQueryListeningThread () {
        thread = new Thread(this);
        
        thread.start();
    }
    
    void stopQueryListeningThread() {
        
        if (thread!=null)
            thread.interrupt();
        
        if (listenSocket!=null)
            listenSocket.close();
            

    }
    
    
    private static String decodeString (ByteBuffer bbuf) {
        byte[] buf = bbuf.array();
        int len = bbuf.get();
       
        if (len>0) {
            String r = new String (buf, bbuf.position(), len);
            bbuf.position( bbuf.position() + len );
        
            return r;
        } else
            return "";
    }

    
    private static void encodeString (ByteBuffer bbuf, String s) {
        if (s==null)
            bbuf.put((byte)0);
        else {
            bbuf.put( (byte) s.length());
            if (s.length()>0)
                bbuf.put( s.getBytes());
        }
    }
    
    public static class UDPMessage implements 
        Serializable, Cloneable { //implements serializable {
        enum MessageType {
            AVIAL_REQ, 
            AVIAL_REPLY,
            QUERY_COMMIT_REQ, 
            VOTE_REPLY, 
            COMMIT_REQ, 
            COMMIT_ACK, 
            COMMIT_RETRY};

        MessageType msgType;
        String guestID;
        String hotelName;
        String targetHotel;
        RoomType roomType;
        SimpleDate inDate, outDate;
        float rate;
        int availCnt;
        int resID; // or transaction ID
        
        int newPort; // new port for transaction
        int newResID; // new reservation ID
        
        int returnCode; // OK / 1; NOT_OK, 0 or other

        
        
       /* public static MessageType getMessageType (byte[] buf) {
            return MessageType.values()[buf[0]];
        } */
        
        public int encode (MessageType msgType, byte[] buf) {
            ByteBuffer bbuf = ByteBuffer.wrap(buf);
            
            this.msgType = msgType;
            bbuf.put ((byte)msgType.ordinal());
            
            encodeString(bbuf, guestID);
            encodeString(bbuf, hotelName);
            encodeString(bbuf, targetHotel);
            if (roomType!=null)
                bbuf.put((byte) roomType.ordinal());
            else
                bbuf.put((byte)255);
            SimpleDate.encodeDate (bbuf, inDate);
            SimpleDate.encodeDate (bbuf, outDate);
            bbuf.putFloat(rate);
            bbuf.putInt(availCnt);
            bbuf.putInt(resID);
            bbuf.putInt(newPort);
            bbuf.putInt(newResID);
            bbuf.putInt(returnCode);
            
            return bbuf.position();
        }
        
        public static UDPMessage decode (byte[] buf) {
            ByteBuffer bbuf = ByteBuffer.wrap(buf);
            
            UDPMessage msg = new UDPMessage();

            msg.msgType = MessageType.values()[bbuf.get()];
            
            msg.guestID = decodeString(bbuf);
            msg.hotelName = decodeString(bbuf);
            msg.targetHotel = decodeString(bbuf);
            
            byte b = bbuf.get();
            if (b>=0 && b<RoomType.values().length)
                msg.roomType = RoomType.values()[b];
            else
                msg.roomType = null;
                    
            msg.inDate = SimpleDate.decodeDate (bbuf);
            msg.outDate = SimpleDate.decodeDate (bbuf);
            msg.rate = bbuf.getFloat();
            msg.availCnt = bbuf.getInt();
            msg.resID = bbuf.getInt();
            msg.newPort = bbuf.getInt();
            msg.newResID = bbuf.getInt();
            msg.returnCode = bbuf.getInt();
            
            return msg;
        }
        
        public String toString() {
            return "msgType:" + msgType +
                   ";guestID:" + guestID +
                   ";hotelName:" + hotelName +
                   ";targetHotel:" + targetHotel +
                   ";roomType:" + roomType +
                   ";inDate:" + inDate +
                   ";outDate:" + outDate +
                   ";rate:" + rate +
                   ";availCnt:" + availCnt +
                   ";resID:" + resID +
                   ";newPort:" + newPort +
                   ";newResID:" + newResID +
                   ";return:" + returnCode; // or transaction ID
        }
        
        @Override
        public Object clone() {
            UDPMessage newMsg = new UDPMessage();
            newMsg.msgType = msgType;
            newMsg.guestID = new String(guestID);
            newMsg.hotelName = new String (hotelName);
            newMsg.targetHotel = new String (targetHotel);
            newMsg.roomType = roomType;
            newMsg.inDate = inDate.clone();
            newMsg.outDate = outDate.clone();
            newMsg.rate = rate;
            newMsg.availCnt = availCnt;
            newMsg.resID = resID;
            newMsg.newPort = newPort;
            newMsg.newResID = newResID;
            newMsg.returnCode = returnCode;
            return newMsg;
        }
    }
    
    
    private void handleAvailRequest(UDPMessage msgRequest, DatagramPacket request, byte[] buf) {
        
        // extract data from request
        //byte[] buf = request.getData();
        
       // UDPMessage msgRequest = UDPMessage.decode(buf);
        
        if (msgRequest == null) {
            ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, 
                    "Error decoding query message from " + request.getAddress());
            return;
        }
        
    /*
        RequestMessage msgRequest = null;
        try {
            msgRequest = new RequestMessage();
            msgRequest.decodeAvailRequest(buf);
        } catch (Exception e) {
            ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, 
                    "Error decoding query message from " + request.getAddress());
            
            return;
        } */
        
        // do the query
        int avail = roomCounts.get(msgRequest.roomType).query(msgRequest.inDate, msgRequest.outDate);
        
        // compose the reply message
        UDPMessage msgReply = new UDPMessage();
        msgReply.hotelName = prof.shortName;
        msgReply.roomType = msgRequest.roomType;
        msgReply.availCnt = avail;
        if (avail>0) 
            msgReply.rate = prof.rates.get(msgReply.roomType);
        else
            msgReply.rate = 0;
        int len = msgReply.encode(UDPMessage.MessageType.AVIAL_REPLY, buf);
        
        /*ReplyMessage msgReply = new ReplyMessage();
        
        msgReply.hotelName = prof.shortName;
        msgReply.type = msgRequest.roomType;
        msgReply.avail = avail;
        if (avail>0) 
            msgReply.rate = prof.rates.get(msgReply.type);
        else
            msgReply.rate = 0;
        
        int len = msgReply.encodeAvailReply(buf); */
        

        DatagramPacket reply = new DatagramPacket(buf, 
                len,
                request.getAddress(), 
                request.getPort());
        
        try {
            listenSocket.send(reply);
        } catch (Exception e) {
            ErrorAndLogMsg.ExceptionErr(e, "Excepting sending reply packet.");
            return;
        }
        
        if (logQueries) {
            String recStr = new Record (0,"", prof.shortName, msgRequest.roomType,
                    msgRequest.inDate, msgRequest.outDate,
                    0).toOneLineString();
            
            ErrorAndLogMsg.LogMsg("Query reply sent: " + recStr + ";toHost:" + request.getAddress());
        }
    }

    private void handleCommitRetry (UDPMessage msgRequest, DatagramPacket request, byte[] buf) {

        if (msgRequest == null) {
            ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, 
                    "Error decoding commit retry message from " + request.getAddress());
            return;
        }
        
        String transInfo = "TransID:" + msgRequest.resID + "-";
        ErrorAndLogMsg.LogMsg( transInfo + "Received " + msgRequest.msgType);
        
        // =======================================
        // Find the record to see if it is really there
        // the reservation ID in msgRequest is the old ID
        // the new reservation ID is unknown, so find with guestID, date, room type, etc
        Record rec = resRecords.findRecord(
                msgRequest.guestID, 
                msgRequest.roomType,
                msgRequest.inDate,
                msgRequest.outDate);
        
        // compose the reply message
        UDPMessage msgReply = (UDPMessage)msgRequest.clone();
        msgReply.returnCode = (rec!=null? 1 : 0 );
        msgReply.newResID = (rec!=null? rec.resID : 0);
        
        int len = msgReply.encode(UDPMessage.MessageType.COMMIT_ACK, buf);
        
        DatagramPacket reply = new DatagramPacket(buf, 
                len,
                request.getAddress(), 
                request.getPort());
        
        try {
            listenSocket.send(reply);
            
            ErrorAndLogMsg.LogMsg(transInfo + "COMMIT_ACK sent" + msgReply);
            
        } catch (Exception e) {
            ErrorAndLogMsg.ExceptionErr(e, transInfo + "Excepting sending reply packet.");
            return;
        }

    }
    
    // The thread body to receive query request and send respond
    public void run() {
        System.out.println ("Query listening thread started...");
        
        byte[] buffer = new byte[5000]; // todo , fine tune buffer
        
        listenSocket = null; 
        
        
        try {
            
            listenSocket = new DatagramSocket (queryPort);
            
            
            while (true) {
                DatagramPacket request = new DatagramPacket (buffer, buffer.length);
                
                if (thread.isInterrupted())
                    return;
                
                try {
                    listenSocket.receive(request);
                } catch (Exception e) {
                    if (!thread.isInterrupted())
                        ErrorAndLogMsg.ExceptionErr(e, "Exception receiving query packet.");
                    break;
                }
                

                // extract data from request
                byte[] buf = request.getData();
                
                UDPMessage msg = UDPMessage.decode(buf);
                //UDPMessage.MessageType reqType = UDPMessage.getMessageType(buf);
                // get request type
                //RequestMessage.RequestType reqType = RequestMessage.getRequestType(buf);
                
                switch (msg.msgType) {
                        //reqType) {
                case AVIAL_REQ:
                        handleAvailRequest(msg, request, buf);
                        break;
                case QUERY_COMMIT_REQ:
                        handleQueryCommit (listenSocket, request);
                        break;
                case COMMIT_RETRY:
                        handleCommitRetry (msg, request, buf);
                        break;
                default:
                    // other message shall not be sent to this port
                    ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, "Request " + 
                            msg.msgType + " should not be sent to port " + queryPort);
                }
            }
        } catch (Exception e) {
            ErrorAndLogMsg.ExceptionErr(e, "Exception in query thread. Query thread aborted.");
        } finally {
            if (listenSocket !=null) 
                listenSocket.close();
        }
    }
    
    @Override
    public List<Availability> checkAvailability (
            String guestID, RoomType roomType,
            SimpleDate checkInDate, SimpleDate checkOutDate )  {
        
        String regHost;
        int regPort, queryPort;

        String recStr = null;
        
        if (logQueries) {
            recStr = new Record (0, guestID, prof.shortName, roomType,
                    checkInDate, checkOutDate, 0).toOneLineString();
            ErrorAndLogMsg.LogMsg( "Availability query launched " + recStr + getClientHostLog());
        }
        
        List<Availability> avails = new ArrayList<Availability> ();

        
        // check myself first  (don't do that as the local socket is also queried
        
/*        int cnt = roomCounts.get(roomType).query(checkInDate, checkOutDate);
        
        if (cnt > 0) {
            // need to refresh the server list
            Availability avail = new Availability();
            avail.hotelName = prof.shortName;
            avail.availCount = cnt;
            avail.rate = prof.rates.get(roomType);
            
            avails.add(avail);        
        } */
                
        // check if we need to load and update all server socket addresses for euqery
        
        if (queryCount == 0) 
            loadServerAddPorts();
        
        if (QUERIES_PER_SERVER_UPDATE > 0) {
                queryCount++;
        
                if (queryCount >= QUERIES_PER_SERVER_UPDATE)
                    queryCount = 0;
        } else
            queryCount = 1;
        
        // send query message to all other servers
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            ErrorAndLogMsg.ExceptionErr(e, "Exception creating query socket.");
            return null;
        }
        
        byte[] buf = new byte[5000];
        
        UDPMessage reqMsg = new UDPMessage();
        
        //RequestMessage reqMsg = new RequestMessage();
        reqMsg.roomType = roomType;
        reqMsg.inDate = checkInDate;
        reqMsg.outDate = checkOutDate;
        
        int len = reqMsg.encode(UDPMessage.MessageType.AVIAL_REQ, buf);
        
        //int len = reqMsg.encodeAvailRequest(buf);
        
        int sent = 0;
        
        for (InetSocketAddress addr : srvSocketAddresses.values()) {
            
            try {
                DatagramPacket request = new DatagramPacket(buf, len, addr);
    
                socket.send(request);
                
                sent ++;
                
                if (logQueries) {
                    ErrorAndLogMsg.LogMsg( "Request message sent to " + addr);
                }
                
            } catch (Exception e) {
                ErrorAndLogMsg.LogMsg("Packet send to " + addr.toString() + "failed.");
            }
        }
        
        // Reply from servers. Maximum timeout set to certain seconds
        
        buf = new byte[200];
        
        //ReplyMessage repMsg = new ReplyMessage();
        
        
        long curTime = System.currentTimeMillis();
        long endTime  = curTime  + 2000; // allow two seconds to get responds
        
        DatagramPacket reply = new DatagramPacket(buf, buf.length);

        int received = 0;
        
        // track the address from where a reply is received
        HashMap <SocketAddress, Boolean> recSockets = new HashMap <SocketAddress, Boolean> ();
        
        int total_cnt = 0; // total available rooms
        
        while (received < sent) {
            
            try {
                long now = System.currentTimeMillis();
                
                if (now < endTime) {
                    socket.setSoTimeout( (int)(endTime - now));
                    socket.receive(reply);
                } else {
                    ErrorAndLogMsg.LogMsg("Time out receiving query respond, finished receiving..");
                    break;
                }
            } catch (SocketTimeoutException e) {
                ErrorAndLogMsg.LogMsg("Time out receiving query respond, finished receiving..");
                break;
            } catch (Exception e) {
                ErrorAndLogMsg.LogMsg("Packet receive error receiving query respond. Try to cotinue...");
                continue;
            }
            
            UDPMessage repMsg = UDPMessage.decode(buf);
            
            if (repMsg.msgType != UDPMessage.MessageType.AVIAL_REPLY) {
                ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, 
                        "Expect AVAIL_REPLY but got " + reqMsg.msgType).printMsg();
                continue; // not a right message
            }

            
            SocketAddress addr_from = reply.getSocketAddress();
            
            if (recSockets.get(addr_from)==null) {
                //this is a new reply
                received ++;
                
                recSockets.put(addr_from, Boolean.TRUE);
            
                /*repMsg.avail = 0;
                
                try {
                    repMsg.decodeAvailReply(buf);
                } catch (Exception e) {
                    ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, "Error decoding reply msg from " + getClientHostLog());
                } */
                
                if (repMsg.availCnt >0) {
                
                    Availability avail = new Availability();
                    
                    avail.hotelName = repMsg.hotelName;
                    avail.availCount = repMsg.availCnt;
                    avail.rate = repMsg.rate;
                    
                    avails.add(avail);
                    
                    total_cnt += repMsg.availCnt;
                }
            }
        }
        
        if (received < sent) {
            // there is at least one server no respond.
            // let's retrieve the server socket addresses again for next query.
            queryCount = 0;
        }
        
        if (logQueries) {
            ErrorAndLogMsg.LogMsg("Got availability reply message from hosts:" + received +
                    " Total rooms:"+ total_cnt);
        }
        
        socket.close();
        
        return avails;
    }
    

    @Override
    public ErrorCode transferRoom(String guestID, int reservationID,
            RoomType roomType, SimpleDate checkInDate, SimpleDate checkOutDate,
            String targetHotel,
            int[] newID) {
        
        byte[] buf = new byte[1000];

        
        // =======================================
        // Find the record first (and lock it) and validate
        Record rec = resRecords.findRecord(guestID, reservationID);
        if (rec!=null &&
            (!rec.checkInDate.equals(checkInDate) ||
             !rec.checkOutDate.equals(checkOutDate) ||
             rec.roomType != roomType) )
             rec = null;
            
        
        if (rec==null) {
            //TODO unlock record
            return ErrorCode.RECORD_NOT_FOUND;
        }
        
        // ======================================
        // local resource ready...
        // =======================================
        

        
        // Send Query to Commit
        UDPMessage msgReq = new UDPMessage();
        msgReq.resID = rec.resID;
        msgReq.guestID = rec.guestID;
        msgReq.hotelName = rec.shortName;
        msgReq.roomType = rec.roomType;
        msgReq.inDate = rec.checkInDate;
        msgReq.outDate = rec.checkOutDate;
        msgReq.targetHotel = targetHotel;
        
        // find the socket address
        InetSocketAddress addr = this.srvSocketAddresses.get(targetHotel);
        if (addr == null) {
            //TODO unlock record
            return ErrorCode.HOTEL_NOT_FOUND;
        }
        
        
        String transInfo = "TransID:" + rec.resID + "-"; // common header for transaction progress
        
        // Log transaction start
        ErrorAndLogMsg.LogMsg(transInfo + "Transfer Transaction started.");
        
        // TODO: lock the record

        
        // send query message to the target server
        ErrorCode result = ErrorCode.SUCCESS;
        
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            ErrorAndLogMsg.ExceptionErr(e, transInfo+ "Exception creating query socket.").printMsg();
            result = ErrorCode.EXCEPTION_THROWED;
        }
        
        if (result == ErrorCode.SUCCESS) {
            
            try {
                int len = msgReq.encode(UDPMessage.MessageType.QUERY_COMMIT_REQ, buf);
                
                DatagramPacket request = new DatagramPacket(buf, len, addr);
    
                socket.send(request);
                
                ErrorAndLogMsg.LogMsg( transInfo + "Query Commit message sent to " + 
                        targetHotel + "\nMessage Detail:" + msgReq );
                
            } catch (Exception e) {
                ErrorAndLogMsg.LogMsg(transInfo + "Packet send to " + addr.toString() + "failed.");
                result = ErrorCode.EXCEPTION_THROWED;
            }
        }
        
        if (result!=ErrorCode.SUCCESS) {
            // we have problem here
            // TODO: roll back unlock record
            
            return result;
        }
        
        // =======================================
        // Receive Quote OK/NOT OK
        DatagramPacket reply = new DatagramPacket(buf, buf.length);

        UDPMessage replyMsg = null;
        result = ErrorCode.SUCCESS;
        
        try {
            socket.setSoTimeout( SOCKET_TIMEOUT );
            socket.receive(reply);
            
            buf = reply.getData();
            
            replyMsg = UDPMessage.decode(buf);

            ErrorAndLogMsg.LogMsg(transInfo + "Received Quote\nMsg:" + replyMsg);
            
        } catch (SocketTimeoutException e) {
            result = ErrorCode.TIME_OUT; 
        } catch (Exception e) {
            ErrorAndLogMsg.ExceptionErr(e, transInfo + "Exception receiving Vote Reply");
            result = ErrorCode.EXCEPTION_THROWED;
        }

        
        if (replyMsg!=null) {
            // check whether it is a valid reply
            
            if (replyMsg.msgType!=UDPMessage.MessageType.VOTE_REPLY ||
                replyMsg.resID != reservationID) {
                // invalid
                replyMsg = null;
                result = ErrorCode.INTERNAL_ERROR;
            }
            
            // check it is OK or NOT OK
            if (replyMsg.returnCode != 1) {
                replyMsg = null;
                result = ErrorCode.ROOM_UNAVAILABLE;
            }
        } 
        
        if (replyMsg==null) {
            // central point of roll back for Vote phase (unlock the record)
            // TODO
            ErrorAndLogMsg.LogMsg(transInfo + "Terminated. ErrorCode=" + result);
            return result;
        }
         
        // =======================================
        //  now we have a valid VOTE OK
        //  perform commit
        
        //  invoke cancelReservation. Note: new reserveID will not be used
        //  as this is a whole record
        boolean r = this.resRecords.cancelReservation(guestID, rec, 0);
        
        if (!r) {
            // it is rare here. report error
            ErrorAndLogMsg.GeneralErr(ErrorCode.INTERNAL_ERROR, 
                    transInfo + "Error deleting record in transfer").printMsg();
            result = ErrorCode.INTERNAL_ERROR;
        }
        
        // =======================================
        // send COMMIT request to the NEW port in replyMsg.newPort
        
        // use a loop because need to re-send if timeout
        int retryCnt=0;
        int newPort = replyMsg.newPort;
        
        do {
            
            result = ErrorCode.SUCCESS;
            
            try {
                // differentiate if a first try or retry
                int len;
                
                InetSocketAddress newAddr;
                
                // the first try is sent to the newPort
                // however, if it is a re-try, send to general query port
                if (retryCnt==0) {
                    len = msgReq.encode(UDPMessage.MessageType.COMMIT_REQ, buf);
                    newAddr = new InetSocketAddress (addr.getHostName(), newPort);
                }
                else {
                    // here is a retry. As the other host may most possbilly timeout
                    // and reverted, we can a COMMIT_QUERY to general port
                    // just to be sure whether the COMMIT is done or not, and also
                    // get the new reservationId if it is done
                    len = msgReq.encode(UDPMessage.MessageType.COMMIT_RETRY, buf);
                    newAddr = addr;
                }
                
                DatagramPacket request = new DatagramPacket(buf, len, newAddr);
    
                socket.send(request);
                
                ErrorAndLogMsg.LogMsg( transInfo + msgReq.msgType + " message sent to " + 
                        targetHotel + "\n" + msgReq );
                
            } catch (Exception e) {
                ErrorAndLogMsg.ExceptionErr(e, transInfo + "Packet send to " + addr.toString() + "failed.");
                result = ErrorCode.EXCEPTION_THROWED;
            }
            
            if (result!=ErrorCode.SUCCESS) {
                // we have problem here
                // roll back
                
                // add the record back
                resRecords.makeReservation(guestID, rec);
                
                ErrorAndLogMsg.LogMsg(transInfo + "Terminated ErrorCode:" + result);
                
                // TODO , unlock the record
                
                return result;
            }
            
            // =======================================
            // Receive Ack
            reply = new DatagramPacket(buf, buf.length);
    
            replyMsg = null;
            
            result = ErrorCode.SUCCESS;
            
            try {
                socket.setSoTimeout( SOCKET_TIMEOUT );
                socket.receive(reply);
                
                buf = reply.getData();
                
                replyMsg = UDPMessage.decode(buf);
                
                ErrorAndLogMsg.LogMsg(transInfo + "Received ack:\nMsg:" + replyMsg);
            } catch (SocketTimeoutException e) {
                result = ErrorCode.TIME_OUT; 
            } catch (Exception e) {
                ErrorAndLogMsg.ExceptionErr(e, transInfo + "Exception receiving Vote Reply");  
                result = ErrorCode.EXCEPTION_THROWED;
            }
            
            retryCnt ++;
 
        } while (result ==ErrorCode.TIME_OUT && retryCnt <3);
        
        
        if (replyMsg!=null) {
            // check whether it is a valid reply
            
            if (replyMsg.msgType!=UDPMessage.MessageType.COMMIT_ACK ||
                replyMsg.resID != reservationID) {
                // invalid
                replyMsg = null;
                result = ErrorCode.INTERNAL_ERROR;
            } else if (replyMsg.returnCode != 1) {
                // returnCode indicates whether the other server successfully processed
                // the transaction or not
                // if yes, commit the transaction as well normally
                // if not, also rollback
                replyMsg = null;
                
                if (retryCnt>1) 
                    // we ever retried the COMMIT request. so the problem is due to timeout
                    result = ErrorCode.TIME_OUT; 
                else
                    // this is an other error
                    result = ErrorCode.INTERNAL_ERROR;
            }
            
            
        } 
        
        if (replyMsg==null) {
            // roll back
            resRecords.makeReservation(guestID, rec);
            
            ErrorAndLogMsg.LogMsg(transInfo + "Terminated. ErrorCode=" + result);
            // TODO, unlock the record
            return result;
        }
        
        // =======================================
        // Now we can end the transaction
        
        // increase the room counters to release the rooms
        
        AvailableRoomCounts cnts = this.roomCounts.get(roomType);
        // shall not be null
        
        if (cnts!=null) {
            cnts.incrementDays(checkInDate, checkOutDate);
        }
        
        // return the newID
        newID[0] = replyMsg.newResID;//replyMsg.
        
        // finishing the transaction. close the socket
        socket.close();
        
        ErrorAndLogMsg.LogMsg(transInfo + "Completed.");
        
        return ErrorCode.SUCCESS;
    }
    
    
    private void handleQueryCommit (DatagramSocket socket, DatagramPacket request) {
        
        byte[] buf;

        // =======================================
        // we have received the request packet as Query to Commit
        // decode and validate it first
        // and do preservation
        boolean valid = true;
        
        buf = request.getData();
        UDPMessage msgReq = UDPMessage.decode(buf);
        
        if (msgReq.msgType != UDPMessage.MessageType.QUERY_COMMIT_REQ ||
            !msgReq.targetHotel.equals(prof.shortName) )
            valid = false;
        
        if (!valid) {
            ErrorAndLogMsg.LogMsg("Invalid request.\n" + msgReq);
            return;
        }
        
        //log the start of transaction
        String transInfo = "TransID:" + msgReq.resID + "-";
        ErrorAndLogMsg.LogMsg(transInfo + "Transaction started.");
        
        int returnCode = 1; //1 for OK, other for NOT OK
        if (valid) {
            // check availability counts
            AvailableRoomCounts cnts = this.roomCounts.get(msgReq.roomType);
            if (cnts==null) {
                returnCode = 0;
            }
            else {
                // try to preserve the rooms
                if (cnts.decrementDays(msgReq.inDate, msgReq.outDate))
                    returnCode = 1;
                else
                    returnCode = 0;
            }
        }        
        
        // ======================================
        // Send Vote OK/NOT OK via a existing socket
        // with the new Port indicated (if OK)
        UDPMessage msgReply = (UDPMessage) msgReq.clone();
        
        ErrorCode result = ErrorCode.SUCCESS;
        
        DatagramSocket newSocket = null;
        
        if (returnCode ==1) {
            // create new socket only for OK case
            try {
                newSocket = new DatagramSocket();
                msgReply.newPort = newSocket.getLocalPort();
            } catch (SocketException e) {
                ErrorAndLogMsg.ExceptionErr(e, "Exception creating transaction socket.").printMsg();
                result = ErrorCode.EXCEPTION_THROWED;
            }
        } else {
            // otherwise, use the current socket to send back result
            newSocket = null;
            msgReply.newPort = 0; // no new port
        }
        
        if (result == ErrorCode.SUCCESS) {
            
            try {
                
                msgReply.returnCode = returnCode;
                
                int len = msgReply.encode(
                        UDPMessage.MessageType.VOTE_REPLY, buf);
                
                DatagramPacket reply = new DatagramPacket(buf, len, 
                        request.getSocketAddress());
    
                // use the existing socket to send
                socket.send(reply);
                
                ErrorAndLogMsg.LogMsg( transInfo + "Vote message RETCODE=" + returnCode + " sent to " + 
                        msgReq.hotelName + "\n" + msgReply );
                
            } catch (Exception e) {
                ErrorAndLogMsg.LogMsg(transInfo + "Packet send to " + 
                      request.getSocketAddress().toString() + "failed.");
                result = ErrorCode.EXCEPTION_THROWED;
            }
        }
        
        if (result!=ErrorCode.SUCCESS) {
            // we have problem here
            // roll back if we have decreased the room count

            if (returnCode ==1) {
                // need to roll back only if there is availble room and decreased the count
                AvailableRoomCounts cnts = this.roomCounts.get(msgReq.roomType);
                if (cnts!=null)
                    // release the rooms
                    cnts.incrementDays(msgReq.inDate, msgReq.outDate);
            } 
            
            ErrorAndLogMsg.LogMsg(transInfo + "Terminated" + result);
            return;
        } else {
            // Quote message sent success.
            // Proceed only if we send a QUOTE OK
            if (returnCode !=1) {
                ErrorAndLogMsg.LogMsg(transInfo + "Transaction Completed.");
                return;
            }
        }
        
        // =======================================
        // now we create a new thread to listen in the new socket for
        // receiving Commit Request and processing
            
        class CommitRequestExecutor extends Thread {
            
            UDPMessage msgReq; // the first Query to Commit message received
            DatagramSocket socket; // the listening socket
            
            CommitRequestExecutor (
                    UDPMessage msgReq,
                    DatagramSocket socket) {
                this.msgReq = msgReq;
                this.socket = socket;
            }
            
            @Override
            public void run() {
                String transInfo = "TransID:" + msgReq.resID + "-";
                // ======================================
                // now receive the Commit Request in the new socket
                byte[] buf = new byte[2000];
                
                DatagramPacket req = new DatagramPacket(buf, buf.length);

                UDPMessage msgReqCommit = null;
                
                ErrorCode result = ErrorCode.SUCCESS;
                
                try {
                    socket.setSoTimeout( SOCKET_TIMEOUT );
                    socket.receive(req);
                    
                    buf = req.getData();
                    
                    msgReqCommit = UDPMessage.decode(buf);
                    
                    ErrorAndLogMsg.LogMsg(transInfo + "Recevied COMMIT Request:\n" + msgReqCommit);
                } catch (SocketTimeoutException e) {
                    result = ErrorCode.TIME_OUT; 
                } catch (Exception e) {
                    ErrorAndLogMsg.ExceptionErr(e, transInfo + "Exception receiving COMMIT REQUEST");
                    result = ErrorCode.EXCEPTION_THROWED;
                }
                
                if (msgReqCommit!=null) {
                    // check if it is valid
                    if (msgReqCommit.msgType != UDPMessage.MessageType.COMMIT_REQ ||
                        msgReqCommit.resID != msgReq.resID ) {
                        result = ErrorCode.INTERNAL_ERROR;
                        msgReqCommit = null;
                    }

                }
                
                if (msgReqCommit == null) {
                    // any error when receiving
                    // need to roll back
                    // we have access to the object here
                    AvailableRoomCounts cnts; // the reference to the server object
                    cnts = roomCounts.get(msgReq.roomType);
                    if (cnts!=null)
                        cnts.incrementDays(msgReq.inDate, msgReq.outDate);
                    
                    ErrorAndLogMsg.LogMsg(transInfo + "Transaction terminated. ErrorCode=" + result);
                    return;
                }

                
                // ======================================
                // now got the Commit Request, perform commit
                
                // add a reservation record
                Record newRec = new Record (
                        getNewReserveID(),
                        msgReq.guestID,
                        prof.shortName,
                        msgReq.roomType,
                        msgReq.inDate,
                        msgReq.outDate,
                        msgReq.rate);
                
                boolean r = resRecords.makeReservation(msgReq.guestID, newRec);
                
                result = ErrorCode.SUCCESS;

                if (!r) {
                    ErrorAndLogMsg.GeneralErr(ErrorCode.INTERNAL_ERROR, 
                            transInfo + "internal error, can not commit with new record." ).printMsg();
                    result = ErrorCode.INTERNAL_ERROR;
                }
                
                // ==========================================
                // now send the Ack, either success or failure
                try {
                    UDPMessage msgReply = (UDPMessage) msgReq.clone();
                    
                    msgReply.newResID = newRec.resID;
                    
                    msgReply.returnCode = (result==ErrorCode.SUCCESS?1:0);
                    
                    int len = msgReply.encode(
                            UDPMessage.MessageType.COMMIT_ACK, buf);
                    
                    DatagramPacket reply = new DatagramPacket(buf, len, 
                           req.getSocketAddress());
        
                    socket.send(reply);
                } catch (Exception e) {
                    ErrorAndLogMsg.ExceptionErr(e, transInfo + "exception sending COMMIT REPLY").printMsg();
                    result = ErrorCode.EXCEPTION_THROWED;
                }
                
                // if record delete error, or ack send failure, or any other error, roll back
                if (result!=ErrorCode.SUCCESS) {
                    resRecords.cancelReservation(msgReq.guestID, newRec, 0);
                    AvailableRoomCounts cnts; // the reference to the server object
                    cnts = roomCounts.get(msgReq.roomType);
                    if (cnts!=null)
                        cnts.incrementDays(msgReq.inDate, msgReq.outDate);
                    ErrorAndLogMsg.LogMsg(transInfo + "Transaction terminated. ErrorCode=" + result);
                } else                
                    ErrorAndLogMsg.LogMsg(transInfo + "Transaction completed."); 
                
                socket.close();
            }
            
        } 
        
        new CommitRequestExecutor (msgReq, newSocket).start();
       
    }
    
    
    
    /* cross check reserve records and avail counts, to see whether they are match
     * This check is supposed to be only performed in idle state, otherwise, the inconsistency happens which
     * is normal.
     * 
     */
    boolean integrityCheck () {
        
        // create a local counter to track the available rooms
        final EnumMap <RoomType, ArrayList<int[]>> counts = new EnumMap <RoomType, ArrayList<int[]>> 
                  (RoomType.class);
        
        for (RoomType typ: RoomType.values()) {
            counts.put(typ, new ArrayList<int[]>());
        }
        

        // trace all records and decrease the room counts
        resRecords.traverseAllRecords( new IRecordOperation () {
            @Override
            public void doOperation(Record r) {

                SimpleDate startDate = roomCounts.get(r.roomType).getBookingStartDate();
                SimpleDate inDate = r.checkInDate;
                SimpleDate outDate = r.checkOutDate;
                
                // index of start date and end date
                int i = SimpleDate.dateDiff(startDate, inDate);
                int j = SimpleDate.dateDiff(startDate, outDate)-1;
                
                // expanse the arraylist if needed
                ArrayList<int[]> list = counts.get(r.roomType);
                while (list.size()<=j) {
                    int [] n = new int[1];
                    n[0] = prof.totalRooms.get(r.roomType);
 
                    list.add(n);
                }
                    
                // decrease the room counters from index i to j ((inclusive)
                while (i <= j) {
                    int [] n = counts.get(r.roomType).get(i);
                    n[0] --;
                    
                    i++;
                } 
            }
        } );
        
        boolean match = true;
        // compare the counters with the one in roomCounts
        for (RoomType typ: RoomType.values()) {
            ArrayList<int[]> list_gen = counts.get(typ);
            AvailableRoomCounts cnts = roomCounts.get(typ);
            
            SimpleDate dt = cnts.getBookingStartDate();
            
            for (int i=0; i<list_gen.size();i++) {
                // compare the #i count
                int cnt = cnts.getCount(dt);
                
                if (cnt != list_gen.get(i)[0]) {
                    match = false;
                    break;
                }
                
                dt.nextDay();
            }
            if (!match) break;
        }
        
        if (!match) {
            System.out.println ("The simulated final counts:");
            printCounterTable (counts);
            
            System.out.println ("The actual counts in the server:");
            printInternalDatas();
            
            return false;
        } else
            return true;
        
    }



}
