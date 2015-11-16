package client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


import client.HotelClient.HotelServerWrapper;
import common.ErrorAndLogMsg;
import common.ErrorAndLogMsg.MsgType;
import client.webservice.Availability;
import client.webservice.HotelProfile;
import client.webservice.HotelServerWS;
import client.webservice.Record;

public class HotelClientCmdLine {

	static Scanner keyboard;
		
	static final String DEFAULT_LOG_FILE_PATH = "ClientLog.txt"; // add time stamp

	 
	
	/*static String dateToString (Date date) {
		return df.format(date);
	} */
	
	public static void main (String [] args) {
		
	    int[] newID = new int[1]; // last reservation ID
	    
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
			System.out.println ("HOTEL BOOKING CLIENT\n");
			System.out.println ("MAIN MENU\n" +
			                    "1. List all hotel information.\n" +
					            "2. Check room availability. \n" +
			                    "3. Reserve rooms.\n" +
			                    "4. Cancel Reservation.\n" +
			                    "5. Transfer Reservation.\n" +
					         //   "**5. [NOT SUPPORT] Query guest reservation.\n"+
			                 //   "**6. [NOT SUPPORT] Backgound update hotel information.\n" +
			                    "6. Exit.\n" +
			                    "========================\n");
			
			System.out.println ("Please input (1-7):");
			
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
    					
    					if (srv.prof!=null) {
        					System.out.println ("-----------\nShort Name:" + prof.getShortName());
        					System.out.println (prof.getFullName());
        					System.out.println (prof.getDescText());
        					
        					List <client.webservice.HotelProfile.TotalRooms.Entry> entries = prof.getTotalRooms().getEntry();
        					
        					for (client.webservice.HotelProfile.TotalRooms.Entry entry : entries) {
        						System.out.println ( entry.getKey().toString() + ":" + 
									     entry.getValue()
									   );
        					}
    					}
    				}
    				break;
    			case 2: {
    				// check room availability
    				
    				Record r = MiscUtil.inputOneRecord();
    				
    				if (r==null) {
    					System.out.println ("Operation aborted....\n");
    					break;
    				}
    				
    				ArrayList <Availability> avails = new ArrayList<Availability> ();
    				
    				m = client.checkAvailability (r, avails);
    				
    				if (m==null || !m.anyError()) {
    					//success, print out all information
    					
    					System.out.print ("Available hotels for Room " + r.getRoomType() + " ");
    					System.out.println (
    							MiscUtil.formatDate(r.getCheckInDate()) + " - " + 
    							MiscUtil.formatDate(r.getCheckOutDate()));
    					
    					System.out.println("Hotel\tAvailable Rooms\tRate");
    					System.out.println("-----------------------");
    
    					for (Availability av : avails) {
    						System.out.println (
    								av.getHotelName() + "\t" + 
    								av.getAvailCount() + "\t" + 
    								av.getRate());
    					}
    				} else {
    					m.printMsg();
    					System.out.println ("Availability query failed. ErrorCode:" + 
    							m.errorCode().toString());
    				}			
    				
    				break; 
    			}
    				
    			case 3: // Reserve a room
    				
