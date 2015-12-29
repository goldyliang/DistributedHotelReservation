package hotelbooking.server;

import java.util.List;


import hotelbooking.CorbaInterface.CAvailability;
import hotelbooking.CorbaInterface.CDate;
import hotelbooking.CorbaInterface.CHotelProfileHolder;
import hotelbooking.CorbaInterface.CIHotelServerManager;
import hotelbooking.CorbaInterface.CIHotelServerManagerHolder;
import hotelbooking.CorbaInterface.CIHotelServerPOA;
import hotelbooking.CorbaInterface.CINetSocketAddress;
import hotelbooking.CorbaInterface.CListAvailabilityHolder;
import hotelbooking.CorbaInterface.CRoomType;
import hotelbooking.miscutil.Utilities;
import hotelbooking.serverinterface.ErrorAndLogMsg;
import hotelbooking.serverinterface.IHotelServer;
import hotelbooking.serverinterface.IHotelServerManager;
import hotelbooking.serverinterface.ErrorAndLogMsg.ErrorCode;
import hotelbooking.serverinterface.IHotelServer.Availability;
import hotelbooking.serverinterface.IHotelServer.HotelProfile;

public class HotelServerImpl extends CIHotelServerPOA {

    IHotelServer server;
    
    public HotelServerImpl (IHotelServer server) {
        this.server = server;
    }
    
    @Override
    public int getProfile(CHotelProfileHolder profile) {
        
        try {
            HotelProfile prof = server.getProfile();
            profile.value = Utilities.toCHotelProfile (prof);
            return ErrorCode.SUCCESS.ordinal();
        } catch (Exception e) {
            return ErrorCode.EXCEPTION_THROWED.ordinal();
        } 
    }

    @Override
    public int reserveHotel(String guestID, CRoomType roomType,
            CDate checkInDate, CDate checkOutDate, int reservationID) {
                
        try {
            ErrorCode r = server.reserveRoom(guestID, Utilities.toRoomType(roomType), 
                    Utilities.toDate(checkInDate), 
                    Utilities.toDate(checkOutDate), 
                    reservationID);
                        
            return r.ordinal();
            
        } catch (Exception e) {
            //e.printStackTrace(); // shall not happen for CORBA, just ignore
            ErrorAndLogMsg.ExceptionErr(e, "Exception reservating hotel.").printMsg();

            return ErrorCode.EXCEPTION_THROWED.ordinal();
        }
    }

    @Override
    public int cancelHotel(String guestID, CRoomType roomType,
            CDate checkInDate, CDate checkOutDate) {
        
        try {
            ErrorCode r = server.cancelRoom(guestID, Utilities.toRoomType(roomType), 
                    Utilities.toDate(checkInDate), 
                    Utilities.toDate(checkOutDate) );
                        
            return r.ordinal();
            
        } catch (Exception e) {
            //e.printStackTrace(); // shall not happen for CORBA, just ignore
            ErrorAndLogMsg.ExceptionErr(e, "Exception reservating hotel.").printMsg();

            return ErrorCode.EXCEPTION_THROWED.ordinal();
        }
    }
    
    @Override
    public int checkAvailability(String guestID, CRoomType roomType,
            CDate checkInDate, CDate checkOutDate,
            CListAvailabilityHolder listAvail) {
        
        try {
            List <Availability> avails = server.checkAvailability(
                    guestID, Utilities.toRoomType(roomType), 
                    Utilities.toDate(checkInDate), 
                    Utilities.toDate(checkOutDate));
            
            if (avails!=null) {
                listAvail.value = new CAvailability[avails.size()];
                int i=0;
                for (Availability avail: avails) {
                    listAvail.value[i++] = Utilities.toCAvailability(avail);
                }
            }
            else
                listAvail.value = new CAvailability[0]; // can not let it be null
             
            return ErrorCode.SUCCESS.ordinal();
            
        } catch (Exception e) {
            //e.printStackTrace(); // shall not happen for CORBA, just ignore
            ErrorAndLogMsg.ExceptionErr(e, "Exception reservating hotel.").printMsg();

            return ErrorCode.EXCEPTION_THROWED.ordinal();
        } 
    }

    @Override
    public int getManagerObject (String manageID, String passCode,
            CIHotelServerManagerHolder serverManager) {
        
        // invoke server for login check up
        IHotelServerManager srvMgr;
        try {
            srvMgr = server.getManagerObject(manageID, passCode);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            ErrorAndLogMsg.ExceptionErr(e, "Exception when retrieving server object").printMsg();
            return ErrorCode.EXCEPTION_THROWED.ordinal();
        }
        
        if (srvMgr != null) {
            // login success
            
            // get the CORBA server object
            // do some tricky thing here, as server is actually a type of HotelServer
            // down cast it so as to be able to invoke getManagerObject_Corba
            
            CIHotelServerManager svrMgrRef = 
                    ((HotelServer)server).getManagerObject_Corba();
            
            
            // fill the object into the para
            //serverManager.value = svrMgrImpl._this();  // this is not correct
            serverManager.value = svrMgrRef;
        
            return ErrorCode.SUCCESS.ordinal();
        } else
            return ErrorCode.MGR_LOGIN_FAILURE.ordinal();
    }

    @Override
    public int transferReservation(
            String guestID, int reservationID,
            CRoomType roomType, CDate checkInDate, CDate checkOutDate,
            String targetHotel,
            int newID) {
        
        try {
            ErrorCode r = server.transferRoom(
                    guestID, 
                    reservationID,
                    Utilities.toRoomType(roomType),
                    Utilities.toDate(checkInDate), 
                    Utilities.toDate(checkOutDate), 
                    targetHotel,
                    newID);
                        
            return r.ordinal();
            
        } catch (Exception e) {
            //e.printStackTrace(); // shall not happen for CORBA, just ignore
            ErrorAndLogMsg.ExceptionErr(e, "Exception reservating hotel.").printMsg();

            return ErrorCode.EXCEPTION_THROWED.ordinal();
        }
    }

    @Override
    public int addQuerySockets(CINetSocketAddress[] sockets) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int deleteQuerySockets(CINetSocketAddress[] sockets) {
        // TODO Auto-generated method stub
        return 0;
    }


}
