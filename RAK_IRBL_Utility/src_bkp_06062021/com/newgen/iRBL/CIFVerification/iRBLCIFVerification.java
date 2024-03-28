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


package com.newgen.iRBL.CIFVerification;

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



public class iRBLCIFVerification implements Runnable
{


	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> CIFVerConfigParamMap= new HashMap<String, String>();


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
			iRBLCIFVerificationLog.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.error("Could not Read Config Properties [RAOPStatus]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("JTSPORT: " + jtsPort);

			queueID = CIFVerConfigParamMap.get("queueID");
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(CIFVerConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(CIFVerConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(CIFVerConfigParamMap.get("SleepIntervalInMin"));
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getSessionID(iRBLCIFVerificationLog.iRBLCIFVerificationLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID);
				while(true)
				{
					iRBLCIFVerificationLog.setLogger();
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("iRBL CIF Verification...123.");
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
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.error("Exception Occurred in iRBL CIF Verification : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.error("Exception Occurred in iRBL CIF Verification : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "iRBL_CIFVerification_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    CIFVerConfigParamMap.put(name, p.getProperty(name));
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
		final String ws_name="CIF_Verification";

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(iRBLCIFVerificationLog.iRBLCIFVerificationLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Fetching all Workitems on iRBL_CIF_Verification queue");
			System.out.println("Fetching all Workitems on iRBL_CIF_Verification queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Number of workitems retrieved on iRBL_CIF_Verification: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on iRBL_CIF_Verification: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("ActivityName: "+ActivityName);
						//RAOP Integration Call
						String decisionValue="";

						iRBLCIFVerificationIntegration objIntegration= new iRBLCIFVerificationIntegration();
						String integrationStatus=objIntegration.customIntegration(cabinetName,sessionId, sJtsIp,
								iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap);

						String attributesTag;
						String ErrDesc = "CIF Verification Done Successfully";
						if ("Success".equalsIgnoreCase(integrationStatus))
						{
							decisionValue = "Success";
							iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision>" + "<IsCIFVerificationRequired>Done</IsCIFVerificationRequired>";
						}
						else
						{
							ErrDesc = integrationStatus.replace("~", ",").replace("|", "\n");
							decisionValue = "Failure";
							iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Decision" +decisionValue);
							attributesTag="<qDecision>"+decisionValue+"</qDecision><FAILEDINTEGRATIONCALL>CIF Verification</FAILEDINTEGRATIONCALL>" + "<MW_ERRORDESC>"
							+ErrDesc+ "</MW_ERRORDESC>" ;

						}


						//To be modified according to output of Integration Call.

					//Lock Workitem.
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,
								processInstanceID,WorkItemID,attributesTag);
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);

						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
						{
							iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);

							String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionId,
									processInstanceID,WorkItemID);
							iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
							iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);


							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");

							iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);


							//Move Workitem to next Workstep

							if (completeWorkitemMaincode.trim().equalsIgnoreCase("0"))
							{
								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
								System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WorkItem moved to next Workstep.");

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=outputDateFormat.format(actionDateTime);
								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WI_NAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_iRBL_WIHISTORY");
								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Completed On "+ ActivityName);


								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("ApInsert successful: "+apInsertMaincode);
									iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								completeWorkitemMaincode="";
								iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
						else
						{
							assignWorkitemAttributeMainCode="";
							iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
					}
				

		}
			}
		}
			catch (Exception e)

		{
			iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Exception: "+e.getMessage());
		}
	}



			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("In WF NG Execute : " + serverPort);
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
					iRBLCIFVerificationLog.iRBLCIFVerificationLogger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}

}



