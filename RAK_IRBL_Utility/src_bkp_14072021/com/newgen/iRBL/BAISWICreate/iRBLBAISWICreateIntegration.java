/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: RAOPIntegration.java
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

import ISPack.CImageServer;
import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;
import Jdts.DataObject.JPDBString;

public class iRBLBAISWICreateIntegration
{
	
	private String DocumentsTag="";
	private char fieldSep = ((char)21); //Constant
	private char recordSep =((char)25);  //Constant
	
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, Map <String, String> BAISWICreateConfigParamMap)
	{
		String FinalStatus = "";
		DocumentsTag="";
		try
		{
			
			String DBQuery = "SELECT ITEMINDEX,WINAME,BPM_REF_BAIS,SOL_ID,TL_NUMBER,RAK_TRACK_NUMBER_BAIS,FINACLE_SR_NUMBER,CIF_NUMBER,PRIORITY,RISK_SCORE,COMPANY_NAME,RO,RM,CHANNEL_REFERRAL,AECB_RESULT_STATUS,ARABIC_DOCS, isnull(format(DOCUMENT_DISPATCH_DATE, 'yyyy-MM-dd'),'') as DOCUMENT_DISPATCH_DATE,DNFBP_STATUS,EXPRESS_CODE,AED_CURRENCY,ADDRESS_OR_EID,INDUSTRY_CODE_AO, (select count(*) from USR_0_IRBL_EXCEPTION_HISTORY with(nolock) where IS_RAISED='true' and WI_NAME = '"+processInstanceID+"') as ExceptionCount FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE WINAME = '"+processInstanceID+"' AND (BPM_REF_BAIS IS NULL OR BPM_REF_BAIS ='')";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false));
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create data input: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create data output: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0"))
			{

				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

				HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					CheckGridDataMap.put("ITEMINDEX",objWorkList.getVal("ITEMINDEX"));
					CheckGridDataMap.put("WINAME",objWorkList.getVal("WINAME"));
					CheckGridDataMap.put("BPM_REF_BAIS",objWorkList.getVal("BPM_REF_BAIS"));
					CheckGridDataMap.put("SOL_ID", objWorkList.getVal("SOL_ID"));
					CheckGridDataMap.put("TL_NUMBER", objWorkList.getVal("TL_NUMBER"));
					CheckGridDataMap.put("RAK_TRACK_NUMBER_BAIS", objWorkList.getVal("RAK_TRACK_NUMBER_BAIS"));
					CheckGridDataMap.put("FINACLE_SR_NUMBER", objWorkList.getVal("FINACLE_SR_NUMBER"));
					CheckGridDataMap.put("CIF_NUMBER", objWorkList.getVal("CIF_NUMBER"));
					CheckGridDataMap.put("PRIORITY", objWorkList.getVal("PRIORITY"));
					CheckGridDataMap.put("RISK_SCORE", objWorkList.getVal("RISK_SCORE"));
					CheckGridDataMap.put("COMPANY_NAME", objWorkList.getVal("COMPANY_NAME"));
					CheckGridDataMap.put("RO", objWorkList.getVal("RO"));
					CheckGridDataMap.put("RM", objWorkList.getVal("RM"));
					CheckGridDataMap.put("CHANNEL_REFERRAL", objWorkList.getVal("CHANNEL_REFERRAL"));
					CheckGridDataMap.put("AECB_RESULT_STATUS", objWorkList.getVal("AECB_RESULT_STATUS"));
					CheckGridDataMap.put("ARABIC_DOCS", objWorkList.getVal("ARABIC_DOCS"));
					CheckGridDataMap.put("DOCUMENT_DISPATCH_DATE", objWorkList.getVal("DOCUMENT_DISPATCH_DATE"));
					CheckGridDataMap.put("DNFBP_STATUS", objWorkList.getVal("DNFBP_STATUS"));
					CheckGridDataMap.put("EXPRESS_CODE", objWorkList.getVal("EXPRESS_CODE"));
					CheckGridDataMap.put("AED_CURRENCY", objWorkList.getVal("AED_CURRENCY"));
					CheckGridDataMap.put("ADDRESS_OR_EID", objWorkList.getVal("ADDRESS_OR_EID"));
					CheckGridDataMap.put("INDUSTRY_CODE_AO", objWorkList.getVal("INDUSTRY_CODE_AO"));
					CheckGridDataMap.put("ExceptionCount", objWorkList.getVal("ExceptionCount"));
		
					
					
					String downloadAndAttachStatus = downloadAllDocsFromiRBLWI(CheckGridDataMap, BAISWICreateConfigParamMap);
					
					if("S".equalsIgnoreCase(downloadAndAttachStatus))
					{
						int ExcepCount = Integer.parseInt(objWorkList.getVal("ExceptionCount"));
						String ExcepRaisedFlag = "N";
						if (ExcepCount>0)
							ExcepRaisedFlag = "Y";
							
						String attributeTag="CHANNEL"+fieldSep+"iRBL.WBA"+recordSep
							+"WIStatus"+fieldSep+objWorkList.getVal("WINAME")+recordSep	
							+"DIGITAL_BANKING_REQUIRED"+fieldSep+"Yes"+recordSep
							+"RAK_CONNECT_REQUIRED"+fieldSep+"Yes"+recordSep
							+"AllDocAttached"+fieldSep+"Yes"+recordSep
							+"isDistributed"+fieldSep+"No"+recordSep
							+"Deferral_Held"+fieldSep+"No"+recordSep
							+"Sol_Id"+fieldSep+objWorkList.getVal("SOL_ID")+recordSep
							+"q_Sol_Id"+fieldSep+objWorkList.getVal("SOL_ID")+recordSep
							+"TLNumber"+fieldSep+objWorkList.getVal("TL_NUMBER")+recordSep
							+"q_TLNumber"+fieldSep+objWorkList.getVal("TL_NUMBER")+recordSep
							+"RAK_Track_Number"+fieldSep+objWorkList.getVal("RAK_TRACK_NUMBER_BAIS")+recordSep
							+"Finacle_SR_Number"+fieldSep+objWorkList.getVal("FINACLE_SR_NUMBER")+recordSep
							+"CIF_Id"+fieldSep+objWorkList.getVal("CIF_NUMBER")+recordSep
							+"Priority"+fieldSep+objWorkList.getVal("PRIORITY")+recordSep
							+"Risk_Score"+fieldSep+objWorkList.getVal("RISK_SCORE")+recordSep
							+"custname"+fieldSep+objWorkList.getVal("COMPANY_NAME")+recordSep
							+"ROCode"+fieldSep+objWorkList.getVal("RO")+recordSep
							+"q_ROCODE"+fieldSep+objWorkList.getVal("RO")+recordSep
							+"rmcode"+fieldSep+objWorkList.getVal("RM")+recordSep
							+"q_RMCODE"+fieldSep+objWorkList.getVal("RM")+recordSep
							+"channel_refferal"+fieldSep+objWorkList.getVal("CHANNEL_REFERRAL")+recordSep
							+"AECBResult"+fieldSep+objWorkList.getVal("AECB_RESULT_STATUS")+recordSep
							+"ArabicDocuments"+fieldSep+objWorkList.getVal("ARABIC_DOCS")+recordSep
							+"DocumentDispatchDate"+fieldSep+objWorkList.getVal("DOCUMENT_DISPATCH_DATE")+recordSep
							+"DNFBP_Status"+fieldSep+objWorkList.getVal("DNFBP_STATUS")+recordSep
							+"ExpressCode"+fieldSep+objWorkList.getVal("EXPRESS_CODE")+recordSep
							+"Currency"+fieldSep+objWorkList.getVal("AED_CURRENCY")+recordSep
							+"AddressOrEID"+fieldSep+objWorkList.getVal("ADDRESS_OR_EID")+recordSep
							+"IndustryCode"+fieldSep+objWorkList.getVal("INDUSTRY_CODE_AO")+recordSep
							+"U_UID"+fieldSep+ExcepRaisedFlag+recordSep;
					
						String WICreateInputXML = "<?xml version=\"1.0\"?>"+
									"<WFUploadWorkItem_Input>"+
									"<Option>WFUploadWorkItem</Option>"+
									"<EngineName>"+CommonConnection.getOFCabinetName()+"</EngineName>"+
									"<SessionId>"+CommonConnection.getOFSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false)+"</SessionId>"+
									"<ValidationRequired><ValidationRequired>"+
									"<ProcessDefId>"+CommonConnection.getOFBAISProcessDefId()+"</ProcessDefId>"+
									"<DataDefName></DataDefName>"+
									"<Fields></Fields>"+
									"<InitiateAlso>Y</InitiateAlso>"+
									"<Documents>"+DocumentsTag+"</Documents>"+
									"<Attributes>"+attributeTag+"</Attributes>"+
									"</WFUploadWorkItem_Input>";
					
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create xml input: "+ WICreateInputXML);
						String WICreateOutputXML = CommonMethods.WFNGExecute(WICreateInputXML,CommonConnection.getOFJTSIP(),CommonConnection.getOFJTSPort(),1);
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("BAIS WI Create xml output: "+ WICreateOutputXML);
						
						XMLParser xmlParserData1= new XMLParser(WICreateOutputXML);
						if(xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0"))
						{
							String BAIS_WINAME = xmlParserData1.getValueOf("ProcessInstanceId");
							FinalStatus = "Success~"+BAIS_WINAME;
							
							
							// Inserting BAIS WIHisotry
							Date d= new Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String sDate = dateFormat.format(d);
							String colNames = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
							
							String colValues = "'"+ BAIS_WINAME + "','Introduction','SUBMIT','" + sDate + "','Workitem created for iBPS "+objWorkList.getVal("WINAME")+" workitem ','System','" + sDate + "'";
							
							String BAISWIHistoryInsertInputXML = CommonMethods.apInsert(CommonConnection.getOFCabinetName(), CommonConnection.getOFSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false), colNames, colValues, "USR_0_BAIS_WIHISTORY");
							String BAISWIHistoryInsertOutputXML = CommonMethods.WFNGExecute(BAISWIHistoryInsertInputXML,CommonConnection.getOFJTSIP(),CommonConnection.getOFJTSPort(),1);
							//////////////////////////////////////////
							
						} else 
						{
							String ErrDesc = xmlParserData1.getValueOf("Subject");
							iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Error In BAIS WI Create");
							FinalStatus = "Failure~"+ErrDesc;
						}
					} else {
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Error In downloadAndAttachStatus BAIS WI Create");
						FinalStatus = "Failure";
					}
				}
			
			}
			else 
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Error In mail BAIS WI Create");
				FinalStatus = "Failure";
			}

		}
		catch(Exception e)
		{
			FinalStatus = "Failure";
		}
		return FinalStatus;
	}

		
	public String downloadAllDocsFromiRBLWI(HashMap<String, String> CheckGridDataMap, Map <String, String> BAISWICreateConfigParamMap)
	{
		String downloadStatus = "";
		String docListXML = GetDocumentsList(CheckGridDataMap.get("ITEMINDEX"), CommonConnection.getSessionID(iRBLBAISWICreateLog.iRBLBAISWICreateLogger, false), CommonConnection.getCabinetName(), CommonConnection.getJTSIP(), CommonConnection.getJTSPort());
		if (!docListXML.trim().equalsIgnoreCase("F"))
		{
			XMLParser sXMLParser=new XMLParser(docListXML);
			int noOfDocs=sXMLParser.getNoOfFields("Document");

			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("No of docs for "+CheckGridDataMap.get("WINAME")+" is "+noOfDocs);

			if(noOfDocs<1)
				downloadStatus="S";
			
			for(int i=0;i<noOfDocs;i++)
			{
				XMLParser subXMLParser = null;
				String subXML1 = sXMLParser.getNextValueOf("Document");
				subXMLParser = new XMLParser(subXML1);
				String docName = subXMLParser.getValueOf("DocumentName");
				String docExt = subXMLParser.getValueOf("CreatedByAppName");
				
				downloadStatus = DownloadDocument(BAISWICreateConfigParamMap, subXMLParser,CheckGridDataMap.get("WINAME"),docName,docExt,CommonConnection.getCabinetName(), CommonConnection.getJTSIP(), CommonConnection.getsSMSPort(), "DownloadLoc", CommonConnection.getsVolumeID(), CommonConnection.getsSiteID());
				
			}
			
			// deleting processed workitem folder
			StringBuffer strFilePath = new StringBuffer();
			strFilePath.append(System.getProperty("user.dir"));
			strFilePath.append(File.separator);
			strFilePath.append("DownloadLoc");
			
			deleteFolder(strFilePath.toString());
		}	
		
		return downloadStatus;
	}
	
	
	public String GetDocumentsList(String itemindex , String sessionId,String cabinetName,String jtsIP,String jtsPort)
	{
		iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("Inside GetDocumentsList Method ...");
		XMLParser docXmlParser = new XMLParser();
		String mainCode="";
		String response="F";
		String outputXML ="";
		try
		{

			String sInputXML = CommonMethods.getDocumentList(itemindex, sessionId, cabinetName);
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug(" Inputxml to get document names for "+itemindex+ " "+sInputXML);

			outputXML = CommonMethods.WFNGExecute(sInputXML, jtsIP, jtsPort,1);
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug(" outputxml to get document names for "+ itemindex+ " "+outputXML);
			docXmlParser.setInputXML(outputXML);
			mainCode = docXmlParser.getValueOf("Status");

			if(mainCode.equals("0"))
			{
				response=outputXML;
			}

		}
		catch (Exception e)
		{
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.error("Exception occured in GetDocumentsList method : "+e);

			response ="F";
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
		}
		return response;
	}
	
	
	public String DownloadDocument(Map <String, String> BAISWICreateConfigParamMap, XMLParser xmlParser,String winame,String docName,String docExt, String cabinetName,String jtsIp,String smsPort,String docDownloadPath,String volumeId,String siteId)
	{
		iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Inside DownloadDocument Method...");

		String status="F";
		String msg="Error";
		StringBuffer strFilePath = new StringBuffer();
		try
		{

			String imageIndex = xmlParser.getValueOf("ISIndex").substring(0, xmlParser.getValueOf("ISIndex").indexOf("#"));
			
			strFilePath.append(System.getProperty("user.dir"));
			strFilePath.append(File.separator);
			strFilePath.append(docDownloadPath);
			strFilePath.append(File.separator);
			strFilePath.append(winame);
			
			File af = null;
     		boolean bool = false;
     		try 
     		{  
     			// returns pathnames for files and directory
     			af = new File(strFilePath.toString());
     			// create directories
     			if (af.exists())
     			{
     				// do nothing
     			}else {	
     				bool = af.mkdirs();
     			}
     			// print
     			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("Directory created? "+bool);
            } catch(Exception e) {
             	// if any error occurs
             	e.printStackTrace();
             	iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("For WNAME "+winame+" Exception in creating file path: " + e.getMessage());
            }
			
     		strFilePath.append(File.separatorChar);
			strFilePath.append(docName+"_"+imageIndex);
			strFilePath.append(".");
			strFilePath.append(docExt);
     		
			String DocNameInBAIS = getIRBLToBAISDocMapping(docName);
			if("".equalsIgnoreCase(DocNameInBAIS.trim()))
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("No need to attach this iRBL document in BAIS - iBRL_DocName:"+docName);
				status="S";
				return status;
			}
     		
			CImageServer cImageServer=null;
			try
			{
				cImageServer = new CImageServer(null, jtsIp, Short.parseShort(smsPort));
			}
			catch (JPISException e)
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("cImageServer excp1:"+e.getMessage());
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exception = sw.toString();
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("cImageServer excp2:"+exception);
				status="F";
			}
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("values passed -> "+ jtsIp+" "+smsPort+" "+cabinetName+" "+volumeId+" "+siteId+" "+imageIndex+" "+strFilePath.toString());
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("signature document name and imageindex for "+winame+" "+docName+","+imageIndex);

			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Fetching OD Download Code ::::::");
			int odDownloadCode=cImageServer.JPISGetDocInFile_MT(null,jtsIp, Short.parseShort(smsPort), cabinetName, Short.parseShort(siteId),Short.parseShort(volumeId), Integer.parseInt(imageIndex),"",strFilePath.toString(),new JPDBString());

			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("OD Download Code :"+odDownloadCode);
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("strFilePath.toString() :"+strFilePath.toString());

			if(odDownloadCode==1)
			{
				
				try
				{
					JPISIsIndex ISINDEX = new JPISIsIndex();
					JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
			        
					String docPath=strFilePath.toString();
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName: "+winame+" The Document address is: "+docPath);
					String sDocsize = "";
					File fppp = new File(docPath);
					long lgvDocSize;
					File obvFile = fppp;
					lgvDocSize = obvFile.length();
					sDocsize = Long.toString(lgvDocSize);
		          
					try
					{
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName: "+winame+" before CPISDocumentTxn AddDocument MT: OF cabinet name:"+CommonConnection.getOFCabinetName()+", OF JTS IP:"+CommonConnection.getOFJTSIP()+", OF JTS Port:"+CommonConnection.getOFJTSPort()+", OF Volumn ID:"+CommonConnection.getOFVOLUMNID());

						if(CommonConnection.getOFJTSPort().startsWith("33"))
						{
							CPISDocumentTxn.AddDocument_MT(null, CommonConnection.getOFJTSIP() , Short.parseShort(CommonConnection.getOFJTSPort()), CommonConnection.getOFCabinetName(), Short.parseShort(CommonConnection.getOFVOLUMNID()), docPath, JPISDEC, "",ISINDEX);
						}
						else
						{
							CPISDocumentTxn.AddDocument_MT(null, CommonConnection.getOFJTSIP() , Short.parseShort(CommonConnection.getOFJTSPort()), CommonConnection.getOFCabinetName(), Short.parseShort(CommonConnection.getOFVOLUMNID()), docPath, JPISDEC, null,"JNDI", ISINDEX);
						}	

						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName: "+winame+" after CPISDocumentTxn AddDocument MT: ");
						status="S";
						String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
						
						DocumentsTag=DocumentsTag+ DocNameInBAIS+fieldSep+sISIndex+fieldSep+ISINDEX.m_nPageNumber+fieldSep+sDocsize+fieldSep+docExt+recordSep;
						fppp.delete();
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName: "+winame+" sISIndex: "+sISIndex);
					}
					catch (NumberFormatException e)
					{
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName1:"+e.getMessage()+CommonMethods.printException(e));
						e.printStackTrace();
						//catchflag=true;
					}
					catch (JPISException e)
					{
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName2:"+e.getMessage());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String exception = sw.toString();
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName22:"+exception);
						//catchflag=true;
					}
					catch (Exception e)
					{
						iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("workItemName3:"+e.getMessage()+CommonMethods.printException(e));
						e.printStackTrace();
						//catchflag=true;
					}
				
					
					
				}
				catch(Exception e)
				{
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Exception in OF Upload "+ winame+" "+docName+","+imageIndex);
					iRBLBAISWICreateLog.iRBLBAISWICreateLogger.error("Exception in OF Upload2 : "+e.getMessage()+CommonMethods.printException(e));
					status="F";
				}

			}
			else
			{
				iRBLBAISWICreateLog.iRBLBAISWICreateLogger.debug("Error in downloading document for "+ winame+" docname "+docName+", imageindex "+imageIndex);

				msg="Error occured while downloading the document :"+docName;
				status="F";
			}
		}
		catch (Exception e)
		{
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.error("Exception occured in DownloadDocument method : "+e.getMessage()+CommonMethods.printException(e));

			status="F";
		}

		return status;

	}
	
	private void deleteFolder(String fileLocation) {
		File folder = new File(fileLocation);
		File[] listofFiles = folder.listFiles();
		if (listofFiles.length == 0) {
			iRBLBAISWICreateLog.iRBLBAISWICreateLogger.info("Folder Name :: " + folder.getAbsolutePath() + " is deleted.");
			folder.delete();
			//isFinished = false;
		} else {
			for (int j = 0; j < listofFiles.length; j++) {
				File file = listofFiles[j];
				if (file.isDirectory()) {
					deleteFolder(file.getAbsolutePath());
				}
			}
		}
	}
	
	public String getIRBLToBAISDocMapping(String iRBLDOCName)
	{
		String BAISDocName = "";
		
		if(iRBLDOCName.contains("AECB_Consent_Form_Company"))
			BAISDocName = "AECB_Consent_Form_Company";
		else if(iRBLDOCName.contains("AECB_Consent_Form_Others"))
			BAISDocName = "AECB_Consent_Form_Others";
		else if(iRBLDOCName.contains("AECB_Docs_Company"))
			BAISDocName = "AECB_Docs_Company";
		else if(iRBLDOCName.contains("AU_Checklist"))
			BAISDocName = "Controls_Checklist";
		else if(iRBLDOCName.contains("AU_Dedupe_Findings"))
			BAISDocName = "AU_Dedupe_Findings";
		else if(iRBLDOCName.contains("Bankstatements_accountspreadsheet"))
			BAISDocName = "Bank_Statements";
		else if(iRBLDOCName.contains("BO_Report"))
			BAISDocName = "De-Dupe";
		else if(iRBLDOCName.contains("Busines_Visit_Report"))
			BAISDocName = "RBL_BVR_CAM_TWC_ABF_LAF Photographs";
		else if(iRBLDOCName.contains("BVR_RM"))
			BAISDocName = "RBL_BVR_CAM_TWC_ABF_LAF Photographs";
		else if(iRBLDOCName.contains("Old_CAM_BVR_LAF_KYC_RiskScore"))
			BAISDocName = "Old_CAM_BVR_LAF_KYC_RiskScore";
		else if(iRBLDOCName.contains("CB_Doc_CO"))
			BAISDocName = "CO-CB";
		else if(iRBLDOCName.contains("CB_Doc_others"))
			BAISDocName = "SIG1-CB";
		else if(iRBLDOCName.contains("CBRB_Checklist"))
			BAISDocName = "OperationsDedupeForAsset";
		else if(iRBLDOCName.contains("CBRB_Docs"))
			BAISDocName = "OperationsDedupeForAsset";
		else if(iRBLDOCName.contains("AECB_Docs_Others"))
			BAISDocName = "OperationsDedupeForAsset";
		else if(iRBLDOCName.contains("CEU_Visit_Report"))
			BAISDocName = "CEU_Visit_Report";
		else if(iRBLDOCName.contains("Cheque_Clearance_Supporting_Doc"))
			BAISDocName = "Invoices_Contracts_Shipment_MOU_Custom";
		else if(iRBLDOCName.contains("CIF_update_form"))
			BAISDocName = "CIF_update_form";
		else if(iRBLDOCName.contains("Constitutional_Docs"))
			BAISDocName = "Constitutional_Documents";
		else if(iRBLDOCName.contains("Credit_Approval_Documents"))
			BAISDocName = "Credit_Approval_Documents";
		else if(iRBLDOCName.contains("Credit_Emails"))
			BAISDocName = "Emails_Approvals";
		else if(iRBLDOCName.contains("Deferral_Documents"))
			BAISDocName = "Deferral_Doc";
		else if(iRBLDOCName.contains("Email_exception_approvals"))
			BAISDocName = "Emails_Approvals";
		else if(iRBLDOCName.contains("HOD_SM Approval"))
			BAISDocName = "HOD_Approval";
		else if(iRBLDOCName.contains("EUM_proof"))
			BAISDocName = "Deferral_Doc";
		else if(iRBLDOCName.contains("Facility_letters"))
			BAISDocName = "Facility_letters";
		else if(iRBLDOCName.contains("Invoices_Contracts_BLs_Chq_copies_Agreement_copies"))
			BAISDocName = "Invoices_Contracts_Shipment_MOU_Custom";
		else if(iRBLDOCName.contains("KYC"))
			BAISDocName = "KYC_Documents";
		else if(iRBLDOCName.contains("LAF_and_other_security_documents"))
			BAISDocName = "RBL_BVR_CAM_TWC_ABF_LAF_Photographs";
		else if(iRBLDOCName.contains("Letters_to_Customer"))
			BAISDocName = "Letters_to_Customer";
		else if(iRBLDOCName.contains("Others"))
			BAISDocName = "Others_Documents";
		else if(iRBLDOCName.contains("Passport_Visa_EID"))
			BAISDocName = "Passport_Copies";
		else if(iRBLDOCName.contains("Photograph_Location_VisitingCard"))
			BAISDocName = "RBL_BVR_CAM_TWC_ABF_LAF_Photographs";
		else if(iRBLDOCName.contains("POS_Transactions"))
			BAISDocName = "POS_Transactions";
		else if(iRBLDOCName.contains("Related_party_bank_statements"))
			BAISDocName = "PEP_Form_And_Other_Related_Party_Docs_And_POA";
		else if(iRBLDOCName.contains("Related_Party_Doc"))
			BAISDocName = "PEP_Form_And_Other_Related_Party_Docs_And_POA";
		else if(iRBLDOCName.contains("RMVR_and_Attested_ID_Copy"))
			BAISDocName = "RBL_BVR_CAM_TWC_ABF_LAF_Photographs";
		else if(iRBLDOCName.contains("Telephonebill_Tenancycontract_WPS_Proofofstaff"))
			BAISDocName = "Company_address_proof";
		else if(iRBLDOCName.contains("TL_On_Website"))
			BAISDocName = "Online_Verified_Docs";
		else if(iRBLDOCName.contains("Translated_Arabic_Docs"))
			BAISDocName = "Translated_Arabic_Docs";
		else if(iRBLDOCName.contains("Turn_Over_Sheet"))
			BAISDocName = "Bank_Statements";
		else if(iRBLDOCName.contains("VAT_Return"))
			BAISDocName = "VAT_Return";
		else if(iRBLDOCName.contains("WC_Doc"))
			BAISDocName = "WC_Doc";
		else if(iRBLDOCName.contains("FIRCO_Clearance_Co_doc"))
			BAISDocName = "CO-WC";
		else if(iRBLDOCName.contains("FIRCO_Clearance_Ind_doc"))
			BAISDocName = "SIG1-WC";
		else if(iRBLDOCName.contains("Account_Opening_Application"))
			BAISDocName = "Account_Opening_Application";
		else if(iRBLDOCName.contains("Account_Opening_Checklist"))
			BAISDocName = "Account_Opening_Checklist";
		else if(iRBLDOCName.contains("CV_Personal_Background"))
			BAISDocName = "CV_Personal_Background";
		else if(iRBLDOCName.contains("DNFBP_Checklist"))
			BAISDocName = "DNFBP_Checklist";
		else if(iRBLDOCName.contains("AED_Express_Form"))
			BAISDocName = "Express_AED_Form";
		else if(iRBLDOCName.contains("FATCA_And_CRS"))
			BAISDocName = "FATCA_And_CRS";
		else if(iRBLDOCName.contains("Risk_Score"))
			BAISDocName = "Risk_Score";
		else if(iRBLDOCName.contains("UBO_Document"))
			BAISDocName = "UBO_Document";
		else if(iRBLDOCName.contains("PEP form and other related party docs and POA"))
			BAISDocName = "PEP_Form_And_Other_Related_Party_Docs_And_POA";
		else if(iRBLDOCName.contains("Dormancy form_Individual"))
			BAISDocName = "Dormancy_form_Individual";
		else if(iRBLDOCName.contains("Dormancy form_Non Individual"))
			BAISDocName = "Dormancy_form_Non_Individual";
		else if(iRBLDOCName.contains("Additional account opening form"))
			BAISDocName = "Additional_account_opening_form";
		else if(iRBLDOCName.contains("OCR_Statementreader_Output"))
			BAISDocName = "OCR_Statementreader_Output";
		else if(iRBLDOCName.contains("Proof of address and physical locaiton"))
			BAISDocName = "Type_Of_Address_Proof_And_Physical_Location";
		else if(iRBLDOCName.contains("Insurance"))
			BAISDocName = "Insurance";
		else if(iRBLDOCName.contains("Property documents"))
			BAISDocName = "Property_documents";
		else if(iRBLDOCName.contains("Personal networth Statement"))
			BAISDocName = "Personal_networth_Statement";
		else if(iRBLDOCName.contains("Other Clearance Proof"))
			BAISDocName = "Other_Clearance_Proof";
		else if(iRBLDOCName.contains("Profile change documents"))
			BAISDocName = "Profile_change_documents";
		else if(iRBLDOCName.contains("Sister company documents"))
			BAISDocName = "Sister_company_documents";
		else if(iRBLDOCName.contains("Transport documents"))
			BAISDocName = "Transport_documents";
		else if(iRBLDOCName.contains("Express service form"))
			BAISDocName = "Express_service_form";
		else if(iRBLDOCName.contains("EID Validation report"))
			BAISDocName = "EID_Validation_report";
				
		
		return BAISDocName;
	}
	
}





