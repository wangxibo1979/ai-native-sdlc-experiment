public class BaselineTest {
  public static void main(String[] args) {
    PaymentService payment = new PaymentService();
    if (new OrderService(payment).pay("old-order", 100) == null) throw new AssertionError("missing payment ID");
    try { payment.create("invalid", 0); throw new AssertionError("zero accepted"); }
    catch (IllegalArgumentException expected) { }
    System.out.println("PASS baseline: create payment and reject zero");
  }
}
