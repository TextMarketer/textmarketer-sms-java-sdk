package uk.co.textmarketer.RestAPI;

/**
 * <a href='http://www.textmarketer.co.uk/developers/restful-api.htm'>RESTful
 * API</a>
 * <p>
 * 
 * Copyright © 2017 Text Marketer Ltd
 * </p>
 * 
 * This is an exception that is thrown whenever a RestClient call generates a
 * error.
 * 
 * @author Marco Morais
 * @version 1.0
 */

public class RestClientException extends Exception {
	public RestClientException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = 1851079605510716440L;
}
