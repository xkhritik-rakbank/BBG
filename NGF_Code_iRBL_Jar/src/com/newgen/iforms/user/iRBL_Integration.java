package com.newgen.iforms.user;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.CharacterData;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
	
public class iRBL_Integration extends iRBL_Common 
{		
		LinkedHashMap<String,String> executeXMLMapMain = new LinkedHashMap<String,String>();
		public static String XMLLOG_HISTORY="NG_iRBL_XMLLOG_HISTORY";

	public String onclickevent(IFormReference iformObj,String control,String StringData) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException 
	{
		String MQ_response="";
		String inputXMLstate="";
		String outputXMLstate= "";
		String subXML="";
		String params="";
		String state_search="";
		String returnValue = "";
		String mainCodeValuestate = "";
		String wiName=getWorkitemName();
		String WSNAME=getActivityName();
		int returnedSignatures = 0;
		
		//open signature window containing the signature associated with account no. from account summary call
		 if(control.equalsIgnoreCase("btn_View_Signature"))
		 {
				String imageArr[] = null;
				String strCode =null;
				String remarksArr[] = null;
				String signGrpNameArr[] = null;
				String customerNameArr[] = null;
				String remarks="";
				String sigGroupName="";
				String CustomerNameSig="";
			    Properties p = new Properties();
				p.load(new FileInputStream(System.getProperty("user.dir")+ System.getProperty("file.separator")+ "CustomConfig"+System.getProperty("file.separator")+"RakBankConfig.properties"));
				String filePath = p.getProperty("iRBL_LoadImage");
				iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", filePath from OpenImage.jsp- "+filePath);
				String signMatchNeededAtChecker = "";
			 String acc_no = StringData;
			 iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", control is view signature..");
			 MQ_response = MQ_connection_response(iformObj,control,acc_no);
			//iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", Inside btn_View_Signature function "+MQ_response);
			MQ_response=MQ_response.substring(MQ_response.indexOf("<?xml v"),MQ_response.indexOf("</MQ_RESPONSE_XML>"));
			//iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", Inside btn_View_Signature function "+MQ_response);	
			
			Document doc=MapXML.getDocument(MQ_response);
			NodeList returnCode = doc.getElementsByTagName("ReturnCode");
			Element elementReturnCode = (Element) returnCode.item(0);
			strCode = getCharacterDataFromElement(elementReturnCode);
			
			NodeList returnDesc = doc.getElementsByTagName("ReturnDesc");
			Element elementReturnDesc = (Element) returnDesc.item(0);
			String strDesc = getCharacterDataFromElement(elementReturnDesc);
			
			if (strCode.equals("0000"))
			{
				NodeList nodesReturnedSignature = doc.getElementsByTagName("returnedSignature");
				NodeList nodesRemarksArr = doc.getElementsByTagName("remarks");
				NodeList nodesSignGrpNameArr = doc.getElementsByTagName("SignGrpName");
				NodeList nodesCustomerNameArr = doc.getElementsByTagName("CustomerName");
				
				//First Check if call is success or not
				
				//Initializing the Image Array
				returnedSignatures = nodesReturnedSignature.getLength();
				imageArr = new String [returnedSignatures];
				remarksArr = new String [returnedSignatures];
				signGrpNameArr = new String [returnedSignatures];
				customerNameArr = new String [returnedSignatures];
				
				iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", returnedSignatures - "+returnedSignatures);
				
				// iterate the returnedSignatures and put it in an array
				for (int i = 0; i < returnedSignatures; i++) {
					Element element = (Element) nodesReturnedSignature.item(i);
					Element element1 = (Element) nodesRemarksArr.item(i);
					Element element2 = (Element) nodesSignGrpNameArr.item(i);
					Element element3 = (Element) nodesCustomerNameArr.item(i);
					try{
						imageArr [i] = getCharacterDataFromElement(element);
						//iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", image value is --"+imageArr[i]);
						remarksArr [i] = getCharacterDataFromElement(element1);
						if(remarks.equals(""))
						 remarks = remarksArr [i];
						else
							remarks = remarks+","+remarksArr [i];
						iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", remarks value is --"+remarksArr[i]);
						
						signGrpNameArr [i] = getCharacterDataFromElement(element2);	
						iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", sig grp name value is --"+signGrpNameArr[i]);
						if(sigGroupName.equals(""))
							sigGroupName = signGrpNameArr [i];
							else
								sigGroupName = sigGroupName+","+signGrpNameArr [i];
						
