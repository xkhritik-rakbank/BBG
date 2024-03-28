/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK DBO Utility
Module					: DBO Process
File Name				: DBO_PrimeCBS_LogsCBS_File_Read.java
Author 					: om.tiwari
Date (DD/MM/YYYY)		: 09/03/2023

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/
package com.newgen.DBO.AWB_Delivery;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.DBO.SignatureCrop.DBO_SignCrop_Logs;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public class DBO_PrimeCBS_File_Read implements Runnable {


	private static NGEjbClient ngEjbClientPrimeCBS;
	Date now=null;
	public String sdate="";
	public static String sourceDestinaton = "";
	String primeFileInputPath="";
	String primeFileSuccessPath="";
	String primeFileFailPath="";
	String primeFileInprogressPath="";
	String primeCloumnName="";
	String primeDataTableName="USR_0_DBO_PRIME_FILE_DATA";
	String CBSFileInputpath="";
	String CBSFileSuccessPath="";
	String CBSFileFailPath="";
	String CBSFileInprogressPath="";
	String CBSColName="";
	String CBSDataTableName="USR_0_DBO_CBS_FILE_DATA";
	String AWBDataTableName="USR_0_DBO_AWB_Status";
	int socketConnectionTimeout=0;
	int integrationWaitTime=0;
	int sleepIntervalInMin=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	public static String fromMailID="";
	public static String toMailID = "";
	public static String mailSubject = "";
	public static String MailStr="";
	public static String jtsIP = "";
	public static String jtsPort = "";
	String cabinetName = "";
	public static String ProcessName="DBO";
	public static String ProcessDefId="";
	private String sessionID = "";
	public  String TimeStamp="";
	public String newFilename=null;
	private String  queueID="";

	static Map<String, String> DBOPrimeConfigParamMap= new HashMap<String, String>();

	@Override
	public void run()
	{
		int sleepIntervalInMin=0;
		try
		{
			DBO_PrimeCBS_Logs.setLogger();
			ngEjbClientPrimeCBS = NGEjbClient.getSharedInstance();

			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.error("Could not Read Config Properties [DBO_PrimeCBS_Logs]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("JTSPORT: " + jtsPort);

			sleepIntervalInMin=Integer.parseInt(DBOPrimeConfigParamMap.get("SleepIntervalInMin"));
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			primeCloumnName=DBOPrimeConfigParamMap.get("primeCloumnName");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("primeCloumnName: "+primeCloumnName);
			
			
			CBSFileInputpath=DBOPrimeConfigParamMap.get("CbsInput");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("CBSfilepath: "+CBSFileInputpath);
			
			CBSFileSuccessPath=DBOPrimeConfigParamMap.get("CbsSuccess");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("CbsSuccess: "+CBSFileSuccessPath);
			
			CBSFileFailPath=DBOPrimeConfigParamMap.get("CbsFail");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("CbsFail: "+CBSFileFailPath);
			
			CBSColName=DBOPrimeConfigParamMap.get("CBSColName");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("CBSColName: "+CBSColName);
			
			CBSFileInprogressPath=DBOPrimeConfigParamMap.get("CbsInprogresspath");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("CbsInprogresspaths: "+CBSFileInprogressPath);
			
			primeFileInputPath=DBOPrimeConfigParamMap.get("primeFileInputPath");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("primeFileInputPath: "+primeFileInputPath);
			
			primeFileSuccessPath=DBOPrimeConfigParamMap.get("primeFileSuccessPath");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("primeFileSuccessPath: "+primeFileSuccessPath);
			
			primeFileFailPath=DBOPrimeConfigParamMap.get("primeFileFailPath");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("primeFileFailPath: "+primeFileFailPath);
			
			/*CBSColName=DBOPrimeConfigParamMap.get("primeCloumnName");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("primeCloumnName: "+primeCloumnName);*/
			
			primeFileInprogressPath=DBOPrimeConfigParamMap.get("primeFileInprogressPath");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("primeFileInprogressPath: "+primeFileInprogressPath);

			fromMailID=DBOPrimeConfigParamMap.get("fromMailID");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("fromMailID: "+fromMailID);
			
			toMailID=DBOPrimeConfigParamMap.get("toMailID");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("toMailID: "+toMailID);
			
			mailSubject=DBOPrimeConfigParamMap.get("mailSubject");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("mailSubject: "+mailSubject);
			
			MailStr=DBOPrimeConfigParamMap.get("MailStr");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("MailStr: "+MailStr);
			
			ProcessDefId=DBOPrimeConfigParamMap.get("ProcessDefId");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ProcessDefId: "+ProcessDefId);
			
			queueID=DBOPrimeConfigParamMap.get("queueID");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("queueID: "+queueID);
			
			
			
			sessionID = CommonConnection.getSessionID(DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				while (true) 
				{
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Session ID found: " + sessionID);
					
					DBO_PrimeCBS_Logs.setLogger();
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("DBO Prime cbs  ...123.");
					
					DBO_ReadPrimefile(cabinetName, jtsIP, jtsPort,sessionID);
					System.out.println("No More Prime TXT files to Process, Sleeping...!");
					
					DBO_ReadCBSfile(cabinetName, jtsIP, jtsPort,sessionID);
					System.out.println("No More CBS TXT file to Process, Sleeping...!");
					
					processCases();
					System.out.println("No More cases to Process, Sleeping...!");
					
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.error("Exception Occurred in DBO Prime CBS  : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.error("Exception Occurred in DBO Prime CBS  : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DBO_Prime_CBS_File_Read.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    DBOPrimeConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}
	public void DBO_ReadPrimefile(String cabinetName,String serverIP,String serverPort, String sessionID)
	{
		try
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Strating DBO_ReadPrimefile: to read data from prime file.. ");
			File folder = new File(primeFileInputPath);
			File files[] = folder.listFiles();
			
			if(files.length == 0)
			{
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Thread prime going to sleep....");
			}
			else
			{
				for(int i=0;i<files.length;i++)
				{
					String file = files[i].getName();
					String pathToMove = "";
					String filePath=primeFileInputPath+File.separator+file;
					String msg = validatePrimefile(filePath);
					String updateStatus="";
					if("Not blank".equalsIgnoreCase(msg))
					{
						TimeStamp=get_timestamp();
						newFilename = Move(primeFileInprogressPath,filePath,TimeStamp,false);
						String finalSourcePath = primeFileInprogressPath+File.separator+newFilename;
						updateStatus = readDataFromPrimeFile(finalSourcePath,newFilename,cabinetName,serverIP,serverPort);
						
						if("success".equalsIgnoreCase(updateStatus))
						{
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("NG_DBO_PrimeCBS_Logs_COURIER table insert successfully...");
							pathToMove = primeFileSuccessPath+File.separator+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(pathToMove,finalSourcePath,TimeStamp,true);
							continue;
						}
						else if ("fail".equalsIgnoreCase(updateStatus))
						{
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Fail to insert in DB NG_DBO_PrimeCBS_Logs_COURIER : ");
							pathToMove = primeFileFailPath+File.separator+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(pathToMove,finalSourcePath,TimeStamp,true);//file is moved to NoDataFile flder
							//sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
							continue;		
						}
					}
					else
					{
						DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("error Prime file : ");
						pathToMove = primeFileFailPath+File.separator+sdate;
						TimeStamp=get_timestamp();
						DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Blank file : TempsourcePath: "+filePath+" primeFileFailPath: "+primeFileFailPath);
						newFilename = Move(pathToMove,filePath,TimeStamp,true);  //file is moved to NoDataFile flder
						//sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
						continue;
					}
				}			
			}
		}
		catch (Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception toString "  + e.toString());
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception getMessage "  + e.getMessage());
		}	
		finally
		{
			System.gc();
		}
	}
		
	public void DBO_ReadCBSfile(String cabinetName,String serverIP,String serverPort, String sessionID) 
	{
		try
		{	
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Strating DBO_ReadCBSfile: to read data from CBS file.. ");
			now = new Date();
			Format formatter = new SimpleDateFormat("dd-MMM-yy");
			sdate = formatter.format(now);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Date: DBO_ReadCBSfile: "+sdate);
			
			File folder = new File(CBSFileInputpath);
			File files[] = folder.listFiles();
			
			if(files.length == 0)
			{
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Thread CBS going to sleep....");
			}
			else
			{
				for(int i=0;i<files.length;i++)	
				{
					String file = files[i].getName();
					String filePath=CBSFileInputpath+File.separator+file;
					String PathToMOve = "";
					// validate txt file is empty or not.
					String msg = validatecbsfile(filePath);
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output of file validation : "+msg);
					String updateStatus="";
					if("Not blank".equalsIgnoreCase(msg))
					{
						TimeStamp=get_timestamp();
						newFilename = Move(CBSFileInprogressPath,filePath,TimeStamp,false);
						
						String finalSourcePath = CBSFileInprogressPath+File.separator+newFilename;
						updateStatus = ReadCSBEFile(finalSourcePath,newFilename,cabinetName,serverIP,serverPort);
						
						if("success".equalsIgnoreCase(updateStatus))
						{
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" NG_DBO_CBS_FILE table insert successfully...");
							PathToMOve = CBSFileSuccessPath+File.separator+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(PathToMOve,finalSourcePath,TimeStamp,true);
							continue;
						}
						else if ("fail".equalsIgnoreCase(updateStatus))
						{
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Fail to insert in DB NG_DBO_CBS_FILE : ");
							
							PathToMOve = CBSFileFailPath+File.separator+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(PathToMOve,finalSourcePath,TimeStamp,true);
							//sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
							continue;	
						}
					
					}
					else
					{
						DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("error in validating CBS file DBO_ReadCBSfile : ");
						PathToMOve = CBSFileFailPath+File.separator+sdate;
						TimeStamp=get_timestamp();
						newFilename = Move(PathToMOve,filePath,TimeStamp,true);
						//sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
						continue;
					}
					
				}			
			}
		}
		
		catch (Exception e)
		{
			//e.printStackTrace();
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception toString "  + e.toString());
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception getMessage "  + e.getMessage());
		}	
		finally
		{
			System.gc();

		}
	}
	public String validatePrimefile(String path)
	{
		String msg="";
		DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Path : "+path);
		File file =  new File(path);
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			if(br.readLine()==null && file.length()==0)
			{
				msg="error";
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("validatecbsfile : File length "+file.length()+"msg"+msg);
			}
			else
			{
				msg="Not blank";
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("validatecbsfile :"+msg);
			}
			br.close();
		}
		catch(Exception e){
			msg="error";
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception : validatecbsfile :"+e.getMessage());
		}
		
		
		return msg;
		
	}
	public String validatecbsfile(String path)
	{
		String msg="";
		DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Path : "+path);
		File file =  new File(path);
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			if(br.readLine()==null && file.length()==0)
			{
				msg="error";
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("validatecbsfile : File length "+file.length()+"msg"+msg);
			}
			else
			{
				msg="Not blank";
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("validatecbsfile :"+msg);
			}
			br.close();
		}
		catch(Exception e)
		{
			msg="error";
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception : validatecbsfile :"+e.getMessage());
		}
		return msg;
	}
	// Data read for Prime: Start
	public String readDataFromPrimeFile(String finalSourcePath,String newFilename,String cabinetName, String sJtsIp, String iJtsPort)
	{
		String status="";
		DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Start readFileForPrime ");
		try
		{
			String elite_crn="";
			String str_Card_Type="";
			
			Date date = new Date();
			DateFormat EntryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String entrydate = EntryDate.format(date);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Start entrydate "+entrydate);
			
	        Scanner read = new Scanner(new File(finalSourcePath));
			String s_prime;
			String s_prime_dbInsert;
			String s_tmp;
	    			
			while(read.hasNextLine())
			{
	    		//changed by gaurav
				s_tmp = read.nextLine();
				s_prime = s_tmp.replace("|", ",");
				s_prime_dbInsert = "'"+s_tmp.replace("|", "','")+"'"; // to use while inserting in the table
	    				
				String [] col_val_ecrn = s_prime.split(",");
				//elite_crn = col_val_ecrn[0];
				elite_crn = col_val_ecrn[13];//till Prime 4 is going to be live
				str_Card_Type = col_val_ecrn[12];
	    				
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ReadPrimeFile elite_crn: "+elite_crn);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ReadPrimeFile str_Card_Type: "+str_Card_Type);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ReadPrimeFile s_prime: "+s_prime);

				// Ap select for getting Ecrn number check and insert flag
				String DebitCardRefNo="",Wi_name="", Query_for_ecrn="",RelatedPartyID="",ProspectID="",AccountNumber="",CustomerName="",MobNumber="",EmailID="",DebitCardRequired="",isChqBookRecipient="",ischqbkreq="";
				Query_for_ecrn = "select TOP 1 r.DebitCardRefNo,e.WINAME,r.RelatedPartyID,e.ProspectID,e.AccountNumber,e.ischqbkreq,r.FullName,r.MobNumber,r.EmailID,r.DebitCardRequired,r.ChequeBookRecipient   from RB_DBO_EXTTABLE e with(nolock) , USR_0_DBO_RelatedPartyGrid r with(nolock) where e.WINAME=r.WINAME and  r.DebitCardRefNo='"+elite_crn+"' and r.DebitCardRequired='Y' and (r.DebitCardBOFIleStatus != 'Received' or r.DebitCardBOFIleStatus is null)";
				String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(Query_for_ecrn, cabinetName, sessionID);
				
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output_Query_for_ecrn: "+extTabDataIPXML);
				String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" extTabDataOPXML : prime "+ extTabDataOPXML);

				XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
				String mainCode = xmlParserData.getValueOf("MainCode");
	    				
	    				
				if("0".equalsIgnoreCase(mainCode))
				{
					String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
					int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null) ?Integer.parseInt(recRetrived):0;
					if(noOfRecords>0)
					{
						NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
						{
							DebitCardRefNo = objWorkList.getVal("DebitCardRefNo").trim();
							Wi_name =objWorkList.getVal("WINAME").trim();
							RelatedPartyID =objWorkList.getVal("RelatedPartyID").trim();
							ProspectID =objWorkList.getVal("ProspectID").trim();
							AccountNumber =objWorkList.getVal("AccountNumber").trim();
							CustomerName =objWorkList.getVal("FullName").trim();
							MobNumber =objWorkList.getVal("MobNumber").trim();
							EmailID =objWorkList.getVal("EmailID").trim();
							DebitCardRequired =objWorkList.getVal("DebitCardRequired").trim();
							ischqbkreq=objWorkList.getVal("ischqbkreq").trim();
							isChqBookRecipient=objWorkList.getVal("ChequeBookRecipient").trim();
							if(("Y".equalsIgnoreCase(ischqbkreq) || "Yes".equalsIgnoreCase(ischqbkreq)) && ("Y".equalsIgnoreCase(isChqBookRecipient) || "Yes".equalsIgnoreCase(isChqBookRecipient)))
							{
								isChqBookRecipient="Y";
							}
							else
							{
								isChqBookRecipient="N";
							}
								
							status=updateInsertPRIMETable(DebitCardRefNo,s_prime_dbInsert,Wi_name);
							if("Success".equalsIgnoreCase(status))
							{
								status=updateInsertAWBTable(DebitCardRefNo,Wi_name,RelatedPartyID,ProspectID,AccountNumber,CustomerName,MobNumber,EmailID,isChqBookRecipient,"PRIME");
								if("Success".equalsIgnoreCase(status))
								{
									updateTableData("USR_0_DBO_RelatedPartyGrid","DebitCardBOFIleStatus","'Received'","WINAME='"+Wi_name+"' and DebitCardRefNo='"+DebitCardRefNo+"' and RelatedPartyID='"+RelatedPartyID+"'" );
									status="success";
								}
								else
								{
									updateTableData("USR_0_DBO_RelatedPartyGrid","DebitCardBOFIleStatus","'Error'","WINAME='"+Wi_name+"' and DebitCardRefNo='"+DebitCardRefNo+"' and  RelatedPartyID='"+RelatedPartyID+"' " );
									status="fail";
									break;
								}
							}
							else
							{
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" InsertUpdate in PRIME table failed: "+ status);
								status="fail";
								return status;
							}
						}
						DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" Wi_name "+Wi_name+" for  DebitCardRefNo "+ DebitCardRefNo);
						
						//DoneWI(Wi_name);
					}
	    		}
				else
					return "fail";
				
	    	}
			read.close();
		}
		catch(Exception e)
		{
			status="fail";
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception occurred prime: "+e.getMessage());
			System.out.println("Exception occurred : prime"+e.getMessage());
		}
		return status;
	}

	public String ReadCSBEFile(String finalSourcePath,String newFilename,String cabinetName, String sJtsIp, String iJtsPort)
	{
		DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ReadCSBEFile : Start ");
		String status="";
		try
		{
			Scanner read = new Scanner(new File(finalSourcePath));
			String insert_val = null;
			String insert_val_dbInsert = null;
			String tmp = null;
			
			while(read.hasNextLine())
			{
				tmp = read.nextLine();
				insert_val =tmp.replace("|", ",");
				insert_val_dbInsert="'"+tmp.replace("|", "','")+"'";
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("input " + insert_val);
			 	String [] chq_Bk_ref = insert_val.split(",");
			 	String chqBk_ref = chq_Bk_ref[1];
			 	DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ReadCSBEFile : input "+chq_Bk_ref[1]);
			 	DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ReadCSBEFile : input "+insert_val);
			 	
			 	String ChequeBk_ref="",WI_name="",RelatedPartyID="",ProspectID="",AccountNumber="",CustomerName="",MobNumber="",EmailID="",DebitCardRequired="";
				String Query_for_ecrn = "select TOP 1 e.ChqBkRefNo,e.WINAME,r.RelatedPartyID,e.ProspectID,e.AccountNumber,r.FullName,r.MobNumber,r.EmailID,r.DebitCardRequired,r.ChequeBookRecipient   from RB_DBO_EXTTABLE e with(nolock) , USR_0_DBO_RelatedPartyGrid r with(nolock) where e.WINAME=r.WINAME and  e.ChqBkRefNo='"+chqBk_ref+"' and r.ChequeBookRecipient='Y' and e.IsChqBkReq='Y' and (e.ChqBkBOFIleStatus != 'Received' or e.ChqBkBOFIleStatus is null)";
				String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(Query_for_ecrn, cabinetName, sessionID);
				
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output_Query_for_ChequeBk_ref: "+extTabDataIPXML);
				String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" extTabDataOPXML : CBS "+ extTabDataOPXML);
					
				XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
				String mainCode = xmlParserData.getValueOf("MainCode");
					
				if("0".equalsIgnoreCase(mainCode))
				{
					String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
					int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null) ?Integer.parseInt(recRetrived):0;
					if(noOfRecords>0)
					{
						NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
						{
							ChequeBk_ref = objWorkList.getVal("ChqBkRefNo").trim();
							WI_name =objWorkList.getVal("WINAME").trim();
							RelatedPartyID =objWorkList.getVal("RelatedPartyID").trim();
							ProspectID =objWorkList.getVal("ProspectID").trim();
							AccountNumber =objWorkList.getVal("AccountNumber").trim();
							CustomerName =objWorkList.getVal("FullName").trim();
							MobNumber =objWorkList.getVal("MobNumber").trim();
							EmailID =objWorkList.getVal("EmailID").trim();
							DebitCardRequired =objWorkList.getVal("DebitCardRequired").trim();
							status=updateInsertCBSTable(chqBk_ref,insert_val_dbInsert,WI_name);
							if("Success".equalsIgnoreCase(status))
							{
								status=updateInsertAWBTable(chqBk_ref,WI_name,RelatedPartyID,ProspectID,AccountNumber,CustomerName,MobNumber,EmailID,DebitCardRequired,"CBS");
								if("Success".equalsIgnoreCase(status))
								{
									updateTableData("RB_DBO_EXTTABLE","ChqBkBOFIleStatus","'Received'","WINAME='"+WI_name+"' and ChqBkRefNo='"+ChequeBk_ref+"' ");
									status="success";
								}
								else
								{
									DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ApInsert failed: "+status);
									updateTableData("RB_DBO_EXTTABLE","ChqBkBOFIleStatus","'Error'","WINAME='"+WI_name+"' and ChqBkRefNo='"+ChequeBk_ref+"'");
									status="fail";
									break;
								}
							}
							else
							{
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("InsertUpdate in CBS table failed: "+status);
								status="fail";
								return status;
							}
						}
						DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" Wi_name "+WI_name+" for  ChequeBk_ref "+ ChequeBk_ref);
						
					}
				}
				else
					return "fail";
				
			}
			read.close();
		}
		catch(Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception : cbs "+e.getCause());
			System.out.println("Exception : "+e.getCause());
		}
		return status;
	}

	public String get_timestamp()
	{
		Date present = new Date();
		Format pformatter = new SimpleDateFormat("dd-MM-yyyy-hhmmss");
		TimeStamp=pformatter.format(present);
		return TimeStamp;
	}
	
	private String updateInsertPRIMETable(String dc_ref,String rowDataToinsertinDB,String winame)
	{
		String status="";
		try
		{
			String Query = "select count(*) as ResultRow from "+primeDataTableName+" with(nolock) where DebitCard_No='"+dc_ref+"'";
			String InputXML=CommonMethods.apSelectWithColumnNames(Query, cabinetName, sessionID);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output_Query_for_DebitCard_ref: "+InputXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(InputXML,jtsIP,jtsPort,1);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" extTabDataOPXML : Prime "+ extTabDataOPXML);
				
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			String mainCode = xmlParserData.getValueOf("MainCode");
			if("0".equalsIgnoreCase(mainCode))
			{
				String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
				int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null)?Integer.parseInt(recRetrived):0;
				if(noOfRecords>0)
				{
					String ResultRowCount = xmlParserData.getValueOf("ResultRow");
					int intResultRowCount = (!"".equalsIgnoreCase(ResultRowCount) && ResultRowCount != null)?Integer.parseInt(ResultRowCount):0;
					if(intResultRowCount>0)
					{

						String values ="getdate(),'"+winame+"','"+newFilename+"',"+rowDataToinsertinDB;
						String sWhere ="DebitCard_No = '"+dc_ref+"'";
						status=updateTableData(primeDataTableName,"RowInsertUpdateDateTime,"+primeCloumnName,values,sWhere);
					}
					else
					{
						String values ="'"+winame+"','"+newFilename+"',"+rowDataToinsertinDB;
						status=insertDataInTable(primeDataTableName,primeCloumnName,values);
					}
				}
				
			}
		}
		catch(Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception in  updateInsertPRIMETable:- "+ e.toString());
		}
		return status;
	}
	private String updateInsertCBSTable(String chqBk_ref,String rowDataToinsertinDB,String winame)
	{
		String status="";
		try
		{
			String Query = "select count(*) as ResultRow from "+CBSDataTableName+" with(nolock) where RequestNo='"+chqBk_ref+"'";
			String InputXML=CommonMethods.apSelectWithColumnNames(Query, cabinetName, sessionID);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output_Query_for_DebitCard_ref: "+InputXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(InputXML,jtsIP,jtsPort,1);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" extTabDataOPXML : CBS "+ extTabDataOPXML);
				
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			String mainCode = xmlParserData.getValueOf("MainCode");
			if("0".equalsIgnoreCase(mainCode))
			{
				String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
				int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null)?Integer.parseInt(recRetrived):0;
				if(noOfRecords>0)
				{
					String ResultRowCount = xmlParserData.getValueOf("ResultRow");
					int intResultRowCount = (!"".equalsIgnoreCase(ResultRowCount) && ResultRowCount != null)?Integer.parseInt(ResultRowCount):0;
					if(intResultRowCount>0)
					{
						String values ="getdate(),'"+winame+"','"+newFilename+"',"+rowDataToinsertinDB;
						String sWhere ="RequestNo ='"+chqBk_ref+"'";
						status=updateTableData(CBSDataTableName,"RowInsertUpdateDateTime,"+CBSColName,values,sWhere);
					}
					else
					{
						String values ="'"+winame+"','"+newFilename+"',"+rowDataToinsertinDB;
						status=insertDataInTable(CBSDataTableName,CBSColName,values);
					}
				} 
				
			}
		}
		catch(Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception in  updateInsertCBSTable:- "+ e.toString());
		}
		return status;
	}
	
	private String updateInsertAWBTable(String refNo,String winame,String RPID,String ProsID,String AccNo,String CustName,String MobNumber,String EmailID,String DebitCardorChequeBookReq,String updateFor)
	{
		String status="";
		String Query ="";
		String columnName ="";
		String values ="";
		String sWhere ="";
		try
		{
			Query = "select count(*) as ResultRow from "+AWBDataTableName+" with(nolock) where WI_name='"+winame+"' and RelatedPartyID='"+RPID+"'";
			
			String InputXML=CommonMethods.apSelectWithColumnNames(Query, cabinetName, sessionID);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output_Query_for_ChequeBk_ref: "+InputXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(InputXML,jtsIP,jtsPort,1);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" extTabDataOPXML : CBS "+ extTabDataOPXML);
			
				
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			String mainCode = xmlParserData.getValueOf("MainCode");
			if("0".equalsIgnoreCase(mainCode))
			{
				String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
				int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null)?Integer.parseInt(recRetrived):0;
				if(noOfRecords>0)
				{
					String ResultRowCount = xmlParserData.getValueOf("ResultRow");
					int intResultRowCount = (!"".equalsIgnoreCase(ResultRowCount) && ResultRowCount != null)?Integer.parseInt(ResultRowCount):0;
					if(intResultRowCount>0)
					{
						sWhere ="WI_name = '"+winame+"' and RelatedPartyID = '"+RPID+"'";
						if("CBS".equalsIgnoreCase(updateFor))
						{
							columnName="ChequeBk_ref";
							values ="'"+refNo+"'";
						}
						else if("PRIME".equalsIgnoreCase(updateFor))
						{
							columnName="DebitCardRefNo";
							values ="'"+refNo+"'";
						}
						status=updateTableData(AWBDataTableName,columnName,values,sWhere);
					}
					else
					{
						if("CBS".equalsIgnoreCase(updateFor))
						{
							columnName="ProcessName,WI_name,RelatedPartyID,Prospect_ID,Account_no,Customer_name,mobile_No,email_id,ChequeBk_Req,ChequeBk_ref,card_req";
							values ="'"+ProcessName+"','"+winame+"','"+RPID+"','"+ProsID+"','"+AccNo+"','"+CustName+"','"+MobNumber+"','"+EmailID+"','Y','"+refNo+"','"+DebitCardorChequeBookReq+"'";
						}
						else if("PRIME".equalsIgnoreCase(updateFor))
						{
							columnName="ProcessName,WI_name,RelatedPartyID,Prospect_ID,Account_no,Customer_name,mobile_No,email_id,ChequeBk_Req,DebitCardRefNo,card_req";
							values ="'"+ProcessName+"','"+winame+"','"+RPID+"','"+ProsID+"','"+AccNo+"','"+CustName+"','"+MobNumber+"','"+EmailID+"','"+DebitCardorChequeBookReq+"','"+refNo+"','Y'";
						}
						else if("Declaration".equalsIgnoreCase(updateFor))
						{
							columnName="ProcessName,WI_name,RelatedPartyID,Prospect_ID,Account_no,Customer_name,mobile_No,email_id,ChequeBk_Req,card_req";
							values ="'"+ProcessName+"','"+winame+"','"+RPID+"','"+ProsID+"','"+AccNo+"','"+CustName+"','"+MobNumber+"','"+EmailID+"','N','"+DebitCardorChequeBookReq+"'";
						}
						status=insertDataInTable(AWBDataTableName,columnName,values);
					}
					
				}
				
			}
		}
		catch(Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception in  updateInsertCBSTable:- "+ e.toString());
		}
		return status;
	}
	
	public String Move(String pstrDestFolderPath, String pstrFilePathToMove,String append,boolean flag ) 
	{
		String newFilename="";
		String lstrExceptionId = "Text_Read.Move";
		try 
		{
			// Destination directory
			File lobjDestFolder = new File(pstrDestFolderPath);

			if (!lobjDestFolder.exists()) 
			{
				lobjDestFolder.mkdirs();
				//delete destination file if it already exists
			}
			File lobjFileTemp;
			File lobjFileToMove = new File(pstrFilePathToMove);
			String orgFileName=lobjFileToMove.getName();

			if(flag)
			{
				// if file is to move then change file name with timestamp 
				newFilename=orgFileName.substring(0,orgFileName.indexOf("."))+"_"+append+orgFileName.substring(orgFileName.indexOf("."));
				lobjFileTemp = new File(pstrDestFolderPath + File.separator + newFilename);
			}
			else
			{
				// if file is not to move then no change in file name 
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug.info("orgFileName::"+orgFileName);
				newFilename=orgFileName;
				lobjFileTemp = new File(pstrDestFolderPath+ File.separator + newFilename );
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug.info("lobjFileTemp::"+lobjFileTemp);
			}
			if (lobjFileTemp.exists()) 
			{
				// to ask  om bhai about it
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug.info("lobjFileTemp exists");
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
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug.info("lobjFileTemp dont exists");
				// lobjFileTemp = null;
			}
			
			// make a file in destination folder
			File lobjNewFolder = new File(lobjDestFolder, newFilename);
			
			boolean lbSTPuccess = false;
			try 
			{
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug.info("lobjFileToMove::"+lobjFileToMove);
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug.info("lobjNewFolder::"+lobjNewFolder);
				// To confirm as this line is to move a file
				lbSTPuccess = lobjFileToMove.renameTo(lobjNewFolder);
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug.info("lbSTPuccess::"+lbSTPuccess);
			} 
			catch (SecurityException lobjExp) 
			{

				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("SecurityException " + lobjExp.toString());
			} 
			catch (NullPointerException lobjNPExp) 
			{

				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("NullPointerException " + lobjNPExp.toString());
			} 
			catch (Exception lobjExp) 
			{

				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception " + lobjExp.toString());
			}
			if (!lbSTPuccess) 
			{
				// File was not successfully moved


				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Failure while moving " + lobjFileToMove.getAbsolutePath() + "===" +
				//	lobjFileToMove.canWrite());
			} 
			else 
			{

				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Success while moving " + lobjFileToMove.getName() + "to" + pstrDestFolderPath);
				//DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Success while moving " + lobjFileToMove.getName() + "to" + lobjNewFolder);
			}
			lobjDestFolder = null;
			lobjFileToMove = null;
			lobjNewFolder = null;
		} 
		catch (Exception lobjExp) 
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(lstrExceptionId + " : " + "Exception occurred while moving " + pstrFilePathToMove + " to " +
					":" + lobjExp.toString());
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

		// The directory is now empty so delete it
		return dir.delete();
	}
	
	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientPrimeCBS.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
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
	private String updateTableData(String tablename, String columnname,String values, String sWhere)
	{
		
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		String status="";
		
		DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,values,sWhere,cabinetName,sessionID);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!"0".equalsIgnoreCase(mainCodeforCheckUpdate)){
					
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Error in executing update on "+tablename+" :maincode"+mainCodeforCheckUpdate));
					status = "Error";
				}
				else
				{
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Succesfully updated "+tablename+" table"));
					return "Success";
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionID  = CommonConnection.getSessionID(DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger, false);
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
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
				status = "Error";
			}
		}
		return status;
	}
	private String insertDataInTable(String tablename, String columnname,String values)
	{
		
		int sessionCheckInt=0;
		int loopCount=50;
		
		DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Inside insert data in table: ");
		
		while(sessionCheckInt<loopCount)
		{
			
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnname,values,tablename);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("apInsertInputXML : " + apInsertInputXML));
				String apInsertOutputXML=WFNGExecute(apInsertInputXML,jtsIP,jtsPort,1);
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("apInsertOutputXML : " + apInsertOutputXML));
				objXMLParser.setInputXML(apInsertOutputXML);
				String apInsertMaincode = null;
				apInsertMaincode=objXMLParser.getValueOf("MainCode");
				if (apInsertOutputXML.equalsIgnoreCase("") || apInsertOutputXML == "" || apInsertOutputXML == null)
					break;
				if ("0".equalsIgnoreCase(apInsertMaincode))
				{
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Succesfully inserted data in "+tablename));
					return "Success";
				}
				else if ("11".equalsIgnoreCase(apInsertMaincode))
				{
					sessionID  = CommonConnection.getSessionID(DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger, false);
				}
				else
				{
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Error in insert of table "+tablename +": maincode" +apInsertMaincode));
					return "error";
				}
			}
			catch(Exception e)
			{
				DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
				return "error";
			}
		}
		return "error";
	}
	private  void processCases()
	{
		try
		{
			//Validate Session ID
			sessionID  = CommonConnection.getSessionID(DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DBO_AWB_Logs.DBO_AWBLogger.error("Could Not Get Session ID "+sessionID);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Fetching all Workitems for Dispatch Hold Queue ");
			System.out.println("Fetching all Workitems on queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,jtsIP,jtsPort,1);

			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Number of workitems retrieved on Sys_Dispatch_Hold : "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on Sys_Dispatch_Hold  : "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ActivityName: "+ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ProcessDefId: "+ProcessDefId);
					//insert AWB entries for which only Declaration is Required
					InsertDataInAWBForOnlyDeclaration(processInstanceID);
					//Complete wi if files corresponding to all deliverable generated
					DoneWI(processInstanceID,ActivityID,ActivityType,ActivityName,entryDateTime);
					
				}
			}
		}
		catch(Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Inside process cases exception--"+e.getMessage()));
		}
	}
	private void InsertDataInAWBForOnlyDeclaration(String WINAME)
	{
		try
		{
			String Query = "select r.RelatedPartyID,e.ProspectID,e.AccountNumber,e.ischqbkreq,r.FullName,r.MobNumber,r.EmailID,r.DebitCardRequired,r.ChequeBookRecipient   from RB_DBO_EXTTABLE e with(nolock) , USR_0_DBO_RelatedPartyGrid r with(nolock) "
					+ "where e.WINAME=r.WINAME and e.WINAME='"+WINAME+"' and r.WINAME='"+WINAME+"' and "
					+ "(r.ChequeBookRecipient != 'Y' and r.DebitCardRequired != 'Y') and DeclarationRequired='Y' and "
					+ "(r.DebitCardBOFIleStatus != 'NA' or r.DebitCardBOFIleStatus is null)";
			String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(Query, cabinetName, sessionID);
			
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output_Query: "+extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" extTabDataOPXML : CBS "+ extTabDataOPXML);
				
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			String mainCode = xmlParserData.getValueOf("MainCode");
			String RelatedPartyID="",ProspectID="",AccountNumber="",CustomerName="",MobNumber="",EmailID="",DebitCardRequired="",ischqbkreq="";	
			if("0".equalsIgnoreCase(mainCode))
			{
				String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
				int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null) ?Integer.parseInt(recRetrived):0;
				if(noOfRecords>0)
				{
					NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
					for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
					{
						
						RelatedPartyID =objWorkList.getVal("RelatedPartyID").trim();
						ProspectID =objWorkList.getVal("ProspectID").trim();
						AccountNumber =objWorkList.getVal("AccountNumber").trim();
						CustomerName =objWorkList.getVal("FullName").trim();
						MobNumber =objWorkList.getVal("MobNumber").trim();
						EmailID =objWorkList.getVal("EmailID").trim();
						DebitCardRequired =objWorkList.getVal("DebitCardRequired").trim();
						ischqbkreq =objWorkList.getVal("ischqbkreq").trim();
						String status=updateInsertAWBTable("",WINAME,RelatedPartyID,ProspectID,AccountNumber,CustomerName,MobNumber,EmailID,DebitCardRequired,"Declaration");
						if("Success".equalsIgnoreCase(status))
						{
							updateTableData("USR_0_DBO_RelatedPartyGrid","DebitCardBOFIleStatus","'NA'","WINAME='"+WINAME+"' and RelatedPartyID='"+RelatedPartyID+"' " );
							status="success";
						}
						else
						{
							updateTableData("USR_0_DBO_RelatedPartyGrid","DebitCardBOFIleStatus","'Error'","WINAME='"+WINAME+"' and RelatedPartyID='"+RelatedPartyID+"' " );
							status="fail";
							break;
						}
						
					}
				}
			}
		}
		catch(Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(("Inside process cases exception--"+e.getMessage()));
		}
	}
	private String DoneWI(String Wi_name,String ActivityID,String ActivityType,String activityName,String entryDateTime )
	{
		String status="";
		String decisionValue="";
		String completeWIFlag="";
		try
		{
			String query = "select "+ 
			"(select count(*) from RB_DBO_EXTTABLE with(nolock) where WINAME ='"+Wi_name+"' and IsChqBkReq='Y'  and (ChqBkBOFIleStatus !='Received' or ChqBkBOFIleStatus is null)) as CBSFilePendingCount,"+
			"(select  count(*)  from USR_0_DBO_RelatedPartyGrid with(nolock) where WINAME='"+Wi_name+"' and DebitCardRequired = 'Y' and (DebitCardBOFIleStatus !='Received' or DebitCardBOFIleStatus is null)) as PRIMEFilePendingCount";
			
			String InputXML=CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Query to get count of pending rows: "+InputXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(InputXML,jtsIP,jtsPort,1);
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug(" Output to validate pending file data "+ extTabDataOPXML);
			
				
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			String mainCode = xmlParserData.getValueOf("MainCode");
			
			if("0".equalsIgnoreCase(mainCode))
			{
				String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
				int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null)?Integer.parseInt(recRetrived):0;
				if(noOfRecords>0)
				{
					String CBSFilePendingCount = xmlParserData.getValueOf("CBSFilePendingCount");
					String PRIMEFilePendingCount = xmlParserData.getValueOf("PRIMEFilePendingCount");
					if("0".equalsIgnoreCase(CBSFilePendingCount.trim()) && "0".equalsIgnoreCase(PRIMEFilePendingCount.trim()))
					{
						decisionValue="Success";
						//Lock Workitem	
						String WorkItemID="1";
						String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, Wi_name,WorkItemID);
						String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,jtsIP,jtsPort,1);
						DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

						XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
						String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
						DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
						if ("0".equals(getWorkItemMainCode.trim()))
						{
							
							String attributesTag="<Decision>"+decisionValue+"</Decision>";
							completeWIFlag="D";
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
							//Move Workitem to next Workstep 
							String completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, Wi_name, WorkItemID,ActivityID,ActivityType, attributesTag,completeWIFlag);
							//completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, Wi_name, WorkItemID, attributesTag,completeWIFlag);
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,jtsIP,jtsPort,1);
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);

							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
							DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);

							if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) 
							{
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("assignWorkitemAttributeInput successful: "+completeWorkitemMaincode);
								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								//SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=inputDateformat.format(entryDatetimeFormat);
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=inputDateformat.format(actionDateTime);
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WINAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+Wi_name+"','"+formattedActionDateTime+"','"+activityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','Card or/and CBS data updated sucessfully for BO consumption'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"USR_0_DBO_WIHISTORY"); // toDo
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,jtsIP,jtsPort,1);
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Completed On "+ activityName);

								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ApInsert successful: "+apInsertMaincode);
									DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else 
							{
								completeWorkitemMaincode="";
								DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception in  DoneWI:- "+ e.toString());
		}
		return status;
	}
	public   void sendMail(String cabinetName ,String wiName,String jtsIp,String jtsPort,String file,String ProcessDefId)throws Exception
    {
        XMLParser objXMLParser = new XMLParser();
        String sInputXML="";
        String sOutputXML="";
        String mainCodeforAPInsert=null;
        int sessionCheckInt = 0;
		while(sessionCheckInt<loopCount)
        {
            try
            {	
            	String FinalMailStr = MailStr.toString().replace("<#file#>",file);
            	
            	DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("finalbody: "+FinalMailStr);

            	String columnName="MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
            	String strValues="'"+fromMailID+"','"+toMailID+"','"+mailSubject+"','"+FinalMailStr+"','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"+CommonMethods.getdateCurrentDateInSQLFormat()+"','"+ProcessDefId+"','"+wiName+"','1','1','0'";
                
				sInputXML = "<?xml version=\"1.0\"?>" +
                        "<APInsert_Input>" +
                        "<Option>APInsert</Option>" +
                        "<TableName>WFMAILQUEUETABLE</TableName>" +
                        "<ColName>" + columnName + "</ColName>" +
                        "<Values>" + strValues + "</Values>" +
                        "<EngineName>" + cabinetName + "</EngineName>" +
                        "<SessionId>" + sessionID + "</SessionId>" +
                        "</APInsert_Input>";
                DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Mail Insert InputXml::::::::::\n"+sInputXML);
                sOutputXML =WFNGExecute(sInputXML, jtsIp,jtsPort,0);
                DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Mail Insert OutputXml::::::::::\n"+sOutputXML);
                objXMLParser.setInputXML(sOutputXML);
                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
                
            }
			
			catch(Exception e)
            {
                e.printStackTrace();
                DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Exception in Sending mail", e);
                sessionCheckInt++;
                waiteloopExecute(waitLoop);
                continue;
            }
            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
            {
                DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("Invalid session in Sending mail");
                sessionCheckInt++;
                //ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
                sessionID  = CommonConnection.getSessionID(DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger, false);
                continue;
            }
            else
            {
                sessionCheckInt++;
                break;
            }
        }
        if(mainCodeforAPInsert.equalsIgnoreCase("0"))
        {
            DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("mail Insert Successful");
            System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
        }
        else
        {
            DBO_PrimeCBS_Logs.DBO_PrimeCBSLogger.debug("mail Insert Unsuccessful");
            System.out.println("Mail Insert Unsuccessful for "+wiName+"in table WFMAILQUEUETABLE");
        }
    }


}






