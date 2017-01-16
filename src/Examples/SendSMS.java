package Examples;
import uk.co.textmarketer.RestAPI.RestClient;
import uk.co.textmarketer.RestAPI.RestClientException;

public class SendSMS {
	public static void main(String[] args) {
		RestClient tmClient = new RestClient("MyAPIUsername", "MyAPIPassword", RestClient.ENV_PRODUCTION);
		
		try {
			tmClient.sendSMS("Hello SMS World!", "447000000000", "SenderName", 72, "", "", null);
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}