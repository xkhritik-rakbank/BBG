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
    static ResponseBean objRespBean = new ResponseBean();

    public static ResponseBean IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {
        String DBQuery =
          "SELECT CIF_NUMBER AS 'CIF','Corporate_CIF'  AS 'CUSTOMER_TYPE',NEW_TOPUP AS 'Product',FINANCE_TYPE AS 'SubProduct','Primary' AS 'CIF_TYPE' FROM RB_iRBL_EXTTABLE WHERE WINAME= '"
            + processInstanceID + "'"
            + "UNION SELECT CIF AS 'CIF', CASE WHEN COMPANYFLAG='Y' THEN 'Corporate_CIF' WHEN COMPANYFLAG='YES' THEN 'Corporate_CIF' ELSE 'Individual_CIF' END  AS 'CUSTOMER_TYPE', '' AS 'Product','' AS 'SubProduct','Secondary' AS 'CIF_TYPE' FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WHERE WI_NAME='"
            + processInstanceID + "' ORDER BY CIF_TYPE";

        String extTabDataIPXML =
          CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
        String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

        XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
        int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("iTotalrec: " + iTotalrec);

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
                String collectionSummaryStatus = callFinancialSummary(processInstanceID, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                String tableName = "";
                String columnNames = "";
                String columnValues = "";
                String sWhereClause = "";
                if ("Primary".equalsIgnoreCase(CheckGridDataMap.get("CIF_TYPE")))
                  {
                    tableName = "RB_iRBL_EXTTABLE";
                    columnNames = "COLLECTION_SUMMARY_STATUS";
                    columnValues = "'"+collectionSummaryStatus+"'";
                    sWhereClause = "WINAME='" + processInstanceID + "' AND CIF_NUMBER='" + CheckGridDataMap.get("CIF") + "'";
                  }
                else
                  {
                    tableName = "USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS";
                    columnNames = "COLLECTION_SUMMARY_STATUS";
                    columnValues = "'"+collectionSummaryStatus+"'";
                    sWhereClause = "WI_NAME='" + processInstanceID + "' AND CIF='" + CheckGridDataMap.get("CIF") + "'";
                  }


                String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false),
                  tableName, columnNames, columnValues, sWhereClause);
                String outputXml = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
                XMLParser sXMLParserChild = new XMLParser(outputXml);
                String StrMainCode = sXMLParserChild.getValueOf("MainCode");
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainCode for apUpdate for " + tableName + " Table : " + StrMainCode);



                //updating Main Integration Grid transaction table
              }
            objRespBean.setDedupeReturnCode("0000");
          }
        else
          {
            objRespBean.setWorkItemMainCode("");
            objRespBean.setDedupeReturnCode("");
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WmgetWorkItem failed: " + objRespBean.getWorkItemMainCode());
          }
        return objRespBean;
      }

    @SuppressWarnings("unused")
    private static String callFinancialSummary(String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap)
      throws IOException, Exception
      {
        String mainFlag = "N";
        try
          {

            String DBQuery = "SELECT ACCTID FROM USR_0_iRBL_InternalExpo_AcctDetails WHERE WI_NAME='" + wiName + "'";
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
                NGXmlList objWorkList = xmlParserData.createList("Records", "Record");


                for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
                  {

                    String AcctID = objWorkList.getVal("ACCTID");
                    String flagAVGBAL = "";
                    String flagLIEN = "";
                    String flagRETURN = "";
                    String flagSAL = "";
                    String flagSI = "";
                    String flagTRANSUM = "";

                    flagAVGBAL = callAVGBALDET(AcctID, wiName, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                    flagLIEN = callLIENDET(AcctID, wiName, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                    flagRETURN = callRETURNDET(AcctID, wiName, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                    flagSAL = callSALDET(AcctID, wiName, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                    flagSI = callSIDET(AcctID, wiName, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                    flagTRANSUM = callTRANSUM(AcctID, wiName, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                    //Need to update flag for Collection summary

                    String tableName = "USR_0_iRBL_InternalExpo_AcctDetails";
                    String columnNames = "AVGBALDET_STATUS,LIENDET_STATUS,RETURNDET_STATUS,SALDET_STATUS,SIDET_STATUS,TRANSUM_STATUS";
                    String columnValues = "'" + flagAVGBAL + "','" + flagLIEN + "','" + flagRETURN + "','" + flagSAL + "','" + flagSI + "','" + flagTRANSUM + "'";
                    String sWhereClause = "WI_NAME='" + wiName + "' AND  ACCTID='" + AcctID + "'";
                    String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false),
                      tableName, columnNames, columnValues, sWhereClause);
                    String outputXml = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
                    XMLParser sXMLParserChild = new XMLParser(outputXml);
                    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
                    if (flagAVGBAL.equalsIgnoreCase("Y") && flagLIEN.equalsIgnoreCase("Y") && flagRETURN.equalsIgnoreCase("Y") && flagSAL.equalsIgnoreCase("Y") && flagSI.equalsIgnoreCase("Y")
                      && flagTRANSUM.equalsIgnoreCase("Y"))
                      {
                        mainFlag = "Y";
                      }

                  }
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Financial Summary Catch: " + e);
            mainFlag = "N";
          }
        return mainFlag;

      }

    private static String callAVGBALDET(String AcctID, String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut,
      HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {

        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat>"
          + "<MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
          + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo>"
          + "<ReturnCode>911</ReturnCode><ReturnDesc>Issuer Timed Out</ReturnDesc><MessageId>143282719608815876</MessageId>"
          + "<Extra1>REQ||LAXMANRET.LAXMANRET</Extra1><Extra2>2015-05-28T21:03:16.088+05:30</Extra2></EE_EAI_HEADER>" + "<FinancialSummaryReq><BankId>RAK</BankId><CIFID>" + CheckGridDataMap.get("CIF")
          + "</CIFID><AcctId>" + AcctID + "</AcctId>" + "<OperationType>AVGBALDET</OperationType><NoOfMonths>3</NoOfMonths></FinancialSummaryReq></EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML for AVGBALDET: " + sInputXML);
        //for dummy
        String responseXML =
          "<?xml version=\"1.0\"?><EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat><MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS161777077375699</MessageId><Extra1>REP||LAXMANRET.LAXMANRET</Extra1><Extra2>2021-04-07T08:46:46.807+04:00</Extra2></EE_EAI_HEADER><FinancialSummaryRes><BankId>RAK</BankId><CIFID>2694103</CIFID><AcctId>0122694103001</AcctId><OperationType>AVGBALDET</OperationType><OperationDesc>AVERAGE BALANCE</OperationDesc><TxnSummary></TxnSummary><AvgBalanceDtls><Month>MAR</Month><AvgBalance>3052.2</AvgBalance></AvgBalanceDtls><AvgBalanceDtls><Month>FEB</Month><AvgBalance>3052.2</AvgBalance></AvgBalanceDtls><AvgBalanceDtls><Month>JAN</Month><AvgBalance>3052.2</AvgBalance></AvgBalanceDtls></FinancialSummaryRes></EE_EAI_MESSAGE>";
        // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);

        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";

        return flag;

      }

    private static String callLIENDET(String AcctID, String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut,
      HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {

        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat>"
          + "<MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
          + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo>"
          + "<ReturnCode>911</ReturnCode><ReturnDesc>Issuer Timed Out</ReturnDesc><MessageId>143282719608815876</MessageId>"
          + "<Extra1>REQ||LAXMANRET.LAXMANRET</Extra1><Extra2>2015-05-28T21:03:16.088+05:30</Extra2></EE_EAI_HEADER>" + "<FinancialSummaryReq><BankId>RAK</BankId><CIFID>" + CheckGridDataMap.get("CIF")
          + "</CIFID><AcctId>" + AcctID + "</AcctId>" + "<OperationType>LIENDET</OperationType><NoOfMonths>6</NoOfMonths></FinancialSummaryReq></EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML for LIENDET: " + sInputXML);
        //for dummy
        String responseXML =
          "<?xml version=\"1.0\"?><EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat><MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS161777077266276</MessageId><Extra1>REP||LAXMANRET.LAXMANRET</Extra1><Extra2>2021-04-07T08:46:45.898+04:00</Extra2></EE_EAI_HEADER><FinancialSummaryRes><BankId>RAK</BankId><CIFID>2694103</CIFID><AcctId>0022694103001</AcctId><OperationType>LIENDET</OperationType><OperationDesc>LIEN DETAILS</OperationDesc><TxnSummary></TxnSummary><LienDetails><LienId>011977155</LienId><LienAmount>15000</LienAmount><LienReasonCode>CP</LienReasonCode><LienStartDate>2021-04-04</LienStartDate></LienDetails></FinancialSummaryRes></EE_EAI_MESSAGE>";
        // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";

        return flag;

      }

    private static String callRETURNDET(String AcctID, String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut,
      HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {

        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat>"
          + "<MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
          + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo>"
          + "<ReturnCode>911</ReturnCode><ReturnDesc>Issuer Timed Out</ReturnDesc><MessageId>143282719608815876</MessageId>"
          + "<Extra1>REQ||LAXMANRET.LAXMANRET</Extra1><Extra2>2015-05-28T21:03:16.088+05:30</Extra2></EE_EAI_HEADER>" + "<FinancialSummaryReq><BankId>RAK</BankId><CIFID>" + CheckGridDataMap.get("CIF")
          + "</CIFID><AcctId>" + AcctID + "</AcctId>" + "<OperationType>RETURNDET</OperationType><NoOfMonths>6</NoOfMonths></FinancialSummaryReq></EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML for RETURNDET: " + sInputXML);
        //for dummy
        String responseXML =
          "<?xml version=\"1.0\"?><EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat><MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS162020335388827</MessageId><Extra1>REP||LAXMANRET.LAXMANRET</Extra1><Extra2>2021-05-05T12:29:15.373+04:00</Extra2></EE_EAI_HEADER><FinancialSummaryRes><BankId>RAK</BankId><CIFID>0465437</CIFID><AcctId>0088465437061</AcctId><OperationType>RETURNDET</OperationType><OperationDesc>RETURN DETAILS(CHQ,DDS)</OperationDesc><TxnSummary></TxnSummary><ReturnsDtls><ReturnType>DDS</ReturnType><ReturnNumber>512000015520200000240140031N5</ReturnNumber><ReturnAmount>2095.45</ReturnAmount><RetReasonCode>INSUFFICIENT FUNDS</RetReasonCode><ReturnDate>2020-11-10</ReturnDate></ReturnsDtls><ReturnsDtls><ReturnType>DDS</ReturnType><ReturnNumber>5120000155202000002401400N8IU</ReturnNumber><ReturnAmount>2095.45</ReturnAmount><RetReasonCode>INSUFFICIENT FUNDS</RetReasonCode><ReturnDate>2021-01-10</ReturnDate></ReturnsDtls><ReturnsDtls><ReturnType>DDS</ReturnType><ReturnNumber>5120000155202000002401400N8IU</ReturnNumber><ReturnAmount>2095.45</ReturnAmount><RetReasonCode>INSUFFICIENT FUNDS</RetReasonCode><ReturnDate>2021-01-16</ReturnDate></ReturnsDtls></FinancialSummaryRes></EE_EAI_MESSAGE>";
        // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";

        return flag;

      }

    private static String callSALDET(String AcctID, String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut,
      HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {

        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat>"
          + "<MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
          + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo>"
          + "<ReturnCode>911</ReturnCode><ReturnDesc>Issuer Timed Out</ReturnDesc><MessageId>143282719608815876</MessageId>"
          + "<Extra1>REQ||LAXMANRET.LAXMANRET</Extra1><Extra2>2015-05-28T21:03:16.088+05:30</Extra2></EE_EAI_HEADER>" + "<FinancialSummaryReq><BankId>RAK</BankId><CIFID>" + CheckGridDataMap.get("CIF")
          + "</CIFID><AcctId>" + AcctID + "</AcctId>" + "<OperationType>SALDET</OperationType><NoOfMonths>12</NoOfMonths></FinancialSummaryReq></EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML for SALDET: " + sInputXML);
        //for dummy
        String responseXML =
          "<?xml version=\"1.0\"?><EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat><MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS16035339446047</MessageId><Extra1>REP||LAXMANRET.LAXMANRET</Extra1><Extra2>2020-10-24T02:05:46.093+04:00</Extra2></EE_EAI_HEADER><FinancialSummaryRes><BankId>RAK</BankId><CIFID>0106771</CIFID><AcctId>0330106771001</AcctId><OperationType>SALDET</OperationType><OperationDesc>SALARY DETAILS</OperationDesc><SalDetails><SalCreditMonth>January</SalCreditMonth><SalCreditDate>2020-01-26</SalCreditDate><SalAmount>12773.17</SalAmount></SalDetails><SalDetails><SalCreditMonth>February</SalCreditMonth><SalCreditDate>2020-02-26</SalCreditDate><SalAmount>14716.66</SalAmount></SalDetails><SalDetails><SalCreditMonth>March</SalCreditMonth><SalCreditDate>2020-03-25</SalCreditDate><SalAmount>14678.75</SalAmount></SalDetails><SalDetails><SalCreditMonth>April</SalCreditMonth><SalCreditDate>2020-04-26</SalCreditDate><SalAmount>12484.07</SalAmount></SalDetails><SalDetails><SalCreditMonth>May</SalCreditMonth><SalCreditDate>2020-05-21</SalCreditDate><SalAmount>12635.25</SalAmount></SalDetails><SalDetails><SalCreditMonth>June</SalCreditMonth><SalCreditDate>2020-06-25</SalCreditDate><SalAmount>12630</SalAmount></SalDetails><SalDetails><SalCreditMonth>July</SalCreditMonth><SalCreditDate>2020-07-20</SalCreditDate><SalAmount>21013.66</SalAmount></SalDetails><SalDetails><SalCreditMonth>October</SalCreditMonth><SalCreditDate>2019-10-31</SalCreditDate><SalAmount>16733.37</SalAmount></SalDetails><SalDetails><SalCreditMonth>October</SalCreditMonth><SalCreditDate>2019-10-24</SalCreditDate><SalAmount>14819.17</SalAmount></SalDetails><SalDetails><SalCreditMonth>December</SalCreditMonth><SalCreditDate>2019-12-22</SalCreditDate><SalAmount>12480.69</SalAmount></SalDetails><TxnSummary></TxnSummary></FinancialSummaryRes></EE_EAI_MESSAGE>";
          // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        System.out.println("Hi");
        flag = flag == "true" ? "Y" : "N";

        return flag;

      }

    private static String callSIDET(String AcctID, String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut,
      HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {

        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat>"
          + "<MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
          + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo>"
          + "<ReturnCode>911</ReturnCode><ReturnDesc>Issuer Timed Out</ReturnDesc><MessageId>143282719608815876</MessageId>"
          + "<Extra1>REQ||LAXMANRET.LAXMANRET</Extra1><Extra2>2015-05-28T21:03:16.088+05:30</Extra2></EE_EAI_HEADER>" + "<FinancialSummaryReq><BankId>RAK</BankId><CIFID>" + CheckGridDataMap.get("CIF")
          + "</CIFID><AcctId>" + AcctID + "</AcctId>" + "<OperationType>SIDET</OperationType><NoOfMonths>6</NoOfMonths></FinancialSummaryReq></EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML for SIDET: " + sInputXML);
        //for dummy
        String responseXML =
          "<?xml version=\"1.0\"?><EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat><MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS159558886036759</MessageId><Extra1>REP||LAXMANRET.LAXMANRET</Extra1><Extra2>2020-07-24T03:07:14.002+04:00</Extra2></EE_EAI_HEADER><FinancialSummaryRes><BankId>RAK</BankId><CIFID>2177549</CIFID><AcctId>0018148251001</AcctId><OperationType>SIDET</OperationType><OperationDesc>STANDING INSTRUCTION DETAILS</OperationDesc><TxnSummary></TxnSummary><SIDetails><SINumber>0148203</SINumber><SIAmount>105</SIAmount><SIRemarks>DIGITAL BNKG CHRG-INCL. VAT</SIRemarks><NextExecDate>2019-12-10</NextExecDate></SIDetails></FinancialSummaryRes></EE_EAI_MESSAGE>"; // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";

        return flag;

      }

    private static String callTRANSUM(String AcctID, String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut,
      HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {

        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat>"
          + "<MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId>"
          + "<RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo>"
          + "<ReturnCode>911</ReturnCode><ReturnDesc>Issuer Timed Out</ReturnDesc><MessageId>143282719608815876</MessageId>"
          + "<Extra1>REQ||LAXMANRET.LAXMANRET</Extra1><Extra2>2015-05-28T21:03:16.088+05:30</Extra2></EE_EAI_HEADER>" + "<FinancialSummaryReq><BankId>RAK</BankId><CIFID>" + CheckGridDataMap.get("CIF")
          + "</CIFID><AcctId>" + AcctID + "</AcctId>" + "<OperationType>TRANSUM</OperationType><NoOfMonths>3</NoOfMonths></FinancialSummaryReq></EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML for TRANSUM: " + sInputXML);
        //for dummy
        String responseXML =
          "<?xml version=\"1.0\"?><EE_EAI_MESSAGE xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><EE_EAI_HEADER><MsgFormat>FINANCIAL_SUMMARY</MsgFormat><MsgVersion>0000</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS161777077397542</MessageId><Extra1>REP||LAXMANRET.LAXMANRET</Extra1><Extra2>2021-04-07T08:46:47.134+04:00</Extra2></EE_EAI_HEADER><FinancialSummaryRes><BankId>RAK</BankId><CIFID>2694103</CIFID><AcctId>0122694103001</AcctId><OperationType>TRANSUM</OperationType><OperationDesc>TRANSACTION DETAILS</OperationDesc><TxnSummary><AvgCrTurnOver>0</AvgCrTurnOver><TxnSummaryDtls><Month>JAN</Month><TotalCrAmt>0</TotalCrAmt><NoOfCredits>0</NoOfCredits><TotalCashDepAmt>0</TotalCashDepAmt><NoOfCashDep>0</NoOfCashDep><TotalDrAmt>0</TotalDrAmt><NoOfDebits>0</NoOfDebits></TxnSummaryDtls><TxnSummaryDtls><Month>FEB</Month><TotalCrAmt>0</TotalCrAmt><NoOfCredits>0</NoOfCredits><TotalCashDepAmt>0</TotalCashDepAmt><NoOfCashDep>0</NoOfCashDep><TotalDrAmt>0</TotalDrAmt><NoOfDebits>0</NoOfDebits></TxnSummaryDtls><TxnSummaryDtls><Month>MAR</Month><TotalCrAmt>0</TotalCrAmt><NoOfCredits>0</NoOfCredits><TotalCashDepAmt>0</TotalCashDepAmt><NoOfCashDep>0</NoOfCashDep><TotalDrAmt>0</TotalDrAmt><NoOfDebits>0</NoOfDebits></TxnSummaryDtls></TxnSummary></FinancialSummaryRes></EE_EAI_MESSAGE>";
           // need to uncomment
        //String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        //CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "WS_NAME",
        //socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        String flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getCabinetName(), wiName, CheckGridDataMap.get("Product"),
          CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";

        return flag;

      }


  }
