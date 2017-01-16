package uk.co.textmarketer.RestAPI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

/**
 * DeliveryReport Class represent a delivery report status from a sent SMS message<p>
 * 
 * Copyright © 2017 Text Marketer Ltd<p>
 * 
 * @author Marco Morais  
 * @version 1.0
 * @see RestClient#getDeliveryReport(String)
 */
public class DeliveryReport {
	public String name;
	public Date lastUpdate;
	public String extension;
	private ArrayList<Hashtable<String, String>> rows;
	
	/**
	 * Constructor for the DeliveryReport class.<p>
	 * 
	 * @param name 			Report name
	 * @param lastUpdate	Date of the last report update
	 * @param extension		extension of the report file, e.g. csv
	 */
	public DeliveryReport(String name, Date lastUpdate, String extension) {
		this.name = name;
		this.lastUpdate = lastUpdate;
		this.extension = extension;
		rows = new ArrayList<Hashtable<String, String>>();
	}
	
	public DeliveryReport(String name, String lastUpdate, String extension) throws ParseException {
		this(name, (Date)new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(lastUpdate), extension);
	}
	
	/**
	 * Constructor for the DeliveryReport class.
	 */
	public DeliveryReport() {
		this("", new Date(), "");
	}

	@Override
	public String toString() {
		return "DeliveryReport [name=" + name + ", lastUpdate=" + lastUpdate
				+ ", extension=" + extension + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public void addRow(String last_updated, String mobile_number, String message_id, String status, String custom) {
		Hashtable<String, String> row = new Hashtable<String, String>();
		row.put("last_updated", last_updated);
		row.put("mobile_number", mobile_number);
		row.put("message_id", message_id);
		row.put("status", status);
		row.put("custom", custom);
		rows.add(row);
	}
	
	/**
	 * Return report rows for this Delivery Report
	 * 
	 * @return Hashtable array with keys: last_updated, mobile_number, message_id, status and custom
	 */
	@SuppressWarnings("unchecked")
	public Hashtable<String, String>[] getRows() {
		Hashtable<?, ?>[] arr = new Hashtable<?, ?>[rows.size()];
		int i = 0;
		for(Hashtable<String, String> elem: rows)
			arr[i++] = elem;
		return (Hashtable<String, String>[]) arr;
	}
}
