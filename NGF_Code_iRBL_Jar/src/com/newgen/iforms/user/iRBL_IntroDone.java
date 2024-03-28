package com.newgen.iforms.user;

import java.util.Calendar;
import java.util.List;
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
				String strRejectCodes = "";
				for(int p=0;p<Integer.parseInt(data);p++)
				{
					/*if(strRejectReasons=="")
						strRejectReasons=iformObj.getTableCellValue("REJECT_REASON_GRID",p,0);
					else
						strRejectReasons=strRejectReasons+"#"+iformObj.getTableCellValue("REJECT_REASON_GRID",p,0);*/
					
					String completeReason = null;
					completeReason = iform.getTableCellValue("REJECT_REASON_GRID", p, 0);
					iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Complete Reject Reasons" + completeReason);
					
					if (strRejectReasons == "")
					{						
						if(completeReason.indexOf("-")>-1)
						{
							strRejectCodes=completeReason.substring(0,completeReason.indexOf("-")).replace("(", "").replace(")", "");
							iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons code" + strRejectCodes);
							strRejectReasons=completeReason.substring(completeReason.indexOf("-")+1);
							iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons" + strRejectReasons);
						}
						else
						{
							strRejectReasons=completeReason;
							iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons else block" + strRejectReasons);
						}
					}	
					else
					{
						iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons 1" + strRejectReasons);						
						if(completeReason.indexOf("-")>-1)
						{
							strRejectCodes=strRejectCodes+"#"+completeReason.substring(0,completeReason.indexOf("-")).replace("(", "").replace(")", "");
							iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons code" + strRejectCodes);
							strRejectReasons=strRejectReasons+"#"+completeReason.substring(completeReason.indexOf("-")+1);
							iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons" + strRejectReasons);
						}
						else
						{
							strRejectReasons=strRejectReasons+"#"+completeReason;
							iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons else block2" + strRejectReasons);
						}
						
						iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", Reject Reasons 2" + strRejectReasons);
					}
					
				}
				/*String EntryDateTime = iform.getValue("EntryDateTime").toString();
				String newEntryDateTime="";
				if(!EntryDateTime.equals(""))
				{
					String[] a = EntryDateTime.split(" ");
					String[] d = a[0].split("-");
					String[] t = a[1].split(":");
					
					//Added for handling month***************
					String[] month_array={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
					String[] Integer_array={"01","02","03","04","05","06","07","08","09","10","11","12"};
					for (int z=0;z<month_array.length;z++)
					{
						if(d[1].indexOf(month_array[z]) != -1)
							d[1]=Integer_array[z];
					}
					//************************************
					
					newEntryDateTime=d[2]+'/'+d[1]+'/'+d[0]+' '+t[0]+':'+t[1]+':'+t[2];
					
				}*/
				
				iRBL.mLogger.debug("Final reject reasons are "+strRejectReasons);
				JSONArray jsonArray=new JSONArray();
				JSONObject obj=new JSONObject();
				Calendar cal = Calendar.getInstance();
			   // SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");			   
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    String strDate = sdf.format(cal.getTime());
			    
				obj.put("Date Time",strDate);
				obj.put("Workstep",iform.getActivityName());
				obj.put("User Name", iform.getUserName());
				obj.put("Decision",iform.getValue("qDecision"));
				obj.put("Reject Reasons", strRejectReasons);
				obj.put("Reject Reason Codes", strRejectCodes);
				obj.put("Remarks", iform.getValue("REMARKS"));
				
			
				iRBL.mLogger.debug("Decision" +iform.getValue("qDecision"));
				
				if("Initiation".equalsIgnoreCase(iform.getActivityName()))
					obj.put("Entry Date Time",iform.getValue("CreatedDateTime"));
				else
					obj.put("Entry Date Time",iform.getValue("EntryDateTime"));
				
				iRBL.mLogger.debug("Entry Date Time : "+obj.get("Entry Date Time"));
				jsonArray.add(obj);
				iform.addDataToGrid("Q_USR_0_IRBL_WIHISTORY", jsonArray);
				
				iRBL.mLogger.debug("jsonArray : "+jsonArray);
			
				//strReturn = "INSERTED";
				
				iRBL.mLogger.debug("WINAME: "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", ControlName: "+controlName+", WI Histroy Added Successfully!");
			} 
			catch (Exception e) {
				iRBL.mLogger.debug("Exception in inserting WI History!" + e.getMessage());
			}
		}
		
		if("SystemCheckIntegrationlatestSuccessDate".equals(controlName))  // this is required to check 30 days expiry on the queues
		{
			String SysCheckDateTime = "";
			try 
			{				
				List lstDecisions = iform
					.getDataFromDB("select top 1 ACTION_DATE_TIME from USR_0_IRBL_WIHISTORY with(nolock) where WI_NAME = '"+getWorkitemName()+"' and WORKSTEP = 'Sys_Checks_Integration' order by ACTION_DATE_TIME desc");
				iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());
								
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					SysCheckDateTime= arr1.get(0);
				}
				strReturn = SysCheckDateTime;
				
			}
			catch (Exception e) 
			{
				iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+iform.getActivityName()+", Exception in SystemCheckIntegrationlatestSuccessDate " + e.getMessage());
			}
			
		}
		
		return strReturn;
	}
}