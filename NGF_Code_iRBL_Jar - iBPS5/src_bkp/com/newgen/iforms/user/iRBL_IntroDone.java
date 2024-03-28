package com.newgen.iforms.user;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.newgen.iforms.custom.IFormReference;

public class iRBL_IntroDone extends iRBL_Common
{
	public String onIntroduceDone(IFormReference iform, String controlName,String event, String data)
	{
		String strReturn="";
		iRBL.mLogger.debug("This is iRBL_IntroDone_Event");
		if("InsertIntoHistory".equals(controlName))
		{
			try {
				iRBL.mLogger.debug("Reject Reasons Grid Length is "+data);
				String strRejectReasons="";
				for(int p=0;p<Integer.parseInt(data);p++)
				{
					if(strRejectReasons=="")	
						strRejectReasons=iform.getTableCellValue("REJECT_REASON_GRID",p,0);
					else
						strRejectReasons=strRejectReasons+"#"+iform.getTableCellValue("REJECT_REASON_GRID",p,0);
				}
				
				iRBL.mLogger.debug("Final reject reasons are "+strRejectReasons);
				JSONArray jsonArray=new JSONArray();
				JSONObject obj=new JSONObject();
				Calendar cal = Calendar.getInstance();
			   // SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");			   
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
			    String strDate = sdf.format(cal.getTime());
			    
				obj.put("Date Time",strDate);
				obj.put("Workstep",iform.getActivityName());
				obj.put("User Name", iform.getUserName());
				obj.put("Decision",iform.getValue("qDecision"));
				obj.put("Reject Reasons", strRejectReasons);
				obj.put("Remarks", iform.getValue("REMARKS"));
				
			
				iRBL.mLogger.debug("Decision" +iform.getValue("qDecision"));
				
				if("Initiation".equalsIgnoreCase(iform.getActivityName()))
					obj.put("Entry Date Time",iform.getValue("CreatedDateTime"));
				else
					obj.put("Entry Date Time",iform.getValue("EntryDateTime"));
				jsonArray.add(obj);
				iform.addDataToGrid("Q_USR_0_IRBL_WIHISTORY", jsonArray);
				
				iRBL.mLogger.debug("Created Date Time"+iform.getValue("CreatedDateTime"));
				iRBL.mLogger.debug("Entry Date Time"+iform.getValue("EntryDateTime"));
			
				
			} catch (Exception e) {
				iRBL.mLogger.debug("Exception in check if system Check Required non borrowing" + e.getMessage());
			}
		}
		return strReturn;
	}
}