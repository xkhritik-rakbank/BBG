package com.newgen.iforms.user;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.xml.sax.SAXException;

import com.newgen.iforms.*;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.custom.IFormServerEventHandler;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;


public class EventHandler extends iRBL_Common  implements IFormServerEventHandler
{	
	public static IFormReference iFormOBJECT;
	public String sessionId="";
	
	public WDGeneralData wdgeneralObj;
	
	@Override
	public void beforeFormLoad(FormDef arg0, IFormReference arg1) 
	{
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
		//CSR_OCC.setLogger();
		iRBL.mLogger.info("Inside executeServerEvent() ak 101 ---control: " + control + "\nevent: " + event + "\nStringData: "
				+ Stringdata);
		wdgeneralObj = iformObj.getObjGeneralData();
		sessionId = wdgeneralObj.getM_strDMSSessionId();
		iFormOBJECT = iformObj;
		
		event = event.toUpperCase();
		
		switch(event)
		{
			case "FORMLOAD": return new iRBL_FormLoad().formLoadEvent(iformObj,control,event,Stringdata);
			case "CLICK" : try {
				return new iRBL_Click().clickEvent(iformObj,control,Stringdata);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			case "INTRODUCEDONE" :	 return new iRBL_IntroDone().onIntroduceDone(iformObj,control,event,Stringdata);
			default: return "";
			
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
	public JSONArray validateSubmittedForm(FormDef arg0, IFormReference arg1,
			String arg2) 
	{
		return null;
	}
}

