/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: Main
File Name				: RAKBankUtility.java
Author 					: Sakshi Grover
Date (DD/MM/YYYY)		: 30/04/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import com.newgen.Common.CommonConnection;
import com.newgen.DBO.AWB_Delivery.DBO_AWBGenration;
import com.newgen.DBO.AWB_Delivery.DBO_PrimeCBS_File_Read;
//import com.newgen.DBO.AWB_Delivery.DBO_AWBGenration;
//import com.newgen.DBO.AWB_Delivery.DBO_PrimeCBS_File_Read;
import com.newgen.DBO.AttachDocument.DBO_AttachDocuments;
import com.newgen.DBO.CBS_Update.DBO_CBS_Update;
import com.newgen.DBO.Notify_DEH.DBO_Notify_DEH;
import com.newgen.DBO.SignatureCrop.DBO_SignatureCrop;
import com.newgen.encryption.DataEncryption;


public class StartUtility
{
	private static Map<String, String> mainPropMap= new HashMap<String, String>();
	private static String loggerName = "MainLogger";
	private static org.apache.log4j.Logger MainLogger = org.apache.log4j.Logger.getLogger(loggerName);

	 static
	 {
			setLogger();
	 }

	public static void main(String[] args)
	{
		System.out.println("Starting utility...");
		MainLogger.info("Starting Utility");
		int mainPropFileReadCode = readMainPropFile();

		if(mainPropFileReadCode!=0)
		{
			System.out.println("Error in Readin Main Property FIle");
			MainLogger.error("Error in Readin Main Property FIle "+mainPropFileReadCode);
			return;
		}

		try
		{
			int socketPort =  Integer.parseInt(mainPropMap.get("Utility_Port"));
			if(socketPort==0)
			{
				System.out.println("Not able to Get Utility Port");
				MainLogger.error("Not able to Get Utility Port "+socketPort);
				return;
			}
			ServerSocket serverSocket = new ServerSocket(socketPort);



			CommonConnection.setUsername(mainPropMap.get("UserName"));
			CommonConnection.setPassword(DataEncryption.decrypt(mainPropMap.get("Password")));
			CommonConnection.setJTSIP(mainPropMap.get("JTSIP"));
			CommonConnection.setJTSPort(mainPropMap.get("JTSPort"));
			CommonConnection.setsSMSPort(mainPropMap.get("SMSPort"));
			CommonConnection.setCabinetName(mainPropMap.get("CabinetName"));
			CommonConnection.setsVolumeID(mainPropMap.get("VolumeID"));
			CommonConnection.setsSiteID(mainPropMap.get("SiteID"));
			
			String sessionID = CommonConnection.getSessionID(MainLogger,true);

			if(sessionID==null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				MainLogger.info("Could Not Get Session ID "+sessionID);
				return;
			}
			if(mainPropMap.get("DBO_Prime_CBS")!=null && mainPropMap.get("DBO_Prime_CBS").equalsIgnoreCase("Y"))
			{
				Thread Prime_CBSThread = new Thread(new DBO_PrimeCBS_File_Read());
				Prime_CBSThread.start();
				System.out.println("DBO to Prime_CBSThread Started");
				MainLogger.info("DBO to DEH Notify Started");
			}

			if(mainPropMap.get("DBO_ATTACHDOC")!=null && mainPropMap.get("DBO_ATTACHDOC").equalsIgnoreCase("Y"))
			{
				Thread DBO_Attach_DOC_Thread = new Thread(new DBO_AttachDocuments());
				DBO_Attach_DOC_Thread.start();
				System.out.println("DBO Attach Doc Started");
				MainLogger.info("DBO Attach Doc Started");
			}
			
			if(mainPropMap.get("DBO_DEH_NOTIFICATION")!=null && mainPropMap.get("DBO_DEH_NOTIFICATION").equalsIgnoreCase("Y"))
			{
				Thread notifyDEHThread = new Thread(new DBO_Notify_DEH());
				notifyDEHThread.start();
				System.out.println("DBO to DEH Notify Started");
				MainLogger.info("DBO to DEH Notify Started");
			}
			
			// added by gaurav 
			
			if(mainPropMap.get("DBO_AWB_COURIER")!=null && mainPropMap.get("DBO_AWB_COURIER").equalsIgnoreCase("Y"))
			{
				Thread awbGenThread = new Thread(new DBO_AWBGenration());
				awbGenThread.start();
				System.out.println("AWB Genration Started");
				MainLogger.info("AWB Genrarion Started");
			}
			if(mainPropMap.get("ReadQRCropSign")!=null && mainPropMap.get("ReadQRCropSign").equalsIgnoreCase("Y"))
			{
				Thread ReadQRCropSignThread = new Thread(new DBO_SignatureCrop());
				ReadQRCropSignThread.start();
				System.out.println("DBO to ReadQRCropSign Started");
				MainLogger.info("DBO to ReadQRCropSign Started");
			}
			if(mainPropMap.get("DBOCBSUpdate")!=null && mainPropMap.get("DBOCBSUpdate").equalsIgnoreCase("Y"))
			{
				Thread DBOCBSUpdateThread = new Thread(new DBO_CBS_Update());
				DBOCBSUpdateThread.start();
				System.out.println("DBO CBS Update Thread Started");
				MainLogger.info("DBO CBS Update Thread Started");
			}
		}
		catch (Exception e)
		{
			if(e.getMessage().toUpperCase().startsWith("Address already in use".toUpperCase()))
			{
				System.out.println("Utility Instance Already Running");
				MainLogger.error("Utility Instance Already Running");
			}
			else
			{
				e.printStackTrace();
				MainLogger.error("Exception Occurred in Main Thread: "+e);
				final Writer result = new StringWriter();
				final PrintWriter printWriter = new PrintWriter(result);
				e.printStackTrace(printWriter);
				MainLogger.error("Exception Occurred in Main Thread : "+result);
			}
			return;
		}
		finally
		{
			System.gc();
		}
	}

	private static void setLogger()
	{
		try
		{
			Date date = new Date();
			DateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir")+ File.separator + "log4jFiles"+ File.separator+ "Main_log4j.properties"));
			String dynamicLog = null;
			String orgFileName = null;
			File d = null;
			File fl = null;

			dynamicLog = "Logs/Main_Logs/"+logDateFormat.format(date)+"/Main_Log.xml";
			orgFileName = p.getProperty("log4j.appender."+loggerName+".File");
			if(!(orgFileName==null || orgFileName.equalsIgnoreCase("")))
			{
				dynamicLog = orgFileName.substring(0,orgFileName.lastIndexOf("/")+1)+logDateFormat.format(date)+orgFileName.substring(orgFileName.lastIndexOf("/"));
			}
			d = new File(dynamicLog.substring(0,dynamicLog.lastIndexOf("/")));
			d.mkdirs();
			fl = new File(dynamicLog);
			if(!fl.exists())
				fl.createNewFile();
			p.put("log4j.appender."+loggerName+".File", dynamicLog );

			PropertyConfigurator.configure(p);
			//System.out.println("Dynamic Logger Created");
		}
		catch(Exception e)
		{
			System.out.println("Exception in creating dynamic log :"+e);
			e.printStackTrace();
		}
	}

	private static int readMainPropFile()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "Main_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			{
			    String name = (String) names.nextElement();
			    mainPropMap.put(name, p.getProperty(name));
			}

		} catch (Exception e) {

			return -1 ;
		}
		return 0;
	}
}