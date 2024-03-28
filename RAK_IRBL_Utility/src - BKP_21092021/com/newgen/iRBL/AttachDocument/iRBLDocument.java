
package com.newgen.iRBL.AttachDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.newgen.omni.wf.util.app.NGEjbClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.management.StringValueExp;
import javax.xml.parsers.ParserConfigurationException;

import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.wfdesktop.xmlapi.*;

public class iRBLDocument implements Runnable
{
	public static boolean IsUserLoggedIn = false;
	public static boolean srmKeepPolling = true;
	static String lastProcessInstanceId ="";
	static String lastWorkItemId = "";
	private static NGEjbClient ngEjbClientiRBLDocument;
	Runtime mobjRuntime = Runtime.getRuntime();
	private static  String cabinetName;
	private static  String jtsIP;
	private  static String jtsPort;
	private  static String smsPort;
	private  String [] attributeNames;
	private  String queueID;
	private String volumeID;
	private String destFilePath;
	private String ErrorFolder;
	private String ExternalTable="";
	private String MaxNoOfTries;
	public String workItemName="";
	public String InputXML="";
	public String outputXML="";
	int mainCode = 0;
	int TimeIntervalBetweenTrialsInMin=0;
	public String returnCode="";
	public String parentFolderIndex ="";
	public String InputXMLEntryDate;
	public String outputXMLEntryDate;
	public String mainCodeEntryDate;

	public static int loopCount=50;
	public static int updatecount=3;
	public static int sessionCheckInt=0;
	public static int waitLoop=50;
	public static String sessionId;
	Date now=null;

	public static String source=null;
	public static String dest=null;
	public static String TimeStamp="";
	public static String newFilename=null;
	public static String sdate="";

	static Map<String, String> iRBLDocumentCofigParamMap= new HashMap<String, String>();

	private Map <String, String> executeXMLMapMethod = new HashMap<String, String>();

