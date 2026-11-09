import java.net.*;
import java.net.http.*;
import java.time.Duration;
public class Consumer {
 static final int VERSION = 1;
 public static void main(String[] args) throws Exception {
  String mode=args[1];String key=mode.equals("failure")?"FAIL":"o1";
  String field=mode.equals("break-request")?"amountCents":"amountFen";
  String body="orderId="+key+"&"+field+"=100"+(VERSION==2?"&channel=WEB":"");
  var req=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+args[0]+"/payments")).timeout(Duration.ofSeconds(5)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
  var response=HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString());
  System.out.println("HTTP="+response.statusCode()+" BODY="+response.body());
  if(mode.equals("failure")){if(response.statusCode()!=503)throw new AssertionError("expected 503");return;}
  if(mode.equals("break-request")){if(response.statusCode()!=400)throw new AssertionError("expected 400");return;}
  if(response.statusCode()!=201)throw new AssertionError("expected 201");
  if(!response.body().contains("\"paymentId\":\""))throw new AssertionError("required response field paymentId missing");
  if(VERSION==2)System.out.println(response.body().contains("\"status\":")?"STATUS=PRESENT":"STATUS=UNKNOWN (supported v1 fallback)");
 }
}
