package com.newgen.iRBL.SysCheckIntegration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class IntegrateDedupeBlacklistFicroRiskscore {
	
	private static String CheckGridTable="USR_0_IRBL_CHECKS_GRID_DTLS";
	private static String RelPartyGridTable="USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS";
	private static String iRBL_EXTTABLE="RB_iRBL_EXTTABLE";
	
	static ResponseBean objRespBean=new ResponseBean();

	public static HashMap<String,String> ConductHistoryGridData(HashMap CheckGridDataMap)
	{
		HashMap<String, String> RelatedPartyDataMap = new HashMap<String, String>();
		try
		{
			String DBQuery = "SELECT CIF, RELATEDPARTYID, RELMOBILENUMBERCOUNTRYCODE, RELMOBILENUMBER," +
					" FIRSTNAME, MIDDLENAME, LASTNAME, DATEOFBIRTH, NATIONALITY, VISANUMBER," +
					" PASSPORTNUMBER, EMIRATESID, GENDER, COUNTRY, RELATIONSHIPTYPE" +
					" FROM "+RelPartyGridTable+" WHERE CIF='"+CheckGridDataMap.get("CIFID")+"' AND " +
					"RELATEDPARTYID='"+CheckGridDataMap.get("RELATEDPARTYID")+"' AND " +
					"WI_NAME = '"+objRespBean.getWorkitemNumber()+"'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RelPartyGridTable IPXML: "+ extTabDataIPXML);
			String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RelPartyGridTable OPXML: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
			{
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");										
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{	
					RelatedPartyDataMap.put("CIF",objWorkList.getVal("CIF"));
					RelatedPartyDataMap.put("RELATEDPARTYID",objWorkList.getVal("RELATEDPARTYID"));
					RelatedPartyDataMap.put("FIRSTNAME",objWorkList.getVal("FIRSTNAME"));
					RelatedPartyDataMap.put("MIDDLENAME",objWorkList.getVal("MIDDLENAME"));
					RelatedPartyDataMap.put("LASTNAME",objWorkList.getVal("LASTNAME"));
					RelatedPartyDataMap.put("DATEOFBIRTH",objWorkList.getVal("DATEOFBIRTH"));
					RelatedPartyDataMap.put("NATIONALITY",objWorkList.getVal("NATIONALITY"));
					RelatedPartyDataMap.put("VISANUMBER",objWorkList.getVal("VISANUMBER"));
					RelatedPartyDataMap.put("PASSPORTNUMBER",objWorkList.getVal("PASSPORTNUMBER"));
					RelatedPartyDataMap.put("EMIRATESID",objWorkList.getVal("EMIRATESID"));
					RelatedPartyDataMap.put("GENDER",objWorkList.getVal("GENDER"));
					RelatedPartyDataMap.put("COUNTRY",objWorkList.getVal("COUNTRY"));
					RelatedPartyDataMap.put("RELATIONSHIPTYPE",objWorkList.getVal("RELATIONSHIPTYPE"));
					RelatedPartyDataMap.put("RELMOBILENUMBERCOUNTRYCODE",objWorkList.getVal("RELMOBILENUMBERCOUNTRYCODE"));
					RelatedPartyDataMap.put("RELMOBILENUMBER",objWorkList.getVal("RELMOBILENUMBER"));
				}
			}
		}
		catch(Exception e)
		{
			
		}
		
		return RelatedPartyDataMap;
	}
	
	public static HashMap<String,String> WorkitemDataExt(String WorkitemName,String iRBL_EXTTABLE)
	{
		HashMap<String, String> WIDataMap = new HashMap<String, String>();
		try
		{
			String DBQuery = "SELECT CIF_NUMBER, ADDRESS_OR_EID, APPLICANT_FULL_NAME, PRODUCTTYPE," +
					" ENTRY_NATIONALITY, INDUSTRY_CODE, PEP_STATUS, MOBILENUMBERCOUNTRYCODE," +
					" MOBILENUMBER, RISK_SCORE_STATUSFROMUTIL, COUNTRYOFINCORPORATION,"+
					"format(DATEOFINCORPORATION,'yyyy-MM-dd') as DATEOFINCORPORATION,COMPANY_NAME,TL_NUMBER" +
					" FROM "+iRBL_EXTTABLE+" with(nolock) WHERE WINAME = '"+WorkitemName+"'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("iRBL_EXTTABLE IPXML: "+ extTabDataIPXML);
			String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("iRBL_EXTTABLE OPXML: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
			{
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");										
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{	
					WIDataMap.put("CIF_NUMBER",objWorkList.getVal("CIF_NUMBER"));
					WIDataMap.put("ADDRESS_OR_EID",objWorkList.getVal("ADDRESS_OR_EID"));
					WIDataMap.put("APPLICANT_FULL_NAME",objWorkList.getVal("APPLICANT_FULL_NAME"));
					WIDataMap.put("MOBILENUMBERCOUNTRYCODE",objWorkList.getVal("MOBILENUMBERCOUNTRYCODE"));
					WIDataMap.put("MOBILENUMBER",objWorkList.getVal("MOBILENUMBER"));
					WIDataMap.put("PRODUCTTYPE",objWorkList.getVal("PRODUCTTYPE"));
					WIDataMap.put("ENTRY_NATIONALITY",objWorkList.getVal("ENTRY_NATIONALITY"));
					WIDataMap.put("INDUSTRY_CODE",objWorkList.getVal("INDUSTRY_CODE"));
					WIDataMap.put("PEP_STATUS",objWorkList.getVal("PEP_STATUS"));
 					WIDataMap.put("RISK_SCORE_STATUSFROMUTIL",objWorkList.getVal("RISK_SCORE_STATUSFROMUTIL"));
					WIDataMap.put("COUNTRYOFINCORPORATION",objWorkList.getVal("COUNTRYOFINCORPORATION"));
					WIDataMap.put("DATEOFINCORPORATION",objWorkList.getVal("DATEOFINCORPORATION"));
					WIDataMap.put("COMPANY_NAME",objWorkList.getVal("COMPANY_NAME"));
					WIDataMap.put("TL_NUMBER",objWorkList.getVal("TL_NUMBER"));
				}
			}
		}
		catch(Exception e)
		{
			
		}
		return WIDataMap;
	}
	
	public static ResponseBean IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception 
	{
		try{
			HashMap<String, String> ExtTabDataMap = new HashMap<String, String>();
			boolean MainCIF_Flag = true;
			boolean DedupeFlag = false;
			boolean BlacklistFlag = false;
			boolean FircoFlag = false;
			boolean FircoHit = false;
			
			String integrationStatus = "";
			
			objRespBean.setWorkitemNumber(processInstanceID);
			
			ExtTabDataMap=WorkitemDataExt(processInstanceID,iRBL_EXTTABLE);
			
			String DBQuery = "SELECT RELATEDPARTYID ,NAME, CIFID, TYPEOFCIF, KYCVALIDTILL, CONDUCTED_ON, DATE_MODIFIED_ON," +
					" CONDUCTED_BY, DEDUPE_STATUS, BLACKLIST_STATUS, FIRCO_STATUS" +
					" FROM "+CheckGridTable+" WITH (NOLOCK) " +
					"WHERE WI_NAME = '"+processInstanceID+"'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CheckGridTable IPXML: "+ extTabDataIPXML);
			String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CheckGridTable OPXML: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
			{
				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

				HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
				HashMap<String, String> RelPartyDataMap = new HashMap<String, String>();			
				
											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					CheckGridDataMap.put("RELATEDPARTYID",objWorkList.getVal("RELATEDPARTYID"));
					CheckGridDataMap.put("NAME", objWorkList.getVal("NAME"));
					CheckGridDataMap.put("CIFID", objWorkList.getVal("CIFID"));
					CheckGridDataMap.put("TYPEOFCIF", objWorkList.getVal("TYPEOFCIF"));
					CheckGridDataMap.put("KYCVALIDTILL", objWorkList.getVal("KYCVALIDTILL"));
					CheckGridDataMap.put("CONDUCTED_ON", objWorkList.getVal("CONDUCTED_ON"));
					CheckGridDataMap.put("DATE_MODIFIED_ON", objWorkList.getVal("DATE_MODIFIED_ON"));
					CheckGridDataMap.put("CONDUCTED_BY", objWorkList.getVal("CONDUCTED_BY"));
					CheckGridDataMap.put("DEDUPE_STATUS", objWorkList.getVal("DEDUPE_STATUS"));
					CheckGridDataMap.put("BLACKLIST_STATUS", objWorkList.getVal("BLACKLIST_STATUS"));
					CheckGridDataMap.put("FIRCO_STATUS", objWorkList.getVal("FIRCO_STATUS"));
					
					if(CheckGridDataMap.get("TYPEOFCIF").trim().equalsIgnoreCase("CompanyCIF"))
						MainCIF_Flag = true;
					else
						MainCIF_Flag = false;
					
					boolean IntegrationFlag = false;
					
					if(!CheckGridDataMap.get("DEDUPE_STATUS").equalsIgnoreCase("Success") && !CheckGridDataMap.get("CIFID").equalsIgnoreCase(""))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside onclick function for Dedupe check call , MainCIF_Flag :"+MainCIF_Flag+" , CIFID : "+CheckGridDataMap.get("CIFID"));
						if(!MainCIF_Flag)
						{
							RelPartyDataMap=ConductHistoryGridData(CheckGridDataMap);
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RelPartyDataMap :"+RelPartyDataMap);
						}
						integrationStatus=iRBLIntegration.DedupeCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, RelPartyDataMap, MainCIF_Flag);
						
						CheckGridDataMap.put("DEDUPE_STATUS", integrationStatus);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("DedupeCall integrationStatus: " +integrationStatus);
						
						//setting Status of integration in Bean
					    if(integrationStatus.equalsIgnoreCase("Failure") && !DedupeFlag)
					    {
							DedupeFlag=true;
							objRespBean.setDedupeReturnCode(integrationStatus);
						}
						else if(!DedupeFlag)
							objRespBean.setDedupeReturnCode(integrationStatus);
					    
					    IntegrationFlag = true;
					}
					else if(CheckGridDataMap.get("DEDUPE_STATUS").equalsIgnoreCase("Success"))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("DedupeCall Status is success already and Integration call is not performed for :"+CheckGridDataMap.get("CIFID"));
						objRespBean.setDedupeReturnCode("Success");
					}
					
					if(!CheckGridDataMap.get("BLACKLIST_STATUS").equalsIgnoreCase("Success") && !CheckGridDataMap.get("CIFID").equalsIgnoreCase(""))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside onclick function for Blacklist check call , MainCIF_Flag :"+MainCIF_Flag+" , CIFID : "+CheckGridDataMap.get("CIFID"));
						if(!MainCIF_Flag)
						{
							RelPartyDataMap=ConductHistoryGridData(CheckGridDataMap);
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RelPartyDataMap :"+RelPartyDataMap);
						}						
						integrationStatus=iRBLIntegration.BlacklistCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, RelPartyDataMap, MainCIF_Flag);
						
						CheckGridDataMap.put("BLACKLIST_STATUS", integrationStatus);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("BlacklistCall integrationStatus: " +integrationStatus);

						
						if(integrationStatus.equalsIgnoreCase("Failure") && !BlacklistFlag)
						{
							BlacklistFlag=true;
							objRespBean.setBlackListReturnCode(integrationStatus);
						}
						else if(!BlacklistFlag)
							objRespBean.setBlackListReturnCode(integrationStatus);
						
						IntegrationFlag = true;
					}
					else if(CheckGridDataMap.get("BLACKLIST_STATUS").equalsIgnoreCase("Success"))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("BlacklistCall Status is success already and Integration call is not performed for :"+CheckGridDataMap.get("CIFID"));
						objRespBean.setBlackListReturnCode("Success");
					}
					
					if(!CheckGridDataMap.get("FIRCO_STATUS").equalsIgnoreCase("Record Found")&& !CheckGridDataMap.get("CIFID").equalsIgnoreCase(""))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside function for FIRCO_STATUS check call , MainCIF_Flag :"+MainCIF_Flag+" , CIFID : "+CheckGridDataMap.get("CIFID"));
						if(!MainCIF_Flag)
						{
							RelPartyDataMap=ConductHistoryGridData(CheckGridDataMap);
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RelPartyDataMap :"+RelPartyDataMap);
						}						
						integrationStatus= iRBLIntegration.FircosoftCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, RelPartyDataMap, MainCIF_Flag);
												
						CheckGridDataMap.put("FIRCO_STATUS", integrationStatus);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("FircosoftCall integrationStatus: " +integrationStatus);
						
						if(integrationStatus.equalsIgnoreCase("Not Checked") && !FircoFlag)
						{
							FircoFlag=true;
							objRespBean.setFircosoftReturnCode(integrationStatus);
						}
						else if(!FircoFlag)
						{
							objRespBean.setFircosoftReturnCode(integrationStatus);
						}
						
						//FircoHit
						if(integrationStatus.equalsIgnoreCase("Record Found") && !FircoHit)
						{
							FircoHit=true;
						}
						
						IntegrationFlag = true;
					}
					else if(CheckGridDataMap.get("FIRCO_STATUS").equalsIgnoreCase("Record Found"))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("FircosoftCall Status is success already and Integration call is not performed for :"+CheckGridDataMap.get("CIFID"));
						objRespBean.setFircosoftReturnCode("Success");
					}
					
					if(!ExtTabDataMap.get("RISK_SCORE_STATUSFROMUTIL").equalsIgnoreCase("Success")&& !CheckGridDataMap.get("CIFID").equalsIgnoreCase("") && MainCIF_Flag)
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside function for RISKSCORE check call, MainCIF_Flag :"+MainCIF_Flag+" , CIFID : "+CheckGridDataMap.get("CIFID"));
						
						integrationStatus=iRBLIntegration.RiskScoreCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, CheckGridDataMap);

						ExtTabDataMap.put("RISK_SCORE_STATUSFROMUTIL", integrationStatus);
						objRespBean.setRISK_SCORE_STATUSFROMUTIL(integrationStatus);
						
						objRespBean.setRiskScoreReturnCode(integrationStatus);						
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RiskScoreCall integrationStatus: " +integrationStatus);
					}
					else if(MainCIF_Flag && ExtTabDataMap.get("RISK_SCORE_STATUSFROMUTIL").equalsIgnoreCase("Success"))
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RiskScoreCall Status is success already and Integration call is not performed for :"+processInstanceID);
						objRespBean.setRiskScoreReturnCode("Success");
					}
														
					//updating Main Integration Grid transaction table
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Before update checkgrid : ");				
					
					if(IntegrationFlag)
					{
						//Columns need to updated in CheckGrid table
						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
						Date date = new Date();  
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("current system date"+formatter.format(date));  
						String current_dateTime =formatter.format(date);
						String[] current_date=current_dateTime.split(" ");
						CheckGridDataMap.put("CONDUCTED_ON", current_date[0]);
						CheckGridDataMap.put("DATE_MODIFIED_ON", current_date[0]);
						CheckGridDataMap.put("CONDUCTED_BY", "");
						
						String sWhere="";
					    String MainGridColNames="";
					    String MainGridColValues="";
					    for(Map.Entry<String,String> map : CheckGridDataMap.entrySet())
						{					    
					    	if(MainGridColNames.equals("") && !map.getValue().toString().equals(""))
					    	{
					    		MainGridColNames= map.getKey();
								MainGridColValues=map.getValue();
					    	}
					    	else if(!MainGridColNames.equals("") && !map.getValue().toString().equals(""))
					    	{
					    		MainGridColNames= MainGridColNames+","+map.getKey();
								MainGridColValues=MainGridColValues+","+map.getValue();
					    	}							
						}
					    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainGridColNames : " +MainGridColNames+" MainGridColValues :"+MainGridColValues);
					    MainGridColValues=MainGridColValues.replaceAll(",", "','");
					    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Aftr replace MainGridColValues :"+MainGridColValues);
					    
					    if(MainCIF_Flag)
					    	sWhere="WI_NAME='"+objRespBean.getWorkitemNumber()+"' AND CIFID='"+CheckGridDataMap.get("CIFID")+"'";
					    else
					    	sWhere="WI_NAME='"+objRespBean.getWorkitemNumber()+"' AND CIFID='"+CheckGridDataMap.get("CIFID")+"' AND RELATEDPARTYID='"+CheckGridDataMap.get("RELATEDPARTYID")+"'";
					    
					    String status= iRBLIntegration.UpdateGridTableMWResponse(MainGridColNames,"'"+MainGridColValues+"'",CheckGridTable,sWhere);
					    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("UpdateGridTableMWResponse CheckGridTable status : " +status);
					}
					
							    					
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getDedupeReturnCode for WI : "+objRespBean.getDedupeReturnCode());
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getBlackListReturnCode for WI : "+objRespBean.getBlackListReturnCode());
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getFircosoftReturnCode for WI : "+objRespBean.getFircosoftReturnCode());
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getRiskScoreReturnCode for WI : "+objRespBean.getRiskScoreReturnCode());
				}

				if(FircoHit)
					objRespBean.setFircoHit("Y");
				else
					objRespBean.setFircoHit("N");
			}
			else
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("No record in checksgrid table for WI : "+objRespBean.getWorkitemNumber());
		}
		catch(Exception e)
		{
			System.out.println("Exception occured in IntegrateMW Fn for WI :"+objRespBean.getWorkitemNumber());
			objRespBean.setWorkItemMainCode("");
			objRespBean.setDedupeReturnCode("");
			objRespBean.setBlackListReturnCode("");
			objRespBean.setFircosoftReturnCode("");
			objRespBean.setRiskScoreReturnCode("");
		}
		return objRespBean;
	}
}
