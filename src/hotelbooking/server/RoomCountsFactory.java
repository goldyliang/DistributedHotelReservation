package hotelbooking.server;

import hotelbooking.miscutil.SimpleDate;

public abstract class RoomCountsFactory {
	
	public static enum Feature {RWLock_SyncObj, RWLock_AtomicInteger};
	
	private static Feature defaultFeature = Feature.RWLock_SyncObj;
	
	public static RoomCounts getRoomCounts (
			Feature feature,
			int totalRooms, SimpleDate startDate, int days) {
		
		switch (feature) {
		case RWLock_SyncObj:
			return new AvailableRoomCounts (totalRooms, startDate, days);
		default:
			return null;
		}
	}
	
	public static RoomCounts getRoomCounts (int totalRooms, SimpleDate startDate, int days) {
		return getRoomCounts (defaultFeature, totalRooms, startDate, days);
	}
}
