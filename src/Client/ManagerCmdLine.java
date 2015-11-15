package Client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import Client.HotelClient.HotelServerWrapper;
import HotelServerInterface.ErrorAndLogMsg;
import HotelServerInterface.IHotelServer;
import HotelServerInterface.ErrorAndLogMsg.MsgType;
import HotelServerInterface.IHotelServer.HotelProfile;
import HotelServerInterface.IHotelServer.Record;
import HotelServerInterface.IHotelServer.RoomType;

public class ManagerCmdLine {

	static Scanner keyboard;
	
	static DateFormat df = new SimpleDateFormat (IHotelServer.dateFormat);
	
	static String DEFAULT_LOG_FILE_PATH = "ManagerLog.txt";
	
	// get a date from input
	// return null if user input empty string
	static Date inputDate () {
		
		int tryCnt=0;
		
		do {
			try {
				System.out.print ("(" + IHotelServer.dateFormat + "). Prese <Enter> to Cancel:");
				String s = keyboard.nextLine();
				
				if (s==null || s.isEmpty() ) return null;
				
				return df.parse(s);//, new ParsePosition(0));
	
			} catch (ParseException e) {
				System.out.println ("Try again...");
			}
		} while (tryCnt++ < 5);
		
		return null;

	}
	
	public static void main (String [] args) {
		
		if (args.length <2) {
			System.out.println ("Need parameters: Host Port\n");
			return;
		}
		
		//Set proper direction of streaming logs and errors
		ErrorAndLogMsg.addStream( MsgType.ERR, System.err);
		ErrorAndLogMsg.addStream(MsgType.WARN, System.out);
		ErrorAndLogMsg.addStream(MsgType.INFO, System.out);
		
		//Open the log file and append contents
		String logFilePath = null;
		if (args.length<3 || args[2].isEmpty()) {
			logFilePath = DEFAULT_LOG_FILE_PATH;
		} else
			logFilePath = args[2];
		

		File logFile = new File (logFilePath);

		PrintStream logStream = null;

		try {
			logStream = new PrintStream(logFile);
		} catch (IOException e) {
			System.out.println ("Log file open and creation error.");
			e.printStackTrace();
		}
		
		if (logStream!=null)
			ErrorAndLogMsg.addStream(MsgType.LOG, logStream ); // the log fine

		//logStream.println("Log started");
		ErrorAndLogMsg.LogMsg("Logging started.").printMsg();
		
		String regHost = args[0];
		int regPort = Integer.parseInt(args[1]);
		
		System.out.println ("Initilization in progress...\n");
		
		HotelClient client = new HotelClient();
		
		ErrorAndLogMsg m = client.Initialize(regHost, regPort);
		
		m.printMsg();
		
		if (m.anyError()) {
			System.out.println ("Error initializing. Exit.\n");
			return;
		}
		
		if (client.getServerCnt() == 0) {
			// No server added, should be an error
			System.out.println ("Error: no any server object retrieved.");
		}
		
		System.out.println ("Initilized.\n");
		
		keyboard = new Scanner(System.in);
		
		
		do {
			// Print out menu
			System.out.println ("========================\n");
			System.out.println ("HOTEL MANAGER CLIENT\n");
			System.out.println ("MAIN MENU\n" +
			                    "1. List all hotel information.\n" +
					            "2. Login a hotel as a manager.\n" +
			                    "3. Print service report.\n" +
			                    "4. Print status report.\n" +
			                    "5. Exit.\n" +
			                    "========================\n");
			
			System.out.println ("Please input (1-5):");
			
			int cmd = 0;
			try {
				cmd = keyboard.nextInt();
				keyboard.nextLine(); // after input a number and <enter>, there follows an empty line
			}
			catch(Exception e) {
				System.out.println("Invalid command, please enter 1-5\n");
			}

			
			switch (cmd) {
				case 1: // List all hotel profiles
					Iterator <HotelServerWrapper> iter = client.getServerIterator();
					
					while (iter.hasNext()) {
						HotelServerWrapper srv = iter.next();
						HotelProfile prof = srv.prof;
						
						System.out.println ("-----------\nShort Name:" + prof.shortName);
						System.out.println (prof.fullName);
						System.out.println (prof.descText);
						for (RoomType typ:RoomType.values()) {
							System.out.println ( typ.toString() + ":" + 
											     prof.totalRooms.get(typ)
											   );
						}
						
						if (srv.serverMgr!=null) {
						    // login as manager
						    System.out.println ("ALREADY LOG IN AS MANAGER");
						}
					}
					break;
				case 2: {
					// Login as hotel manager
					System.out.print ("Hotel short name:");
					String name = keyboard.nextLine();
					if (name==null || name.isEmpty())
						break;
					
					System.out.print ("Manage ID:");
					String id = keyboard.nextLine();
					if (id==null || id.isEmpty())
						break;
					
					System.out.print ("Password:");
					String pass = keyboard.nextLine();
					if (pass==null || pass.isEmpty())
						break;
					
					m = client.loginAsManager(name, id, pass);
					
					if (m!=null)
						System.out.println ("Manager login to Hotel '" + name + "' failed.");
					else
						System.out.println ("Manager login to Hotel '" + name + "' success.");
					
					break;
				}
	
				case 3: {
					// Print service report
	
					System.out.print ("Hotel short name:");
					String name = keyboard.nextLine();
					if (name==null || name.isEmpty())
						break;
					
					
					Collection <Record> records = new ArrayList<Record> ();
					
					Date date = inputDate();
					if (date==null) break;
					
					m = client.getServiceReport(name, date, records);
					
					if (records.isEmpty())
						System.out.println ("No room need to be serviced on that day.\n");
					else {
						
						System.out.println ("Rooms to be serviced on that day:");
						
						int i = 0;
						for (Record r:records) {
							i ++;
							System.out.println ("#" + i + "--------------\n");
							System.out.println (r);
						}
					}
					
					if (m!=null && m.anyError()) {
						System.out.println ("Some error retrieving records.");
						m.printMsg();
					}
					
					break;				
				}
				case 4: {
					// Print status report
					System.out.print ("Hotel short name:");
					String name = keyboard.nextLine();
					if (name==null || name.isEmpty())
						break;
					
					
					Collection <Record> records = new ArrayList<Record> ();
					
					Date date = inputDate();
					if (date==null) break;
					
					m = client.getStatusReport(name, date, records);
					
					if (records.isEmpty())
						System.out.println ("No room will be occupied on that day.\n");
					else {
						
						System.out.println ("Rooms to be occupied on that day:");
						
						int i = 0;
						for (Record r:records) {
							i ++;
							System.out.println ("#" + i + "--------------\n");
							System.out.println (r);
						}
					}
					
					if (m!=null && m.anyError()) {
						System.out.println ("Some error retrieving records.");
						m.printMsg();
					}
					
					break;						}
				case 5: {
					System.out.println ("Good buy!");
					return;
				}
			}
		} while (true);
	}
	
}
