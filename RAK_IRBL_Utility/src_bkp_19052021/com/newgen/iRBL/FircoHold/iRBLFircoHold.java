/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: RAOPStatus.java
Author 					: Shubham Gupta
Date (DD/MM/YYYY)		: 15/06/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.FircoHold;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;



public class iRBLFircoHold implements Runnable
{


	private static NGEjbClient ngEjbClientRAOPStatus;

	static Map<String, String> raopStatusConfigParamMap= new HashMap<String, String>();


	@Override
	public void run()
	{
		String sessionID = "";
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;

		try
		{
			iRBLFircoHoldLog.setLogger();
			ngEjbClientRAOPStatus = NGEjbClient.getSharedInstance();

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLFircoHoldLog.iRBLFircoHoldLogger.error("Could not Read Config Properties [RAOPStatus]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("JTSPORT: " + jtsPort);

			queueID = raopStatusConfigParamMap.get("queueID");
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(raopStatusConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(raopStatusConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(raopStatusConfigParamMap.get("SleepIntervalInMin"));
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getSessionID(iRBLFircoHoldLog.iRBLFircoHoldLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID);
				while(true)
				{
					iRBLFircoHoldLog.setLogger();
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("RAOP Status...123.");
					startRAOPStatusUtility(cabinetName, jtsIP, jtsPort,sessionID,
							queueID,socketConnectionTimeout, integrationWaitTime,socketDetailsMap);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.error("Exception Occurred in RAOP CBS : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLFircoHoldLog.iRBLFircoHoldLogger.error("Exception Occurred in RAOP CBS : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "RAOP_Status_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    raopStatusConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startRAOPStatusUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="RAOP_Status_Update";

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(iRBLFircoHoldLog.iRBLFircoHoldLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				iRBLFircoHoldLog.iRBLFircoHoldLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Fetching all Workitems on RAOP_Status_Update queue");
			System.out.println("Fetching all Workitems on RAOP_Status_Update queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Number of workitems retrieved on RAOP_Status_Update: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on RAOP_Status_Update: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Current EntryDateTime: "+entryDateTime);



					String extTabDBQuery = "SELECT PREV_WS, WINAME, YAP_STATUS, (select Item_Code from USR_0_RAOP_ERROR_DESC_MASTER where Item_Desc=right(YAP_REJECT_REASON, len(YAP_REJECT_REASON) - CHARINDEX('-',YAP_REJECT_REASON))) as YAP_REJECT_CODE, YAP_REJECT_REASON, DECISION, CURRENT_WS, REMARKS, CHANNEL" +
							" FROM RB_RAOP_EXTTABLE A WITH (NOLOCK) , WFINSTRUMENTTABLE B WITH (NOLOCK) " +
							"WHERE A.WINAME = B.PROCESSINSTANCEID  AND B.WORKITEMID = '1' " +
							"AND A.WINAME = '"+processInstanceID+"'";


					String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(extTabDBQuery,cabinetName, sessionId);
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("extTabDataIPXML: "+ extTabDataIPXML);
					String extTabDataOPXML = WFNGExecute(extTabDataIPXML,sJtsIp,iJtsPort,1);
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("extTabDataOPXML: "+ extTabDataOPXML);

					XMLParser xmlParserextTabData= new XMLParser(extTabDataOPXML);


					if(xmlParserextTabData.getValueOf("MainCode").equalsIgnoreCase("0")&& Integer.parseInt(xmlParserextTabData.getValueOf("TotalRetrieved"))>0)
					{
						String xmlDataExtTab=xmlParserextTabData.getNextValueOf("Record");
						xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");


						XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);

						String CurrWS = xmlParserExtTabDataRecord.getValueOf("CURRENT_WS");
						String winame = xmlParserExtTabDataRecord.getValueOf("WINAME");
						String status = xmlParserExtTabDataRecord.getValueOf("YAP_STATUS");
						String rejectcode = xmlParserExtTabDataRecord.getValueOf("YAP_REJECT_CODE");
						String rejectreason = xmlParserExtTabDataRecord.getValueOf("YAP_REJECT_REASON");
						String remark = xmlParserExtTabDataRecord.getValueOf("REMARKS");
						String channel = xmlParserExtTabDataRecord.getValueOf("CHANNEL");
					//	String Dec = xmlParserExtTabDataRecord.getValueOf("DECISION");
						
						try
						{
							if ("".equalsIgnoreCase(rejectcode))
							{
								if (rejectreason.contains("-"))
								{
									String [] rcd = rejectreason.split("-");
									rejectcode = rcd[0].replace("(", "").replace(")", "");
								}
							}
						}
						catch(Exception e)
						{
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Exception in getting reject code: "+e.getMessage());
						}
						
						try
						{
							if (rejectreason.contains("-"))
							{
								String [] rcd = rejectreason.split("-");
								rejectreason = rcd[1];
							}
						}
						catch(Exception e)
						{
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Exception in getting reject reason: "+e.getMessage());
						}

						HashMap<String, String> ExtTabDataMap = new HashMap<String, String>();
						ExtTabDataMap.put("WINAME", winame);
						ExtTabDataMap.put("STATUS", status);
						ExtTabDataMap.put("REJECTCODE", rejectcode);
						ExtTabDataMap.put("REJECTREASON", rejectreason);
						ExtTabDataMap.put("REMARKS", remark);
						ExtTabDataMap.put("CHANNEL", channel);


						//RAOP Integration Call
						String decisionValue="";

						iRBLFircoHoldIntegration objRAOPIntegration= new iRBLFircoHoldIntegration();
						String integrationStatus=objRAOPIntegration.customIntegration(cabinetName,sessionId, sJtsIp,
								iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap);

						String[] splitintstatus =integrationStatus.split("~");


						String ErrDesc = "MessageId: "+splitintstatus[2] + ", Return Code: "+splitintstatus[0] +", Return Desc: "+ splitintstatus[1];
						String attributesTag;

						if (splitintstatus[0].equals("0000"))
						{
							decisionValue = "Success";
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Decision" +decisionValue);
							 attributesTag="<DECISION>"+decisionValue+"</DECISION>" + "<INTEGRATION_ERROR_RECEIVED>" + "" + "</INTEGRATION_ERROR_RECEIVED>";
						}
						else
						{
							decisionValue = "Failure";
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Decision" +decisionValue);
							attributesTag="<DECISION>"+decisionValue+"</DECISION>" + "<INTEGRATION_ERROR_RECEIVED>" +  ws_name + "</INTEGRATION_ERROR_RECEIVED>"+"<FAILEDINTEGRATIONCALL>NOTIFY_SR_STATUS</FAILEDINTEGRATIONCALL>" + "<MW_ERRORDESC>"
							+ErrDesc+ "</MW_ERRORDESC>" ;

						}


						//To be modified according to output of Integration Call.

					//Lock Workitem.
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,
								processInstanceID,WorkItemID,attributesTag);
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);

						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
						{
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);

							String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionId,
									processInstanceID,WorkItemID);
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);


							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");

							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);


							//Move Workitem to next Workstep

							if (completeWorkitemMaincode.trim().equalsIgnoreCase("0"))
							{
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
								System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WorkItem moved to next Workstep.");

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=outputDateFormat.format(actionDateTime);
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WI_NAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ws_name+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_RAOP_WIHISTORY");
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Completed On "+ CurrWS);


								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("ApInsert successful: "+apInsertMaincode);
									iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								completeWorkitemMaincode="";
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
						else
						{
							assignWorkitemAttributeMainCode="";
							iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
					}
				}

			else
			{
				iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WMFetchWorkList failed: "+fetchWorkItemListMainCode);
			}

		}
			}
		}
			catch (Exception e)

		{
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Exception: "+e.getMessage());
		}
	}



			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'RAOP' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("In WF NG Execute : " + serverPort);
				try
				{
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP,
								Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientRAOPStatus.makeCall(jtsServerIP, serverPort,
								"WebSphere", ipXML);
				}
				catch (Exception e)
				{
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}

}



