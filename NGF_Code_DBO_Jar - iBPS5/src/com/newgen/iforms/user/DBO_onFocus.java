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

public class DBO_onFocus extends DBOCommon {

	private static final String EXCEPTION_OCCURED = null;
	private static final String UNHANDLED = null;
	private static final String SUCCESS = null;
	private static final String FAIL = null;
	private String WI_Name = null;
	private String actName = null;
	private String userName = null;
	private String inputString = null;
	private IFormReference giformObj = null;

	public String onFocus(IFormReference iformObj, String control, String stringdata) {
		DBO.mLogger.info("Inside onChange method of DBO_onFocus with control id-- " + control);
		WI_Name = getWorkitemName(iformObj);
		actName = iformObj.getActivityName();
		userName = iformObj.getUserName();
		inputString = stringdata;
		giformObj = iformObj;
		String decision =  (String) iformObj.getValue("DECISION");

		try {
			DBO.mLogger.debug("value of control inside focus event before if condition " + control);
			DBO.mLogger.debug("value of control inside change event" + iformObj.getValue("AssignToGroup"));
			
			if("AssignToGroup".equalsIgnoreCase(control))
			{
				DBO.mLogger.info("To removeItemFromCombo AssignToGroup Start  ...");
				String groupName = "";
				String query = "select top 1 GroupName from (select  GroupName from PDBGroup "
							+ "with(nolock) where GroupIndex in (select GroupIndex from PDBGroupMember "
							+ "with(nolock) where UserIndex = (select userindex from PDBUser "
							+ "with(nolock) where UserName='"+userName+"')) intersect select  GroupName from "
							+ "PDBGroup with(nolock) where GroupIndex in (select Userid from QUEUEUSERTABLE"
							+ " with(nolock) where AssociationType=1 and QueueID = (select QueueID from "
							+ "QUEUEDEFTABLE with(nolock) where QueueName='DBO_STP_Operator'))) as temp"
							+ " order by GroupName desc";
				groupName = getUserGroup(query , iformObj);
				DBO.mLogger.info("groupName value ..." +groupName);
				if(!groupName.contains("_Controls")){
					DBO.mLogger.info("groupName value inside if loop of groupNme value..." +groupName);
					iformObj.removeItemFromCombo("AssignToGroup", 4);
					DBO.mLogger.info("groupName value inside if loop of groupNme value after removing the value from combo..." +groupName);
				}
				DBO.mLogger.info("To removeItemFromCombo AssignToGroup End  ...");
			}
		} catch (Exception exc) {
			DBO.printException(exc);
			DBO.mLogger.debug("Exception 2 - " + exc);
		}

		return UNHANDLED;
	}

}
