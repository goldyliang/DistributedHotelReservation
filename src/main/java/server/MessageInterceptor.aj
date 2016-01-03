package HotelServer;

import HotelServer.HotelServer.UDPMessage.MessageType;

import java.net.DatagramPacket;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public privileged aspect MessageInterceptor {
    
    //Messages to intercept
    Set <MessageType> msgIntercept = new TreeSet <MessageType> ();
    
    {
        // intercept all
        /*for (MessageType type: MessageType.values())
            msgIntercept.add(type);*/
        msgIntercept.add(MessageType.COMMIT_ACK);
    }
    
    //Intercept rate . 0 to disable
    private static int lost_rate = 0;
    //Intercept how many times then let go. 0 to disable
    private static int lost_times = 2;
    
    
    int cnt=0;
    Random rand = new Random();

    pointcut UDPSend (DatagramPacket pkt): 
        call(void java.net.DatagramSocket.send(..)) &&
        args(pkt);
    
    void around(DatagramPacket pkt) : UDPSend(pkt) {
        byte[] buf = pkt.getData();
        
        HotelServer.UDPMessage msg = HotelServer.UDPMessage.decode(buf);
        
        if (! msgIntercept.contains(msg.msgType)) {
            proceed (pkt);
        } else if (lost_rate>0) {
            int r = rand.nextInt(100);
            
            if (r < lost_rate) {
                // lost
                //System.out.println ("Intercepted: " + msg.toString());
            } else
                proceed (pkt);
        } else if (lost_times > 0) {
            cnt ++;
            if (cnt > lost_times)
                proceed (pkt);
        }

    }
    
}
