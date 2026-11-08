import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class Provider {
 static final int VERSION = 2;
 public static void main(String[] args) throws Exception {
  boolean broken=args.length>0 && args[0].equals("break-response");
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/payments",e->{
   int code=201;String body;
   try {
    Map<String,String> f=new HashMap<>();
    for(String pair:new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8).split("&")){
     String[] a=pair.split("=",2);if(a.length!=2)throw new IllegalArgumentException();f.put(a[0],URLDecoder.decode(a[1],StandardCharsets.UTF_8));
    }
    String key=Objects.requireNonNull(f.get("orderId"));long amount=Long.parseLong(f.get("amountFen"));
    if(key.isBlank()||amount<=0)throw new IllegalArgumentException();
    if(key.equals("FAIL")){code=503;body="{\"error\":\"unavailable\"}";}
    else {String field=broken?"receiptId":"paymentId";body="{\""+field+"\":\"pay-"+amount+"\""+(VERSION==2?",\"status\":\"PENDING\"":"")+"}";}
   } catch(RuntimeException ex){code=400;body="{\"error\":\"invalid\"}";}
   byte[] b=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(code,b.length);try(var out=e.getResponseBody()){out.write(b);}
  });server.start();System.out.println(server.getAddress().getPort());System.out.flush();
 }
}
