/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: RAOPIntegration.java
Author 					: Shubham Gupta
Date (DD/MM/YYYY)		: 15/06/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.AOApprovalHold;

import java.util.HashMap;
import java.util.Map;


import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class iRBLAOApprovalHoldIntegration
{
		
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int integrationWaitTime, Map <String, String> AOApprovalHoldConfigParamMap)
	{
		String ActivityName = "";
		try
		{
			
			String DBQuery = "SELECT BPM_REF_BAIS FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE WINAME = '"+processInstanceID+"'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger, false));
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("iRBL - AO Approval Hold data input: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("iRBL - AO Approval Hold data output: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);		
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0"))
			{

				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					String BAISWIName = objWorkList.getVal("BPM_REF_BAIS").trim();
										
					
					String OFDBQuery = "select top 1 activityname from QUEUEVIEW with(nolock) where processname='BAIS' and processinstanceid = '"+BAISWIName+"' order by entryDATETIME desc";

					String OFDataIPXML = CommonMethods.apSelectWithColumnNames(OFDBQuery,CommonConnection.getOFCabinetName(), CommonConnection.getOFSessionID(iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger, false));
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("BAIS WI Status input: "+ OFDataIPXML);
					String OFDataOPXML = CommonMethods.WFNGExecute(OFDataIPXML,CommonConnection.getOFJTSIP(),CommonConnection.getOFJTSPort(),1);
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("BAIS WI Status output: "+ OFDataOPXML);

					XMLParser xmlParserData1= new XMLParser(OFDataOPXML);						
					
					if(xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0"))
					{
						String xmlDataExtTab1=xmlParserData1.getNextValueOf("Record");
						xmlDataExtTab1 =xmlDataExtTab1.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
						
						NGXmlList objWorkList1=xmlParserData1.createList("Records", "Record");

						for (; objWorkList1.hasMoreElements(true); objWorkList1.skip(true))
						{		
							ActivityName = objWorkList1.getVal("activityname").trim()+"~"+BAISWIName;
							break;
						}
					}
					else 
					{
						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Error In BAIS WI Status");
					}
				}
			}
			else 
			{
				iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Error In main BAIS WI Status");
			}

		}
		catch(Exception e)
		{
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("exception In main BAIS WI Status"+e.getMessage());
		}
		return ActivityName;
	}
	
}





