package uk.co.textmarketer.RestAPI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The RESTful API is an advanced version of the simple SMS API giving you greater flexibility with enhanced tools and services. 
 * As with all our developer solutions, we also offer support if you have any integration questions.<p>
 * 
 * <a href='http://www.textmarketer.co.uk/developers/restful-api.htm'>RESTful API</a><p>
 * 
 * Copyright © 2017 Text Marketer Ltd<p>
 * 
 * Client class to access Text Marketer RESTful API
 * 
 * @author Marco Morais
 * @author Diogo Afonso  
 * @version {@value #VERSION}
 */
public class RestClient {
	final private static int HTTP_GET		= 1;
	final private static int HTTP_POST		= 2;
	final private static int HTTP_PUT		= 3;
	final private static int HTTP_DELETE	= 4;
	
	final private static String PROD_URL = "https://api.textmarketer.co.uk/services/rest/";
	final private static String SAND_URL = "http://sandbox.api.textmarketer.co.uk/services/rest/";
	final private static String APICLIENT = "tm-java-";
	final private static String VERSION = "1.4";
	
	private String xmlResponse;
	private Hashtable<String, String> params;
	private Hashtable<String, String> errors;
	private boolean production;
	
	final public static boolean ENV_SANDBOX = false;
	final public static boolean ENV_PRODUCTION = true;
	
	/**
	 * Constructor for the RestClient class.<p>
	 * 
	 * @param username 	your API Gateway Username
	 * @param password 	your API Gateway Password
	 * @param env		possible values RestClient.ENV_SANDBOX or RestClient.ENV_PRODUCTION
	 *
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *</pre></blockquote>
	 */
	public RestClient(String username, String password, boolean env) {
		this.production = env;
		errors = new Hashtable<String, String>();
		params = new Hashtable<String, String>();
		params.put("password", password);
		params.put("username", username);
		params.put("apiClient", APICLIENT + VERSION);
	}
	
	/**
	 * Make a call to TM Rest API Gateway to test if the username and password are correct.
	 * 
	 * @return boolean TRUE if login valid, FALSE if username or password not correct
	 * @throws RestClientException on error
	 * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    if(tmClient.isLoginvalid())
	 *        System.out.println("Login is OK!");
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *</pre></blockquote>
	 */
	public boolean isLoginValid() throws RestClientException {
		try {
			restGatewayCall("credits", HTTP_GET, null);
			return true;
		} catch (Exception e) {
			throw new RestClientException(e);
		}
	}
	
	/**
     * Get the number of credits currently available on your account.
     *
	 * @return number of credits currently available on your account.
	 * @throws RestClientException on error
     * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    credits = tmClient.getCredits();
	 *    System.out.println("Account have " + credits + " credits.");
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *</pre></blockquote>
     */
    public int getCredits() throws RestClientException {
        int credits = 0;
    	try {
    		xmlResponse = restGatewayCall("credits", HTTP_GET, null);
        	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder docBuilder;
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse (new InputSource(new StringReader(xmlResponse)));
				
				NodeList creditsList = doc.getElementsByTagName("credits");
				if(creditsList.getLength() > 0) {
					credits = Integer.parseInt(creditsList.item(0).getTextContent());
				}
			} catch (Exception e) {
				throw new RestClientException(e);
			}
        } catch (Exception e) {
			throw new RestClientException(e);
		}
        return credits;
    }
    
    /**
     * Send a text message to the specified recipient.
     * 
     * <a href="http://www.textmarketer.co.uk/blog/2009/07/bulk-sms/supported-and-unsupported-characters-in-text-messages-gsm-character-set/">GSM character set</a>
     * 
     * @param message		The textual content of the message to be sent. Up to 612 characters from the GSM alphabet. The SMS characters we can support is documented at <a href="http://www.textmarketer.co.uk/blog/2009/07/bulk-sms/supported-and-unsupported-characters-in-text-messages-gsm-character-set/">GSM character set</a>. Please ensure that data is encoded in UTF-8.
     * @param mobile_number	The mobile number of the intended recipient, in international format, e.g. 447777123123. Only one number is allowed. To send a message to multiple recipients, you must call the API for each number.
     * @param originator	A string (up to 11 alpha-numeric characters) or the international mobile number (up to 16 digits) of the sender, to be displayed to the recipient, e.g. 447777123123 for a UK number.
     * @param validity		An integer from 1 to 72, indicating the number of hours during which the message is valid for delivery. Messages which cannot be delivered within the specified time will fail.
     * @param email			Optional. Available to txtUs Plus customers only. Specifies the email address for incoming responses. If you specify an email address, you must specify an originator that is a txtUs Plus number that is on your account, or you will get an error response.
     * @param custom		Optional. An alpha-numeric string, 1-20 characters long, which will be used to 'tag' your outgoing message and will appear in delivery reports, thus facilitating filtering of reports.
     * @param schedule		Optional. Date parameter to schedule the message to send at a given time.
     * @param checkStop		Optional. If set to 'true', prior to sending the number(s) will checked against the STOP group.
     * @return Hash table with keys: message_id, scheduled_id, credits_used and status
     * @throws RestClientException on error
     * 
     * 
	 *<p><b>Example:</b></p>
	 *
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    Hashtable<String, String> result = tmClient.sendSMS("Hello SMS World!", "447777123123", "Hello World", 72, "", "");
	 *    System.out.println("Used " + result.get("credits_used") + " Credits, ID: " + result.get("message_id") + ",  Scheduled ID: " + result.get("scheduled_id") + ", Status: " + result.get("status"));
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String> sendSMS(String message, String mobile_number, String originator, int validity, String email, String custom, Date schedule, boolean checkStop)  throws RestClientException {
    	if(validity < 1 || validity > 72)
    		validity = 72;
    	if(email == null)
    		email = "";
    	if(custom == null)
    		custom = "";
    	
    	Hashtable<String, String> extraparams = new Hashtable<String, String>();
    	extraparams.put("message", message);
    	extraparams.put("mobile_number", mobile_number);
    	extraparams.put("originator", originator);
    	extraparams.put("validity", Integer.toString(validity));
    	extraparams.put("email", email);
    	extraparams.put("custom", custom);
    	extraparams.put("check_stop", Boolean.valueOf(checkStop).toString());
    	
    	if(schedule != null) {
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    		extraparams.put("schedule", df.format(schedule));
    	}
    	try {
    		xmlResponse = restGatewayCall("sms", HTTP_POST, extraparams);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return parseResponseXML(xmlResponse, "");
    }
    
    /**
     * Send a text message to the specified recipient.
     * 
     * @param message		The textual content of the message to be sent. Up to 612 characters from the GSM alphabet. The SMS characters we can support is documented at <a href="http://www.textmarketer.co.uk/blog/2009/07/bulk-sms/supported-and-unsupported-characters-in-text-messages-gsm-character-set/">GSM character set</a>. Please ensure that data is encoded in UTF-8.
     * @param mobile_number	The mobile number of the intended recipient, in international format, e.g. 447777123123. Only one number is allowed. To send a message to multiple recipients, you must call the API for each number.
     * @param originator	A string (up to 11 alpha-numeric characters) or the international mobile number (up to 16 digits) of the sender, to be displayed to the recipient, e.g. 447777123123 for a UK number.
     * @return Hash table with keys: message_id, scheduled_id, credits_used and status
     * @throws RestClientException on error
     */
    public Hashtable<String, String> sendSMS(String message, String mobile_number, String originator)  throws RestClientException {
    	return sendSMS(message, mobile_number, originator, 0, null, null, null, false);
    }
    
    /**
     * Send a text message to the specified recipient, with a limited validity to be delivered.
     * 
     * @param message		The textual content of the message to be sent. Up to 612 characters from the GSM alphabet. The SMS characters we can support is documented at <a href="http://www.textmarketer.co.uk/blog/2009/07/bulk-sms/supported-and-unsupported-characters-in-text-messages-gsm-character-set/">GSM character set</a>. Please ensure that data is encoded in UTF-8.
     * @param mobile_number	The mobile number of the intended recipient, in international format, e.g. 447777123123. Only one number is allowed. To send a message to multiple recipients, you must call the API for each number.
     * @param originator	A string (up to 11 alpha-numeric characters) or the international mobile number (up to 16 digits) of the sender, to be displayed to the recipient, e.g. 447777123123 for a UK number.
     * @param validity		Optional. An integer from 1 to 72, indicating the number of hours during which the message is valid for delivery. Messages which cannot be delivered within the specified time will fail.
     * @return Hash table with keys: message_id, scheduled_id, credits_used and status
     * @throws RestClientException on error
     * 
     */
    public Hashtable<String, String> sendSMS(String message, String mobile_number, String originator, int validity)  throws RestClientException {
    	return sendSMS(message, mobile_number, originator, validity, null, null, null, false);
    }
    
    /**
     * Send a text message to the specified recipient, with validity and email set to receive replies from.
     * 
     * @param message		The textual content of the message to be sent. Up to 612 characters from the GSM alphabet. The SMS characters we can support is documented at <a href="http://www.textmarketer.co.uk/blog/2009/07/bulk-sms/supported-and-unsupported-characters-in-text-messages-gsm-character-set/">GSM character set</a>. Please ensure that data is encoded in UTF-8.
     * @param mobile_number	The mobile number of the intended recipient, in international format, e.g. 447777123123. Only one number is allowed. To send a message to multiple recipients, you must call the API for each number.
     * @param originator	A string (up to 11 alpha-numeric characters) or the international mobile number (up to 16 digits) of the sender, to be displayed to the recipient, e.g. 447777123123 for a UK number.
     * @param validity		An integer from 1 to 72, indicating the number of hours during which the message is valid for delivery. Messages which cannot be delivered within the specified time will fail.
     * @param email			Optional. Available to txtUs Plus customers only. Specifies the email address for incoming responses. If you specify an email address, you must specify an originator that is a txtUs Plus number that is on your account, or you will get an error response.
     * @return Hash table with keys: message_id, scheduled_id, credits_used and status
     * @throws RestClientException on error
     */
    public Hashtable<String, String> sendSMS(String message, String mobile_number, String originator, int validity, String email)  throws RestClientException {
    	return sendSMS(message, mobile_number, originator, validity, email, null, null, false);
    }
    
    /**
     * Send a text message to the specified recipient, with validity, email and a custom alpha-numeric reference tag.
     * 
     * @param message		The textual content of the message to be sent. Up to 612 characters from the GSM alphabet. The SMS characters we can support is documented at <a href="http://www.textmarketer.co.uk/blog/2009/07/bulk-sms/supported-and-unsupported-characters-in-text-messages-gsm-character-set/">GSM character set</a>. Please ensure that data is encoded in UTF-8.
     * @param mobile_number	The mobile number of the intended recipient, in international format, e.g. 447777123123. Only one number is allowed. To send a message to multiple recipients, you must call the API for each number.
     * @param originator	A string (up to 11 alpha-numeric characters) or the international mobile number (up to 16 digits) of the sender, to be displayed to the recipient, e.g. 447777123123 for a UK number.
     * @param validity		An integer from 1 to 72, indicating the number of hours during which the message is valid for delivery. Messages which cannot be delivered within the specified time will fail.
     * @param email			Optional. Available to txtUs Plus customers only. Specifies the email address for incoming responses. If you specify an email address, you must specify an originator that is a txtUs Plus number that is on your account, or you will get an error response.
     * @param custom		Optional. An alpha-numeric string, 1-20 characters long, which will be used to 'tag' your outgoing message and will appear in delivery reports, thus facilitating filtering of reports.
     * @return Hash table with keys: message_id, scheduled_id, credits_used and status
     * @throws RestClientException on error 
     */
    public Hashtable<String, String> sendSMS(String message, String mobile_number, String originator, int validity, String email, String custom)  throws RestClientException {
    	return sendSMS(message, mobile_number, originator, validity, email, custom, null, false);
    }
    
    /**
     * Schedule send a text message to the specified recipient.
     * 
     * @param message		The textual content of the message to be sent. Up to 612 characters from the GSM alphabet. The SMS characters we can support is documented at <a href="http://www.textmarketer.co.uk/blog/2009/07/bulk-sms/supported-and-unsupported-characters-in-text-messages-gsm-character-set/">GSM character set</a>. Please ensure that data is encoded in UTF-8.
     * @param mobile_number	The mobile number of the intended recipient, in international format, e.g. 447777123123. Only one number is allowed. To send a message to multiple recipients, you must call the API for each number.
     * @param originator	A string (up to 11 alpha-numeric characters) or the international mobile number (up to 16 digits) of the sender, to be displayed to the recipient, e.g. 447777123123 for a UK number.
     * @param validity		An integer from 1 to 72, indicating the number of hours during which the message is valid for delivery. Messages which cannot be delivered within the specified time will fail.
     * @param email			Optional. Available to txtUs Plus customers only. Specifies the email address for incoming responses. If you specify an email address, you must specify an originator that is a txtUs Plus number that is on your account, or you will get an error response.
     * @param custom		Optional. An alpha-numeric string, 1-20 characters long, which will be used to 'tag' your outgoing message and will appear in delivery reports, thus facilitating filtering of reports.
     * @param schedule		Optional. Date parameter to schedule the message to send at a given time.
     * @return Hash table with keys: message_id, scheduled_id, credits_used and status
     * @throws RestClientException on error
     */
    public Hashtable<String, String> sendSMS(String message, String mobile_number, String originator, int validity, String email, String custom, Date schedule)  throws RestClientException {
    	return sendSMS(message, mobile_number, originator, validity, email, custom, schedule, false);
    }
    
    /**
     * Transfer credits from one account to another account, using the account number for the target.
     * 
     * @param quantity	The number of credits to transfer from the source account to the target account.
     * @param target	The account number of the account to transfer the credits to
     * @return Hash table with keys: source_credits_before, source_credits_after, target_credits_before and target_credits_after
     * @throws RestClientException on error
     * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    Hashtable<String, String> result = tmClient.transferCreditsToAccount(3, "902");
	 *    System.out.println("Transfered 3 Credits (have " + result.get("source_credits_after") + " now), to account 902, now with " + result.get("target_credits_after") + "Credits");
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String> transferCreditsToAccount(int quantity, String target) throws RestClientException {
    	Hashtable<String, String> extraparams = new Hashtable<String, String>();
    	extraparams.put("quantity", Integer.toString(quantity));
    	extraparams.put("target", target);
    	try {
    		xmlResponse = restGatewayCall("credits", HTTP_POST, extraparams);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return parseResponseXML(xmlResponse, "");
    }
    
    /**
     * Transfer credits from one account to another account, using the username for the target.
     * 
     * @param quantity			The number of credits to transfer from the source account to the target account.
     * @param target_username	The username of the account to transfer the credits to.
     * @param target_password	The password of the account to transfer the credits to.
     * @return Hash table with keys: source_credits_before, source_credits_after, target_credits_before and target_credits_after
     * @throws RestClientException on error
     * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    Hashtable<String, String> result = tmClient.transferCreditsToUser(3, "targetusername", "targetuserpass");
	 *    System.out.println("Transfered 3 Credits (have " + result.get("source_credits_after") + " now), to account targetusername, now with " + result.get("target_credits_after") + "Credits");
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String> transferCreditsToUser(int quantity, String target_username, String target_password) throws RestClientException {
    	Hashtable<String, String> extraparams = new Hashtable<String, String>();
    	extraparams.put("quantity", Integer.toString(quantity));
    	extraparams.put("target_username", target_username);
    	extraparams.put("target_password", target_password);
    	try {
    		xmlResponse = restGatewayCall("credits", HTTP_POST, extraparams);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return parseResponseXML(xmlResponse, "");
    }
    
    /**
     * Get the availability of a given reply keyword.<p>
     * A reply keyword allows you receive incoming text messages to your account by providing people with a keyword, which they text to the short code 88802, e.g. text 'INFO' to 88802 to see this in action.
     * 
     * @param keyword The keyword to check is availability
     * @return Hash table with keys: available and recycle.
     * @throws RestClientException on error
     * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    Hashtable<String, String> result = tmClient.getKeyword("gold");
	 *    System.out.println("The 'gold' keyword is available (" + result.get("available") + "), recycled ("+ result.get("recycle") + ")");
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String> getKeyword(String keyword) throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("keywords/" + URLEncoder.encode(keyword, "UTF-8"), HTTP_GET, null);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return parseResponseXML(xmlResponse, "");
    }
    
    /**
     * Get a list of available 'send groups' - pre-defined groups containing a list of mobile numbers to send a message to.<p> 
     * Also lists 'stop groups' - numbers in these groups will never be sent messages.<p>
     * Every account has at least one stop group, so that your recipients can always opt out of receiving messages from you. This is a legal requirement.
     * 
     * @return Hash table array, each hash table with keys: id, numbers, name and is_stop.
     * @throws RestClientException on error
     * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    Hashtable<String, String>[] result = tmClient.getGroups();
	 *    for(Hashtable<String, String> group: result) {
	 *        System.out.println("Group ID: " + group.get("id"));
	 *        System.out.println("Group numbers: " + group.get("numbers"));
	 *        System.out.println("Group name: " + group.get("name"));
	 *        System.out.println("Group IS STOP: " + group.get("is_stop"));
	 *    }
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String>[] getGroups() throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("groups", HTTP_GET, null);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return parseResponseGroupsXML(xmlResponse, "group");
    }
    
    /**
     * Get all the numbers in the group, if there are any.
     * 
     * @param group Group name or group ID to get the numbers of
     * @return array of String with the numbers of the group
     * @throws RestClientException on error
     * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    String[] numbers = tmClient.getGroup("directors");
	 *    for(String number: numbers)
	 *        System.out.println("Number: " + number);
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public String[] getGroup(String group) throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("group/" + URLEncoder.encode(group, "UTF-8"), HTTP_GET, null);
    		
    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder docBuilder;
    		docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new InputSource(new StringReader(xmlResponse)));
			
			NodeList nodes = doc.getElementsByTagName("number");
			String[] numbers = new String[nodes.getLength()];
			for(int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				numbers[i] = node.getTextContent();
			}
    		return numbers;
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    }
    
    /**
     * Add a number/numbers to a 'send group' (excluding 'merge' groups).
     * 
     * @param group name or group ID to add the numbers to
     * @param numbers The MSISDN (mobile number) you wish to add, if you want to add more then one use a comma delimited list
     * 
     * @return Return the number of added numbers to the selected group
     * @throws RestClientException on error
     * 
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    int numAdded = tmClient.addNumbersToGroup("My Group", "447777000001,447777000002,44777700000");
	 *    System.out.println("Added " + numAdded + " to My Group");
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     * 
     */
    public int addNumbersToGroup(String group, String numbers) throws RestClientException {
    	Hashtable<String, String> extraparams = new Hashtable<String, String>();
    	extraparams.put("numbers", numbers);
    	try {
    		xmlResponse = restGatewayCall("group/" + URLEncoder.encode(group, "UTF-8"), HTTP_POST, extraparams);
    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder docBuilder;
    		docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new InputSource(new StringReader(xmlResponse)));
			
			NodeList nodes = doc.getElementsByTagName("added");
			Node node = nodes.item(0);
			if(node != null)
				return Integer.parseInt(node.getAttributes().item(0).getTextContent());
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return 0;
    }
    
    /**
     * Create a new group.
     * 
     * @param group the new Group name to be created
     * 
     * @return Return true if the group is added with success
     * @throws RestClientException on error
     * 
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    if(tmClient.addGroup("New Group"))
	 *        System.out.println("'New Group' added with success.");
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     * 
     */
    public boolean addGroup(String group) throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("group/" + URLEncoder.encode(group, "UTF-8"), HTTP_PUT, null);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return true;
    }
    
    /**
     * Retrieve a list of available delivery report names.
     * 
     * @return String array with all the reports names  
     * @throws RestClientException on error
     * 
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    String[] reports = tmClient.getDeliveryReports();
	 *    for(String report: reports)
	 *        System.out.println("Report name: " + report);
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public String [] getDeliveryReports() throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("deliveryReports", HTTP_GET, null);
    		
    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder docBuilder;
    		docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new InputSource(new StringReader(xmlResponse)));
			
			NodeList nodes = doc.getElementsByTagName("report");
			String[] reports = new String[nodes.getLength()];
			for(int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i).getAttributes().getNamedItem("name");
				reports[i] = node.getTextContent();
			}
    		return reports;
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    }
    
    /**
     * Retrieve individual delivery report shows the current known status of all messages sent on a given day, or for a particular campaign.<p> 
	 * Whereas the function getDeliveryReports() gets a list of available delivery report names, including delivery reports for campaigns.
	 * 
	 * @param name Name of the delivery report to retrieve or 'all' to retrieve all campaign/API report data
	 * @return DeliveryReport object array
	 * @throws RestClientException
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    DeliveryReport[] reports = tmClient.getDeliveryReport("all");
	 *    for(DeliveryReport report: reports) {
	 *        System.out.println(report);
	 *        for(Hashtable<String, String> row: report.getRows()) {
	 *            System.out.println("\tMessage ID: " + row.get("message_id"));
	 *            System.out.println("\tLast Updated: " + row.get("last_updated"));
	 *            System.out.println("\tMobile Number: " + row.get("mobile_number"));
	 *            System.out.println("\tStatus: " + row.get("status"));
	 *            System.out.println("\tCustom Tag: " + row.get("custom"));
	 *        }
	 *    }
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
	 *
     */
    public DeliveryReport[] getDeliveryReport(String name) throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("deliveryReport/" + URLEncoder.encode(name, "UTF-8"), HTTP_GET, null);
    		
    		return parseDeliveryReport(xmlResponse);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    }
    
    /**
     * Retrieve individual delivery report shows the current known status of all messages sent on a given day, or for a particular campaign.<p> 
	 * Whereas the function getDeliveryReports() gets a list of available delivery report names, including delivery reports for campaigns.
	 * 
	 * @param name Name of the delivery report to retrieve or 'all' to retrieve all campaign/API report data
	 * @param custom Can specify a custom 'tag', which will restrict the search to those messages
	 * @return DeliveryReport object array
	 * @throws RestClientException
	 * 
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    DeliveryReport[] reports = tmClient.getDeliveryReport("all", "test");
	 *    for(DeliveryReport report: reports) {
	 *        System.out.println(report);
	 *        for(Hashtable<String, String> row: report.getRows()) {
	 *            System.out.println("\tMessage ID: " + row.get("message_id"));
	 *            System.out.println("\tLast Updated: " + row.get("last_updated"));
	 *            System.out.println("\tMobile Number: " + row.get("mobile_number"));
	 *            System.out.println("\tStatus: " + row.get("status"));
	 *            System.out.println("\tCustom Tag: " + row.get("custom"));
	 *        }
	 *    }
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public DeliveryReport[] getDeliveryReport(String name, String custom) throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("deliveryReport/" + URLEncoder.encode(name, "UTF-8") + "/custom/" + URLEncoder.encode(custom, "UTF-8"), HTTP_GET, null);
    		
    		return parseDeliveryReport(xmlResponse);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    }
    
    /**
     * Retrieve individual delivery report shows the current known status of all messages sent on a given day, or for a particular campaign.<p> 
	 * Whereas the function getDeliveryReports() gets a list of available delivery report names, including delivery reports for campaigns.
	 * 
	 * @param name Name of the delivery report to retrieve or 'all' to retrieve all campaign/API report data
	 * @param start Get delivery report from start Date
     * @param end Get delivery report to end Date
	 * @return DeliveryReport object array
	 * @throws RestClientException
	 * 
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    // Get all reports from Year 2012
	 *    Calendar cal = Calendar.getInstance();
	 *    cal.set(2012, 1, 1);
	 *    DeliveryReport[] reports = tmClient.getDeliveryReport("all", cal.getTime(), new Date());
	 *    for(DeliveryReport report: reports) {
	 *        System.out.println(report);
	 *        for(Hashtable<String, String> row: report.getRows()) {
	 *            System.out.println("\tMessage ID: " + row.get("message_id"));
	 *            System.out.println("\tLast Updated: " + row.get("last_updated"));
	 *            System.out.println("\tMobile Number: " + row.get("mobile_number"));
	 *            System.out.println("\tStatus: " + row.get("status"));
	 *            System.out.println("\tCustom Tag: " + row.get("custom"));
	 *        }
	 *    }
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public DeliveryReport[] getDeliveryReport(String name, Date start, Date end) throws RestClientException {
    	try {
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    		xmlResponse = restGatewayCall("deliveryReport/" + URLEncoder.encode(name, "UTF-8") + "/" + URLEncoder.encode(df.format(start), "UTF-8") + "/" + URLEncoder.encode(df.format(end), "UTF-8"), HTTP_GET, null);
    		return parseDeliveryReport(xmlResponse);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    }
    
    /**
     * Retrieve individual delivery report shows the current known status of all messages sent on a given day, or for a particular campaign.<p> 
	 * Whereas the function getDeliveryReports() gets a list of available delivery report names, including delivery reports for campaigns.
	 * 
	 * @param name Name of the delivery report to retrieve or 'all' to retrieve all campaign/API report data
	 * @param custom Can specify a custom 'tag', which will restrict the search to those messages
	 * @param start Get delivery report from start Date
     * @param end Get delivery report to end Date
	 * @return DeliveryReport object array
	 * @throws RestClientException
	 * 
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    // Get all reports from Year 2012 with the custom tag 'test'
	 *    Calendar cal = Calendar.getInstance();
	 *    cal.set(2012, 1, 1);
	 *    DeliveryReport[] reports = tmClient.getDeliveryReport("all", "test", cal.getTime(), new Date());
	 *    for(DeliveryReport report: reports) {
	 *        System.out.println(report);
	 *        for(Hashtable<String, String> row: report.getRows()) {
	 *            System.out.println("\tMessage ID: " + row.get("message_id"));
	 *            System.out.println("\tLast Updated: " + row.get("last_updated"));
	 *            System.out.println("\tMobile Number: " + row.get("mobile_number"));
	 *            System.out.println("\tStatus: " + row.get("status"));
	 *            System.out.println("\tCustom Tag: " + row.get("custom"));
	 *        }
	 *    }
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public DeliveryReport[] getDeliveryReport(String name, String custom, Date start, Date end) throws RestClientException {
    	try {
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    		xmlResponse = restGatewayCall("deliveryReport/" + URLEncoder.encode(name, "UTF-8") + "/custom/" + URLEncoder.encode(custom, "UTF-8") + "/" + URLEncoder.encode(df.format(start), "UTF-8") + "/" + URLEncoder.encode(df.format(end), "UTF-8"), HTTP_GET, null);
    		return parseDeliveryReport(xmlResponse);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    }
    
    /**
     * Create a new account (requires additional permissions on your account, please contact Text Marketer to apply)
     * 
     * @param companyName The company name for the new account owner
     * @param notificationMobile (Optional*) the mobile number of the account (*required if notificationEmail is not set)
     * @param notificationEmail (Optional*) the email address of the account (*required if notificationMobile is not set)
     * @param username (Optional) the username you wish to set on the new account - the API username will be the same
     * @param password (Optional) the password you wish to set on the new account - the API password will be the same
     * @param promoCode (Optional) a promotional code entitling the account to extra credits
     * @param overrideRates If set to true, use the credits rates set on your main account (the account used to access the API), rather than the Text Marketer defaults.
     * @return Hash table with keys: account_id, company_name, create_date, credits, notification_email, notification_mobile, username, api_username and api_password
     * @throws RestClientException
     * 
     * <p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    Hashtable<String, String> result = tmClient.createSubAccount("my subaccount", "44123456789", null, "subusername", "subpassword", null, false);
	 *    System.out.println("Account ID: " + result.get("account_id"));
	 *    System.out.println("Company Name: " + result.get("company_name"));
	 *    System.out.println("Create Date: " + result.get("create_date"));
	 *    System.out.println("Credits: " + result.get("credits"));
	 *    System.out.println("Notification Email: " + result.get("notification_email"));
	 *    System.out.println("Notification Mobile: " + result.get("notification_mobile"));
	 *    System.out.println("Username: " + result.get("username"));
	 *    System.out.println("Password: " + result.get("password"));
	 *    System.out.println("API Username: " + result.get("api_username"));
	 *    System.out.println("API Password: " + result.get("api_password"));
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String> createSubAccount(String companyName, String notificationMobile, String notificationEmail, 
            String username, String password, String promoCode, boolean overrideRates) throws RestClientException {
    	
    	Hashtable<String, String> extraparams = new Hashtable<String, String>();
    	extraparams.put("company_name", companyName);
        if(notificationMobile != null)
            extraparams.put("notification_mobile", notificationMobile);
        if(notificationEmail != null)
            extraparams.put("notification_email", notificationEmail);
        if(username != null)
            extraparams.put("account_username", username);
        if(password != null)
            extraparams.put("account_password", password);
        if(promoCode != null)
            extraparams.put("promo_code", promoCode);
        extraparams.put("override_pricing", Boolean.toString(overrideRates));
    	try {
    		xmlResponse = restGatewayCall("account/sub", HTTP_POST, extraparams);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return parseResponseXML(xmlResponse, "account");
    }
    
    /**
     * Return the last xml string returned from the last RestClient call
     * 
     * @return Xml string from the last call to RestClient API
     * 
     *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    tmClient.getGroup("directors");
	 *    xml = tmClient.getXML();
	 *    System.out.println(xml);
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public String getXML() {
    	return xmlResponse;
    }
    
    /**
     * Return the last error code raised from the last RestClient call
     * 
     * @return Error code integer or 0 if there is no error
     */
    public int getLastErrorCode() {
    	if(!errors.isEmpty()) {
    		Map.Entry<String, String> error = errors.entrySet().iterator().next();
    		return Integer.parseInt(error.getKey());
    	}
    	return 0;
    }
    
    /**
     * Return the last error message raised from the last RestClient call
     * 
     * @return Error message String or "" if there is no error
     */
    public String getLastErrorMessage() {
    	if(!errors.isEmpty()) {
    		Map.Entry<String, String> error = errors.entrySet().iterator().next();
    		return error.getValue();
    	}
    	return "";
    }
    
    /**
     * Return the all the errors raised from the last RestClient call
     * 
     * @return Errors Hashtable with all the errors codes and messages
     * 
 	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    credits = tmClient.getCredits();
	 *    System.out.println("Account have " + credits + " credits.");
	 *} catch(RestClientException e) {
	 *    Hashtable<String, String> errors = tmClient.getLastErrors();
	 *    for(Map.Entry<String, String> error: errors.entrySet())
	 *        System.out.println("Error code " + error.getKey() + ": " + error.getValue());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String> getLastErrors() {
    	return errors;
    }
    	
	/**
	 * Make the HTTP call to the REST API
	 * 
	 * @param service 	e.g. credits, sms, group, etc...
	 * @param method	HTTP method to use HTTP_GET, HTTP_POST or HTTP_PUT
	 * 
	 * @throws Exception
	 */
	private String restGatewayCall(String service, int method, Hashtable<String, String> extraparams) throws IOException, Exception {
		HttpURLConnection 	connection = null;  
		String 				strurl;
	    
		if(production)
			strurl = PROD_URL + service;
		else
			strurl = SAND_URL + service;
		
		// Construct data
		try {
			// Add class params
			StringBuffer strparams = new StringBuffer(); 
			for(Map.Entry<String, String> param: params.entrySet()) {
				strparams.append(param.getKey());
				strparams.append('=');
				strparams.append(URLEncoder.encode(param.getValue(), "UTF-8"));
				strparams.append('&');
			}
			// Add extra params
			if(extraparams != null) {
				for(Map.Entry<String, String> param: extraparams.entrySet()) {
					strparams.append(param.getKey());
					strparams.append('=');
					strparams.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					strparams.append('&');
				}
			}
			// Build URL
			if(method == HTTP_GET || method == HTTP_PUT || method == HTTP_DELETE)  
				strurl += "?" + strparams.toString();
			URL url = new URL(strurl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches (false);
			connection.setDoOutput(true);
			switch(method) {
				case HTTP_GET:
					connection.setRequestMethod("GET");
					break;
					
				case HTTP_POST:
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					connection.setRequestProperty("Content-Length", Integer.toString(strparams.toString().getBytes().length));
					connection.setDoInput(true);
					// Send request
					DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
					wr.writeBytes(strparams.toString());
					wr.flush ();
					wr.close ();
					break;
					
				case HTTP_PUT:
					connection.setRequestMethod("PUT");
					break;
				case HTTP_DELETE:
					connection.setRequestMethod("DELETE");
					break;
			}
			// Get Response	
			return getConnectionResponse(connection.getInputStream()); 
		} catch (IOException e) {
			int httpCode = connection.getResponseCode();
			String errorResponse = getConnectionResponse(connection.getErrorStream());
			errors.clear();
			if(httpCode == 400) {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder docBuilder;
				try {
					docBuilder = docBuilderFactory.newDocumentBuilder();
					Document doc = docBuilder.parse (new InputSource(new StringReader(errorResponse)));
					
					NodeList errorsList = doc.getElementsByTagName("error");
					for(int i = 0; i < errorsList.getLength(); i++) {
						Node errorMsg = errorsList.item(i);
						Node errorCode = errorsList.item(i).getAttributes().getNamedItem("code");
						errors.put(errorCode.getTextContent(), errorMsg.getTextContent());
					}
				} catch (Exception ex) {
					throw new RestClientException(e);
				}
			}
			else
				errors.put(Integer.toString(httpCode), errorResponse);
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if(connection != null)
				connection.disconnect(); 
		}
	}
	
	/**
	 * Read the response from InputStream
	 * 
	 * @param connection to read from
	 * @return String with the response
	 * 
	 * @throws Exception
	 */
	private String getConnectionResponse(InputStream is) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer(); 
		while((line = rd.readLine()) != null) {
			response.append(line);
	        response.append('\r');
		}
		rd.close();
		return response.toString();
	}
	
	/**
	 * Parse xml string and return a hashtable
	 * 
	 * @param xml string to parse
	 * @param group Child group to parse
	 * @return hashtable with node name as key and node text content as value
	 */
	private Hashtable<String, String> parseResponseXML(String xml, String group) {
		Hashtable<String, String> retValues = new Hashtable<String, String>();
		
		if(xml == null)
			return retValues;
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new InputSource(new StringReader(xml)));
			NodeList nodes;
			if(group == null || group.compareTo("") == 0)
				nodes = doc.getChildNodes().item(1).getChildNodes();
			else
				nodes = doc.getElementsByTagName(group).item(0).getChildNodes();
			for(int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE)
					retValues.put(node.getNodeName(), node.getTextContent());
			}
		} catch (Exception ex) { }
		
		return retValues;
	}
	
	/**
	 * Parse xml string and return a array of hashtables
	 * 
	 * @param xml string to parse
	 * @return array of hashtable with node name as key and node text content as value
	 */
	@SuppressWarnings("unchecked")
	private Hashtable<String, String>[] parseResponseGroupsXML(String xml, String group) {
		ArrayList<Hashtable<String, String>> hashArr = new ArrayList<Hashtable<String, String>>(); 
		
		if(xml == null)
			return (Hashtable<String, String>[]) hashArr.toArray();
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse (new InputSource(new StringReader(xml)));
			
			NodeList nodes = doc.getElementsByTagName(group);
			for(int i = 0; i < nodes.getLength(); i++) {
				NamedNodeMap attrs = nodes.item(i).getAttributes();
				Hashtable<String, String> retValues = new Hashtable<String, String>();
				for(int z = 0; z < attrs.getLength(); z++) {
					Node node = attrs.item(z);
					retValues.put(node.getNodeName(), node.getTextContent());
				}
				hashArr.add(retValues);
			}
		} catch (Exception ex) { }
		
		Hashtable<?, ?>[] arr = new Hashtable<?, ?>[hashArr.size()];
		int i = 0;
		for(Hashtable<String, String> elem: hashArr)
			arr[i++] = elem;
		return (Hashtable<String, String>[]) arr;
	}
	
	/**
	 * Parse Delivery Reports xml string and return a array of DeliveryReport Objects
	 * 
	 * @return array of DeliveryReport Objects
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws DOMException
	 * @throws ParseException
	 */
	private DeliveryReport[] parseDeliveryReport(String xml) throws ParserConfigurationException, SAXException, IOException, DOMException, ParseException {
    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
    	docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (new InputSource(new StringReader(xml)));
			
		NodeList nodes = doc.getElementsByTagName("report");
		DeliveryReport[] reports = new DeliveryReport[nodes.getLength()];
		for(int i = 0; i < nodes.getLength(); i++) {
			Node repName = nodes.item(i).getAttributes().getNamedItem("name");
			Node update = nodes.item(i).getAttributes().getNamedItem("last_updated");
			Node extension = nodes.item(i).getAttributes().getNamedItem("extension");
			reports[i] = new DeliveryReport(repName.getTextContent(), update.getTextContent(), extension.getTextContent());
			NodeList rows = nodes.item(i).getChildNodes();
			for(int z = 0; z < rows.getLength(); z++) {
				NamedNodeMap row = rows.item(z).getAttributes();
				if(row != null) {
					Node rowUpdate = row.getNamedItem("last_updated");
					Node mobile_number = row.getNamedItem("mobile_number");
					Node message_id = row.getNamedItem("message_id");
					Node status = row.getNamedItem("status");
					Node custom = row.getNamedItem("custom");
					reports[i].addRow(rowUpdate.getTextContent(), mobile_number.getTextContent(), message_id.getTextContent(), status.getTextContent(), custom.getTextContent());
				}
			}
		}
    	return reports;    		
    }
	
	 /**
     * Delete a scheduled text message.
     * 
     * @param scheduled_id	The id of the scheduled text message, as returned by the sendSMS method.
     * @return Hash table with keys: scheduled_id and status
     * @throws RestClientException on error
     * 
     * 
	 *<p><b>Example:</b></p>
	 *<blockquote><pre>
	 *{@code
	 *RestClient tmClient = new RestClient("myuser", "mypass", RestClient.ENV_SANDBOX);
	 *try {
	 *    Hashtable<String, String> result = tmClient.deleteSMS();
	 *    System.out.println("Scheduled ID:  " + result.get("scheduled_id") + ", Status: " + result.get("status"));
	 *} catch(RestClientException e) {
	 *    System.out.println(e.getMessage());
	 *}
	 *}
	 *</pre></blockquote>
     */
    public Hashtable<String, String> deleteSMS(String scheduled_id)  throws RestClientException {
    	try {
    		xmlResponse = restGatewayCall("sms/"+URLEncoder.encode(scheduled_id, "UTF-8"), HTTP_DELETE, null);
    	} catch (Exception e) {
			throw new RestClientException(e);
		}
    	return parseResponseXML(xmlResponse, "");
    }
}
