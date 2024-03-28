package com.newgen.iforms.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.integration.GetJSON;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;


@SuppressWarnings("unchecked")
public class DBOCommon {
	
private static NGEjbClient ngEjbClientDBOStatus;
	
	static
	{
	  try
      {
		  ngEjbClientDBOStatus = NGEjbClient.getSharedInstance();
      }
    catch (NGException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	}

    String sLocaleForMessage = java.util.Locale.getDefault().toString();

    public List < List < String >> getDataFromDB(String query,IFormReference iformObj) {
        DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", Inside Done()--->query is: " + query);
        try {
            List < List < String >> result = (iformObj).getDataFromDB(query);
            DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", Inside Done()---result:" + result);
            if (!result.isEmpty() && result.get(0) != null) {
                return result;
            }
        } catch (Exception e) {
            DBO.printException(e);
        }
        return null;
    }

    public String saveDataInDB(String query,IFormReference iformObj) {
        DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", Inside Done()---Exception_Mail_ID->query is: " + query);
        try {
            int mainCode = iformObj.saveDataInDB(query);
            DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", Inside Done()---result:" + mainCode);
            return mainCode + "";
        } catch (Exception e) {
            DBO.printException(e);
        }
        return null;
    }
    
    public String getUserGroup(String query,IFormReference iformObj) {
        DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", To get User Group Name ->query is: " + query);
        try {
        	  String groupName = "";
 			 // to get user group to insert in decision history start
 			try {
 				DBO.mLogger.info("To insert user group in decision history  ...");
 				DBO.mLogger.info("Query--" + query);
 				List<List<String>> lstDecisions = iformObj.getDataFromDB(query);
 				DBO.mLogger.info("Result  ..." + lstDecisions);
 				List<String> arr1 =  lstDecisions.get(0);
 				groupName = arr1.get(0);
 				DBO.mLogger.debug("value of groupName " + groupName);
 			} catch (Exception e) {
 				DBO.mLogger.debug("WINAME: " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj)
 						+ ", Exception in getting user group for decision history: " + e.getMessage());
 			}
 			
 		    // to insert user group in decision history end
            return groupName;
        } catch (Exception e) {
            DBO.printException(e);
        }
        return null;
    }
    
    
    
    
    //**********************************************************************************//
    //Description            	:Method to Trim Strings
    //**********************************************************************************//
    public String Trim(String str) {
        if (str == null) return str;
        int i = 0, j = 0;
        for (i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ')
                break;
        }
        for (j = str.length() - 1; j >= 0; j--) {
            if (str.charAt(j) != ' ')
                break;
        }
        if (j < i) j = i - 1;
        str = str.substring(i, j + 1);
        return str;
    }
    
    public String uniqueIDGeneration(String prefix){
    	
    	String timeStamp = "";
		Date date = new Date();
		
		String year = date.getYear() +1900 +"";
		String month = date.getMonth() +1 +"";
		month = Integer.parseInt(month) <= 9 ? "0" + month : month;
		String curDate = date.getDate() +"";
		curDate = Integer.parseInt(curDate) <= 9 ? "0" + curDate : curDate;
		
		String hours = date.getHours() +"";
		String minutes = date.getMinutes() +"";
		String seconds = date.getSeconds() +"";
		timeStamp = prefix + year + month + curDate + hours + minutes + seconds; 
		
		return timeStamp;
    }

    public void enableControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
            	iformObj.setStyle(arrFields[idx], "disable", "false");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }
    public void disableControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
            	iformObj.setStyle(arrFields[idx], "disable", "true");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }
    public void hideControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
                iformObj.setStyle(arrFields[idx], "visible", "false");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }
    public void showControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
                iformObj.setStyle(arrFields[idx], "visible", "true");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }
    public void mandatoryControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
                iformObj.setStyle(arrFields[idx], "mandatory", "true");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }
    public void nonmandatoryControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
                iformObj.setStyle(arrFields[idx], "mandatory", "false");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }

    

    public void lockControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
                iformObj.getIFormControl(arrFields[idx]).getM_objControlStyle().setM_strReadOnly("true");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }

    public void unlockControl(String strFields,IFormReference iformObj) {
        String arrFields[] = strFields.split(",");
        for (int idx = 0; idx < arrFields.length; idx++) {
            try {
                iformObj.getIFormControl(arrFields[idx]).getM_objControlStyle().setM_strReadOnly("false");
            } catch (Exception ex) {
                DBO.printException(ex);
            }
        }
    }

    public void clearCombos(String controlName,IFormReference iformObj) {
        String arrFields[] = controlName.split(",");
        for (int i = 0; i < arrFields.length; i++)
            try {
                iformObj.clearCombo(controlName);
            } catch (Exception e) {
                DBO.printException(e);
            }
    }
    public void clearCombo(String controlName,IFormReference iformObj) {
        try {
            iformObj.clearCombo(controlName);
        } catch (Exception e) {
            DBO.printException(e);
        }
    }

    public void addItemInCombo(String arg0, String arg1, String arg2,IFormReference iformObj) {
        try {
            iformObj.addItemInCombo(arg0, arg1, arg2);
        } catch (Exception e) {
            DBO.printException(e);
        }
    }

    public String getSessionId(IFormReference iformObj) {
        return ((iformObj).getObjGeneralData()).getM_strDMSSessionId();
    }

    public String getItemIndex(IFormReference iformObj) {
        return ((iformObj).getObjGeneralData()).getM_strFolderId();
    }

    public String getWorkitemName(IFormReference iformObj) {
        return ((iformObj).getObjGeneralData()).getM_strProcessInstanceId();
    }

    public void setControlValue(String controlName, String controlValue,IFormReference iformObj) {
        iformObj.setValue(controlName, controlValue);
    }

    public String getCabinetName(IFormReference iformObj) {
        return (String) iformObj.getCabinetName();
    }

    public String getUserName(IFormReference iformObj) {
        return (String) iformObj.getUserName();
    }

    public String getActivityName(IFormReference iformObj) {
        return (String) iformObj.getActivityName();
    }

    

    public String getControlValue(String controlName,IFormReference iformObj) {
        return (String) iformObj.getValue(controlName);
    }

    public boolean isControValueEmpty(String controlName,IFormReference iformObj) {
        String controlValue = getControlValue(controlName,iformObj);

        if (controlValue == null || controlValue.equals(""))
            return true;
        else
            return false;
    }
	 @SuppressWarnings("unchecked")
	 public void AddDataToChecKlistGrid(String query, String gridId,IFormReference iformObj)
	    {
	 	  try {
	 		  DBO.mLogger.debug("Inside AddDataToChecKlistGrid.." + gridId);
	 			 List < List < String >> result =getDataFromDB(query,iformObj);
	 			 DBO.mLogger.debug(" getDataFromDB---result:" + result);
	 	         JSONArray jsonArray=new JSONArray();
	 	         if (!result.isEmpty() && result.get(0) != null) {
	 	        	 int i=0;
	 	        	 List < String > options = new ArrayList<String>();
	 	        	 for (List < String > row :result)
	 				{
	 	        		 JSONObject obj=new JSONObject();
	 	        		 String description=row.get(0);
	 	        		// FPU.mLogger.info("Inside ChecklistValidations..description "+i+"- "+description);
	 	        		 options.add(row.get(1));
	 	        		// FPU.mLogger.info("Inside ChecklistValidations..options "+i+"- "+options);
	 	        		 obj.put("Description",description);
	 	        		 //obj.put("Option","");
	 	        		// obj.put("Remarks","");
	 	        		//FPU.mLogger.info("Inside ChecklistValidations..obj(Row data) "+i+"- "+obj);
	 	        		 jsonArray.add(obj);
	 	        		// FPU.mLogger.info("Inside ChecklistValidations..jsonArray "+i+"- "+jsonArray);
	 	        		 i++;
	               }
	 	        	 DBO.mLogger.info("Inside ChecklistValidations..Final Json Array - "+jsonArray);
	 	        	iformObj.addDataToGrid(gridId, jsonArray);
	 	        	// giformObj.getObjGeneralData()
	 	        	 DBO.mLogger.info("Inside ChecklistValidations..Add Options--"+options);
	 	        	 addOptionToGrid(options,i,gridId,iformObj);
	          	}
	      } catch (Exception e) {
	          DBO.printException(e);
	          DBO.mLogger.debug("Some Error occured in AddDataToChecKlistGrid .." + e.toString());
	      }
	    }
	 public void addLabelInChecklistOptions(String Query,int gridSizeCPV,String gridId,IFormReference iformObj)
		{
		 try {
			 	List lstDecisions = iformObj.getDataFromDB(Query);
				DBO.mLogger.info("Result  ..."+lstDecisions);
				List < String > options = new ArrayList<String>();
				for(int i=0;i<lstDecisions.size();i++)
				{
					List<String> arr1=(List)lstDecisions.get(i);
					String value=arr1.get(0);
					options.add(value);
					DBO.mLogger.info("Item to add in decision combo  ..."+value);
					
				}
				addOptionToGrid(options,gridSizeCPV,gridId,iformObj);
		 }
		 catch(Exception e)
		 {
			 DBO.printException(e);
	          DBO.mLogger.debug("Some Error occured in addLabelInChecklistOptions .." + e.toString());
		 }
			
		}


    public String convertDateFormat(String idate, String ipDateFormat, String opDateFormat, IFormReference iformObj,Locale...opLocale) {
        Locale defaultLocale = Locale.getDefault();

        DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", defaultLocale : " + defaultLocale);

        assert opLocale.length <= 1;
        Locale opDateFmtLocale = opLocale.length > 0 ? opLocale[0] : defaultLocale;

        DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", Loacle for output Date : " + opDateFmtLocale);
        try {
            if (idate == null) {
                return "";
            }
            if (idate.equalsIgnoreCase("")) {
                return "";
            }
            DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", idate :" + idate);
            String odate = "";
            DateFormat dfinput = new SimpleDateFormat(ipDateFormat);
            DateFormat dfoutput = new SimpleDateFormat(opDateFormat, opDateFmtLocale);

            Date dt = dfinput.parse(idate);
            DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", Indate " + dt);
            odate = dfoutput.format(dt);
            DBO.mLogger.debug("WINAME : " + getWorkitemName(iformObj) + ", WSNAME: " + getActivityName(iformObj) + ", Outdate " + odate);
            return odate;
        } catch (Exception e) {
            return "";
        }
    }

    public String getCurrentDate(String outputFormat) {
        String current_date = "";
        try {
            java.util.Calendar dateCreated1 = java.util.Calendar.getInstance();
            java.text.DateFormat df2 = new java.text.SimpleDateFormat(outputFormat);
            current_date = df2.format(dateCreated1.getTime());
        } catch (Exception e) {
            System.out.println("Exception in getting Current date :" + e);
        }
        return current_date;
    }
    public void loginfo(String msg) {
        DBO.mLogger.info(msg);
    }
    public void printException(Exception e) {
        DBO.printException(e);
    }

    public String makeIntegrationCall(IFormReference iformObj, String control, String stringData) {
        String status = "", exceptionOccured = "", callName = "";
        GetJSON getJSON = new GetJSON();
        JSONObject jsonRequest = null;
        JSONObject jsonResponse = null;
        String responseReceivedAt = "";
        String requestSentAt = "";
        String returnValue = "";
        if ("IntegrationCallBlacklist".equalsIgnoreCase(control)) {
            callName = "BlacklistCheck";
        }
        try {
            if (!"".equalsIgnoreCase(callName)) {
                jsonRequest = getJSON.getRequestJson(callName, iformObj);
                //jsonResponse = getJSON.connectURL(jsonRequest, callName);
                //responseReceivedAt = getCurrentTimeStamp();

            }
        } catch (Exception e) {
            printException(e);
            status = "fail";
            //exceptionOccured = Constants.exceptionOccured;
            DBO.mLogger.info("CallName : " + callName + " : " + status);
        }
        return returnValue;
    }
    public String getCurrentTimeStamp() {
        try {
            return new Timestamp(System.currentTimeMillis()).toString();
        } catch (Exception e) {
            printException(e);
            return "";
        }
    }
    public String logWIActivityName(IFormReference iformObj) {
        try {
            return getWorkitemName(iformObj) + " - " + getActivityName(iformObj) + " : ";
        } catch (Exception e) {
            DBO.mLogger.info("Exception in logWIActivityName()");
            printException(e);
            return "";
        }
    }
    public String readFile(String path) {
        try {
            File xmlFile = new File(path);
            Reader fileReader = new FileReader(xmlFile);
            BufferedReader bufReader = new BufferedReader(fileReader);

            StringBuilder sb = new StringBuilder();
            String line = bufReader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = bufReader.readLine();
            }
            String xml2String = sb.toString();
            System.out.println("XML to String using BufferedReader : ");
            System.out.println(xml2String);

            bufReader.close();
            DBO.mLogger.info("readFile with Values: " + xml2String);
            return xml2String;
        } catch (Exception e) {
            DBO.mLogger.info("Exceptions: " + e);
            return "";
        }

    }
    public void responseInsertinDB(String call_name, String request, String response, String successindicator, String wi_name,IFormReference iformObj) {
        String currDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime()).toString();

        String Query = "insert into ng_FPU_xml_log_table(CALL_NAME,DATETIME_STAMP,REQUEST,RESPONSE,successIndicator,WI_NAME) values('" + call_name + "','" + currDate + "','" + request + "','" + response.replaceAll("'", "\"") + "','" + successindicator + "','" + wi_name + "')";

        saveDataInDB(Query,iformObj);
    }
    

    public String Execute_Webservice(String EndpointURL, String inputXML, String username, String password) {
    
        String inputRequest = inputXML;
        String inputLine = "";
        String outputResponse = "";
        //String username=username;
        //String password=password;
        try {
            DBO.mLogger.info("The url value is" + EndpointURL);
            URL url =  new URL(null, EndpointURL, "");
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
           // url.openConnection();
            con.setRequestProperty("Authorization", authHeaderValue);
            con.setRequestProperty("Content-Type", "text/xml");
            con.setRequestProperty("SOAPAction", "urn:ADCBBPMNATID");
            con.setConnectTimeout(150000);
            con.setReadTimeout(150000);
            con.setRequestMethod("POST");
            byte[] postData = inputRequest.getBytes();
            DBO.mLogger.info("The postData value is" + postData);
            OutputStream out = con.getOutputStream();
            DBO.mLogger.info("The out value is" + out);
            out.write(postData);
            out.close();
            int ResponseCode = con.getResponseCode();
            DBO.mLogger.info("Respone code value is " + ResponseCode);
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getResponseCode() == 200 ? con.getInputStream() : con.getErrorStream()));
            StringBuffer content = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            if (ResponseCode == 200) {
                outputResponse = content.toString();
                DBO.mLogger.info("outputResponse" + outputResponse);
            } else {
                outputResponse = content.toString();
                DBO.mLogger.info("error response" + con.getErrorStream());
                DBO.mLogger.info("outputResponse" + outputResponse);
            }

        } catch (Exception ex) {
            DBO.mLogger.info("Error in Execute_Webservice " + ex.getMessage());
            DBO.mLogger.info("Error in Execute_Webservice 2 " + ex.getStackTrace());
        }
        return outputResponse;
    
    }
    
   
    
 public static String getASCIIValuesFromArabic(String inputParam) throws ParseException{
	 String retValue="";
 	if(inputParam.isEmpty()) {
 		return retValue;
 	}
 	
 	String[] scanArray = inputParam.split("");
		for(int i=0;i<scanArray.length;i++)
		{ 
			// int x=0;
			// x = i NumberFormat.getInstance().parse(scanArray[i]).intValue();
			String x = scanArray[i];
			//if(x>194)
			//{
			switch(x)
			
			{
				case " ":
				retValue+= "32";
				break;

				case "ا":
				retValue+=   "199";
				break;
				
				case "ب":
				retValue+= "200";
				break;
				
				case "ت":
				retValue+= "202";
				break;
				
				case "ث":
				retValue+= "203";
				break;
				
				case "ج":
				retValue+= "204";
				break;
				
				case  "ح":
				retValue+="205";
				break;
				
				case "خ":
				retValue+= "206";
				break;
				
				case "د":
				retValue+= "207";
				break;
				
				case "ذ":
				retValue+= "208";
				break;
				
				case "ر":
				retValue+= "209";
				
				break;
				
				case "ز":
				retValue+= "210";
				break;
				
				case "س":
				retValue+= "211";
				break;
				
				case "ش":
				retValue+= "212";
				break;
				
				case "ص":
				retValue+= "213";
				break;
				
				case "ض":
				retValue+= "214";
				break;
				
				case "ط":
				retValue+= "216";
				break;
				
				case "ظ":
				retValue+= "217";
				break;
				
				case "ع":
				retValue+= "218";
				break;
				
				case "غ":
				retValue+= "219";
				break;
				
				case "�?":
				retValue+= "221";
				break;
				
				case "ق":
				retValue+= "222";
				break;
				
				case "ك":
				retValue+= "223";
				break;
				
				case "ل":
				retValue+= "225";
				break;
				
				case "م":
				retValue+= "227";
				break;
				
				case "ن":
				retValue+= "228";
				break;
				
				case "ه":
				retValue+= "229";
				break;
				
				case "و":
				retValue+= "230";
				break;
				
				case "ي":
				retValue+= "237";
				break;
				
				case "آ":
				retValue+= "194";
				break;
				
				case "ة":
				retValue+= "201";
				break;
				
				case  "ى":
				retValue+="236";
				break;
			
				case  "ؤ":
				retValue+="196";
				break;
				
				case "ئ":
				retValue+= "198";
				break;
				
				// case 220:
				// break;
				case "أ":
				retValue+= "195";
				break;
				
				case "إ":
				retValue+= "197";
				
				break;			 
				
				default:
					retValue+= "";
				break;
				
			}
				if(i < scanArray.length - 1) {
					retValue+= "-";
				}
			}
		
		return retValue;
		
	
 }
    


 
 public static String GetTagValue(String XML , String Tagname)
 {
	 String starttag = "<"+Tagname+">";
	 String endtag = "</"+Tagname+">";
	 DBO.mLogger.info("GetTagValue " + starttag);
	 if(XML.indexOf(starttag)>=0)
	 {
		 if("MATURITYDATE".equals(Tagname))
		 {
			 String date = XML.substring(XML.indexOf(starttag)+(starttag.length()),XML.indexOf(endtag));
			 return date.substring(6,8)+date.substring(4,6)+date.substring(0,4);
		 }
	 return XML.substring(XML.indexOf(starttag)+(starttag.length()),XML.indexOf(endtag));
	 }
	 else
	 {
		 return "";
	 }
 }
 
 public String calculateEMI(String LoanAMT , int noofpayemntsinayear , String Rate,String noOfinstallments)
 {
	 try
	 {
		 DBO.mLogger.info("Inside Calculate EMI ");
	 DecimalFormat df = new DecimalFormat("0.00");
	 double P = Double.parseDouble(LoanAMT);
	  //double m = Double.parseDouble(noofpayemntsinayear);
	    double R = ((Double.parseDouble(Rate)/100)/noofpayemntsinayear);
	    int n=Integer.parseInt(noOfinstallments);
	    double EMI = (P * R) / (1 - Math.pow((1+R), -n));
	    DBO.mLogger.info(" EMI will be for given parameters is-"+EMI);
	    return ""+df.format(EMI);
	 }
	 catch(Exception ex)
	 {
		 DBO.mLogger.info("Exception in  calculateEMI " + ex.getMessage());
		 return "";
	 }
	 
 }
 @SuppressWarnings("unchecked")
