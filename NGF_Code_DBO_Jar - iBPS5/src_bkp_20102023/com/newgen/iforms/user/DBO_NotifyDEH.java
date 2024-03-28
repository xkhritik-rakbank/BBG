package com.newgen.iforms.user;


import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class DBO_NotifyDEH extends DBOCommon {
	
	public static String XMLLOG_HISTORY = "NG_DBO_XMLLOG_HISTORY";
	
	
	public String onevent(IFormReference iformObj, String control, String StringData) throws IOException {
		String wiName = getWorkitemName(iformObj);
		String WSNAME = getActivityName(iformObj);
		String returnValue = "";
		String MQ_response = "";
		String cabinetName = getCabinetName(iformObj);
		String decisionValue = "";
		String attributesTag = "";
		String socketServerIP = "";
		String socketServerPort = "";
		
		

		MQ_response = MQ_connection_response(iformObj, control, StringData);

		// return MQ_response;StatusCode
		if (MQ_response.indexOf("<MessageStatus>") != -1)
			returnValue = MQ_response.substring(
					MQ_response.indexOf("<MessageStatus>") + "</MessageStatus>".length() - 1,
					MQ_response.indexOf("</MessageStatus>"));

		if (MQ_response.contains("INVALID SESSION"))
			returnValue = "INVALID SESSION";

		if ("Success".equalsIgnoreCase(returnValue))
			 returnValue = MQ_response;
			returnValue = "NOTIFY CALL SUCCESS";
		// save response data start
		XMLParser xmlParserSocketDetails = new XMLParser(MQ_response);
		DBO.mLogger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails);
		//just to test 
		String returnCode = xmlParserSocketDetails.getValueOf("ReturnCode");
		try{
		if("0000".equalsIgnoreCase(returnCode)){
			
			DBO.mLogger.debug(" NOTIFY CALL Status :  NOTIFY CALL SUCCESS");
			// update table after notifying DEH
			int size =iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS").size();
			DBO.mLogger.debug("size of Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS: " + size);
			if(size >= 1){
					for (int i = 0; i < size; i++){
						iformObj.setTableCellValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS", i, 9, "Y");
						DBO.mLogger.debug("Successfull in updating  Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS Y as IsSentToDEH ");
					}
				}
			int size1 =iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNL_DOC_REQUIRED").size();
			DBO.mLogger.debug("size of Q_USR_0_DBO_ADDNL_DOC_REQUIRED: " + size1);
			if(size1 >= 1){
					for (int i = 0; i < size1; i++)
					{
						iformObj.setTableCellValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED", i, 9, "Y");
						DBO.mLogger.debug("Successfull in updating  Q_USR_0_DBO_ADDNL_DOC_REQUIRED Y as IsSentToDEH ");
					}
				}
			return "NOTIFY CALL SUCCESS";
		
		}else{
			DBO.mLogger.debug("NOTIFY CALL Status : Some Error Occured at Server End");
			return "Some Error Occured at Server End";
		}
		}catch(Exception e){
			DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			return "Some Error Occured at Server End";
		}
		/*String SystemErrorCode = xmlParserSocketDetails.getValueOf("SystemErrorCode");
		DBO.mLogger.debug("SystemErrorCode : " + SystemErrorCode + " for WI: " + wiName);
		String SystemErrorMessage = xmlParserSocketDetails.getValueOf("SystemErrorMessage");
		DBO.mLogger.debug("SystemErrorMessage : " + SystemErrorMessage + " for WI: " + wiName);
		if (SystemErrorCode != null && !SystemErrorCode.equals("")) {
			decisionValue = "Failed";
			DBO.mLogger.debug("Decision in else : " + decisionValue);
			attributesTag = "<Decision>" + decisionValue + "</Decision>";
		} else {
			decisionValue = "Success";
			DBO.mLogger.debug("Decision in success: " + decisionValue);
			attributesTag = "<Decision>" + decisionValue + "</Decision>";
		}*/

		//return returnValue;

	}
	public String MQ_connection_response(IFormReference iformObj, String control, String Data) {
		DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
				+ ", Inside MQ_connection_response function for DBO Notify Call");
		final IFormReference iFormOBJECT;
		final WDGeneralData wdgeneralObj;
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String mqOutputResponse = null;
		String mqOutputResponse1 = null;
		String mqInputRequest = null;
		String cabinetName = getCabinetName(iformObj);
		String wi_name = getWorkitemName(iformObj);
		String ws_name = getActivityName(iformObj);
		String userName = getUserName(iformObj);
		String socketServerIP;
		int socketServerPort;
		wdgeneralObj = iformObj.getObjGeneralData();
		String sessionID = wdgeneralObj.getM_strDMSSessionId();
		String CallName = "";
		StringBuilder finalXml = new StringBuilder();
		String xmlBody = "";
		
		String DECISION = (String) iformObj.getValue("DECISION");
		String ProspectID = (String) iformObj.getValue("ProspectID");
		
		String CompanyCategory = (String) iformObj.getValue("CompanyCategory");
		String copyof_CompanyCategory = (String) iformObj.getValue("copyof_CompanyCategory");
		String Persona = (String) iformObj.getValue("Persona");
		String copyof_Persona = (String) iformObj.getValue("copyof_Persona");
		
		String TLNumber = (String) iformObj.getValue("TLNumber");
		String TradeName = (String) iformObj.getValue("TradeName");
		String TLIssuingAuthority = (String) iformObj.getValue("TLIssuingAuthority");
		String TLIssusingAuthorithyEmirate = (String) iformObj.getValue("TLIssusingAuthorithyEmirate");
		String TLExpiryDate = (String) iformObj.getValue("TLExpiryDate");
		String TLTypeOfOffice = (String) iformObj.getValue("TLTypeOfOffice");
		String DateOfIncorporation = (String) iformObj.getValue("DateOfIncorporation");
		String CompanyName = (String) iformObj.getValue("CompanyName");
		String CompanyShortName = (String) iformObj.getValue("CompanyShortName");
		String NameOnChqBk = (String) iformObj.getValue("NameOnChqBk");
		//String LicenseType = (String) iformObj.getValue("LicenseTypeLicenseType");
		String CountryOfIncorporation = (String) iformObj.getValue("Q_RB_DBO_TXNTABLE_CountryOfIncorporation");
		String CompanyPEP = (String) iformObj.getValue("CompanyPEP");
		String RiskScore = (String) iformObj.getValue("RiskScore");
		
		String copyof_TL_Number = (String) iformObj.getValue("copyof_TL_Number");
		String copyof_TradeName = (String) iformObj.getValue("copyof_TradeName");
		String copyof_TL_Issuing_Authority = (String) iformObj.getValue("copyof_TL_Issuing_Authority");
		String copyof_TL_Issuing_Authority_Emirate = (String) iformObj.getValue("copyof_TL_Issuing_Authority_Emirate");
		String copyof_TL_ExpiryDate = (String) iformObj.getValue("copyof_TL_ExpiryDate");
		String copyof_TL_TypeofOffice = (String) iformObj.getValue("copyof_TL_TypeofOffice");
		String copyof_DateofIncorporation = (String) iformObj.getValue("copyof_DateofIncorporation");
		String copyof_CompanyName = (String) iformObj.getValue("copyof_CompanyName");
		String copyof_CompanyShortName = (String) iformObj.getValue("copyof_CompanyShortName");
		String copyof_NameOnChequeBook = (String) iformObj.getValue("copyof_NameOnChequeBook");
		//String copyof_LicenseType = (String) iformObj.getValue("copyof_LicenseType");
		String copyof_CountryOfIncoporation = (String) iformObj.getValue("Q_RB_DBO_TXNTABLE_copyof_CountryOfIncoporation");
		
		SimpleDateFormat inputdateFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat outputdateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String notify_xml_dbo = "";
		notify_xml_dbo += "<EE_EAI_MESSAGE>";
		notify_xml_dbo += "\n\t<EE_EAI_HEADER>";
		notify_xml_dbo += "\n\t\t<MsgFormat>NOTIFY_BBG_APPLICATION</MsgFormat>";
		notify_xml_dbo += "\n\t\t<MsgVersion>0001</MsgVersion>";
		notify_xml_dbo += "\n\t\t<RequestorChannelId>BPM</RequestorChannelId>";
		notify_xml_dbo += "\n\t\t<RequestorUserId>RAKUSER</RequestorUserId>";
		notify_xml_dbo += "\n\t\t<RequestorLanguage>E</RequestorLanguage>";
		notify_xml_dbo += "\n\t\t<RequestorSecurityInfo>secure</RequestorSecurityInfo>";
		notify_xml_dbo += "\n\t\t<ReturnCode>911</ReturnCode>";
		notify_xml_dbo += "\n\t\t<ReturnDesc>Issuer Timed Out</ReturnDesc>";
		notify_xml_dbo += "\n\t\t<MessageId>123123453</MessageId>";
		notify_xml_dbo += "\n\t\t<Extra1>REQ||SHELL.JOHN</Extra1>";
		notify_xml_dbo += "\n\t\t<Extra2>yyyy-MM-dd HH:mm:ssThh:mm:ss.mmm+hh:mm</Extra2>";
		notify_xml_dbo += "\n\t</EE_EAI_HEADER>";
		notify_xml_dbo += "\n\t<NotifyBBGApplicationStatusRequest>";
		notify_xml_dbo += "\n\t\t<BankId>RAK</BankId>";
		notify_xml_dbo += "\n\t\t<Process_Name>DBO</Process_Name>";
		notify_xml_dbo += "\n\t\t<ProspectId>" + ProspectID + "</ProspectId>";
		notify_xml_dbo += "\n\t\t<ChannelId>EBC.WBA</ChannelId>";
		notify_xml_dbo += "\n\t\t<WorkitemNumber>" + wi_name + "</WorkitemNumber>";
		notify_xml_dbo += "\n\t\t<Event>" + "#Subprocess_Name#" + "</Event>";
		notify_xml_dbo += "#body#";
		notify_xml_dbo += "\n\t</NotifyBBGApplicationStatusRequest>";
		notify_xml_dbo += "\n</EE_EAI_MESSAGE>";
		
		if(DECISION.equalsIgnoreCase("Submit with No Change")){
			try{
				DBO.mLogger.info("Notify XML  ::::: no change before replace"  + notify_xml_dbo);
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "INIT_ACC_OPENING");
				//
				if(CompanyPEP != null && !"".equalsIgnoreCase(CompanyPEP)){
					xmlBody += "\n\t\t" +"<CompanyPEPMatchStatus>" + CompanyPEP + "</CompanyPEPMatchStatus>";
				}
				if(RiskScore != null && !"".equalsIgnoreCase(RiskScore)){
					xmlBody += "\n\t\t" +"<RiskScore>" + RiskScore + "</RiskScore>";
				}
				
				int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size);
				
				xmlBody  += "\n\t\t<UpdateProspectDetailsFromBPM>";
				for (int i = 0; i < size; i++)
				{
					String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 0);
					DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
					String PEPMatchStatus = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 33);
					DBO.mLogger.debug("value of PEPMatchStatus: " + PEPMatchStatus );
					//
					String RCIF_ID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 1);
					DBO.mLogger.debug("value of RCIF_ID: " + RCIF_ID );
					//
					if(PEPMatchStatus != null && !"".equalsIgnoreCase(PEPMatchStatus))
					{
						xmlBody += "\n\t\t" + "<SignatoryInfoDetailsFromBPM>";
						if(RelatedPartyID != null && !"".equalsIgnoreCase(RelatedPartyID)){
							xmlBody += "\n\t\t\t" +"<RelatedPartyID>"+RelatedPartyID+"</RelatedPartyID>";
						}
						xmlBody += "\n\t\t\t" +"<PEPMatchStatus>"+PEPMatchStatus+"</PEPMatchStatus>";
						if(RCIF_ID != null && !"".equalsIgnoreCase(RCIF_ID)){
							xmlBody += "\n\t\t\t" +"<RCIFID>"+RCIF_ID+"</RCIFID>";
						}
						xmlBody += "\n\t\t" +"</SignatoryInfoDetailsFromBPM>";
					}
				}
				xmlBody += "\n\t\t</UpdateProspectDetailsFromBPM>";
				//
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: xmlBody"  + xmlBody);
				
				DBO.mLogger.info("Notify XML  ::::: no change after replace"  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Correction required at Front End")){
			try{
				DBO.mLogger.info("Notify XML  ::::: no change before replace"  + notify_xml_dbo);
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "FE_CORRECTION");
				
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: xmlBody"  + xmlBody);
				
				DBO.mLogger.info("Notify XML  ::::: no change after replace"  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		}
		else if(DECISION.equalsIgnoreCase("Information Correction")){
			try{
				DBO.mLogger.info("Notify XML  ::::: no change before replace"  + notify_xml_dbo);
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "INFO_CORRECTION");
				if(CompanyCategory != null && !"".equalsIgnoreCase(CompanyCategory) && !CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory)){
					xmlBody += "\n\t\t" +"<CompanyCategory>" + CompanyCategory + "</CompanyCategory>";
				}
				if(CompanyPEP != null && !"".equalsIgnoreCase(CompanyPEP)){
					xmlBody += "\n\t\t" +"<CompanyPEPMatchStatus>" + CompanyPEP + "</CompanyPEPMatchStatus>";
				}
				if(RiskScore != null && !"".equalsIgnoreCase(RiskScore)){
					xmlBody += "\n\t\t" +"<RiskScore>" + RiskScore + "</RiskScore>";
				}
				xmlBody  += "\n\t\t<UpdateProspectDetailsFromBPM>";
				
				if(TLNumber != null && !TLNumber.equalsIgnoreCase("") && !TLNumber.equalsIgnoreCase(copyof_TL_Number)){
					xmlBody += "\n\t\t" +"<UpdatedTLNumber>" + TLNumber + "</UpdatedTLNumber>";
				}
				if(TradeName != null && !TradeName.equalsIgnoreCase("") && !TradeName.equalsIgnoreCase(copyof_TradeName)){
					xmlBody += "\n\t\t" +"<UpdatedTradeName>" + TradeName + "</UpdatedTradeName>";
				}
				if(TLIssuingAuthority != null && !TLIssuingAuthority.equalsIgnoreCase("") && !TLIssuingAuthority.equalsIgnoreCase(copyof_TL_Issuing_Authority)){
					xmlBody += "\n\t\t" +"<UpdatedLicenseIssuingAuthority>" + TLIssuingAuthority + "</UpdatedLicenseIssuingAuthority>";
				}
				if(TLIssusingAuthorithyEmirate != null && !TLIssusingAuthorithyEmirate.equalsIgnoreCase("") && !TLIssusingAuthorithyEmirate.equalsIgnoreCase(copyof_TL_Issuing_Authority_Emirate)){
					xmlBody += "\n\t\t" +"<UpdatedLicenseIssuingAuthorityEmirate>" + TLIssusingAuthorithyEmirate + "</UpdatedLicenseIssuingAuthorityEmirate>";
				}
				if(TLExpiryDate != null && !TLExpiryDate.equalsIgnoreCase("") && !TLExpiryDate.equalsIgnoreCase(copyof_TL_ExpiryDate)){
					Date date = inputdateFormat.parse(TLExpiryDate);
					DBO.mLogger.debug("value of TLExpiryDate: "+TLExpiryDate );
					DBO.mLogger.debug("value of TLExpiryDate: "+outputdateFormat.format(date) );
					xmlBody += "\n\t\t" +"<UpdatedLicenseOrCOIExpiryDate>" + outputdateFormat.format(date) + "</UpdatedLicenseOrCOIExpiryDate>";
				}
				if(TLTypeOfOffice != null && !TLTypeOfOffice.equalsIgnoreCase("") && !TLTypeOfOffice.equalsIgnoreCase(copyof_TL_TypeofOffice)){
					xmlBody += "\n\t\t" +"<UpdatedTypeOfOffice>" + TLTypeOfOffice + "</UpdatedTypeOfOffice>";
				}
				if(CountryOfIncorporation != null && !CountryOfIncorporation.equalsIgnoreCase("") && !CountryOfIncorporation.equalsIgnoreCase(copyof_CountryOfIncoporation) ){
					xmlBody += "\n\t\t" +"<UpdatedCountryOfIncorporation>" + CountryOfIncorporation + "</UpdatedCountryOfIncorporation>";
				}
				if(DateOfIncorporation != null && !DateOfIncorporation.equalsIgnoreCase("") && !DateOfIncorporation.equalsIgnoreCase(copyof_DateofIncorporation)){
					Date date = inputdateFormat.parse(DateOfIncorporation);
					DBO.mLogger.debug("value of DateOfIncorporation: "+DateOfIncorporation );
					DBO.mLogger.debug("value of DateOfIncorporation: "+outputdateFormat.format(date) );
					xmlBody += "\n\t\t" +"<UpdatedDateOfIncorporation>" + outputdateFormat.format(date) + "</UpdatedDateOfIncorporation>";
				}
				if(CompanyName != null && !CompanyName.equalsIgnoreCase("") && !CompanyName.equalsIgnoreCase(copyof_CompanyName)){
					xmlBody += "\n\t\t" +"<UpdatedCompanyName>" + CompanyName + "</UpdatedCompanyName>";
				}
				if(CompanyShortName != null && !CompanyShortName.equalsIgnoreCase("") && !CompanyShortName.equalsIgnoreCase(copyof_CompanyShortName)){
					xmlBody += "\n\t\t" +"<UpdatedCompanyShortName>" + CompanyShortName + "</UpdatedCompanyShortName>";
				}
				if(NameOnChqBk != null && !NameOnChqBk.equalsIgnoreCase("") && !NameOnChqBk.equalsIgnoreCase(copyof_NameOnChequeBook)){
					DBO.mLogger.debug("value of NameOnChqBk: "+NameOnChqBk );
					DBO.mLogger.debug("value of copyof_NameOnChequeBook when not equal: "+copyof_NameOnChequeBook );
					xmlBody += "\n\t\t" +"<UpdatedNameOnChequeBook>" + NameOnChqBk + "</UpdatedNameOnChequeBook>";
					DBO.mLogger.debug("value of NameOnChqBk after adding in xml: "+NameOnChqBk );
					DBO.mLogger.debug("value of xmlBody after adding in xml: "+xmlBody );
				}
				
				int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size);
				
				for (int i = 0; i < size; i++)
				{
					String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 0);
					DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
					String RCIF_ID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 1);
					DBO.mLogger.debug("value of RCIF_ID: " + RCIF_ID );
					String PEPMatchStatus = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 33);
					DBO.mLogger.debug("value of FullName: " + PEPMatchStatus );
					String FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 2);
					DBO.mLogger.debug("value of FullName: " + FullName );
					String FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 5);
					DBO.mLogger.debug("value of FirstName: "+FirstName );
					String MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 6);
					DBO.mLogger.debug("value of MiddleName: " +MiddleName);
					String LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 7);
					DBO.mLogger.debug("value of LastName: "+LastName );
					String NameonDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 27);
					DBO.mLogger.debug("value of NameonDebitCard: "+NameonDebitCard );
					
					String tmp_date =  iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 3);
					String DOB = "";
					Date date = inputdateFormat.parse(tmp_date);
					DOB = outputdateFormat.format(date);
					DBO.mLogger.debug("value of DOB: "+tmp_date );
					DBO.mLogger.debug("value of DOB: "+DOB );
					String Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 8);
					DBO.mLogger.debug("value of Title: "+Title );
					String Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 9);
					DBO.mLogger.debug("value of Nationality: "+Nationality );
					String copyof_FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 36);
					DBO.mLogger.debug("value of copyof_FullName: "+copyof_FullName );
					String copyof_FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 37);
					DBO.mLogger.debug("value of copyof_FirstName: "+copyof_FirstName );
					String copyof_MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 38);
					DBO.mLogger.debug("value of copyof_MiddleName: "+copyof_MiddleName );
					String copyof_LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 39);
					DBO.mLogger.debug("value of copyof_LastName: "+copyof_LastName );
					String copyof_NameOnDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 40);
					
					String tmp_date1 = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 42);
					String copyof_DOB = "";
					Date date1 = inputdateFormat.parse(tmp_date1);
					DBO.mLogger.debug("value of DOB: "+tmp_date1 );
					copyof_DOB = outputdateFormat.format(date1);
					DBO.mLogger.debug("value of copyof_DOB: "+copyof_DOB );
					String copyof_Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 43);
					DBO.mLogger.debug("value of copyof_Title: "+copyof_Title );
					String copyof_Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 44);
					DBO.mLogger.debug("value of copyof_Nationality: "+copyof_Nationality );
					if(!FullName.equalsIgnoreCase(copyof_FullName) || !FirstName.equalsIgnoreCase(copyof_FirstName) ||
							!MiddleName.equalsIgnoreCase(copyof_MiddleName) || !LastName.equalsIgnoreCase(copyof_LastName) ||
							!NameonDebitCard.equalsIgnoreCase(copyof_NameOnDebitCard) || !DOB.equalsIgnoreCase(copyof_DOB) || 
							!Title.equalsIgnoreCase(copyof_Title) || !Nationality.equalsIgnoreCase(copyof_Nationality)){
						
						xmlBody += "\n\t\t" + "<SignatoryInfoDetailsFromBPM>";
						if(DOB != null && !"".equalsIgnoreCase(DOB)&& !DOB.equalsIgnoreCase(copyof_DOB)  ){
							xmlBody += "\n\t\t\t" +"<UpdatedDOB>"+DOB+"</UpdatedDOB>";
						}
						if(Title != null && !"".equalsIgnoreCase(Title) && !Title.equalsIgnoreCase(copyof_Title)){
							xmlBody += "\n\t\t\t" +"<UpdatedTitle>"+Title+"</UpdatedTitle>";
						}
						if(Nationality != null && !"".equalsIgnoreCase(Nationality) && !Nationality.equalsIgnoreCase(copyof_Nationality)){
							xmlBody += "\n\t\t\t" +"<UpdatedNationality>"+Nationality+"</UpdatedNationality>";
						}
						xmlBody += "\n\t\t\t" +"<RelatedPartyID>"+RelatedPartyID+"</RelatedPartyID>";
						if(FullName != null && !"".equalsIgnoreCase(FullName) && !FullName.equalsIgnoreCase(copyof_FullName)){
							xmlBody += "\n\t\t\t" +"<UpdatedFullName>"+FullName+"</UpdatedFullName>";
						}
						if(FirstName != null && !"".equalsIgnoreCase(FirstName) && !FirstName.equalsIgnoreCase(copyof_FirstName)){
							xmlBody += "\n\t\t\t" +"<UpdatedFirstName>"+FirstName+"</UpdatedFirstName>";
						}
						if(MiddleName != null && !"".equalsIgnoreCase(MiddleName) && !MiddleName.equalsIgnoreCase(copyof_MiddleName)){
							xmlBody += "\n\t\t\t" +"<UpdatedMiddleName>"+MiddleName+"</UpdatedMiddleName>";
						}
						if(LastName != null && !"".equalsIgnoreCase(LastName) && !LastName.equalsIgnoreCase(copyof_LastName)){
							xmlBody += "\n\t\t\t" +"<UpdatedLastName>"+LastName+"</UpdatedLastName>";
						}
						if(NameonDebitCard != null && !"".equalsIgnoreCase(NameonDebitCard) && !NameonDebitCard.equalsIgnoreCase(copyof_NameOnDebitCard)){
							xmlBody += "\n\t\t\t" +"<UpdatedNameOnDebitCard>"+NameonDebitCard+"</UpdatedNameOnDebitCard>";
						}
						if(PEPMatchStatus != null && !"".equalsIgnoreCase(PEPMatchStatus))
						{
							xmlBody += "\n\t\t\t" +"<PEPMatchStatus>"+PEPMatchStatus+"</PEPMatchStatus>";
						}
						if(RCIF_ID != null && !"".equalsIgnoreCase(RCIF_ID)){
							xmlBody += "\n\t\t\t" +"<RCIFID>"+RCIF_ID+"</RCIFID>";
						}
						
						xmlBody += "\n\t\t" +"</SignatoryInfoDetailsFromBPM>";
					
					}
				}
				xmlBody += "\n\t\t</UpdateProspectDetailsFromBPM>";
				
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: info change "  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Sole LLC to Sole and No Correction")){
			try{
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "SOLELLC_TO_SOLE");
				
				if(Persona != null && !"".equalsIgnoreCase(Persona) && !Persona.equalsIgnoreCase(copyof_Persona)){
					xmlBody += "\n\t\t" +"<Persona>" + Persona + "</Persona>";
				}
				if(CompanyCategory != null && !"".equalsIgnoreCase(CompanyCategory) && !CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory)){
					xmlBody += "\n\t\t" +"<CompanyCategory>" + CompanyCategory + "</CompanyCategory>";
				}
				if(CompanyPEP != null && !"".equalsIgnoreCase(CompanyPEP)){
					xmlBody += "\n\t\t" +"<CompanyPEPMatchStatus>" + CompanyPEP + "</CompanyPEPMatchStatus>";
				}
				if(RiskScore != null && !"".equalsIgnoreCase(RiskScore)){
					xmlBody += "\n\t\t" +"<RiskScore>" + RiskScore + "</RiskScore>";
				}
				
				int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size);
				xmlBody += "\n\t\t<UpdateProspectDetailsFromBPM>";
				for (int i = 0; i < size; i++)
				{
					String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 0);
					DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
					String PEPMatchStatus = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 33);
					DBO.mLogger.debug("value of PEPMatchStatus: " + PEPMatchStatus );
					String RCIF_ID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 1);
					DBO.mLogger.debug("value of RCIF_ID: " + RCIF_ID );
					if(PEPMatchStatus != null && !"".equalsIgnoreCase(PEPMatchStatus))
					{
						xmlBody += "\n\t\t" + "<SignatoryInfoDetailsFromBPM>";
						if(RelatedPartyID != null && !"".equalsIgnoreCase(RelatedPartyID)){
							xmlBody += "\n\t\t\t" +"<RelatedPartyID>"+RelatedPartyID+"</RelatedPartyID>";
						}
						xmlBody += "\n\t\t\t" +"<PEPMatchStatus>"+PEPMatchStatus+"</PEPMatchStatus>";
						if(RCIF_ID != null && !"".equalsIgnoreCase(RCIF_ID)){
							xmlBody += "\n\t\t\t" +"<RCIFID>"+RCIF_ID+"</RCIFID>";
						}
						xmlBody += "\n\t\t" +"</SignatoryInfoDetailsFromBPM>";
					}
				}
				xmlBody += "\n\t\t</UpdateProspectDetailsFromBPM>";
				
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: Sole LLC to Sole "  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Sole LLC to Sole and Information Correction")){
			try{
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "SOLELLC_TO_SOLE_WITH_INFO_CORRECTION");

				if(Persona != null && !"".equalsIgnoreCase(Persona) && !Persona.equalsIgnoreCase(copyof_Persona)){
					xmlBody += "\n\t\t" +"<Persona>" + Persona + "</Persona>";
				}
				if(CompanyCategory != null && !"".equalsIgnoreCase(CompanyCategory) && !CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory)){
					xmlBody += "\n\t\t" +"<CompanyCategory>" + CompanyCategory + "</CompanyCategory>";
				}
				if(CompanyPEP != null && !"".equalsIgnoreCase(CompanyPEP)){
					xmlBody += "\n\t\t" +"<CompanyPEPMatchStatus>" + CompanyPEP + "</CompanyPEPMatchStatus>";
				}
				if(RiskScore != null && !"".equalsIgnoreCase(RiskScore)){
					xmlBody += "\n\t\t" +"<RiskScore>" + RiskScore + "</RiskScore>";
				}
				xmlBody  += "\n\t\t<UpdateProspectDetailsFromBPM>";
				
				if(TLNumber != null && !TLNumber.equalsIgnoreCase("") && !TLNumber.equalsIgnoreCase(copyof_TL_Number)){
					xmlBody += "\n\t\t" +"<UpdatedTLNumber>" + TLNumber + "</UpdatedTLNumber>";
				}
				if(TradeName != null && !TradeName.equalsIgnoreCase("") && !TradeName.equalsIgnoreCase(copyof_TradeName)){
					xmlBody += "\n\t\t" +"<UpdatedTradeName>" + TradeName + "</UpdatedTradeName>";
				}
				if(TLIssuingAuthority != null && !TLIssuingAuthority.equalsIgnoreCase("") && !TLIssuingAuthority.equalsIgnoreCase(copyof_TL_Issuing_Authority)){
					xmlBody += "\n\t\t" +"<UpdatedLicenseIssuingAuthority>" + TLIssuingAuthority + "</UpdatedLicenseIssuingAuthority>";
				}
				if(TLIssusingAuthorithyEmirate != null && !TLIssusingAuthorithyEmirate.equalsIgnoreCase("") && !TLIssusingAuthorithyEmirate.equalsIgnoreCase(copyof_TL_Issuing_Authority_Emirate)){
					xmlBody += "\n\t\t" +"<UpdatedLicenseIssuingAuthorityEmirate>" + TLIssusingAuthorithyEmirate + "</UpdatedLicenseIssuingAuthorityEmirate>";
				}
				if(TLExpiryDate != null && !TLExpiryDate.equalsIgnoreCase("") && !TLExpiryDate.equalsIgnoreCase(copyof_TL_ExpiryDate)){
					Date date = inputdateFormat.parse(TLExpiryDate);
					DBO.mLogger.debug("value of TLExpiryDate: "+TLExpiryDate );
					DBO.mLogger.debug("value of TLExpiryDate: "+outputdateFormat.format(date) );
					xmlBody += "\n\t\t" +"<UpdatedLicenseOrCOIExpiryDate>" + outputdateFormat.format(date) + "</UpdatedLicenseOrCOIExpiryDate>";
				}
				if(TLTypeOfOffice != null && !TLTypeOfOffice.equalsIgnoreCase("") && !TLTypeOfOffice.equalsIgnoreCase(copyof_TL_TypeofOffice)){
					xmlBody += "\n\t\t" +"<UpdatedTypeOfOffice>" + TLTypeOfOffice + "</UpdatedTypeOfOffice>";
				}
				if(CountryOfIncorporation != null && !CountryOfIncorporation.equalsIgnoreCase("") && !CountryOfIncorporation.equalsIgnoreCase(copyof_CountryOfIncoporation) ){
					xmlBody += "\n\t\t" +"<UpdatedCountryOfIncorporation>" + CountryOfIncorporation + "</UpdatedCountryOfIncorporation>";
				}
				if(DateOfIncorporation != null && !DateOfIncorporation.equalsIgnoreCase("") && !DateOfIncorporation.equalsIgnoreCase(copyof_DateofIncorporation)){
					Date date = inputdateFormat.parse(DateOfIncorporation);
					DBO.mLogger.debug("value of DateOfIncorporation: "+DateOfIncorporation );
					DBO.mLogger.debug("value of DateOfIncorporation: "+outputdateFormat.format(date) );
					xmlBody += "\n\t\t" +"<UpdatedDateOfIncorporation>" + outputdateFormat.format(date) + "</UpdatedDateOfIncorporation>";
				}
				if(CompanyName != null && !CompanyName.equalsIgnoreCase("") && !CompanyName.equalsIgnoreCase(copyof_CompanyName)){
					xmlBody += "\n\t\t" +"<UpdatedCompanyName>" + CompanyName + "</UpdatedCompanyName>";
				}
				if(CompanyShortName != null && !CompanyShortName.equalsIgnoreCase("") && !CompanyShortName.equalsIgnoreCase(copyof_CompanyShortName)){
					xmlBody += "\n\t\t" +"<UpdatedCompanyShortName>" + CompanyShortName + "</UpdatedCompanyShortName>";
				}
				if(NameOnChqBk != null && !NameOnChqBk.equalsIgnoreCase("") && !NameOnChqBk.equalsIgnoreCase(copyof_NameOnChequeBook)){
					xmlBody += "\n\t\t" +"<UpdatedNameOnChequeBook>" + NameOnChqBk + "</UpdatedNameOnChequeBook>";
				}
				
				int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size);
				
				for (int i = 0; i < size; i++)
				{
					String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 0);
					DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
					String PEPMatchStatus = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 33);
					DBO.mLogger.debug("value of PEPMatchStatus: " + PEPMatchStatus );
					String RCIF_ID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 1);
					DBO.mLogger.debug("value of RCIF_ID: " + RCIF_ID );
					String FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 2);
					DBO.mLogger.debug("value of FullName: " + FullName );
					String FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 5);
					DBO.mLogger.debug("value of FirstName: "+FirstName );
					String MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 6);
					DBO.mLogger.debug("value of MiddleName: " +MiddleName);
					String LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 7);
					DBO.mLogger.debug("value of LastName: "+LastName );
					String NameonDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 27);
					DBO.mLogger.debug("value of NameonDebitCard: "+NameonDebitCard );
					
					String tmp_date =  iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 3);
					String DOB = "";
					Date date = inputdateFormat.parse(tmp_date);
					DOB = outputdateFormat.format(date);
					DBO.mLogger.debug("value of DOB: "+tmp_date );
					DBO.mLogger.debug("value of DOB: "+DOB );
					String Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 8);
					DBO.mLogger.debug("value of Title: "+Title );
					String Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 9);
					DBO.mLogger.debug("value of Nationality: "+Nationality );
					String copyof_FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 36);
					DBO.mLogger.debug("value of copyof_FullName: "+copyof_FullName );
					String copyof_FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 37);
					DBO.mLogger.debug("value of copyof_FirstName: "+copyof_FirstName );
					String copyof_MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 38);
					DBO.mLogger.debug("value of copyof_MiddleName: "+copyof_MiddleName );
					String copyof_LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 39);
					DBO.mLogger.debug("value of copyof_LastName: "+copyof_LastName );
					String copyof_NameOnDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 40);
					
					String tmp_date1 = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 42);
					String copyof_DOB = "";
					Date date1 = inputdateFormat.parse(tmp_date1);
					DBO.mLogger.debug("value of DOB: "+tmp_date1 );
					copyof_DOB = outputdateFormat.format(date1);
					DBO.mLogger.debug("value of copyof_DOB: "+copyof_DOB );
					String copyof_Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 43);
					DBO.mLogger.debug("value of copyof_Title: "+copyof_Title );
					String copyof_Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 44);
					DBO.mLogger.debug("value of copyof_Nationality: "+copyof_Nationality );
					if(!FullName.equalsIgnoreCase(copyof_FullName) || !FirstName.equalsIgnoreCase(copyof_FirstName) ||
							!MiddleName.equalsIgnoreCase(copyof_MiddleName) || !LastName.equalsIgnoreCase(copyof_LastName) ||
							!NameonDebitCard.equalsIgnoreCase(copyof_NameOnDebitCard) || !DOB.equalsIgnoreCase(copyof_DOB) || 
							!Title.equalsIgnoreCase(copyof_Title) || !Nationality.equalsIgnoreCase(copyof_Nationality)){
						
						xmlBody += "\n\t\t" + "<SignatoryInfoDetailsFromBPM>";
						if(DOB != null && !"".equalsIgnoreCase(DOB)&& !DOB.equalsIgnoreCase(copyof_DOB)  ){
							xmlBody += "\n\t\t\t" +"<UpdatedDOB>"+DOB+"</UpdatedDOB>";
						}
						if(Title != null && !"".equalsIgnoreCase(Title) && !Title.equalsIgnoreCase(copyof_Title)){
							xmlBody += "\n\t\t\t" +"<UpdatedTitle>"+Title+"</UpdatedTitle>";
						}
						if(Nationality != null && !"".equalsIgnoreCase(Nationality) && !Nationality.equalsIgnoreCase(copyof_Nationality)){
							xmlBody += "\n\t\t\t" +"<UpdatedNationality>"+Nationality+"</UpdatedNationality>";
						}
						xmlBody += "\n\t\t\t" +"<RelatedPartyID>"+RelatedPartyID+"</RelatedPartyID>";
						if(FullName != null && !"".equalsIgnoreCase(FullName) && !FullName.equalsIgnoreCase(copyof_FullName)){
							xmlBody += "\n\t\t\t" +"<UpdatedFullName>"+FullName+"</UpdatedFullName>";
						}
						if(FirstName != null && !"".equalsIgnoreCase(FirstName) && !FirstName.equalsIgnoreCase(copyof_FirstName)){
							xmlBody += "\n\t\t\t" +"<UpdatedFirstName>"+FirstName+"</UpdatedFirstName>";
						}
						if(MiddleName != null && !"".equalsIgnoreCase(MiddleName) && !MiddleName.equalsIgnoreCase(copyof_MiddleName)){
							xmlBody += "\n\t\t\t" +"<UpdatedMiddleName>"+MiddleName+"</UpdatedMiddleName>";
						}
						if(LastName != null && !"".equalsIgnoreCase(LastName) && !LastName.equalsIgnoreCase(copyof_LastName)){
							xmlBody += "\n\t\t\t" +"<UpdatedLastName>"+LastName+"</UpdatedLastName>";
						}
						if(NameonDebitCard != null && !"".equalsIgnoreCase(NameonDebitCard) && !NameonDebitCard.equalsIgnoreCase(copyof_NameOnDebitCard)){
							xmlBody += "\n\t\t\t" +"<UpdatedNameOnDebitCard>"+NameonDebitCard+"</UpdatedNameOnDebitCard>";
						}
						if(PEPMatchStatus != null && !"".equalsIgnoreCase(PEPMatchStatus))
						{
							xmlBody += "\n\t\t\t" +"<PEPMatchStatus>"+PEPMatchStatus+"</PEPMatchStatus>";
						}
						if(RCIF_ID != null && !"".equalsIgnoreCase(RCIF_ID)){
							xmlBody += "\n\t\t\t" +"<RCIFID>"+RCIF_ID+"</RCIFID>";
						}
						xmlBody += "\n\t\t" +"</SignatoryInfoDetailsFromBPM>";
					
					}
				}
				xmlBody += "\n\t\t</UpdateProspectDetailsFromBPM>";
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: Info chanfe and Sole LLC to Sole "  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Sole to Sole LLC and No Correction")){
			try{
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "SOLE_TO_SOLELLC");
				if(Persona != null && !"".equalsIgnoreCase(Persona) && !Persona.equalsIgnoreCase(copyof_Persona)){
					xmlBody += "\n\t\t" +"<Persona>" + Persona + "</Persona>";
				}
				if(CompanyCategory != null && !"".equalsIgnoreCase(CompanyCategory) && !CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory)){
					xmlBody += "\n\t\t" +"<CompanyCategory>" + CompanyCategory + "</CompanyCategory>";
				}
				if(CompanyPEP != null && !"".equalsIgnoreCase(CompanyPEP)){
					xmlBody += "\n\t\t" +"<CompanyPEPMatchStatus>" + CompanyPEP + "</CompanyPEPMatchStatus>";
				}
				int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size);
				xmlBody += "\n\t\t<UpdateProspectDetailsFromBPM>";
				for (int i = 0; i < size; i++)
				{
					String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 0);
					DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
					String PEPMatchStatus = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 33);
					DBO.mLogger.debug("value of PEPMatchStatus: " + PEPMatchStatus );
					String RCIF_ID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 1);
					DBO.mLogger.debug("value of RCIF_ID: " + RCIF_ID );
					if(PEPMatchStatus != null && !"".equalsIgnoreCase(PEPMatchStatus))
					{
						xmlBody += "\n\t\t" + "<SignatoryInfoDetailsFromBPM>";
						if(RelatedPartyID != null && !"".equalsIgnoreCase(RelatedPartyID)){
							xmlBody += "\n\t\t\t" +"<RelatedPartyID>"+RelatedPartyID+"</RelatedPartyID>";
						}
						xmlBody += "\n\t\t\t" +"<PEPMatchStatus>"+PEPMatchStatus+"</PEPMatchStatus>";
						if(RCIF_ID != null && !"".equalsIgnoreCase(RCIF_ID)){
							xmlBody += "\n\t\t\t" +"<RCIFID>"+RCIF_ID+"</RCIFID>";
						}
						xmlBody += "\n\t\t" +"</SignatoryInfoDetailsFromBPM>";
					}
				}
				xmlBody += "\n\t\t</UpdateProspectDetailsFromBPM>";
				
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  :::::  Sole to Sole LLC "  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Sole to Sole LLC and Information Correction")){
			try{
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "SOLE_TO_SOLELLC_INFO_CORRECTION");
				
				if(Persona != null && !"".equalsIgnoreCase(Persona) && !Persona.equalsIgnoreCase(copyof_Persona)){
					xmlBody += "\n\t\t" +"<Persona>" + Persona + "</Persona>";
				}
				if(CompanyCategory != null && !"".equalsIgnoreCase(CompanyCategory) && !CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory)){
					xmlBody += "\n\t\t" +"<CompanyCategory>" + CompanyCategory + "</CompanyCategory>";
				}
				if(CompanyPEP != null && !"".equalsIgnoreCase(CompanyPEP)){
					xmlBody += "\n\t\t" +"<CompanyPEPMatchStatus>" + CompanyPEP + "</CompanyPEPMatchStatus>";
				}
				xmlBody  += "\n\t\t<UpdateProspectDetailsFromBPM>";
				
				if(TLNumber != null && !TLNumber.equalsIgnoreCase("") && !TLNumber.equalsIgnoreCase(copyof_TL_Number)){
					xmlBody += "\n\t\t" +"<UpdatedTLNumber>" + TLNumber + "</UpdatedTLNumber>";
				}
				if(TradeName != null && !TradeName.equalsIgnoreCase("") && !TradeName.equalsIgnoreCase(copyof_TradeName)){
					xmlBody += "\n\t\t" +"<UpdatedTradeName>" + TradeName + "</UpdatedTradeName>";
				}
				if(TLIssuingAuthority != null && !TLIssuingAuthority.equalsIgnoreCase("") && !TLIssuingAuthority.equalsIgnoreCase(copyof_TL_Issuing_Authority)){
					xmlBody += "\n\t\t" +"<UpdatedLicenseIssuingAuthority>" + TLIssuingAuthority + "</UpdatedLicenseIssuingAuthority>";
				}
				if(TLIssusingAuthorithyEmirate != null && !TLIssusingAuthorithyEmirate.equalsIgnoreCase("") && !TLIssusingAuthorithyEmirate.equalsIgnoreCase(copyof_TL_Issuing_Authority_Emirate)){
					xmlBody += "\n\t\t" +"<UpdatedLicenseIssuingAuthorityEmirate>" + TLIssusingAuthorithyEmirate + "</UpdatedLicenseIssuingAuthorityEmirate>";
				}
				if(TLExpiryDate != null && !TLExpiryDate.equalsIgnoreCase("") && !TLExpiryDate.equalsIgnoreCase(copyof_TL_ExpiryDate)){
					Date date = inputdateFormat.parse(TLExpiryDate);
					DBO.mLogger.debug("value of TLExpiryDate: "+TLExpiryDate );
					DBO.mLogger.debug("value of TLExpiryDate: "+outputdateFormat.format(date) );
					xmlBody += "\n\t\t" +"<UpdatedLicenseOrCOIExpiryDate>" + outputdateFormat.format(date) + "</UpdatedLicenseOrCOIExpiryDate>";
				}
				if(TLTypeOfOffice != null && !TLTypeOfOffice.equalsIgnoreCase("") && !TLTypeOfOffice.equalsIgnoreCase(copyof_TL_TypeofOffice)){
					xmlBody += "\n\t\t" +"<UpdatedTypeOfOffice>" + TLTypeOfOffice + "</UpdatedTypeOfOffice>";
				}
				if(CountryOfIncorporation != null && !CountryOfIncorporation.equalsIgnoreCase("") && !CountryOfIncorporation.equalsIgnoreCase(copyof_CountryOfIncoporation) ){
					xmlBody += "\n\t\t" +"<UpdatedCountryOfIncorporation>" + CountryOfIncorporation + "</UpdatedCountryOfIncorporation>";
				}
				if(DateOfIncorporation != null && !DateOfIncorporation.equalsIgnoreCase("") && !DateOfIncorporation.equalsIgnoreCase(copyof_DateofIncorporation)){
					Date date = inputdateFormat.parse(DateOfIncorporation);
					DBO.mLogger.debug("value of DateOfIncorporation: "+DateOfIncorporation );
					DBO.mLogger.debug("value of DateOfIncorporation: "+outputdateFormat.format(date) );
					xmlBody += "\n\t\t" +"<UpdatedDateOfIncorporation>" + outputdateFormat.format(date) + "</UpdatedDateOfIncorporation>";
				}
				if(CompanyName != null && !CompanyName.equalsIgnoreCase("") && !CompanyName.equalsIgnoreCase(copyof_CompanyName)){
					xmlBody += "\n\t\t" +"<UpdatedCompanyName>" + CompanyName + "</UpdatedCompanyName>";
				}
				if(CompanyShortName != null && !CompanyShortName.equalsIgnoreCase("") && !CompanyShortName.equalsIgnoreCase(copyof_CompanyShortName)){
					xmlBody += "\n\t\t" +"<UpdatedCompanyShortName>" + CompanyShortName + "</UpdatedCompanyShortName>";
				}
				if(NameOnChqBk != null && !NameOnChqBk.equalsIgnoreCase("") && !NameOnChqBk.equalsIgnoreCase(copyof_NameOnChequeBook)){
					xmlBody += "\n\t\t" +"<UpdatedNameOnChequeBook>" + NameOnChqBk + "</UpdatedNameOnChequeBook>";
				}
				
				int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size);
				
				for (int i = 0; i < size; i++)
				{
					String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 0);
					DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
					String PEPMatchStatus = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 33);
					DBO.mLogger.debug("value of PEPMatchStatus: " + PEPMatchStatus );
					String RCIF_ID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 1);
					DBO.mLogger.debug("value of RCIF_ID: " + RCIF_ID );
					String FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 2);
					DBO.mLogger.debug("value of FullName: " + FullName );
					String FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 5);
					DBO.mLogger.debug("value of FirstName: "+FirstName );
					String MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 6);
					DBO.mLogger.debug("value of MiddleName: " +MiddleName);
					String LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 7);
					DBO.mLogger.debug("value of LastName: "+LastName );
					String NameonDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 27);
					DBO.mLogger.debug("value of NameonDebitCard: "+NameonDebitCard );
					
					String tmp_date =  iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 3);
					String DOB = "";
					Date date = inputdateFormat.parse(tmp_date);
					DOB = outputdateFormat.format(date);
					DBO.mLogger.debug("value of DOB: "+tmp_date );
					DBO.mLogger.debug("value of DOB: "+DOB );
					String Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 8);
					DBO.mLogger.debug("value of Title: "+Title );
					String Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 9);
					DBO.mLogger.debug("value of Nationality: "+Nationality );
					String copyof_FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 36);
					DBO.mLogger.debug("value of copyof_FullName: "+copyof_FullName );
					String copyof_FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 37);
					DBO.mLogger.debug("value of copyof_FirstName: "+copyof_FirstName );
					String copyof_MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 38);
					DBO.mLogger.debug("value of copyof_MiddleName: "+copyof_MiddleName );
					String copyof_LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 39);
					DBO.mLogger.debug("value of copyof_LastName: "+copyof_LastName );
					String copyof_NameOnDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 40);
					
					String tmp_date1 = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 42);
					String copyof_DOB = "";
					Date date1 = inputdateFormat.parse(tmp_date1);
					DBO.mLogger.debug("value of DOB: "+tmp_date1 );
					copyof_DOB = outputdateFormat.format(date1);
					DBO.mLogger.debug("value of copyof_DOB: "+copyof_DOB );
					String copyof_Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 43);
					DBO.mLogger.debug("value of copyof_Title: "+copyof_Title );
					String copyof_Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 44);
					DBO.mLogger.debug("value of copyof_Nationality: "+copyof_Nationality );
					if(!FullName.equalsIgnoreCase(copyof_FullName) || !FirstName.equalsIgnoreCase(copyof_FirstName) ||
							!MiddleName.equalsIgnoreCase(copyof_MiddleName) || !LastName.equalsIgnoreCase(copyof_LastName) ||
							!NameonDebitCard.equalsIgnoreCase(copyof_NameOnDebitCard) || !DOB.equalsIgnoreCase(copyof_DOB) || 
							!Title.equalsIgnoreCase(copyof_Title) || !Nationality.equalsIgnoreCase(copyof_Nationality)){
						
						xmlBody += "\n\t\t" + "<SignatoryInfoDetailsFromBPM>";
						if(DOB != null && !"".equalsIgnoreCase(DOB)&& !DOB.equalsIgnoreCase(copyof_DOB)  ){
							xmlBody += "\n\t\t\t" +"<UpdatedDOB>"+DOB+"</UpdatedDOB>";
						}
						if(Title != null && !"".equalsIgnoreCase(Title) && !Title.equalsIgnoreCase(copyof_Title)){
							xmlBody += "\n\t\t\t" +"<UpdatedTitle>"+Title+"</UpdatedTitle>";
						}
						if(Nationality != null && !"".equalsIgnoreCase(Nationality) && !Nationality.equalsIgnoreCase(copyof_Nationality)){
							xmlBody += "\n\t\t\t" +"<UpdatedNationality>"+Nationality+"</UpdatedNationality>";
						}
						xmlBody += "\n\t\t\t" +"<RelatedPartyID>"+RelatedPartyID+"</RelatedPartyID>";
						if(FullName != null && !"".equalsIgnoreCase(FullName) && !FullName.equalsIgnoreCase(copyof_FullName)){
							xmlBody += "\n\t\t\t" +"<UpdatedFullName>"+FullName+"</UpdatedFullName>";
						}
						if(FirstName != null && !"".equalsIgnoreCase(FirstName) && !FirstName.equalsIgnoreCase(copyof_FirstName)){
							xmlBody += "\n\t\t\t" +"<UpdatedFirstName>"+FirstName+"</UpdatedFirstName>";
						}
						if(MiddleName != null && !"".equalsIgnoreCase(MiddleName) && !MiddleName.equalsIgnoreCase(copyof_MiddleName)){
							xmlBody += "\n\t\t\t" +"<UpdatedMiddleName>"+MiddleName+"</UpdatedMiddleName>";
						}
						if(LastName != null && !"".equalsIgnoreCase(LastName) && !LastName.equalsIgnoreCase(copyof_LastName)){
							xmlBody += "\n\t\t\t" +"<UpdatedLastName>"+LastName+"</UpdatedLastName>";
						}
						if(NameonDebitCard != null && !"".equalsIgnoreCase(NameonDebitCard) && !NameonDebitCard.equalsIgnoreCase(copyof_NameOnDebitCard)){
							xmlBody += "\n\t\t\t" +"<UpdatedNameOnDebitCard>"+NameonDebitCard+"</UpdatedNameOnDebitCard>";
						}
						if(PEPMatchStatus != null && !"".equalsIgnoreCase(PEPMatchStatus))
						{
							xmlBody += "\n\t\t\t" +"<PEPMatchStatus>"+PEPMatchStatus+"</PEPMatchStatus>";
						}
						if(RCIF_ID != null && !"".equalsIgnoreCase(RCIF_ID)){
							xmlBody += "\n\t\t\t" +"<RCIFID>"+RCIF_ID+"</RCIFID>";
						}
						xmlBody += "\n\t\t" +"</SignatoryInfoDetailsFromBPM>";
					
					}
				}
				xmlBody += "\n\t\t</UpdateProspectDetailsFromBPM>";
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("xmlBody "  + xmlBody);
				DBO.mLogger.info("Notify XML  ::::: Info chanfe and  Sole to Sole LLC "  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Sole to any other")){
			try{
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "SOLE_TO_OTHER");
				
				if(Persona != null && !"".equalsIgnoreCase(Persona) && !Persona.equalsIgnoreCase(copyof_Persona)){
					xmlBody += "\n\t\t" +"<Persona>" + Persona + "</Persona>";
				}
				if(CompanyCategory != null && !"".equalsIgnoreCase(CompanyCategory) && !CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory)){
					xmlBody += "\n\t\t" +"<CompanyCategory>" + CompanyCategory + "</CompanyCategory>";
				}
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: Info Change and Sole to other "  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Additional Information Required from Customer")){
			try{
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "ADDNL_INFO_REQ_CUST");
				
				try {
					
					int size =iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS").size();
					DBO.mLogger.debug("size of Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS: " + size);
					int counterInfoGR = 0;
					int counterDocGR = 0;
					//y handling start 
					JSONArray output1 = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS");
					JSONArray output2 = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNL_DOC_REQUIRED");
					if(!output1.isEmpty()){
						for(int i =0; i<output1.size(); i++){
							String str_outputData = output1.get(i).toString();
							DBO.mLogger.debug(" JSONArray<Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS> output " + str_outputData);
							if(str_outputData.contains("IsQuerySentToDEH\":\"Y\"")){
								DBO.mLogger.debug("  This time IsQuerySentToDEH is Y " );
								counterInfoGR +=1;
							 }
						}
					}
					if(!output2.isEmpty()){
						for(int i =0; i<output2.size(); i++){
							String str_outputData = output2.get(i).toString();
							DBO.mLogger.debug(" JSONArray<Q_USR_0_DBO_ADDNL_DOC_REQUIRED> output " + str_outputData);
							if(str_outputData.contains("IsDocSentToDEH\":\"Y\"")){
								DBO.mLogger.debug("  This time IsDocSentToDEH is Y " );
								counterDocGR +=1;
							 }
						}
					}
					//y handling end 
					for (int i = counterInfoGR; i < size; i++)
					{
						String queryUniqueID = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS", i, 0);
						String queryCategory = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS", i, 1);
						String queryType = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS", i, 2);
						String queryRemarks = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS", i, 3);
						
						String Query = "SELECT TOP 1 CUSTOMER_MESSAGE FROM USR_0_DBO_ADDNL_INFO_CATEGORY_MASTER"
								+ " WITH(NOLOCK) WHERE SUB_CATEGORY='"+queryType+"' and"
								+ " MASTER_CATEGORY = '"+queryCategory+"' and ISACTIVE='Y'";
						String custMsg_queryType = "";
						List<List<String>> lstcustMsg_queryType = iformObj.getDataFromDB(Query);
		 				DBO.mLogger.info("Result  ..." + lstcustMsg_queryType);
		 				List<String> arr1 =  lstcustMsg_queryType.get(0);
		 				custMsg_queryType =  arr1.get(0);
		 				DBO.mLogger.debug("value of custMsg_queryType " + custMsg_queryType);
						
						xmlBody += "\n\t\t" +"<AdditionalInfoDetailsFromBPM>"+ 
								"\n\t\t\t" +"<QueryUniqueID>"+queryUniqueID+"</QueryUniqueID>"+ 
								"\n\t\t\t" +"<QueryCategory>"+queryCategory+"</QueryCategory>"+ 
								"\n\t\t\t" +"<QueryType>"+custMsg_queryType+"</QueryType>"+ 
								"\n\t\t\t" +"<QueryRemarks>"+queryRemarks+"</QueryRemarks>"+ 
								/*"\n\t\t\t" +"<Freefield1>"+""+"</Freefield1>"+ 
								"\n\t\t\t" +"<Freefield2>"+""+"</Freefield2>"+ 
								"\n\t\t\t" +"<Freefield3>"+""+"</Freefield3>"+ 
								"\n\t\t\t" +"<Freefield4>"+""+"</Freefield4>"+ 
								"\n\t\t\t" +"<Freefield5>"+""+"</Freefield5>"+*/ 
								"\n\t\t" +"</AdditionalInfoDetailsFromBPM>";
					}
					
					int size1 =iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNL_DOC_REQUIRED").size();
					DBO.mLogger.debug("size of Q_USR_0_DBO_ADDNL_DOC_REQUIRED: " + size1);
					
					for (int i = counterDocGR; i < size1; i++)
					{
						String docUniqueID = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED", i, 0);
						String documentcategory = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED", i, 1);
						String documentType = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED", i, 2);
						String docRemarks = iformObj.getTableCellValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED", i, 3);
						
						xmlBody += "\n\t\t" +"<AdditionalDocumentDetailsFromBPM>"+
								"\n\t\t\t" +"<DocumentUniqueId>"+docUniqueID+"</DocumentUniqueId>"+
								"\n\t\t\t" +"<DocumentCategory>"+documentcategory+"</DocumentCategory>"+
								"\n\t\t\t" +"<DocumentType>"+documentType+"</DocumentType>"+ 
								"\n\t\t\t" +"<DocumentRemarks>"+docRemarks+"</DocumentRemarks>"+
								/*"\n\t\t\t" +"<Freefield1>"+""+"</Freefield1>"+
								"\n\t\t\t" +"<Freefield2>"+""+"</Freefield2>"+ 
								"\n\t\t\t" +"<Freefield3>"+""+"</Freefield3>"+
								"\n\t\t\t" +"<Freefield4>"+""+"</Freefield4>"+ 
								"\n\t\t\t" +"<Freefield5>"+""+"</Freefield5>"+*/
								"\n\t\t" +"</AdditionalDocumentDetailsFromBPM>";
					}
					
				} catch (Exception ex) {
					DBO.mLogger.debug("Exception " + ex);
				}
				
				
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: Additional Info Required "  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Decline")){
			try{
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "WI_STATUS");
				xmlBody += "\n\t\t" +"<WorkItemStatus>" + "DECLINE" + "</WorkItemStatus>";
				int size2 =iformObj.getDataFromGrid("Q_USR_0_DBO_DECLINE_REJECT_DTLS").size();
				DBO.mLogger.debug("size of Q_USR_0_DBO_DECLINE_REJECT_DTLS: " + size2);
				
				for (int i = 0; i < size2; i++)
				{
					String declineReasonID = iformObj.getTableCellValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS", i, 0);
					String declineCategory = iformObj.getTableCellValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS", i, 1);
					String declineType = iformObj.getTableCellValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS", i, 2);
					String declineRemarks = iformObj.getTableCellValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS", i, 3);
					String declineMainCategory = iformObj.getTableCellValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS", i, 4);
					String custMsg_queryType = iformObj.getTableCellValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS", i, 5);
					
					/*String Query = "SELECT TOP 1 CUSTOMER_MESSAGE FROM USR_0_DBO_DECLINE_CATEGORY_MASTER"
							+ " WITH(NOLOCK) WHERE SUB_CATEGORY='"+declineType+"' and"
							+ " MASTER_CATEGORY = '"+declineCategory+"' and ISACTIVE='Y'";
					String custMsg_queryType = "";
					List<List<String>> lstcustMsg_queryType = iformObj.getDataFromDB(Query);
	 				DBO.mLogger.info("Result  ..." + lstcustMsg_queryType);
	 				List<String> arr1 =  lstcustMsg_queryType.get(0);
	 				custMsg_queryType =  arr1.get(0);
	 				DBO.mLogger.debug("value of custMsg_queryType " + custMsg_queryType);*/
					
					xmlBody += "\n\t\t" +"<DeclineReasonDetailsFromBPM>"+ 
							"\n\t\t\t" +"<DeclinereasonId>"+declineReasonID+"</DeclinereasonId>"+ 
							"\n\t\t\t" +"<DeclineCategory>"+declineCategory+"</DeclineCategory>"+
							"\n\t\t\t" +"<DeclineROMessage>"+declineType+"</DeclineROMessage>"+
							"\n\t\t\t" +"<DeclineCustMessage>"+custMsg_queryType+"</DeclineCustMessage>"+
							"\n\t\t\t" +"<DeclineRemarks>"+declineRemarks+"</DeclineRemarks>"+ 
							"\n\t\t\t" +"<Freefield1>"+declineMainCategory+"</Freefield1>"+
							/*"\n\t\t\t" +"<Freefield2>"+""+"</Freefield2>"+
							"\n\t\t\t" +"<Freefield3>"+""+"</Freefield3>"+
							"\n\t\t\t" +"<Freefield4>"+""+"</Freefield4>"+ 
							"\n\t\t\t" +"<Freefield5>"+""+"</Freefield5>"+ */
							"\n\t\t" +"</DeclineReasonDetailsFromBPM>";
				}
				
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML ::::: Decline"  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else if(DECISION.equalsIgnoreCase("Correction required at Front End")){
			try{
				DBO.mLogger.info("Notify XML  ::::: no change before replace"  + notify_xml_dbo);
				notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "FE_CORRECTION");
				
				notify_xml_dbo  = notify_xml_dbo.replace("#body#", xmlBody);
				DBO.mLogger.info("Notify XML  ::::: xmlBody"  + xmlBody);
				
				DBO.mLogger.info("Notify XML  ::::: no change after replace"  + notify_xml_dbo);
				 
			}catch(Exception e){
				DBO.mLogger.info("Exception :::::  "  + e.getMessage());
			}
		
		}else{
			notify_xml_dbo  = notify_xml_dbo.replace("#Subprocess_Name#", "");
			notify_xml_dbo  = notify_xml_dbo.replace("#body#", "");
		}
		
		

			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", Inside DBO NOTIFY CALL control--");
			CallName = "DEH_NOTIFY";
			DBO.mLogger.debug(
					"DBO NOTIFY CALL - WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj));

			finalXml = new StringBuilder(notify_xml_dbo);
			DBO.mLogger.debug("DBO NOTIFY CALL - notify_xml_dbo : " + notify_xml_dbo);
			DBO.mLogger.debug("DBO NOTIFY CALL - finalXml : " + finalXml );
			

			mqInputRequest = getMQInputXML(sessionID, cabinetName, wi_name, ws_name, userName, finalXml);
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", mqInputRequest for DecTech call" + mqInputRequest);
			DBO.mLogger.debug("DBO NOTIFY CALL - mqInputRequest : " + mqInputRequest );
			
		try {

			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", userName " + userName);
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", sessionID " + sessionID);

			String sMQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DBO' and CallingSource = 'Form'";
			List<List<String>> outputMQXML = iformObj.getDataFromDB(sMQuery);
			
			if (!outputMQXML.isEmpty()) {
				
				socketServerIP = outputMQXML.get(0).get(0);
				DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
						+ getActivityName(iformObj) + ", socketServerIP " + socketServerIP);
				socketServerPort = Integer.parseInt(outputMQXML.get(0).get(1));
				DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
						+ getActivityName(iformObj) + ", socketServerPort " + socketServerPort);
				if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", Inside serverIP Port " + socketServerPort
							+ "-socketServerIP-" + socketServerIP);
					socket = new Socket(socketServerIP, socketServerPort);
					// new Code added by Deepak to set connection timeout
					int connection_timeout = 60;
					try {
						connection_timeout = 70;
					} catch (Exception e) {
						connection_timeout = 60;
					}

					socket.setSoTimeout(connection_timeout * 1000);
					out = socket.getOutputStream();
					socketInputStream = socket.getInputStream();
					dout = new DataOutputStream(out);
					din = new DataInputStream(socketInputStream);
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", dout " + dout);
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", din " + din);
					mqOutputResponse = "";

					if (mqInputRequest != null && mqInputRequest.length() > 0) {
						int outPut_len = mqInputRequest.getBytes("UTF-16LE").length;
						DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
								+ getActivityName(iformObj) + ", Final XML output len: " + outPut_len + "");
						mqInputRequest = outPut_len + "##8##;" + mqInputRequest;
						DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
								+ getActivityName(iformObj) + ", MqInputRequest" + "Input Request Bytes : "
								+ mqInputRequest.getBytes("UTF-16LE"));
						dout.write(mqInputRequest.getBytes("UTF-16LE"));
						dout.flush();
					}

					byte[] readBuffer = new byte[500];
					int num = din.read(readBuffer);
					if (num > 0) {

						byte[] arrayBytes = new byte[num];
						System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
						mqOutputResponse = mqOutputResponse + new String(arrayBytes, "UTF-16LE");
						DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
								+ getActivityName(iformObj) + ", mqOutputResponse/message ID :  " + mqOutputResponse);

						mqOutputResponse = getOutWtthMessageID("", iformObj, mqOutputResponse);

						if (mqOutputResponse.contains("&lt;")) {
							mqOutputResponse = mqOutputResponse.replaceAll("&lt;", "<");
							mqOutputResponse = mqOutputResponse.replaceAll("&gt;", ">");

						}
					}
					socket.close();
					return mqOutputResponse;

				} else {
					DBO.mLogger
							.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
									+ ", SocketServerIp and SocketServerPort is not maintained " + "");
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ", SocketServerIp is not maintained " + socketServerIP);
					DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
							+ getActivityName(iformObj) + ",  SocketServerPort is not maintained " + socketServerPort);
					return "MQ details not maintained";
				}
			} else {
				DBO.mLogger
						.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
								+ ", SOcket details are not maintained in NG_BPM_MQ_TABLE table" + "");
				return "MQ details not maintained";
			}

		} catch (Exception e) {
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", Exception Occured Mq_connection_CC" + e.getStackTrace());
			return "";
		} finally {
			try {
				if (out != null) {

					out.close();
					out = null;
				}
				if (socketInputStream != null) {

					socketInputStream.close();
					socketInputStream = null;
				}
				if (dout != null) {

					dout.close();
					dout = null;
				}
				if (din != null) {

					din.close();
					din = null;
				}
				if (socket != null) {
					if (!socket.isClosed()) {
						socket.close();
					}
					socket = null;
				}
			} catch (Exception e) {

				DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: "
						+ getActivityName(iformObj) + ", Final Exception Occured Mq_connection_CC" + e.getStackTrace());

			}
		}
	}
	
	public String getOutWtthMessageID(String callName, IFormReference iformObj, String message_ID) {
		String outputxml = "";
		try {
			DBO.mLogger.debug("getOutWtthMessageID - callName :" + callName);

			String wi_name = getWorkitemName(iformObj);
			String str_query = "select OUTPUT_XML from " + XMLLOG_HISTORY + " with (nolock) where MESSAGE_ID ='"
					+ message_ID + "' and WI_NAME = '" + wi_name + "'";
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", inside getOutWtthMessageID str_query: " + str_query);
			List<List<String>> result = iformObj.getDataFromDB(str_query);
			// below code added by nikhil 18/10 for Connection timeout
			String Integration_timeOut = "100";
			int Loop_wait_count = 10;
			try {
				Loop_wait_count = Integer.parseInt(Integration_timeOut);
			} catch (Exception ex) {
				Loop_wait_count = 10;
			}

			for (int Loop_count = 0; Loop_count < Loop_wait_count; Loop_count++) {
				if (result.size() > 0) {
					outputxml = result.get(0).get(0);
					break;
				} else {
					Thread.sleep(1000);
					result = iformObj.getDataFromDB(str_query);
				}
			}

			if ("".equalsIgnoreCase(outputxml)) {
				outputxml = "Error";
			}
			DBO.mLogger.debug("This is output xml from DB");
			String outputxmlMasked = outputxml;
			
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", getOutWtthMessageID" + outputxmlMasked);
		} catch (Exception e) {
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", Exception BTurred in getOutWtthMessageID" + e.getMessage());
			DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
					+ ", Exception BTurred in getOutWtthMessageID" + e.getStackTrace());
			outputxml = "Error";
		}
		return outputxml;
	}
	
	private static String getMQInputXML(String sessionID, String cabinetName, String wi_name, String ws_name,
			String userName, StringBuilder final_xml) {
		DBO.mLogger.debug("inside getMQInputXML function");
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>" + XMLLOG_HISTORY + "</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(final_xml);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		return strBuff.toString();
	}
	
}
