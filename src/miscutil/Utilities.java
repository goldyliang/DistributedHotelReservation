package miscutil;

import java.util.EnumMap;

import CHotelServerInterface.CAvailability;
import CHotelServerInterface.CDate;
import CHotelServerInterface.CHotelProfile;
import CHotelServerInterface.CRoomType;
import HotelServerInterface.IHotelServer.Availability;
import HotelServerInterface.IHotelServer.HotelProfile;
import HotelServerInterface.IHotelServer.RoomType;

public class Utilities {

    public static CDate toCDate(SimpleDate dt) {
        return new CDate( (short)dt.getYear(), 
                (short)dt.getMonth(), 
                (short)dt.getDay());
    }
    
    public static SimpleDate toDate (CDate dtstru) {
        return new SimpleDate (dtstru.year, dtstru.month, dtstru.day);
    }
    
    public static HotelProfile toHotelProfile (CHotelProfile prof) {
        HotelProfile rProf = new HotelProfile();
        rProf.shortName = prof.shortName;
        rProf.fullName = prof.fullName;
        rProf.descText = prof.descText;
        rProf.totalRooms = new EnumMap <RoomType, Integer> (RoomType.class);
        rProf.rates = new EnumMap <RoomType, Float> (RoomType.class);

        rProf.allTotalRooms = 0;
        for (RoomType type: RoomType.values()) {
            Integer i = new Integer (prof.totalRooms[type.ordinal()]);
            rProf.totalRooms.put(type, i);
            rProf.allTotalRooms += i;
            
            Float r = new Float (prof.rates[type.ordinal()]);
            rProf.rates.put(type,r);
        }
        
        return rProf;
    }
    
    public static CHotelProfile toCHotelProfile (HotelProfile prof) {
        
        CHotelProfile rProf = new CHotelProfile();
        
        rProf.shortName = prof.shortName;
        rProf.fullName = prof.fullName;
        rProf.descText = prof.descText;
        
        rProf.totalRooms = new short[RoomType.values().length];
        
        for (RoomType type: RoomType.values()) {
            rProf.totalRooms[type.ordinal()] = prof.totalRooms.get(type).shortValue();
        }
        
        rProf.rates = new float[RoomType.values().length];
        
        for (RoomType type: RoomType.values()) {
            rProf.rates[type.ordinal()] = prof.rates.get(type);
        }
        
        return rProf;
    }
    
    public static RoomType toRoomType (CRoomType type) {
        return RoomType.values()[type.value()];
    }
    
    public static CRoomType toCRoomType (RoomType type) {
        return CRoomType.from_int(type.ordinal());
    }
    
    public static CAvailability toCAvailability (Availability avail) {
        
        return new CAvailability(
                avail.hotelName,
                (short)avail.availCount,
                avail.rate);        
    }
    
    public static Availability toAvailability (CAvailability avail) {
        
        Availability r = new Availability();
        r.hotelName = avail.hotelName;
        r.availCount = avail.availCount;
        r.rate = avail.rate;
        return r;        
    }

}
