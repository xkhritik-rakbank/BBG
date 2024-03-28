package com.newgen.iforms.user;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class DBO_FetchAccBal extends DBOCommon{
	
	public static String XMLLOG_HISTORY = "NG_DBO_XMLLOG_HISTORY";
	
	
	public String onevent(IFormReference iformObj, String control, String StringData) throws IOException {
		String wiName = getWorkitemName(iformObj);
		String WSNAME = getActivityName(iformObj);
		String returnValue = "";
		String MQ_response = "";
		String cabinetName = getCabinetName(iformObj);
		String decisionValue = "";
		String attributesTag = "";
		String socketServerIP = "";
		String socketServerPort = "";
		
		

		MQ_response = MQ_connection_response(iformObj, control, StringData);

		// return MQ_response;StatusCode
		if (MQ_response.indexOf("<MessageStatus>") != -1)
			returnValue = MQ_response.substring(
					MQ_response.indexOf("<MessageStatus>") + "</MessageStatus>".length() - 1,
					MQ_response.indexOf("</MessageStatus>"));

		if (MQ_response.contains("INVALID SESSION"))
			returnValue = "INVALID SESSION";

		if ("Success".equalsIgnoreCase(returnValue))
			 returnValue = MQ_response;
			returnValue = "FETCH ACCOUNT BALANCE CALL SUCCESS";
		// save response data start
		XMLParser xmlParserSocketDetails = new XMLParser(MQ_response);
		DBO.mLogger.debug("WINAME: "+wiName+", WSNAME: "+WSNAME+", ControlName: "+control+" xmlParserSocketDetails : " + xmlParserSocketDetails);
		String returnCode = xmlParserSocketDetails.getValueOf("ReturnCode");
		
		try{
		if("0000".equalsIgnoreCase(returnCode)){
			
			DBO.mLogger.debug("WINAME: "+wiName+", WSNAME: "+WSNAME+", ControlName: "+control+"FETCH ACCOUNT BALANCE Status :  FETCH ACCOUNT BALANCE SUCCESS");
			//to save the Account Bal on Form
			String AccountDetails = xmlParserSocketDetails.getValueOf("AccountDetails");
			DBO.mLogger.debug("AccountDetails : " + AccountDetails);
			String AcctBal[] = getTagValues(AccountDetails, "AcctBal").split("`");
			DBO.mLogger.debug("length of AcctBal" + AcctBal.length);
			String BalType = "";
			for (int i = 0; i < AcctBal.length; i++) {
				BalType = getTagValues(AcctBal[i], "BalType");
				DBO.mLogger.debug("BalType : " + BalType);
				if(BalType.equalsIgnoreCase("EFFAVL")){
					String Amount = getTagValues(AcctBal[i], "Amount");
					DBO.mLogger.debug("Amount : " + Amount);
					iformObj.setValue("AccountBal", Amount);
				}
			}
			return "FETCH ACCOUNT BALANCE SUCCESS";
		
		}else{
			DBO.mLogger.debug("WINAME: "+wiName+", WSNAME: "+WSNAME+", ControlName: "+control+"FETCH ACCOUNT BALANCE Status : Some Error Occured at Server End");
			return "Some Error Occured at Server End";
		}
		}catch(Exception e){
			DBO.mLogger.info("WINAME: "+wiName+", WSNAME: "+WSNAME+", ControlName: "+control+"Exception :::::  "  + e.getMessage());
			return "Some Error Occured at Server End";
		}
	}
	public String MQ_connection_response(IFormReference iformObj, String control, String Data) {
		DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
				+ ", Inside MQ_connection_response function for DBO FETCH ACCOUNT BALANCE Call");
		final IFormReference iFormOBJECT;
		final WDGeneralData wdgeneralObj;
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String mqOutputResponse = null;
		String mqOutputResponse1 = null;
		String mqInputRequest = null;
		String cabinetName = getCabinetName(iformObj);
		String wi_name = getWorkitemName(iformObj);
		String ws_name = getActivityName(iformObj);
		String userName = getUserName(iformObj);
		String socketServerIP;
		int socketServerPort;
		wdgeneralObj = iformObj.getObjGeneralData();
		String sessionID = wdgeneralObj.getM_strDMSSessionId();
		String CallName = "";
		StringBuilder finalXml = new StringBuilder();
		String ProspectID = (String) iformObj.getValue("ProspectID");
		String AccountNumber = (String) iformObj.getValue("AccountNumber");
		//SimpleDateFormat inputdateFormat = new SimpleDateFormat("dd/MM/yyyy");
		//SimpleDateFormat outputdateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String fetchAccBal_xml_dbo = "";
		fetchAccBal_xml_dbo += "<EE_EAI_MESSAGE>";
		fetchAccBal_xml_dbo += "\n\t<EE_EAI_HEADER>";
		fetchAccBal_xml_dbo += "\n\t\t<MsgFormat>ACCOUNT_DETAILS</MsgFormat>";
		fetchAccBal_xml_dbo += "\n\t\t<MsgVersion>0001</MsgVersion>";
		fetchAccBal_xml_dbo += "\n\t\t<RequestorChannelId>BPM</RequestorChannelId>";
		fetchAccBal_xml_dbo += "\n\t\t<RequestorUserId>BPMUSER</RequestorUserId>";
		fetchAccBal_xml_dbo += "\n\t\t<RequestorLanguage>E</RequestorLanguage>";
		fetchAccBal_xml_dbo += "\n\t\t<RequestorSecurityInfo>secure</RequestorSecurityInfo>";
		fetchAccBal_xml_dbo += "\n\t\t<ReturnCode>0000</ReturnCode>";
		fetchAccBal_xml_dbo += "\n\t\t<ReturnDesc>success</ReturnDesc>";
		fetchAccBal_xml_dbo += "\n\t\t<MessageId>BPM30101231701333196802674824</MessageId>";
		fetchAccBal_xml_dbo += "\n\t\t<Extra1>REQ||SHELL.JOHN</Extra1>";
		fetchAccBal_xml_dbo += "\n\t\t<Extra2>2023-11-30T12:33:16.033+04:00</Extra2>";
		fetchAccBal_xml_dbo += "\n\t</EE_EAI_HEADER>";
		fetchAccBal_xml_dbo += "\n\t<AccountDetails>";
		fetchAccBal_xml_dbo += "\n\t\t<BankId>RAK</BankId>";
		fetchAccBal_xml_dbo += "\n\t\t<CustId></CustId>";
		fetchAccBal_xml_dbo += "\n\t\t<Acid>"+AccountNumber+"</Acid>";
		fetchAccBal_xml_dbo += "\n\t\t<BranchId></BranchId>";
		fetchAccBal_xml_dbo += "\n\t\t<AcType></AcType>";
		fetchAccBal_xml_dbo += "\n\t\t<BackendName></BackendName>";
		fetchAccBal_xml_dbo += "\n\t</AccountDetails>";
		fetchAccBal_xml_dbo += "\n</EE_EAI_MESSAGE>";
		
			DBO.mLogger.debug("WINAME: "+wi_name+", WSNAME: "+ws_name+", ControlName: "+control);
			CallName = "DBO_FETCH_ACC_BAL";

			finalXml = new StringBuilder(fetchAccBal_xml_dbo);
			DBO.mLogger.debug("DBO_FETCH_ACC_BAL CALL - fetchAccBal_xml_dbo : " + fetchAccBal_xml_dbo);
			DBO.mLogger.debug("DBO_FETCH_ACC_BAL CALL - finalXml : " + finalXml );
			

			mqInputRequest = getMQInputXML(sessionID, cabinetName, wi_name, ws_name, userName, finalXml);
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", mqInputRequest for DBO_FETCH_ACC_BAL call" + mqInputRequest);
			DBO.mLogger.debug("DBO_FETCH_ACC_BAL CALL - mqInputRequest : " + mqInputRequest );
			
		try {

			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", userName " + userName);
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", sessionID " + sessionID);

			String sMQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DBO' and CallingSource = 'Form'";
			List<List<String>> outputMQXML = iformObj.getDataFromDB(sMQuery);
			
			if (!outputMQXML.isEmpty()) {
				
				socketServerIP = outputMQXML.get(0).get(0);
				DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
						+ getActivityName(iformObj) + ", socketServerIP " + socketServerIP);
				socketServerPort = Integer.parseInt(outputMQXML.get(0).get(1));
				DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
						+ getActivityName(iformObj) + ", socketServerPort " + socketServerPort);
				if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", Inside serverIP Port " + socketServerPort
							+ "-socketServerIP-" + socketServerIP);
					socket = new Socket(socketServerIP, socketServerPort);
					// new Code added by Deepak to set connection timeout
					int connection_timeout = 60;
					try {
						connection_timeout = 70;
					} catch (Exception e) {
						connection_timeout = 60;
					}

					socket.setSoTimeout(connection_timeout * 1000);
					out = socket.getOutputStream();
					socketInputStream = socket.getInputStream();
					dout = new DataOutputStream(out);
					din = new DataInputStream(socketInputStream);
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", dout " + dout);
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", din " + din);
					mqOutputResponse = "";

					if (mqInputRequest != null && mqInputRequest.length() > 0) {
						int outPut_len = mqInputRequest.getBytes("UTF-16LE").length;
						DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
								+ getActivityName(iformObj) + ", Final XML output len: " + outPut_len + "");
						mqInputRequest = outPut_len + "##8##;" + mqInputRequest;
						DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
								+ getActivityName(iformObj) + ", MqInputRequest" + "Input Request Bytes : "
								+ mqInputRequest.getBytes("UTF-16LE"));
						dout.write(mqInputRequest.getBytes("UTF-16LE"));
						dout.flush();
					}

					byte[] readBuffer = new byte[500];
					int num = din.read(readBuffer);
					if (num > 0) {

						byte[] arrayBytes = new byte[num];
						System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
						mqOutputResponse = mqOutputResponse + new String(arrayBytes, "UTF-16LE");
						DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
								+ getActivityName(iformObj) + ", mqOutputResponse/message ID :  " + mqOutputResponse);

						mqOutputResponse = getOutWtthMessageID("", iformObj, mqOutputResponse);

						if (mqOutputResponse.contains("&lt;")) {
							mqOutputResponse = mqOutputResponse.replaceAll("&lt;", "<");
							mqOutputResponse = mqOutputResponse.replaceAll("&gt;", ">");

						}
					}
					socket.close();
					return mqOutputResponse;

				} else {
					DBO.mLogger
							.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
									+ ", SocketServerIp and SocketServerPort is not maintained " + "");
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", SocketServerIp is not maintained " + socketServerIP);
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ",  SocketServerPort is not maintained " + socketServerPort);
					return "MQ details not maintained";
				}
			} else {
				DBO.mLogger
						.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
								+ ", SOcket details are not maintained in NG_BPM_MQ_TABLE table" + "");
				return "MQ details not maintained";
			}

		} catch (Exception e) {
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", Exception Occured Mq_connection_CC" + e.getStackTrace());
			return "";
		} finally {
			try {
				if (out != null) {

					out.close();
					out = null;
				}
				if (socketInputStream != null) {

					socketInputStream.close();
					socketInputStream = null;
				}
				if (dout != null) {

					dout.close();
					dout = null;
				}
				if (din != null) {

					din.close();
					din = null;
				}
				if (socket != null) {
					if (!socket.isClosed()) {
						socket.close();
					}
					socket = null;
				}
			} catch (Exception e) {

				DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
						+ getActivityName(iformObj) + ", Final Exception Occured Mq_connection_CC" + e.getStackTrace());

			}
		}
	}
	
	public String getOutWtthMessageID(String callName, IFormReference iformObj, String message_ID) {
		String outputxml = "";
		try {
			DBO.mLogger.debug("getOutWtthMessageID - callName :" + callName);

			String wi_name = getWorkitemName(iformObj);
			String str_query = "select OUTPUT_XML from " + XMLLOG_HISTORY + " with (nolock) where MESSAGE_ID ='"
					+ message_ID + "' and WI_NAME = '" + wi_name + "'";
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", inside getOutWtthMessageID str_query: " + str_query);
			List<List<String>> result = iformObj.getDataFromDB(str_query);
			// below code added by nikhil 18/10 for Connection timeout
			String Integration_timeOut = "100";
			int Loop_wait_count = 10;
			try {
				Loop_wait_count = Integer.parseInt(Integration_timeOut);
			} catch (Exception ex) {
				Loop_wait_count = 10;
			}

			for (int Loop_count = 0; Loop_count < Loop_wait_count; Loop_count++) {
				if (result.size() > 0) {
					outputxml = result.get(0).get(0);
					break;
				} else {
					Thread.sleep(1000);
					result = iformObj.getDataFromDB(str_query);
				}
			}

			if ("".equalsIgnoreCase(outputxml)) {
				outputxml = "Error";
			}
			DBO.mLogger.debug("This is output xml from DB");
			String outputxmlMasked = outputxml;
			
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", getOutWtthMessageID" + outputxmlMasked);
		} catch (Exception e) {
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", Exception BTurred in getOutWtthMessageID" + e.getMessage());
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", Exception BTurred in getOutWtthMessageID" + e.getStackTrace());
			outputxml = "Error";
		}
		return outputxml;
	}
	
	private static String getMQInputXML(String sessionID, String cabinetName, String wi_name, String ws_name,
			String userName, StringBuilder final_xml) {
		DBO.mLogger.debug("inside getMQInputXML function");
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>" + XMLLOG_HISTORY + "</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(final_xml);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		return strBuff.toString();
	}
	private static String getTagValues(String sXML, String sTagName) {
		String sTagValues = "";
		String sStartTag = "<" + sTagName + ">";
		String sEndTag = "</" + sTagName + ">";
		String tempXML = sXML;
		try {

			for (int i = 0; i < sXML.split(sEndTag).length; i++) {
				if (tempXML.indexOf(sStartTag) != -1) {
					sTagValues += tempXML.substring(tempXML.indexOf(sStartTag) + sStartTag.length(),
							tempXML.indexOf(sEndTag));
					tempXML = tempXML.substring(tempXML.indexOf(sEndTag) + sEndTag.length(), tempXML.length());
				}
				if (tempXML.indexOf(sStartTag) != -1) {
					sTagValues += "`";
					// System.out.println("sTagValues"+sTagValues);
				}
				// System.out.println("sTagValues"+sTagValues);
			}
			// System.out.println(" Final sTagValues"+sTagValues);
		} catch (Exception e) {
		}
		return sTagValues;
	}
	
}
