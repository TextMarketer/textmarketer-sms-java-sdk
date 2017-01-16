package Examples;

import uk.co.textmarketer.RestAPI.RestClient;
import uk.co.textmarketer.RestAPI.RestClientException;

public class CheckBalance {
	public static void main(String[] args) {
		RestClient tmClient = new RestClient("MyAPIUsername", "MyAPIPassword", RestClient.ENV_PRODUCTION);
		try {
			int creditsAvailable = tmClient.getCredits();
			System.out.println("Account have " + creditsAvailable + " credits.");
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}
