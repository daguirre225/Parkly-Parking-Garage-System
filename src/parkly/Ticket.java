package parkly;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.io.Serializable;

public class Ticket implements ObjectTag, Serializable {
	private static final long serialVerisionUID = 1L;
	private static int nextTicketID = 0;
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
	private final String tag = "TICKET";
	private int ticketID;
	private transient LocalDateTime entryStamp; // transient is needed to send object over stream because LocalDateTime is not serializable 
	private String entryDate;
	private String entryTime;
    private int entryHour;
    private int entryMinute;
    private transient LocalDateTime exitStamp;
    private String exitDate;
    private String exitTime;
    private int exitHour;
    private int exitMinute;
    private int totalTime;
    private int fee;
    private int totalDue;
    private boolean isPaid;
    private static synchronized int getNextTicketID() {return ++nextTicketID;};
    
    public Ticket() {
        this.ticketID = getNextTicketID();
        this.entryStamp = LocalDateTime.now(); // create new date time object here.
//        this.entryDate = String.valueOf(this.entryStamp.getMonthValue()) + "/" + String.valueOf(this.entryStamp.getDayOfMonth()) + "/" + String.valueOf(this.entryStamp.getYear()); // MM/dd/yyyy
//        this.entryTime = String.valueOf(this.entryStamp.getHour()) + ":" + String.valueOf(this.entryStamp.getMinute());
        this.entryDate = this.entryStamp.format(DATE_FORMATTER);
        this.entryTime = this.entryStamp.format(TIME_FORMATTER);
        this.entryMinute = this.entryStamp.getMinute();
        this.entryHour = this.entryStamp.getHour();
        this.exitStamp = null;
        this.exitDate = "";
        this.exitTime = "";
//        this.exitMinute = 0;
//        this.exitHour = 0;
        this.totalTime = 0;
        this.fee = 5;
        this.totalDue = this.fee;
        this.isPaid = false;
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
    // Return String Date 
    public String getEntryDate() {
    	return this.entryDate;
    }
    public String getExitDate() {
    	return this.exitDate;
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
    	// Inside setExitStamp()
//    	this.exitDate = String.valueOf(this.exitStamp.getMonthValue()) + "/" + String.valueOf(this.exitStamp.getDayOfMonth()) + "/" + String.valueOf(this.exitStamp.getYear());    	
//    	this.exitTime = String.valueOf(this.exitStamp.getHour()) + ":" + String.valueOf(this.exitStamp.getMinute());
    	this.exitDate = this.exitStamp.format(DATE_FORMATTER);
    	this.exitTime = this.exitStamp.format(TIME_FORMATTER);
    	calcTotalTime();
    	calcFeeTotals();
    	System.out.println("TICKET.setExitStamp:\n\tExit Date: " + this.exitDate + "\n\tExit Time: " + this.exitTime);
    }
    public String getExitTime() {
    	return this.exitTime;
    }
    // return total minutes
    public void calcTotalTime() {
    	if (this.exitStamp != null) {
    		Duration timeDifference = Duration.between(this.entryStamp, this.exitStamp);
    		this.totalTime = (int) timeDifference.toMinutes();
    		this.totalTime = (this.totalTime + 59) / 60;
    	}
    }
    
    public int getTotalTime() {
    	return this.totalTime;
    }
    public void calcFeeTotals() {
        this.totalDue = this.fee * this.totalTime;
    }

    public int getTotalFees() {
    	return this.totalDue;
    }
    public void markPaid() {
    	this.isPaid = true;
    }

    public boolean isPaid() {
    	return this.isPaid;
    }
    public boolean isValid() {
    	return false;
    }
}


