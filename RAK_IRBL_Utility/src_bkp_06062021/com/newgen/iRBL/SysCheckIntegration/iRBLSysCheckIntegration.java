package com.newgen.iRBL.SysCheckIntegration;


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

import com.newgen.common.CommonMethods;
import com.newgen.common.CommonConnection;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class iRBLSysCheckIntegration implements Runnable{

	static Map<String, String> iRBLStatusConfigParamMap= new HashMap<String, String>();

	public String MQ_response="";
	public String ReturnCode1="";
	public String ReturnDesc="";
	public String MainCifId="";
	public String MainCustomerSegment="";
	public String MainCustomerSubSegment="";
	public String MainCustomerName="";
	public String MainTotalRiskScore="";
	public String MainDateOfBirth="";
	public String MainNationality="";
	public String MainGender="";
	public String MainEmiratesID="";
	public String MainPassportNo="";
	public String MainResAddress="";
	public String MainMobileNo="";
	public String rowVal="";
	final String ws_name="";
	final String winame="";	

	HashMap<String, String> ExtTabDataMap = new HashMap<String, String>();
	static NGEjbClient ngEjbClientiRBLSysCheckIntegrate;
	static
	{
	  try
      {
        ngEjbClientiRBLSysCheckIntegrate = NGEjbClient.getSharedInstance();
      }
    catch (NGException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	}
	
	public void run()
	{
		String sessionID = "";
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		String UserName= "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;

		final String MQ_TABLE="NG_BPM_MQ_TABLE";
		try
		{
			iRBLSysCheckIntegrationLog.setLogger();
			ngEjbClientiRBLSysCheckIntegrate = NGEjbClient.getSharedInstance();

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.error("Could not Read Config Properties [iRBLStatus]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("JTSPORT: " + jtsPort);

			queueID = iRBLStatusConfigParamMap.get("queueID");
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("QueueID: " + queueID);
			
			
			UserName = iRBLStatusConfigParamMap.get("UserName");
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("UserName: " + UserName);

			socketConnectionTimeout=Integer.parseInt(iRBLStatusConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(iRBLStatusConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(iRBLStatusConfigParamMap.get("SleepIntervalInMin"));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID,MQ_TABLE);
				while(true)
				{
					iRBLSysCheckIntegrationLog.setLogger();
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("iRBL Status...123.");
					startiRBLSysCheckIntegrationUtility(cabinetName, UserName, jtsIP, jtsPort,sessionID,
							queueID,socketConnectionTimeout, integrationWaitTime,socketDetailsMap);
							System.out.println("No More workitems to Process, Sleeping!");
							Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.error("Exception Occurred in iRBL CBS : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.error("Exception Occurred in iRBL CBS : "+result);
		}
	}
	
	private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
			String sessionID, String MQ_TABLE)
	{
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM "+MQ_TABLE+" with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Utility'";

			String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

			String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

			if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
			{
				String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketServerIP: "+socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		}
		catch (Exception e)
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
		}

		return socketDetailsMap;
	}
	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "iRBL_SysCheckIntegration_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    iRBLStatusConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
		  e.printStackTrace();
			return -1 ;
		}
		return 0;
	}
	
	private void startiRBLSysCheckIntegrationUtility(String cabinetName,String UserName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="Sys_Checks_Integration";
		final String IRBL_WI_HISTORY="USR_0_iRBL_WIHISTORY";

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Fetching all Workitems on iRBL_SysCheckIntegration queue");
			System.out.println("Fetching all Workitems on iRBL_SysCheckIntegration queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Number of workitems retrieved on iRBL_SysCheckIntegration: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on iRBL_SysCheckIntegration: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Current ProcessInstanceID: "+processInstanceID);
										
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Current EntryDateTime: "+entryDateTime);

					//Lock Workitem.
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					ResponseBean objResponseBean;
					String IntExpoCollSumStatus = "";
					String ExtExpoStatus = "";
					String FinSumStatus = ""; 
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
					if (getWorkItemMainCode.trim().equals("0"))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
						
						objResponseBean = IntegrateDedupeBlacklistFicroRiskscore.IntegratewithMW(processInstanceID, ws_name, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
												
						IntExpoCollSumStatus = IntegrateInternalExposureCollectionsSummary.IntegratewithMW(processInstanceID,integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);					
						
						if("Success".equalsIgnoreCase(IntExpoCollSumStatus))
							ExtExpoStatus = IntegrateExternalExposure.IntegratewithMW(processInstanceID,integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);					
						
						if("Success".equalsIgnoreCase(IntExpoCollSumStatus))
							FinSumStatus = IntegrateFinancialSummary.IntegratewithMW(processInstanceID,integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);					
						
						String updateStatus = UpdateExtAndTransTableData.UpdateData(processInstanceID);
						
						
						String strIntegrationErrCode="";
						
						if(!objResponseBean.getDedupeReturnCode().equals("Failure"))
							objResponseBean.setDedupeReturnCode("Success");
												
						if(!objResponseBean.getBlackListReturnCode().equals("Failure"))
							objResponseBean.setBlackListReturnCode("Success");
												
						if(!objResponseBean.getFircosoftReturnCode().equals("Not Checked"))
							objResponseBean.setFircosoftReturnCode("Success");
						

						if("Success".equals(objResponseBean.getDedupeReturnCode()) && "Success".equals(objResponseBean.getBlackListReturnCode())
								&& "Success".equals(objResponseBean.getFircosoftReturnCode())
								&& "Success".equalsIgnoreCase(IntExpoCollSumStatus)
								&& "Success".equalsIgnoreCase(ExtExpoStatus)
								&& "Success".equalsIgnoreCase(FinSumStatus) )
						{
							objResponseBean.setIntegrationDecision("Success");
							strIntegrationErrCode="";
						}
						else
						{
							objResponseBean.setIntegrationDecision("Failure");
							strIntegrationErrCode=ws_name;
						}
						
						
						/*String RiskScore = objResponseBean.getRiskScore_Details().trim();
						if(!"".equalsIgnoreCase(RiskScore))
						{
							RiskScore = "<RISK_SCORE>"+RiskScore+"</RISK_SCORE>";
						}*/

						String strMWErrorDesc = "MessageID: "+objResponseBean.getMsgID()+", Return Code: "+objResponseBean.getIntFailedCode()+", Return Desc: "+objResponseBean.getIntFailedReason();
						String attributesTag="<DECISION>"+objResponseBean.getIntegrationDecision()+"</DECISION>"
														+ "<INTEGRATION_ERROR_RECEIVED>"+strIntegrationErrCode+"</INTEGRATION_ERROR_RECEIVED>"
														+ "<RISK_SCORE_STATUSFROMUTIL>"+objResponseBean.getRiskScoreReturnCode()+"</RISK_SCORE_STATUSFROMUTIL>"														
														+ "<FIRCOHit>"+objResponseBean.getFircoHit()+"</FIRCOHit>"
														+ "<FAILEDINTEGRATIONCALL>"+objResponseBean.getIntCallFailed()+"</FAILEDINTEGRATIONCALL>"
														+ "<MW_ERRORDESC>"+strMWErrorDesc+"</MW_ERRORDESC>";


						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.info("get Workitem call successfull for "+processInstanceID);

						String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false),
								processInstanceID,WorkItemID,attributesTag);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for assign Attribute is "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),1);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for assign Attribues is "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserAssignAtt=new XMLParser(assignWorkitemAttributeOutputXML);

						String mainCodeAssignAtt=xmlParserAssignAtt.getValueOf("MainCode");
						if("0".equals(mainCodeAssignAtt.trim()))
						{
							String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false),
									processInstanceID,WorkItemID);

							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for complete WI is "+completeWorkItemInputXML);

							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);

							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);

							if("0".equals(completeWorkitemMaincode))
							{
								//inserting into history table
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
								System.out.println(processInstanceID + " Completed Sussesfully with status "+objResponseBean.getIntegrationDecision());

								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WorkItem moved to next Workstep.");

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=outputDateFormat.format(actionDateTime);
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.

								String columnNames="WI_NAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME";
								String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ws_name+"','"
								+CommonConnection.getUsername()+"','"+objResponseBean.getIntegrationDecision()+"','"+formattedEntryDatetime+"'";

								String apInsertInputXML=CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnNames, columnValues,IRBL_WI_HISTORY);
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,CommonConnection.getJTSIP(),
										CommonConnection.getJTSPort(),1);
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("APInsertOutputXML: "+ apInsertOutputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);
								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ApInsert successful: "+apInsertMaincode);
									iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.error("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								System.out.println("Error in completeWI call for "+processInstanceID);
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.error("Error in completeWI call for "+processInstanceID);
							}
						}
						else
						{
							//System.out.println("Error in Assign Attribute call for "+processInstanceID);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.error("Error in Assign Attribute call for WI "+processInstanceID);
						}					
					}	
					else
					{
						System.out.println("WMgetWorkItemCall failed: "+processInstanceID);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WMgetWorkItemCall failed: "+processInstanceID);
					}

				}
			}
		}
		catch (Exception e)

		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception: "+e.getMessage());
		}
	}
	
	public static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
			int flag) throws IOException, Exception
	{
		//System.out.println("In WF NG Execute : " + serverPort);
		try
		{
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP,
						Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientiRBLSysCheckIntegrate.makeCall(jtsServerIP, serverPort,
						"WebSphere", ipXML);
		}
		catch (Exception e)
		{
			System.out.println("Exception Occured in WF NG Execute : "+ e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
}
