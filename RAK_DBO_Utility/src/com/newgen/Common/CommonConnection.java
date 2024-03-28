/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: Common
File Name				: CommonConnection.java
Author 					: Sakshi Grover
Date (DD/MM/YYYY)		: 30/04/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.Common;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class CommonConnection
{
	private static String sUsername;
	private static String sPassword;
	private static String sSessionID;
	private static String sCabinetName;
	private static String sJTSIP;
	private static String sJTSPort;
	private static String sSMSPort;
	private static String VolumeId;
	private static String sSiteID;
	private static NGEjbClient ngEjbClientConnection;

	static
	{
		try
		{
			ngEjbClientConnection = NGEjbClient.getSharedInstance();
		}
		catch (NGException e)
		{
			e.printStackTrace();
		}
	}

	public static String getSessionID(Logger ConnectionLogger, boolean forceFulConnection)
	{
		String sessionId="";
		String errMsg="";
		try
		{
			ConnectionLogger.debug("Inside ConnectCabinet");

			if(!forceFulConnection)
			{
				sessionId = checkExistingSession(ConnectionLogger);

				if (!sessionId.equalsIgnoreCase("") && !sessionId.equalsIgnoreCase("null"))
				{
					ConnectionLogger.debug("got existng sessionid: "+sessionId);
					setSessionID(sessionId);
					return sessionId;
				}
			}

			String connectInputXML = CommonMethods.connectCabinetInput(sCabinetName,sUsername,sPassword);
			ConnectionLogger.debug("Input XML for Connect Cabinet: "+connectInputXML.substring(0,connectInputXML.indexOf("<Password>")+10)+"xxxx"+connectInputXML.substring(connectInputXML.indexOf("</Password>"),connectInputXML.length()));

			String connectOutputXML = WFNGExecute(connectInputXML, sJTSIP, sJTSPort, 0 , ConnectionLogger);
			ConnectionLogger.debug("Connect cabinet output: "+connectOutputXML);

			XMLParser xmlparser = new XMLParser(connectOutputXML);
	        if(xmlparser.getValueOf("MainCode").equalsIgnoreCase("0"))
	        {
	        	sessionId = xmlparser.getValueOf("SessionId");
	        	ConnectionLogger.debug("Connected to cabinet successfully: "+sessionId);
	        	System.out.println("Connected to cabinet successfully: "+sessionId);

	        	xmlparser=null;
	        }
	        else
	        {
	            errMsg = xmlparser.getValueOf("Error");
	            xmlparser=null;

	            ConnectionLogger.debug("Error in connecting to Cabinet: "+errMsg);
	            System.out.println("Error in Connecting to Cabinet: "+errMsg);
	        }
		}
		catch(Exception e)
		{
			ConnectionLogger.debug("Exception in connecting to Cabinet: "+e.getMessage());
		}
		setSessionID(sessionId);
		return sessionId;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
			int flag, Logger ConnectionLogger) throws IOException, Exception
	{
		ConnectionLogger.debug("In WF NG Execute : " + serverPort);
		try
		{
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP,
						Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientConnection.makeCall(jtsServerIP, serverPort,
						"WebSphere", ipXML);
		}
		catch (Exception e)
		{
			ConnectionLogger.debug("Exception Occured in WF NG Execute : "
					+ e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}

	private static String checkExistingSession(Logger ConnectionLogger)
	{
		ConnectionLogger.debug("inside checkExistingSession");
		//String getSessionQry="select top(1) RANDOMNUMBER from pdbconnection";
		String getSessionQry="select SessionID from WFSESSIONVIEW with(nolock) where UserID in (select userindex from WFUSERVIEW with(nolock) where username='"+sUsername+"')";
		String sInputXML=CommonMethods.apSelectWithColumnNames(getSessionQry, sCabinetName, "");
		ConnectionLogger.debug("Input XML: "+sInputXML);
		String sOutputXML =  null;
		try
		{
			sOutputXML = WFNGExecute(sInputXML, sJTSIP, sJTSPort, 1 , ConnectionLogger);
			ConnectionLogger.debug("Output XML: "+sOutputXML);
		}
		catch (IOException e)
		{
			ConnectionLogger.error("IOException in checkExistingSession "+e);
			return "";
		}
		catch (Exception e)
		{
			ConnectionLogger.error("Exception in checkExistingSession "+e);
			return "";
		}

		String sSessionID=CommonMethods.getTagValues(sOutputXML,"SessionID");
		ConnectionLogger.debug("SessionID: "+sSessionID);
		return sSessionID;
	}

	public static void setUsername(String username)
	{
		CommonConnection.sUsername = username;
	}

	public static String getUsername()
	{
		return sUsername;
	}

	public static void setPassword(String password)
	{
		CommonConnection.sPassword = password;
	}

	public static void setSessionID(String sSessionID)
	{
		CommonConnection.sSessionID = sSessionID;
	}

	public static String getCabinetName()
	{
		return sCabinetName;
	}

	public static void setCabinetName(String sCabinetName)
	{
		CommonConnection.sCabinetName = sCabinetName;
	}

	public static String getJTSIP()
	{
		return sJTSIP;
	}

	public static void setJTSIP(String sJTSIP)
	{
		CommonConnection.sJTSIP = sJTSIP;
	}

	public static String getJTSPort()
	{
		return sJTSPort;
	}

	public static void setJTSPort(String jtsPort)
	{
		CommonConnection.sJTSPort = jtsPort;
	}

	public static String getsSMSPort() {
		return sSMSPort;
	}

	public static void setsSMSPort(String sSMSPort) {
		CommonConnection.sSMSPort = sSMSPort;
	}
	public static String getsVolumeID() {
		return VolumeId;
	}

	public static void setsVolumeID(String mVolumeId) {
		CommonConnection.VolumeId = mVolumeId;
	}
	
	public static String getsSiteID() {
		return sSiteID;
	}

	public static void setsSiteID(String mSiteID) {
		CommonConnection.sSiteID = mSiteID;
	}
	
}