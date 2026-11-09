public class ReviewRuleTest { public static void main(String[] args){if(!ReviewRule.accepts("REVIEW"))throw new AssertionError("REVIEW lost");System.out.println("PASS REVIEW rule");} }
