/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: Common
File Name				: CommonMethods.java
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
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class CommonMethods
{
	private static NGEjbClient ngEjbClientiRBLStatus;
	
	static
	{
	  try
      {
		  ngEjbClientiRBLStatus = NGEjbClient.getSharedInstance();
      }
    catch (NGException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	}
	
	public static String connectCabinetInput(String cabinetName, String username, String password)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMConnect_Input>\n");
		ipXMLBuffer.append("<Option>WMConnect</Option>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<Participant>\n");
		ipXMLBuffer.append("<Name>");
		ipXMLBuffer.append(username);
		ipXMLBuffer.append("</Name>\n");
		ipXMLBuffer.append("<Password>");
		ipXMLBuffer.append(password);
		ipXMLBuffer.append("</Password>\n");
		ipXMLBuffer.append("<Scope></Scope>\n");
		ipXMLBuffer.append("<UserExist>N</UserExist>\n");
		ipXMLBuffer.append("<Locale>en-us</Locale>\n");
		ipXMLBuffer.append("<ParticipantType>U</ParticipantType>\n");
		ipXMLBuffer.append("</Particpant>\n");
		ipXMLBuffer.append("</WMConnect_Input>");

		return ipXMLBuffer.toString();

	}

	public static String disconnectCabinetInput(String cabinetName,String sessionID)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<NGODisconnectCabinet_Input>\n");
		ipXMLBuffer.append("<Option>NGODisconnectCabinet</Option>\n");
		ipXMLBuffer.append("<CabinetName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</CabinetName>\n");
		ipXMLBuffer.append("<UserDBId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</UserDBId>\n");
		ipXMLBuffer.append("</NGODisconnectCabinet_Input>");

		return ipXMLBuffer.toString();
	}

	public static String fetchWorkItemsInput(String cabinetName,String sessionID, String queueID )
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMFetchWorkItems_Input>\n");
		ipXMLBuffer.append("<Option>WMFetchWorkItem</Option>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionID>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionID>\n");
		ipXMLBuffer.append("<QueueId>");
		ipXMLBuffer.append(queueID);
		ipXMLBuffer.append("</QueueId>\n");
		ipXMLBuffer.append("<BatchInfo>\n");
		ipXMLBuffer.append("<NoOfRecordsToFetch>10</NoOfRecordsToFetch>\n");
		ipXMLBuffer.append("<LastWorkItem></LastWorkItem>\n");
		ipXMLBuffer.append("<LastValue></LastValue>\n");
		ipXMLBuffer.append("<LastProcessInstance></LastProcessInstance>\n");
		ipXMLBuffer.append("</BatchInfo>\n");
		ipXMLBuffer.append("</WMFetchWorkItems_Input>\n");
		return ipXMLBuffer.toString();

	}

	public static String apSelectWithColumnNames(String QueryString, String cabinetName, String sessionID)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<APSelect_Input>\n");
		ipXMLBuffer.append("<Option>APSelectWithColumnNames</Option>\n");
		ipXMLBuffer.append("<Query>");
		ipXMLBuffer.append(QueryString);
		ipXMLBuffer.append("</Query>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("</APSelect_Input>");

		return ipXMLBuffer.toString();
	}
	public static String apUpdateInput(String cabinetName,String sessionID, String tableName, String columnName,
			 String strValues,String sWhereClause)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<APUpdate_Input>\n");
		ipXMLBuffer.append("<Option>APUpdate</Option>\n");
		ipXMLBuffer.append("<TableName>");
		ipXMLBuffer.append(tableName);
		ipXMLBuffer.append("</TableName>\n");
		ipXMLBuffer.append("<ColName>");
		ipXMLBuffer.append(columnName);
		ipXMLBuffer.append("</ColName>\n");
		ipXMLBuffer.append("<Values>");
		ipXMLBuffer.append(strValues);
		ipXMLBuffer.append("</Values>\n");
		ipXMLBuffer.append("<WhereClause>");
		ipXMLBuffer.append(sWhereClause);
		ipXMLBuffer.append("</WhereClause>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("</APUpdate_Input>");

		return ipXMLBuffer.toString();

	 }

	public static String assignWorkitemAttributeInput(String sCabinetName,String sessionID, String workItemName, String WorkItemID,String ActivityId,String ActivityType, String attributesTag,String completeWIFlag)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMAssignWorkItemAttributes_Input>\n");
		ipXMLBuffer.append("<Option>WMAssignWorkItemAttributes</Option>");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(sCabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("<ProcessInstanceId>");
		ipXMLBuffer.append(workItemName);
		ipXMLBuffer.append("</ProcessInstanceId>\n");
		ipXMLBuffer.append("<WorkItemId>");
		ipXMLBuffer.append(WorkItemID);
		ipXMLBuffer.append("</WorkItemId>\n");
		ipXMLBuffer.append("<ActivityId>");
		ipXMLBuffer.append(ActivityId);
		ipXMLBuffer.append("</ActivityId>");
		ipXMLBuffer.append("<LastModifiedTime></LastModifiedTime>\n");
		ipXMLBuffer.append("<ActivityType>");
		ipXMLBuffer.append(ActivityType);
		ipXMLBuffer.append("</ActivityType>");
		ipXMLBuffer.append("<complete>");
		ipXMLBuffer.append(completeWIFlag);
		ipXMLBuffer.append("</complete>\n");
		ipXMLBuffer.append("<UserDefVarFlag>Y</UserDefVarFlag>\n");
		ipXMLBuffer.append("<Attributes>");
		ipXMLBuffer.append(attributesTag);
		ipXMLBuffer.append("</Attributes>\n");
		ipXMLBuffer.append("</WMAssignWorkItemAttributes_Input>");

		return ipXMLBuffer.toString();

	}
	public static String getWorkItemInput(String sCabinetName, String sessionID, String workItemName, String WorkItemID)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMGetWorkItem_Input>\n");
		ipXMLBuffer.append("<Option>WMGetWorkItem</Option>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(sCabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("<ProcessInstanceId>");
		ipXMLBuffer.append(workItemName);
		ipXMLBuffer.append("</ProcessInstanceId>\n");
		ipXMLBuffer.append("<WorkItemId>");
		ipXMLBuffer.append(WorkItemID);
		ipXMLBuffer.append("</WorkItemId>\n");
		ipXMLBuffer.append("</WMGetWorkItem_Input>");

		return ipXMLBuffer.toString();
	}
	public static String completeWorkItemInput(String cabName, String sessionID, String workItemName, String WorkItemID){

		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMCompleteWorkItem_Input>\n");
		ipXMLBuffer.append("<Option>WMCompleteWorkItem</Option>");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("<ProcessInstanceId>");
		ipXMLBuffer.append(workItemName);
		ipXMLBuffer.append("</ProcessInstanceId>\n");
		ipXMLBuffer.append("<WorkItemId>");
		ipXMLBuffer.append(WorkItemID);
		ipXMLBuffer.append("</WorkItemId>\n");
		ipXMLBuffer.append("<AuditStatus></AuditStatus>\n");
		ipXMLBuffer.append("<Comments></Comments>\n");
		ipXMLBuffer.append("</WMCompleteWorkItem_Input>");

		return ipXMLBuffer.toString();
	}

	public static String apInsert(String sCabName, String sSessionId, String colNames, String colValues, String tableName)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<APInsertExtd_Input>\n");
		ipXMLBuffer.append("<Option>APInsert</Option>");
		ipXMLBuffer.append("<TableName>");
		ipXMLBuffer.append(tableName);
		ipXMLBuffer.append("</TableName>");
		ipXMLBuffer.append("<ColName>");
		ipXMLBuffer.append(colNames);
		ipXMLBuffer.append("</ColName>\n");
		ipXMLBuffer.append("<Values>");
		ipXMLBuffer.append(colValues);
		ipXMLBuffer.append("</Values>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(sCabName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sSessionId);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("</APInsertExtd_Input>");

		return ipXMLBuffer.toString();
	}
	public static String getdateCurrentDateInSQLFormat()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
		return simpleDateFormat.format(new Date());
	}

	public static String getAPUpdateIpXML(String tableName,String columnName,String strValues,String sWhere,String cabinetName,String sessionId)
	{
		if(strValues==null)
		{
			strValues = "''";
		}

		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<APUpdate_Input>\n");
		ipXMLBuffer.append("<Option>APUpdate</Option>");
		ipXMLBuffer.append("<TableName>");
		ipXMLBuffer.append(tableName);
		ipXMLBuffer.append("</TableName>\n");
		ipXMLBuffer.append("<ColName>");
		ipXMLBuffer.append(columnName);
		ipXMLBuffer.append("</ColName>\n");
		ipXMLBuffer.append("<Values>");
		ipXMLBuffer.append(strValues);
		ipXMLBuffer.append("</Values>\n");
		ipXMLBuffer.append("<WhereClause>");
		ipXMLBuffer.append(sWhere);
		ipXMLBuffer.append("</WhereClause>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionId);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("</APUpdate_Input>\n");

		return ipXMLBuffer.toString();
	}

	public static String getFetchWorkItemAttributesXML(String sCabinetName,String sessionID, String workItemName, String WorkItemID)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMFetchWorkItemAttributes_Input>\n");
		ipXMLBuffer.append("<Option>WMFetchWorkItemAttributes</Option>");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(sCabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("<ProcessInstanceId>");
		ipXMLBuffer.append(workItemName);
		ipXMLBuffer.append("</ProcessInstanceId>\n");
		ipXMLBuffer.append("<WorkItemId>");
		ipXMLBuffer.append(WorkItemID);
		ipXMLBuffer.append("</WorkItemId>\n");
		ipXMLBuffer.append("</WMFetchWorkItemAttributes_Input>");


		return ipXMLBuffer.toString();

	}

	public static String getFetchWorkItemsInputXML(String processInstanceId, String lastWorkItemId,  String sessionId, String cabinetName, String queueId)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMFetchWorkItems_Input>\n");
		ipXMLBuffer.append("<Option>WMFetchWorkItem</Option>");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionID>");
		ipXMLBuffer.append(sessionId);
		ipXMLBuffer.append("</SessionID>\n");
		ipXMLBuffer.append("<QueueId>");
		ipXMLBuffer.append(queueId);
		ipXMLBuffer.append("</QueueId>\n");
		ipXMLBuffer.append("<BatchInfo>\n");
		ipXMLBuffer.append("<NoOfRecordsToFetch>100</NoOfRecordsToFetch>\n");
		ipXMLBuffer.append("<LastWorkItem>");
		ipXMLBuffer.append(lastWorkItemId);
		ipXMLBuffer.append("</LastWorkItem>\n");
		ipXMLBuffer.append("<LastValue></LastValue>\n");
		ipXMLBuffer.append("<LastProcessInstance>");
		ipXMLBuffer.append(processInstanceId);
		ipXMLBuffer.append("</LastProcessInstance>\n");
		ipXMLBuffer.append("</BatchInfo>\n");
		ipXMLBuffer.append("</WMFetchWorkItems_Input>");

		return ipXMLBuffer.toString();
	}

	public static String getNGOAddDocument(String parentFolderIndex, String strDocumentName,String fileName,String DocumentType,String strExtension,
			String sISIndex,String lstrDocFileSize, String volumeID, String cabinetName, String sessionId)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<NGOAddDocument_Input>\n");
		ipXMLBuffer.append("<Option>NGOAddDocument</Option>");
		ipXMLBuffer.append("<CabinetName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</CabinetName>\n");
		ipXMLBuffer.append("<UserDBId>");
		ipXMLBuffer.append(sessionId);
		ipXMLBuffer.append("</UserDBId>\n");
		ipXMLBuffer.append("<GroupIndex>0</GroupIndex>\n");
		ipXMLBuffer.append("<Document>\n");
		ipXMLBuffer.append("<VersionFlag>Y</VersionFlag>\n");
		ipXMLBuffer.append("<ParentFolderIndex>");
		ipXMLBuffer.append(parentFolderIndex);
		ipXMLBuffer.append("</ParentFolderIndex>\n");
		ipXMLBuffer.append("<DocumentName>");
		ipXMLBuffer.append(strDocumentName);
		ipXMLBuffer.append("</DocumentName>\n");
		ipXMLBuffer.append("<VolumeIndex>");
		ipXMLBuffer.append(volumeID);
		ipXMLBuffer.append("</VolumeIndex>\n");
		ipXMLBuffer.append("<ISIndex>");
		ipXMLBuffer.append(sISIndex);
		ipXMLBuffer.append("</ISIndex>\n");
		ipXMLBuffer.append("<NoOfPages>1</NoOfPages>\n");
		ipXMLBuffer.append("<DocumentType>");
		ipXMLBuffer.append(DocumentType);
		ipXMLBuffer.append("</DocumentType>\n");
		ipXMLBuffer.append("<DocumentSize>");
		ipXMLBuffer.append(lstrDocFileSize);
		ipXMLBuffer.append("</DocumentSize>\n");
		ipXMLBuffer.append("<CreatedByAppName>");
		ipXMLBuffer.append(strExtension);
		ipXMLBuffer.append("</CreatedByAppName>\n");
		ipXMLBuffer.append("<Comment>");
		ipXMLBuffer.append(fileName);
		ipXMLBuffer.append("</Comment>\n");
		ipXMLBuffer.append("</Document>\n");
		ipXMLBuffer.append("</NGOAddDocument_Input>\n");
		return ipXMLBuffer.toString();
    }

	public static String getTagValues (String sXML, String sTagName)
	{
		String sTagValues = "";
		String sStartTag = "<" + sTagName + ">";
		String sEndTag = "</" + sTagName + ">";
		String tempXML = sXML;
	    try
	    {
			for(int i=0;i<sXML.split(sEndTag).length;i++)
			{
				if(tempXML.indexOf(sStartTag) != -1)
				{
					sTagValues += tempXML.substring(tempXML.indexOf(sStartTag) + sStartTag.length(), tempXML.indexOf(sEndTag));
					//System.//out.println("sTagValues"+sTagValues);
					tempXML=tempXML.substring(tempXML.indexOf(sEndTag) + sEndTag.length(), tempXML.length());
		        }
				if(tempXML.indexOf(sStartTag) != -1)
				{
					sTagValues +="`";
					//System.//out.println("sTagValues"+sTagValues);

				}
				//System.//out.println("sTagValues"+sTagValues);
			}
			//System.//out.println(" Final sTagValues"+sTagValues);
		}

		catch(Exception e)
		{
		}
		return sTagValues;
	}

	 public static int getMainCode(String xml) throws Exception
	 {
			String code = "";
			try {
				code = getTagValues(xml, "MainCode");
			} catch (Exception e) {
				throw e;
			}
			int mainCode = -1;
			try {
				mainCode = Integer.parseInt(code);
			} catch (NumberFormatException e) {
				mainCode = -1;
			}
			return mainCode;
	}

	public static Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException
	{
			//mLogger.error("mapxml 4 "+xml);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(xml)));
			return doc;
	}

	public static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		System.out.println("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientiRBLStatus.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			System.out.println("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	public static String getTagValues(Node node, String tag) {
		//mLogger.error("Let's see");
		String value = "";
		NodeList nodeList = node.getChildNodes();
		int length = nodeList.getLength();
		for (int i = 0; i < length; ++i) {
			Node child = nodeList.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& child.getNodeName().equalsIgnoreCase(tag)) {
				return child.getTextContent();
			}
		}
		return value;
	}
	
	public static long findDifference(String strDate, String DateFormat, String ReturnType) // method called to get year difference based on date of birth to identify minor customer
    {
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
        long difference_In_Time = 0;
        long difference_In_Years = 0;
        long difference_In_Days = 0;
        long difference_In_Hours = 0;
        long difference_In_Minutes = 0;
        long difference_In_Seconds = 0;
        try {
            Date d1 = null;
            try {
                d1 = sdf.parse(strDate);
            } catch (java.text.ParseException e) {
            	 System.out.println("Exception in parsing dates: "+e.getMessage());
                e.printStackTrace();
            }
            Date d2 = new Date();
            difference_In_Time = d2.getTime() - d1.getTime();
            difference_In_Seconds = (difference_In_Time / 1000) % 60;
            difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
            difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;
            difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
    		
            System.out.println("Difference between two dates is:");
            System.out.println(difference_In_Years
                + " years, "
                + difference_In_Days
                + " days, "
                + difference_In_Hours
                + " hours, "
                + difference_In_Minutes
                + " minutes, "
                + difference_In_Seconds
                + " seconds");		
    	}
        catch (Exception e) {
        	 System.out.println("Exception in main parsing dates: "+e.getMessage());
            e.printStackTrace();
        }
        
        if("Years".equalsIgnoreCase(ReturnType))
        	return difference_In_Years;
        else if("Days".equalsIgnoreCase(ReturnType))
        	return difference_In_Days;
        else if("Hours".equalsIgnoreCase(ReturnType))
        	return difference_In_Hours;
        else if("Minutes".equalsIgnoreCase(ReturnType))
        	return difference_In_Minutes;
        if("Seconds".equalsIgnoreCase(ReturnType))
        	return difference_In_Seconds;
        
        return difference_In_Time; // won't be satified ever
    }
	
}


