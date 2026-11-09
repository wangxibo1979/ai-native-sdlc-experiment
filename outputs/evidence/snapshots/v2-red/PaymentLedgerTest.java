public class PaymentLedgerTest {
    private static int failures;
    private static int count;
    private static void test(String name, Runnable r) {
        count++;
        try { r.run(); System.out.println("PASS "+name); }
        catch (Throwable t) {failures++;System.out.println("FAIL "+name+": "+t);}
    }
    private static void require(boolean b,String msg){if(!b)throw new AssertionError(msg);}
    private static void rejects(Class<? extends Throwable> type,Runnable r){
        try{r.run();}catch(Throwable t){if(type.isInstance(t))return;throw new AssertionError(t);}
        throw new AssertionError("expected "+type.getSimpleName());
    }
    public static void main(String[] args){
        test("first valid request",()->{var p=new PaymentLedger();require(p.submit("k1",100,false)!=null,"id missing");require(p.executions()==1,"effect count");});
        test("retry does not reexecute",()->{var p=new PaymentLedger();var id=p.submit("k1",100,false);require(id.equals(p.submit("k1",100,false)),"retry changed id");require(p.executions()==1,"effect repeated");});
        test("conflicting parameters rejected without side effect",()->{var p=new PaymentLedger();p.submit("k1",100,false);rejects(IllegalArgumentException.class,()->p.submit("k1",101,false));require(p.executions()==1,"conflict repeated effect");});
        test("invalid input has no side effect",()->{var p=new PaymentLedger();rejects(IllegalArgumentException.class,()->p.submit(" ",100,false));rejects(IllegalArgumentException.class,()->p.submit(null,100,false));rejects(IllegalArgumentException.class,()->p.submit("k",0,false));require(p.executions()==0,"invalid executed");});
        test("failure before commit can be retried",()->{var p=new PaymentLedger();rejects(IllegalStateException.class,()->p.submit("k",100,true));require(p.executions()==0,"failure executed");var id=p.submit("k",100,false);require(id.equals(p.submit("k",100,false)),"recovery duplicated");require(p.executions()==1,"recovery repeated effect");});
        System.out.println("CASES="+count+" FAILURES="+failures);
        if(failures>0)throw new AssertionError("business behavior failed: "+failures);
    }
}
