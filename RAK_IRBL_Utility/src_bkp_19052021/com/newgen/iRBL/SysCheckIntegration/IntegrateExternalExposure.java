package com.newgen.iRBL.SysCheckIntegration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class IntegrateExternalExposure
{
	private String rowVal;
	private static String CheckGridTable="USR_0_IRBL_CHECKS_GRID_DTLS";
	static ResponseBean objRespBean = new ResponseBean();
	
	public static ResponseBean IntegratewithMW(int integrationWaitTime,	int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception {

		String DBQuery = "SELECT ROWUNIQUEID ,NAME, CIFID, CONDUCTED_ON, DATE_MODIFIED_ON," +
				" CONDUCTED_BY, CBRB_STATUS, SVS_STATUS, EXTEXPOSURE_STATUS," +
				" FROM "+CheckGridTable+" WITH (NOLOCK) " +
				"WHERE WI_NAME = '"+objRespBean.getWorkitemNumber()+"'";

		String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataIPXML: "+ extTabDataIPXML);
		String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataOPXML: "+ extTabDataOPXML);

		XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
		int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
		
		if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
		{
			String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
			xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
			
			//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
			NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

			HashMap<String, String> ExtTabDataMap = new HashMap<String, String>();
			HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

			iRBLIntegration objiRBLIntegration= new iRBLIntegration();
										
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
			{		
				CheckGridDataMap.put("ROWUNIQUEID",objWorkList.getVal("ROWUNIQUEID"));
				CheckGridDataMap.put("NAME", objWorkList.getVal("NAME"));
				CheckGridDataMap.put("CIFID", objWorkList.getVal("CIFID"));
				CheckGridDataMap.put("CONDUCTED_ON", objWorkList.getVal("CONDUCTED_ON"));
				CheckGridDataMap.put("DATE_MODIFIED_ON", objWorkList.getVal("DATE_MODIFIED_ON"));
				CheckGridDataMap.put("CONDUCTED_BY", objWorkList.getVal("CONDUCTED_BY"));
				CheckGridDataMap.put("CBRB_STATUS", objWorkList.getVal("CBRB_STATUS"));
				CheckGridDataMap.put("SVS_STATUS", objWorkList.getVal("SVS_STATUS"));				
				CheckGridDataMap.put("EXTEXPOSURE_STATUS", objWorkList.getVal("EXTEXPOSURE_STATUS"));
				
				for(Map.Entry<String, String> map : CheckGridDataMap.entrySet())
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CheckGridDataMap map key: " +map.getKey()+" map value :"+map.getValue());
				}							
				
				
				if(objWorkList.getVal("EXTEXPOSURE_STATUS").equalsIgnoreCase(""))
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside function for EXTEXPOSURE_STATUS check call");
					
					ExtTabDataMap.put("WINAME", objWorkList.getVal("winame"));
					ExtTabDataMap.put("CIF", objWorkList.getVal("CIFID"));
					
					//String integrationStatus=objiRBLIntegration.ExtExposureCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
							//CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, CheckGridDataMap);

					/*String[] splitintstatus =integrationStatus.split("~");

					String ErrDesc = "MessageId: "+splitintstatus[2] + ", Return Code: "+splitintstatus[0] +", Return Desc: "+ splitintstatus[1];
					String attributesTag;

					if (splitintstatus[0].equals("0000"))
					{
						EXTEXPOSURE_STATUS = "Success";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("EXTEXPOSURE_STATUS : " +EXTEXPOSURE_STATUS);
					}
					else
					{
						EXTEXPOSURE_STATUS = "Failure";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("EXTEXPOSURE_STATUS : " +EXTEXPOSURE_STATUS);
					}*/
				}
				
				
				
				/*if(objWorkList.getVal("FINANCIALSUMMARY_STATUS").equalsIgnoreCase("") && !objWorkList.getVal("CIF_ID").equalsIgnoreCase(""))
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+winame+", WSNAME: "+ws_name+", inside function for FINANCIALSUMMARY_STATUS check call");
					
					ExtTabDataMap.put("WINAME", objWorkList.getVal("winame"));
					ExtTabDataMap.put("CIF", objWorkList.getVal("CIFID"));
					
					String integrationStatus=objiRBLIntegration.FinancialSummaryCall(cabinetName,UserName,sessionId, sJtsIp,
							iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap);

					String[] splitintstatus =integrationStatus.split("~");

					String ErrDesc = "MessageId: "+splitintstatus[2] + ", Return Code: "+splitintstatus[0] +", Return Desc: "+ splitintstatus[1];
					String attributesTag;

					if (splitintstatus[0].equals("0000"))
					{
						FINANCIALSUMMARY_STATUS = "Success";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("FINANCIALSUMMARY_STATUS : " +FINANCIALSUMMARY_STATUS);
					}
					else
					{
						FINANCIALSUMMARY_STATUS = "Failure";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("FINANCIALSUMMARY_STATUS : " +FINANCIALSUMMARY_STATUS);
					}
				}*/		
				
				//updating Main Integration Grid transaction table
			    String MainGridColNames="";
			    String MainGridColValues="";
			    for(Map.Entry<String,String> map : CheckGridDataMap.entrySet())
				{
					MainGridColNames+=map.getKey();
					MainGridColValues+=map.getValue();
				}
			    String sWhere="ROWUNIQUEID="+CheckGridDataMap.get("ROWUNIQUEID");
			    String status= iRBLIntegration.UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,CheckGridTable,sWhere);
			    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("UpdateGridTableMWResponse CheckGridTable status : " +status);
			}
			objRespBean.setDedupeReturnCode("0000");
		}
		else
		{
			objRespBean.setWorkItemMainCode("");
			objRespBean.setDedupeReturnCode("");			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WmgetWorkItem failed: "+objRespBean.getWorkItemMainCode());
		}
		return objRespBean;
	}

	public static String parseExternalExposure(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String prod, String subprod, String cifId, String parentWiName,String cust_type)
	{
		String flag1="";
		try{
			String ReturnCode="";

			if(parseXml.indexOf("<ReturnCode>")>-1)
			{
				ReturnCode= parseXml.substring(parseXml.indexOf("<ReturnCode>")+12,parseXml.indexOf("</ReturnCode>"));
				//WriteLog("$$return Code "+ReturnCode);
			}

			//Commented for PCSP-526
			/* if(ReturnCode.equalsIgnoreCase("B003"))
			{
				//WriteLog("AECB:No record found!!");
				return "B003";
			} */
			//WriteLog("wrapperIP jsp: parseExternalExposure: "+wrapperIP);
			//WriteLog("wrapperPort jsp: parseExternalExposure: "+wrapperPort);
			//WriteLog("sessionId jsp: parseExternalExposure: "+sessionId);
			//WriteLog("cabinetName jsp: parseExternalExposure: "+cabinetName);
			//WriteLog("wi_name jsp: parseExternalExposure: "+wi_name);
			//WriteLog("appServerType jsp: parseExternalExposure: "+appServerType);
			//WriteLog("parseXml jsp: parseExternalExposure: "+parseXml);
			//WriteLog("returnType jsp: parseExternalExposure: "+returnType);
			//WriteLog("cifId jsp: parseExternalExposure: "+cifId);
			//WriteLog("parentWiName jsp: parseExternalExposure: "+parentWiName);

			String tagName="";
			String subTagName="";
			String sTableName="";

			String subtag_single="";

			tagName="ChequeDetails"; 
			subTagName = "";
			sTableName="USR_0_IRBL_EXTERNALEXPOSURE_ChequeDetails";
			flag1 = IntegrateInternalExposureCollectionsSummary.commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
			//WriteLog("cifId jsp: parseExternalExposure: updated or inserted"+flag1);

			if(flag1.equalsIgnoreCase("true")){
				tagName="LoanDetails"; 
				subTagName = "KeyDt,AmountDtls";
				sTableName="USR_0_IRBL_EXTERNALEXPOSURE_LoanDetails";
				flag1 = IntegrateInternalExposureCollectionsSummary.commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
				//WriteLog("cifId jsp: parseExternalExposure: updated or inserted1"+flag1);

				if(flag1.equalsIgnoreCase("true")){
					tagName="CardDetails"; 
					subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
					sTableName="USR_0_IRBL_EXTERNALEXPOSURE_CardDetails";
					flag1 = IntegrateInternalExposureCollectionsSummary.commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
					//WriteLog("cifId jsp: parseExternalExposure: updated or inserted2"+flag1);
					if(flag1.equalsIgnoreCase("true")){
						tagName="Derived"; 
						subTagName = "";
						sTableName="NG_rlos_custexpose_Derived";
						flag1 = IntegrateInternalExposureCollectionsSummary.commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);	
						//WriteLog("cifId jsp: parseExternalExposure: updated or NG_rlos_custexpose_Derived"+flag1);
						if(flag1.equalsIgnoreCase("true")){
							tagName="RecordDestribution";
							subTagName = "";
							sTableName="NG_RLOS_CUSTEXPOSE_RecordDestribution";
							flag1=IntegrateInternalExposureCollectionsSummary.commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
							if(flag1.equalsIgnoreCase("true")){
								tagName="AcctDetails";
								subTagName = "KeyDt,AmountDtls";
								sTableName="USR_0_IRBL_EXTERNALEXPOSURE_AccountDetails";
								flag1=IntegrateInternalExposureCollectionsSummary.commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
								if(flag1.equalsIgnoreCase("true")){
									tagName="ServicesDetails";
									subTagName = "KeyDt,AmountDtls";
									sTableName="USR_0_IRBL_EXTERNALEXPOSURE_ServicesDetails";
									flag1=IntegrateInternalExposureCollectionsSummary.commonParseProduct(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
									}
								else{
									flag1="false";
									}
							}
							else{
								flag1="false";
							}
						}
						else{
							flag1="false";
						}
					}
					else{
						flag1 ="false";
						//WriteLog("cifId jsp: parseExternalExposure: updated or NG_rlos_custexpose_Derived"+flag1);
					}
				}
				else{
					flag1 ="false";
					//WriteLog("cifId jsp: parseExternalExposure: updated or insertedfalse"+flag1);
				}


			}
			else{
				flag1 ="false";
				//WriteLog("cifId jsp: parseExternalExposure: updated or insertedfalse1"+flag1);
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}

		//WriteLog("cifId jsp: parseExternalExposure: updated or inserted final value"+flag1);
		return flag1;
	}

}
