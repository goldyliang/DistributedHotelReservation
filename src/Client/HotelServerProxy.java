package Client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.IntHolder;

import miscutil.SimpleDate;
import miscutil.Utilities;
import CHotelServerInterface.*;
import HotelServerInterface.ErrorAndLogMsg.ErrorCode;
import HotelServerInterface.ErrorAndLogMsg;
import HotelServerInterface.IHotelServer;
import HotelServerInterface.IHotelServerManager;

// This is a "PROXY" class that invoked by the Client,
// providing the same interface of that from RMI Remote object.
// Under this class, it changes to invoke the corresponding RPC methods provided by
// a CORBA object instead.

public class HotelServerProxy implements IHotelServer {
    
    CIHotelServer serverObj;
    
    // Construct the object with an Corba remote object
    HotelServerProxy (CIHotelServer serverObj) {
        this.serverObj = serverObj;
    }

    @Override
    public HotelProfile getProfile() throws RemoteException {
        
        CHotelProfileHolder profile = new CHotelProfileHolder();
        
        int err = serverObj.getProfile(profile);
        
        if ( err!= ErrorCode.SUCCESS.ordinal() ||
             profile.value ==null)
            return null;
        else
            return Utilities.toHotelProfile(profile.value);
    }

    @Override
    public ErrorCode reserveRoom(String guestID, RoomType roomType,
            SimpleDate checkInDate, SimpleDate checkOutDate, int id) throws RemoteException {
        
        int r = this.serverObj.reserveHotel(
                guestID,
                Utilities.toCRoomType (roomType),
                Utilities.toCDate (checkInDate),
                Utilities.toCDate (checkOutDate),
                id);
        
        ErrorCode ret = ErrorCode.values()[r];
                
        return ret;
    }

    @Override
    public ErrorCode cancelRoom(String guestID, RoomType roomType,
            SimpleDate checkInDate, SimpleDate checkOutDate) throws RemoteException {
        
        int r = this.serverObj.cancelHotel(
                guestID,
                Utilities.toCRoomType (roomType),
                Utilities.toCDate (checkInDate),
                Utilities.toCDate (checkOutDate));
        
        ErrorCode ret = ErrorCode.values()[r];
        
        return ret;
    }

    @Override
    public List<Availability> checkAvailability(String guestID,
            RoomType roomType, SimpleDate checkInDate, SimpleDate checkOutDate)
            throws RemoteException {

        CListAvailabilityHolder holder = new CListAvailabilityHolder();
        
        int r = this.serverObj.checkAvailability(
                guestID,
                Utilities.toCRoomType (roomType),
                Utilities.toCDate (checkInDate),
                Utilities.toCDate (checkOutDate),
                holder);
        
        ErrorCode ret = ErrorCode.values()[r];
        
        List <Availability> list = new ArrayList <Availability> ();
        
        if (holder.value!=null)
            for (CAvailability avail: holder.value) {
                list.add(Utilities.toAvailability (avail));
            }
        
        return list;
    }

    @Override
    public Record[] getReserveRecords(String guestID) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IHotelServerManager getManagerObject(String guestID, String passWord)
            throws RemoteException {
        
        CIHotelServerManagerHolder mgrHolder = new CIHotelServerManagerHolder();
        
        int r = serverObj.getManagerObject(guestID, passWord, mgrHolder);
        
        ErrorCode err = ErrorCode.values()[r];
        
        if (err==ErrorCode.SUCCESS && mgrHolder.value!=null) {
            
            // return a wrapper object with RMI like interface
            return new HotelServerManagerProxy(mgrHolder.value);
        } else
            return null;
    }

    @Override
    public ErrorCode transferRoom(String guestID, int reservationID,
            RoomType roomType, SimpleDate checkInDate, SimpleDate checkOutDate,
            String targetHotel, int newID) {
                
        int r = this.serverObj.transferReservation(
                guestID,
                reservationID,
                Utilities.toCRoomType (roomType),
                Utilities.toCDate (checkInDate),
                Utilities.toCDate (checkOutDate),
                targetHotel,
                newID);
        
        ErrorCode ret = ErrorCode.values()[r];
                
        return ret;

    }

}
