package hotelbooking.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSocketServer implements Runnable {
    
    
    // here you need to put the list of socket address of other servers.
    // you can hard-code them for simple.
    static InetSocketAddress[]  srvSocketAddresses = {
        new InetSocketAddress ("localhost",2000),
        new InetSocketAddress ("localhost",2100)
    };
        
    int serverNo;
    int port; // this is the port of this server
    Thread thread;
    
    // construct the server from the server number  (0,1,...)
    TestSocketServer (int serverNo) {
        this.serverNo = serverNo;
        
        // get the hardcoded server port
        this.port= srvSocketAddresses[serverNo].getPort();
        
        // start the listening thread
        thread = new Thread (this);
        thread.start();
    }
    
    // put extra arguments as required
    public List<String> checkAvailability () {
        
        List<String> avails = new ArrayList<String> ();
         
        // send query message to all other servers
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            
            return null;
        }
        
        String s = "Check Availability: SINGLE";  // construct the information you want to send here
        // ... ...
        
        byte[] buf = s.getBytes();        
        
        
        int sent = 0;
        

        
        for (InetSocketAddress addr : srvSocketAddresses) {
            
            try {
                DatagramPacket request = new DatagramPacket(buf, buf.length, addr);
    
                socket.send(request);
                
                sent ++;

            } catch (Exception e) {
                e.printStackTrace();;
            }
        }
        
        buf = new byte[2000];        
        
        DatagramPacket reply = new DatagramPacket(buf, buf.length);

        int received = 0;
                
        while (received < sent) {
            
            try {

               socket.receive(reply);
               
               s = new String(reply.getData(), reply.getOffset(), reply.getLength());
               
               // now you get a respond, get information from there
               // and put information to the List avails.
               avails.add(s);

            }  catch (Exception e) {
                e.printStackTrace();;
                continue;
            }
            
            received ++; 
        }
        
        socket.close();
        
        return avails;
    }
    
    
    // The thread body to receive query request and send respond
    // you may just put it in main() if do not want to create a thread
    public void run() {
        
        byte[] buffer = new byte[5000]; // todo , fine tune buffer
        
        DatagramSocket listenSocket = null; 
                
        try {
            
            listenSocket = new DatagramSocket (port); // this is the port to listen
            
            
            while (true) {
                DatagramPacket request = new DatagramPacket (buffer, buffer.length);
                
                try {
                    listenSocket.receive(request);
                } catch (Exception e) {
                    e.printStackTrace();;
                    break;
                }
                
                // extract data from request
                String s = new String (request.getData(), request.getOffset(),request.getLength());
                
                // now you can get information from s
                // do something, like query local room availability
                System.out.println ("Server " + serverNo + " received request:" + s);

                
                // now you need to construct the result back to a String result
                // just a simple example here
                String result = s + " -- " + "Response from server " +  serverNo + " AVAILABLE: 1";
                
                // then put the result to a buffer
                byte[] buf = result.getBytes();
                
                DatagramPacket reply = new DatagramPacket(buf, 
                        buf.length,
                        request.getAddress(), 
                        request.getPort());
                
                // send it back
                try {
                    listenSocket.send(reply);
                } catch (Exception e) {
                    e.printStackTrace();;
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();;
        } finally {
            if (listenSocket !=null) 
                listenSocket.close();
        }
    }

    
    public static void main (String[] args) {
        
        // following code just for demo....
        // in reality, you just create one server object here
        
        // here I demonstrate two servers
        TestSocketServer test1 = new TestSocketServer(0); // with socket address 2000
        TestSocketServer test2 = new TestSocketServer(1);

        //try {Thread.sleep(500);} catch (InterruptedException e) {};
        
        // try invoking a checkAvailability query for test1 and get result
        List <String> result = test1.checkAvailability();
        
        for (String r: result) {
            System.out.println (r);
        }
        
        Scanner keyboard = new Scanner(System.in);
        System.out.println ("Press <Enter> to exit");
        keyboard.nextLine();
        
        // the small issue in this demo is that the servers can not be stopped gracefully (because of the thread)
        // but no worry, just stop it in Eclipse
        
    }
    
}
