package HotelServer;

import java.io.InputStream;
import java.util.ArrayList;
//import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import miscutil.SimpleDate;
import HotelServerInterface.IHotelServer.Record;
import HotelServerInterface.IHotelServer.RoomType;


/*
 * Store reserver records for one hotel for all types of rooms
 * Provide necessary operation and query towards the records
 * Thread-safe and provide concurrency operations as much as possible
 * 
 * This class together with AvailableRoomCounts, consist of a complete data
 * of reservation for one Hotel.
 */
public class ReserveRecords {

    ConcurrentHashMap 
  //      HashMap
                      < String, //GuestID as the key
                        List<Record> // List of records of this guest (unsorted)
                      >
        mapRecords;
        
    
    public ReserveRecords () {
        super();
        mapRecords = new 
                //HashMap
                ConcurrentHashMap 
                <String, List<Record>> ();
    }
    
    // Add a reservation record.
    // The record is copied and inserted in the list mapped by key guestID
    // Shall return true always
    public boolean makeReservation (String guestID, Record record) {
        
        List<Record> rec_list = mapRecords.get(guestID);

        if (rec_list == null) {
            rec_list = new ArrayList <Record> (10); // a reasonable initial capacity
            mapRecords.put( guestID, rec_list);
        }
        
        // though the access to ConcurrentMap is thread-safe,
        // the access within the value still needs synchronized
        synchronized (rec_list) {
            rec_list.add (new Record (record));
        }
        
        return true;
    }
    
    
    // Delete one or partial of the reservation
    // If the record is found within the range of dates,
    //    update the record or delete it, and return true;
    //    use newResID for the new record if the original one is splitted
    // If no any record found, do nothing and return false;
    public boolean cancelReservation (String guestID, Record record, int newResID) {
        List<Record> rec_list = mapRecords.get(guestID);
        if (rec_list == null) return false;
        
        synchronized (rec_list) {
        
            int p1=-1, // the 1st priority of match
                p2=-1, // the 2nd priority of match
                p3=-1; // the 3rd priority of match
            
            for (int i=0; i<rec_list.size();i++) {
                Record rec = rec_list.get(i);
                
                if (rec.roomType == record.roomType) {
                    int c1 = rec.checkInDate.compareTo(record.checkInDate);
                    int c2 = rec.checkOutDate.compareTo(record.checkOutDate);
                    
                    if (c1==0 && c2==0) {
                        //Found exactly the record, delete it
                        rec_list.remove(i); // remove rec from the list.
                        return true;
                    } else if (c1<0 && c2==0) {
                        //The cancel range is at the end side, mark as priority 2
                        p2 = i;
                    } else if (c1==0 && c2>0) {
                        //The cancel range is at the beginning, mark as priority 2
                        p2 = i;
                    } else if (c1<0 && c2>0) {
                        //The cancel dates in the middle, mark as priority 3
                        p3 = i;
                     }
                }
            }
            
            if (p2>=0) {
                Record rec = rec_list.get(p2);
                
                int c1 = rec.checkInDate.compareTo(record.checkInDate);
                int c2 = rec.checkOutDate.compareTo(record.checkOutDate);

                if (c1<0 && c2==0) {
                    //The cancel range is at the end side, just update outDate
                    rec.checkOutDate = record.checkInDate;
                    return true;
                } else if (c1==0 && c2>0) {
                    //The cancel range is at the beginning, update inDate
                    rec.checkInDate = record.checkOutDate;
                    return true;
                }
                return false;
            }
            
            if (p3>=0) {
                Record rec = rec_list.get(p3);
                
                int c1 = rec.checkInDate.compareTo(record.checkInDate);
                int c2 = rec.checkOutDate.compareTo(record.checkOutDate);
                
                if (c1<0 && c2>0) {
                    //The cancel dates in the middle, split into two records
                    
                    //Current record reflect the earlier part
                    SimpleDate o_out = rec.checkOutDate;
                    rec.checkOutDate = record.checkInDate;
                    
                    //Add a record reflecting the later part
                    Record r_new = new Record (
                            newResID, // use new ID provided
                            guestID,
                            rec.shortName,
                            rec.roomType, 
                            record.checkOutDate, 
                            o_out,
                            rec.rate);
                    r_new.checkInDate = record.checkOutDate;
                    rec_list.add (r_new);
                    return true;
                } else
                    return false;
            }
            
            /*
            Iterator <Record> iter = rec_list.listIterator();
            
            
            while ( iter.hasNext() ) {
                
                Record rec = iter.next();
                
                if (rec.roomType == record.roomType) {
                    int c1 = rec.checkInDate.compareTo(record.checkInDate);
                    int c2 = rec.checkOutDate.compareTo(record.checkOutDate);
                    
                    if (c1==0 && c2==0) {
                        //Found exactly the record, delete it
                        iter.remove(); // remove rec from the list.
                        return true;
                    } else if (c1<0 && c2==0) {
                        //The cancel range is at the end side, just update outDate
                        rec.checkOutDate = record.checkInDate;
                        return true;
                    } else if (c1==0 && c2>0) {
                        //The cancel range is at the beginning, update inDate
                        rec.checkInDate = record.checkOutDate;
                        return true;
                    } else if (c1<0 && c2>0) {
                        //The cancel dates in the middle, split into two records
                        
                        //Current record reflect the earlier part
                        Date o_out = rec.checkOutDate;
                        rec.checkOutDate = record.checkInDate;
                        
                        //Add a record reflecting the later part
                        Record r_new = new Record (
                                guestID,
                                rec.shortName,
                                rec.roomType, 
                                record.checkOutDate, 
                                o_out,
                                rec.rate);
                        r_new.checkInDate = record.checkOutDate;
                        rec_list.add (r_new);
                        return true;
                    }
                }
            } */
        }
        
        //not record found, return false
        return false;
    }
    
