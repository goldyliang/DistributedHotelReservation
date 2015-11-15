package HotelServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

import HotelServer.ReserveRecords.IRecordOperation;
import HotelServerInterface.ErrorAndLogMsg;
import HotelServerInterface.IHotelCentralAdmin;
import HotelServerInterface.IHotelServer;
import HotelServerInterface.IHotelServerManager;
import HotelServerInterface.IHotelServer.Availability;
import HotelServerInterface.IHotelServer.HotelProfile;
import HotelServerInterface.IHotelServer.Record;
import HotelServerInterface.IHotelServer.RoomType;
import HotelServerInterface.ErrorAndLogMsg.ErrorCode;
import HotelServerInterface.ErrorAndLogMsg.MsgType;


public class HotelServer implements IHotelServer, IHotelServerManager, Runnable {

    final static String DEFAULT_LOG_FILE_PATH = "ServerLog.txt";
    
    static boolean logQueries = false;
    
    // Count of available rooms per room type, across the days
    private EnumMap <RoomType, AvailableRoomCounts> roomCounts;
    
    // Reservation records for different guests and all room types
    ReserveRecords resRecords;
    
    HotelProfile prof;
    
    IHotelCentralAdmin adminObj; // the central admin object
    
/*    public static class AddressPort {
        private InetAddress add;
        private int port;
        AddressPort (InetAddress add, int port) {
            this.add = add;
            this.port =port;
        }
    }*/
    
    List<InetSocketAddress> srvSocketAddresses;
    int queryPort;
    final static int DEFAULT_LISTEN_PORT = 5000;
    
    // how many counts we have done.  
    // Update the server query port information every 10 times of query
    int queryCount = 0;
    final int  QUERIES_PER_SERVER_UPDATE = 10;
    
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
        
        srvSocketAddresses = new ArrayList<InetSocketAddress> ();
        
