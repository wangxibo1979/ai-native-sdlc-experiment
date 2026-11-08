import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
public class OrderHttpClient {
  public static void main(String[] args) throws Exception {
    String body="orderId="+URLEncoder.encode(args[1],StandardCharsets.UTF_8)+"&amountFen="+URLEncoder.encode(args[2],StandardCharsets.UTF_8);
    HttpRequest request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+args[0]+"/payments"))
      .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build();
    HttpResponse<String> response=HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
    System.out.println(response.statusCode()+":"+response.body());
  }
}
