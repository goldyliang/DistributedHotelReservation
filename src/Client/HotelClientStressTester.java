package Client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import miscutil.SimpleDate;
import Client.HotelClient.HotelServerWrapper;
import HotelServerInterface.ErrorAndLogMsg;
import HotelServerInterface.ErrorAndLogMsg.ErrorCode;
import HotelServerInterface.ErrorAndLogMsg.MsgType;
import HotelServerInterface.IHotelServer.Availability;
import HotelServerInterface.IHotelServer.Record;
import HotelServerInterface.IHotelServer.RoomType;

public class HotelClientStressTester implements Runnable {

    HotelClient client;
    
    String IDPrefix;
    ArrayList <String> guestIDs; // should have different range in different tester
    
    int totalDays;
    
    Random rand;
    
    String[] serverNames;// = {"Gordon", "Star", "Motel"};
    
    HashMap <String, ArrayList<Record>> records;
    //ArrayList <Record> records;
    
   
    int minutes;
   
    Date getDateByDelta (Date d, int delta)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, delta);
        
        return cal.getTime();
    }
    
 /*   void getRandomDateRange (Date d1, Date d2) {
        int i = rand.nextInt(totalDays);
        int j = i + 1 + rand.nextInt (totalDays-i);
        
        Date today = new Date();
        
        Date dd1 = getDateByDelta (today, i);
        Date dd2 = getDateByDelta (today, j);
        
        d1.setTime(d1.getTime());
        d2.setTime(d2.getTime());
    }*/
    
    Record getRandomRecord () {
        String guestID = guestIDs.get(rand.nextInt(guestIDs.size()));
        
        int i = rand.nextInt(totalDays);
        int j = i + 1 + rand.nextInt (totalDays-i);
        
        SimpleDate today = new SimpleDate();
        
        SimpleDate inDate = SimpleDate.getDateByDelta (today, i);
        SimpleDate outDate = SimpleDate.getDateByDelta (today, j);
        
        RoomType type = RoomType.values()[rand.nextInt(RoomType.values().length)];
        
        String hotel = serverNames[rand.nextInt(serverNames.length)];
        
        return new Record (0, guestID, hotel, type, inDate, outDate, 0);
    }
    
    
    void randomCheckAvail () {
        Record r = getRandomRecord();

        ArrayList <Availability> avails  = new ArrayList<Availability>();
        
        ErrorAndLogMsg m = client.checkAvailability(r, avails);
        
        if (m!=null && !m.anyError())
            m.printMsg();
    }
    
    void randomReserve () {
        // chose a random guest
        
        Record r = getRandomRecord();
        
        int id[] = new int[1];
        
        ErrorAndLogMsg m = client.reserveHotel(
                r.guestID, r.shortName, r.roomType, r.checkInDate, r.checkOutDate, id);
        
        r.resID = id[0];
        
        if (m==null || !m.anyError()) {
            ArrayList <Record> list = records.get(r.guestID);
            
            if (list ==null) {
                list = new ArrayList <Record> ();
                records.put(r.guestID, list);
            }
            
            list.add(r);
        }
        else if (m.error != ErrorCode.ROOM_UNAVAILABLE)
            // there is a problem unless room unavailable
            m.printMsg();
    }
    
    void randomCancelValid() {
        
        String guestID = guestIDs.get(rand.nextInt(guestIDs.size()));

        ArrayList <Record> list = records.get(guestID);
        
        if (list==null || list.size() <=0) return;
        
        int i = rand.nextInt( list.size());
        Record r = list.get(i);
        
        ErrorAndLogMsg m = client.cancelHotel(
                r.guestID, r.shortName, r.roomType, r.checkInDate, r.checkOutDate);
        
        if (m==null || !m.anyError())
            list.remove(i);
        else
            // there is a problem
            m.printMsg();
    }
    
    void randomCancelInvalid () {
        
        Record r = getRandomRecord();
        
        // find out if this happens to be within the days of a reserved record
        ArrayList <Record> list = records.get(r.guestID);
        
        if (list!=null) {
            for (Record rr : list) {
                int c1 = rr.checkInDate.compareTo(r.checkInDate);
                int c2 = rr.checkOutDate.compareTo(r.checkOutDate);
                
                if (c1<=0 && c2>=0) // just ignore it
                    return;
            }
        }
        
        ErrorAndLogMsg m = client.cancelHotel(
                r.guestID, r.shortName, r.roomType, r.checkInDate, r.checkOutDate);
        
        if (m==null || !m.anyError()) {
            // should not be a valid record
            ErrorAndLogMsg.InfoMsg("cancel record return true but no record found");//, info)
        } else // not success
            if (m.error != ErrorCode.RECORD_NOT_FOUND)
                m.printMsg(); // some error
            
    }
    
    void randomRecordQueryPerID () {
        
        String guestID = guestIDs.get(rand.nextInt(guestIDs.size()));

        ArrayList<Record> list = records.get(guestID);
        
        int cnt = 0;
        
        if (list!=null) cnt = list.size();
        
        int return_cnt = 0;

        ArrayList<Record> records = new ArrayList<Record> (100);
        ErrorAndLogMsg m = client.getReserveRecords(guestID, records);
        
        if (m!=null && m.anyError())
            m.printMsg();
        else
            return_cnt += records.size();
                    
        if (cnt != return_cnt) 
            ErrorAndLogMsg.InfoMsg("records count mismatch for query of ID:" + guestID +
                    " expected:" + cnt + " returned:" + return_cnt);
    }
    
    void randomTransferRecordValid() {
        
        String guestID = guestIDs.get(rand.nextInt(guestIDs.size()));

        ArrayList <Record> list = records.get(guestID);
        
        if (list==null || list.size() <=0) return;
        
        int i = rand.nextInt( list.size());
        Record r = list.get(i);
        
        // get a random target hotel (instead of itself)
        String toHotel = null;
        do {
            i = rand.nextInt (serverNames.length);
            toHotel = serverNames[i];
        } while (toHotel.equals(r.shortName));
        
        int [] idHolder = new int[1];
        idHolder[0] = r.resID;
        
        ErrorAndLogMsg m = client.transferRoom(
                r.guestID, idHolder, r.shortName, r.roomType, r.checkInDate, r.checkOutDate,
                toHotel);
        
        if (m==null || !m.anyError()) {
            //successful transferred
            //update the record with new ID and new Hotel
            r.resID = idHolder[0];
            r.shortName = toHotel;
        }
        else if (m.error!=ErrorCode.ROOM_UNAVAILABLE)
            // there is a problem
            m.printMsg();
        
    }
    
    // args[0], host of registry
    // args[1], port of registry
    // args[2], prefix of guestID
    // args[3], number of guestIDs
    // args[4], total days
    // args[5], total time (minutes)
    // args[6], silent  (Y, N)
    // args[7], duplicate how many client runners

    public HotelClientStressTester(String args[]) {
        
        rand = new Random();

        int nIDs = Integer.parseInt(args[3]);
        
        IDPrefix = String.valueOf(args[2]);
        
        guestIDs = new ArrayList<String> (nIDs);
        
        for (int i=0; i<nIDs; i++) {
            int dd = rand.nextInt(10000);
            guestIDs.add(IDPrefix + String.valueOf(dd));
        }
        
        records = new HashMap<String, ArrayList<Record> > (); // (20000);
        
        totalDays = Integer.parseInt(args[4]);
        
        client = new HotelClient();
                
        ErrorAndLogMsg m = client.Initialize(args[0], Integer.parseInt(args[1]));
                
        m.printMsg(); 
        
        // get the names of servers
        HotelServerWrapper[] servers = client.getServers();
        
        serverNames = new String[servers.length];
        for (int i=0; i < serverNames.length; i++)
            serverNames[i] = servers[i].prof.shortName;
        
                
        for (String hotel : serverNames) {
            client.loginAsManager(hotel, "manager", "pass");
        }
        
        minutes = Integer.parseInt(args[5]);

     }
    
    public void randomServiceReport() {
        int i = rand.nextInt(totalDays);
        
        SimpleDate today = new SimpleDate();
        
        SimpleDate date = SimpleDate.getDateByDelta (today, i);
        
        String hotel = serverNames[rand.nextInt(serverNames.length)];
        
        ArrayList<Record> records = new ArrayList<Record> (50);
        
        ErrorAndLogMsg m = client.getServiceReport(hotel, date, records);
        
        if (m!=null && m.anyError())
            m.printMsg();
    }
    
    public void randomStatusReport() {
        int i = rand.nextInt(totalDays);
        
        SimpleDate today = new SimpleDate();
        
        SimpleDate date = SimpleDate.getDateByDelta (today, i);
        
        String hotel = serverNames[rand.nextInt(serverNames.length)];
        
        ArrayList<Record> records = new ArrayList<Record> (50);
        
        ErrorAndLogMsg m = client.getStatusReport(hotel, date, records);
        
        if (m!=null && m.anyError())
            m.printMsg();
    }
    
    @Override
    public void run() {
        

        
        int [] pos_density = {
                30, // check avail
                20, // reserve
                18,// valid cancel
                2, // invalid cancel
                0, // query per ID
                5, // service report
                5, // status report 
                20 // transfer reservation
        };
        
        int[] event_ratios = new int[pos_density.length];
        {
            event_ratios[0] = pos_density[0];
            for (int i=1;i<event_ratios.length;i++) {
                event_ratios[i] = event_ratios[i-1] + pos_density[i];
            }
        };
        
        Random rand = new Random();
        
        long endTime = System.currentTimeMillis() + minutes * 60 * 1000;
        
        while ( System.currentTimeMillis() < endTime) {
            
            int r = rand.nextInt(100);
            
            if (r < event_ratios[0]) {
                // check avail
                randomCheckAvail();
            } else if (r < event_ratios[1]) {
                // reserve
                randomReserve();
            } else if (r < event_ratios[2]) {
                // valid cancel
                randomCancelValid();
            } else if (r < event_ratios[3]) {
                // invalid cancel
                randomCancelInvalid();
            } else if (r < event_ratios[4]) {
                // query per ID
                randomRecordQueryPerID();
            } else if (r < event_ratios[5]) {
                // service report
                randomServiceReport();
            } else if (r < event_ratios[6]) {
                // status report
                randomStatusReport();
            } else if (r < event_ratios[7]) {
                // transfer record valid
                randomTransferRecordValid();
            }
        }
   }
    
    // args[0], host of registry
    // args[1], port of registry
    // args[2], prefix of guestID
    // args[3], number of guestIDs
    // args[4], total days
    // args[5], total time (minutes)
    // args[6], silent  (Y, N)
    // args[7], duplicate how many client runners
   public static void main (String [] args) {
       
       
       ErrorAndLogMsg.addStream(MsgType.ERR, System.err);
       
       if (args[6].equals("N"))
               ErrorAndLogMsg.addStream(MsgType.LOG, System.out);
       
       ErrorAndLogMsg.addStream(MsgType.WARN, System.err);
       ErrorAndLogMsg.addStream(MsgType.INFO, System.err);
       
       
       int iRunners = Integer.parseInt(args[7]);
       
       for (int i=1; i<=iRunners; i++) {
           
           args[2] = String.valueOf(i);
           
           HotelClientStressTester tester = new HotelClientStressTester (args);
       
           Thread th = new Thread(tester);
           th.start();
       }
       
   }
    
}
