package com.newgen.iforms.user;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.iforms.custom.IFormReference;

public class DBO_IntroDone extends DBOCommon{
	
	private String WI_Name = null;
	private String activityName = null;
	private IFormReference giformObj = null;
	@SuppressWarnings("unchecked")
	public String onIntroduceDone(IFormReference iformObj,String controlName,String data) throws IOException
	{
		DBO.mLogger.info("Inside onIntroduceDone...");
		String strReturn="";
		WI_Name=getWorkitemName(iformObj);
		activityName=iformObj.getActivityName();
		giformObj=iformObj;
		String entryDateTime = "";
		DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", This is DBO_IntroDone");
		String DECISION = (String) iformObj.getValue("DECISION");
		String Persona = (String) iformObj.getValue("Persona");
		String copyof_Persona = (String) iformObj.getValue("copyof_Persona");
		DBO.mLogger.debug("value of Persona: "+Persona );
		DBO.mLogger.debug("value of copyof_Persona: "+copyof_Persona );
		
		String TLNumber = (String) iformObj.getValue("TLNumber");
		if (TLNumber == null) {
			TLNumber = "";
		}
		String TradeName = (String) iformObj.getValue("TradeName");
		if (TradeName == null) {
			TradeName = "";
		}
		String TLIssuingAuthority = (String) iformObj.getValue("TLIssuingAuthority");
		if (TLIssuingAuthority == null) {
			TLIssuingAuthority = "";
		}
		String TLIssusingAuthorithyEmirate = (String) iformObj.getValue("TLIssusingAuthorithyEmirate");
		if (TLIssusingAuthorithyEmirate == null) {
			TLIssusingAuthorithyEmirate = "";
		}
		String TLExpiryDate = (String) iformObj.getValue("TLExpiryDate");
		if (TLExpiryDate == null) {
			TLExpiryDate = "";
		}
		String TLTypeOfOffice = (String) iformObj.getValue("TLTypeOfOffice");
		if (TLTypeOfOffice == null) {
			TLTypeOfOffice = "";
		}
		String DateOfIncorporation = (String) iformObj.getValue("DateOfIncorporation");
		if (DateOfIncorporation == null) {
			DateOfIncorporation = "";
		}
		String CompanyName = (String) iformObj.getValue("CompanyName");
		if (CompanyName == null) {
			CompanyName = "";
		}
		String CompanyShortName = (String) iformObj.getValue("CompanyShortName");
		if (CompanyShortName == null) {
			CompanyShortName = "";
		}
		String NameOnChqBk = (String) iformObj.getValue("NameOnChqBk");
		if (NameOnChqBk == null) {
			NameOnChqBk = "";
		}
		String CountryOfIncorporation = (String) iformObj.getValue("Q_RB_DBO_TXNTABLE_CountryOfIncorporation");
		if (CountryOfIncorporation == null) {
			CountryOfIncorporation = "";
		}
		String CompanyCategory = (String) iformObj.getValue("CompanyCategory");
		if (CompanyCategory == null) {
			CompanyCategory = "";
		}
		DBO.mLogger.debug("value of CompanyCategory: "+CompanyCategory );
		
