package com.newgen.iforms.user;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;

import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.iforms.custom.IFormReference;


public class DBO_clickhandler extends DBOCommon {

    private static final String EXCEPTION_OCCURED = null;
    private static final String UNHANDLED = null;
    private static final String SUCCESS = null;
    private static final String FAIL = null;
    private String WI_Name = null;
	private String activityName = null;
	private String userName = null;
	private String DECISION = null;
	private IFormReference giformObj = null;
    
    public String onClick(IFormReference iformObj, String control, String stringdata) {
    	activityName=iformObj.getActivityName();
    	WI_Name=getWorkitemName(iformObj);
		giformObj=iformObj;
		userName = iformObj.getUserName();
		DECISION = (String) iformObj.getValue("DECISION");
    	DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", onClick:::::::::::::::::::::::::::::");
		try
		{
			if("GetGridRowCount".equalsIgnoreCase(control))
			{
				if("Q_USR_0_DBO_DECLINE_REJECT_DTLS".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_DECLINE_REJECT_DTLS with (nolock)  where"
							+ " WINAME = '" + WI_Name+ "'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				else if("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_ADDNAL_INFO_REQ_DTLS with (nolock)  where"
							+ " Wi_Name = '" + WI_Name+ "'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				else if("Q_USR_0_DBO_ADDNL_DOC_REQUIRED".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_ADDNL_DOC_REQUIRED with (nolock)  where"
							+ " WINAME = '" + WI_Name+ "'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				else if("Q_USR_0_DBO_RelatedPartyGrid".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_RelatedPartyGrid with (nolock)  where"
							+ " WINAME = '" + WI_Name+ "'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				else if("Q_USR_0_DBO_FIRCO_DTLS".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_FIRCO_DTLS with (nolock)  where"
							+ " WINAME = '" + WI_Name+ "'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				else if("Q_USR_0_DBO_EIDAPINGDATA".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_EIDAPINGDATA with (nolock)  where"
							+ " WINAME = '" + WI_Name+ "'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				else if("Q_USR_0_DBO_WIHISTORY".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_WIHISTORY with (nolock)  where"
							+ " WINAME = '" + WI_Name+ "'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				//
				else if("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS".equalsIgnoreCase(stringdata)){
					String Query = "select count(*) as GRCount from USR_0_DBO_INTERNAL_CLRFCTN_DTLS with (nolock)  where"
							+ " WINAME = '" + WI_Name+ "' and IC_DEC_MAPID != 'Y'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstVals= iformObj.getDataFromDB(Query);
					String gridRowCount = lstVals.get(0).get(0).trim();
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", gridRowCount " + gridRowCount);
					return gridRowCount;
				}
				//
			}
			//
			//191023
			if("InfoGRColDisableAdd".equalsIgnoreCase(control))
			{
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_IsQuerySentToDEH", "visible", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Category", "disable", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Category", "mandatory", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Type", "disable", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Type", "mandatory", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Remarks", "disable", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Remarks", "mandatory", "true");
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", InfoGRColDisableAdd " + "Done");
				return "Done";
			}
			if("InfoGRColDisableModify".equalsIgnoreCase(control))
			{
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_IsQuerySentToDEH", "visible", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Category", "disable", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Category", "mandatory", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Type", "disable", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Type", "mandatory", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Remarks", "disable", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Remarks", "mandatory", "false");
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", InfoGRColDisableAdd " + "Done");
				return "Done";
			}
			if("DocGRColDisableAdd".equalsIgnoreCase(control))
			{
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_IsDocSentToDEH_hidden", "visible", "false");	
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Category", "disable", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Category", "mandatory", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Type", "disable", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Type", "mandatory", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Remarks", "disable", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Remarks", "mandatory", "true");
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", InfoGRColDisableAdd " + "Done");
				return "Done";
			}
			if("DocGRColDisableModify".equalsIgnoreCase(control))
			{
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_IsDocSentToDEH_hidden", "visible", "false");	
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Category", "disable", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Category", "mandatory", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Type", "disable", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Type", "mandatory", "false");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Remarks", "disable", "true");
				iformObj.setStyle("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Remarks", "mandatory", "false");
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", InfoGRColDisableAdd " + "Done");
				return "Done";
			}
			//
			if("InternalClarification".equalsIgnoreCase(control))
			{
				JSONArray output2 = iformObj.getDataFromGrid("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS");
				String str_arrRow = "";
				if(!output2.isEmpty()){
					for(int i =0; i<output2.size(); i++){
						String str_outputData = output2.get(i).toString();
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", JSONArray<Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS> output " + str_outputData);
						if(str_outputData.contains("IC_DEC_MAPID\":\"Y\"")){
							DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", This time IC_DEC_MAPID is Y " );
							str_arrRow += i;
						 }
					}
				}
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", str_arrRow " + str_arrRow);
				return str_arrRow;
			}
			//
			if("RelDedupeCifsSplit".equalsIgnoreCase(control))
			{
				String statusDedupeCifs = "";
				String selectedVal = (String) iformObj.getValue("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID");
 				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value selectedVal on load  ..." + selectedVal);
                iformObj.addItemInCombo("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", "NTB", "NTB", "NTB");
                String DedupeCifs = (String) iformObj.getValue("Q_USR_0_DBO_RelatedPartyGrid_DedupeCIFs");
    			DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", DedupeCifs " + DedupeCifs);
    			if(!"".equalsIgnoreCase(DedupeCifs) && DedupeCifs != null)
    			{
	    			String[] arrDedupeCifs = DedupeCifs.split(",");
	    			for(int k = 0; k<arrDedupeCifs.length;k++)
	                {
		                iformObj.addItemInCombo("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", arrDedupeCifs[k], arrDedupeCifs[k], arrDedupeCifs[k]);
		                if(!"".equalsIgnoreCase(selectedVal) && arrDedupeCifs[k].equalsIgnoreCase(selectedVal)){
	 						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value selectedVal on load  ..." + selectedVal);
	 						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value valueCode on load  ..." + selectedVal);
	 						iformObj.setValue("Q_USR_0_DBO_RelatedPartyGrid_RCIF_ID", selectedVal);
	 					}
		                statusDedupeCifs = "inserted";
		                DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inserted in combo " +  arrDedupeCifs[k]);
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", statusDedupeCifs-- "+statusDedupeCifs);
	                }
    			}
				return statusDedupeCifs;
			}
			
			if("ClrGridData".equalsIgnoreCase(control))
			{
				String str_arrRow = "";
				try {
						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", To populate Reasons on click of view History button   ...");
						String Query = "select DecisionSeqNo,ClarificationUniqueId,ClarificationCategory,ClarificationReason,"
								+ "ClarificationRemarks from USR_0_DBO_INTERNAL_CLRFCTN_DTLS with(nolock) where"
								+ " WINAME = '" + WI_Name+ "'";
						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
						List<List<String>> lstVals= iformObj.getDataFromDB(Query);
						DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Result  ..." + lstVals);
						if(lstVals != null)
	                    {
							for (int j = 0; j < lstVals.size(); j++)
                            {
                                  str_arrRow += lstVals.get(j).get(0) + "#~#";
                                  if(lstVals.get(j).get(1).length() > 0)
                                        str_arrRow += "Unique Id - " + lstVals.get(j).get(1) + "#~#";
                                  if(lstVals.get(j).get(2).length() > 0)
                                        str_arrRow += "Category - " + lstVals.get(j).get(2) + "#~#";
                                  if(lstVals.get(j).get(3).length() > 0)
                                       str_arrRow += "Type - " + lstVals.get(j).get(3) + "#~#";
                                  if(lstVals.get(j).get(4).length() > 0)
                                              str_arrRow += "Remarks - " + lstVals.get(j).get(4);
                                  
                                   if(j < lstVals.size()-1) str_arrRow += "$~$";
                            }
	                    }

					} catch (Exception e) {
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Exception in To populate Reasons on click of view History button: " + e.getMessage());
					}
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", str_arrRow " + str_arrRow);
				return str_arrRow;
			}
			//
			if("PostHookloadSectionFirco".equalsIgnoreCase(control))
			{
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside  PostHookloadSectionFirco ...");
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","1", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","2", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","3", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","4", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","5", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","6", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","7", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","8", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","9", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","10", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","11", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","12", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","13", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","15", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_FIRCO_DTLS","17", true);
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside  PostHookloadSectionFirco after enable disable...");
					
					String str_arrRow = "";
					int sizFircoGR = iformObj.getDataFromGrid("Q_USR_0_DBO_FIRCO_DTLS").size();
					for(int i=0; i<sizFircoGR; i++){
						String isFrozen = iformObj.getTableCellValue("Q_USR_0_DBO_FIRCO_DTLS", i, 18);
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", value of isFrozen is " + isFrozen);
						if("Y".equalsIgnoreCase(isFrozen) || "Yes".equalsIgnoreCase(isFrozen) ){
							DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+",  This time isFrozen is Y " );
							str_arrRow += i;
						 }
					}	
					return str_arrRow;
			}
			if("PostHookloadSectionPingData".equalsIgnoreCase(control))
			{
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside  PostHookloadSectionPINGDATA ...");
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","0", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","1", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","2", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","3", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","4", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","5", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","6", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","7", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_EIDAPINGDATA","8", true);
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside  PostHookloadSectionPINGDATA after enable disable...");
					
					String str_arrRow = "Disabled";
					return str_arrRow;
			}
			if("PostHookloadSection".equalsIgnoreCase(control))
			{
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside  PostHookloadSection ...");
					iformObj.setColumnDisable("Q_USR_0_DBO_WIHISTORY","0", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_WIHISTORY","1", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_WIHISTORY","2", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_WIHISTORY","3", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_WIHISTORY","4", true);
					iformObj.setColumnDisable("Q_USR_0_DBO_WIHISTORY","6", true);
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside  PostHookloadSection after enable disable...");
					
					JSONArray output3 = iformObj.getDataFromGrid("Q_USR_0_DBO_WIHISTORY");
					String str_arrRow = "";
					if(!output3.isEmpty()){
						for(int i =0; i<output3.size(); i++){
							String str_workstep = iformObj.getTableCellValue("Q_USR_0_DBO_WIHISTORY",i , 1);
							String str_Decision = iformObj.getTableCellValue("Q_USR_0_DBO_WIHISTORY",i , 4);
							DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", JSONArray<Q_USR_0_DBO_WIHISTORY> output " + str_workstep);
							if(!("STP_Operator".equalsIgnoreCase(str_workstep) &&
									"Correction required at Front End".equalsIgnoreCase(str_Decision) ||
									str_Decision.contains("Assign To"))){
								DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+",  This time Workstep is not STP_Operator" );
								DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+",  This time Workstep is not STP_Operator and decision is not "
											+ "Assign To OR Correction Req at Front" );
								if("".equalsIgnoreCase(str_arrRow))
								{
									str_arrRow += i;
								}else{
									str_arrRow += ","+i;
								}
							}
						}
					}
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", str_arrRow " + str_arrRow);
					return str_arrRow;
					
			}
			if("popupAddRowDeclineGrid".equalsIgnoreCase(control))
			{ 
				int DBO_DeclineReasonOneTimeLimit = 0;
				JSONArray outputDeclineGR = iformObj.getDataFromGrid("Q_USR_0_DBO_DECLINE_REJECT_DTLS");
				
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", To get limit value for Decline Grid ...");
				String Query = "SELECT  CONST_FIELD_NAME,CONST_FIELD_VALUE FROM USR_0_BPM_CONSTANTS"
						+ " WITH(NOLOCK) WHERE CONST_FIELD_NAME = 'DBO_DeclineReason_OneTimeLimit'";
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
				List<List<String>> lstValues = iformObj.getDataFromDB(Query);
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Result  ..." + lstValues);
				DBO_DeclineReasonOneTimeLimit = Integer.parseInt(lstValues.get(0).get(1));
				DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", DBO_DeclineReasonOneTimeLimit" + DBO_DeclineReasonOneTimeLimit);
				
				if(outputDeclineGR.size() < DBO_DeclineReasonOneTimeLimit){
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", outputDeclineGR.size()" + outputDeclineGR.size() + "DBO_DeclineReasonOneTimeLimit" +DBO_DeclineReasonOneTimeLimit);
					return "GoodToGo";
				}else if(outputDeclineGR.size() == DBO_DeclineReasonOneTimeLimit){
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", outputDeclineGR.size()" + outputDeclineGR.size() + "DBO_DeclineReasonOneTimeLimit" +DBO_DeclineReasonOneTimeLimit);
					return "One Time Maximum Limit Reached ! ";
				}
			}
			if("popupAddRow".equalsIgnoreCase(control))
			{ 
				int DBO_AddInfo_OneTimeLimit = 0;
				int DBO_AddInfo_LifeTimeLimit = 0;
				int DBO_AddDoc_OneTimeLimit = 0;
				int DBO_AddDoc_LifeTimeLimit = 0;
				int counterInfoGR = 0;
				int counterDocGR = 0;
				JSONArray output = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS");
				JSONArray output1 = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNL_DOC_REQUIRED");
				// To get limit value ...
				try {
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", To get limit value  ...");
					String Query = "SELECT  CONST_FIELD_NAME,CONST_FIELD_VALUE FROM USR_0_BPM_CONSTANTS"
							+ " WITH(NOLOCK) WHERE CONST_FIELD_NAME IN ('DBO_AddInfo_OneTimeLimit',"
							+ "'DBO_AddInfo_LifeTimeLimit','DBO_AddDoc_OneTimeLimit','DBO_AddDoc_LifeTimeLimit') "
							+ "order by CONST_FIELD_NAME desc ;";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					List<List<String>> lstValues = iformObj.getDataFromDB(Query);
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Result  ..." + lstValues);
					DBO_AddInfo_OneTimeLimit = Integer.parseInt(lstValues.get(0).get(1));
					DBO_AddInfo_LifeTimeLimit = Integer.parseInt(lstValues.get(1).get(1));
					DBO_AddDoc_OneTimeLimit = Integer.parseInt(lstValues.get(2).get(1));
					DBO_AddDoc_LifeTimeLimit = Integer.parseInt(lstValues.get(3).get(1));
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", DBO_AddInfo_OneTimeLimit" + DBO_AddInfo_OneTimeLimit);
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", DBO_AddInfo_LifeTimeLimit" + DBO_AddInfo_LifeTimeLimit);
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", DBO_AddDoc_OneTimeLimit" + DBO_AddDoc_OneTimeLimit);
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", DBO_AddDoc_LifeTimeLimit" + DBO_AddDoc_LifeTimeLimit);

					} catch (Exception e) {
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Exception in limit value load: " + e.getMessage());
					}
				
				if(stringdata.equalsIgnoreCase("popupAddRowInfoGrid"))
				{

					ArrayList<Object> outputData = new ArrayList<Object> ();
					if(!output.isEmpty()){
						for(int i =0; i<output.size(); i++){
							outputData.add(output.get(i));
						}
					}
					if(!outputData.isEmpty()){
						for(int i=0; i<outputData.size(); i++){
							String str_outputData = outputData.get(i).toString();
							DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", ArrayList<Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS> outputData " + str_outputData);
							if(str_outputData.contains("IsQuerySentToDEH\":\"Y\"")){
								DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", This time IsQuerySentToDEH is Y " );
								counterInfoGR += 1;
							 }
						}
					}
					/*String[] str_arrRowReturn = str_arrRow.split(",");
					int[] arrRowReturn = new int[str_arrRow.length()];
					for(int i=0; i<str_arrRow.length(); i++){
						arrRowReturn[i] = Integer.parseInt(str_arrRowReturn[i]);
					}*/
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", counterInfoGR " + counterInfoGR);
				
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside control popupAddRowInfoGrid");
					if(output.size() >= DBO_AddInfo_LifeTimeLimit){
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", output.size()" + output.size() + "DBO_AddInfo_LifeTimeLimit" +DBO_AddInfo_LifeTimeLimit);
						return "Life Time Maximum Limit Reached ! ";
					}else if(output.size() - counterInfoGR >= DBO_AddInfo_OneTimeLimit){
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", counterInfoGR" + counterInfoGR + "DBO_AddInfo_OneTimeLimit" +DBO_AddInfo_OneTimeLimit);
						return "One Time Maximum Limit Reached ! ";
					}else{
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", GoodToGo");
						return "GoodToGo";
					}
				}
				else if(stringdata.equalsIgnoreCase("popupAddRowDocGrid"))
				{	
					if(!output1.isEmpty()){
						for(int i =0; i<output1.size(); i++){
							String str_outputData = output1.get(i).toString();
							DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", JSONArray<Q_USR_0_DBO_ADDNL_DOC_REQUIRED> output " + str_outputData);
							if(str_outputData.contains("IsDocSentToDEH\":\"Y\"")){
								DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+",  This time IsDocSentToDEH is Y " );
								counterDocGR +=1;
							 }
						}
					}
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", counterDocGR " + counterDocGR);
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", inside control popupAddRowDocGrid");
					if(output1.size() >= DBO_AddDoc_LifeTimeLimit){
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", output1.size()" + output1.size() + "DBO_AddDoc_LifeTimeLimit" +DBO_AddDoc_LifeTimeLimit);
						return "Life Time Maximum Limit Reached ! ";
					}else if(output1.size() - counterDocGR >= DBO_AddDoc_OneTimeLimit){
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", counterDocGR" + counterDocGR + "DBO_AddDoc_OneTimeLimit" +DBO_AddDoc_OneTimeLimit);
						return "One Time Maximum Limit Reached ! ";
					}else{
						DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", GoodToGo");
						return "GoodToGo";
					}
				}
			
			}
			else if("arrRowDisable".equalsIgnoreCase(control))
			{ 
				JSONArray output = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS");
				JSONArray output1 = iformObj.getDataFromGrid("Q_USR_0_DBO_ADDNL_DOC_REQUIRED");
				if(stringdata.equalsIgnoreCase("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS"))
				{
					ArrayList<Object> outputData = new ArrayList<Object> ();
					String str_arrRow = "";
					if(!output.isEmpty()){
						for(int i =0; i<output.size(); i++){
							outputData.add(output.get(i));
						}
					}
					if(!outputData.isEmpty()){
						for(int i=0; i<outputData.size(); i++){
							String str_outputData = outputData.get(i).toString();
							DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", ArrayList<Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS> outputData " + str_outputData);
							if(str_outputData.contains("IsQuerySentToDEH\":\"Y\"")){
								DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+",  This time IsQuerySentToDEH is Y " );
								str_arrRow += i;
							 }
						}
					}
					/*String[] str_arrRowReturn = str_arrRow.split(",");
					int[] arrRowReturn = new int[str_arrRow.length()];
					for(int i=0; i<str_arrRow.length(); i++){
						arrRowReturn[i] = Integer.parseInt(str_arrRowReturn[i]);
					}*/
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", str_arrRow " + str_arrRow);
					return str_arrRow;
				}
				else if(stringdata.equalsIgnoreCase("Q_USR_0_DBO_ADDNL_DOC_REQUIRED"))
				{
					String str_arrRow = "";
					if(!output1.isEmpty()){
						for(int i =0; i<output1.size(); i++){
							String str_outputData = output1.get(i).toString();
							DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", JSONArray<Q_USR_0_DBO_ADDNL_DOC_REQUIRED> output " + str_outputData);
							if(str_outputData.contains("IsDocSentToDEH\":\"Y\"")){
								DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+",  This time IsDocSentToDEH is Y " );
								str_arrRow += i;
							 }
						}
					}
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", str_arrRow " + str_arrRow);
					return str_arrRow;
				}
			}
			else if("IDGeneration".equalsIgnoreCase(control))
			{ 
				//19/10/2023 14:03:21
				//yyyy-MM-dd
			    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			    LocalDateTime now = LocalDateTime.now();
			    
				String arr[] = stringdata.split("#");
				String prefix = arr[0];
				String queryID = uniqueIDGeneration(prefix);
				String tableId = arr[1];
				
				DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", onClick:::::::::: to check control for  userName 1  ");
				if("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS".equalsIgnoreCase(tableId))
				{
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", onClick:::::::   to check control for  userName  2   ");
					iformObj.setValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Unique_ID", queryID);
					iformObj.setValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Query_Name", activityName);
					iformObj.setValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Added_By", userName);
					iformObj.setValue("Q_USR_0_DBO_ADDNAL_INFO_REQ_DTLS_Added_Date_Time", dtf.format(now));
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", onClick::::::: to check control for  userName  3   ");
				}
				else if("Q_USR_0_DBO_ADDNL_DOC_REQUIRED".equalsIgnoreCase(tableId))
				{
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", onClick:::::::   to check control for  userName  2   ");
					iformObj.setValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Doc_Unique_ID", queryID);
					iformObj.setValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Queue_Name", activityName);
					iformObj.setValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_Added_By", userName);
					iformObj.setValue("Q_USR_0_DBO_ADDNL_DOC_REQUIRED_AddedDateTime", dtf.format(now));
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", onClick::::::: to check control for  userName  3   ");
				}
				else if("Q_USR_0_DBO_DECLINE_REJECT_DTLS".equalsIgnoreCase(tableId))
				{
					iformObj.setValue("Q_USR_0_DBO_DECLINE_REJECT_DTLS_Decline_Reason_ID", queryID);
				}
				else if("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS".equalsIgnoreCase(tableId))
				{
					iformObj.setValue("Q_USR_0_DBO_INTERNAL_CLRFCTN_DTLS_CLARIFICATION_ID", queryID);
				}
			} 
			else if("OpenExpandedCommunication".equalsIgnoreCase(control))
			{
				
				
				String decisionSeq = stringdata.trim();
				try 
				{
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", To get expanded data for internal clarification...");
					String Query =  "select H.workstep,H.action_date_time,C.clarificationcategory,C.ClarificationReason,C.clarificationRemarks from USR_0_DBO_INTERNAL_CLRFCTN_DTLS C with(nolock) , USR_0_DBO_WIHISTORY H with(nolock) "
							+ "where c.DecisionSeqNo=H.DecisionSeqNo and C.winame='"+WI_Name+"' and H.winame='"+WI_Name+"' and c.DecisionSeqNo='"+decisionSeq+"'";
					DBO.mLogger.info("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Query--" + Query);
					JSONArray jsonArray=new JSONArray();
					List lstdtls = iformObj.getDataFromDB(Query);
					for (int i = 0; i < lstdtls.size(); i++)
					{
						List<String> arr1 = (List) lstdtls.get(i);
						JSONObject obj=new JSONObject();
						obj.put("Workstep",arr1.get(0));
						obj.put("Added Date Time",arr1.get(1));
						obj.put("Query Category",arr1.get(2));
						obj.put("Query Type",arr1.get(3));
						obj.put("Remarks",arr1.get(4));
						jsonArray.add(obj);
					}
					iformObj.addDataToGrid("Exapnded_Internal_Communication", jsonArray);
				} 
				catch (Exception e)
				{
					DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+", Exception in getting expanded internal clarification dtls: " + e.getMessage());
					return "";
				}
			} 
			else if("RelatedPartyDetails".equalsIgnoreCase(control))
			{
				
			} 
			else if("FIRCOCheckDetails".equalsIgnoreCase(control))
			{
				
			} 
			else if("InternalClarification".equalsIgnoreCase(control))
			{
				
			} 
			else if("DeclineReason".equalsIgnoreCase(control))
			{
				
			} 
			else if("AdditionalInfoRequired".equalsIgnoreCase(control))
			{
				
			} 
			else if("AdditionalDocumentsRequired".equalsIgnoreCase(control)){
				
			} 
			else if("DecisionHistory".equalsIgnoreCase(control))
			{
				
			}
		
			
		}
		catch(Exception exc)
    		{
        		DBO.printException(exc);
    			DBO.mLogger.debug("WINAME: "+WI_Name+", WSNAME: "+activityName+", ControlName: "+control+",Exception 2 - " +exc);
    		}
    	return UNHANDLED;
    }

}