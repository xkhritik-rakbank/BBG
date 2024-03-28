/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK iRBL Utility
Module					: AP Approval Hold Status
File Name				: AOApprovalHold.java
Author 					: Angad Shah
Date (DD/MM/YYYY)		: 29/06/2021

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.AOApprovalHold;

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



public class iRBLAOApprovalHold implements Runnable
{


	private static NGEjbClient ngEjbClientAOApprovalHold;

	static Map<String, String> AOApprovalHoldConfigParamMap= new HashMap<String, String>();


	@Override
	public void run()
	{
		String sessionID = "";
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;
		
		try
		{
			iRBLAOApprovalHoldLog.setLogger();
			ngEjbClientAOApprovalHold = NGEjbClient.getSharedInstance();

			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.error("Could not Read Config Properties [IRBLAOApprovalHold]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("JTSPORT: " + jtsPort);

			queueID = AOApprovalHoldConfigParamMap.get("queueID");
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("QueueID: " + queueID);

			sleepIntervalInMin=Integer.parseInt(AOApprovalHoldConfigParamMap.get("SleepIntervalInMin"));
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getSessionID(iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Session ID found: " + sessionID);
				
				while(true)
				{
					iRBLAOApprovalHoldLog.setLogger();
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("IRBL AO Approval Hold...123.");
					startAOApprovalHoldStatusUtility(cabinetName, jtsIP, jtsPort,sessionID,
							queueID, integrationWaitTime);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.error("Exception Occurred in IRBL AO Approval Hold : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.error("Exception Occurred in iRBL AO Approval Hold : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "iRBL_AOApprovalHold_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    AOApprovalHoldConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startAOApprovalHoldStatusUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int integrationWaitTime)
	{
		String ws_name="";

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Fetching all Workitems on iRBL_AO_Approval_Hold queue");
			System.out.println("Fetching all Workitems on iRBL_AO_Approval_Hold queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Number of workitems retrieved on iRBL_AO_Approval_Hold : "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on iRBL_AO_Approval_Hold : "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Current EntryDateTime: "+entryDateTime);

					ws_name=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Current ws_name: "+ws_name);		
					
					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("ProcessDefId: "+ProcessDefId);
					
					String decisionValue="";

					iRBLAOApprovalHoldIntegration objIntegration= new iRBLAOApprovalHoldIntegration();
					String integrationStatus=objIntegration.customIntegration(cabinetName,sessionId, sJtsIp,
							iJtsPort,processInstanceID,ws_name,integrationWaitTime, AOApprovalHoldConfigParamMap);

					
					String attributesTag="";
					String RemarksForDecHistory = "";
					if(integrationStatus.contains("~"))
					{
						String tmp[] = integrationStatus.split("~");
						if ("Exit".equalsIgnoreCase(tmp[0]))
						{
							decisionValue = "Approved";
							RemarksForDecHistory="This BAIS Workitem "+tmp[1]+" is Approved";
							iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision>" + "<DECISION>" +decisionValue+ "</DECISION>";
						}
						else if ("Reject".equalsIgnoreCase(tmp[0]))
						{
							decisionValue = "Rejected";
							RemarksForDecHistory="This BAIS Workitem "+tmp[1]+" is Rejected";
							iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision>" + "<DECISION>" +decisionValue+ "</DECISION>";
						}
						else
						{
							continue;
						}
					}
					else
					{
						continue;
					}

					//To be modified according to output of Integration Call.

					//Lock Workitem.
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

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
						
						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);

						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
						{
							iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);

							/*String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionId,
									processInstanceID,WorkItemID);
							iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
							iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);


							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");

							iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);
							*/

							//Move Workitem to next Workstep

							//if (completeWorkitemMaincode.trim().equalsIgnoreCase("0"))
							if ("0".trim().equalsIgnoreCase("0"))
							{
								//iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
								System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

								iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("WorkItem moved to next Workstep.");

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
								iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=outputDateFormat.format(actionDateTime);
								iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WI_NAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ws_name+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+RemarksForDecHistory+"'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_IRBL_WIHISTORY");
								iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("APInsertOutputXML: "+ apInsertOutputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Completed On "+ ws_name);


								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("ApInsert successful: "+apInsertMaincode);
									iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								//completeWorkitemMaincode="";
								//iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
						else
						{
							assignWorkitemAttributeMainCode="";
							iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
					}
				


		}
			}
		}
			catch (Exception e)

		{
			iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Exception: "+e.getMessage());
		}
	}



			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("In WF NG Execute : " + serverPort);
				try
				{
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP,
								Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientAOApprovalHold.makeCall(jtsServerIP, serverPort,
								"WebSphere", ipXML);
				}
				catch (Exception e)
				{
					iRBLAOApprovalHoldLog.iRBLAOApprovalHoldLogger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}

}



