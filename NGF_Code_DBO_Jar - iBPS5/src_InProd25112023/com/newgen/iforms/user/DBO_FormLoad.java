package com.newgen.iforms.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.util.PropertyFileLoaderUtil;

public class DBO_FormLoad extends DBOCommon {

	private String WI_Name = null;
	private String userName = null;
	private String activityName = null;
	private IFormReference giformObj = null;

	public String formLoad(IFormReference iformObj, String control, String stringdata) {
		WI_Name = getWorkitemName(iformObj);
		userName = iformObj.getUserName();
		activityName = iformObj.getActivityName();
		giformObj = iformObj;
		
		DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Inside FormLoad...");
		
		String str_groupCount ="onlyOneGroup";
		int groupCount=0;
		try {
			//to hide AssignToOperator by default
			iformObj.setStyle("AssignToGroup", "visible", "false");
			iformObj.setStyle("AssignToGroup", "mandatory", "false");
			iformObj.setValue("AssignToGroup", "");
			iformObj.setStyle("AssignToOperator", "visible", "false");
			iformObj.setStyle("AssignToOperator", "mandatory", "false");
			iformObj.setValue("AssignToOperator", "");
			
			iformObj.setStyle("Q_USR_0_DBO_STPOperatorSection", "visible", "false");
			iformObj.setValue("label_Loggedin", "LoggedIn As: <br><font color =\"black\">" + userName + "</font>");
			iformObj.setValue("label_Workstep", "Workstep: <br><font color =\"black\">" + activityName + "</font>");
			iformObj.setValue("label_WINumber", "Workitem No: <br><font color =\"black\">" + WI_Name + "</font>");
			
			if (activityName.equalsIgnoreCase("STP_Operator")) 
			{
				String Persona =  (String) iformObj.getValue("Persona");
				String TypeOfRequest = (String) iformObj.getValue("TypeOfRequest");
				String IsChqBkReq =  (String) iformObj.getValue("IsChqBkReq");
				String Q_USR_0_DBO_RelatedPartyGrid_Title = (String) iformObj.getValue("Q_USR_0_DBO_RelatedPartyGrid_Title");
				
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of Persona inside formload event " + Persona);
				if(Persona.equalsIgnoreCase("OTHER")){
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "disable", "false");
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "mandatory", "true");
				}else{
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "mandatory", "false");
					iformObj.setStyle("Q_RB_DBO_TXNTABLE_OtherPersona", "disable", "true");
					iformObj.setValue("Q_RB_DBO_TXNTABLE_OtherPersona", "");
				}
				iformObj.setStyle("BtnCompAECBReport", "visible", "false");
				iformObj.setStyle("NameOnChqBk", "mandatory", "false");
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of IsChqBkReq inside formload event " + IsChqBkReq);
				/*DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of Q_USR_0_DBO_RelatedPartyGrid_Title inside change event " + Q_USR_0_DBO_RelatedPartyGrid_Title);
				if(Q_USR_0_DBO_RelatedPartyGrid_Title.equalsIgnoreCase("Mrs") || Q_USR_0_DBO_RelatedPartyGrid_Title.equalsIgnoreCase("Ms")){
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of Q_USR_0_DBO_RelatedPartyGrid_Title inside if " + Q_USR_0_DBO_RelatedPartyGrid_Title);
					iformObj.setValue("Q_USR_0_DBO_RelatedPartyGrid_Gender", "F");
				}else if(Q_USR_0_DBO_RelatedPartyGrid_Title.equalsIgnoreCase("Mr")) {
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of Q_USR_0_DBO_RelatedPartyGrid_Title inside else " + Q_USR_0_DBO_RelatedPartyGrid_Title);
					iformObj.setValue("Q_USR_0_DBO_RelatedPartyGrid_Gender", "M");
				}*/
				
