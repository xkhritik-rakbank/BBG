package com.newgen.iRBL.SysCheckIntegration;

import java.io.IOException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class UpdateExtAndTransTableData
  {
    
    public static String UpdateData(String processInstanceID) throws IOException, Exception
      {
        
    	String RetVal = "";
        XMLParser xmlParserData;
        int iTotalrec = 0;
        String CIF = "";

    	String xmlDataExtTab = "";

        NGXmlList objWorkList;

        /*********************************updating form data start*************************************/
        String tableName = "";
        String columnNames = "";
        String columnValues = "";
        String sWhereClause = "";
        String sQuery =
          "SELECT TOP 1 AccountOpenDate FROM USR_0_iRBL_InternalExpo_AcctDetails with(nolock) WHERE Wi_Name = '"+processInstanceID+"' ORDER BY CAST(AccountOpenDate AS date) desc";

        String inputXML =
          CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));

        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for external data update from acct details:" + inputXML);
        String outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for external data update from acct details: " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        String AccOpenDate = "";
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
        {
        	AccOpenDate = xmlParserData.getValueOf("AccountOpenDate").trim();
        }
        
        sQuery =
        "SELECT TOP 1 TotalOutstandingAmt FROM USR_0_iRBL_InternalExpo_LoanDetails with(nolock) WHERE Wi_Name = '"+processInstanceID+"' AND Request_Type = 'CollectionsSummary' AND SchemeCardProd = 'RAKFIN'";

        inputXML =
        CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));

        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for external data update loan details " + inputXML);
        outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for external data update loan details " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        String TotOutstandAmt = "";
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
        {
        	TotOutstandAmt = xmlParserData.getValueOf("TotalOutstandingAmt").trim();
        }
        
        sQuery ="select count(*) as ReturnCount from USR_0_iRBL_ExternalExpo_ChequeDetails with(nolock) where Wi_Name = '"+processInstanceID+"' and CifId in (select CIF_NUMBER from RB_iRBL_EXTTABLE with(nolock) where WINAME = '"+processInstanceID+"')";

        inputXML =
        CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));

        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for external data update cheque details " + inputXML);
        outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for external data update cheque details " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        int chequeReturnCount = 0;
        String AECBResultStatus = "";
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
        {
        	chequeReturnCount = Integer.parseInt(xmlParserData.getValueOf("ReturnCount").trim());
        }
        
        // Updating AECB Result Status based on cheque return count for primary cif from external exposure - POLP-9464
        if (chequeReturnCount == 0)
        	AECBResultStatus = "Positive";
        else if (chequeReturnCount >= 1 && chequeReturnCount <=3)
        	AECBResultStatus = "Moderate";
        else if (chequeReturnCount >= 4)
        	AECBResultStatus = "Negative";
        
        if(!"".equalsIgnoreCase(AccOpenDate) || !"".equalsIgnoreCase(TotOutstandAmt) || !"".equalsIgnoreCase(AECBResultStatus))
        {
        	tableName = "RB_iRBL_EXTTABLE";
            columnNames = "AECB_RESULT_STATUS";
            columnValues = "'" + AECBResultStatus + "'";
            
            if(!"".equalsIgnoreCase(TotOutstandAmt))
            {
            	columnNames = columnNames+ ",TOTAL_CURRENT_OUTSTANDING";
                columnValues = columnNames+ ",'" + TotOutstandAmt + "'";
            }
            
            if(!"".equalsIgnoreCase(AccOpenDate))
            {
            	columnNames = columnNames+ ",ACCOUNT_OPENED_DATE";
                columnValues = columnNames+ ",'" + AccOpenDate + "'";
            }
            
            sWhereClause = "WINAME='" + processInstanceID + "'";
            inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), tableName,
              columnNames, columnValues, sWhereClause);
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apUpdate for " + tableName + " Table : " + inputXML);
            String outputXml = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdate for " + tableName + " Table : " + outputXml);
            XMLParser sXMLParserChild = new XMLParser(outputXml);
            String StrMainCode = sXMLParserChild.getValueOf("MainCode");
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainCode for apUpdate for " + tableName + " Table : " + StrMainCode);
  		}	
        
        columnNames = "";
        columnValues = "";
        tableName = "USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS";
        sQuery = "SELECT TOP 1 isnull(sum(CAST (TotalCrAmt AS float)),0)/12 AS TotalCrAmt,count(*) AS count FROM USR_0_iRBL_FinancialSummary_TxnSummary WITH(nolock) "+
				" Where wi_NAME='"+processInstanceID+"' and CIFID IN "+              
				"( SELECT CIF_NUMBER FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE WINAME='"+processInstanceID+"' "+
				" UNION "+ 
				" SELECT CIF FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(nolock) "+
				" WHERE WI_NAME='"+processInstanceID+"' AND CO_BORROWER_STATUS IN('Y','Yes') )";
        inputXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update " + inputXML);
        outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update: " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
          {
            columnNames = "RAKBANK_12M_TO_DETAILS,RAKBANK_12M_TO_FROM,RAKBANK_12M_TO_TO";
            columnValues = "'" + xmlParserData.getValueOf("TotalCrAmt") + "','',''";
          }
        
        
        sQuery = "SELECT jan,feb,mar,apr,may,jun,jul,aug,spt,oct,nov,dec FROM USR_0_iRBL_FinancialSummary_AvgBalanceDtls with(nolock) WHERE WI_Name='" + processInstanceID + "' AND CIFID IN "+              
				"( SELECT CIF_NUMBER FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE WINAME='"+processInstanceID+"' "+
				" UNION "+ 
				" SELECT CIF FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(nolock) "+
				" WHERE WI_NAME='"+processInstanceID+"' AND CO_BORROWER_STATUS IN('Y','Yes') )";
        inputXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update " + inputXML);
        outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update: " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        double abDetails = 0.00;
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
          {
            xmlDataExtTab = xmlParserData.getNextValueOf("Record");
            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

            //XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
            objWorkList = xmlParserData.createList("Records", "Record");

            int calCnt = 0;	

            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
              {
                if (!objWorkList.getVal("jan").equalsIgnoreCase("NA") && !objWorkList.getVal("jan").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("jan"));
                  }
                if (!objWorkList.getVal("feb").equalsIgnoreCase("NA") && !objWorkList.getVal("feb").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("feb"));
                  }
                if (!objWorkList.getVal("mar").equalsIgnoreCase("NA") && !objWorkList.getVal("mar").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("mar"));
                  }
                if (!objWorkList.getVal("apr").equalsIgnoreCase("NA") && !objWorkList.getVal("apr").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("apr"));
                  }
                if (!objWorkList.getVal("may").equalsIgnoreCase("NA") && !objWorkList.getVal("may").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("may"));
                  }
                if (!objWorkList.getVal("jun").equalsIgnoreCase("NA") && !objWorkList.getVal("jun").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("jun"));
                  }
                if (!objWorkList.getVal("jul").equalsIgnoreCase("NA") && !objWorkList.getVal("jul").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("jul"));
                  }
                if (!objWorkList.getVal("aug").equalsIgnoreCase("NA") && !objWorkList.getVal("aug").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("aug"));
                  }
                if (!objWorkList.getVal("sep").equalsIgnoreCase("NA") && !objWorkList.getVal("sep").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("sep"));
                  }
                if (!objWorkList.getVal("oct").equalsIgnoreCase("NA") && !objWorkList.getVal("oct").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("oct"));
                  }
                if (!objWorkList.getVal("nov").equalsIgnoreCase("NA") && !objWorkList.getVal("nov").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("nov"));
                  }
                if (!objWorkList.getVal("dec").equalsIgnoreCase("NA") && !objWorkList.getVal("dec").equalsIgnoreCase(""))
                  {
                	calCnt++;
                    abDetails = abDetails + Double.parseDouble(objWorkList.getVal("dec"));
                  }
              }
            if (calCnt == 0)
            	calCnt = 1;
            columnNames = columnNames + ",RAKBANK_12M_AB_DETAILS";
            columnValues = columnValues + ",'" + (abDetails / calCnt) / iTotalrec + "'";
          }
        
		sQuery = "SELECT count(*) AS count FROM USR_0_iRBL_FinancialSummary_ReturnsDtls WITH (nolock) "
				+ " WHERE WI_Name='"+processInstanceID+"' AND returnNumber IS NOT NULL AND CIFID IN "
				+ "( SELECT CIF_NUMBER FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE WINAME='"+processInstanceID+"' "
				+ "UNION "
				+ "SELECT CIF FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(nolock) "
				+ "WHERE WI_NAME='"+processInstanceID+"' AND CO_BORROWER_STATUS IN('Y','Yes') )";
        inputXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update " + inputXML);
        outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update: " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
          {
            columnNames = columnNames + ",CHQ_RETURNS_RAKBANK_DETAILS";
            columnValues = columnValues + ",'" + xmlParserData.getValueOf("count") + "'";
          }
        
        sQuery = "SELECT isnull(sum(CAST(LienAmount AS DECIMAL(18,2))),0) AS LienAmount FROM "
        		+ " USR_0_iRBL_FinancialSummary_LienDetails with(nolock) WHERE WI_Name='" + processInstanceID + "' AND CIFID IN "
        		+ "( SELECT CIF_NUMBER FROM RB_iRBL_EXTTABLE WITH(nolock) WHERE WINAME='"+processInstanceID+"' "
        		+ "UNION "
        		+ "SELECT CIF FROM USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS WITH(nolock) "
        		+ "WHERE WI_NAME='"+processInstanceID+"' AND CO_BORROWER_STATUS IN('Y','Yes') )";
        inputXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update " + inputXML);
        outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update: " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
          {
            columnNames = columnNames + ",LD_BALANCE_DETAILS";
            columnValues = columnValues + ",'" + xmlParserData.getValueOf("LienAmount") + "'";
          }

        tableName = "USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS";
        sWhereClause = "WI_NAME='" + processInstanceID + "'";
        inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), tableName,
          columnNames, columnValues, sWhereClause);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apUpdate for " + tableName + " Table : " + inputXML);
        String outputXml = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdate for " + tableName + " Table : " + outputXml);
        XMLParser sXMLParserChild = new XMLParser(outputXml);
        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainCode for apUpdate for " + tableName + " Table : " + StrMainCode);
        if("0".equalsIgnoreCase(StrMainCode))
        	RetVal = "Success";
        else 
        	RetVal = "Failure";
        /*sQuery="SELECT AgreementId,TotalOutstandingAmt,TotalNoOfInstalments,RemainingInstalments,SchemeCardProd,cifID FROM  USR_0_iRBL_INTERNALExpo_LOANDetails WHERE wi_NAMe='"+processInstanceID+"'";
        inputXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update " + inputXML);
        outputXML = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputXML: for USR_0_IRBL_FINANCIAL_ELIGIBILITY_CHECKS data update: " + outputXML);
        xmlParserData = new XMLParser(outputXML);
        iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
          {
            xmlDataExtTab = xmlParserData.getNextValueOf("Record");
            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

            //XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
            objWorkList = xmlParserData.createList("Records", "Record");
            tableName="USR_0_IRBL_LOANS_GRID_DTLS";
            columnNames="WI_NAME, AGREEMENT_NO, OUTSTANDING, NO_OF_EMIS_PAID, SCHEME_NAME, REMARKS";
            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
              {
               String columnValuesLoan="'" + processInstanceID + "','"+objWorkList.getVal("AgreementId")+"','"+objWorkList.getVal("TotalOutstandingAmt")+"','"+objWorkList.getVal("RemainingInstalments")+"','"+objWorkList.getVal("SchemeCardProd")+"','TEST'";
              CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnNames, columnValuesLoan, tableName);
              String outputXmlLoan = iRBLSysCheckIntegration.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
              iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
              XMLParser sXMLParserChildLoan = new XMLParser(outputXml);
              String StrMainCodeLoan = sXMLParserChild.getValueOf("MainCode");
              iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainCode for apUpdate for " + tableName + " Table : " + StrMainCode);
              }
            
          }*/
        
        /*********************************updating form data end*************************************/

       
        return RetVal;
      }

    public static String getDate(long minusMonth)
      {

        //return (LocalDate.now().minusMonths(minusMonth).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))).substring(0, 8) + "01";
    	return "";
      }


  }
