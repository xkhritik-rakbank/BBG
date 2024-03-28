package com.newgen.iRBL.SysCheckIntegration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class IntegrateInternalExposureCollectionsSummary {

	private String rowVal;
	private static String CheckGridTable="USR_0_IRBL_CHECKS_GRID_DTLS";
	static ResponseBean objRespBean = new ResponseBean();
	
	public static ResponseBean IntegratewithMW(int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception
	{
		String DBQuery = "SELECT ROWUNIQUEID ,NAME, CIFID, CONDUCTED_ON, DATE_MODIFIED_ON," +
				" CONDUCTED_BY, CBRB_STATUS, SVS_STATUS, INTEXPOSURE_STATUS, EXTEXPOSURE_STATUS," +
				" FINANCIALSUMMARY_STATUS, COLLECTIONSUMMARY_STATUS, DETECH_STATUS" +
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
			boolean MainCIF_Flag = true;
										
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
				CheckGridDataMap.put("INTEXPOSURE_STATUS", objWorkList.getVal("INTEXPOSURE_STATUS"));
				CheckGridDataMap.put("EXTEXPOSURE_STATUS", objWorkList.getVal("EXTEXPOSURE_STATUS"));
				CheckGridDataMap.put("FINANCIALSUMMARY_STATUS", objWorkList.getVal("FINANCIALSUMMARY_STATUS"));
				CheckGridDataMap.put("COLLECTIONSUMMARY_STATUS", objWorkList.getVal("COLLECTIONSUMMARY_STATUS"));
				
				for(Map.Entry<String, String> map : CheckGridDataMap.entrySet())
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CheckGridDataMap map key: " +map.getKey()+" map value :"+map.getValue());
				}			
				
				if(objWorkList.getVal("INTEXPOSURE_STATUS").equalsIgnoreCase("") && !objWorkList.getVal("CIF_ID").equalsIgnoreCase(""))
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside function for INTEXPOSURE_STATUS check call");
					
					ExtTabDataMap.put("WINAME", objWorkList.getVal("winame"));
					ExtTabDataMap.put("CIF", objWorkList.getVal("CIFID"));
					
					//String integrationStatus=objiRBLIntegration.IntExposureCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
							//CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, CheckGridDataMap);

					/*String[] splitintstatus =integrationStatus.split("~");

					String ErrDesc = "MessageId: "+splitintstatus[2] + ", Return Code: "+splitintstatus[0] +", Return Desc: "+ splitintstatus[1];
					String attributesTag;

					if (splitintstatus[0].equals("0000"))
					{
						INTEXPOSURE_STATUS = "Success";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("INTEXPOSURE_STATUS : " +INTEXPOSURE_STATUS);
					}
					else
					{
						INTEXPOSURE_STATUS = "Failure";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("INTEXPOSURE_STATUS : " +INTEXPOSURE_STATUS);
					}*/
				}
				
				if(objWorkList.getVal("COLLECTIONSUMMARY_STATUS").equalsIgnoreCase("") && !objWorkList.getVal("CIF_ID").equalsIgnoreCase(""))
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", inside function for COLLECTIONSUMMARY_STATUS check call");
					
					ExtTabDataMap.put("WINAME", objWorkList.getVal("winame"));
					ExtTabDataMap.put("CIF", objWorkList.getVal("CIFID"));
					
					//String integrationStatus=objiRBLIntegration.CollectionSummaryCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(),
							//CommonConnection.getJTSPort(),objRespBean.getWorkitemNumber(),objRespBean.getWorkStep(),integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap, CheckGridDataMap);

					/*String[] splitintstatus =integrationStatus.split("~");

					String ErrDesc = "MessageId: "+splitintstatus[2] + ", Return Code: "+splitintstatus[0] +", Return Desc: "+ splitintstatus[1];
					String attributesTag;

					if (splitintstatus[0].equals("0000"))
					{
						COLLECTIONSUMMARY_STATUS = "Success";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("COLLECTIONSUMMARY_STATUS : " +COLLECTIONSUMMARY_STATUS);
					}
					else
					{
						COLLECTIONSUMMARY_STATUS = "Failure";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("COLLECTIONSUMMARY_STATUS : " +COLLECTIONSUMMARY_STATUS);
					}*/
				}
				
				/*if(objWorkList.getVal("EXTEXPOSURE_STATUS").equalsIgnoreCase(""))
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+winame+", WSNAME: "+ws_name+", inside function for EXTEXPOSURE_STATUS check call");
					
					ExtTabDataMap.put("WINAME", objWorkList.getVal("winame"));
					ExtTabDataMap.put("CIF", objWorkList.getVal("CIFID"));
					
					String integrationStatus=objiRBLIntegration.ExtExposureCall(cabinetName,UserName,sessionId, sJtsIp,
							iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap);

					String[] splitintstatus =integrationStatus.split("~");

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
					}
				}
				
				
				
				if(objWorkList.getVal("FINANCIALSUMMARY_STATUS").equalsIgnoreCase("") && !objWorkList.getVal("CIF_ID").equalsIgnoreCase(""))
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
				
				/*if(DETECH_STATUS.equalsIgnoreCase(""))
				{
					String integrationStatus=objiRBLIntegration.DetechCall(cabinetName,UserName,sessionId, sJtsIp,
							iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeOut,  socketDetailsMap, ExtTabDataMap);

					String[] splitintstatus =integrationStatus.split("~");

					String ErrDesc = "MessageId: "+splitintstatus[2] + ", Return Code: "+splitintstatus[0] +", Return Desc: "+ splitintstatus[1];
					String attributesTag;

					if (splitintstatus[0].equals("0000"))
					{
						ENTITYDETAILS_STATUS = "Success";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ENTITYDETAILS_STATUS : " +ENTITYDETAILS_STATUS);
					}
					else
					{
						ENTITYDETAILS_STATUS = "Failure";
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ENTITYDETAILS_STATUS : " +ENTITYDETAILS_STATUS);
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
			    
			    String sWhere="";
			    if(MainCIF_Flag)
			    	sWhere="WI_NAME='"+objRespBean.getWorkitemNumber()+"' AND CIFID='"+CheckGridDataMap.get("CIFID")+"'";
			    else
			    	sWhere="WI_NAME='"+objRespBean.getWorkitemNumber()+"' AND CIFID='"+CheckGridDataMap.get("CIFID")+"' AND RELATEDPARTYID='"+CheckGridDataMap.get("RELATEDPARTYID")+"'";
			    
			    
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
	
	public static String parseInternalExposure(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String prod, String subprod, String cifId, String parentWiName,String cust_type,String CompanyCIF)
	{
		String flag1="";
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseInternalExposure: "+wrapperIP);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseInternalExposure: "+wrapperPort);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseInternalExposure: "+sessionId);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseInternalExposure: "+cabinetName);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseInternalExposure: "+wi_name);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseInternalExposure: "+appServerType);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseInternalExposure: "+parseXml);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseInternalExposure: "+returnType);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseInternalExposure: "+cifId);
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parentWiName jsp: parseInternalExposure: "+parentWiName);

		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		String result="";
		//Deepak code commented method changed with new subtag_single param 23jan2018
		String subtag_single="";
		InputStream is = new ByteArrayInputStream(parseXml.getBytes());
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataParent_deep jsp: strOutputXml: "+is);
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			NodeList nList_loan = doc.getElementsByTagName("CustomerExposureResponse");


			for(int i = 0 ; i<nList_loan.getLength();i++)
			{
				Node node  = nList_loan.item(i);
				Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				DOMImplementationLS abc  = (DOMImplementationLS) newXmlDocument.getImplementation();
				LSSerializer lsSerializer = abc.createLSSerializer();

				Element root = newXmlDocument.createElement("root");
				newXmlDocument.appendChild(root);
				root.appendChild(newXmlDocument.importNode(node, true));
				String n_parseXml = lsSerializer.writeToString(newXmlDocument);
				n_parseXml = n_parseXml.substring(n_parseXml.indexOf("<root>")+6,n_parseXml.indexOf("</root>"));
				cifId =  (n_parseXml.contains("<CustIdValue>")) ? n_parseXml.substring(n_parseXml.indexOf("<CustIdValue>")+"</CustIdValue>".length()-1,n_parseXml.indexOf("</CustIdValue>")):cifId;
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cif parseInternalExposure: "+cifId);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Company cif parseInternalExposure: "+CompanyCIF);
				if(!CompanyCIF.equalsIgnoreCase("") && cifId.equalsIgnoreCase(CompanyCIF))
				{
					cust_type="Corporate_CIF";
				}
				else
				{
					cust_type="Individual_CIF";
				}
				tagName="LoanDetails"; 
				subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
				sTableName="USR_0_iRBL_INTERNALEXPOSE_LoanDetails";
				subtag_single="";
				flag1=commonParseProduct(n_parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);

				if(flag1.equalsIgnoreCase("true")){
					tagName="CardDetails";
					subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
					sTableName="USR_0_iRBL_INTERNALEXPOSE_CardDetails";
					subtag_single="";
					flag1=commonParseProduct(n_parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
					
					if(flag1.equalsIgnoreCase("true")){
					tagName="InvestmentDetails";
					subTagName = "AmountDtls";
					sTableName="USR_0_iRBL_INTERNALEXPOSE_InvestmentDetails";
					subtag_single="";
					flag1=commonParseProduct(n_parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
					
					if(flag1.equalsIgnoreCase("true")){
						tagName="AcctDetails";
						subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
						sTableName="USR_0_iRBL_INTERNALEXPOSE_AcctDetails";
						subtag_single="ODDetails";
						flag1=commonParseProduct(n_parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
						if(flag1.equalsIgnoreCase("true")){
							tagName="Derived";
							subTagName = "";
							sTableName="USR_0_iRBL_INTERNALEXPOSE_Derived";
							subtag_single="";
							flag1=commonParseProduct(n_parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
							if(flag1.equalsIgnoreCase("true")){
								tagName="RecordDestribution";
								subTagName = "";
								sTableName="USR_0_iRBL_INTERNALEXPOSE_RecordDestribution";
								subtag_single="";
								flag1=commonParseProduct(n_parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
								  //Deepak 22 july 2019 new condition added to save custinfo
								if(flag1.equalsIgnoreCase("true")){
									tagName="CustInfo";
									subTagName = "";
									sTableName="USR_0_iRBL_INTERNALEXPOSE_CustInfo";
									subtag_single="";
									flag1=commonParseProduct(n_parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,cust_type,subtag_single);
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
					else
					{
					flag1="false";
					}
					}
					else{
						flag1="false";
					}
				}
				else
				{
					flag1="false";
				}
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
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
		return flag1;
	}
	
	public static String commonParseProduct(String parseXml,String tagName,String wi_name,String returnType,String sTableName,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String appServerType, String subTagName,String prod,String subprod, String cifId, String parentWiName,String cust_type,String subtag_single)
	{
		String retVal = "";

		try{
			if(!parseXml.contains(tagName)){
				return "true";
			} 
			else
			{
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("commonParse jsp: inside: ");
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
				String selectdata="";
				String 	sQry="";
				String ReportUrl = "";
				String NoOfContracts = "";
				String ECRN = "";
				String BorrowingCustomer = "";
				String FullNm = "";
				String TotalOutstanding = "";
				String TotalOverdue = "";

				String companyUpdateQuery="";
				String companiestobeUpdated = "";
				boolean stopIndividualToInsert = false;
				String referenceNo = "";
				String scoreInfo = "";
				String Aecb_Score = "";
				String range = "";

				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tagName jsp: commonParse: "+tagName);
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("subTagName jsp: commonParse: "+subTagName);
				//Parsing AECB score, range and Reference No. for 2.1 start, Added by Shivang
			
				referenceNo=(parseXml.contains("<ReferenceNumber>")) ? parseXml.substring(parseXml.indexOf("<ReferenceNumber>")+"</ReferenceNumber>".length()-1,parseXml.indexOf("</ReferenceNumber>")):"";
				if(parseXml.contains("<ScoreInfo>")){
					scoreInfo = parseXml.substring(parseXml.indexOf("<ScoreInfo>")+"</ScoreInfo>".length()-1,parseXml.indexOf("</ScoreInfo>"));
					Aecb_Score=(scoreInfo.contains("<Value>")) ? scoreInfo.substring(scoreInfo.indexOf("<Value>")+"</Value>".length()-1,scoreInfo.indexOf("</Value>")):"";
					range=(scoreInfo.contains("<Range>")) ? scoreInfo.substring(scoreInfo.indexOf("<Range>")+"</Range>".length()-1,scoreInfo.indexOf("</Range>")):"";
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parsexml jsp: commonParse: AECB Score: "+Aecb_Score + " Range: "+range);
				}
			
				//Parsing AECB score, range and Reference No. for 2.1 end, Added by Shivang
				//Deepak 23 Dec changes done to save updated Rerport URL in DB
				ReportUrl=(parseXml.contains("<ReportUrl>")) ? parseXml.substring(parseXml.indexOf("<ReportUrl>")+"</ReportUrl>".length()-1,parseXml.indexOf("</ReportUrl>")):"";
				//cifId=(parseXml.contains("<CustIdValue>")) ? parseXml.substring(parseXml.indexOf("<CustIdValue>")+"</CustIdValue>".length()-1,parseXml.indexOf("</CustIdValue>")):"";
				FullNm=(parseXml.contains("<FullNm>")) ? parseXml.substring(parseXml.indexOf("<FullNm>")+"</FullNm>".length()-1,parseXml.indexOf("</FullNm>")):"";
				TotalOutstanding=(parseXml.contains("<TotalOutstanding>")) ? parseXml.substring(parseXml.indexOf("<TotalOutstanding>")+"</TotalOutstanding>".length()-1,parseXml.indexOf("</TotalOutstanding>")):"";
				TotalOverdue=(parseXml.contains("<TotalOverdue>")) ? parseXml.substring(parseXml.indexOf("<TotalOverdue>")+"</TotalOverdue>".length()-1,parseXml.indexOf("</TotalOverdue>")):"";
				NoOfContracts=(parseXml.contains("<NoOfContracts>")) ? parseXml.substring(parseXml.indexOf("<NoOfContracts>")+"</NoOfContracts>".length()-1,parseXml.indexOf("</NoOfContracts>")):"";
				ECRN=(parseXml.contains("<ECRN>")) ? parseXml.substring(parseXml.indexOf("<ECRN>")+"</ECRN>".length()-1,parseXml.indexOf("</ECRN>")):"";
				BorrowingCustomer=(parseXml.contains("<BorrowingCustomer>")) ? parseXml.substring(parseXml.indexOf("<BorrowingCustomer>")+"</BorrowingCustomer>".length()-1,parseXml.indexOf("</BorrowingCustomer>")):"";


				Map<String, String> tagValuesMap= new LinkedHashMap<String, String>();		 
				tagValuesMap= CommonMethods.getTagDataParent_deep(parseXml,tagName,subTagName,subtag_single);

				Map<String, String> map = tagValuesMap;
				// String colValue="";
				for (Map.Entry<String, String> entry : map.entrySet())
				{
					valueArr=entry.getValue().split("~");
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values" + entry.getValue());

					//columnValues = valueArr[1].spilt(",");
					// columnValues=columnValues+",'"+getCellData(SheetName1, rCnt, cCnt)+"'";
					//colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
					columnName = valueArr[0]+",Wi_Name,Request_Type,Product_Type,CardType,CifId,Child_Wi";
					columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+prod+"','"+subprod+"','"+cifId+"','"+wi_name+"'";



					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnName commonParse" + columnName);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnValues commonParse" + columnValues);
					if(sTableName.equalsIgnoreCase("USR_0_iRBL_INTERNALEXPOSE_CardDetails")){
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi,Liability_type";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"','"+cust_type+"'";
						sWhere="CardEmbossNum = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"' And Liability_type ='"+cust_type+"'";
						sQry="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And CardEmbossNum = '"+entry.getKey()+"' And Liability_type ='Individual_CIF' ";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "sQry sQry" + sQry);
						if(cust_type.equalsIgnoreCase("Individual_CIF")) {
							companyUpdateQuery="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And CardEmbossNum = '"+entry.getKey()+"' And Liability_type ='Corporate_CIF'";
						}
						if(parseXml.contains("<LinkedCIFs>"))
						{
							parseLinkedCif(parseXml,cifId,parentWiName,wi_name,entry.getKey(),cust_type,"Card",cabinetName,sessionId,wrapperIP,wrapperPort,appServerType);
						}
					}
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_INTERNALEXPOSE_LoanDetails")){
						columnName = valueArr[0]+",Wi_Name,Request_Type,Product_Type,CardType,CifId,Child_Wi,Liability_type";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+prod+"','"+subprod+"','"+cifId+"','"+wi_name+"','"+cust_type+"'";
						columnName =columnName.replace("OutStandingAmt","TotalOutStandingAmt");
						sWhere="AgreementId = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"' And Liability_type ='"+cust_type+"'";
						sQry="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And  AgreementId = '"+entry.getKey()+"' And Liability_type ='Individual_CIF'";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "sQry  loan sQry" + sQry);
						if(cust_type.equalsIgnoreCase("Individual_CIF")) {
							companyUpdateQuery="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And AgreementId = '"+entry.getKey()+"' And Liability_type ='Corporate_CIF'";
						}
						if(parseXml.contains("<LinkedCIFs>"))
						{
							parseLinkedCif(parseXml,cifId,parentWiName,wi_name,entry.getKey(),cust_type,"Loan",cabinetName,sessionId,wrapperIP,wrapperPort,appServerType);
						}
					}
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_EXTERNALEXPOSE_ChequeDetails")){
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"'";
						sWhere="Wi_Name='"+parentWiName+"' AND ChqType = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"'";
					}
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_EXTERNALEXPOSE_LoanDetails")){
						String History = parseHistoryUtilization(parseXml, entry.getKey(), "LoanDetails", "<History>", "</History>");
						History = History.replace("\n", "").replace("\r", "");
						String Utilization = parseHistoryUtilization(parseXml, entry.getKey(), "LoanDetails", "<Utilizations24Months>", "</Utilizations24Months>");
						Utilization = Utilization.replace("\n", "").replace("\r", "");
						 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside parseHistoryUtilization" + History);
						 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside parseHistoryUtilization" + Utilization);
						columnName = valueArr[0]+",Wi_Name,Request_Type,Product_Type,CardType,CifId,Child_Wi,Liability_type";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+prod+"','"+subprod+"','"+cifId+"','"+wi_name+"','"+cust_type+"'";
						String columnName_arr[] = columnName.split(",");
						  String columnValues_arr[] = columnValues.split(",");
						  for(int arrlen=0;arrlen<columnName_arr.length;arrlen++){
							  if("LoanType".equalsIgnoreCase(columnName_arr[arrlen])){
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
								  String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
								  columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
								  
							  }
							  if("History".equalsIgnoreCase(columnName_arr[arrlen])){
								 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
								 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
								  //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
								  columnValues = columnValues.replace(columnValues_arr[arrlen], "'"+History+"'");
								  
							  }
							  if("Utilizations24Months".equalsIgnoreCase(columnName_arr[arrlen])){
								 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
								 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
								  //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
								  columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], "'"+Utilization+"'");
								  
							  }
						  }
						columnName =columnName.replace("OutStanding Balance","OutStanding_Balance");
						columnName =columnName.replace("LastUpdateDate","datelastupdated");
						columnName =columnName.replace("Total Amount","Total_Amount");
						columnName =columnName.replace("Payments Amount","Payments_Amount");
						columnName =columnName.replace("Overdue Amount","Overdue_Amount");
						 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside parseHistoryUtilization" + columnName);
						 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside parseHistoryUtilization" + columnValues);
						//sWhere="Wi_Name='"+parentWiName+"' AND AgreementId = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"'";
						sWhere="Wi_Name='"+parentWiName+"' AND AgreementId = '"+entry.getKey()+"'";
					}
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_EXTERNALEXPOSE_CardDetails")){
						String History = parseHistoryUtilization(parseXml, entry.getKey(), "CardDetails", "<History>", "</History>");
						History = History.replace("\n", "").replace("\r", "");
						String Utilization = parseHistoryUtilization(parseXml, entry.getKey(), "CardDetails", "<Utilizations24Months>", "</Utilizations24Months>");
						Utilization = Utilization.replace("\n", "").replace("\r", "");
						columnName = valueArr[0]+",Wi_Name,Request_Type,Product_Type,sub_product_type,CifId,Child_Wi,Liability_type";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+prod+"','"+subprod+"','"+cifId+"','"+wi_name+"','"+cust_type+"'";
						 String columnName_arr[] = columnName.split(",");
						  String columnValues_arr[] = columnValues.split(",");
						  for(int arrlen=0;arrlen<columnName_arr.length;arrlen++){
							  if("CardType".equalsIgnoreCase(columnName_arr[arrlen])){
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
								  String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
								  columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
								  
							  }
							  if("History".equalsIgnoreCase(columnName_arr[arrlen])){
									 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
									 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
									  //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
									  columnValues = columnValues.replace(columnValues_arr[arrlen], "'"+History+"'");
									  
								  }
								  if("Utilizations24Months".equalsIgnoreCase(columnName_arr[arrlen])){
									 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
									 // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
									  //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
									  columnValues = columnValues.replace(columnValues_arr[arrlen], "'"+Utilization+"'");
									  
								  }
						  }
						 sWhere="Wi_Name='"+parentWiName+"' AND CardEmbossNum = '"+entry.getKey()+"'";
						//sWhere="Wi_Name='"+parentWiName+"' AND CardEmbossNum = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"'";
					}
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_INTERNALEXPOSE_Derived")){
					//Deepak 23 Dec changes done to save updated Rerport URL in DB.
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,FullNm,TotalOutstanding,TotalOverdue,NoOfContracts,ReportURL,Child_Wi,ReferenceNo,AECB_Score,Range";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+FullNm+"','"+TotalOutstanding+"','"+TotalOverdue+"','"+NoOfContracts+"','"+ReportUrl+"','"+wi_name+"','"+referenceNo+"','"+Aecb_Score+"','"+range+"'";
						sWhere="Wi_Name='"+parentWiName+"' AND Request_Type = '"+returnType+"' and Child_Wi='"+wi_name+"' and cifid='"+cifId+"'";
					}
					//Changes Done to save data in USR_0_iRBL_INTERNALEXPOSE_RecordDestribution table on 14th sept by Aman
					//Deepak Child workitem added in both columnName & columnValues to get it saved in backend - 8 July 2019.
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_INTERNALEXPOSE_RecordDestribution")){
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"'";
						sWhere="Wi_Name='"+parentWiName+"' AND ContractType = '"+entry.getKey()+"'and CifId='"+cifId+"'";
					}
					//Changes Done to save data in USR_0_iRBL_INTERNALEXPOSE_RecordDestribution table on 14th sept by Aman
					//Deepak Child workitem added in both columnName & columnValues to get it saved in backend - 8 July 2019.
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_EXTERNALEXPOSE_AccountDetails")){
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"'";
						String columnName_arr[] = columnName.split(",");
						  String columnValues_arr[] = columnValues.split(",");
						  for(int arrlen=0;arrlen<columnName_arr.length;arrlen++){
							  if("AcctType".equalsIgnoreCase(columnName_arr[arrlen])){
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
								  String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
								  columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
								  break;
							  }
						  }
						sWhere="Wi_Name='"+parentWiName+"' AND AcctId = '"+entry.getKey()+"'";//Cif_id removed
					}
						//Deepak changes done for Service details
					   else if(sTableName.equalsIgnoreCase("USR_0_iRBL_EXTERNALEXPOSE_ServicesDetails")){
						  	  columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi";
							  columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"'";
							  String columnName_arr[] = columnName.split(",");
							  String columnValues_arr[] = columnValues.split(",");
							 
						   for(int arrlen=0;arrlen<columnName_arr.length;arrlen++){
								  if("ServiceName".equalsIgnoreCase(columnName_arr[arrlen])){
									  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
									  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
									  String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
									  columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
									  break;
								  }
						   }
							  sWhere="Wi_Name='"+parentWiName+"' AND ServiceID = '"+entry.getKey()+"'";
					  }
					//below changes Done to save AccountType in USR_0_iRBL_INTERNALEXPOSE_AcctDetails table on 29th Dec by Disha
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_INTERNALEXPOSE_AcctDetails"))
					{
						String CreditGrade = (parseXml.contains("<CreditGrade>")) ? parseXml.substring(parseXml.indexOf("<CreditGrade>")+"</CreditGrade>".length()-1,parseXml.indexOf("</CreditGrade>")):"";
						//PCASP-2833 
						String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>")+"</IsDirect>".length()-1,parseXml.indexOf("</IsDirect>")):"";
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi,CreditGrade,Account_Type,isDirect";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"','"+CreditGrade+"','"+cust_type+"','"+isDirect+"'";
						sWhere="Request_Type='"+returnType+"' AND AcctId = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"' AND Account_Type = '"+cust_type+"'";
						String columnName_arr[] = columnName.split(",");
					  String columnValues_arr[] = columnValues.split(",");
					  String LimitSactionDate="";
					  for(int arrlen=0;arrlen<columnName_arr.length;arrlen++)
					  {
						  if("LimitSactionDate".equalsIgnoreCase(columnName_arr[arrlen]))
						  {
							  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside LimitSactionDate tag name" + columnName_arr[arrlen]);
							 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside LimitSactionDate value" + columnValues_arr[arrlen]);
							  LimitSactionDate = columnValues_arr[arrlen];
						  }
							  if("MonthsOnBook".equalsIgnoreCase(columnName_arr[arrlen]))
							  {
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside MonthsOnBook tag name" + columnName_arr[arrlen]);
								  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside MonthsOnBook value" + columnValues_arr[arrlen]);
								  if(!LimitSactionDate.equals(""))
								  {
									  String MOB = get_Mob_forOD(LimitSactionDate);
									  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside MonthsOnBook value" + MOB);
									  if(!MOB.equalsIgnoreCase("Invalid"))
									  {
										  columnValues = columnValues.replace(columnValues_arr[arrlen], "'"+MOB+"'");
									  }
								  }
								  
							  }
						}
						//change by saurabh on 24th Feb for skipping employer accounts to save.
						sQry="Select count(*) as selectdata from NG_RLOS_ALOC_OFFLINE_DATA where CIF_ID ='Nikhil123'";
						if(parseXml.contains("<LinkedCIFs>"))
						{
							parseLinkedCif(parseXml,cifId,parentWiName,wi_name,entry.getKey(),cust_type,"Account",cabinetName,sessionId,wrapperIP,wrapperPort,appServerType);
						}
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "sQry  loan sQry" + sQry);	  
					}
					else if(sTableName.equalsIgnoreCase("USR_0_iRBL_INTERNALEXPOSE_InvestmentDetails")){
						
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"'";
						sWhere="Request_Type='"+returnType+"' AND Child_Wi='"+wi_name+"' and InvestmentID='"+entry.getKey()+"'";
						  
					}
					//above changes Done to save AccountType in USR_0_iRBL_INTERNALEXPOSE_AcctDetails table on 29th Dec by Disha
					  //Deepak 22 july 2019 new condition added to save custinfo
					  else if(sTableName.equalsIgnoreCase("USR_0_iRBL_INTERNALEXPOSE_CustInfo")){
					  String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>")+"</IsDirect>".length()-1,parseXml.indexOf("</IsDirect>")):"";
						  columnName = valueArr[0]+",Wi_Name,Child_Wi,Request_Type,CifId,isDirect";
							columnValues = valueArr[1]+",'"+parentWiName+"','"+wi_name+"','"+returnType+"','"+cifId+"','"+isDirect+"'";
						   sWhere="Child_Wi='"+wi_name+"' AND Request_Type = '"+returnType+"' AND CifId = '"+cifId+"'";	  
						  }
					else{
						sWhere="Request_Type='"+returnType+"' AND Child_Wi='"+wi_name+"'";;
					}
	
					//strInputXml =	ExecuteQuery_APUpdate(sTableName,columnName,columnValues,sWhere,cabinetName,sessionId);
					strInputXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), sTableName, columnName, columnValues, sWhere);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apUpdateInput from "+sTableName+" Table "+strInputXml);

					strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput Table "+strOutputXml);

					tagNameU = "APUpdate_Output";
					subTagNameU = "MainCode";
					subTagNameU_2 = "Output";
					mainCode = CommonMethods.getTagDataValue(strOutputXml,tagNameU,subTagNameU);
					row_updated = CommonMethods.getTagDataValue(strOutputXml,tagNameU,subTagNameU_2);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select mainCode --> "+mainCode);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select mainCode --> "+row_updated);
					if(!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
					{	//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sQry sQry sQry --> "+sQry);
						if (!sQry.equalsIgnoreCase("")){
							
							strInputXml = CommonMethods.apSelectWithColumnNames(sQry,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml: "+ strInputXml);
							strOutputXml = iRBLSysCheckIntegration.WFNGExecute(strInputXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml: "+ strOutputXml);

							mainCode = (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>")+"</MainCode>".length()-1,strOutputXml.indexOf("</MainCode>")):"";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select mainCode --> "+mainCode);
							selectdata=(strOutputXml.contains("<selectdata>")) ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>")+"</selectdata>".length()-1,strOutputXml.indexOf("</selectdata>")):"";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select selectdata --> "+selectdata);
						}
						if (!companyUpdateQuery.equalsIgnoreCase("")){
							
							strInputXml = CommonMethods.apSelectWithColumnNames(companyUpdateQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml: "+ strInputXml);
							strOutputXml = iRBLSysCheckIntegration.WFNGExecute(strInputXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml: "+ strOutputXml);

							mainCode = (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>")+"</MainCode>".length()-1,strOutputXml.indexOf("</MainCode>")):"";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select mainCode --> "+mainCode);

							companiestobeUpdated=(strOutputXml.contains("<selectdata>")) ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>")+"</selectdata>".length()-1,strOutputXml.indexOf("</selectdata>")):"";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select companiestobeUpdated --> "+companiestobeUpdated);

							if(Integer.parseInt(companiestobeUpdated)>0){
								sWhere="Child_Wi='"+wi_name+"' AND CardEmbossNum = '"+entry.getKey()+"' And Liability_type ='Corporate_CIF'";
								//strInputXml =	ExecuteQuery_APUpdate(sTableName,columnName,columnValues,sWhere,cabinetName,sessionId);
								
								strInputXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), sTableName, columnName, columnValues, sWhere);
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apUpdateInput from "+sTableName+" Table "+strInputXml);

								strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput Table "+strOutputXml);

								tagNameU = "APUpdate_Output";
								subTagNameU = "MainCode";
								subTagNameU_2 = "Output";
								mainCode = CommonMethods.getTagDataValue(strOutputXml,tagNameU,subTagNameU);
								//row_updated = getTagDataValue(strOutputXml,tagNameU,subTagNameU_2);
								////iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select mainCode for update query for cif"+cifId+"--> "+mainCode);
								////iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagDataValue select rowUpdated for company for update query for cif"+cifId+" --> "+row_updated);
								stopIndividualToInsert = true;
							}
						}

						if(sQry.equalsIgnoreCase("") || (mainCode.equalsIgnoreCase("0") && selectdata.equalsIgnoreCase("0") && !stopIndividualToInsert)){
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("calling APInsert for cif --> "+cifId);
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("calling APInsert for table --> "+sTableName);
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("calling APInsert for cust_type --> "+cust_type);
							//strInputXml =	ExecuteQuery_APInsert(sTableName,columnName,columnValues,cabinetName,sessionId);
							
							strInputXml = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnName, columnValues, sTableName);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert from "+sTableName+" Table "+strInputXml);

							strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
							
							mainCode = CommonMethods.getTagDataValue(strOutputXml,"APInsert_Output",subTagNameU);
							if(!mainCode.equalsIgnoreCase("0"))
							{
								retVal = "false";
								//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:false "+retVal);
							}
							else
							{
								retVal = "true";
								//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: commonparseproduct:true "+retVal);
							}
						}
						else{
							retVal = "true";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
						}
					}
					else
					{
						retVal = "true";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
					}

				}
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: finalValue: "+retVal);
				return retVal;
			} 
		}
		catch(Exception e){
			System.out.println("Exception occured in commonParseProduct: "+ e.getMessage());
			e.printStackTrace();
			retVal = "false";
		}
		return retVal;
	}
	
	private static void parseLinkedCif(String parseXml, String Main_CIF, String Wi_name, String Child_wi, String Agreement_id, String Cust_Type, String Liability_type, String cabinetName,String sessionId, String wrapperIP, String wrapperPort, String appServerType) 
	{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF");
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Input_XMl"+parseXml);
		
			try
			{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new InputSource(new StringReader(parseXml)));
				doc.getDocumentElement().normalize();
				String Liabilityid = "";
				//String ParentTag= doc.getDocumentElement().getNodeName();
				NodeList nList;
				if("Account".equalsIgnoreCase(Liability_type))
				{
				nList = doc.getElementsByTagName("AcctDetails");
				Liabilityid="AcctId";
				}
				else if ("Loan".equalsIgnoreCase(Liability_type))
				{
					nList = doc.getElementsByTagName("LoanDetails");
					Liabilityid="AgreementId";
				}
				else 
				{
					nList = doc.getElementsByTagName("CardDetails");
					Liabilityid="CardEmbossNum";
				}
					
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: nList.getLength()"+nList.getLength());
				   for (int temp = 0; temp < nList.getLength(); temp++) 
				   {
			            Node nNode = nList.item(temp);
			          //  System.out.println("\nCurrent Element :" + nNode.getNodeName());
			            
			            if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			            {
			            
			             Element eElement = (Element) nNode;
			             String Liability_ID=eElement.getElementsByTagName(Liabilityid).item(0).getTextContent();
			             iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: AcctId"+Liability_ID);
			             if(Liability_ID.equalsIgnoreCase(Agreement_id))
			             {
			            			             
			            	 NodeList Linked_CIF= eElement.getElementsByTagName("LinkedCIFs");
			            	 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Linked_CIF.getLength()"+Linked_CIF.getLength());
			                 for (int temp1 = 0; temp1 < Linked_CIF.getLength(); temp1++)
			                    {
			                     Node node1 = Linked_CIF.item(temp1);
			                     if (node1.getNodeType() == Node.ELEMENT_NODE)
			                       { 
			                           Element eElement1 = (Element) node1;
			                           String Linked_CIF1= eElement1.getElementsByTagName("CIFId").item(0).getTextContent();
			                           String Relation1 = eElement1.getElementsByTagName("RelationType").item(0).getTextContent();
			                           iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Linked_CIF"+Linked_CIF1);
			     					  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Relation"+Relation1);
			                    
			             /*  String Linked_CIF= eElement.getElementsByTagName("CIFId").item(0).getTextContent();
			              String Relation = eElement.getElementsByTagName("RelationType").item(0).getTextContent(); */
			     					  
			     		 String SQuery = "select count(wi_name) as Select_Count from USR_0_iRBL_INTERNALEXPOSE_LinkedICF where Linked_CIFs='"+Linked_CIF1+"' and Relation='"+Relation1+"' and wi_name='"+Wi_name+"' and  child_wi='"+Child_wi+"' and Main_Cif='"+Main_CIF+"' and AgreementId='"+Agreement_id+"'";
			             		
			     		//String strInputXml =	ExecuteQuery_APSelect(SQuery,cabinetName,sessionId);
     					 String strInputXml = CommonMethods.apSelectWithColumnNames(SQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
     					 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml: "+ strInputXml);
     					 String strOutputXml = iRBLSysCheckIntegration.WFNGExecute(strInputXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
     					 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml: "+ strOutputXml);
			              
							String mainCode = (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>")+"</MainCode>".length()-1,strOutputXml.indexOf("</MainCode>")):"";
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF select mainCode --> "+mainCode);
							if("0".equalsIgnoreCase(mainCode))
							{
								String selectdata=(strOutputXml.contains("<Select_Count>")) ? strOutputXml.substring(strOutputXml.indexOf("<Select_Count>")+"</Select_Count>".length()-1,strOutputXml.indexOf("</Select_Count>")):"";
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF select selectdata --> "+selectdata);
								int totalretrieved=Integer.parseInt(selectdata);
								if(totalretrieved==0)
								{
									String sTableName="USR_0_iRBL_INTERNALEXPOSE_LinkedICF";
									String columnName="Wi_name,Child_wi,Linked_CIFs,Relation,AgreementId,Main_Cif,Liability_Type,Cust_Type";
									String columnValues="'"+Wi_name+"','"+Child_wi+"','"+Linked_CIF1+"','"+Relation1+"','"+Agreement_id+"','"+Main_CIF+"','"+Liability_type+"','"+Cust_Type+"'";
									//strInputXml =	ExecuteQuery_APInsert(sTableName,columnName,columnValues,cabinetName,sessionId);
									//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml" + strInputXml);
									
									strInputXml = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnName, columnValues, sTableName);
									iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert from "+sTableName+" Table "+strInputXml);

									strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
									
									mainCode = CommonMethods.getTagDataValue(strOutputXml,"APInsert_Output","MainCode");
									
									iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( " Parse linked cif  strInputXml" + strInputXml);
								}
							}
							//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select selectdata --> "+selectdata);
			             }
			            }
			             }
			            }
				   }
			}
			catch(Exception ex)
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parse linked cif : "+ ex.getMessage());
				ex.printStackTrace();
			}
	}
	
	public static String get_loanDesc(String loan_code,String cabinetName,String sessionId,String wrapperIP,String wrapperPort,String appServerType ){
		String loan_desc="";
		try{
			String str_Loandesc = "select Description from NG_MASTER_contract_type with(nolock) where code =:code";
			String params="code=="+loan_code.replace("'", "");
			
			String strInputXml = CommonMethods.apSelectWithColumnNames(str_Loandesc,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml: "+ strInputXml);
			String strOutputXml = iRBLSysCheckIntegration.WFNGExecute(strInputXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml: "+ strOutputXml);

			/*String strInputXml=ExecuteQuery_APSelectwithparam(str_Loandesc,params,cabinetName,sessionId);
			String strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);*/
			String Maincode = strOutputXml.substring(strOutputXml.indexOf("<MainCode>")+"</MainCode>".length()-1,strOutputXml.indexOf("</MainCode>"));
			if("0".equalsIgnoreCase(Maincode)){
				loan_desc=strOutputXml.substring(strOutputXml.indexOf("<Description>")+"</Description>".length()-1,strOutputXml.indexOf("</Description>"));	
			}else{
				loan_desc = loan_code;
			}
		}
		catch(Exception e){
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in get_loanDesc:  "+e.getMessage());
			loan_desc = loan_code;
		}
		return "'"+loan_desc+"'";
	}
	
	public static String parseHistoryUtilization(String Xml,String Agreement_id,String Liability_type,String StartType, String EndType)
	{
		//WriteLog("Inside parse CIF");
		//WriteLog("Inside parse CIF:: Input_XMl" + Xml);
		String Output_desired = "";

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(Xml)));
			doc.getDocumentElement().normalize();
			String Liabilityid = "";
			//String ParentTag= doc.getDocumentElement().getNodeName();
			NodeList nList;
			 if ("LoanDetails".equalsIgnoreCase(Liability_type)) {
				nList = doc.getElementsByTagName("LoanDetails");
				Liabilityid = "AgreementId";
			} else {
				nList = doc.getElementsByTagName("CardDetails");
				Liabilityid = "CardEmbossNum";
			}

			 iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: nList.getLength()" + nList.getLength());

					for (int temp = 0; temp < nList.getLength(); temp++) {
						Node nNode = nList.item(temp);
						  if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				            {
				            
				             Element eElement = (Element) nNode;
							 
				             String Liability_ID=eElement.getElementsByTagName(Liabilityid).item(0).getTextContent();
				             iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: AcctId"+Liability_ID);
				             if(Liability_ID.equalsIgnoreCase(Agreement_id))
				             {
						//  System.out.println("\nCurrent Element :" + nNode.getNodeName());
				        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: ExecuteQuery_APSelect" + nNode);
						String Liability_aggregate = CommonMethods.nodeToString(nNode);
						Output_desired = Liability_aggregate.substring(Liability_aggregate.indexOf(StartType),
								Liability_aggregate.lastIndexOf(EndType) + EndType.length());

					}
				}
			}
		
		}catch (Exception ex) {
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parse history Utilitixation cif : " + ex.getMessage());
			ex.printStackTrace();
		}
		return Output_desired;
	}
	
	public static String get_Mob_forOD(String LimitSactionDate)
	{
		try 
		{
			LimitSactionDate = LimitSactionDate.replaceAll("'", "");
			Date Current_date = new Date();
			Date Old_Date = new SimpleDateFormat("yyyy-MM-dd").parse(LimitSactionDate);
			int yy = Current_date.getYear()-Old_Date.getYear();
			int mm = Current_date.getMonth()-Old_Date.getMonth();
			if(mm<0)
			{
				yy--;
				mm = 12 - Old_Date.getMonth() + Current_date.getMonth();
				if(Current_date.getDate() < Old_Date.getDate())
				{
					mm--;
				}
			}
			else if (mm == 0 && Current_date.getDate() < Old_Date.getDate())
			{
				yy--;
				mm = 11 - Old_Date.getMonth() + Current_date.getMonth();
			}
			else if (mm > 0 && Current_date.getDate() < Old_Date.getDate())
			{
				mm--;
			}
			else if (Current_date.getDate() - Old_Date.getDate() !=0)
			{
				if(mm==12)
				{
					yy++;
					mm=0;
				}
			}
			
			return String.valueOf((yy*12)+mm);
	}
	catch (Exception ex)
	{
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in get_Mob_forOD: "+ ex.getMessage());
		ex.printStackTrace();
		return "Invalid";
	}
	
	}
}
