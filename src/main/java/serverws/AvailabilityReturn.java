package serverws;

import java.util.List;

import HotelServer.ErrorAndLogMsg.ErrorCode;
import HotelServer.HotelServerTypes.*;


public class AvailabilityReturn {
	public ErrorCode error;

	public List<Availability> avails;
	
	public AvailabilityReturn() {}
}
