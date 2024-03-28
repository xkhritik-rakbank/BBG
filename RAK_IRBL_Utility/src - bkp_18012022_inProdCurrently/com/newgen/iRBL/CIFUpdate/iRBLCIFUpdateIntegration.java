/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM - iRBL
Application				: RAK iRBL Utility
Module					: CIF Update
File Name				: Integration.java
Author 					: Angad Shah
Date (DD/MM/YYYY)		: 21/05/2021

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.CIFUpdate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class iRBLCIFUpdateIntegration
{

	
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		String FinalStatus = "";
		try
		{
			iRBLCIFUpdateLog.setLogger();
			
			String DBQuery = "SELECT 'Primary' AS CIFType, CIF_NUMBER AS CIFID, 'C' AS CustType, NTBEXISTING as NTBEXISTING, CIFUPDATEREQMAIN AS CIFUpdateStatus, PREFERREDADDRLINE1, PREFERREDADDRLINE2, PREFERREDADDRLINE3, PREFERREDADDRLINE4, PREFERREDCITY, PREFERREDCOUNTRYCODE, PREFERREDPOBOX, PREFERREDSTATE, PREFERREDZIPCODE, NUMBEROFYEARSINBUSINESS AS YEARSINBUSINESS, '' AS VISANUMBER, NULL AS VISAEXPIRYDATE,'' AS NAMEOFORGANIZATION, (select top 1 INDUSTRY_CODE from USR_0_IRBL_INDUSTRY_CODE_DTLS with(nolock) where WI_NAME = '"+processInstanceID+"') as INDUSTRY_CODE, (select top 1 INDUSTRY_SUB_CATEGORY from USR_0_IRBL_INDUSTRY_CODE_DTLS with(nolock) where WI_NAME = '"+processInstanceID+"') as INDUSTRY_SUB_CATEGORY, NUMBEROFYEARSINBUSINESS FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE (CIFUPDATEREQMAIN is null OR CIFUPDATEREQMAIN != 'Success') AND CIF_NUMBER IS NOT NULL AND CIF_NUMBER != '' AND WINAME = '"+processInstanceID+"'"+
					"UNION ALL "+
					"SELECT 'Related' AS CIFType, CIF AS CIFID, CASE WHEN COMPANYFLAG='Y' THEN 'C' WHEN COMPANYFLAG='YES' THEN 'C' ELSE 'R' END AS CustType, EXISTINGNTB as NTBEXISTING, CIFUPDATEREQREL AS CIFUpdateStatus, PREFERREDADDRLINE1, PREFERREDADDRLINE2, PREFERREDADDRLINE3, PREFERREDADDRLINE4, PREFERREDCITY, PREFERREDCOUNTRYCODE, PREFERREDPOBOX, PREFERREDSTATE, PREFERREDZIPCODE, '' AS YEARSINBUSINESS, VISANUMBER, isnull(format(VISAEXPIRYDATE,'yyyy-MM-dd'),NULL) AS VISAEXPIRYDATE, '' AS NAMEOFORGANIZATION, (select top 1 INDUSTRY_CODE from USR_0_IRBL_INDUSTRY_CODE_DTLS with(nolock) where WI_NAME = '"+processInstanceID+"') as INDUSTRY_CODE, (select top 1 INDUSTRY_SUB_CATEGORY from USR_0_IRBL_INDUSTRY_CODE_DTLS with(nolock) where WI_NAME = '"+processInstanceID+"') as INDUSTRY_SUB_CATEGORY, '' as NUMBEROFYEARSINBUSINESS FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(nolock) WHERE (CIFUPDATEREQREL is null OR CIFUPDATEREQREL != 'Success') AND CIF IS NOT NULL AND CIF != '' AND WI_NAME = '"+processInstanceID+"'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLCIFUpdateLog.iRBLCIFUpdateLogger, false));
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("CIF Update data input: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("CIF Update data output: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			
			if(iTotalrec == 0)
				return "Success";
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
			{

				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

				HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					CheckGridDataMap.put("CIFType",objWorkList.getVal("CIFType"));
					CheckGridDataMap.put("CIFID", objWorkList.getVal("CIFID"));
					CheckGridDataMap.put("CustType", objWorkList.getVal("CustType"));
					CheckGridDataMap.put("NTBEXISTING", objWorkList.getVal("NTBEXISTING"));
					CheckGridDataMap.put("CIFUpdateStatus", objWorkList.getVal("CIFUpdateStatus"));
					CheckGridDataMap.put("PREFERREDADDRLINE1", objWorkList.getVal("PREFERREDADDRLINE1"));
					CheckGridDataMap.put("PREFERREDADDRLINE2", objWorkList.getVal("PREFERREDADDRLINE2"));
					CheckGridDataMap.put("PREFERREDADDRLINE3", objWorkList.getVal("PREFERREDADDRLINE3"));
					CheckGridDataMap.put("PREFERREDADDRLINE4", objWorkList.getVal("PREFERREDADDRLINE4"));
					CheckGridDataMap.put("PREFERREDCITY", objWorkList.getVal("PREFERREDCITY"));
					CheckGridDataMap.put("PREFERREDCOUNTRYCODE", objWorkList.getVal("PREFERREDCOUNTRYCODE"));
					CheckGridDataMap.put("PREFERREDPOBOX", objWorkList.getVal("PREFERREDPOBOX"));
					CheckGridDataMap.put("PREFERREDSTATE", objWorkList.getVal("PREFERREDSTATE"));
					CheckGridDataMap.put("PREFERREDZIPCODE", objWorkList.getVal("PREFERREDZIPCODE"));
					CheckGridDataMap.put("YEARSINBUSINESS", objWorkList.getVal("YEARSINBUSINESS"));
					CheckGridDataMap.put("VISANUMBER", objWorkList.getVal("VISANUMBER"));
					CheckGridDataMap.put("VISAEXPIRYDATE", objWorkList.getVal("VISAEXPIRYDATE"));
					CheckGridDataMap.put("NAMEOFORGANIZATION", objWorkList.getVal("NAMEOFORGANIZATION"));
					CheckGridDataMap.put("INDUSTRY_CODE", objWorkList.getVal("INDUSTRY_CODE"));
					CheckGridDataMap.put("INDUSTRY_SUB_CATEGORY", objWorkList.getVal("INDUSTRY_SUB_CATEGORY"));
					CheckGridDataMap.put("NUMBEROFYEARSINBUSINESS", objWorkList.getVal("NUMBEROFYEARSINBUSINESS"));
		
					for(Map.Entry<String, String> map : CheckGridDataMap.entrySet())
					{
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("CheckGridDataMap map key: " +map.getKey()+" map value :"+map.getValue());
					}
					
					if(!"Success".equalsIgnoreCase(objWorkList.getVal("CIFUpdateStatus").trim()) && !objWorkList.getVal("CIFID").equalsIgnoreCase(""))
					{
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WINAME : "+processInstanceID);

						String integrationStatus=CIFUpdateCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLCIFUpdateLog.iRBLCIFUpdateLogger, false), CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),processInstanceID,ws_name,integrationWaitTime,socket_connection_timeout, socketDetailsMap, CheckGridDataMap);

						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("CIF Update integrationStatus: " +integrationStatus);
						String statuses [] = integrationStatus.split("~");
						if(statuses[0].equalsIgnoreCase("0000"))
						{
							if(!FinalStatus.contains("Failure"))
								FinalStatus = "Success";
						} 
						else
						{
							if (FinalStatus.equalsIgnoreCase(""))
								FinalStatus = "Failure~ For CIF: "+CheckGridDataMap.get("CIFID")+"~ MsgStatus: "+statuses[1]+"~ MsgId: "+statuses[2];
							else 
								FinalStatus = FinalStatus+ "|Failure~ For CIF: "+CheckGridDataMap.get("CIFID")+"~ MsgStatus: "+statuses[1]+"~ MsgId: "+statuses[2];
						}	
					}	
				}
			
			}
			else
			{
				FinalStatus = "Failure";
			}


		}
		catch(Exception e)
		{
			return "Exception";
		}
		return FinalStatus;
	}

	
	public String CIFUpdateCall( String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> CheckGridDataMap)
	{
		try
		{
			String CIFType = CheckGridDataMap.get("CIFType");
			String CIFID = CheckGridDataMap.get("CIFID");
			String CustType = CheckGridDataMap.get("CustType");
			
			String IndustryDetails = "";
			// Industry Details to be passed for all cifs including main cif and all related parties
			if ( (!CheckGridDataMap.get("INDUSTRY_CODE").trim().equalsIgnoreCase("") && !CheckGridDataMap.get("INDUSTRY_CODE").trim().equalsIgnoreCase("null"))
					|| (!CheckGridDataMap.get("INDUSTRY_SUB_CATEGORY").trim().equalsIgnoreCase("") && !CheckGridDataMap.get("INDUSTRY_SUB_CATEGORY").trim().equalsIgnoreCase("null")) )
			{
				IndustryDetails = "<IndustryDet>\n"+
						"<IndustrySegment>"+CheckGridDataMap.get("INDUSTRY_CODE").trim()+"</IndustrySegment>\n"+
						"<IndustrySubSegment>"+CheckGridDataMap.get("INDUSTRY_SUB_CATEGORY").trim()+"</IndustrySubSegment>\n"+
					"</IndustryDet>\n";
			}
			
			// Office Address to be passed for all cifs including main cif and all related parties
			String OfficeAddressForCIFUpdate=AddressDetailsForUpdateCustomer("OFFICE", CheckGridDataMap);
			
			String DocDetails = "";
			if (!CheckGridDataMap.get("VISANUMBER").trim().equalsIgnoreCase("") && !CheckGridDataMap.get("VISANUMBER").trim().equalsIgnoreCase("null") && !CheckGridDataMap.get("VISAEXPIRYDATE").trim().equalsIgnoreCase("") && !CheckGridDataMap.get("VISAEXPIRYDATE").trim().equalsIgnoreCase("null")  && CheckGridDataMap.get("VISAEXPIRYDATE").trim() != null)
			{
				DocDetails = "<DocDet>\n" +
					"<DocType>VISA</DocType>\n" +
					"<DocIsVerified>Y</DocIsVerified>\n"+
					"<DocNo>"+CheckGridDataMap.get("VISANUMBER").trim()+"</DocNo>\n" +
					"<DocExpDate>"+CheckGridDataMap.get("VISAEXPIRYDATE").trim()+"</DocExpDate>\n" +
				"</DocDet>\n";
			}
			
			String ProductProccessor = "";
			String RetailAddnlDetails = "";
			if("R".equalsIgnoreCase(CustType))
			{
				RetailAddnlDetails = "<RtlAddnlDet>\n";
				RetailAddnlDetails = RetailAddnlDetails + "<EmploymentType>Self employed</EmploymentType>\n";
				
				if (!CheckGridDataMap.get("NAMEOFORGANIZATION").trim().equalsIgnoreCase("") && !CheckGridDataMap.get("NAMEOFORGANIZATION").trim().equalsIgnoreCase("null")) 
					RetailAddnlDetails = RetailAddnlDetails + "<EmployerNm>"+CheckGridDataMap.get("NAMEOFORGANIZATION").trim()+"</EmployerNm>\n";
				
				RetailAddnlDetails = RetailAddnlDetails+"</RtlAddnlDet>\n";
				
				ProductProccessor = "<ProductProccessor>RLS</ProductProccessor>\n"; // POLP-9866
				
			}
			
			if("C".equalsIgnoreCase(CustType))
			{
				ProductProccessor = "<ProductProccessor>FINACLECORE,RLS</ProductProccessor>\n"; // POLP-9866
			}
			
			// below block is application only for Main CIF
			/*if(CIFType.equalsIgnoreCase("Primary"))
			{
				if (!CheckGridDataMap.get("NUMBEROFYEARSINBUSINESS").trim().equalsIgnoreCase("") && !CheckGridDataMap.get("NUMBEROFYEARSINBUSINESS").trim().equalsIgnoreCase("null")) 
					RetailAddnlDetails = "<RtlAddnlDet>\n" + "<BusinessDuration>"+CheckGridDataMap.get("NAMEOFORGANIZATION").trim()+"</BusinessDuration>\n"+"</RtlAddnlDet>\n";
			}*/
			
			java.util.Date d1 = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			String DateExtra2 = sdf1.format(d1)+"+04:00";
			StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>\n" +
			"<EE_EAI_HEADER>\n" +
				"<MsgFormat>CUSTOMER_UPDATE_REQ</MsgFormat>\n" +
				"<MsgVersion>001</MsgVersion>\n" +
				"<RequestorChannelId>BPM</RequestorChannelId>\n" +
				"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
				"<RequestorLanguage>E</RequestorLanguage>\n" +
				"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
				"<ReturnCode>911</ReturnCode>\n" +
				"<ReturnDesc>Issuer Timed Out</ReturnDesc>\n" +
				"<MessageId>cifupdate001</MessageId>\n" +
				"<Extra1>REQ||SHELL.dfgJOHN</Extra1>\n" +
				"<Extra2>"+DateExtra2+"</Extra2>\n" +
			"</EE_EAI_HEADER>\n" +
			"<CustomerDetailsUpdateReq>\n" +
				"<BankId>RAK</BankId>\n" +
				"<CIFId>"+CIFID+"</CIFId>\n" +
				"<RetCorpFlag>"+CustType+"</RetCorpFlag>\n" +
				ProductProccessor +
				"<CustClassification>B</CustClassification>\n" + // passing hardcoded as B for all CIFs POLP - 9866
				"<ActionRequired>U</ActionRequired>\n" +
				IndustryDetails+
				OfficeAddressForCIFUpdate+"\n"+
				DocDetails+
				RetailAddnlDetails+
			"</CustomerDetailsUpdateReq>\n" +
			"</EE_EAI_MESSAGE>");

			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("CIF Update Integration input XML: "+sInputXML.toString());

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
				
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
			if (return_code.equalsIgnoreCase("0000")) {
				String MainGridColNames = "";
				String MainGridColValues = "";
				String sTableName = "";
				String sWhere = "";
				if(CIFType.equalsIgnoreCase("Primary"))
				{
					MainGridColNames = "CIFUPDATEREQMAIN";
					MainGridColValues = "'Success'";
					sTableName = "RB_iRBL_EXTTABLE";
					sWhere = "WINAME = '"+processInstanceID+"'";
				}
				else 
				{
					MainGridColNames = "CIFUPDATEREQREL";
					MainGridColValues = "'Success'";
					sTableName = "USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS";
					sWhere = "WI_NAME = '"+processInstanceID+"' and CIF = '"+CIFID+"'";
				}
				String status= UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,sTableName,sWhere);
			    iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("UpdateGridTableMWResponse CheckGridTable status : " +status);
			}
			
		    iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Return Desc: "+return_desc);
		    
		    return (return_code + "~" + return_desc + "~"+ MsgId +"~End");
		}
		catch(Exception e)
		{
			return "Exception in CIF Update";
		}
	}
		

	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
	{

		String socketServerIP;
		int socketServerPort;
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String outputResponse = null;
		String inputRequest = null;
		String inputMessageID = null;



		try
		{

			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("userName "+ username);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Dout " + dout);
    			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Din " + din);

    			outputResponse = "";



    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
    			}
    			byte[] readBuffer = new byte[500];
    			int num = din.read(readBuffer);
    			if (num > 0)
    			{

    				byte[] arrayBytes = new byte[num];
    				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
    				outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
    				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))

    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId,
    							processInstanceID,outputResponse,integrationWaitTime );




    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
			return "";
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
					out=null;
				}
				if(socketInputStream != null)
				{

					socketInputStream.close();
					socketInputStream=null;
				}
				if(dout != null)
				{

					dout.close();
					dout=null;
				}
				if(din != null)
				{

					din.close();
					din=null;
				}
				if(socket != null)
				{
					if(!socket.isClosed())
						socket.close();
					socket=null;
				}

			}

			catch(Exception e)
			{
				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}

	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_iRBL_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();

	}

	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String
			sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_iRBL_XMLLOG_HISTORY with (nolock) where " +
					"MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	public String UpdateGridTableMWResponse(String columnNames, String columnValues, String TransactionTable, String sWhereClause) throws IOException, Exception
	{	
		String RetStatus="";
		String QueryString="";
		String sInputXML="";
		String sOutputXML="";
			//Updating records
			sInputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLCIFUpdateLog.iRBLCIFUpdateLogger, false), TransactionTable, columnNames, columnValues, sWhereClause);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Input XML for apUpdateInput from "+TransactionTable+" Table "+sInputXML);

			sOutputXML=CommonMethods.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Output XML for apUpdateInput Table "+sOutputXML);

			XMLParser sXMLParserChild= new XMLParser(sOutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("StrMainCode: "+StrMainCode);

		    if (StrMainCode.equals("0"))
			{
		    	iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Successful in apUpdateInput the record in : "+TransactionTable);
		    	RetStatus="Success in apUpdateInput the record";
			}
		    else
		    {
		    	iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Error in Executing apUpdateInput sOutputXML : "+TransactionTable);
		    	RetStatus="Error in Executing apUpdateInput";
		    }
			
		return RetStatus;
	}
	
	public String AddressDetailsForUpdateCustomer(String AddressType, HashMap<String, String> CheckGridDataMap)
	{
		Date date1 = new Date();
		DateFormat logDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
		String AddressXml = "<AddrDet><AddressType></AddressType><EffectiveFrom>"+logDateFormat1.format(date1)+"</EffectiveFrom><EffectiveTo>2099-12-31</EffectiveTo><HoldMailFlag>N</HoldMailFlag><ReturnFlag>N</ReturnFlag><AddrPrefFlag>Y</AddrPrefFlag><AddrLine1></AddrLine1><AddrLine2></AddrLine2><AddrLine3></AddrLine3><AddrLine4></AddrLine4><POBox></POBox><State></State><City></City><CountryCode></CountryCode></AddrDet>";
		if (CheckGridDataMap.get("PREFERREDADDRLINE2")==null || CheckGridDataMap.get("PREFERREDADDRLINE2").equals(""))
			CheckGridDataMap.put("PREFERREDADDRLINE2",".");
		if(CheckGridDataMap.get("PREFERREDADDRLINE1")==null || CheckGridDataMap.get("PREFERREDADDRLINE1").equals("") || CheckGridDataMap.get("PREFERREDCITY")==null || CheckGridDataMap.get("PREFERREDCITY").equals(""))
			return "";
		else if (CheckGridDataMap.get("PREFERREDCOUNTRYCODE")==null || CheckGridDataMap.get("PREFERREDCOUNTRYCODE").equals("") || CheckGridDataMap.get("PREFERREDCOUNTRYCODE").equals("--Select--"))
			return "";

		StringBuffer addressB = new StringBuffer(AddressXml);

		addressB = addressB.insert(addressB.indexOf("<AddressType>")+"<AddressType>".length(),AddressType );

		addressB = addressB.insert(addressB.indexOf("<AddrLine1>")+"<AddrLine1>".length(),CheckGridDataMap.get("PREFERREDADDRLINE1") );

		addressB = addressB.insert(addressB.indexOf("<AddrLine2>")+"<AddrLine2>".length(),CheckGridDataMap.get("PREFERREDADDRLINE2") );

		if(CheckGridDataMap.get("PREFERREDADDRLINE3")!=null && !CheckGridDataMap.get("PREFERREDADDRLINE3").equals("") )
			addressB = addressB.insert(addressB.indexOf("<AddrLine3>")+"<AddrLine3>".length(),CheckGridDataMap.get("PREFERREDADDRLINE3") );
		else
			addressB = addressB.delete(addressB.indexOf("<AddrLine3>"), addressB.indexOf("</AddrLine3>")+"</AddrLine3>".length());

		if(CheckGridDataMap.get("PREFERREDADDRLINE4")!=null && !CheckGridDataMap.get("PREFERREDADDRLINE4").equals("") )
			addressB = addressB.insert(addressB.indexOf("<AddrLine4>")+"<AddrLine4>".length(),CheckGridDataMap.get("PREFERREDADDRLINE4") );
		else
			addressB = addressB.delete(addressB.indexOf("<AddrLine4>"), addressB.indexOf("</AddrLine4>")+"</AddrLine4>".length());
		
		if(CheckGridDataMap.get("PREFERREDPOBOX")!=null && !CheckGridDataMap.get("PREFERREDPOBOX").equals("") )
			addressB = addressB.insert(addressB.indexOf("<POBox>")+"<POBox>".length(),CheckGridDataMap.get("PREFERREDPOBOX") );
		else
			addressB = addressB.delete(addressB.indexOf("<POBox>"), addressB.indexOf("</POBox>")+"</POBox>".length());
		
		if(CheckGridDataMap.get("PREFERREDSTATE")!=null && !CheckGridDataMap.get("PREFERREDSTATE").equals("") )
			addressB = addressB.insert(addressB.indexOf("<State>")+"<State>".length(),CheckGridDataMap.get("PREFERREDSTATE") );
		else
			addressB = addressB.delete(addressB.indexOf("<State>"), addressB.indexOf("</State>")+"</State>".length());

		addressB = addressB.insert(addressB.indexOf("<City>")+"<City>".length(),CheckGridDataMap.get("PREFERREDCITY") );

		addressB = addressB.insert(addressB.indexOf("<CountryCode>")+"<CountryCode>".length(),CheckGridDataMap.get("PREFERREDCOUNTRYCODE") );
	
		AddressXml =addressB.toString();
		return AddressXml;
	}
	
}





