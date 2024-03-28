package com.newgen.iRBL.SysCheckIntegration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class ResponseParser
  {
 
    private static NGEjbClient ngEjbClientConnection;

    static
      {
        try
          {
            ngEjbClientConnection = NGEjbClient.getSharedInstance();
          }
        catch (NGException e)
          {
            e.printStackTrace();
          }
      }
    public static String getOutputXMLValues(String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name, String prod,
      String subprod, String cifId, String cust_type)

      {
    	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside getOutputXMLValues");
        String outputXMLHead = "";
        String outputXMLMsg = "";
        String returnDesc = "";
        String returnCode = "";
        String response = "";
        String returnType = "";
        String result_str = "";
        String MsgFormat = "";
        //String CompanyCIF = "Corporate_CIF";
        try
          {

            /*
             * String squery_comp =
             * "select case when COUNT(CompanyCIF)>0 then  ISNULL(CompanyCIF,'') else '' end as CompanyCIF  from NG_RLOS_GR_CompanyDetails where comp_winame ='"
             * +wi_name+"' and applicantCategory='Business' group by CompanyCIF"; String
             * strInputXml_comp = ExecuteQuery_APSelect(squery_comp,cabinetName,sessionId); String
             * strOutputXml_comp = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort,
             * appServerType, strInputXml_comp);
             * 
             * iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Out put XML of company : "+strOutputXml_comp);
             * if(!"".equalsIgnoreCase(strOutputXml_comp)){ String row_count_str_comp =
             * strOutputXml_comp.substring(strOutputXml_comp.indexOf("<TotalRetrieved>")+16,
             * strOutputXml_comp.indexOf("</TotalRetrieved>")); int result_count_comp =
             * Integer.parseInt(row_count_str_comp); if (result_count_comp>0){
             * CompanyCIF=strOutputXml_comp.substring(strOutputXml_comp.indexOf("<CompanyCIF>")+12,
             * strOutputXml_comp.indexOf("</CompanyCIF>")); } else{ CompanyCIF=""; } }
             */
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in custexpose_output_PL company : " + e.getMessage());
            e.printStackTrace();

          }
        try
          {

            if (parseXml.indexOf("<EE_EAI_HEADER>") > -1)
              {
                outputXMLHead = parseXml.substring(parseXml.indexOf("<EE_EAI_HEADER>"), parseXml.indexOf("</EE_EAI_HEADER>") + 16);
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RLOSCommon valueSetCustomer"+ outputXMLHead);
              }
            if (outputXMLHead.indexOf("<MsgFormat>") > -1)
              {
                response = outputXMLHead.substring(outputXMLHead.indexOf("<MsgFormat>") + 11, outputXMLHead.indexOf("</MsgFormat>"));
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$response "+response);
              }
            if (outputXMLHead.indexOf("<ReturnDesc>") > -1)
              {
                returnDesc = outputXMLHead.substring(outputXMLHead.indexOf("<ReturnDesc>") + 12, outputXMLHead.indexOf("</ReturnDesc>"));
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$returnDesc "+returnDesc);
              }
            if (outputXMLHead.indexOf("<ReturnCode>") > -1)
              {
                returnCode = outputXMLHead.substring(outputXMLHead.indexOf("<ReturnCode>") + 12, outputXMLHead.indexOf("</ReturnCode>"));
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$returnCode "+returnCode);
              }
            //Deepak changes done for Commented for PCSP-526
            if (parseXml.indexOf("<RequestType>") > -1)
              {
                returnType = parseXml.substring(parseXml.indexOf("<RequestType>") + 13, parseXml.indexOf("</RequestType>"));

                if ("0000".equalsIgnoreCase(returnCode) || ("ExternalExposure".equalsIgnoreCase(returnType) && ("B003".equalsIgnoreCase(returnCode) || "B005".equalsIgnoreCase(returnCode))))
                  {
                    if ("InternalExposure".equalsIgnoreCase(returnType))
                      {
                        result_str = parseInternalExposure(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, prod, subprod, cifId, cust_type);
                      }
                    else if ("ExternalExposure".equalsIgnoreCase(returnType))
                      {
                         result_str = parseExternalExposure(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, prod, subprod, cifId, cust_type);
                      }
                    else if ("CollectionsSummary".equalsIgnoreCase(returnType))
                      {
                        result_str = parseCollectionSummary(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, prod, subprod, cifId, cust_type);
                      }
                  }

                /*
                 * if(!"0000".equalsIgnoreCase(returnCode)) { String
                 * errorQuery="SELECT isnull((SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE  error_code='"
                 * +returnCode+"'),(SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE error_code='DEFAULT')) As Alert"
                 * ; String strInputXml = ExecuteQuery_APSelect(errorQuery,cabinetName,sessionId);
                 * String strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP,
                 * wrapperPort, appServerType, strInputXml);
                 * result_str=strOutputXml.substring(strOutputXml.indexOf("<Alert>")+"</Alert>".
                 * length()-1,strOutputXml.indexOf("</Alert>")); return result_str; }
                 */
              }

            //added
            if (parseXml.indexOf("<MsgFormat>") > -1)
              {
                returnType = parseXml.substring(parseXml.indexOf("<MsgFormat>") + 11, parseXml.indexOf("</MsgFormat>"));
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$MsgFormat "+returnType);
                //Added By Prabhakar
                /*
                 * if(!"0000".equalsIgnoreCase(returnCode)) { String
                 * errorQuery="SELECT isnull((SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE  error_code='"
                 * +returnCode+"'),(SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE error_code='DEFAULT')) As Alert"
                 * ; //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("@@@@@@@@@@@@@@  "+errorQuery); String strInputXml =
                 * ExecuteQuery_APSelect(errorQuery,cabinetName,sessionId); String strOutputXml =
                 * NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType,
                 * strInputXml); //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Deepak Out put: "+strOutputXml);
                 * result_str=strOutputXml.substring(strOutputXml.indexOf("<Alert>")+"</Alert>".
                 * length()-1,strOutputXml.indexOf("</Alert>"));
                 * //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("@@@@@@@@@@@@@@  "+result_str); return result_str; }
                 */
                if (returnType.equalsIgnoreCase("CARD_INSTALLMENT_DETAILS"))
                  {
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: getOutputXMLValuesresult:CardInstallmentDetailsFlag inside card installment123");
                    result_str =
                      parseCardInstallmentsDetails(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, prod, subprod, cifId);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: getOutputXMLValuesresult:CardInstallmentDetailsFlag "+result_str);
                  }
              }
            //ended


            if (parseXml.indexOf("<OperationType>") > -1)
              {
                returnType = parseXml.substring(parseXml.indexOf("<OperationType>") + 15, parseXml.indexOf("</OperationType>"));
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$returnType "+returnType);
                //Added By Prabhakar
                /*
                 * if(!"0000".equalsIgnoreCase(returnCode)) {
                 * 
                 * String
                 * errorQuery="SELECT isnull((SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE  error_code='"
                 * +returnCode+"'),(SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE error_code='DEFAULT')) As Alert"
                 * ; //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("@@@@@@@@@@@@@@  "+errorQuery); String strInputXml =
                 * ExecuteQuery_APSelect(errorQuery,cabinetName,sessionId); String strOutputXml =
                 * NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType,
                 * strInputXml);
                 * result_str=strOutputXml.substring(strOutputXml.indexOf("<alert>")+"</alert>".
                 * length()-1,strOutputXml.indexOf("</alert>"));
                 * //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("select result is: "+result_str);
                 * //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("@@@@@@@@@@@@@@  "+result_str); return result_str; }
                 */
                if (returnType.equalsIgnoreCase("TRANSUM"))
                  {
                    result_str = parseTRANSUM(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("AVGBALDET"))
                  {
                    result_str = parseAVGBALDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("RETURNDET"))
                  {
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Hi");
                    result_str = parseRETURNDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("LIENDET"))
                  {
                    result_str = parseLIENDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("SIDET"))
                  {
                    result_str = parseSIDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }
                else if (returnType.equalsIgnoreCase("SALDET"))
                  {
                    result_str = parseSALDET(returnType, parseXml, wrapperIP, wrapperPort, sessionId, cabinetName, wi_name, cifId);
                  }

              }
            returnType = parseXml.substring(parseXml.indexOf("<MsgFormat>") + 11, parseXml.indexOf("</MsgFormat>"));
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$returnType result_strresult_strresult_str"+returnType);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$MsgFormat "+returnType);
            if (returnType.equalsIgnoreCase("FINANCIAL_SUMMARY") && (result_str.equalsIgnoreCase("")))
              {
                result_str = returnCode;
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$result_str result_strresult_strresult_str"+result_str);
              }
            //ended

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in getOutputXMLValues: " + e.getMessage());
            e.printStackTrace();
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in getOutputXMLValues method:  "+e.getMessage());
            result_str = "Failure";
          }
        return (result_str);
      }

    public static String parseInternalExposure(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String prod, String subprod, String cifId, String cust_type)
      {
        String flag1 = "";
        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String result = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parseInternalExposure");
        try
          {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList_loan = doc.getElementsByTagName("CustomerExposureResponse");


            for (int i = 0; i < nList_loan.getLength(); i++)
              {
                Node node = nList_loan.item(i);
                Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                DOMImplementationLS abc = (DOMImplementationLS) newXmlDocument.getImplementation();
                LSSerializer lsSerializer = abc.createLSSerializer();

                Element root = newXmlDocument.createElement("root");
                newXmlDocument.appendChild(root);
                root.appendChild(newXmlDocument.importNode(node, true));
                String n_parseXml = lsSerializer.writeToString(newXmlDocument);
                n_parseXml = n_parseXml.substring(n_parseXml.indexOf("<root>") + 6, n_parseXml.indexOf("</root>"));
                cifId =
                  (n_parseXml.contains("<CustIdValue>")) ? n_parseXml.substring(n_parseXml.indexOf("<CustIdValue>") + "</CustIdValue>".length() - 1, n_parseXml.indexOf("</CustIdValue>")) : cifId;

               
                tagName = "LoanDetails";
                subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                sTableName = "USR_0_iRBL_InternalExpo_LoanDetails";
                subtag_single = "";
                flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
                   cust_type, subtag_single);

                if (flag1.equalsIgnoreCase("true"))
                  {
                    tagName = "CardDetails";
                    subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                    sTableName = "USR_0_iRBL_InternalExpo_CardDetails";
                    subtag_single = "";
                    flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId, cust_type, subtag_single);

                    if (flag1.equalsIgnoreCase("true"))
                      {
                        tagName = "InvestmentDetails";
                        subTagName = "AmountDtls";
                        sTableName = "USR_0_iRBL_InternalExpo_InvestmentDetails";
                        subtag_single = "";
                        flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                          cifId, cust_type, subtag_single);

                        if (flag1.equalsIgnoreCase("true"))
                          {
                            tagName = "AcctDetails";
                            subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                            sTableName = "USR_0_iRBL_InternalExpo_AcctDetails";
                            subtag_single = "ODDetails";
                            flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                              cifId, cust_type, subtag_single);
                            if (flag1.equalsIgnoreCase("true"))
                              {
                                tagName = "Derived";
                                subTagName = "";
                                sTableName = "USR_0_iRBL_InternalExpo_Derived";
                                subtag_single = "";
                                flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod,
                                  subprod, cifId, cust_type, subtag_single);
                                if (flag1.equalsIgnoreCase("true"))
                                  {
                                    tagName = "RecordDestribution";
                                    subTagName = "";
                                    sTableName = "USR_0_iRBL_InternalExpo_RecordDestribution";
                                    subtag_single = "";
                                    flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod,
                                      subprod, cifId, cust_type, subtag_single);
                                    //Deepak 22 july 2019 new condition added to save custinfo
                                    if (flag1.equalsIgnoreCase("true"))
                                      {
                                        tagName = "CustInfo";
                                        subTagName = "";
                                        sTableName = "USR_0_iRBL_InternalExpo_CustInfo";
                                        subtag_single = "";
                                        flag1 = commonParseProduct(n_parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName,
                                          prod, subprod, cifId, cust_type, subtag_single);
                                      }
                                    else
                                      {
                                        flag1 = "false";
                                      }
                                  }
                                else
                                  {
                                    flag1 = "false";
                                  }
                              }
                            else
                              {
                                flag1 = "false";
                              }
                          }
                        else
                          {
                            flag1 = "false";
                          }
                      }
                    else
                      {
                        flag1 = "false";
                      }
                  }
                else
                  {
                    flag1 = "false";
                  }
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return flag1;
      }


    public static String parseExternalExposure(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String prod, String subprod, String cifId, String cust_type)
      {
        String flag1 = "";
        try
          {
            String ReturnCode = "";
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside parseExternalExposure: ");	
            if (parseXml.indexOf("<ReturnCode>") > -1)
              {
                ReturnCode = parseXml.substring(parseXml.indexOf("<ReturnCode>") + 12, parseXml.indexOf("</ReturnCode>"));
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("$$return Code "+ReturnCode);
              }

            //Commented for PCSP-526
            /*
             * if(ReturnCode.equalsIgnoreCase("B003")) {
             * //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("AECB:No record found!!"); return "B003"; }
             */
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseExternalExposure: "+wrapperIP);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseExternalExposure: "+wrapperPort);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseExternalExposure: "+sessionId);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseExternalExposure: "+cabinetName);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseExternalExposure: "+wi_name);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseExternalExposure: "+appServerType);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseExternalExposure: "+parseXml);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseExternalExposure: "+returnType);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: "+cifId);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parentWiName jsp: parseExternalExposure: "+parentWiName);

            String tagName = "";
            String subTagName = "";
            String sTableName = "";

            String subtag_single = "";

            tagName = "ChequeDetails";
            subTagName = "";
            sTableName = "USR_0_iRBL_ExternalExpo_ChequeDetails";
            flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId, cust_type, subtag_single);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted"+flag1);

            if (flag1.equalsIgnoreCase("true"))
              {
                tagName = "LoanDetails";
                subTagName = "KeyDt,AmountDtls";
                sTableName = "USR_0_iRBL_ExternalExpo_LoanDetails";
                flag1 =commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,cust_type, subtag_single);
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted1"+flag1);

                if (flag1.equalsIgnoreCase("true"))
                  {
                    tagName = "CardDetails";
                    subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                    sTableName = "USR_0_iRBL_ExternalExpo_CardDetails";
                    flag1 =commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,cust_type, subtag_single);
                      
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted2"+flag1);
                    if (flag1.equalsIgnoreCase("true"))
                      {
                        tagName = "Derived";
                        subTagName = "";
                        sTableName = "USR_0_iRBL_InternalExpo_Derived";
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Hi");
                        flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
                           cust_type, subtag_single);
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or NG_rlos_custexpose_Derived"+flag1);
                        if (flag1.equalsIgnoreCase("true"))
                          {
                            tagName = "RecordDestribution";
                            subTagName = "";
                            sTableName = "USR_0_iRBL_InternalExpo_RecordDestribution";
                            flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                              cifId, cust_type, subtag_single);
                            if (flag1.equalsIgnoreCase("true"))
                              {
                                tagName = "AcctDetails";
                                subTagName = "KeyDt,AmountDtls";
                                sTableName = "USR_0_iRBL_ExternalExpo_AccountDetails";
                                flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                                  cifId, cust_type, subtag_single);
                                if (flag1.equalsIgnoreCase("true"))
                                  {
                                    tagName = "ServicesDetails";
                                    subTagName = "KeyDt,AmountDtls";
                                    sTableName = "USR_0_iRBL_ExternalExpo_ServicesDetails";
                                    flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod,
                                      subprod, cifId, cust_type, subtag_single);
                                  }
                                else
                                  {
                                    flag1 = "false";
                                  }
                              }
                            else
                              {
                                flag1 = "false";
                              }
                          }
                        else
                          {
                            flag1 = "false";
                          }
                      }
                    else
                      {
                        flag1 = "false";
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or NG_rlos_custexpose_Derived"+flag1);
                      }
                  }
                else
                  {
                    flag1 = "false";
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or insertedfalse"+flag1);
                  }


              }
            else
              {
                flag1 = "false";
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or insertedfalse1"+flag1);
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }

        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseExternalExposure: updated or inserted final value"+flag1);
        return flag1;
      }

    public static String parseCollectionSummary(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String prod, String subprod, String cifId, String cust_type)
      {
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseCollectionSummary: "+wrapperIP);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseCollectionSummary: "+wrapperPort);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseCollectionSummary: "+sessionId);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseCollectionSummary: "+cabinetName);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseCollectionSummary: "+wi_name);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseCollectionSummary: "+appServerType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseCollectionSummary: "+parseXml);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseCollectionSummary: "+returnType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseCollectionSummary: "+cifId);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parentWiName jsp: parseCollectionSummary: "+parentWiName);
        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String result = "";
        String flag1 = "";
        try
          {

            //Deepak code commented method changed with new subtag_single param 23jan2018
            String subtag_single = "";

            tagName = "LoanDetails";
            subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
            sTableName = "USR_0_iRBL_InternalExpo_LoanDetails";
            flag1 = commonParseProduct_collection(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
              subtag_single, cust_type);

            if (flag1.equalsIgnoreCase("true"))
              {
                tagName = "CardDetails";
                subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
                sTableName = "USR_0_iRBL_InternalExpo_CardDetails";
                flag1 = commonParseProduct_collection(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod,
                  cifId, subtag_single, cust_type);
                if (flag1.equalsIgnoreCase("true"))
                  {
                    tagName = "Derived";
                    subTagName = "";
                    sTableName = "USR_0_iRBL_InternalExpo_Derived";
                    flag1 = commonParseProduct(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, prod, subprod, cifId,
                      cust_type, subtag_single);

                  }
                else
                  {
                    flag1 = "false";
                  }
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }

        return flag1;
      }

    public static String parseCardInstallmentsDetails(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
       String prod, String subprod, String cifId)
      {
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseCardInstallmentsDetails: "+wrapperIP);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseCardInstallmentsDetails: "+wrapperPort);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseCardInstallmentsDetails: "+sessionId);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseCardInstallmentsDetails: "+cabinetName);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseCardInstallmentsDetails: "+wi_name);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseCardInstallmentsDetails: "+appServerType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseCardInstallmentsDetails: "+parseXml);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseCardInstallmentsDetails: "+returnType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseCardInstallmentsDetails: "+cifId);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseCardInstallmentsDetails:CardNumber "+CardNumber);

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String result = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        try
          {
            tagName = "TransactionDetailsRec";
            subTagName = "";
            sTableName = "USR_0_iRBL_InternalExpo_CardInstallmentDetails";
            flag1 = commonParseFinance_CardInstallment(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName,
             subtag_single);

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }

        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: CardInstallmentDetailsResponse: "+flag1);


        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: CardInstallmentDetailsResponse final value: "+flag1);
        return flag1;
      }


    public static String parseTRANSUM(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
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

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        try
          {
            tagName = "TxnSummaryDtls";
            subTagName = "";
            sTableName = "USR_0_iRBL_FinancialSummary_TxnSummary";
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String parseAVGBALDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
      {
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseAVGBALDET: "+wrapperIP);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseAVGBALDET: "+wrapperPort);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseAVGBALDET: "+sessionId);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseAVGBALDET: "+cabinetName);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseAVGBALDET: "+wi_name);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseAVGBALDET: "+appServerType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseAVGBALDET: "+parseXml);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseAVGBALDET: "+returnType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseAVGBALDET: "+cifId);

        String flag1 = "";
        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";

        try
          {
            tagName = "FinancialSummaryRes";
            subTagName = "AvgBalanceDtls";
            sTableName = "USR_0_iRBL_FinancialSummary_AvgBalanceDtls";
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String parseRETURNDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
      {
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseRETURNDET: "+wrapperIP);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseRETURNDET: "+wrapperPort);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseRETURNDET: "+sessionId);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseRETURNDET: "+cabinetName);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseRETURNDET: "+wi_name);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseRETURNDET: "+appServerType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseRETURNDET: "+parseXml);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseRETURNDET: "+returnType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseRETURNDET: "+cifId);

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        try
          {
            tagName = "ReturnsDtls";
            subTagName = "";
            sTableName = "USR_0_iRBL_FinancialSummary_ReturnsDtls";
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;

      }

    public static String parseLIENDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name,
      String cifId)
      {
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperIP jsp: parseLIENDET: "+wrapperIP);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wrapperPort jsp: parseLIENDET: "+wrapperPort);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sessionId jsp: parseLIENDET: "+sessionId);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cabinetName jsp: parseLIENDET: "+cabinetName);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("wi_name jsp: parseLIENDET: "+wi_name);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("appServerType jsp: parseLIENDET: "+appServerType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parseXml jsp: parseLIENDET: "+parseXml);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: parseLIENDET: "+returnType);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("cifId jsp: parseLIENDET: "+cifId);

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        tagName = "LienDetails";
        subTagName = "";
        sTableName = "USR_0_iRBL_FinancialSummary_LienDetails";
        try
          {
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String parseSIDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name, String cifId)
      {
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside parseSIDET: ");


        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";

        tagName = "SIDetails";
        subTagName = "";
        sTableName = "USR_0_iRBL_FinancialSummary_SiDtls";
        try
          {
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            if (flag1.equalsIgnoreCase("true"))
              {
                flag1 = "true";
              }
            else
              {
                flag1 = "false";
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;

      }

    public static String parseSALDET(String returnType, String parseXml, String wrapperIP, String wrapperPort, String sessionId, String cabinetName, String wi_name, String cifId)
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

        String tagName = "";
        String subTagName = "";
        String sTableName = "";
        String sParentTagName = "";
        String flag1 = "";
        //Deepak code commented method changed with new subtag_single param 23jan2018
        String subtag_single = "";
        tagName = "SalDetails";
        subTagName = "";
        sTableName = "USR_0_iRBL_FinancialSummary_SalTxnDetails";

        if (parseXml.indexOf("<AcctId>") > -1)
          {
            String acc_no = parseXml.substring(parseXml.indexOf("<AcctId>") + "</AcctId>".length() - 1, parseXml.indexOf("</AcctId>"));
            String sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = '" + acc_no + "'";
            String strInputXml = ExecuteQuery_APdelete(sTableName, sWhere, cabinetName, sessionId);
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml delete returndtls " + strInputXml);
            try
              {
                //String strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                String strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml delete SalDetails: " + strOutputXml);
              }
            catch (NGException e)
              {
                e.printStackTrace();
              }
            catch (Exception ex)
              {
                ex.printStackTrace();
              }


          }
        try
          {
            flag1 = commonParseFinance(parseXml, tagName, wi_name, returnType, sTableName, wrapperIP, wrapperPort, sessionId, cabinetName, subTagName, subtag_single);
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("return flag1 jsp: parseSALDET: "+flag1);
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseInternalExposure: " + e.getMessage());
            e.printStackTrace();
            flag1 = "false";
          }
        return flag1;
      }

    public static String commonParseProduct(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
      String cabinetName, String subTagName, String prod, String subprod, String cifId, String cust_type, String subtag_single)
      {
        String retVal = "";

        try
          {
            if (!parseXml.contains(tagName))
              {
                return "true";
              }
            else
              {
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside commonParseProduct for: "+sTableName);
                String[] valueArr = null;
                String strInputXml = "";
                String strOutputXml = "";
                String columnName = "";
                String columnValues = "";
                String tagNameU = "";
                String subTagNameU = "";
                String subTagNameU_2 = "";
                String mainCode = "";
                String sWhere = "";
                String row_updated = "";
                String selectdata = "";
                String sQry = "";
                String ReportUrl = "";
                String NoOfContracts = "";
                String ECRN = "";
                String BorrowingCustomer = "";
                String FullNm = "";
                String TotalOutstanding = "";
                String TotalOverdue = "";

                String companyUpdateQuery = "";
                String companiestobeUpdated = "";
                boolean stopIndividualToInsert = false;
                String referenceNo = "";
                String scoreInfo = "";
                String Aecb_Score = "";
                String range = "";

                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tagName jsp: commonParse: "+tagName);
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("subTagName jsp: commonParse: "+subTagName);
                //Parsing AECB score, range and Reference No. for 2.1 start, Added by Shivang

                referenceNo =
                  (parseXml.contains("<ReferenceNumber>")) ? parseXml.substring(parseXml.indexOf("<ReferenceNumber>") + "</ReferenceNumber>".length() - 1, parseXml.indexOf("</ReferenceNumber>")) : "";
                if (parseXml.contains("<ScoreInfo>"))
                  {
                    scoreInfo = parseXml.substring(parseXml.indexOf("<ScoreInfo>") + "</ScoreInfo>".length() - 1, parseXml.indexOf("</ScoreInfo>"));
                    Aecb_Score = (scoreInfo.contains("<Value>")) ? scoreInfo.substring(scoreInfo.indexOf("<Value>") + "</Value>".length() - 1, scoreInfo.indexOf("</Value>")) : "";
                    range = (scoreInfo.contains("<Range>")) ? scoreInfo.substring(scoreInfo.indexOf("<Range>") + "</Range>".length() - 1, scoreInfo.indexOf("</Range>")) : "";
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("parsexml jsp: commonParse: AECB Score: " + Aecb_Score + " Range: " + range);
                  }

                //Parsing AECB score, range and Reference No. for 2.1 end, Added by Shivang
                //Deepak 23 Dec changes done to save updated Rerport URL in DB
                ReportUrl = (parseXml.contains("<ReportUrl>")) ? parseXml.substring(parseXml.indexOf("<ReportUrl>") + "</ReportUrl>".length() - 1, parseXml.indexOf("</ReportUrl>")) : "";
                //cifId=(parseXml.contains("<CustIdValue>")) ? parseXml.substring(parseXml.indexOf("<CustIdValue>")+"</CustIdValue>".length()-1,parseXml.indexOf("</CustIdValue>")):"";
                FullNm = (parseXml.contains("<FullNm>")) ? parseXml.substring(parseXml.indexOf("<FullNm>") + "</FullNm>".length() - 1, parseXml.indexOf("</FullNm>")) : "";
                TotalOutstanding = (parseXml.contains("<TotalOutstanding>"))
                  ? parseXml.substring(parseXml.indexOf("<TotalOutstanding>") + "</TotalOutstanding>".length() - 1, parseXml.indexOf("</TotalOutstanding>")) : "";
                TotalOverdue =
                  (parseXml.contains("<TotalOverdue>")) ? parseXml.substring(parseXml.indexOf("<TotalOverdue>") + "</TotalOverdue>".length() - 1, parseXml.indexOf("</TotalOverdue>")) : "";
                NoOfContracts =
                  (parseXml.contains("<NoOfContracts>")) ? parseXml.substring(parseXml.indexOf("<NoOfContracts>") + "</NoOfContracts>".length() - 1, parseXml.indexOf("</NoOfContracts>")) : "";
                ECRN = (parseXml.contains("<ECRN>")) ? parseXml.substring(parseXml.indexOf("<ECRN>") + "</ECRN>".length() - 1, parseXml.indexOf("</ECRN>")) : "";
                BorrowingCustomer = (parseXml.contains("<BorrowingCustomer>"))
                  ? parseXml.substring(parseXml.indexOf("<BorrowingCustomer>") + "</BorrowingCustomer>".length() - 1, parseXml.indexOf("</BorrowingCustomer>")) : "";


                Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
                tagValuesMap = getTagDataParent_deep(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                // String colValue="";
                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values" + entry.getValue());

                    //columnValues = valueArr[1].spilt(",");
                    // columnValues=columnValues+",'"+getCellData(SheetName1, rCnt, cCnt)+"'";
                    //colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
                    columnName = valueArr[0] + ",CifId,Request_Type,Product_Type,CardType,Wi_Name";
                    columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + prod + "','" + subprod + "','" + wi_name + "'";



                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnName commonParse" + columnName);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnValues commonParse" + columnValues);
                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardDetails"))
                      {
                        columnName = valueArr[0] + ",Liability_type,Request_Type,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + cifId + "','" + wi_name + "'";
                        sWhere = "CardEmbossNum = '" + entry.getKey() + "' AND wi_name='" + wi_name + "' And Liability_type ='" + cust_type + "'";
                        sQry =
                          "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Individual_CIF' ";
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "sQry sQry" + sQry);
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery =
                              "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }
                        if (parseXml.contains("<LinkedCIFs>"))
                          {
                            parseLinkedCif(parseXml, sTableName, cifId, wi_name, entry.getKey(), cust_type, "Card", cabinetName, sessionId, wrapperIP, wrapperPort);
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_LoanDetails"))
                      {
                        columnName = valueArr[0] + ",Liability_type,Request_Type,Product_Type,CardType,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + prod + "','" + subprod + "','" + cifId + "','" + wi_name + "'";
                        columnName = columnName.replace("OutStandingAmt", "TotalOutStandingAmt");
                        sWhere = "AgreementId = '" + entry.getKey() + "' AND wi_name='" + wi_name + "' And Liability_type ='" + cust_type + "'";
                        sQry =
                          "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And  AgreementId = '" + entry.getKey() + "' And Liability_type ='Individual_CIF'";
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "sQry  loan sQry" + sQry);
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery =
                              "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And AgreementId = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }
                        if (parseXml.contains("<LinkedCIFs>"))
                          {
                            parseLinkedCif(parseXml, sTableName, cifId, wi_name, entry.getKey(), cust_type, "Loan", cabinetName, sessionId, wrapperIP, wrapperPort);
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_ExternalExpo_ChequeDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        sWhere = "Wi_Name='" + wi_name + "' AND ChqType = '" + entry.getKey() + "'";
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_ExternalExpo_LoanDetails"))
                      {
                        String History = parseHistoryUtilization(parseXml, entry.getKey(), "LoanDetails", "<History>", "</History>");
                        History = History.replace("\n", "").replace("\r", "");
                        String Utilization = parseHistoryUtilization(parseXml, entry.getKey(), "LoanDetails", "<Utilizations24Months>", "</Utilizations24Months>");
                        Utilization = Utilization.replace("\n", "").replace("\r", "");
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside parseHistoryUtilization" + History);
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside parseHistoryUtilization" + Utilization);
                        columnName = valueArr[0] + ",Liability_type,Request_Type,Product_Type,CardType,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + prod + "','" + subprod + "','" + cifId + "','" + wi_name + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("LoanType".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);

                              }
                            if ("History".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + History + "'");

                              }
                            if ("Utilizations24Months".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], "'" + Utilization + "'");

                              }
                          }
                        columnName = columnName.replace("OutStanding Balance", "OutStanding_Balance");
                        columnName = columnName.replace("LastUpdateDate", "datelastupdated");
                        columnName = columnName.replace("Total Amount", "Total_Amount");
                        columnName = columnName.replace("Payments Amount", "Payments_Amount");
                        columnName = columnName.replace("Overdue Amount", "Overdue_Amount");
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside parseHistoryUtilization" + columnName);
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside parseHistoryUtilization" + columnValues);
                        //sWhere="Wi_Name='"+parentWiName+"' AND AgreementId = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"'";
                        sWhere = "Wi_Name='" + wi_name + "' AND AgreementId = '" + entry.getKey() + "'";
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_ExternalExpo_CardDetails"))
                      {
                        String History = parseHistoryUtilization(parseXml, entry.getKey(), "CardDetails", "<History>", "</History>");
                        History = History.replace("\n", "").replace("\r", "");
                        String Utilization = parseHistoryUtilization(parseXml, entry.getKey(), "CardDetails", "<Utilizations24Months>", "</Utilizations24Months>");
                        Utilization = Utilization.replace("\n", "").replace("\r", "");
                        columnName = valueArr[0] + ",Liability_type,Request_Type,Product_Type,sub_product_type,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cust_type + "','" + returnType + "','" + prod + "','" + subprod + "','" + cifId + "','" + wi_name + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("CardType".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);

                              }
                            if ("History".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + History + "'");

                              }
                            if ("Utilizations24Months".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag name" + columnName_arr[arrlen]);
                                // iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "inside loan desc tag value" + columnValues_arr[arrlen]);
                                //String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP,wrapperPort, appServerType);
                                columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + Utilization + "'");

                              }
                          }
                        sWhere = "Wi_Name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "'";
                        //sWhere="Wi_Name='"+parentWiName+"' AND CardEmbossNum = '"+entry.getKey()+"' AND Child_Wi='"+wi_name+"'";
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_Derived"))
                      {
                        //Deepak 23 Dec changes done to save updated Rerport URL in DB.
                        columnName = valueArr[0] + ",Range,Request_Type,CifId,FullNm,TotalOutstanding,TotalOverdue,NoOfContracts,ReportURL,ReferenceNo,AECB_Score,Wi_Name";
                        columnValues = valueArr[1] + ",'" + range + "','" + returnType + "','" + cifId + "','" + FullNm + "','" + TotalOutstanding + "','" + TotalOverdue + "','" + NoOfContracts
                          + "','" + ReportUrl + "','"+ referenceNo + "','" + Aecb_Score + "','" + wi_name + "'";
                        sWhere = "Wi_Name='" + wi_name + "' AND Request_Type = '" + returnType + "' and cifid='" + cifId + "'";
                      }
                    //Changes Done to save data in NG_RLOS_CUSTEXPOSE_RecordDestribution table on 14th sept by Aman
                    //Deepak Child workitem added in both columnName & columnValues to get it saved in backend - 8 July 2019.
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_RecordDestribution"))
                      {
                        columnName = valueArr[0] + ",Request_Type,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        sWhere = "Wi_Name='" + wi_name + "' AND ContractType = '" + entry.getKey() + "' AND CifId='" + cifId + "'";
                      }
                    //Changes Done to save data in NG_RLOS_CUSTEXPOSE_RecordDestribution table on 14th sept by Aman
                    //Deepak Child workitem added in both columnName & columnValues to get it saved in backend - 8 July 2019.
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_ExternalExpo_AccountDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("AcctType".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
                                break;
                              }
                          }
                        sWhere = "Wi_Name='" + wi_name + "' AND AcctId = '" + entry.getKey() + "'";//Cif_id removed
                      }
                    //Deepak changes done for Service details
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_ExternalExpo_ServicesDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name  + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");

                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("ServiceName".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag name" + columnName_arr[arrlen]);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside loan desc tag value" + columnValues_arr[arrlen]);
                                String loan_desc = get_loanDesc(columnValues_arr[arrlen], cabinetName, sessionId, wrapperIP, wrapperPort);
                                columnValues = columnValues.replaceFirst(columnValues_arr[arrlen], loan_desc);
                                break;
                              }
                          }
                        sWhere = "Wi_Name='" + wi_name + "' AND ServiceID = '" + entry.getKey() + "'";
                      }
                    //below changes Done to save AccountType in ng_RLOS_CUSTEXPOSE_AcctDetails table on 29th Dec by Disha
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_AcctDetails"))
                      {
                        String CreditGrade =
                          (parseXml.contains("<CreditGrade>")) ? parseXml.substring(parseXml.indexOf("<CreditGrade>") + "</CreditGrade>".length() - 1, parseXml.indexOf("</CreditGrade>")) : "";
                        //PCASP-2833 
                        String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>") + "</IsDirect>".length() - 1, parseXml.indexOf("</IsDirect>")) : "";
                        columnName = valueArr[0] + ",isDirect,Request_Type,CifId,CreditGrade,Account_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + isDirect + "','" + returnType + "','" + cifId + "','" + CreditGrade + "','" + cust_type + "','" +  wi_name + "'";
                        sWhere = "Request_Type='" + returnType + "' AND AcctId = '" + entry.getKey() + "' AND wi_name='" + wi_name + "' AND Account_Type = '" + cust_type + "'";
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        String LimitSactionDate = "";
                        for (int arrlen = 0; arrlen < columnName_arr.length; arrlen++)
                          {
                            if ("LimitSactionDate".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside LimitSactionDate tag name" + columnName_arr[arrlen]);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside LimitSactionDate value" + columnValues_arr[arrlen]);
                                LimitSactionDate = columnValues_arr[arrlen];
                              }
                            if ("MonthsOnBook".equalsIgnoreCase(columnName_arr[arrlen]))
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside MonthsOnBook tag name" + columnName_arr[arrlen]);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside MonthsOnBook value" + columnValues_arr[arrlen]);
                                if (!LimitSactionDate.equals(""))
                                  {
                                    String MOB = get_Mob_forOD(LimitSactionDate);
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside MonthsOnBook value" + MOB);
                                    if (!MOB.equalsIgnoreCase("Invalid"))
                                      {
                                        columnValues = columnValues.replace(columnValues_arr[arrlen], "'" + MOB + "'");
                                      }
                                  }

                              }
                          }
                        //change by saurabh on 24th Feb for skipping employer accounts to save.
                        sQry = "Select count(*) as selectdata from NG_RLOS_ALOC_OFFLINE_DATA where CIF_ID ='Nikhil123'";
                        if (parseXml.contains("<LinkedCIFs>"))
                          {
                            parseLinkedCif(parseXml, sTableName, cifId, wi_name, entry.getKey(), cust_type, "Account", cabinetName, sessionId, wrapperIP, wrapperPort);
                          }
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "sQry  loan sQry" + sQry);    
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_InvestmentDetails"))
                      {

                        columnName = valueArr[0] + ",CifId,Request_Type,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name  + "'";
                        sWhere = "Request_Type='" + returnType + "' AND wi_name='" + wi_name + "' and InvestmentID='" + entry.getKey() + "'";

                      }
                    //above changes Done to save AccountType in ng_RLOS_CUSTEXPOSE_AcctDetails table on 29th Dec by Disha
                    //Deepak 22 july 2019 new condition added to save custinfo
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CustInfo"))
                      {
                        String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>") + "</IsDirect>".length() - 1, parseXml.indexOf("</IsDirect>")) : "";
                        columnName = valueArr[0] + ",isDirect,Request_Type,CifId,Wi_Name";
                        columnValues = valueArr[1] + ",'" + isDirect +"','" + returnType + "','" + cifId + "','" + wi_name  + "'";
                        sWhere = "wi_name='" + wi_name + "' AND Request_Type = '" + returnType + "' AND CifId = '" + cifId + "'";
                      }
                    else
                      {
                        sWhere = "Request_Type='" + returnType + "' AND wi_name='" + wi_name + "'";
                      }

                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml update for "+sTableName+" table: " + strInputXml);
                    try
                      {
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Hi");
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);;

                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml update for "+sTableName+" table: "+strOutputXml);
                      }
                    catch (NGException e)
                      {
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception update for "+sTableName+" table: " + e.getMessage());
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception update for "+sTableName+" table: " + ex.getMessage());
                        ex.printStackTrace();
                      }

                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("maincode update for "+sTableName+" table:  --> "+mainCode);
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("row updated update for "+sTableName+" table: --> "+row_updated);
                    if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
                      {   iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sQry sQry sQry");
                        if (!sQry.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(sQry, cabinetName, sessionId);
                            try
                              {
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                              }
                            catch (NGException e)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception select for "+sTableName+" table sQry sQry sQry: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception select for "+sTableName+" table sQry sQry sQry: " + ex.getMessage());
                                ex.printStackTrace();
                              }
                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("maincode select for "+sTableName+" table sQry sQry sQry --> "+mainCode);
                            selectdata = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("selectdata select for "+sTableName+" table sQry sQry sQry--> "+selectdata);
                          }
                        if (!companyUpdateQuery.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(companyUpdateQuery, cabinetName, sessionId);
                            try
                              {
                            	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("companyUpdateQuery select for "+sTableName+" table: "+strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" companyUpdateQuery select for "+sTableName+" table: "+strOutputXml);
                              }
                            catch (NGException e)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception companyUpdateQuery select for "+sTableName+" table: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception companyUpdateQuery select for "+sTableName+" table: " + ex.getMessage());
                                ex.printStackTrace();
                              }

                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("maincode companyUpdateQuery select for "+sTableName+" table --> "+mainCode);

                            companiestobeUpdated = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("selectdata companyUpdateQuery select for "+sTableName+" table--> "+companiestobeUpdated);

                            if (Integer.parseInt(companiestobeUpdated) > 0)
                              {
                                sWhere = "wi_name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                                strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml companiestobeUpdated update for "+sTableName+" table: " + strInputXml);
                                try
                                  {
                                    
                                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml companiestobeUpdated update for "+sTableName+" table: "+strOutputXml);
                                  }
                                catch (NGException e)
                                  {
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception companiestobeUpdated update for "+sTableName+" table: " + e.getMessage());
                                    e.printStackTrace();
                                  }
                                catch (Exception ex)
                                  {
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception companiestobeUpdated update for "+sTableName+" table: " + ex.getMessage());
                                    ex.printStackTrace();
                                  }

                                tagNameU = "APUpdate_Output";
                                subTagNameU = "MainCode";
                                subTagNameU_2 = "Output";
                                mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                                //row_updated = getTagValue(strOutputXml,tagNameU,subTagNameU_2);
                                ////iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode for update query for cif"+cifId+"--> "+mainCode);
                                ////iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select rowUpdated for company for update query for cif"+cifId+" --> "+row_updated);
                                stopIndividualToInsert = true;
                              }
                          }

                        if (sQry.equalsIgnoreCase("") || (mainCode.equalsIgnoreCase("0") && selectdata.equalsIgnoreCase("0") && !stopIndividualToInsert))
                          {
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("calling APInsert for cif --> "+cifId);
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("calling APInsert for table --> "+sTableName);
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("calling APInsert for cust_type --> "+cust_type);
                            strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml final insert for "+sTableName+" table:" + strInputXml);
                            try
                              {
                                
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                    
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml final insert for "+sTableName+" table: "+strOutputXml);
                                mainCode = getTagValue(strOutputXml, "APInsert_Output", subTagNameU);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("mainCode"+mainCode);
                                if (!mainCode.equalsIgnoreCase("0"))
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
                            catch (NGException e)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception strInputXml final insert for "+sTableName+" table: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception strInputXml final insert for "+sTableName+" table: " + ex.getMessage());
                                ex.printStackTrace();
                              }
                          }
                        else
                          {
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
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("return for "+sTableName+" table:finalValue: "+retVal);
                return retVal;
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseProduct: " + e.getMessage());
            e.printStackTrace();
            retVal = "false";
          }
        return retVal;
      }


    public static String commonParseProduct_collection(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
      String cabinetName, String subTagName, String prod, String subprod, String cifId, String subtag_single, String cust_type)
      {
        String retVal = "";
        try
          {
            if (!parseXml.contains(tagName))
              {
                return "true";
              }
            else
              {
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside commonParseProduct_collection ");

                String[] valueArr = null;
                String strInputXml = "";
                String strOutputXml = "";
                String columnName = "";
                String columnValues = "";
                String tagNameU = "";
                String subTagNameU = "";
                String subTagNameU_2 = "";
                String mainCode = "";
                String sWhere = "";
                String row_updated = "";
                String sQry = "";
                String selectdata = "";
                String companyUpdateQuery = "";
                String companiestobeUpdated = "";
                boolean stopIndividualToInsert = false;
                cifId = (parseXml.contains("<CustIdValue>")) ? parseXml.substring(parseXml.indexOf("<CustIdValue>") + "</CustIdValue>".length() - 1, parseXml.indexOf("</CustIdValue>")) : "";
                
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Cifid jsp: ReportUrl: "+cifId);
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tagName jsp: commonParse: "+tagName);
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("subTagName jsp: commonParse: "+subTagName);


                Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
                tagValuesMap = getTagDataParent_deep(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                //  String colValue="";
                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values" + entry.getValue());


                    //colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
                    columnName = valueArr[0] + ",CifId,Request_Type,Product_Type,CardType,Wi_Name";
                    columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + prod + "','" + subprod + "','" + wi_name + "'";



                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnName commonParse" + columnName);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnValues commonParse" + columnValues);
                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardDetails"))
                      {

                        columnName = valueArr[0] + ",CifId,Request_Type,wi_name";
                        columnName = columnName.replaceAll("Card_approve_date", "ApplicationCreationDate");
                        columnName = columnName.replaceAll("Outstanding_balance", "OutstandingAmt");
                        columnName = columnName.replaceAll("Credit_limit", "CreditLimit");
                        columnName = columnName.replaceAll("Overdue_amount", "OverdueAmt");
                        columnName = columnName.replaceAll("GeneralStatus", "General_Status");
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";
                        sWhere = "wi_name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "'";
                        sQry =
                          "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Individual_CIF'";
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery = "Select count(*) as selectdata from " + sTableName + " where  CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }

                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_LoanDetails"))
                      {
                        columnName = valueArr[0] + ",CifId,Request_Type,Product_Type,CardType,Wi_Name";
                        columnValues = valueArr[1] + ",'" + cifId + "','" + returnType + "','" + prod + "','" + subprod + "','" + wi_name + "'";
                        columnName = columnName.replaceAll("OutstandingAmt", "TotalOutstandingAmt");
                        columnName = columnName.replaceAll("Loan_close_date", "LoanMaturityDate");
                        columnName = columnName.replaceAll("GeneralStatus", "General_Status");//Deepak code added to save value in General_Status for PCAS-1264 as it was mising in PL & CC And same was there in RLOS
                        sWhere = "wi_name='" + wi_name + "' AND AgreementId = '" + entry.getKey() + "'";
                        sQry = "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And AgreementId = '" + entry.getKey() + "' And Liability_type ='Individual_CIF'";
                        if (cust_type.equalsIgnoreCase("Individual_CIF"))
                          {
                            companyUpdateQuery =
                              "Select count(*) as selectdata from " + sTableName + " where wi_name='" + wi_name + "' And AgreementId = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_AcctDetails"))
                      {
                        String CreditGrade =
                          (parseXml.contains("<CreditGrade>")) ? parseXml.substring(parseXml.indexOf("<CreditGrade>") + "</CreditGrade>".length() - 1, parseXml.indexOf("</CreditGrade>")) : "";
                        //PCASP-2833 
                        //String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>")+"</IsDirect>".length()-1,parseXml.indexOf("</IsDirect>")):"";
                        columnName = valueArr[0] + ",Request_Type,CifId,CreditGrade,wi_name";
                        columnValues = valueArr[1] + ",'" + CreditGrade + "','" + returnType + "','" + cifId + "','" + wi_name + "'";
                        sWhere = "wi_name='" + wi_name + "' AND AcctId = '" + entry.getKey() + "'";
                        sQry = "Select count(*) as selectdata from " + sTableName + " where Wi_Name='" + wi_name + "' And AcctId = '" + entry.getKey() + "' And Account_Type ='Individual_CIF'";
                      }
                    else
                      {
                        sWhere = "Wi_Name='" + wi_name + "' AND Request_Type='" + returnType + "'";
                      }
                    
                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);

                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "collection summary update "+sTableName+" input:  " + strInputXml);
                    try
                      {
                        
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "collection summary update "+sTableName+" output:  " + strOutputXml);
                      }
                    catch (NGException e)
                      {
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception collection summary update "+sTableName+" output: " + e.getMessage());
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception collection summary update "+sTableName+" output: " + ex.getMessage());
                        ex.printStackTrace();
                      }

                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+row_updated);
                    if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
                      {   //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sQry sQry sQry --> "+sQry);
                        if (!sQry.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(sQry, cabinetName, sessionId);
                            try
                              {
                                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml ExecuteQuery_APSelect: "+strOutputXml);
                              }
                            catch (NGException e)
                              {
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                ex.printStackTrace();
                              }
                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
                            selectdata = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select selectdata --> "+selectdata);
                          }

                        if (!companyUpdateQuery.equalsIgnoreCase(""))
                          {
                            strInputXml = ExecuteQuery_APSelect(companyUpdateQuery, cabinetName, sessionId);
                            try
                              {
                                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml ExecuteQuery_APSelect: "+strOutputXml);
                              }
                            catch (NGException e)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseProduct_collection: " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseProduct_collection: " + ex.getMessage());
                                ex.printStackTrace();
                              }



                            mainCode =
                              (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);

                            companiestobeUpdated = (strOutputXml.contains("<selectdata>"))
                              ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>") + "</selectdata>".length() - 1, strOutputXml.indexOf("</selectdata>")) : "";
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select companiestobeUpdated --> "+companiestobeUpdated);

                            if (Integer.parseInt(companiestobeUpdated) > 0)
                              {
                                if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardDetails"))
                                  {
                                    sWhere = "wi_name='" + wi_name + "' AND CardEmbossNum = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                                  }
                                else if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_LoanDetails"))
                                  {
                                    sWhere = "wi_name='" + wi_name + "' AND AgreementId = '" + entry.getKey() + "' And Liability_type ='Corporate_CIF'";
                                  }
                                strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);

                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "companiestobeUpdated collection summary update "+sTableName+" input: " + strInputXml);
                                try
                                  {
                                    
                                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("companiestobeUpdated collection summary update "+sTableName+" output: "+strOutputXml);
                                  }
                                catch (NGException e)
                                  {
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception companiestobeUpdated collection summary "+sTableName+" update: " + e.getMessage());
                                    e.printStackTrace();
                                  }
                                catch (Exception ex)
                                  {
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception companiestobeUpdated collection summary "+sTableName+" update: " + ex.getMessage());
                                    ex.printStackTrace();
                                  }

                                tagNameU = "APUpdate_Output";
                                subTagNameU = "MainCode";
                                subTagNameU_2 = "Output";
                                mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                                //row_updated = getTagValue(strOutputXml,tagNameU,subTagNameU_2);
                                ////iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode for update query for cif"+cifId+"--> "+mainCode);
                                ////iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select rowUpdated for company for update query for cif"+cifId+" --> "+row_updated);
                                stopIndividualToInsert = true;
                              }


                          }

                        if (sQry.equalsIgnoreCase("") || (mainCode.equalsIgnoreCase("0") && selectdata.equalsIgnoreCase("0") && !stopIndividualToInsert))
                          {
                            strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml final collection summary "+sTableName+" update: " + strInputXml);
                            try
                              {
                                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml final collection summary "+sTableName+" update: "+strOutputXml);
                                mainCode = getTagValue(strOutputXml, "APInsert_Output", subTagNameU);
                                if (!mainCode.equalsIgnoreCase("0"))
                                  {
                                    retVal = "false";
                                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: ApINsertfalse for collection summary: "+retVal);
                                  }
                                else
                                  {
                                    retVal = "true";
                                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: ApINserttrue for collection summary: "+retVal);
                                  }
                              }
                            catch (NGException e)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseProduct_collection for "+sTableName+" : " + e.getMessage());
                                e.printStackTrace();
                              }
                            catch (Exception ex)
                              {
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseProduct_collection for "+sTableName+" : " + ex.getMessage());
                                ex.printStackTrace();
                              }
                          }
                        //change by saurabh for company call if its not able to overwrite individual data but call was successful so at frontend it should be successfull. Change on 2nd feb.
                        else
                          {
                            retVal = "true";
                          }
                      }
                    else
                      {
                        retVal = "true";
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: ApUpdatetrue for collection summary: "+retVal);
                      }
                  }

                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("final value for collection summary: "+retVal);
                return retVal;
              }

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseProduct_collection: " + e.getMessage());
            e.printStackTrace();
            retVal = "false";
          }
        return retVal;
      }

    public static String commonParseFinance_CardInstallment(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort,
      String sessionId, String cabinetName, String subTagName, String subtag_single)
      {
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("commonParseFinance jsp: inside: ");
        String retVal = "";
        String[] valueArr = null;
        String strInputXml = "";
        String strOutputXml = "";
        String columnName = "";
        String columnValues = "";
        String tagNameU = "";
        String subTagNameU = "";
        String subTagNameU_2 = "";
        String mainCode = "";
        String sWhere = "";
        String row_updated = "";
        String txnNum = "";
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tagName jsp: commonParseFinance: "+tagName);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("subTagName jsp: commonParseFinance: "+subTagName);
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sTableName jsp: commonParseFinance: "+sTableName);
        try
          {

            if ((returnType.equalsIgnoreCase("CARD_INSTALLMENT_DETAILS") && parseXml.contains("TransactionDetailsRec")))
              {

                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: commonParseFinance: "+returnType);
                Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
                tagValuesMap = getTagDataParent_deep(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                String colValue = "";
                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    for (int i = 0; i < valueArr.length; i++)
                      {
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values:12345 " +valueArr[i]);
                      }
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values: " + entry.getValue());
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Key values: " + entry.getKey());

                    colValue = "'" + valueArr[1].replaceAll("[,]", "','") + "'";


                    //added
                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_InternalExpo_CardInstallmentDetails"))
                      {
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for USR_0_iRBL_InternalExpo_CardInstallmentDetails");
                        String header_info = getTagDataParent_cardInstallment_header(parseXml, "CardInstallmentDetailsResponse",
                          "CIFID,CardCRNNumber,CardSerialNumber,OTBAmount,TotalExposureAmount,TotalRepaymentAmount,InstallmentAccountStatus");

                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside commonParseFinance for USR_0_iRBL_InternalExpo_CardInstallmentDetails header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");

                        columnName = valueArr[0] +","+ header_info_arr[0]+ ",Request_Type,Wi_Name" ;
                        columnValues = valueArr[1] + ",'" + header_info_arr[1] + "','CARD_INSTALLMENT_DETAILS'," + wi_name;
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        columnValues = "";
                        for (int i = 0; i < columnName_arr.length; i++)
                          {
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside Card Installment for loop to remove I:"+columnName_arr[i]);
                            if (columnName_arr[i].equalsIgnoreCase("CardNumber"))
                              {
                                columnValues_arr[i] = columnValues_arr[i].replace("I", "");
                              }
                            if (i == 0)
                              {
                                columnValues = columnValues_arr[i];
                              }
                            else
                              {
                                columnValues = columnValues + "," + columnValues_arr[i];
                              }
                          }

                        txnNum = columnValues_arr[Arrays.asList(columnName_arr).indexOf("TxnSerialNum")];
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside Cardinstallment: columnName after merging:"+columnValues);

                        // sWhere="Wi_Name='"+wi_name+"' AND Request_Type='"+returnType+"' AND TxnSerialNum = '"+entry.getKey()+"' ";
                        sWhere = "Wi_Name='" + wi_name + "' AND Request_Type='" + returnType + "' AND TxnSerialNum = " + txnNum + "";

                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sWhere of cardinstallmentDetails"+sWhere);
                      }
                    //ended

                    else
                      {
                        columnName = valueArr[0] + ",Request_Type,Wi_Name";
                        columnValues = colValue + ",'" + returnType + "','" + wi_name + "'";
                      }


                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnName commonParse123" + columnName);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnValues commonParse456" + columnValues);

                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml update for finance " + strInputXml);
                    try
                      {
                        //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml update:123 "+strOutputXml);
                      }
                    catch (NGException e)
                      {
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        ex.printStackTrace();
                      }

                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode123 --> "+mainCode);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode123 --> "+row_updated);
                    if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
                      {
                        strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml123Installment insert Query:" + strInputXml);
                        try
                          {
                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml:1234 "+strOutputXml);
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml:mainCode value "+mainCode);
                            mainCode = getTagValue(strOutputXml, "APInsert_Output", subTagNameU);
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml:mainCode value1234 "+mainCode);
                            if (!mainCode.equalsIgnoreCase("0"))
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
                        catch (NGException e)
                          {
                            e.printStackTrace();
                          }
                        catch (Exception ex)
                          {
                            ex.printStackTrace();
                          }
                      }
                    else
                      {
                        retVal = "true";
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
                      }
                  }

              }
            else
              {
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: commonParseFinance Empty tag : "+returnType+" Wi_Name: "+wi_name);
              }
            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: final value for financial summary "+retVal);

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseFinance_CardInstallment: " + e.getMessage());
            e.printStackTrace();
            retVal = "";
          }
        return retVal;
      }



    public static String commonParseFinance(String parseXml, String tagName, String wi_name, String returnType, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
      String cabinetName, String subTagName, String subtag_single)
      {
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("commonParseFinance jsp: inside: ");
        String retVal = "";
        String[] valueArr = null;
        String strInputXml = "";
        String strOutputXml = "";
        String columnName = "";
        String columnValues = "";
        String tagNameU = "";
        String subTagNameU = "";
        String subTagNameU_2 = "";
        String mainCode = "";
        String sWhere = "";
        String row_updated = "";
        String id = "";
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tagName jsp: commonParseFinance: " + tagName);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("subTagName jsp: commonParseFinance: " + subTagName);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sTableName jsp: commonParseFinance: " + sTableName);
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("sTableName jsp: commonParseFinance: " + parseXml);
        try
          {
            if ((returnType.equalsIgnoreCase("RETURNDET") && parseXml.contains("ReturnsDtls")) || (returnType.equalsIgnoreCase("AVGBALDET") && parseXml.contains("AcctId"))
              || (returnType.equalsIgnoreCase("LIENDET") && parseXml.contains("LienDetails")) || (returnType.equalsIgnoreCase("SIDET") && parseXml.contains("SIDetails"))
              || (returnType.equalsIgnoreCase("TRANSUM") && parseXml.contains("TxnSummary")) || (returnType.equalsIgnoreCase("SALDET") && parseXml.contains("SalDetails")))
              {

                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: commonParseFinance: "+returnType);
                Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
                tagValuesMap = getTagDataParent_deep(parseXml, tagName, subTagName, subtag_single);

                Map<String, String> map = tagValuesMap;
                String colValue = "";


                for (Map.Entry<String, String> entry : map.entrySet())
                  {
                    valueArr = entry.getValue().split("~");
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values:1234 " +valueArr);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "tag values: " + entry.getValue());

                    colValue = "'" + valueArr[1].replaceAll("[,]", "','") + "'";
                    if (returnType.equalsIgnoreCase("AVGBALDET") && valueArr[0].contains("AcctId"))
                      {
                        String columnName_arr[] = valueArr[0].split(",");
                        String columnValues_arr[] = valueArr[1].split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                      }

                    if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_AvgBalanceDtls"))
                      {
                        columnName = valueArr[0] + ",Wi_Name";
                        columnValues = valueArr[1] + ",'" + wi_name + "'";
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id;
                      }
                    //modified by akshay on 6/2/18  
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_ReturnsDtls"))
                      {
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_ReturnsDtls");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CIFID,AcctId,OperationType");
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_ReturnsDtls header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside Return Details-->columnValues: "+columnValues);
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        if (returnType.equalsIgnoreCase("RETURNDET") && valueArr[0].contains("ReturnNumber"))
                          {
                            id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("ReturnNumber")];
                            sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND ReturnNumber = " + id;
                          }
                        else
                          {
                            id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                            sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id;
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_LienDetails"))
                      {
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_LienDetails");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CIFID,AcctId,OperationType");
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_LienDetails header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];

                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                        String leinId = columnValues_arr[Arrays.asList(columnName_arr).indexOf("LienId")];
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " and LienId = " + leinId;
                        strInputXml = ExecuteQuery_APdelete(sTableName, sWhere, cabinetName, sessionId);
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml delete returndtls " + strInputXml);
                        try
                          {
                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml delete returndtls: "+strOutputXml);
                          }
                        catch (NGException e)
                          {
                            e.printStackTrace();
                          }
                        catch (Exception ex)
                          {
                            ex.printStackTrace();
                          }
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_TxnSummary"))
                      {
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_TxnSummary");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CIFID,AcctId,OperationType");
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_TxnSummary header info: "+ header_info);
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",Wi_Name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];
                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                        String Month = columnValues_arr[Arrays.asList(columnName_arr).indexOf("Month")];
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " and Month = " + Month + "";
                        strInputXml = ExecuteQuery_APdelete(sTableName, sWhere, cabinetName, sessionId);
                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_SalTxnDetails"))
                      {
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "Inside commonParseFinance for ng_rlos_FinancialSummary_SalTxnDetails");
                        String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CifId,AcctId,OperationType");
                        String[] header_info_arr = header_info.split(":");
                        columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                        columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];

                        String columnName_arr[] = columnName.split(",");
                        String columnValues_arr[] = columnValues.split(",");
                        id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                        String SalCreditDate = columnValues_arr[Arrays.asList(columnName_arr).indexOf("SalCreditDate")];
                        sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " and 1=2 and SalCreditDate = " + SalCreditDate + "";

                      }
                    else if (sTableName.equalsIgnoreCase("USR_0_iRBL_FinancialSummary_SiDtls"))
                      {
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside commonParseFinance: ng_rlos_FinancialSummary_SiDtls ");
                        try
                          {
                            String header_info = getTagDataParent_financ_header(parseXml, "FinancialSummaryRes", "CifId,AcctId,OperationType");
                            String[] header_info_arr = header_info.split(":");
                            columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                            columnValues = valueArr[1] + ",'" + wi_name +  "'," + header_info_arr[1];
                            String columnName_arr[] = columnName.split(",");
                            String columnValues_arr[] = columnValues.split(",");
                            id = columnValues_arr[Arrays.asList(columnName_arr).indexOf("AcctId")];
                            String SINumber = columnValues_arr[Arrays.asList(columnName_arr).indexOf("SINumber")];
                            columnName = valueArr[0] + ",wi_name," + header_info_arr[0];
                            columnValues = valueArr[1] + ",'" + wi_name + "'," + header_info_arr[1];
                            String sWhere_delete = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id;
                            sWhere = "wi_name='" + wi_name + "' AND OperationType='" + returnType + "' AND AcctId = " + id + " And SINumber=" + SINumber;
                            //strInputXml = ExecuteQuery_APdelete(sTableName,sWhere_delete,cabinetName,sessionId);
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "strInputXml delete ng_rlos_FinancialSummary_SiDtls " + strInputXml);
                            /*
                             * try { strOutputXml =
                             * NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort,
                             * appServerType, strInputXml);
                             * 
                             * System.out.
                             * println("CustExpose_Output jsp: strOutputXml delete returndtls: "
                             * +strOutputXml); } catch (NGException e) { e.printStackTrace();
                             */
                          }
                        catch (Exception ex)
                          {
                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in ng_rlos_FinancialSummary_SiDtls: " + ex.getMessage());
                          }
                      }
                    else
                      {
                        columnName = valueArr[0] + ",Request_Type,Wi_Name";
                        columnValues = colValue + ",'" + returnType +  "','" +  wi_name + "'";
                      }


                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnName commonParse" + columnName);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "columnValues commonParse" + columnValues);

                    strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, columnValues, sWhere, cabinetName, sessionId);

                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml update " + strInputXml);
                    try
                      {
                        //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                        strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml update: "+strOutputXml);
                      }
                    catch (NGException e)
                      {
                        e.printStackTrace();
                      }
                    catch (Exception ex)
                      {
                        ex.printStackTrace();
                      }
                    //changed by akshay on 2/5/18 for proc 8964
                    tagNameU = "APUpdate_Output";
                    subTagNameU = "MainCode";
                    subTagNameU_2 = "Output";
                    mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                    row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+mainCode);
                    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("getTagValue select mainCode --> "+row_updated);
                    if (!(mainCode.equalsIgnoreCase("0")) || row_updated.equalsIgnoreCase("0"))
                      {
                        strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml" + strInputXml);
                        try
                          {
                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                            tagNameU = "APInsert_Output";
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: strOutputXml: "+strOutputXml);
                            mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
                            //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug( "mainCode value is: " +mainCode );
                            if (!mainCode.equalsIgnoreCase("0"))
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
                        catch (NGException e)
                          {
                            e.printStackTrace();
                          }
                        catch (Exception ex)
                          {
                            ex.printStackTrace();
                          }
                      }
                    else
                      {
                        retVal = "true";
                        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: commonparseproductapupdate:true "+retVal);
                      }
                  }
              }
            else
              {
                retVal = "true";
                //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("returnType jsp: commonParseFinance Empty tag : "+returnType+" Wi_Name: "+wi_name);
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseFinance: " + e.getMessage());
            e.printStackTrace();
            retVal = "false";
          }
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("CustExpose_Output jsp: final value for financial summary "+retVal);
        return retVal;
      }


    public static String ExecuteQuery_APInsert(String tableName, String columnName, String strValues, String cabinetName, String sessionId)
      {
        StringBuffer ipXMLBuffer = new StringBuffer();

        ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
        ipXMLBuffer.append("<APInsertExtd_Input>\n");
        ipXMLBuffer.append("<Option>APInsert</Option>");
        ipXMLBuffer.append("<TableName>");
        ipXMLBuffer.append(tableName);
        ipXMLBuffer.append("</TableName>");
        ipXMLBuffer.append("<ColName>");
        ipXMLBuffer.append(columnName);
        ipXMLBuffer.append("</ColName>\n");
        ipXMLBuffer.append("<Values>");
        ipXMLBuffer.append(strValues);
        ipXMLBuffer.append("</Values>\n");
        ipXMLBuffer.append("<EngineName>");
        ipXMLBuffer.append(cabinetName);
        ipXMLBuffer.append("</EngineName>\n");
        ipXMLBuffer.append("<SessionId>");
        ipXMLBuffer.append(sessionId);
        ipXMLBuffer.append("</SessionId>\n");
        ipXMLBuffer.append("</APInsertExtd_Input>");

        return ipXMLBuffer.toString();
      }

    public static String ExecuteQuery_APdelete(String tableName, String sWhere, String cabinetName, String sessionId)
      {
        String sInputXML = "<?xml version=\"1.0\"?>" + "<APDelete_Input><Option>APDelete</Option>" + "<TableName>" + tableName + "</TableName>" + "<WhereClause>" + sWhere + "</WhereClause>"
          + "<EngineName>" + cabinetName + "</EngineName>" + "<SessionId>" + sessionId + "</SessionId>" + "</APDelete_Input>";
        return sInputXML;
      }

    public static String ExecuteQuery_APUpdate(String tableName, String columnName, String strValues, String sWhere, String cabinetName, String sessionId)
      {
        String sInputXML = "<?xml version=\"1.0\"?>" + "<APUpdate_Input><Option>APUpdate</Option>" + "<TableName>" + tableName + "</TableName>" + "<ColName>" + columnName + "</ColName>" + "<Values>"
          + strValues + "</Values>" + "<WhereClause>" + sWhere + "</WhereClause>" + "<EngineName>" + cabinetName + "</EngineName>" + "<SessionId>" + sessionId + "</SessionId>" + "</APUpdate_Input>";
        return sInputXML;
      }

    public static String ExecuteQuery_APSelectwithparam(String sQry, String params, String cabinetName, String sessionId)
      {
        String sInputXML = "<?xml version='1.0'?><APSelectWithNamedParam_Input>" + "<option>APSelectWithNamedParam</option>" + "<Query>" + sQry + "</Query>" + "<Params>" + params + "</Params>"
          + "<EngineName>" + cabinetName + "</EngineName>" + "<SessionID>" + sessionId + "</SessionID>" + "</APSelectWithNamedParam_Input>";
        return sInputXML;
      }

    public static String ExecuteQuery_APSelect(String sQry, String cabinetName, String sessionId)
      {

        StringBuffer ipXMLBuffer = new StringBuffer();

        ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
        ipXMLBuffer.append("<APSelect_Input>\n");
        ipXMLBuffer.append("<Option>APSelectWithColumnNames</Option>\n");
        ipXMLBuffer.append("<Query>");
        ipXMLBuffer.append(sQry);
        ipXMLBuffer.append("</Query>\n");
        ipXMLBuffer.append("<EngineName>");
        ipXMLBuffer.append(cabinetName);
        ipXMLBuffer.append("</EngineName>\n");
        ipXMLBuffer.append("<SessionId>");
        ipXMLBuffer.append(sessionId);
        ipXMLBuffer.append("</SessionId>\n");
        ipXMLBuffer.append("</APSelect_Input>");

        return ipXMLBuffer.toString();

      }

    public static String getTagValue(String parseXml, String tagName, String subTagName)
      {
        //WriteLog("getTagValue jsp: inside: ");
        String[] valueArr = null;
        String mainCodeValue = "";

        //WriteLog("tagName jsp: getTagValue: "+tagName);
        //WriteLog("subTagName jsp: getTagValue: "+subTagName);

        try
          {
            Map<Integer, String> tagValuesMap = new LinkedHashMap<Integer, String>();
            tagValuesMap = getTagDataParent(parseXml, tagName, subTagName);

            Map<Integer, String> map = tagValuesMap;
            for (Map.Entry<Integer, String> entry : map.entrySet())
              {
                valueArr = entry.getValue().split("~");
                //WriteLog( "tag values" + entry.getValue());
                mainCodeValue = valueArr[1];
                //WriteLog( "mainCodeValue" + mainCodeValue);
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured getTagValue: " + e.getMessage());
            e.printStackTrace();
          }
        return mainCodeValue;
      }

    public static void updateQuery(String sTableName, String columnName, String colValue, String sWhere, String cabinetName, String sessionId, String returnType, String wrapperIP, String wrapperPort,
      String appServerType, String cifId, String wi_name)
      {
        String strInputXml = "";
        String strOutputXml = "";
        String mainCode = "";
        String tagNameU = "";
        String subTagNameU = "";
        String subTagNameU_2 = "";
        String columnValues = "";
        String row_updated = "";
        try
          {
            strInputXml = ExecuteQuery_APUpdate(sTableName, columnName, colValue, sWhere, cabinetName, sessionId);
            //WriteLog( "strInputXml update " + strInputXml);
            try
              {
                //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                //WriteLog("CustExpose_Output jsp: strOutputXml update: "+strOutputXml);
              }
            catch (NGException e)
              {
                e.printStackTrace();
              }
            catch (Exception ex)
              {
                ex.printStackTrace();
              }

            tagNameU = "APUpdate_Output";
            subTagNameU = "MainCode";
            subTagNameU_2 = "Output";
            mainCode = getTagValue(strOutputXml, tagNameU, subTagNameU);
            row_updated = getTagValue(strOutputXml, tagNameU, subTagNameU_2);
            //WriteLog("getTagValue select mainCode --> "+mainCode);
            if (!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
              {
                //colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";

                columnName = columnName + ",Request_Type,CifId,Wi_Name";
                columnValues = colValue + ",'" + cifId + "','" + returnType + "','" + wi_name + "'";

                strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                //WriteLog( "strInputXml insert " + strInputXml);
                try
                  {
                    //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);

                    //WriteLog("CustExpose_Output jsp: strOutputXml insert: "+strOutputXml);
                  }
                catch (NGException e)
                  {
                    e.printStackTrace();
                  }
                catch (Exception ex)
                  {
                    ex.printStackTrace();
                  }
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured updateQuery: " + e.getMessage());
            e.printStackTrace();
          }
      }

    public static Map<String, String> getTagDataParent_deep(String parseXml, String tagName, String sub_tag, String subtag_single)
      {

        Map<String, String> tagValuesMap = new LinkedHashMap<String, String>();
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        try
          {
            //WriteLog("getTagDataParent_deep jsp: parseXml: "+parseXml);
            //WriteLog("getTagDataParent_deep jsp: tagName: "+tagName);
            //WriteLog("getTagDataParent_deep jsp: subTagName: "+sub_tag);
            String tag_notused = "BankId,OperationDesc,TxnSummary,#text";


            //WriteLog("getTagDataParent_deep jsp: strOutputXml: "+is);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList_loan = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nList_loan.getLength(); i++)
              {
                String col_name = "";
                String col_val = "";
                NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
                String id = "";
                if ("ReturnsDtls".equalsIgnoreCase(tagName))
                  {
                    id = ch_nodeList.item(1).getTextContent();
                  }
                else if ("SalDetails".equalsIgnoreCase(tagName))
                  {
                    id = ch_nodeList.item(0).getTextContent() + i;
                  }
                else if ("ServicesDetails".equalsIgnoreCase(tagName))
                  {
                    id = ch_nodeList.item(1).getTextContent();
                  }
                else if ("InvestmentDetails".equalsIgnoreCase(tagName))
                  {
                    id = ch_nodeList.item(1).getTextContent();
                  }
                else
                  {
                    id = ch_nodeList.item(0).getTextContent();
                  }
                //String id = ch_nodeList.item(0).getTextContent();
                for (int ch_len = 0; ch_len < ch_nodeList.getLength(); ch_len++)
                  {
                    if (sub_tag.contains(ch_nodeList.item(ch_len).getNodeName()))
                      {
                        NodeList sub_ch_nodeList = ch_nodeList.item(ch_len).getChildNodes();
                        if (!sub_ch_nodeList.item(0).getTextContent().equalsIgnoreCase("#text"))
                          {
                            if (col_name.equalsIgnoreCase(""))
                              {
                                col_name = sub_ch_nodeList.item(0).getTextContent();
                                col_val = "'" + sub_ch_nodeList.item(1).getTextContent() + "'";
                              }
                            else if (!col_name.contains(sub_ch_nodeList.item(0).getTextContent()))
                              {
                                col_name = col_name + "," + sub_ch_nodeList.item(0).getTextContent();
                                col_val = col_val + ",'" + sub_ch_nodeList.item(1).getTextContent() + "'";
                              }
                          }

                      }
                    else if (tag_notused.contains(ch_nodeList.item(ch_len).getNodeName()))
                      {
                        //WriteLog("this tag not to be passed: "+ch_nodeList.item(ch_len).getNodeName());
                      }
                    else if (subtag_single.contains(ch_nodeList.item(ch_len).getNodeName()))
                      {
                        NodeList sub_ch_nodeList = ch_nodeList.item(ch_len).getChildNodes();
                        if (!sub_ch_nodeList.item(0).getTextContent().equalsIgnoreCase("#text"))
                          {
                            for (int sub_chd_len = 0; sub_chd_len < sub_ch_nodeList.getLength(); sub_chd_len++)
                              {
                                if (col_name.equalsIgnoreCase(""))
                                  {
                                    col_name = sub_ch_nodeList.item(sub_chd_len).getNodeName();
                                    col_val = "'" + sub_ch_nodeList.item(sub_chd_len).getTextContent() + "'";
                                  }
                                else if (!col_name.contains(sub_ch_nodeList.item(0).getTextContent()))
                                  {
                                    col_name = col_name + "," + sub_ch_nodeList.item(sub_chd_len).getNodeName();
                                    col_val = col_val + ",'" + sub_ch_nodeList.item(sub_chd_len).getTextContent() + "'";
                                  }
                              }
                          }
                      }
                    else
                      {
                        if (col_name.equalsIgnoreCase(""))
                          {
                            col_name = ch_nodeList.item(ch_len).getNodeName();
                            col_val = "'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                        else if (!col_name.contains(ch_nodeList.item(ch_len).getNodeName()))
                          {
                            col_name = col_name + "," + ch_nodeList.item(ch_len).getNodeName();
                            col_val = col_val + ",'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }

                      }

                  }
                //WriteLog("insert/update for id: "+id);
                //WriteLog("insert/update cal_name: "+col_name);
                //WriteLog("insert/update col_val: "+col_val);
                if (!col_name.equalsIgnoreCase(""))
                  tagValuesMap.put(id, col_name + "~" + col_val);
              }

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in getTagDataParent_deep: " + e.getMessage());
            e.printStackTrace();
            //WriteLog("Exception occured in getTagDataParent method:  "+e.getMessage());
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return tagValuesMap;
      }

    public static void parseLinkedCif(String Xml, String TableName, String Main_CIF,String Wi_name, String Agreement_id, String Cust_Type, String Liability_type, String cabinetName,
      String sessionId, String wrapperIP, String wrapperPort)
      {
        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF");
        //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Input_XMl" + Xml);

        try
          {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(Xml)));
            doc.getDocumentElement().normalize();
            String Liabilityid = "";
            //String ParentTag= doc.getDocumentElement().getNodeName();
            NodeList nList;
            if ("Account".equalsIgnoreCase(Liability_type))
              {
                nList = doc.getElementsByTagName("AcctDetails");
                Liabilityid = "AcctId";
              }
            else if ("Loan".equalsIgnoreCase(Liability_type))
              {
                nList = doc.getElementsByTagName("LoanDetails");
                Liabilityid = "AgreementId";
              }
            else
              {
                nList = doc.getElementsByTagName("CardDetails");
                Liabilityid = "CardEmbossNum";
              }

            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: nList.getLength()" + nList.getLength());
            for (int temp = 0; temp < nList.getLength(); temp++)
              {
                Node nNode = nList.item(temp);
                //  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                  {

                    Element eElement = (Element) nNode;
                    String Liability_ID = eElement.getElementsByTagName(Liabilityid).item(0).getTextContent();
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: AcctId" + Liability_ID);
                    if (Liability_ID.equalsIgnoreCase(Agreement_id))
                      {

                        NodeList Linked_CIF = eElement.getElementsByTagName("LinkedCIFs");
                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Linked_CIF.getLength()" + Linked_CIF.getLength());
                        for (int temp1 = 0; temp1 < Linked_CIF.getLength(); temp1++)
                          {
                            Node node1 = Linked_CIF.item(temp1);
                            if (node1.getNodeType() == Node.ELEMENT_NODE)
                              {
                                Element eElement1 = (Element) node1;
                                String Linked_CIF1 = eElement1.getElementsByTagName("CIFId").item(0).getTextContent();
                                String Relation1 = eElement1.getElementsByTagName("RelationType").item(0).getTextContent();
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Linked_CIF" + Linked_CIF1);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: Relation" + Relation1);

                                /*
                                 * String Linked_CIF=
                                 * eElement.getElementsByTagName("CIFId").item(0).getTextContent();
                                 * String Relation =
                                 * eElement.getElementsByTagName("RelationType").item(0).
                                 * getTextContent();
                                 */
                                String SQuery = "select count(wi_name) as Select_Count from USR_0_iRBL_InternalExpo_LinkedICF where Linked_CIFs='" + Linked_CIF1 + "' and Relation='" + Relation1
                                  + "' and wi_name='" + Wi_name + "' and Main_Cif='" + Main_CIF + "' and AgreementId='" + Agreement_id + "'";
                                String strInputXml = ExecuteQuery_APSelect(SQuery, cabinetName, sessionId);
                                String strOutputXml = "";
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: ExecuteQuery_APSelect" + strInputXml);
                                try
                                  {
                                    //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                    strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                    //WriteLog("CustExpose_Output jsp: strOutputXml ExecuteQuery_APSelect: "+strOutputXml);
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: ExecuteQuery_APSelect output" + strOutputXml);
                                  }
                                catch (Exception ex)
                                  {
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in commonParseProduct: " + ex.getMessage());
                                    ex.printStackTrace();
                                  }
                                String mainCode = (strOutputXml.contains("<MainCode>"))
                                  ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>")) : "";
                                //WriteLog("getTagValue select mainCode --> "+mainCode);
                                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF select mainCode --> " + mainCode);
                                if ("0".equalsIgnoreCase(mainCode))
                                  {
                                    String selectdata = (strOutputXml.contains("<Select_Count>"))
                                      ? strOutputXml.substring(strOutputXml.indexOf("<Select_Count>") + "</Select_Count>".length() - 1, strOutputXml.indexOf("</Select_Count>")) : "";
                                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF select selectdata --> " + selectdata);
                                    int totalretrieved = Integer.parseInt(selectdata);
                                    if (totalretrieved == 0)
                                      {
                                        //need to change by prabhakar
                                        String sTableName = "USR_0_iRBL_InternalExpo_LinkedICF";
                                        String columnName = "Wi_name,Linked_CIFs,Relation,AgreementId,Main_Cif,Liability_Type,Cust_Type";
                                        String columnValues =
                                          "'" + Wi_name + "','" + Linked_CIF1 + "','" + Relation1 + "','" + Agreement_id + "','" + Main_CIF + "','" + Liability_type + "','" + Cust_Type + "'";
                                        strInputXml = ExecuteQuery_APInsert(sTableName, columnName, columnValues, cabinetName, sessionId);
                                        //WriteLog( "strInputXml" + strInputXml);
                                        iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" Parse linked cif  strInputXml" + strInputXml);
                                        try
                                          {
                                            //strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
                                            strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
                                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" Parse linked cif  strOutputXml" + strOutputXml);
                                            //WriteLog("CustExpose_Output jsp: strOutputXml: "+strOutputXml);
                                            mainCode = getTagValue(strOutputXml, "APInsert_Output", "MainCode");


                                          }
                                        catch (Exception ex)
                                          {
                                            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parseCIF: " + ex.getMessage());
                                            ex.printStackTrace();
                                          }
                                      }
                                  }
                                //WriteLog("getTagValue select selectdata --> "+selectdata);
                              }
                          }
                      }
                  }
              }
          }
        catch (Exception ex)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parse linked cif : " + ex.getMessage());
            ex.printStackTrace();
          }
      }

    public static String parseHistoryUtilization(String Xml, String Agreement_id, String Liability_type, String StartType, String EndType)
      {
        //WriteLog("Inside parse CIF");
        //WriteLog("Inside parse CIF:: Input_XMl" + Xml);
        String Output_desired = "";

        try
          {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(Xml)));
            doc.getDocumentElement().normalize();
            String Liabilityid = "";
            //String ParentTag= doc.getDocumentElement().getNodeName();
            NodeList nList;
            if ("LoanDetails".equalsIgnoreCase(Liability_type))
              {
                nList = doc.getElementsByTagName("LoanDetails");
                Liabilityid = "AgreementId";
              }
            else
              {
                nList = doc.getElementsByTagName("CardDetails");
                Liabilityid = "CardEmbossNum";
              }

            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: nList.getLength()" + nList.getLength());

            for (int temp = 0; temp < nList.getLength(); temp++)
              {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                  {

                    Element eElement = (Element) nNode;

                    String Liability_ID = eElement.getElementsByTagName(Liabilityid).item(0).getTextContent();
                    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Inside parse CIF:: AcctId" + Liability_ID);
                    if (Liability_ID.equalsIgnoreCase(Agreement_id))
                      {
                        //  iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("\nCurrent Element :" + nNode.getNodeName());
                        //  WriteLog("Inside parse CIF:: ExecuteQuery_APSelect" + nodeToString(nNode));
                        String Liability_aggregate = nodeToString(nNode);
                        Output_desired = Liability_aggregate.substring(Liability_aggregate.indexOf(StartType), Liability_aggregate.lastIndexOf(EndType) + EndType.length());

                      }
                  }
              }

          }
        catch (Exception ex)
          {
            
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in parse history Utilitixation cif : " + ex.getMessage());
            //ex.printStackTrace();
          }
        return Output_desired;
      }

    public static String get_loanDesc(String loan_code, String cabinetName, String sessionId, String wrapperIP, String wrapperPort)
      {
        String loan_desc = "";
        try
          {
            String str_Loandesc = "select Description from NG_MASTER_contract_type with(nolock) where code = '"+loan_code.replace("'", "")+"'";
            String params = "code==" + loan_code.replace("'", "");
            String strInputXml = ExecuteQuery_APSelect(str_Loandesc, cabinetName, sessionId);//(str_Loandesc, params, cabinetName, sessionId);
            //String strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
            String strOutputXml = WFNGExecute(strInputXml, wrapperIP, wrapperPort, 0);
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside get_loanDesc strOutputXml:  " + strOutputXml);
            String Maincode = strOutputXml.substring(strOutputXml.indexOf("<MainCode>") + "</MainCode>".length() - 1, strOutputXml.indexOf("</MainCode>"));
            if ("0".equalsIgnoreCase(Maincode))
              {
                loan_desc = strOutputXml.substring(strOutputXml.indexOf("<Description>") + "</Description>".length() - 1, strOutputXml.indexOf("</Description>"));
              }
            else
              {
                loan_desc = loan_code;
              }
          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in get_loanDesc:  " + e.getMessage());
            loan_desc = loan_code;
          }
        return "'" + loan_desc + "'";
      }

    public static String get_Mob_forOD(String LimitSactionDate)
      {
        try
          {
            LimitSactionDate = LimitSactionDate.replaceAll("'", "");
            Date Current_date = new Date();
            Date Old_Date = new SimpleDateFormat("yyyy-MM-dd").parse(LimitSactionDate);
            int yy = Current_date.getYear() - Old_Date.getYear();
            int mm = Current_date.getMonth() - Old_Date.getMonth();
            if (mm < 0)
              {
                yy--;
                mm = 12 - Old_Date.getMonth() + Current_date.getMonth();
                if (Current_date.getDate() < Old_Date.getDate())
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
            else if (Current_date.getDate() - Old_Date.getDate() != 0)
              {
                if (mm == 12)
                  {
                    yy++;
                    mm = 0;
                  }
              }

            return String.valueOf((yy * 12) + mm);
          }
        catch (Exception ex)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in get_Mob_forOD: " + ex.getMessage());
            ex.printStackTrace();
            return "Invalid";
          }

      }

    public static String getTagDataParent_cardInstallment_header(String parseXml, String tagName, String sub_tag)
      {
        String col_name = "";
        String col_val = "";
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        try
          {
            //WriteLog("getTagDataParent_cardInstallment_header jsp: parseXml: "+parseXml);
            //WriteLog("getTagDataParent_cardInstallment_header jsp: tagName: "+tagName);
            //WriteLog("getTagDataParent_cardInstallment_header jsp: subTagName: "+sub_tag);

            //InputStream is = new FileInputStream(parseXml);

            //WriteLog("getTagDataParent_cardInstallment_header jsp: strOutputXml: "+is);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList_loan = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nList_loan.getLength(); i++)
              {

                NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
                String id = ch_nodeList.item(0).getTextContent();
                for (int ch_len = 0; ch_len < ch_nodeList.getLength(); ch_len++)
                  {
                    if (sub_tag.toUpperCase().contains(ch_nodeList.item(ch_len).getNodeName().toUpperCase()))
                      {
                        if (col_name.equalsIgnoreCase(""))
                          {
                            col_name = ch_nodeList.item(ch_len).getNodeName();
                            col_val = "'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                        else
                          {
                            col_name = col_name + "," + ch_nodeList.item(ch_len).getNodeName();
                            col_val = col_val + ",'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                      }
                  }
                //WriteLog("insert/update getTagDataParent_cardInstallment_header for id: "+id);
                //WriteLog("insert/update getTagDataParent_cardInstallment_header cal_name: "+col_name);
                //WriteLog("insert/update getTagDataParent_cardInstallment_header col_val: "+col_val);

              }

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in getTagDataParent_cardInstallment_header: " + e.getMessage());
            e.printStackTrace();
            //WriteLog("Exception occured in getTagDataParent_cardInstallment_header method:  "+e.getMessage());
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return col_name + ":" + col_val;
      }

    public static String getTagDataParent_financ_header(String parseXml, String tagName, String sub_tag)
      {
        String col_name = "";
        String col_val = "";
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        try
          {
            //WriteLog("getTagDataParent_financ_header jsp: parseXml: "+parseXml);
            //WriteLog("getTagDataParent_financ_header jsp: tagName: "+tagName);
            //WriteLog("getTagDataParent_financ_header jsp: subTagName: "+sub_tag);

            //InputStream is = new FileInputStream(parseXml);

            //WriteLog("getTagDataParent_financ_header jsp: strOutputXml: "+is);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList_loan = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nList_loan.getLength(); i++)
              {

                NodeList ch_nodeList = nList_loan.item(i).getChildNodes();
                String id = ch_nodeList.item(0).getTextContent();
                for (int ch_len = 0; ch_len < ch_nodeList.getLength(); ch_len++)
                  {
                    if (sub_tag.toUpperCase().contains(ch_nodeList.item(ch_len).getNodeName().toUpperCase()))
                      {
                        if (col_name.equalsIgnoreCase(""))
                          {
                            col_name = ch_nodeList.item(ch_len).getNodeName();
                            col_val = "'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                        else
                          {
                            col_name = col_name + "," + ch_nodeList.item(ch_len).getNodeName();
                            col_val = col_val + ",'" + ch_nodeList.item(ch_len).getTextContent() + "'";
                          }
                      }
                  }
                //WriteLog("insert/update getTagDataParent_financ_header for id: "+id);
                //WriteLog("insert/update getTagDataParent_financ_header cal_name: "+col_name);
                //WriteLog("insert/update getTagDataParent_financ_header col_val: "+col_val);

              }

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in getTagDataParent_financ_header: " + e.getMessage());
            e.printStackTrace();
            //WriteLog("Exception occured in getTagDataParent_financ_header method:  "+e.getMessage());
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return col_name + ":" + col_val;
      }

    public static Map<Integer, String> getTagDataParent(String parseXml, String tagName, String subTagName)
      {
        Map<Integer, String> tagValuesMap = new LinkedHashMap<Integer, String>();
        InputStream is = new ByteArrayInputStream(parseXml.getBytes());
        try
          {
            //WriteLog("getTagDataParent jsp: parseXml: "+parseXml);
            //WriteLog("getTagDataParent jsp: tagName: "+tagName);
            //WriteLog("getTagDataParent jsp: subTagName: "+subTagName);
            //InputStream is = new FileInputStream(parseXml);

            //WriteLog("getTagDataParent jsp: strOutputXml: "+is);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(tagName);

            String[] values = subTagName.split(",");
            String value = "";
            String subTagDerivedvalue = "";
            for (int temp = 0; temp < nList.getLength(); temp++)
              {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                  {
                    Element eElement = (Element) nNode;
                    Node uNode = eElement.getParentNode();

                    for (int j = 0; j < values.length; j++)
                      {
                        if (eElement.getElementsByTagName(values[j]).item(0) != null)
                          {
                            value = value + "," + eElement.getElementsByTagName(values[j]).item(0).getTextContent();
                            subTagDerivedvalue = subTagDerivedvalue + "," + values[j];
                          }

                      }
                    value = value.substring(1, value.length());
                    subTagDerivedvalue = subTagDerivedvalue.substring(1, subTagDerivedvalue.length());

                    Node nNode_c = doc.getElementsByTagName(uNode.getNodeName()).item(temp);
                    Element eElement_agg = (Element) nNode_c;
                    String id_val = "";
                    if (uNode.getNodeName().equalsIgnoreCase("LoanDetails"))
                      {
                        id_val = eElement_agg.getElementsByTagName("AgreementId").item(0).getTextContent();
                      }
                    else if (uNode.getNodeName().equalsIgnoreCase("CardDetails"))
                      {
                        id_val = eElement_agg.getElementsByTagName("CardEmbossNum").item(0).getTextContent();
                      }
                    else if (uNode.getNodeName().equalsIgnoreCase("AcctDetails"))
                      {
                        id_val = eElement_agg.getElementsByTagName("AcctId").item(0).getTextContent();
                      }
                    else
                      {
                        id_val = "";
                      }

                    tagValuesMap.put(temp + 1, subTagDerivedvalue + "~" + value + "~" + uNode.getNodeName() + "~" + id_val);
                    value = "";
                    subTagDerivedvalue = "";
                  }
              }

          }
        catch (Exception e)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in getTagDataParent" + e.getMessage());
            e.printStackTrace();
            //WriteLog("Exception occured in getTagDataParent method:  "+e.getMessage());
          }
        finally
          {
            try
              {
                if (is != null)
                  {
                    is.close();
                    is = null;
                  }
              }
            catch (Exception e)
              {
                iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occured in is close:  " + e.getMessage());
              }
          }
        return tagValuesMap;
      }

    public static String nodeToString(Node node)
      {
        StringWriter sw = new StringWriter();
        try
          {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
          }
        catch (TransformerException te)
          {
            iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("nodeToString Transformer Exception");
          }
        return sw.toString();
      }

    protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag) throws IOException, Exception
      {
        //ConnectionLogger.debug("In WF NG Execute : " + serverPort);
        try
          {
            if (serverPort.startsWith("33"))
              return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
            else
              return ngEjbClientConnection.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
          }
        catch (Exception e)
          {
            //ConnectionLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
            e.printStackTrace();
            return "Error";
          }
      }
  }
