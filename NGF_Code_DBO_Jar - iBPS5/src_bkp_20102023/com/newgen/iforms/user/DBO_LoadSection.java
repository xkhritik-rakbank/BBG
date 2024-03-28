package com.newgen.iforms.user;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.newgen.iforms.custom.IFormReference;

public class DBO_LoadSection extends DBOCommon {

	private static final String EXCEPTION_OCCURED = null;
	private static final String UNHANDLED = null;
	private static final String SUCCESS = null;
	private static final String FAIL = null;
	private String WI_Name = null;
	private String activityName = null;
	private IFormReference giformObj = null;

	public String onLoadSection(IFormReference iformObj, String control, String stringdata) {
		DBO.mLogger.info("onLoadSection:::::::::::::::::::::::::::::");
		activityName = iformObj.getActivityName();
		WI_Name = getWorkitemName(iformObj);
		giformObj = iformObj;
		try {
			
		} catch (Exception exc) {
			DBO.printException(exc);
			DBO.mLogger.debug("Exception 2 - " + exc);
		}
		return UNHANDLED;
	}

}