public void addOptionToGrid(List < String > options,int noOfRows,String tableId,IFormReference iformObj)
  {
	  try {
			DBO.mLogger.debug("Inside addOptionToGrid.." );
	         
			if (!options.isEmpty() && options.get(0) != null) 
			{
				for (int i=0;i<noOfRows;i++)
				{
					String optionDBOr[]=options.get(i).split(",");
					DBO.mLogger.info("Inside addOptionToGrid..options "+i+"- "+optionDBOr);
						for(String opt:optionDBOr)
						{ /*
							String desc=iformObj.getTableCellValue(tableId, i, 0);
							FPU.mLogger.info("Description in rwoindex "+i+" -"+desc);*/
							//FPU.mLogger.info("Option in rwoindex "+i+" -"+opt);
							iformObj.addItemInTableCellCombo(tableId,i,1,opt,opt);
							//FPU.mLogger.info("After adding Option in rwoindex "+i+" -"+opt);
						}
					DBO.mLogger.info("Addeded..options for rwoindex"+i);
             }
        	}
    } 
	  catch (Exception e)
	  {
		  DBO.mLogger.debug("Some Error occured in addOptionToGrid .." + e.toString());
		  DBO.printException(e);
     }
  }
 public String tatHours(String EntryDateTime) {
     
     try {
    	 String newEntryDateTime="";
			if(!EntryDateTime.equals(""))
			{
				 newEntryDateTime=EntryDateTime.substring(0, EntryDateTime.length()-3);
				 DBO.mLogger.info("newEntryDateTime -"+newEntryDateTime);
				 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	 DBO.mLogger.info("getdateCurrentDateInSQLFormat -"+getdateCurrentDateInSQLFormat());
		    	 Date d1 = format.parse(getdateCurrentDateInSQLFormat());
		    	 Date d2 = format.parse(newEntryDateTime);//2021-12-19 16:57:34
		    	 DBO.mLogger.info("getdateCurrentDate after parsing -"+d1);
		    	 DBO.mLogger.info("newEntryDateTime after parsing -"+d2);
		    	 long age = (d1.getTime() / 1000 / 60 / 60) - (d2.getTime() / 1000 / 60 / 60);
		    	 DBO.mLogger.info("age is :-"+age);
		    	 return Long.toString(age);
			}
			return "";
    	 
     } catch (Exception ex) {
    	 DBO.mLogger.info("Exception in tatHours -"+ex);
         return null;
     }
 
}
 public String getdateCurrentDateInSQLFormat()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return simpleDateFormat.format(new Date());
	}
 public String tatStatus(String tathours) {
	 try {
    	 
			if(!tathours.equals(""))
			{
				DBO.mLogger.info("Total tat Hours "+ tathours);
				long ageing=Long.parseLong(tathours);
				if (ageing <= 48)
					return "On Time";
				else if (ageing > 48) {
					return "Exceeds TAT";
					}
			}
		
  } 
	 catch (Exception ex) {
 	 DBO.mLogger.debug("Some Error occured in tatStatus .." + ex.toString());
      DBO.printException(ex);
     
  }
  return null;
 
}
 public String ageing(String tathours) {
     
     try {
    	 
			if(!tathours.equals(""))
			{
				DBO.mLogger.info("Total tat Hours "+ tathours);
				long ageing=Long.parseLong(tathours)/24;
				if (ageing == 0 || ageing == 1 || ageing == 2)
					return "0-2";
				else if (ageing == 3 || ageing == 4)
					return "3-4";
				else if (ageing >= 5)
					return ">=5";
			}
		
     } catch (Exception ex) {
    	 DBO.mLogger.debug("Some Error occured in ageing .." + ex.toString());
         DBO.printException(ex);
        
     }
     return null;
}
 public static String dateIncremnter() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		//Getting current date
		Calendar cal = Calendar.getInstance();
		//Displaying current date in the desired format
		System.out.println("Current Date: "+sdf.format(cal.getTime()));
		   
		//Number of Days to add
	        cal.add(Calendar.DAY_OF_MONTH, 2);  
		//Date after adding the days to the current date
		String newDate = sdf.format(cal.getTime());  
		//Displaying the new Date after addition of Days to current date
		System.out.println("Date after Addition: "+newDate);
		return newDate;
	}
 //Process Specific Common Methods
 
 
 public String setCutOffDate(String str, IFormReference iform) {
	 String rs = null;
	 String date = null;
	 try {
	 String query = "SELECT CONST_FIELD_VALUE FROM USR_0_BPM_CONSTANTS WITH(nolock) WHERE CONST_FIELD_NAME='DBO_Cut_Off_Date'";
		List<List<String>> data = getDataFromDB(query, iform);
		if(!data.isEmpty()) {
			date = data.get(0).get(0);
		}
		Date dt = addOneMonth(str);
	 	 DBO.mLogger.info("Date" + dt.toString());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy HH:mm:ss");
		  rs = sdf.format(dt);
	 	 DBO.mLogger.info("Date" + rs.toString());
		rs = date+"/"+rs;
	 }catch(Exception e) {
		 
	 }
	 
	 return rs;
 }
 
 public static String populatePendingZeroAccDatainTableFromMasterData (IFormReference iform)
 {
	 
			try {
				
				String departMent=(String) iform.getValue("Department");
				String sumissionTeam=(String) iform.getValue("Submission_Team");
				String month_year=(String) iform.getValue("Submission_Month");
				String sub = "Pending";
				String queryForZeroBal = "SELECT SA.Account_Number FROM USR_0_DBO_Suspense_Account_Master AS SA\r\n"
						+ "JOIN USR_0_DBO_Acc_to_Acc_Owner_Mapping_Mstr AS AOM ON AOM.Account_Number=SA.Account_Number\r\n"
						+ "LEFT JOIN USR_0_DBO_Monthly_Acc_Bal_Master AS MAB ON SA.Account_Number=MAB.Account_Number\r\n"
						+ "LEFT JOIN USR_0_DBO_GRID_PENDING_ACC_ZERO_BAL AS DBO ON DBO.AccountNumber= SA.Account_Number\r\n"
						+ "WHERE (DBO.Submission IS NULL OR DBO.Submission = 'Pending') AND AOM.Department='"+departMent+"' AND MAB.Balance IS NULL"; 
				
				JSONArray jsonArray = new JSONArray();
				
				
				DBO.mLogger.info("Pending account zero bal table :"+ queryForZeroBal);
				
				List<List<String>> data = iform.getDataFromDB(queryForZeroBal);
				DBO.mLogger.info("Pending account zero bal table :"+ queryForZeroBal);
				
				if(!data.isEmpty()) {
					
					for(int i=0 ; i<data.size();i++) {
						
						JSONObject obj = new JSONObject();
						String AccNo = data.get(i).get(0);
						String bal="0.00";
						
						obj.put("Account_Number", AccNo);
						obj.put("Balance", bal);
						obj.put("Month_(Mon-YY)", month_year);
						obj.put("Submission", sub);
						
						jsonArray.add(obj);
						
					}
					DBO.mLogger.info("Pending account zero bal grid jason :"+ jsonArray);
					iform.addDataToGrid("GR_PENDING_ACC_ZERO_BAL", jsonArray); 
				}
				/*
				 * iform.setColumnDisable("GR_PENDING_ACC_ZERO_BAL", "0", true);
				 * iform.setColumnDisable("GR_PENDING_ACC_ZERO_BAL", "1", true);
				 * iform.setColumnDisable("GR_PENDING_ACC_ZERO_BAL", "2", true);
				 */
				
			}catch(Exception exc){
				DBO.printException(exc);
				DBO.mLogger.debug( "Exception 2 - "+exc);
			}
	 return "";
 }
 
 
 
 public static String populateSubmittedZeroAccDatainTableFromMasterData (IFormReference iform)
 {
	 
			try {
				String departMent=(String) iform.getValue("Department");
				String sumissionTeam=(String) iform.getValue("Submission_Team");
				String month_year=(String) iform.getValue("Submission_Month");
				String queryForZeroBal = "SELECT DBO.AccountNumber,DBO.Balance,ext.Submission_Month,ext.Submission_Date,DBO.Verified,ext.Curr_WS\r\n"
						+ "FROM USR_0_DBO_GRID_PENDING_ACC_ZERO_BAL DBO\r\n"
						+ "JOIN RB_DBO_EXTTABLE AS ext on ext.WI_NAME=DBO.WI_NAME\r\n"
						+ "WHERE ext.Department='"+departMent+"' AND ext.Submission_Month='"+month_year+"' AND DBO.Submission='Submitted'"; 
				
				JSONArray jsonArray = new JSONArray();
				
				
				DBO.mLogger.info("Submitted account zero bal table :"+ queryForZeroBal);
				
				List<List<String>> data = iform.getDataFromDB(queryForZeroBal);
				DBO.mLogger.info("Submitted account zero bal table :"+ queryForZeroBal);
				
				if(!data.isEmpty()) {
					
					for(int i=0 ; i<data.size();i++) {
						
						JSONObject obj = new JSONObject();
						String AccNo = data.get(i).get(0);
						String bal= data.get(i).get(1);
						String month= data.get(i).get(2);
						String subDate= data.get(i).get(3);
						String verification= data.get(i).get(4);
						String currWs= data.get(i).get(5);
						
						obj.put("Account_Number", AccNo);
						obj.put("Balance", bal);
						obj.put("Month_(Mon-YY)", month);
						obj.put("Submitted on ", subDate);
						obj.put("Verified", verification);
						obj.put("Status", currWs);
						
						jsonArray.add(obj);
						
					}
					DBO.mLogger.info("Submitted account zero bal grid jason :"+ jsonArray);
					iform.addDataToGrid("GR_SUBMITTED_ACC_ZERO_BAL", jsonArray); 
				}
				/*
				 * iform.setColumnDisable("GR_SUBMITTED_ACC_ZERO_BAL", "0", true);
				 * iform.setColumnDisable("GR_SUBMITTED_ACC_ZERO_BAL", "1", true);
				 * iform.setColumnDisable("GR_SUBMITTED_ACC_ZERO_BAL", "2", true);
				 */
			}catch(Exception exc){
				DBO.printException(exc);
				DBO.mLogger.debug( "Exception 2 - "+exc);
			}
	 return "";
 }
 
 
 
 
 public static String populatePendingAccForReconTableFromMasterData (IFormReference iform)
 {
	 
			try {
				String departMent=(String) iform.getValue("Department");
				String sumissionTeam=(String) iform.getValue("Submission_Team");
				String month_year=(String) iform.getValue("Submission_Month");
				String sub = "Pending";
				String queryForPendAccBal = "SELECT MAB.Account_Number,MAB.Balance FROM USR_0_DBO_Monthly_Acc_Bal_Master AS MAB\r\n"
						+ "JOIN USR_0_DBO_Acc_to_Acc_Owner_Mapping_Mstr AS AOM ON AOM.Account_Number=MAB.Account_Number\r\n"
						+ "LEFT JOIN USR_0_DBO_GRID_PENDING_ACC_FOR_RECONCILATION AS DBO ON DBO.AccountNumber= MAB.Account_Number\r\n"
						+ "WHERE (DBO.Submission IS NULL OR DBO.Submission = 'Pending') and MAB.Month_year='"+month_year+"' AND AOM.Department='"+departMent+"'"; 
				
				JSONArray jsonArray = new JSONArray();
				
				
				DBO.mLogger.info("Pending Account for Reconcilliation table :"+ queryForPendAccBal);
				
				List<List<String>> data = iform.getDataFromDB(queryForPendAccBal);
				
				if(!data.isEmpty()) {
					
					for(int i=0 ; i<data.size();i++) {
						
						JSONObject obj = new JSONObject();
						String AccNo = data.get(i).get(0);
						String bal=data.get(i).get(1);						
						obj.put("Account_Number", AccNo);
						obj.put("Balance", bal);
						obj.put("Month_(Mon-YY)", month_year);
						obj.put("Submission", sub);
					
						jsonArray.add(obj);
						
					}
					DBO.mLogger.info("Pending Account for Reconcilliation grid jason :"+ jsonArray);
					iform.addDataToGrid("GR_PENDING_ACC_FOR_RECONCILATION", jsonArray); 
				}
				/*
				 * iform.setColumnDisable("GR_PENDING_ACC_FOR_RECONCILATION", "0", true);
				 * iform.setColumnDisable("GR_PENDING_ACC_FOR_RECONCILATION", "1", true);
				 * iform.setColumnDisable("GR_PENDING_ACC_FOR_RECONCILATION", "2", true);
				 */
				
			}catch(Exception exc){
				DBO.printException(exc);
				DBO.mLogger.info( "Exception 2 - "+exc);
			}
			
			
			
			
	 return "";
 }
 
 
 public static String populateSubmittedAccForReconTableFromMasterData (IFormReference iform)
 {
	 
			try {
				String departMent=(String) iform.getValue("Department");
				String sumissionTeam=(String) iform.getValue("Submission_Team");
				String month_year=(String) iform.getValue("Submission_Month");
				String queryForZeroBal = "SELECT DBO.AccountNumber,DBO.Balance,ext.Submission_Month,ext.Submission_Date,DBO.Verified,ext.Curr_WS\r\n"
						+ "FROM USR_0_DBO_GRID_PENDING_ACC_FOR_RECONCILATION DBO\r\n"
						+ "JOIN RB_DBO_EXTTABLE AS ext on ext.WI_NAME=DBO.WI_NAME\r\n"
						+ "WHERE ext.Department='"+departMent+"' AND ext.Submission_Month='"+month_year+"' AND DBO.Submission='Submitted'"; 
				
				JSONArray jsonArray = new JSONArray();
				
				
				DBO.mLogger.info("Submitted account for Reconcilliation bal table :"+ queryForZeroBal);
				
				List<List<String>> data = iform.getDataFromDB(queryForZeroBal);
				DBO.mLogger.info("Submitted account for Reconcilliation bal table :"+ queryForZeroBal);
				
				if(!data.isEmpty()) {
					
					for(int i=0 ; i<data.size();i++) {
						
						JSONObject obj = new JSONObject();
						String AccNo = data.get(i).get(0);
						String bal= data.get(i).get(1);
						String month= data.get(i).get(2);
						String subDate= data.get(i).get(3);
						String verification= data.get(i).get(4);
						String currWs= data.get(i).get(5);
						
						obj.put("Account_Number", AccNo);
						obj.put("Balance", bal);
						obj.put("Month_(Mon-YY)", month);
						obj.put("Submitted on ", subDate);
						obj.put("Verified", verification);
						obj.put("Status", currWs);
						
						jsonArray.add(obj);
						
					}
					DBO.mLogger.info("Submitted account for Reconcilliation grid jason :"+ jsonArray);
					iform.addDataToGrid("GR_SUBMITTED_ACC_FOR_RECONCILATION", jsonArray); 
				}
				/*
				 * iform.setColumnDisable("GR_SUBMITTED_ACC_FOR_RECONCILATION", "0", true);
				 * iform.setColumnDisable("GR_SUBMITTED_ACC_FOR_RECONCILATION", "1", true);
				 * iform.setColumnDisable("GR_SUBMITTED_ACC_FOR_RECONCILATION", "2", true);
				 */
				
			}catch(Exception exc){
				DBO.printException(exc);
				DBO.mLogger.debug( "Exception 2 - "+exc);
			}
	 return "";
 }
 
 
 public static Date addOneMonth(String str)
 {
 	Date date = new Date();
 	SimpleDateFormat formatter = new SimpleDateFormat("MMM-yy");
 	try {
 		date = formatter.parse(str);
 	}
 	catch (java.text.ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		DBO.mLogger.error( "Exception - "+e);
	}	
     Calendar cal = Calendar.getInstance();
     cal.setTime(date);
     cal.add(Calendar.MONTH, 1);
     return cal.getTime();
 } 
 public String copyDataintempTable(IFormReference iform,String TableId)
 {
	 try
	 {
		 	String departMent=(String) iform.getValue("Department");
			String sumissionTeam=(String) iform.getValue("Submission_Team");
			String month_year=(String) iform.getValue("Submission_Month");
			
			if(insertReqRespINDatabase(iform,departMent,month_year,TableId))
				return "SUCCESS";
	 }
	 catch(Exception e)
	 {
		 
	 }
	 return "FAIL";
 }
 
 
 
 public  boolean insertReqRespINDatabase(IFormReference iform,String department,String monthYear,String tableID)
 {
	 try
	 {
				
				 String params="'"+getWorkitemName(iform)+"'"
				 +",'" +department+"'"
				 +",'" +monthYear+"'"
				 +",'" +tableID+"'";
				 String inputXML = getAPProcedureInputXML(iform.getCabinetName(),getSessionId(iform),"NG_DBO_INSERT_DATA_INTO_CMPTBL",params);
				 //WriteLog("inputXML AP Procedure new params: "+params);
				 DBO.mLogger.info("inputXML AP Procedure XML Entry "+inputXML);
				 String sOutputXML=WFNGExecute(inputXML, iform.getServerIp(), iform.getServerPort());
				 DBO.mLogger.info("outputXML AP Procedure XML Entry "+sOutputXML);
				
				
				
				 if(sOutputXML.indexOf("<MainCode>0</MainCode>")>-1)
				 {
					 DBO.mLogger.info("inputXML AP Procedure Insert Successful");
					 return true;
				 }
				 else
				 {
					 DBO.mLogger.info("inputXML AP Procedure Insert Failed");
					 return false;
				 }
		}
		 catch(Exception e)
		 {
		 e.printStackTrace();
		 }
	 	return false;
 }
 private String getAPProcedureInputXML(String engineName,String sSessionId,String procName,String Params)
 {
	 StringBuffer bfrInputXML = new StringBuffer();
	 bfrInputXML.append("<?xml version=\"1.0\"?>\n");
	 bfrInputXML.append("<APProcedure_WithDBO_Input>\n");
	 bfrInputXML.append("<Option>APProcedure_WithDBO</Option>\n");
	 bfrInputXML.append("<ProcName>");
	 bfrInputXML.append(procName);
	 bfrInputXML.append("</ProcName>");
	 bfrInputXML.append("<Params>");
	 bfrInputXML.append(Params);
	 bfrInputXML.append("</Params>");
	 bfrInputXML.append("<EngineName>");
	 bfrInputXML.append(engineName);
	 bfrInputXML.append("</EngineName>");
	 bfrInputXML.append("<SessionId>");
	 bfrInputXML.append(sSessionId);
	 bfrInputXML.append("</SessionId>");
	 bfrInputXML.append("</APProcedure_WithDBO_Input>");
	 return bfrInputXML.toString();
 }
 
 protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort) throws IOException, Exception
	{
		DBO.mLogger.info("In WF NG Execute : " + serverPort);
		try
		{
			/*if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP,
						Integer.parseInt(serverPort), 1);
			else*/
				return ngEjbClientDBOStatus.makeCall(jtsServerIP, serverPort,
						"WebSphere", ipXML);
		}
		catch (Exception e)
		{
			DBO.mLogger.info("Exception Occured in WF NG Execute : "
					+ e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}

 public void setValueInField(String LoggedInUser , IFormReference iformObj) {
//parwez
		try {
			DBO.mLogger.info("To populate Department in department drop down  ...");
			String ReportingTeam = (String) iformObj.getValue("q_DBO_ReportingTeam");

			//Set value in Reporting Team 
			if(!ReportingTeam.isEmpty() || !"".equalsIgnoreCase(ReportingTeam) || ReportingTeam!= null) {

				String ReportingTeamQuery = "SELECT DISTINCT Reporting_Team FROM USR_0_DBO_ReconRepHierarchyParam "
						+ "WITH(nolock) WHERE Maker_Group_Name in (SELECT GroupName FROM PDBGroup WITH(nolock) "
						+ "WHERE GroupIndex IN (SELECT DISTINCT GroupIndex FROM PDBGroupMember WITH(nolock) "
						+ "WHERE UserIndex IN (SELECT DISTINCT UserIndex FROM PDBUser WITH(nolock) "
						+ "WHERE UserName = '"+LoggedInUser+"'))) ORDER BY Reporting_Team";

				DBO.mLogger.info("ReportingTeamQuery--" + ReportingTeamQuery);

				List lstOfReportingTeam = iformObj.getDataFromDB(ReportingTeamQuery);

				iformObj.clearCombo("q_DBO_Department");
				iformObj.clearCombo("q_DBO_ReportingTeam");
				iformObj.clearCombo("q_DBO_Acc_Owner");

				String reportingteam="";
				DBO.mLogger.info("size of reportingTeam :"+ lstOfReportingTeam.size());

				if(lstOfReportingTeam.size() == 1) {
					List<String> arr1 = (List) lstOfReportingTeam.get(0);
					reportingteam = arr1.get(0);
					iformObj.addItemInCombo("q_DBO_ReportingTeam", reportingteam, reportingteam);
					iformObj.setValue("q_DBO_ReportingTeam", reportingteam);
					fetchAccountOwner(reportingteam , iformObj);
					disableControl("q_DBO_ReportingTeam", iformObj);
					
				}
				else  
				{
					for (int i = 0; i < lstOfReportingTeam.size(); i++) {
						List<String> arr1 = (List) lstOfReportingTeam.get(i);
						reportingteam = arr1.get(0);
						iformObj.addItemInCombo("q_DBO_ReportingTeam", reportingteam, reportingteam);
						// enableControl("Department", iformObj);
					}
                  // iformObj.getValue("Submission_Team")
					enableControl("q_DBO_ReportingTeam", iformObj);
				}
			}								
				
		} 
		catch (Exception e) {
			DBO.mLogger.debug("Exception in Reporting drop down load: " + e.getMessage());
		}

	}

public  void fetchAccountOwner(String ReportingTeam , IFormReference iformObj){
	try{
		// Set value in Account Owner
		//String ReportingTeam= (String) iformObj.getValue("Submission_Team");
		String AccountOwner = "";
		String AccountOwnQuery = "SELECT DISTINCT Ac_owner FROM USR_0_DBO_ReconRepHierarchyParam WITH(nolock) "
				+ "WHERE Reporting_Team ='" + ReportingTeam + "'";

		DBO.mLogger.info("AccountOwnQuery--" + AccountOwnQuery);
		List lstOfAccountOwner = iformObj.getDataFromDB(AccountOwnQuery);

		DBO.mLogger.info("size of accountOwner :" + lstOfAccountOwner.size());
		if(lstOfAccountOwner.size() == 1) {
			List<String> arr1 = (List) lstOfAccountOwner.get(0);
			 AccountOwner = arr1.get(0);
			iformObj.addItemInCombo("q_DBO_Acc_Owner", AccountOwner, AccountOwner);
			iformObj.setValue("q_DBO_Acc_Owner", AccountOwner);
			fetchDepartment(ReportingTeam,AccountOwner , iformObj);
			disableControl("q_DBO_Acc_Owner", iformObj);
		}
		else if (lstOfAccountOwner.size() > 1) {
			for (int i = 0; i < lstOfAccountOwner.size(); i++) {
				List<String> arr1 = (List) lstOfAccountOwner.get(0);
				 AccountOwner = arr1.get(0);
				iformObj.addItemInCombo("q_DBO_Acc_Owner", AccountOwner, AccountOwner);
			}
				enableControl("q_DBO_Acc_Owner", iformObj);
				
		}
		
		
	} catch (Exception e) {
		DBO.mLogger.debug("Exception in AccountOwner drop down load: " + e.getMessage());
	}
				
}
public  String getWorkItemInput(String sCabinetName, String sessionID, String workItemName, String WorkItemID)
{
	StringBuffer ipXMLBuffer=new StringBuffer();

	ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
	ipXMLBuffer.append("<WMGetWorkItem_Input>\n");
	ipXMLBuffer.append("<Option>WMGetWorkItem</Option>\n");
	ipXMLBuffer.append("<EngineName>");
	ipXMLBuffer.append(sCabinetName);
	ipXMLBuffer.append("</EngineName>\n");
	ipXMLBuffer.append("<SessionId>");
	ipXMLBuffer.append(sessionID);
	ipXMLBuffer.append("</SessionId>\n");
	ipXMLBuffer.append("<ProcessInstanceId>");
	ipXMLBuffer.append(workItemName);
	ipXMLBuffer.append("</ProcessInstanceId>\n");
	ipXMLBuffer.append("<WorkItemId>");
	ipXMLBuffer.append(WorkItemID);
	ipXMLBuffer.append("</WorkItemId>\n");
	ipXMLBuffer.append("</WMGetWorkItem_Input>");

	return ipXMLBuffer.toString();
}
public  void fetchDepartment(String ReportingTeam,String AccountOwner , IFormReference iformObj){
	try{
		// Set value in Department
		String Department ="";
		//String ReportingTeam = (String) iformObj.getValue("Submission_Team");
		//String AccountOwner= (String) iformObj.getValue("q_DBO_Acc_Owner");
		String DepartmentQuery = "SELECT DISTINCT Department FROM USR_0_DBO_ReconRepHierarchyParam WHERE "
				+ "Reporting_Team = '"+ReportingTeam+"' AND Ac_owner = '"+AccountOwner+"'";

		DBO.mLogger.info("DepartmentQuery--" + DepartmentQuery);
		List lstOfDepartment = iformObj.getDataFromDB(DepartmentQuery);

		DBO.mLogger.info("size of List" + lstOfDepartment.size());
		
		  if(lstOfDepartment.size() == 1) { 
		 List<String> arr1 = (List) lstOfDepartment.get(0);
		 Department = arr1.get(0);
		  iformObj.addItemInCombo("q_DBO_Department", Department, Department);
		  iformObj.setValue("q_DBO_Department", Department);
		  disableControl("q_DBO_Department", iformObj);
		  }
		 
		  else if (lstOfDepartment.size() > 1) {
			for (int i = 0; i < lstOfDepartment.size(); i++) {
				List<String> arr1 = (List) lstOfDepartment.get(i);
				 Department = arr1.get(0);
				iformObj.addItemInCombo("q_DBO_Department",Department , Department);
			}
			enableControl("q_DBO_Department", iformObj);

		}
		
	} catch (Exception e) {
		DBO.mLogger.debug("Exception in AccountOwner drop down load: " + e.getMessage());
	}
}

}
