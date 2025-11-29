public class Customer {
    private String id;
    private Payment payment;

    public Customer(String id) {
        this.id = id;
        this.payment = null;
    }

    public String getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public Ticket distributeTicket() {
        return null;
    }

    public boolean pay(Ticket t) {
        return false;
    }
}
