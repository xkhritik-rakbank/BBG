/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects 2
Project/Product			: RAK iRBL
Application				: RAK iRBL Utility
Module					: iRBL BAIS Hold queue
File Name				: RAOPStatus.java
Author 					: Angad Shah
Date (DD/MM/YYYY)		: 23/06/2021

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.iRBLHoldInBAISProcess;

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
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;



public class iRBLHoldInBAISProcess implements Runnable
{


	private static NGEjbClient ngEjbClientiRBLHoldInBAISProcess;
	static Map<String, String> iRBLHoldInBAISProcessConfigParamMap= new HashMap<String, String>();


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
			iRBLHoldInBAISProcessLog.setLogger();
			ngEjbClientiRBLHoldInBAISProcess = NGEjbClient.getSharedInstance();

			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.error("Could not Read Config Properties [IRBLStatus]");
				return;
			}

			cabinetName = CommonConnection.getOFCabinetName();
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getOFJTSIP();
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getOFJTSPort();
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("JTSPORT: " + jtsPort);

			queueID = iRBLHoldInBAISProcessConfigParamMap.get("iRBLHoldQueueIDInBAIS");
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("iRBLHoldQueueIDInBAIS: " + queueID);

			sleepIntervalInMin=Integer.parseInt(iRBLHoldInBAISProcessConfigParamMap.get("SleepIntervalInMin"));
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getOFSessionID(iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Session ID found: " + sessionID);
				while(true)
				{
					iRBLHoldInBAISProcessLog.setLogger();
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("IRBL Status...123.");
					startiRBLHoldInBAISProcessUtility(cabinetName, jtsIP, jtsPort,sessionID,
							queueID,socketConnectionTimeout, integrationWaitTime);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.error("Exception Occurred in IRBL CBS : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.error("Exception Occurred in iRBL CBS : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "BAIS_iRBLHoldInBAISProcess_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    iRBLHoldInBAISProcessConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startiRBLHoldInBAISProcessUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime)
	{
		String ws_name="";

		try
		{
			//Validate Session ID
			sessionId = CommonConnection.getOFSessionID(iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Fetching all Workitems on Sys_iRBL_Hold queue in BAIS Process");
			System.out.println("Fetching all Workitems on Sys_iRBL_Hold queue in BAIS Process");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Number of workitems retrieved on Sys_iRBL_Hold queue in BAIS Process : "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on Sys_iRBL_Hold queue in BAIS Process : "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Current EntryDateTime: "+entryDateTime);

					ws_name=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Current ws_name: "+ws_name);		

				
					String decisionValue="";

					iRBLHoldInBAISProcessIntegration objIntegration= new iRBLHoldInBAISProcessIntegration();
					String integrationStatus=objIntegration.customIntegration(cabinetName,sessionId, sJtsIp,
							iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut, iRBLHoldInBAISProcessConfigParamMap);

					String attributesTag;

					if ("N".equalsIgnoreCase(integrationStatus))
					{
						decisionValue = "Success";
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("ExceptionStatus" +integrationStatus);
						attributesTag="<U_UID>"+integrationStatus+"</U_UID>";
						String DBQuery="select a.DOMESTIC_CURR,a.Compliance_Decision,a.FCYBlockText from (select DOMESTIC_CURR ,(select top 1 decision from USR_0_IRBL_WIHISTORY with(nolock) where WORKSTEP='Compliance_EDD' and WI_NAME=(Select WINAME from RB_iRBL_EXTTABLE with(nolock) where BPM_REF_BAIS='"+processInstanceID+"' ) order by ACTION_DATE_TIME desc) as Compliance_Decision, "
								+ "(select CONST_FIELD_VALUE from USR_0_BPM_CONSTANTS with(nolock) where CONST_FIELD_NAME='BAIS_DefaultTextForDomesticCurrencyOnlyOrNoFCY') as FCYBlockText FROM RB_iRBL_EXTTABLE WITH(nolock)"
								+ "WHERE BPM_REF_BAIS ='"+processInstanceID+"') a";
						String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger, false));
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("BAIS WI update data input: "+ extTabDataIPXML);
						String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("BAIS WI update data output: "+ extTabDataOPXML);

						XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
						int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

						
						if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0"))
						{

							String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
							xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
							
							//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
							NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
						
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{	
								if("Yes".equalsIgnoreCase(objWorkList.getVal("DOMESTIC_CURR")) && ("Approve".equalsIgnoreCase(objWorkList.getVal("Compliance_Decision")) || "Approve with CUSTOPREM BLOCK (NO FCY)".equalsIgnoreCase(objWorkList.getVal("Compliance_Decision"))) )
								{
									attributesTag+="<DomesticCurrencyOnlyOrNoFCY>"+objWorkList.getVal("FCYBlockText")+"</DomesticCurrencyOnlyOrNoFCY>";
								}
								else
								{
									attributesTag+="<DomesticCurrencyOnlyOrNoFCY></DomesticCurrencyOnlyOrNoFCY>";
								}
							}
						}
						//To be modified according to output of Integration Call.

					//Lock Workitem.
						String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
						String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);
	
						XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
						String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
	
						if (getWorkItemMainCode.trim().equals("0"))
						{
							iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
							
							
							String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,
									processInstanceID,WorkItemID,attributesTag);
							iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);
	
							String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
									iJtsPort,1);
	
							iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);
	
							XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
							String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
							iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);
	
							if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
							{
								iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
	
								String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionId,
										processInstanceID,WorkItemID);
								iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);
	
								String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
								iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);
	
	
								XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
								String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
	
								iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);
	
	
								//Move Workitem to next Workstep
	
								if (completeWorkitemMaincode.trim().equalsIgnoreCase("0"))
								{
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
									System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
	
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("WorkItem moved to next Workstep.");
	
									SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
									SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
	
									Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
									String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
	
									Date actionDateTime= new Date();
									String formattedActionDateTime=outputDateFormat.format(actionDateTime);
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);
	
									//Insert in WIHistory Table.
									String colNames = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
									
									String colValues = "'"+ processInstanceID + "','"+ws_name+"','SUBMIT','" + formattedActionDateTime + "','Exception cleared for iRBL workitem','System','" + formattedEntryDatetime + "'";
									
									String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, colNames, colValues,"USR_0_BAIS_WIHISTORY");
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("APInsertInputXML: "+apInsertInputXML);
	
									String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("APInsertOutputXML: "+ apInsertOutputXML);
									
									XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
									String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);
	
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Completed On "+ ws_name);
	
	
									if(apInsertMaincode.equalsIgnoreCase("0"))
									{
										iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("ApInsert successful: "+apInsertMaincode);
										iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Inserted in WiHistory table successfully.");
									}
									else
									{
										iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("ApInsert failed: "+apInsertMaincode);
									}
								}
								else
								{
									completeWorkitemMaincode="";
									iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
								}
							}
							else
							{
								assignWorkitemAttributeMainCode="";
								iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
							}
						}
						else
						{
							getWorkItemMainCode="";
							iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
						}
				
					}
					else
					{
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Exception not yet cleared ExceptionStatus" +integrationStatus);
						// nothing to do
					}	

		}
			}
		}
			catch (Exception e)

		{
			iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Exception: "+e.getMessage());
		}
	}



			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("In WF NG Execute : " + serverPort);
				try
				{
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP,
								Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientiRBLHoldInBAISProcess.makeCall(jtsServerIP, serverPort,
								"WebSphere", ipXML);
				}
				catch (Exception e)
				{
					iRBLHoldInBAISProcessLog.iRBLHoldInBAISProcessLogger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}

}



