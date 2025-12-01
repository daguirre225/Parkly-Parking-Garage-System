package parkly;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.io.Serializable;

public class Ticket implements ObjectTag {
	private static int nextTicketID = 0;
	private final String tag = "TICKET";
	private int ticketID;
	private transient LocalDateTime entryStamp; // transient is needed to send object over stream because LocalDateTime is not serializable 
	private String entryDate;
	private String entryTime;
    private int entryHour;
    private int entryMinute;
    private transient LocalDateTime exitStamp;
    private String exitDate;
    private int exitHour;
    private int exitMinute;
    private int totalTime;
    private int fee;
    
    private static synchronized int getNextTicketID() {return ++nextTicketID;};
    
    public Ticket() {
        this.ticketID = getNextTicketID();
        this.entryStamp = LocalDateTime.now(); // create new date time object here.
        this.entryDate = String.valueOf(this.entryStamp.getMonthValue()) + "/" + String.valueOf(this.entryStamp.getDayOfMonth()) + "/" + String.valueOf(this.entryStamp.getYear()); // MM/dd/yyyy
        this.entryTime = String.valueOf(this.entryStamp.getHour()) + ":" + String.valueOf(this.entryStamp.getMinute());
        this.entryMinute = this.entryStamp.getMinute();
        this.entryHour = this.entryStamp.getHour();
        this.exitStamp = null;
        this.exitMinute = 0;
        this.exitHour = 0;
        this.totalTime = 0;
        this.fee = 5;
    }
    
    // Get object tag
    public String getObjectTag() {
    	return this.tag;
    }
    // Return ticket ID
    public String getTicketID() {
        return String.valueOf(this.ticketID);
    }
    // Return LocalDateTime entry stamp
    public LocalDateTime getEntryStamp() {
        return this.entryStamp;
    }
    // Return String Time Stamp
    public String getEntryTime() {
    	return this.entryTime;
    }
    // Return LocalDateTime exit stamp
    public LocalDateTime getExitStamp() {
    	if (this.exitStamp != null) {
    		return this.exitStamp;    		
    	}
    	return this.entryStamp;
    }
    // Set LocalDateTime exit stamp
    public void setExitStamp() {
    	this.exitStamp = LocalDateTime.now();
    	calcTotalTime();
    }
    // return total minutes
    public void calcTotalTime() {
    	if (this.exitStamp != null) {
    		Duration timeDifference = Duration.between(this.entryStamp, this.exitStamp);
    		this.totalTime = (int) timeDifference.toMinutes();
    		this.totalTime = (this.totalTime + 59) / 60;
    	}
    }
    
    public int calcFeeTotals() {
        return this.fee * this.totalTime;
    }

    public void markPaid() {
    	
    }

    public boolean isValid() {
        return false;
    }
}


