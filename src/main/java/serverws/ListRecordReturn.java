package serverws;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import server.ErrorAndLogMsg.ErrorCode;
import server.DataTypes.Record;

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
