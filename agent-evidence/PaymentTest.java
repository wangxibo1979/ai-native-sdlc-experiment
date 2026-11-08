import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PaymentTest {
    public static void main(String[] args) throws Exception {
        Class<?> type;
        try {
            type = Class.forName("Payment");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Payment implementation is missing", e);
        }
        Object payment = type.getConstructor().newInstance();
        Method submit = type.getMethod("submit", String.class, long.class);
        Object first = submit.invoke(payment, "order-1", 100L);
        Object retry = submit.invoke(payment, "order-1", 100L);
        if (first == null || !first.equals(retry)) {
            throw new AssertionError("Identical retries must return the same non-null payment ID");
        }
        try {
            submit.invoke(payment, "order-1", 101L);
            throw new AssertionError("Conflicting amount must be rejected");
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof IllegalArgumentException)) throw e;
        }
        Object another = submit.invoke(payment, "order-2", 100L);
        if (first.equals(another)) throw new AssertionError("Different orders must have distinct payment IDs");
        System.out.println("PaymentTest: PASS");
    }
}
