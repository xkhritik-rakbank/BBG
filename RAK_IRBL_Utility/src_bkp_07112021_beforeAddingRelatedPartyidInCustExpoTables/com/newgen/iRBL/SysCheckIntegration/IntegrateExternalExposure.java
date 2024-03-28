package com.newgen.iRBL.SysCheckIntegration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class IntegrateExternalExposure
  {
    private String rowVal;
    private static String CheckGridTable = "USR_0_IRBL_CHECKS_GRID_DTLS";
    
    public static String IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {
    	String MainStatusFlag = "Success";
        String DBQuery =
          "SELECT CIF_NUMBER AS 'CIF','Corporate_CIF'  AS 'CUSTOMER_TYPE',NEW_TOPUP AS 'Product',FINANCE_TYPE AS 'SubProduct','Primary' AS 'CIF_TYPE',INTERNAL_EXPOSURE_STATUS,EXTERNAL_EXPOSURE_STATUS, COLLECTION_SUMMARY_STATUS, FINANCIAL_SUMMARY_STATUS, CONVENTIONAL_ISLAMIC, REQUESTEDLOANAMOUNT, (select top 1 Description from USR_0_iRBL_Proposed_Tenor_Master with(nolock) where Code = PROPOSED_TENOR) AS PROPOSED_TENOR, 'EXT' as 'DATAFROM', '' AS 'COUNTRYOFRESIDENCE', '' AS 'RELATEDPARTYID', '' as 'RELATIONSHIPTYPE', '' as 'DATEOFBIRTH' FROM RB_iRBL_EXTTABLE WITH(NOLOCK) WHERE WINAME= '"
            + processInstanceID + "'"
            + "UNION ALL SELECT CIF AS 'CIF', CASE WHEN COMPANYFLAG='Y' THEN 'Corporate_CIF' WHEN COMPANYFLAG='YES' THEN 'Corporate_CIF' ELSE 'Individual_CIF' END  AS 'CUSTOMER_TYPE', '' AS 'Product','' AS 'SubProduct','Primary' AS 'CIF_TYPE',INTERNAL_EXPOSURE_STATUS,EXTERNAL_EXPOSURE_STATUS, COLLECTION_SUMMARY_STATUS, FINANCIAL_SUMMARY_STATUS, '' AS CONVENTIONAL_ISLAMIC, '' as REQUESTEDLOANAMOUNT, '' as PROPOSED_TENOR, 'RELATED' as 'DATAFROM', COUNTRYOFRESIDENCE, RELATEDPARTYID, RELATIONSHIPTYPE, isnull(format(DATEOFBIRTH,'dd-MM-yyyy'),'') as DATEOFBIRTH FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(NOLOCK) WHERE WI_NAME='"
            + processInstanceID + "' ";

        String extTabDataIPXML =
          CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
        String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

        XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
        int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
          {
            String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

            //XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
            NGXmlList objWorkList = xmlParserData.createList("Records", "Record");

            HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

            String InquiryPurpose = "";
            String TxmAmount = "";
            String ProposedTenor = "";

            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
              {
                CheckGridDataMap.put("CIF", objWorkList.getVal("CIF"));
                CheckGridDataMap.put("CUSTOMER_TYPE", objWorkList.getVal("CUSTOMER_TYPE"));
                CheckGridDataMap.put("Product", objWorkList.getVal("Product"));
                CheckGridDataMap.put("SubProduct", objWorkList.getVal("SubProduct"));
                CheckGridDataMap.put("CIF_TYPE", objWorkList.getVal("CIF_TYPE"));
                CheckGridDataMap.put("EXTERNAL_EXPOSURE_STATUS", objWorkList.getVal("EXTERNAL_EXPOSURE_STATUS"));
                CheckGridDataMap.put("CONVENTIONAL_ISLAMIC", objWorkList.getVal("CONVENTIONAL_ISLAMIC"));
                CheckGridDataMap.put("REQUESTEDLOANAMOUNT", objWorkList.getVal("REQUESTEDLOANAMOUNT"));
                CheckGridDataMap.put("PROPOSED_TENOR", objWorkList.getVal("PROPOSED_TENOR"));
                CheckGridDataMap.put("DATAFROM", objWorkList.getVal("DATAFROM"));
                CheckGridDataMap.put("COUNTRYOFRESIDENCE", objWorkList.getVal("COUNTRYOFRESIDENCE"));
                CheckGridDataMap.put("RELATEDPARTYID", objWorkList.getVal("RELATEDPARTYID"));
                CheckGridDataMap.put("RELATIONSHIPTYPE", objWorkList.getVal("RELATIONSHIPTYPE"));
                CheckGridDataMap.put("DATEOFBIRTH", objWorkList.getVal("DATEOFBIRTH"));
                
                if("".equalsIgnoreCase(InquiryPurpose))
                {	
                	String FinanceType = CheckGridDataMap.get("SubProduct").trim();
                	String ConvIslamic = CheckGridDataMap.get("CONVENTIONAL_ISLAMIC").trim();
                	if("Conventional".equalsIgnoreCase(ConvIslamic))
                	{	
	                	if("RBL".equalsIgnoreCase(FinanceType) || "RBL Express".equalsIgnoreCase(FinanceType))
	                		InquiryPurpose = "22";
	                	else if("RBL Micro".equalsIgnoreCase(FinanceType))
	                		InquiryPurpose = "12";
	                	else if("TWC".equalsIgnoreCase(FinanceType) || "TWC Express".equalsIgnoreCase(FinanceType))
	                		InquiryPurpose = "63";
	                	else if("ABF".equalsIgnoreCase(FinanceType))
	                		InquiryPurpose = "10";
	                	else
	                		InquiryPurpose = "1";
                	}	
                	else if("Islamic".equalsIgnoreCase(ConvIslamic))
                	{	
	                	if("RBL".equalsIgnoreCase(FinanceType) || "RBL Express".equalsIgnoreCase(FinanceType)  || "RBL Micro".equalsIgnoreCase(FinanceType))
	                		InquiryPurpose = "88";
	                	else if("TWC".equalsIgnoreCase(FinanceType) || "TWC Express".equalsIgnoreCase(FinanceType))
	                		InquiryPurpose = "74";
	                	else if("ABF".equalsIgnoreCase(FinanceType))
	                		InquiryPurpose = "28";
	                	else
	                		InquiryPurpose = "1";
                	}
                	else
                		InquiryPurpose = "1";
                }
                
                if("".equalsIgnoreCase(TxmAmount) 
                		&& "EXT".equalsIgnoreCase(CheckGridDataMap.get("DATAFROM").trim()))
                {
                	TxmAmount = CheckGridDataMap.get("REQUESTEDLOANAMOUNT").trim();
                }
                
                if("".equalsIgnoreCase(ProposedTenor) 
                		&& "EXT".equalsIgnoreCase(CheckGridDataMap.get("DATAFROM").trim()))
                {
                	ProposedTenor = CheckGridDataMap.get("PROPOSED_TENOR").trim();
                }
                
                // As per Rachit - External Exposure will not be called when (Country of Residence not AE or relationship type is Others or for Minors 21 year below) for Related Parties, for Main CIF it will be always triggered
                // As per sameera mail subject (RE: jira 10102)
                if(!"EXT".equalsIgnoreCase(CheckGridDataMap.get("DATAFROM").trim()))
                {
                	String DateOfBirth = objWorkList.getVal("DATEOFBIRTH").trim();
                	if(!"AE".equalsIgnoreCase(objWorkList.getVal("COUNTRYOFRESIDENCE").trim()))
	                {
	                	CheckGridDataMap.put("EXTERNAL_EXPOSURE_STATUS", "NA");
	                	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("For Non-Resident External Exposure call will not be called");
	                }
                	else if("Others".equalsIgnoreCase(objWorkList.getVal("RELATIONSHIPTYPE").trim()))
	                {
	                	CheckGridDataMap.put("EXTERNAL_EXPOSURE_STATUS", "NA");
	                	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("For Others Relationship Type External Exposure call will not be called");
	                }
                	else if(!"".equalsIgnoreCase(DateOfBirth) && DateOfBirth != null)
                	{
                		long diffInYears = findDifference(DateOfBirth);
                		if(diffInYears < 21) // less than 21 considered as minor
                		{
                			CheckGridDataMap.put("EXTERNAL_EXPOSURE_STATUS", "NA");
    	                	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("For Minors External Exposure call will not be called");
                		}
                	}
                	
              	}
                //////////////////////////////////////////////////////////
                
                if (!(CheckGridDataMap.get("EXTERNAL_EXPOSURE_STATUS").equalsIgnoreCase("Y")))
                {
                    String flag = callExternalExposure(processInstanceID, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap, InquiryPurpose, TxmAmount, ProposedTenor);
                    if("N".equalsIgnoreCase(flag))
                    {
                    	if(!"Failure".equalsIgnoreCase(MainStatusFlag))
                    	{
                    		MainStatusFlag = "Failure";
                    	}
                    }	
                }



              }
          }
        else
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WmgetWorkItem status: " + xmlParserData.getValueOf("MainCode"));
          }
        return MainStatusFlag;
      }

    private static String callExternalExposure(String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap, String InquiryPurpose, String TxmAmount, String ProposedTenor)
      throws IOException, Exception
      {
    	java.util.Date d1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
		String DateExtra2 = sdf1.format(d1)+"+04:00";
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
		String ReqDateTime = sdf2.format(d1);
		
		String flag = "";
        StringBuilder sInputXML = new StringBuilder();
        if (!"NA".equalsIgnoreCase(CheckGridDataMap.get("EXTERNAL_EXPOSURE_STATUS")))
        {
	        if ("EXT".equalsIgnoreCase(CheckGridDataMap.get("DATAFROM")))
	          {
	            String DBQuery = "SELECT TOP 1 CONCAT(ext.MOBILENUMBERCOUNTRYCODE,'',ext.MOBILENUMBER) AS 'MOBILE',ext.COMPANY_NAME, "
	            		+ "ext.TL_NUMBER,(SELECT top 1 AgreementId FROM USR_0_iRBL_InternalExpo_loanDetails with(nolock) WHERE Wi_Name='"+wiName+"' AND LoanStat='A') AS AgreementId, "
	            		+ "ISSUING_EMIRATE "
	            		+ "FROM RB_iRBL_EXTTABLE ext with(nolock) WHERE ext.WINAME='"+wiName+"' ";
	            
	            String CIFIDXml = "";
	            String CIFID = CheckGridDataMap.get("CIF").trim();
	            if(!"".equalsIgnoreCase(CIFID) && !"null".equalsIgnoreCase(CIFID) && CIFID != null)
	            	CIFIDXml = "<CIFId><CIFIdType>" + CheckGridDataMap.get("CIF_TYPE") + "</CIFIdType>" + "<CIFIdValue>"
	            			+ CIFID + "</CIFIdValue></CIFId>";
	            
	            String extTabDataIPXML =
	              CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
	            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
	            String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
	            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
	            XMLParser xmlParser = new XMLParser(extTabDataOPXML);
	            sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat>"
	              + "<MsgVersion>0001</MsgVersion><RequestorChannelId>BPM</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
	              + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>911</ReturnCode>"
	              + "<ReturnDesc>IssuerTimedOut</ReturnDesc><MessageId>CUSTOMER_EXPOSUER_0V27</MessageId><Extra1>REQ||SHELL.JOHN</Extra1>"
	              + "<Extra2>"+DateExtra2+"</Extra2></EE_EAI_HEADER><CustomerExposureRequest><BankId>RAK</BankId>"
	              + "<BranchId>RAK123</BranchId><RequestType>ExternalExposure</RequestType>"
	              + CIFIDXml+"<CustType>2</CustType><UserId>deepak</UserId>"
	              + "<AcctId>" + xmlParser.getValueOf("AgreementId") + "</AcctId>" + "<TxnAmount>" + TxmAmount + "</TxnAmount>"
	              + "<NoOfInstallments></NoOfInstallments><DurationOfAgreement>"+ProposedTenor+"</DurationOfAgreement><InquiryPurpose>"+InquiryPurpose+"</InquiryPurpose>" 
	              + "<ProviderApplNo>" + wiName.split("-")[1] + (new Date()).getTime() + "</ProviderApplNo><CBApplNo></CBApplNo>"
	              + "<IsCoApplicant>0</IsCoApplicant>"
	              + "<LosIndicator>1</LosIndicator><ContractType>1</ContractType><OverridePeriod>0</OverridePeriod>" 
	              + "<PrimaryMobileNo>" + xmlParser.getValueOf("MOBILE")
	              + "</PrimaryMobileNo><ConsentFlag>1</ConsentFlag><BureauCategory>Company</BureauCategory>" 
	              + "<BureauId>10</BureauId><CallType>Synchronous</CallType><TradeName>"
	              + xmlParser.getValueOf("COMPANY_NAME") + "</TradeName>" 
	              + "<TradeLicenseNumber>" + xmlParser.getValueOf("TL_NUMBER")
	              + "</TradeLicenseNumber><TradeLicensePlace>"+xmlParser.getValueOf("ISSUING_EMIRATE")+"</TradeLicensePlace>"
	              + "<ScoreType>0</ScoreType></CustomerExposureRequest>" 
	              + "</EE_EAI_MESSAGE>");
	            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML XML for ExternalExposure  " + sInputXML);
	          }
	        else
	          {
	            String BureauCategory = "Retail";
	            if ("Corporate_CIF".equalsIgnoreCase(CheckGridDataMap.get("CUSTOMER_TYPE").trim()))
	            		BureauCategory = "Company";
	            
	            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("BureauCategory: "+BureauCategory);
	            
	            String DBQuery = "SELECT NAME_OF_SISTER_COMPANY AS 'COMPANY_NAME',TL_NUMBER, "
	            		+ "CONCAT(RELMOBILENUMBERCOUNTRYCODE,'', RELMOBILENUMBER) AS 'MOBILE', "
	            		+ "(SELECT top 1 AgreementId FROM USR_0_iRBL_InternalExpo_loanDetails with(nolock) WHERE Wi_Name='"+wiName+"' AND LoanStat='A') AS AgreementId, "
	            		+ "ISSUINGEMIRATE, FIRSTNAME, MIDDLENAME, LASTNAME, EMIRATESID, VISANUMBER, PASSPORTNUMBER, isnull(format(DATEOFBIRTH,'yyyy-MM-dd'),'') as DATEOFBIRTH, "
	            		+ "GENDER, NATIONALITY "
	            		+ "FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS with(nolock) WHERE WI_NAME='"+wiName+"'  AND RELATEDPARTYID = '"+CheckGridDataMap.get("RELATEDPARTYID")+"'";
	            String extTabDataIPXML =
	              CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
	            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
	            String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
	            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
	            XMLParser xmlParser = new XMLParser(extTabDataOPXML);
	            
	            String CompanyDetails = "";
	            String CustType = "";
	            if("Company".equalsIgnoreCase(BureauCategory))
	            {
	            	CompanyDetails = "<TradeName>"+ xmlParser.getValueOf("COMPANY_NAME").trim() + "</TradeName>" ;
	            	
	            	if(!"".equalsIgnoreCase(xmlParser.getValueOf("TL_NUMBER").trim()))
	            		CompanyDetails = CompanyDetails + "<TradeLicenseNumber>" + xmlParser.getValueOf("TL_NUMBER").trim()+ "</TradeLicenseNumber>";
	            	
	            	if(!"".equalsIgnoreCase(xmlParser.getValueOf("ISSUINGEMIRATE").trim()))
	            		CompanyDetails = CompanyDetails + "<TradeLicensePlace>"+xmlParser.getValueOf("ISSUINGEMIRATE").trim()+"</TradeLicensePlace>";
	            
	            	CustType = "<CustType>2</CustType>";
	            }
	            
	            String RetailDetail = "";
	            String RetailDocDetails = "";
	            String Nationality = "";
	            if("Retail".equalsIgnoreCase(BureauCategory))
	            {
	            	String FullName = "";
	            	if(!"".equalsIgnoreCase(xmlParser.getValueOf("MIDDLENAME").trim()))
	            		FullName = xmlParser.getValueOf("FIRSTNAME").trim()+" "+xmlParser.getValueOf("MIDDLENAME").trim()+" "+xmlParser.getValueOf("LASTNAME").trim();
	            	else
	            		FullName = xmlParser.getValueOf("FIRSTNAME").trim()+" "+xmlParser.getValueOf("LASTNAME").trim();
	            	
	            	if("".equalsIgnoreCase(FullName.trim()))
	            		FullName = xmlParser.getValueOf("COMPANY_NAME").trim();
	            	
	            	if(!"".equalsIgnoreCase(FullName.trim()))
	            		RetailDetail = RetailDetail + "<FullNm>" + FullName + "</FullNm>";
	            	
	            	if(!"".equalsIgnoreCase(xmlParser.getValueOf("DATEOFBIRTH").trim()))
	            		RetailDetail = RetailDetail + "<BirthDt>"+xmlParser.getValueOf("DATEOFBIRTH").trim()+"</BirthDt>";
	            	
	            	if(!"".equalsIgnoreCase(xmlParser.getValueOf("GENDER").trim()))
	            	{
	            		String Gender = xmlParser.getValueOf("GENDER").trim();
	            		if("F".equalsIgnoreCase(Gender) || "Female".equalsIgnoreCase(Gender))
	            			Gender = "1";
	            		else if("M".equalsIgnoreCase(Gender) || "Male".equalsIgnoreCase(Gender))
	            			Gender = "2";
	            		else 
	            			Gender = "3";
	            		RetailDetail = RetailDetail + "<Gender>"+Gender+"</Gender>";
	            	}
	            	
	            	if(!"".equalsIgnoreCase(xmlParser.getValueOf("EMIRATESID").trim()))
	            	{
	            		RetailDocDetails = RetailDocDetails 
	            				+ "<LegalDocInfo>"
	            					+"<DocType>Emirates id</DocType>"
	            					+"<DocNum>"+xmlParser.getValueOf("EMIRATESID").trim()+"</DocNum>"
				            	+"</LegalDocInfo>";
	            	}
	            	if(!"".equalsIgnoreCase(xmlParser.getValueOf("PASSPORTNUMBER").trim()))
	            	{
	            		RetailDocDetails = RetailDocDetails 
	            				+ "<LegalDocInfo>"
	            					+"<DocType>Passport Number</DocType>"
	            					+"<DocNum>"+xmlParser.getValueOf("PASSPORTNUMBER").trim()+"</DocNum>"
				            	+"</LegalDocInfo>";
	            		
	            		Nationality = "<Nationality>"+xmlParser.getValueOf("NATIONALITY").trim()+"</Nationality>";
	            	}
	            	
	            	CustType = "<CustType>1</CustType>";
	            }
	            
	            String CIFIDXml = "";
	            String CIFID = CheckGridDataMap.get("CIF").trim();
	            if(!"".equalsIgnoreCase(CIFID) && !"null".equalsIgnoreCase(CIFID) && CIFID != null)
	            	CIFIDXml = "<CIFId><CIFIdType>" + CheckGridDataMap.get("CIF_TYPE").trim() + "</CIFIdType>" + "<CIFIdValue>"
	            			+ CIFID + "</CIFIdValue></CIFId>";
	
	            sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat>"
	              + "<MsgVersion>0001</MsgVersion><RequestorChannelId>BPM</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
	              + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>911</ReturnCode>"
	              + "<ReturnDesc>IssuerTimedOut</ReturnDesc><MessageId>CUSTOMER_EXPOSUER_0V27</MessageId><Extra1>REQ||SHELL.JOHN</Extra1>"
	              + "<Extra2>"+DateExtra2+"</Extra2></EE_EAI_HEADER><CustomerExposureRequest><BankId>RAK</BankId>"
	              + "<BranchId>RAK123</BranchId><RequestType>ExternalExposure</RequestType>"
	              + CIFIDXml+CustType+"<UserId>deepak</UserId>"
	              + "<AcctId>" + xmlParser.getValueOf("AgreementId") + "</AcctId>"
	              + "<TxnAmount>"+TxmAmount+"</TxnAmount><NoOfInstallments></NoOfInstallments>"
	              + "<DurationOfAgreement>"+ProposedTenor+"</DurationOfAgreement>"+RetailDetail
	              + Nationality+"<InquiryPurpose>"+InquiryPurpose+"</InquiryPurpose>" 
	              + "<ProviderApplNo>" + wiName.split("-")[1] + (new Date()).getTime() + "</ProviderApplNo><CBApplNo></CBApplNo>"
	              + "<IsCoApplicant>0</IsCoApplicant>"
	              + "<LosIndicator>1</LosIndicator><ContractType>1</ContractType><OverridePeriod>0</OverridePeriod>" 
	              + "<PrimaryMobileNo>" + xmlParser.getValueOf("MOBILE")
	              + "</PrimaryMobileNo><ConsentFlag>1</ConsentFlag><BureauCategory>" + BureauCategory + "</BureauCategory>" 
	              + "<BureauId>10</BureauId><CallType>Synchronous</CallType>"
	              + CompanyDetails+"<ScoreType>0</ScoreType>"+RetailDocDetails+"</CustomerExposureRequest>" 
	              + "</EE_EAI_MESSAGE>");
	            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML XML for ExternalExposure  " + sInputXML);
	          }
	
	        //need to get mapping -CustType-AcctId-TxnAmount-TradeLicensePlace
	
	
	        //for dummy
	        //String responseXML ="<EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat><MsgVersion>0001</MsgVersion><RequestorChannelId>BPM</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS161285778725510</MessageId><Extra1>REP||SHELL.JOHN</Extra1><Extra2>2021-02-09T12:03:53.264+04:00</Extra2></EE_EAI_HEADER><CustomerExposureResponse><RequestType>ExternalExposure</RequestType><ReferenceNumber>311019</ReferenceNumber><ReportUrl>https://ant2a2aapps01.rakbanktst.ae:446/GetPdf.aspx?refno=p48EJaR29vQ%3d</ReportUrl><IsDirect>N</IsDirect><CustInfo><FullNm>MINERAL CHASM MINING GROUP</FullNm><Activity>0</Activity><TotalOutstanding>18165987.00</TotalOutstanding><TotalOverdue>39989.00</TotalOverdue><NoOfContracts>0</NoOfContracts><CustInfoListDet><ReferenceNumber>311019</ReferenceNumber><InfoType>NameENInfo</InfoType><CustName>MINERAL CHASM MINING GROUP</CustName><CustNameType>CompanyTradeName</CustNameType><ActualFlag>true</ActualFlag><ProviderNo>B01</ProviderNo><CreatedOn>2021-02-09</CreatedOn><DateOfUpdate>2020-11-10</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>311019</ReferenceNumber><InfoType>NameENInfo</InfoType><CustName>MINERAL CHASM MINING GROUP</CustName><CustNameType>CompanyTradeName</CustNameType><ActualFlag>true</ActualFlag><ProviderNo>C02</ProviderNo><CreatedOn>2021-02-09</CreatedOn><DateOfUpdate>2020-08-27</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>311019</ReferenceNumber><InfoType>EconomicActivityHistorylst</InfoType><ActualFlag>true</ActualFlag><ProviderNo>C02</ProviderNo><EconomicActivity>100</EconomicActivity><CreatedOn>2021-02-09</CreatedOn><DateOfUpdate>2020-08-27</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>311019</ReferenceNumber><InfoType>TradeLicenseHistorylst</InfoType><ActualFlag>true</ActualFlag><ProviderNo>C02</ProviderNo><RegistrationPlace>3</RegistrationPlace><LicenseNumber>DED9209068</LicenseNumber><CreatedOn>2021-02-09</CreatedOn><DateOfUpdate>2020-08-27</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>311019</ReferenceNumber><InfoType>TradeLicenseHistorylst</InfoType><ActualFlag>true</ActualFlag><ProviderNo>C02</ProviderNo><RegistrationPlace>3</RegistrationPlace><LicenseNumber>DED9209068</LicenseNumber><CreatedOn>2021-02-09</CreatedOn><DateOfUpdate>2020-08-27</DateOfUpdate></CustInfoListDet><PhoneInfo><ReportedDate>2020-08-27</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2020-08-27</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2020-11-10</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2020-11-10</ReportedDate></PhoneInfo><InquiryInfo><ContractCategory>C</ContractCategory></InquiryInfo></CustInfo><ScoreInfo><Value>554</Value><Range>C</Range></ScoreInfo><AddrInfo><EnrichedThroughEnquiry>1</EnrichedThroughEnquiry></AddrInfo><RecordDestributions><RecordDestribution><ContractType>TotalSummary</ContractType><Contract_Role_Type></Contract_Role_Type><TotalNo>4</TotalNo><DataProvidersNo>2</DataProvidersNo><RequestNo>1</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>3</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>Installments</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>1</TotalNo><DataProvidersNo>1</DataProvidersNo><RequestNo>0</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>1</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>NotInstallments</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>1</TotalNo><DataProvidersNo>1</DataProvidersNo><RequestNo>0</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>1</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>CreditCards</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>2</TotalNo><DataProvidersNo>1</DataProvidersNo><RequestNo>1</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>1</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>Services</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>0</TotalNo><DataProvidersNo>0</DataProvidersNo><RequestNo>0</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>0</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution></RecordDestributions><Derived><Total_Exposure>15103297</Total_Exposure><Oldest_Contract_Start_Date>01-05-2018</Oldest_Contract_Start_Date><WorstCurrentPaymentDelay>0</WorstCurrentPaymentDelay><Worst_PaymentDelay_Last24Months>180</Worst_PaymentDelay_Last24Months><Worst_Status_Last24Months>U</Worst_Status_Last24Months><Nof_Records>3</Nof_Records><NoOf_Cheque_Return_Last3>0</NoOf_Cheque_Return_Last3><Nof_DDES_Return_Last3Months>0</Nof_DDES_Return_Last3Months><Nof_DDES_Return_Last6Months>0</Nof_DDES_Return_Last6Months><Nof_Cheque_Return_Last6>0</Nof_Cheque_Return_Last6><Nof_Enq_Last90Days>0</Nof_Enq_Last90Days><Nof_Enq_Last60Days>0</Nof_Enq_Last60Days><Nof_Enq_Last30Days>0</Nof_Enq_Last30Days><TotOverDue_GuarteContrct>0</TotOverDue_GuarteContrct></Derived><ProductExposureDetails><ChequeDetails><ChqType>Bounced Cheques</ChqType><Number>46122</Number><Amount>9000</Amount><ReturnDate>2020-09-20</ReturnDate><ProviderNo>C02</ProviderNo><ReasonCode>Insufficient Funds</ReasonCode><Severity>Single</Severity></ChequeDetails><ChequeDetails><ChqType>Bounced Cheques</ChqType><Number>36121</Number><Amount>9000</Amount><ReturnDate>2020-10-20</ReturnDate><ProviderNo>C02</ProviderNo><ReasonCode>Insufficient Funds</ReasonCode><Severity>Single</Severity></ChequeDetails><ChequeDetails><ChqType>Bounced Cheques</ChqType><Number>15123</Number><Amount>9000</Amount><ReturnDate>2020-08-20</ReturnDate><ProviderNo>C02</ProviderNo><ReasonCode>Insufficient Funds</ReasonCode><Severity>Single</Severity></ChequeDetails><LoanDetails><AgreementId>G05505588</AgreementId><LoanStat>Active</LoanStat><LoanType>03</LoanType><LoanDesc>Installments</LoanDesc><CustRoleType>Main Contract Holder</CustRoleType><TotalNoOfInstalments>24</TotalNoOfInstalments><ProviderNo>C02</ProviderNo><RemainingInstalments>7</RemainingInstalments><KeyDt><KeyDtType>LoanApprovedDate</KeyDtType><KeyDtValue>2019-02-01</KeyDtValue></KeyDt><KeyDt><KeyDtType>LoanMaturityDate</KeyDtType><KeyDtValue>2021-02-01</KeyDtValue></KeyDt><KeyDt><KeyDtType>MaxOverdueAmountDate</KeyDtType><KeyDtValue>2020-07-31</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>OutstandingAmt</AmtType><Amt>15098296</Amt></AmountDtls><AmountDtls><AmtType>TotalAmt</AmtType><Amt>1</Amt></AmountDtls><AmountDtls><AmtType>PaymentsAmt</AmtType><Amt>854</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmt</AmtType><Amt>33095</Amt></AmountDtls><WriteoffStat>U</WriteoffStat><WriteoffStatDt>2020-07-31</WriteoffStatDt><NofDaysPmtDelay>120</NofDaysPmtDelay><MaxDaysPmtDelay>0</MaxDaysPmtDelay><MaxDaysPmtDelayDt>2020-07-31</MaxDaysPmtDelayDt><MonthsOnBook>25</MonthsOnBook><LastRepmtDt>2020-07-01</LastRepmtDt><IsDuplicate>0</IsDuplicate><NonActivePmt>0</NonActivePmt><IsCurrent>0</IsCurrent><CurUtilRate>0</CurUtilRate><AECBHistMonthCnt>18</AECBHistMonthCnt><DPD5_Last3Months>0</DPD5_Last3Months><DPD30_Last6Months>0</DPD30_Last6Months><DPD60_Last18Months>5</DPD60_Last18Months><DPD60Plus_Last12Months>5</DPD60Plus_Last12Months><DPD5_Last12Months>6</DPD5_Last12Months><MaximumOverDueAmount>0</MaximumOverDueAmount><History><Key>07-2020</Key><Status>120</Status></History><History><Key>06-2020</Key><Status>90</Status></History><History><Key>05-2020</Key><Status>120</Status></History><History><Key>04-2020</Key><Status>90</Status></History><History><Key>03-2020</Key><Status>120</Status></History><History><Key>02-2020</Key><Status>5</Status></History><History><Key>01-2020</Key><Status>N/A</Status></History><History><Key>12-2019</Key><Status>N/A</Status></History><History><Key>11-2019</Key><Status>N/A</Status></History><History><Key>10-2019</Key><Status>N/A</Status></History><History><Key>09-2019</Key><Status>N/A</Status></History><History><Key>08-2019</Key><Status>N/A</Status></History><History><Key>07-2019</Key><Status>N/A</Status></History><History><Key>06-2019</Key><Status>N/A</Status></History><History><Key>05-2019</Key><Status>N/A</Status></History><History><Key>04-2019</Key><Status>N/A</Status></History><History><Key>03-2019</Key><Status>N/A</Status></History><History><Key>02-2019</Key><Status>OK</Status></History><History><Key>01-2019</Key><Status>N/A</Status></History><History><Key>12-2018</Key><Status>N/A</Status></History><History><Key>11-2018</Key><Status>N/A</Status></History><History><Key>10-2018</Key><Status>N/A</Status></History><History><Key>09-2018</Key><Status>N/A</Status></History><History><Key>08-2018</Key><Status>N/A</Status></History></LoanDetails><LoanDetails><AgreementId>K05459563</AgreementId><LoanStat>Active</LoanStat><LoanType>23</LoanType><LoanDesc>Installments</LoanDesc><CustRoleType>Main Contract Holder</CustRoleType><TotalNoOfInstalments>36</TotalNoOfInstalments><ProviderNo>C02</ProviderNo><RemainingInstalments>35</RemainingInstalments><KeyDt><KeyDtType>LoanApprovedDate</KeyDtType><KeyDtValue>2016-04-01</KeyDtValue></KeyDt><KeyDt><KeyDtType>LoanMaturityDate</KeyDtType><KeyDtValue>2016-04-30</KeyDtValue></KeyDt><KeyDt><KeyDtType>MaxOverdueAmountDate</KeyDtType><KeyDtValue>2016-04-30</KeyDtValue></KeyDt><CurCode>USD</CurCode><AmountDtls><AmtType>OutstandingAmt</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>TotalAmt</AmtType><Amt>1802</Amt></AmountDtls><AmountDtls><AmtType>PaymentsAmt</AmtType><Amt>5000</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmt</AmtType><Amt>0</Amt></AmountDtls><PaymentMode>Bank draft; Automated bank draft</PaymentMode><WriteoffStat>U</WriteoffStat><WriteoffStatDt>2016-04-30</WriteoffStatDt><NofDaysPmtDelay>0</NofDaysPmtDelay><MaxDaysPmtDelay>0</MaxDaysPmtDelay><MaxDaysPmtDelayDt>2016-04-30</MaxDaysPmtDelayDt><MonthsOnBook>47.00</MonthsOnBook><LastRepmtDt>2016-04-01</LastRepmtDt><IsDuplicate>0</IsDuplicate><NonActivePmt>0</NonActivePmt><IsCurrent>1</IsCurrent><CurUtilRate>0</CurUtilRate><AECBHistMonthCnt>0</AECBHistMonthCnt><DPD5_Last3Months>0</DPD5_Last3Months><DPD30_Last6Months>0</DPD30_Last6Months><DPD60_Last18Months>0</DPD60_Last18Months><DPD60Plus_Last12Months>0</DPD60Plus_Last12Months><DPD5_Last12Months>0</DPD5_Last12Months><MaximumOverDueAmount>0</MaximumOverDueAmount><History><Key>04-2016</Key><Status>OK</Status></History><History><Key>03-2016</Key><Status>N/A</Status></History><History><Key>02-2016</Key><Status>N/A</Status></History><History><Key>01-2016</Key><Status>N/A</Status></History><History><Key>12-2015</Key><Status>N/A</Status></History><History><Key>11-2015</Key><Status>N/A</Status></History><History><Key>10-2015</Key><Status>N/A</Status></History><History><Key>09-2015</Key><Status>N/A</Status></History><History><Key>08-2015</Key><Status>N/A</Status></History><History><Key>07-2015</Key><Status>N/A</Status></History><History><Key>06-2015</Key><Status>N/A</Status></History><History><Key>05-2015</Key><Status>N/A</Status></History><History><Key>04-2015</Key><Status>N/A</Status></History><History><Key>03-2015</Key><Status>N/A</Status></History><History><Key>02-2015</Key><Status>N/A</Status></History><History><Key>01-2015</Key><Status>N/A</Status></History><History><Key>12-2014</Key><Status>N/A</Status></History><History><Key>11-2014</Key><Status>N/A</Status></History><History><Key>10-2014</Key><Status>N/A</Status></History><History><Key>09-2014</Key><Status>N/A</Status></History><History><Key>08-2014</Key><Status>N/A</Status></History><History><Key>07-2014</Key><Status>N/A</Status></History><History><Key>06-2014</Key><Status>N/A</Status></History><History><Key>05-2014</Key><Status>N/A</Status></History></LoanDetails><LoanDetails><AgreementId>I05459699</AgreementId><LoanStat>Pipeline</LoanStat><LoanType>03</LoanType><LoanDesc>Requested</LoanDesc><CustRoleType>Main Contract Holder</CustRoleType><TotalNoOfInstalments>12</TotalNoOfInstalments><ProviderNo>B03</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2016-11-09</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>TotalAmt</AmtType><Amt>10000</Amt></AmountDtls><AmountDtls><AmtType>CreditLimit</AmtType><Amt>0</Amt></AmountDtls><NoOfDaysInPipeline>1210</NoOfDaysInPipeline></LoanDetails><LoanDetails><AgreementId>D05459742</AgreementId><LoanStat>Pipeline</LoanStat><LoanType>03</LoanType><LoanDesc>Requested</LoanDesc><CustRoleType>Main Contract Holder</CustRoleType><TotalNoOfInstalments>12</TotalNoOfInstalments><ProviderNo>B03</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2016-11-09</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>TotalAmt</AmtType><Amt>10000</Amt></AmountDtls><AmountDtls><AmtType>CreditLimit</AmtType><Amt>0</Amt></AmountDtls><NoOfDaysInPipeline>1210</NoOfDaysInPipeline></LoanDetails><LoanDetails><AgreementId>K05459760</AgreementId><LoanStat>Pipeline</LoanStat><LoanType>03</LoanType><LoanDesc>Requested</LoanDesc><CustRoleType>Main Contract Holder</CustRoleType><TotalNoOfInstalments>12</TotalNoOfInstalments><ProviderNo>B03</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2016-11-09</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>TotalAmt</AmtType><Amt>1000</Amt></AmountDtls><AmountDtls><AmtType>CreditLimit</AmtType><Amt>0</Amt></AmountDtls><NoOfDaysInPipeline>1210</NoOfDaysInPipeline></LoanDetails><LoanDetails><AgreementId>I05460568</AgreementId><LoanStat>Pipeline</LoanStat><LoanType>15</LoanType><LoanDesc>Requested</LoanDesc><CustRoleType>Main Contract Holder</CustRoleType><TotalNoOfInstalments>12</TotalNoOfInstalments><ProviderNo>B03</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2017-04-30</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>TotalAmt</AmtType><Amt>546646</Amt></AmountDtls><AmountDtls><AmtType>CreditLimit</AmtType><Amt>0</Amt></AmountDtls><NoOfDaysInPipeline>1038</NoOfDaysInPipeline></LoanDetails><CardDetails><CardEmbossNum>B05504976</CardEmbossNum><CardStatus>Active</CardStatus><CardType>01</CardType><CardTypeDesc>01</CardTypeDesc><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>C02</ProviderNo><KeyDt><KeyDtType>StartDate</KeyDtType><KeyDtValue>2018-05-01</KeyDtValue></KeyDt><KeyDt><KeyDtType>MaxOverDueAmountDate</KeyDtType><KeyDtValue>2020-08-31</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>CurrentBalance</AmtType><Amt>2600</Amt></AmountDtls><AmountDtls><AmtType>TotalAmount</AmtType><Amt>1</Amt></AmountDtls><AmountDtls><AmtType>PaymentsAmount</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>CashLimit</AmtType><Amt>1</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmount</AmtType><Amt>0</Amt></AmountDtls><WriteoffStat>U</WriteoffStat><WriteoffStatDt>2020-08-31</WriteoffStatDt><NofDaysPmtDelay>0</NofDaysPmtDelay><MaxDaysPmtDelay>0</MaxDaysPmtDelay><MaxDaysPmtDelayDt>2020-08-31</MaxDaysPmtDelayDt><MonthsOnBook>34</MonthsOnBook><LastRepmtDt>2020-08-01</LastRepmtDt><IsDuplicate>0</IsDuplicate><NonActivePmt>0</NonActivePmt><IsCurrent>1</IsCurrent><CurUtilRate>260000</CurUtilRate><AECBHistMonthCnt>14</AECBHistMonthCnt><DPD5_Last3Months>0</DPD5_Last3Months><DPD30_Last6Months>0</DPD30_Last6Months><DPD60_Last18Months>2</DPD60_Last18Months><DPD60Plus_Last12Months>2</DPD60Plus_Last12Months><DPD5_Last12Months>6</DPD5_Last12Months><MaximumOverDueAmount>0</MaximumOverDueAmount><Utilizations24Months><Month>08-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>2600</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>07-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>960</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>06-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>960</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>05-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>450</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>04-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>450</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>03-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>960</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>02-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>450</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>01-2020</Month><CreditLimit>1</CreditLimit><OutstandingBalance>450</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>12-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>11-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>10-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>09-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>08-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>07-2019</Month><CreditLimit>1</CreditLimit><OutstandingBalance>960</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>06-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>05-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>04-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>03-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>02-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>01-2019</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>12-2018</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>11-2018</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>10-2018</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>09-2018</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><History><Key>08-2020</Key><Status>OK</Status></History><History><Key>07-2020</Key><Status>30</Status></History><History><Key>06-2020</Key><Status>30</Status></History><History><Key>05-2020</Key><Status>30</Status></History><History><Key>04-2020</Key><Status>180+</Status></History><History><Key>03-2020</Key><Status>30</Status></History><History><Key>02-2020</Key><Status>60</Status></History><History><Key>01-2020</Key><Status>OK</Status></History><History><Key>12-2019</Key><Status>N/A</Status></History><History><Key>11-2019</Key><Status>N/A</Status></History><History><Key>10-2019</Key><Status>N/A</Status></History><History><Key>09-2019</Key><Status>N/A</Status></History><History><Key>08-2019</Key><Status>N/A</Status></History><History><Key>07-2019</Key><Status>30</Status></History><History><Key>06-2019</Key><Status>N/A</Status></History><History><Key>05-2019</Key><Status>N/A</Status></History><History><Key>04-2019</Key><Status>N/A</Status></History><History><Key>03-2019</Key><Status>N/A</Status></History><History><Key>02-2019</Key><Status>N/A</Status></History><History><Key>01-2019</Key><Status>N/A</Status></History><History><Key>12-2018</Key><Status>N/A</Status></History><History><Key>11-2018</Key><Status>N/A</Status></History><History><Key>10-2018</Key><Status>N/A</Status></History><History><Key>09-2018</Key><Status>N/A</Status></History></CardDetails><CardDetails><CardEmbossNum>H05505636</CardEmbossNum><CardStatus>Pipeline</CardStatus><CardType>00</CardType><CardTypeDesc>Requested</CardTypeDesc><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>B01</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2020-11-10</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>TotalAmount</AmtType><Amt>5000</Amt></AmountDtls><AmountDtls><AmtType>CashLimit</AmtType><Amt>5000</Amt></AmountDtls><DelinquencyInfo><BucketType>NoOfInstallments</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>NoOfDaysInPipeLine</BucketType><BucketValue>91</BucketValue></DelinquencyInfo></CardDetails><AcctDetails><AcctId>J05506034</AcctId><IBANNumber></IBANNumber><AcctStat>Active</AcctStat><AcctCur>AED</AcctCur><AcctNm>MINERAL CHASM MINING GROUP</AcctNm><AcctType>58</AcctType><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>C02</ProviderNo><KeyDt><KeyDtType>StartDate</KeyDtType><KeyDtValue>2019-04-01</KeyDtValue></KeyDt><KeyDt><KeyDtType>MaxOverDueAmountDate</KeyDtType><KeyDtValue>2020-07-31</KeyDtValue></KeyDt><AmountDtls><AmtType>OutStandingBalance</AmtType><Amt>3065091</Amt></AmountDtls><AmountDtls><AmtType>TotalAmount</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>PaymentsAmount</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>CreditLimit</AmtType><Amt>5000</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmount</AmtType><Amt>6894</Amt></AmountDtls><WriteoffStat>U</WriteoffStat><WriteoffStatDt>2020-07-31</WriteoffStatDt><NofDaysPmtDelay>120</NofDaysPmtDelay><MaxDaysPmtDelay>0</MaxDaysPmtDelay><MaxDaysPmtDelayDt>2020-07-31</MaxDaysPmtDelayDt><MonthsOnBook>23</MonthsOnBook><LastRepmtDt>2020-07-01</LastRepmtDt><IsDuplicate>0</IsDuplicate><NonActivePmt>0</NonActivePmt><IsCurrent>0</IsCurrent><CurUtilRate>61301.82</CurUtilRate><AECBHistMonthCnt>24</AECBHistMonthCnt><DPD5_Last3Months>0</DPD5_Last3Months><DPD30_Last6Months>0</DPD30_Last6Months><DPD60_Last18Months>1</DPD60_Last18Months><DPD60Plus_Last12Months>1</DPD60Plus_Last12Months><DPD5_Last12Months>1</DPD5_Last12Months><MaximumOverDueAmount>0</MaximumOverDueAmount><History><Key>07-2020</Key><Status>90%</Status></History><History><Key>06-2020</Key><Status>N/A</Status></History><History><Key>05-2020</Key><Status>N/A</Status></History><History><Key>04-2020</Key><Status>N/A</Status></History><History><Key>03-2020</Key><Status>N/A</Status></History><History><Key>02-2020</Key><Status>N/A</Status></History><History><Key>01-2020</Key><Status>N/A</Status></History><History><Key>12-2019</Key><Status>N/A</Status></History><History><Key>11-2019</Key><Status>N/A</Status></History><History><Key>10-2019</Key><Status>90%</Status></History><History><Key>09-2019</Key><Status>90%</Status></History><History><Key>08-2019</Key><Status>90%</Status></History><History><Key>07-2019</Key><Status>90%</Status></History><History><Key>06-2019</Key><Status>90%</Status></History><History><Key>05-2019</Key><Status>90%</Status></History><History><Key>04-2019</Key><Status>90%</Status></History><History><Key>03-2019</Key><Status>N/A</Status></History><History><Key>02-2019</Key><Status>N/A</Status></History><History><Key>01-2019</Key><Status>N/A</Status></History><History><Key>12-2018</Key><Status>N/A</Status></History><History><Key>11-2018</Key><Status>N/A</Status></History><History><Key>10-2018</Key><Status>N/A</Status></History><History><Key>09-2018</Key><Status>N/A</Status></History><History><Key>08-2018</Key><Status>N/A</Status></History></AcctDetails></ProductExposureDetails></CustomerExposureResponse></EE_EAI_MESSAGE>";
	        
	        // need to uncomment
	        String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "Sys_Checks_Integration", socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
	        
	        
	        //Inserting in Integration details table
	        XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc").replace("'", "");
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description").replace("'", "");
			
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
		    String CallStatus = "";
		    if(return_code.equals("0000") || return_code.equals("B003") || return_code.equals("B005"))
		    	CallStatus="Success";
		    else
		    	CallStatus="Failure";
		    java.util.Date d2 = new Date();
		    String ResDateTime = sdf2.format(d2);
		    String TableName = "USR_0_IRBL_INTEGRATION_DTLS";
		    String columnnames="WI_NAME, CIFID, AccountNumber, CallName, Operation, RequestDateTime, CallStatus, MessageId, ResponseDateTime, ReturnCode, ReturnError";
		    String columnvalues="'"+wiName+"','"+CheckGridDataMap.get("CIF")+"','','CUSTOMER_EXPOSURE','ExternalExposure','"+ReqDateTime+"','"+CallStatus+"','"+MsgId+"','"+ResDateTime+"','"+return_code+"','"+return_desc+"' ";
			String InputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnnames, columnvalues, TableName);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert CUSTOMER_EXPOSURE(ExternalExposure) "+TableName+" Table : "+InputXML);
	
			String OutputXML=CommonMethods.WFNGExecute(InputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for CUSTOMER_EXPOSURE(ExternalExposure) apInsert "+TableName+" Table : "+OutputXML);
	
			XMLParser sXMLParserChild= new XMLParser(OutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    if (StrMainCode.equals("0"))
			   	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+TableName);	
		    else
		       	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+OutputXML);
		    ////////////////////////////////////////
	        
	        
	        
	        flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
	          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
	          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
	        flag = flag == "true" ? "Y" : "N";
        }
        else
        	flag = "NA";
        
        String tableName = "";
        String columnNames = "";
        String columnValues = "";
        String sWhereClause = "";
        if ("EXT".equalsIgnoreCase(CheckGridDataMap.get("DATAFROM")))
          {
            tableName = "RB_iRBL_EXTTABLE";
            columnNames = "EXTERNAL_EXPOSURE_STATUS";
            columnValues = "'" + flag + "'";
            sWhereClause = "WINAME='" + wiName + "' AND CIF_NUMBER='" + CheckGridDataMap.get("CIF") + "'";
          }
        else
          {
            tableName = "USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS";
            columnNames = "EXTERNAL_EXPOSURE_STATUS";
            columnValues = "'" + flag + "'";
            sWhereClause = "WI_NAME='" + wiName + "' AND RELATEDPARTYID='" + CheckGridDataMap.get("RELATEDPARTYID") + "'";
          }
        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), tableName,
          columnNames, columnValues, sWhereClause);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
        String outputXml = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
        XMLParser sXMLParserChild = new XMLParser(outputXml);
        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
        String RetStatus = "";
        if (StrMainCode.equals("0"))
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in apUpdateInput the record in : " + tableName);
            RetStatus = "Success in apUpdateInput the record";
          }
        else
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apUpdateInput sOutputXML : " + outputXml);
            RetStatus = "Error in Executing apUpdateInput";
          }
        return flag;
      }

    private static long findDifference(String strDate) // method called to get year difference based on date of birth to identify minor customer
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        long difference_In_Years = 0;
        try {
            Date d1 = null;
            try {
                d1 = sdf.parse(strDate);
            } catch (java.text.ParseException e) {
            	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception in parsing dates: "+e.getMessage());
                e.printStackTrace();
            }
            Date d2 = new Date();
            long difference_In_Time = d2.getTime() - d1.getTime();
            long difference_In_Seconds = (difference_In_Time / 1000) % 60;
            long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
            long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;
            difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
    		
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Difference between two dates is:");
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(difference_In_Years
                + " years, "
                + difference_In_Days
                + " days, "
                + difference_In_Hours
                + " hours, "
                + difference_In_Minutes
                + " minutes, "
                + difference_In_Seconds
                + " seconds");		
    	}
        catch (Exception e) {
        	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception in main parsing dates: "+e.getMessage());
            e.printStackTrace();
        }
        return difference_In_Years;
    }
    
  }
