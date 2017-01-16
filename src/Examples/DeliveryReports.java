package Examples;

import java.util.Hashtable;

import uk.co.textmarketer.RestAPI.DeliveryReport;
import uk.co.textmarketer.RestAPI.RestClient;
import uk.co.textmarketer.RestAPI.RestClientException;

public class DeliveryReports {
	public static void main(String[] args) {
		RestClient tmClient = new RestClient("MyAPIUsername", "MyAPIPassword", RestClient.ENV_PRODUCTION);
		
		DeliveryReport[] reports;
		try {
			reports = tmClient.getDeliveryReport("all");
			for(DeliveryReport report: reports) {
				System.out.println(report);
				for(Hashtable<String, String> row: report.getRows()) {
					System.out.println("\tMessage ID: " + row.get("message_id"));
					System.out.println("\tLast Updated: " + row.get("last_updated"));
					System.out.println("\tMobile Number: " + row.get("mobile_number"));
					System.out.println("\tStatus: " + row.get("status"));
					System.out.println("\tCustom Tag: " + row.get("custom"));
				}
			} 	
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}