						customerNameArr [i] = getCharacterDataFromElement(element3);
						//iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", customer name value is --"+imageArr[i]);
						if(CustomerNameSig.equals(""))
							CustomerNameSig = signGrpNameArr [i];
							else
								CustomerNameSig = CustomerNameSig+","+customerNameArr [i];
					}catch(Exception ex){
						iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", Exception 1 - "+ex);
					}
				}
			}
			
			if (strCode.equals("0000"))
			{
				 getImages(filePath,acc_no,imageArr,remarksArr);
				 return strCode+"~"+Integer.toString(returnedSignatures)+"~"+remarks+"~"+sigGroupName+"~"+CustomerNameSig;
			} else {
				
				return strCode+"~"+strDesc;
			}
			
			
		 }
		 else if(control.equalsIgnoreCase("ViewAECBReport"))
		 {
			 
			String sName =  iformObj.getTableCellValue("Q_USR_0_IRBL_EVAL_CHECKS_AECB_GRID_DTLS",Integer.parseInt(StringData), 0).trim(); // Name
			String RelatedPartyId =  iformObj.getTableCellValue("Q_USR_0_IRBL_EVAL_CHECKS_AECB_GRID_DTLS",Integer.parseInt(StringData), 7).trim(); // RelatedPartyId
			List lstReportUrls = iformObj
						.getDataFromDB("SELECT top 1 ReportURL FROM USR_0_iRBL_InternalExpo_Derived WITH(NOLOCK) WHERE Wi_Name='"+getWorkitemName()+"' and RelatedPartyId='"+RelatedPartyId+"'");
					
			String value="";
			for(int i=0;i<lstReportUrls.size();i++)
			{
				List<String> arr1=(List)lstReportUrls.get(i);
				value=arr1.get(0);
				iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", value : "+value);
				returnValue=value;
			}
			
			// if not available based on Related Party Id then checking based on Name
			if("".equalsIgnoreCase(returnValue.trim()))
			{
				lstReportUrls = iformObj
						.getDataFromDB("SELECT top 1 ReportURL FROM USR_0_iRBL_InternalExpo_Derived WITH(NOLOCK) WHERE Wi_Name='"+getWorkitemName()+"' and FullNm='"+sName+"'");
					
				value="";
				for(int i=0;i<lstReportUrls.size();i++)
				{
					List<String> arr1=(List)lstReportUrls.get(i);
					value=arr1.get(0);
					iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", value : "+value);
					returnValue=value;
				}
		 	}
			
		 }
		 return returnValue;		 
       }
                
        	
