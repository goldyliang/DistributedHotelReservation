package serverws;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import HotelServer.ErrorAndLogMsg.ErrorCode;
import HotelServer.HotelServerTypes.Record;

@XmlRootElement
public class ListRecordReturn {
	
	public ErrorCode error;
	
	public List<Record> listRecord;
	
	public ListRecordReturn () {}
	
	public ListRecordReturn (ErrorCode error) {
		this.error = error;
		this.listRecord = null;
	}
}
