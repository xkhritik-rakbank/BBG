package com.newgen.iRBL.SysCheckIntegration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.print.PrintException;
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

public class iRBLIntegration {

	private static String rowVal;
	private static String DedupeTable="USR_0_iRBL_DEDUPE_GRID_DTLS";
	private static String BlacklistTable="USR_0_IRBL_BLACKLIST_GRID_DTLS";
	private static String FircosoftTable="USR_0_IRBL_FIRCO_GRID_DTLS";

	private static String IRBL_PRODUCT_TYPE="USR_0_IRBL_PRODUCT_TYPE";
	private static String IRBL_COUNTRY_MASTER="USR_0_BPM_COUNTRY_MASTER";
	private static String IRBL_INDUSTRY_SUBSEGMENT="USR_0_BPM_INDUSTRY_SUBSEGMENT";
	
	private static String DocName = "";
	private static String returnValue = "";
	
	public static ArrayList<String> DedupeGridCIFID = new ArrayList<String>();	
	public static ArrayList<String> DedupeGridFullName = new ArrayList<String>();	
	public static ArrayList<String> DedupeGridDOB = new ArrayList<String>();
	public static ArrayList<String> DedupeGridGender = new ArrayList<String>();
	public static ArrayList<String> DedupeGridEmiratesID = new ArrayList<String>();
	public static ArrayList<String> DedupeGridPassportNo = new ArrayList<String>();
	public static ArrayList<String> DedupeGridNationality = new ArrayList<String>();
	public static ArrayList<String> DedupeGridResAddress = new ArrayList<String>();
	public static ArrayList<String> DedupeGridMobNo = new ArrayList<String>();
	public static ArrayList<String> DedupeGridBlacklistedFlag = new ArrayList<String>();
	public static ArrayList<String> DedupeGridNegativelistedFlag = new ArrayList<String>();	

	public static ArrayList<String> BlacklistGridCIFID = new ArrayList<String>();
	public static ArrayList<String> BlacklistGridCifStatus = new ArrayList<String>();
	public static ArrayList<String> BlacklistGridFullName = new ArrayList<String>();
	public static ArrayList<String> BlacklistGridEmiratesID = new ArrayList<String>();
	public static ArrayList<String> BlacklistGridPassportNo = new ArrayList<String>();
	public static ArrayList<String> BlacklistGridResAddress = new ArrayList<String>();	
	public static ArrayList<String> BlacklistGridMobNo = new ArrayList<String>();
	public static ArrayList<String> BlacklistGridBlacklistedFlag = new ArrayList<String>();
	public static ArrayList<String> BlacklistGridNegatedFlag = new ArrayList<String>();
	
	public static ArrayList<String> FircoGridSRNo = new ArrayList<String>();
	public static ArrayList<String> FircoGridOFACID = new ArrayList<String>();
	public static ArrayList<String> FircoGridName = new ArrayList<String>();
	public static ArrayList<String> FircoGridMatchingText = new ArrayList<String>();
	public static ArrayList<String> FircoGridOrigin = new ArrayList<String>();
	public static ArrayList<String> FircoGridDestination = new ArrayList<String>();
	public static ArrayList<String> FircoGridDOB = new ArrayList<String>();
	public static ArrayList<String> FircoGridUserData1 = new ArrayList<String>();
	public static ArrayList<String> FircoGridNationality = new ArrayList<String>();
	public static ArrayList<String> FircoGridPassport = new ArrayList<String>();
	public static ArrayList<String> FircoGridAdditionalInfo = new ArrayList<String>();
	public static ArrayList<String> FircoGridREFERENCENO = new ArrayList<String>();
	
	
	static ResponseBean objRespBean=new ResponseBean();
		
	public static	String DedupeCall( String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> RelPartyGridDataMap, boolean MainCIF_Flag)
	{
		String CustDormancy = "";
		try
		{
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside DedupeCall Fn");

			String DocDetXml="";
			String NATIONALITY = "";			
			String DOB = "";
			String MidName = "";
			String FullName = "";
			String MobileNumberDetails = "";
			String MobileNumber = "";
			String CIF_ID="";
			String First_Name="";
			String Last_Name="";
			String Maritalstatus="";
			String OrganizationDetails = "";
			String PersonalDetails = "";
			String CustomerType = "";
			String RetailCorpFlag = "";
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainCIF_Flag :"+MainCIF_Flag);
			if(MainCIF_Flag)
			{
				CIF_ID = ExtTabDataMap.get("CIF_NUMBER");
				CustomerType = 	"<CustomerType>C</CustomerType>";
				RetailCorpFlag = "<RetailCorpFlag>C</RetailCorpFlag>";
				OrganizationDetails = "<OrganizationDetails>"+
						"<CorporateName>"+ExtTabDataMap.get("COMPANY_NAME")+"</CorporateName>"+
						//"<RepresentativeLastName>"+ExtTabDataMap.get("APPLICANT_FULL_NAME")+"</RepresentativeLastName>"+
						"<CountryOfIncorporation>"+ExtTabDataMap.get("COUNTRYOFINCORPORATION")+"</CountryOfIncorporation>"+
						"<DateOfIncorporation>"+ExtTabDataMap.get("DATEOFINCORPORATION")+"</DateOfIncorporation></OrganizationDetails>";
				
				if (!"".equalsIgnoreCase(ExtTabDataMap.get("MOBILENUMBERCOUNTRYCODE")) && !"".equalsIgnoreCase(ExtTabDataMap.get("MOBILENUMBER")))
				{
					MobileNumber = "+"+ExtTabDataMap.get("MOBILENUMBERCOUNTRYCODE")+"()"+ExtTabDataMap.get("MOBILENUMBER");
					MobileNumberDetails = "<ContactDetails>\n"+
							"<PhoneFax>\n"+
								"<PhoneType>Phone</PhoneType>\n"+
								"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
							"</PhoneFax>\n"+
						"</ContactDetails>";
				}
				
				if(!(ExtTabDataMap.get("TL_NUMBER").equals("")))
				{
					DocDetXml = DocDetXml+"<Document>\n" +
						"<DocumentType>TDLIC</DocumentType>\n" +
						"<DocumentRefNumber>"+ExtTabDataMap.get("TL_NUMBER")+"</DocumentRefNumber>\n" +
					"</Document>";
				}
				
				
			}
			else
			{
				CIF_ID = RelPartyGridDataMap.get("CIF");

				String CompFlag = RelPartyGridDataMap.get("COMPANYFLAG");
				
				if("Y".equalsIgnoreCase(CompFlag) || "Yes".equalsIgnoreCase(CompFlag)) 
					CompFlag = "C";
				else
					CompFlag = "R";
				
				CustomerType = 	"<CustomerType>"+CompFlag+"</CustomerType>";
				RetailCorpFlag = "<RetailCorpFlag>"+CompFlag+"</RetailCorpFlag>";

				if("R".equalsIgnoreCase(CompFlag))
				{
					if(!(RelPartyGridDataMap.get("EMIRATESID").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>EMID</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("EMIRATESID")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					if(!(RelPartyGridDataMap.get("PASSPORTNUMBER").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>PPT</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("PASSPORTNUMBER")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					if(!(RelPartyGridDataMap.get("VISANUMBER").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>VISA</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("VISANUMBER")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					
					PersonalDetails = "<PersonDetails>";
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("FIRSTNAME")))
						PersonalDetails = PersonalDetails+"<FirstName>"+RelPartyGridDataMap.get("FIRSTNAME")+"</FirstName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("MIDDLENAME")))
						PersonalDetails = PersonalDetails+"<MiddleName>"+RelPartyGridDataMap.get("MIDDLENAME")+"</MiddleName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("LASTNAME")))
						PersonalDetails = PersonalDetails+"<LastName>"+RelPartyGridDataMap.get("LASTNAME")+"</LastName>";
					
					String FullName1 = RelPartyGridDataMap.get("FIRSTNAME") +" "+RelPartyGridDataMap.get("LASTNAME");
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("MIDDLENAME")))
						FullName1 = RelPartyGridDataMap.get("FIRSTNAME") +" "+ RelPartyGridDataMap.get("MIDDLENAME") +" "+RelPartyGridDataMap.get("LASTNAME");
					
					if(!"".equalsIgnoreCase(FullName1.trim()))
						PersonalDetails = PersonalDetails+"<FullName>"+FullName1+"</FullName>";
					else if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")))
						PersonalDetails = PersonalDetails+"<FullName>"+RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")+"</FullName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("NATIONALITY")))
						PersonalDetails = PersonalDetails+"<Nationality>"+RelPartyGridDataMap.get("NATIONALITY")+"</Nationality>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("DATEOFBIRTH")))
						PersonalDetails = PersonalDetails+"<DateOfBirth>"+RelPartyGridDataMap.get("DATEOFBIRTH")+"</DateOfBirth>";
					
					PersonalDetails = PersonalDetails+"</PersonDetails>";
					
				}
				else if("C".equalsIgnoreCase(CompFlag))
				{
					if(!(RelPartyGridDataMap.get("TL_NUMBER").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>TDLIC</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("TL_NUMBER")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					
					OrganizationDetails = "";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")))
						OrganizationDetails = OrganizationDetails + "<CorporateName>"+RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")+"</CorporateName>";
					
					//if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("LASTNAME")) && RelPartyGridDataMap.get("LASTNAME").trim().length() <=30) 
						//OrganizationDetails = OrganizationDetails + "<RepresentativeLastName>"+RelPartyGridDataMap.get("LASTNAME")+"</RepresentativeLastName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("COUNTRY"))) 
						OrganizationDetails = OrganizationDetails + "<CountryOfIncorporation>"+RelPartyGridDataMap.get("COUNTRY")+"</CountryOfIncorporation>";
							
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("DATEOFINCORPORATION"))) 			
						OrganizationDetails = OrganizationDetails + "<DateOfIncorporation>"+RelPartyGridDataMap.get("DATEOFINCORPORATION")+"</DateOfIncorporation>";
					
					if(!OrganizationDetails.equalsIgnoreCase(""))
						OrganizationDetails = "<OrganizationDetails>"+OrganizationDetails+"</OrganizationDetails>";
					
				}
				
								
				if (!"".equalsIgnoreCase(RelPartyGridDataMap.get("RELMOBILENUMBERCOUNTRYCODE")) && !"".equalsIgnoreCase(RelPartyGridDataMap.get("RELMOBILENUMBER")))
				{
					MobileNumber = "+"+RelPartyGridDataMap.get("RELMOBILENUMBERCOUNTRYCODE")+"()"+RelPartyGridDataMap.get("RELMOBILENUMBER");
					MobileNumberDetails = "<ContactDetails>\n"+
							"<PhoneFax>\n"+
								"<PhoneType>Phone</PhoneType>\n"+
								"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
							"</PhoneFax>\n"+
						"</ContactDetails>";
				}
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("else MobileNumberDetails: "+MobileNumberDetails);

			}
			
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("before sInputXML : ");
			java.util.Date d1 = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			String DateExtra2 = sdf1.format(d1)+"+04:00";
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
			String ReqDateTime = sdf2.format(d1);
			
			StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>DEDUP_SUMMARY</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>BPM_MARY_02</MessageId>\n" +
					"<Extra1>REQ||BPM.123</Extra1>\n" +
					"<Extra2>"+DateExtra2+"</Extra2>\n" +
					"</EE_EAI_HEADER>\n"+
					"<CustomerDuplicationListRequest>" +
						"<BankId>RAK</BankId><CIFID>"+CIF_ID+"</CIFID>"+CustomerType+" " +
						" "+RetailCorpFlag+"<EntityType>All</EntityType>" +
						" "+PersonalDetails+" "+
						OrganizationDetails+MobileNumberDetails+DocDetXml+"</CustomerDuplicationListRequest>\n"+
					"</EE_EAI_MESSAGE>");
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe input XML: "+sInputXML);

			String responseXML =iRBLIntegration.socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(),  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("socketConnection responseXML: "+responseXML);

			XMLParser xmlParserDetails= new XMLParser(responseXML);
		    String return_code = xmlParserDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String MsgFormat = xmlParserDetails.getValueOf("MsgFormat");
		    String return_desc = xmlParserDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserDetails.getValueOf("Description").replace("'", "");
			
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    
		    //Inserting in Integration details table
		    String CallStatus = "";
		    if(return_code.equals("0000"))
		    	CallStatus="Success";
		    else
		    	CallStatus="Failure";
		    java.util.Date d2 = new Date();
		    String ResDateTime = sdf2.format(d2);
		    String TableName = "USR_0_IRBL_INTEGRATION_DTLS";
		    String columnnames="WI_NAME, CIFID, AccountNumber, CallName, Operation, RequestDateTime, CallStatus, MessageId, ResponseDateTime, ReturnCode, ReturnError";
		    String columnvalues="'"+processInstanceID+"','"+CIF_ID+"','','DEDUP_SUMMARY','','"+ReqDateTime+"','"+CallStatus+"','"+MsgId+"','"+ResDateTime+"','"+return_code+"','"+return_desc+"' ";
			String InputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnnames, columnvalues, TableName);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert DEDUP_SUMMARY "+TableName+" Table : "+InputXML);

			String OutputXML=CommonMethods.WFNGExecute(InputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for DEDUP_SUMMARY apInsert "+TableName+" Table : "+OutputXML);

			XMLParser sXMLParserChild= new XMLParser(OutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    if (StrMainCode.equals("0"))
			   	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+TableName);	
		    else
		       	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+OutputXML);
		    ////////////////////////////////////////
		    