public String getImages(String tempImagePath,String debt_acc_num,String[] imageArr,String[] remarksArr)
{
	
	String strCode =null;
	StringBuilder html = new StringBuilder();
	if(imageArr==null)
		return "";
	for (int i=0;i<imageArr.length;i++)
	{
		try
		{	
			iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", Inside Get Images 0");
			byte[] btDataFile = new sun.misc.BASE64Decoder().decodeBuffer(imageArr[i]);
			//File of = new File(filePath+debt_acc_num+"imageCreatedN"+i+".jpg");
			String imagePath = System.getProperty("user.dir")+ tempImagePath+System.getProperty("file.separator")+debt_acc_num+"imageCreatedN"+i+".jpg";
			iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", imagePath"+imagePath);
			File of = new File(imagePath);
			
			FileOutputStream osf = new FileOutputStream(of);
			osf.write(btDataFile);
			osf.flush();
			osf.close();
		}
		catch (Exception e)
		{
			iRBL.mLogger.debug( e.getMessage());
			e.printStackTrace();
			iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", Not able to get the image imageCreated"+e);
		}
	}
	//WriteLog( html.toString());
	return "";
}
	public static String readFileFromServer(String filename)
	{	
		iRBL.mLogger.debug("inside readFileFromServer--"+filename);
		String xmlReturn="";
		try {
			File file = new File(filename);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
			fileReader.close();
			iRBL.mLogger.debug("Contents of file:");
			xmlReturn = stringBuffer.toString();
			iRBL.mLogger.debug("file content"+xmlReturn);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return xmlReturn;
	
	
	}
	
	public static String writeFileFromServer(String filename,String oldString)
	{
		 String newString=oldString;	
		iRBL.mLogger.debug("\n Inside writeFileFromServer function");
		
		try {
			 //newString = oldString.replaceAll("<p>", " ");
			 //newString = newString.replaceAll("</p>"," ");
            //Rewriting the input text file with newString
			
            FileOutputStream out = new FileOutputStream(filename);
			out.write(newString.getBytes());
			out.close();
			
			//iRBL.mLogger.debug("after writing into file"+newString);
				
		} catch (IOException e) {
			iRBL.mLogger.debug("The Exception is "+e.getMessage());
			e.printStackTrace();
		}
			return newString;
	}

	
	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
		   CharacterData cd = (CharacterData) child;
		   return cd.getData();
		}
		return "NO_DATA";
	}
	
	public String MQ_connection_response(IFormReference iformObj,String control,String Data) 
	{
		
		iRBL.mLogger.debug("Inside MQ_connection_response function");
		final IFormReference iFormOBJECT;
		final WDGeneralData wdgeneralObj;	
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String mqOutputResponse = null;
		String mqOutputResponse_Entity = null;
		String mqOutputResponse_Account = null;
		String mqInputRequest = null;
		String cabinetName = getCabinetName();
		String wi_name = getWorkitemName();
		String ws_name = getActivityName();
		String sessionID = getSessionId();
		String userName = getUserName();
		String socketServerIP;
		int socketServerPort;
		wdgeneralObj = iformObj.getObjGeneralData();
		sessionID = wdgeneralObj.getM_strDMSSessionId();
		String CIFNumber="";
		String EmiratesID ="";
		String AccountNumber="";
		String CardNumber="";
		String LoanAgreementID="";
		String Nikita="";
		
		if(control.equals("btn_View_Signature"))
		{
			iRBL.mLogger.debug("Inside btn_View_Signature control--"+Data);
			String acc_selected = Data;
			String selectedCIFNumber = getControlValue("CIF_NUMBER");
			iRBL.mLogger.debug("$$selectedCIFNumber "+selectedCIFNumber);
			StringBuilder finalXml = new StringBuilder("<EE_EAI_MESSAGE>\n"+
					"<EE_EAI_HEADER>\n"+
					"<MsgFormat>SIGNATURE_DETAILS</MsgFormat>\n"+
					"<MsgVersion>0001</MsgVersion>\n"+
					"<RequestorChannelId>BPM</RequestorChannelId>\n"+
					"<RequestorUserId>RAKUSER</RequestorUserId>\n"+
					"<RequestorLanguage>E</RequestorLanguage>\n"+
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n"+
					"<ReturnCode>911</ReturnCode>\n"+
					"<ReturnDesc>Issuer Timed Out</ReturnDesc>\n"+
					"<MessageId>MDL053169111</MessageId>\n"+
					"<Extra1>REQ||PERCOMER.PERCOMER</Extra1>\n"+
					"<Extra2>2007-01-01T10:30:30.000Z</Extra2>\n"+
					"</EE_EAI_HEADER>\n"+
					"<SignatureDetailsReq><BankId>RAK</BankId><CustId></CustId><AcctId>"+acc_selected+"</AcctId></SignatureDetailsReq>\n"+
					"</EE_EAI_MESSAGE>");
		mqInputRequest = getMQInputXML(sessionID, cabinetName,wi_name, ws_name, userName, finalXml);
		iRBL.mLogger.debug("$$outputgGridtXML "+"mqInputRequest for Signature Details call" + mqInputRequest);
		}
		
		try {
		
		//iRBL.mLogger.debug("userName "+ userName);
		//iRBL.mLogger.debug("sessionID "+ sessionID);
		
		String sMQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'iRBL' and CallingSource = 'Form'";
		List<List<String>> outputMQXML = iformObj.getDataFromDB(sMQuery);
		//CreditCard.mLogger.info("$$outputgGridtXML "+ "sMQuery " + sMQuery);
		if (!outputMQXML.isEmpty()) {
			//CreditCard.mLogger.info("$$outputgGridtXML "+ outputMQXML.get(0).get(0) + "," + outputMQXML.get(0).get(1));
			socketServerIP = outputMQXML.get(0).get(0);
			//iRBL.mLogger.debug("socketServerIP " + socketServerIP);
			socketServerPort = Integer.parseInt(outputMQXML.get(0).get(1));
			//iRBL.mLogger.debug("socketServerPort " + socketServerPort);
			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0)) {
				socket = new Socket(socketServerIP, socketServerPort);
				//new Code added by Deepak to set connection timeout
				int connection_timeout=60;
					try{
						connection_timeout=70;
						//connection_timeout = Integer.parseInt(NGFUserResourceMgr_CreditCard.getGlobalVar("Integration_Connection_Timeout"));
					}
					catch(Exception e){
						connection_timeout=60;
					}
					
				socket.setSoTimeout(connection_timeout*1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				iRBL.mLogger.debug("dout " + dout);
				iRBL.mLogger.debug("din " + din);
				mqOutputResponse = "";
				
		
				if (mqInputRequest != null && mqInputRequest.length() > 0) {
					int outPut_len = mqInputRequest.getBytes("UTF-16LE").length;
					iRBL.mLogger.debug("Final XML output len: "+outPut_len + "");
					mqInputRequest = outPut_len + "##8##;" + mqInputRequest;
					iRBL.mLogger.debug("MqInputRequest"+"Input Request Bytes : "+ mqInputRequest.getBytes("UTF-16LE"));
					dout.write(mqInputRequest.getBytes("UTF-16LE"));dout.flush();
				}
				byte[] readBuffer = new byte[500];
				int num = din.read(readBuffer);
				if (num > 0) {
		
					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					mqOutputResponse = mqOutputResponse+ new String(arrayBytes, "UTF-16LE");
					iRBL.mLogger.debug("mqOutputResponse/message ID :  "+mqOutputResponse);
					if(!"".equalsIgnoreCase(mqOutputResponse) && control.equalsIgnoreCase("btn_View_Signature"))
					{
						mqOutputResponse = getOutWtthMessageID("SIGNATURE_DETAILS",iformObj,mqOutputResponse);
					}
						
					if(mqOutputResponse.contains("&lt;")){
						mqOutputResponse=mqOutputResponse.replaceAll("&lt;", "<");
						mqOutputResponse=mqOutputResponse.replaceAll("&gt;", ">");
					}
				}
				socket.close();
				return mqOutputResponse;
				
			} else {
				iRBL.mLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
				iRBL.mLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
				iRBL.mLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
				return "MQ details not maintained";
			}
		} else {
			iRBL.mLogger.debug("SOcket details are not maintained in NG_RLOS_MQ_TABLE table"+"");
			return "MQ details not maintained";
		}
		
		} catch (Exception e) {
			iRBL.mLogger.debug("Exception Occurred Mq_connection_CC"+e.getStackTrace());
		return "";
		}
		finally
		{
			try
			{
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
					if(!socket.isClosed()){
						socket.close();
					}
					socket=null;
				}
			}catch(Exception e)
			{
				//		RLOS.mLogger.info("Exception occurred while closing socket");
				iRBL.mLogger.debug("Final Exception Occurred Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}
	}
	
	
	private static String getMQInputXML(String sessionID, String cabinetName,
			String wi_name, String ws_name, String userName,
			StringBuilder final_xml) {
			//FormContext.getCurrentInstance().getFormConfig();
			//iRBL.mLogger.debug("inside getMQInputXML function");
			StringBuffer strBuff = new StringBuffer();
			strBuff.append("<APMQPUTGET_Input>");
			strBuff.append("<SessionId>" + sessionID + "</SessionId>");
			strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
			strBuff.append("<XMLHISTORY_TABLENAME>NG_iRBL_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
			strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
			strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
			strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
			strBuff.append("<MQ_REQUEST_XML>");
			strBuff.append(final_xml);
			strBuff.append("</MQ_REQUEST_XML>");
			strBuff.append("</APMQPUTGET_Input>");
			//iRBL.mLogger.debug("inside getOutputXMLValues"+ "getMQInputXML"+ strBuff.toString());
			return strBuff.toString();
		}
		
	public String getOutWtthMessageID(String callName,IFormReference iformObj,String message_ID){
		String outputxml="";
		try{
			//String wi_name = iformObj.getWFWorkitemName();
			String wi_name = getWorkitemName();
			String str_query = "select OUTPUT_XML from NG_iRBL_XMLLOG_HISTORY with (nolock) where CALLNAME ='"+callName+"' and MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+wi_name+"'";
			iRBL.mLogger.debug("inside getOutWtthMessageID str_query: "+ str_query);
			List<List<String>> result=iformObj.getDataFromDB(str_query);
			//below code added by nikhil 18/10 for Connection timeout
			//String Integration_timeOut=NGFUserResourceMgr_CreditCard.getGlobalVar("Inegration_Wait_Count");
			String Integration_timeOut="100";
			int Loop_wait_count=10;
			try
			{
				Loop_wait_count=Integer.parseInt(Integration_timeOut);
			}
			catch(Exception ex)
			{
				Loop_wait_count=10;
			}
		
			for(int Loop_count=0;Loop_count<Loop_wait_count;Loop_count++){
				if(result.size()>0){
					outputxml = result.get(0).get(0);
					break;
				}
				else{
					Thread.sleep(1000);
					result=iformObj.getDataFromDB(str_query);
				}
			}
			
			if("".equalsIgnoreCase(outputxml)){
				outputxml="Error";
			}
			iRBL.mLogger.debug("getOutWtthMessageID" + outputxml);				
		}
		catch(Exception e){
			iRBL.mLogger.debug("Exception occurred in getOutWtthMessageID" + e.getMessage());
			iRBL.mLogger.debug("Exception occurred in getOutWtthMessageID" + e.getStackTrace());
			outputxml="Error";
		}
		return outputxml;
	}
	
	public Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException
	{
		// Step 1: create a DocumentBuilderFactory
		DocumentBuilderFactory dbf =
				DocumentBuilderFactory.newInstance();

		// Step 2: create a DocumentBuilder
		DocumentBuilder db = dbf.newDocumentBuilder();

		// Step 3: parse the input file to get a Document object
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		iRBL.mLogger.debug("xml is-"+xml);
		return doc;
	}
	
	public String maskXmlogBasedOnCallType(String outputxmlMasked, String callType)
	{
		String Tags = "";
		if (callType.equalsIgnoreCase("CUSTOMER_DETAILS"))
		{
			outputxmlMasked = outputxmlMasked.replace("("," ").replace(")"," ").replace("@"," ").replace("+"," ").replace("&amp;/"," ").replace("&amp; /"," ").replace("."," ").replace(","," ");
			Tags = "<ACCNumber>~,~<AccountName>~,~<ECRNumber>~,~<DOB>~,~<MothersName>~,~<IBANNumber>~,~<DocId>~,~<DocExpDt>~,~<DocIssDate>~,~<PassportNum>~,~<MotherMaidenName>~,~<LinkedDebitCardNumber>~,~<FirstName>~,~<MiddleName>~,~<LastName>~,~<FullName>~,~<ARMCode>~,~<ARMName>~,~<PhnCountryCode>~,~<PhnLocalCode>~,~<PhoneNo>~,~<EmailID>~,~<CustomerName>~,~<CustomerMobileNumber>~,~<PrimaryEmailId>~,~<Fax>~,~<AddressType>~,~<AddrLine1>~,~<AddrLine2>~,~<AddrLine3>~,~<AddrLine4>~,~<POBox>~,~<City>~,~<Country>~,~<AddressLine1>~,~<AddressLine2>~,~<AddressLine3>~,~<AddressLine4>~,~<CityCode>~,~<State>~,~<CountryCode>~,~<Nationality>~,~<ResidentCountry>~,~<PrimaryContactName>~,~<PrimaryContactNum>~,~<SecondaryContactName>~,~<SecondaryContactNum>";
			
		}

			else if (callType.equalsIgnoreCase("ACCOUNT_SUMMARY"))
		{
			outputxmlMasked = outputxmlMasked.replace("("," ").replace(")"," ").replace("@"," ").replace("+"," ").replace("&amp;/"," ").replace("&amp; /"," ").replace("."," ").replace(","," ");
			Tags = "<Acid>~,~<Foracid>~,~<NicName>~,~<AccountName>~,~<AcctBal>~,~<LoanAmtAED>~,~<AcctOpnDt>~,~<MaturityAmt>~,~<EffAvailableBal>~,~<EquivalentAmt>~,~<LedgerBalanceinAED>~,~<LedgerBalance>";
		}
		else if (callType.equalsIgnoreCase("SIGNATURE_DETAILS"))
		{
			outputxmlMasked = outputxmlMasked.replace("&amp;/"," ").replace("&amp; /"," ").replace("."," ").replace(","," ");
			Tags = "<CustomerName>";
		}
		if (!Tags.equalsIgnoreCase(""))
		{
	    	String Tag[] = Tags.split("~,~");
	    	for(int i=0;i<Tag.length;i++)
	    	{
	    		//outputxmlMasked = maskXmlTags(outputxmlMasked,Tag[i]);
	    	}
		}
    	return outputxmlMasked;
	}
	
}


