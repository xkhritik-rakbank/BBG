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


package com.newgen.DBO.CBS_Update;

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

import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;



public class DBO_CBS_Update implements Runnable
{


	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> CBSUpdateConfigParamMap= new HashMap<String, String>();


	@Override
	public void run()
	{
		String sessionID = "";
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String smsPort = "";
		String volumeId = "";
		String siteId = "";
		String queueID = "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;
		String docDownloadPath = "";
		String Sig_Remarks = "";
		
		try
		{
			DBO_CBS_Update_Log.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.error("Could not Read Config Properties [CBSUpdate]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("JTSPORT: " + jtsPort);
			
			smsPort = CommonConnection.getsSMSPort();
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("smsPort: " + smsPort);
			
			volumeId = CommonConnection.getsVolumeID();
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("volumeId: " + volumeId);
			
			siteId = CommonConnection.getsSiteID();
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("siteId: " + siteId);

			queueID = CBSUpdateConfigParamMap.get("queueID");
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("QueueID: " + queueID);
			
			socketConnectionTimeout=Integer.parseInt(CBSUpdateConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(CBSUpdateConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(CBSUpdateConfigParamMap.get("SleepIntervalInMin"));
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			docDownloadPath=CBSUpdateConfigParamMap.get("FileDownloadPath");
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("docDownloadPath: "+docDownloadPath);
			
			Sig_Remarks=CBSUpdateConfigParamMap.get("DefaultSigntureRemarks");
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Sig_Remarks: "+Sig_Remarks);

			sessionID = CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID);
				while(true)
				{
					DBO_CBS_Update_Log.setLogger();
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("DBO Core_System_Update...123.");
					startDBOCBSUpdateUtility(cabinetName, jtsIP, jtsPort,sessionID,
							queueID,socketConnectionTimeout, integrationWaitTime,socketDetailsMap, smsPort, docDownloadPath,
							volumeId, siteId, Sig_Remarks);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.error("Exception Occurred in DBO Core_System_Update : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.error("Exception Occurred in DBO Core_System_Update : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DBO_CBSUpdate_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    CBSUpdateConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startDBOCBSUpdateUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, String smsPort, String docDownloadPath,String volumeId,String siteId, String Sig_Remarks)
	{
		final String ws_name="Sys_Core_Update";

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Fetching all Workitems on DBO_Core_System_Update queue");
			System.out.println("Fetching all Workitems on DBO_Core_System_Update queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Number of workitems retrieved on DBO_Core_System_Update: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DBO_Core_System_Update: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ActivityName: "+ActivityName);
					
					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ProcessDefId: "+ProcessDefId);
					
						//RAOP Integration Call
						String decisionValue="";

						DBO_CBS_Update_Integration objIntegration= new DBO_CBS_Update_Integration();
						String integrationStatus=objIntegration.customIntegration(cabinetName,sessionId, sJtsIp,
								iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, smsPort, docDownloadPath,
								volumeId, siteId, Sig_Remarks);

						String attributesTag;
						String ErrDesc = "Signature Push and Debit Unfreeze Done Successfully";
						if ("Success".equalsIgnoreCase(integrationStatus))
						{
							decisionValue = "Success";
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Decision" +decisionValue);
							attributesTag="<DECISION>"+decisionValue+"</DECISION>";
						}
						else
						{
							ErrDesc = integrationStatus.replace("~", ",").replace("|", "\n");
							decisionValue = "Failure";
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Decision" +decisionValue);
							attributesTag="<DECISION>"+decisionValue+"</DECISION>" ;

						}


						//To be modified according to output of Integration Call.

					//Lock Workitem.
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						//String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,processInstanceID,WorkItemID,attributesTag);
						
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
						
						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);

						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
						{
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);

							/*String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionId,
									processInstanceID,WorkItemID);
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);


							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");

							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);
							*/	

							//Move Workitem to next Workstep

							//if (completeWorkitemMaincode.trim().equalsIgnoreCase("0"))
							if ("0".trim().equalsIgnoreCase("0"))	
							{
								//DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WmCompleteWorkItem successful: "+assignWorkitemAttributeMainCode);
								//System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WorkItem moved to next Workstep.");

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=outputDateFormat.format(actionDateTime);
								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WINAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_DBO_WIHISTORY");
								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Completed On "+ ActivityName);


								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ApInsert successful: "+apInsertMaincode);
									DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								//completeWorkitemMaincode="";
								//DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
						else
						{
							assignWorkitemAttributeMainCode="";
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
					}
				

		}
			}
		}
			catch (Exception e)

		{
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Exception: "+e.getMessage());
		}
	}



			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DBO' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("In WF NG Execute : " + serverPort);
				try
				{
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP,
								Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort,
								"WebSphere", ipXML);
				}
				catch (Exception e)
				{
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}

}



