package hotelbooking.server;

import hotelbooking.miscutil.SimpleDate;

public abstract class RoomCountsFactory {
	
	public static enum Feature {RWLock_SyncObj, RWLock_AtomicInteger, AllSyncObj};
	
	private static Feature defaultFeature = Feature.AllSyncObj;
	
	public static RoomCounts getRoomCounts (
			Feature feature,
			int totalRooms, SimpleDate startDate, int days) {
		
		switch (feature) {
		case AllSyncObj:
			return new RoomCounts_AllSyncObj (totalRooms, startDate, days);
		default:
			return null;
		}
	}
	
	public static RoomCounts getRoomCounts (int totalRooms, SimpleDate startDate, int days) {
		return getRoomCounts (defaultFeature, totalRooms, startDate, days);
	}
}