		String copyof_TL_Number = (String) iformObj.getValue("copyof_TL_Number");
		if (copyof_TL_Number == null) {
			copyof_TL_Number = "";
		}
		String copyof_TradeName = (String) iformObj.getValue("copyof_TradeName");
		if (copyof_TradeName == null) {
			copyof_TradeName = "";
		}
		String copyof_TL_Issuing_Authority = (String) iformObj.getValue("copyof_TL_Issuing_Authority");
		if (copyof_TL_Issuing_Authority == null) {
			copyof_TL_Issuing_Authority = "";
		}
		String copyof_TL_Issuing_Authority_Emirate = (String) iformObj.getValue("copyof_TL_Issuing_Authority_Emirate");
		if (copyof_TL_Issuing_Authority_Emirate == null) {
			copyof_TL_Issuing_Authority_Emirate = "";
		}
		String copyof_TL_ExpiryDate = (String) iformObj.getValue("copyof_TL_ExpiryDate");
		if (copyof_TL_ExpiryDate == null) {
			copyof_TL_ExpiryDate = "";
		}
		String copyof_TL_TypeofOffice = (String) iformObj.getValue("copyof_TL_TypeofOffice");
		if (copyof_TL_TypeofOffice == null) {
			copyof_TL_TypeofOffice = "";
		}
		String copyof_DateofIncorporation = (String) iformObj.getValue("copyof_DateofIncorporation");
		if (copyof_DateofIncorporation == null) {
			copyof_DateofIncorporation = "";
		}
		String copyof_CompanyName = (String) iformObj.getValue("copyof_CompanyName");
		if (copyof_CompanyName == null) {
			copyof_CompanyName = "";
		}
		String copyof_CompanyShortName = (String) iformObj.getValue("copyof_CompanyShortName");
		if (copyof_CompanyShortName == null) {
			copyof_CompanyShortName = "";
		}
		String copyof_NameOnChequeBook = (String) iformObj.getValue("copyof_NameOnChequeBook");
		if (copyof_NameOnChequeBook == null) {
			copyof_NameOnChequeBook = "";
		}
		String copyof_CountryOfIncoporation = (String) iformObj.getValue("Q_RB_DBO_TXNTABLE_copyof_CountryOfIncoporation");
		if (copyof_CountryOfIncoporation == null) {
			copyof_CountryOfIncoporation = "";
		}
		String copyof_CompanyCategory = (String) iformObj.getValue("copyof_CompanyCategory");
		if (copyof_CompanyCategory == null) {
			copyof_CompanyCategory = "";
		}
		DBO.mLogger.debug("value of copyof_CompanyCategory: "+copyof_CompanyCategory );
		
				if(controlName.equals("DEH_NotifyCall")){
					return new DBO_NotifyDEH().onevent(iformObj, controlName, data);
				}
				
				
		
