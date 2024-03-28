package com.newgen.iforms.user;

import org.json.simple.JSONArray;
import java.util.List;
import org.json.simple.JSONObject;
import com.newgen.iforms.custom.IFormReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class iRBL_FormLoad extends iRBL_Common
{
	
	public String formLoadEvent(IFormReference iform, String controlName,String event, String data)
	{
		String strReturn=""; 
	
		iRBL.mLogger.debug("This is iRBL_FormLoad_Event"+event+" controlName :"+controlName);
		
		String Workstep=iform.getActivityName();
		iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Workstep :"+Workstep);
		
		if("DecisionDropDown".equals(controlName))
		{
			try {				
				List lstDecisions = iform
					.getDataFromDB("SELECT DECISION FROM USR_0_IRBL_DECISION_MASTER WITH(NOLOCK) WHERE WORKSTEP_NAME='"+iform.getActivityName()+"' and ISACTIVE='Y' ORDER BY DECISION ASC");
				
				String value="";
				iform.clearCombo("qDecision");
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					value=arr1.get(0);
					iform.addItemInCombo("qDecision",value,value);
					strReturn="Decision Loaded";
				}
				
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Decision drop down load " + e.getMessage());
			}
		}
		//To load all the exceptions automatically.
		else if (controlName.equalsIgnoreCase("Exception"))
		{
			iform.getDataFromGrid("Q_USR_0_IRBL_EXCEPTION_HISTORY").clear();
			iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", after clear from Q_USR_0_IRBL_EXCEPTION_HISTORY "+iform.getDataFromGrid("Q_USR_0_IRBL_EXCEPTION_HISTORY").size());
			iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Data Coming in Exceptions is "+data);
			try {	
					List<List<String>> lstException = iform
							.getDataFromDB("SELECT ExceptionName,CanRaise,CanClear,CanView FROM USR_0_IRBL_EXCEPTION_MASTER WITH(NOLOCK) where ISACTIVE='Y' and WORKSTEP_NAME='"+Workstep+"'");
					JSONArray jsonArray = new JSONArray();
					String value = "";
					for (int i = 0; i < lstException.size(); i++) {						
						JSONObject obj = new JSONObject();
						List<String> arr = (List) lstException.get(i);
						value = arr.get(0);
						iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+",  value : "+value);
						obj.put("Exception", value);
						if ("".equals(strReturn)) {
							strReturn = strReturn + value + ":" + arr.get(1) + ":" + arr.get(2) + ":" + arr.get(3);
						} else {
							strReturn = strReturn + "~" + value + ":" + arr.get(1) + ":" + arr.get(2) + ":"
									+ arr.get(3);
						}
						jsonArray.add(obj);
					}
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", return for exception " + jsonArray.toJSONString());
					// iform.addDataToGrid("table7", jsonArray);
					if(!("Rights".equals(data)))
					{
						iform.addDataToGrid("Q_USR_0_IRBL_EXCEPTION_HISTORY", jsonArray);						
					}
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in ExceptionHistory load " + e.getMessage());
			}
		}
		//Raising Automatic exception*******************************************************************
		else if("RaiseAutomaticException".equals(controlName))
		{
		try {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Data Coming in RaiseAutomaticException is "+data);
				int tablecount = iform.getDataFromGrid("Q_USR_0_IRBL_EXCEPTION_HISTORY").size();
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", tablecount "+tablecount);
				for (int i = 0; i< tablecount; i++)
				{
					String exceptioName=iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i, 1);
					if(exceptioName.equalsIgnoreCase(data))
					{
						Calendar cal = Calendar.getInstance();
					    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					    String strDate = sdf.format(cal.getTime());
						String strRaisedCleared="Raised";
						String strNewLine="";
						if("".equals(iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i, 4)) || iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i, 4)==null)
							strNewLine="";
						else
							strNewLine="\n";
						
						
						iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i,0,"true");
						iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i,2,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i, 2)+strNewLine+iform.getActivityName());
						iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i,3,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i, 3)+strNewLine+"System");
						iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i,4,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i, 4)+strNewLine+strRaisedCleared);
						iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i,5,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",i, 5)+strNewLine+strDate);
						strReturn=data;					
						iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Successfully Raised Automatic Exception for "+strReturn);
					}
			  }			
				
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Raising Automatic Exception " + e.getMessage());
			}
			//*****************************************************************************
		}	
		//To set values when user manually make changes in Exception History Window. 
		else if("raiseClearException".equals(controlName))
		{
			try {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Data for Exception is "+data);
				String strCheckUncheck=iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 0);
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception check uncheck is "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 0));
				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    String strDate = sdf.format(cal.getTime());
				String strRaisedCleared="";
				String strNewLine="";
				if("".equals(iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)) || iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)==null)
					strNewLine="";
				else
					strNewLine="\n";
				if("true".equals(strCheckUncheck))
					strRaisedCleared="Raised";
				else
					strRaisedCleared="Approved";
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 2) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 2)+strNewLine+iform.getActivityName());
				iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),2,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 2)+strNewLine+iform.getActivityName());
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 3) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 3)+strNewLine+iform.getUserName());
				iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),3,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 3)+strNewLine+iform.getUserName());
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 4) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)+strNewLine+strRaisedCleared);
				iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),4,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)+strNewLine+strRaisedCleared);
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 5) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 5)+strNewLine+strDate);
				iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),5,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 5)+strNewLine+strDate);
				strReturn = "Cleared";
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Service Request drop down load " + e.getMessage());
			}
		}
		//To calculate aging in days automatically.
        else if("AgeingInDays".equals(controlName))
		{
			try {				
				List lstDecisions = iform
					.getDataFromDB("select dbo.GetOPSTAT_IRBL('"+getWorkitemName(iform)+"','"+iform.getActivityName()+"')  as OPSTAT where 1= 1");
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());
				
				//String Ageingvalue=lstDecisions.toString();
				
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					//Ageingvalue=arr1.get(0);
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", arr1.get(0) : "+arr1.get(0));
					
					strReturn=arr1.get(0);
				}
				
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in AgeingInDays " + e.getMessage());
			}
		}
		//To load RM,SM, SOL_ID from RO automatically.
        else if("RO".equals(controlName))
		{
			String ROField = (String) iform.getValue("RO");
			iRBL.mLogger.debug("ROField -: "+ROField);
			try 
			{				
				List lstDecisions = iform
					.getDataFromDB("SELECT RM,SOLID,SM FROM USR_0_IRBL_RMSMRO_Master WITH(NOLOCK) WHERE RO='"+ROField+"' AND ISACTIVE='Y' ORDER BY RO ASC");
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());
				
				String value1="";
				String value2="";
				String value3="";
				iform.setValue("RM","");
				iform.setValue("SOL_ID","");
				iform.setValue("SM","");
				
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					value1=arr1.get(0);
					value2=arr1.get(1);
					value3=arr1.get(2);
					iform.setValue("RM",value1);
					iform.setValue("SOL_ID",value2);
					iform.setValue("SM",value3);
					strReturn="RM SM RO SOL_ID Loaded";
				}
			}
			catch (Exception e) 
			{
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in RM,SM,SOLID load " + e.getMessage());
			}
		}
		//To Fetch Signature details.
        else if("Signature".equals(controlName))
		{
			try 
			{	
				iRBL.mLogger.debug("Inside Signature");
				List lstDecisions = iform
						.getDataFromDB("SELECT DISTINCT AcctId FROM USR_0_iRBL_InternalExpo_AcctDetails WITH(NOLOCK) WHERE Wi_Name = '"+getWorkitemName(iform)+"'");
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Account ID : "+arr1.get(0));
					
					strReturn=strReturn+arr1.get(0)+"@";
					//strReturn=strReturn.substring(0,strReturn.length()-1);
					iRBL.mLogger.debug("strReturn---"+strReturn);
				}
				
			} 
			catch (Exception e) 
			{
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Loading Signature !" + e.getMessage());
			}
		}
		//Count for raising Nationality Exception
        else if("RestrictedValues".equals(controlName))
        {	
        	int CRPartygridsize=iform.getDataFromGrid("Q_USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS").size();
        	//int count=0;
        	String value="";
        	String StrNationality="";
        	try 
        	{
        		/*for(int i=0;i<CRPartygridsize;i++)
        		{
        			String Nationality = iform.getTableCellValue("Q_USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS", i,30);
        			if(StrNationality.equals(""))
        				StrNationality=Nationality;
        			else
        				StrNationality=StrNationality+"','"+Nationality;
    				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", StrNationality : "+StrNationality);

        		}*/
        		
        		String query = "select count(*) from USR_0_IRBL_CountryMaster with(nolock) where countryCode in("
        				+ " (select NATIONALITY from USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS with(nolock) where wi_name = '"+getWorkitemName(iform)+"' and NATIONALITY is not null and NATIONALITY != ''"
        				+ " UNION ALL"
        				+ " select ADDITIONALNATIONALITY from USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS with(nolock) where wi_name = '"+getWorkitemName(iform)+"' and ADDITIONALNATIONALITY is not null and ADDITIONALNATIONALITY != ''"
        				+ " UNION ALL"
        				+ " select ADDITIONALNATIONALITY2 from USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS with(nolock) where wi_name = '"+getWorkitemName(iform)+"' and ADDITIONALNATIONALITY2 is not null and ADDITIONALNATIONALITY2 != ''"
        				+ " UNION ALL"
        				+ " select ADDITIONALNATIONALITY3 from USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS with(nolock) where wi_name = '"+getWorkitemName(iform)+"' and ADDITIONALNATIONALITY3 is not null and ADDITIONALNATIONALITY3 != '' )"
        				+ " )"
        				+ " and IsRestricted = 'Y'";
        		
        		iRBL.mLogger.debug("Query for Retricted Nationality :"+query);
        		
    			List lstDecisions = iform
    				.getDataFromDB(query);
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());

    			for(int j=0;j<lstDecisions.size();j++)
				{
					List<String> arr1=(List)lstDecisions.get(j);
        			
					value=arr1.get(0);
				}
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Nationality value : "+value);

        		//value=Integer.toString(count);
        		strReturn=value;
        	}
        	catch (Exception e) 
        	{
        		iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Nationality Restriction " + e.getMessage());
        	}
        }
		//Count for raising Demographic Exception
        else if("DemographicValues".equals(controlName))
        {	
        	int CRPartygridsize=iform.getDataFromGrid("Q_USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS").size();
        	//int count=0;
        	String value="";
        	String StrDemographic="";
        	try 
        	{
        		/*for(int i=0;i<CRPartygridsize;i++)
        		{
        			String Demographic = iform.getTableCellValue("Q_USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS", i,30);
        			if(StrDemographic.equals(""))
        				StrDemographic=Demographic;
        			else
        				StrDemographic=StrDemographic+"','"+Demographic;
    				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", StrDemographic : "+StrDemographic);
        		}*/
        		String query = "select count(*) from USR_0_IRBL_CountryMaster with(nolock) where countryCode in("
        				+ " select DEMOGRAPHIC from USR_0_IRBL_DEMOGRAPHIC_DTLS with(nolock) where WI_NAME = '"+getWorkitemName(iform)+"'"
        				+ " ) and IsDemographic = 'Y'";
    			List lstDecisions = iform
    				.getDataFromDB(query);
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());

    			for(int j=0;j<lstDecisions.size();j++)
				{
					List<String> arr1=(List)lstDecisions.get(j);
        			
					value=arr1.get(0);
				}
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Demographic value : "+value);

        		//value=Integer.toString(count);
        		strReturn=value;
        	}
        	catch (Exception e) 
        	{
        		iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Demographic Restriction " + e.getMessage());
        	}
        }
		//To fetch the names of all the exceptions to clear them automatically.
        else if("ExceptionNames".equals(controlName))
        {	
        	int exceptionGridSize=iform.getDataFromGrid("Q_USR_0_IRBL_EXCEPTION_HISTORY").size();
        	String checkNames="";
        	try 
        	{
        		for(int i=0;i<exceptionGridSize;i++)
        		{
        			String exceptionName = iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY", i,1);
        			if(checkNames.equals(""))
        				checkNames=exceptionName;
        			else
        				checkNames=checkNames+","+exceptionName;
    				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", checkNames : "+checkNames);
        		}
        		strReturn=checkNames;
        	}
        	catch (Exception e) 
        	{
        		iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Loading Exception Names" + e.getMessage());
        	}
        }
		//To clear all the exceptions automatically.
        else if("raiseAutomaticClearException".equals(controlName))
		{
			try {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Data for Exception is "+data);
				String strCheckUncheck=iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 0);
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception check uncheck is "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 0));
				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    String strDate = sdf.format(cal.getTime());
				String strRaisedCleared="";
				String strNewLine="";
				if("".equals(iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)) || iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)==null)
					strNewLine="";
				else
					strNewLine="\n";
				if("true".equals(strCheckUncheck))
				{
					strRaisedCleared="Approved";
					
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 0) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 0));
					iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),0,"false");
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 2) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 2)+strNewLine+iform.getActivityName());
					iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),2,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 2)+strNewLine+iform.getActivityName());
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 3) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 3)+strNewLine+iform.getUserName());
					iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),3,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 3)+strNewLine+iform.getUserName());
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 4) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)+strNewLine+strRaisedCleared);
					iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),4,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 4)+strNewLine+strRaisedCleared);
					iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", iform.getTableCellValue(Q_USR_0_IRBL_EXCEPTION_HISTORY,Integer.parseInt(data), 5) "+iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 5)+strNewLine+strDate);
					iform.setTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data),5,iform.getTableCellValue("Q_USR_0_IRBL_EXCEPTION_HISTORY",Integer.parseInt(data), 5)+strNewLine+strDate);
					
				}
				strReturn = "Cleared";
			} 
			catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Service Request drop down load " + e.getMessage());
			}
		}
		//To load Third Party Vendor Email ID from Third Party Vendor field automatically.
        else if("SetEmail".equals(controlName))
		{
			String TPVendor = (String) iform.getValue("THIRD_PARTY_VENDOR");
			iRBL.mLogger.debug("TPVendor : "+TPVendor);
			try 
			{				
				List lstDecisions = iform
					.getDataFromDB("SELECT ThirdParty_Email FROM USR_0_IRBL_THIRDPARTY_MASTER WITH(NOLOCK) WHERE THIRDPARTY='"+TPVendor+"' AND ISACTIVE='Y'");
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());
				
				String value1="";
				iform.setValue("THIRD_PARTY_VENDOR_EMAIL","");
				
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					value1=arr1.get(0);
					iform.setValue("THIRD_PARTY_VENDOR_EMAIL",value1);
					strReturn="Email Loaded";
				}
			}
			catch (Exception e) 
			{
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in loading Third Party Vendor Email " + e.getMessage());
			}
		}
        else if("RestrictedValues_Nationality".equals(controlName))
        {
			try {	
				//String Nationality = (String) iform.getValue("Q_USR_0_IRBL_SIGNATORY_GRID_DTLS_NATIONALITY");			
				/*List lstDecisions = iform
					.getDataFromDB("SELECT IsRestricted FROM USR_0_IRBL_CountryMaster WITH(NOLOCK) WHERE countryCode='"+iform.getValue("Q_USR_0_IRBL_SIGNATORY_GRID_DTLS_NATIONALITY")+"' and ISACTIVE='Y'");
				*/
				List lstDecisions = iform
					.getDataFromDB("SELECT NationalityStatus FROM USR_0_IRBL_CountryMaster WITH(NOLOCK) WHERE countryCode='"+iform.getValue("Q_USR_0_IRBL_SIGNATORY_GRID_DTLS_NATIONALITY")+"' and ISACTIVE='Y' and NationalityStatus IS NOT NULL");
				
				
				String value="";
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					value=arr1.get(0);
					/*if("Y".equalsIgnoreCase(value))
					  iform.setValue("Q_USR_0_IRBL_SIGNATORY_GRID_DTLS_NATIONALITY_STATUS","Restricted");
					else
					  iform.setValue("Q_USR_0_IRBL_SIGNATORY_GRID_DTLS_NATIONALITY_STATUS","Not Restricted");
					 */
					 iform.setValue("Q_USR_0_IRBL_SIGNATORY_GRID_DTLS_NATIONALITY_STATUS",value);
					
					strReturn="Decision Loaded";
				}
				
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Decision drop down load " + e.getMessage());
			}
		}
        else if("ProposedTenorValue".equals(controlName))
        {
        	String ProposedTenor = (String) iform.getValue("PROPOSED_TENOR");
			iRBL.mLogger.debug("ProposedTenor : "+ProposedTenor); 
			try {	
				List lstDecisions = iform
					.getDataFromDB("select Description from USR_0_iRBL_Proposed_Tenor_Master with(nolock) where code ='"+ProposedTenor+"'");
				
				String value="";
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					value=arr1.get(0);
					
					strReturn=value;
				}
				
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in loading Proposed Tenor value" + e.getMessage());
			}
		}
        else if("setProductPriorityLevelBasedOnPriority".equals(controlName))
        {
        	String Priority = (String) iform.getValue("PRIORITY");
        	String query="";
        	if("Express".equalsIgnoreCase(Priority.trim()))
        		query = "update WFINSTRUMENTTABLE set PriorityLevel = 4 where ProcessInstanceID = '"+getWorkitemName(iform)+"'";
        	else 
        		query = "update WFINSTRUMENTTABLE set PriorityLevel = 1 where ProcessInstanceID = '"+getWorkitemName(iform)+"'";
        	iform.saveDataInDB(query);
        }
        else if("blacklistException".equals(controlName))
        {
        	List lstDecisions = iform.getDataFromDB("SELECT MATCH_STATUS FROM USR_0_IRBL_BLACKLIST_GRID_DTLS WITH(nolock) WHERE WI_NAME='"+getWorkitemName(iform)+"'");
			iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", lstDecisions : "+lstDecisions.toString());
			String value="";
			for(int i=0;i<lstDecisions.size();i++)
			{
				List<String> arr1=(List)lstDecisions.get(i);
				value=arr1.get(0);
				if("true".equalsIgnoreCase(value))
				{
					strReturn=value;
					break;
				}
			}
			
        }
        else if("blacklistException_firco".equals(controlName))
        {
        	List lstDecisions = iform.getDataFromDB("SELECT MATCH_STATUS FROM USR_0_IRBL_FIRCO_GRID_DTLS WITH(nolock) WHERE WI_NAME='"+getWorkitemName(iform)+"'");
			iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", lstDecisions : "+lstDecisions.toString());
			String value="";
			for(int i=0;i<lstDecisions.size();i++)
			{
				List<String> arr1=(List)lstDecisions.get(i);
				value=arr1.get(0);
				if("true".equalsIgnoreCase(value))
				{
					strReturn=value;
					break;
				}
			}
        }
        else if("PEPException".equals(controlName))
        {
        	List lstDecisions = iform.getDataFromDB("SELECT ISGOVERNMENTRELATION, COMPANYCATEGORY FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(nolock) WHERE WI_NAME='"+getWorkitemName(iform)+"'");
			iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", lstDecisions : "+lstDecisions.toString());
			for(int i=0;i<lstDecisions.size();i++)
			{
				List<String> arr1=(List)lstDecisions.get(i);
				if("Y".equalsIgnoreCase(arr1.get(0)) || "Yes".equalsIgnoreCase(arr1.get(0)) || "RF".equalsIgnoreCase(arr1.get(1)))
				{ 
					strReturn="true";
					break;
				}
			}
        }
		//Count for raising Demographic Exception
        else if("POSCalculation".equals(controlName))
        {
        	try 
        	{
        	  double Pos_Turnover_Amount = (iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_POS_TURNOVER_AMOUNT"))!=""?Double.parseDouble((String) iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_POS_TURNOVER_AMOUNT")):0.0;
              double Pos_Turnover_Timeperiod = (iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_POS_TURNOVER_TIMEPERIOD"))!=""?Double.parseDouble((String) iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_POS_TURNOVER_TIMEPERIOD")):0.0;
              double Turnover_Amount = (iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_TURNOVER_AMOUNT"))!=""?Double.parseDouble((String) iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_TURNOVER_AMOUNT")):0.0;
              double Turnover_Timeperiod= (iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_TURNOVER_TIMEPERIOD"))!=""?Double.parseDouble((String) iform.getValue("Q_USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS_TURNOVER_TIMEPERIOD")):0.0;
                         		  
	            if(!"0.0".equals(Turnover_Timeperiod))
	            {
	                double temporary_var1=(12/Turnover_Timeperiod);
	                Turnover_Amount=Turnover_Amount*temporary_var1;
	            }
	            if(!"0.0".equals(Pos_Turnover_Timeperiod))
	            {
	            	double temporary_var2=(12/Pos_Turnover_Timeperiod);
	            	Pos_Turnover_Amount= Pos_Turnover_Amount*temporary_var2;
	            }    
              
              double POS_Percentage=0;
              if(Turnover_Amount>0.0 && Pos_Turnover_Amount>0.0)
              {
                             
                 POS_Percentage = (((Pos_Turnover_Amount/Turnover_Amount)*100));
                 List POS_PercentageCal = iform.getDataFromDB("select round("+POS_Percentage+",2)");
                 for(int i=0;i<POS_PercentageCal.size();i++)
                 {
                            List<String>arr1=(List)POS_PercentageCal.get(i);
                            strReturn=arr1.get(0);   
                 }
              }
        	} 
        	catch (Exception e)
        	{
        		iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in POSCalculation " + e.getMessage());
        	}
        }
        else if("MacroDropDown".equals(controlName))
		{
			try {
				List lstDecisions = iform
					.getDataFromDB("SELECT distinct MACRO FROM USR_0_IRBL_Micro_Macro_Master WITH(NOLOCK) WHERE Sector='"+iform.getValue("Q_RB_iRBL_TXNTABLE_DROPDOWN_FIELD1")+"' and ISACTIVE='Y'");
				
				String value="";
				iform.clearCombo("Q_RB_iRBL_TXNTABLE_DROPDOWN_FIELD2");
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					value=arr1.get(0);
					iform.addItemInCombo("Q_RB_iRBL_TXNTABLE_DROPDOWN_FIELD2",value,value);
					strReturn="IndustryMacro Loaded";
				}
				
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in IndustryMacro drop down load " + e.getMessage());
			}
		}
        else if("MicroDropDown".equals(controlName))
		{
			try {
				List lstDecisions = iform
					.getDataFromDB("SELECT distinct MICRO FROM USR_0_IRBL_Micro_Macro_Master WITH(NOLOCK) WHERE Sector='"+iform.getValue("Q_RB_iRBL_TXNTABLE_DROPDOWN_FIELD1")+"' and MACRO='"+iform.getValue("Q_RB_iRBL_TXNTABLE_DROPDOWN_FIELD2")+"' and ISACTIVE='Y'");
				
				String value="";
				iform.clearCombo("Q_RB_iRBL_TXNTABLE_DROPDOWN_FIELD3");
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					value=arr1.get(0);
					iform.addItemInCombo("Q_RB_iRBL_TXNTABLE_DROPDOWN_FIELD3",value,value);
					strReturn="IndustryMicro Loaded";
				}
				
			} catch (Exception e) {
				iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in Micro drop down load " + e.getMessage());
			}
		}
        else if("FircoStatus".equals(controlName))
        {
        	try {
        		List lstDecisions = iform
        			.getDataFromDB("select count(*) from WFINSTRUMENTTABLE with(nolock) where ProcessInstanceID='"+getWorkitemName(iform)+"' and VAR_STR5='Firco_Clearance' and ActivityName!='Compliance_EDD'");
        		
        		String value="";
        		for(int i=0;i<lstDecisions.size();i++)
        		{
        			List<String> arr1=(List)lstDecisions.get(i);
        			value=arr1.get(0);
        			strReturn=value;
        		}
        		
        	} catch (Exception e) {
        		iRBL.mLogger.debug("WINAME : "+getWorkitemName(iform)+", WSNAME: "+iform.getActivityName()+", Exception in firco status check " + e.getMessage());
        	}
        }

		
        
		return strReturn;
	}
	
	
}
