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

import java.util.Map;


import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class iRBLHoldInBAISProcessIntegration
{
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime, Map <String, String> iRBLHoldInBAISProcessConfigParamMap)
	{
		String ExcepRaisedFlag = "";
		try
		{
			String DBQuery = "select count(*) as ExceptionCount from USR_0_IRBL_EXCEPTION_HISTORY with(nolock) where IS_RAISED='true' and WI_NAME in (select WINAME from RB_iRBL_EXTTABLE with(nolock) where BPM_REF_BAIS = '"+processInstanceID+"')";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger, false));
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Exception status input: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Exception status output: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			//int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0"))
			{
				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					int ExcepCount = Integer.parseInt(objWorkList.getVal("ExceptionCount"));
					ExcepRaisedFlag = "N";
					if (ExcepCount>0)
						ExcepRaisedFlag = "Y";
				}
			}
			else 
			{
				iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Error In mail BAIS WI Create");
				ExcepRaisedFlag = "F";
			}

		}
		catch(Exception e)
		{
			ExcepRaisedFlag = "F";
		}
		return ExcepRaisedFlag;
	}

}
