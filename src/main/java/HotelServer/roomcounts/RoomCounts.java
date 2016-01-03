package HotelServer.roomcounts;

import HotelServer.HotelServerTypes.SimpleDate;

public interface RoomCounts {

	    /**
	     * Return number of total rooms
	     * @return Number of total rooms
	     */
	    public int getTotalRoomCount ();
	        
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
	    public int queryCount (SimpleDate date);
	    
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
	    public int queryCount (SimpleDate inDate, SimpleDate outDate);

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
	    public boolean decreaseDayRange (SimpleDate inDate, SimpleDate outDate);
	    
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
	    public boolean increaseDayRange (SimpleDate inDate, SimpleDate outDate);

	    /**
	     * Get the start date of the allocated counters
	     * The return date will become invalid if a 
	     * call to {@link AvailableRoomCount#deleteCountersTillDate(SimpleDate)} is invoked after.
	     * 
	     * @return the start date of the allocated counters
	     */
	    public SimpleDate getStartDate ();
	    
	    /**
	     * Get the date for the last allocated counter
	     * All counter beyond this day would be the same as the upper-bound (totalRooms)
	     * 
	     * The date will become invalid if a resizing happens after.
	     * 
	     * @return the last date on which a counter is allocated
	     */
	    public SimpleDate getLastAllocatedDate ();

	    /**
	     * Delete all counters before a specific date so as to shrink the list
	     * All access later-on to the deleted days will result to RuntimeException
	     * 
	     * This method is synchronized and thread-safe.
	     * 
	     * @param date The date before which the counters are to be deleted
	     * @return <@code true> if delete succeeds, or the date is after current start date
	     */
	    public boolean deleteCountersTillDate (SimpleDate date);
	
}
