public class Ticket {
    private String ticketID;
    private Date entryTime;
    private Date exitTime;
    private int totalMinutes;
    private int fee;

    public void markPaid() { }
    public boolean isValid() { return false; }
}
