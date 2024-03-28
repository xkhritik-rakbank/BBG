/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM - iRBL
Application				: RAK iRBL Utility
Module					: CIF Verification
File Name				: Integration.java
Author 					: Angad Shah
Date (DD/MM/YYYY)		: 15/05/2021

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.CIFVerification;

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

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class iRBLCIFVerificationIntegration
{

	
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		String FinalStatus = "";
		try
		{
			iRBLCIFVerificationLog.setLogger();
			
			String DBQuery = "SELECT 'Primary' AS CIFType, CIF_NUMBER AS CIFID, 'C' AS CustType, CIFVerificationStatus FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE NTBEXISTING = 'NTB' AND CIF_NUMBER IS NOT NULL AND CIF_NUMBER != '' AND (CIFVerificationStatus IS NULL OR CIFVerificationStatus != 'Success')  AND WINAME = '"+processInstanceID+"'"+
					"UNION ALL "+
					"SELECT 'Related' AS CIFType, CIF AS CIFID, COMPANYFLAG AS CustType, CIFVerificationStatus FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(nolock) WHERE EXISTINGNTB = 'NTB' AND CIF IS NOT NULL AND CIF != '' AND (CIFVerificationStatus IS NULL OR CIFVerificationStatus != 'Success')  AND WI_NAME = '"+processInstanceID+"'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLCIFVerificationLog.iRBLCIFVerificationLogger, false));
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("CIF Verification data input: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("CIF Verification data output: "+ extTabDataOPXML);

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

				HashMap<String, String> ExtTabDataMap = new HashMap<String, String>();
				HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

				boolean MainCIF_Flag = true;
											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					CheckGridDataMap.put("CIFType",objWorkList.getVal("CIFType"));
					CheckGridDataMap.put("CIFID", objWorkList.getVal("CIFID"));
					CheckGridDataMap.put("CustType", objWorkList.getVal("CustType"));
					CheckGridDataMap.put("CIFVerificationStatus", objWorkList.getVal("CIFVerificationStatus"));
		
					for(Map.Entry<String, String> map : CheckGridDataMap.entrySet())
					{
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("CheckGridDataMap map key: " +map.getKey()+" map value :"+map.getValue());
					}
					
					if((objWorkList.getVal("CIFVerificationStatus").equalsIgnoreCase("") || objWorkList.getVal("CIFVerificationStatus").equalsIgnoreCase("Failure")) && !objWorkList.getVal("CIFID").equalsIgnoreCase(""))
					{
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WINAME : "+processInstanceID);

						String integrationStatus=CIFVerficationCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLCIFVerificationLog.iRBLCIFVerificationLogger, false), CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),processInstanceID,ws_name,integrationWaitTime,socket_connection_timeout, socketDetailsMap, ExtTabDataMap, CheckGridDataMap);

						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("CIFVerification integrationStatus: " +integrationStatus);
						String statuses [] = integrationStatus.split("~");
						if(statuses[0].equalsIgnoreCase("0000") || statuses[0].equalsIgnoreCase("MSGEXC50107"))
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

	
	public String CIFVerficationCall( String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> CheckGridDataMap)
	{
		try
		{
			String CIFType = CheckGridDataMap.get("CIFType");
			String CIFID = CheckGridDataMap.get("CIFID");
			String CustType = CheckGridDataMap.get("CustType");
			String RetCorpFlag = "";
			if(CIFType.equalsIgnoreCase("Primary"))
				RetCorpFlag = "C";
			else
			{
				if("Y".equalsIgnoreCase(CustType))
					RetCorpFlag = "C";
				else 
					RetCorpFlag = "R";
			}	
			java.util.Date d1 = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			String DateExtra2 = sdf1.format(d1)+"+04:00";
			StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>\n"+
					"<EE_EAI_HEADER>\n"+
						"<MsgFormat>CUSTOMER_UPDATE_REQ</MsgFormat>\n"+
						"<MsgVersion>0001</MsgVersion>\n"+
						"<RequestorChannelId>BPM</RequestorChannelId>\n"+
						"<RequestorUserId>RAKUSER</RequestorUserId>\n"+
						"<RequestorLanguage>E</RequestorLanguage>\n"+
						"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n"+
						"<ReturnCode>0000</ReturnCode>\n"+
						"<ReturnDesc>REQ</ReturnDesc>\n"+
						"<MessageId>CIFVER123456789</MessageId>\n"+
						"<Extra1>REQ||BPM.123</Extra1>\n"+
						"<Extra2>"+DateExtra2+"</Extra2>\n"+
					"</EE_EAI_HEADER>\n"+
					"<CustomerDetailsUpdateReq>\n"+
						"<BankId>RAK</BankId>\n"+
						"<CIFId>"+CIFID+"</CIFId>\n"+
						"<RetCorpFlag>"+RetCorpFlag+"</RetCorpFlag>\n"+
						"<ActionRequired>V</ActionRequired>\n"+
							"<VerifyCIF><Decision>Approve</Decision>\n"+
							"<Reason>Approve it</Reason>\n"+
						"</VerifyCIF>\n"+
					"</CustomerDetailsUpdateReq>\n"+
					"</EE_EAI_MESSAGE>\n");

			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Integration input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
				
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
			if (return_code.equalsIgnoreCase("0000") || return_code.equalsIgnoreCase("MSGEXC50107")) {
				String MainGridColNames = "CIFVerificationStatus";
				String MainGridColValues = "'Success'";
				String sTableName = "";
				String sWhere = "";
				if(CIFType.equalsIgnoreCase("Primary"))
				{
					sTableName = "RB_iRBL_EXTTABLE";
					sWhere = "WINAME = '"+processInstanceID+"'";
				}
				else 
				{
					sTableName = "USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS";
					sWhere = "WI_NAME = '"+processInstanceID+"' and CIF = '"+CIFID+"'";
				}
				String status= UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,sTableName,sWhere);
			    iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("UpdateGridTableMWResponse CheckGridTable status : " +status);
			}
			
		    iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Return Desc: "+return_desc);
		    
		    return (return_code + "~" + return_desc + "~"+ MsgId +"~End");
		}
		catch(Exception e)
		{
			return "Exception in CIF Verification";
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

			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("userName "+ username);
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Dout " + dout);
    			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Din " + din);

    			outputResponse = "";



    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("OutputResponse: "+outputResponse);

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

				//iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
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
		iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("GetRequestXML: "+ strBuff.toString());
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
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
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
			sInputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLCIFVerificationLog.iRBLCIFVerificationLogger, false), TransactionTable, columnNames, columnValues, sWhereClause);
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Input XML for apUpdateInput from "+TransactionTable+" Table "+sInputXML);

			sOutputXML=CommonMethods.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Output XML for apUpdateInput Table "+sOutputXML);

			XMLParser sXMLParserChild= new XMLParser(sOutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("StrMainCode: "+StrMainCode);

		    if (StrMainCode.equals("0"))
			{
		    	iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Successful in apUpdateInput the record in : "+TransactionTable);
		    	RetStatus="Success in apUpdateInput the record";
			}
		    else
		    {
		    	iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Error in Executing apUpdateInput sOutputXML : "+TransactionTable);
		    	RetStatus="Error in Executing apUpdateInput";
		    }
			
		return RetStatus;
	}
}





