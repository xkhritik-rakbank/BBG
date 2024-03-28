package com.newgen.iRBL.SysCheckIntegration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import org.xml.sax.InputSource;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class IntegrateMemopadDetails
  {
    

    public static String IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {
    	String mainFlag = "Success";
        try
          {

            String DBQuery =
              "SELECT DISTINCT ACCTID, isnull(MEMOPAD_STATUS,'') as MEMOPAD_STATUS FROM USR_0_iRBL_InternalExpo_AcctDetails with(nolock) WHERE AcctStat = 'ACTIVE' AND WI_NAME='" + processInstanceID + "' ";
            String extTabDataIPXML =
              CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Account list for memo pad input: " + extTabDataIPXML);
            String extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Account list for memo pad output: " + extTabDataOPXML);
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
                    String flagMemopad = "";
                    if (!(objWorkList.getVal("MEMOPAD_STATUS").equalsIgnoreCase("Y")))
                    {
                    	flagMemopad = callMemopadDetails(AcctID, processInstanceID, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                    	
                    	 String tableName = "USR_0_iRBL_InternalExpo_AcctDetails";
                         String columnNames = "MEMOPAD_STATUS";
                         String columnValues = "'" + flagMemopad + "'";
                         String sWhereClause = "WI_NAME='" + processInstanceID + "' AND  ACCTID='" + AcctID + "' ";
                         String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false),
                           tableName, columnNames, columnValues, sWhereClause);
                         iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("memopad input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
                         String outputXml = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
                         iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("memopad Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
                         XMLParser sXMLParserChild = new XMLParser(outputXml);
                         String StrMainCode = sXMLParserChild.getValueOf("MainCode");
                         if (flagMemopad.equalsIgnoreCase("N"))
                         {
                         	if(!"Failure".equalsIgnoreCase(mainFlag))
                         		mainFlag = "Failure";
                         }
                    	
                    }
                    else
                    {
                    	// nothing ot do
                    }
                   
                  }
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Financial Summary Catch: " + e);
            mainFlag = "Failure";
          }
        return mainFlag;
      }

    @SuppressWarnings("unused")
   
    private static String callMemopadDetails(String AcctID, String wiName, int integrationWaitTime, int socketConnectionTimeOut,
      HashMap<String, String> socketDetailsMap) throws IOException, Exception
      {
    	String flag = "Y";
    	java.util.Date d1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
		String DateExtra2 = sdf1.format(d1)+"+04:00";
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
		String ReqDateTime = sdf2.format(d1);
		
        StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>"
        		+ "<EE_EAI_HEADER>"
	        		+ "<MsgFormat>MEMOPAD_DETAILS</MsgFormat>"
	        		+ "<MsgVersion>0001</MsgVersion>"
	        		+ "<RequestorChannelId>BPM</RequestorChannelId>"
	        		+ "<RequestorUserId>BPMUSER</RequestorUserId>"
	        		+ "<RequestorLanguage>E</RequestorLanguage>"
	        		+ "<RequestorSecurityInfo>secure</RequestorSecurityInfo>"
	        		+ "<ReturnCode>0000</ReturnCode>"
	        		+ "<ReturnDesc>success</ReturnDesc>"
	        		+ "<MessageId>[B@270027</MessageId>"
	        		+ "<Extra1>REQ||SHELL.JOHN</Extra1>"
	        		+ "<Extra2>"+DateExtra2+"</Extra2>"
        		+ "</EE_EAI_HEADER>"
        		+ "<MemoDetailsReq>"
        			+ "<BankId>RAK</BankId>"
        			+"<AcctId>"+AcctID+"</AcctId>"
        		+ "</MemoDetailsReq>"
        		+ "</EE_EAI_MESSAGE>");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Request XML for Memopad for "+AcctID+" account : " + sInputXML);
        //for dummy
        //String responseXML = "<?xml version="1.0"?><EE_EAI_MESSAGE xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><EE_EAI_HEADER><MsgFormat>MEMOPAD_DETAILS</MsgFormat><MsgVersion>0001</MsgVersion><RequestorChannelId>BPM</RequestorChannelId><RequestorUserId>BPMUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>[B@270027</MessageId><Extra1>REP||SHELL.JOHN</Extra1><Extra2>2021-05-31T06:49:34.552+04:00</Extra2></EE_EAI_HEADER><MemoDetailsRes><BankId>RAK</BankId><CustId>0300300</CustId><AcctId>0002300300001</AcctId><AcctNotes><MemoPadSrlNum>3284586</MemoPadSrlNum><Topic>CLEARING</Topic><FuncCode>001</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>MKRESHMA</CreatedBy><CreationDt>2017-03-01</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>“REFER CBD RM FOR ANY TECHNICAL DISCREPANCIES ON ANY CHEQUES”</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>267728</MemoPadSrlNum><Topic>PDCS</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>GNADEERA</CreatedBy><CreationDt>2012-10-08</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>REFER ANY CHEQUE SIGNED BY SH FAISAL TO CBD NAKHEEL.</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>1355992</MemoPadSrlNum><Topic>MEMOPAD</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>RNISHA</CreatedBy><CreationDt>2014-08-26</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>WHITE LISTED CUSTOMER FOR CASH WITHDRAWALS</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>4038914</MemoPadSrlNum><Topic>MEMO</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>UWSAJITH</CreatedBy><CreationDt>2018-03-18</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>BLANKET APPROVAL HELD FOR ACCEPTANCE OF OUTWARD REMITTANCE INSTRUCTIONS ON NON-S TANDARD APPLICATION</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>5558239</MemoPadSrlNum><Topic>WHITELIST</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>KARUPI</CreatedBy><CreationDt>2020-06-04</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>WHITELISTED. EXEMPTION FROM CANCELLED CHEQUE ONLY. CALL BACK MANDATORY. APPLICAB LE FOR EFT/INTERNAL FUNDS TRANSFER REQUESTS SUBMITTED IN ORIGINAL ONLY BY THIRD  PARTIES</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>102980</MemoPadSrlNum><Topic>MEMO</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>SYSTEM</CreatedBy><CreationDt>2012-06-08</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>- INDEMNITY WAIVER HELD FOR SALARY S/O PLACEMENT/AMENDMENTS APPR BY GM E-MAIL DTD 15/07/2010.REFER CMNIDOCS</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>102681</MemoPadSrlNum><Topic>MEMO</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>SYSTEM</CreatedBy><CreationDt>2012-06-08</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>- IF STALE CHQUE (OVER 6 MONTHS OLD) HAS BEEN PRESENTED, PLEASE INFORM RAK GOVT FINANCE DEPARTMENT-LTR DT 21/12/05</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>102682</MemoPadSrlNum><Topic>MEMO</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>SYSTEM</CreatedBy><CreationDt>2012-06-08</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>- If stale cheque (over 6 months) has been presented, please contact Rak Govt Finance dept- ltr dt: 21/12/2005</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>115193</MemoPadSrlNum><Topic>MEMO</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>SYSTEM</CreatedBy><CreationDt>2012-06-08</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>-PDC DEPOSITS CAN BE ACCEPTED FROM THE REPRESENTATIVES REFER EMAIL 04-06-12</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>1654967</MemoPadSrlNum><Topic>MEMO</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>DFELINA</CreatedBy><CreationDt>2014-12-31</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>KINDLY ACCEPT CHQS DEPOSITED IN FAVOUR OF FINANCE DEPT / DEPT OF CUSTOMS &amp; PORTS / LAND DEPT/ COURTS DEPT/ DEPT OF ECONOMIC DEVELOPMENT / RAK POLICE GHQ / HQ OF  LOCAL GUARD GROUP  - RAK</NoteText></AcctNotes><AcctNotes><MemoPadSrlNum>1490542</MemoPadSrlNum><Topic>MEMOPAD</Topic><FuncCode>FT</FuncCode><Intent>G</Intent><Security>P</Security><ReasonCode></ReasonCode><CreatedBy>DFELINA</CreatedBy><CreationDt>2014-11-12</CreationDt><ExpiryDt>2099-12-31</ExpiryDt><NoteText>CHARGES WAIVED FOR ALL LOCAL OUTWARD  TT TRANSFERS/SALARY PAYMENTS</NoteText></AcctNotes></MemoDetailsRes></EE_EAI_MESSAGE>";
               
        // need to uncomment
        String responseXML = iRBLIntegration.socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(),
        CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "Sys_Checks_Integration",
        socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);

        XMLParser xmlParserDetails= new XMLParser(responseXML);
	    String return_code = xmlParserDetails.getValueOf("ReturnCode");
	    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

	    String return_desc = xmlParserDetails.getValueOf("ReturnDesc");
		
		if (return_desc.trim().equalsIgnoreCase(""))
			return_desc = xmlParserDetails.getValueOf("Description");
		
		String MsgId = "";
		if (responseXML.contains("<MessageId>"))
			MsgId = xmlParserDetails.getValueOf("MessageId");
		
	    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
	    
	    
	    //Inserting in Integration details table
	    //CINF0171 - FIN : ACCOUNT CLOSED
	    //CINF0166 - FIN : NOTES NOT MAINTAINED FOR THE CUSTOMER
        String CallStatus = "";
	    if("0000".equals(return_code) || "CINF0171".equals(return_code) || "CINF0166".equals(return_code))
	    	CallStatus="Success";
	    else
	    	CallStatus="Failure";
	    java.util.Date d2 = new Date();
	    String ResDateTime = sdf2.format(d2);
	    String TableName1 = "USR_0_IRBL_INTEGRATION_DTLS";
	    String columnnames1 = "WI_NAME, CIFID, AccountNumber, CallName, Operation, RequestDateTime, CallStatus, MessageId, ResponseDateTime, ReturnCode, ReturnError";
	    String columnvalues1 = "'"+wiName+"','','"+AcctID+"','MEMOPAD_DETAILS','','"+ReqDateTime+"','"+CallStatus+"','"+MsgId+"','"+ResDateTime+"','"+return_code+"','"+return_desc+"' ";
		String InputXML1 = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnnames1, columnvalues1, TableName1);
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert MEMOPAD_DETAILS "+TableName1+" Table : "+TableName1);

		String OutputXML1=CommonMethods.WFNGExecute(InputXML1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for MEMOPAD_DETAILS apInsert "+TableName1+" Table : "+OutputXML1);

		XMLParser sXMLParserChild1= new XMLParser(OutputXML1);
	    String StrMainCode1 = sXMLParserChild1.getValueOf("MainCode");
	    if (StrMainCode1.equals("0"))
		   	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+TableName1);	
	    else
	       	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+OutputXML1);
	    ////////////////////////////////////////
	    
	    
	    if("0000".equals(return_code))
		{
	    	DocumentBuilderFactory factory_1 = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder_1 = factory_1.newDocumentBuilder();
			InputSource is_1 = new InputSource(new StringReader(responseXML));

			Document doc_1 = builder_1.parse(is_1);
			doc_1.getDocumentElement().normalize();
			
			NodeList nList = doc_1.getElementsByTagName("AcctNotes");
			
			String TableName = "USR_0_IRBL_MEMOPAD_LIST_DTLS";
			String sWhereClause= "WI_NAME='"+wiName+"' AND ACCTNO='"+AcctID+"'";		
			// Deleting existing entries for the account
			String strInputXML = CommonMethods.apDeleteInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), TableName, sWhereClause);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apDeleteInput "+TableName+" Table : "+strInputXML);

			String strOutputXML=iRBLSysCheckIntegration.WFNGExecute(strInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apDeleteInput "+TableName+" Table : "+strOutputXML);

			////////////////////////////////////////////
			
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				String Reason_Decision="";
				Node nNode = nList.item(temp);


				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					String MemoPadSrlNum= eElement.getElementsByTagName("MemoPadSrlNum").item(0).getTextContent();
					String Topic= eElement.getElementsByTagName("Topic").item(0).getTextContent();
					String FuncCode= eElement.getElementsByTagName("FuncCode").item(0).getTextContent();
					String CreatedBy= eElement.getElementsByTagName("CreatedBy").item(0).getTextContent();
					String CreationDt= eElement.getElementsByTagName("CreationDt").item(0).getTextContent() ;
					String ExpiryDt=eElement.getElementsByTagName("ExpiryDt").item(0).getTextContent() ;
					String NoteText=eElement.getElementsByTagName("NoteText").item(0).getTextContent() ;
					
					NoteText=NoteText.replaceAll("&gt;", ">");
					NoteText=NoteText.replaceAll("&lt;", "<");
					NoteText=NoteText.replaceAll("'", "");
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.info("NoteText---->"+NoteText);
					
					String columnnames="WI_NAME, ACCTNO, SRNO, TOPIC, FUNCCODE, CREATEDBY, CREATIONDT, EXPIRYDT, MEMOPAD_DET";
				    String columnvalues="'"+wiName+"','"+AcctID+"','"+MemoPadSrlNum+"','"+Topic+"','"+FuncCode+"','"+CreatedBy+"','"+CreationDt+"','"+ExpiryDt+"','"+NoteText+"'";
					String InputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnnames, columnvalues, TableName);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert "+TableName+" Table : "+InputXML);

					String OutputXML=CommonMethods.WFNGExecute(InputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apInsert "+TableName+" Table : "+OutputXML);

					XMLParser sXMLParserChild= new XMLParser(OutputXML);
				    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
				    if (StrMainCode.equals("0"))
					   	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+TableName);	
				    else
				       	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+OutputXML);
				    		
				
				}					
				
			}
			
		}
	    else if("CINF0171".equals(return_code) || "CINF0166".equals(return_code))
	    {
	    	flag = "Y";
	    }
	    else
	    {
	    	flag = "N";
	    }	
       
	    return flag;

      }



  }
