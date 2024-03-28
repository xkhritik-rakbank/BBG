/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects 2
Project/Product			: RAK iRBL
Application				: RAK iRBL Utility
Module					: iRBL BAIS Hold queue
File Name				: RAOPStatus.java
Author 					: Angad Shah
Date (DD/MM/YYYY)		: 23/06/2021

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.iRBLHoldInBAISProcess;


import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


import org.apache.log4j.*;

public final class iRBLHoldInBAISProcessLog
{
	private static String loggerName = "iRBLHoldInBAISProcessLogger";
    protected static org.apache.log4j.Logger iRBLHoldInBAISProcessLogger = org.apache.log4j.Logger.getLogger(loggerName);;

    static
    {
    	setLogger();
    }

    protected static void setLogger()
    {
    	try
		{
    		Date date = new Date();
			DateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir")+ File.separator + "log4jFiles"+ File.separator+ "BAIS_iRBLHoldInBAISProcess_log4j.properties"));
			String dynamicLog = null;
			String orgFileName = null;
			File d = null;
			File fl = null;

			dynamicLog = "Logs/BAIS_iRBLHoldInBAISProcess_Logs/"+logDateFormat.format(date)+"/BAIS_iRBLHoldInBAISProcess_Log.xml";
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }
}
