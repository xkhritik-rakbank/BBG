package com.newgen.DBO.Notify_DEH;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.DBO.AWB_Delivery.DBO_AWB_Logs;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;

public class DBO_Notify_DEH implements Runnable {

	static Map<String, String> NotifyAppConfigParamMap = new HashMap<String, String>();
	
//	private static NGEjbClient ngEjbClientAWBGen;

	int socketConnectionTimeout = 0;
	int integrationWaitTime = 0;
	int sleepIntervalInMin = 0;
	public static int waitLoop=50;
	private static String  sessionID = "";
	private static String cabinetName = "";
	private static String jtsIP = "";
	private static String jtsPort = "";
	final private static String ws_name = "Sys_Notify_DEH";
	static HashMap<String, String> commDetailsMap = new HashMap<>();
	
	public void run() {

		String queueID = "";

		try {

			DBO_NotifyDEHLogs.setLogger();
			
//			ngEjbClientAWBGen = NGEjbClient.getSharedInstance();

			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("configReadStatus " + configReadStatus);
			if (configReadStatus != 0) {
				DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Could not Read Config Properties [DBONotifyAPP]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("JTSPORT: " + jtsPort);

			queueID = NotifyAppConfigParamMap.get("queueID");
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout = Integer.parseInt(NotifyAppConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("SocketConnectionTimeOut: " + socketConnectionTimeout);

			integrationWaitTime = Integer.parseInt(NotifyAppConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("IntegrationWaitTime: " + integrationWaitTime);

			sleepIntervalInMin = Integer.parseInt(NotifyAppConfigParamMap.get("SleepIntervalInMin"));
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("SleepIntervalInMin: " + sleepIntervalInMin);

			sessionID = CommonConnection.getSessionID(DBO_NotifyDEHLogs.DBO_NotifyLogger, false);

			if (sessionID.trim().equalsIgnoreCase("")) {
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Could Not Connect to Server!");
			} else {
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = getSocketConnectionDetails(cabinetName, jtsIP,jtsPort, sessionID); // toDo
				getCommunicationDetails();
				
				while (true) 
				{
					DBO_NotifyDEHLogs.setLogger();
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("DBO Notify TO DEH ...123.");
					
					NotifyDEHForTable(socketDetailsMap);
					processCasesAtNotifyStep(queueID,socketDetailsMap);
					System.out.println("No More workitems to Process, Sleeping!");
					
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Exception Occurred in DBONotifyAPP : " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Exception Occurred in DBONotifyAPP : " + result);
		}		
				
		
	}
	
	private void NotifyDEHForTable(HashMap<String, String> socketDetailsMap)
	{
		try
		{
			sessionID  = CommonConnection.getSessionID(DBO_NotifyDEHLogs.DBO_NotifyLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Could Not Get Session ID "+sessionID);
				return;
			}
			String DBQuery = "SELECT * FROM USR_0_DBO_DEH_Notification WITH(nolock) WHERE NotificationStatus != 'Done' OR NotificationStatus IS NULL ";
			
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,
					CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(DBO_NotifyDEHLogs.DBO_NotifyLogger, false));
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
	
			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
	
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
	
			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
			{
				NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
	
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) 
				{
					String processInstanceID = objWorkList.getVal("WINAME");
					String NotifyDEHEvent = objWorkList.getVal("NotifyDEHEvent");
					String StatusCode = objWorkList.getVal("StatusCode");
					String DeliveryStatus = objWorkList.getVal("DeliveryStatus");
					String ProspectID = objWorkList.getVal("ProspectId");
					String awbNo = objWorkList.getVal("AWBNo"); 
					String statusDescription = objWorkList.getVal("DeliveryStatus");
					
					if("WI_STATUS".equals(NotifyDEHEvent))
					{
						
						if(DeliveryStatus != null && StatusCode != null)
						{
							
							String notify_xml_dbo = "";
							notify_xml_dbo += "<EE_EAI_MESSAGE>";
							notify_xml_dbo += "\n\t<EE_EAI_HEADER>";
							notify_xml_dbo += "\n\t\t<MsgFormat>NOTIFY_BBG_APPLICATION</MsgFormat>";
							notify_xml_dbo += "\n\t\t<MsgVersion>0001</MsgVersion>";
							notify_xml_dbo += "\n\t\t<RequestorChannelId>BPM</RequestorChannelId>";
							notify_xml_dbo += "\n\t\t<RequestorUserId>RAKUSER</RequestorUserId>";
							notify_xml_dbo += "\n\t\t<RequestorLanguage>E</RequestorLanguage>";
							notify_xml_dbo += "\n\t\t<RequestorSecurityInfo>secure</RequestorSecurityInfo>";
							notify_xml_dbo += "\n\t\t<ReturnCode>911</ReturnCode>";
							notify_xml_dbo += "\n\t\t<ReturnDesc>Issuer Timed Out</ReturnDesc>";
							notify_xml_dbo += "\n\t\t<MessageId>123123453</MessageId>";
							notify_xml_dbo += "\n\t\t<Extra1>REQ||SHELL.JOHN</Extra1>";
							notify_xml_dbo += "\n\t\t<Extra2>yyyy-MM-dd HH:mm:ssThh:mm:ss.mmm+hh:mm</Extra2>";
							notify_xml_dbo += "\n\t</EE_EAI_HEADER>";
							notify_xml_dbo += "\n\t<NotifyBBGApplicationStatusRequest>";
							notify_xml_dbo += "\n\t\t<BankId>RAK</BankId>";
							notify_xml_dbo += "\n\t\t<Process_Name>DBO</Process_Name>";
							notify_xml_dbo += "\n\t\t<ProspectId>" + ProspectID + "</ProspectId>";
							notify_xml_dbo += "\n\t\t<ChannelId>EBC.WBA</ChannelId>";
							notify_xml_dbo += "\n\t\t<WorkitemNumber>" + processInstanceID + "</WorkitemNumber>";
							notify_xml_dbo += "\n\t\t<Event>" + NotifyDEHEvent + "</Event>";
							notify_xml_dbo += "#body#"; // wi_status
							notify_xml_dbo += "\n\t</NotifyBBGApplicationStatusRequest>";
							notify_xml_dbo += "\n</EE_EAI_MESSAGE>";
							
							String xmlBody = "\n\t\t" +"<WorkItemStatus>" + statusDescription + "</WorkItemStatus>";
							notify_xml_dbo  = notify_xml_dbo.replaceAll("#body#", xmlBody);

							DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Notify_appliation: " + notify_xml_dbo);

							String integrationStatus = "Success";
							String ErrDesc = "";
							StringBuilder finalString = new StringBuilder();
							finalString = finalString.append(notify_xml_dbo);
							// changes need to done to update the correct flag
							
							integrationStatus = socketConnection( CommonConnection.getUsername(), processInstanceID, ws_name, 60, 65, socketDetailsMap, finalString);

							XMLParser xmlParserSocketDetails = new XMLParser(integrationStatus);
							DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails);
							String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
							DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Return Code: " + return_code + "WI: " + processInstanceID);
							String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
							DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("return_desc : " + return_desc + "WI: " + processInstanceID);

							String MsgId = "";
							if (integrationStatus.contains("<MessageId>"))
								MsgId = xmlParserSocketDetails.getValueOf("MessageId");

							DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("MsgId : " + MsgId + " for WI: " + processInstanceID);

							if (return_code.equalsIgnoreCase("0000")) 
							{
								integrationStatus = "Success";
								ErrDesc = "Notify Done Successfully";
							}
							
							String tablename = "USR_0_DBO_DEH_Notification";
							String columnname="NotificationSentDate,NotificationStatus";
							String sWhere="WINAME='"+processInstanceID+"' and AWBNo='"+awbNo+"' and NotificationStatus != 'Done'";
							String values = "";
							if ("Success".equalsIgnoreCase(integrationStatus)) 
							{
								values = "getDate(), 'Done'";
							}
							else 
							{
								values = "getDate(), 'Error'";
							}
							
//							String abc ="'"+ processInstanceID +" ', '', '" + NotifyDEHEvent + "', '', ' "+ DeliveryStatus +" ', ' "+ StatusCode +"', getDate(), " +"'"+ integrationStatus +"'";
							updateTableData(tablename , columnname, values,  sWhere); 
						}
					}
				}
			}	
		}
		catch(Exception e)
		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Exception occured at NotifyDEHForTable: " + e);
		}		
	}
	
	
	@SuppressWarnings("unused")
	private void processCasesAtNotifyStep(String queueID, HashMap<String, String> socketDetailsMap)
	{
		try {
			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			// Validate Session ID
			sessionID = CommonConnection.getSessionID(DBO_NotifyDEHLogs.DBO_NotifyLogger, false);

			if (sessionID == null || "".equalsIgnoreCase(sessionID) || "null".equalsIgnoreCase(sessionID)) 
			{
				DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Could Not Get Session ID " + sessionID);
				return;
			}

			// Fetch all Work-Items on given queueID.
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Fetching all Workitems on DBONotifyAPP queue");
			//System.out.println("Fetching all Workitems on DBONotifyAPP queue");
			String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, jtsIP, jtsPort,1);

			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("WMFetchWorkList DBONotifyAPP OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);

			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Number of workitems retrieved on DBONotifyAPP: " + fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DBONotifyAPP: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) 
			{
				for (int i = 0; i < fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					DBO_NotifyDEHLogs.DBO_NotifyLogger
							.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Current ProcessInstanceID: " + processInstanceID);

					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Processing Workitem: " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Current WorkItemID: " + WorkItemID);

					String entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Current EntryDateTime: " + entryDateTime);

					String ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ActivityName: " + ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ActivityID: " + ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ActivityType: " + ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ProcessDefId: " + ProcessDefId);
					
					String DBQuery = "select ProspectID,NotifyDEHAction,PrevWSatNotifyDEH from RB_DBO_EXTTABLE with(nolock) where WINAME='"+ processInstanceID +"'";
					String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,cabinetName,sessionID);
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
					String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,jtsIP,jtsPort, 1);
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
			
					XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			
					int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			
					if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
					{
						NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
			
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) 
						{
							String NotifyDEHEvent = objWorkList.getVal("NotifyDEHAction");
							String prevWSforNotify = objWorkList.getVal("PrevWSatNotifyDEH");
							String ProspectID = objWorkList.getVal("ProspectID");
							
							if("WI_STATUS".equals(NotifyDEHEvent))
							{
								
								String notify_xml_dbo = "";
								notify_xml_dbo += "<EE_EAI_MESSAGE>";
								notify_xml_dbo += "\n\t<EE_EAI_HEADER>";
								notify_xml_dbo += "\n\t\t<MsgFormat>NOTIFY_BBG_APPLICATION</MsgFormat>";
								notify_xml_dbo += "\n\t\t<MsgVersion>0001</MsgVersion>";
								notify_xml_dbo += "\n\t\t<RequestorChannelId>BPM</RequestorChannelId>";
								notify_xml_dbo += "\n\t\t<RequestorUserId>RAKUSER</RequestorUserId>";
								notify_xml_dbo += "\n\t\t<RequestorLanguage>E</RequestorLanguage>";
								notify_xml_dbo += "\n\t\t<RequestorSecurityInfo>secure</RequestorSecurityInfo>";
								notify_xml_dbo += "\n\t\t<ReturnCode>911</ReturnCode>";
								notify_xml_dbo += "\n\t\t<ReturnDesc>Issuer Timed Out</ReturnDesc>";
								notify_xml_dbo += "\n\t\t<MessageId>123123453</MessageId>";
								notify_xml_dbo += "\n\t\t<Extra1>REQ||SHELL.JOHN</Extra1>";
								notify_xml_dbo += "\n\t\t<Extra2>yyyy-MM-dd HH:mm:ssThh:mm:ss.mmm+hh:mm</Extra2>";
								notify_xml_dbo += "\n\t</EE_EAI_HEADER>";
								notify_xml_dbo += "\n\t<NotifyBBGApplicationStatusRequest>";
								notify_xml_dbo += "\n\t\t<BankId>RAK</BankId>";
								notify_xml_dbo += "\n\t\t<Process_Name>DBO</Process_Name>";
								notify_xml_dbo += "\n\t\t<ProspectId>" + ProspectID + "</ProspectId>";
								notify_xml_dbo += "\n\t\t<ChannelId>EBC.WBA</ChannelId>";
								notify_xml_dbo += "\n\t\t<WorkitemNumber>" + processInstanceID + "</WorkitemNumber>";
								notify_xml_dbo += "\n\t\t<Event>" + NotifyDEHEvent + "</Event>";
								notify_xml_dbo += "#body#"; // wi_status
								notify_xml_dbo += "\n\t</NotifyBBGApplicationStatusRequest>";
								notify_xml_dbo += "\n</EE_EAI_MESSAGE>";
								
								String xmlBody = "";
								if("DebitFreezeRemoval".equalsIgnoreCase(prevWSforNotify))
								{
									xmlBody = "\n\t\t" +"<WorkItemStatus>ACCOUNT_ACTIVATED</WorkItemStatus>";
								}
								else if("AccuntClousre".equalsIgnoreCase(prevWSforNotify)||"CourierExpiry".equalsIgnoreCase(prevWSforNotify)||"PostFactoExpiry".equalsIgnoreCase(prevWSforNotify))
								{
									xmlBody = "\n\t\t" +"<WorkItemStatus>ACCOUNT_CLOSED</WorkItemStatus>";
									
									DBQuery = "select DeclineUniqueId,DeclineCategory,DeclineRemarks,DeclineType,DeclineMainCategory,declineCustomerMessage from USR_0_DBO_DECLINE_REJECT_DTLs with(nolock) where WINAME='"+ processInstanceID +"'";
									 extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,cabinetName,sessionID);
									DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
									 extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,jtsIP,jtsPort, 1);
									DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
							
									xmlParserData = new XMLParser(extTabDataOPXML);
							
									iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
							
									if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
									{
										 objWorkList = xmlParserData.createList("Records", "Record");
							
										for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) 
										{
											String declineReasonID = objWorkList.getVal("DeclineUniqueId");
											String declineCategory = objWorkList.getVal("DeclineCategory");
											String declineType = objWorkList.getVal("DeclineType");
											String declineRemarks = objWorkList.getVal("DeclineRemarks");
											String CustomerMessage = objWorkList.getVal("declineCustomerMessage");
											String DeclineMainCategory = objWorkList.getVal("DeclineMainCategory");
											xmlBody += "\n\t\t" +"<DeclineReasonDetailsFromBPM>"+ 
													"\n\t\t\t" +"<DeclinereasonId>"+declineReasonID+"</DeclinereasonId>"+ 
													"\n\t\t\t" +"<DeclineCategory>"+declineCategory+"</DeclineCategory>"+
													"\n\t\t\t" +"<DeclineROMessage>"+declineType+"</DeclineROMessage>"+
													"\n\t\t\t" +"<DeclineCustMessage>"+CustomerMessage+"</DeclineCustMessage>"+
													"\n\t\t\t" +"<DeclineRemarks>"+declineRemarks+"</DeclineRemarks>"+ 
													"\n\t\t\t" +"<Freefield1>"+DeclineMainCategory+"</Freefield1>"+ 
													"\n\t\t" +"</DeclineReasonDetailsFromBPM>";
										}
											
									}
									
								}
								notify_xml_dbo  = notify_xml_dbo.replaceAll("#body#", xmlBody);

								DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Notify_appliation: " + notify_xml_dbo);

								String integrationStatus = "Success";
								String ErrDesc = "";
								StringBuilder finalString = new StringBuilder();
								finalString = finalString.append(notify_xml_dbo);
								// changes need to done to update the correct flag
								
								integrationStatus = socketConnection( CommonConnection.getUsername(), processInstanceID, ws_name, 60, 65, socketDetailsMap, finalString);

								XMLParser xmlParserSocketDetails = new XMLParser(integrationStatus);
								DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails);
								String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
								DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Return Code: " + return_code + "WI: " + processInstanceID);
								String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
								DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("return_desc : " + return_desc + "WI: " + processInstanceID);

								String MsgId = "";
								String decisionValue ="";
								String attributesTag ="";
								String commStatus="0";
								if (integrationStatus.contains("<MessageId>"))
									MsgId = xmlParserSocketDetails.getValueOf("MessageId");

								DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("MsgId : " + MsgId + " for WI: " + processInstanceID);

								if (return_code.equalsIgnoreCase("0000")) 
								{
									integrationStatus = "Success";
									ErrDesc = "Notify Done Successfully";
									if("DebitFreezeRemoval".equalsIgnoreCase(prevWSforNotify))
										commStatus=sendCommunication(processInstanceID,ProcessDefId,ActivityID);
									
									if(!"0".equals(commStatus))
									{
										ErrDesc = "Communication to Stakeholders failed";
									}
								}
								if ("Success".equalsIgnoreCase(integrationStatus) && "0".equals(commStatus))
								{
									decisionValue = "Success";
									DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Decision in success: " + decisionValue);
								}
								else
								{
									ErrDesc = return_desc;
									decisionValue = "Failure";
									DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Decision in else : " + decisionValue);
								}
								DoneWI (processInstanceID,WorkItemID,decisionValue,ErrDesc,ActivityID,ActivityType,entryDateTime);
							}
						}
					}
				}
			}
		} catch (Exception e)

		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Exception: " + e.getMessage());
		}
	}

	private String socketConnection( String username,String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime, HashMap<String, String> socketDetailsMap,
			StringBuilder sInputXML) {

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

		try {

			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("userName " + username);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("SessionId " + sessionID);

			socketServerIP = socketDetailsMap.get("SocketServerIP");
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("SocketServerIP " + socketServerIP);
			socketServerPort = Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("SocketServerPort " + socketServerPort);

			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {

				socket = new Socket(socketServerIP, socketServerPort);
				socket.setSoTimeout(connection_timeout * 1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Dout " + dout);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Din " + din);

				outputResponse = "";

				inputRequest = getRequestXML(processInstanceID, ws_name, username, sInputXML);

				if (inputRequest != null && inputRequest.length() > 0) {
					int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("RequestLen: " + inputRequestLen + "");
					inputRequest = inputRequestLen + "##8##;" + inputRequest;
					DBO_NotifyDEHLogs.DBO_NotifyLogger
							.debug("InputRequest" + "Input Request Bytes : " + inputRequest.getBytes("UTF-16LE"));
					dout.write(inputRequest.getBytes("UTF-16LE"));
					dout.flush();
				}
				byte[] readBuffer = new byte[500];
				int num = din.read(readBuffer);
				if (num > 0) {

					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse + new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("OutputResponse: " + outputResponse);

					if (!"".equalsIgnoreCase(outputResponse))
						outputResponse = getResponseXML( processInstanceID,
								outputResponse, integrationWaitTime);

					if (outputResponse.contains("&lt;")) {
						outputResponse = outputResponse.replaceAll("&lt;", "<");
						outputResponse = outputResponse.replaceAll("&gt;", ">");
					}
				}
				socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>",
						"</MessageId>/n<InputMessageId>" + inputMessageID + "</InputMessageId>");

				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

			}
			else {
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("SocketServerIp is not maintained " + socketServerIP);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(" SocketServerPort is not maintained " + socketServerPort);
				return "Socket Details not maintained";
			}
		}

		catch (Exception e) {
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Exception Occured Mq_connection_CC" + e.getStackTrace());
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
					if (!socket.isClosed())
						socket.close();
					socket = null;
				}
			}
			catch (Exception e) {
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Final Exception Occured Mq_connection_CC" + e.getStackTrace());
				// printException(e);
			}
		}

	}
	
	public HashMap<String, String> getSocketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP, SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);

			String socketDetailsOutputXML = CommonMethods.WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}
	private static void getCommunicationDetails()
	{
		try
		{
			String DBQuery = "select MailTemplate,MailPlaceHolders,mailSubject,fromMail,defaultCCmail,SMSEnglishTemplate from  USR_0_DBO_Communication_Templates with(nolock)";
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,cabinetName,sessionID);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,jtsIP,jtsPort, 1);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
			{
				commDetailsMap.put("MailTemplate",xmlParserData.getValueOf("MailTemplate"));
				commDetailsMap.put("MailPlaceHolders",xmlParserData.getValueOf("MailPlaceHolders"));
				commDetailsMap.put("mailSubject",xmlParserData.getValueOf("mailSubject"));
				commDetailsMap.put("fromMail",xmlParserData.getValueOf("fromMail"));
				commDetailsMap.put("defaultCCmail",xmlParserData.getValueOf("defaultCCmail"));
				commDetailsMap.put("SMSEnglishTemplate",xmlParserData.getValueOf("SMSEnglishTemplate"));
			}
		}
		catch (Exception e)
		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Exception in getCommunicationDetails : " + e.toString());
		}
	}
	
	private String getResponseXML(String processInstanceID, String message_ID, int integrationWaitTime) {

		String outputResponseXML = "";
		try {
			String QueryString = "select OUTPUT_XML from NG_DBO_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"
					+ message_ID + "' and WI_NAME = '" + processInstanceID + "'"; // toDo

			String responseInputXML = CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionID);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Response APSelect InputXML: " + responseInputXML);

			int Loop_count = 0;
			do {
				String responseOutputXML = CommonMethods.WFNGExecute(responseInputXML, jtsIP, jtsPort, 1);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Response APSelect OutputXML: " + responseOutputXML);

				XMLParser xmlParserSocketDetails = new XMLParser(responseOutputXML);
				String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ResponseMainCode: " + responseMainCode);

				int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ResponseTotalRecords: " + responseTotalRecords);

				if (responseMainCode.equals("0") && responseTotalRecords > 0) {

					String responseXMLData = xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData = responseXMLData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
					// DBONotifyAPPLog.DBO_NotifyLogger.debug("ResponseXMLData: "+responseXMLData);

					outputResponseXML = xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
					// DBONotifyAPPLog.DBO_NotifyLogger.debug("OutputResponseXML:
					// "+outputResponseXML);

					if ("".equalsIgnoreCase(outputResponseXML)) {
						outputResponseXML = "Error";
					}
					break;
				}
				Loop_count++;
				Thread.sleep(1000);
			} while (Loop_count < integrationWaitTime);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("integrationWaitTime: " + integrationWaitTime);

		} catch (Exception e) {
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML = "Error";
		}

		return outputResponseXML;

	}

	private String getRequestXML(String processInstanceID, String ws_name, String userName, StringBuilder sInputXML) {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DBO_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("GetRequestXML: " + strBuff.toString());
		return strBuff.toString();
	}
	
	private int readConfig() {
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles"
					+ File.separator + "DBO_Notify_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				NotifyAppConfigParamMap.put(name, p.getProperty(name));
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}
	private static String sendCommunication(String WINAME,String ProcessDefID, String activityId)
	{
		String commStatus="";
		try
		{
			String DBQuery = "select RP.relatedPartyID,RP.Fullname,RP.emailID,Concat(RP.MobNoCountryCode,RP.MobNumber) as MoBNO,EX.TLnumber,EX.AccountNumber,EX.ProductCurrency from USR_0_DBO_RelatedPartyGrid RP with(nolock),RB_DBO_EXTTABLE EX with(nolock) where RP.winame=EX.winame and RP.winame='"+WINAME+"' and ( RP.DeclarationRequired='Y' or RP.DeclarationRequired='Yes')";
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,cabinetName,sessionID);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,jtsIP,jtsPort, 1);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
	
			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
	
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
	
			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
			{
				NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
	
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) 
				{
					String RPID = objWorkList.getVal("relatedPartyID");
					String Name = objWorkList.getVal("Fullname");
					String mailTo = objWorkList.getVal("emailID");
					String TLnumber = objWorkList.getVal("TLnumber");
					String accNo = objWorkList.getVal("AccountNumber");
					String productCurr = objWorkList.getVal("ProductCurrency");
					String Mobile_No = objWorkList.getVal("MoBNO");
					
					DBQuery = "select top 3 pd.ImageIndex,pd.Name,pd.VolumeId,ltrim(rtrim(pd.AppName)) as AppName,pd.CreatedDateTime,pd.comment "+
			                      "from PDBFolder pf WITH(NOLOCK),"+ 
			                      "PDBDocumentContent pdc WITH(NOLOCK),"+
			                      "PDBDocument pd WITH(NOLOCK) "+
						          "where pf.Name = '"+WINAME+"' "+
						          "and pf.FolderIndex = pdc.ParentFolderIndex "+
						          "and pd.DocumentIndex = pdc.DocumentIndex "+
						          "and ((pd.Name = 'Signed Declaration form' and pd.comment like '%"+RPID+"%') or pd.Name = 'Account Opening Application' or pd.Name = 'Accepted Authorization') order by pd.CreatedDateTime desc";
					extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,cabinetName,sessionID);
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
					extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,jtsIP,jtsPort, 1);
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
			
					xmlParserData = new XMLParser(extTabDataOPXML);
			
					iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			
					if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
					{
						NGXmlList objWorkList2 = xmlParserData.createList("Records", "Record");
						String finalattachmentISINDEX="";
						String finalname="";
						String finalextn="";
						for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true)) 
						{
							String imageIndex = objWorkList2.getVal("ImageIndex");
							String name = objWorkList2.getVal("Name");
							String VolumeId = objWorkList2.getVal("VolumeId");
							String extn = objWorkList2.getVal("AppName");
							
							String attachmentISINDEX=imageIndex + "#" + VolumeId +"#";
							if("".equalsIgnoreCase(finalattachmentISINDEX))
							{
								finalattachmentISINDEX=attachmentISINDEX;
							}
							else
							{
								finalattachmentISINDEX+=";"+attachmentISINDEX;
							}
							if("".equalsIgnoreCase(finalname))
							{
								finalname=name;
							}
							else
							{
								finalname+=";"+name;
							}
							if("".equalsIgnoreCase(finalextn))
							{
								finalextn=extn;
							}
							else
							{
								finalextn+=";"+extn;
							}
						}
						
						String mailSubject=commDetailsMap.get("mailSubject").replace("$TLNUMBER$",TLnumber );
						String MailStr = commDetailsMap.get("MailTemplate").replace("$AccountNumber$",accNo ).replace("$ProductCurrency$",productCurr );
						commStatus=sendMail(mailSubject, MailStr,mailTo,ProcessDefID,activityId,finalattachmentISINDEX,finalname,finalextn);
						commStatus=sendSMS(Mobile_No,ProcessDefID,"DBO_Communication",WINAME,"DBO");
						
					}
				}
			}
		}
		catch(Exception e)
		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Exception occurred in sendCommunication" + e.getMessage());
		}
		return commStatus;
	}
	public static String sendMail( String mailSubject,String MailStr,String mailTo,String ProcessDefID,String activityId,String attachmentISINDEX,String attachmentNames,String attachmentExts)throws Exception
	{
		XMLParser objXMLParser = new XMLParser();
		String sInputXML="";
		String sOutputXML="";
		String mainCodeforAPInsert=null;
		int sessionCheckInt=0;
		int loopCount=50;
		String retVal="-1";
		while(sessionCheckInt<loopCount)
		{
			try
			{
				
				String columnName = "mailFrom,mailTo,mailCC,mailSubject,mailMessage,mailContentType,attachmentISINDEX,attachmentNames,attachmentExts,mailPriority,mailStatus,mailActionType,insertedTime,processDefId,workitemId,activityId,noOfTrials,zipFlag";
				String strValues = "'"+commDetailsMap.get("fromMail")+"','"+mailTo+"','"+commDetailsMap.get("defaultCCmail")+"','"+mailSubject+"',N'"+MailStr+"','text/html;charset=UTF-8','"+attachmentISINDEX+"','"+attachmentNames+"','"+attachmentExts+"','1','N','TRIGGER','"+CommonMethods.getdateCurrentDateInSQLFormat()+"','"+ProcessDefID+"','1','"+activityId+"','0','N'";
				
				sInputXML = "<?xml version=\"1.0\"?>" +
						"<APInsert_Input>" +
						"<Option>APInsert</Option>" +
						"<TableName>WFMAILQUEUETABLE</TableName>" +
						"<ColName>" + columnName + "</ColName>" +
						"<Values>" + strValues + "</Values>" +
						"<EngineName>" + cabinetName + "</EngineName>" +
						"<SessionId>" + sessionID + "</SessionId>" +
						"</APInsert_Input>";

				DBO_NotifyDEHLogs.DBO_NotifyLogger.info("Mail Insert InputXml::::::::::\n"+sInputXML);
				sOutputXML =CommonMethods.WFNGExecute(sInputXML, jtsIP, jtsPort, 0);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.info("Mail Insert OutputXml::::::::::\n"+sOutputXML);
				objXMLParser.setInputXML(sOutputXML);
				mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Exception in Sending mail", e);
				sessionCheckInt++;
				waiteloopExecute(waitLoop);
				continue;
			}
			if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
			{
				DBO_NotifyDEHLogs.DBO_NotifyLogger.info("Invalid session in Sending mail");
				sessionCheckInt++;
				sessionID=CommonConnection.getSessionID(DBO_NotifyDEHLogs.DBO_NotifyLogger, true);
				continue;
			}
			else
			{
				sessionCheckInt++;
				break;
			}
		}
		if(mainCodeforAPInsert.equalsIgnoreCase("0"))
		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.info("mail Insert Successful");
			retVal="0";
		}
		else
		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.info("mail Insert Unsuccessful");
		}
		return retVal;
	}
	public static String sendSMS(String Mobile_No,String ProcessDefID,String wsname,String WI_NAME,String processNames)throws Exception
	{
		XMLParser objXMLParser = new XMLParser();
		String sInputXML="";
		String sOutputXML="";
		String mainCodeforAPInsert=null;
		String AlertName="DBOCommunication";
		String AlertCode="";
		String AlertStatus="P";
		
		int sessionCheckInt=0;
		int loopCount=50;
		
		String retVal="-1";
		String Alert_Subject = "DBO RAKBANK Communication";
		while(sessionCheckInt<loopCount)
		{
			try
			{
				String columnName = "Alert_Name,Alert_Code,ALert_Status,Mobile_No,Alert_Text,Alert_Subject,WI_Name,Workstep_Name,Inserted_Date";
				String strValues = "'"+AlertName+"','"+AlertCode+"','"+AlertStatus+"','"+Mobile_No+"',N'"+commDetailsMap.get("SMSEnglishTemplate")+"','"+Alert_Subject+"','"+WI_NAME+"','"+wsname+"','"+CommonMethods.getdateCurrentDateInSQLFormat()+"'";
				//insertQuery="Insert into USR_0_BPM_SMSQUEUETABLE ("+columnName+" ) values ("+strValues+")";
				//result = updateQuery(insertQuery,dataSource);
				sInputXML = "<?xml version=\"1.0\"?>" +
						"<APInsert_Input>" +
						"<Option>APInsert</Option>" +
						"<TableName>USR_0_BPM_SMSQUEUETABLE</TableName>" +
						"<ColName>" + columnName + "</ColName>" +
						"<Values>" + strValues + "</Values>" +
						"<EngineName>" + cabinetName + "</EngineName>" +
						"<SessionId>" + sessionID + "</SessionId>" +
						"</APInsert_Input>";

				DBO_NotifyDEHLogs.DBO_NotifyLogger.info("SMS Insert InputXml::::::::::\n"+sInputXML);
				sOutputXML =CommonMethods.WFNGExecute(sInputXML, jtsIP, jtsPort, 0);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.info("SMS Insert OutputXml::::::::::\n"+sOutputXML);
				objXMLParser.setInputXML(sOutputXML);
				mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				DBO_NotifyDEHLogs.DBO_NotifyLogger.error("Exception in Sending SMS", e);
				sessionCheckInt++;
				waiteloopExecute(waitLoop);
				continue;
			}
			if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
			{
				DBO_NotifyDEHLogs.DBO_NotifyLogger.info("Invalid session in Sending SMS");
				sessionCheckInt++;
				sessionID=CommonConnection.getSessionID(DBO_NotifyDEHLogs.DBO_NotifyLogger, true);
				continue;
			}
			else
			{
				sessionCheckInt++;
				break;
			}
		}
		if(mainCodeforAPInsert.equalsIgnoreCase("0"))//if(result>0)
		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.info("SMS Insert Successful");
			retVal="0";
			//UpdateCommFlag(WI_NAME,stage,processName,sessionId,"'Done'");
			//InsertRecordInCommHistory(WI_NAME,wsname,processName,sessionId,"SMS","Success","NA",Mobile_No,"NA",category,Alert_Subject,Alert_Text);
		}
		else
		{
			DBO_NotifyDEHLogs.DBO_NotifyLogger.info("SMS Insert Unsuccessful");
		}
		return retVal;
	}
	public static void waiteloopExecute(long wtime) 
	{
		try 
		{
			for (int i = 0; i < 10; i++) 
			{
				Thread.yield();
				Thread.sleep(wtime / 10);
			}
		} 
		catch (InterruptedException e) 
		{
		}
	}
	private static String updateTableData(String tablename, String columnname,String values, String sWhere)
	{
		
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		String status="";
		
		DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,values,sWhere,cabinetName,sessionID);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=CommonMethods.WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!"0".equalsIgnoreCase(mainCodeforCheckUpdate)){
					
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(("Error in executing update on "+tablename+" :maincode"+mainCodeforCheckUpdate));
					status = "Error";
				}
				else
				{
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(("Succesfully updated "+tablename+" table"));
					return "Success";
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionID  = CommonConnection.getSessionID(DBO_NotifyDEHLogs.DBO_NotifyLogger, false);
				}
				else
				{
					sessionCheckInt++;
					break;
				}

				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == "" || outXmlCheckAPUpdate == null)
					break;

			}
			catch(Exception e)
			{
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
				status = "Error";
			}
		}
		return status;
	}
	
	private static String DoneWI (String processInstanceID,String WorkItemID, String decision, String Remarks,String ActivityID,String ActivityType,String entryDateTime)
	{
		String retValue="";
		try
		{
			//Lock WorkItem
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML,jtsIP,jtsPort,1);
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);
			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
			if (getWorkItemMainCode.trim().equals("0"))
			{
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

				String attributesTag = "<Decision>" + decision + "</Decision>";

				DBO_NotifyDEHLogs.DBO_NotifyLogger.info("get Workitem call successfull for "+processInstanceID);
				String completeWIFlag="D";
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
				//Move Workitem to next Workstep 
				String completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, processInstanceID, WorkItemID,ActivityID,ActivityType, attributesTag,completeWIFlag);
				//completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, Wi_name, WorkItemID, attributesTag,completeWIFlag);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

				String completeWorkItemOutputXML = CommonMethods.WFNGExecute(completeWorkItemInputXML,jtsIP,jtsPort,1);
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);

				XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
				String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
				DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);

				if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) 
				{
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("assignWorkitemAttributeInput successful: "+completeWorkitemMaincode);
					SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					//SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");

					Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
					String formattedEntryDatetime=inputDateformat.format(entryDatetimeFormat);
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

					Date actionDateTime= new Date();
					String formattedActionDateTime=inputDateformat.format(actionDateTime);
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

					//Insert in WIHistory Table.
					String columnNames="WINAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
					String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ws_name+"','"
					+CommonConnection.getUsername()+"','"+decision+"','"+formattedEntryDatetime+"','"+Remarks+"'";

					String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"USR_0_DBO_WIHISTORY"); // toDo
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("APInsertInputXML: "+apInsertInputXML);

					String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,jtsIP,jtsPort,1);
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

					XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
					String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Completed On "+ ws_name);

					if(apInsertMaincode.equalsIgnoreCase("0"))
					{
						DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ApInsert successful: "+apInsertMaincode);
						DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Inserted in WiHistory table successfully.");
					}
					else
					{
						DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("ApInsert failed: "+apInsertMaincode);
					}
				}
				else 
				{
					completeWorkitemMaincode="";
					DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
				}
			
				
									
			}
		}catch(Exception e){
			DBO_NotifyDEHLogs.DBO_NotifyLogger.debug("Exception: "+e.getMessage());
		}
		return retValue;
	}

}
