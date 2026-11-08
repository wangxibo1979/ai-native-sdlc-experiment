public class IdempotencyTest {
  public static void main(String[] args) {
    PaymentService payment = new PaymentService();
    OrderService order = new OrderService(payment);
    String first = order.pay("order-1", 100);
    String retry = order.pay("order-1", 100);
    if (!first.equals(retry)) throw new AssertionError("identical retry must return original payment ID");
    try { order.pay("order-1", 200); throw new AssertionError("conflicting retry accepted"); }
    catch (IllegalArgumentException expected) { }
    System.out.println("PASS identical retry and conflicting amount");
  }
}