				/*if(TypeOfRequest.equalsIgnoreCase("STP")){
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_ChkBk_Recipient", "disable", "true");
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "disable", "true");
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_ChkBk_Recipient", "mandatory", "false");
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "false");
				}else{
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_ChkBk_Recipient", "disable", "false");
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "disable", "false");
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_ChkBk_Recipient", "mandatory", "true");
					iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "mandatory", "true");
				}*/
				//iformObj.setStyle("Q_USR_0_DBO_FIRCO_DTLS", "disable", "true");
				//iformObj.setStyle("CompanyPEP", "visible", "false");
				//iformObj.setStyle("Q_USR_0_DBO_RelatedPartyGrid_PEP", "visible", "false");
				String enableFieldStp_Operator = PropertyFileLoaderUtil.getProperty("FieldToBeEnabledAtStp_Operator");
				enableControl(enableFieldStp_Operator, iformObj);
	
				String enableFieldMandatoryStp_Operator = PropertyFileLoaderUtil.getProperty("FieldToBeMandatoryAtStp_Operator");
				mandatoryControl(enableFieldMandatoryStp_Operator, iformObj);
				
				if("Y".equalsIgnoreCase(IsChqBkReq) || "Yes".equalsIgnoreCase(IsChqBkReq)){
					iformObj.setStyle("NameOnChqBk", "disable", "false");
				}else {
					iformObj.setStyle("NameOnChqBk", "disable", "true");
				}
				
