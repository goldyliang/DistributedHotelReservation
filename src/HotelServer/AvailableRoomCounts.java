package HotelServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * Store data of the available rooms (for one hotel and one type of room only)
 * Provide necessary operation and query towards the records
 * Thread-safe and provide concurrency operations as much as possible
 */
public class AvailableRoomCounts {

    // Total number of rooms
    int totalRooms;
    
    // The startDate of the room availability counts
    Date startDate;
    
    // Availability counts per day
    // The integer of index 0 is the count of the startDate,
    // Then the counts are stored day by day.
    // For the dates beyond the end of the list, no booking yet (maximum availability)
    // Use int[] as a simple object composed by one integer.
    ArrayList <int[]> availCounts;
    
    // Read write lock to sync the operation between
    //    > increase/decrease of a count, and 
    //    > Operation of deleting a day at the beginning, or adding new days at the end 
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r_lock = rwl.readLock();
    private final Lock w_lock = rwl.writeLock();
    
    // Synchronization objects of the availability counter values
    // For each integer of availCounts[i], the update
    // is synchronized by the object of syncObj[i % NUM_SYNC_OBJS]
    final int NUM_SYNC_OBJS = 4;
    Object[] syncObj;
    
    public AvailableRoomCounts (int totalRooms, int days) {
        this.totalRooms = totalRooms;
        availCounts = new ArrayList <int[]> (days);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      
        try {
            this.startDate = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        } // set to today
        
        for (int i=0; i<days; i++) {
            int[] n = new int[1];
            n[0] = totalRooms;
            availCounts.add(n);
        }
        
        syncObj  = new Object[NUM_SYNC_OBJS];
        
        for (int i=0;i<NUM_SYNC_OBJS;i++)
            syncObj[i] = new Object();
    }
    
    // Time difference between d1 and d2. 
    // Supposed d2 >= d1
    // Return int instead as the value shall not exceed the maximum integer
    public static int dateDiff1 (Date d1, Date d2)
    {
        long diff = d2.getTime() - d1.getTime();
        
        return (int)TimeUnit.MILLISECONDS.toDays(diff);
    }
    
