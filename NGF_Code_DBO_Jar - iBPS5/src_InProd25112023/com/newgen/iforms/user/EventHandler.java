package com.newgen.iforms.user;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.json.simple.JSONArray;

import com.newgen.iforms.EControl;
import com.newgen.iforms.FormDef;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.custom.IFormServerEventHandler;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;
import com.newgen.mvcbeans.model.WorkdeskModel;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;


public class EventHandler extends DBOCommon implements IFormServerEventHandler
{	
	
	public IFormReference iFormOBJECT;
	public String sessionId="";
	
	public WDGeneralData wdgeneralObj;
	
	
	@Override
	public void beforeFormLoad(FormDef arg0, IFormReference arg1) 
	{
		
	}
	 public String introduceWorkItemInWorkFlow( IFormReference p0,  HttpServletRequest p1,  HttpServletResponse p2)
	 {
		 return "";
	 }
	    
	 public   String introduceWorkItemInWorkFlow( IFormReference p0,  HttpServletRequest p1,  HttpServletResponse p2,  WorkdeskModel p3)
	 {
		 return "";
	 }

	@Override
	public String executeCustomService(FormDef arg0, IFormReference arg1,
			String arg2, String arg3, String arg4) 
	{
		return null;
	}

	@Override
	public JSONArray executeEvent(FormDef arg0, IFormReference arg1,String arg2, String arg3) 
	{
		return null;
	}

	
	public String executeServerEvent(IFormReference iformObj, String control,String event, String Stringdata)
	{
		DBO.mLogger.info("Inside executeServerEvent() ak 107 ---control: " + control + "\nevent: " + event + "\nStringdata" +Stringdata);
		wdgeneralObj = iformObj.getObjGeneralData();
		sessionId = wdgeneralObj.getM_strDMSSessionId();
		iFormOBJECT = iformObj;
		event = event.toLowerCase();
		switch(event) {
		case "click" : return new DBO_clickhandler().onClick(iformObj, control, Stringdata);
		case "focus" : return new DBO_onFocus().onFocus(iformObj, control, Stringdata);
		case "change": return new DBO_ChangeHandler().onChange(iformObj, control, Stringdata);
		case "formload" : return new DBO_FormLoad().formLoad(iformObj, control, Stringdata);
		case "introducedone" :	 try {
				return new DBO_IntroDone().onIntroduceDone(iformObj, control, Stringdata);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case "sectionload" :	 return new DBO_LoadSection().onLoadSection(iformObj, control, Stringdata);
		case "posthookloadsection" :	 return new DBO_LoadSectionPostHook().onLoadSectionPostHook(iformObj, control, Stringdata);
		default		 : return "unhandled event";
		}
	}

	@Override
	public String getCustomFilterXML(FormDef arg0, IFormReference arg1,String arg2) 
	{
		return null;
	}

	@Override
	public String setMaskedValue(String arg0, String arg1) 
	{	return arg1;
	}

	@Override
	public JSONArray validateSubmittedForm(FormDef arg0, IFormReference arg1,String arg2) 
	{
		return null;
	}

	@Override
	public String generateHTML(EControl arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateDataInWidget(IFormReference arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String validateDocumentConfiguration(String arg0, String arg1, File arg2, Locale arg3) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean introduceWorkItemInSpecificProcess(IFormReference arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String onChangeEventServerSide(IFormReference arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String postHookExportToPDF(IFormReference arg0, File arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void postHookOnDocumentUpload(IFormReference arg0, String arg1, String arg2, File arg3, int arg4) {
		// TODO Auto-generated method stub
		
	}
}

