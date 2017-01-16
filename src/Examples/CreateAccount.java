package Examples;

import uk.co.textmarketer.RestAPI.RestClient;
import uk.co.textmarketer.RestAPI.RestClientException;

public class CreateAccount {
	public static void main(String[] args) {
		RestClient tmClient = new RestClient("MyAPIUsername", "MyAPIPassword", RestClient.ENV_PRODUCTION);
		try {
			tmClient.createSubAccount("My client", null, null, null, null, null, false);
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}