				// setting Passport issue date of stakeholder in OPS activity section
				/*int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", size of Q_USR_0_DBO_RelatedPartyGrid at onload: " + size);
				String passportIssueDate = "";
				for (int i = 0; i < size; i++)
				{
					passportIssueDate = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 31).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of passportIssueDate: " + passportIssueDate );
					break;
				}
				if(!"".equalsIgnoreCase(passportIssueDate) && passportIssueDate != null)
				{
					iformObj.setValue("Q_RB_DBO_TXNTABLE_PassportIssueDate", passportIssueDate);
				}*/
				//////////////////////////
				
			}
			else{
				iformObj.setStyle("DecisionDescription", "visible", "false");
				iformObj.setStyle("infoButton", "visible", "false");
			}
			
			if(activityName.equalsIgnoreCase("Post_Facto_Validation")){
				iformObj.setStyle("DecisionDescription", "visible", "false");
				String enableFieldPost_Facto_Validation = PropertyFileLoaderUtil.getProperty("FieldToBeEnabledAtPost_Facto_Validation");
				enableControl(enableFieldPost_Facto_Validation, iformObj);
	
				String enableFieldMandatoryPost_Facto_Validation = PropertyFileLoaderUtil.getProperty("FieldToBeMandatoryAtPost_Facto_Validation");
				mandatoryControl(enableFieldMandatoryPost_Facto_Validation, iformObj);
			}
			else if(activityName.equalsIgnoreCase("OPS_Review_Maker")){
				int size =iformObj.getDataFromGrid("Q_USR_0_DBO_RelatedPartyGrid").size();
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", size of Q_USR_0_DBO_RelatedPartyGrid at onload: " + size);
				String DebitCardReqFlag = "N";
				for (int i = 0; i < size; i++)
				{
					DebitCardReqFlag = iformObj.getTableCellValue("Q_USR_0_DBO_RelatedPartyGrid", i, 26).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of DebitCardReqFlag: " + DebitCardReqFlag );
					break;
				}
				if("R".equalsIgnoreCase(DebitCardReqFlag) || "Required".equalsIgnoreCase(DebitCardReqFlag))
				{
					enableControl("FinacleSRNumber", iformObj);
					mandatoryControl("FinacleSRNumber", iformObj);
				}
			}
		
		} catch (Exception exc) {
			DBO.printException(exc);
			DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Error in form load - " + exc);
		}

		// To populate DECISIONs in decision drop down ...
		try {
			DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", To populate DECISIONs in decision drop down  ...");
			String Query = "SELECT DECISION FROM USR_0_DBO_DECISION_MASTER WITH(NOLOCK) WHERE WORKSTEP_NAME='" + activityName
					+ "' and ISACTIVE='Y'";
			if(activityName.equalsIgnoreCase("STP_Operator"))
			{
				String groupName = "";
				String dbColNameGroupName = "";
				String query2 = "select top 1 GroupName from (select  GroupName from PDBGroup "
						+ "with(nolock) where GroupIndex in (select GroupIndex from PDBGroupMember "
						+ "with(nolock) where UserIndex = (select userindex from PDBUser "
						+ "with(nolock) where UserName='"+userName+"')) intersect select  GroupName from "
						+ "PDBGroup with(nolock) where GroupIndex in (select Userid from QUEUEUSERTABLE"
						+ " with(nolock) where AssociationType=1 and QueueID = (select QueueID from "
						+ "QUEUEDEFTABLE with(nolock) where QueueName='DBO_STP_Operator'))) as temp"
						+ " order by GroupName desc";
				groupName = getUserGroup(query2 , iformObj);
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", groupName value ..." +groupName);
				iformObj.setValue("UserGroupSTP_Operator", groupName);
				if(groupName.contains("_Operations")){
					dbColNameGroupName = "Operations";
				}else if(groupName.contains("_Controls")){
					dbColNameGroupName = "Controls";
				}else if(groupName.contains("_RO")){
					dbColNameGroupName = "RO";
				}else if(groupName.contains("_Compliance")){
					dbColNameGroupName = "Compliance";
				}
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", dbColNameGroupName value ..." +dbColNameGroupName);
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", To populate DECISIONs in decision drop down usergroup query ...");
				String query1 = "select  count(*) GroupName from (select  GroupName from PDBGroup with(nolock)"
						+ " where GroupIndex in (select GroupIndex from PDBGroupMember with (nolock) where UserIndex"
						+ " = (select userindex from PDBUser with(nolock) where UserName='"+userName+"')) intersect "
						+ "select GroupName from PDBGroup with(nolock) where GroupIndex in (select Userid from"
						+ " QUEUEUSERTABLE with(nolock) where AssociationType=1 and QueueID= (select QueueID from"
						+ " QUEUEDEFTABLE with(nolock) where QueueName='DBO_STP_Operator'))) as temp";
				groupCount = Integer.parseInt(getUserGroup(query1 , iformObj));
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", groupCount value ..." +groupCount);
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", groupCount value str_groupCount ..." +str_groupCount);
				if(groupCount > 1)
				{
					iformObj.setStyle("CompanyDetails", "disable", "true");
					iformObj.setStyle("AccountDetails", "disable", "true");
					iformObj.setStyle("ChequeBookDetails", "disable", "true");
					iformObj.setStyle("RelatedPartyDetails", "disable", "true");
					iformObj.setStyle("DecisionHistory", "disable", "true");
					iformObj.setStyle("FreeFieldsSection", "disable", "true");
					iformObj.setStyle("DECISION", "disable", "true");
					iformObj.setStyle("Remarks", "disable", "true");
					str_groupCount = "multipleGroup";
				}else if(groupCount == 1)
				{
					iformObj.setValue("loggedInUserGroupOnlyOne", groupName);
					iformObj.setValue("label_Loggedin", "LoggedIn As: <br><font color =\"black\">" + groupName + ": " + userName + "</font>");
				}
				Query = "SELECT DECISION FROM USR_0_DBO_DECISION_MASTER WITH(NOLOCK) WHERE WORKSTEP_NAME='" + activityName
						+ "' and ISACTIVE='Y' and "+dbColNameGroupName +"='Yes'";
			}
			DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
			List lstDecisions = iformObj.getDataFromDB(Query);
			DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Result  ..." + lstDecisions);
			String value = "";
			iformObj.clearCombo("DECISION");
			for (int i = 0; i < lstDecisions.size(); i++) {
				List<String> arr1 = (List) lstDecisions.get(i);
				value = arr1.get(0);
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Item to add in decision combo  ..." + value);
				iformObj.addItemInCombo("DECISION", value, value);
			}

		} catch (Exception e) {
			DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control
					+ ", Exception in Decision drop down load: " + e.getMessage());
		}
		DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", end formLoad function");
		return str_groupCount;
	}
}
