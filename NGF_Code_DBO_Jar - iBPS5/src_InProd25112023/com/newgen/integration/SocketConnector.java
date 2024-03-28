package com.newgen.integration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.json.simple.JSONObject;

public class SocketConnector {
	
	@SuppressWarnings("unchecked")
	public JSONObject getSocketJSONResponse(String callName) {
		Socket socket = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		JSONObject jsonRequest = null;
		JSONObject jsonResponse = null;
		String serverIP = "", serverPort = "";
		serverIP = GetJSON.prop.getProperty("SocketServerIP");
		serverPort = GetJSON.prop.getProperty("SocketServerPort");
		try {
			socket = new Socket(serverIP, Integer.parseInt(serverPort));
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = null; 
			jsonRequest = new JSONObject();
			jsonRequest.put("NewgenIntegrationCallName", callName);
			oos.writeObject(jsonRequest);
			ois = new ObjectInputStream(socket.getInputStream());
			jsonResponse = (JSONObject)ois.readObject(); 
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
				try {
					if(socket!=null) {
						socket.close();
						socket = null;
					}
					if(oos != null) {
						oos.close();
						oos = null;
					}
					if(ois != null) {
						ois.close();
						ois = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return jsonResponse;
	}

}