        this.queryPort = queryPort;
        
    }
    

    
    @Override
    public HotelProfile getProfile () throws RemoteException {
        return prof;
    }
    
    private String getClientHostLog() {
        try {
            return ";From:" + RemoteServer.getClientHost();
        } catch (ServerNotActiveException e) {
            e.printStackTrace(); // abnormal error, just print
            return "";
        }
    }

    @Override
    public ErrorCode reserveRoom (
            String guestID, RoomType roomType, 
            Date checkInDate, Date checkOutDate) throws RemoteException {
        
        
        String logStr = new Record (guestID, this.prof.shortName, 
                roomType, checkInDate, checkOutDate, 0).toOneLineString();
        logStr = logStr + getClientHostLog();

        
        AvailableRoomCounts counts = roomCounts.get(roomType);
        
        //First do "pre-server" operation by checking and decreasing the available room counters
        boolean r = counts.decrementDays(checkInDate, checkOutDate);
        
        if (r) {
            // Successfuly pre-servered, now add the reservation records

            
            try {
                r = resRecords.makeReservation(guestID, 
                        new Record (guestID, 
                                prof.shortName, 
                                roomType, 
                                checkInDate, 
                                checkOutDate,
                                prof.rates.get(roomType)) );
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
            Date checkInDate, Date checkOutDate) throws RemoteException {
        
        String logStr = new Record (guestID, this.prof.shortName, 
                roomType, checkInDate, checkOutDate, 0).toOneLineString();
        logStr = logStr + getClientHostLog();

        
        AvailableRoomCounts counts = roomCounts.get(roomType);
        Record cancelRec = new Record (
                guestID, 
                prof.shortName, 
                roomType, 
                checkInDate, 
                checkOutDate,
                prof.rates.get(roomType));
        
        //First to see if we can find the reservation record and delete/update it
        boolean r = resRecords.cancelReservation(guestID, cancelRec);
        
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
            String guestID ) throws RemoteException {
        
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
    
    public Record[] getServiceReport (final Date serviceDate) throws RemoteException {
        
        // Have to traverse the whole records.
        
        int totalRooms = prof.allTotalRooms;
        
        final ArrayList <Record> res = 
                new ArrayList<Record> (totalRooms); // resulting records shall be less than
                                                    // total rooms
        
        resRecords.traverseAllRecords(
                new IRecordOperation() {
                    public void doOperation(Record r) {
                        if (r.checkOutDate.equals(serviceDate)) 
                            res.add(r);                    
                    }
                } );
        
        if (logQueries) {
            ErrorAndLogMsg.LogMsg("Get service report: Date:" + serviceDate.toString() +
                    getClientHostLog());
        }
        
        return res.toArray(new Record[0]) ;
    }
    
    public Record[] getStatusReport (final Date date) throws RemoteException {
        
        // Have to traverse the whole records.
        
        int totalRooms = prof.allTotalRooms;
        
        final ArrayList <Record> res = 
                new ArrayList<Record> (totalRooms); // resulting records shall be less than
                                                    // total rooms
        
        resRecords.traverseAllRecords(
                new IRecordOperation() {
                    public void doOperation(Record r) {
                        if (!r.checkInDate.after(date) &&
                             r.checkOutDate.after(date))
                            res.add(r);                    
                    }
                } );
        
        if (logQueries) {
            ErrorAndLogMsg.LogMsg("Get status report: Date:" + date.toString() +
                    getClientHostLog());
        }
        
        return res.toArray(new Record[0]) ;
    }

    
    //Export server to register server host:port, with name
    public void registerServer (
            String host,  // host name of registry
            int port,     // port of registry
            String name,  // name to bind
            String adminName // the bind name of Hotel Admin object to register
            ) throws RemoteException, NotBoundException {
        
        Remote obj = UnicastRemoteObject.exportObject(this,0);
        
        Registry r = LocateRegistry.getRegistry(host, port);
        
        r.rebind(name, obj);
        
//        IHotelServer oo = (IHotelServer) r.lookup("Gordon");
        
//        if (oo!=null) oo.getProfile();
        adminObj = (IHotelCentralAdmin) r.lookup(adminName);
        
        adminObj.registerHotelQuerySocket (queryPort);
        
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
            
            Date dt=null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      
            try {
                dt = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            } // set to today
            
            Date dt_end = ct.getBookingEndDate();
            
            for (int n=0; n<10; n++) {
                System.out.print ( String.format("%3d",n));
            }
            System.out.println ("");
            for (int n=0;n<10;n++) System.out.print("+--");
            System.out.println ("");
            
            while (dt_end.after(dt))  {
                
                int n = 0;
                while (n<10 && !dt.after(dt_end)) {
                    int cnt = ct.getCount(dt);
                    System.out.print (String.format("%3d",cnt));
                    n++;
                    
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dt);
                    cal.add(Calendar.DATE, 1);
                    dt = cal.getTime();
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
        
        String regHost;
        int regPort, queryPort;
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
            regHost = prop.getProperty("RegistryHost");
            regPort = Integer.parseInt(prop.getProperty("RegistryPort"));
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
            server.registerServer (regHost, regPort, name, centralAdminName);
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
                
                try {
                    UnicastRemoteObject.unexportObject(server, true);
                } catch (NoSuchObjectException e) {
                    e.printStackTrace();
                }
                
                server.stopQueryListeningThread();
                
                return;
            } 
        }
    }
    
    public IHotelServerManager getManagerObject (String managerID, String passWord) throws RemoteException {
        
        // To be simplified, only check the guestID
        
        String logStr = getClientHostLog();
        
        if (managerID.equals(managerID) &&
            passWord.equals(managerPassword))
        {
            ErrorAndLogMsg.LogMsg("Server object returned: ManagerID:" + managerID + logStr);
            return (IHotelServerManager) this;
        }
        else {
            ErrorAndLogMsg.LogMsg("Server object NOT returned, auth failure: ManagerID:" + managerID + logStr);
            return null;
        }
        
    }
    
    private void loadServerAddPorts () {

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
        }

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
            
        try {
            adminObj.unRegisterHotelQuerySocket(queryPort);
        } catch (RemoteException e) {
            ErrorAndLogMsg.ExceptionErr(e, "Exception when un-registering socket");
        }

    }
    
    private class RequestMessage {
        RoomType type;
        Date inDate, outDate;
        
        private void putDate (ByteBuffer bbuf, Date date) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            bbuf.putShort( (short)cal.get(Calendar.YEAR));
            bbuf.put ( (byte)cal.get(Calendar.MONTH));
            bbuf.put ( (byte)cal.get(Calendar.DAY_OF_MONTH));
        }
        
        private Date getDate(ByteBuffer bbuf) {
            short year = bbuf.getShort(); // use 100x format just to increase buffer readability
            byte month = bbuf.get();
            byte day = bbuf.get();
            
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            return cal.getTime();
        }
        
        public int encode(byte[] buf) {
            ByteBuffer bbuf = ByteBuffer.wrap(buf);// .allocate(request.getLength());

            bbuf.put((byte)(type.ordinal()));
            
            putDate (bbuf, inDate);
            putDate (bbuf, outDate);
            
            return bbuf.position();
        }
        
        
        public void decode(byte[] buf) {
            // extract data from request
    
            ByteBuffer bbuf = ByteBuffer.wrap(buf);// .allocate(request.getLength());
    
            type = RoomType.values()[bbuf.get()];
            
            inDate = getDate(bbuf);
            outDate = getDate(bbuf);
        }
    }
    
    private class ReplyMessage {
        
        String hotelName;
        RoomType type;
        int avail;
        float rate;
        
        public int encode(byte[] buf) {
            ByteBuffer bbuf = ByteBuffer.wrap(buf);// .allocate(request.getLength());
            
            bbuf.put( (byte)hotelName.length());
            bbuf.put(hotelName.getBytes());

            bbuf.put( (byte)type.ordinal());
            
            bbuf.putShort( (short)avail);
            
            if (avail>0) {
                int rate_int = Float.floatToIntBits(rate);
                bbuf.putInt(rate_int);
            }
            
            return bbuf.position();
        }
        
        
        public void decode(byte[] buf) {
            // extract data from request
    
            ByteBuffer bbuf = ByteBuffer.wrap(buf);// .allocate(request.getLength());
            
            int i= (int) buf[0]; // length of name
            
            hotelName = new String (buf, 1, i);
            
            bbuf.position(i+1);
            
            byte tp_b = bbuf.get();
            
            if (tp_b>=0 && tp_b<RoomType.values().length)
                type = RoomType.values()[tp_b];
            
            avail = bbuf.getShort();
            
            if (avail>0) {
                int i_avail = bbuf.getInt();
                rate = Float.intBitsToFloat(i_avail);
            }
        }
    }
    

    // The thread body to receive query request and send respond
    public void run() {
        System.out.println ("Query listening thread started...");
        
        byte[] buffer = new byte[200]; // todo , fine tune buffer
        
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
                
                RequestMessage msgRequest = null;
                try {
                    msgRequest = new RequestMessage();
                    msgRequest.decode(buf);
                } catch (Exception e) {
                    ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, 
                            "Error decoding query message from " + request.getAddress());
                    
                    continue;
                }
                
                // do the query
                int avail = roomCounts.get(msgRequest.type).query(msgRequest.inDate, msgRequest.outDate);
                
                // compose the reply message
                ReplyMessage msgReply = new ReplyMessage();
                
                msgReply.hotelName = prof.shortName;
                msgReply.type = msgRequest.type;
                msgReply.avail = avail;
                if (avail>0) 
                    msgReply.rate = prof.rates.get(msgReply.type);
                else
                    msgReply.rate = 0;
                
                int len = msgReply.encode(buf);
                

                DatagramPacket reply = new DatagramPacket(buf, 
                        len,
                        request.getAddress(), 
                        request.getPort());
                
                try {
                    listenSocket.send(reply);
                } catch (Exception e) {
                    ErrorAndLogMsg.ExceptionErr(e, "Excepting sending reply packet.");
                    continue;
                }
                
                if (logQueries) {
                    String recStr = new Record ("", prof.shortName, msgRequest.type,
                            msgRequest.inDate, msgRequest.outDate,
                            0).toOneLineString();
                    
                    ErrorAndLogMsg.LogMsg("Query reply sent: " + recStr + ";toHost:" + request.getAddress());
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
            Date checkInDate, Date checkOutDate ) throws RemoteException {
        
        String recStr = null;
        
        if (logQueries) {
            recStr = new Record (guestID, prof.shortName, roomType,
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
        
        queryCount++;
        if (queryCount >= QUERIES_PER_SERVER_UPDATE)
            queryCount = 0;
        
        // send query message to all other servers
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            ErrorAndLogMsg.ExceptionErr(e, "Exception creating query socket.");
            return null;
        }
        
        byte[] buf = new byte[200];
        
        RequestMessage reqMsg = new RequestMessage();
        reqMsg.type = roomType;
        reqMsg.inDate = checkInDate;
        reqMsg.outDate = checkOutDate;
        int len = reqMsg.encode(buf);
        
        int sent = 0;
        
        for (InetSocketAddress addr : srvSocketAddresses) {
            
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
        
        ReplyMessage repMsg = new ReplyMessage();
        
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
            
            SocketAddress addr_from = reply.getSocketAddress();
            
            if (recSockets.get(addr_from)==null) {
                //this is a new reply
                received ++;
                
                recSockets.put(addr_from, Boolean.TRUE);
            
                repMsg.avail = 0;
                
                try {
                    repMsg.decode(buf);
                } catch (Exception e) {
                    ErrorAndLogMsg.GeneralErr(ErrorCode.MSG_DECODE_ERR, "Error decoding reply msg from " + getClientHostLog());
                }
                
                if (repMsg.avail>0) {
                
                    Availability avail = new Availability();
                    
                    avail.hotelName = repMsg.hotelName;
                    avail.availCount = repMsg.avail;
                    avail.rate = repMsg.rate;
                    
                    avails.add(avail);
                    
                    total_cnt += repMsg.avail;
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
        
        return avails;
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

                Date startDate = roomCounts.get(r.roomType).getBookingStartDate();
                Date inDate = r.checkInDate;
                Date outDate = r.checkOutDate;
                
                // index of start date and end date
                int i = AvailableRoomCounts.dateDiff(startDate, inDate);
                int j = AvailableRoomCounts.dateDiff(startDate, outDate)-1;
                
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
            
            Date startDate = cnts.getBookingStartDate();
            
            for (int i=0; i<list_gen.size();i++) {
                // compare the #i count
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.add(Calendar.DATE, i);
                Date dt = cal.getTime();
                
                int cnt = cnts.getCount(dt);
                
                if (cnt != list_gen.get(i)[0]) {
                    match = false;
                    break;
                }
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
