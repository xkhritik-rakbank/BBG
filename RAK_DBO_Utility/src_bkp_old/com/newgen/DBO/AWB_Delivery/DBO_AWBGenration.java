package com.newgen.DBO.AWB_Delivery;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.DBO.AttachDocument.AttachDocLogs;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;

public class DBO_AWBGenration implements Runnable  {

	private static NGEjbClient ngEjbClientAWBGen;

	static Map<String, String> AWB_GEN_MAP= new HashMap<String, String>();


	private static int socketConnectionTimeout=0;
	private static int integrationWaitTime=0;
	private static int sleepIntervalInMin=0;
	private static String  sessionID = "";
	private static String cabinetName = "";
	private static String jtsIP = "";
	private static String jtsPort = "";
	private static String smsPort = "";
	private static String VolumeID="";
	private static String activityName = "Sys_AWB_Generation";
	private static String AWBDataTableName="USR_0_DBO_AWB_Status";
	private static HashMap<String, String> RelatedPartyData = new HashMap<String, String>();
	private static String generatedPDFPath="";
	private static String SinglePagerDocType="";
	
	@Override
	public void run()
	{
		
		String queueID = "";

		try
		{
			DBO_AWB_Logs.setLogger();
			ngEjbClientAWBGen = NGEjbClient.getSharedInstance();
			
			DBO_AWB_Logs.DBO_AWBLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DBO_AWB_Logs.DBO_AWBLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DBO_AWB_Logs.DBO_AWBLogger.error("Could not Read Config Properties [DAO_AWB_Gen]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DBO_AWB_Logs.DBO_AWBLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DBO_AWB_Logs.DBO_AWBLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DBO_AWB_Logs.DBO_AWBLogger.debug("JTSPORT: " + jtsPort);
			
			smsPort = CommonConnection.getsSMSPort();
			DBO_AWB_Logs.DBO_AWBLogger.debug("SMSPort: " + smsPort);
			
			VolumeID = AWB_GEN_MAP.get("VolumeID");
			DBO_AWB_Logs.DBO_AWBLogger.debug("VolumeID: " + VolumeID);
			
			queueID = AWB_GEN_MAP.get("queueID");
			DBO_AWB_Logs.DBO_AWBLogger.debug("QueueID: " + queueID);
			
			SinglePagerDocType = AWB_GEN_MAP.get("SinglePagerDocType");
			DBO_AWB_Logs.DBO_AWBLogger.debug("SinglePagerDocType: " + SinglePagerDocType);
			
			socketConnectionTimeout=Integer.parseInt(AWB_GEN_MAP.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DBO_AWB_Logs.DBO_AWBLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(AWB_GEN_MAP.get("INTEGRATION_WAIT_TIME"));
			DBO_AWB_Logs.DBO_AWBLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(AWB_GEN_MAP.get("SleepIntervalInMin"));
			DBO_AWB_Logs.DBO_AWBLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			
			sessionID = CommonConnection.getSessionID(DBO_AWB_Logs.DBO_AWBLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DBO_AWB_Logs.DBO_AWBLogger.debug("Could Not Connect to Server!");
				
			}
			else
			{
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) 
				{
					DBO_AWB_Logs.DBO_AWBLogger.debug("Session ID found: " + sessionID);
					DBO_AWB_Logs.setLogger();
					DBO_AWB_Logs.DBO_AWBLogger.debug("AWB_Gen_file...123.");
					startDBO_AWB_Gen(queueID,socketDetailsMap);
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			DBO_AWB_Logs.DBO_AWBLogger.error("Exception Occurred in AWB_Gen_file : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DBO_AWB_Logs.DBO_AWBLogger.error("Exception Occurred in AWB_Gen_file : "+result);
		}
	}

	private static void startDBO_AWB_Gen( String queueID,HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="Sys_AWB_Generation";
		
		
		try
		{
			//Validate Session ID
			sessionID  = CommonConnection.getSessionID(DBO_AWB_Logs.DBO_AWBLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DBO_AWB_Logs.DBO_AWBLogger.error("Could Not Get Session ID "+sessionID);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DBO_AWB_Logs.DBO_AWBLogger.debug("Fetching all Workitems for AWB DBO ");
			System.out.println("Fetching all Workitems on queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DBO_AWB_Logs.DBO_AWBLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,jtsIP,jtsPort,1);

			DBO_AWB_Logs.DBO_AWBLogger.debug("WMFetchWorkList DAO_AWB_Gen OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DBO_AWB_Logs.DBO_AWBLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DBO_AWB_Logs.DBO_AWBLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DBO_AWB_Logs.DBO_AWBLogger.debug("Number of workitems retrieved on DAO_AWB_Gen: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DAO_AWB_Gen: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					
					
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

 					DBO_AWB_Logs.DBO_AWBLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DBO_AWB_Logs.DBO_AWBLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					DBO_AWB_Logs.DBO_AWBLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DBO_AWB_Logs.DBO_AWBLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DBO_AWB_Logs.DBO_AWBLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DBO_AWB_Logs.DBO_AWBLogger.debug("ActivityName: "+ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DBO_AWB_Logs.DBO_AWBLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DBO_AWB_Logs.DBO_AWBLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DBO_AWB_Logs.DBO_AWBLogger.debug("ProcessDefId: "+ProcessDefId);
					
					String DBQuery1="select e.WINAME,e.itemindex,e.CompanyShortName,e.ProspectID,e.ProductType,a.RelatedPartyID,a.AWB_Number,a.AWB_Status,e.ApplicationType,r.FullName,r.EmirateID,"+
					"r.PassportNumber,r.IsSignatory,r.IsShareholder,concat(isnull(r.AddrLine1,''),' ',isnull(r.AddrLine2,''),' ',isnull(r.AddrLine3,''),' ',isnull(r.AddrLine4,'')) as Address,"+
					"r.AddrCity,(select countryName from USR_0_DBO_CountryMaster with(nolock) where countryCode=r.AddrCountry) as AddressCountry,"+
					"concat( r.MobNoCountryCode,r.MobNumber) as MobNo,(select countryName from USR_0_DBO_CountryMaster where countryCode=r.Nationality) as Nationality "+
					" from USR_0_DBO_AWB_Status a with(nolock),RB_DBO_EXTTABLE e with(nolock),"+
					"USR_0_DBO_RelatedPartyGrid r with(nolock) where a.WI_name=e.WINAME and a.RelatedPartyID=r.RelatedPartyID "+
					"and e.WINAME='" + processInstanceID + "' and r.WINAME='" + processInstanceID + "' and a.WI_name='" + processInstanceID + "' "+
					"and (a.AWB_Status != 'Generated' or a.AWB_Status is null or a.SinglePagerPath is null or a.SinglePagerPath = '')";
					String extTabDataIPXML1 =CommonMethods.apSelectWithColumnNames(DBQuery1,cabinetName,sessionID);
					DBO_AWB_Logs.DBO_AWBLogger.debug("extTabDataIPXML1: " + extTabDataIPXML1);
					String extTabDataOPXML1 = WFNGExecute(extTabDataIPXML1, jtsIP, jtsPort, 1);
					DBO_AWB_Logs.DBO_AWBLogger.debug("extTabDataOPXML1: " + extTabDataOPXML1);
					// using xml parser to pass the output data in desired format 
					XMLParser xmlParserData1 = new XMLParser(extTabDataOPXML1);
					String mainCode = xmlParserData1.getValueOf("MainCode");
					if("0".equalsIgnoreCase(mainCode))
					{
						String recRetrived = xmlParserData1.getValueOf("TotalRetrieved");
						int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null)?Integer.parseInt(recRetrived):0;
						if(noOfRecords>0)
						{
							NGXmlList objWorkList = xmlParserData1.createList("Records", "Record");
							String AWBgenStatus="Success";
							String decision="";
							String Remarks="";
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{
								RelatedPartyData.put("WINAME",validateVariableData(objWorkList.getVal("WINAME")).trim());
								RelatedPartyData.put("PARENTFOLDERINDEX",validateVariableData(objWorkList.getVal("itemindex")).trim());
								RelatedPartyData.put("RelatedPartyID",validateVariableData(objWorkList.getVal("RelatedPartyID")).trim());
								RelatedPartyData.put("ProspectID",validateVariableData(objWorkList.getVal("ProspectID")).trim());
								RelatedPartyData.put("CompanyShortName",validateVariableData(objWorkList.getVal("CompanyShortName")).trim());
								RelatedPartyData.put("AWB_Status",validateVariableData(objWorkList.getVal("AWB_Status")).trim());
								RelatedPartyData.put("AWBNumber",validateVariableData(objWorkList.getVal("AWB_Number")).trim());
								RelatedPartyData.put("ApplicationType",validateVariableData(objWorkList.getVal("ApplicationType")).trim());
								RelatedPartyData.put("FullName",validateVariableData(objWorkList.getVal("FullName")).trim());
								RelatedPartyData.put("EmirateID",validateVariableData(objWorkList.getVal("EmirateID")).trim());
								RelatedPartyData.put("Address",validateVariableData(objWorkList.getVal("Address")).trim());
								RelatedPartyData.put("AddrCity",validateVariableData(objWorkList.getVal("AddrCity")).trim());
								RelatedPartyData.put("AddressCountry",validateVariableData(objWorkList.getVal("AddressCountry")).trim());
								RelatedPartyData.put("MobNo",validateVariableData(objWorkList.getVal("MobNo")).trim());
								RelatedPartyData.put("Nationality",validateVariableData(objWorkList.getVal("Nationality")).trim());
								RelatedPartyData.put("AddressCountry",validateVariableData(objWorkList.getVal("AddressCountry")).trim());
								RelatedPartyData.put("ProductType",validateVariableData(objWorkList.getVal("ProductType")).trim());
								RelatedPartyData.put("IsSignatory",validateVariableData(objWorkList.getVal("IsSignatory")).trim());
								RelatedPartyData.put("IsShareholder",validateVariableData(objWorkList.getVal("IsShareholder")).trim());
								RelatedPartyData.put("PassportNumber",validateVariableData(objWorkList.getVal("PassportNumber")).trim());
								
								if(!"".equalsIgnoreCase(RelatedPartyData.get("RelatedPartyID")) && !"Generated".equalsIgnoreCase(RelatedPartyData.get("AWB_Status")))
								{
									AWBgenStatus=generateAWB(processInstanceID,RelatedPartyData.get("RelatedPartyID"),socketDetailsMap);
								}
								if(!"Success".equalsIgnoreCase(AWBgenStatus))
								{
									decision = "Failure" ;
									Remarks = "Error in AWB Number Generation";
									DoneWI( cabinetName, sessionID, processInstanceID, WorkItemID, decision, Remarks,ActivityID,ActivityType,entryDateTime);
									break;
								}
								AWBgenStatus=generateSinglePager(RelatedPartyData.get("ApplicationType"),processInstanceID);
								if(!"Success".equalsIgnoreCase(AWBgenStatus))
								{
									decision = "Failure" ;
									Remarks = "Error in Single Pager Generation";
									DoneWI( cabinetName, sessionID, processInstanceID, WorkItemID, decision, Remarks,ActivityID,ActivityType,entryDateTime);
									break;
								}
								RelatedPartyData.clear();
							}
							if("Success".equalsIgnoreCase(AWBgenStatus))
							{
								String AWBTableColumnToBeUpdated="AWB_Status";
								String AWBTableValues="'R'";
								String where="WI_name='"+processInstanceID+"'";
								String retValue=updateTableData(AWBDataTableName,AWBTableColumnToBeUpdated,AWBTableValues,where);
								if("Success".equalsIgnoreCase(retValue))
								{
									decision = "Success" ;
									Remarks = "AWB and Single Pager Generated Successfully";
									DoneWI( cabinetName, sessionID, processInstanceID, WorkItemID, decision, Remarks,ActivityID,ActivityType,entryDateTime);
								}
								else
								{
									DBO_AWB_Logs.DBO_AWBLogger.debug("Error in AWB data Update: "+retValue);
								}
							}
						}
					}
				 }	
				}
		}
		catch (Exception e)
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug("Exception: "+e.getMessage());
		}
	}
	private static String generateAWB (String WINAME,String RPID,HashMap<String, String> socketDetailsMap)
	{
		String retValue="";
		try
		{
			String AWBInputXML="<EE_EAI_MESSAGE>"+
					   "<EE_EAI_HEADER>"+
					      "<MsgFormat>AWB_GENERATION</MsgFormat>"+
					      "<MsgVersion>0001</MsgVersion>"+
					      "<RequestorChannelId>CAS</RequestorChannelId>"+
					      "<RequestorUserId>RAKUSER</RequestorUserId>"+
					      "<RequestorLanguage>E</RequestorLanguage>"+
					      "<RequestorSecurityInfo>secure</RequestorSecurityInfo>"+
					      "<ReturnCode>0000</ReturnCode>"+
					      "<ReturnDesc>REQ</ReturnDesc>"+
					      "<MessageId>CA179811c23</MessageId>"+
					      "<Extra1>REQ||BPM.123</Extra1>"+
					      "<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>"+
					   "</EE_EAI_HEADER>"+
					   "<AWBGenerationRequest>"+
					      "<ToCompany>"+RelatedPartyData.get("CompanyShortName")+"</ToCompany>"+
					      "<ToAddress>"+RelatedPartyData.get("Address")+"</ToAddress>"+
					      "<ToLocation>"+RelatedPartyData.get("AddrCity")+"</ToLocation>"+
					      "<ToCountry>"+RelatedPartyData.get("AddressCountry")+"</ToCountry>"+
					      "<ToCperson>"+RelatedPartyData.get("FullName")+"</ToCperson>"+
					      "<ToContactno>"+RelatedPartyData.get("MobNo")+"</ToContactno>"+
					      "<ToMobileno>"+RelatedPartyData.get("MobNo")+"</ToMobileno>"+
					      "<ReferenceNumber>"+WINAME+"</ReferenceNumber>"+
					      "<Weight>1.0</Weight>"+
					      "<Pieces>1</Pieces>"+
					      "<PackageType>Document</PackageType>"+
					      "<CurrencyCode>AED</CurrencyCode>"+
					      "<NcndAmount>1.0</NcndAmount>"+
					      "<ItemDescription></ItemDescription>"+
					      "<SpecialInstruction></SpecialInstruction>"+
					      "<ProductType>0</ProductType>"+
					      "<BranchName>Dubai</BranchName>"+
					   "</AWBGenerationRequest>"+
					"</EE_EAI_MESSAGE>";
			String integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, jtsIP, jtsPort, WINAME, activityName, socketConnectionTimeout, integrationWaitTime,socketDetailsMap, AWBInputXML);
			XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
			DBO_AWB_Logs.DBO_AWBLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
			String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
			DBO_AWB_Logs.DBO_AWBLogger.debug("Return Code: "+return_code+ "WI: "+WINAME);
			String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			DBO_AWB_Logs.DBO_AWBLogger.debug("return_desc : "+return_desc+ "WI: "+WINAME);
			
			String MsgId ="";
			if (integrationStatus.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");

			DBO_AWB_Logs.DBO_AWBLogger.debug("MsgId : "+MsgId+" AWB for WI: "+WINAME);

			if("0000".equalsIgnoreCase(return_code))
			{
				retValue="Success";
				
				String AWB_No = xmlParserSocketDetails.getValueOf("AWBNumber");
				String AWB_pdf = xmlParserSocketDetails.getValueOf("AWBPdf");
				String AWb_status = "Generated";
				RelatedPartyData.put("AWBNumber",AWB_No);
				String AWBTableColumnToBeUpdated="AWB_Number,AWB_Status,AWB_Gen_success_date";
				String AWBTableValues="'"+AWB_No+"','"+AWb_status+"',getDate()";
				String where="WI_name='"+WINAME+"' and RelatedPartyID='"+RPID+"'";
				String whereRPGrid="WINAME='"+WINAME+"' and RelatedPartyID='"+RPID+"'";
				retValue=updateTableData(AWBDataTableName,AWBTableColumnToBeUpdated,AWBTableValues,where);
				retValue=updateTableData("USR_0_DBO_RelatedPartyGrid","AWBGenerationStatus","'Done'",whereRPGrid);
			}
			else
			{
				String AWb_status = "AWBGenFailed";
				
				String AWBTableColumnToBeUpdated="AWB_Status";
				String AWBTableValues="'"+AWb_status+"'";
				String where="WI_name='"+WINAME+"' and RelatedPartyID='"+RPID+"'";
				updateTableData(AWBDataTableName,AWBTableColumnToBeUpdated,AWBTableValues,where);
				String whereRPGrid="WINAME='"+WINAME+"' and RelatedPartyID='"+RPID+"'";
				retValue=updateTableData("USR_0_DBO_RelatedPartyGrid","AWBGenerationStatus","'Error'",whereRPGrid);
				retValue="Error";
			}
			/*String EmployerName="",Address="",ProspectID="",StreetLocation="",AddressCountry="",FullName="",MobNumber="",EmiratesID="",Nationality="";
			String QueryToGetWIData = "select e.WINAME,e.CompanyName,e.ProspectID,r.FullName,r.EmirateID ,concat(isnull(r.AddrLine1,''),' ',isnull(r.AddrLine2,''),' ',isnull(r.AddrLine3,''),' ',isnull(r.AddrLine4,'')) as Address ,r.AddrCity,(select countryName from USR_0_DBO_CountryMaster with(nolock) where countryCode=r.AddrCountry) as AddressCountry, concat( r.MobNoCountryCode,r.MobNumber) as MobNo,(select countryName from USR_0_DBO_CountryMaster where countryCode=r.Nationality) as Nationality  from RB_DBO_EXTTABLE e with(nolock) , USR_0_DBO_RelatedPartyGrid r with(nolock) where e.WINAME=r.WINAME and e.WINAME='"+WINAME+"' and r.WINAME='"+WINAME+"' and r.RelatedPartyID='"+RPID+"'";
			String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(QueryToGetWIData, cabinetName, sessionID);
			
			DBO_AWB_Logs.DBO_AWBLogger.debug("Output_Query_for_ChequeBk_ref: "+extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			DBO_AWB_Logs.DBO_AWBLogger.debug(" extTabDataOPXML : CBS "+ extTabDataOPXML);
				
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			String mainCode = xmlParserData.getValueOf("MainCode");
				
			if("0".equalsIgnoreCase(mainCode))
			{
				String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
				int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null) ?Integer.parseInt(recRetrived):0;
				if(noOfRecords>0)
				{
					NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
					for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
					{
						EmployerName = validateVariableData(objWorkList.getVal("CompanyName")).trim();
						Address =validateVariableData(objWorkList.getVal("Address")).trim();
						StreetLocation =validateVariableData(objWorkList.getVal("AddrCity")).trim();
						ProspectID =validateVariableData(objWorkList.getVal("ProspectID")).trim();
						AddressCountry =validateVariableData(objWorkList.getVal("AddressCountry")).trim();
						FullName =validateVariableData(objWorkList.getVal("FullName")).trim();
						MobNumber =validateVariableData(objWorkList.getVal("MobNo")).trim();
						EmiratesID =validateVariableData(objWorkList.getVal("EmirateID")).trim();
						Nationality =validateVariableData(objWorkList.getVal("Nationality")).trim();
					}
					
					
				}
			}*/
		}
		catch(Exception e)
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug(("generateAWB exception"+e.getMessage()));
			retValue = "Error";
		}
		return retValue;
	}
	private static String DoneWI (String cabinetName,String sessionID,String processInstanceID,String WorkItemID, String decision, String Remarks,String ActivityID,String ActivityType,String entryDateTime)
	{
		String retValue="";
		try
		{
			//Lock WorkItem
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML,jtsIP,jtsPort,1);
			DBO_AWB_Logs.DBO_AWBLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);
			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			DBO_AWB_Logs.DBO_AWBLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
			if (getWorkItemMainCode.trim().equals("0"))
			{
				DBO_AWB_Logs.DBO_AWBLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

				String attributesTag = "<Decision>" + decision + "</Decision>";

				DBO_AWB_Logs.DBO_AWBLogger.info("get Workitem call successfull for "+processInstanceID);
				String completeWIFlag="D";
				DBO_AWB_Logs.DBO_AWBLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
				//Move Workitem to next Workstep 
				String completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, processInstanceID, WorkItemID,ActivityID,ActivityType, attributesTag,completeWIFlag);
				//completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, Wi_name, WorkItemID, attributesTag,completeWIFlag);
				DBO_AWB_Logs.DBO_AWBLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

				String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,jtsIP,jtsPort,1);
				DBO_AWB_Logs.DBO_AWBLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);

				XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
				String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
				DBO_AWB_Logs.DBO_AWBLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);

