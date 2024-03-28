/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM - iRBL
Application				: RAK iRBL Utility
Module					: CIF Verification
File Name				: Integration.java
Author 					: Angad Shah
Date (DD/MM/YYYY)		: 15/05/2021

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DBO.CBS_Update;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

import ISPack.CImageServer;
import ISPack.ISUtil.JPISException;
import Jdts.DataObject.JPDBString;

public class DBO_CBS_Update_Integration
{

	
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, String smsPort, String docDownloadPath,String volumeId,String siteId, String Sig_Remarks)
	{
		String SignPushFinalStatus = "";
		String FinalStatus = "";
		try
		{
			DBO_CBS_Update_Log.setLogger();
			
			String AccountNumber = "";
			String CCIF_ID = "";
			String CompanyShortName = "";
			String DebitFreezeRemovalStatus = "";
			String ItemIndex ="";
			
			String DBQuery = "select 'ExtData' as DataType, "
					+ "isnull(AccountNumber,'')  as Data1, "
					+ "isnull(CCIF_ID,'') as Data2, "
					+ "isnull(CompanyShortName,'') as Data3, "
					+ "ItemIndex as Data4, "
					+ "isnull(DebitFreezeRemovalStatus,'') as Data5 "
					+ "from RB_DBO_EXTTABLE with(nolock) where WINAME = '"+processInstanceID+"'" 
					
					+ " union all "
					
					+ "select 'RelatedParty' as DataType, "
					+ "RelatedPartyID as Data1, "
					+ "isnull(SignaturePushStatus,'') as Data2, "
					+ "isnull(FullName,'') as Data3, "
					+ "isnull(CIFID,'') as Data4, "
					+ "'' as Data5 "
					+ "from USR_0_DBO_RelatedPartyGrid with(nolock) where WINAME = '"+processInstanceID+"'";
					
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false));
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("RelatedPartyGrid data input: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("RelatedPartyGrid data output: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			
			if(iTotalrec == 0)
				return "Success";
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
			{

				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{	
					if ("ExtData".equalsIgnoreCase(objWorkList.getVal("DataType").trim()))
					{
						AccountNumber = objWorkList.getVal("Data1").trim();
						CCIF_ID = objWorkList.getVal("Data2").trim();
						CompanyShortName = objWorkList.getVal("Data3").trim();
						ItemIndex = objWorkList.getVal("Data4").trim();
						DebitFreezeRemovalStatus = objWorkList.getVal("Data5").trim();
					}
					else 
					{
						String RelatedPartyID = objWorkList.getVal("Data1").trim();
						String SignaturePushStatus = objWorkList.getVal("Data2").trim();
						String FullName = objWorkList.getVal("Data3").trim();
						String RCIFID = objWorkList.getVal("Data4").trim();
						
			
						if(!SignaturePushStatus.trim().equalsIgnoreCase("Success"))
						{
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("WINAME : "+processInstanceID);
	
							String integrationStatus=SignaturePushCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false), CommonConnection.getJTSIP(),
									CommonConnection.getJTSPort(),processInstanceID,ws_name,integrationWaitTime,socket_connection_timeout, socketDetailsMap, smsPort, docDownloadPath,
									volumeId, siteId, Sig_Remarks, AccountNumber, CCIF_ID, CompanyShortName, ItemIndex, RelatedPartyID, FullName, RCIFID, iTotalrec);
	
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Signature Push Status integrationStatus: " +integrationStatus);
							String statuses [] = integrationStatus.split("~");
							if("0000".equalsIgnoreCase(integrationStatus))
							{
								if(!SignPushFinalStatus.contains("Failure"))
									SignPushFinalStatus = "Success";
								
								String MainGridColNames = "SignaturePushStatus";
								String MainGridColValues = "'Success'";
								String sTableName = "USR_0_DBO_RelatedPartyGrid";
								String sWhere = "WINAME = '"+processInstanceID+"' and RelatedPartyID = '"+RelatedPartyID+"' ";
								
								String status= UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,sTableName,sWhere);
								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Update Signature Push status : " +status);
							} 
							else
							{
								if (SignPushFinalStatus.equalsIgnoreCase(""))
									SignPushFinalStatus = integrationStatus;
								else 
									SignPushFinalStatus = SignPushFinalStatus+ "|" + integrationStatus;
							}	
						}
						else
						{
							if(!SignPushFinalStatus.contains("Failure"))
								SignPushFinalStatus = "Success";
						}
					}
				}
				
				if(SignPushFinalStatus.contains("Failure"))
					return SignPushFinalStatus;
				
				// when Signature Call will be success for all then FinalStatus will be Success
				// going for debit freeze removal
				if("Success".equalsIgnoreCase(SignPushFinalStatus)  && !"Success".equalsIgnoreCase(DebitFreezeRemovalStatus))
				{
					String integrationStatus=DebitFreezeRemovalCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false), CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(),processInstanceID,ws_name,integrationWaitTime,socket_connection_timeout, socketDetailsMap, AccountNumber);
					
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Debit Freeze Removal integrationStatus: " +integrationStatus);
					String statuses [] = integrationStatus.split("~");
					if(statuses[0].equalsIgnoreCase("0000") || statuses[0].equalsIgnoreCase("E4860")) // adding E4860 as part of Sprint 3- PDB-3652 25/03/2024
					{
						if(!FinalStatus.contains("Failure"))
							FinalStatus = "Success";
						
						String MainGridColNames = "DebitFreezeRemovalStatus";
						String MainGridColValues = "'Success'";
						String sTableName = "RB_DBO_EXTTABLE";
						String sWhere = "WINAME = '"+processInstanceID+"'";
						
						String status= UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,sTableName,sWhere);
					    DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Update DebitFreezeRemoval Status in extTable status : " +status);
					    
					} 
					else
					{
						FinalStatus = "Failure~Debit Unfreeze(CallName-FIN_FREEZE_UNFREEZE) Failed For Account: "+AccountNumber+"~ MsgStatus: "+statuses[1]+"~ MsgId: "+statuses[2];
					}
				}
				else
				{
					FinalStatus = "Success";
				}
				
			}
			else
			{
				FinalStatus = "Failure";
			}


		}
		catch(Exception e)
		{
			FinalStatus = "Failure";
		}
		return FinalStatus;
	}

	
	public String SignaturePushCall( String cabinetName,String UserName,String sessionID,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, String smsPort, String docDownloadPath,String volumeId,String siteId, String Sig_Remarks,
			String AccountNumber, String CCIF_ID, String CompanyShortName, String ItemIndex, String RelatedPartyID, String FullName, String RCIFID, int iTotalrec)
	{
		String sReturnDet = "";
		try
		{
			String sigDocAvailFlag = "NotAvailable";
			String docListXML = GetDocumentsList(ItemIndex, sessionID, cabinetName,sJtsIp,iJtsPort);
			if (!docListXML.trim().equalsIgnoreCase("F"))
			{
				XMLParser sXMLParser=new XMLParser(docListXML);
				int noOfDocs=sXMLParser.getNoOfFields("Document");

				DBO_CBS_Update_Log.DBOCBSUpdateLogger.info("No of docs for "+processInstanceID+" is "+noOfDocs);

				for(int i=0;i<noOfDocs;i++)
				{
					XMLParser subXMLParser = null;
					String subXML1 = sXMLParser.getNextValueOf("Document");
					subXMLParser = new XMLParser(subXML1);
					String docName = subXMLParser.getValueOf("DocumentName");
					String docExt = subXMLParser.getValueOf("CreatedByAppName");
					String Comment = subXMLParser.getValueOf("Comment");
					
					Date date = new Date();
					DateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
					String trDate = logDateFormat.format(date);

					String retDesc = "";
					String retCode = "";
					String msgId = "";
					
					if("Signature".equalsIgnoreCase(docName) 
							&& (iTotalrec == 2 || Comment.contains(RelatedPartyID)) ) // iTotalrec will be 2 for single Sole customer so no need to check relatedPartyId in comment.
					{
						sigDocAvailFlag = "Available";
						//Commented for some time as download code is not working
						String downloadStatus = DownloadDocument(subXMLParser,processInstanceID,docName,docExt,AccountNumber,cabinetName,sJtsIp,iJtsPort,docDownloadPath,volumeId,siteId);

						//Hard Coded value for some time
						//String downloadStatus="S";
						if(!("F".equals(downloadStatus)))
						{
							
							// Start - adding signature of multiple account - 26022024
							String DBQuery = "select AccountNumber,isnull(SignPushStatus,'') as SignPushStatus"
									+ " from USR_0_DBO_ADDNLCURRENCY_DTLS with(nolock) "
									+ " where WI_NAME = '"+processInstanceID+"' and AccountNumber is not null and AccountNumber !='' " ;
									
							String addnlAccountIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false));
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Additional Account Data Input: "+ addnlAccountIPXML);
							String addnlAccountOPXML = CommonMethods.WFNGExecute(addnlAccountIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Additional Account Data output: "+ addnlAccountOPXML);

							XMLParser xmlParserData= new XMLParser(addnlAccountOPXML);						
							int iTotalrecAcc = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

							
							if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrecAcc>0)
							{

								String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
								xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
								
								//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
								NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
															
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
								{	
									
										String AddnlAccountNumber = objWorkList.getVal("AccountNumber").trim();
										String AddnlSignPushStatus = objWorkList.getVal("SignPushStatus").trim();
										
							
										if(!AddnlSignPushStatus.trim().equalsIgnoreCase("Success"))
										{
											StringBuilder sInputXML = new StringBuilder(getSignatureUploadXML(downloadStatus,AddnlAccountNumber,RCIFID, trDate, FullName, CommonConnection.getUsername(), sessionID, cabinetName, Sig_Remarks));
											
											String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false),sJtsIp,
													 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
													  socketDetailsMap, sInputXML);
											

											sXMLParser=new XMLParser(responseXML);
											if("0000".equals(sXMLParser.getValueOf("ReturnCode")))
											{
												String MainGridColNames = "SignPushStatus";
												String MainGridColValues = "'Success'";
												String sTableName = "USR_0_DBO_ADDNLCURRENCY_DTLS";
												String sWhere = "WI_NAME = '"+processInstanceID+"' and AccountNumber = '"+AddnlAccountNumber+"' ";
												
												String status= UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,sTableName,sWhere);
												DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Update Signature Push status : " +status);
											}
											else
											{
												sReturnDet = "Failure";
												if(responseXML.contains("<ReturnDesc>"))
													retDesc = sXMLParser.getValueOf("ReturnDesc");
												else if(responseXML.contains("<Description>"))
													retDesc = sXMLParser.getValueOf("Description");
											
												if(responseXML.contains("<ReturnCode>"))
													retCode = sXMLParser.getValueOf("ReturnCode");
												if(responseXML.contains("<MessageId>"))
													msgId = sXMLParser.getValueOf("MessageId");
												
												sReturnDet = sReturnDet + "~" + "CallName - SIGNATURE_ADDITION_REQ, MsgId - "+msgId+", ReturnCode - "+ retCode +", ReturnDesc - "+retDesc;
												return sReturnDet;
											}
										}
									
								}
							}
							// End - adding signature of multiple account - 26022024
							
							
							StringBuilder sInputXML = new StringBuilder(getSignatureUploadXML(downloadStatus,AccountNumber,RCIFID, trDate, FullName, CommonConnection.getUsername(), sessionID, cabinetName, Sig_Remarks));
							
							// Start - Code for delete Downloaded tiff
							StringBuffer strFilePath = new StringBuffer();
							strFilePath.append(System.getProperty("user.dir"));
							strFilePath.append(File.separator);
							strFilePath.append("DownloadLoc");
								
							File file = new File(strFilePath.toString());
							
							if(file.listFiles()!=null)
							{
								for(File f: file.listFiles()) 
								{
									f.delete(); 
								}
							}
							strFilePath.delete(0,strFilePath.length());
							// End - Code for delete Downloaded tiff
							
							
							String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false),sJtsIp,
									 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
									  socketDetailsMap, sInputXML);
							

							sXMLParser=new XMLParser(responseXML);
							if("0000".equals(sXMLParser.getValueOf("ReturnCode")))
							{
								sReturnDet = "0000";								
								break;
							}
							else
							{
								sReturnDet = "Failure";
								if(responseXML.contains("<ReturnDesc>"))
									retDesc = sXMLParser.getValueOf("ReturnDesc");
								else if(responseXML.contains("<Description>"))
									retDesc = sXMLParser.getValueOf("Description");
							
								if(responseXML.contains("<ReturnCode>"))
									retCode = sXMLParser.getValueOf("ReturnCode");
								if(responseXML.contains("<MessageId>"))
									msgId = sXMLParser.getValueOf("MessageId");
								
								sReturnDet = sReturnDet + "~" + "CallName - SIGNATURE_ADDITION_REQ, MsgId - "+msgId+", ReturnCode - "+ retCode +", ReturnDesc - "+retDesc;
								return sReturnDet;
							}

						}
						else
						{
							sReturnDet = "Failure";
							retDesc = "Signature Document is not Available";
							retCode = "SIGNOTAVAIL";
							msgId = "SIGNOTAVAIL";
							sReturnDet = sReturnDet + "~" + "CallName - SIGNATURE_ADDITION_REQ, MsgId - "+msgId+", ReturnCode - "+ retCode +", ReturnDesc - "+retDesc;
							return sReturnDet;
						}

					}
				}
				if(sigDocAvailFlag.equalsIgnoreCase("NotAvailable"))
				{
					
					sReturnDet = "Failure";
					String retDesc = "Signature Document is not Available";
					String retCode = "SIGNOTAVAIL";
					String msgId = "SIGNOTAVAIL";
					sReturnDet = sReturnDet + "~" + "CallName - SIGNATURE_ADDITION_REQ, MsgId - "+msgId+", ReturnCode - "+ retCode +", ReturnDesc - "+retDesc;
					return sReturnDet;
				}
			}
			else
			{
				sReturnDet = "Failure";
				String retDesc = "Error in downloading documents";
				String retCode = "ERRDOC";
				String msgId = "ERRDOC";
				sReturnDet = sReturnDet + "~" + "CallName - SIGNATURE_ADDITION_REQ, MsgId - "+msgId+", ReturnCode - "+ retCode +", ReturnDesc - "+retDesc;
				return sReturnDet;
			}
		}
		catch(Exception e)
		{
			sReturnDet = "Failure";
			String retDesc = "exception in downloading documents";
			String retCode = "ERRDOC";
			String msgId = "ERRDOC";
			sReturnDet = sReturnDet + "~" + "CallName - SIGNATURE_ADDITION_REQ, MsgId - "+msgId+", ReturnCode - "+ retCode +", ReturnDesc - "+retDesc;
			return sReturnDet;
		}
		return sReturnDet;
	}
	
	public String DebitFreezeRemovalCall( String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, String AccountNumber)
	{
		try
		{
			
			// Start - adding debit unfreeze of multiple account - 26022024
			String DBQuery = "select AccountNumber,isnull(DebitUnFreezeStatus,'') as DebitUnFreezeStatus"
					+ " from USR_0_DBO_ADDNLCURRENCY_DTLS with(nolock) "
					+ " where WI_NAME = '"+processInstanceID+"' and AccountNumber is not null and AccountNumber !='' " ;
					
			String addnlAccountIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false));
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Additional Account Data Input: "+ addnlAccountIPXML);
			String addnlAccountOPXML = CommonMethods.WFNGExecute(addnlAccountIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Additional Account Data output: "+ addnlAccountOPXML);

			XMLParser xmlParserData= new XMLParser(addnlAccountOPXML);						
			int iTotalrecAcc = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrecAcc>0)
			{

				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{	
					
						String AddnlAccountNumber = objWorkList.getVal("AccountNumber").trim();
						String DebitUnFreezeStatus = objWorkList.getVal("DebitUnFreezeStatus").trim();
						
			
						if(!DebitUnFreezeStatus.trim().equalsIgnoreCase("Success"))
						{
							
							StringBuilder sInputXML = new StringBuilder(getDebitUnfreezeXML(AddnlAccountNumber));

							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("AddnlAccount Debit UnFreezeIntegration input XML: "+sInputXML);

							String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
									 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
									  socketDetailsMap, sInputXML);

							DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("AddnlAccount Debit UnFreeze responseXML: "+responseXML);

							XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
						    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
						    DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("AddnlAccount Return Code: "+return_code);
						    
						    if("0000".equals(xmlParserSocketDetails.getValueOf("ReturnCode")) || "E4860".equals(xmlParserSocketDetails.getValueOf("ReturnCode")) ) // adding E4860 as part of Sprint 3- PDB-3652 25/03/2024
							{
								String MainGridColNames = "DebitUnFreezeStatus";
								String MainGridColValues = "'Success'";
								String sTableName = "USR_0_DBO_ADDNLCURRENCY_DTLS";
								String sWhere = "WI_NAME = '"+processInstanceID+"' and AccountNumber = '"+AddnlAccountNumber+"' ";
								
								String status= UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,sTableName,sWhere);
								DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Update Addln DebitUnFreeze Status : " +status);
							}
							else
							{
								String return_desc = "";
								String MsgId = "";
								String retCode = "";
								
								if(responseXML.contains("<ReturnDesc>"))
									return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
								else if(responseXML.contains("<Description>"))
									return_desc = xmlParserSocketDetails.getValueOf("Description");
							
								if(responseXML.contains("<ReturnCode>"))
									retCode = xmlParserSocketDetails.getValueOf("ReturnCode");
								if(responseXML.contains("<MessageId>"))
									MsgId = xmlParserSocketDetails.getValueOf("MessageId");
								
								return (return_code + "~" + return_desc + "~"+ MsgId +"~End");
							}
						}
					
				}
			}
			// End - adding debit unfreeze of multiple account - 26022024
			
			
			StringBuilder sInputXML = new StringBuilder(getDebitUnfreezeXML(AccountNumber));

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Debit UnFreezeIntegration input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Debit UnFreeze responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
				
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
						
		    DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Return Desc: "+return_desc);
		    
		    return (return_code + "~" + return_desc + "~"+ MsgId +"~End");
		}
		catch(Exception e)
		{
			return "Exception in Debit Freeze Removal";
		}
	}
		
	public String GetDocumentsList(String itemindex , String sessionId,String cabinetName,String jtsIP,String jtsPort)
	{
		DBO_CBS_Update_Log.DBOCBSUpdateLogger.info("Inside GetDocumentsList Method ...");
		XMLParser docXmlParser = new XMLParser();
		String mainCode="";
		String response="F";
		String outputXML ="";
		try
		{

			String sInputXML = getDocumentList(itemindex, sessionId, cabinetName);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug(" Inputxml to get document names for "+itemindex+ " "+sInputXML);

			outputXML = CommonMethods.WFNGExecute(sInputXML, jtsIP, jtsPort,1);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug(" outputxml to get document names for "+ itemindex+ " "+outputXML);
			docXmlParser.setInputXML(outputXML);
			mainCode = docXmlParser.getValueOf("Status");

			if(mainCode.equals("0"))
			{
				response=outputXML;
			}

		}
		catch (Exception e)
		{
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.error("Exception occured in GetDocumentsList method : "+e);

			response ="F";
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
		}
		return response;

	}
	
	public String getDocumentList(String folderIndex, String sessionId, String cabinetName)
	{

		//folderIndex="26979";   //only for testing

		String xml = "<?xml version=\"1.0\"?><NGOGetDocumentListExt_Input>" +
				"<Option>NGOGetDocumentListExt</Option>" +
				"<CabinetName>"+cabinetName+"</CabinetName>" +
				"<UserDBId>"+sessionId+"</UserDBId>" +
				"<CurrentDateTime></CurrentDateTime>" +
				"<FolderIndex>"+folderIndex+"</FolderIndex>" +
				"<DocumentIndex></DocumentIndex>" +
				"<PreviousIndex>0</PreviousIndex>" +
				"<LastSortField></LastSortField>" +
				"<StartPos>0</StartPos>" +
				"<NoOfRecordsToFetch>1000</NoOfRecordsToFetch>" +
				"<OrderBy>5</OrderBy><SortOrder>D</SortOrder><DataAlsoFlag>N</DataAlsoFlag>" +
				"<AnnotationFlag>Y</AnnotationFlag><LinkDocFlag>Y</LinkDocFlag>" +
				"<PreviousRefIndex>0</PreviousRefIndex><LastRefField></LastRefField>" +
				"<RefOrderBy>2</RefOrderBy><RefSortOrder>A</RefSortOrder>" +
				"<NoOfReferenceToFetch>1000</NoOfReferenceToFetch>" +
				"<DocumentType>B</DocumentType>" +
				"<RecursiveFlag>N</RecursiveFlag><ThumbnailAlsoFlag>N</ThumbnailAlsoFlag>" +
				"</NGOGetDocumentListExt_Input>";

		return xml;
	}
	
	public String DownloadDocument(XMLParser xmlParser,String winame,String docName,String docExt, String account_no,String cabinetName,String jtsIp,String iJtsPort,String docDownloadPath,String volumeId,String siteId)
	{
		DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Inside DownloadDocument Method...");

		String status="F";
		String msg="Error";
		StringBuffer strFilePath = new StringBuffer();
		try
		{

			String base64String = null;
			String imageIndex = xmlParser.getValueOf("ISIndex").substring(0, xmlParser.getValueOf("ISIndex").indexOf("#"));

			strFilePath.append(System.getProperty("user.dir"));
			strFilePath.append(File.separator);
			strFilePath.append(docDownloadPath);
			strFilePath.append(File.separatorChar);
			strFilePath.append(winame);
			strFilePath.append("_");
			strFilePath.append(docName);
			strFilePath.append(".");
			strFilePath.append(docExt);

			CImageServer cImageServer=null;
			try
			{
				cImageServer = new CImageServer(null, jtsIp, Short.parseShort(iJtsPort));
			}
			catch (JPISException e)
			{
				e.printStackTrace();
				msg = e.getMessage();
				status="F";
			}
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("values passed -> "+ jtsIp+" "+iJtsPort+" "+cabinetName+" "+volumeId+" "+siteId+" "+imageIndex+" "+strFilePath.toString());
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("signature document name and imageindex for "+winame+" "+docName+","+imageIndex);

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Fetching OD Download Code ::::::");
			int odDownloadCode=cImageServer.JPISGetDocInFile_MT(null,jtsIp, Short.parseShort(iJtsPort), 
					cabinetName, Short.parseShort(siteId),Short.parseShort(volumeId), 
					Integer.parseInt(imageIndex),"",strFilePath.toString(),new JPDBString());
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("OD Download Code :"+odDownloadCode);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("strFilePath.toString() :"+strFilePath.toString());

			if(odDownloadCode==1)
			{
				try
				{
					base64String=ConvertToBase64.convertToBase64((strFilePath.toString()).trim());
					//RAOPCBSLog.RAOPCBSLogger.debug("base64String -----" +base64String);
					deleteDownloadedSignature(strFilePath.toString().trim());
					status=base64String;

				}
				catch(Exception e)
				{
					DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Exception in converting image to Base64 for "+ winame+" "+docName+","+imageIndex);

					msg=e.getMessage();
					status="F";
				}

			}
			else
			{
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Error in downloading document for "+ winame+" docname "+docName+", imageindex "+imageIndex);

				msg="Error occured while downloading the document :"+docName;
				status="F";
			}
		}
		catch (Exception e)
		{
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.error("Exception occured in DownloadDocument method : "+e);

			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			msg=e.getMessage();
			status="F";
		}

		return status;

	}
	
	public void deleteDownloadedSignature(String path)
	{
		File file = new File(path);
        if(file.delete()){
        	DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Downloaded Signture file has been deleted");
        }
        else
        {
        	DBO_CBS_Update_Log.DBOCBSUpdateLogger.error("Error in deleting the downloaded signature");
        }

	}
	
	public String getSignatureUploadXML(String base64String,String ACCNO,String CIFID,String DATE, String CustomerName,String userName, String sessionId, String cabinetName, String Sig_Remarks)
	{
		java.util.Date d1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
		String DateExtra2 = sdf1.format(d1)+"+04:00";
		
		String integrationXML = "<EE_EAI_MESSAGE>" +
			   "<EE_EAI_HEADER>" +
				  "<MsgFormat>SIGNATURE_ADDITION_REQ</MsgFormat>" +
				  "<MsgVersion>0001</MsgVersion>" +
				  "<RequestorChannelId>BPM</RequestorChannelId>" +
				  "<RequestorUserId>RAKUSER</RequestorUserId>" +
				  "<RequestorLanguage>E</RequestorLanguage>" +
				  "<RequestorSecurityInfo>secure</RequestorSecurityInfo>" +
				  "<ReturnCode>911</ReturnCode>" +
				  "<ReturnDesc>Issuer Timed Out</ReturnDesc>" +
				  "<MessageId>UniqueMessageId123</MessageId>" +
				  "<Extra1>REQ||SHELL.JOHN</Extra1>" +
				  "<Extra2>"+DateExtra2+"</Extra2>" +
			   "</EE_EAI_HEADER>" +
			   "<SignatureAddReq>" +
				  "<BankId>RAK</BankId>" +
				  "<AcctId>"+ACCNO+"</AcctId>" +
				  "<AccType>N</AccType>" +
				  "<CustId>"+CIFID+"</CustId>" +
				  "<BankCode></BankCode>" +
				  "<EmpId></EmpId>" +
				  "<CustomerName>"+CustomerName+"</CustomerName>" +
				  "<SignPowerNumber></SignPowerNumber>" +
				  "<ImageAccessCode>1</ImageAccessCode>" +
				  "<SignExpDate>2112-03-06T23:59:59.000</SignExpDate>" +
				  "<SignEffDate>2010-12-31T23:59:59.000</SignEffDate>" +
				  "<SignFile>"+base64String+"</SignFile>" +
				  "<PictureExpDate>2099-12-31T23:59:59.000</PictureExpDate>" +
				  "<PictureEffDate>2010-12-31T23:59:59.000</PictureEffDate>" +
				  "<PictureFile></PictureFile>" +
				  "<SignGroupId>SVSB11</SignGroupId>" +
				  "<Remarks>"+Sig_Remarks+"</Remarks>" +
			   "</SignatureAddReq>" +
			"</EE_EAI_MESSAGE>";


		return integrationXML;
	}
	
	public String getDebitUnfreezeXML(String ACCNO)
	{
		java.util.Date d1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
		String DateExtra2 = sdf1.format(d1)+"+04:00";
		
		String integrationXML = "<EE_EAI_MESSAGE>\n"+
				"<EE_EAI_HEADER>\n"+
					"<MsgFormat>FIN_FREEZE_UNFREEZE</MsgFormat>\n"+
					"<MsgVersion>0001</MsgVersion>\n"+
					"<RequestorChannelId>BPM</RequestorChannelId>\n"+
					"<RequestorUserId>RAKUSER</RequestorUserId>\n"+
					"<RequestorLanguage>E</RequestorLanguage>\n"+
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n"+
					"<ReturnCode>0000</ReturnCode>\n"+
					"<ReturnDesc>REQ</ReturnDesc>\n"+
					"<MessageId>UNFREEZE123456789</MessageId>\n"+
					"<Extra1>REQ||BPM.123</Extra1>\n"+
					"<Extra2>"+DateExtra2+"</Extra2>\n"+
				"</EE_EAI_HEADER>\n"+
				"<FreezeUnfreezeRequest>"+
					"<BankId>RAK</BankId>"+
					"<FuncCode>U</FuncCode>"+
					"<AcctId>"+ACCNO+"</AcctId>"+
					"<FrezCode>D</FrezCode>"+
				"</FreezeUnfreezeRequest>"+
				"</EE_EAI_MESSAGE>\n";


		return integrationXML;
	}
	
	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
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

			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("userName "+ username);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Dout " + dout);
    			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Din " + din);

    			outputResponse = "";



    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))

    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId,
    							processInstanceID,outputResponse,integrationWaitTime );




    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}

	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
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
		DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();

	}

	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String
			sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DBO_XMLLOG_HISTORY with (nolock) where " +
					"MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			int Loop_count=0;
			do
			{
				String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false));
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Response APSelect InputXML: "+responseInputXML);
				
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	public String UpdateGridTableMWResponse(String columnNames, String columnValues, String TransactionTable, String sWhereClause) throws IOException, Exception
	{	
		String RetStatus="";
		String QueryString="";
		String sInputXML="";
		String sOutputXML="";
			//Updating records
			sInputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DBO_CBS_Update_Log.DBOCBSUpdateLogger, false), TransactionTable, columnNames, columnValues, sWhereClause);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Input XML for apUpdateInput from "+TransactionTable+" Table "+sInputXML);

			sOutputXML=CommonMethods.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Output XML for apUpdateInput Table "+sOutputXML);

			XMLParser sXMLParserChild= new XMLParser(sOutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("StrMainCode: "+StrMainCode);

		    if (StrMainCode.equals("0"))
			{
		    	DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Successful in apUpdateInput the record in : "+TransactionTable);
		    	RetStatus="Success in apUpdateInput the record";
			}
		    else
		    {
		    	DBO_CBS_Update_Log.DBOCBSUpdateLogger.debug("Error in Executing apUpdateInput sOutputXML : "+TransactionTable);
		    	RetStatus="Error in Executing apUpdateInput";
		    }
			
		return RetStatus;
	}
}





