package hotelbooking.server.roomcounts;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import hotelbooking.miscutil.SimpleDate;

/**
 * Store counters of the available rooms (for one hotel and one room-type only)
 * on a range of dates. Provide necessary operation and query towards the counters.
 * 
 * Following operations are designed to be thread-safe and allow concurrent access
 * as much as possible.
 * <li>Increment/decrement of the counter for a specific range of dates
 * <li>Query of the counter for a specific date or a specific range of dates
 * 
 * Following operations are synchronized and thread-safe:
 * <li>Resizing the overall date range when performing decrement operation
 * on dates beyond the current range
 * <li>Deleting dates at the beginning of the range
 * 
 * This is tailored for a project assignment of the course Distributed System, 
 * yet it is written in a way to enable further maintain and re-use if needed. 
 * 
 * It could be made general used as a class for vector of concurrent counters 
 * which size is extensible and shrinkable.
 * 
 * @author Guopeng Liang, Concordia University, Montreal, Canada
 */
public class RoomCounts_RWLock_AtomInt implements RoomCounts {

    // Total number of available rooms before any booking
    private final int totalRooms;
    
    // The date on which the first counter stores for the booking
    private SimpleDate startDate;
    
    // Availability counters per day
    // The counter of index 0 is for the date of startDate,
    // Then the other counts are stored in day-increasing order.
    // For the dates beyond the end of the list, there is no 
    // booking yet (ie. counter value = totalRooms)
    private final ArrayList <AtomicInteger> availCounts;
    
    // Read write lock to sync the operation between
    //    > read/increase/decrease/modify of a counter, and 
    //    > Operation of deleting expired days at the beginning, or adding new days at the end 
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();
    
    /**
     * Construct with the parameter of available room count,
     * and the number of days for initial counters.
     * The start date of the counters is set to the system's current
     * calendar date.
     * 
     * @param totalRooms Number of total rooms available
     * @param startDate The start day of allocated counters
     * @param days  Number of days for the initial allocated counters (or, initiate size)
     * @throws RuntimeException if invalid arguments (totalRooms<=0, or days <=0)
     */
    public RoomCounts_RWLock_AtomInt (int totalRooms, SimpleDate startDate, int days) {
    	
    	if (totalRooms <= 0) throw new RuntimeException ("Invalid totalRooms:" + totalRooms);
    	if (days <= 0) throw new RuntimeException ("Invalid days:" + days);
    	
        this.totalRooms = totalRooms;
        availCounts = new ArrayList <AtomicInteger> (days);
        
        this.startDate = startDate.clone(); // SimpleDate is mutable, so needs a clone
        
        for (int i=0; i<days; i++) {
            availCounts.add(new AtomicInteger(totalRooms));
        }
        
    }
    
    /**
     * Return number of total rooms
     * @return Number of total rooms
     */
    public int getTotalRoomCount () {
    	return totalRooms;
    }
        
    /**
     * Query and return the available room count on a specific single day.
	 *
     * The query allows concurrent access with increment/decrement operations
     * for the same counter.
     * So if another thread concurrently modify the count, the query may return
     * the old count or the modified count.
     * 
     * @param date The date on which the available room count is queried
     * @return The available room count.
     * @throws IndexOutOfBoundsException if the query date is before current system date
     */
    public int queryCount (SimpleDate date) {
    	
    	// Lock for reading towards the counter list
        rLock.lock();
        try {
            // Index of the counter = date difference between startDate and the specified date
            int i = SimpleDate.dateDiff (this.startDate, date);
            
            if (i >= availCounts.size()) {
            	// Out of range, just return the totalRoom
            	return totalRooms;
            }
            
            return availCounts.get(i).get();
        } finally {
        	rLock.unlock();
        }
    }
    