				if(activityName.equalsIgnoreCase("STP_Operator")){
					//mandatory Rel Party values change start
					if ("GridEmptyValCheck".equals(controlName)&& !DECISION.equalsIgnoreCase("Additional Information Required from Customer") &&
							!DECISION.equalsIgnoreCase("Decline") && !DECISION.equalsIgnoreCase("Assign To")
							&& !DECISION.equalsIgnoreCase("Correction required at Front End") && 
							!DECISION.equalsIgnoreCase("Sole to any other")) {
						
						int size1 =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
						DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size1);
						
						for (int j = 0; j < size1; j++)
						{
							String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 0);
							DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
							String RelatedPartyCIF_ID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 1);
							DBO.mLogger.debug("value of RelatedPartyCIF_ID: " + RelatedPartyCIF_ID );
							String FullNameRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 2);
							DBO.mLogger.debug("value of FullName: " + FullNameRel );
							String FirstNameRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 5);
							DBO.mLogger.debug("value of FirstName: "+FirstNameRel );
							String MiddleNameRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 6);
							DBO.mLogger.debug("value of MiddleName: " +MiddleNameRel);
							String LastNameRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 7);
							DBO.mLogger.debug("value of LastName: "+LastNameRel );
							String DebitCardReqRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 26);
							DBO.mLogger.debug("value of DebitCardReqRel: "+DebitCardReqRel );
							String NameonDebitCardRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 27);
							DBO.mLogger.debug("value of NameonDebitCard: "+NameonDebitCardRel );
							String DOBRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 3);
							DBO.mLogger.debug("value of DOB: "+DOBRel );
							String TitleRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 8);
							DBO.mLogger.debug("value of Title: "+TitleRel );
							String NationalityRel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 9);
							DBO.mLogger.debug("value of Nationality: "+NationalityRel );
							String VerifiedAddressProof1Rel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 55);
							DBO.mLogger.debug("value of VerifiedAddressProof1Rel: "+VerifiedAddressProof1Rel );
							String VerifiedManualSOF1Rel = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 59);
							DBO.mLogger.debug("value of VerifiedManualSOF1Rel: "+VerifiedManualSOF1Rel );
							if(RelatedPartyCIF_ID.equalsIgnoreCase("") || RelatedPartyCIF_ID == null){
								DBO.mLogger.debug("Kindly select RCIF ID for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly select RCIF ID for the Related Party whose ID is :  " + RelatedPartyID;
							}
							else if(FullNameRel.equalsIgnoreCase("") || FullNameRel == null){
								DBO.mLogger.debug("Kindly fill Full Name for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill Full Name for the Related Party whose ID is :  " + RelatedPartyID;
							}else if(FirstNameRel.equalsIgnoreCase("") || FirstNameRel == null){
								DBO.mLogger.debug("Kindly fill First Name for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill First Name for the Related Party whose ID is :  " + RelatedPartyID;
							}else if(LastNameRel.equalsIgnoreCase("") || LastNameRel == null){
								DBO.mLogger.debug("Kindly fill Last Name for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill Last Name for the Related Party whose ID is :  " + RelatedPartyID;
							}else if(NameonDebitCardRel.equalsIgnoreCase("") || NameonDebitCardRel == null){
								if("Y".equalsIgnoreCase(DebitCardReqRel) || "Yes".equalsIgnoreCase(DebitCardReqRel))
								{
									DBO.mLogger.debug("Kindly fill Name on Debit Card for the Related Party whose ID is :  " + RelatedPartyID);
									return "Kindly fill Name on Debit Card for the Related Party whose ID is :  " + RelatedPartyID;
								}else{
									DBO.mLogger.debug("DebitCardReqRel is not req for the Related Party whose ID is :  " + RelatedPartyID);
								}
							}else if(NationalityRel.equalsIgnoreCase("") || NationalityRel == null){
								DBO.mLogger.debug("Kindly fill Nationality for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill Nationality for the Related Party whose ID is :  " + RelatedPartyID;
							}else if(DOBRel.equalsIgnoreCase("") || DOBRel == null){
								DBO.mLogger.debug("Kindly fill DOB for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill DOB for the Related Party whose ID is :  " + RelatedPartyID;
							}else if(TitleRel.equalsIgnoreCase("") || TitleRel == null){
								DBO.mLogger.debug("Kindly fill Title for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill Title for the Related Party whose ID is :  " + RelatedPartyID;
							}else if(VerifiedAddressProof1Rel.equalsIgnoreCase("") || VerifiedAddressProof1Rel == null){
								DBO.mLogger.debug("Kindly fill Verified Address Proof 1 for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill Verified Address Proof 1 for the Related Party whose ID is :  " + RelatedPartyID;
							}else if(VerifiedManualSOF1Rel.equalsIgnoreCase("") || VerifiedManualSOF1Rel == null){
								DBO.mLogger.debug("Kindly fill Verified Manual SOF 1 for the Related Party whose ID is :  " + RelatedPartyID);
								return "Kindly fill Verified Manual SOF 1  for the Related Party whose ID is :  " + RelatedPartyID;
							}
						}
						if("GridEmptyValCheck".equals(controlName)&& !DECISION.equalsIgnoreCase("Sole to Sole LLC and No Correction") &&
								!DECISION.equalsIgnoreCase("Sole to Sole LLC and Information Correction"))
						{
							for (int j = 0; j < size1; j++)
							{
								String RelatedPartyID = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 0);
								DBO.mLogger.debug("value of RelatedPartyID: " + RelatedPartyID );
								String PEP = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", j, 33);
								DBO.mLogger.debug("value of PEP: "+PEP );
								if(PEP.equalsIgnoreCase("") || PEP == null){
									DBO.mLogger.debug("Kindly fill PEP for the Related Party whose ID is :  " + RelatedPartyID);
									return "Kindly fill PEP for the Related Party whose ID is :  " + RelatedPartyID;
								}
							}
						}
					}	
					//mandatory Rel Party values change end 
					String RelPartyDataChange = "No Change";
					if ("ValidateSTPDec".equals(controlName)) {
						
						int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
						DBO.mLogger.debug("size of Q_USR_0_DBO_RelatedPartyGrid: " + size);
						
						for (int i = 0; i < size; i++)
						{
							String FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 2);
							if (FullName == null) {
								FullName = "";
							}
							DBO.mLogger.debug("value of FullName: " + FullName );
							String FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 5);
							if (FirstName == null) {
								FirstName = "";
							}
							DBO.mLogger.debug("value of FirstName: "+FirstName );
							String MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 6);
							if (MiddleName == null) {
								MiddleName = "";
							}
							DBO.mLogger.debug("value of MiddleName: " +MiddleName);
							String LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 7);
							if (LastName == null) {
								LastName = "";
							}
							DBO.mLogger.debug("value of LastName: "+LastName );
							String NameonDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 27);
							if (NameonDebitCard == null) {
								NameonDebitCard = "";
							}
							DBO.mLogger.debug("value of NameonDebitCard: "+NameonDebitCard );
							String DOB = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 3);
							if (DOB == null) {
								DOB = "";
							}
							DBO.mLogger.debug("value of DOB: "+DOB );
							String Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 8);
							if (Title == null) {
								Title = "";
							}
							DBO.mLogger.debug("value of Title: "+Title );
							String Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 9);
							if (Nationality == null) {
								Nationality = "";
							}
							DBO.mLogger.debug("value of Nationality: "+Nationality );
							String copyof_FullName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 36);
							if (copyof_FullName == null) {
								copyof_FullName = "";
							}
							DBO.mLogger.debug("value of copyof_FullName: "+copyof_FullName );
							String copyof_FirstName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 37);
							if (copyof_FirstName == null) {
								copyof_FirstName = "";
							}
							DBO.mLogger.debug("value of copyof_FirstName: "+copyof_FirstName );
							String copyof_MiddleName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 38);
							if (copyof_MiddleName == null) {
								copyof_MiddleName = "";
							}
							DBO.mLogger.debug("value of copyof_MiddleName: "+copyof_MiddleName );
							String copyof_LastName = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 39);
							if (copyof_LastName == null) {
								copyof_LastName = "";
							}
							DBO.mLogger.debug("value of copyof_LastName: "+copyof_LastName );
							String copyof_NameOnDebitCard = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 40);
							if (copyof_NameOnDebitCard == null) {
								copyof_NameOnDebitCard = "";
							}
							DBO.mLogger.debug("value of copyof_NameOnDebitCard: "+copyof_NameOnDebitCard );
							String copyof_DOB = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 42);
							if (copyof_DOB == null) {
								copyof_DOB = "";
							}
							DBO.mLogger.debug("value of copyof_DOB: "+copyof_DOB );
							String copyof_Title = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 43);
							if (copyof_Title == null) {
								copyof_Title = "";
							}
							DBO.mLogger.debug("value of copyof_Title: "+copyof_Title );
							String copyof_Nationality = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 44);
							if (copyof_Nationality == null) {
								copyof_Nationality = "";
							}
							DBO.mLogger.debug("value of copyof_Nationality: "+copyof_Nationality );
							
							if(!(FullName.equalsIgnoreCase(copyof_FullName) || FirstName.equalsIgnoreCase(copyof_FirstName) ||
									MiddleName.equalsIgnoreCase(copyof_MiddleName) || LastName.equalsIgnoreCase(copyof_LastName) ||
									NameonDebitCard.equalsIgnoreCase(copyof_NameOnDebitCard) || DOB.equalsIgnoreCase(copyof_DOB) ||
									Title.equalsIgnoreCase(copyof_Title) || Nationality.equalsIgnoreCase(copyof_Nationality))){
								RelPartyDataChange = "Change";
								DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid: changed" );
								break;
							}
							
						}
					// no change
					DBO.mLogger.info("Inside ValidateSTPDec...");
					if(TLNumber.equalsIgnoreCase(copyof_TL_Number) && TradeName.equalsIgnoreCase(copyof_TradeName) && TLIssuingAuthority.equalsIgnoreCase(copyof_TL_Issuing_Authority)
							&& TLIssusingAuthorithyEmirate.equalsIgnoreCase(copyof_TL_Issuing_Authority_Emirate) && TLExpiryDate.equalsIgnoreCase(copyof_TL_ExpiryDate)
							&& TLTypeOfOffice.equalsIgnoreCase(copyof_TL_TypeofOffice) && DateOfIncorporation.equalsIgnoreCase(copyof_DateofIncorporation)
							&& CompanyName.equalsIgnoreCase(copyof_CompanyName) && CompanyShortName.equalsIgnoreCase(copyof_CompanyShortName)
							&& NameOnChqBk.equalsIgnoreCase(copyof_NameOnChequeBook) && CountryOfIncorporation.equalsIgnoreCase(copyof_CountryOfIncoporation) && CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory) && Persona.equalsIgnoreCase(copyof_Persona) && "No Change".equalsIgnoreCase(RelPartyDataChange)){
						DBO.mLogger.info("Inside ValidateSTPDec...No Change");
						return "No Change";
					}
					// info change
					else if((!(TLNumber.equalsIgnoreCase(copyof_TL_Number) && TradeName.equalsIgnoreCase(copyof_TradeName) && TLIssuingAuthority.equalsIgnoreCase(copyof_TL_Issuing_Authority)
							&& TLIssusingAuthorithyEmirate.equalsIgnoreCase(copyof_TL_Issuing_Authority_Emirate) && TLExpiryDate.equalsIgnoreCase(copyof_TL_ExpiryDate)
							&& TLTypeOfOffice.equalsIgnoreCase(copyof_TL_TypeofOffice) && DateOfIncorporation.equalsIgnoreCase(copyof_DateofIncorporation)
							&& CompanyName.equalsIgnoreCase(copyof_CompanyName) && CompanyShortName.equalsIgnoreCase(copyof_CompanyShortName)
							&& NameOnChqBk.equalsIgnoreCase(copyof_NameOnChequeBook) && CountryOfIncorporation.equalsIgnoreCase(copyof_CountryOfIncoporation) && CompanyCategory.equalsIgnoreCase(copyof_CompanyCategory) && "No Change".equalsIgnoreCase(RelPartyDataChange))) && Persona.equalsIgnoreCase(copyof_Persona)){
						DBO.mLogger.info("Inside ValidateSTPDec...Info Change");
						return "Info Change";
					}
					// persona change
					else if(TLNumber.equalsIgnoreCase(copyof_TL_Number) && TradeName.equalsIgnoreCase(copyof_TradeName) && TLIssuingAuthority.equalsIgnoreCase(copyof_TL_Issuing_Authority)
							&& TLIssusingAuthorithyEmirate.equalsIgnoreCase(copyof_TL_Issuing_Authority_Emirate) && TLExpiryDate.equalsIgnoreCase(copyof_TL_ExpiryDate)
							&& TLTypeOfOffice.equalsIgnoreCase(copyof_TL_TypeofOffice) && DateOfIncorporation.equalsIgnoreCase(copyof_DateofIncorporation)
							&& CompanyName.equalsIgnoreCase(copyof_CompanyName) && CompanyShortName.equalsIgnoreCase(copyof_CompanyShortName)
							&& NameOnChqBk.equalsIgnoreCase(copyof_NameOnChequeBook) && CountryOfIncorporation.equalsIgnoreCase(copyof_CountryOfIncoporation) && "No Change".equalsIgnoreCase(RelPartyDataChange) && (!Persona.equalsIgnoreCase(copyof_Persona))){
						DBO.mLogger.info("Inside ValidateSTPDec...Persona Change");
						return "Persona Change";
					}
					// Persona and Info both change
					else if(!(TLNumber.equalsIgnoreCase(copyof_TL_Number) && TradeName.equalsIgnoreCase(copyof_TradeName) && TLIssuingAuthority.equalsIgnoreCase(copyof_TL_Issuing_Authority)
							&& TLIssusingAuthorithyEmirate.equalsIgnoreCase(copyof_TL_Issuing_Authority_Emirate) && TLExpiryDate.equalsIgnoreCase(copyof_TL_ExpiryDate)
							&& TLTypeOfOffice.equalsIgnoreCase(copyof_TL_TypeofOffice) && DateOfIncorporation.equalsIgnoreCase(copyof_DateofIncorporation)
							&& CompanyName.equalsIgnoreCase(copyof_CompanyName) && CompanyShortName.equalsIgnoreCase(copyof_CompanyShortName)
							&& NameOnChqBk.equalsIgnoreCase(copyof_NameOnChequeBook) && CountryOfIncorporation.equalsIgnoreCase(copyof_CountryOfIncoporation)  && "No Change".equalsIgnoreCase(RelPartyDataChange)) && !Persona.equalsIgnoreCase(copyof_Persona)){
						DBO.mLogger.info("Inside ValidateSTPDec...Persona and Info both Change");
						return "Persona and Info both Change";
					}
					else{
						DBO.mLogger.info("Inside ValidateSTPDec...invalid case");
						return "";
					}
				}}
		
				if("InsertIntoHistory".equals(controlName))
				{

					try {
						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons Grid Length is: "+data);
						/*String strRejectReasons="";
						String strRejectCodes = "";
						int rejectReasonGridSize=iformObj.getDataFromGrid("REJECT_REASON_GRID").size();
						if(rejectReasonGridSize==0)
						{
							String decision = (String) iformObj.getValue("Decision");
							if((decision.indexOf("Reject")!=-1) || (decision.indexOf("Reject to Initiator")!=-1))
							{
								return "Reject_Reason_Empty";
							}
							
						}
						for(int p=0;p<rejectReasonGridSize;p++)
						{
							String completeReason = null;
							completeReason = iformObj.getTableCellValue("REJECT_REASON_GRID", p, 0);
							DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Complete Reject Reasons" + completeReason);
							
							if (strRejectReasons == "")
							{						
								if(completeReason.indexOf("-")>-1)
								{
									strRejectCodes=completeReason.substring(0,completeReason.indexOf("-")).replace("(", "").replace(")", "");
									DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons code" + strRejectCodes);
									strRejectReasons=completeReason.substring(completeReason.indexOf("-")+1);
									DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons" + strRejectReasons);
								}
								else
								{
									strRejectReasons=completeReason;
									DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons else block" + strRejectReasons);
								}
							}	
							else
							{
								DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons 1" + strRejectReasons);						
								if(completeReason.indexOf("-")>-1)
								{
									strRejectCodes=strRejectCodes+"#"+completeReason.substring(0,completeReason.indexOf("-")).replace("(", "").replace(")", "");
									DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons code" + strRejectCodes);
									strRejectReasons=strRejectReasons+"#"+completeReason.substring(completeReason.indexOf("-")+1);
									DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons" + strRejectReasons);
								}
								else
								{
									strRejectReasons=strRejectReasons+"#"+completeReason;
									DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons else block2" + strRejectReasons);
								}
								
								DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Reject Reasons 2" + strRejectReasons);
							}
							
						}*/
						//DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Final reject reasons are: "+strRejectReasons);
						JSONArray jsonArray=new JSONArray();
						JSONObject obj=new JSONObject();
						Calendar cal = Calendar.getInstance();
					    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					    String strDate = sdf.format(cal.getTime());
					    String groupName = (String) iformObj.getValue("UserGroupSTP_Operator");
					    DBO.mLogger.info("groupName--" + groupName);
					    DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Final date for history is: "+strDate);
						obj.put("Date Time",strDate);
						obj.put("Workstep",activityName);
						obj.put("User Group",groupName);
						obj.put("User Name", iformObj.getUserName());
						obj.put("Decision",iformObj.getValue("DECISION"));
						obj.put("Remarks", iformObj.getValue("Remarks"));
						//obj.put("Reject Reasons", strRejectReasons);
						//obj.put("Reject Reason Codes", strRejectCodes);
						entryDateTime = (String)iformObj.getValue("EntryDateTime");
						//DBO.mLogger.info("Printing entry Date time "+entryDateTime);
						entryDateTime = convertDateFormat(entryDateTime,"yyyy-MM-dd HH:mm:ss","dd/MM/yyyy HH:mm:ss",iformObj);
						//DBO.mLogger.info("Printing entry Date time "+entryDateTime);
						DBO.mLogger.debug("value of ActionDateTime after add: "+strDate);
						DBO.mLogger.debug("value of EntryDateTime before add: "+iformObj.getValue("EntryDateTime"));
						DBO.mLogger.debug("value of EntryDateTime before add: "+entryDateTime);
						obj.put("Entry Date Time",iformObj.getValue("EntryDateTime"));
						DBO.mLogger.debug("value of EntryDateTime after add: "+iformObj.getValue("EntryDateTime"));
						DBO.mLogger.debug("value of EntryDateTime after add: "+entryDateTime);
							
						// To get latest decision seq ...
						int decisionSeq=0;
						String value="";
						try 
						{
							DBO.mLogger.info("To get latest decision sequence from history table ...");
							String Query = "select Top 1 decisionseqno from USR_0_DBO_WIHISTORY with(nolock) where winame='"+WI_Name+"' order by decisionseqno desc";
							DBO.mLogger.info("Query--" + Query);
							List lstDecisions = iformObj.getDataFromDB(Query);
							DBO.mLogger.info("Result  ..." + lstDecisions);
							if(lstDecisions != null)
							value = (String)((List) lstDecisions.get(0)).get(0);
							if(value != null && !"".equalsIgnoreCase(value))
							{
								decisionSeq=Integer.parseInt(value);
							}

						} 
						catch (Exception e)
						{
							DBO.mLogger.debug("WINAME: " + WI_Name + ", WSNAME: " + activityName+ ", Exception in getting latest seqNo: " + e.getMessage());
							return "";
						}
						obj.put("decisionSeqNo",decisionSeq+1);
						// To update decision seq in internal communication grid ...
						int sizeOfgrid =iformObj.getDataFromGrid("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS").size();
						DBO.mLogger.debug("size of Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS: " + sizeOfgrid);
						
						for (int j = 0; j < sizeOfgrid; j++)
						{
							String decMapedFlag = iformObj.getTableCellValue("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS", j, 4);
							if(!"Y".equalsIgnoreCase(decMapedFlag))
							{
								iformObj.setTableCellValue("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS", j, 4, "Y");
								iformObj.setTableCellValue("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS", j, 5, String.valueOf(decisionSeq+1));
							}
						}
						jsonArray.add(obj);
						iformObj.addDataToGrid("Q_USR_0_DBO_WIHISTORY", jsonArray);
						
						strReturn = "INSERTED";
						
						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", WI Histroy Added Successfully!");
					}
					catch (Exception e)
					{
						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", WI Histroy Not Added Successfully!: " + e.getMessage());
					}
				} 
				
				
		
		DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+controlName+", Returning");
		return strReturn;
	}

}