    			{
    				Record r = MiscUtil.inputOneRecord();
    				
    				if (r==null) {
    					System.out.println ("Operation aborted....\n");
    					break;
    				}
    				
    				// Try to reserve now
    				
    				m = client.reserveHotel(
    				        r.getGuestID(), 
    				        r.getShortName(), 
    				        r.getRoomType(), 
    				        r.getCheckInDate(), 
    				        r.getCheckOutDate(),
    				        newID);
    				
    				if (m==null || !m.anyError()) {
    					//success
    					System.out.println ("Reservation confirmed. ReservationID:" + newID[0] +
    					        " Enjoy your Hotel!\n");
    					
    				} else {
    					m.printMsg();
    				}			
    				
    				break; 
    			}
    			case 4: // cancel reservation
    			{
    				Record r = MiscUtil.inputOneRecord();
    				
    				if (r==null) {
    					System.out.println ("Operation aborted....\n");
    					break;
    				}
    				
    				// Try to cancel now
    				
    				m = client.cancelHotel(r.getGuestID(), 
    				        r.getShortName(), r.getRoomType(), 
    				        r.getCheckInDate(), r.getCheckOutDate());
    				
    				if (m==null || !m.anyError()) {
    					//success
    					System.out.println ("Reservation cancelled!\n");
    				} else {
    					m.printMsg();
    				}			
    				
    				break; 
    			}
    			case 5: // transfer reservation
    			{
    			    Record r = MiscUtil.inputOneRecord();
    			    
    			    if (r==null) {
                        System.out.println ("Operation aborted....\n");
                        break;
    			    }
    			    
    			    System.out.print ("Input reservation ID:");
    			    String ln = keyboard.nextLine();
    			    int id = Integer.parseInt(ln);
    			    
    			    
    			    if (id>0) {
    			        System.out.print ("Input target Hotel:");
    			        String targetHotel = keyboard.nextLine();
    			        
    			        int [] idHolder = new int[1];
    			        idHolder [0] = id;
    			        
    			        if (targetHotel!=null && !targetHotel.isEmpty()) {
        			        m = client.transferRoom(
        			                r.getGuestID(), 
        			                idHolder, 
        			                r.getShortName(), 
        			                r.getRoomType(), 
        			                r.getCheckInDate(), 
        			                r.getCheckOutDate(), 
        			                targetHotel);
        			        
        		             if (m==null || !m.anyError()) {
        		                    //success
        		                    System.out.println (
        		                            "Transferation success! New reservation ID:" +
        		                            idHolder[0] + "\n");
        		                } else {
        		                    m.printMsg();
        		                }           
    			        }
    			    } else
    			        System.out.println ("Invalid ID:" + ln);
    			    
    			    break; 
    			    
    			/*    // query reservation records for one guest
    			{
    			    System.out.println ("Function not supported at thsi time...");
    			    break;
    			    /*
    				// Input Guest ID
    				System.out.print ("Guest ID:");
    				String guestID = keyboard.nextLine();
    				if (guestID==null || guestID.isEmpty())
    					break;
    				
    				Collection <Record> records = new ArrayList<Record> ();
    				
    				m = client.getReserveRecords(guestID, records);
    				
    				if (records.isEmpty())
    					System.out.println ("No records found.\n");
    				else {
    					int i = 0;
    					for (Record r:records) {
    						i ++;
    						System.out.println ("Record #" + i + "--------------\n");
    						System.out.println (r);
    					}
    				}
    				
    				if (m!=null && m.anyError()) {
    					System.out.println ("Some error retrieving records.");
    					m.printMsg();
    				}
    				
    				break; */
    			}
    			/*case 6:  {
    			    System.out.println ("Function not supported at thsi time...");
    			    break;
    			    
    				m = client.startHotelServerBackgroundScan();
    				if (m.anyError()) {
    					System.out.println ("Background process start failed. \n");
    				}
    				break; 
    			} */
    			case 6:
    			    // stop the background scan thread
    			    client.stopHotelServerBackgroundScan();
    				return;
    				
                case 12: {
                    // check room availability quick test
                    
 /*                   Record r = new Record (
                            0,
                            "123",
                            "Gordon",
                            RoomType.SINGLE,
                            new SimpleDate (2015,12,1),
                            new SimpleDate (2015,12,10),
                            0);
                    
                    ArrayList <Availability> avails = new ArrayList<Availability> ();
                    
                    m = client.checkAvailability (r, avails);
                    
                    if (m==null || !m.anyError()) {
                        //success, print out all information
                        
                        System.out.print ("Available hotels for Room " + r.roomType.toString() + " ");
                        System.out.println (r.checkInDate + " - " + r.checkOutDate);
                        
                        System.out.println("Hotel\tAvailable Rooms\tRate");
                        System.out.println("-----------------------");
    
                        for (Availability av : avails) {
                            System.out.println (av.hotelName + "\t" + av.availCount + "\t" + av.rate);
                        }
                    } else {
                        m.printMsg();
                        System.out.println ("Availability query failed. ErrorCode:" + 
                                m.errorCode().toString());
                    }           
                    
                    break; */
                }
                
    				
    			case 13: {
    	             // short cut for testing reservation
    			    
    			    
  /*                  m = client.reserveHotel("123", "Gordon", RoomType.SINGLE, 
                           new SimpleDate (2015, 12, 5), 
                           new SimpleDate (2015, 12, 10), 
                           newID);
                    
                    if (m!=null) m.printMsg();
                    else
                        System.out.println ("Success. ResID=" + newID[0]);
                    break; */
    			}
    			case 15: {
    			    // short cut for testing transfer
    	                

/*    			    m = client.transferRoom("123", newID, 
    			            "Gordon", RoomType.SINGLE,
    			            new SimpleDate (2015, 12, 5), 
    			            new SimpleDate (2015, 12, 10), 
                            "Star");
                    if (m!=null) m.printMsg();
                    else
                        System.out.println ("Success. ResID=" + newID[0]);
                    
                    break; */
    			}
			}
		} while (true);
	}
	
}
