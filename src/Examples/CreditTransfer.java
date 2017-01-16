package Examples;

import uk.co.textmarketer.RestAPI.RestClient;
import uk.co.textmarketer.RestAPI.RestClientException;

public class CreditTransfer {
	public static void main(String[] args) {
		RestClient tmClient = new RestClient("MyAPIUsername", "MyAPIPassword", RestClient.ENV_PRODUCTION);
		try {
			tmClient.transferCreditsToUser(5000, "targetAPIusername", "targetAPIpassword");
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}