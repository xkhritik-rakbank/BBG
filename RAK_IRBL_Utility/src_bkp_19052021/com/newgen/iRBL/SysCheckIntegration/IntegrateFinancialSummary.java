package com.newgen.iRBL.SysCheckIntegration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class IntegrateFinancialSummary 
{
	private String rowVal;
	private static String CheckGridTable="USR_0_IRBL_CHECKS_GRID_DTLS";
	static ResponseBean objRespBean = new ResponseBean();
	
	public static ResponseBean IntegratewithMW(int integrationWaitTime,	int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception 
	{		
		String DBQuery = "SELECT ROWUNIQUEID ,NAME, CIFID, CONDUCTED_ON, DATE_MODIFIED_ON," +
				" CONDUCTED_BY, FINANCIALSUMMARY_STATUS," +
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
				CheckGridDataMap.put("FINANCIALSUMMARY_STATUS", objWorkList.getVal("FINANCIALSUMMARY_STATUS"));
				
				for(Map.Entry<String, String> map : CheckGridDataMap.entrySet())
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CheckGridDataMap map key: " +map.getKey()+" map value :"+map.getValue());
				}				
				
				if(objWorkList.getVal("FINANCIALSUMMARY_STATUS").equalsIgnoreCase("") && !objWorkList.getVal("CIF_ID").equalsIgnoreCase(""))
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside function for FINANCIALSUMMARY_STATUS check call");
					
					ExtTabDataMap.put("WINAME", objWorkList.getVal("winame"));
					ExtTabDataMap.put("CIF", objWorkList.getVal("CIFID"));
					
					//String integrationStatus=objiRBLIntegration.FinancialSummaryCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
							//CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, CheckGridDataMap);

					/*String[] splitintstatus =integrationStatus.split("~");

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
					}*/
				}
				
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
	
	public static String parseTRANSUM(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String cifId, String parentWiName)
	{
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseTRANSUM: "+wrapperIP);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseTRANSUM: "+wrapperPort);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseTRANSUM: "+sessionId);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseTRANSUM: "+cabinetName);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseTRANSUM: "+wi_name);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseTRANSUM: "+appServerType);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseTRANSUM: "+parseXml);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseTRANSUM: "+returnType);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseTRANSUM: "+cifId);

		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		String flag1="";
		//Deepak code commented method changed with new subtag_single param 23jan2018
		String subtag_single="";
		try{
			tagName= "TxnSummaryDtls";		
			subTagName= "";
			sTableName="USR_0_IRBL_FinancialSummary_TxnSummary";
			flag1=commonParseFinance(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,parentWiName,subtag_single);
			if(flag1.equalsIgnoreCase("true")){
				flag1="true";
			}
			else{
				flag1="false";
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}
		return flag1;
	}

	public static String parseAVGBALDET(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String cifId, String parentWiName)
	{
		//WriteLog("wrapperIP jsp: parseAVGBALDET: "+wrapperIP);
		//WriteLog("wrapperPort jsp: parseAVGBALDET: "+wrapperPort);
		//WriteLog("sessionId jsp: parseAVGBALDET: "+sessionId);
		//WriteLog("cabinetName jsp: parseAVGBALDET: "+cabinetName);
		//WriteLog("wi_name jsp: parseAVGBALDET: "+wi_name);
		//WriteLog("appServerType jsp: parseAVGBALDET: "+appServerType);
		//WriteLog("parseXml jsp: parseAVGBALDET: "+parseXml);
		//WriteLog("returnType jsp: parseAVGBALDET: "+returnType);
		//WriteLog("cifId jsp: parseAVGBALDET: "+cifId);

		String flag1="";
		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		//Deepak code commented method changed with new subtag_single param 23jan2018
		String subtag_single="";

		try{
			tagName= "FinancialSummaryRes";		
			subTagName= "AvgBalanceDtls";
			sTableName="ng_rlos_FinancialSummary_AvgBalanceDtls";
			flag1=commonParseFinance(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,parentWiName,subtag_single);
			if(flag1.equalsIgnoreCase("true")){
				flag1="true";
			}
			else{
				flag1="false";
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}
		return flag1;
	}
	
	public static String parseRETURNDET(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String cifId, String parentWiName)
	{
		//WriteLog("wrapperIP jsp: parseRETURNDET: "+wrapperIP);
		//WriteLog("wrapperPort jsp: parseRETURNDET: "+wrapperPort);
		//WriteLog("sessionId jsp: parseRETURNDET: "+sessionId);
		//WriteLog("cabinetName jsp: parseRETURNDET: "+cabinetName);
		//WriteLog("wi_name jsp: parseRETURNDET: "+wi_name);
		//WriteLog("appServerType jsp: parseRETURNDET: "+appServerType);
		//WriteLog("parseXml jsp: parseRETURNDET: "+parseXml);
		//WriteLog("returnType jsp: parseRETURNDET: "+returnType);
		//WriteLog("cifId jsp: parseRETURNDET: "+cifId);

		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		String flag1="";
		//Deepak code commented method changed with new subtag_single param 23jan2018
		String subtag_single="";
		try{
			tagName= "ReturnsDtls";		
			subTagName= "";
			sTableName="ng_rlos_FinancialSummary_ReturnsDtls";
			flag1=commonParseFinance(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,parentWiName,subtag_single);
			if(flag1.equalsIgnoreCase("true")){
				flag1="true";
			}
			else{
				flag1="false";
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}
		return flag1;

	}
	
	public static String parseLIENDET(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String cifId, String parentWiName)
	{
		//WriteLog("wrapperIP jsp: parseLIENDET: "+wrapperIP);
		//WriteLog("wrapperPort jsp: parseLIENDET: "+wrapperPort);
		//WriteLog("sessionId jsp: parseLIENDET: "+sessionId);
		//WriteLog("cabinetName jsp: parseLIENDET: "+cabinetName);
		//WriteLog("wi_name jsp: parseLIENDET: "+wi_name);
		//WriteLog("appServerType jsp: parseLIENDET: "+appServerType);
		//WriteLog("parseXml jsp: parseLIENDET: "+parseXml);
		//WriteLog("returnType jsp: parseLIENDET: "+returnType);
		//WriteLog("cifId jsp: parseLIENDET: "+cifId);

		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		String flag1="";
		//Deepak code commented method changed with new subtag_single param 23jan2018
		String subtag_single="";
		tagName= "LienDetails";		
		subTagName= "";
		sTableName="ng_rlos_FinancialSummary_LienDetails";
		try{
			flag1=commonParseFinance(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,parentWiName,subtag_single);
			if(flag1.equalsIgnoreCase("true")){
				flag1="true";
			}
			else{
				flag1="false";
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}
		return flag1;
	}
	
	public static String parseSIDET(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String cifId, String parentWiName)
	{
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside parseSIDET: ");
		

		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		String flag1="";
		//Deepak code commented method changed with new subtag_single param 23jan2018
		String subtag_single="";

		tagName= "SIDetails";		
		subTagName= "";
		sTableName="ng_rlos_FinancialSummary_SiDtls";
		try{
			flag1=commonParseFinance(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,parentWiName,subtag_single);
			if(flag1.equalsIgnoreCase("true")){
				flag1="true";
			}
			else{
				flag1="false";
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}
		return flag1;

	}
	
	public static String parseSALDET(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String cifId, String parentWiName) throws IOException, Exception
	{
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseSALDET: "+wrapperIP);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseSALDET: "+wrapperPort);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseSALDET: "+sessionId);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseSALDET: "+cabinetName);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseSALDET: "+wi_name);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseSALDET: "+appServerType);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseSALDET: "+parseXml);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseSALDET: "+returnType);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseSALDET: "+cifId);

		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		String flag1="";
		//Deepak code commented method changed with new subtag_single param 23jan2018
		String subtag_single="";
		tagName= "SalDetails";		
		subTagName= "";
		sTableName="ng_rlos_FinancialSummary_SalTxnDetails";
		
		if(parseXml.indexOf("<AcctId>")>-1){
			String acc_no = parseXml.substring(parseXml.indexOf("<AcctId>")+"</AcctId>".length()-1,parseXml.indexOf("</AcctId>"));	
			String sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = '"+acc_no+"'";
			//String strInputXml =	ExecuteQuery_APdelete(sTableName,sWhere,cabinetName,sessionId);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml delete returndtls " + strInputXml);
			
			String strInputXml = CommonMethods.apDeleteInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), sTableName, sWhere);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apDeleteInput from "+sTableName+" Table "+strInputXml);

			String strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apDeleteInput Table "+strOutputXml);
			
		}
		try{
			flag1=commonParseFinance(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,parentWiName,subtag_single);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("return flag1 jsp: parseSALDET: "+flag1);
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}
		return flag1;
	}
	
	public static String commonParseFinance(String parseXml,String tagName,String wi_name,String returnType,String sTableName,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String appServerType, String subTagName, String parentWiName,String subtag_single)
	{
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("commonParseFinance jsp: inside: ");
		String retVal = "";
		String [] valueArr= null;
		String strInputXml="";
		String strOutputXml="";
		String columnName = "";
		String columnValues = "";
		String tagNameU = "";
		String subTagNameU = "";
		String subTagNameU_2 = "";
		String mainCode = "";
		String sWhere = "";
		String row_updated = "";
		String id="";
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tagName jsp: commonParseFinance: "+tagName);
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("subTagName jsp: commonParseFinance: "+subTagName);
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sTableName jsp: commonParseFinance: "+sTableName);
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sTableName jsp: commonParseFinance: "+parseXml);
		try{
			if((returnType.equalsIgnoreCase("RETURNDET")&& parseXml.contains("ReturnsDtls"))||(returnType.equalsIgnoreCase("AVGBALDET")&& parseXml.contains("AcctId"))||(returnType.equalsIgnoreCase("LIENDET")&& parseXml.contains("LienDetails"))||(returnType.equalsIgnoreCase("SIDET")&& parseXml.contains("SIDetails"))||(returnType.equalsIgnoreCase("TRANSUM")&& parseXml.contains("TxnSummary"))||(returnType.equalsIgnoreCase("SALDET")&& parseXml.contains("SalDetails")))
			{

				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: commonParseFinance: "+returnType);
				Map<String, String> tagValuesMap= new LinkedHashMap<String, String>();		 
				tagValuesMap=CommonMethods.getTagDataParent_deep(parseXml,tagName,subTagName,subtag_single);

				Map<String , String> map = tagValuesMap;
				String colValue="";


				for (Map.Entry<String, String> entry : map.entrySet())
				{
					valueArr=entry.getValue().split("~");
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values:1234 " +valueArr);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values: " + entry.getValue());

					colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
					if(returnType.equalsIgnoreCase("AVGBALDET")&& valueArr[0].contains("AcctId")){
						String columnName_arr[] = valueArr[0].split(",");
						String columnValues_arr[] = valueArr[1].split(",");
						id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
					}

					if(sTableName.equalsIgnoreCase("ng_rlos_FinancialSummary_AvgBalanceDtls")){
						columnName = valueArr[0]+",Wi_Name,Child_Wi";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"'";
						sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = "+id;
					}
					//modified by akshay on 6/2/18	
					else if(sTableName.equalsIgnoreCase("ng_rlos_FinancialSummary_ReturnsDtls")){
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_ReturnsDtls");
						String header_info = getTagDataParent_financ_header(parseXml,"FinancialSummaryRes","CIFID,AcctId,OperationType");
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_ReturnsDtls header info: "+ header_info);
						String [] header_info_arr = header_info.split(":");
						columnName = valueArr[0]+",Wi_Name,Child_Wi,"+header_info_arr[0];
						columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"',"+header_info_arr[1];
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside Return Details-->columnValues: "+columnValues);
						String columnName_arr[] = columnName.split(",");
						String columnValues_arr[] = columnValues.split(",");
						if(returnType.equalsIgnoreCase("RETURNDET") && valueArr[0].contains("ReturnNumber")){
							id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("ReturnNumber")];
							sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND ReturnNumber = "+id;
						}else{
							id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
							sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = "+id;
						}
					}
					else if(sTableName.equalsIgnoreCase("ng_rlos_FinancialSummary_LienDetails")){
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_LienDetails");
						String header_info = getTagDataParent_financ_header(parseXml,"FinancialSummaryRes","CIFID,AcctId,OperationType");
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_LienDetails header info: "+ header_info);
						String [] header_info_arr = header_info.split(":");
						columnName = valueArr[0]+",Wi_Name,Child_Wi,"+header_info_arr[0];
						columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"',"+header_info_arr[1];

						String columnName_arr[] = columnName.split(",");
						String columnValues_arr[] = columnValues.split(",");
						id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
						String leinId=columnValues_arr[Arrays.asList(columnName_arr).indexOf("LienId")];
						sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = "+id+" and LienId = "+leinId;
						//strInputXml =	ExecuteQuery_APdelete(sTableName,sWhere,cabinetName,sessionId);
						
						strInputXml = CommonMethods.apDeleteInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), sTableName, sWhere);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apDeleteInput from "+sTableName+" Table "+strInputXml);

						strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apDeleteInput Table "+strOutputXml);
						
					}
					else if(sTableName.equalsIgnoreCase("ng_rlos_FinancialSummary_TxnSummary")){
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_TxnSummary");
						String header_info = getTagDataParent_financ_header(parseXml,"FinancialSummaryRes","CIFID,AcctId,OperationType");
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_TxnSummary header info: "+ header_info);
						String [] header_info_arr = header_info.split(":");
						columnName = valueArr[0]+",Wi_Name,Child_Wi,"+header_info_arr[0];
						columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"',"+header_info_arr[1];
						String columnName_arr[] =columnName.split(",");
						String columnValues_arr[] = columnValues.split(",");
						id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
						String Month = columnValues_arr[Arrays.asList(columnName_arr).indexOf("Month")];
						sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = "+id+" and Month = "+Month+"";
						//strInputXml =	ExecuteQuery_APdelete(sTableName,sWhere,cabinetName,sessionId);
						
						strInputXml = CommonMethods.apDeleteInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), sTableName, sWhere);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apDeleteInput from "+sTableName+" Table "+strInputXml);

						strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apDeleteInput Table "+strOutputXml);
						
					}
					else if(sTableName.equalsIgnoreCase("ng_rlos_FinancialSummary_SalTxnDetails")){
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_SalTxnDetails");
						String header_info = getTagDataParent_financ_header(parseXml,"FinancialSummaryRes","CifId,AcctId,OperationType");
						String [] header_info_arr = header_info.split(":");
						columnName = valueArr[0]+",Wi_Name,Child_Wi,"+header_info_arr[0];
						columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"',"+header_info_arr[1];

						String columnName_arr[] = columnName.split(",");
						String columnValues_arr[] = columnValues.split(",");
						id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
						String SalCreditDate = columnValues_arr[Arrays.asList(columnName_arr).indexOf("SalCreditDate")];
						sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = "+id+" and 1=2 and SalCreditDate = "+SalCreditDate+"";

					}
					else if(sTableName.equalsIgnoreCase("ng_rlos_FinancialSummary_SiDtls")){
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside commonParseFinance: ng_rlos_FinancialSummary_SiDtls "); 
						try 
						{
							String header_info = getTagDataParent_financ_header(parseXml,"FinancialSummaryRes","CifId,AcctId,OperationType");
							String [] header_info_arr = header_info.split(":");
							columnName = valueArr[0]+",Wi_Name,Child_Wi,"+header_info_arr[0];
							columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"',"+header_info_arr[1];
							String columnName_arr[] = columnName.split(",");
							String columnValues_arr[] = columnValues.split(",");
							id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
							String SINumber = columnValues_arr[Arrays.asList(columnName_arr).indexOf("SINumber")];
							columnName = valueArr[0]+",Wi_Name,Child_Wi,"+header_info_arr[0];
							columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"',"+header_info_arr[1];
							String sWhere_delete="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = "+id;
							sWhere="Child_Wi='"+wi_name+"' AND OperationType='"+returnType+"' AND AcctId = "+id+" And SINumber="+SINumber;
						//strInputXml =	ExecuteQuery_APdelete(sTableName,sWhere_delete,cabinetName,sessionId);
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml delete ng_rlos_FinancialSummary_SiDtls " + strInputXml);
						/* try 
						{
							strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);

							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml delete returndtls: "+strOutputXml);
						} 
						catch (NGException e) 
						{
							e.printStackTrace();
							*/
						}
						catch (Exception ex) 
						{
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in ng_rlos_FinancialSummary_SiDtls: "+ex.getMessage());  
						}
					}
					else{
						columnName = valueArr[0]+",Wi_Name,Child_Wi,Request_Type";
						columnValues = colValue+",'"+parentWiName+"','"+wi_name+"','"+returnType+"'";  
					}


					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnName commonParse" + columnName);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnValues commonParse" + columnValues);

					//strInputXml =	ExecuteQuery_APUpdate(sTableName,columnName,columnValues,sWhere,cabinetName,sessionId);
					
					strInputXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), sTableName, columnName, columnValues, sWhere);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apUpdateInput from "+sTableName+" Table "+strInputXml);

					strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput Table "+strOutputXml);

					//changed by akshay on 2/5/18 for proc 8964
					tagNameU = "APUpdate_Output";
					subTagNameU = "MainCode";
					subTagNameU_2 = "Output";
					mainCode = CommonMethods.getTagDataValue(strOutputXml,tagNameU,subTagNameU);
					row_updated = CommonMethods.getTagDataValue(strOutputXml,tagNameU,subTagNameU_2);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+row_updated);
					if(!(mainCode.equalsIgnoreCase("0")) || row_updated.equalsIgnoreCase("0"))
					{
						//strInputXml =	ExecuteQuery_APInsert(sTableName,columnName,columnValues,cabinetName,sessionId);
						
						strInputXml = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnName, columnValues, sTableName);
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert from "+sTableName+" Table "+strInputXml);

						strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
												
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml" + strInputXml);
						tagNameU = "APInsert_Output";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml: "+strOutputXml);

						mainCode = CommonMethods.getTagDataValue(strOutputXml,"APInsert_Output",subTagNameU);
						
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "mainCode value is: " +mainCode );
						if(!mainCode.equalsIgnoreCase("0"))
						{
							retVal = "false";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: ApINsertfalse for financial summary: "+retVal);
						}
						else
						{
							retVal = "true";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: ApINserttrue for financial summary: "+retVal);
						}					
						
					}
					else
					{
						retVal = "true";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
					}
				}
			}
			else{
				retVal = "true";
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: commonParseFinance Empty tag : "+returnType+" Wi_Name: "+wi_name);
			}
		}catch(Exception e){
			System.out.println("Exception occured in commonParseFinance: "+ e.getMessage());
			e.printStackTrace();
			retVal = "false";
		}
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: final value for financial summary "+retVal);
		return retVal;
	}
	
	public static String getTagDataParent_financ_header(String parseXml,String tagName,String sub_tag){
		String col_name = "";
		String col_val ="";
		InputStream is = new ByteArrayInputStream(parseXml.getBytes());
		try {
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataParent_financ_header jsp: parseXml: "+parseXml);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataParent_financ_header jsp: tagName: "+tagName);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataParent_financ_header jsp: subTagName: "+sub_tag);

			//InputStream is = new FileInputStream(parseXml);
			
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataParent_financ_header jsp: strOutputXml: "+is);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			Document dBuilder = (Document) dbFactory.newDocumentBuilder();
			Document doc = ((DocumentBuilder) dBuilder).parse(is);
			doc.getDocumentElement().normalize();

			NodeList nList_loan = doc.getElementsByTagName(tagName);
			for(int i = 0 ; i<nList_loan.getLength();i++){

				NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
				String id = ch_nodeList.item(0).getTextContent();
				for(int ch_len = 0 ;ch_len< ch_nodeList.getLength(); ch_len++){
					if(sub_tag.toUpperCase().contains(ch_nodeList.item(ch_len).getNodeName().toUpperCase())){
						if(col_name.equalsIgnoreCase("")){
							col_name = ch_nodeList.item(ch_len).getNodeName();
							col_val = "'"+ch_nodeList.item(ch_len).getTextContent()+"'";
						}
						else{
							col_name = col_name+","+ch_nodeList.item(ch_len).getNodeName();
							col_val = col_val+",'"+ch_nodeList.item(ch_len).getTextContent()+"'";
						}
					}							
				}
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("insert/update getTagDataParent_financ_header for id: "+id);
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("insert/update getTagDataParent_financ_header cal_name: "+col_name);
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("insert/update getTagDataParent_financ_header col_val: "+col_val);

			}

		} catch (Exception e) {
			System.out.println("Exception occured in getTagDataParent_financ_header: "+e.getMessage());
			e.printStackTrace();
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in getTagDataParent_financ_header method:  "+e.getMessage());
		}
				finally
			{
				try{
			    		if(is!=null)
			    		{
			    		is.close();
			    		is=null;
			    		}
			    	}
			    	catch(Exception e){
			    		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in is close:  "+e.getMessage());
			    	}
			}
		return col_name+":"+col_val;
	}

}