    /**
     * Query and return the available room count for a day ranges
     * IE. the minimum available room count for the ranges of days.
     * 
     * Check in date is included in the range.
     * Check out date is not included in the range, but the day before included.
     * 
     * The query allows concurrent access with increment/decrement operations
     * for the same counters.
     * So if another thread concurrently modify the counters, the query may return
     * the old count or the modified count.
     * 
     * @param inDate  (check in date) the first day of the range
     * @param outDate (check out date) the day after the last day of the range
     * @return available room count (minimum count of the range)
     * @throws RuntimeException if inDate/outDate is in the history, or outDate<=inDate
     */
    public int queryCount (SimpleDate inDate, SimpleDate outDate) {
    	
        int days = SimpleDate.dateDiff (inDate, outDate);
        if (days <=0) throw new RuntimeException("Query outDate <= inDate");

        // Now we start to access the values of elements,
        // but not the structure of the list, apply the Read lock
        rLock.lock();
        try {
            int idx = SimpleDate.dateDiff (startDate, inDate);
            
            if (idx<0) throw new RuntimeException ("Query inDate < startDate");
                    
            int endIdx = idx+days-1;
            
            int len = availCounts.size();
            
            if (idx >= len) {
            	// inDate beyond range, just return totalRooms
            	return totalRooms;
            }
            
            if (endIdx >= len) {
                // Do not need to go beyond last day, as the counter would be maximum
            	endIdx = len-1;
            }
            
            int cnt = 0;
            
	        int i = idx;
	        
	        cnt = totalRooms;
	        
	        while (i <= endIdx) {
	            int n = availCounts.get(i++).get();
	            if (n < cnt)
	                cnt = n;
	        }
	        
	        return cnt;

        } finally {
        	rLock.unlock();
        }

    }
    
    //public AtomicInteger contentCount = new AtomicInteger(0);
    
    // Decrease the avail count indexed by idx.
    // Return false if the count is already zero.
    // Return true  if originally greater than zero
    // It is assumed that the rLock is already granted before the call
    // And this call is synchronized with other update towards the same counter
    private boolean decreaseCount (int idx) {
    
    	// Assumed that the rLock is already granted for the whole list
    	// So getting the counter object is safe
        AtomicInteger n = availCounts.get(idx);
        
        int cnt;
        //int cc = -1;
        do {
        	//cc ++;
        	cnt = n.get();
        	if (cnt <=0 )
        		return false;
        	// if the compare does not pass, it indicates a modification
        	// by other thread. The check needs to be redone
        } while (! n.compareAndSet(cnt, cnt - 1));
        
        //contentCount.addAndGet(cc);
        
        return true;
    }
    
    // Increase the avail count indexed by idx (upper bound = totalRooms)
    // Return false if the count is already maximum (=totalRooms).
    // Return true  if not.
    // It is assumed that the rLock is already granted before the call
    // And this call is synchronized with other update towards the same counter
    private boolean increaseCount (int idx) {
    
    	// Assumed that the rLock is already granted for the whole list
    	// So getting the counter object is safe
        AtomicInteger n = availCounts.get(idx);
        
        int cnt;
        //int cc = -1;
        do {
        	//cc ++;
        	cnt = n.get();
        	if (cnt >= totalRooms )
        		return false;
        	
        	// if the compare does not pass, it indicates a modification
        	// by other thread. The check needs to be redone
        } while (! n.compareAndSet(cnt, cnt + 1));
        
       // contentCount.addAndGet(cc);

        
        return true;
    }

