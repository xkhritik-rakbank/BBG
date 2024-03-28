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


package com.newgen.iRBL.BAISWICreate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.encryption.DataEncryption;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class iRBLBAISWICreateIntegration
{
	
	private static String sOFSessionID;
	
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, Map <String, String> BAISWICreateConfigParamMap)
	{
		String FinalStatus = "";
		try
		{
			

			String DBQuery = "SELECT BPM_REF_BAIS,RAK_CONNECT_REQ,ALL_DOCS_ATTACHED,ARABIC_DOCS,EXPRESS_CODE,AED_CURRENCY,ADDRESS_OR_EID,AECB_RESULT_STATUS,DNFBP_STATUS,INDUSTRY_CODE_AO FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE WINAME = '"+processInstanceID+"' AND (BPM_REF_BAIS IS NULL OR BPM_REF_BAIS ='')";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false));
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create data input: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create data output: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0"))
			{

				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

				HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					CheckGridDataMap.put("BPM_REF_BAIS",objWorkList.getVal("BPM_REF_BAIS"));
					CheckGridDataMap.put("RAK_CONNECT_REQ", objWorkList.getVal("RAK_CONNECT_REQ"));
					CheckGridDataMap.put("ALL_DOCS_ATTACHED", objWorkList.getVal("ALL_DOCS_ATTACHED"));
					CheckGridDataMap.put("ARABIC_DOCS", objWorkList.getVal("ARABIC_DOCS"));
					CheckGridDataMap.put("EXPRESS_CODE", objWorkList.getVal("EXPRESS_CODE"));
					CheckGridDataMap.put("AED_CURRENCY", objWorkList.getVal("AED_CURRENCY"));
					CheckGridDataMap.put("ADDRESS_OR_EID", objWorkList.getVal("ADDRESS_OR_EID"));
					CheckGridDataMap.put("AECB_RESULT_STATUS", objWorkList.getVal("AECB_RESULT_STATUS"));
					CheckGridDataMap.put("DNFBP_STATUS", objWorkList.getVal("DNFBP_STATUS"));
					CheckGridDataMap.put("INDUSTRY_CODE_AO", objWorkList.getVal("INDUSTRY_CODE_AO"));
		
					String documentTag = "";	
					String attributeTag="RAK_CONNECT_REQ"+(char)21+objWorkList.getVal("RAK_CONNECT_REQ")+(char)25+"ALL_DOCS_ATTACHED"+(char)21+objWorkList.getVal("ALL_DOCS_ATTACHED")+(char)25+"ARABIC_DOCS"+(char)21+objWorkList.getVal("ARABIC_DOCS")+(char)25;
					String WICreateInputXML = "<?xml version=\"1.0\"?>"+
									"<WFUploadWorkItem_Input>"+
									"<Option>WFUploadWorkItem</Option>"+
									"<EngineName>"+BAISWICreateConfigParamMap.get("OFCabinetName")+"</EngineName>"+
									"<SessionId>"+iRBLBAISWICreateIntegration.getSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false, BAISWICreateConfigParamMap)+"</SessionId>"+
									"<ValidationRequired><ValidationRequired>"+
									"<ProcessDefId>"+BAISWICreateConfigParamMap.get("OFBAISProcessDefId")+"</ProcessDefId>"+
									"<DataDefName></DataDefName>"+
									"<Fields></Fields>"+
									"<InitiateAlso>Y</InitiateAlso>"+
									"<Documents>"+documentTag+"</Documents>"+
									"<Attributes>"+attributeTag+"</Attributes>"+
									"</WFUploadWorkItem_Input>";
					
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create xml input: "+ WICreateInputXML);
					String WICreateOutputXML = CommonMethods.WFNGExecute(WICreateInputXML,BAISWICreateConfigParamMap.get("OFJTSIP"),BAISWICreateConfigParamMap.get("OFJTSPort"),1);
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create xml output: "+ WICreateOutputXML);
					
					XMLParser xmlParserData1= new XMLParser(WICreateOutputXML);
					if(xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0"))
					{
						String BAIS_WINAME = xmlParserData1.getValueOf("ProcessInstanceId");
						FinalStatus = "Success~"+BAIS_WINAME;
					} else 
					{
						String ErrDesc = xmlParserData1.getValueOf("Subject");
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Error In BAIS WI Create");
						FinalStatus = "Failure~"+ErrDesc;
					}
					break;
				}
			
			}
			else 
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Error In BAIS WI Create");
				FinalStatus = "Failure";
			}

		}
		catch(Exception e)
		{
			FinalStatus = "Failure";
		}
		return FinalStatus;
	}

	
	public static String getSessionID(Logger ConnectionLogger, boolean forceFulConnection, Map <String, String> BAISWICreateConfigParamMap)
	{
		String sessionId="";
		String errMsg="";
		try
		{
			ConnectionLogger.debug("Inside OF ConnectCabinet");

			if(!forceFulConnection)
			{
				sessionId = checkExistingSession(ConnectionLogger, BAISWICreateConfigParamMap);

				if (!sessionId.equalsIgnoreCase("") && !sessionId.equalsIgnoreCase("null"))
				{
					ConnectionLogger.debug("got existng OF sessionid: "+sessionId);
					setSessionID(sessionId);
					return sessionId;
				}
			}

			String connectInputXML = CommonMethods.connectCabinetInput(BAISWICreateConfigParamMap.get("OFCabinetName"),BAISWICreateConfigParamMap.get("OFUserName"),DataEncryption.decrypt(BAISWICreateConfigParamMap.get("OFPassword")));
			ConnectionLogger.debug("Input XML for OF Connect Cabinet: "+connectInputXML.substring(0,connectInputXML.indexOf("<Password>")+10)+"xxxx"+connectInputXML.substring(connectInputXML.indexOf("</Password>"),connectInputXML.length()));

			String connectOutputXML = CommonMethods.WFNGExecute(connectInputXML, BAISWICreateConfigParamMap.get("OFJTSIP"), BAISWICreateConfigParamMap.get("OFJTSPort"), 1);
			ConnectionLogger.debug("Connect OF cabinet output: "+connectOutputXML);

			XMLParser xmlparser = new XMLParser(connectOutputXML);
	        if(xmlparser.getValueOf("MainCode").equalsIgnoreCase("0"))
	        {
	        	sessionId = xmlparser.getValueOf("SessionId");
	        	ConnectionLogger.debug("Connected to OF cabinet successfully: "+sessionId);
	        	System.out.println("Connected to OF cabinet successfully: "+sessionId);

	        	xmlparser=null;
	        }
	        else
	        {
	            errMsg = xmlparser.getValueOf("Error");
	            xmlparser=null;

	            ConnectionLogger.debug("Error in OF connecting to Cabinet: "+errMsg);
	            System.out.println("Error in OF Connecting to Cabinet: "+errMsg);
	        }
		}
		catch(Exception e)
		{
			ConnectionLogger.debug("Exception in connecting to OF Cabinet: "+e.getMessage());
		}
		setSessionID(sessionId);
		return sessionId;
	}
	
	private static String checkExistingSession(Logger ConnectionLogger, Map <String, String> BAISWICreateConfigParamMap)
	{
		ConnectionLogger.debug("inside checkExistingSession");
		//String getSessionQry="select top(1) RANDOMNUMBER from pdbconnection";
		String getSessionQry="select randomnumber from pdbconnection with(nolock) where userindex in (select userindex from pdbuser with(nolock) where username='"+BAISWICreateConfigParamMap.get("OFUserName")+"')";
		String sInputXML=CommonMethods.apSelectWithColumnNames(getSessionQry, BAISWICreateConfigParamMap.get("OFCabinetName"), "");
		ConnectionLogger.debug("Input XML: "+sInputXML);
		String sOutputXML =  null;
		try
		{
			sOutputXML = CommonMethods.WFNGExecute(sInputXML, BAISWICreateConfigParamMap.get("OFJTSIP"), BAISWICreateConfigParamMap.get("OFJTSPort"), 1);
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

		String sSessionID=CommonMethods.getTagValues(sOutputXML,"randomnumber");
		ConnectionLogger.debug("SessionID: "+sSessionID);
		return sSessionID;
	}
	public static void setSessionID(String sSessionID)
	{
		iRBLBAISWICreateIntegration.sOFSessionID = sSessionID;
	}
}





