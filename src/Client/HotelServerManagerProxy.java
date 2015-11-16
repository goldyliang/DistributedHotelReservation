package Client;

import java.rmi.RemoteException;

import miscutil.SimpleDate;
import miscutil.Utilities;
import CHotelServerInterface.CIHotelServerManager;
import CHotelServerInterface.CListRecordHolder;
import CHotelServerInterface.CRecord;
import HotelServerInterface.IHotelServer.Record;
import HotelServerInterface.IHotelServerManager;

public class HotelServerManagerProxy implements IHotelServerManager {
    CIHotelServerManager server;

    HotelServerManagerProxy (CIHotelServerManager srv) {
        server = srv;
    }
    
    @Override
    public Record[] getServiceReport(SimpleDate serviceDate) throws RemoteException {
        
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
    public Record[] getStatusReport(SimpleDate  date) throws RemoteException {
        
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

}
