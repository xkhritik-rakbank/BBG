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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.iRBL.CIFVerification.iRBLCIFVerificationLog;
import com.newgen.iRBL.SysCheckIntegration.iRBLSysCheckIntegration;
import com.newgen.iRBL.SysCheckIntegration.iRBLSysCheckIntegrationLog;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;



public class iRBLFircoHold implements Runnable
{


	private static NGEjbClient ngEjbClientIRBLFircoHold;

	static Map<String, String> irblFircoHoldConfigParamMap= new HashMap<String, String>();


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
			ngEjbClientIRBLFircoHold = NGEjbClient.getSharedInstance();

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLFircoHoldLog.iRBLFircoHoldLogger.error("Could not Read Config Properties [iRBLFircoHold]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("JTSPORT: " + jtsPort);

			queueID = irblFircoHoldConfigParamMap.get("queueID");
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(irblFircoHoldConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(irblFircoHoldConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(irblFircoHoldConfigParamMap.get("SleepIntervalInMin"));
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
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("iRBL FircoHold...123.");
					startIRBLFircoHoldUtility(cabinetName, jtsIP, jtsPort,sessionID,
							queueID,socketConnectionTimeout, integrationWaitTime,socketDetailsMap);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			iRBLFircoHoldLog.iRBLFircoHoldLogger.error("Exception Occurred in iRBL FircoHold : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLFircoHoldLog.iRBLFircoHoldLogger.error("Exception Occurred in iRBL FircoHold : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "iRBL_FircoHold_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    irblFircoHoldConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startIRBLFircoHoldUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="iRBL_Firco_Clearance";
		final String NG_RLOS_FIRCO="NG_RLOS_FIRCO";
		final String IRBL_FIRCO_GRID_DTLS="USR_0_IRBL_FIRCO_GRID_DTLS";

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
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Fetching all Workitems on Firco_Clearance queue");
			System.out.println("Fetching all Workitems on Firco_Clearance queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= CommonMethods.WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Number of workitems retrieved on Firco_Clearance: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on Firco_Clearance: "+fetchWorkitemListCount);

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

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("ActivityName: "+ActivityName);
					
					//Getting PreFix along with ending digits of winame
					/*if(!processInstanceID.equalsIgnoreCase(""))
					{
						processInstanceID = processInstanceID.split("-")[0]+"-"+processInstanceID.split("-")[1].replaceFirst("^0+(?!$)", "");
					}*/

					String DBQuery = "SELECT Firco_ID,Workstep_name,Call_type,StatusBehavior,StatusName,AlertDetails" +
							" FROM "+NG_RLOS_FIRCO+" WITH (NOLOCK) WHERE Workitem_no = '"+processInstanceID+"'";			
						
						String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLFircoHoldLog.iRBLFircoHoldLogger, false));
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug(" NG_RLOS_FIRCO IPXML: "+ extTabDataIPXML);
						String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
						iRBLFircoHoldLog.iRBLFircoHoldLogger.debug(" NG_RLOS_FIRCO OPXML: "+ extTabDataOPXML);

						XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
						int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
						boolean LoopBreak=false;
						String decisionValue="";
						
						if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
						{
							HashMap<String,String> FircoDataMap = new HashMap<String,String>();
							//String StatusBehavior="";
							String StatusName="";
							int j=1;
							NGXmlList objWorkList=xmlParserData.createList("Records", "Record");										
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{	
								FircoDataMap.put("Firco_ID-"+Integer.toString(j),objWorkList.getVal("Firco_ID"));
								FircoDataMap.put("Workstep_name-"+Integer.toString(j),objWorkList.getVal("Workstep_name"));
								FircoDataMap.put("Call_type-"+Integer.toString(j),objWorkList.getVal("Call_type"));
								FircoDataMap.put("StatusBehavior-"+Integer.toString(j),objWorkList.getVal("StatusBehavior"));
								FircoDataMap.put("StatusName-"+Integer.toString(j),objWorkList.getVal("StatusName"));
								FircoDataMap.put("AlertDetails-"+Integer.toString(j),objWorkList.getVal("AlertDetails"));
								//StatusBehavior = objWorkList.getVal("StatusBehavior");
								StatusName = objWorkList.getVal("StatusName");
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("StatusName : " +StatusName);
								if(!StatusName.equalsIgnoreCase(""))
								{
									if(!StatusName.equals("Approved"))
									{
										iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Decision Reject : " +StatusName);
										decisionValue="Reject";
									}
								}
								else //FircoStatus not updated
								{
									LoopBreak=true;
									break;
								}
								
								iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Decision : " +decisionValue);
								j++;
							}
							
							//Firco Hold								
							if(!LoopBreak)
							{
								//All Firco Hits are cleared
								if(!decisionValue.equals("Reject"))
								{
									decisionValue="Approve";
									FircoDataMap.put("Firco_ID-"+Integer.toString(j),objWorkList.getVal("Firco_ID"));
									
									//Updating records in IRBL Firco_DTLS
									String columnNames="MATCH_STATUS";
									String columnValues="False";
									String sWhereClause="WI_NAME = '"+processInstanceID+"'";
									
									extTabDataIPXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLFircoHoldLog.iRBLFircoHoldLogger, false), IRBL_FIRCO_GRID_DTLS, columnNames, columnValues, sWhereClause);
									iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Input XML for apUpdateInput for "+IRBL_FIRCO_GRID_DTLS+" Table : "+extTabDataIPXML);

									extTabDataOPXML=CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
									iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Output XML for apUpdateInput for "+IRBL_FIRCO_GRID_DTLS+" Table : "+extTabDataOPXML);

									XMLParser sXMLParserChild= new XMLParser(extTabDataOPXML);
								    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
								    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("StrMainCode: "+StrMainCode);

								    if (StrMainCode.equals("0"))
									{
								    	iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Successful in apUpdateInput the record in : "+IRBL_FIRCO_GRID_DTLS);
								    	
									}
								    else
								    {
								    	iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Error in Executing apUpdateInput sOutputXML : "+extTabDataOPXML);
								    	
								    }
									
								}
								else //Atleaset one Firco Hit is not cleared
								{
									for (String name: FircoDataMap.keySet()){
							            String key = name.toString();
							            String keyvalue = FircoDataMap.get(name).toString();  
							            iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("FircoDataMap : "+key + " " + keyvalue);  
									}
									
									//FircoDataMap.get("Firco_ID-"+Integer.toString(j),objWorkList.getVal("Firco_ID"));
									
								}
								
								String ErrDesc = "";
								String attributesTag;
								
								if (decisionValue.equals("Approve"))
								{
									//decisionValue = "Success";
									iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Decision" +decisionValue);
									 attributesTag="<qDecision>"+decisionValue+"</qDecision>" + "<INTEGRATION_ERROR_RECEIVED>" + "" + "</INTEGRATION_ERROR_RECEIVED>";
								}
								else
								{
									ErrDesc = "Firco Hits are not cleared";
									//decisionValue = "Failure";
									iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Decision" +decisionValue);
									attributesTag="<qDecision>"+decisionValue+"</qDecision>" + "<INTEGRATION_ERROR_RECEIVED>" +  ws_name + "</INTEGRATION_ERROR_RECEIVED><MW_ERRORDESC>"
									+ErrDesc+ "</MW_ERRORDESC>" ;

								}
								
								//To be modified according to output of Integration Call.

								//Lock Workitem.
								String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
								String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
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

									String assignWorkitemAttributeOutputXML=CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
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

										String completeWorkItemOutputXML = CommonMethods.WFNGExecute(completeWorkItemInputXML,sJtsIp,iJtsPort,1);
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
											String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ws_name+"'," +
													"'System','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

											String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_IRBL_WIHISTORY");
											iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("APInsertInputXML: "+apInsertInputXML);

											String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
											iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

											XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
											String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
											iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

											//iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Completed On "+ CurrWS);


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

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					iRBLFircoHoldLog.iRBLFircoHoldLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=CommonMethods.WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
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
						return ngEjbClientIRBLFircoHold.makeCall(jtsServerIP, serverPort,
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



