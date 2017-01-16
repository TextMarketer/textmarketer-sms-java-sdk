package Examples;

import java.util.Hashtable;
import java.util.Map;

import uk.co.textmarketer.RestAPI.RestClient;
import uk.co.textmarketer.RestAPI.RestClientException;

public class ErrorCapture {
	public static void main(String[] args) {
		RestClient tmClient = new RestClient("MyAPIUsername", "MyAPIPassword", RestClient.ENV_PRODUCTION);
		
		try {
			tmClient.sendSMS("Hello SMS World!", "447000000000", "SenderName", 72, "", "", null);
			// if the send fails, execution jumps to the start of the catch-block
			int creditsAvailable = tmClient.getCredits();
			System.out.println("Account have " + creditsAvailable + " credits.");
		} catch (RestClientException e) {
			Hashtable<String, String> errors = tmClient.getLastErrors();
			for(Map.Entry<String, String> error: errors.entrySet())
				System.out.println("Error code " + error.getKey() + ": " + error.getValue());
		}
	}
}
