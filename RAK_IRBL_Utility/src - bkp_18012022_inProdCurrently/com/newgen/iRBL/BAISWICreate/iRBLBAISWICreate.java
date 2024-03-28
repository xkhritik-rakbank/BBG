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


package com.newgen.iRBL.BAISWICreate;

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



public class iRBLBAISWICreate implements Runnable
{


	private static NGEjbClient ngEjbClientBAISWICreate;

	static Map<String, String> BAISWICreateConfigParamMap= new HashMap<String, String>();


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
			iRBLBAISWICreateLog.setLogger();
			ngEjbClientBAISWICreate = NGEjbClient.getSharedInstance();

			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.error("Could not Read Config Properties [IRBLStatus]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("JTSPORT: " + jtsPort);

			queueID = BAISWICreateConfigParamMap.get("queueID");
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("QueueID: " + queueID);
			
			socketConnectionTimeout=Integer.parseInt(BAISWICreateConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(BAISWICreateConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(BAISWICreateConfigParamMap.get("SleepIntervalInMin"));
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID);
				while(true)
				{
					iRBLBAISWICreateLog.setLogger();
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("IRBL Status...123.");
					startBAISWICreateStatusUtility(cabinetName, jtsIP, jtsPort,sessionID,
							queueID,socketConnectionTimeout, integrationWaitTime,socketDetailsMap);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.error("Exception Occurred in IRBL CBS : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.error("Exception Occurred in iRBL CBS : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "iRBL_BAISWICreate_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    BAISWICreateConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startBAISWICreateStatusUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		String ws_name="";

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Fetching all Workitems on BAIS_WICreate queue");
			System.out.println("Fetching all Workitems on BAIS_WICreate queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Number of workitems retrieved on BAIS_WICreate : "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on BAIS_WICreate : "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Current EntryDateTime: "+entryDateTime);

					ws_name=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Current ws_name: "+ws_name);	
					
					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("ProcessDefId: "+ProcessDefId);

					
						String decisionValue="";

						iRBLBAISWICreateIntegration objIntegration= new iRBLBAISWICreateIntegration();
						String integrationStatus=objIntegration.customIntegration(cabinetName,sessionId, sJtsIp,
								iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, BAISWICreateConfigParamMap);

						
						String attributesTag;

						if (integrationStatus.contains("Success"))
						{
							String[] splitintstatus =integrationStatus.split("~");
							decisionValue = "Success";
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Decision" +decisionValue);
							 attributesTag="<qDecision>"+decisionValue+"</qDecision>" + "<BPM_REF_BAIS>" +splitintstatus[1]+ "</BPM_REF_BAIS>";
						}
						else if (integrationStatus.contains("Customer SR"))
						{
							decisionValue = "Failure";
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision>" +"<FAILEDINTEGRATIONCALL>"+integrationStatus+"</FAILEDINTEGRATIONCALL>" + "<MW_ERRORDESC>"
							+integrationStatus+ "</MW_ERRORDESC>" ;

						}
						else
						{
							decisionValue = "Failure";
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision>" +"<FAILEDINTEGRATIONCALL>BAIS WI Create</FAILEDINTEGRATIONCALL>" + "<MW_ERRORDESC>"
							+integrationStatus+ "</MW_ERRORDESC>" ;

						}


						//To be modified according to output of Integration Call.

					//Lock Workitem.
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						//String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId, processInstanceID,WorkItemID,attributesTag);
						
						String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
								+ "<Option>WMAssignWorkItemAttributes</Option>"
								+ "<EngineName>"+cabinetName+"</EngineName>"
								+ "<SessionId>"+sessionId+"</SessionId>"
								+ "<ProcessInstanceId>"+processInstanceID+"</ProcessInstanceId>"
								+ "<WorkItemId>"+WorkItemID+"</WorkItemId>"
								+ "<ActivityId>"+ActivityID+"</ActivityId>"
								+ "<ProcessDefId>"+ProcessDefId+"</ProcessDefId>"
								+ "<LastModifiedTime></LastModifiedTime>"
								+ "<ActivityType>"+ActivityType+"</ActivityType>"
								+ "<complete>D</complete>"
								+ "<AuditStatus></AuditStatus>"
								+ "<Comments></Comments>"
								+ "<UserDefVarFlag>Y</UserDefVarFlag>"
								+ "<Attributes>"+attributesTag+"</Attributes>"
								+ "</WMAssignWorkItemAttributes_Input>";
						
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);

						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
						{
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);

							/*String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionId,
									processInstanceID,WorkItemID);
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);


							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");

							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);
							*/

							//Move Workitem to next Workstep

							//if (completeWorkitemMaincode.trim().equalsIgnoreCase("0"))
							if ("0".trim().equalsIgnoreCase("0"))
							{
								//iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
								System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

								iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("WorkItem moved to next Workstep.");

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
								iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=outputDateFormat.format(actionDateTime);
								iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WI_NAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ws_name+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+integrationStatus+"'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_IRBL_WIHISTORY");
								iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("APInsertOutputXML: "+ apInsertOutputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Completed On "+ ws_name);


								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("ApInsert successful: "+apInsertMaincode);
									iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								//completeWorkitemMaincode="";
								//iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
						else
						{
							assignWorkitemAttributeMainCode="";
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
					}
				


		}
			}
		}
			catch (Exception e)

		{
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Exception: "+e.getMessage());
		}
	}



			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("In WF NG Execute : " + serverPort);
				try
				{
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP,
								Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientBAISWICreate.makeCall(jtsServerIP, serverPort,
								"WebSphere", ipXML);
				}
				catch (Exception e)
				{
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}

}



