package Examples;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import uk.co.textmarketer.RestAPI.RestClient;
import uk.co.textmarketer.RestAPI.RestClientException;

public class GenericExample {
	public static void main(String[] args) {
		RestClient tmClient = new RestClient("myusername", ",ypassword", RestClient.ENV_SANDBOX);
		try {
			tmClient.isLoginValid();
			System.out.println("Login is OK!");
			
			int credits = tmClient.getCredits();
			System.out.println("Account have " + credits + " credits.");
			
			Hashtable<String, String> result = tmClient.sendSMS("Hello SMS World!", "440000000123", "Hello World", 72, "", "", new Date());
			System.out.println("Used " + result.get("credits_used") + " Credits, ID: " + result.get("message_id") + ", Scheduled ID: " + result.get("scheduled_id") + ", Status: " + result.get("status"));
			
			System.out.println(tmClient.getXML());	
		} catch(RestClientException e) {
			e.printStackTrace();
			
			Hashtable<String, String> errors = tmClient.getLastErrors();
			for(Map.Entry<String, String> error: errors.entrySet())
				System.out.println("Error code " + error.getKey() + ": " + error.getValue());
		}
	}
}