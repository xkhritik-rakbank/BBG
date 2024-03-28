package com.newgen.iforms.user;

import java.util.List;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.integration.GetJSON;
import com.newgen.integration.SocketConnector;

public class DBO_ChangeHandler extends DBOCommon {

	private static final String EXCEPTION_OCCURED = null;
	private static final String UNHANDLED = null;
	private static final String SUCCESS = null;
	private static final String FAIL = null;
	private String WI_Name = null;
	private String WI_ID = null;
	private String actName = null;
	private String userName = null;
	private String inputString = null;
	private IFormReference giformObj = null;

	public String onChange(IFormReference iformObj, String control, String stringdata) {
		DBO.mLogger.info("Inside onChange method of DBO_ChangeHandler with control id-- " + control);
		WI_Name = getWorkitemName(iformObj);
		WI_ID=(iformObj).getObjGeneralData().getM_strWorkitemId();
		actName = iformObj.getActivityName();
		userName = iformObj.getUserName();
		String Persona =  (String) iformObj.getValue("Persona");
		String TLIssuingAuthority =  (String) iformObj.getValue("TLIssuingAuthority");
		inputString = stringdata;
		giformObj = iformObj;
		String decision =  (String) iformObj.getValue("DECISION");
		String Q_USR_0_DBO_RelatedPartyGrid_Title =  (String) iformObj.getValue("Q_USR_0_DBO_RelatedPartyGrid_Title");
		String Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq =  (String) iformObj.getValue("Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq");

		try {
			
			if("OnPersonaValChange".equalsIgnoreCase(control))
			{
				DBO.mLogger.debug("value of Persona inside change event " + Persona);
				if(Persona.equalsIgnoreCase("OTHER")){
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "disable", "false");
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "mandatory", "true");
				}else{
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "mandatory", "false");
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "disable", "true");
					iformObj.setValue("Q_RB_DBO_TXNTABLE_OtherPersona", "");
				}
			}
			else if("SetMainCategoryCustMsg".equalsIgnoreCase(control))
			{
	 			try {
	 				String declineCategory = (String) iformObj.getValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS_Decline_Category");
	 				String declineType = (String) iformObj.getValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS_Decline_Type");
	 				String query = "select top 1 MAIN_CATEGORY, CUSTOMER_MESSAGE from USR_0_DBO_DECLINE_CATEGORY_MASTER with (NOLOCK )"
						+ " where isActive = 'Y' and MASTER_CATEGORY = '"+declineCategory+"' and"
								+ " SUB_CATEGORY = '"+declineType+"'";
	 				DBO.mLogger.info("Query--" + query);
	 				List<List<String>> lstValue = iformObj.getDataFromDB(query);
	 				DBO.mLogger.info("Result  ..." + lstValue);
	 				List<String> arr1 = (List) lstValue.get(0);
	 				String mainCategory = arr1.get(0);
	 				String customerMessage = arr1.get(1);
	 				DBO.mLogger.debug("value of mainCategory before add: "+mainCategory);
	 				DBO.mLogger.debug("value of customerMessage before add: "+customerMessage);
	 				iformObj.setValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS_Decline_MainCategory", mainCategory);
	 				iformObj.setValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS_Decline_CustomerMessage", customerMessage);
	 				DBO.mLogger.debug("value of mainCategory after add: "+mainCategory);
	 				DBO.mLogger.debug("value of customerMessage after add: "+customerMessage);
	 				} catch (Exception e) {
	 				DBO.mLogger.debug("WINAME: " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
	 						+ ", Exception in SetMainCategoryCustMsg: " + e.getMessage());
	 				}
	            return "ValueAdded";
				
			}
			else if("getCompanyCategoryValuesonLoad".equalsIgnoreCase(control))
			{
				 String cc_status = "";
	 			 // to get cc values to insert in cc dropdown start
	 			try {
	 				String ccValOnLoad = (String) iformObj.getValue("CompanyCategory");
	 				DBO.mLogger.info("value ccValOnLoad on load  ..." + ccValOnLoad);
	 				DBO.mLogger.info("to get cc values to insert in cc dropdown start ...");
	 				String query = "select Description, Code from USR_0_DBO_CompanyCategory with (NOLOCK )"
	 						+ " where isActive = 'Y' and PersonaCode = '"+Persona+"' and (Zone = (select top 1 Zone"
	 						+ " from USR_0_DBO_TLIssueAuthorityMaster with(nolock) where IssuingAuthCode  = '"+TLIssuingAuthority+"')"
	 						+ " or Zone is null or Zone = '')";
	 				DBO.mLogger.info("Query--" + query);
	 				List<List<String>> lstValues = iformObj.getDataFromDB(query);
	 				DBO.mLogger.info("Result  ..." + lstValues);
	 				iformObj.clearCombo("CompanyCategory");
	 				String valueCode = "";
	 				String valueDesc = "";
	 				for (int i = 0; i < lstValues.size(); i++) {
	 					List<String> arr1 = (List) lstValues.get(i);
	 					valueDesc = arr1.get(0);
	 					valueCode = arr1.get(1);
	 					DBO.mLogger.info("Item to add in CompanyCategory combo  ..." + arr1);
	 					iformObj.addItemInCombo("CompanyCategory", valueDesc, valueCode ,valueDesc);
	 					if(!"".equalsIgnoreCase(ccValOnLoad) && valueCode.equalsIgnoreCase(ccValOnLoad)){
	 						DBO.mLogger.info("value ccValOnLoad on load  ..." + ccValOnLoad);
	 						DBO.mLogger.info("value valueDesc on load  ..." + valueDesc);
	 						DBO.mLogger.info("value valueCode on load  ..." + valueCode);
	 						iformObj.setValue("CompanyCategory", valueCode);
	 					}
	 					if(lstValues.size() == 1){
	 						DBO.mLogger.info("value valueDesc on load  ..." + valueDesc);
	 						DBO.mLogger.info("value valueCode on load  ..." + valueCode);
	 						iformObj.setValue("CompanyCategory", valueCode);
	 					}
	 				}
	 				cc_status = "inserted";
	 			} catch (Exception e) {
	 				DBO.mLogger.debug("WINAME: " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
	 						+ ", Exception in getting cc values to insert in cc dropdown: " + e.getMessage());
	 			}
	 		    // to call on change in js
	            return cc_status;
				
			}
			else if("getCompanyCategoryValues".equalsIgnoreCase(control))
			{
				 String cc_status = "";
	 			 // to get cc values to insert in cc dropdown start
	 			try {
	 				DBO.mLogger.info("to get cc values to insert in cc dropdown start ...");
	 				String query = "select Description, Code from USR_0_DBO_CompanyCategory with (NOLOCK )"
	 						+ " where isActive = 'Y' and PersonaCode = '"+Persona+"' and (Zone = (select top 1 Zone"
	 						+ " from USR_0_DBO_TLIssueAuthorityMaster with(nolock) where IssuingAuthCode  = '"+TLIssuingAuthority+"')"
	 						+ " or Zone is null or Zone = '')";
	 				DBO.mLogger.info("Query--" + query);
	 				List<List<String>> lstValues = iformObj.getDataFromDB(query);
	 				DBO.mLogger.info("Result  ..." + lstValues);
	 				iformObj.clearCombo("CompanyCategory");
	 				String valueCode = "";
	 				String valueDesc = "";
	 				for (int i = 0; i < lstValues.size(); i++) {
	 					List<String> arr1 = (List) lstValues.get(i);
	 					valueDesc = arr1.get(0);
	 					valueCode = arr1.get(1);
	 					DBO.mLogger.info("Item to add in CompanyCategory combo  ..." + arr1);
	 					iformObj.addItemInCombo("CompanyCategory", valueDesc, valueCode ,valueDesc);
	 					if(lstValues.size() == 1){
	 						DBO.mLogger.info("value valueDesc on load  ..." + valueDesc);
	 						DBO.mLogger.info("value valueCode on load  ..." + valueCode);
	 						iformObj.setValue("CompanyCategory", valueCode);
	 					}
	 				}
	 				cc_status = "inserted";
	 			} catch (Exception e) {
	 				DBO.mLogger.debug("WINAME: " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
	 						+ ", Exception in getting cc values to insert in cc dropdown: " + e.getMessage());
	 			}
	 		    // to call on change in js
	            return cc_status;
				
			}
			else if("Emirates_onTLIAChange".equalsIgnoreCase(control))
			{
				 String TL_status = "";
	 			try {
	 				DBO.mLogger.info("to insert TL_IA Emirates in dropdown start ...");
	 				String TL_IA = (String) iformObj.getValue("TLIssuingAuthority");
	 				DBO.mLogger.info("value of  TL_IA ..." +TL_IA);
	 				String query1 = "select distinct e.IssuingEmiratesName , e.IssuingEmiratesCode from "
	 						+ "USR_0_DBO_TLIssueEmiratesMaster e with(nolock) inner join "
	 						+ "USR_0_DBO_TLIssueAuthorityMaster a on e.IssuingEmiratesCode = a.EmiratesCode"
	 						+ " where a.IssuingAuthCode  = '"+TL_IA+"'";
	 				DBO.mLogger.info("Query--" + query1);
	 				List<List<String>> lstValues1 = iformObj.getDataFromDB(query1);
	 				DBO.mLogger.info("Result  ..." + lstValues1);
	 				iformObj.clearCombo("TLIssusingAuthorithyEmirate");
	 				String valueDesc1 = "";
	 				String valueCode1 = "";
	 				for (int i = 0; i < lstValues1.size(); i++) {
	 					List<String> arr1 = (List) lstValues1.get(i);
	 					valueDesc1 = arr1.get(0);
	 					valueCode1 = arr1.get(1);
	 					DBO.mLogger.info("Item to add in TL_IA Emirates combo  ..." + arr1);
	 					iformObj.addItemInCombo("TLIssusingAuthorithyEmirate", valueDesc1, valueCode1 , valueDesc1);
	 					if(lstValues1.size() == 1){
	 						DBO.mLogger.info("value valueDesc on load  ..." + valueDesc1);
	 						DBO.mLogger.info("value valueCode on load  ..." + valueCode1);
	 						iformObj.setValue("TLIssusingAuthorithyEmirate", valueCode1);
	 					}
	 				}
	 				TL_status = "inserted";
	 			} catch (Exception e) {
	 				DBO.mLogger.debug("WINAME: " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
	 						+ ", Exception in getting TLEmirates values to insert in TLEmirates dropdown: " + e.getMessage());
	 			}
	 		    // to call on change in js
	            return TL_status;
				
			}
			else if("Q_USR_0_DBO_RelatedPartyGrid_Title".equalsIgnoreCase(control))
			{
				DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_Title inside change event " + Q_USR_0_DBO_RelatedPartyGrid_Title);
				if(Q_USR_0_DBO_RelatedPartyGrid_Title.equalsIgnoreCase("Mrs") || Q_USR_0_DBO_RelatedPartyGrid_Title.equalsIgnoreCase("Ms")){
					DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_Title inside if " + Q_USR_0_DBO_RelatedPartyGrid_Title);
					iformObj.setValue("Q_USR_0_DBO_RelatedPartyGrid_Gender", "F");
				}else if(Q_USR_0_DBO_RelatedPartyGrid_Title.equalsIgnoreCase("Mr")) {
					DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_Title inside else " + Q_USR_0_DBO_RelatedPartyGrid_Title);
					iformObj.setValue("Q_USR_0_DBO_RelatedPartyGrid_Gender", "M");
				}
			}
			else if("DECISION".equalsIgnoreCase(control))
			{
				int gridRowCount_DeclineGR = iformObj.getDataFromGrid("Q_USR_0_DBO_DECLINE_REJECT_DTLS").size();
				int gridRowCount_InfoGR = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS").size();
				int gridRowCount_DocGR = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNL_DOC_REQUIRED").size();
				int gridRowCount_RpGR = iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				int gridRowCount_FircoGR = iformObj.getDataFromGrid("Q_USR_0_DBO_FIRCO_DTLS").size();
				
				String QueryIC_GR = "select count(*) as GRCount from USR_0_DBO_INTERNAL_CLRFCTN_DTLS with (nolock)  where"
						+ " WINAME = '" + WI_Name+ "' and IC_DEC_MAPID != 'Y'";
				DBO.mLogger.info("Query--" + QueryIC_GR);
				List<List<String>> lstVals= iformObj.getDataFromDB(QueryIC_GR);
				int gridRowCount_InterClrsGR = Integer.parseInt(lstVals.get(0).get(0).trim());
				DBO.mLogger.debug(" gridRowCount " + gridRowCount_InterClrsGR);
				
				if (actName.equalsIgnoreCase("STP_Operator")) 
				{
					if (!(decision.equalsIgnoreCase("") || decision.equalsIgnoreCase("Select"))) 
					{
					// To populate DECISIONDescription ...
						try {
							DBO.mLogger.info("To populate DECISIONs Description in  drop down  ...");
							String Query = "SELECT  DECISION_DESC FROM USR_0_DBO_DECISION_MASTER WITH(NOLOCK) WHERE WORKSTEP_NAME='" + actName
								+ "' and ISACTIVE='Y' and DECISION='" + decision+ "'";
							DBO.mLogger.info("Query--" + Query);
							List<List<String>> lstDecisions = iformObj.getDataFromDB(Query);
							DBO.mLogger.info("Result  ..." + lstDecisions);
							String value = lstDecisions.get(0).get(0);
							iformObj.setValue("DecisionDescription", value);

							} catch (Exception e) {
							DBO.mLogger.debug("WINAME: " + WI_Name + ", WSNAME: " + actName
								+ ", Exception in DecisionDescription load: " + e.getMessage());
							}
					}
					//Decision Desc Change end
					
					if(decision.equalsIgnoreCase("Additional Information Required from Customer")){
						iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						if(gridRowCount_DeclineGR != 0){
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						}else{
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
						}
						if(gridRowCount_InterClrsGR != 0){
							iformObj.setStyle("InternalClarification", "visible", "true");
						}else{
							iformObj.setStyle("InternalClarification", "visible", "false");
						}
						iformObj.setStyle("q_Assign_to_Group", "visible", "false");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "false");
						iformObj.setValue("q_Assign_to_Group", "");
						iformObj.setStyle("AssignToOperator", "visible", "false");
						iformObj.setStyle("AssignToOperator", "mandatory", "false");
						iformObj.setValue("AssignToOperator", "");
						iformObj.setStyle("TLNumber", "mandatory", "false");
						iformObj.setStyle("TradeName", "mandatory", "false");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "false");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "false");
						iformObj.setStyle("TLExpiryDate", "mandatory", "false");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "false");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "false");
						iformObj.setStyle("CompanyName", "mandatory", "false");
						iformObj.setStyle("CompanyShortName", "mandatory", "false");
						iformObj.setStyle("Persona", "mandatory", "false");
						iformObj.setStyle("CompanyCategory", "mandatory", "false");
						iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "false");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "false");
						
						iformObj.setStyle("CompanyPEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "false");
					}else if(decision.equalsIgnoreCase("Decline")){
						iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						if(gridRowCount_InfoGR != 0){
							iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalInfoRequired", "visible", "false");
						}
						if(gridRowCount_DocGR != 0){
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "false");
						}
						if(gridRowCount_InterClrsGR != 0){
							iformObj.setStyle("InternalClarification", "visible", "true");
						}else{
							iformObj.setStyle("InternalClarification", "visible", "false");
						}
						iformObj.setStyle("q_Assign_to_Group", "visible", "false");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "false");
						iformObj.setValue("q_Assign_to_Group", "");
						iformObj.setStyle("AssignToOperator", "visible", "false");
						iformObj.setStyle("AssignToOperator", "mandatory", "false");
						iformObj.setValue("AssignToOperator", "");
						
						iformObj.setStyle("TLNumber", "mandatory", "false");
						iformObj.setStyle("TradeName", "mandatory", "false");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "false");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "false");
						iformObj.setStyle("TLExpiryDate", "mandatory", "false");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "false");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "false");
						iformObj.setStyle("CompanyName", "mandatory", "false");
						iformObj.setStyle("CompanyShortName", "mandatory", "false");
						iformObj.setStyle("Persona", "mandatory", "false");
						iformObj.setStyle("CompanyCategory", "mandatory", "false");
						iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "false");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "false");
						
						iformObj.setStyle("CompanyPEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "false");
					}else if(decision.equalsIgnoreCase("Assign To")){
						iformObj.setStyle("q_Assign_to_Group", "visible", "true");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "true");
						iformObj.setStyle("InternalClarification", "visible", "true");
						if(gridRowCount_DeclineGR != 0){
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						}else{
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
						}
						if(gridRowCount_InfoGR != 0){
							iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalInfoRequired", "visible", "false");
						}
						if(gridRowCount_DocGR != 0){
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "false");
						}
						iformObj.setStyle("TLNumber", "mandatory", "false");
						iformObj.setStyle("TradeName", "mandatory", "false");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "false");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "false");
						iformObj.setStyle("TLExpiryDate", "mandatory", "false");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "false");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "false");
						iformObj.setStyle("CompanyName", "mandatory", "false");
						iformObj.setStyle("CompanyShortName", "mandatory", "false");
						iformObj.setStyle("Persona", "mandatory", "false");
						iformObj.setStyle("CompanyCategory", "mandatory", "false");
						iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "false");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "false");
						
						iformObj.setStyle("CompanyPEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "false");
					}else if(decision.equalsIgnoreCase("Correction required at Front End")){
						iformObj.setStyle("InternalClarification", "visible", "true");
						if(gridRowCount_InfoGR != 0){
							iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalInfoRequired", "visible", "false");
						}
						if(gridRowCount_DocGR != 0){
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "false");
						}
						if(gridRowCount_DeclineGR != 0){
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						}else{
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
						}
						iformObj.setStyle("q_Assign_to_Group", "visible", "false");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "false");
						iformObj.setValue("q_Assign_to_Group", "");
						iformObj.setStyle("AssignToOperator", "visible", "false");
						iformObj.setStyle("AssignToOperator", "mandatory", "false");
						iformObj.setValue("AssignToOperator", "");
						
						iformObj.setStyle("TLNumber", "mandatory", "false");
						iformObj.setStyle("TradeName", "mandatory", "false");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "false");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "false");
						iformObj.setStyle("TLExpiryDate", "mandatory", "false");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "false");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "false");
						iformObj.setStyle("CompanyName", "mandatory", "false");
						iformObj.setStyle("CompanyShortName", "mandatory", "false");
						iformObj.setStyle("Persona", "mandatory", "false");
						iformObj.setStyle("CompanyCategory", "mandatory", "false");
						iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "false");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "false");
						
						iformObj.setStyle("CompanyPEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "false");
					}else if(decision.equalsIgnoreCase("Sole to any other")){
						if(gridRowCount_InfoGR != 0){
							iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalInfoRequired", "visible", "false");
						}
						if(gridRowCount_DocGR != 0){
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "false");
						}
						if(gridRowCount_DeclineGR != 0){
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						}else{
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
						}
						if(gridRowCount_InterClrsGR != 0){
							iformObj.setStyle("InternalClarification", "visible", "true");
						}else{
							iformObj.setStyle("InternalClarification", "visible", "false");
						}
						iformObj.setStyle("q_Assign_to_Group", "visible", "false");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "false");
						iformObj.setValue("q_Assign_to_Group", "");
						iformObj.setStyle("AssignToOperator", "visible", "false");
						iformObj.setStyle("AssignToOperator", "mandatory", "false");
						iformObj.setValue("AssignToOperator", "");
						
						iformObj.setStyle("TLNumber", "mandatory", "false");
						iformObj.setStyle("TradeName", "mandatory", "false");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "false");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "false");
						iformObj.setStyle("TLExpiryDate", "mandatory", "false");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "false");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "false");
						iformObj.setStyle("CompanyName", "mandatory", "false");
						iformObj.setStyle("CompanyShortName", "mandatory", "false");
						iformObj.setStyle("Persona", "mandatory", "false");
						iformObj.setStyle("CompanyCategory", "mandatory", "false");
						iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "false");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "false");
						
						iformObj.setStyle("CompanyPEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "false");
					}else if(decision.equalsIgnoreCase("Sole to Sole LLC and No Correction")){

						if(gridRowCount_InfoGR != 0){
							iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalInfoRequired", "visible", "false");
						}
						if(gridRowCount_DocGR != 0){
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "false");
						}
						if(gridRowCount_DeclineGR != 0){
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						}else{
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
						}
						if(gridRowCount_InterClrsGR != 0){
							iformObj.setStyle("InternalClarification", "visible", "true");
						}else{
							iformObj.setStyle("InternalClarification", "visible", "false");
						}
						iformObj.setStyle("q_Assign_to_Group", "visible", "false");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "false");
						iformObj.setValue("q_Assign_to_Group", "");
						iformObj.setStyle("AssignToOperator", "visible", "false");
						iformObj.setStyle("AssignToOperator", "mandatory", "false");
						iformObj.setValue("AssignToOperator", "");
						
						iformObj.setStyle("TLNumber", "mandatory", "true");
						iformObj.setStyle("TradeName", "mandatory", "true");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "true");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "true");
						iformObj.setStyle("TLExpiryDate", "mandatory", "true");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "true");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "true");
						iformObj.setStyle("CompanyName", "mandatory", "true");
						iformObj.setStyle("CompanyShortName", "mandatory", "true");
						//
						iformObj.setStyle("Persona", "mandatory", "true");
						iformObj.setStyle("CompanyCategory", "mandatory", "true");
						//iformObj.setStyle("NameOnChqBk", "mandatory", "true");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "true");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "true");
						//iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "true");
						String IsChqBkReq =  (String) iformObj.getValue("IsChqBkReq");
						DBO.mLogger.debug("value of IsChqBkReq inside change event " + IsChqBkReq);
						if(IsChqBkReq.equalsIgnoreCase("Y")){
							DBO.mLogger.debug("value of IsChqBkReq inside if " + IsChqBkReq);
							iformObj.setStyle("NameOnChqBk", "mandatory", "true");
						}else {
							DBO.mLogger.debug("value of IsChqBkReq inside else " + IsChqBkReq);
							iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						}
						DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside change event " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
						if(Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq.equalsIgnoreCase("Y")){
							DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside if " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
							iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "true");
						}else{
							DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside else " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
							iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						}
						iformObj.setStyle("CompanyPEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "true");
					}else if(decision.equalsIgnoreCase("Sole to Sole LLC and Information Correction")){

						if(gridRowCount_InfoGR != 0){
							iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalInfoRequired", "visible", "false");
						}
						if(gridRowCount_DocGR != 0){
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "false");
						}
						if(gridRowCount_DeclineGR != 0){
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						}else{
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
						}
						if(gridRowCount_InterClrsGR != 0){
							iformObj.setStyle("InternalClarification", "visible", "true");
						}else{
							iformObj.setStyle("InternalClarification", "visible", "false");
						}
						iformObj.setStyle("q_Assign_to_Group", "visible", "false");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "false");
						iformObj.setValue("q_Assign_to_Group", "");
						iformObj.setStyle("AssignToOperator", "visible", "false");
						iformObj.setStyle("AssignToOperator", "mandatory", "false");
						iformObj.setValue("AssignToOperator", "");
						
						iformObj.setStyle("TLNumber", "mandatory", "true");
						iformObj.setStyle("TradeName", "mandatory", "true");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "true");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "true");
						iformObj.setStyle("TLExpiryDate", "mandatory", "true");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "true");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "true");
						iformObj.setStyle("CompanyName", "mandatory", "true");
						iformObj.setStyle("CompanyShortName", "mandatory", "true");
						//
						iformObj.setStyle("Persona", "mandatory", "true");
						iformObj.setStyle("CompanyCategory", "mandatory", "true");
						//iformObj.setStyle("NameOnChqBk", "mandatory", "true");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "true");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "true");
						//iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "true");
						String IsChqBkReq =  (String) iformObj.getValue("IsChqBkReq");
						DBO.mLogger.debug("value of IsChqBkReq inside change event " + IsChqBkReq);
						if(IsChqBkReq.equalsIgnoreCase("Y")){
							DBO.mLogger.debug("value of IsChqBkReq inside if " + IsChqBkReq);
							iformObj.setStyle("NameOnChqBk", "mandatory", "true");
						}else {
							DBO.mLogger.debug("value of IsChqBkReq inside else " + IsChqBkReq);
							iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						}
						DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside change event " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
						if(Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq.equalsIgnoreCase("Y")){
							DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside if " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
							iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "true");
						}else{
							DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside else " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
							iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						}
						iformObj.setStyle("CompanyPEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "true");
					}
					else{
						if(gridRowCount_InfoGR != 0){
							iformObj.setStyle("AdditionalInfoRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalInfoRequired", "visible", "false");
						}
						if(gridRowCount_DocGR != 0){
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "true");
						}else{
							iformObj.setStyle("AdditionalDocumentsRequired", "visible", "false");
						}
						if(gridRowCount_DeclineGR != 0){
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
						}else{
							iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
						}
						if(gridRowCount_InterClrsGR != 0){
							iformObj.setStyle("InternalClarification", "visible", "true");
						}else{
							iformObj.setStyle("InternalClarification", "visible", "false");
						}
						iformObj.setStyle("q_Assign_to_Group", "visible", "false");
						iformObj.setStyle("q_Assign_to_Group", "mandatory", "false");
						iformObj.setValue("q_Assign_to_Group", "");
						iformObj.setStyle("AssignToOperator", "visible", "false");
						iformObj.setStyle("AssignToOperator", "mandatory", "false");
						iformObj.setValue("AssignToOperator", "");
						
						iformObj.setStyle("TLNumber", "mandatory", "true");
						iformObj.setStyle("TradeName", "mandatory", "true");
						iformObj.setStyle("TLIssuingAuthority", "mandatory", "true");
						iformObj.setStyle("TLIssusingAuthorithyEmirate", "mandatory", "true");
						iformObj.setStyle("TLExpiryDate", "mandatory", "true");
						iformObj.setStyle("TLTypeOfOffice", "mandatory", "true");
						iformObj.setStyle("DateOfIncorporation", "mandatory", "true");
						iformObj.setStyle("CompanyName", "mandatory", "true");
						iformObj.setStyle("CompanyShortName", "mandatory", "true");
						//
						iformObj.setStyle("Persona", "mandatory", "true");
						iformObj.setStyle("CompanyCategory", "mandatory", "true");
						//iformObj.setStyle("NameOnChqBk", "mandatory", "true");
						iformObj.setStyle("Q_RB_DBO_TXNTABLE_CountryOfIncorporation", "mandatory", "true");
						iformObj.setStyle("Verified_Address_Proof_1", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Full_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_First_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Last_Name", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_DOB", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Nationality", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Title", "mandatory", "true");
						//iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedSOF_1", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_VerifiedAdd_1", "mandatory", "true");
						String IsChqBkReq =  (String) iformObj.getValue("IsChqBkReq");
						DBO.mLogger.debug("value of IsChqBkReq inside change event " + IsChqBkReq);
						if(IsChqBkReq.equalsIgnoreCase("Y")){
							DBO.mLogger.debug("value of IsChqBkReq inside if " + IsChqBkReq);
							iformObj.setStyle("NameOnChqBk", "mandatory", "true");
						}else {
							DBO.mLogger.debug("value of IsChqBkReq inside else " + IsChqBkReq);
							iformObj.setStyle("NameOnChqBk", "mandatory", "false");
						}
						DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside change event " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
						if(Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq.equalsIgnoreCase("Y")){
							DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside if " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
							iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "true");
						}else{
							DBO.mLogger.debug("value of Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq inside else " + Q_USR_0_DBO_RelatedPartyGrid_DebitCardReq);
							iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_Name_On_Debit_Card", "mandatory", "false");
						}
						
						iformObj.setStyle("CompanyPEP", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "true");
						iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "mandatory", "true");
					}
				}else if (actName.equalsIgnoreCase("Post_Facto_Validation")) 
				{
					 if(decision.equalsIgnoreCase("Failed Validation-Send for Closure"))
					 {
						iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "true");
					 }else{
						iformObj.setStyle("DECLINE_REASON_SECTION", "visible", "false");
					 }
				}
					
			}
			//
			else if("TLIssuingAuthority".equalsIgnoreCase(control))
            {
                  String TLIssuingAuth = ((String)iformObj.getValue("TLIssuingAuthority")).trim();
                  String priority = "";
                  
                  if("RAKE".equalsIgnoreCase(TLIssuingAuth))
                  {
                        priority="4";
                  }
                  else
                  {
                        priority="1";
                  }
                  
                  String inputXML ="<?xml version=\"1.0\"?>"+
                              "<WFChangeWorkItemPriority_Input>"+
                              "<Option>WFChangeWorkItemPriority</Option>"+
                              "<EngineName>"+iformObj.getCabinetName()+"</EngineName>"+
                              "<SessionId>"+getSessionId(iformObj)+"</SessionId>"+
                              "<ProcessInstanceId>"+WI_Name+"</ProcessInstanceId>"+
                              "<WorkItemId>"+WI_ID+"</WorkItemId>"+
                              "<Priority>"+priority+"</Priority>"+
                              "</WFChangeWorkItemPriority_Input>";
                  String sOutputXML=WFNGExecute(inputXML, iformObj.getServerIp(), iformObj.getServerPort());
                  DBO.mLogger.info("outputXML AP Procedure XML Entry "+sOutputXML);
                  
                  if(sOutputXML.indexOf("<MainCode>0</MainCode>")>-1)
                  {
                    DBO.mLogger.info("WFChangeWorkItemPriority Successful ");
                    String getWIinput =getWorkItemInput(iformObj.getCabinetName(),getSessionId(iformObj),WI_Name,WI_ID);
                    sOutputXML=WFNGExecute(getWIinput, iformObj.getServerIp(), iformObj.getServerPort());
                    if(sOutputXML.indexOf("<MainCode>0</MainCode>")>-1)
                    {  
                    	return "Success";
                    }
                    else
                    {
                    	DBO.mLogger.info("GetWorkItem failed ");
                        return "Error";
                    }
                  }
                  else
                  {
                        DBO.mLogger.info("WFChangeWorkItemPriority failed ");
                        return "Error";
                  }
            }
			else if("SetqVariableValues".equalsIgnoreCase(control))
			{
				if(!"".equalsIgnoreCase(stringdata) && stringdata != null && !"null".equalsIgnoreCase(stringdata))
				{
					String tmp[] = stringdata.split("~");
					String fromField = tmp[0];
					String toField = tmp[1];
					String fromValue =  (String) iformObj.getValue(fromField);
					iformObj.setValue(toField, fromValue);
				}
			}

			//
			
			//InternalClarification

		} catch (Exception exc) {
			DBO.printException(exc);
			DBO.mLogger.debug("Exception 2 - " + exc);
		}

		return UNHANDLED;
	}

}
