package client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

import HotelServerInterface.IHotelServer;
import client.HotelClient.HotelServerWrapper;
import common.ErrorAndLogMsg;
import common.ErrorAndLogMsg.MsgType;
import client.webservice.*;

public class ManagerCmdLine {

	static Scanner keyboard;
		
	static String DEFAULT_LOG_FILE_PATH = "ManagerLog.txt";
	
	public static void main (String [] args) {
		
		
		//Set proper direction of streaming logs and errors
		ErrorAndLogMsg.addStream( MsgType.ERR, System.err);
		ErrorAndLogMsg.addStream(MsgType.WARN, System.out);
		ErrorAndLogMsg.addStream(MsgType.INFO, System.out);
		
		//Open the log file and append contents
		String logFilePath = null;
		if (args.length<1 || args[0].isEmpty()) {
			logFilePath = DEFAULT_LOG_FILE_PATH;
		} else
			logFilePath = args[0];
		

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
		
		
		System.out.println ("Initilization in progress...\n");
		
		HotelClient client = new HotelClient();
		
		ErrorAndLogMsg m = client.Initialize();
		
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
					            "1. Login a hotel as a manager.\n" +
			                    "2. Print service report.\n" +
			                    "3. Print status report.\n" +
			                    "4. Exit.\n" +
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
/*				case 1: // List all hotel profiles
					Iterator <HotelServerWrapper> iter = client.getServerIterator();
					
					while (iter.hasNext()) {
						HotelServerWrapper srv = iter.next();
						HotelProfile prof = srv.prof;
						
						System.out.println ("-----------\nShort Name:" + prof.getShortName());
						System.out.println (prof.getFullName());
						System.out.println (prof.getDescText());
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
					break; */
				case 1: {
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
					
					long token = client.loginAsManager(name, id, pass);
					
					if (token < 0) {
						System.out.println ("Manager login to Hotel '" + name + "' failed.");
						
					}
					else
						System.out.println ("Manager login to Hotel '" + name + "' success.");
					
					break;
				}
	
				case 2: {
					// Print service report
	
					System.out.print ("Hotel short name:");
					String name = keyboard.nextLine();
					if (name==null || name.isEmpty())
						break;
					
					
					Collection <Record> records = new ArrayList<Record> ();
					
					SimpleDate date = MiscUtil.inputDate();
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
							System.out.println (MiscUtil.getRecordText(r));
						}
					}
					
					if (m!=null && m.anyError()) {
						System.out.println ("Some error retrieving records.");
						m.printMsg();
					}
					
					break;				
				}
				case 3: {
					// Print status report
					System.out.print ("Hotel short name:");
					String name = keyboard.nextLine();
					if (name==null || name.isEmpty())
						break;
					
					
					Collection <Record> records = new ArrayList<Record> ();
					
					SimpleDate date = MiscUtil.inputDate();
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
							System.out.println (MiscUtil.getRecordText(r));
						}
					}
					
					if (m!=null && m.anyError()) {
						System.out.println ("Some error retrieving records.");
						m.printMsg();
					}
					
					break; 
				}
				case 4: {
					System.out.println ("Good buy!");
					return;
				}
/*                case 12: {
                    // Login as hotel manager
                    String name = "Gordon";
                    
                    m = client.loginAsManager(name, "manager", "pass");
                    
                    if (m!=null)
                        System.out.println ("Manager login to Hotel '" + name + "' failed.");
                    else
                        System.out.println ("Manager login to Hotel '" + name + "' success.");
                    
                    break;
                }
    
                case 13: {
                    // Print service report
    
                    
                    Collection <Record> records = new ArrayList<Record> ();

                    
                    m = client.getServiceReport("Gordon", new SimpleDate(2015,12,10), records);
                    
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
                case 14: {
                    // Print status report - short cut for testing
                    Collection <Record> records = new ArrayList<Record> ();
                    
                    SimpleDate date = new SimpleDate(2015,12,7);
                    
                    m = client.getStatusReport("Gordon", date, records);
                    
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
                    
                    break;
                } */
			}
		} while (true);
	}
	
}
