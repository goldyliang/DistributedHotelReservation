package Client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import HotelServerInterface.ErrorAndLogMsg;
import HotelServerInterface.ErrorAndLogMsg.MsgType;
import HotelServerInterface.IHotelServer.Availability;
import HotelServerInterface.IHotelServer.HotelProfile;
import HotelServerInterface.IHotelServer.Record;
import HotelServerInterface.IHotelServer.RoomType;
import Client.HotelClient.HotelServerWrapper;


import miscutil.SimpleDate;

public class HotelClientCmdLine {

	static Scanner keyboard;
		
	static final String DEFAULT_LOG_FILE_PATH = "ClientLog.txt"; // add time stamp

	// get a date from input
	// return null if user input empty string
	static SimpleDate inputDate () {
		
		int tryCnt=0;
		
		do {
			try {
				System.out.print ("(" + SimpleDate.getDateFormat() + "). Prese <Enter> to Cancel:");
				String s = keyboard.nextLine();
				
				if (s==null || s.isEmpty() ) return null;
				
				return SimpleDate.parse(s);//, new ParsePosition(0));
	
			} catch (ParseException e) {
				System.out.println ("Try again...");
			}
		} while (tryCnt++ < 5);
		
		return null;

	}
	
	/*static String dateToString (Date date) {
		return df.format(date);
	} */
	
	static Record inputOneRecord () {
		Record r = new Record();
		
		// Input Guest ID
		System.out.print ("Guest ID:");
		r.guestID = keyboard.nextLine();
		if (r.guestID==null || r.guestID.isEmpty())
			return null;
		
		// Input short name
		System.out.print ("Hotel Short Name:");
		r.shortName = keyboard.nextLine();
		if (r.shortName==null || r.shortName.isEmpty())
			return null;
		
		// Input room type
		System.out.print ("Room type:");
		int i = 1;
		for (RoomType typ: RoomType.values()) {
			System.out.print (" " + (i++) + "-" + typ.toString() );
		}
		
		boolean valid = false;
		do {
			int typ = keyboard.nextInt();
			keyboard.nextLine(); // skip empty line after integer

			valid = typ>0 && typ<=RoomType.values().length;
			
			if (valid)
				r.roomType = RoomType.values() [typ-1];
		} while (!valid);
		
		// Input check in date
		System.out.println ("Checkin Date:");
		
		valid = false;
		
		SimpleDate today =  new SimpleDate();
        
		do {
			r.checkInDate = inputDate();
			if (r.checkInDate == null) break;
			System.out.println ("Entered check in date:" + r.checkInDate);
			valid = !r.checkInDate.before(today);
			if (!valid)
				System.out.println ("Check in date not valid. Try again..\n");
		} while (!valid);
		if (r.checkInDate==null)
			return null;
		
		// Input check out date
		System.out.println ("Checkout Date:");
		
		do {
			r.checkOutDate = inputDate();
			if (r.checkOutDate ==null) break;
			System.out.println ("Entered check out date:" + r.checkOutDate);
			valid = r.checkOutDate.after(r.checkInDate);
			if (!valid)
				System.out.println ("Check out date not valid. Try again..\n");
		} while (!valid);
		
		if (r.checkOutDate == null)
			return null;
		
		System.out.println ("---------------\n");
		System.out.print (r);
		System.out.println ("---------------\n");

		System.out.print ("Confirm (y/n):");
		String s = keyboard.nextLine();
		if (s.charAt(0) != 'y' && s.charAt(0) != 'Y')
			return null;
		
		return r;
	}
	
	public static void main (String [] args) {
		
	    int newID = (int)(System.currentTimeMillis());// last reservation ID.
	    
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
        					System.out.println ("-----------\nShort Name:" + prof.shortName);
        					System.out.println (prof.fullName);
        					System.out.println (prof.descText);
        					for (RoomType typ:RoomType.values()) {
        						System.out.println ( typ.toString() + ":" + 
        										     prof.totalRooms.get(typ)
        										   );
        					}
    					}
    				}
    				break;
    			case 2: {
    				// check room availability
    				
    				Record r = inputOneRecord();
    				
    				if (r==null) {
    					System.out.println ("Operation aborted....\n");
    					break;
    				}
    				
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
    				
    				break;
    			}
    				
    			case 3: // Reserve a room
    				
    			{
    				Record r = inputOneRecord();
    				
    				if (r==null) {
    					System.out.println ("Operation aborted....\n");
    					break;
    				}
    				
    				// Try to reserve now
    				
    				m = client.reserveHotel(
    				        r.guestID, r.shortName, r.roomType, r.checkInDate, r.checkOutDate,
    				        newID);
    				
    				if (m==null || !m.anyError()) {
    					//success
    					System.out.println ("Reservation confirmed. ReservationID:" + newID +
    					        " Enjoy your Hotel!\n");
    					
    				} else {
    					m.printMsg();
    				}			
    				
    				newID++;
    				
    				break;
    			}
    			case 4: // cancel reservation
    			{
    				Record r = inputOneRecord();
    				
    				if (r==null) {
    					System.out.println ("Operation aborted....\n");
    					break;
    				}
    				
    				// Try to cancel now
    				
    				m = client.cancelHotel(r.guestID, 
    				        r.shortName, r.roomType, 
    				        r.checkInDate, r.checkOutDate);
    				
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
    			    Record r = inputOneRecord();
    			    
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
    			        
    			        
    			        if (targetHotel!=null && !targetHotel.isEmpty()) {
        			        m = client.transferRoom(
        			                r.guestID, 
        			                id, 
        			                r.shortName, 
        			                r.roomType, 
        			                r.checkInDate, 
        			                r.checkOutDate, 
        			                targetHotel,
        			                newID);
        			        
        		             if (m==null || !m.anyError()) {
        		                    //success
        		                    System.out.println (
        		                            "Transferation success! New reservation ID:" +
        		                            newID + "\n");
        		                } else {
        		                    m.printMsg();
        		                }     
        		             newID++;
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
                    
                    Record r = new Record (
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
                    
                    break;
                }
                
    				
    			case 13: {
    	             // short cut for testing reservation
    			    
    			    
                    m = client.reserveHotel("123", "Gordon", RoomType.SINGLE, 
                           new SimpleDate (2015, 12, 5), 
                           new SimpleDate (2015, 12, 10), 
                           newID);
                    
                    if (m!=null) 
                    	m.printMsg();
                    else
                        System.out.println ("Success. ResID=" + newID);
                    
                    newID++;
                    
                    break;

    			}
    			case 15: {
    			    // short cut for testing transfer
    	            // shall be right after launching case 14

    			    m = client.transferRoom("123", newID-1, 
    			            "Gordon", RoomType.SINGLE,
    			            new SimpleDate (2015, 12, 5), 
    			            new SimpleDate (2015, 12, 10), 
                            "Star",newID);
                    if (m!=null) m.printMsg();
                    else
                        System.out.println ("Success. ResID=" + newID);
                    newID ++;
                    break;
    			}
			}
		} while (true);
	}
	
}