    /*
     * Increase the size of counter to at least new_size
     * Return true if re-size success.
     * wLock shall be already locked before invoking increaseSize
     */
    private boolean increaseSize (int new_size) {
        
        int len = availCounts.size();
        
        // To avoid too frequent re-size, the new_size is increased to 
        // MAX (1.5 times of original, new_size)
    	int size1 = len + (len >> 1);
    	
    	new_size = (size1 > new_size? size1 : new_size);
    	
        for (int i=0; i< new_size -len; i++) {
            availCounts.add(new AtomicInteger(totalRooms));
        }
        return true;
    }
    
    
    /**
     * Decrease the available counters by one for the whole range
     * inDate is inclusive and outDate not for the range.
     * 
     * This method is synchronized if the allocated counter list is re-structured
     * (resize, etc), but also allow concurrent read/modify to different counters
     * correctly.
     * 
     * @param inDate (check in date) the start date of the range
     * @param outDate (check out date) the date after the last day of the range
     * @return {@code true} if none of the original counts is zero, and all gets decreased by one
     *         {@code false} if any of the original counts is already zero. Or part of the range < startDate
     * @throws RuntimeException if the date range is not valid (outDate <= inDate)
     */
    public boolean decreaseDayRange (SimpleDate inDate, SimpleDate outDate) {
    	
        int days = SimpleDate.dateDiff (inDate, outDate);
        if (days <=0) throw new RuntimeException("Query outDate <= inDate");
        
        boolean success = true;
        
        int idx = 0;
        int endIdx = 0;
        
        // Now we start to access and possibly resize the list of counters
        // Apply wLock first
        wLock.lock();
        try {
            idx = SimpleDate.dateDiff (startDate, inDate);
            
            if (idx<0) return false;
            
            endIdx = idx+days-1;
            
            int len = availCounts.size();
            
            if (endIdx >= len) {
            	// Resize to at
                increaseSize (endIdx + 1);
            }
            
            // From now on, only read operation to the list of counters
            rLock.lock();
        } finally {
        	wLock.unlock();
        }
        
        // Now rLock shall have been locked
        try {
            int i = idx;

	        while (i <= endIdx) {
	        	
	        	// increaseCount/decreaseCount is performed synchronized to
	        	// other modification towards the same counter.
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
        } finally {
        	rLock.unlock();
        }
        
        return success;
    }
    
    /**
     * Increase the available counters by one for the whole range
     * 
     * inDate is inclusive and outDate not for the range.
     * 
     * This method is synchronized if the allocated counter list is re-structured
     * (resize, etc), but also allow concurrent read/modify to different counters
     * correctly.
     * 
     * @param inDate (check in date) the start date of the range
     * @param outDate (check out date) the date after the last day of the range
     * @return {@code true} if none of the original counts is equal or greater than 
     *  upper-bound (totalRooms), and all gets increased by one.
     *         {@code false} if any of the original counts is already upperbound.  
     *         Or part of the range < startDate
     * @throws RuntimeException if the date range is not valid (outDate <= inDate)
     */
    public boolean increaseDayRange (SimpleDate inDate, SimpleDate outDate){

        int days = SimpleDate.dateDiff (inDate, outDate);

        if (days <=0) throw new RuntimeException("Increase outDate" + outDate +" <= inDate " + inDate);

        // Now we start to access and change the values of elements,
        // but not the structure of the list, apply the Read lock
        rLock.lock();
        try {
        	
            int idx = SimpleDate.dateDiff (startDate, inDate);
            
            if (idx<0) return false;
            
            int endIdx = idx+days-1;
            
            boolean success = true;
            
            int len = availCounts.size();
            
            if (endIdx >= len) {
                // This simply will fail, return false
                return false;
            }
            
            int i = idx;
            while (i <= endIdx) {
                if (!increaseCount (i)) {
                    success = false;
                    break;
                } else i++;
            }
            
            if (!success) {
                // Revert back the counts that increased
                while (i>idx) {
                    i--;
                    decreaseCount (i);
                }
            }
            
            return success;

        } finally {
        	rLock.unlock();
        }
        
    }
    


    /**
     * Get the start date of the allocated counters
     * The return date will become invalid if a 
     * call to {@link AvailableRoomCount#deleteCountersTillDate(SimpleDate)} is invoked after.
     * 
     * @return the start date of the allocated counters
     */
    public SimpleDate getStartDate () {
    	
    	// apply rLock to synchronize with structure change
    	rLock.lock();
    	try {
    		return startDate.clone();
    	} finally {
    		rLock.unlock();
    	}
    }
    
    /**
     * Get the date for the last allocated counter
     * All counter beyond this day would be the same as the upper-bound (totalRooms)
     * 
     * The date will become invalid if a resizing happens after.
     * 
     * @return the last date on which a counter is allocated
     */
    public SimpleDate getLastAllocatedDate () {
        
    	// apply rLock to synchronize with structure change
    	rLock.lock();
    	try {
	        int len = availCounts.size();
	        
	        SimpleDate end = new SimpleDate (startDate);
	        end.forwardDay(len-1);
	        return end;
    	} finally {
    		rLock.unlock();
    	}
    }

    /**
     * Delete all counters before a specific date so as to shrink the list
     * All access later-on to the deleted days will result to RuntimeException
     * 
     * This method is synchronized and thread-safe.
     * 
     * @param date The date before which the counters are to be deleted
     * @return <@code true> if delete succeeds, or the date is after current start date
     */
    public boolean deleteCountersTillDate (SimpleDate date) {
    	
        boolean success = false;
        
        if (date.getYear()==2015 && date.getMonth()==12 && date.getDay()==30)
        	success = false;

        // Apply write lock first
        wLock.lock();
        try {
            int cnt = SimpleDate.dateDiff (startDate, date);
            
            if (cnt < 0) return false;
            
            if (cnt == 0) return true; // nothing to do
                    
            
        	// loop cnt times, and delete the first counter
        	for (int i=0; i< cnt; i++) {
        		if (availCounts.size() > 0)
        			availCounts.remove(0);
        		else {
        			// everything cleared, can not delete more
        			break; 
        		}
        	}
        	
        	// Update startDate now, by forwarding cnt days
        	startDate.forwardDay( cnt );
        	success = true;
        } finally {
        	wLock.unlock();
        }
        
        return success;
    }
    
}
