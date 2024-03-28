/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.user;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.newgen.iforms.custom.IFormReference;
import static com.newgen.iforms.user.iRBL_Integration.readFileFromServer;
import static com.newgen.iforms.user.iRBL_Integration.writeFileFromServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Properties;
//import com.itextpdf.html2pdf.HtmlConverter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author reddy.naidu
 */

public class iRBL_GeneratePDF extends iRBL_Common
{
    LinkedHashMap<String,String> executeXMLMapMain = new LinkedHashMap<String,String>();
    
    public static String XMLLOG_HISTORY="NG_iRBL_XMLLOG_HISTORY";
    
    public String onclickevent(IFormReference iformObj,String control,String StringData) throws FileNotFoundException, IOException 
    {
		
        String returnValue = "";
        String pid = getWorkitemName();

        if(control.equals("PDFGenerate"))
        {
        	LinkedHashMap<String,String> mapCheckDeferral = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckLoans = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckTBCompany = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckTBOwner = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckConduct1 = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckConduct2 = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckSignatory = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckEvalAECB = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckEvalFTS = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckCBRB = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckTVChecks = new LinkedHashMap<String,String>();
            //LinkedHashMap<String,String> mapCheckSysPerformed = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckSDeviation = new LinkedHashMap<String,String>();
            //LinkedHashMap<String,String> mapCheckExcHist = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckELAmount = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckIDBR = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckDocChecklist = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckAddlChecks = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckBasicLF = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckMemopad = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckWIHist = new LinkedHashMap<String,String>();
            //LinkedHashMap<String,String> mapCheckDReasons = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckPolicycheck = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckMachineID = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckMainCheck = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckDedupeCheck = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckBlacklistCheck = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckBlacklistExt = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckFircoCheck = new LinkedHashMap<String,String>();
            //Below are the ones used for ext table columns
            //LinkedHashMap<String,String> mapCheckAddlSecurity = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckApplicationDet = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckFinEligibility = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckLoanDetails = new LinkedHashMap<String,String>();			
            //LinkedHashMap<String,String> mapCheckConductHistory = new LinkedHashMap<String,String>();			
            //LinkedHashMap<String,String> mapCheckEvaluationChecksAECB = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckCBRBNotes = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckTVNotes = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckSummaryNotes = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckPolicyCheckNotes = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckLOSChannel = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckAnalystRemarks = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckRejectEntry = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckTESSLoan = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckAddlChecksNotes = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckBAOpening = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckCPV = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckMemopadDet = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckCropsDet = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckAddlDet = new LinkedHashMap<String,String>();			
            LinkedHashMap<String,String> mapCheckProfileChange = new LinkedHashMap<String,String>();
            LinkedHashMap<String,String> mapCheckIndustryCode = new LinkedHashMap<String,String>();

			String replacedString="";
            String HTMLTemplatePath = "";

            String generateddocPath = "";
			String UniqueNo = Long.toString((new Date()).getTime());
            String dynamicHTMLName = getWorkitemName()+ "template"+UniqueNo+".html";
            
            Properties properties = new Properties();
            try 
            {
                properties.load(new FileInputStream(System.getProperty("user.dir")+ System.getProperty("file.separator")+"CustomConfig"+System.getProperty("file.separator")+ "iRBL.properties"));
            } 
            catch (IOException e) 
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String tempDir = System.getProperty("user.dir");

            HTMLTemplatePath = tempDir + properties.getProperty("iRBL_TEMPLATE_HTML_PATH");
            iRBL.mLogger.debug("\nTemplate HTML Path :" + HTMLTemplatePath);

            generateddocPath = properties.getProperty("iRBL_GENERATED_HTML_PATH");//Get the loaction of the path where generated template will be saved
            generateddocPath += dynamicHTMLName;
            generateddocPath = tempDir + generateddocPath;//Complete path of generated document
            iRBL.mLogger.debug("\nTemplate Doc generateddocPath :" + generateddocPath);

            String sMappOutPutXML = readFileFromServer(HTMLTemplatePath);
            iRBL.mLogger.debug("replaced string reddy new log--"+sMappOutPutXML);
            replacedString = sMappOutPutXML.replaceAll("&lt;","<");
            replacedString = replacedString.replaceAll("&gt;",">");
            replacedString = replacedString.replaceAll("“","");  
            replacedString = replacedString.replaceAll("�?"," "); 
            replacedString = replacedString.replaceAll("&amp;","&");   
            replacedString = replacedString.replaceAll("’","'");  
			 
			//Application details grid**********
			String Application_GridColName = properties.getProperty("Application_Grid_Col_Name");
			String ApplicationGridColumn[] = Application_GridColName.split(",");


			for(String GridColumn : ApplicationGridColumn)
			{
				iRBL.mLogger.debug("each GridColumn--"+GridColumn);
				mapCheckApplicationDet.put(GridColumn,iformObj.getValue(GridColumn).toString());
			}
			//**********************************

			//Financial Eligibility grid**********
			String FinEligibility_Grid_Table = properties.getProperty("FinEligibility_Grid_Table");
			String FinEligibility_GridColName = properties.getProperty("FinEligibility_Grid_Col_Name");
			String FinEligibility_GridColumn[] = FinEligibility_GridColName.split(",");


			for(String GridColumn : FinEligibility_GridColumn)
			{
				iRBL.mLogger.debug("each GridColumn--"+GridColumn);
				iRBL.mLogger.debug("each iformObj.getValue(FinEligibility_Grid_Table+.+GridColumn).toString()--"+iformObj.getValue(FinEligibility_Grid_Table+"."+GridColumn).toString());
				mapCheckFinEligibility.put(GridColumn,iformObj.getValue(FinEligibility_Grid_Table+"."+GridColumn).toString());
			}
			//**********************************
			
			//Loan details grid**********
            String loanDetails_GridColName = properties.getProperty("LoanDetails_Grid_Col_Name");
            String loanDetailsGridColumn[] = loanDetails_GridColName.split(",");


            for(String GridColumn : loanDetailsGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckLoanDetails.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************
            
          //CBRB Notes details grid**********
            String CBRBNotes_GridColName = properties.getProperty("CBRBNotes_Grid_Col_Name");
            String CBRBNotesGridColumn[] = CBRBNotes_GridColName.split(",");


            for(String GridColumn : CBRBNotesGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckCBRBNotes.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Tele-Verification Notes details grid**********
            String TVNotes_GridColName = properties.getProperty("TVNotes_Grid_Col_Name");
            String TVNotesGridColumn[] = TVNotes_GridColName.split(",");


            for(String GridColumn : TVNotesGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckTVNotes.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Summary of Deviation details grid**********
            String SummaryNotes_GridColName = properties.getProperty("SummaryNotes_Grid_Col_Name");
            String SummaryNotesGridColumn[] = SummaryNotes_GridColName.split(",");


            for(String GridColumn : SummaryNotesGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckSummaryNotes.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Policy Checklist Notes details grid**********
            String PolicyCheckNotes_GridColName = properties.getProperty("PolicyCheckNotes_Grid_Col_Name");
            String PolicyCheckNotesGridColumn[] = PolicyCheckNotes_GridColName.split(",");


            for(String GridColumn : PolicyCheckNotesGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckPolicyCheckNotes.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //LOS Channel details grid**********
            String LOSChannel_GridColName = properties.getProperty("LOSChannel_Grid_Col_Name");
            String LOSChannelGridColumn[] = LOSChannel_GridColName.split(",");


            for(String GridColumn : LOSChannelGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckLOSChannel.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Analyst Remarks details grid**********
            String AnalystRemarks_GridColName = properties.getProperty("AnalystRemarks_Grid_Col_Name");
            String AnalystRemarksGridColumn[] = AnalystRemarks_GridColName.split(",");


            for(String GridColumn : AnalystRemarksGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckAnalystRemarks.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Reject Entry details grid**********
            String RejectEntry_GridColName = properties.getProperty("RejectEntry_Grid_Col_Name");
            String RejectEntryGridColumn[] = RejectEntry_GridColName.split(",");


            for(String GridColumn : RejectEntryGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckRejectEntry.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //TESS Loan History details grid**********
            String TESSLoan_GridColName = properties.getProperty("TESSLoan_Grid_Col_Name");
            String TESSLoanGridColumn[] = TESSLoan_GridColName.split(",");


            for(String GridColumn : TESSLoanGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckTESSLoan.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Addl Checks Notes details grid**********
            String AddlChecksNotes_GridColName = properties.getProperty("AddlChecksNotes_Grid_Col_Name");
            String AddlChecksNotesGridColumn[] = AddlChecksNotes_GridColName.split(",");


            for(String GridColumn : AddlChecksNotesGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckAddlChecksNotes.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Business Account Opening details grid**********
            String BAOpening_GridColName = properties.getProperty("BAOpening_Grid_Col_Name");
            String BAOpeningGridColumn[] = BAOpening_GridColName.split(",");


            for(String GridColumn : BAOpeningGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckBAOpening.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //CPV details grid**********
            String CPV_GridColName = properties.getProperty("CPV_Grid_Col_Name");
            String CPVGridColumn[] = CPV_GridColName.split(",");


            for(String GridColumn : CPVGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckCPV.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Memopad details grid**********
            String MemopadDet_GridColName = properties.getProperty("MemopadDet_Grid_Col_Name");
            String MemopadDetGridColumn[] = MemopadDet_GridColName.split(",");


            for(String GridColumn : MemopadDetGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckMemopadDet.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //CROPS FIELDS details grid**********
            String CropsDet_GridColName = properties.getProperty("CropsDet_Grid_Col_Name");
            String CropsDetGridColumn[] = CropsDet_GridColName.split(",");


            for(String GridColumn : CropsDetGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckCropsDet.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //ADDITIONAL FIELDS details grid**********
            String AddlDet_GridColName = properties.getProperty("AddlDet_Grid_Col_Name");
            String AddlDetGridColumn[] = AddlDet_GridColName.split(",");


            for(String GridColumn : AddlDetGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckAddlDet.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************

            //Any Profile Change details grid**********
            String ProfileChange_GridColName = properties.getProperty("ProfileChange_Grid_Col_Name");
            String ProfileChangeGridColumn[] = ProfileChange_GridColName.split(",");


            for(String GridColumn : ProfileChangeGridColumn)
            {
                    iRBL.mLogger.debug("each GridColumn--"+GridColumn);
                    mapCheckAddlDet.put(GridColumn,iformObj.getValue(GridColumn).toString());
            }
            //**********************************


         // Table Section conditions start from here**************************				
            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside deferral details section --->");

            String Deferral_GridTable = properties.getProperty("Deferral_Grid_Table");
            String deferral_GridColName = properties.getProperty("Deferral_Grid_Col_Name");
            String DeferralGridColumn[] = deferral_GridColName.split(",");

            String deferral_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+Deferral_GridTable+"' class='tableborder1'>";

            deferral_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Document Type Referral</td><td class='TableHeaderIRBL'>Approving Authority (Name)</td><td class='TableHeaderIRBL'>Deferral Expiry Date</td><td class='TableHeaderIRBL'>Deferral Status</td></tr>";
            int tablecount = iformObj.getDataFromGrid("Q_USR_0_iRBL_DEFERRAL_DTLS").size();
            //iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+iformObj.getActivityName()+", tablecount "+tablecount);
            for (int i = 0; i< tablecount; i++)
            {
                    int j=0;
                    for(String GridColumn : DeferralGridColumn)
                    {
                            mapCheckDeferral.put(GridColumn+i,iformObj.getTableCellValue(Deferral_GridTable,i, j).toString());
                            j++;
                    }
            }			
            iRBL.mLogger.debug("mapCheckDeferral size x--"+mapCheckDeferral.size());

            deferral_details += "<tr>";
            int ColCount=0;
            for (Map.Entry<String,String> entry : mapCheckDeferral.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val=String.valueOf(ColCount);

                    if(key.indexOf(val) != -1)
                    {
                            deferral_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount++;
                            deferral_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }			
            }
            deferral_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Deferral Details--"+deferral_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside loans section --->");
            String loans_GridTable = properties.getProperty("Loans_Grid_Table");
            String loans_GridColName = properties.getProperty("Loans_Grid_Col_Name");
            String LoansGridColumn[] = loans_GridColName.split(",");

            String loans_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+loans_GridTable+"' class='tableborder1'>";

            loans_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Agreement Number</td><td class='TableHeaderIRBL'>Outstanding Amount</td><td class='TableHeaderIRBL'>No. of EMI's Paid/Last Tenor</td><td class='TableHeaderIRBL'>Scheme Name</td><td class='TableHeaderIRBL'>Remarks</td></tr>";
            int tablecount2 = iformObj.getDataFromGrid("Q_USR_0_IRBL_LOANS_GRID_DTLS").size();

            for (int i = 0; i< tablecount2; i++)
            {
                    int j=0;

                    for(String GridColumn1 : LoansGridColumn)
                    {					
                            mapCheckLoans.put(GridColumn1+i,iformObj.getTableCellValue(loans_GridTable,i, j).toString());
                            j++;
                    }
            }              
            iRBL.mLogger.debug("mapCheckLoans size x--"+mapCheckLoans.size());

            loans_details += "<tr>";
            int ColCount1=0;
            for (Map.Entry<String,String> entry : mapCheckLoans.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val1=String.valueOf(ColCount1);

                    if(key.indexOf(val1) != -1)
                    {
                            loans_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount1++;
                            loans_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            loans_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Loan Details--"+loans_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Total Borrowing Company Details section --->");
            String TBCompany_GridTable = properties.getProperty("TBCompany_Grid_Table");
            String tbcompany_GridColName = properties.getProperty("TBCompany_Grid_Col_Name");
            String TBCompanyGridColumn[] = tbcompany_GridColName.split(",");

            String total_borr_com_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+TBCompany_GridTable+"' class='tableborder1'>";

            total_borr_com_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Bank</td><td class='TableHeaderIRBL'>Facility Type</td><td class='TableHeaderIRBL'>Loan/Facility Amount</td><td class='TableHeaderIRBL'>EMI Amount</td><td class='TableHeaderIRBL'>EMI's Paid/Tenor</td><td class='TableHeaderIRBL'>Cleared From</td><td class='TableHeaderIRBL'>Outstanding Balance</td><td class='TableHeaderIRBL'>Date of Facility</td></tr>";
            int tablecount3 = iformObj.getDataFromGrid("Q_USR_0_IRBL_TOTAL_BORR_COMPANY_GRID_DTLS").size();
            for (int i = 0; i< tablecount3; i++)
            {
                    int j=0;

                    for(String GridColumn2 : TBCompanyGridColumn)
                    {					
                            mapCheckTBCompany.put(GridColumn2+i,iformObj.getTableCellValue(TBCompany_GridTable,i, j).toString());
                            j++;
                    }				
            }             
            iRBL.mLogger.debug("mapCheckTBCompany size x--"+mapCheckTBCompany.size());

            total_borr_com_details += "<tr>";
            int ColCount2=0;
            for (Map.Entry<String,String> entry : mapCheckTBCompany.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val2=String.valueOf(ColCount2);

                    if(key.indexOf(val2) != -1)
                    {
                            total_borr_com_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount2++;
                            total_borr_com_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            total_borr_com_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Total Borrowing Company Details--"+total_borr_com_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Total Borrowing Owner / Sister Company Details section --->");
            String TBOwner_GridTable = properties.getProperty("TBOwner_Grid_Table");
            String tbowner_GridColName = properties.getProperty("TBOwner_Grid_Col_Name");
            String TBOwnerGridColumn[] = tbowner_GridColName.split(",");

            String total_borr_owner_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+TBOwner_GridTable+"' class='tableborder1'>";

            total_borr_owner_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Name</td><td class='TableHeaderIRBL'>Bank</td><td class='TableHeaderIRBL'>Facility Type</td><td class='TableHeaderIRBL'>Loan/Facility</td><td class='TableHeaderIRBL'>EMI's Paid/Tenor</td><td class='TableHeaderIRBL'>Outstanding Amount</td><td class='TableHeaderIRBL'>For Eligibility</td></tr>";
            int tablecount4 = iformObj.getDataFromGrid("Q_USR_0_IRBL_TOTAL_BORR_SISTER_GRID_DTLS").size();
            for (int i = 0; i< tablecount4; i++)
            {
                    int j=0;

                    for(String GridColumn3 : TBOwnerGridColumn)
                    {
                            mapCheckTBOwner.put(GridColumn3+i,iformObj.getTableCellValue(TBOwner_GridTable,i, j).toString());
                            j++;
                    }
            }            
            iRBL.mLogger.debug("mapCheckTBOwner size x--"+mapCheckTBOwner.size());

            total_borr_owner_details += "<tr>";
            int ColCount3=0;
            for (Map.Entry<String,String> entry : mapCheckTBOwner.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val3=String.valueOf(ColCount3);

                    if(key.indexOf(val3) != -1)
                    {				
                            total_borr_owner_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount3++;
                            total_borr_owner_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            total_borr_owner_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Total Borrowing Owner / Sister Company Details--"+total_borr_owner_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Conduct History Main details section --->");

            String Conduct1_GridTable = properties.getProperty("Conduct1_Grid_Table");
            String conduct1_GridColName = properties.getProperty("Conduct1_Grid_Col_Name");
            String Conduct1GridColumn[] = conduct1_GridColName.split(",");

            String conduct1_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+Conduct1_GridTable+"' class='tableborder1'>";

            conduct1_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Name of Sister/Owner Company</td><td class='TableHeaderIRBL'>CIF</td><td class='TableHeaderIRBL'>Credit Grade</td><td class='TableHeaderIRBL'>AECB Score</td></tr>";
            int tablecount5 = iformObj.getDataFromGrid("Q_USR_0_IRBL_CONDUCT_REL_PARTY_GRID_DTLS").size();
            for (int i = 0; i< tablecount5; i++)
            {
                    int j=0;

                    for(String GridColumn4 : Conduct1GridColumn)
                    {
                            mapCheckConduct1.put(GridColumn4+i,iformObj.getTableCellValue(Conduct1_GridTable,i, j).toString());
                            j++;
                    }
            }              
            iRBL.mLogger.debug("mapCheckConduct1 size x--"+mapCheckConduct1.size());

            conduct1_details += "<tr>";
            int ColCount4=0;
            for (Map.Entry<String,String> entry : mapCheckConduct1.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val4=String.valueOf(ColCount4);

                    if(key.indexOf(val4) != -1)
                    {
                            conduct1_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount4++;
                            conduct1_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            conduct1_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Conduct History Main Details--"+conduct1_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside CONDUCTED_HISTORY_DETAILS section --->");
            String conduct2_GridTable = properties.getProperty("Conduct2_Grid_Table");
            String conduct2_GridColName = properties.getProperty("Conduct2_Grid_Col_Name");
            String Conduct2GridColumn[] = conduct2_GridColName.split(",");

            String conduct2_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+conduct2_GridTable+"' class='tableborder1'>";

            conduct2_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Name of Sister Company/Owner</td><td class='TableHeaderIRBL'>Agreement No./ Card No.</td><td class='TableHeaderIRBL'>Loan Amount</td><td class='TableHeaderIRBL'>EMI Amount</td><td class='TableHeaderIRBL'>No. of EMI's Paid/Total Tenor</td><td class='TableHeaderIRBL'>Outstanding Balance</td></tr>";
            int tablecount6 = iformObj.getDataFromGrid("Q_USR_0_IRBL_CONDUCT_REL_PARTY_TABLE_DTLS").size();
            for (int i = 0; i< tablecount6; i++)
            {
                    int j=0;

                    for(String GridColumn5 : Conduct2GridColumn)
                    {
                            mapCheckConduct2.put(GridColumn5+i,iformObj.getTableCellValue(conduct2_GridTable,i, j).toString());
                            j++;
                    }
            }           
            iRBL.mLogger.debug("mapCheckConduct2 size x--"+mapCheckConduct2.size());

            conduct2_details += "<tr>";
            int ColCount5=0;
            for (Map.Entry<String,String> entry : mapCheckConduct2.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val5=String.valueOf(ColCount5);

                    if(key.indexOf(val5) != -1)
                    {
                            conduct2_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount5++;
                            conduct2_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            conduct2_details += "</tr></table><br/>";
            iRBL.mLogger.debug("CONDUCTED_HISTORY_DETAILS--"+conduct2_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
             iRBL.mLogger.debug("Inside Signatory Details section --->");
             String Signatory_GridTable = properties.getProperty("Signatory_Grid_Table");
             String signatory_GridColName = properties.getProperty("Signatory_Grid_Col_Name");
             String SignatoryGridColumn[] = signatory_GridColName.split(",");

            String signatory_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+Signatory_GridTable+"' class='tableborder1'>";

            signatory_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Name of Signatory</td><td class='TableHeaderIRBL'>Effective LOB</td><td class='TableHeaderIRBL'>Nationality</td><td class='TableHeaderIRBL'>Nationality Status</td><td class='TableHeaderIRBL'>Date of Birth</td><td class='TableHeaderIRBL'>Age</td><td class='TableHeaderIRBL'>Age at Maturity</td><td class='TableHeaderIRBL'>CIF</td><td class='TableHeaderIRBL'>Credit Grade</td><td class='TableHeaderIRBL'>SVS Status</td><td class='TableHeaderIRBL'>Active SAS</td></tr>";
            int tablecount7 = iformObj.getDataFromGrid("Q_USR_0_IRBL_SIGNATORY_GRID_DTLS").size();
            for (int i = 0; i< tablecount7; i++)
            {
                    int j=0;

                    for(String GridColumn6 : SignatoryGridColumn)
                    {
                            mapCheckSignatory.put(GridColumn6+i,iformObj.getTableCellValue(Signatory_GridTable,i, j).toString());
                            j++;
                    }
            }
            iRBL.mLogger.debug("mapCheckSignatory size x--"+mapCheckSignatory.size());

            signatory_details += "<tr>";
            int ColCount6=0;
            for (Map.Entry<String,String> entry : mapCheckSignatory.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val6=String.valueOf(ColCount6);

                    if(key.indexOf(val6) != -1)
                    {
                            signatory_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount6++;
                            signatory_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            signatory_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Signatory Details--"+signatory_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Evaluation Checks AECB section --->");
            String evalAECB_GridTable = properties.getProperty("EvalAECB_Grid_Table");
            String evalAECB_GridColName = properties.getProperty("EvalAECB_Grid_Col_Name");
            String EvalAECBGridColumn[] = evalAECB_GridColName.split(",");

            String evalAECB_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+evalAECB_GridTable+"' class='tableborder1'>";

            evalAECB_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Name</td><td class='TableHeaderIRBL'>Consent Status</td><td class='TableHeaderIRBL'>Conducted on</td><td class='TableHeaderIRBL'>Date Modified on</td><td class='TableHeaderIRBL'>Conducted by</td></tr>";
            int tablecount8 = iformObj.getDataFromGrid("Q_USR_0_IRBL_EVAL_CHECKS_AECB_GRID_DTLS").size();
            for (int i = 0; i< tablecount8; i++)
            {
                    int j=0;

                    for(String GridColumn7 : EvalAECBGridColumn)
                    {
                            mapCheckEvalAECB.put(GridColumn7+i,iformObj.getTableCellValue(evalAECB_GridTable,i, j).toString());
                            j++;
                    }
            }               
            iRBL.mLogger.debug("mapCheckEvalAECB size x--"+mapCheckEvalAECB.size());

            evalAECB_details += "<tr>";
             int ColCount7=0;
            for (Map.Entry<String,String> entry : mapCheckEvalAECB.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val7=String.valueOf(ColCount7);

                    if(key.indexOf(val7) != -1)
                    {
                            evalAECB_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount7++;
                            evalAECB_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            evalAECB_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Evaluation Checks AECB Details--"+evalAECB_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Evaluation Checks FTS section --->");
            String evalFTS_GridTable = properties.getProperty("EvalFTS_Grid_Table");
            String evalFTS_GridColName = properties.getProperty("EvalFTS_Grid_Col_Name");
            String EvalFTSGridColumn[] = evalFTS_GridColName.split(",");

            String evalFTS_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+evalFTS_GridTable+"' class='tableborder1'>";

            evalFTS_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Bank Name</td><td class='TableHeaderIRBL'>Account Number</td><td class='TableHeaderIRBL'>Period From</td><td class='TableHeaderIRBL'>Period To</td><td class='TableHeaderIRBL'>FTS Reference Number</td><td class='TableHeaderIRBL'>Consent Status</td><td class='TableHeaderIRBL'>Response</td><td class='TableHeaderIRBL'>File Status</td></tr>";
            int tablecount9 = iformObj.getDataFromGrid("Q_USR_0_IRBL_FTS_EVALUATION_GRID_DTLS").size();
            for (int i = 0; i< tablecount9; i++)
            {
                    int j=0;

                    for(String GridColumn8 : EvalFTSGridColumn)
                    {
                            mapCheckEvalFTS.put(GridColumn8+i,iformObj.getTableCellValue(evalFTS_GridTable,i, j).toString());
                            j++;
                     }
            }                
            iRBL.mLogger.debug("mapCheckEvalFTS size x--"+mapCheckEvalFTS.size());

            evalFTS_details += "<tr>";
            int ColCount8=0;
            for (Map.Entry<String,String> entry : mapCheckEvalFTS.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val8=String.valueOf(ColCount8);

                    if(key.indexOf(val8) != -1)
                    {
                            evalFTS_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount8++;
                            evalFTS_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            evalFTS_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Evaluation Checks FTS Details--"+evalFTS_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside CBRB_CHECK section --->");
            String cbrb_GridTable = properties.getProperty("CBRB_Grid_Table");
            String cbrb_GridColName = properties.getProperty("CBRB_Grid_Col_Name");
            String CBRBGridColumn[] = cbrb_GridColName.split(",");


            String cbrb_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+cbrb_GridTable+"' class='tableborder1'>";

            cbrb_details += "<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Name of the Person</td><td class='TableHeaderIRBL'>Identity Number</td><td class='TableHeaderIRBL'>Exposure Found (Yes/No/NA)</td><td class='TableHeaderIRBL'>Exposure Status (Internal/External/NA)</td><td class='TableHeaderIRBL'>Lowest Classification</td><td class='TableHeaderIRBL'>Conducted on</td><td class='TableHeaderIRBL'>Modified on</td></tr>";            

            int tablecount10 = iformObj.getDataFromGrid("Q_USR_0_IRBL_CBRB_CHECK_DTLS").size();
            for (int i = 0; i< tablecount10; i++)
            {
                    int j=0;

                    for(String GridColumn9 : CBRBGridColumn)
                    {
                            mapCheckCBRB.put(GridColumn9+i,iformObj.getTableCellValue(cbrb_GridTable,i, j).toString());
                            j++;
                    }
            }               
            iRBL.mLogger.debug("mapCheckCBRB size x--"+mapCheckCBRB.size());

            cbrb_details += "<tr>";
            int ColCount9=0;
            for (Map.Entry<String,String> entry : mapCheckCBRB.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val9=String.valueOf(ColCount9);

                    if(key.indexOf(val9) != -1)
                    {
                            cbrb_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount9++;
                            cbrb_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            cbrb_details += "</tr></table><br/>";
            iRBL.mLogger.debug("CBRB_CHECK--"+cbrb_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Tele-Verification Details section --->");
            String TVChecks_GridTable = properties.getProperty("TVChecks_Grid_Table");
            String tvchecks_GridColName = properties.getProperty("TVChecks_Grid_Col_Name");
            String TVChecksGridColumn[] = tvchecks_GridColName.split(",");

            String tvchecks_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+TVChecks_GridTable+"' class='tableborder1'>";

            tvchecks_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Conducted By</td><td class='TableHeaderIRBL'>Conducted From</td><td class='TableHeaderIRBL'>Conducted at(Date)</td><td class='TableHeaderIRBL'>Conducted at(Time)</td><td class='TableHeaderIRBL'>Name of the Person Spoken to</td><td class='TableHeaderIRBL'>Contacted at</td><td class='TableHeaderIRBL'>Remarks</td><td class='TableHeaderIRBL'>Country Name</td><td class='TableHeaderIRBL'>Designation of Person Spoken to</td><td class='TableHeaderIRBL'>Relationship</td></tr>";
            int tablecount11 = iformObj.getDataFromGrid("Q_USR_0_IRBL_TELE_VERIFICATION_GRID_DTLS").size();
            for (int i = 0; i< tablecount11; i++)
            {
                    int j=0;

                    for(String GridColumn10 : TVChecksGridColumn)
                    {
                            mapCheckTVChecks.put(GridColumn10+i,iformObj.getTableCellValue(TVChecks_GridTable,i, j).toString());
                            j++;
                    }
            }            
            iRBL.mLogger.debug("mapCheckTVChecks size x--"+mapCheckTVChecks.size());

            tvchecks_details += "<tr>";
            int ColCount10=0;
            for (Map.Entry<String,String> entry : mapCheckTVChecks.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val10=String.valueOf(ColCount10);

                    if(key.indexOf(val10) != -1)
                    {
                            tvchecks_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount10++;
                            tvchecks_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            tvchecks_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Tele-Verification Details--"+tvchecks_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            /* iRBL.mLogger.debug("Inside System checks performed on CIFs section --->");

            String SysPerformed_GridTable = properties.getProperty("SysPerformed_Grid_Table");
            String sysperformed_GridColName = properties.getProperty("SysPerformed_Grid_Col_Name");
            String SysPerformedGridColumn[] = sysperformed_GridColName.split(",");

            String sysperformed_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+SysPerformed_GridTable+"' class='tableborder1'>";

            sysperformed_details+="<tr class='TableHeaderBgIRBL'>";
            int tablecount12 = iformObj.getDataFromGrid("Q_USR_0_IRBL_SYSTEM_CHECK_CIF_GRID_DTLS").size();
            //iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+iformObj.getActivityName()+", tablecount12 "+tablecount12);
            for (int i = 0; i< tablecount12; i++)
            {
                    //iRBL.mLogger.debug("Inside tablecount loop for i --->"+i);
                    int j=0;

                    for(String GridColumn11 : SysPerformedGridColumn)
                    {
                            if(!HeaderColumnFlag11)
                            {
                                    sysperformed_details += "<td class='TableHeaderIRBL'>"+GridColumn11+"</td>";            
                            }
                            //iRBL.mLogger.debug("Inside SysPerformedGridColumn loop for j --->"+j);
                            mapCheckSysPerformed.put(GridColumn11+i,iformObj.getTableCellValue(SysPerformed_GridTable,i, j).toString());
                            j++;
                    }
                     HeaderColumnFlag11=true;
            }              
            iRBL.mLogger.debug("mapCheckSysPerformed size x--"+mapCheckSysPerformed.size());

            sysperformed_details += "<tr>";
            int ColCount11=0;
            for (Map.Entry<String,String> entry : mapCheckSysPerformed.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    //iRBL.mLogger.debug("key -- "+key);
                    //iRBL.mLogger.debug("values -- "+values);
                    String val11=String.valueOf(ColCount11);
                    //iRBL.mLogger.debug("abc -- "+abc);

                    if(key.indexOf(val11) != -1)
                    {
                            sysperformed_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount11++;
                            sysperformed_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            sysperformed_details += "</tr></table><br/>";
            iRBL.mLogger.debug("System checks performed on CIFs--"+sysperformed_details);*/

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Summary of Deviation section --->");

            String SDeviation_GridTable = properties.getProperty("SDeviation_Grid_Table");
            String sdeviation_GridColName = properties.getProperty("SDeviation_Grid_Col_Name");
            String SDeviationGridColumn[] = sdeviation_GridColName.split(",");

            String sdeviation_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+SDeviation_GridTable+"' class='tableborder1'>";

            sdeviation_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Deviation</td><td class='TableHeaderIRBL'>Deviation Description</td><td class='TableHeaderIRBL'>Deviation Remarks</td></tr>";
            int tablecount13 = iformObj.getDataFromGrid("Q_USR_0_IRBL_SUMMARY_DEV_DTLS").size();
            for (int i = 0; i< tablecount13; i++)
            {
                    int j=0;

                    for(String GridColumn12 : SDeviationGridColumn)
                    {
                            mapCheckSDeviation.put(GridColumn12+i,iformObj.getTableCellValue(SDeviation_GridTable,i, j).toString());
                            j++;
                    }
            }
            iRBL.mLogger.debug("mapCheckSDeviation size x--"+mapCheckSDeviation.size());

            sdeviation_details += "<tr>";
            int ColCount12=0;
            for (Map.Entry<String,String> entry : mapCheckSDeviation.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val12=String.valueOf(ColCount12);

                    if(key.indexOf(val12) != -1)
                    {
                            sdeviation_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount12++;
                            sdeviation_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            sdeviation_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Summary of Deviation--"+sdeviation_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            /*iRBL.mLogger.debug("Inside Exception History section --->");
            String ExcHist_GridTable = properties.getProperty("ExcHist_Grid_Table");
            String excHist_GridColName = properties.getProperty("ExcHist_Grid_Col_Name");
            String ExcHistGridColumn[] = excHist_GridColName.split(",");

            String excHist_details  = "<br/><table cellspacing='1' cellpadding='1' id ='"+ExcHist_GridTable+"' class='tableborder1'>";

            excHist_details +="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Items</td><td class='TableHeaderIRBL'>Exceptions</td><td class='TableHeaderIRBL'>Workstep</td><td class='TableHeaderIRBL'>User Name</td><td class='TableHeaderIRBL'>Decision Taken</td><td class='TableHeaderIRBL'>Date and Time</td></tr>";
            int tablecount14 = iformObj.getDataFromGrid("Q_USR_0_IRBL_EXCEPTION_HISTORY").size();
            for (int i = 0; i< tablecount14; i++)
            {
                    int j=0;

                    for(String GridColumn13 : ExcHistGridColumn)
                    {
                            mapCheckExcHist.put(GridColumn13+i,iformObj.getTableCellValue(ExcHist_GridTable,i, j).toString());
                            j++;
                    }
            }           
            iRBL.mLogger.debug("mapCheckExcHist size x--"+mapCheckExcHist.size());

            excHist_details  += "<tr>";
            int ColCount13=0;
            for (Map.Entry<String,String> entry : mapCheckExcHist.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val13=String.valueOf(ColCount13);

                    if(key.indexOf(val13) != -1)
                    {
                            excHist_details  += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount13++;
                            excHist_details  += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            excHist_details  += "</tr></table><br/>";
        	iRBL.mLogger.debug("Exception History Details--"+excHist_details);*/

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Eligible Amount section --->");
            String ELAmount_GridTable = properties.getProperty("ELAmount_Grid_Table");
            String elamount_GridColName = properties.getProperty("ELAmount_Grid_Col_Name");
            String ELAmountGridColumn[] = elamount_GridColName.split(",");

            String elamount_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+ELAmount_GridTable+"' class='tableborder1'>";

            elamount_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Criteria</td><td class='TableHeaderIRBL'>Policy Multiple</td><td class='TableHeaderIRBL'>Client Actual</td><td class='TableHeaderIRBL'>Sectional Eligibility</td></tr>";
            int tablecount15 = iformObj.getDataFromGrid("Q_USR_0_IRBL_ELIGIBLE_AMT_DTLS").size();
            for (int i = 0; i< tablecount15; i++)
            {
                    int j=0;

                    for(String GridColumn14 : ELAmountGridColumn)
                    {
                            mapCheckELAmount.put(GridColumn14+i,iformObj.getTableCellValue(ELAmount_GridTable,i, j).toString());
                            j++;
                    }
            }           
            iRBL.mLogger.debug("mapCheckELAmount size x--"+mapCheckELAmount.size());

            elamount_details += "<tr>";
            int ColCount14=0;
            for (Map.Entry<String,String> entry : mapCheckELAmount.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val14=String.valueOf(ColCount14);

                    if(key.indexOf(val14) != -1)
                    {
                            elamount_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount14++;
                            elamount_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            elamount_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Eligible Amount--"+elamount_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Indicative DBR section --->");

            String IDBR_GridTable = properties.getProperty("IDBR_Grid_Table");
            String idbr_GridColName = properties.getProperty("IDBR_Grid_Col_Name");
            String IDBRGridColumn[] = idbr_GridColName.split(",");

            String idbr_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+IDBR_GridTable+"' class='tableborder1'>";

            idbr_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Criteria</td><td class='TableHeaderIRBL'>Without RAKBANK Actual</td><td class='TableHeaderIRBL'>With New Loan</td></tr>";
            int tablecount16 = iformObj.getDataFromGrid("Q_USR_0_IRBL_INDICATIVE_DTLS").size();
            for (int i = 0; i< tablecount16; i++)
            {
                    int j=0;

                    for(String GridColumn15 :IDBRGridColumn)
                    {
                            mapCheckIDBR.put(GridColumn15+i,iformObj.getTableCellValue(IDBR_GridTable,i, j).toString());
                            j++;
                    }
            } 
            iRBL.mLogger.debug("mapCheckIDBR size x--"+mapCheckIDBR.size());

            idbr_details += "<tr>";
            int ColCount15=0;
            for (Map.Entry<String,String> entry : mapCheckIDBR.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val15=String.valueOf(ColCount15);

                    if(key.indexOf(val15) != -1)
                    {
                            idbr_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount15++;
                            idbr_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            idbr_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Indicative DBR--"+idbr_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Document Checklist section --->");

            String DocChecklist_GridTable = properties.getProperty("DocChecklist_Grid_Table");
            String docchecklist_GridColName = properties.getProperty("DocChecklist_Grid_Col_Name");
            String DocChecklistGridColumn[] = docchecklist_GridColName.split(",");

            String docchecklist_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+DocChecklist_GridTable+"' class='tableborder1'>";

            docchecklist_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>SNO</td><td class='TableHeaderIRBL'>Type of Document</td><td class='TableHeaderIRBL'>Status</td></tr>";
            int tablecount17 = iformObj.getDataFromGrid("Q_USR_0_IRBL_DOC_CHECKLIST_DTLS").size();
            for (int i = 0; i< tablecount17; i++)
            {
                    int j=0;

                    for(String GridColumn16 : DocChecklistGridColumn)
                    {
                            mapCheckDocChecklist.put(GridColumn16+i,iformObj.getTableCellValue(DocChecklist_GridTable,i, j).toString());
                            j++;
                    }
            }    
            iRBL.mLogger.debug("mapCheckDocChecklist size x--"+mapCheckDocChecklist.size());

            docchecklist_details += "<tr>";
            int ColCount16=0;
            for (Map.Entry<String,String> entry : mapCheckDocChecklist.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val16=String.valueOf(ColCount16);

                    if(key.indexOf(val16) != -1)
                    {
                            docchecklist_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount16++;
                            docchecklist_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            docchecklist_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Document Checklist--"+docchecklist_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Additional Checks section --->");

            String AddlChecks_GridTable = properties.getProperty("AddlChecks_Grid_Table");
            String addlchecks_GridColName = properties.getProperty("AddlChecks_Grid_Col_Name");
            String AddlChecksGridColumn[] = addlchecks_GridColName.split(",");

            String addlchecks_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+AddlChecks_GridTable+"' class='tableborder1'>";

            addlchecks_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Field Name</td><td class='TableHeaderIRBL'>Response</td><td class='TableHeaderIRBL'>Remarks</td></tr>";
            int tablecount18 = iformObj.getDataFromGrid("Q_USR_0_IRBL_ADDL_CHECKS_DTLS").size();
            for (int i = 0; i< tablecount18; i++)
            {
                    int j=0;

                    for(String GridColumn17 : AddlChecksGridColumn)
                    {
                            mapCheckAddlChecks.put(GridColumn17+i,iformObj.getTableCellValue(AddlChecks_GridTable,i, j).toString());
                            j++;
                    }
            }        
            iRBL.mLogger.debug("mapCheckAddlChecks size x--"+mapCheckAddlChecks.size());

            addlchecks_details += "<tr>";
            int ColCount17=0;
            for (Map.Entry<String,String> entry :mapCheckAddlChecks.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val17=String.valueOf(ColCount17);

                    if(key.indexOf(val17) != -1)
                    {
                            addlchecks_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount17++;
                            addlchecks_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            addlchecks_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Additional Checks--"+addlchecks_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Basic Lead Filteration (Go / No-Go) section --->");

            String BasicLF_GridTable = properties.getProperty("BasicLF_Grid_Table");
            String basiclf_GridColName = properties.getProperty("BasicLF_Grid_Col_Name");
            String BasicLFGridColumn[] = basiclf_GridColName.split(",");

            String basiclf_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+BasicLF_GridTable+"' class='tableborder1'>";

            basiclf_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Criteria</td><td class='TableHeaderIRBL'>Client Actual</td><td class='TableHeaderIRBL'>Go/No-Go</td></tr>";
            int tablecount19 = iformObj.getDataFromGrid("Q_USR_0_IRBL_BASIC_LEAD_FIL_DTLS").size();
            for (int i = 0; i< tablecount19; i++)
            {
                    int j=0;

                    for(String GridColumn18 : BasicLFGridColumn)
                    {
                            mapCheckBasicLF.put(GridColumn18+i,iformObj.getTableCellValue(BasicLF_GridTable,i, j).toString());
                            j++;
                    }
            }              
            iRBL.mLogger.debug("mapCheckBasicLF size x--"+mapCheckBasicLF.size());

            basiclf_details += "<tr>";
            int ColCount18=0;
            for (Map.Entry<String,String> entry : mapCheckBasicLF.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");
                    
                    //Handling for special characters
                    if(values.indexOf("<") != -1)
                    {
                    	values=values.replaceAll("<", "&lt;");
                    }
                    else if(values.indexOf(">") != -1)
                    {
                    	values=values.replaceAll(">", "&gt;");
                    }

                    String val18=String.valueOf(ColCount18);

                    if(key.indexOf(val18) != -1)
                    {
                            basiclf_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount18++;
                            basiclf_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            basiclf_details += "</tr></table><br/>";
        iRBL.mLogger.debug("Basic Lead Filteration (Go / No-Go)--"+basiclf_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Memopads section --->");

            String Memopad_GridTable = properties.getProperty("Memopad_Grid_Table");
            String memopad_GridColName = properties.getProperty("Memopad_Grid_Col_Name");
            String MemopadGridColumn[] = memopad_GridColName.split(",");

            String memopad_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+Memopad_GridTable+"' class='tableborder1'>";

            memopad_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>S.No</td><td class='TableHeaderIRBL'>CIF No</td><td class='TableHeaderIRBL'>Memopad Detail</td></tr>";
            int tablecount20 = iformObj.getDataFromGrid("Q_USR_0_IRBL_MEMOPAD_LIST_DTLS").size();
            for (int i = 0; i< tablecount20; i++)
            {
                    int j=0;

                    for(String GridColumn19 : MemopadGridColumn)
                    {
                            mapCheckMemopad.put(GridColumn19+i,iformObj.getTableCellValue(Memopad_GridTable,i, j).toString());
                            j++;
                    }
            }      
            iRBL.mLogger.debug("mapCheckMemopad size x--"+mapCheckMemopad.size());

            memopad_details += "<tr>";
            int ColCount19=0;
            for (Map.Entry<String,String> entry : mapCheckMemopad.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val19=String.valueOf(ColCount19);

                    if(key.indexOf(val19) != -1)
                    {
                            memopad_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount19++;
                            memopad_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            memopad_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Memopads--"+memopad_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Decision History section --->");
            String WIHist_GridTable = properties.getProperty("WIHist_Grid_Table");
            String wiHist_GridColName = properties.getProperty("WIHist_Grid_Col_Name");
            String WIHistGridColumn[] = wiHist_GridColName.split(",");

            String wiHist_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+WIHist_GridTable+"' class='tableborder1'>";

            wiHist_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Date and Time</td><td class='TableHeaderIRBL'>Workstep</td><td class='TableHeaderIRBL'>User Name</td><td class='TableHeaderIRBL'>Decision</td><td class='TableHeaderIRBL'>Reject Reasons</td><td class='TableHeaderIRBL'>Remarks</td></tr>";
            int tablecount21 = iformObj.getDataFromGrid("Q_USR_0_IRBL_WIHISTORY").size();
            for (int i = 0; i< tablecount21; i++)
             {
                    int j=0;

                    for(String GridColumn20 : WIHistGridColumn)
                    {
                            mapCheckWIHist.put(GridColumn20+i,iformObj.getTableCellValue(WIHist_GridTable,i, j).toString());
                            j++;
                    }
            }             
            iRBL.mLogger.debug("mapCheckWIHist size x--"+mapCheckWIHist.size());

            wiHist_details += "<tr>";
            int ColCount20=0;
            for (Map.Entry<String,String> entry : mapCheckWIHist.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val20=String.valueOf(ColCount20);

                    if(key.indexOf(val20) != -1)
                    {
                            wiHist_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount20++;
                            wiHist_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            wiHist_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Decision History Details--"+wiHist_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            /*iRBL.mLogger.debug("Inside Decision Reasons section --->");

            String DecReasons_GridTable = properties.getProperty("DecReasons_Grid_Table");
            String decreasons_GridColName = properties.getProperty("DecReasons_Grid_Col_Name");
            String DecReasonsGridColumn[] = decreasons_GridColName.split(",");
             boolean HeaderColumnFlag21=false;

            String decreasons_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+DecReasons_GridTable+"' class='tableborder1'>";

            decreasons_details+="<tr class='TableHeaderBgIRBL'>";

            int tablecount22 = iformObj.getDataFromGrid("Q_USR_0_IRBL_DEC_REASONS_DTLS").size();
            //iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+iformObj.getActivityName()+", tablecount22 "+tablecount22);
            for (int i = 0; i< tablecount22; i++)
            {
                    //iRBL.mLogger.debug("Inside tablecount loop for i --->"+i);
                     int j=0;

                    for(String GridColumn21 : DecReasonsGridColumn)
                    {
                            if(!HeaderColumnFlag21)
                            {
                                     decreasons_details += "<td class='TableHeaderIRBL'>"+GridColumn21+"</td>";            
                            }
                            //iRBL.mLogger.debug("Inside DecReasonsGridColumn loop for j --->"+j);
                            mapCheckDReasons.put(GridColumn21+i,iformObj.getTableCellValue(DecReasons_GridTable,i, j).toString());
                            j++;
                    }
                    HeaderColumnFlag21=true;
            }             
            iRBL.mLogger.debug("mapCheckDReasons size x--"+mapCheckDReasons.size());

            decreasons_details += "<tr>";
            int ColCount21=0;
            for (Map.Entry<String,String> entry : mapCheckDReasons.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    //iRBL.mLogger.debug("key -- "+key);
                    //iRBL.mLogger.debug("values -- "+values);
                    String val21=String.valueOf(ColCount21);
                    //iRBL.mLogger.debug("abc -- "+abc);

                    if(key.indexOf(val21) != -1)
                    {
                            decreasons_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount21++;
                            decreasons_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            decreasons_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Decision Reasons--"+decreasons_details);*/
            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside POLICY_CHECKLIST section --->");

            String PolicyChecklist_GridTable = properties.getProperty("PolicyChecklist_Grid_Table");
            String policychecklist_GridColName = properties.getProperty("PolicyChecklist_Grid_Col_Name");
            String PolicyChecklistGridColumn[] = policychecklist_GridColName.split(",");

            String policychecklist_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+PolicyChecklist_GridTable+"' class='tableborder1'>";

            policychecklist_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Criteria</td><td class='TableHeaderIRBL'>Policy</td></tr>";
            int tablecount23 = iformObj.getDataFromGrid("Q_USR_0_IRBL_POLICY_CHECKLIST_DTLS").size();
            for (int i = 0; i< tablecount23; i++)
            {
                    int j=0;

                    for(String GridColumn22 : PolicyChecklistGridColumn)
                    {
                            mapCheckPolicycheck.put(GridColumn22+i,iformObj.getTableCellValue(PolicyChecklist_GridTable,i, j).toString());
                            j++;
                    }
            }              
            iRBL.mLogger.debug("mapCheckPolicycheck size x--"+mapCheckPolicycheck.size());

            policychecklist_details += "<tr>";
            int ColCount22=0;
            for (Map.Entry<String,String> entry : mapCheckPolicycheck.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                  //Handling for special characters
                    if(values.indexOf("<") != -1)
                    {
                    	values=values.replaceAll("<", "&lt;");
                    }
                    else if(values.indexOf(">") != -1)
                    {
                    	values=values.replaceAll(">", "&gt;");
                    }
                    
                    String val22=String.valueOf(ColCount22);

                    if(key.indexOf(val22) != -1)
                    {
                            policychecklist_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount22++;
                            policychecklist_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            policychecklist_details += "</tr></table><br/>";
            iRBL.mLogger.debug("POLICY_CHECKLIST--"+policychecklist_details);

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Machine ID section --->");

            String MachineID_GridTable = properties.getProperty("MachineID_Grid_Table");
            String machineid_GridColName = properties.getProperty("MachineID_Grid_Col_Name");
            String MachineIDGridColumn[] = machineid_GridColName.split(",");

            String machineid_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+MachineID_GridTable+"' class='tableborder1'>";

            machineid_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>ID</td><td class='TableHeaderIRBL'>Provider</td></tr>";
            int tablecount24 = iformObj.getDataFromGrid("Q_USR_0_IRBL_MACHINEID_DTLS").size();
            for (int i = 0; i< tablecount24; i++)
            {
                    //iRBL.mLogger.debug("Inside tablecount loop for i --->"+i);
                    int j=0;

                    for(String GridColumn23 : MachineIDGridColumn)
                    {
                            mapCheckMachineID.put(GridColumn23+i,iformObj.getTableCellValue(MachineID_GridTable,i, j).toString());
                            j++;
                    }
            }           
            iRBL.mLogger.debug("mapCheckMachineID size x--"+mapCheckMachineID.size());

            machineid_details += "<tr>";
            int ColCount23=0;
            for (Map.Entry<String,String> entry : mapCheckMachineID.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val23=String.valueOf(ColCount23);

                    if(key.indexOf(val23) != -1)
                    {
                            machineid_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount23++;
                            machineid_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            machineid_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Machine ID--"+machineid_details);

            //End of Repeat Table Tag******************************

            // Changed this as listbox on form
            //Start of Repeat Table Tag******************************
        /*iRBL.mLogger.debug("Inside Additional Security section --->");

            String AddlSecurity_GridTable = properties.getProperty("AddlSecurity_Grid_Table");
            String addlsecurity_GridColName = properties.getProperty("AddlSecurity_Grid_Col_Name");
            String AddlSecurityGridColumn[] = addlsecurity_GridColName.split(",");
            boolean HeaderColumnFlag24=false;

            String addlsecurity_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+AddlSecurity_GridTable+"' class='tableborder1'>";

            addlsecurity_details+="<tr class='TableHeaderBgIRBL'>";
            int tablecount25 = iformObj.getDataFromGrid("Q_USR_0_IRBL_ADDL_SECUTRITY_DTLS").size();
            iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+iformObj.getActivityName()+", tablecount25 "+tablecount25);
            for (int i = 0; i< tablecount25; i++)
            {
                    //iRBL.mLogger.debug("Inside tablecount loop for i --->"+i);
                    int j=0;

                    for(String GridColumn24 : AddlSecurityGridColumn)
                    {
                            if(!HeaderColumnFlag24)
                            {
                                    addlsecurity_details += "<td class='TableHeaderIRBL'>"+GridColumn24+"</td>";            
                            }
                            //iRBL.mLogger.debug("Inside AddlSecurityGridColumn loop for j --->"+j);
                            mapCheckAddlSecurity.put(GridColumn24+i,iformObj.getTableCellValue(AddlSecurity_GridTable,i, j).toString());
                            j++;
                    }
                    HeaderColumnFlag24=true;
            }            
            iRBL.mLogger.debug("mapCheckAddlSecurity size x--"+mapCheckAddlSecurity.size());

            addlsecurity_details += "<tr>";
            int ColCount24=0;
            for (Map.Entry<String,String> entry : mapCheckAddlSecurity.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    //iRBL.mLogger.debug("key -- "+key);
                    //iRBL.mLogger.debug("values -- "+values);
                    String val24=String.valueOf(4);
                    //iRBL.mLogger.debug("abc -- "+abc);
                    if(key.indexOf(val24) != -1)
                    {
                            addlsecurity_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount24++;
                            addlsecurity_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            addlsecurity_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Additional Security--"+addlsecurity_details);*/

            //End of Repeat Table Tag******************************

            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Industry Code Details section --->");

            String IndustryCode_GridTable = properties.getProperty("IndustryCode_Grid_Table");
            String industryCode_GridColName = properties.getProperty("IndustryCode_Grid_Col_Name");
            String IndustryCodeGridColumn[] = industryCode_GridColName.split(",");

            String industryCode_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+IndustryCode_GridTable+"' class='tableborder1'>";

            industryCode_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Industry Code</td><td class='TableHeaderIRBL'>Industry Sub-Category</td></tr>";
            int tablecount25 = iformObj.getDataFromGrid("Q_USR_0_IRBL_INDUSTRY_CODE_DTLS").size();
            for (int i = 0; i< tablecount25; i++)
            {
                    int j=0;

                    for(String GridColumn25 : IndustryCodeGridColumn)
                    {
                            mapCheckIndustryCode.put(GridColumn25+i,iformObj.getTableCellValue(IndustryCode_GridTable,i, j).toString());
                            j++;
                    }
            }             
            iRBL.mLogger.debug("mapCheckIndustryCode size x--"+mapCheckIndustryCode.size());

            industryCode_details += "<tr>";
            int ColCount25=0;
            for (Map.Entry<String,String> entry : mapCheckIndustryCode.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val25=String.valueOf(ColCount25);

                    if(key.indexOf(val25) != -1)
                    {
                            industryCode_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount25++;
                            industryCode_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            industryCode_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Industry Code Details--"+industryCode_details);

            //End of Repeat Table Tag******************************
            
            //Start of Repeat Table Tag******************************
            iRBL.mLogger.debug("Inside Main Check Grid Details section --->");

            String MainCheck_GridTable = properties.getProperty("MainCheck_Grid_Table");
            String maincheck_GridColName = properties.getProperty("MainCheck_Grid_Col_Name");
            String MainCheckGridColumn[] = maincheck_GridColName.split(",");

            String maincheck_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+MainCheck_GridTable+"'  class='tableborder1'>";

            maincheck_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Name</td><td class='TableHeaderIRBL'>CIF</td><td class='TableHeaderIRBL'>Type of CIF</td><td class='TableHeaderIRBL'>KYC Valid Till</td><td class='TableHeaderIRBL'>Conducted on</td><td class='TableHeaderIRBL'>Date Modified on</td><td class='TableHeaderIRBL'>Conducted By</td></tr>";
           
            //Below columns made visibility False on form.
            //<td class='TableHeaderIRBL'>Dedupe Status</td><td class='TableHeaderIRBL'>Blacklist Status</td><td class='TableHeaderIRBL'>FircoSoft Status</td>
            
            int tablecount26 = iformObj.getDataFromGrid("Q_USR_0_IRBL_CHECKS_GRID_DTLS").size();
            for (int i = 0; i< tablecount26; i++)
            {
                int j=0;

                for(String GridColumn26 : MainCheckGridColumn)
                {
                    mapCheckMainCheck.put(GridColumn26+i,iformObj.getTableCellValue(MainCheck_GridTable,i, j).toString());
                    j++;
                }
            }        
            iRBL.mLogger.debug("mapCheckMainCheck size x--"+mapCheckMainCheck.size());

            maincheck_details += "<tr>";
            int ColCount26=0;
            for (Map.Entry<String,String> entry : mapCheckMainCheck.entrySet()) 
            {
                String key = entry.getKey();
                String values = entry.getValue();
                values = values.replaceAll((char)(13)+"", "<br/>");

                String val26=String.valueOf(ColCount26);

                if(key.indexOf(val26) != -1)
                {
                    maincheck_details += "<td class='TableValueIRBL'>"+values+"</td>";
                }
                else
                {
                    ColCount26++;
                    maincheck_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                }
            }
            maincheck_details +="</tr></table><br/>";
            iRBL.mLogger.debug("Main Check Grid Details--"+maincheck_details);
            
            //End of Repeat Table Tag******************************

            //Start of Dedupe Table Tag******************************
            //LinkedHashMap<String,String> mapCheckDedupeCheck = new LinkedHashMap<String,String>();
            iRBL.mLogger.debug("Inside Dedupe Check Grid Details section --->");

            String DedupeCheck_GridTable = properties.getProperty("DedupeCheck_Grid_Table");
            String dedupecheck_GridColName = properties.getProperty("DedupeCheck_Grid_Col_Name");
            String DedupeCheckGridColumn[] = dedupecheck_GridColName.split(",");

            String dedupecheck_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+DedupeCheck_GridTable+"'  class='tableborder1'>";

            dedupecheck_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>CIF ID</td><td class='TableHeaderIRBL'>First Name</td><td class='TableHeaderIRBL'>Last Name</td><td class='TableHeaderIRBL'>Full Name</td><td class='TableHeaderIRBL'>Emirates ID</td><td class='TableHeaderIRBL'>Remarks</td><td class='TableHeaderIRBL'>Match Status</td></tr>";
            int tablecount27 = iformObj.getDataFromGrid("Q_USR_0_IRBL_DEDUPE_GRID_DTLS").size();
            for (int i = 0; i< tablecount27; i++)
            {
                int j=0;

               for(String GridColumn27 : DedupeCheckGridColumn)
                {
                    mapCheckDedupeCheck.put(GridColumn27+i,iformObj.getTableCellValue(DedupeCheck_GridTable,i, j).toString());
                    j++;
                }
            }           
            iRBL.mLogger.debug("mapCheckDedupeCheck size x--"+mapCheckDedupeCheck.size());

            dedupecheck_details += "<tr>";

            int ColCount27=0;
            for (Map.Entry<String,String> entry : mapCheckDedupeCheck.entrySet()) 
            {
                    String key = entry.getKey();
                    String values = entry.getValue();
                    values = values.replaceAll((char)(13)+"", "<br/>");

                    String val27=String.valueOf(ColCount27);

                    if(key.indexOf(val27) != -1)
                    {
                            dedupecheck_details += "<td class='TableValueIRBL'>"+values+"</td>";
                    }
                    else
                    {
                            ColCount27++;
                            dedupecheck_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                    }            
            }
            dedupecheck_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Dedupe Check Grid Details--"+dedupecheck_details);

            //End of Dedupe Table Tag******************************

            
            //Start of Blacklist Internal Table Tag******************************
            iRBL.mLogger.debug("Inside Blacklist Check Grid Details section --->");

            String BlacklistCheck_GridTable = properties.getProperty("BlacklistCheck_Grid_Table");
            String blacklistcheck_GridColName = properties.getProperty("BlacklistCheck_Grid_Col_Name");
            String BlacklistCheckGridColumn[] = blacklistcheck_GridColName.split(",");

            String blacklistcheck_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+BlacklistCheck_GridTable+"'  class='tableborder1'>";

            blacklistcheck_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>CIF ID</td><td class='TableHeaderIRBL'>First Name</td><td class='TableHeaderIRBL'>Last Name</td><td class='TableHeaderIRBL'>Full Name</td><td class='TableHeaderIRBL'>Emirates ID</td><td class='TableHeaderIRBL'>Remarks</td><td class='TableHeaderIRBL'>Match Status</td></tr>";
            int tablecount28 = iformObj.getDataFromGrid("Q_USR_0_IRBL_BLACKLIST_GRID_DTLS").size();
            for (int i = 0; i< tablecount28; i++)
            {
               int j=0;

               for(String GridColumn28 : BlacklistCheckGridColumn)
                {
                    mapCheckBlacklistCheck.put(GridColumn28+i,iformObj.getTableCellValue(BlacklistCheck_GridTable,i, j).toString());
                    j++;
                }
            }             
            iRBL.mLogger.debug("mapCheckBlacklistCheck size x--"+mapCheckBlacklistCheck.size());

            blacklistcheck_details += "<tr>";

            int ColCount28=0;
            for (Map.Entry<String,String> entry : mapCheckBlacklistCheck.entrySet()) 
            {
                String key = entry.getKey();
                String values = entry.getValue();
                values = values.replaceAll((char)(13)+"", "<br/>");

                String val28=String.valueOf(ColCount28);

                if(key.indexOf(val28) != -1)
                {
                    blacklistcheck_details += "<td class='TableValueIRBL'>"+values+"</td>";
                }
                else
                {
                    ColCount28++;
                    blacklistcheck_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                }            
            }
            blacklistcheck_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Blacklist Check Grid Details--"+blacklistcheck_details);

            //End of Blacklist Internal Table Tag******************************

            //Start of Blacklist External Table Tag******************************
            iRBL.mLogger.debug("Inside Blacklist Ext Grid Details section --->");

            String BlacklistExt_GridTable = properties.getProperty("BlacklistExt_Grid_Table");
            String blacklistext_GridColName = properties.getProperty("BlacklistExt_Grid_Col_Name");
            String BlacklistExtGridColumn[] = blacklistext_GridColName.split(",");

            String blacklistext_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+BlacklistExt_GridTable+"'  class='tableborder1'>";

            blacklistext_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>Full Name</td><td class='TableHeaderIRBL'>Passport No.</td><td class='TableHeaderIRBL'>Master Data ID</td><td class='TableHeaderIRBL'>External Blacklist</td><td class='TableHeaderIRBL'>External Blacklist Date</td><td class='TableHeaderIRBL'>External Blacklist Remarks</td><td class='TableHeaderIRBL'>Match Status</td></tr>";
            int tablecount29 = iformObj.getDataFromGrid("Q_USR_0_IRBL_BLACKLIST_EXT_DTLS").size();
            for (int i = 0; i< tablecount29; i++)
            {
                int j=0;

               for(String GridColumn29 : BlacklistExtGridColumn)
                {
                    mapCheckBlacklistExt.put(GridColumn29+i,iformObj.getTableCellValue(BlacklistExt_GridTable,i, j).toString());
                    j++;
                }
            }              
            iRBL.mLogger.debug("mapCheckBlacklistExt size x--"+mapCheckBlacklistExt.size());

            blacklistext_details += "<tr>";

            int ColCount29=0;
            for (Map.Entry<String,String> entry : mapCheckBlacklistExt.entrySet()) 
            {
                String key = entry.getKey();
                String values = entry.getValue();
                values = values.replaceAll((char)(13)+"", "<br/>");

                String val29=String.valueOf(ColCount29);

                if(key.indexOf(val29) != -1)
                {
                    blacklistext_details += "<td class='TableValueIRBL'>"+values+"</td>";
                }
                else
                {
                    ColCount29++;
                    blacklistext_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                }            
            }
            blacklistext_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Blacklist Ext Grid Details--"+blacklistext_details);


            //End of Blacklist External Table Tag******************************
            
            //Start of Firco Check Table Tag******************************
            iRBL.mLogger.debug("Inside Firco Check Grid Details section --->");

            String FircoCheck_GridTable = properties.getProperty("FircoCheck_Grid_Table");
            String fircocheck_GridColName = properties.getProperty("FircoCheck_Grid_Col_Name");
            String FircoCheckGridColumn[] = fircocheck_GridColName.split(",");

            String fircocheck_details = "<br/><table cellspacing='1' cellpadding='1' id ='"+FircoCheck_GridTable+"'  class='tableborder1'>";

            fircocheck_details+="<tr class='TableHeaderBgIRBL'><td class='TableHeaderIRBL'>S.No</td><td class='TableHeaderIRBL'>OFAC ID</td><td class='TableHeaderIRBL'>Details For</td><td class='TableHeaderIRBL'>Matching Text</td><td class='TableHeaderIRBL'>Name</td><td class='TableHeaderIRBL'>Origin</td><td class='TableHeaderIRBL'>Designation</td><td class='TableHeaderIRBL'>Date of Birth</td><td class='TableHeaderIRBL'>User Data 1</td><td class='TableHeaderIRBL'>Nationality</td><td class='TableHeaderIRBL'>Passport</td><td class='TableHeaderIRBL'>Additional Info</td><td class='TableHeaderIRBL'>Reference No</td><td class='TableHeaderIRBL'>Match Status</td><td class='TableHeaderIRBL'>Remarks</td><td class='TableHeaderIRBL'>User</td></tr>";
            int tablecount30 = iformObj.getDataFromGrid("Q_USR_0_IRBL_FIRCO_GRID_DTLS").size();
            for (int i = 0; i< tablecount30; i++)
            {
                int j=0;

               for(String GridColumn30 : FircoCheckGridColumn)
                {
                    mapCheckFircoCheck.put(GridColumn30+i,iformObj.getTableCellValue(FircoCheck_GridTable,i, j).toString());
                    j++;
                }
            }           
            iRBL.mLogger.debug("mapCheckFircoCheck size x--"+mapCheckFircoCheck.size());

            fircocheck_details += "<tr>";

            int ColCount30=0;
            for (Map.Entry<String,String> entry : mapCheckFircoCheck.entrySet()) 
            {
                String key = entry.getKey();
                String values = entry.getValue();
                values = values.replaceAll((char)(13)+"", "<br/>");

                String val30=String.valueOf(ColCount30);

                if(key.indexOf(val30) != -1)
                {
                    fircocheck_details += "<td class='TableValueIRBL'>"+values+"</td>";
                }
                else
                {
                    ColCount30++;
                    fircocheck_details += "</tr><tr><td class='TableValueIRBL'>"+values+"</td>";
                }            
            }
            fircocheck_details += "</tr></table><br/>";
            iRBL.mLogger.debug("Firco Check Grid Details--"+fircocheck_details);
            //End of Firco Check Table Tag******************************

            //Start of Additional Security Details Column Tag******************************
            iRBL.mLogger.debug("Inside Additional Security Details Column --->");
           
            String AddlSecurityGridColumn[] = iformObj.getValue("Q_USR_0_IRBL_ADDL_SECUTRITY_DTLS").toString().split(",");
            int columnSize1 = AddlSecurityGridColumn.length;
            String addlSecurity_details = "";
            
            for(int i=0; i<columnSize1; i++)
			{
				addlSecurity_details=addlSecurity_details.concat(",").concat(AddlSecurityGridColumn[i]);
				iRBL.mLogger.debug("Additional Security Details Column--"+addlSecurity_details);
				addlSecurity_details=addlSecurity_details.replaceAll("[\\[\\](){}]","");
				addlSecurity_details=addlSecurity_details.replaceAll("\"","");
			}				
			 iRBL.mLogger.debug("Additional Security Details Column--"+addlSecurity_details);
			//End of Additional Security Details Column Tag********************************
            
			//Start of EUM Proposed Details Column Tag*************************************
            iRBL.mLogger.debug("Inside EUM Proposed Details Column --->");
            
            String EUMProposedGridColumn[] = iformObj.getValue("Q_USR_0_IRBL_EUMPROPOSED_DTLS").toString().split(",");
        	int columnSize2 = EUMProposedGridColumn.length;
			
			String eum_proposed_details="";
			for(int i=0; i<columnSize2; i++)
			{
				eum_proposed_details=eum_proposed_details.concat(",").concat(EUMProposedGridColumn[i]);
				eum_proposed_details=eum_proposed_details.replaceAll("[\\[\\](){}]","");
				eum_proposed_details=eum_proposed_details.replaceAll("\"","");
			}
			iRBL.mLogger.debug("EUM Proposed Details Column--"+eum_proposed_details);
			//End of EUM Proposed Details Column Tag*******************************************
			
            
			//Replacing Application details grid**
			for(Map.Entry<String, String> m:mapCheckApplicationDet.entrySet())
			{
				replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
			}
			//*************************************

			//Replacing Financial Eligibility Details grid**
			for(Map.Entry<String, String> m:mapCheckFinEligibility.entrySet())
			{
				replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
			}
			//*************************************

			//Replacing Laon details grid**
            for(Map.Entry<String, String> m:mapCheckLoanDetails.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************
            
          //Replacing CBRB details grid**
            for(Map.Entry<String, String> m:mapCheckCBRBNotes.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Tele - Verification Notes details grid**
            for(Map.Entry<String, String> m:mapCheckTVNotes.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Summary of Deviation Notes details grid**
            for(Map.Entry<String, String> m:mapCheckSummaryNotes.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Policy Checklist Notes details grid**
            for(Map.Entry<String, String> m:mapCheckPolicyCheckNotes.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing LOS Channel details grid**
            for(Map.Entry<String, String> m:mapCheckLOSChannel.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Analyst Remarks details grid**
            for(Map.Entry<String, String> m:mapCheckAnalystRemarks.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Any Profile Change details grid**
            for(Map.Entry<String, String> m:mapCheckProfileChange.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Reject Entry details grid**
            for(Map.Entry<String, String> m:mapCheckRejectEntry.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing TESS Loan History details grid**
            for(Map.Entry<String, String> m:mapCheckTESSLoan.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Addl Checks Notes details grid**
            for(Map.Entry<String, String> m:mapCheckAddlChecksNotes.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Business Account Opening details grid**
            for(Map.Entry<String, String> m:mapCheckBAOpening.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing CPV details grid**
            for(Map.Entry<String, String> m:mapCheckCPV.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing Memopad details grid**
            for(Map.Entry<String, String> m:mapCheckMemopadDet.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing CROPS FIELDS details grid**
            for(Map.Entry<String, String> m:mapCheckCropsDet.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing ADDITIONAL FIELDS details grid**
            for(Map.Entry<String, String> m:mapCheckAddlDet.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            //Replacing MACHINEID details grid**
            for(Map.Entry<String, String> m:mapCheckAddlDet.entrySet())
            {
                    replacedString=replacedString.replaceAll("<&"+m.getKey()+"&>",m.getValue());
            }
            //*************************************

            replacedString=replacedString.replaceAll("<&DEFERRAL_DETAILS&>",deferral_details);			
            replacedString=replacedString.replaceAll("<&LOANS_DETAILS&>",loans_details);	
            replacedString=replacedString.replaceAll("<&TOTAL_BORROWINGS_COMPAMY&>",total_borr_com_details);			
            replacedString=replacedString.replaceAll("<&TOTAL_BORR_OWNER_SISTER_COMPANIES&>",total_borr_owner_details);		
            replacedString=replacedString.replaceAll("<&CONDUCTED_HISTORY_MAIN&>",conduct1_details);			
            replacedString=replacedString.replaceAll("<&CONDUCTED_HISTORY_DETAILS&>",conduct2_details);	
            replacedString=replacedString.replaceAll("<&SIGNATORY_DETAILS&>",signatory_details);
            replacedString=replacedString.replaceAll("<&EVAL_CHECKS_AECB&>",evalAECB_details);
            replacedString=replacedString.replaceAll("<&EVAL_CHECKS_FTS_EVALUATION&>",evalFTS_details);
            replacedString=replacedString.replaceAll("<&CBRB_CHECK&>",cbrb_details);
            replacedString=replacedString.replaceAll("<&TELE_VERIFICATION_CHECKS&>",tvchecks_details);	
            //replacedString=replacedString.replaceAll("<&SYS_CHECKS_PERFORMED_DETAILS&>",sysperformed_details);	
            //replacedString=replacedString.replaceAll("<&EXCEPTION_HISTORY&>",excHist_details);	
            replacedString=replacedString.replaceAll("<&ELIGIBLE_AMOUNT&>",elamount_details);	
            replacedString=replacedString.replaceAll("<&INDICATIVE_DBR&>",idbr_details);	
            replacedString=replacedString.replaceAll("<&ADDITIONAL_CHECKS&>",addlchecks_details);			
            replacedString=replacedString.replaceAll("<&BASIC_LEAD_FILTRATION_DETAILS&>",basiclf_details);
            replacedString=replacedString.replaceAll("<&POLICY_CHECKLIST&>",policychecklist_details);
            replacedString=replacedString.replaceAll("<&MACHINEID_DETAILS&>",machineid_details);
            replacedString=replacedString.replaceAll("<&SUMMARY_OF_DEVIATION&>",sdeviation_details);
            replacedString=replacedString.replaceAll("<&DOCUMENT_CHECKLIST&>",docchecklist_details);
            replacedString=replacedString.replaceAll("<&INDUSTRYCODE_DETAILS&>",industryCode_details);
            replacedString=replacedString.replaceAll("<&DECISION_HISTORY_DETAILS&>",wiHist_details);
            replacedString=replacedString.replaceAll("<&MEMOPAD_DETAILS&>",memopad_details);
            replacedString=replacedString.replaceAll("<&CHECKS_MAIN&>",maincheck_details);
            replacedString=replacedString.replaceAll("<&DEDUPE_CHECK&>",dedupecheck_details);
            replacedString=replacedString.replaceAll("<&BLACKLIST_CHECK&>",blacklistcheck_details);
            replacedString=replacedString.replaceAll("<&BLACKLIST_EXT&>",blacklistext_details);
            replacedString=replacedString.replaceAll("<&FIRCO_CHECK&>",fircocheck_details);
            
            //For MultiSelect Columns:
            replacedString=replacedString.replaceAll("<&ADDITIONAL_SECURITY&>",addlSecurity_details);
            replacedString=replacedString.replaceAll("<&EUM_PROPOSED&>",eum_proposed_details);
            
			String OutputWriteString = writeFileFromServer(generateddocPath,replacedString);
			iRBL.mLogger.debug("HTML Generated--"+OutputWriteString);
			
			//String UniqueNo = Long.toString((new Date()).getTime());
			String dynamicPdfName = getWorkitemName()+ "template"+UniqueNo+".pdf";
			
			String generatedpdfPath = properties.getProperty("iRBL_GENERATED_PDF_PATH");//Get the location of the path where generated template will be saved
            generatedpdfPath += dynamicPdfName;
            generatedpdfPath = tempDir + generatedpdfPath;//Complete path of generated document
            iRBL.mLogger.debug("\nTemplate Doc generatedpdfPath :" + generatedpdfPath);
			
			//String htmlSource = "D:\\iRBL-0000000101-processtemplate.html";
		    //String destPdf = "D:\\iRBL-0000000101-processtemplate2134546896.pdf";
		    //String cssSource = "D:\\formatting.css";
		    
			String cssSource = tempDir + properties.getProperty("iRBL_TEMPLATE_CSS_PATH");
            iRBL.mLogger.debug("\nTemplate cssSource Path :" + cssSource);
			
			try
			{
				ByteArrayInputStream html = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(generateddocPath)));
				ByteArrayInputStream css = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(cssSource)));
				//html = getHtmlByteArrayStream(); //this is only for getting picture not necessary 
				
				Document document = new Document();
				PdfWriter writer = null;
				try 
				{
					writer = PdfWriter.getInstance(document, new FileOutputStream(generatedpdfPath));
				} 
				catch (DocumentException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try 
				{
					writer.setInitialLeading(12);
				} 
				catch (DocumentException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				document.open();
				XMLWorkerHelper.getInstance().parseXHtml(writer, document, html, css);
				document.close();
					
				try
				{
					//For Adding Doc to WI*******************
					String DocName = "Single_View";
					returnValue = AttachDocumentWithWI(iformObj,pid,dynamicPdfName,DocName);
					//***************************************
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			catch(Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		    
			iRBL.mLogger.debug("\nFinal PDF Generated successfully !");
        }
    return returnValue;
 }




public String AttachDocumentWithWI(IFormReference iformObj,String pid,String dynamicPdfName,String DocumentName)
{
	String docxml="";
	String documentindex="";
	String doctype="";
	try
	{			
		iRBL.mLogger.debug("inside ODAddDocument");		
		
		iRBL.mLogger.debug("Proess Instance Id: "+pid);
		
		String sCabname=getCabinetName();
		iRBL.mLogger.debug("sCabname"+sCabname);
		
		String sSessionId = getSessionId();
		iRBL.mLogger.debug("sSessionId"+sSessionId);
		
		String sJtsIp = iformObj.getServerIp();
		
		int iJtsPort_int =Integer.parseInt(iformObj.getServerPort());
		
		//String volume id="1";
		//String sPath="";
		//String dynamicPdfName="";
		
		Properties properties = new Properties();
		try 
        {
            properties.load(new FileInputStream(System.getProperty("user.dir")+ System.getProperty("file.separator")+"CustomConfig"+System.getProperty("file.separator")+ "iRBL.properties"));
        } 
        catch (IOException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		String tempDir = System.getProperty("user.dir");
		String generatedpdfPath = properties.getProperty("iRBL_GENERATED_PDF_PATH");//Get the location of the path where generated template will be saved
        generatedpdfPath += dynamicPdfName;
        generatedpdfPath = tempDir + generatedpdfPath;//Complete path of generated document
        iRBL.mLogger.debug("\nTemplate Doc generatedpdfPath :" + generatedpdfPath);
		
		docxml = SearchExistingDoc(iformObj,pid,DocumentName,sCabname,sSessionId,sJtsIp,iJtsPort_int,generatedpdfPath);
		iRBL.mLogger.debug("Final Document Output: "+docxml);
		documentindex = getTagValue(docxml,"DocumentIndex");
		if(getTagValue(docxml,"Option").equalsIgnoreCase("NGOChangeDocumentProperty")) 
		{
			doctype="deleteadd";
		} 
		else 
		{
			doctype="new";
		}
		iRBL.mLogger.debug(docxml+"~"+documentindex+"~"+doctype+"~"+dynamicPdfName);
		String Output="0000~"+docxml+"~"+documentindex+"~"+doctype+"~"+dynamicPdfName;
		return Output;
	} 
	catch (Exception e) 
	{
		iRBL.mLogger.debug("Exception while adding the document: "+e);
		return "Exception while adding the document: "+e;
	}
}


}