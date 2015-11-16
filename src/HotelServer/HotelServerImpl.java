package HotelServer;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import miscutil.Utilities;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import CHotelServerInterface.CAvailability;
import CHotelServerInterface.CDate;
import CHotelServerInterface.CHotelProfileHolder;
import CHotelServerInterface.CIHotelServerManager;
import CHotelServerInterface.CIHotelServerManagerHelper;
import CHotelServerInterface.CIHotelServerManagerHolder;
import CHotelServerInterface.CIHotelServerPOA;
import CHotelServerInterface.CINetSocketAddress;
import CHotelServerInterface.CListAvailabilityHolder;
import CHotelServerInterface.CRoomType;
import HotelServerInterface.ErrorAndLogMsg;
import HotelServerInterface.IHotelServer;
import HotelServerInterface.ErrorAndLogMsg.ErrorCode;
import HotelServerInterface.IHotelServer.Availability;
import HotelServerInterface.IHotelServer.HotelProfile;
import HotelServerInterface.IHotelServerManager;

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
            CDate checkInDate, CDate checkOutDate, IntHolder reservationID) {
        
        int [] id = new int[1];
        
        try {
            ErrorCode r = server.reserveRoom(guestID, Utilities.toRoomType(roomType), 
                    Utilities.toDate(checkInDate), 
                    Utilities.toDate(checkOutDate), 
                    id);
            
            reservationID.value = id[0];
            
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
            IntHolder newID) {
        int [] id = new int[1];
        
        try {
            ErrorCode r = server.transferRoom(
                    guestID, 
                    reservationID,
                    Utilities.toRoomType(roomType),
                    Utilities.toDate(checkInDate), 
                    Utilities.toDate(checkOutDate), 
                    targetHotel,
                    id);
            
            newID.value = id[0];
            
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
