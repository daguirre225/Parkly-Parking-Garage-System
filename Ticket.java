import java.util.Date;

public class Ticket {
    private String ticketID;
    private Date entryTime;
    private Date exitTime;
    private int totalMinutes;
    private int fee;

    public Ticket(String ticketID, Date entryTime) {
        this.ticketID = ticketID;
        this.entryTime = entryTime;
        this.exitTime = null;
        this.totalMinutes = 0;
        this.fee = 0;
    }

    public String getTicketID() {
        return ticketID;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public Date getExitTime() {
        return exitTime;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public int getFee() {
        return fee;
    }

    public void markPaid() { }

    public boolean isValid() {
        return false;
    }
}
