public class OrderService {
    private final PaymentService payment;
    public OrderService(PaymentService payment) { this.payment = payment; }
    public String pay(String orderId, long amountFen) { return payment.create(orderId, amountFen); }
}
