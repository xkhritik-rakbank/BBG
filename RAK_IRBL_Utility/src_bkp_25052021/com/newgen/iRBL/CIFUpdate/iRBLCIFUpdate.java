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


package com.newgen.iRBL.CIFUpdate;

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



public class iRBLCIFUpdate implements Runnable
{


	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> CIFUpdateConfigParamMap= new HashMap<String, String>();


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
			iRBLCIFUpdateLog.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.error("Could not Read Config Properties [RAOPStatus]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("JTSPORT: " + jtsPort);

			queueID = CIFUpdateConfigParamMap.get("queueID");
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(CIFUpdateConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(CIFUpdateConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(CIFUpdateConfigParamMap.get("SleepIntervalInMin"));
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getSessionID(iRBLCIFUpdateLog.iRBLCIFUpdateLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID);
				while(true)
				{
					iRBLCIFUpdateLog.setLogger();
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("iRBL CIF Verification...123.");
					startIRBLCIFVerUtility(cabinetName, jtsIP, jtsPort,sessionID,
							queueID,socketConnectionTimeout, integrationWaitTime,socketDetailsMap);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.error("Exception Occurred in iRBL CIF Verification : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.error("Exception Occurred in iRBL CIF Verification : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "iRBL_CIFUpdate_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    CIFUpdateConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startIRBLCIFVerUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="CIF_Update_Initial";

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(iRBLCIFUpdateLog.iRBLCIFUpdateLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Fetching all Workitems on CIF_Update_Initial queue");
			System.out.println("Fetching all Workitems on CIF_Update_Initial queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Number of workitems retrieved on CIF_Update_Initial: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on CIF_Update_Initial: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("ActivityName: "+ActivityName);
						//RAOP Integration Call
						String decisionValue="";

						iRBLCIFUpdateIntegration objIntegration= new iRBLCIFUpdateIntegration();
						String integrationStatus=objIntegration.customIntegration(cabinetName,sessionId, sJtsIp,
								iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap);

						String attributesTag;
						String ErrDesc = "CIF Update Done Successfully";
						if ("Success".equalsIgnoreCase(integrationStatus))
						{
							decisionValue = "Success";
							iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision>" + "<CIFUpdate>Done</CIFUpdate>";
						}
						else
						{
							ErrDesc = integrationStatus.replace("~", ",").replace("|", "\n");
							decisionValue = "Failure";
							iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision><FAILEDINTEGRATIONCALL>CIF Update</FAILEDINTEGRATIONCALL>" + "<MW_ERRORDESC>"
							+ErrDesc+ "</MW_ERRORDESC>" ;

						}


						//To be modified according to output of Integration Call.

					//Lock Workitem.
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,
								processInstanceID,WorkItemID,attributesTag);
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);

						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
						{
							iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);

							String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionId,
									processInstanceID,WorkItemID);
							iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
							iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);


							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");

							iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);


							//Move Workitem to next Workstep

							if (completeWorkitemMaincode.trim().equalsIgnoreCase("0"))
							{
								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
								System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WorkItem moved to next Workstep.");

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=outputDateFormat.format(actionDateTime);
								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WI_NAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_iRBL_WIHISTORY");
								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Completed On "+ ActivityName);


								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("ApInsert successful: "+apInsertMaincode);
									iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								completeWorkitemMaincode="";
								iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
						else
						{
							assignWorkitemAttributeMainCode="";
							iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
					}
				

		}
			}
		}
			catch (Exception e)

		{
			iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Exception: "+e.getMessage());
		}
	}



			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("In WF NG Execute : " + serverPort);
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
					iRBLCIFUpdateLog.iRBLCIFUpdateLogger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}

}