				if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) 
				{
					DBO_AWB_Logs.DBO_AWBLogger.debug("assignWorkitemAttributeInput successful: "+completeWorkitemMaincode);
					SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					//SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");

					Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
					String formattedEntryDatetime=inputDateformat.format(entryDatetimeFormat);
					DBO_AWB_Logs.DBO_AWBLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

					Date actionDateTime= new Date();
					String formattedActionDateTime=inputDateformat.format(actionDateTime);
					DBO_AWB_Logs.DBO_AWBLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

					//Insert in WIHistory Table.
					String columnNames="WINAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
					String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+activityName+"','"
					+CommonConnection.getUsername()+"','"+decision+"','"+formattedEntryDatetime+"','"+Remarks+"'";

					String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"USR_0_DBO_WIHISTORY"); // toDo
					DBO_AWB_Logs.DBO_AWBLogger.debug("APInsertInputXML: "+apInsertInputXML);

					String apInsertOutputXML = WFNGExecute(apInsertInputXML,jtsIP,jtsPort,1);
					DBO_AWB_Logs.DBO_AWBLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

					XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
					String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
					DBO_AWB_Logs.DBO_AWBLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

					DBO_AWB_Logs.DBO_AWBLogger.debug("Completed On "+ activityName);

					if(apInsertMaincode.equalsIgnoreCase("0"))
					{
						DBO_AWB_Logs.DBO_AWBLogger.debug("ApInsert successful: "+apInsertMaincode);
						DBO_AWB_Logs.DBO_AWBLogger.debug("Inserted in WiHistory table successfully.");
					}
					else
					{
						DBO_AWB_Logs.DBO_AWBLogger.debug("ApInsert failed: "+apInsertMaincode);
					}
				}
				else 
				{
					completeWorkitemMaincode="";
					DBO_AWB_Logs.DBO_AWBLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
				}
			
				
									
			}
		}catch(Exception e){
			DBO_AWB_Logs.DBO_AWBLogger.debug("Exception: "+e.getMessage());
		}
		return retValue;
	}
	private static String validateVariableData(String var)
	{
		return (var == null ? "":var);
	}
	private static String updateTableData(String tablename, String columnname,String values, String sWhere)
	{
		
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		String status="";
		
		DBO_AWB_Logs.DBO_AWBLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,values,sWhere,cabinetName,sessionID);
				DBO_AWB_Logs.DBO_AWBLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DBO_AWB_Logs.DBO_AWBLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!"0".equalsIgnoreCase(mainCodeforCheckUpdate)){
					
					DBO_AWB_Logs.DBO_AWBLogger.debug(("Error in executing update on "+tablename+" :maincode"+mainCodeforCheckUpdate));
					status = "Error";
				}
				else
				{
					DBO_AWB_Logs.DBO_AWBLogger.debug(("Succesfully updated "+tablename+" table"));
					return "Success";
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionID  = CommonConnection.getSessionID(DBO_AWB_Logs.DBO_AWBLogger, false);
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
				DBO_AWB_Logs.DBO_AWBLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
				status = "Error";
			}
		}
		return status;
	}
	private static String generateSinglePager(String Product,String processInstanceID)
	{
		String SingPageStatus="";
		try
		{
			DBO_GenerateAckTemplate objDocGen = new DBO_GenerateAckTemplate(DBO_AWB_Logs.DBO_AWBLogger);
			String docToBeGen = ("ISLAMIC".equalsIgnoreCase(Product) || "I".equalsIgnoreCase(Product)) ? 
			"DBO_Single_pager_declaration_form_islamic" : "DBO_Single_pager_declaration_form_conventional";
			
			String templateSrcPath = AWB_GEN_MAP.get("TemplateSourcePath");
			DBO_AWB_Logs.DBO_AWBLogger.debug("TemplateSourcePath: " + templateSrcPath);
			String singlePagerDestPath=AWB_GEN_MAP.get("GeneratedSinglePager");
			singlePagerDestPath+=File.separator+processInstanceID+File.separator+RelatedPartyData.get("AWBNumber");
			generatedPDFPath = objDocGen.generateAckTemplate(docToBeGen, templateSrcPath, singlePagerDestPath, processInstanceID, RelatedPartyData, sessionID);
			
			if(!("".equalsIgnoreCase(generatedPDFPath) || generatedPDFPath == null) && generatedPDFPath.contains(".pdf") )
			{
				String singlePagerWithQRPath = singlePagerDestPath+File.separator+"WithQR";
				String tempArr[] = generatedPDFPath.split(File.separator+File.separator);
				String singlePagerFileName = tempArr[tempArr.length-1];
				if(CopyFile(generatedPDFPath,singlePagerWithQRPath))
				{
					String qrPath = singlePagerDestPath+File.separator+"QR";
					singlePagerWithQRPath=singlePagerWithQRPath+File.separator+singlePagerFileName;
					SingPageStatus = objDocGen.generateAttachQR( singlePagerWithQRPath, qrPath, RelatedPartyData);
					if("QR_Code_GenAttach_Success".equalsIgnoreCase(SingPageStatus))
					{	
						String docAttachStatus=attachDocument(singlePagerWithQRPath,singlePagerFileName,processInstanceID);
						if("S".equalsIgnoreCase(docAttachStatus))
						{
							String AWBTableColumnToBeUpdated="SinglePagerPath";
							String AWBTableValues="'"+singlePagerFileName+"'";
							String where="WI_name='"+processInstanceID+"' and RelatedPartyID='"+RelatedPartyData.get("RelatedPartyID")+"'";
							if(!("".equalsIgnoreCase(generatedPDFPath) || generatedPDFPath == null))
							{
								String FinalPathofSinglepager=AWB_GEN_MAP.get("FinalSinglePagePath")+File.separator+processInstanceID+File.separator+RelatedPartyData.get("AWBNumber");
								if(CopyFile(singlePagerWithQRPath,FinalPathofSinglepager))
									SingPageStatus=updateTableData(AWBDataTableName,AWBTableColumnToBeUpdated,AWBTableValues,where);
								else
									SingPageStatus="Error in Final movement";
							}
						}
						else
							SingPageStatus="Error in attaching single pager";
						
					}
				}
			}
		}
		catch(Exception e)
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug(("Inside generateSinglePager exception"+e.getMessage()));
			SingPageStatus="Error";
		}
		return SingPageStatus;
	}
	private static boolean CopyFile(String sourceFile,String destFile)
	{
		File FileToBecopied = new File(sourceFile);
		File destinationFolder = new File(destFile);
		if (!destinationFolder.exists())
		{
			destinationFolder.mkdirs();
		}
		try 
		{
			 FileUtils.copyFileToDirectory(FileToBecopied,destinationFolder);
			 return true;
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			DBO_AWB_Logs.DBO_AWBLogger.debug(("Exception in CopyFile:- "+e.getMessage()));
			return false;
		}
	}
	 private static String attachDocument(String filetobeaddedpath,String fileName,String workItemName)
	 {
	        try 
	        {
	            DBO_AWB_Logs.DBO_AWBLogger.info("attachDocument volumeID : " + VolumeID + " filetobeaddedpath :" + filetobeaddedpath + " DocName :" + fileName);
	            if (filetobeaddedpath.equalsIgnoreCase("") || fileName.equalsIgnoreCase("")) {
	                return "N";
	            }
	            String parentFolderIndex= RelatedPartyData.get("PARENTFOLDERINDEX");
	            String sDocsize = "";
	            JPISIsIndex IsIndex = new JPISIsIndex();
	            JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
	            JPISDEC.m_cDocumentType = 'N';
	            JPISDEC.m_sVolumeId = Short.parseShort(VolumeID);
	            File fppp = new File(filetobeaddedpath);
	            long lgvDocSize;
	            File obvFile = fppp;
	            lgvDocSize = obvFile.length();
	            sDocsize = Long.toString(lgvDocSize);
	            String DocAttach = "Y";
	            if (fppp.exists()) {
	                DBO_AWB_Logs.DBO_AWBLogger.info("fpp exists : " + fppp.getPath());
	            } else {
	                DBO_AWB_Logs.DBO_AWBLogger.info("fpp does not exists");
	                DocAttach = "N";
	            }
	            if (!DocAttach.equalsIgnoreCase("N")) {
	                if (fppp.isFile()) {
	                    DBO_AWB_Logs.DBO_AWBLogger.info("fpp is file");
	                } else {
	                    DBO_AWB_Logs.DBO_AWBLogger.info("fpp is not file");
	                    DocAttach = "N";
	                }
	            }
	            if (!DocAttach.equalsIgnoreCase("N")) 
	            {
	                DBO_AWB_Logs.DBO_AWBLogger.info("Before AddDocument_MT Completion");
	                try 
	                {
	                    if(smsPort.startsWith("33"))
						{
							CPISDocumentTxn.AddDocument_MT(null, jtsIP , Short.parseShort(smsPort), cabinetName, Short.parseShort(VolumeID), filetobeaddedpath, JPISDEC, "",IsIndex);
						}
						else
						{
							CPISDocumentTxn.AddDocument_MT(null, jtsIP , Short.parseShort(smsPort), cabinetName, Short.parseShort(VolumeID), filetobeaddedpath, JPISDEC, null,"JNDI", IsIndex);
						}
	                    DBO_AWB_Logs.DBO_AWBLogger.info("AddDocument_MT Completed successfully");
	                    //fppp.delete();
	                    DBO_AWB_Logs.DBO_AWBLogger.info("Generated File deleted successfully");
	                } 
	                catch (Exception e) 
	                {
	                    DBO_AWB_Logs.DBO_AWBLogger.info("Exception in CPISDocumentTxn");
	                    DocAttach = "N";
	                    //updateRecordData(ReferenceNumber, iD2, "E", "", RETRY, e.toString().replace(",", "").replace("'", "''"));
	                    return "N";
	                } 
	                catch (JPISException e)
	                {
	                    DBO_AWB_Logs.DBO_AWBLogger.info("Exception in CPISDocumentTxn : " + e);
	                    StringWriter sw = new StringWriter();
	                    PrintWriter pw = new PrintWriter(sw);
	                    e.printStackTrace(pw);
	                    DBO_AWB_Logs.DBO_AWBLogger.info("Exception in CPISDocumentTxn 2 : " + sw);
	                    DocAttach = "N";
	                    //updateRecordData(ReferenceNumber, iD2, "E", "", RETRY, e.toString().replace(",", "").replace("'", "''"));
	                    return "N";
	                }
	            }

	            if (!DocAttach.equalsIgnoreCase("N"))
	            {
					
					String sISIndex = IsIndex.m_nDocIndex + "#" + IsIndex.m_sVolumeId;
					DBO_AWB_Logs.DBO_AWBLogger.info("workItemName: "+workItemName+" sISIndex: "+sISIndex);
					String strExtension = "pdf";
	                String DocumentType = "pdf";
	               
	                strExtension= fileName.substring(fileName.lastIndexOf(".")+1);
	                
	                if(strExtension.equalsIgnoreCase("JPG") || strExtension.equalsIgnoreCase("TIF") || strExtension.equalsIgnoreCase("JPEG") || strExtension.equalsIgnoreCase("TIFF"))
					{
						DocumentType = "I";
					}
					else
					{
						DocumentType = "N";
					}
	                
	                fileName = fileName.substring(0,fileName.lastIndexOf("."));
	               
	                String sMappedInputXml = CommonMethods.getNGOAddDocument(parentFolderIndex,SinglePagerDocType,fileName,DocumentType,strExtension,sISIndex,sDocsize,VolumeID,cabinetName,sessionID);
	                DBO_AWB_Logs.DBO_AWBLogger.info("workItemName: " + workItemName + " sMappedInputXml " + sMappedInputXml);
	               
	                String sOutputXml =WFNGExecute(sMappedInputXml, jtsIP, jtsPort, 1);
	                sOutputXml = sOutputXml.replace("<Document>", "");
	                sOutputXml = sOutputXml.replace("</Document>", "");
	                DBO_AWB_Logs.DBO_AWBLogger.info("workItemName: " + workItemName + " Output xml For NGOAddDocument Call: " + sOutputXml);
	                XMLParser objXMLParser = new XMLParser(sOutputXml);
					String Status = objXMLParser.getValueOf("Status");/*
					XMLParser xmlParserData1 = new XMLParser(extTabDataOPXML1);
					String mainCode = xmlParserData1.getValueOf("MainCode");*/
					DBO_AWB_Logs.DBO_AWBLogger.info("Status of AddDocument_MT:" + Status);
	                if ("0".equalsIgnoreCase(Status))
                    {
	                	DocAttach = "S";
                    } 
                    else
                    {
                    	DBO_AWB_Logs.DBO_AWBLogger.info("Error of AddDocument_MT:" + objXMLParser.getValueOf(sOutputXml, "Error"));
                        DocAttach = "N";
                    }
	                				
	            }
	            return DocAttach;
	        } catch (Exception e)
	        {
	            return "N";
	        }
	    }

	/*// Code for insert into ng_digital_awb_status entry.
	private static void insert_ng_digital_awb_status(String Wi_name,String cabinetName,String  sessionId, String sJtsIp,String iJtsPort,String ActivityName,String ProspectID, String AccountNumber, String relatedPartyID )
	{
		try
		{
			String process_name="";
			// select the values from ext table to insert into ng_dbo_awb_status  AccountNumber,ProspectID
		final HashMap<String, String> CheckGridDataMap_awb = new HashMap<String, String>();
		String DBQuery_awb ="select WINAME,FirstName,LastName,MobNumber,EmailID,IsChqBkReq,ChqBkRefNo,DebitCardRefNo,AWB_Number,DebitCardRequired,AWBGenerationStatus from USR_0_DBO_RelatedPartyGrid where WINAME='" + Wi_name + "'and RelatedPartyID='" + relatedPartyID + "'";
		// select method (product written) used to get the data in form of xml.
		String extTabDataIPXML_awb =CommonMethods.apSelectWithColumnNames(DBQuery_awb, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DBO_AWB_Logs.DBO_AWBLogger, false));
		DBO_AWB_Logs.DBO_AWBLogger.debug("extTabDataOPXML_awb: " + extTabDataIPXML_awb);
		String extTabDataOPXML_awb = WFNGExecute(extTabDataIPXML_awb, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		DBO_AWB_Logs.DBO_AWBLogger.debug("extTabDataOPXML_awb: " + extTabDataOPXML_awb);
		// using xml parser to pass the output data in desired format 
		XMLParser xmlParserData_awb = new XMLParser(extTabDataOPXML_awb);
		
		int iTotalrec = Integer.parseInt(xmlParserData_awb.getValueOf("TotalRetrieved"));
		// Main code we get if the ap select call is triggered success.
		if (xmlParserData_awb.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
		{
			String xmlDataExtTab = xmlParserData_awb.getNextValueOf("Record");
			xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
			// replace the char above.
			NGXmlList objWorkList = xmlParserData_awb.createList("Records", "Record");
			
			// loop over the map to put value key pair.
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
			{
				CheckGridDataMap_awb.put("WI_name", objWorkList.getVal("WINAME"));
				CheckGridDataMap_awb.put("prospect_id", ProspectID);
				CheckGridDataMap_awb.put("Given_Name", objWorkList.getVal("FirstName"));
				CheckGridDataMap_awb.put("Surname", objWorkList.getVal("LastName"));
				CheckGridDataMap_awb.put("mobile_no_1", objWorkList.getVal("MobNumber"));
				CheckGridDataMap_awb.put("email_id_1", objWorkList.getVal("EmailID"));
				CheckGridDataMap_awb.put("ChequeBk_Req", objWorkList.getVal("IsChqBkReq"));
				CheckGridDataMap_awb.put("ChequeBk_ref", objWorkList.getVal("ChqBkRefNo"));
				CheckGridDataMap_awb.put("ECRN", objWorkList.getVal("DebitCardRefNo"));
				CheckGridDataMap_awb.put("AWB_Number", objWorkList.getVal("AWB_Number"));
				CheckGridDataMap_awb.put("is_prime_req", objWorkList.getVal("DebitCardRequired"));
				CheckGridDataMap_awb.put("AWB_status", objWorkList.getVal("AWBGenerationStatus"));
				CheckGridDataMap_awb.put("account_no", objWorkList.getVal("AccountNumber"));
				
			}
			
			String processname[] = Wi_name.split("-");
			DBO_AWB_Logs.DBO_AWBLogger.debug("processname [] : "+processname[0]);
			if(processname[0].equalsIgnoreCase("DBO"))
			{
				 process_name ="DBO";
			}
			DBO_AWB_Logs.DBO_AWBLogger.debug("processname [] : "+process_name);
		}
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sDate = dateFormat.format(d);
		DBO_AWB_Logs.DBO_AWBLogger.debug("insert_ng_digital_awb_status : sDate "+sDate);
		
		String prospect_id= CheckGridDataMap_awb.get("prospect_id").trim();
		String Full_name=CheckGridDataMap_awb.get("Given_Name").trim() +" "+CheckGridDataMap_awb.get("Surname").trim();
		
		DBO_AWB_Logs.DBO_AWBLogger.debug("insert_ng_digital_awb_status : prospect_id "+prospect_id);
		DBO_AWB_Logs.DBO_AWBLogger.debug("insert_ng_digital_awb_status : Full_name "+Full_name);
		
		String columnNames_awbTable="WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ChequeBk_Req,ChequeBk_ref,DebitCardRefNo,AWB_Number,AWB_Status,card_req,processName, singlePager_ref_no,Account_no,AWB_Gen_success_date";
		String columnValues_awbTable="'"+Wi_name+"','"+prospect_id+"','"+Full_name+"','"
							+CheckGridDataMap_awb.get("mobile_no_1").trim()
							+"','"+CheckGridDataMap_awb.get("email_id_1").trim()+
							"','"+CheckGridDataMap_awb.get("ChequeBk_Req").trim()+
							"','"+CheckGridDataMap_awb.get("ChequeBk_ref").trim()
							+"','"+CheckGridDataMap_awb.get("ECRN").trim()+"','"+
							CheckGridDataMap_awb.get("AWB_Number").trim()
							+"','"+CheckGridDataMap_awb.get("AWB_status").trim()+"','"+
							CheckGridDataMap_awb.get("is_prime_req").trim()+"','"+process_name+"','"+Wi_name+"','"+
							CheckGridDataMap_awb.get("account_no").trim()+"','"+sDate+"'";
		
		DBO_AWB_Logs.DBO_AWBLogger.debug("insert_ng_digital_awb_status : columnNames_awbTable "+columnNames_awbTable);
		DBO_AWB_Logs.DBO_AWBLogger.debug("insert_ng_digital_awb_status : columnValues_awbTable "+columnValues_awbTable);

		String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames_awbTable, columnValues_awbTable,"USR_0_DBO_AWB_Status");
		DBO_AWB_Logs.DBO_AWBLogger.debug("APInsertInputXML: USR_0_DBO_AWB_Status "+apInsertInputXML);

		String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
		DBO_AWB_Logs.DBO_AWBLogger.debug("APInsertOutputXML: USR_0_DBO_AWB_Status "+ apInsertInputXML);
		
		XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
		String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
		DBO_AWB_Logs.DBO_AWBLogger.debug("Status of apInsertMaincode  USR_0_DBO_AWB_Status "+ apInsertMaincode);

		DBO_AWB_Logs.DBO_AWBLogger.debug("Completed On USR_0_DBO_AWB_Status "+ ActivityName);
		
		if(apInsertMaincode.equalsIgnoreCase("0"))
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug("ApInsert successful: USR_0_DBO_AWB_Status "+apInsertMaincode);
			DBO_AWB_Logs.DBO_AWBLogger.debug("ApInsert successful: USR_0_DBO_AWB_Status "+apInsertMaincode);
			System.out.println("ApInsert successful: USR_0_DBO_AWB_Status "+Wi_name);
			CheckGridDataMap_awb.clear();
		
		}
		else
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug("ApInsert failed for USR_0_DBO_AWB_Status: "+apInsertMaincode);
			System.out.println("ApInsert failed: ng_digital_awb_status "+Wi_name);
		}
		
		}
		catch(Exception e)
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug("insert_ng_digital_awb_status : "+e.getMessage());
		}
		
	}*/
	// Code for insert into ng_digital_awb_status entry. -- end
	private static HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DBO_AWB_Logs.DBO_AWBLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DBO_AWB_Logs.DBO_AWBLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DBO_AWB_Logs.DBO_AWBLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DBO_AWB_Logs.DBO_AWBLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DBO_AWB_Logs.DBO_AWBLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DBO_AWB_Logs.DBO_AWBLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DBO_AWB_Logs.DBO_AWBLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DBO_AWB_Logs.DBO_AWBLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DBO_AWB_Logs.DBO_AWBLogger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}
		
		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DBO_AWB_Logs.DBO_AWBLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientAWBGen.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DBO_AWB_Logs.DBO_AWBLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, String sInputXML)
	{

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



		try
		{

			DBO_AWB_Logs.DBO_AWBLogger.debug("userName "+ username);
			DBO_AWB_Logs.DBO_AWBLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DBO_AWB_Logs.DBO_AWBLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DBO_AWB_Logs.DBO_AWBLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DBO_AWB_Logs.DBO_AWBLogger.debug("Dout " + dout);
    			DBO_AWB_Logs.DBO_AWBLogger.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DBO_AWB_Logs.DBO_AWBLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DBO_AWB_Logs.DBO_AWBLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
    			}
    			byte[] readBuffer = new byte[500];
    			int num = din.read(readBuffer);
    			if (num > 0)
    			{

    				byte[] arrayBytes = new byte[num];
    				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
    				outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
    				DBO_AWB_Logs.DBO_AWBLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))
    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId, processInstanceID,outputResponse,integrationWaitTime );

    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//DAONotifyAPPLog.DAONotifyAPPLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DBO_AWB_Logs.DBO_AWBLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DBO_AWB_Logs.DBO_AWBLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DBO_AWB_Logs.DBO_AWBLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
			return "";
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
					out=null;
				}
				if(socketInputStream != null)
				{

					socketInputStream.close();
					socketInputStream=null;
				}
				if(dout != null)
				{

					dout.close();
					dout=null;
				}
				if(din != null)
				{

					din.close();
					din=null;
				}
				if(socket != null)
				{
					if(!socket.isClosed())
						socket.close();
					socket=null;
				}

			}

			catch(Exception e)
			{
				DBO_AWB_Logs.DBO_AWBLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}
	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DBO_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DBO_AWB_Logs.DBO_AWBLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DBO_AWB_Logs.DBO_AWBLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DBO_AWB_Logs.DBO_AWBLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DBO_AWB_Logs.DBO_AWBLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DAONotifyAPPLog.DAONotifyAPPLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DAONotifyAPPLog.DAONotifyAPPLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DBO_AWB_Logs.DBO_AWBLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DBO_AWB_Logs.DBO_AWBLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private static String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, String sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DBO_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DBO_AWB_Logs.DBO_AWBLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	private static void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere, String jtsIP, String jtsPort, String cabinetName,String sessionId)
	{
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		
		DBO_AWB_Logs.DBO_AWBLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionId);
				DBO_AWB_Logs.DBO_AWBLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DBO_AWB_Logs.DBO_AWBLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0"))
				{
					DBO_AWB_Logs.DBO_AWBLogger.debug(("Exception in ExecuteQuery_APUpdate updating "+tablename+" table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else
				{
					DBO_AWB_Logs.DBO_AWBLogger.debug(("Succesfully updated "+tablename+" table"));
					System.out.println("Succesfully updated "+tablename+" table");
					//ThreadConnect.addToTextArea("Successfully updated transaction table");
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionId  = CommonConnection.getSessionID(DBO_AWB_Logs.DBO_AWBLogger, false);
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
				DBO_AWB_Logs.DBO_AWBLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
			}
		}
	}

	private static int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DBO_AWB_Gen_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    AWB_GEN_MAP.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}
}