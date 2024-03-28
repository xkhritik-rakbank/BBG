package com.newgen.integration;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.newgen.iforms.custom.IFormReference;

import com.newgen.iforms.user.DBO;

public class GetJSON
{
	public static Properties prop = new Properties();
	static JSONParser parser = new JSONParser();	
	
	public JSONObject getRequestJson(String callName, IFormReference iformObj)
	{
		JSONObject jsonRequest = null;
		try {
			prop.load(new FileReader(System.getProperty("user.dir") + File.separator + "AO_UBA_Properties" + File.separator + "properties" + File.separator + "Integration.properties"));
			//if(((String)GetJSON.prop.get("DUMMYCALL")).equalsIgnoreCase("true"))
				//return new URLConnectivity().dummyResponse(callName);
			Object obj = parser.parse(new FileReader(System.getProperty("user.dir") + File.separator + "AO_UBA_Properties" + File.separator + "JSONFiles" + File.separator + callName + ".json"));
			jsonRequest = (JSONObject) obj;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		for (Object key : jsonRequest.keySet()) {
			String value = (String) jsonRequest.get((String)key);
			if(value.contains("default"))
			{
				//TODO get from properties file.
				jsonRequest.put((String)key, (value.split("~"))[1]);
			}
			else if(value.contains("formid"))
			{
				if(iformObj!=null)
					value = (String) iformObj.getValue(value.split("~")[1]);
				jsonRequest.put((String)key, value);
			}
			else if(value.contains("custom"))
			{
				
			}
		}
		return jsonRequest;
	}

	public JSONObject getFormJSON(String callName) {
		try {
			Object formJSON = parser.parse(new FileReader(System.getProperty("user.dir") + File.separator + "AO_UBA_Properties" + File.separator + "JSONFiles" + File.separator + callName + "_FormID.json"));
			return (JSONObject) formJSON;
		}catch(Exception e) {
			e.printStackTrace(); 
			DBO.mLogger.info("Exception: Unable to find FormJSON : callName is: " +callName);
			return null;
		}
	}
}