	@Override
	public void run()
	{
		int sleepIntervalInMin=0;


		try
		{
			iRBLDocumentLog.setLogger();
			ngEjbClientiRBLDocument = NGEjbClient.getSharedInstance();

			iRBLDocumentLog.iRBLDocumentLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			iRBLDocumentLog.iRBLDocumentLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				iRBLDocumentLog.iRBLDocumentLogger.error("Could not Read Config Properties [iRBLDocument]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			iRBLDocumentLog.iRBLDocumentLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			iRBLDocumentLog.iRBLDocumentLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			iRBLDocumentLog.iRBLDocumentLogger.debug("JTSPORT: " + jtsPort);

			smsPort = CommonConnection.getsSMSPort();
			iRBLDocumentLog.iRBLDocumentLogger.debug("SMSPort: " + smsPort);
			

			sleepIntervalInMin=Integer.parseInt(iRBLDocumentCofigParamMap.get("SleepIntervalInMin"));
			iRBLDocumentLog.iRBLDocumentLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			attributeNames=iRBLDocumentCofigParamMap.get("AttributeNames").split(",");
			iRBLDocumentLog.iRBLDocumentLogger.debug("AttributeNames: " + attributeNames);

			ExternalTable=iRBLDocumentCofigParamMap.get("ExtTableName");
			iRBLDocumentLog.iRBLDocumentLogger.debug("ExternalTable: " + ExternalTable);

			destFilePath=iRBLDocumentCofigParamMap.get("destFilePath");
			iRBLDocumentLog.iRBLDocumentLogger.debug("destFilePath: " + destFilePath);

			ErrorFolder=iRBLDocumentCofigParamMap.get("failDestFilePath");
			iRBLDocumentLog.iRBLDocumentLogger.debug("ErrorFolder: " + ErrorFolder);

			volumeID=iRBLDocumentCofigParamMap.get("VolumeID");
			iRBLDocumentLog.iRBLDocumentLogger.debug("VolumeID: " + volumeID);

			MaxNoOfTries=iRBLDocumentCofigParamMap.get("MaxNoOfTries");
			iRBLDocumentLog.iRBLDocumentLogger.debug("MaxNoOfTries: " + MaxNoOfTries);

			TimeIntervalBetweenTrialsInMin=Integer.parseInt(iRBLDocumentCofigParamMap.get("TimeIntervalBetweenTrialsInMin"));
			iRBLDocumentLog.iRBLDocumentLogger.debug("TimeIntervalBetweenTrialsInMin: " + TimeIntervalBetweenTrialsInMin);

			sessionId = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
			if(sessionId.trim().equalsIgnoreCase(""))
			{
				iRBLDocumentLog.iRBLDocumentLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				iRBLDocumentLog.iRBLDocumentLogger.debug("Session ID found: " + sessionId);
				while(true)
				{
					iRBLDocumentLog.setLogger();
					startiRBLDocumentUtility();
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			iRBLDocumentLog.iRBLDocumentLogger.error("Exception Occurred in iRBL Document Thread: "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			iRBLDocumentLog.iRBLDocumentLogger.error("Exception Occurred in iRBL Document Thread : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "iRBL_Document_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			{
			    String name = (String) names.nextElement();
			    iRBLDocumentCofigParamMap.put(name, p.getProperty(name));
			}
		}
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startiRBLDocumentUtility()
	{
		iRBLDocumentLog.iRBLDocumentLogger.info("ProcessWI function for iRBL Doc Attachment Utility started");

		String sOutputXml="";
		String sMappedInputXml="";
		long lLngFileSize = 0L;
		String lstrDocFileSize = "";
		String decisionToUpdate="";
		String FailedIntegration="";
		String ErrorMessageFrmIntegration="";
		String Integration_error_received="";
		String statusXML="";
		String ErrorMsg="";
		String strfullFileName="";
		String strDocumentName="";
		String strExtension="";
		String DocumentType="";
		String FilePath="";
		boolean catchflag=false;
		int iNoOfTries=0;
		int iMaxNoOfTries=0;

		sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);

		if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
		{
			iRBLDocumentLog.iRBLDocumentLogger.error("Could Not Get Session ID "+sessionId);
			return;
		}

		List<WorkItem> wiList = new ArrayList<WorkItem>();
		try
		{
			queueID = iRBLDocumentCofigParamMap.get("QueueID");
			iRBLDocumentLog.iRBLDocumentLogger.debug("QueueID: " + queueID);
			wiList = loadWorkItems(queueID,sessionId);
		}
		catch (NumberFormatException e1)
		{
			catchflag=true;
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			catchflag=true;
			e1.printStackTrace();
		}
		catch (Exception e1)
		{
			catchflag=true;
			e1.printStackTrace();
		}

		if (wiList != null)
		{
			for (WorkItem wi : wiList)
			{
				workItemName = wi.getAttribute("WorkItemName");
				parentFolderIndex = wi.getAttribute("ITEMINDEX");
				iRBLDocumentLog.iRBLDocumentLogger.info("The work Item number: " + workItemName);
				iRBLDocumentLog.iRBLDocumentLogger.info("The parentFolder of work Item: " +workItemName+ " issss " +parentFolderIndex);


				FilePath=iRBLDocumentCofigParamMap.get("filePath");
				iRBLDocumentLog.iRBLDocumentLogger.debug("filePath: " + FilePath);

				File folder = new File(FilePath);  //RAKFolder
				File[] listOfFiles = folder.listFiles();
				iRBLDocumentLog.iRBLDocumentLogger.info("List of all folders are--"+listOfFiles);

				boolean ErrorFlag = true;
				String PreviousStage = wi.getAttribute("PreviousStage");
				String NoOfTries = wi.getAttribute("ATTACHDOCNOOFTRIES");
				String LastAttachTryTime = wi.getAttribute("LAST_ATTACH_TRY_TIME");


				Date CurrentDateTime= new Date();
				DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
				String formattedCurrentDateTime = dateFormat.format(CurrentDateTime);
				iRBLDocumentLog.iRBLDocumentLogger.info("LastAttachTryTime--"+LastAttachTryTime);
				iRBLDocumentLog.iRBLDocumentLogger.info("formattedCurrentDateTime--"+formattedCurrentDateTime);


				if (NoOfTries.equalsIgnoreCase("") || NoOfTries == null || NoOfTries == "" || (PreviousStage.equalsIgnoreCase("Dec_Doc_Error_Handling") && NoOfTries.equalsIgnoreCase("4")) )
				{
					NoOfTries = "0";
					LastAttachTryTime = "";
				}
				
				long diffMinutes=0;
				if(!(LastAttachTryTime==null || LastAttachTryTime.equalsIgnoreCase("")))
				{
					//Date d1 = null;
					Date d2 = null;

					try
					{
						//d1=dateFormat.parse(formattedCurrentDateTime);
						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date LasttrytimeFormat = inputDateformat.parse(LastAttachTryTime);
						String formattedLastTryDatetime=outputDateFormat.format(LasttrytimeFormat);

						d2=dateFormat.parse(formattedLastTryDatetime);
						iRBLDocumentLog.iRBLDocumentLogger.info("d2 ----"+d2);

					}

					catch(Exception e)
					{
						e.printStackTrace();
						catchflag=true;
					}
					long diff = CurrentDateTime.getTime() - d2.getTime();
					diffMinutes = diff / (60 * 1000) % 60;
				}
				else
				{
					diffMinutes = 10000;
				}

				File documentFolder = null;
				iNoOfTries = Integer.parseInt(NoOfTries);
				iRBLDocumentLog.iRBLDocumentLogger.info("work Item number: " + workItemName + " iNoOfTries is: "+iNoOfTries+" ,PreviousStage: "+PreviousStage);
				iRBLDocumentLog.iRBLDocumentLogger.info("No if tries are ----"+iNoOfTries);
				iMaxNoOfTries = Integer.parseInt(MaxNoOfTries);
				iRBLDocumentLog.iRBLDocumentLogger.info("diffMinutes ----"+diffMinutes);
				iRBLDocumentLog.iRBLDocumentLogger.info("TimeIntervalBetweenTrialsInMin ----"+TimeIntervalBetweenTrialsInMin);

				if (iNoOfTries < iMaxNoOfTries)
				{
					if(diffMinutes>TimeIntervalBetweenTrialsInMin)
					{
						iRBLDocumentLog.iRBLDocumentLogger.info("Inside if loop 100");
						for (File file : listOfFiles)
						{
							iRBLDocumentLog.iRBLDocumentLogger.info("Inside for loop 101");
							if (file.isDirectory())
							{
								iRBLDocumentLog.iRBLDocumentLogger.info("Inside if loop 102");
								iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" This is a folder : "+file.getName());

								String foldername = file.getName();
								String path = file.getAbsolutePath();

								if(foldername.equalsIgnoreCase(workItemName))
								{
									iRBLDocumentLog.iRBLDocumentLogger.info("Inside 103");
									iRBLDocumentLog.iRBLDocumentLogger.info("Processing Starts for "+workItemName);
									
									// Checking if workitem folder time and execution time is same
									SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
									String strModifiedDate = dateFormat1.format(file.lastModified());
									Date d = new Date();
									String strCurrDateTime = dateFormat1.format(d);
									iRBLDocumentLog.iRBLDocumentLogger.info(file.getName()+", last modified: "+strModifiedDate+", strCurrDateTime: "+strCurrDateTime);
									try {
										Date ModifiedDate=dateFormat1.parse(strModifiedDate);
										Date CurrDateTime=dateFormat1.parse(strCurrDateTime);
										long seconds = (CurrDateTime.getTime()-ModifiedDate.getTime())/1000;
										iRBLDocumentLog.iRBLDocumentLogger.info("Diff in Secs: "+seconds);
										if(seconds < 30)
										{
											try {
												Thread.sleep(30000); // sleeping thread for 30 sec when difference between workitem folder and execution time is less than 30 sec
											} catch (InterruptedException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									} catch (ParseException e) {
										e.printStackTrace();
									}
									//***********************************************************
									
									documentFolder = new File(path);
									File[] listOfDocument = documentFolder.listFiles();
									for (File listOfDoc : listOfDocument)
									{
										if (listOfDoc.isFile())
										{
											strfullFileName = listOfDoc.getName();

											iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" strfullFileName : "+strfullFileName);

											strDocumentName = strfullFileName.substring(0,strfullFileName.lastIndexOf("."));

											String DocNameAsProcess = "";
										
											if (strDocumentName.contains("RBL_BVR_CAM_TWC_ABF_LAF Photographs") || strDocumentName.contains("RBL_BVR_CAM_TWC_ABF_LAF_Photographs"))
												DocNameAsProcess = "RBL_BVR_CAM_TWC_ABF_LAF Photographs";
											else if (strDocumentName.contains("ABF"))
												DocNameAsProcess = "ABF";
											else if (strDocumentName.contains("Account_Opening_Application"))
												DocNameAsProcess = "Account_Opening_Application";
											else if (strDocumentName.contains("Account_Opening_Checklist"))
												DocNameAsProcess = "Account_Opening_Checklist";
											else if (strDocumentName.contains("Additional account opening form"))
												DocNameAsProcess = "Additional account opening form";
											else if (strDocumentName.contains("ADMIN_DOCS"))
												DocNameAsProcess = "ADMIN_DOCS";
											else if (strDocumentName.contains("AECB_Consent_Form_Company"))
												DocNameAsProcess = "AECB_Consent_Form_Company";
											else if (strDocumentName.contains("AECB_Consent_Form_Others"))
												DocNameAsProcess = "AECB_Consent_Form_Others";
											else if (strDocumentName.contains("AECB_Docs_Company"))
												DocNameAsProcess = "AECB_Docs_Company";
											else if (strDocumentName.contains("AECB_Docs_Others"))
												DocNameAsProcess = "AECB_Docs_Others";
											else if (strDocumentName.contains("AED_Express_Form"))
												DocNameAsProcess = "AED_Express_Form";
											else if (strDocumentName.contains("AU_Checklist"))
												DocNameAsProcess = "AU_Checklist";
											else if (strDocumentName.contains("AU_Dedupe_Findings"))
												DocNameAsProcess = "AU_Dedupe_Findings";
											else if (strDocumentName.contains("AU-Confidential"))
												DocNameAsProcess = "AU-Confidential";
											else if (strDocumentName.contains("Audited_Financial_Statements"))
												DocNameAsProcess = "Audited_Financial_Statements";
											else if (strDocumentName.contains("Bankstatements_accountspreadsheet"))
												DocNameAsProcess = "Bankstatements_accountspreadsheet";
											else if (strDocumentName.contains("BB-Visit Confidential"))
												DocNameAsProcess = "BB-Visit Confidential";
											else if (strDocumentName.contains("BO_Report"))
												DocNameAsProcess = "BO_Report";
											else if (strDocumentName.contains("Busines_Visit_Report"))
												DocNameAsProcess = "Busines_Visit_Report";
											else if (strDocumentName.contains("BVR_RM"))
												DocNameAsProcess = "BVR_RM";
											else if (strDocumentName.contains("BVR_Annexure_Form"))
												DocNameAsProcess = "BVR_Annexure_Form";
											else if (strDocumentName.contains("CB_Doc_CO"))
												DocNameAsProcess = "CB_Doc_CO";
											else if (strDocumentName.contains("CB_Doc_others"))
												DocNameAsProcess = "CB_Doc_others";
											else if (strDocumentName.contains("CBRB_Checklist"))
												DocNameAsProcess = "CBRB_Checklist";
											else if (strDocumentName.contains("CBRB_Docs"))
												DocNameAsProcess = "CBRB_Docs";
											else if (strDocumentName.contains("Central Bank Blacklist Check"))
												DocNameAsProcess = "Central Bank Blacklist Check";
											else if (strDocumentName.contains("CEU_Visit_Report"))
												DocNameAsProcess = "CEU_Visit_Report";
											else if (strDocumentName.contains("Cheque_Clearance_Supporting_Doc"))
												DocNameAsProcess = "Cheque_Clearance_Supporting_Doc";
											else if (strDocumentName.contains("CIF_update_form"))
												DocNameAsProcess = "CIF_update_form";
											else if (strDocumentName.contains("Constitutional_Docs"))
												DocNameAsProcess = "Constitutional_Docs";
											else if (strDocumentName.contains("COPS-Confidential"))
												DocNameAsProcess = "COPS-Confidential";
											else if (strDocumentName.contains("CPV_Checklist"))
												DocNameAsProcess = "CPV_Checklist";
											else if (strDocumentName.contains("Credit_Approval_Documents"))
												DocNameAsProcess = "Credit_Approval_Documents";
											else if (strDocumentName.contains("Credit_Emails"))
												DocNameAsProcess = "Credit_Emails";
											else if (strDocumentName.contains("CROPS_Checklist"))
												DocNameAsProcess = "CROPS_Checklist";
											else if (strDocumentName.contains("CV_Personal_Background"))
												DocNameAsProcess = "CV_Personal_Background";
											else if (strDocumentName.contains("Deferral_Documents"))
												DocNameAsProcess = "Deferral_Documents";
											else if (strDocumentName.contains("DNFBP_Checklist"))
												DocNameAsProcess = "DNFBP_Checklist";
											else if (strDocumentName.contains("Dormancy form_Individual"))
												DocNameAsProcess = "Dormancy form_Individual";
											else if (strDocumentName.contains("Dormancy form_Non Individual"))
												DocNameAsProcess = "Dormancy form_Non Individual";
											else if (strDocumentName.contains("Dormancy form"))
												DocNameAsProcess = "Dormancy form";
											else if (strDocumentName.contains("EDD-Confidential"))
												DocNameAsProcess = "EDD-Confidential";
											else if (strDocumentName.contains("EID Validation report"))
												DocNameAsProcess = "EID Validation report";
											else if (strDocumentName.contains("Email_exception_approvals"))
												DocNameAsProcess = "Email_exception_approvals";
											else if (strDocumentName.contains("EUM_proof"))
												DocNameAsProcess = "EUM_proof";
											else if (strDocumentName.contains("Exception list"))
												DocNameAsProcess = "Exception list";
											else if (strDocumentName.contains("Express service form"))
												DocNameAsProcess = "Express service form";
											else if (strDocumentName.contains("Facility_letters"))
												DocNameAsProcess = "Facility_letters";
											else if (strDocumentName.contains("Fast_Track_Check_List"))
												DocNameAsProcess = "Fast_Track_Check_List";
											else if (strDocumentName.contains("FATCA_And_CRS"))
												DocNameAsProcess = "FATCA_And_CRS";
											else if (strDocumentName.contains("FIRCO_Clearance_Co_doc"))
												DocNameAsProcess = "FIRCO_Clearance_Co_doc";
											else if (strDocumentName.contains("FIRCO_Clearance_Ind_doc"))
												DocNameAsProcess = "FIRCO_Clearance_Ind_doc";
											else if (strDocumentName.contains("HOD_SM Approval"))
												DocNameAsProcess = "HOD_SM Approval";
											else if (strDocumentName.contains("WPS_Insurance"))
												DocNameAsProcess = "WPS_Insurance";
											else if (strDocumentName.contains("Insurance"))
												DocNameAsProcess = "Insurance";
											else if (strDocumentName.contains("Invoices_Contracts_BLs_Chq_copies_Agreement_copies"))
												DocNameAsProcess = "Invoices_Contracts_BLs_Chq_copies_Agreement_copies";
											else if (strDocumentName.contains("Old_CAM_BVR_LAF_KYC_RiskScore"))
												DocNameAsProcess = "Old_CAM_BVR_LAF_KYC_RiskScore";
											else if (strDocumentName.contains("KYC"))
												DocNameAsProcess = "KYC";
											else if (strDocumentName.contains("LAF_and_other_security_documents"))
												DocNameAsProcess = "LAF_and_other_security_documents";
											else if (strDocumentName.contains("Letters_to_Customer"))
												DocNameAsProcess = "Letters_to_Customer";
											else if (strDocumentName.contains("Loan_Calculator"))
												DocNameAsProcess = "Loan_Calculator";
											else if (strDocumentName.contains("MRA"))
												DocNameAsProcess = "MRA";
											else if (strDocumentName.contains("OCR_Statementreader_Output"))
												DocNameAsProcess = "OCR_Statementreader_Output";
											else if (strDocumentName.contains("OptionalDocs"))
												DocNameAsProcess = "OptionalDocs";
											else if (strDocumentName.contains("Other Clearance Proof"))
												DocNameAsProcess = "Other Clearance Proof";
											else if (strDocumentName.contains("Others"))
												DocNameAsProcess = "Others";
											else if (strDocumentName.contains("Passport_Visa_EID"))
												DocNameAsProcess = "Passport_Visa_EID";
											else if (strDocumentName.contains("PEP form and other related party docs and POA"))
												DocNameAsProcess = "PEP form and other related party docs and POA";
											else if (strDocumentName.contains("Personal networth Statement"))
												DocNameAsProcess = "Personal networth Statement";
											else if (strDocumentName.contains("PF_Calculation_on_Incremental_Amount"))
												DocNameAsProcess = "PF_Calculation_on_Incremental_Amount";
											else if (strDocumentName.contains("Photograph_Location_VisitingCard"))
												DocNameAsProcess = "Photograph_Location_VisitingCard";
											else if (strDocumentName.contains("POS_Transactions"))
												DocNameAsProcess = "POS_Transactions";
											else if (strDocumentName.contains("Profile change documents"))
												DocNameAsProcess = "Profile change documents";
											else if (strDocumentName.contains("Proof of address and physical locaiton"))
												DocNameAsProcess = "Proof of address and physical locaiton";
											else if (strDocumentName.contains("Property documents"))
												DocNameAsProcess = "Property documents";
											else if (strDocumentName.contains("Related_party_bank_statements"))
												DocNameAsProcess = "Related_party_bank_statements";
											else if (strDocumentName.contains("Related_Party_Doc"))
												DocNameAsProcess = "Related_Party_Doc";
											else if (strDocumentName.contains("Risk_Score"))
												DocNameAsProcess = "Risk_Score";
											else if (strDocumentName.contains("RMVR_and_Attested_ID_Copy"))
												DocNameAsProcess = "RMVR_and_Attested_ID_Copy";
											else if (strDocumentName.contains("Single_View"))
												DocNameAsProcess = "Single_View";
											else if (strDocumentName.contains("Sister company documents"))
												DocNameAsProcess = "Sister company documents";
											else if (strDocumentName.contains("Telephonebill_Tenancycontract_WPS_Proofofstaff"))
												DocNameAsProcess = "Telephonebill_Tenancycontract_WPS_Proofofstaff";
											else if (strDocumentName.contains("TL_On_Website"))
												DocNameAsProcess = "TL_On_Website";
											else if (strDocumentName.contains("Translated_Arabic_Docs"))
												DocNameAsProcess = "Translated_Arabic_Docs";
											else if (strDocumentName.contains("Transport documents"))
												DocNameAsProcess = "Transport documents";
											else if (strDocumentName.contains("Turn_Over_Sheet"))
												DocNameAsProcess = "Turn_Over_Sheet";
											else if (strDocumentName.contains("UBO_Document"))
												DocNameAsProcess = "UBO_Document";
											else if (strDocumentName.contains("Valuationreport_Merchantstatement_chargebackdata"))
												DocNameAsProcess = "Valuationreport_Merchantstatement_chargebackdata";
											else if (strDocumentName.contains("VAT_Return"))
												DocNameAsProcess = "VAT_Return";
											else if (strDocumentName.contains("WC_Doc"))
												DocNameAsProcess = "WC_Doc";
											else	
												DocNameAsProcess = "Others";
											
											strExtension = strfullFileName.substring(strfullFileName.lastIndexOf(".")+1,strfullFileName.length());
											if(strExtension.equalsIgnoreCase("JPG") || strExtension.equalsIgnoreCase("TIF") || strExtension.equalsIgnoreCase("JPEG") || strExtension.equalsIgnoreCase("TIFF"))
											{
												DocumentType = "I";
											}
											else
											{
												DocumentType = "N";
											}

											iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" strDocumentName : "+strDocumentName+" strExtension : "+strExtension);
											String fileExtension= getFileExtension(listOfDoc);

											iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" fileExtension : "+fileExtension);

											/*String[] part = strfullFileName.split("~");

											String DocumentType = part[0];
											String DocumentName = part[1];
											System.out.println("DocumentType "+DocumentType);
											System.out.println("DocumentName "+DocumentName);*/

											//Getting DocName for Addition

											for (int i = 0; i < 3; i++)
											{
												iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" Inside for Loop!");
												//System.out.println("Inside for Loop!");

												JPISIsIndex ISINDEX = new JPISIsIndex();
												JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
												lLngFileSize = listOfDoc.length();
												lstrDocFileSize = Long.toString(lLngFileSize);

												if(lLngFileSize != 0L)
												{
													iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" The Document address is: "+path+System.getProperty("file.separator")+listOfDoc.getName());
													//String docPath=path.concat("/").concat(listOfDoc.getName());
													String docPath=path+System.getProperty("file.separator")+listOfDoc.getName();

													try
													{
														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" before CPISDocumentTxn AddDocument MT: ");

														if(smsPort.startsWith("33"))
														{
															CPISDocumentTxn.AddDocument_MT(null, jtsIP , Short.parseShort(smsPort), cabinetName, Short.parseShort(volumeID), docPath, JPISDEC, "",ISINDEX);
														}
														else
														{
															CPISDocumentTxn.AddDocument_MT(null, jtsIP , Short.parseShort(smsPort), cabinetName, Short.parseShort(volumeID), docPath, JPISDEC, null,"JNDI", ISINDEX);
														}	

														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" after CPISDocumentTxn AddDocument MT: ");

														String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" sISIndex: "+sISIndex);
														sMappedInputXml = CommonMethods.getNGOAddDocument(parentFolderIndex,DocNameAsProcess,DocumentType,strExtension,sISIndex,lstrDocFileSize,volumeID,cabinetName,sessionId);
														iRBLDocumentLog.iRBLDocumentLogger.debug("workItemName: "+workItemName+" sMappedInputXml "+sMappedInputXml);
														iRBLDocumentLog.iRBLDocumentLogger.debug("Input xml For NGOAddDocument Call: "+sMappedInputXml);

														sOutputXml=WFNGExecute(sMappedInputXml,jtsIP,Integer.parseInt(jtsPort),1);
														sOutputXml=sOutputXml.replace("<Document>","");
														sOutputXml=sOutputXml.replace("</Document>","");
														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" Output xml For NGOAddDocument Call: "+sOutputXml);
														iRBLDocumentLog.iRBLDocumentLogger.debug("Output xml For NGOAddDocument Call: "+sOutputXml);
														statusXML = CommonMethods.getTagValues(sOutputXml,"Status");
														ErrorMsg = CommonMethods.getTagValues(sOutputXml,"Error");
														//statusXML ="0";
														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" The maincode of the output xml file is " +statusXML);
														//System.out.println("The maincode of the output xml file is " +statusXML);

														// updating expected document status in table
														updateExternalTable("USR_0_IRBL_DOC_CHECKLIST_DTLS","STATUS_","'Yes'","WI_NAME='"+workItemName+"' and DOC_TYPE='"+DocNameAsProcess+"' and EXPECTED_FROM_DEH = 'Yes' ");
													
													}
													catch (NumberFormatException e)
													{
														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName1:"+e.getMessage());
														e.printStackTrace();
														catchflag=true;
													}
													catch (JPISException e)
													{
														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName2:"+e.getMessage());
														e.printStackTrace();
														catchflag=true;
													}
													catch (Exception e)
													{
														iRBLDocumentLog.iRBLDocumentLogger.info("workItemName3:"+e.getMessage());
														e.printStackTrace();
														catchflag=true;
													}
												}
												if(statusXML.equalsIgnoreCase("0"))
													i=3;
											}

											//update historytable external table and doneworkitem
											now = new Date();
											Format formatter = new SimpleDateFormat("dd-MMM-yy");
											sdate = formatter.format(now);
											iRBLDocumentLog.iRBLDocumentLogger.info("statusXML maincode is--"+statusXML);
											if("0".equalsIgnoreCase(statusXML))
											{
												iRBLDocumentLog.iRBLDocumentLogger.debug("File "+strfullFileName +" destination "+destFilePath);
												//source = ""+documentFolder+"/"+strfullFileName+"";
												source = ""+documentFolder+System.getProperty("file.separator")+strfullFileName+"";
												//dest = ""+destFilePath+"/"+sdate+"/"+workItemName;
												dest = ""+destFilePath+System.getProperty("file.separator")+sdate+System.getProperty("file.separator")+workItemName;
												TimeStamp=get_timestamp();
												newFilename = Move(dest,source,TimeStamp);
											}
											iRBLDocumentLog.iRBLDocumentLogger.info("catch flag is--"+catchflag);
											if(!("0".equalsIgnoreCase(statusXML)) || catchflag==true)
											{
												iRBLDocumentLog.iRBLDocumentLogger.info("WI Going to the error folder");
												iRBLDocumentLog.iRBLDocumentLogger.debug("File "+strfullFileName +" destination "+destFilePath);
												//source = ""+documentFolder+"/"+strfullFileName+"";
												source = ""+documentFolder+System.getProperty("file.separator")+strfullFileName+"";
												//dest = ""+ErrorFolder+"/"+sdate+"/"+workItemName;
												dest = ""+ErrorFolder+System.getProperty("file.separator")+sdate+System.getProperty("file.separator")+workItemName;
												TimeStamp=get_timestamp();
												newFilename = Move(dest,source,TimeStamp);
												continue;
											}
										}
									}

									try
									{
										int status = getStatusOfExpectedDocs(workItemName);
										if("0".equalsIgnoreCase(statusXML))
										{
											String DecRemarks = "";
											documentFolder.delete();
											if (status >= 1)
											{
												DecRemarks = "Expected Documents are not available";
												decisionToUpdate="Failure";
											} 
											else 
											{
												DecRemarks = "Expected Documents Attached by Utility";
												decisionToUpdate="Success";
											}	
											historyCaller(workItemName,decisionToUpdate,DecRemarks);
											FailedIntegration=" ";
											ErrorMessageFrmIntegration=" ";
											Integration_error_received= " ";
										}
										else
										{
											documentFolder.delete();
											if(ErrorMsg.trim().equalsIgnoreCase(""))
												ErrorMsg = "Expected Documents are not available";
											decisionToUpdate="Failure";
											historyCaller(workItemName,decisionToUpdate,ErrorMsg);
											FailedIntegration="NGOAddDocument";
											Integration_error_received="Attach_Online_Document";
											ErrorMessageFrmIntegration=ErrorMsg;
										}

										iRBLDocumentLog.iRBLDocumentLogger.info("Current date time is---"+get_timestamp());
										updateExternalTable(ExternalTable,"decision,FAILEDINTEGRATIONCALL,MW_ERRORDESC,LAST_ATTACH_TRY_TIME","'" + decisionToUpdate + "','"+FailedIntegration+"','"+ErrorMessageFrmIntegration+"','"+formattedCurrentDateTime+"'","ITEMINDEX='"+parentFolderIndex+"'");
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
									//Call done workitem to move the workitem to next step
									ErrorFlag = false;
									doneWorkItem(workItemName, "");
								}
								else
								{
									iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" Folder name doesn't match the workitem name");
								}
							}
							else
							{
								iRBLDocumentLog.iRBLDocumentLogger.info("workItemName: "+workItemName+" It is not a folder"+file.getName());
							}
						}
					}
					else
					{
						continue;
					}
				}

				// updating number tries AttachDocNoOfTries in external table
				try
				{
					if (ErrorFlag)
					{
						iRBLDocumentLog.iRBLDocumentLogger.info("updating AttachDocNoOfTries");
						decisionToUpdate = "Failure";
						FailedIntegration= "DocNotAvailable";
						ErrorMessageFrmIntegration = "Document Not Available";
						Integration_error_received="Attach_Online_Document";
						iNoOfTries++;
						updateExternalTable(ExternalTable,"decision,ATTACHDOCNOOFTRIES,LAST_ATTACH_TRY_TIME","'" + decisionToUpdate + "','"+iNoOfTries+"','"+formattedCurrentDateTime+"'","ITEMINDEX='"+parentFolderIndex+"'");

						if (iNoOfTries > iMaxNoOfTries)
						{
							historyCaller(workItemName,"Failure","Expected Documents are not available");
							doneWorkItem(workItemName, "");
						}
					}
				}
				catch (Exception e)
				{
					iRBLDocumentLog.iRBLDocumentLogger.info("exception in updating AttachDocNoOfTries");
				}
				//****************************************
			}
		}
		iRBLDocumentLog.iRBLDocumentLogger.info("exiting ProcessWI function iRBL Doc Attach Utility");
	}

	private void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere)
	{
		sessionCheckInt=0;

		while(sessionCheckInt<loopCount)
		{
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionId);
				iRBLDocumentLog.iRBLDocumentLogger.debug("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate);
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,Integer.parseInt(jtsPort),1);
				iRBLDocumentLog.iRBLDocumentLogger.info("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate);
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0"))
				{
					iRBLDocumentLog.iRBLDocumentLogger.error("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else
				{
					iRBLDocumentLog.iRBLDocumentLogger.error("Succesfully updated "+tablename+" table");
					System.out.println("Succesfully updated "+tablename+" table");
					//ThreadConnect.addToTextArea("Successfully updated transaction table");
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
				}
				else
				{
					sessionCheckInt++;
					break;
				}

				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == "" || outXmlCheckAPUpdate == null)
					break;

			}
			catch(Exception e)
			{
				iRBLDocumentLog.iRBLDocumentLogger.error("Inside create validateSessionID exception"+e);
			}
		}
	}

	//Function to make thread sleep
	public static void waiteloop(long wtime)
	{
        try
		{
            for (int i = 0; i < 10; i++)
			{
                Thread.yield();
                Thread.sleep(wtime / 10);
                if (!srmKeepPolling)
				{
                    break;
                }
            }
        }
		catch (InterruptedException e)
		{
        }
    }




	public static void waiteloopExecute(long wtime) {
		try {
			for (int i = 0; i < 10; i++) {
				Thread.yield();
				Thread.sleep(wtime / 10);
			}
		} catch (InterruptedException e) {
		}
	}



		 private List loadWorkItems(String queueID,String sessionId) throws NumberFormatException, IOException, Exception
		{
			 	iRBLDocumentLog.iRBLDocumentLogger.info("Starting loadWorkitem function for queueID -->"+queueID);
				List workItemList = null;
				String workItemListInputXML="";
				sessionCheckInt=0;
				String workItemListOutputXML="";
				iRBLDocumentLog.iRBLDocumentLogger.info("loopCount aa:" + loopCount);
				iRBLDocumentLog.iRBLDocumentLogger.info("lastWorkItemId aa:" + lastWorkItemId);
				iRBLDocumentLog.iRBLDocumentLogger.info("lastProcessInstanceId aa:" + lastProcessInstanceId);
				while(sessionCheckInt<loopCount)
				{
					iRBLDocumentLog.iRBLDocumentLogger.info("123 cabinet name..."+cabinetName);
					iRBLDocumentLog.iRBLDocumentLogger.info("123 session id is..."+sessionId);
					workItemListInputXML = CommonMethods.getFetchWorkItemsInputXML(lastProcessInstanceId, lastWorkItemId, sessionId, cabinetName, queueID);
					iRBLDocumentLog.iRBLDocumentLogger.info("workItemListInputXML aa:" + workItemListInputXML);
					try
					{
						workItemListOutputXML=WFNGExecute(workItemListInputXML,jtsIP,Integer.parseInt(jtsPort),1);
					}
					catch(Exception e)
					{
						iRBLDocumentLog.iRBLDocumentLogger.error("Exception in Execute : " + e);
						sessionCheckInt++;
						waiteloopExecute(waitLoop);
						sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
						continue;
					}

					iRBLDocumentLog.iRBLDocumentLogger.info("workItemListOutputXML : " + workItemListOutputXML);
					if (CommonMethods.getTagValues(workItemListOutputXML,"MainCode").equalsIgnoreCase("11"))
					{
						sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
					}
					else
					{
						sessionCheckInt++;
						break;
					}
				}

				int i = 0;
				while(i <= 3)
				{
					if (CommonMethods.getMainCode(workItemListOutputXML) == 0)
					{
						i = 4;
						String [] last = new String[2];
						workItemList = new ArrayList();
						List workItems = getWorkItems(sessionId,workItemListOutputXML, last);
						workItemList.addAll(workItems);
						lastProcessInstanceId = "";
						lastWorkItemId = "";
					}
					else
					{
						i++;
						lastProcessInstanceId = "";
						lastWorkItemId = "";
					}
				}
				iRBLDocumentLog.iRBLDocumentLogger.info("Exiting loadWorkitem function for queueID -->"+queueID);
				return workItemList;
			}



		 public static String WFNGExecute(String ipXML, String serverIP,
					int serverPort, int flag) throws IOException, Exception {
			 String jtsPort=""+serverPort;
				if (jtsPort.startsWith("33"))
					return WFCallBroker.execute(ipXML, serverIP, serverPort, flag);
				else
					return ngEjbClientiRBLDocument.makeCall(serverIP, serverPort + "", "WebSphere",
							ipXML);
			}

		 private List getWorkItems(String sessionId, String workItemListOutputXML, String[] last) throws NumberFormatException, Exception
		 {
				// TODO Auto-generated method stub
			 iRBLDocumentLog.iRBLDocumentLogger.info("Starting getWorkitems function ");
				Document doc = CommonMethods.getDocument(workItemListOutputXML);

				NodeList instruments = doc.getElementsByTagName("Instrument");
				List workItems = new ArrayList();

				int length = instruments.getLength();

				for (int i =0; i < length; ++i)
				{
					Node inst = instruments.item(i);
					WorkItem wi = getWI(sessionId, inst);
					workItems.add(wi);
				}
				int size = workItems.size();
				if (size > 0)
				{
					WorkItem item = (WorkItem)workItems.get(size -1);
					last[0] = item.processInstanceId;
					last[1] = item.workItemId;

					iRBLDocumentLog.iRBLDocumentLogger.info("last[0] : "+last[0]);
				}
				iRBLDocumentLog.iRBLDocumentLogger.info("Exiting getWorkitems function");
				return workItems;
			}

		 private String getFileExtension(File file) {
		        String name = file.getName();
		        try {
		            return name.substring(name.lastIndexOf(".") + 1);
		        } catch (Exception e) {
		            return "";
		        }
		    }
		 public static String getAttribute(String fetchAttributeOutputXML, String accountNo) throws ParserConfigurationException, SAXException, IOException {
				Document doc = CommonMethods.getDocument(fetchAttributeOutputXML);
				NodeList nodeList = doc.getElementsByTagName("Attribute");
				int length = nodeList.getLength();
				for (int i = 0; i < length; ++i) {
					Node item = nodeList.item(i);
					String name = CommonMethods.getTagValues(item, "Name");
					if (name.trim().equalsIgnoreCase(accountNo.trim())) {
						return CommonMethods.getTagValues(item, "Value");
					}
				}
				return "";
			}

		 public static String get_timestamp()
			{
				Date present = new Date();
				Format pformatter = new SimpleDateFormat("dd-MM-yyyy-hhmmss");
				TimeStamp=pformatter.format(present);
				return TimeStamp;
			}
			public static String Move(String destFolderPath, String srcFolderPath,String append)
			{
				try
				{
					File objDestFolder = new File(destFolderPath);
					if (!objDestFolder.exists())
					{
						objDestFolder.mkdirs();
					}
					File objsrcFolderPath = new File(srcFolderPath);
					newFilename = objsrcFolderPath.getName();
					File lobjFileTemp = new File(destFolderPath + File.separator + newFilename);
					if (lobjFileTemp.exists())
					{
						if (!lobjFileTemp.isDirectory())
						{
							lobjFileTemp.delete();
						}
						else
						{
							deleteDir(lobjFileTemp);
						}
					}
					else
					{
						lobjFileTemp = null;
					}
					File lobjNewFolder = new File(objDestFolder, newFilename +"_"+ append);

					boolean lbSTPuccess = false;
					try
					{
						lbSTPuccess = objsrcFolderPath.renameTo(lobjNewFolder);
					}
					catch (SecurityException lobjExp)
					{
						System.out.println("SecurityException");
					}
					catch (NullPointerException lobjNPExp)
					{
						System.out.println("NullPointerException");
					}
					catch (Exception lobjExp)
					{
						System.out.println("Exception");
					}
					if (!lbSTPuccess)
					{
						System.out.println("lbSTPuccess");
					}
					else
					{
						System.out.println("else");
					}
					objDestFolder = null;
					objsrcFolderPath = null;
					lobjNewFolder = null;
				}
				catch (Exception lobjExp)
				{
				}

				return newFilename;
			}

			public static boolean deleteDir(File dir) throws Exception {
				if (dir.isDirectory()) {
					String[] lstrChildren = dir.list();
					for (int i = 0; i < lstrChildren.length; i++) {
						boolean success = deleteDir(new File(dir, lstrChildren[i]));
						if (!success) {
							return false;
						}
					}
				}
				return dir.delete();
			}

			private void historyCaller(String workItemName, String decision, String remarks)
			{
				iRBLDocumentLog.iRBLDocumentLogger.debug("In History Caller method");

				XMLParser objXMLParser = new XMLParser();
				String sOutputXML=null;
				String mainCodeforAPInsert=null;
				sessionCheckInt=0;
				while(sessionCheckInt<loopCount)
				{
					try{

						if(workItemName!=null)
						{
							String hist_table="USR_0_iRBL_WIHISTORY";
							String columns="wi_name,workstep,decision,action_date_time,remarks,user_name,Entry_Date_Time";
							String WI_NAME=workItemName;
							String WSNAME="Attach_Online_Document";
							
							String lusername="System";


							/*java.util.Date today = new java.util.Date();
							SimpleDateFormat simpleDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
							String actionDateTime = simpleDate.format(today).toString();*/

							SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

							Date actionDateTime= new Date();
							String formattedActionDateTime=outputDateFormat.format(actionDateTime);
							iRBLDocumentLog.iRBLDocumentLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

							String entryDatetime=getEntryDatetimefromDB(workItemName);


							String values = "'" + WI_NAME +"'" + "," + "'" + WSNAME +"'" + "," + "'" + decision +"'" + ","  + "'"+formattedActionDateTime+"'" + "," + "'" + remarks +"'" + "," +  "'" + lusername + "'" +  "," + "'"+entryDatetime+"'";
							iRBLDocumentLog.iRBLDocumentLogger.debug("Values for history : \n"+values);

							String sInputXMLAPInsert = CommonMethods.apInsert(cabinetName,sessionId,columns,values,hist_table);

							iRBLDocumentLog.iRBLDocumentLogger.info("History_InputXml::::::::::\n"+sInputXMLAPInsert);
							sOutputXML= WFNGExecute(sInputXMLAPInsert,jtsIP,Integer.parseInt(jtsPort),1);
							iRBLDocumentLog.iRBLDocumentLogger.info("History_OutputXml::::::::::\n"+sOutputXML);
							objXMLParser.setInputXML(sOutputXML);
							mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");

						}
					}
					catch(Exception e){
						e.printStackTrace();
						iRBLDocumentLog.iRBLDocumentLogger.error("Exception in historyCaller of UpdateExpiryDate", e);
						sessionCheckInt++;
						waiteloopExecute(waitLoop);
						sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
						continue;

					}
					if (mainCodeforAPInsert.equalsIgnoreCase("11")) {
						sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
					}else{
						sessionCheckInt++;
						break;
					}
				}
				if(mainCodeforAPInsert.equalsIgnoreCase("0")){
					iRBLDocumentLog.iRBLDocumentLogger.info("Insert Successful");
				}
				else{

					iRBLDocumentLog.iRBLDocumentLogger.info("Insert Unsuccessful");
				}
				iRBLDocumentLog.iRBLDocumentLogger.debug("Out History Caller method");
			}

			public String getEntryDatetimefromDB(String workItemName)
			{
				iRBLDocumentLog.iRBLDocumentLogger.info("Start of function getEntryDatetimefromDB ");
				String entryDatetimeAttachCust="";
				String formattedEntryDatetime="";
				String outputXMLEntryDate=null;
				String mainCodeEntryDate=null;

				sessionCheckInt=0;
				while(sessionCheckInt<loopCount)
				{
					try {
						XMLParser objXMLParser = new XMLParser();
						String sqlQuery = "select entryat from RB_iRBL_EXTTABLE with(nolock) where WINAME='"+workItemName+"'";
						String InputXMLEntryDate = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionId);
						iRBLDocumentLog.iRBLDocumentLogger.info("Getting getIntegrationErrorDescription from exttable table "+InputXMLEntryDate);
						outputXMLEntryDate = WFNGExecute(InputXMLEntryDate, jtsIP, Integer.parseInt(jtsPort), 1);
						iRBLDocumentLog.iRBLDocumentLogger.info("OutputXML for getting getIntegrationErrorDescription from external table "+outputXMLEntryDate);
						objXMLParser.setInputXML(outputXMLEntryDate);
						mainCodeEntryDate=objXMLParser.getValueOf("MainCode");
						} catch (Exception e) {
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
					if (!mainCodeEntryDate.equalsIgnoreCase("0"))
					{
						sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);

					}else{
							sessionCheckInt++;
							break;
						}
				}

				if (mainCodeEntryDate.equalsIgnoreCase("0")) {
					try {
						entryDatetimeAttachCust = CommonMethods.getTagValues(outputXMLEntryDate, "entryat");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDatetimeAttachCust);
						formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
						iRBLDocumentLog.iRBLDocumentLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

						iRBLDocumentLog.iRBLDocumentLogger.info("newentrydatetime "+ formattedEntryDatetime);
					}catch (Exception e) {
						e.printStackTrace();
					}


				}
			return formattedEntryDatetime;
		}

			public int getStatusOfExpectedDocs(String workItemName)
			{
				iRBLDocumentLog.iRBLDocumentLogger.info("Start of function getStatusOfExpectedDocs ");
				int count=0;
				String outputXMLEntryDate=null;
				String mainCodeEntryDate=null;

				sessionCheckInt=0;
				while(sessionCheckInt<loopCount)
				{
					try {
						XMLParser objXMLParser = new XMLParser();
						String sqlQuery = "SELECT count(*) AS StatusCount FROM USR_0_IRBL_DOC_CHECKLIST_DTLS with(nolock) WHERE WI_NAME = '"+workItemName+"' AND EXPECTED_FROM_DEH = 'Yes' AND (STATUS_ IS NULL OR STATUS_ = 'No' OR STATUS_ = 'NA' OR STATUS_ = '') ";
						String InputXMLEntryDate = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionId);
						iRBLDocumentLog.iRBLDocumentLogger.info("Getting getStatusOfExpectedDocs: "+InputXMLEntryDate);
						outputXMLEntryDate = WFNGExecute(InputXMLEntryDate, jtsIP, Integer.parseInt(jtsPort), 1);
						iRBLDocumentLog.iRBLDocumentLogger.info("OutputXML for getting getStatusOfExpectedDocs: "+outputXMLEntryDate);
						objXMLParser.setInputXML(outputXMLEntryDate);
						mainCodeEntryDate=objXMLParser.getValueOf("MainCode");
						} catch (Exception e) {
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
					if (!mainCodeEntryDate.equalsIgnoreCase("0"))
					{
						sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);

					}else{
							sessionCheckInt++;
							break;
						}
				}

				if (mainCodeEntryDate.equalsIgnoreCase("0")) {
					try {
						count = Integer.parseInt(CommonMethods.getTagValues(outputXMLEntryDate, "StatusCount"));

						iRBLDocumentLog.iRBLDocumentLogger.debug("count: "+count);
					}catch (Exception e) {
						e.printStackTrace();
					}


				}
			return count;
		}
			
			private WorkItem getWI(String sessionId, Node inst) throws NumberFormatException, IOException, Exception
			{
				iRBLDocumentLog.iRBLDocumentLogger.info("Starting getWI function");
				WorkItem wi = new WorkItem();
				wi.processInstanceId = CommonMethods.getTagValues(inst, "ProcessInstanceId");
				wi.workItemId = CommonMethods.getTagValues(inst, "WorkItemId");
				String fetchAttributeInputXML="";
				String fetchAttributeOutputXML="";
				sessionCheckInt=0;
				while(sessionCheckInt<loopCount)
		        {
					fetchAttributeInputXML = CommonMethods.getFetchWorkItemAttributesXML(cabinetName,sessionId,wi.processInstanceId, wi.workItemId);
					iRBLDocumentLog.iRBLDocumentLogger.info("FetchAttributeInputXMl "+fetchAttributeInputXML);
					fetchAttributeOutputXML=WFNGExecute(fetchAttributeInputXML,jtsIP,Integer.parseInt(jtsPort),1);
					fetchAttributeOutputXML=fetchAttributeOutputXML.replaceAll("&","&amp;");
					//fetchAttributeOutputXML=fetchAttributeOutputXML.replaceAll("<","&lt;");
					//fetchAttributeOutputXML=fetchAttributeOutputXML.replaceAll(">","&gt;");
					iRBLDocumentLog.iRBLDocumentLogger.info("fetchAttributeOutputXML "+fetchAttributeOutputXML);
					if (CommonMethods.getTagValues(fetchAttributeOutputXML, "MainCode").equalsIgnoreCase("11"))
					{
						sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);

					} else {
							sessionCheckInt++;
							break;
							}

					if (CommonMethods.getMainCode(fetchAttributeOutputXML) != 0)
					{
						iRBLDocumentLog.iRBLDocumentLogger.debug(" MapXML.getMainCode(fetchAttributeOutputXML) != 0 ");
						//throw new RuntimeException();
					}
				}

				try
				{
					for (int i = 0; i < attributeNames.length; ++i)
					{
						String columnValue = getAttribute(fetchAttributeOutputXML, attributeNames[i]);
						if (columnValue != null)
						{
							if(attributeNames[i].equalsIgnoreCase("ACCOUNT_NUMBER"))
								wi.map.put(attributeNames[i], columnValue.replaceAll("-",""));
							else
								wi.map.put(attributeNames[i], columnValue);
						}
						else
						{
							wi.map.put(attributeNames[i], "");
						}
					}

				}
				catch(Exception e)
				{
					e.printStackTrace();
					iRBLDocumentLog.iRBLDocumentLogger.debug("Inside catch of get wi function with exception.."+e);
				}
				iRBLDocumentLog.iRBLDocumentLogger.info("Exiting getWI function");
				return wi;
			}

			private void doneWorkItem(String wi_name,String values,Boolean... compeletFlag)
			{
				assert compeletFlag.length <= 1;
				sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
				try
				{
					executeXMLMapMethod.clear();
					sessionCheckInt=0;
					while(sessionCheckInt<loopCount)
					{
						executeXMLMapMethod.put("getWorkItemInputXML",CommonMethods.getWorkItemInput(cabinetName,sessionId,wi_name, "1"));
						//System.out.println("getWorkItemInputXML ---: "+executeXMLMapMethod.get("getWorkItemInputXML"));
						try
						{
							executeXMLMapMethod.put("getWorkItemOutputXML",WFNGExecute((String)executeXMLMapMethod.get("getWorkItemInputXML"),jtsIP,Integer.parseInt(jtsPort),1));
						}
						catch(Exception e)
						{
							iRBLDocumentLog.iRBLDocumentLogger.error("Exception in Execute : " + e);
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);
							continue;
						}

						//System.out.println("getWI call output : "+executeXMLMapMethod.get("getWorkItemOutputXML"));
						sessionCheckInt++;
						if (CommonMethods.getTagValues((String)executeXMLMapMethod.get("getWorkItemOutputXML"),"MainCode").equalsIgnoreCase("11"))
						{
							sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);

						}
						else
						{
							sessionCheckInt++;
							break;
						}
					}
					if (CommonMethods.getTagValues((String)executeXMLMapMethod.get("getWorkItemOutputXML"),"MainCode").equalsIgnoreCase("0"))
					{
						sessionCheckInt=0;
						while(sessionCheckInt<loopCount)
						{
							executeXMLMapMethod.put("inputXml1",CommonMethods.completeWorkItemInput(cabinetName,sessionId,wi_name,Integer.toString(1)));
							iRBLDocumentLog.iRBLDocumentLogger.info("inputXml1 ---: "+executeXMLMapMethod.get("inputXml1"));
							iRBLDocumentLog.iRBLDocumentLogger.debug("Output XML APCOMPLETE "+executeXMLMapMethod.get("inputXml1"));
							try
							{
								executeXMLMapMethod.put("outXml1",WFNGExecute((String)executeXMLMapMethod.get("inputXml1"),jtsIP,Integer.parseInt(jtsPort),1));
							}
							catch(Exception e)
							{
								iRBLDocumentLog.iRBLDocumentLogger.error("Exception in Execute : " + e);
								sessionCheckInt++;
								waiteloopExecute(waitLoop);
								sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);

								continue;
							}

							iRBLDocumentLog.iRBLDocumentLogger.info("outXml1 "+executeXMLMapMethod.get("outXml1"));
							sessionCheckInt++;
							if (CommonMethods.getTagValues((String)executeXMLMapMethod.get("outXml1"),"MainCode").equalsIgnoreCase("11"))
							{
								sessionId  = CommonConnection.getSessionID(iRBLDocumentLog.iRBLDocumentLogger, false);

							}
							else
							{
								sessionCheckInt++;
								break;
							}
						}
					}
					if (CommonMethods.getTagValues((String)executeXMLMapMethod.get("outXml1"),"MainCode").equalsIgnoreCase("0"))
					{
						iRBLDocumentLog.iRBLDocumentLogger.info("Completed "+wi_name);
						//if(!decision.equalsIgnoreCase("failure"))
						//decision="Success";
						//createHistory(wi_name,"Book Utility","","Book_Transaction","Submit");
					}
					else
					{
						//decision="failure";
						iRBLDocumentLog.iRBLDocumentLogger.info("Problem in completion of "+wi_name+" ,Maincode :"+CommonMethods.getTagValues((String)executeXMLMapMethod.get("outXml1"),"MainCode"));
					}
				}
				catch(Exception e)
				{
					iRBLDocumentLog.iRBLDocumentLogger.error("Exception in workitem done = " +e);

					final Writer result = new StringWriter();
					final PrintWriter printWriter = new PrintWriter(result);
					e.printStackTrace(printWriter);
					iRBLDocumentLog.iRBLDocumentLogger.error("Exception Occurred in done wi : "+result);
				}
			}
}


