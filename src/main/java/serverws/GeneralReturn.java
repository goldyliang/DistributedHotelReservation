package serverws;

import javax.xml.bind.annotation.XmlRootElement;

import HotelServer.ErrorAndLogMsg.ErrorCode;

/*
 * Class for general return information (ErrorCode, and ReserverationID (if any)
 */
@XmlRootElement
public class GeneralReturn {

	
	public ErrorCode error;
	
	public int resID;
	
	public GeneralReturn () {}
	
	public GeneralReturn (ErrorCode error, int resID) {
		this.error = error;
		this.resID = resID;
	}
}
