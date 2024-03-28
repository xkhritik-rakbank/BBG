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

public class IntegrateInternalExposureCollectionsSummary
  {

    static ResponseBean objRespBean = new ResponseBean();

    public static ResponseBean IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {
        String DBQuery =
          "SELECT CIF_NUMBER AS 'CIF','Corporate_CIF'  AS 'CUSTOMER_TYPE',NEW_TOPUP AS 'Product',FINANCE_TYPE AS 'SubProduct','Primary' AS 'CIF_TYPE' FROM RB_iRBL_EXTTABLE WHERE WINAME= '"
            + processInstanceID + "'"
            + "UNION SELECT CIF AS 'CIF', CASE WHEN COMPANYFLAG='Y' THEN 'Corporate_CIF' WHEN COMPANYFLAG='YES' THEN 'Corporate_CIF' ELSE 'Individual_CIF' END  AS 'CUSTOMER_TYPE', '' AS 'Product','' AS 'SubProduct','Secondary' AS 'CIF_TYPE' FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WHERE WI_NAME='"
            + processInstanceID + "' ORDER BY CIF_TYPE";
        //Need to add integration check

        String extTabDataIPXML =
          CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        //CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
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

            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
              {
                CheckGridDataMap.put("CIF", objWorkList.getVal("CIF"));
                CheckGridDataMap.put("CUSTOMER_TYPE", objWorkList.getVal("CUSTOMER_TYPE"));
                CheckGridDataMap.put("Product", objWorkList.getVal("Product"));
                CheckGridDataMap.put("SubProduct", objWorkList.getVal("SubProduct"));
                CheckGridDataMap.put("CIF_TYPE", objWorkList.getVal("CIF_TYPE"));
                //Flag Check Required
                String interalStatus = callInternalExposure(processInstanceID, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                String collectionStatus = callCollectionSummary(processInstanceID, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);


               
              }
          }
        else
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WmgetWorkItem failed: " + objRespBean.getWorkItemMainCode());
          }
        return objRespBean;
      }

    private static String callInternalExposure(String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap)
      throws IOException, Exception
      {
        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat><MsgVersion>0001</MsgVersion><RequestorChannelId>CAS</RequestorChannelId>"
          + "<RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo>"
          + "<ReturnCode>911</ReturnCode><ReturnDesc>IssuerTimedOut</ReturnDesc><MessageId>CUSTOMER_EXPOSUER_0V27</MessageId><Extra1>REQ||SHELL.JOHN</Extra1>"
          + "<Extra2>YYYY-MM-DDThh:mm:ss.mmm+hh:mm</Extra2></EE_EAI_HEADER><CustomerExposureRequest><BankId>RAK</BankId><BranchId>RAK123</BranchId>"
          + "<RequestType>InternalExposure</RequestType><CIFId><CIFIdType>" + CheckGridDataMap.get("CIF_TYPE") + "</CIFIdType><CIFIdValue>" + CheckGridDataMap.get("CIF")
          + "</CIFIdValue></CIFId></CustomerExposureRequest>" + "</EE_EAI_MESSAGE>");

        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request  XML for InternalExposure  " + sInputXML);
        //for dummy
        String responseXML =
          "<EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat><MsgVersion>0001</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS162030941331678</MessageId><Extra1>REP||SHELL.JOHN</Extra1><Extra2>2021-05-06T05:57:19.449+04:00</Extra2></EE_EAI_HEADER><CustomerExposureResponse><RequestType>InternalExposure</RequestType><IsDirect>Y</IsDirect><CustInfo><CustId><CustIdType>CIF Id</CustIdType><CustIdValue>2022403</CustIdValue></CustId><FullNm></FullNm><BirthDt>1988-01-01</BirthDt><Nationality>INDIAN</Nationality><CustSegment>PERSONAL BANKING</CustSegment><CustSubSegment>PB - WEALTH MGT</CustSubSegment><RMName>PERSONAL BANKER</RMName><CreditGrade>P2 - PERSONAL - ACCEPTABLE CREDIT</CreditGrade><ECRN>068362600</ECRN><BorrowingCustomer>Y</BorrowingCustomer></CustInfo><ProductExposureDetails><LoanDetails><AgreementId>20499092</AgreementId><LoanStat>A</LoanStat><LoanType>PL</LoanType><LoanDesc></LoanDesc><TotalNoOfInstalments>49</TotalNoOfInstalments><KeyDt><KeyDtType>LoanApprovedDate</KeyDtType><KeyDtValue>2019-07-26</KeyDtValue></KeyDt><KeyDt><KeyDtType>LoanMaturityDate</KeyDtType><KeyDtValue>2023-08-01</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>TotalLoanAmount</AmtType><Amt>85000</Amt></AmountDtls><AmountDtls><AmtType>TotalOutstandingAmt</AmtType><Amt>58578.83</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmt</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>NextInstallmentAmt</AmtType><Amt>2402</Amt></AmountDtls><LinkedCIFs><CIFId>0263401</CIFId><RelationType>Auth Signatory</RelationType></LinkedCIFs></LoanDetails><LoanDetails><AgreementId>20421629</AgreementId><LoanStat>C</LoanStat><LoanType>PL</LoanType><LoanDesc></LoanDesc><TotalNoOfInstalments>50</TotalNoOfInstalments><KeyDt><KeyDtType>LoanApprovedDate</KeyDtType><KeyDtValue>2017-11-07</KeyDtValue></KeyDt><KeyDt><KeyDtType>LoanMaturityDate</KeyDtType><KeyDtValue>2022-02-01</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>TotalLoanAmount</AmtType><Amt>87500</Amt></AmountDtls><AmountDtls><AmtType>TotalOutstandingAmt</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmt</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>NextInstallmentAmt</AmtType><Amt>0</Amt></AmountDtls><LinkedCIFs><CIFId>0466081</CIFId><RelationType>Main Account Holder</RelationType></LinkedCIFs></LoanDetails><LoanDetails><AgreementId>0122569516002</AgreementId><LoanStat>CLOSED</LoanStat><LoanType>CREDIT CARD GUARANTEES FIXED</LoanType><LoanDesc></LoanDesc><KeyDt><KeyDtType>LimitSactionDate</KeyDtType><KeyDtValue>2019-07-18</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitExpiryDate</KeyDtType><KeyDtValue>2099-02-13</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>CumulativeDebitAmt</AmtType><Amt>55600</Amt></AmountDtls><MonthsOnBook>20.00</MonthsOnBook><DelinquencyInfo><BucketType>DaysPastDue</BucketType><BucketValue>0</BucketValue></DelinquencyInfo></LoanDetails><CardDetails><CardEmbossNum>068362600</CardEmbossNum><CardStatus>CLSC</CardStatus><CardType>MRBH PLTM EXPAT</CardType><CustRoleType>Primary</CustRoleType><KeyDt><KeyDtType>ApplicationCreationDate</KeyDtType><KeyDtValue>2014-06-17</KeyDtValue></KeyDt><KeyDt><KeyDtType>ExpiryDate</KeyDtType><KeyDtValue>1900-01-01</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>CreditLimit</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>OutstandingAmt</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmt</AmtType><Amt>0</Amt></AmountDtls><LinkedCIFs><CIFId>0263401</CIFId><RelationType>Auth Signatory</RelationType></LinkedCIFs></CardDetails><AcctDetails><AcctId>8882022403901</AcctId><IBANNumber>AE420400008882022403901</IBANNumber><AcctStat>ACTIVE</AcctStat><AcctCur>AED</AcctCur><AcctNm>SHEEMAR YASHIR</AcctNm><AcctType>AMAL ADVANTAGE ACCOUNT</AcctType><AcctSegment>PBD</AcctSegment><AcctSubSegment>PRS</AcctSubSegment><CustRoleType>Main</CustRoleType><KeyDt><KeyDtType>AccountOpenDate</KeyDtType><KeyDtValue>2014-04-17</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitSactionDate</KeyDtType><KeyDtValue>2014-04-17</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitExpiryDate</KeyDtType><KeyDtValue>2014-04-18</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitStartDate</KeyDtType><KeyDtValue>2014-04-17</KeyDtValue></KeyDt><AmountDtls><AmtType>AvailableBalance</AmtType><Amt>4698.99</Amt></AmountDtls><AmountDtls><AmtType>ClearBalanceAmount</AmtType><Amt>4698.99</Amt></AmountDtls><AmountDtls><AmtType>LedgerBalance</AmtType><Amt>4698.99</Amt></AmountDtls><AmountDtls><AmtType>EffectiveAvailableBalance</AmtType><Amt>4698.99</Amt></AmountDtls><AmountDtls><AmtType>CumulativeDebitAmount</AmtType><Amt>7963545.89</Amt></AmountDtls><WriteoffStat>Y</WriteoffStat><WorstDelay24Months>P2</WorstDelay24Months><MonthsOnBook>85.00</MonthsOnBook><LastRepmtDt>MAY</LastRepmtDt><IsCurrent>Y</IsCurrent><ChargeOffFlag>N</ChargeOffFlag><SOLID>888</SOLID><DelinquencyInfo><BucketType>DaysPastDue</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><LinkedCIFs><CIFId>0263401</CIFId><RelationType>Auth Signatory</RelationType></LinkedCIFs></AcctDetails><AcctDetails><AcctId>0088466081061</AcctId><IBANNumber>AE910400000088466081061</IBANNumber><AcctStat>ACTIVE</AcctStat><AcctCur>AED</AcctCur><AcctNm>SHEEMAR YASHIR</AcctNm><AcctType>RAKFINANCE ACCOUNT</AcctType><AcctSegment>PBD</AcctSegment><AcctSubSegment>PSL</AcctSubSegment><CustRoleType>Auth Sign.</CustRoleType><KeyDt><KeyDtType>AccountOpenDate</KeyDtType><KeyDtValue>2012-02-21</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitSactionDate</KeyDtType><KeyDtValue>2012-02-21</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitExpiryDate</KeyDtType><KeyDtValue>2012-06-08</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitStartDate</KeyDtType><KeyDtValue>2012-02-21</KeyDtValue></KeyDt><AmountDtls><AmtType>AvailableBalance</AmtType><Amt>149638.46</Amt></AmountDtls><AmountDtls><AmtType>ClearBalanceAmount</AmtType><Amt>149638.46</Amt></AmountDtls><AmountDtls><AmtType>LedgerBalance</AmtType><Amt>149638.46</Amt></AmountDtls><AmountDtls><AmtType>EffectiveAvailableBalance</AmtType><Amt>149638.46</Amt></AmountDtls><AmountDtls><AmtType>CumulativeDebitAmount</AmtType><Amt>31645022.36</Amt></AmountDtls><AmountDtls><AmtType>SanctionLimit</AmtType><Amt>0</Amt></AmountDtls><WriteoffStat>Y</WriteoffStat><WorstDelay24Months>P2</WorstDelay24Months><MonthsOnBook>111.00</MonthsOnBook><LastRepmtDt>MAY</LastRepmtDt><IsCurrent>Y</IsCurrent><ChargeOffFlag>N</ChargeOffFlag><SOLID>088</SOLID><DelinquencyInfo><BucketType>DaysPastDue</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><LinkedCIFs><CIFId>0466081</CIFId><RelationType>Main Account Holder</RelationType></LinkedCIFs></AcctDetails><AcctDetails><AcctId>0882022403001</AcctId><IBANNumber>AE280400000882022403001</IBANNumber><AcctStat>ACTIVE</AcctStat><AcctCur>AED</AcctCur><AcctNm>SHEEMAR YASHIR</AcctNm><AcctType>FAST SAVER</AcctType><AcctSegment>PBD</AcctSegment><AcctSubSegment>PRS</AcctSubSegment><CustRoleType>Main</CustRoleType><KeyDt><KeyDtType>AccountOpenDate</KeyDtType><KeyDtValue>2012-08-30</KeyDtValue></KeyDt><AmountDtls><AmtType>AvailableBalance</AmtType><Amt>8000.00</Amt></AmountDtls><AmountDtls><AmtType>LedgerBalance</AmtType><Amt>8000.00</Amt></AmountDtls><AmountDtls><AmtType>EffectiveAvailableBalance</AmtType><Amt>8000.00</Amt></AmountDtls><IsCurrent></IsCurrent><SOLID>088</SOLID></AcctDetails><AcctDetails><AcctId>0882022403998</AcctId><IBANNumber>AE750400000882022403998</IBANNumber><AcctStat>CLOSED</AcctStat><AcctCur>AED</AcctCur><AcctNm>SHEEMAR YASHIR</AcctNm><AcctType>CHARGE RECEIVABLE</AcctType><AcctSegment>PBD</AcctSegment><AcctSubSegment>PBN</AcctSubSegment><CustRoleType>Main</CustRoleType><KeyDt><KeyDtType>AccountOpenDate</KeyDtType><KeyDtValue>2012-10-11</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitSactionDate</KeyDtType><KeyDtValue>2012-10-11</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitExpiryDate</KeyDtType><KeyDtValue>2099-01-01</KeyDtValue></KeyDt><KeyDt><KeyDtType>LimitStartDate</KeyDtType><KeyDtValue>2012-10-11</KeyDtValue></KeyDt><AmountDtls><AmtType>AvailableBalance</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>ClearBalanceAmount</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>LedgerBalance</AmtType><Amt>0.00</Amt></AmountDtls><AmountDtls><AmtType>EffectiveAvailableBalance</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>CumulativeDebitAmount</AmtType><Amt>3</Amt></AmountDtls><AmountDtls><AmtType>SanctionLimit</AmtType><Amt>99999999999999</Amt></AmountDtls><WriteoffStat>Y</WriteoffStat><WorstDelay24Months>P2</WorstDelay24Months><MonthsOnBook>2.00</MonthsOnBook><LastRepmtDt>DEC</LastRepmtDt><IsCurrent>Y</IsCurrent><ChargeOffFlag>N</ChargeOffFlag><SOLID>088</SOLID><DelinquencyInfo><BucketType>DaysPastDue</BucketType><BucketValue>0</BucketValue></DelinquencyInfo></AcctDetails><InvestmentDetails><ParentInvestmentId>202240301</ParentInvestmentId><InvestmentID>17717332</InvestmentID><InvProductName>FRIENDS PROVIDENT INTERNATIONAL - PREMIER ADVANCE</InvProductName><InvAssetClass>INSURANCE</InvAssetClass><InvNoOfUnits></InvNoOfUnits><InvCurrency></InvCurrency><AmountDtls><AmtType>InvTotalPurchaseValue</AmtType><Amt>234153.75</Amt></AmountDtls><InvTotalCouponReceived></InvTotalCouponReceived><LienFlg></LienFlg><InvLienAmount></InvLienAmount></InvestmentDetails></ProductExposureDetails></CustomerExposureResponse></EE_EAI_MESSAGE>";
        System.out.println("Hi");
        // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";
        String tableName = "";
        String columnNames = "";
        String columnValues = "";
        String sWhereClause = "";
        if ("Primary".equalsIgnoreCase(CheckGridDataMap.get("CIF_TYPE")))
          {
            tableName = "RB_iRBL_EXTTABLE";
            columnNames = "INTERNAL_EXPOSURE_STATUS";
            columnValues = "'"+flag+"'";
            sWhereClause = "WINAME='" + wiName + "' AND CIF_NUMBER='" + CheckGridDataMap.get("CIF") + "'";
          }
        else
          {
            tableName = "USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS";
            columnNames = "INTERNAL_EXPOSURE_STATUS";
            columnValues = "'"+flag+"'";
            sWhereClause = "WI_NAME='" + wiName + "' AND CIF='" + CheckGridDataMap.get("CIF") + "'";
          }
        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), tableName,
          columnNames, columnValues, sWhereClause);
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

    private static String callCollectionSummary(String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap)
      throws IOException, Exception
      {
        StringBuilder sInputXML = new StringBuilder(
          "<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat><MsgVersion>0001</MsgVersion>"
          + "<RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
          + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>911</ReturnCode>"
          + "<ReturnDesc>Issuer Timed Out</ReturnDesc><MessageId>CUSTOMER_EXPOSUER_0V27</MessageId><Extra1>REQ||SHELL.JOHN</Extra1>"
          + "<Extra2>YYYY-MM-DDThh:mm:ss.mmm+hh:mm</Extra2></EE_EAI_HEADER><CustomerExposureRequest><BankId>RAK</BankId>"
          + "<BranchId>RAK123</BranchId><RequestType>CollectionsSummary</RequestType><CIFId><CIFIdType>" + CheckGridDataMap.get("CIF_TYPE") + "</CIFIdType>"
          + "<CIFIdValue>" + CheckGridDataMap.get("CIF")+"</CIFIdValue></CIFId><CustType>R</CustType></CustomerExposureRequest></EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request  XML for Collection Summary  " + sInputXML);
        //for dummy
        System.out.println("H");
        String responseXML ="<?xml version=\"1.0\"?><EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat><MsgVersion>0001</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS162030942155168</MessageId><Extra1>REP||SHELL.JOHN</Extra1><Extra2>2021-05-06T05:57:28.425+04:00</Extra2></EE_EAI_HEADER><CustomerExposureResponse><RequestType>CollectionsSummary</RequestType><CustInfo><CustId><CustIdType>Primary</CustIdType><CustIdValue>2022403</CustIdValue></CustId><FullNm></FullNm><TotalOutstanding>42434</TotalOutstanding><TotalOverdue>0</TotalOverdue><NoOfContracts>0</NoOfContracts></CustInfo><Derived><Nof_Records>1</Nof_Records></Derived><ProductExposureDetails><LoanDetails><AgreementId>20251879</AgreementId><LoanStat>C</LoanStat><LoanType>PL</LoanType><LoanDesc></LoanDesc><TotalNoOfInstalments>36</TotalNoOfInstalments><RemainingInstalments>0</RemainingInstalments><Bucket>0</Bucket><KeyDt><KeyDtType>Loan_Start_Date</KeyDtType><KeyDtValue>2014-04-29</KeyDtValue></KeyDt><KeyDt><KeyDtType>Loan_close_date</KeyDtType><KeyDtValue>2017-05-01</KeyDtValue></KeyDt><KeyDt><KeyDtType>Loan_disbursal_date</KeyDtType><KeyDtValue>2014-04-29</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>OutstandingAmt</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>ProductAmt</AmtType><Amt>350000</Amt></AmountDtls><AmountDtls><AmtType>PaymentsAmt</AmtType><Amt>13800</Amt></AmountDtls><AmountDtls><AmtType>NextInstallmentAmt</AmtType><Amt>13800</Amt></AmountDtls><AmountDtls><AmtType>CreditLimit</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmt</AmtType><Amt>0</Amt></AmountDtls><PaymentMode>ACCOUNT TRANSFER</PaymentMode><NofDaysPmtDelay>0</NofDaysPmtDelay><MonthsOnBook>75</MonthsOnBook><LastRepmtDt>05-2017</LastRepmtDt><Internal_WriteOff_Check>0</Internal_WriteOff_Check><DelinquencyInfo><BucketType>Bucket</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><InterestRate>24</InterestRate><SchemeCardProd>AMAL BUSINESS FINANCE PLUS - PBN</SchemeCardProd><PreviousLoanDBR>0</PreviousLoanDBR><PreviousLoanTAI>0</PreviousLoanTAI><CurrentlyCurrentFlg>Y</CurrentlyCurrentFlg><GeneralStatus>REGULAR</GeneralStatus></LoanDetails><CardDetails><CardEmbossNum>068362600</CardEmbossNum><CardStatus>A</CardStatus><CardType>CREDIT CARDS</CardType><KeyDt><KeyDtType>Card_approve_date</KeyDtType><KeyDtValue>2014-06-16</KeyDtValue></KeyDt><KeyDt><KeyDtType>CardsApplcnRecvdDate</KeyDtType><KeyDtValue>2014-06-16</KeyDtValue></KeyDt><CurCode>AED</CurCode><AmountDtls><AmtType>Outstanding_balance</AmtType><Amt>42434</Amt></AmountDtls><AmountDtls><AmtType>Credit_limit</AmtType><Amt>49900</Amt></AmountDtls><AmountDtls><AmtType>Overdue_amount</AmtType><Amt>0</Amt></AmountDtls><NofDaysPmtDelay>0</NofDaysPmtDelay><MonthsOnBook>74</MonthsOnBook><LastRepmtDt>08-2020</LastRepmtDt><SchemeCardProd>MRBH PLTM EXPAT</SchemeCardProd><CurrentlyCurrentFlg>Y</CurrentlyCurrentFlg><GeneralStatus>CR05</GeneralStatus><DelinquencyInfo><BucketType>DPD_30_in_last_3_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_30_in_last_6_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_30_in_last_9_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_30_in_last_12_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_30_in_last_18_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_30_in_last_24_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_60_in_last_3_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_60_in_last_6_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_60_in_last_9_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_60_in_last_12_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_60_in_last_18_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_60_in_last_24_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_90_in_last_3_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_90_in_last_6_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_90_in_last_9_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_90_in_last_12_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_90_in_last_18_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_90_in_last_24_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_120_in_last_3_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_120_in_last_6_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_120_in_last_9_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_120_in_last_12_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_120_in_last_18_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_120_in_last_24_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_150_in_last_3_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_150_in_last_6_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_150_in_last_9_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_150_in_last_12_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_150_in_last_18_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_150_in_last_24_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_180_in_last_3_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_180_in_last_6_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_180_in_last_9_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_180_in_last_12_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_180_in_last_18_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>DPD_180_in_last_24_months</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>Bucket</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><MarketingCode>BAU</MarketingCode></CardDetails></ProductExposureDetails></CustomerExposureResponse></EE_EAI_MESSAGE>";
            // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";
        String tableName = "";
        String columnNames = "";
        String columnValues = "";
        String sWhereClause = "";
        if ("Primary".equalsIgnoreCase(CheckGridDataMap.get("CIF_TYPE")))
          {
            tableName = "RB_iRBL_EXTTABLE";
            columnNames = "COLLECTION_SUMMARY_STATUS";
            columnValues = "'"+flag+"'";
            sWhereClause = "WINAME='" + wiName + "' AND CIF_NUMBER='" + CheckGridDataMap.get("CIF") + "'";
          }
        else
          {
            tableName = "USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS";
            columnNames = "COLLECTION_SUMMARY_STATUS";
            columnValues = "'"+flag+"'";
            sWhereClause = "WI_NAME='" + wiName + "' AND CIF='" + CheckGridDataMap.get("CIF") + "'";
          }
        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), tableName,
          columnNames, columnValues, sWhereClause);
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

  }
