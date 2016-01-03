package server;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;


// Helper class to store, return, pass information and errors

public class ErrorAndLogMsg {
	
	public enum MsgType {INFO, WARN, ERR, LOG};
	
	public enum ErrorCode {
		SUCCESS,
		HOTEL_NOT_FOUND, // Can not find the hotel by name
		RECORD_NOT_FOUND, // No current record found as requested
		ROOM_UNAVAILABLE, // No available room as requested
		INTERNAL_ERROR, // Error of internal data
		INVALID_DATES,  // Invalid date input
		INVALID_GUEST_ID, // Invalid guest ID
		EXCEPTION_THROWED, // Various Runtime exception throwed (except for the RemoteException
		REGISTRY_CONNECTION_FAILURE,
		SERVER_CONNECTION_FAILURE,
		MGR_LOGIN_FAILURE,
		MSG_DECODE_ERR, 
		TIME_OUT, 
		INVALID_REQUEST
	}
	
	public String info;
	public MsgType type;
	public ErrorCode error;
	public Exception exception;
	
	static private EnumMap <MsgType, ArrayList<PrintStream> > streams;
	
	static {
		streams = new EnumMap<MsgType, ArrayList<PrintStream> > (MsgType.class);
	}
	
	public static void addStream (MsgType type, PrintStream stream) {
		
		ArrayList<PrintStream> arr = streams.get(type);
		
		if (arr==null) {
			arr = new ArrayList<PrintStream> (2);
			streams.put(type, arr);
		}
		
		arr.add(stream);
	}
	

	
	public ErrorAndLogMsg (MsgType type, String info) {
		this.type = type;
		this.info = info;
		error = null;
		exception = null;
	}
	
	public void addErr (ErrorCode err) {
		error = err;
	}
	
	public void addException (Exception e) {
		exception = e;
	}
	
	// Print to preset PrintStream
	public void printMsg () {
		
		ArrayList <PrintStream> sts = streams.get(type);
		
		if (sts==null) return;
		
		for (PrintStream st : sts)
			print (st);
	}
	
	public void print ( PrintStream st) {
		if (st==null) return;
		
		synchronized (st) {
			st.print (type.toString() + ":");
			st.println(info);
			
			if (error!=null) {
				st.print ("ErrCode:");
				st.println (error.toString());
			}
	
			if (exception!=null) {
				st.print ("Exception:");
				exception.printStackTrace(st);
			}
		}
	}
	
	static private boolean autoPrint = false;

	public static void setAutoPrint (boolean auto) {
		autoPrint = auto;
	}
	
	public static ErrorAndLogMsg ExceptionErr (Exception e, String info) {
		ErrorAndLogMsg  m = new ErrorAndLogMsg (MsgType.ERR, info);
		m.addErr (ErrorCode.EXCEPTION_THROWED);
		m.addException(e);
		if (autoPrint) m.printMsg();
		return m;
	}
	
	public static ErrorAndLogMsg GeneralErr (ErrorCode err, String info) {
		ErrorAndLogMsg  m = new ErrorAndLogMsg (MsgType.ERR, info);
		m.addErr (err);
		if (autoPrint) m.printMsg();
		return m;		
	}
	
	public static ErrorAndLogMsg InfoMsg (String info) {
		ErrorAndLogMsg  m = new ErrorAndLogMsg (MsgType.INFO, info);
		if (autoPrint) m.printMsg();
		return m;
	}
	
	public static ErrorAndLogMsg LogMsg (String log) {
		Date now = new Date();
		ErrorAndLogMsg m = new ErrorAndLogMsg (MsgType.LOG, now.toString() + ":" + log);
		if (autoPrint) m.printMsg();
		return m;
	}
	
	public boolean anyError () {
		return error != null && error != ErrorCode.SUCCESS;
	}
	
	public ErrorCode errorCode () {
		return error;
	}
	
}
