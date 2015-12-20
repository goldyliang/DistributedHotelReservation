package hotelbooking.server;

import java.util.Collection;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import hotelbooking.CorbaInterface.CDate;
import hotelbooking.CorbaInterface.CIHotelServerManager;
import hotelbooking.CorbaInterface.CIHotelServerManagerPOA;
import hotelbooking.CorbaInterface.CListRecordHolder;
import hotelbooking.CorbaInterface.CRecord;
import hotelbooking.miscutil.Utilities;
import hotelbooking.serverinterface.ErrorAndLogMsg;
import hotelbooking.serverinterface.IHotelServerManager;
import hotelbooking.serverinterface.ErrorAndLogMsg.ErrorCode;
import hotelbooking.serverinterface.IHotelServer.Record;

public class HotelServerManagerImpl extends CIHotelServerManagerPOA {

    IHotelServerManager server;
    
    public HotelServerManagerImpl (IHotelServerManager srv) {
        server = srv;
    }
    
    @Override
    public int getServiceReport(CDate serviceDate, CListRecordHolder records) {
        
        try {
            Record[] rec = server.getServiceReport(Utilities.toDate(serviceDate));
            
            if (rec==null | rec.length==0) {
                records.value = new CRecord[0]; // can not let value be null
                return ErrorCode.RECORD_NOT_FOUND.ordinal();
            }
            
            
            records.value = new CRecord[rec.length];
            
            int i=0;
            for (Record r: rec) {
                records.value[i++] = new CRecord (
                        r.resID,
                        r.guestID,
                        r.shortName,
                        Utilities.toCRoomType(r.roomType),
                        Utilities.toCDate(r.checkInDate),
                        Utilities.toCDate(r.checkOutDate),
                        r.rate);
            }
            
            return ErrorCode.SUCCESS.ordinal();
            
        } catch (Exception e) {
            ErrorAndLogMsg.ExceptionErr(e, "Exception when getting service report.").printMsg();
            return ErrorCode.EXCEPTION_THROWED.ordinal();
        }

    }

    @Override
    public int getStatusReport(CDate serviceDate, CListRecordHolder records) {
        try {
            Record[] rec = server.getStatusReport(Utilities.toDate(serviceDate));
            
            if (rec==null | rec.length==0) {
                records.value = new CRecord[0]; // can not let value be null
                return ErrorCode.RECORD_NOT_FOUND.ordinal();
            }
            
            records.value = new CRecord[rec.length];
            
            int i=0;
            for (Record r: rec) {
                records.value[i++] = new CRecord (
                        r.resID,
                        r.guestID,
                        r.shortName,
                        Utilities.toCRoomType(r.roomType),
                        Utilities.toCDate(r.checkInDate),
                        Utilities.toCDate(r.checkOutDate),
                        r.rate);
            }
            
            return ErrorCode.SUCCESS.ordinal();
            
        } catch (Exception e) {
            ErrorAndLogMsg.ExceptionErr(e, "Exception when getting service report.").printMsg();
            return ErrorCode.EXCEPTION_THROWED.ordinal();
        }
    }

	@Override
	public int getRecordsSnapshot(CListRecordHolder records) {
		
		Collection <Record> colRec = server.getReserveRecordSnapshot();
		
		records.value = new CRecord[colRec.size()];
		
		int i = 0;
		for (Record r : colRec) {
			records.value[i++] = new CRecord (
                    r.resID,
                    r.guestID,
                    r.shortName,
                    Utilities.toCRoomType(r.roomType),
                    Utilities.toCDate(r.checkInDate),
                    Utilities.toCDate(r.checkOutDate),
                    r.rate);
		}
		
		return ErrorCode.SUCCESS.ordinal();
	} 

}