		    if(return_code.equals("0000"))
			{		    	
		    	
				//String CustomerTag = xmlParserDetails.getValueOf("Customer");
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" CustomerTag : "+CustomerTag);
				
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("before Customer");
				while(responseXML.contains("<Customer>"))
				{
					//XMLParser xmlParserChildTagDetails= new XMLParser(CustomerTag);
					String colNames="";
			    	String colValues="";
					String MainCifId = "";
					String MainCustomerName = "";
					String MainDateOfBirth = "";
					String MainNationality = "";
					String MainEmiratesID = "";
					String MainPassportNo="";
					String MainMobileNo="";
					String MainGender="";
					String MainResAddress="";
					
					rowVal = responseXML.substring(responseXML.indexOf("<Customer>"),responseXML.indexOf("</Customer>")+"</Customer>".length());
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", rowVal : "+rowVal);
					if(rowVal.equalsIgnoreCase("<Customer></Customer>"))
						return "No result";
					MainCifId = (rowVal.contains("<CIFID>")) ? rowVal.substring(rowVal.indexOf("<CIFID>")+"</CIFID>".length()-1,rowVal.indexOf("</CIFID>")):"";
					MainCustomerName = (rowVal.contains("<FullName>")) ? rowVal.substring(rowVal.indexOf("<FullName>")+"</FullName>".length()-1,rowVal.indexOf("</FullName>")):"";
					String DateOfBirth = (rowVal.contains("<DateOfBirth>")) ? rowVal.substring(rowVal.indexOf("<DateOfBirth>")+"</DateOfBirth>".length()-1,rowVal.indexOf("</DateOfBirth>")):"";
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", DateOfBirth : "+DateOfBirth);
					MainNationality = (rowVal.contains("<Nationality>")) ? rowVal.substring(rowVal.indexOf("<Nationality>")+"</Nationality>".length()-1,rowVal.indexOf("</Nationality>")):"";
					
					RetailCorpFlag = (rowVal.contains("<RetailCorpFlag>")) ? rowVal.substring(rowVal.indexOf("<RetailCorpFlag>")+"</RetailCorpFlag>".length()-1,rowVal.indexOf("</RetailCorpFlag>")):"";
					
					if("".equalsIgnoreCase(MainCustomerName.trim()))
						MainCustomerName = (rowVal.contains("<CorporateName>")) ? rowVal.substring(rowVal.indexOf("<CorporateName>")+"</CorporateName>".length()-1,rowVal.indexOf("</CorporateName>")):"";
					
					MainDateOfBirth = "";
					if(!(DateOfBirth==null || DateOfBirth.equalsIgnoreCase("")))  // Change by Ajay
					{
						//String StrDOB[] = DateOfBirth.split("-");
						//MainDateOfBirth=StrDOB[2]+"/"+StrDOB[1]+"/"+StrDOB[0];
						MainDateOfBirth = DateOfBirth;
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainDateOfBirth : "+MainDateOfBirth);
					}
							
					int countwhilchk = 0;
					while(rowVal.contains("<Document>"))
					{							
						String rowData = rowVal.substring(rowVal.indexOf("<Document>"),rowVal.indexOf("</Document>")+"</Document>".length());
						String DocumentType = (rowData.contains("<DocumentType>")) ? rowData.substring(rowData.indexOf("<DocumentType>")+"</DocumentType>".length()-1,rowData.indexOf("</DocumentType>")):"";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", DocumentType "+DocumentType);
						//Emirates ID
						if (DocumentType.equalsIgnoreCase("EMID"))
						{
							MainEmiratesID = rowData.substring(rowData.indexOf("<DocumentRefNumber>")+"<DocumentRefNumber>".length(),rowData.indexOf("</DocumentRefNumber>"));
					
						}							
						//passport number
						if (DocumentType.equalsIgnoreCase("PPT"))
						{
							MainPassportNo = rowData.substring(rowData.indexOf("<DocumentRefNumber>")+"<DocumentRefNumber>".length(),rowData.indexOf("</DocumentRefNumber>"));
							
						}
						
						rowVal = rowVal.substring(0,rowVal.indexOf(rowData))+ rowVal.substring(rowVal.indexOf(rowData)+rowData.length());
						
						countwhilchk++;
						if(countwhilchk == 50)
						{
							countwhilchk = 0;
							break;
						}
					
					 }
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainEmiratesID "+MainEmiratesID);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainPassportNo "+MainPassportNo);
					
					
					countwhilchk = 0;
					while(rowVal.contains("<PhoneFax>"))
					{							
						String rowData = rowVal.substring(rowVal.indexOf("<PhoneFax>"),rowVal.indexOf("</PhoneFax>")+"</PhoneFax>".length());
						String PhoneType = (rowData.contains("<PhoneType>")) ? rowData.substring(rowData.indexOf("<PhoneType>")+"</PhoneType>".length()-1,rowData.indexOf("</PhoneType>")):"";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", PhoneType "+PhoneType);
						
						if (PhoneType.equalsIgnoreCase("CELLPH1"))
						{
							MainMobileNo = (rowData.contains("<PhoneValue>")) ? rowData.substring(rowData.indexOf("<PhoneValue>")+"</PhoneValue>".length()-1,rowData.indexOf("</PhoneValue>")):"";
						}							
						
						rowVal = rowVal.substring(0,rowVal.indexOf(rowData))+ rowVal.substring(rowVal.indexOf(rowData)+rowData.length());
							
						countwhilchk++;
						if(countwhilchk == 50)
						{
							countwhilchk = 0;
							break;
						}
					
					 }
					
					countwhilchk = 0;
					String BlacklistedStatusFlag = "";
					String NegativelistedStatusFlag = "";
					while(rowVal.contains("<StatusInfo>"))
					{							
						String rowData = rowVal.substring(rowVal.indexOf("<StatusInfo>"),rowVal.indexOf("</StatusInfo>")+"</StatusInfo>".length());
						String StatusType = (rowData.contains("<StatusType>")) ? rowData.substring(rowData.indexOf("<StatusType>")+"</StatusType>".length()-1,rowData.indexOf("</StatusType>")):"";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+objRespBean.getWorkitemNumber()+", WSNAME: "+objRespBean.getWorkStep()+", PhoneType "+PhoneType);
						
						if (StatusType.equalsIgnoreCase("Blacklisted"))
						{
							BlacklistedStatusFlag = (rowData.contains("<StatusFlag>")) ? rowData.substring(rowData.indexOf("<StatusFlag>")+"</StatusFlag>".length()-1,rowData.indexOf("</StatusFlag>")):"";
						}	
						else if (StatusType.equalsIgnoreCase("Negativelisted"))
						{
							NegativelistedStatusFlag = (rowData.contains("<StatusFlag>")) ? rowData.substring(rowData.indexOf("<StatusFlag>")+"</StatusFlag>".length()-1,rowData.indexOf("</StatusFlag>")):"";
						}
						
						rowVal = rowVal.substring(0,rowVal.indexOf(rowData))+ rowVal.substring(rowVal.indexOf(rowData)+rowData.length());
							
						countwhilchk++;
						if(countwhilchk == 50)
						{
							countwhilchk = 0;
							break;
						}
					
					 }
					
					// Addition of Cust Status and Dormancy in Dedupe window added on 18/10/2020
					String CustStatus = (rowVal.contains("<CustStatus>")) ? rowVal.substring(rowVal.indexOf("<CustStatus>")+"</CustStatus>".length()-1,rowVal.indexOf("</CustStatus>")):"";
					CustDormancy = (rowVal.contains("<CustDormancy>")) ? rowVal.substring(rowVal.indexOf("<CustDormancy>")+"</CustDormancy>".length()-1,rowVal.indexOf("</CustDormancy>")):"";
					if("Y".equalsIgnoreCase(CustDormancy.trim()))
						CustDormancy = "dormant";	
					/////////////////////////////////////////////
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("before dedupe hashmap");
					
					HashMap<String,String> obj1= new HashMap<String,String>();
					obj1.put("WI_NAME",processInstanceID);
					if(MainCIF_Flag)
						obj1.put("RELATEDPARTYID","");
					else
						obj1.put("RELATEDPARTYID", RelPartyGridDataMap.get("RELATEDPARTYID"));
					
					obj1.put("CIF_ID", MainCifId);
					DedupeGridCIFID.add(MainCifId);
					obj1.put("CustomerFULL_NAME", MainCustomerName);
					DedupeGridFullName.add(MainCustomerName);
					obj1.put("DOB", MainDateOfBirth);
					DedupeGridDOB.add(MainDateOfBirth);
					obj1.put("GENDER", MainGender);
					DedupeGridGender.add(MainGender);
					obj1.put("EMIRATES_ID", MainEmiratesID);
					DedupeGridEmiratesID.add(MainEmiratesID);
					obj1.put("PASSPORT_Number", MainPassportNo);
					DedupeGridPassportNo.add(MainPassportNo);
					obj1.put("NATIONALITY", MainNationality);
					DedupeGridNationality.add(MainNationality);
					obj1.put("RESIDENTIAL_ADDRESS", MainResAddress);
					DedupeGridResAddress.add(MainResAddress);
					obj1.put("MOBILE_Number", MainMobileNo);
					DedupeGridMobNo.add(MainMobileNo);
					obj1.put("IsBlackListed", BlacklistedStatusFlag);
					DedupeGridBlacklistedFlag.add(BlacklistedStatusFlag);
					obj1.put("IsNegativeListed", NegativelistedStatusFlag);
					DedupeGridNegativelistedFlag.add(NegativelistedStatusFlag);
					obj1.put("RetailCorpFlag", RetailCorpFlag);
					
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("before dedupe for loop");
					
					//Appending Dedupe Output values to Dedupe DB Columns
					for(Map.Entry<String,String> map : obj1.entrySet())
					{
						iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("iterating ... map.getKey() :"+map.getKey().toString()+" map.getValue() : "+map.getValue().toString());
						
						if(colNames.equals("") && !map.getValue().toString().equals(""))
				    	{
							colNames= map.getKey();							
							colValues=map.getValue();
				    	}
				    	else if(!colNames.equals("") && !map.getValue().toString().equals(""))
				    	{
				    		colNames= colNames+","+map.getKey();							
				    		colValues=colValues+","+map.getValue();
				    	}
					}
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe colNames : "+colNames+" colValues : "+colValues);
					
					colValues=colValues.replaceAll(",", "','");
				    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Aftr replace colValues :"+colValues);
				    
					String sWhere="";
				    if(MainCIF_Flag)
				    	sWhere="WI_NAME='"+processInstanceID+"' AND CIF_ID='"+obj1.get("CIF_ID")+"'";
				    else
				    	sWhere="WI_NAME='"+processInstanceID+"' AND CIF_ID='"+obj1.get("CIF_ID")+"' AND RELATEDPARTYID='"+RelPartyGridDataMap.get("RELATEDPARTYID")+"'";
				    
					
					String status= iRBLIntegration.UpdateGridTableMWResponse(colNames,"'"+colValues+"'",DedupeTable,sWhere);
				    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("UpdateGridTable status : "+status+" for "+DedupeTable);
				    colNames="";
				    colValues="";
				    
					responseXML = responseXML.substring(0,responseXML.indexOf("<Customer>"))+ responseXML.substring(responseXML.indexOf("</Customer>")+"</Customer>".length());
				}
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Size After Adding Dup : ");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", @@@@@@@@@@ : after add of dedupe details");
				//CheckGridDataMap.put("DEDUPE_STATUS", "Success");
				
				//****************************
			    if(MainCIF_Flag)
			    {
					DocName = "Dedupe_ForCompany";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
				}
			    else
			    {
			    	DocName = "Dedupe_ForSignatories";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
			    	
				}
			    //*****************************
				
				//Clearing data in Arraylist
				DedupeGridCIFID.clear();
				DedupeGridFullName.clear();
				DedupeGridDOB.clear();
				DedupeGridGender.clear();
				DedupeGridEmiratesID.clear();
				DedupeGridPassportNo.clear();
				DedupeGridNationality.clear();
				DedupeGridResAddress.clear();
				DedupeGridMobNo.clear();
				DedupeGridBlacklistedFlag.clear();
				DedupeGridNegativelistedFlag.clear();
			    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Response of attach doc in dedupe call"+returnValue);
				
				return "Success~"+CustDormancy+"~End";
			}			
			else
			{
				//setControlValue("MAIN_CIF_SEARCH","N");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Error in Response of dedupe call"+return_code);
				//setControlValue("DedupeStatus","N");	
				//setControlValue("DUPLICATE_CIF_FOUND","No Result");
				//CheckGridDataMap.put("DEDUPE_STATUS", "Failure");
				return "Failure";
			}
		}
		catch(Exception e)
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception in DedupeCall Fn for WI :"+processInstanceID+", exception:"+e.getMessage()+", print:"+ CommonMethods.printException(e));
			return "Failure";
		}
	}
		
	public static	String BlacklistCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap,HashMap<String, String> RelPartyGridDataMap, boolean MainCIF_Flag)
	{	
		try
		{
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside BlacklistCall Fn");

			String DocDetXml="";
			String NATIONALITY = "";			
			String DOB = "";
			String MidName = "";
			String FullName = "";
			String MobileNumberDetails = "";
			String MobileNumber = "";
			String CIF_ID="";
			String First_Name="";
			String Last_Name="";
			String Maritalstatus="";
			String OrganizationDetails = "";
			String PersonalDetails = "";
			String CustomerType = "";
			String RetailCorpFlag = "";
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainCIF_Flag :"+MainCIF_Flag);
			if(MainCIF_Flag)
			{
				CIF_ID = ExtTabDataMap.get("CIF_NUMBER");
				CustomerType = 	"<CustomerType>C</CustomerType>";
				RetailCorpFlag = "<RetailCorpFlag>C</RetailCorpFlag>";
				OrganizationDetails = "<OrganizationDetails>"+
						"<CorporateName>"+ExtTabDataMap.get("COMPANY_NAME")+"</CorporateName>"+
						//"<RepresentativeLastName>"+ExtTabDataMap.get("APPLICANT_FULL_NAME")+"</RepresentativeLastName>"+
						"<CountryOfIncorporation>"+ExtTabDataMap.get("COUNTRYOFINCORPORATION")+"</CountryOfIncorporation>"+
						"<DateOfIncorporation>"+ExtTabDataMap.get("DATEOFINCORPORATION")+"</DateOfIncorporation></OrganizationDetails>";
				
				if (!"".equalsIgnoreCase(ExtTabDataMap.get("MOBILENUMBERCOUNTRYCODE")) && !"".equalsIgnoreCase(ExtTabDataMap.get("MOBILENUMBER")))
				{
					MobileNumber = "+"+ExtTabDataMap.get("MOBILENUMBERCOUNTRYCODE")+"()"+ExtTabDataMap.get("MOBILENUMBER");
					MobileNumberDetails = "<ContactDetails>\n"+
							"<PhoneFax>\n"+
								"<PhoneType>Phone</PhoneType>\n"+
								"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
							"</PhoneFax>\n"+
						"</ContactDetails>";
				}
				
				if(!(ExtTabDataMap.get("TL_NUMBER").equals("")))
				{
					DocDetXml = DocDetXml+"<Document>\n" +
						"<DocumentType>TDLIC</DocumentType>\n" +
						"<DocumentRefNumber>"+ExtTabDataMap.get("TL_NUMBER")+"</DocumentRefNumber>\n" +
					"</Document>";
				}
				
				
			}
			else
			{
				CIF_ID = RelPartyGridDataMap.get("CIF");

				String CompFlag = RelPartyGridDataMap.get("COMPANYFLAG");
				
				if("Y".equalsIgnoreCase(CompFlag) || "Yes".equalsIgnoreCase(CompFlag)) 
					CompFlag = "C";
				else
					CompFlag = "R";
				
				CustomerType = 	"<CustomerType>"+CompFlag+"</CustomerType>";
				RetailCorpFlag = "<RetailCorpFlag>"+CompFlag+"</RetailCorpFlag>";
				
				if("R".equalsIgnoreCase(CompFlag))
				{
					if(!(RelPartyGridDataMap.get("EMIRATESID").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>EMID</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("EMIRATESID")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					if(!(RelPartyGridDataMap.get("PASSPORTNUMBER").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>PPT</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("PASSPORTNUMBER")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					if(!(RelPartyGridDataMap.get("VISANUMBER").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>VISA</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("VISANUMBER")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					
					PersonalDetails = "<PersonDetails>";
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("FIRSTNAME")))
						PersonalDetails = PersonalDetails+"<FirstName>"+RelPartyGridDataMap.get("FIRSTNAME")+"</FirstName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("MIDDLENAME")))
						PersonalDetails = PersonalDetails+"<MiddleName>"+RelPartyGridDataMap.get("MIDDLENAME")+"</MiddleName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("LASTNAME")))
						PersonalDetails = PersonalDetails+"<LastName>"+RelPartyGridDataMap.get("LASTNAME")+"</LastName>";
					
					String FullName1 = RelPartyGridDataMap.get("FIRSTNAME") +" "+RelPartyGridDataMap.get("LASTNAME");
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("MIDDLENAME")))
						FullName1 = RelPartyGridDataMap.get("FIRSTNAME") +" "+ RelPartyGridDataMap.get("MIDDLENAME") +" "+RelPartyGridDataMap.get("LASTNAME");
					
					if(!"".equalsIgnoreCase(FullName1.trim()))
						PersonalDetails = PersonalDetails+"<FullName>"+FullName1+"</FullName>";
					else if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")))
						PersonalDetails = PersonalDetails+"<FullName>"+RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")+"</FullName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("NATIONALITY")))
						PersonalDetails = PersonalDetails+"<Nationality>"+RelPartyGridDataMap.get("NATIONALITY")+"</Nationality>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("DATEOFBIRTH")))
						PersonalDetails = PersonalDetails+"<DateOfBirth>"+RelPartyGridDataMap.get("DATEOFBIRTH")+"</DateOfBirth>";
					
					PersonalDetails = PersonalDetails+"</PersonDetails>";
					
				}
				else if("C".equalsIgnoreCase(CompFlag))
				{
					if(!(RelPartyGridDataMap.get("TL_NUMBER").equals("")))
					{
						DocDetXml = DocDetXml+"<Document>\n" +
							"<DocumentType>TDLIC</DocumentType>\n" +
							"<DocumentRefNumber>"+RelPartyGridDataMap.get("TL_NUMBER")+"</DocumentRefNumber>\n" +
						"</Document>";
					}
					
					OrganizationDetails = "";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")))
						OrganizationDetails = OrganizationDetails + "<CorporateName>"+RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY")+"</CorporateName>";
					
					//if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("LASTNAME").trim()) && RelPartyGridDataMap.get("LASTNAME").trim().length() <=30) 
						//OrganizationDetails = OrganizationDetails + "<RepresentativeLastName>"+RelPartyGridDataMap.get("LASTNAME")+"</RepresentativeLastName>";
					
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("COUNTRY"))) 
						OrganizationDetails = OrganizationDetails + "<CountryOfIncorporation>"+RelPartyGridDataMap.get("COUNTRY")+"</CountryOfIncorporation>";
							
					if(!"".equalsIgnoreCase(RelPartyGridDataMap.get("DATEOFINCORPORATION"))) 			
						OrganizationDetails = OrganizationDetails + "<DateOfIncorporation>"+RelPartyGridDataMap.get("DATEOFINCORPORATION")+"</DateOfIncorporation>";
				
					if(!OrganizationDetails.equalsIgnoreCase(""))
						OrganizationDetails = "<OrganizationDetails>"+OrganizationDetails+"</OrganizationDetails>";
				
				}
				
				
				
				if (!"".equalsIgnoreCase(RelPartyGridDataMap.get("RELMOBILENUMBERCOUNTRYCODE")) && !"".equalsIgnoreCase(RelPartyGridDataMap.get("RELMOBILENUMBER")))
				{
					MobileNumber = "+"+RelPartyGridDataMap.get("RELMOBILENUMBERCOUNTRYCODE")+"()"+RelPartyGridDataMap.get("RELMOBILENUMBER");
					MobileNumberDetails = "<ContactDetails>\n"+
							"<PhoneFax>\n"+
								"<PhoneType>Phone</PhoneType>\n"+
								"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
							"</PhoneFax>\n"+
						"</ContactDetails>";
				}
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("else MobileNumberDetails: "+MobileNumberDetails);

			}
			
			
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("before sInputXML : ");
			
			java.util.Date d1 = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			String DateExtra2 = sdf1.format(d1)+"+04:00";
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
			String ReqDateTime = sdf2.format(d1);
			
			StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>\n" +
					//"<ProcessName>RAO</ProcessName>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>BLACKLIST_DETAILS</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>BPM_BLACK_LIST_009</MessageId>\n" +
					"<Extra1>REQ||BPM.123</Extra1>\n" +
					"<Extra2>"+DateExtra2+"</Extra2>\n" +
					"</EE_EAI_HEADER>" +
						"<CustomerBlackListRequest>"+
						"<BankId>RAK</BankId><CIFID>"+CIF_ID+"</CIFID>"+CustomerType+" " +
						" "+RetailCorpFlag+"<EntityType>All</EntityType>" +
						" "+PersonalDetails+" "+
						OrganizationDetails+MobileNumberDetails+DocDetXml+"</CustomerBlackListRequest>\n" +
					"</EE_EAI_MESSAGE>");
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Blacklist input XML: "+sInputXML);

			String responseXML = iRBLIntegration.socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);
	
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);
	
			XMLParser xmlParserDetails= new XMLParser(responseXML);
		    String return_code = xmlParserDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);
	
		    String return_desc = xmlParserDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserDetails.getValueOf("Description").replace("'", "");
			
		    String MsgFormat = xmlParserDetails.getValueOf("MsgFormat");
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    
		    
		  //Inserting in Integration details table 
		    String CallStatus = "";
		    if(return_code.equals("0000") || return_code.equals("CINF0516"))
		    	CallStatus="Success";
		    else
		    	CallStatus="Failure";
		    java.util.Date d2 = new Date();
		    String ResDateTime = sdf2.format(d2);
		    String TableName = "USR_0_IRBL_INTEGRATION_DTLS";
		    String columnnames="WI_NAME, CIFID, AccountNumber, CallName, Operation, RequestDateTime, CallStatus, MessageId, ResponseDateTime, ReturnCode, ReturnError";
		    String columnvalues="'"+processInstanceID+"','"+CIF_ID+"','','BLACKLIST_DETAILS','','"+ReqDateTime+"','"+CallStatus+"','"+MsgId+"','"+ResDateTime+"','"+return_code+"','"+return_desc+"' ";
			String InputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnnames, columnvalues, TableName);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert BLACKLIST_DETAILS "+TableName+" Table : "+InputXML);

			String OutputXML=CommonMethods.WFNGExecute(InputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for BLACKLIST_DETAILS apInsert "+TableName+" Table : "+OutputXML);

			XMLParser sXMLParserChild= new XMLParser(OutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    if (StrMainCode.equals("0"))
			   	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+TableName);	
		    else
		       	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+OutputXML);
		    ////////////////////////////////////////
		    
		    
		    if(return_code.equals("0000"))
			{
		    	//String CustomerTag = xmlParserDetails.getValueOf("Customer");
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" CustomerTag : "+CustomerTag);
				
				//JSONArray jsonArray1=new JSONArray();
				while(responseXML.contains("<Customer>"))
				{
					String colNames="";
				    String colValues="";
					String MainCifId = "";
					String MainCifStatus = "";
					String MainEmiratesID = "";
					String MainPassportNo="";
					String MainMobileNo="";
					String MainCustomerName="";
					String MainBlacklistFlag="";
					String MainNegatedFlag="";
					
					rowVal = responseXML.substring(responseXML.indexOf("<Customer>"),responseXML.indexOf("</Customer>")+"</Customer>".length());
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", rowVal : "+rowVal);
					if(rowVal.equalsIgnoreCase("<Customer></Customer>"))
						return "No result";
					MainCifId = (rowVal.contains("<CIFID>")) ? rowVal.substring(rowVal.indexOf("<CIFID>")+"</CIFID>".length()-1,rowVal.indexOf("</CIFID>")):"";
					MainCifStatus = (rowVal.contains("<CustomerStatus>")) ? rowVal.substring(rowVal.indexOf("<CustomerStatus>")+"</CustomerStatus>".length()-1,rowVal.indexOf("</CustomerStatus>")):"";
                    
					// customer full name
                    String FirstName = (rowVal.contains("<FirstName>")) ? rowVal.substring(rowVal.indexOf("<FirstName>")+"</FirstName>".length()-1,rowVal.indexOf("</FirstName>")):"";
                    String MiddleName = (rowVal.contains("<MiddleName>")) ? rowVal.substring(rowVal.indexOf("<MiddleName>")+"</MiddleName>".length()-1,rowVal.indexOf("</MiddleName>")):"";
                    String LastName = (rowVal.contains("<LastName>")) ? rowVal.substring(rowVal.indexOf("<LastName>")+"</LastName>".length()-1,rowVal.indexOf("</LastName>")):"";
                    String fullName=FirstName+" "+MiddleName+" "+LastName;
					MainCustomerName = (rowVal.contains("<fullName>")) ? rowVal.substring(rowVal.indexOf("<fullName>")+"</fullName>".length()-1,rowVal.indexOf("</fullName>")):"";
					
					if("".equalsIgnoreCase(fullName.trim()))
						fullName = (rowVal.contains("<CorporateName>")) ? rowVal.substring(rowVal.indexOf("<CorporateName>")+"</CorporateName>".length()-1,rowVal.indexOf("</CorporateName>")):"";
					
					int countwhilchk = 0;
					while(rowVal.contains("<Document>"))
					{							
						String rowData = rowVal.substring(rowVal.indexOf("<Document>"),rowVal.indexOf("</Document>")+"</Document>".length());
						String DocumentType = (rowData.contains("<DocumentType>")) ? rowData.substring(rowData.indexOf("<DocumentType>")+"</DocumentType>".length()-1,rowData.indexOf("</DocumentType>")):"";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", DocumentType "+DocumentType);
						//Emirates ID
						if (DocumentType.equalsIgnoreCase("EMID"))
						{
							MainEmiratesID = rowData.substring(rowData.indexOf("<DocumentRefNumber>")+"<DocumentRefNumber>".length(),rowData.indexOf("</DocumentRefNumber>"));
						}							
						//passport number
						if (DocumentType.equalsIgnoreCase("PPT"))
						{
							MainPassportNo = rowData.substring(rowData.indexOf("<DocumentRefNumber>")+"<DocumentRefNumber>".length(),rowData.indexOf("</DocumentRefNumber>"));
						}
							rowVal = rowVal.substring(0,rowVal.indexOf(rowData))+ rowVal.substring(rowVal.indexOf(rowData)+rowData.length());
							
							countwhilchk++;
							if(countwhilchk == 50)
							{
								countwhilchk = 0;
								break;
							}
					
					 }
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainEmiratesID "+MainEmiratesID);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainPassportNo "+MainPassportNo);
					
					countwhilchk = 0;
					while(rowVal.contains("<StatusInfo>"))
					{
						String rowData = rowVal.substring(rowVal.indexOf("<StatusInfo>"),rowVal.indexOf("</StatusInfo>")+"</StatusInfo>".length());
						
						String StatusType = (rowData.contains("<StatusType>")) ? rowData.substring(rowData.indexOf("<StatusType>")+"</StatusType>".length()-1,rowData.indexOf("</StatusType>")):"";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", StatusType "+StatusType);
						// Blacklist Flag
						if (StatusType.equalsIgnoreCase("Black List"))
						{
							MainBlacklistFlag = rowData.substring(rowData.indexOf("<StatusFlag>")+"<StatusFlag>".length(),rowData.indexOf("</StatusFlag>"));
							
						}
						
						// Negated Flag
						if (StatusType.equalsIgnoreCase("Negative List"))
						{
							MainNegatedFlag = rowData.substring(rowData.indexOf("<StatusFlag>")+"<StatusFlag>".length(),rowData.indexOf("</StatusFlag>"));
							
						}
						rowVal = rowVal.substring(0,rowVal.indexOf(rowData))+ rowVal.substring(rowVal.indexOf(rowData)+rowData.length());
					
						countwhilchk++;
						if(countwhilchk == 50)
						{
							countwhilchk = 0;
							break;
						}
					}	
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainBlacklistFlag "+MainBlacklistFlag);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainNegatedFlag "+MainNegatedFlag);
					
				
					// mobile number
					/*MainMobileNo=(rowVal.contains("<MOBILE_NUMBER>")) ? rowVal.substring(rowVal.indexOf("<MOBILE_NUMBER>")+"</MOBILE_NUMBER>".length()-1,rowVal.indexOf("</MOBILE_NUMBER>")):"";
					String phonetype = (rowVal.contains("<phonetype>")) ? rowVal.substring(rowVal.indexOf("<phonetype>")+"</phonetype>".length()-1,rowVal.indexOf("</phonetype>")):"";
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", phonetype "+phonetype);
					if(phonetype.equalsIgnoreCase("CELLPH1"))
					{
						MainMobileNo = rowVal.substring(rowVal.indexOf("<PhoneValue>")+"<PhoneValue>".length(),rowVal.indexOf("</PhoneValue>"));
					
					}*/
					
					countwhilchk = 0;
					while(rowVal.contains("<PhoneFax>"))
					{							
						String rowData = rowVal.substring(rowVal.indexOf("<PhoneFax>"),rowVal.indexOf("</PhoneFax>")+"</PhoneFax>".length());
						String PhoneType = (rowData.contains("<PhoneType>")) ? rowData.substring(rowData.indexOf("<PhoneType>")+"</PhoneType>".length()-1,rowData.indexOf("</PhoneType>")):"";
						//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", PhoneType "+PhoneType);
						
						if (PhoneType.equalsIgnoreCase("CELLPH1"))
						{
							MainMobileNo = (rowData.contains("<PhoneValue>")) ? rowData.substring(rowData.indexOf("<PhoneValue>")+"</PhoneValue>".length()-1,rowData.indexOf("</PhoneValue>")):"";
						}							
						
						rowVal = rowVal.substring(0,rowVal.indexOf(rowData))+ rowVal.substring(rowVal.indexOf(rowData)+rowData.length());
							
						countwhilchk++;
						if(countwhilchk == 50)
						{
							countwhilchk = 0;
							break;
						}					
					 }
					
					HashMap<String,String> obj1=new HashMap<String,String>();
					//JSONObject obj1=new JSONObject();
					
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", MainCifId :"+MainCifId);
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("before Blacklist hashmap");
					
					// Match Status Updated as pat of JIRA POLP-899, If Blacklist Status or Negative List status is Y then Match Found will be true or else false
					String MatchStatus = "false";
					if("Y".equalsIgnoreCase(MainBlacklistFlag.trim()) || "Y".equalsIgnoreCase(MainNegatedFlag.trim()))
						MatchStatus = "true";
					
					obj1.put("WI_NAME",processInstanceID);
					if(MainCIF_Flag)
						obj1.put("RELATEDPARTYID","");
					else
						obj1.put("RELATEDPARTYID", RelPartyGridDataMap.get("RELATEDPARTYID"));
					
					obj1.put("CIF_ID", MainCifId);
					obj1.put("CIF_STATUS", MainCifStatus);
					obj1.put("CUSTOMER_FULL_NAME", fullName);
					obj1.put("EMIRATES_ID", MainEmiratesID);
					obj1.put("PASSPORT_Number", MainPassportNo);
					
					//obj1.put("RESIDENTIAL_ADDRESS", "");
					obj1.put("MOBILE_NO", MainMobileNo);
					obj1.put("BLACKLIST_FLAG", MainBlacklistFlag);
					//obj1.put("BLACKLIST_NOTES", "");
					//obj1.put("BLACKLIST_REASON", "");
					//obj1.put("BLACKLIST_CODES", "");
					obj1.put("NEGATED_FLAG", MainNegatedFlag);
					//obj1.put("NEGATED_NOTES", "");
					//obj1.put("NEGATED_REASON", "");
					//obj1.put("NEGATED_CODE", "");		
					obj1.put("MATCH_STATUS", MatchStatus);

					BlacklistGridCIFID.add(MainCifId);
					BlacklistGridCifStatus.add(MainCifStatus);
					BlacklistGridFullName.add(fullName);
					BlacklistGridEmiratesID.add(MainEmiratesID);
					BlacklistGridPassportNo.add(MainPassportNo);
					BlacklistGridResAddress.add("");
					BlacklistGridMobNo.add(MainMobileNo);
					BlacklistGridBlacklistedFlag.add(MainBlacklistFlag);
					BlacklistGridNegatedFlag.add(MainNegatedFlag);
					
					//Appending Blacklist Output values to Blacklist DB Columns
					for(Map.Entry<String,String> map : obj1.entrySet())
					{
						if(colNames.equals("") && !map.getValue().toString().equals(""))
				    	{
							colNames= map.getKey();
							colValues=map.getValue();
				    	}
				    	else if(!colNames.equals("") && !map.getValue().toString().equals(""))
				    	{
				    		colNames= colNames+","+map.getKey();
				    		colValues=colValues+","+map.getValue();
				    	}
					}
					//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Blackllist colNames : "+colNames+" colValues : "+colValues);
					
					colValues=colValues.replaceAll(",", "','");
				    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Aftr replace colValues :"+colValues);
				    
				    String sWhere="";
				    if(MainCIF_Flag)
				    	sWhere="WI_NAME='"+processInstanceID+"' AND CIF_ID='"+obj1.get("CIF_ID")+"'";
				    else
				    	sWhere="WI_NAME='"+processInstanceID+"' AND CIF_ID='"+obj1.get("CIF_ID")+"' AND RELATEDPARTYID='"+RelPartyGridDataMap.get("RELATEDPARTYID")+"'";
				    
				    
				    String status= iRBLIntegration.UpdateGridTableMWResponse(colNames,"'"+colValues+"'",BlacklistTable,sWhere);
				    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("UpdateGridTable status : "+status+" for "+BlacklistTable);
				    colNames="";
				    colValues="";
				    
					responseXML = responseXML.substring(0,responseXML.indexOf("<Customer>"))+ responseXML.substring(responseXML.indexOf("</Customer>")+"</Customer>".length());
				}
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Size After Adding Blacklist : ");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", @@@@@@@@@@ : after add of Blacklist details");
				
				//CheckGridDataMap.put("BLACKLIST_STATUS", "Success");
				//****************************
			    if(MainCIF_Flag)
			    {
					DocName = "Blacklist_ForCompany";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
			    }
			    else
			    {
			    	DocName = "Blacklist_ForSignatories";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
			    }
			    //*****************************
				
				//Clearing data in Arraylist for MainCIF
				BlacklistGridCIFID.clear();
				BlacklistGridCifStatus.clear();
				BlacklistGridFullName.clear();
				BlacklistGridEmiratesID.clear();
				BlacklistGridPassportNo.clear();
				BlacklistGridResAddress.clear();
				BlacklistGridMobNo.clear();
				BlacklistGridBlacklistedFlag.clear();
				BlacklistGridNegatedFlag.clear();
				
			    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Response of attach doc in dedupe call"+returnValue);
								
				return "Success";
			}
		    else if (return_code.equals("CINF0516"))
		    {
		    	if(MainCIF_Flag)
			    {
					DocName = "Blacklist_ForCompany";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
			    }
			    else
			    {
			    	DocName = "Blacklist_ForSignatories";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
			    }
		    	// No records found
		    	return "Success";
		    }
			else
			{
				//setControlValue("MAIN_CIF_SEARCH","N");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Error in Response of dedupe call"+return_code);
				//setControlValue("DedupeStatus","N");	
				//setControlValue("DUPLICATE_CIF_FOUND","No Result");
				//CheckGridDataMap.put("BLACKLIST_STATUS", "Failure");
				return "Failure";
			}
		    
		    //return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		}
		catch(Exception e)
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception in BlacklistCall Fn for WI: "+processInstanceID+", exception:"+e.getMessage()+", print:"+ CommonMethods.printException(e));
			return "Failure";
		}
	}
	
	public static	String FircosoftCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> RelPartyGridDataMap, boolean MainCIF_Flag)
	{
		try
		{
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside FircosoftCall Fn");
			String RetStatus = "";
			String mqInputRequest = null;
			String DocDetXml="";
			String CIF_ID="";
			String NATIONALITY="";
			String FullName="";
			String DOB="";
			String gender="";
			String RESIDENCEADDRCOUNTRY="";
			String PASSPORT_NUMBER="";
			String ReferenceNo = getFircoReferenceNumber(processInstanceID);
			String MidName="";
			String Details_For="";
			String recordType = "";
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MainCIF_Flag :"+MainCIF_Flag);
			if(MainCIF_Flag)
			{
				CIF_ID = ExtTabDataMap.get("CIF_NUMBER");				
				
				//NATIONALITY = ExtTabDataMap.get("NATIONALITY");
				
				DOB = ExtTabDataMap.get("DATEOFINCORPORATION");
				
				
				RESIDENCEADDRCOUNTRY = ExtTabDataMap.get("COUNTRYOFINCORPORATION");

				FullName=ExtTabDataMap.get("COMPANY_NAME");
				recordType = "C";
			}
			else
			{
				String CompayFlag = RelPartyGridDataMap.get("COMPANYFLAG");
				NATIONALITY = RelPartyGridDataMap.get("NATIONALITY");
				CIF_ID = RelPartyGridDataMap.get("CIF");

				PASSPORT_NUMBER = RelPartyGridDataMap.get("PASSPORTNUMBER");				
				
				if("Y".equalsIgnoreCase(CompayFlag))
				{
					FullName = RelPartyGridDataMap.get("NAME_OF_SISTER_COMPANY").trim();
					DOB = RelPartyGridDataMap.get("DATEOFINCORPORATION");
					RESIDENCEADDRCOUNTRY = RelPartyGridDataMap.get("COUNTRY");
					recordType = "C";
				}
				
				if("N".equalsIgnoreCase(CompayFlag))
				{
					String FirstName = RelPartyGridDataMap.get("FIRSTNAME").trim();
					String MiddleName = RelPartyGridDataMap.get("MIDDLENAME").trim();
					String LastName = RelPartyGridDataMap.get("LASTNAME").trim();
				
					FullName = FirstName + " " + LastName;
					if(!"".equalsIgnoreCase(MiddleName))
						FullName = FirstName+" "+ MiddleName + " " + LastName;
										
					gender = RelPartyGridDataMap.get("GENDER");
					if((gender).equalsIgnoreCase("F"))
						gender = "Female";
					if((gender).equalsIgnoreCase("M"))
						gender = "Male";
					
					DOB = RelPartyGridDataMap.get("DATEOFBIRTH");
					RESIDENCEADDRCOUNTRY = RelPartyGridDataMap.get("COUNTRYOFRESIDENCE");
					recordType = "I";
				}	
			}	
			
			java.util.Date d1 = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			String DateExtra2 = sdf1.format(d1)+"+04:00";
			
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
			String ReqDateTime = sdf2.format(d1);
			String RequestingUnitName = "BUSINESSFINANCESME"; // earlier it was CENTRALOPERATIONSDUBAI, changed as per raina's mail subject - Digital Loan - Fircotrust
			StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>\n"+
								"<EE_EAI_HEADER>\n"+
								"<MsgFormat>COMPLIANCE_CHECK</MsgFormat>\n"+
								"<MsgVersion>0001</MsgVersion>\n"+
								"<RequestorChannelId>BPM</RequestorChannelId>\n"+
								"<RequestorUserId>RAKUSER</RequestorUserId>\n"+
								"<RequestorLanguage>E</RequestorLanguage>\n"+
								"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n"+
								"<ReturnCode>911</ReturnCode>\n"+
								"<ReturnDesc>Issuer Timed Out</ReturnDesc>\n"+
								"<MessageId>Test123456</MessageId>\n"+
								"<Extra1>REQ||PERCOMER.PERCOMER</Extra1>\n"+
								"<Extra2>"+DateExtra2+"</Extra2>\n"+
								"</EE_EAI_HEADER>\n"+
								"<ComplianceCheckRequest><DisplayAlertsFlag>0</DisplayAlertsFlag>" +
								"<RetryRequiredFlag>N</RetryRequiredFlag><RequestingUnitName>"+RequestingUnitName+"</RequestingUnitName>" +
								"<RecordType>"+recordType+"</RecordType>"+
								"<ReferenceNo>"+ReferenceNo+"</ReferenceNo>" +
								"<EntityName>"+FullName+"</EntityName>" +
								"<Gender>"+gender+"</Gender>" +
								"<DateOfBirthOrIncorporation>"+DOB+"</DateOfBirthOrIncorporation>" +
								"<Nationality>"+NATIONALITY+"</Nationality>" +
								"<CountryOfResidence>"+RESIDENCEADDRCOUNTRY+"</CountryOfResidence>" +
								"<PassportNumber>"+PASSPORT_NUMBER+"</PassportNumber>" +
								"<PreviousPassportNo>ok</PreviousPassportNo>" +
						"</ComplianceCheckRequest>"+
					"</EE_EAI_MESSAGE>");				
		
	    				
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("firco input XML: "+sInputXML);

			String responseXML = iRBLIntegration.socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc").replace("'", "");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description").replace("'", "");
			
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
			String MsgFormat = xmlParserSocketDetails.getValueOf("MsgFormat");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    
		    
		  //Inserting in Integration details table 
		    String CallStatus = "";
		    if(return_code.equals("0000") || return_code.equals("FFF002") || return_code.equals("FFF_OK") || return_code.equals("FFFBAD") || return_code.equals("FFFPEN"))
		    	CallStatus="Success";
		    else
		    	CallStatus="Failure";
		    java.util.Date d2 = new Date();
		    String ResDateTime = sdf2.format(d2);
		    String TableName = "USR_0_IRBL_INTEGRATION_DTLS";
		    String columnnames="WI_NAME, CIFID, AccountNumber, CallName, Operation, RequestDateTime, CallStatus, MessageId, ResponseDateTime, ReturnCode, ReturnError";
		    String columnvalues="'"+processInstanceID+"','"+CIF_ID+"','','COMPLIANCE_CHECK','','"+ReqDateTime+"','"+CallStatus+"','"+MsgId+"','"+ResDateTime+"','"+return_code+"','"+return_desc+"' ";
			String InputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnnames, columnvalues, TableName);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert COMPLIANCE_CHECK "+TableName+" Table : "+InputXML);

			String OutputXML=CommonMethods.WFNGExecute(InputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for COMPLIANCE_CHECK apInsert "+TableName+" Table : "+OutputXML);

			XMLParser sXMLParserChild= new XMLParser(OutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    if (StrMainCode.equals("0"))
			   	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+TableName);	
		    else
		       	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+OutputXML);
		    ////////////////////////////////////////
		    
		    
		    if(return_code.equals("0000") || return_code.equals("FFF002") || return_code.equals("FFF_OK") || return_code.equals("FFFBAD") || return_code.equals("FFFPEN"))
			{
				//JSONArray jsonArray1=new JSONArray();
				if(responseXML.contains("<AlertDetails>"))
				{	
						String AlertDetailsTagResponse=responseXML.substring(responseXML.indexOf("<AlertDetails>")+"</AlertDetails>".length()-1,responseXML.indexOf("</AlertDetails>"));
					   
					   rowVal = responseXML.substring(responseXML.indexOf("<AlertDetails>")+"</AlertDetails>".length()-1,responseXML.indexOf("</AlertDetails>"));
						//docdetails.put(eElement.getElementsByTagName("AlertDetails").item(0).getTextContent(), rowvalues);
						//System.out.println("values = "+rowvalues);
					   
					   String StatusBehavior = "";
					   String StatusName = "";
					   if(responseXML.contains("<StatusBehavior>"))
						   StatusBehavior = responseXML.substring(responseXML.indexOf("<StatusBehavior>")+"</StatusBehavior>".length()-1,responseXML.indexOf("</StatusBehavior>"));
					   
					   if(responseXML.contains("<StatusName>"))
						   StatusName = responseXML.substring(responseXML.indexOf("<StatusName>")+"</StatusName>".length()-1,responseXML.indexOf("</StatusName>"));
					   
					   String[] arrOfStr1 = null; 
					   if (rowVal.contains("Suspect detected #1"))
					   {						   	
							arrOfStr1 = rowVal.split("=============================");
							if(arrOfStr1.length==2)
							{
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("No Records Found : ");
								//objRespBean.setFircosoft_Details("Record Not Found"); 
								if(MainCIF_Flag)
							    {
						   			FircoGridREFERENCENO.add(ReferenceNo);
									DocName = "Fircosoft_ForCompany";
									returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
									FircoGridREFERENCENO.clear();
							    }
							    else
							    {
							    	FircoGridREFERENCENO.add(ReferenceNo);
							    	DocName = "Fircosoft_ForSignatories";
									returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
									FircoGridREFERENCENO.clear();
							    }
								return "Record Not Found";
							}
							else if(arrOfStr1.length>2)
							{
								try {
									int FIRCOSOFTGridsize = 0;
							    	String colNames = "";
							    	String colValues = "";
									
		                            for(int i=1;i<arrOfStr1.length-1;i++)
									{
		                            	String sRecords=arrOfStr1[i].replace(": \n", ":"); 
		                            	sRecords=sRecords.replace(":\n", ":");
		                            	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Firco sRecords: "+sRecords);
										Map<String,String> Columnvalues = new HashMap<String,String>(); 
										BufferedReader bufReader = new BufferedReader(new StringReader(sRecords));
										String line=null;
										while( (line=bufReader.readLine()) != null )
										{
											String[] PDFColumns = {"OFAC ID", "NAME", "MATCHINGTEXT", "ORIGIN", "DESIGNATION", "DATE OF BIRTH", "USER DATA 1", "NATIONALITY", "PASSPORT", "ADDITIONAL INFOS"};
											for(int k=0;k<PDFColumns.length;k++)
											{
												if(line.contains(PDFColumns[k]+":"))
												{
													String colData = "";
													String [] tmp = line.split(":");
													//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tmp.length : "+tmp.length+", line : "+line);
												
													//********below loop added for handling hardcoded Fircosoft XML in offshore dev server
													if(tmp.length == 1)
														colData="";//***************************
													else if(tmp[1].trim().equalsIgnoreCase("Synonyms") || tmp[1].trim().equalsIgnoreCase("none") || tmp[1].trim().equalsIgnoreCase(""))
														colData="";
													else
													{
														//colData=tmp[1].trim();
														for(int m=1; m<tmp.length; m++)
														{
															colData=colData+" "+tmp[m].trim();
														}
													}
													
													/*if("DATE OF BIRTH".equalsIgnoreCase(PDFColumns[k].trim()))
													{
														colData=colData.trim();
														try
														{
															if(colData.length()==4) // when received only one year like 1975
																colData="01-01-"+colData; 
															else if(colData.contains(" ") && colData.substring(0, colData.indexOf(" ")).length() == 4) // when received multiple year like 1975 1976
															{
																colData="01-01-"+colData.substring(0, colData.indexOf(" "));
															}
															else if(colData.length() == 10 && !colData.contains(" ")) // when only one valid date is received like 01-01-1975
																colData = colData;
															else if(colData.length()>10) // when multiple valid date is received like 01-01-1975 01-01-1976
																colData = colData.substring(0,10);
															else 
																colData = "";
															
															if(colData.contains(" "))
																colData = "";
														}
														catch (Exception e)
														{
															iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("excep in parsing date:"+colData);
															colData = "";
														}
													}*/	
													
													Columnvalues.put(PDFColumns[k],colData);
													iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ColName: "+PDFColumns[k]+", ColData: "+colData);
												}
											}
										}									
										
										iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Firco Response pasrsing done successfully");
													
										HashMap<String,String> obj1= new HashMap<String,String>();
										//JSONObject obj1=new JSONObject();
										
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", ReferenceNo :"+ReferenceNo);
										obj1.put("WI_NAME",processInstanceID);
										if(MainCIF_Flag)
										{
											obj1.put("RELATEDPARTYID","");
											Details_For="Company";
										}
										else
										{
											obj1.put("RELATEDPARTYID", RelPartyGridDataMap.get("RELATEDPARTYID"));											
											Details_For=RelPartyGridDataMap.get("RELATEDPARTYID");
										}
										
										FIRCOSOFTGridsize=FIRCOSOFTGridsize+1;
										obj1.put("SRNumber", String.valueOf(FIRCOSOFTGridsize));

										obj1.put("DETAILS_FOR", Details_For);
										obj1.put("OFAC_ID", Columnvalues.get("OFAC ID").toString().trim());
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("OFAC ID : "+Columnvalues.get("OFAC ID"));
										
										obj1.put("NAME", Columnvalues.get("NAME"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("NAME : "+Columnvalues.get("NAME"));
										
										obj1.put("MATCHINGTEXT", Columnvalues.get("MATCHINGTEXT"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("MATCHINGTEXT : "+Columnvalues.get("MATCHINGTEXT"));
										
										obj1.put("ORIGIN", Columnvalues.get("ORIGIN"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ORIGIN : "+Columnvalues.get("ORIGIN"));
										
										obj1.put("DESIGNATION", Columnvalues.get("DESIGNATION"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("DESIGNATION : "+Columnvalues.get("DESIGNATION"));
										
										obj1.put("DATEOFBIRTHTEXT", Columnvalues.get("DATE OF BIRTH"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("DATE OF BIRTH : "+Columnvalues.get("DATE OF BIRTH"));
										
										obj1.put("USERDATA1", Columnvalues.get("USER DATA 1"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("User Data 1 : "+Columnvalues.get("USER DATA 1"));
										
										obj1.put("NATIONALITY", Columnvalues.get("NATIONALITY"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("NATIONALITY : "+NATIONALITY);
										
										obj1.put("PASSPORT", Columnvalues.get("PASSPORT"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("PASSPORT : "+Columnvalues.get("PASSPORT"));
										
										obj1.put("ADDITIONALINFO", Columnvalues.get("ADDITIONAL INFOS"));
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ADDITIONAL : "+Columnvalues.get("ADDITIONAL INFOS"));
										
										obj1.put("MATCH_STATUS", "");
									//	obj1.put("USER_", PreviousPassportNo);
									//	obj1.put("REMARKS", Remarks);
										obj1.put("REFERENCE_NO", ReferenceNo);
										
										
										FircoGridSRNo.add(Integer.toString(i));
										FircoGridOFACID.add(Columnvalues.get("OFAC ID"));
										FircoGridName.add(Columnvalues.get("NAME"));
										FircoGridMatchingText.add(Columnvalues.get("MATCHINGTEXT"));
										FircoGridOrigin.add(Columnvalues.get("ORIGIN"));
										FircoGridDestination.add(Columnvalues.get("DESIGNATION"));
										FircoGridDOB.add(Columnvalues.get("DATE OF BIRTH"));
										FircoGridUserData1.add(Columnvalues.get("User Data 1"));
										FircoGridNationality.add(Columnvalues.get("NATIONALITY"));
										FircoGridPassport.add(Columnvalues.get("PASSPORT"));
										FircoGridAdditionalInfo.add(Columnvalues.get("ADDITIONAL INFOS"));
										FircoGridREFERENCENO.add(ReferenceNo);
										
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("before FircoSoft for loop");
										
										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
										//Date date = new Date();
										
										//Appending Firco Output values to Firco DB Columns
										for(Map.Entry<String,String> map : obj1.entrySet())
										{
											iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("iterating ... map.getKey() :"+map.getKey().toString()+", map.getValue() : "+map.getValue().toString());
											
											if(colNames.equals("") && !map.getValue().toString().equals(""))
									    	{
												//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("if ... map.getValue() : "+map.getValue().toString());
												
												colNames= map.getKey();
												
												if(map.getValue().toString().indexOf(",") != -1)
									    			colValues=map.getValue().replace(",", " ");
									    		else
													colValues=map.getValue();
									    	}
									    	else if(!colNames.equals("") && !map.getValue().toString().equals(""))
									    	{
									    		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("else ... colNames : "+colNames);
									    		colNames= colNames+","+map.getKey();
									    		
									    		if(map.getValue().toString().indexOf(",") != -1)
									    			colValues=colValues+","+map.getValue().replace(",", " ");
									    		else
													colValues=colValues+","+map.getValue();
									    	}
										}
										//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Fircosoft colNames : "+colNames+" colValues : "+colValues);
										
										colValues=colValues.replaceAll(",", "','");
									    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Aftr replace colValues :"+colValues);
									    
										String sWhere="";
									    if(MainCIF_Flag)
									    	sWhere="WI_NAME='"+processInstanceID+"' AND OFAC_ID='"+obj1.get("OFAC_ID")+"'";
									    else
									    	sWhere="WI_NAME='"+processInstanceID+"' AND OFAC_ID='"+obj1.get("OFAC_ID")+"' AND RELATEDPARTYID='"+RelPartyGridDataMap.get("RELATEDPARTYID")+"'";
									    
										
										String status= iRBLIntegration.UpdateGridTableMWResponse(colNames,"'"+colValues+"'",FircosoftTable,sWhere);
									    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("UpdateGridTable status : "+status+" for "+FircosoftTable);
									    colNames="";
									    colValues="";
									    
									    //Inserting the Fircosoft Response in Firco table which is used by FIRCO System
									    if (i==1) // in ng rlos firco table only one row will insert for one request.
									    {
										    String FircoTable="NG_RLOS_FIRCO";
										    String Call_Type="Primary";									    
										    if(!MainCIF_Flag)
										       	Call_Type="Secondary";
										   									    
										    Calendar cal = Calendar.getInstance();
										    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			   
										    String CurrDateTime = sdf.format(cal.getTime());
										    
										    
										    columnnames="Process_name, Workitem_no, Firco_ID, StatusBehavior,StatusName, Request_datatime, Workstep_name, Newgen_status, AlertDetails, passport, Call_type, call_valid";
										    columnvalues="'IRBL','"+processInstanceID+"','"+ReferenceNo+"','"+StatusBehavior+"','"+StatusName+"','"+CurrDateTime+"','"+ws_name+"','Pending','"+AlertDetailsTagResponse+"','"+PASSPORT_NUMBER+"','"+Call_Type+"','Y'";
											InputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnnames, columnvalues, FircoTable);
											iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert "+FircoTable+" Table : "+InputXML);
	
											OutputXML=iRBLSysCheckIntegration.WFNGExecute(InputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
											iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apInsert "+FircoTable+" Table : "+OutputXML);
	
											sXMLParserChild= new XMLParser(OutputXML);
										    StrMainCode = sXMLParserChild.getValueOf("MainCode");
										    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("StrMainCode: "+StrMainCode);
	
										    if (StrMainCode.equals("0"))
											   	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+FircoTable);	
										    else
										       	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+OutputXML);
										}									    
										
										iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", @@@@@@@@@@ for fircosoft detail call :::");
										
									}	
								}catch(Exception e)
								{
									iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Exception in parsing firco response: "+return_code);
									//objRespBean.setFircosoft_Details("Not Checked");									
									e.printStackTrace();
									return "Not Checked";	
								}
							    											
								iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", @@@@@@@@@@ : after add of fircosoft details 2");
									
								//MQ_response = MQ_response.substring(0,MQ_response.indexOf("<AlertDetails>"))+ MQ_response.substring(MQ_response.indexOf("</AlertDetails>")+"</AlertDetails>".length());
								//setControlValue("Fircosoft_Details","Records Found");
								//objRespBean.setFircosoft_Details("Record Found");
								//***********************************************
								if(MainCIF_Flag)
							    {
									DocName = "Fircosoft_ForCompany";
									returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
								
							    }
							    else
							    {
							    	DocName = "Fircosoft_ForSignatories";
									returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
							    }
							    //*****************************
								
								//Clearing data in Arraylist
								FircoGridSRNo.clear();
								FircoGridOFACID.clear();
								FircoGridName.clear();
								FircoGridMatchingText.clear();
								FircoGridOrigin.clear();
								FircoGridDestination.clear();
								FircoGridDOB.clear();
								FircoGridUserData1.clear();
								FircoGridNationality.clear();
								FircoGridPassport.clear();
								FircoGridAdditionalInfo.clear();
								FircoGridREFERENCENO.clear();
								
							    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Response of attach doc in dedupe call"+returnValue);
								
								return "Record Found";
								//******************************
							}
				   	} else {
				   		iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("No Records Found : ");
				   		//objRespBean.setFircosoft_Details("Record Not Found");
				   		// attaching blank pdf when no record found.
				   		if(MainCIF_Flag)
					    {
				   			FircoGridREFERENCENO.add(ReferenceNo);
							DocName = "Fircosoft_ForCompany";
							returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
							FircoGridREFERENCENO.clear();
					    }
					    else
					    {
					    	FircoGridREFERENCENO.add(ReferenceNo);
					    	DocName = "Fircosoft_ForSignatories";
							returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
							FircoGridREFERENCENO.clear();
					    }
				   		//**************
				   		return "Record Not Found";
				   	}
				}
				else
				{
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", not getting Alert Details tag in Response of Fircosoft call"+return_code);
					//objRespBean.setFircosoft_Details("Record Not Found");
					// attaching blank pdf when no record found.
			   		if(MainCIF_Flag)
				    {
			   			FircoGridREFERENCENO.add(ReferenceNo);
						DocName = "Fircosoft_ForCompany";
						returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
						FircoGridREFERENCENO.clear();
				    }
				    else
				    {
				    	FircoGridREFERENCENO.add(ReferenceNo);
				    	DocName = "Fircosoft_ForSignatories";
						returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
						FircoGridREFERENCENO.clear();
				    }
			   		//**************
					return "Record Not Found";
				}
				// attaching blank pdf when no record found.
		   		if(MainCIF_Flag)
			    {
		   			FircoGridREFERENCENO.add(ReferenceNo);
					DocName = "Fircosoft_ForCompany";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,ExtTabDataMap);
					FircoGridREFERENCENO.clear();
			    }
			    else
			    {
			    	FircoGridREFERENCENO.add(ReferenceNo);
			    	DocName = "Fircosoft_ForSignatories";
					returnValue = GeneratePDF.PDFTemplate(MainCIF_Flag,processInstanceID,ws_name,MsgFormat,DocName,RelPartyGridDataMap);
					FircoGridREFERENCENO.clear();
			    }
		   		//**************
				return "Record Not Found";
			}
			else
			{
				//setControlValue("MAIN_CIF_SEARCH","N");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Error in Response of Fircosoft call"+return_code);
				//objRespBean.setFircosoft_Details("Not Checked");
				return "Not Checked";
			}
		}
		catch(Exception e)
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception in FircosoftCall exception:"+e.getMessage()+", print:"+ CommonMethods.printException(e));
			return "Not Checked";
		}
	}
	
	public static	String RiskScoreCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> CheckGridDataMap)
	{	
		try
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("inside RiskScoreCall Fn");
			String DBQuery="";
			String extTabDataIPXML="";
			String extTabDataOPXML="";
			int iTotalrec=0;
			String ReferenceCifId = "";
			if(!ExtTabDataMap.get("CIF_NUMBER").equalsIgnoreCase(""))
			{
				ReferenceCifId = "<RequestInfo>\n" +
					"<RequestType>CIF Id</RequestType>\n" +
					"<RequestValue>"+ExtTabDataMap.get("CIF_NUMBER")+"</RequestValue>\n" +
				"</RequestInfo>\n";
			}
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ReferenceCifId : "+ReferenceCifId);
			
			String ProductsInfoXml = "";
			String ProductType = ExtTabDataMap.get("PRODUCTTYPE").trim();
			String ProductCurrency = "";
			if(!ProductType.equalsIgnoreCase(""))
			{
				/*List<List<String>> cctgry = iformObj.getDataFromDB("SELECT top 1 Product_Type_Display, Product_Currency FROM USR_0_RAOP_PRODUCT_TYPE WITH(NOLOCK) WHERE Product_Code = '"+ProductType+"' AND ISACTIVE='Y'");

				for (List<String> row : cctgry) {
					if (!row.get(0).equalsIgnoreCase(""))
					{
						ProductType = row.get(0);
						ProductCurrency = row.get(1);
					}
				}*/
				
				DBQuery = "SELECT top 1 Product_Type_Display, Product_Currency FROM "+IRBL_PRODUCT_TYPE+" WITH(NOLOCK) WHERE Product_Code = '"+ProductType+"' AND ISACTIVE='Y'";

				extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_PRODUCT_TYPE IPXML: "+ extTabDataIPXML);
				extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_PRODUCT_TYPE OPXML: "+ extTabDataOPXML);

				XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
				iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				
				if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
				{
					NGXmlList objWorkList=xmlParserData.createList("Records", "Record");										
					for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
					{	
						ProductType = objWorkList.getVal("Product_Type_Display");
						ProductCurrency = objWorkList.getVal("Product_Currency");
					}
				}
							
				ProductsInfoXml = ProductsInfoXml + "<ProductsInfo>\n" +
					"<Product>"+ProductType+"</Product>\n" +
					"<Currency>"+ProductCurrency+"</Currency>\n" +
				"</ProductsInfo>" ;
			}
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ProductsInfoXml : "+ProductsInfoXml);
			
			/*String FirstName=ExtTabDataMap.get("FIRST_NAME");
			String MidName=ExtTabDataMap.get("MIDDLE_NAME");
			String LstName=ExtTabDataMap.get("LAST_NAME");*/
			String CustomerName=ExtTabDataMap.get("COMPANY_NAME");
			
			/*if (MidName.equalsIgnoreCase(""))
				CustomerName = FirstName + ' ' + LstName;
			else	
				CustomerName = FirstName + ' ' + MidName + ' ' + LstName;*/
			 
			
			String Demographic=ExtTabDataMap.get("ENTRY_NATIONALITY").trim();			
			String DemographicXml = "";
			if (!Demographic.equalsIgnoreCase(""))
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Demographic value: "+Demographic);
				if (Demographic.contains("|"))
					Demographic = Demographic.replace("|","','");
				
				/*List<List<String>> demogphc = iformObj.getDataFromDB("SELECT countryName FROM USR_0_BPM_COUNTRY_MASTER WITH(NOLOCK) WHERE countryCode in ('"+Demographic+"') AND ISACTIVE='Y'");
				for (List<String> row : demogphc) {
					if (!row.get(0).equalsIgnoreCase(""))
						DemographicXml = DemographicXml + "<Demographic>"+row.get(0)+"</Demographic>\n";
				}*/
				
				DBQuery = "SELECT countryName FROM "+IRBL_COUNTRY_MASTER+" WITH(NOLOCK) WHERE countryCode in ('"+Demographic+"') AND ISACTIVE='Y'";

				extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_COUNTRY_MASTER IPXML: "+ extTabDataIPXML);
				extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_COUNTRY_MASTER OPXML: "+ extTabDataOPXML);

				XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
				iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				
				if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
				{
					NGXmlList objWorkList=xmlParserData.createList("Records", "Record");										
					for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
					{
						DemographicXml = DemographicXml + "<Demographic>"+objWorkList.getVal("countryName")+"</Demographic>\n";
					}
				}
			}
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("DemographicXml : "+DemographicXml);
			
			String Nationality = ExtTabDataMap.get("ENTRY_NATIONALITY");
			String NationalityXml = "";
			if(!Nationality.equalsIgnoreCase(""))
			{
				/*List<List<String>> natnly = iformObj.getDataFromDB("SELECT top 1 countryName FROM USR_0_BPM_COUNTRY_MASTER WITH(NOLOCK) WHERE countryCode = '"+Nationality+"' AND ISACTIVE='Y'");
				for (List<String> row : natnly) {
					if (!row.get(0).equalsIgnoreCase(""))
						NationalityXml = "<Nationality>"+row.get(0)+"</Nationality>\n";
				}*/
				DBQuery = "SELECT top 1 countryName FROM "+IRBL_COUNTRY_MASTER+" WITH(NOLOCK) WHERE countryCode = '"+Nationality+"' AND ISACTIVE='Y'";

				extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_COUNTRY_MASTER IPXML: "+ extTabDataIPXML);
				extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_COUNTRY_MASTER aOPXML: "+ extTabDataOPXML);

				XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
				iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				
				if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
				{
					NGXmlList objWorkList=xmlParserData.createList("Records", "Record");										
					for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
					{	
						NationalityXml = "<Nationality>"+objWorkList.getVal("countryName")+"</Nationality>\n";
					}
				}			
			}
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("NationalityXml : "+NationalityXml);
			
			
			String CustCategory="";
			/*String CustCategory = ExtTabDataMap.get("CUSTOMER_CATEGORY").trim();
			if(!CustCategory.equalsIgnoreCase(""))
			{
				List<List<String>> cctgry = iformObj.getDataFromDB("SELECT top 1 CUSTCATEGORY FROM USR_0_BPM_CUSTOMER_CATEGORY WITH(NOLOCK) WHERE CUSTCODE = '"+CustCategory+"' AND ISACTIVE='Y'");
				for (List<String> row : cctgry) {
					if (!row.get(0).equalsIgnoreCase(""))
						CustCategory = row.get(0);
				}
			}*/
			
			String CustSegment="";
			/*String CustSegment = ExtTabDataMap.get("CUSTOMER_SEGMENT").trim();
			if(!CustSegment.equalsIgnoreCase(""))
			{
				List<List<String>> cseg = iformObj.getDataFromDB("SELECT top 1 SEGMENT_DESCRIPTION FROM USR_0_RAOP_CUSTOMER_SEGMENT WITH(NOLOCK) WHERE SEGMENT_CODE = '"+CustSegment+"' AND ISACTIVE='Y'");
				for (List<String> row : cseg) {
					if (!row.get(0).equalsIgnoreCase(""))
						CustSegment = row.get(0);
				}
			}*/
			
			String CustSubSegment="";
			/*String CustSubSegment = ExtTabDataMap.get("CUSTOMER_SUBSEGMENT").trim();
			if(!CustSubSegment.equalsIgnoreCase(""))
			{
				List<List<String>> csubseg = iformObj.getDataFromDB("SELECT top 1 SUBSEGMENT_DESCRIPTION FROM USR_0_RAOP_CUSTOMER_SUBSEGMENT WITH(NOLOCK) WHERE SUBSEGMENT_CODE = '"+CustSubSegment+"' AND ISACTIVE='Y'");
				for (List<String> row : csubseg) {
					if (!row.get(0).equalsIgnoreCase(""))
						CustSubSegment = row.get(0);
				}
			}*/
			
			String IndSubSegment = ExtTabDataMap.get("INDUSTRY_CODE").trim();
			if(!IndSubSegment.equalsIgnoreCase(""))
			{
				/*List<List<String>> indsubseg = iformObj.getDataFromDB("SELECT top 1 INDUSTRYSUBSEGTYPE FROM USR_0_BPM_INDUSTRY_SUBSEGMENT WITH(NOLOCK) WHERE INDSUBSEGCODE = '"+IndSubSegment+"' AND ISACTIVE='Y'");
				for (List<String> row : indsubseg) {
					if (!row.get(0).equalsIgnoreCase(""))
						IndSubSegment = row.get(0);
				}*/
				
				DBQuery = "SELECT top 1 INDUSTRYSUBSEGTYPE FROM "+IRBL_INDUSTRY_SUBSEGMENT+" WITH(NOLOCK) WHERE INDSUBSEGCODE = '"+IndSubSegment+"' AND ISACTIVE='Y'";

				extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_INDUSTRY_SUBSEGMENT IPXML: "+ extTabDataIPXML);
				extTabDataOPXML = iRBLSysCheckIntegration.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" IRBL_INDUSTRY_SUBSEGMENT OPXML: "+ extTabDataOPXML);

				XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
				iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				
				if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
				{
					NGXmlList objWorkList=xmlParserData.createList("Records", "Record");										
					for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
					{	
						IndSubSegment = objWorkList.getVal("INDUSTRYSUBSEGTYPE");
					}
				}

			}

			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("IndSubSegment : "+IndSubSegment);
			
			String employmentType = ""; 
			 /*String employmentType = ExtTabDataMap.get("EMPLOYMENT_TYPE").trim();
			if(!employmentType.equalsIgnoreCase(""))
			{
				List<List<String>> emptype = iformObj.getDataFromDB("SELECT top 1 EmpTypeDisplay FROM USR_0_BPM_EMPLOYMENT_TYPE WITH(NOLOCK) WHERE EmpCode = '"+employmentType+"' AND ISACTIVE='Y'");
				for (List<String> row : emptype) {
					if (!row.get(0).equalsIgnoreCase(""))
						employmentType = row.get(0);
				}
			}*/
			
			String isPEP = ExtTabDataMap.get("PEP_STATUS").trim();
			if (isPEP.equalsIgnoreCase("NPEP") || isPEP.equalsIgnoreCase(""))
				isPEP = "N";
			else
				isPEP = "Y";
			
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("isPEP : "+isPEP);

			
			StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>\n" +
				"<EE_EAI_HEADER>\n" +
				"<MsgFormat>RISK_SCORE_DETAILS</MsgFormat>\n" +
				"<MsgVersion>0001</MsgVersion>\n" +
				"<RequestorChannelId>BPM</RequestorChannelId>\n" +
				"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
				"<RequestorLanguage>E</RequestorLanguage>\n" +
				"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
				"<ReturnCode>0000</ReturnCode>\n" +
				"<ReturnDesc>REQ</ReturnDesc>\n" +
				"<MessageId>BPM_MARY_02</MessageId>\n" +
				"<Extra1>REQ||BPM.123</Extra1>\n" +
				"<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>\n" +
				"</EE_EAI_HEADER>\n"+
					"<RiskScoreDetailsRequest>\n" + 
						ReferenceCifId +
						"<RequestInfo>\n" +
							"<RequestType>Reference Id</RequestType>\n" +
							"<RequestValue>"+processInstanceID+"</RequestValue>\n" +
						"</RequestInfo>\n" +
						"<CustomerType>Individual</CustomerType>\n" + 
						"<CustomerCategory>"+CustCategory+"</CustomerCategory>\n" + 
						"<IsPoliticallyExposed>"+isPEP+"</IsPoliticallyExposed>\n" + 
						"<CustomerName>"+CustomerName+"</CustomerName>\n" + 
						"<EmploymentType>"+employmentType+"</EmploymentType>\n" +  // Passing Salaried default on confirmation of Natesh
						"<Segment>"+CustSegment+"</Segment> \n" +
						"<SubSegment>"+CustSubSegment+"</SubSegment> \n" +
						"<Demographics>\n" +
							DemographicXml +
						"</Demographics>\n" +
						"<Nationalities>\n" +
							NationalityXml +
						"</Nationalities>\n" +
						"<Industries>\n" +
							"<Industry>"+IndSubSegment+"</Industry>\n" + 
						"</Industries>\n" +
						ProductsInfoXml + "\n" +
					"</RiskScoreDetailsRequest>\n"+
					"</EE_EAI_MESSAGE>");
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RiskScore input XML: "+sInputXML);

			String responseXML = iRBLIntegration.socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);
	
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);
	
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
		    if(return_code.equals("0000"))
			{
				String MainTotalRiskScore = "";
				
				MainTotalRiskScore = (responseXML.contains("<TotalRiskScore>")) ? responseXML.substring(responseXML.indexOf("<TotalRiskScore>")+"</TotalRiskScore>".length()-1,responseXML.indexOf("</TotalRiskScore>")):"";							
				//rowVal = MQ_response.substring(MQ_response.indexOf("<Customer>"),MQ_response.indexOf("</Customer>")+"</Customer>".length());
											
				//MainCustomerName = (rowVal.contains("<FullName>")) ? rowVal.substring(rowVal.indexOf("<FullName>")+"</FullName>".length()-1,rowVal.indexOf("</FullName>")):"";
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", MainTotalRiskScore--"+MainTotalRiskScore);
				objRespBean.setRiskScore_Details(MainTotalRiskScore);		
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", @@@@@@@@@@ : Successful in getting RiskScore details");	
				/*String PdfName="Risk_Score_Details";
				String Status=createPDF(iformObj,"Risk_Score",WINAME,PdfName);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", @@@@@@@@@@ : Status : "+Status);
				String Response="";
				if(!Status.contains("Error"))
				{
					Response=AttachDocumentWithWI(iformObj,WINAME,PdfName);
					return return_code+"~"+ReturnDesc+"~"+Response;
				}
				else
				{
					return Response=return_code+"~"+Status;
				}*/
				//return ReturnCode1+"~"+ReturnDesc+"~"+Response;
				//CheckGridDataMap.put("RISKSCORE_STATUS", "Success");
				return "Success";
			}						
			else
			{
				//setControlValue("MAIN_CIF_SEARCH","N");
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Error in Response of RiskScore call"+return_code+"!"+return_desc);
				//return return_code+"~"+return_desc;	
				//CheckGridDataMap.put("RISKSCORE_STATUS", "Failure");
				return "Failure";
			}	
		
		    
		    //return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		}
		catch(Exception e)
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception in RiskScore Call exception:"+e.getMessage()+", print:"+ CommonMethods.printException(e));
			return "Failure";
		}
	}
	
	public static String getFircoReferenceNumber(String workitemno)
	{
		if(!workitemno.equalsIgnoreCase(""))
		{
			workitemno = workitemno.split("-")[0]+"-"+workitemno.split("-")[1].replaceFirst("^0+(?!$)", "");
		}
		Timestamp localTimestamp = new Timestamp(System.currentTimeMillis());
		String date = Integer.toString(localTimestamp.getDate());
		if(date.length() == 1)
			date = "0"+date;
		
		int iMonth =localTimestamp.getMonth()+1;
		String month = Integer.toString(iMonth);
		if(month.length() == 1)
			month = "0"+month;
		
		int iYear = localTimestamp.getYear()+1900;
		String year = Integer.toString(iYear);

		String hour = Integer.toString(localTimestamp.getHours());
		if(hour.length() == 1)
			hour = "0"+hour;
		
		String minutes = Integer.toString(localTimestamp.getMinutes());
		if(minutes.length() == 1)
			minutes = "0"+minutes;
		
		String second = Integer.toString(localTimestamp.getSeconds());
		if(second.length() == 1)
			second = "0"+second;
		//String ReferenceNo=workitemno+"_"+System.currentTimeMillis()/1000*60;
		String ReferenceNo=workitemno+"-"+ date+month+year+hour+minutes+second;
		return ReferenceNo;	
	}
	
	/*public	String CBRBCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> RelPartyGridDataMap, boolean MainCIF_Flag)
	{
		try
		{
			String mqInputRequest = null;
			String DocDetXml="";
			
			if(!(ExtTabDataMap.get("EMIRATES_ID").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>EMID</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("EMIRATES_ID")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("PASSPORT_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>PPT</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("PASSPORT_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("VIS_UID_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>VISA</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("VIS_UID_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			String DOB = ExtTabDataMap.get("DOB");
			if(!DOB.trim().equalsIgnoreCase(""))
			{
				String tempDOB [] = DOB.split("/");
				DOB = tempDOB[2]+"-"+tempDOB[1]+"-"+tempDOB[0];
			}
			
			String MidName = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				MidName="<MiddleName>"+ExtTabDataMap.get("MIDDLE_NAME")+"</MiddleName>";
			
			String FullName = "<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				FullName="<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("MIDDLE_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			
			String MobileNumberDetails = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_COUNTRY_CODE")) && !"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_NO")))
			{
				String MobileNumber = "+"+ExtTabDataMap.get("MOBILE_COUNTRY_CODE")+"()"+ExtTabDataMap.get("MOBILE_NO");
				MobileNumberDetails = "<ContactDetails>\n"+
						"<PhoneFax>\n"+
							"<PhoneType>Phone</PhoneType>\n"+
							"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
						"</PhoneFax>\n"+
					"</ContactDetails>";
			}
			
			StringBuilder sInputXML = new StringBuilder("<?xml version=\"1.0\"?>\n"
					+ "<BPM_APMQPutGetMessage_Input>\n"+
					"<Option>BPM_APMQPutGetMessageDirect</Option>\n"+
					"<UserID>"+UserName+"</UserID>\n" +
					"<SessionId>"+sessionId+"</SessionId>\n"+
					"<EngineName>"+cabinetName+"</EngineName>\n" +
					"<RequestMessage><EE_EAI_MESSAGE>\n" +
					//"<ProcessName>RAO</ProcessName>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>DEDUP_SUMMARY</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>BPM_MARY_02</MessageId>\n" +
					"<Extra1>REQ||BPM.123</Extra1>\n" +
					"<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>\n" +
					"</EE_EAI_HEADER>\n"+
					"<CustomerDuplicationListRequest>" +
						"<BankId>RAK</BankId><CIFID></CIFID><CustomerType>R</CustomerType>" +
						"<RetailCorpFlag>R</RetailCorpFlag><EntityType>All</EntityType>" +
						"<PersonDetails><FirstName>"+ExtTabDataMap.get("custfirstname")+"</FirstName>"+MidName+"" +
						"<LastName>"+ExtTabDataMap.get("custlastname")+"</LastName>"+FullName+"" +
						"<MaritalStatus>"+ExtTabDataMap.get("custmaritalstatus")+"</MaritalStatus>" +
						"<Nationality>"+ExtTabDataMap.get("custnationality")+"</Nationality>" +
						"<DateOfBirth>"+ExtTabDataMap.get("custdob")+"</DateOfBirth>" +
						"</PersonDetails>\n"+MobileNumberDetails+DocDetXml+"</CustomerDuplicationListRequest>\n"+
					"</EE_EAI_MESSAGE></RequestMessage>\n" +				
					"</BPM_APMQPutGetMessage_Input>");
						
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
			
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public	String SVSCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap)
	{
		try
		{
			String mqInputRequest = null;
			String DocDetXml="";
			
			if(!(ExtTabDataMap.get("EMIRATES_ID").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>EMID</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("EMIRATES_ID")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("PASSPORT_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>PPT</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("PASSPORT_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("VIS_UID_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>VISA</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("VIS_UID_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			String DOB = ExtTabDataMap.get("DOB");
			if(!DOB.trim().equalsIgnoreCase(""))
			{
				String tempDOB [] = DOB.split("/");
				DOB = tempDOB[2]+"-"+tempDOB[1]+"-"+tempDOB[0];
			}
			
			String MidName = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				MidName="<MiddleName>"+ExtTabDataMap.get("MIDDLE_NAME")+"</MiddleName>";
			
			String FullName = "<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				FullName="<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("MIDDLE_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			
			String MobileNumberDetails = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_COUNTRY_CODE")) && !"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_NO")))
			{
				String MobileNumber = "+"+ExtTabDataMap.get("MOBILE_COUNTRY_CODE")+"()"+ExtTabDataMap.get("MOBILE_NO");
				MobileNumberDetails = "<ContactDetails>\n"+
						"<PhoneFax>\n"+
							"<PhoneType>Phone</PhoneType>\n"+
							"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
						"</PhoneFax>\n"+
					"</ContactDetails>";
			}
			
			StringBuilder sInputXML = new StringBuilder("<?xml version=\"1.0\"?>\n"
					+ "<BPM_APMQPutGetMessage_Input>\n"+
					"<Option>BPM_APMQPutGetMessageDirect</Option>\n"+
					"<UserID>"+UserName+"</UserID>\n" +
					"<SessionId>"+sessionId+"</SessionId>\n"+
					"<EngineName>"+cabinetName+"</EngineName>\n" +
					"<RequestMessage><EE_EAI_MESSAGE>\n" +
					//"<ProcessName>RAO</ProcessName>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>DEDUP_SUMMARY</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>BPM_MARY_02</MessageId>\n" +
					"<Extra1>REQ||BPM.123</Extra1>\n" +
					"<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>\n" +
					"</EE_EAI_HEADER>\n"+
					"<CustomerDuplicationListRequest>" +
						"<BankId>RAK</BankId><CIFID></CIFID><CustomerType>R</CustomerType>" +
						"<RetailCorpFlag>R</RetailCorpFlag><EntityType>All</EntityType>" +
						"<PersonDetails><FirstName>"+ExtTabDataMap.get("custfirstname")+"</FirstName>"+MidName+"" +
						"<LastName>"+ExtTabDataMap.get("custlastname")+"</LastName>"+FullName+"" +
						"<MaritalStatus>"+ExtTabDataMap.get("custmaritalstatus")+"</MaritalStatus>" +
						"<Nationality>"+ExtTabDataMap.get("custnationality")+"</Nationality>" +
						"<DateOfBirth>"+ExtTabDataMap.get("custdob")+"</DateOfBirth>" +
						"</PersonDetails>\n"+MobileNumberDetails+DocDetXml+"</CustomerDuplicationListRequest>\n"+
					"</EE_EAI_MESSAGE></RequestMessage>\n" +				
					"</BPM_APMQPutGetMessage_Input>");
			
			//mqInputRequest = getRequestXML(cabinetName, sessionId, processInstanceID, ws_name, UserName, sInputXML);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", $$outputgGridtXML "+"mqInputRequest for Dedupe Check call" + mqInputRequest);
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
			
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public	String IntExposureCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> CheckGridDataMap)
	{
		try
		{
			StringBuilder sInputXML = new StringBuilder("<?xml version=\"1.0\"?>\n"
					+ "<BPM_APMQPutGetMessage_Input>\n"+
					"<Option>BPM_APMQPutGetMessageDirect</Option>\n"+
					"<UserID>"+UserName+"</UserID>\n" +
					"<SessionId>"+sessionId+"</SessionId>\n"+
					"<EngineName>"+cabinetName+"</EngineName>\n" +
					"<RequestMessage><EE_EAI_MESSAGE>\n" +
					//"<ProcessName>RAO</ProcessName>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>CUSTOMER_EXPOSURE</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>CUSTOMER_EXPOSUER_0V27</MessageId>\n" +
					"<Extra1>REQ||SHELL.JOHN</Extra1>\n" +
					"<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>\n" +
					"</EE_EAI_HEADER>\n"+
					"<CustomerExposureRequest>" +
						"<BankId>RAK</BankId><BranchId>RAK123</BranchId><RequestType>InternalExposure</RequestType>" +
						"<CIFId>" +
							"<CIFIdType>Primary</CIFIdType>" +
							"<CIFIdValue>"+ExtTabDataMap.get("CIF_ID")+"</CIFIdValue>" +
						"</CIFId>" +
					"</CustomerExposureRequest>\n"+
					"</EE_EAI_MESSAGE></RequestMessage>\n" +				
					"</BPM_APMQPutGetMessage_Input>");
			
			//mqInputRequest = getRequestXML(cabinetName, sessionId, processInstanceID, ws_name, UserName, sInputXML);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", $$outputgGridtXML "+"mqInputRequest for Dedupe Check call" + mqInputRequest);
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String result_str="";
		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
			
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    
		    if("0000".equalsIgnoreCase(return_code))
			{
		    	String returnType = xmlParserSocketDetails.getValueOf("RequestType");
				if("InternalExposure".equalsIgnoreCase(returnType))
				{
					String appServerType="";
					String prod="";
					String subprod="";
					String cifId="";
					String parentWiName="";
					String cust_type="";
					String CompanyCIF="";
					result_str=IntegrateInternalExposureCollectionsSummary.parseInternalExposure(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,processInstanceID,appServerType,prod,subprod,cifId,parentWiName,cust_type,CompanyCIF);
					CheckGridDataMap.put("InternalExposure_STATUS", "Success");
				}
			}
		    else
		    {
		    	CheckGridDataMap.put("InternalExposure_STATUS", "Failure");
		    	result_str="Failure";
		    }
		    return result_str;
		    //return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public	String ExtExposureCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> CheckGridDataMap)
	{
		try
		{
			String mqInputRequest = null;
			String DocDetXml="";
			
			if(!(ExtTabDataMap.get("EMIRATES_ID").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>EMID</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("EMIRATES_ID")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("PASSPORT_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>PPT</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("PASSPORT_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("VIS_UID_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>VISA</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("VIS_UID_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			String DOB = ExtTabDataMap.get("DOB");
			if(!DOB.trim().equalsIgnoreCase(""))
			{
				String tempDOB [] = DOB.split("/");
				DOB = tempDOB[2]+"-"+tempDOB[1]+"-"+tempDOB[0];
			}
			
			String MidName = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				MidName="<MiddleName>"+ExtTabDataMap.get("MIDDLE_NAME")+"</MiddleName>";
			
			String FullName = "<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				FullName="<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("MIDDLE_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			
			String MobileNumberDetails = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_COUNTRY_CODE")) && !"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_NO")))
			{
				String MobileNumber = "+"+ExtTabDataMap.get("MOBILE_COUNTRY_CODE")+"()"+ExtTabDataMap.get("MOBILE_NO");
				MobileNumberDetails = "<ContactDetails>\n"+
						"<PhoneFax>\n"+
							"<PhoneType>Phone</PhoneType>\n"+
							"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
						"</PhoneFax>\n"+
					"</ContactDetails>";
			}
			
			StringBuilder sInputXML = new StringBuilder("<?xml version=\"1.0\"?>\n"
					+ "<BPM_APMQPutGetMessage_Input>\n"+
					"<Option>BPM_APMQPutGetMessageDirect</Option>\n"+
					"<UserID>"+UserName+"</UserID>\n" +
					"<SessionId>"+sessionId+"</SessionId>\n"+
					"<EngineName>"+cabinetName+"</EngineName>\n" +
					"<RequestMessage><EE_EAI_MESSAGE>\n" +
					//"<ProcessName>RAO</ProcessName>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>DEDUP_SUMMARY</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>BPM_MARY_02</MessageId>\n" +
					"<Extra1>REQ||BPM.123</Extra1>\n" +
					"<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>\n" +
					"</EE_EAI_HEADER>\n"+
					"<CustomerDuplicationListRequest>" +
						"<BankId>RAK</BankId><CIFID></CIFID><CustomerType>R</CustomerType>" +
						"<RetailCorpFlag>R</RetailCorpFlag><EntityType>All</EntityType>" +
						"<PersonDetails><FirstName>"+ExtTabDataMap.get("custfirstname")+"</FirstName>"+MidName+"" +
						"<LastName>"+ExtTabDataMap.get("custlastname")+"</LastName>"+FullName+"" +
						"<MaritalStatus>"+ExtTabDataMap.get("custmaritalstatus")+"</MaritalStatus>" +
						"<Nationality>"+ExtTabDataMap.get("custnationality")+"</Nationality>" +
						"<DateOfBirth>"+ExtTabDataMap.get("custdob")+"</DateOfBirth>" +
						"</PersonDetails>\n"+MobileNumberDetails+DocDetXml+"</CustomerDuplicationListRequest>\n"+
					"</EE_EAI_MESSAGE></RequestMessage>\n" +				
					"</BPM_APMQPutGetMessage_Input>");
			
			//mqInputRequest = getRequestXML(cabinetName, sessionId, processInstanceID, ws_name, UserName, sInputXML);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", $$outputgGridtXML "+"mqInputRequest for Dedupe Check call" + mqInputRequest);
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
			
			String MsgId = "";
			String result_str="";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    String returnType = xmlParserSocketDetails.getValueOf("RequestType");
		    
		    if("0000".equalsIgnoreCase(return_code) || ("ExternalExposure".equalsIgnoreCase(returnType) && ("B003".equalsIgnoreCase(return_code)||"B005".equalsIgnoreCase(return_code))))
			{		    	
				if("ExternalExposure".equalsIgnoreCase(returnType))
				{
					String appServerType="";
					String prod="";
					String subprod="";
					String cifId="";
					String parentWiName="";
					String cust_type="";
					String CompanyCIF="";
					result_str=IntegrateExternalExposure.parseExternalExposure(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,processInstanceID,appServerType,prod,subprod,cifId,parentWiName,cust_type);
					CheckGridDataMap.put("ExtExposure_STATUS", "Success");
				}
			}
		    else
		    {
		    	CheckGridDataMap.put("ExtExposure_STATUS", "Failure");
		    	result_str="Failure";
		    }
		    return result_str;
		    
		    //return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public	String CollectionSummaryCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> CheckGridDataMap)
	{
		try
		{
			String mqInputRequest = null;
			String DocDetXml="";
			
			if(!(ExtTabDataMap.get("EMIRATES_ID").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>EMID</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("EMIRATES_ID")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("PASSPORT_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>PPT</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("PASSPORT_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("VIS_UID_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>VISA</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("VIS_UID_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			String DOB = ExtTabDataMap.get("DOB");
			if(!DOB.trim().equalsIgnoreCase(""))
			{
				String tempDOB [] = DOB.split("/");
				DOB = tempDOB[2]+"-"+tempDOB[1]+"-"+tempDOB[0];
			}
			
			String MidName = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				MidName="<MiddleName>"+ExtTabDataMap.get("MIDDLE_NAME")+"</MiddleName>";
			
			String FullName = "<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				FullName="<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("MIDDLE_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			
			String MobileNumberDetails = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_COUNTRY_CODE")) && !"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_NO")))
			{
				String MobileNumber = "+"+ExtTabDataMap.get("MOBILE_COUNTRY_CODE")+"()"+ExtTabDataMap.get("MOBILE_NO");
				MobileNumberDetails = "<ContactDetails>\n"+
						"<PhoneFax>\n"+
							"<PhoneType>Phone</PhoneType>\n"+
							"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
						"</PhoneFax>\n"+
					"</ContactDetails>";
			}
			
			StringBuilder sInputXML = new StringBuilder("<?xml version=\"1.0\"?>\n"
					+ "<BPM_APMQPutGetMessage_Input>\n"+
					"<Option>BPM_APMQPutGetMessageDirect</Option>\n"+
					"<UserID>"+UserName+"</UserID>\n" +
					"<SessionId>"+sessionId+"</SessionId>\n"+
					"<EngineName>"+cabinetName+"</EngineName>\n" +
					"<RequestMessage><EE_EAI_MESSAGE>\n" +
					//"<ProcessName>RAO</ProcessName>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>DEDUP_SUMMARY</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>BPM_MARY_02</MessageId>\n" +
					"<Extra1>REQ||BPM.123</Extra1>\n" +
					"<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>\n" +
					"</EE_EAI_HEADER>\n"+
					"<CustomerDuplicationListRequest>" +
						"<BankId>RAK</BankId><CIFID></CIFID><CustomerType>R</CustomerType>" +
						"<RetailCorpFlag>R</RetailCorpFlag><EntityType>All</EntityType>" +
						"<PersonDetails><FirstName>"+ExtTabDataMap.get("custfirstname")+"</FirstName>"+MidName+"" +
						"<LastName>"+ExtTabDataMap.get("custlastname")+"</LastName>"+FullName+"" +
						"<MaritalStatus>"+ExtTabDataMap.get("custmaritalstatus")+"</MaritalStatus>" +
						"<Nationality>"+ExtTabDataMap.get("custnationality")+"</Nationality>" +
						"<DateOfBirth>"+ExtTabDataMap.get("custdob")+"</DateOfBirth>" +
						"</PersonDetails>\n"+MobileNumberDetails+DocDetXml+"</CustomerDuplicationListRequest>\n"+
					"</EE_EAI_MESSAGE></RequestMessage>\n" +				
					"</BPM_APMQPutGetMessage_Input>");
			
			//mqInputRequest = getRequestXML(cabinetName, sessionId, processInstanceID, ws_name, UserName, sInputXML);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", $$outputgGridtXML "+"mqInputRequest for Dedupe Check call" + mqInputRequest);
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
			
			String MsgId = "";
			String result_str="";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    
		    if("0000".equalsIgnoreCase(return_code))
			{
		    	String returnType = xmlParserSocketDetails.getValueOf("RequestType");
			    if("CollectionsSummary".equalsIgnoreCase(returnType))
				{
			    	String appServerType="";
					String prod="";
					String subprod="";
					String cifId="";
					String parentWiName="";
					String cust_type="";
					String CompanyCIF="";
					result_str=parseCollectionSummary(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,processInstanceID,appServerType,prod,subprod,cifId,parentWiName,cust_type,CompanyCIF);
					CheckGridDataMap.put("CollectionsSummary_STATUS", "Success");
				}	
			}
		    else
		    {
		    	CheckGridDataMap.put("CollectionsSummary_STATUS", "Failure");
		    	result_str="Failure";
		    }
			
		    //return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		    return result_str;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public	String FinancialSummaryCall(String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> ExtTabDataMap, HashMap<String, String> CheckGridDataMap)
	{
		try
		{
			String mqInputRequest = null;
			String DocDetXml="";
			
			if(!(ExtTabDataMap.get("EMIRATES_ID").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>EMID</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("EMIRATES_ID")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("PASSPORT_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>PPT</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("PASSPORT_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			if(!(ExtTabDataMap.get("VIS_UID_NUMBER").equals("")))
			{
				DocDetXml = DocDetXml+"<Document>\n" +
					"<DocumentType>VISA</DocumentType>\n" +
					"<DocumentRefNumber>"+ExtTabDataMap.get("VIS_UID_NUMBER")+"</DocumentRefNumber>\n" +
				"</Document>";
			}
			String DOB = ExtTabDataMap.get("DOB");
			if(!DOB.trim().equalsIgnoreCase(""))
			{
				String tempDOB [] = DOB.split("/");
				DOB = tempDOB[2]+"-"+tempDOB[1]+"-"+tempDOB[0];
			}
			
			String MidName = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				MidName="<MiddleName>"+ExtTabDataMap.get("MIDDLE_NAME")+"</MiddleName>";
			
			String FullName = "<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MIDDLE_NAME")))
				FullName="<FullName>"+ExtTabDataMap.get("FIRST_NAME")+" "+ExtTabDataMap.get("MIDDLE_NAME")+" "+ExtTabDataMap.get("LAST_NAME")+"</FullName>";
			
			String MobileNumberDetails = "";
			if (!"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_COUNTRY_CODE")) && !"".equalsIgnoreCase(ExtTabDataMap.get("MOBILE_NO")))
			{
				String MobileNumber = "+"+ExtTabDataMap.get("MOBILE_COUNTRY_CODE")+"()"+ExtTabDataMap.get("MOBILE_NO");
				MobileNumberDetails = "<ContactDetails>\n"+
						"<PhoneFax>\n"+
							"<PhoneType>Phone</PhoneType>\n"+
							"<PhoneValue>"+MobileNumber+"</PhoneValue>\n"+
						"</PhoneFax>\n"+
					"</ContactDetails>";
			}
			
			StringBuilder sInputXML = new StringBuilder("<?xml version=\"1.0\"?>\n"
					+ "<BPM_APMQPutGetMessage_Input>\n"+
					"<Option>BPM_APMQPutGetMessageDirect</Option>\n"+
					"<UserID>"+UserName+"</UserID>\n" +
					"<SessionId>"+sessionId+"</SessionId>\n"+
					"<EngineName>"+cabinetName+"</EngineName>\n" +
					"<RequestMessage><EE_EAI_MESSAGE>\n" +
					//"<ProcessName>RAO</ProcessName>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>DEDUP_SUMMARY</MsgFormat>\n" +
					"<MsgVersion>0001</MsgVersion>\n" +
					"<RequestorChannelId>BPM</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>0000</ReturnCode>\n" +
					"<ReturnDesc>REQ</ReturnDesc>\n" +
					"<MessageId>BPM_MARY_02</MessageId>\n" +
					"<Extra1>REQ||BPM.123</Extra1>\n" +
					"<Extra2>2014-03-25T11:05:30.000+04:00</Extra2>\n" +
					"</EE_EAI_HEADER>\n"+
					"<CustomerDuplicationListRequest>" +
						"<BankId>RAK</BankId><CIFID></CIFID><CustomerType>R</CustomerType>" +
						"<RetailCorpFlag>R</RetailCorpFlag><EntityType>All</EntityType>" +
						"<PersonDetails><FirstName>"+ExtTabDataMap.get("custfirstname")+"</FirstName>"+MidName+"" +
						"<LastName>"+ExtTabDataMap.get("custlastname")+"</LastName>"+FullName+"" +
						"<MaritalStatus>"+ExtTabDataMap.get("custmaritalstatus")+"</MaritalStatus>" +
						"<Nationality>"+ExtTabDataMap.get("custnationality")+"</Nationality>" +
						"<DateOfBirth>"+ExtTabDataMap.get("custdob")+"</DateOfBirth>" +
						"</PersonDetails>\n"+MobileNumberDetails+DocDetXml+"</CustomerDuplicationListRequest>\n"+
					"</EE_EAI_MESSAGE></RequestMessage>\n" +				
					"</BPM_APMQPutGetMessage_Input>");
			
			//mqInputRequest = getRequestXML(cabinetName, sessionId, processInstanceID, ws_name, UserName, sInputXML);
			//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", $$outputgGridtXML "+"mqInputRequest for Dedupe Check call" + mqInputRequest);
			
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dedupe input XML: "+sInputXML);

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
			
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Return Desc: "+return_desc);
		    
		    String result_str="";
		    String returnType="";
		    if(responseXML.indexOf("<OperationType>")>-1)
			{
				returnType= responseXML.substring(responseXML.indexOf("<OperationType>")+15,responseXML.indexOf("</OperationType>"));
				//WriteLog("$$returnType "+returnType);
				String wi_name="";
				String appServerType="";
				String cifId="";
				String parentWiName="";
				if(!"0000".equalsIgnoreCase(return_code))
				{

					String errorQuery="SELECT isnull((SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE  error_code='"+return_code+"'),(SELECT alert FROM ng_MASTER_INTEGRATION_ERROR_CODE WHERE error_code='DEFAULT')) As Alert";
					//WriteLog("@@@@@@@@@@@@@@  "+errorQuery);
					//String strInputXml = ExecuteQuery_APSelect(errorQuery,cabinetName,sessionId);
					//String strOutputXml = NGEjbClient.getSharedInstance().makeCall(wrapperIP, wrapperPort, appServerType, strInputXml);
					
					String strInputXml = CommonMethods.apSelectWithColumnNames(errorQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml: "+ strInputXml);
					String strOutputXml = iRBLSysCheckIntegration.WFNGExecute(strInputXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml: "+ strOutputXml);
					result_str=strOutputXml.substring(strOutputXml.indexOf("<alert>")+"</alert>".length()-1,strOutputXml.indexOf("</alert>"));
										
					//WriteLog("select result is: "+result_str);
					//WriteLog("@@@@@@@@@@@@@@  "+result_str);
					return result_str;
				}
				if(returnType.equalsIgnoreCase("TRANSUM"))
				{
					result_str=IntegrateFinancialSummary.parseTRANSUM(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,wi_name,appServerType,cifId,parentWiName);
				}
				else if(returnType.equalsIgnoreCase("AVGBALDET"))
				{
					result_str=IntegrateFinancialSummary.parseAVGBALDET(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,wi_name,appServerType,cifId,parentWiName);
				}
				else if(returnType.equalsIgnoreCase("RETURNDET"))
				{
					result_str=IntegrateFinancialSummary.parseRETURNDET(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,wi_name,appServerType,cifId,parentWiName);
				}
				else if(returnType.equalsIgnoreCase("LIENDET"))
				{
					result_str=IntegrateFinancialSummary.parseLIENDET(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,wi_name,appServerType,cifId,parentWiName);
				}
				else if(returnType.equalsIgnoreCase("SIDET"))
				{
					result_str=IntegrateFinancialSummary.parseSIDET(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,wi_name,appServerType,cifId,parentWiName);
				}
				else if(returnType.equalsIgnoreCase("SALDET"))
				{
					result_str=IntegrateFinancialSummary.parseSALDET(returnType,responseXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),sessionId,cabinetName,wi_name,appServerType,cifId,parentWiName);
				}

			}
			returnType= responseXML.substring(responseXML.indexOf("<MsgFormat>")+11,responseXML.indexOf("</MsgFormat>"));
			//WriteLog("$$returnType result_strresult_strresult_str"+returnType);
			//WriteLog("$$MsgFormat "+returnType);
			if(returnType.equalsIgnoreCase("FINANCIAL_SUMMARY") &&(result_str.equalsIgnoreCase(""))){
				result_str=return_code;
				//WriteLog("$$result_str result_strresult_strresult_str"+result_str);
			}
			return result_str;
		    //return (return_code + "~" + return_desc +"~" + MsgId + "~End");
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public static String parseCollectionSummary(String returnType,String parseXml,String wrapperIP,String wrapperPort,String sessionId,String cabinetName,String wi_name,String appServerType, String prod, String subprod, String cifId, String parentWiName,String cust_type,String CompanyCIF)
	{
		//WriteLog("wrapperIP jsp: parseCollectionSummary: "+wrapperIP);
		//WriteLog("wrapperPort jsp: parseCollectionSummary: "+wrapperPort);
		//WriteLog("sessionId jsp: parseCollectionSummary: "+sessionId);
		//WriteLog("cabinetName jsp: parseCollectionSummary: "+cabinetName);
		//WriteLog("wi_name jsp: parseCollectionSummary: "+wi_name);
		//WriteLog("appServerType jsp: parseCollectionSummary: "+appServerType);
		//WriteLog("parseXml jsp: parseCollectionSummary: "+parseXml);
		//WriteLog("returnType jsp: parseCollectionSummary: "+returnType);
		//WriteLog("cifId jsp: parseCollectionSummary: "+cifId);
		//WriteLog("parentWiName jsp: parseCollectionSummary: "+parentWiName);
		String tagName="";
		String subTagName="";
		String sTableName="";
		String sParentTagName="";
		String result="";
		String flag1="";
		try{	

			//Deepak code commented method changed with new subtag_single param 23jan2018
			String subtag_single="";

			tagName="LoanDetails"; 
			subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
			sTableName="ng_RLOS_CUSTEXPOSE_LoanDetails";
			flag1=commonParseProduct_collection(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,subtag_single,cust_type,CompanyCIF);

			if(flag1.equalsIgnoreCase("true")){
				tagName="CardDetails";
				subTagName = "KeyDt,AmountDtls,DelinquencyInfo";
				sTableName="ng_RLOS_CUSTEXPOSE_CardDetails";
				flag1=commonParseProduct_collection(parseXml,tagName,wi_name,returnType,sTableName,wrapperIP,wrapperPort,sessionId,cabinetName,appServerType,subTagName,prod,subprod,cifId,parentWiName,subtag_single,cust_type,CompanyCIF);
				if(flag1.equalsIgnoreCase("true")){
					tagName="Derived";
					subTagName = "";
					sTableName="NG_rlos_custexpose_Derived";
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
		catch(Exception e){
			System.out.println("Exception occured in parseInternalExposure: "+ e.getMessage());
			e.printStackTrace();
			flag1="false";
		}

		return flag1;
	}
	
	public static String commonParseProduct_collection(String parseXml, String tagName, String wi_name,
			String returnType, String sTableName, String wrapperIP, String wrapperPort, String sessionId,
			String cabinetName, String appServerType, String subTagName, String prod, String subprod, String cifId,
			String parentWiName, String subtag_single, String cust_type, String CompanyCIF) {
		String retVal = "";
		try{
			if(!parseXml.contains(tagName)){
				return "true";
			}
			else
			{ 
				//WriteLog("commonParse jsp: inside: ");

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
				String 	sQry="";
				String selectdata="";
				String companyUpdateQuery="";
				String companiestobeUpdated = "";
				boolean stopIndividualToInsert = false;
				cifId=(parseXml.contains("<CustIdValue>")) ? parseXml.substring(parseXml.indexOf("<CustIdValue>")+"</CustIdValue>".length()-1,parseXml.indexOf("</CustIdValue>")):"";
				if(!CompanyCIF.equalsIgnoreCase("") && cifId.equalsIgnoreCase(CompanyCIF))
				{
					cust_type="Corporate_CIF";
				}
				else
				{
					cust_type="Individual_CIF";
				}
				//WriteLog("Cifid jsp: ReportUrl: "+cifId);
				//WriteLog("tagName jsp: commonParse: "+tagName);
				//WriteLog("subTagName jsp: commonParse: "+subTagName);


				Map<String, String> tagValuesMap= new LinkedHashMap<String, String>();		 
				tagValuesMap=CommonMethods.getTagDataParent_deep(parseXml,tagName,subTagName,subtag_single);

				Map<String, String> map = tagValuesMap;
				//	String colValue="";
				for (Map.Entry<String, String> entry : map.entrySet())
				{
					valueArr=entry.getValue().split("~");
					//WriteLog( "tag values" + entry.getValue());


					//colValue = "'"+valueArr[1].replaceAll("[,]", "','")+"'";
					columnName = valueArr[0]+",Wi_Name,Request_Type,Product_Type,CardType,CifId";
					columnValues = valueArr[1]+",'"+wi_name+"','"+returnType+"','"+prod+"','"+subprod+"','"+cifId+"'";



					//WriteLog( "columnName commonParse" + columnName);
					//WriteLog( "columnValues commonParse" + columnValues);
					if(sTableName.equalsIgnoreCase("ng_RLOS_CUSTEXPOSE_CardDetails")){

						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,Child_Wi";
						columnName = columnName.replaceAll("Card_approve_date","ApplicationCreationDate");
						columnName = columnName.replaceAll("Outstanding_balance","OutstandingAmt");
						columnName = columnName.replaceAll("Credit_limit","CreditLimit");
						columnName = columnName.replaceAll("Overdue_amount","OverdueAmt");
						columnName = columnName.replaceAll("GeneralStatus","General_Status");
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+wi_name+"'";
						sWhere="Child_Wi='"+wi_name+"' AND CardEmbossNum = '"+entry.getKey()+"'";
						sQry="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And CardEmbossNum = '"+entry.getKey()+"' And Liability_type ='Individual_CIF'";
						if(cust_type.equalsIgnoreCase("Individual_CIF")) {
							companyUpdateQuery="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And CardEmbossNum = '"+entry.getKey()+"' And Liability_type ='Corporate_CIF'";
						}

					}
					else if(sTableName.equalsIgnoreCase("ng_RLOS_CUSTEXPOSE_LoanDetails")){
						columnName = valueArr[0]+",Wi_Name,Request_Type,Product_Type,CardType,CifId,Child_Wi";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+prod+"','"+subprod+"','"+cifId+"','"+wi_name+"'";
						columnName = columnName.replaceAll("OutstandingAmt","TotalOutstandingAmt");
						columnName = columnName.replaceAll("Loan_close_date","LoanMaturityDate");
						columnName = columnName.replaceAll("GeneralStatus","General_Status");//Deepak code added to save value in General_Status for PCAS-1264 as it was mising in PL & CC And same was there in RLOS
						sWhere="Child_Wi='"+wi_name+"' AND AgreementId = '"+entry.getKey()+"'";
						sQry="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And AgreementId = '"+entry.getKey()+"' And Liability_type ='Individual_CIF'";
						if(cust_type.equalsIgnoreCase("Individual_CIF")) {
							companyUpdateQuery="Select count(*) as selectdata from "+sTableName+" where Child_Wi='"+wi_name+"' And AgreementId = '"+entry.getKey()+"' And Liability_type ='Corporate_CIF'";
						}
					}
					else if(sTableName.equalsIgnoreCase("ng_RLOS_CUSTEXPOSE_AcctDetails")){
						String CreditGrade = (parseXml.contains("<CreditGrade>")) ? parseXml.substring(parseXml.indexOf("<CreditGrade>")+"</CreditGrade>".length()-1,parseXml.indexOf("</CreditGrade>")):"";
						//PCASP-2833 
						//String isDirect = (parseXml.contains("<IsDirect>")) ? parseXml.substring(parseXml.indexOf("<IsDirect>")+"</IsDirect>".length()-1,parseXml.indexOf("</IsDirect>")):"";
						columnName = valueArr[0]+",Wi_Name,Request_Type,CifId,CreditGrade,Child_Wi";
						columnValues = valueArr[1]+",'"+parentWiName+"','"+returnType+"','"+cifId+"','"+CreditGrade+"','"+wi_name+"'";
						sWhere="Child_Wi='"+wi_name+"' AND AcctId = '"+entry.getKey()+"'";
						sQry="Select count(*) as selectdata from "+sTableName+" where Wi_Name='"+wi_name+"' And AcctId = '"+entry.getKey()+"' And Account_Type ='Individual_CIF'";
					}
					else{
						sWhere="Wi_Name='"+wi_name+"' AND Request_Type='"+returnType+"'";
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
					//WriteLog("getTagValue select mainCode --> "+mainCode);
					//WriteLog("getTagValue select mainCode --> "+row_updated);
					if(!mainCode.equalsIgnoreCase("0") || row_updated.equalsIgnoreCase("0"))
					{	//WriteLog("sQry sQry sQry --> "+sQry);
						if (!sQry.equalsIgnoreCase("")){
							//strInputXml =	ExecuteQuery_APSelect(sQry,cabinetName,sessionId);
							strInputXml = CommonMethods.apSelectWithColumnNames(sQry,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml: "+ strInputXml);
							strOutputXml = iRBLSysCheckIntegration.WFNGExecute(strInputXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml: "+ strOutputXml);

							mainCode = (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>")+"</MainCode>".length()-1,strOutputXml.indexOf("</MainCode>")):"";
							//WriteLog("getTagValue select mainCode --> "+mainCode);
							selectdata=(strOutputXml.contains("<selectdata>")) ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>")+"</selectdata>".length()-1,strOutputXml.indexOf("</selectdata>")):"";
							//WriteLog("getTagValue select selectdata --> "+selectdata);
						}

						if (!companyUpdateQuery.equalsIgnoreCase("")){
							//strInputXml =	ExecuteQuery_APSelect(companyUpdateQuery,cabinetName,sessionId);
							
							strInputXml = CommonMethods.apSelectWithColumnNames(companyUpdateQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strInputXml: "+ strInputXml);
							strOutputXml = iRBLSysCheckIntegration.WFNGExecute(strInputXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("strOutputXml: "+ strOutputXml);

							mainCode = (strOutputXml.contains("<MainCode>")) ? strOutputXml.substring(strOutputXml.indexOf("<MainCode>")+"</MainCode>".length()-1,strOutputXml.indexOf("</MainCode>")):"";
							//WriteLog("getTagValue select mainCode --> "+mainCode);

							companiestobeUpdated=(strOutputXml.contains("<selectdata>")) ? strOutputXml.substring(strOutputXml.indexOf("<selectdata>")+"</selectdata>".length()-1,strOutputXml.indexOf("</selectdata>")):"";
							//WriteLog("getTagValue select companiestobeUpdated --> "+companiestobeUpdated);

							if(Integer.parseInt(companiestobeUpdated)>0){
								if(sTableName.equalsIgnoreCase("ng_RLOS_CUSTEXPOSE_CardDetails")){
									sWhere="Child_Wi='"+wi_name+"' AND CardEmbossNum = '"+entry.getKey()+"' And Liability_type ='Corporate_CIF'";
								}
								else if(sTableName.equalsIgnoreCase("ng_RLOS_CUSTEXPOSE_LoanDetails")){
									sWhere="Child_Wi='"+wi_name+"' AND AgreementId = '"+entry.getKey()+"' And Liability_type ='Corporate_CIF'";
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
								//row_updated = getTagValue(strOutputXml,tagNameU,subTagNameU_2);
								////WriteLog("getTagValue select mainCode for update query for cif"+cifId+"--> "+mainCode);
								////WriteLog("getTagValue select rowUpdated for company for update query for cif"+cifId+" --> "+row_updated);
								stopIndividualToInsert = true;
							}


						}

						if(sQry.equalsIgnoreCase("") || (mainCode.equalsIgnoreCase("0") && selectdata.equalsIgnoreCase("0") && !stopIndividualToInsert)){
							//strInputXml =	ExecuteQuery_APInsert(sTableName,columnName,columnValues,cabinetName,sessionId);
							strInputXml = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnName, columnValues, sTableName);
							iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert from "+sTableName+" Table "+strInputXml);

							strOutputXml=iRBLSysCheckIntegration.WFNGExecute(strInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
														
							mainCode = CommonMethods.getTagDataValue(strOutputXml,tagNameU,subTagNameU);
							if(!mainCode.equalsIgnoreCase("0"))
							{
								retVal = "false";
								//WriteLog("CustExpose_Output jsp: ApINsertfalse for collection summary: "+retVal);
							}
							else
							{
								retVal = "true";
								//WriteLog("CustExpose_Output jsp: ApINserttrue for collection summary: "+retVal);
							}							
						}
						//change by saurabh for company call if its not able to overwrite individual data but call was successful so at frontend it should be successfull. Change on 2nd feb.
						else{
							retVal = "true";
						}
					} 
					else
					{
						retVal = "true";
						//WriteLog("CustExpose_Output jsp: ApUpdatetrue for collection summary: "+retVal);
					}
				}
				//WriteLog("CustExpose_Output jsp: final value for collection summary: "+retVal);
				return retVal;
			}
		}
		catch(Exception e){
			System.out.println("Exception occured in commonParseProduct_collection: "+ e.getMessage());
			e.printStackTrace();
			retVal = "false";
		}
		return retVal;
	}*/

	public static String UpdateGridTableMWResponse(String columnNames, String columnValues, String TransactionTable, String sWhereClause) throws IOException, Exception
	{	
		String RetStatus="";
		String QueryString="";
		String sInputXML="";
		String sOutputXML="";
		if(TransactionTable.equalsIgnoreCase("USR_0_IRBL_CHECKS_GRID_DTLS"))
		{
			//Updating records
			sInputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), TransactionTable, columnNames, columnValues, sWhereClause);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apUpdateInput for "+TransactionTable+" Table : "+sInputXML);

			sOutputXML=iRBLSysCheckIntegration.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apUpdateInput for "+TransactionTable+" Table : "+sOutputXML);

			XMLParser sXMLParserChild= new XMLParser(sOutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("StrMainCode: "+StrMainCode);

		    if (StrMainCode.equals("0"))
			{
		    	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in apUpdateInput the record in : "+TransactionTable);
		    	RetStatus="Success in apUpdateInput the record";
			}
		    else
		    {
		    	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apUpdateInput sOutputXML : "+sOutputXML);
		    	RetStatus="Error in Executing apUpdateInput";
		    }
		}
		else //for Child Integration tables
		{
			QueryString="SELECT "+columnNames+" FROM "+TransactionTable+" with (nolock) where "+sWhereClause;

			//objResponseBean.setAccountCreationReturnCode("Success");

			sInputXML =CommonMethods.apSelectWithColumnNames(QueryString, CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for Apselect for "+TransactionTable+" Table : "+sInputXML);

			sOutputXML=iRBLSysCheckIntegration.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for Apselect for "+TransactionTable+" Table : "+sOutputXML);

			XMLParser sXMLParser= new XMLParser(sOutputXML);
		    String sMainCode = sXMLParser.getValueOf("MainCode");
		    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SMainCode: "+sMainCode);

		    int sTotalRecords = Integer.parseInt(sXMLParser.getValueOf("TotalRetrieved"));
		    iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("STotalRecords: "+sTotalRecords);

			if (sMainCode.equals("0"))
			{
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in APSelect");
		    	
				if(sTotalRecords > 0) //Deletion of records
				{
					sInputXML = CommonMethods.apDeleteInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), TransactionTable, sWhereClause);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apDeleteInput "+TransactionTable+" Table : "+sInputXML);

					sOutputXML=iRBLSysCheckIntegration.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
					iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apDeleteInput "+TransactionTable+" Table : "+sOutputXML);

					XMLParser sXMLParserChild= new XMLParser(sOutputXML);
				    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
				    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("StrMainCode: "+StrMainCode);

				    if (StrMainCode.equals("0"))
					{
				    	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in deleting the record in : "+TransactionTable);
				    	RetStatus="Success in deleting the record";
					}
				    else
				    {
				    	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing APDelete sOutputXML : "+sOutputXML);
				    	RetStatus="Error in Executing APDelete";
				    }
				}
				
				//Insertion of records
				sInputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger, false), columnNames, columnValues, TransactionTable);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Input XML for apInsert "+TransactionTable+" Table : "+sInputXML);

				sOutputXML=iRBLSysCheckIntegration.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Output XML for apInsert "+TransactionTable+" Table : "+sOutputXML);

				XMLParser sXMLParserChild= new XMLParser(sOutputXML);
			    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
			    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("StrMainCode: "+StrMainCode);

			    if (StrMainCode.equals("0"))
				{
			    	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Successful in Inserting the record in : "+TransactionTable);
			    	RetStatus="Success in Inserting the record";
				}
			    else
			    {
			    	iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executing apInsert sOutputXML : "+sOutputXML);
			    	RetStatus="Error in Executing apInsert";
			    }				
			}
			else
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Error in Executin APSelect sOutputXML : "+sOutputXML);
				RetStatus="Error in Executing APSelect";
			}
		}		
		return RetStatus;
	}
	
	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
	{

		String socketServerIP;
		int socketServerPort;
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String outputResponse = null;
		String inputRequest = null;
		String inputMessageID = null;

		try
		{

			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("userName "+ username);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Dout " + dout);
    			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Din " + din);

    			outputResponse = "";



    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
    			}
    			byte[] readBuffer = new byte[500];
    			int num = din.read(readBuffer);
    			if (num > 0)
    			{

    				byte[] arrayBytes = new byte[num];
    				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
    				outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
    				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))

    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId,
    							processInstanceID,outputResponse,integrationWaitTime );




    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId><InputMessageId>"+inputMessageID+"</InputMessageId>");

				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
			return "";
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
					out=null;
				}
				if(socketInputStream != null)
				{

					socketInputStream.close();
					socketInputStream=null;
				}
				if(dout != null)
				{

					dout.close();
					dout=null;
				}
				if(din != null)
				{

					din.close();
					din=null;
				}
				if(socket != null)
				{
					if(!socket.isClosed())
						socket.close();
					socket=null;
				}

			}

			catch(Exception e)
			{
				iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}
	}
	
	private static String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_iRBL_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String
			sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_iRBL_XMLLOG_HISTORY with (nolock) where " +
					"MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=iRBLSysCheckIntegration.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("OutputResponseXML: "+outputResponseXML);
	        		
	        		if(outputResponseXML.contains("<MQ_RESPONSE_XML>"))
	        		{
	        			XMLParser xmlParserResponseXMLData1 = new XMLParser(outputResponseXML);
	        			outputResponseXML = xmlParserResponseXMLData1.getValueOf("MQ_RESPONSE_XML");
	        		}
	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
}
