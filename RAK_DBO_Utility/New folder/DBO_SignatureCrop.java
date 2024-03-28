package com.newgen.DBO.SignatureCrop;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.nio.*;
import java.nio.file.StandardCopyOption;

import org.apache.commons.codec.binary.Base64;

import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.DBO.AWB_Delivery.DBO_AWB_Logs;
import com.newgen.DBO.AWB_Delivery.DBO_PrimeCBS_Logs;
import com.newgen.DBO.AttachDocument.AttachDocLogs;
import com.newgen.DBO.QRCodePOC.QRCodePOC;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class DBO_SignatureCrop implements Runnable {
	
	
	static Map<String, String> DBO_SignCrop_cofigParamMap= new HashMap<String, String>();
	private static  String SegregatedDocument_Success=null;
	private static  String SegregatedDocument_Input=null;
	private static  String SegregatedDocument_Error=null;
	private static  String ReadyToAttach_Error=null;
	private static  String ReadyToAttach_Input=null;
	private static  String ReadyToAttach_Success=null;
	private static  String cabinetName;
	private static  String jtsIP;
	private static String jtsPort;
	private static String smsPort;
	private int sleepIntervalInMin;
	private String queueID;
	private String volumeID;
	public static String sessionId;
	public static String OnePagerSourceLocation=null;
	public String msg=null;
	public String base64String=null;
	public String Account_No=null;
	public String CIF_ID=null;
	public String DATE=null;
	public String CustomerName=null;
	public String Sig_Remarks=null;
	private static NGEjbClient ngEjbClientCIFVer;
	private String processInstanceID=null;
	private String AWBNo=null;
	private String ws_name=null;
	public static String newFilename=null;
	Date now=null;
		
	public void run() {
		DBO_SignCrop_Logs.setLogger();
		try {
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();
		} catch (NGException e) {
			e.printStackTrace();
		}
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Connecting to Cabinet.");
		int configReadStatus = readConfig();
		
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("configReadStatus "+configReadStatus);
		if(configReadStatus !=0)
		{
			DBO_SignCrop_Logs.DBO_SignCropLogger.error("Could not Read Config Properties: DBO_SignCrop.properties");
			return;
		}
		
		cabinetName = CommonConnection.getCabinetName();
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Cabinet Name: " + cabinetName);
		
		jtsIP = CommonConnection.getJTSIP();
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("JTSIP: " + jtsIP);
		
		jtsPort = CommonConnection.getJTSPort();
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("JTSPORT: " + jtsPort);
		
		smsPort = CommonConnection.getsSMSPort();
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SMSPort: " + smsPort);
		
		sleepIntervalInMin=Integer.parseInt(DBO_SignCrop_cofigParamMap.get("SleepIntervalInMin"));
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);
		
		volumeID=DBO_SignCrop_cofigParamMap.get("VolumeID");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("VolumeID: " + volumeID);
		
		queueID=DBO_SignCrop_cofigParamMap.get("queueID");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("queueID: " + queueID);
				
		SegregatedDocument_Success=DBO_SignCrop_cofigParamMap.get("SegregatedDocument_Success");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SegregatedDocument_Success: " + SegregatedDocument_Success);
		
		SegregatedDocument_Input=DBO_SignCrop_cofigParamMap.get("SegregatedDocument_Input");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SegregatedDocument_Input: " + SegregatedDocument_Input);
		
		SegregatedDocument_Error=DBO_SignCrop_cofigParamMap.get("SegregatedDocument_Error");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SegregatedDocument_Error: " + SegregatedDocument_Error);
		
		ReadyToAttach_Error=DBO_SignCrop_cofigParamMap.get("ReadyToAttach_Error");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ReadyToAttach_Error: " + ReadyToAttach_Error);
		
		ReadyToAttach_Input=DBO_SignCrop_cofigParamMap.get("ReadyToAttach_Input");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ReadyToAttach_Input: " + ReadyToAttach_Input);
		
		ReadyToAttach_Success=DBO_SignCrop_cofigParamMap.get("ReadyToAttach_Success");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ReadyToAttach_Success: " + ReadyToAttach_Success);
	
		sessionId = CommonConnection.getSessionID(DBO_SignCrop_Logs.DBO_SignCropLogger, false);
		if(sessionId.trim().equalsIgnoreCase(""))
		{
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Could Not Connect to Server!");
		}
		else{
			try {
				DocMoveToSegregatedLocation();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("DocMoveToSegregatedLocation..");
		}
	}
	
	private void DocMoveToSegregatedLocation() throws IOException{
		
		now = new Date();
		Format formatter = new SimpleDateFormat("dd-MMM-yy");
		DATE = formatter.format(now);
		
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("DATE "+DATE);
		
		File folder = new File(SegregatedDocument_Input);
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("folder "+folder);
		File filesinFolder[] = folder.listFiles();
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("files "+filesinFolder);

		if(filesinFolder.length == 0){
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("No Files in the Signed Document folder from Control-M to move.");
		}else{
			
			for(int i=filesinFolder.length-1;i>=0;i--)	
			{
				String file = filesinFolder[i].getName();
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file Name: "+filesinFolder[i].getName());
				String TempsourcePath = "";
				String TempfailPath = "";
				String TempmovedPath = "";
				
				TempsourcePath = SegregatedDocument_Input + File.separator + file;
				TempfailPath = SegregatedDocument_Error + File.separator + file;
				TempmovedPath = SegregatedDocument_Success + File.separator + file;
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file TempsourcePath: "+TempsourcePath);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file TempfailPath: "+TempfailPath);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file TempmovedPath: "+TempmovedPath);
				
				/*
				QRCodePOC.readQRcodePdf(TempsourcePath);
				Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();  
				QRCodePOC.readQRcodePdf(TempsourcePath);
				try {
					QRCodePOC.readpdfscanned(TempsourcePath, hintMap);
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (ChecksumException e) {
					e.printStackTrace();
				} catch (FormatException e) {
					e.printStackTrace();
				}
				*/
				processInstanceID="DBO-01-Process";
				AWBNo="AWB02";
				
				// Inside the SegregatedDocument_Success Folder check if the same processInstanceID Folder exist.
				
				File SegDocSuccessFolder = new File(SegregatedDocument_Success);
				String NewFileInSegregatedDoc_Success=SegregatedDocument_Success + File.separator + processInstanceID;
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file NewFileInSegregatedDocument_Success: "+NewFileInSegregatedDoc_Success);
				File NewFile_W_WINAME = new File(NewFileInSegregatedDoc_Success);
				if(NewFile_W_WINAME.exists() && NewFile_W_WINAME.isDirectory()){
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Folder with same ProcessInstanceID exisit already.");
					String NewFileInSegDocSuc_AWBNO=SegregatedDocument_Success + File.separator + processInstanceID + File.separator +AWBNo;
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("If processInstanceID Folder exisit then create NewFileInSegDocSuc_AWBNO: "+NewFileInSegDocSuc_AWBNO);
					File NewFile_W_AWB = new File(NewFileInSegDocSuc_AWBNO);
					boolean bool_AWB = NewFile_W_AWB.mkdirs();
					if(bool_AWB){
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("AWB Folder Created Successfully in processInstanceID existing folder "+ AWBNo);
						TempmovedPath = NewFileInSegDocSuc_AWBNO + File.separator + file;
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("AWB Folder Created Successfully in processInstanceID existing folder with TempmovedPath to move"+ TempmovedPath);
						Files.move(TempsourcePath, TempmovedPath, StandardCopyOption.REPLACE_EXISTING);
					}
					else{
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("AWB Folder NOT Created Successfully in processInstanceID existing folder "+ AWBNo);
					}
				}
				else{
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Folder with same ProcessInstanceID NOT exisit already.");
					boolean bool = NewFile_W_WINAME.mkdirs();
					if(bool){
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("New Folder Created Successfully "+processInstanceID);
						String NewFileInSegDocSuc_AWBNO=SegregatedDocument_Success + File.separator + processInstanceID + File.separator +AWBNo;
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("If processInstanceID Folder not exist then create NewFileInSegDocSuc_AWBNO: "+NewFileInSegDocSuc_AWBNO);
						File NewFile_W_AWB = new File(NewFileInSegDocSuc_AWBNO);
						boolean bool_AWB = NewFile_W_AWB.mkdirs();
						if(bool_AWB){
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("AWB Folder Created Successfully in processInstanceID new folder"+ AWBNo);
							TempmovedPath = NewFileInSegDocSuc_AWBNO + File.separator + file;
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("AWB Folder Created Successfully in processInstanceID new folder with TempmovedPath to move"+ AWBNo);
						}
						else{
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("AWB Folder NOT Created Successfully in processInstanceID new folder"+ AWBNo);
						}
					}
					else{
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Folder NOT Created Successfully "+processInstanceID);
					}
				}		
			}
		}		
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

		// The directory is now empty so delete it
		return dir.delete();
	}
	
	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DBO_SignCrop.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			{
			    String name = (String) names.nextElement();
			    DBO_SignCrop_cofigParamMap.put(name, p.getProperty(name));
			}
		}
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}
	
	public String signUploadUtility(){
		String FilePath="C:\\Users\\xkhritik\\Desktop\\New folder";
		File folder = new File(FilePath);  //RAKFolder
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles)
		{
			String foldername = file.getName();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("foldername: "+foldername);
			String path = file.getAbsolutePath();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("path: "+path);
			
			File documentFolder = null;
			documentFolder = new File(path);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("documentFolder "+documentFolder);
			File[] listOfDocument = documentFolder.listFiles();
			for (File listOfDoc : listOfDocument)
			{
				if (listOfDoc.isFile()){
					String strfullFileName = listOfDoc.getName();
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("strfullFileName: "+strfullFileName);
					String strDocumentName = strfullFileName.substring(0,strfullFileName.lastIndexOf("."));
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("strDocumentName: "+strDocumentName);
					base64String = convertToBase64(path.toString()+"\\"+strfullFileName);
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("base64String: "+base64String);
					
					String finalString = getSignatureUploadXML(base64String,Account_No,CIF_ID,DATE,CustomerName,Sig_Remarks);
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("finalString: "+finalString);
					
					String integrationStatus="Success";
					String attributesTag;
					String ErrDesc = "";
								
					HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, jtsIP, jtsPort,sessionId); 
					integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, jtsIP, jtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
					
					XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
				    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
				    DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
				    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
				    DBO_SignCrop_Logs.DBO_SignCropLogger.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
					String MsgId ="";
				    if (integrationStatus.contains("<MessageId>"))
				    	MsgId = xmlParserSocketDetails.getValueOf("MessageId");
					
				    DBO_SignCrop_Logs.DBO_SignCropLogger.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);
				    if("0000".equalsIgnoreCase(return_code)){
				    	integrationStatus="Success";
				    	ErrDesc = "Signature Upload Successfully";
				    }
				    else{
						ErrDesc = return_desc;
					}
				}
			}
		}
		return msg;
	}
	
	public static String convertToBase64(String filePath) {
		String retValue = "";

		try {
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("inside convertToBase64 method");
			File file = new File(filePath);

			FileInputStream fis = new FileInputStream(file);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			long size = 0;

			try {
				for (int readNum; (readNum = fis.read(buf)) != -1;) {
					// Writes to this byte array output stream
					bos.write(buf, 0, readNum);
					// out.println("read " + readNum + " bytes,");
					size = size + readNum;
				}

				byte[] encodedBytes = Base64.encodeBase64(bos.toByteArray());
				String sEncodedBytes = new String(encodedBytes);

				retValue = sEncodedBytes;
				// WriteLog("Base64 string..:" +retValue);
			} catch (IOException ex) {
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Error converting to Base64:" + ex.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retValue;
	}
	public String getSignatureUploadXML(String base64String,String ACCNO,String CIFID,String DATE, String CustomerName, String Sig_Remarks)
	{
		java.util.Date d1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
		String DateExtra2 = sdf1.format(d1)+"+04:00";
		
		Sig_Remarks="To Sign Singly";
		String integrationXML = "<EE_EAI_MESSAGE>" +
			   "<EE_EAI_HEADER>" +
				  "<MsgFormat>SIGNATURE_ADDITION_REQ</MsgFormat>" +
				  "<MsgVersion>0001</MsgVersion>" +
				  "<RequestorChannelId>BPM</RequestorChannelId>" +
				  "<RequestorUserId>RAKUSER</RequestorUserId>" +
				  "<RequestorLanguage>E</RequestorLanguage>" +
				  "<RequestorSecurityInfo>secure</RequestorSecurityInfo>" +
				  "<ReturnCode>911</ReturnCode>" +
				  "<ReturnDesc>Issuer Timed Out</ReturnDesc>" +
				  "<MessageId>UniqueMessageId123</MessageId>" +
				  "<Extra1>REQ||SHELL.JOHN</Extra1>" +
				  "<Extra2>"+DateExtra2+"</Extra2>" +
			   "</EE_EAI_HEADER>" +
			   "<SignatureAddReq>" +
				  "<BankId>RAK</BankId>" +
				  "<AcctId>"+ACCNO+"</AcctId>" +
				  "<AccType>N</AccType>" +
				  "<CustId>"+CIFID+"</CustId>" +
				  "<BankCode></BankCode>" +
				  "<EmpId></EmpId>" +
				  "<CustomerName>"+CustomerName+"</CustomerName>" +
				  "<SignPowerNumber></SignPowerNumber>" +
				  "<ImageAccessCode>1</ImageAccessCode>" +
				  "<SignExpDate>2112-03-06T23:59:59.000</SignExpDate>" +
				  "<SignEffDate>2010-12-31T23:59:59.000</SignEffDate>" +
				  "<SignFile>"+base64String+"</SignFile>" +
				  "<PictureExpDate>2099-12-31T23:59:59.000</PictureExpDate>" +
				  "<PictureEffDate>2010-12-31T23:59:59.000</PictureEffDate>" +
				  "<PictureFile></PictureFile>" +
				  "<SignGroupId>SVSB11</SignGroupId>" +
				  "<Remarks>"+Sig_Remarks+"</Remarks>" +
			   "</SignatureAddReq>" +
			"</EE_EAI_MESSAGE>";

		return integrationXML;
	}
	
	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, String finalString)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DAO_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(finalString);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime){
		String outputResponseXML="";
		try{
			String QueryString = "select OUTPUT_XML from NG_DOB_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DBO_SignCrop_Logs.DBO_SignCropLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag) throws IOException, Exception {
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
}
	
	private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}
	
	
	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String processInstanceID, String ws_name,
	int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, String finalString){
		
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
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("userName "+ username);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0)){
    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Dout " + dout);
    			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Din " + din);
    			outputResponse = "";
    			
    			inputRequest = getRequestXML(cabinetName,sessionId ,processInstanceID, ws_name, username, finalString);

    			if (inputRequest != null && inputRequest.length() > 0){
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
    			}
    			byte[] readBuffer = new byte[500];
    			int num = din.read(readBuffer);
    			if (num > 0){
    				byte[] arrayBytes = new byte[num];
    				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
    				outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
    				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))
    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId, processInstanceID,outputResponse,integrationWaitTime );

    				if(outputResponse.contains("&lt;")){
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();
				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");
				return outputResponse;
    	 		}

    		else{
	    			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
	    			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
	    			DBO_SignCrop_Logs.DBO_SignCropLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
	    			return "Socket Details not maintained";
	    		}
	   		}

		catch (Exception e){
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
			return "";
		}
		finally{
			try{
				if(out != null){
					out.close();
					out=null;
				}
				if(socketInputStream != null){

					socketInputStream.close();
					socketInputStream=null;
				}
				if(dout != null){

					dout.close();
					dout=null;
				}
				if(din != null){

					din.close();
					din=null;
				}
				if(socket != null){
					if(!socket.isClosed())
					socket.close();
					socket=null;
				}

			}

			catch(Exception e)
			{	DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
			}
		}
	}
}