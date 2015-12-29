package hotelbooking.client;

import java.util.ArrayList;
import java.util.Collection;

import hotelbooking.CorbaInterface.CIHotelServerManager;
import hotelbooking.CorbaInterface.CListRecordHolder;
import hotelbooking.CorbaInterface.CRecord;
import hotelbooking.miscutil.SimpleDate;
import hotelbooking.miscutil.Utilities;
import hotelbooking.serverinterface.IHotelServerManager;
import hotelbooking.serverinterface.IHotelServer.Record;

public class HotelServerManagerProxy implements IHotelServerManager {
    CIHotelServerManager server;

    HotelServerManagerProxy (CIHotelServerManager srv) {
        server = srv;
    }
    
    @Override
    public Record[] getServiceReport(SimpleDate serviceDate){
        
        CListRecordHolder list = new CListRecordHolder();
        
        server.getServiceReport(Utilities.toCDate(serviceDate), list);
        
        if (list.value==null || list.value.length == 0) 
            return null;
        
        else {
            Record[] r = new Record[list.value.length];
            
            int i=0;
            
            for (CRecord rec : list.value) {
                      
                r [i++] = new Record (
                        rec.reserveID,
                        rec.guestID,
                        rec.hotelName,
                        Utilities.toRoomType(rec.roomType),
                        Utilities.toDate(rec.checkInDate),
                        Utilities.toDate(rec.checkOutDate),
                        rec.rate);
            }
            
            return r;
        }
    }

    @Override
    public Record[] getStatusReport(SimpleDate  date) {
        
        CListRecordHolder list = new CListRecordHolder();
        
        server.getStatusReport(Utilities.toCDate(date), list);
        
        if (list.value==null || list.value.length == 0) 
            return null;
        
        else {
            Record[] r = new Record[list.value.length];
            
            int i=0;
            
            for (CRecord rec : list.value) {
                      
                r [i++] = new Record (
                        rec.reserveID,
                        rec.guestID,
                        rec.hotelName,
                        Utilities.toRoomType(rec.roomType),
                        Utilities.toDate(rec.checkInDate),
                        Utilities.toDate(rec.checkOutDate),
                        rec.rate);
            }
            
            return r;
        }

    }

	@Override
	public Collection<Record> getReserveRecordSnapshot() {

		CListRecordHolder list = new CListRecordHolder();
		
		server.getRecordsSnapshot(list);
		
        if (list.value==null || list.value.length == 0) 
            return null;
        
        else {
            Collection<Record> r = new ArrayList<Record> ();
            
            for (CRecord rec : list.value) {
                      
            	
                r.add( new Record (
                        rec.reserveID,
                        rec.guestID,
                        rec.hotelName,
                        Utilities.toRoomType(rec.roomType),
                        Utilities.toDate(rec.checkInDate),
                        Utilities.toDate(rec.checkOutDate),
                        rec.rate));
            }
            
            return r;
        }
	}

}
