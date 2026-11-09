public class AmountRule { public static void validate(long amount){if(amount<=0)throw new IllegalArgumentException("positive amount required");} }
