package serverws;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import common.ErrorAndLogMsg.ErrorCode;
import common.HotelServerTypes.Record;

@XmlRootElement
public class ManagerReturn {
	
	public ErrorCode error;
	
	public List<Record> listRecord;
	
	public ManagerReturn () {}
	
	public ManagerReturn (ErrorCode error) {
		this.error = error;
		this.listRecord = null;
	}
}