    // Get a snap shot of the records for guestID
    public Record[] getRecords (String guestID) {
        
        Record [] ret;
        
        List <Record>  ll = mapRecords.get(guestID);
        
        if (ll==null) return null;
        
        synchronized (ll) {
            ret = ll.toArray(new Record[0]);
        }
        
        return ret;
    }
    
    public Record findRecord (String guestID, int resID) {
        List <Record> ll = mapRecords.get(guestID);
        
        if (ll==null) return null;
        
        synchronized (ll) {
            for (Record r:ll)
                if (r.resID == resID)
                    return r;
        }
    
        return null;
    }
    
    public Record findRecord (String guestID, RoomType type, 
            SimpleDate inDate, SimpleDate outDate) {
        List <Record> ll = mapRecords.get(guestID);
        
        if (ll==null) return null;
        
        synchronized (ll) {
            for (Record r:ll)
                if (r.roomType == type &&
                    r.checkInDate.equals(inDate) &&
                    r.checkOutDate.equals(outDate))
                    return r;
        }
    
        return null;
    }
    
    interface IRecordOperation {
        public void doOperation (Record r);
    }
    
    public void traverseAllRecords (IRecordOperation oper) {
        
        Set<String> ids = mapRecords.keySet();
        
        if (ids==null) return;
        
        Iterator<String> iter = ids.iterator();
        
        while (iter.hasNext()) {
            
            String guestID = iter.next();
            
            //System.out.println ("ID-" + guestID);
            
            List <Record> records = mapRecords.get(guestID);
            
            if (records!=null) {
                synchronized (records) {
                    for (Record r: records) 
                        oper.doOperation(r);
                }
            } else //it should not happen ,throw an RuntimeException
                throw new RuntimeException ("Map Record key w/o value");
        }
    }
    
    public boolean LoadRecords(InputStream s) {
        return true;
    }
    
    public boolean SafeRecords(InputStream s) {
        return true;
    }
    
    public String toString() {
        
        return mapRecords.toString();
    }
}
