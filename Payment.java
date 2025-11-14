public class Payment {
    private float amount;
    private PaymentMethod method;
    private PaymentStatus status;

    public boolean process() { return false; }
    public boolean refund() { return false; }
}