    public static int dateDiff2 (Date d1, Date d2) {
        Calendar dayOne = Calendar.getInstance();
        dayOne.setTime(d1);
        Calendar dayTwo = Calendar.getInstance();
        dayTwo.setTime(d2);

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return dayTwo.get(Calendar.DAY_OF_YEAR) - dayOne.get(Calendar.DAY_OF_YEAR);
        } else {
            if (dayTwo.get(Calendar.YEAR) < dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            
            int extraDays = 0;

            
            while (dayOne.get(Calendar.YEAR) < dayTwo.get(Calendar.YEAR)) {
                dayTwo.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                // TODO: there is some error here!!!!!
                extraDays += dayTwo.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayOne.get(Calendar.DAY_OF_YEAR) + dayTwo.get(Calendar.DAY_OF_YEAR);
        }
    }
    
    public static int dateDiff (Date d1, Date d2) {
        Calendar dayOne = Calendar.getInstance();
        dayOne.setTime(d1);
        Calendar dayTwo = Calendar.getInstance();
        dayTwo.setTime(d2);

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return dayTwo.get(Calendar.DAY_OF_YEAR) - dayOne.get(Calendar.DAY_OF_YEAR);
        } else {
            if (dayTwo.get(Calendar.YEAR) < dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            
            int extraDays = dayTwo.get(Calendar.DAY_OF_YEAR);
            dayTwo.add(Calendar.DATE, - extraDays);
            
            while (dayOne.get(Calendar.YEAR) < dayTwo.get(Calendar.YEAR)) {
                extraDays += dayTwo.getActualMaximum(Calendar.DAY_OF_YEAR);

                dayTwo.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                // TODO: there is some error here!!!!!
            }

            return extraDays - dayOne.get(Calendar.DAY_OF_YEAR) + dayTwo.get(Calendar.DAY_OF_YEAR);
        }
    }
    
    
    // Get a count of specified date
    int getCount (Date dt) {
  
        r_lock.lock();
        
        int i = dateDiff (this.startDate, dt);
        int cnt = 0;
        
        synchronized (syncObj [i % NUM_SYNC_OBJS]) 
//        synchronized (this)
        {
            cnt = availCounts.get(i)[0];
        }
        
        r_lock.unlock();
        return cnt;
    }
    
    // Operation of decrease the avail count indexed by idx
    // Return false if the count is already zero.
    // True if not zero, and the count decrease by one
    boolean decreaseCount (int idx) {
    
        boolean r = false;
        
        synchronized (syncObj [idx % NUM_SYNC_OBJS]) 
 //       synchronized (this)
        {
            int[] n = availCounts.get(idx);
            if (n[0] > 0) {
                
               // try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

                n[0]--;
                r = true;
            }
        }
        return r;
    }
    
    // Operation of increase the avail count of n
    // Return false if the count is already ==totalRooms.
    // True if not, and the count increase by one
    boolean increaseCount (int idx) {
    
        boolean r = false;
        
        synchronized (syncObj [idx % NUM_SYNC_OBJS]) 
//        synchronized (this)

        {
            int[] n = availCounts.get(idx);
            if (n[0] < totalRooms) {
                //try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                n[0]++;
                r = true;
            }
        }
        return r;
    }

    boolean increaseSize (int new_size) {
        //need to expand the size of the counts
        //to avoid too much expansion, we expand beyond one week (7 days)
        //Totally add new_size - len   elements
        
        //As we are changing the structure, apply the write lock
        w_lock.lock();
        
        System.out.println ("increase to " + new_size);
        
        int len = availCounts.size();
        
        try {
            for (int i=0; i< new_size -len; i++) {
                int [] a = new int[1];
                a[0] = totalRooms;
                availCounts.add(a);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            w_lock.unlock();
        }
    }
    
    boolean increaseSizeTillDate (Date dt_end) {
        
        Date dt = getBookingEndDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        
        Calendar cal_end = Calendar.getInstance();
        cal_end.setTime(dt_end);
        
        //As we are changing the structure, apply the write lock
        w_lock.lock();
        
        try {

            
            while (cal.before(cal_end)) {
                int [] a = new int[1];
                a[0] = totalRooms;
                availCounts.add(a);
                
                cal.add(Calendar.DATE, 1);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            w_lock.unlock();
        }
        
    }
    
    public boolean decrementDays (Date inDate, Date outDate) {
        int idx = dateDiff (startDate, inDate);
        int days = dateDiff (inDate, outDate);
        
        if (idx<0) throw new RuntimeException ("Error: query inDate<startDate");
        if (days <=0) throw new RuntimeException("Error: query outDate<=inDate");
        
        int endIdx = idx+days-1;
        
        int len = availCounts.size();
        
        // inject delay
        //try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
        
        if (endIdx >= len) {
            increaseSize (endIdx + 1);
        }
        
        // Now we start to access and change the values of elements,
        // but not the structure of the list, apply the Read lock
        r_lock.lock();
        
        int i = idx;
        boolean success = true;

        while (i <= endIdx) {
            if (!decreaseCount (i)) {
                success = false;
                break;
            } else i++;
        }
        
        if (!success) {
            // revert back the counts that decreased
            while (i>idx) {
                i--;
                increaseCount (i);
            }
        }
        
        r_lock.unlock();
        
        return success;
    }
    
    public boolean incrementDays (Date inDate, Date outDate){
        int idx = dateDiff (startDate, inDate);
        int days = dateDiff (inDate, outDate);
        
        if (idx<0) throw new RuntimeException ("Error: query inDate<startDate");
        if (days <=0) throw new RuntimeException("Error: query outDate<=inDate");
        
        int endIdx = idx+days-1;
        
        int len = availCounts.size();
        
        if (endIdx >= len) {
            // This simply should not happen
            throw new RuntimeException ("Error: incrementDays out of bound");
        }
        
        // Now we start to access and change the values of elements,
        // but not the structure of the list, apply the Read lock
        r_lock.lock();
        
        int i = idx;
        boolean success = true;

        while (i <= endIdx) {
            if (!increaseCount (i)) {
                success = false;
                break;
            } else i++;
        }
        
        if (!success) {
            // This normally should not happen.
            // Could happen in the testing only.
            // Anyway, revert back the counts that increase
            System.out.println("Count increment overflow");
            
            // revert back the counts that increased
            while (i>idx) {
                i--;
                decreaseCount (i);
            }
        }
        
        r_lock.unlock();
        
        return success;
    }
    
    public int query (Date inDate, Date outDate) {
        int idx = dateDiff (startDate, inDate);
        int days = dateDiff (inDate, outDate);
        
        if (idx<0) throw new RuntimeException ("Error: query inDate<startDate");
        if (days <=0) throw new RuntimeException("Error: query outDate<=inDate");
                
        int endIdx = idx+days-1;
        
        int len = availCounts.size();
        
        if (endIdx >= len) {
            increaseSize (endIdx + 1);
        }
        
        // Now we start to access and change the values of elements,
        // but not the structure of the list, apply the Read lock
        r_lock.lock();
        
        int i = idx;
        
        int cnt = Integer.MAX_VALUE;
        
        while (i <= endIdx) {
            int n = availCounts.get(i++)[0];
            if (n < cnt)
                cnt = n;
        }
        
        r_lock.unlock();

        return cnt;
    }
    
    public Date getBookingStartDate () {
        return startDate;
    }
    
    public Date getBookingEndDate () {
        
        int len = availCounts.size();
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        cal.add(Calendar.DATE, len-1);
        
        return cal.getTime();
    }
    

    public boolean delteTillDate (Date date) {
        return true;
    }
    
    public boolean loadCounts (InputStream s) throws IOException {
        return true;
    }
    
    public boolean saveCounts (OutputStream s) throws IOException {
        return true;
    }
    

}
