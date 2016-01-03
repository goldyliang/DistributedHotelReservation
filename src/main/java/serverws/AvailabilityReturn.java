package serverws;

import java.util.List;

import server.ErrorAndLogMsg.ErrorCode;
import server.DataTypes.*;


public class AvailabilityReturn {
	public ErrorCode error;

	public List<Availability> avails;
	
	public AvailabilityReturn() {}
}
