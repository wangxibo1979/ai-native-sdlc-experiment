import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class PaymentHttpServer {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", Integer.parseInt(args[0])), 0);
    Map<String, String[]> payments = new HashMap<>();
    server.createContext("/payments", e -> {
      int status; String body;
      try {
        if (!e.getRequestMethod().equals("POST")) throw new IllegalArgumentException();
        Map<String,String> fields = new HashMap<>();
        for (String pair : new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).split("&")) {
          String[] kv=pair.split("=",2); if(kv.length!=2) throw new IllegalArgumentException();
          fields.put(URLDecoder.decode(kv[0],StandardCharsets.UTF_8),URLDecoder.decode(kv[1],StandardCharsets.UTF_8));
        }
        String id=Objects.requireNonNull(fields.get("orderId")); long amount=Long.parseLong(fields.get("amountFen"));
        if (id.isBlank() || amount<=0) throw new IllegalArgumentException();
        String[] existing=payments.get(id);
        if(existing!=null && !existing[0].equals(Long.toString(amount))) {status=409;body="conflict";}
        else { if(existing==null) {existing=new String[]{Long.toString(amount), UUID.randomUUID().toString()};payments.put(id,existing);} status=201;body=existing[1]; }
      } catch (RuntimeException ex) {status=400;body="invalid request";}
      byte[] data=body.getBytes(StandardCharsets.UTF_8);e.sendResponseHeaders(status,data.length);try(var out=e.getResponseBody()){out.write(data);}
    });
    server.start();
  }
}
