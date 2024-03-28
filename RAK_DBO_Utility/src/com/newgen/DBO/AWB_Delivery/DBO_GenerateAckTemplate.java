package com.newgen.DBO.AWB_Delivery;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;  
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.zxing.BarcodeFormat;  
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatWriter;  
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.WriterException;  
import com.google.zxing.client.j2se.MatrixToImageWriter;  
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.MultiFormatReader;  
import com.google.zxing.Result;  
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;  
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.io.FileInputStream;  
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.niplj.codec.pdf.PDF;
import com.newgen.niplj.generic.NGIMException;
import com.newgen.omni.jts.cmgr.XMLParser;



public class DBO_GenerateAckTemplate 
{
	public static Logger DBO_GenerateAckTemplateLog=null;
	public DBO_GenerateAckTemplate(Logger DBO_GenerateAckTemplateLogName)
	{
		DBO_GenerateAckTemplateLog=DBO_GenerateAckTemplateLogName;
	}
	
	public String generateAckTemplate(String pdfName, String templateSrcPath, String templateDestPath, String processInstanceID,HashMap<String,String> RelatedPartyData , String sessionId)
	throws IOException, Exception {
		
		//String attrbList = "";
		String Output = "";
		DBO_GenerateAckTemplateLog.debug("Inside the generate_template Method DBO: ");
		
		/*String prop_file_loc = System.getProperty("user.dir") + System.getProperty("file.separator") + "ConfigFiles"
		+ System.getProperty("file.separator") + "DBO_TemplateGen_Config.properties"; 
		DBO_GenerateAckTemplateLog.debug("prop_file_loc: " + prop_file_loc);
		
		File file = new File(prop_file_loc);
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();
		
		String gtIP = properties.getProperty("gtIP");
		DBO_GenerateAckTemplateLog.debug("gtIP: " + gtIP);
		
		String gtPortProperty = properties.getProperty("gtPortProperty");
		DBO_GenerateAckTemplateLog.debug("gtPortProperty: " + gtPortProperty);
		
		int gtPort = Integer.parseInt(gtPortProperty);
		DBO_GenerateAckTemplateLog.debug("gtPort: " + gtPort);
		
		//*********************** query to fetch detail from db*************************
		//to do    add condition rp 
		String tbQuery = "select FirstName + ' ' + IsNULL(MiddleName,'') + ' ' + LastName AS StakeholderName, Nationality AS StakeholderNationality, EmirateID AS StakeholderEmiratesID from USR_0_DBO_RelatedPartyGrid with (NOLOCK) where WINAME ='" + processInstanceID + "'"; 		
		DBO_GenerateAckTemplateLog.debug("tbQuery : " + tbQuery);
		
		String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(tbQuery, CommonConnection.getCabinetName(),sessionId);
		DBO_GenerateAckTemplateLog.debug("extTabDataIPXML template: " + extTabDataIPXML);
		
		String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		DBO_GenerateAckTemplateLog.debug("extTabDataOPXML template: " + extTabDataOPXML);

		XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
		DBO_GenerateAckTemplateLog.debug("xmlParserData template: " + xmlParserData);
		
		String strMainCode = xmlParserData.getValueOf("MainCode");
		DBO_GenerateAckTemplateLog.debug("apSelectWithColumnNames for main code: "+strMainCode);
		
		String RetrievedCount = xmlParserData.getValueOf("TotalRetrieved");
		DBO_GenerateAckTemplateLog.debug("RetrievedCount for apSelectWithColumnNames Call for retr: "+RetrievedCount);*/

		//if condition
		try {	
			if (!"".equalsIgnoreCase(pdfName) && pdfName != null)
			{
				/*Date d = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String CurrentDateTime = dateFormat.format(d);*/
				
				// Generating template
				Map<String,String> XMLMap = new HashMap<String,String>();
				DBO_GenerateAckTemplateLog.debug("CompanyShortName: " +  RelatedPartyData.get("CompanyShortName"));
				XMLMap.put("CompanyName", RelatedPartyData.get("CompanyShortName"));
				DBO_GenerateAckTemplateLog.debug("ProductTypeEnglish: " +  RelatedPartyData.get("ProductType"));
				XMLMap.put("ProductTypeEnglish", RelatedPartyData.get("ProductType"));
				
				DBO_GenerateAckTemplateLog.debug("ProductTypeArabic: " +  RelatedPartyData.get("ProductType"));
				XMLMap.put("ProductTypeArabic", RelatedPartyData.get("ProductType"));
				
				DBO_GenerateAckTemplateLog.debug("StakeholderName: " +  RelatedPartyData.get("FullName"));
				XMLMap.put("SignatoryAuthority", RelatedPartyData.get("FullName"));
				
				DBO_GenerateAckTemplateLog.debug("ProspectID: " +  RelatedPartyData.get("ProspectID"));
				XMLMap.put("ProspectId", RelatedPartyData.get("ProspectID"));
				if("".equalsIgnoreCase(RelatedPartyData.get("EmirateID")))
				{
					DBO_GenerateAckTemplateLog.debug("PassportNumber: " +  RelatedPartyData.get("PassportNumber"));
					XMLMap.put("PassportNo", RelatedPartyData.get("PassportNumber"));
				}
				else
				{
					DBO_GenerateAckTemplateLog.debug("EmirateID: " +  RelatedPartyData.get("EmirateID"));
					XMLMap.put("PassportNo", RelatedPartyData.get("EmirateID"));
				}
				
				if("Y".equalsIgnoreCase(RelatedPartyData.get("IsSignatory")) && "Y".equalsIgnoreCase(RelatedPartyData.get("IsShareholder")) )
				{
					XMLMap.put("AuthorityToSign", "Shareholder and Authorized Signatory");
				}
				else if("Y".equalsIgnoreCase(RelatedPartyData.get("IsSignatory")))
				{
					DBO_GenerateAckTemplateLog.debug("AuthorityToSign: " +  RelatedPartyData.get("IsSignatory"));
					XMLMap.put("AuthorityToSign", "Authorized Signatory");
				}
				else if("Y".equalsIgnoreCase(RelatedPartyData.get("IsShareholder")))
				{
					DBO_GenerateAckTemplateLog.debug("Nationality: " +  RelatedPartyData.get("Nationality"));
					XMLMap.put("AuthorityToSign", "Shareholder");
				}
				/*else
				{
					XMLMap.put("AuthorityToSign", "Authorized Signatory");
				}*/
				
				Date date = new Date();
				DateFormat EntryDate = new SimpleDateFormat("dd-MM-yyyy");
				String currDate = EntryDate.format(date);
				DBO_GenerateAckTemplateLog.debug("DateEnglish: " +  currDate);
				XMLMap.put("DateEnglish", currDate);
				DBO_GenerateAckTemplateLog.debug("DateArabic: " + currDate);
				XMLMap.put("DateArabic", currDate);/*
				DBO_GenerateAckTemplateLog.debug("EmirateID: " +  RelatedPartyData.get("EmirateID"));
				XMLMap.put("StakeholderEmiratesIdArabic", RelatedPartyData.get("EmirateID"));
				DBO_GenerateAckTemplateLog.debug("Nationality: " +  RelatedPartyData.get("Nationality"));
				XMLMap.put("StakeholderNationalityArabic", RelatedPartyData.get("Nationality"));*/
				
				Output = CreatePDF(pdfName, templateSrcPath, templateDestPath, RelatedPartyData.get("AWBNumber"), XMLMap, processInstanceID,RelatedPartyData.get("RelatedPartyID"), RelatedPartyData.get("ProspectID"), RelatedPartyData.get("FullName")); // Work -> Generated PDF Path
				// Attach document (No Need)
				DBO_GenerateAckTemplateLog.debug("output for template "+ pdfName +":-" + Output);
			}
			else
			{				
				DBO_GenerateAckTemplateLog.debug("main code is not 0 try again :");
			}
		}
		catch (Exception e)
		{
			DBO_GenerateAckTemplateLog.debug("Exception: "+e.getMessage());
		}
		
		return Output;
	}
	
	
	public String CreatePDF(String Template_Filename, String templateSrcPath, String templateDestPath, String AwbNo, Map<String, String> XMLMap, String processInstanceID,String RPID, String ProspectID, String FullName) 
	{
		String finalPdfPath = "";
        try {
			
            if (!Template_Filename.equals("")) 
            {
                String pdfTemplatePath = templateSrcPath + File.separator+ Template_Filename + ".pdf";
                finalPdfPath = templateDestPath+File.separator+"WithoutQR"; // toDo
                File objDestFolder = new File(finalPdfPath);
    			if (!objDestFolder.exists())
    			{
    				objDestFolder.mkdirs();
    			}
    			
    			String winameForFile = processInstanceID.split("-")[0]+processInstanceID.split("-")[1].replaceFirst("^0+(?!$)", "");
    			
    			finalPdfPath=finalPdfPath+File.separator+ProspectID+"_"+winameForFile+"_"+AwbNo+"_"+RPID+"_"+FullName+".pdf";
    			//finalPdfPath=finalPdfPath+File.separator+RPID+"_"+Template_Filename+".pdf";
                DBO_GenerateAckTemplateLog.debug("finalPdfName : " + finalPdfPath);				

                DBO_GenerateAckTemplateLog.debug("createPDF : SourcePath :" + pdfTemplatePath);
                
                if("Success".equalsIgnoreCase(generatePDF(pdfTemplatePath, finalPdfPath, XMLMap)))
                {
                	DBO_GenerateAckTemplateLog.debug("createPDF : Single pager Generated Succesfully at location :" + finalPdfPath);
                	return finalPdfPath;
                }
                else
                return "Error in generate PDF";
             } 
            else 
            {
            	DBO_GenerateAckTemplateLog.debug("Error : NO document name maintained for this product_name: ");
				return "failed : No file maintained for this product";
            }
        } 
        catch (Exception e)
        {
            DBO_GenerateAckTemplateLog.debug("createPDF : ex.getMessage() :" + e.getMessage());
            e.printStackTrace();
			
			return "failed : Exception";
        }
	}
	
	public String generatePDF(String sourceName, String destPath, Map<String, String> ht)
	{
		
        DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : start :");
        DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : ht.size() :" + ht.size());
		
        try {
        	
            PdfReader reader = new PdfReader(sourceName);
            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : Created reader object from source template pdf:");

            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(destPath));
            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : Created stamper object in destination pdf:");

            AcroFields form = stamp.getAcroFields();
            
            BaseFont unicode = BaseFont.createFont("./SinglePagerGeneration/Templates/arabtype.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : Created arabtype font:");

            ArrayList<BaseFont> al = new ArrayList<BaseFont>();
            al.add(unicode);

            form.setSubstitutionFonts(al);

            PdfWriter p = stamp.getWriter();
            p.setRunDirection(p.RUN_DIRECTION_RTL);
			
            BaseFont bf1 = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.EMBEDDED);
            form.addSubstitutionFont(bf1);
            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF :  Created writer, set font times roman :");
            
            Set<String> PDFSet = ht.keySet();
            Iterator<String> PDFIt = PDFSet.iterator();
            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : Replacing values from XMLMap:");
			
            while (PDFIt.hasNext()) {
                String HT_Key = (String) PDFIt.next();
                String HT_Value = (String) ht.get(HT_Key);
                form.setField(HT_Key, HT_Value);
            }

            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : Values replaced from XMLMap:");
            stamp.setFormFlattening(true);
			
            stamp.close();
            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : Stamper closed:");
            return "Success";

        } 
        catch (Exception ex) 
        {
            DBO_GenerateAckTemplateLog.debug("createPDF : createNewPDF : ex.getMessage() : 2 :" + ex.getMessage());
            return "Error";
        }
		
    }
	
	// QR logic starts from here
	public String generateAttachQR(String generatedPDFPath, String srcQRPath, HashMap<String,String> informationMap)
			throws WriterException, IOException, NGIMException, NotFoundException, ChecksumException, FormatException 
	{
		String charset = "UTF-8";  
		Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();  
		//generates QR code with Low level(L) error correction capability  
		hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  
		String information = informationMap.get("WINAME")+"~"+informationMap.get("AWBNumber")+"~"+informationMap.get("RelatedPartyID")+"~"+informationMap.get("FullName");
		
		File objDestFolder = new File(srcQRPath);
		if (!objDestFolder.exists())
		{
			objDestFolder.mkdirs();
		}
		srcQRPath=srcQRPath+File.separator+informationMap.get("RelatedPartyID")+"_QRimage.png";
		//invoking the user-defined method that creates the QR code  
		generateQRcode(information, srcQRPath, charset, hashMap, 70,70); // increase or decrease height and width accordingly(default were 150,0,50,50)   
		//prints if the QR code is generated 
		System.out.println("QR Code created successfully.");  
		
		// Start - Below block added to create border and static bottom text in QR Code Image added on 07092023 by Angad
		/*final BufferedImage image = ImageIO.read(new File(srcQRPath));
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Graphics g = image.getGraphics();
		g.setFont(g.getFont().deriveFont(9f));
		Color textColor = Color.BLACK;
		g.setColor(textColor);
		g.drawRect(0, 0, image.getWidth()-1, image.getHeight()-1);
		g.drawString("For Bank Use", 5, 68);
		g.dispose();
		ImageIO.write(image, "png", new File(srcQRPath));		
		DBO_GenerateAckTemplateLog.debug("QR Code created successfully.");*/
		//End - Below block added to create border and static bottom text in QR Code Image added on 07092023 by Angad
				
		/*charset = "UTF-8";  
		Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();  
		//generates QR code with Low level(L) error correction capability  
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);*/
		
		if("Attached".equalsIgnoreCase(attachQRcode(generatedPDFPath, srcQRPath)))
		{
			return "QR_Code_GenAttach_Success";
		}
		else
		{
			return "QR_Code_GenAttach_Error";
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void generateQRcode(String data, String path, String charset, Map map, int h, int w) throws WriterException, IOException  
	{  
		//the BitMatrix class represents the 2D matrix of bits  
		//MultiFormatWriter is a factory class that finds the appropriate Writer subclass for the BarcodeFormat requested and encodes the barcode with the supplied contents.  
		BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);  
		MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
	}
	
	public static String readQRcode(String path, String charset, Map map) throws FileNotFoundException, IOException, NotFoundException  
	{  
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(path)))));  
		Result rslt = new MultiFormatReader().decode(binaryBitmap);  
		return rslt.getText();  
	} 
	
	public static String attachQRcode(String tempraroryPathForPDF, String qrcodepath) throws NGIMException, IOException
	{
		String status="";
		
		PDF pdf = null;
		pdf = PDF.getPDFInEditMode(tempraroryPathForPDF , "");
		try {
			java.awt.Rectangle rectPostiontion = new java.awt.Rectangle();
			rectPostiontion.x = 260;
			rectPostiontion.y = 1;
			rectPostiontion.height = 70;
			rectPostiontion.width = 70;//(we generate 75 ,75 and compress it to 55,55)
						
			
			File fnew=new File(qrcodepath);
			BufferedImage originalImage=ImageIO.read(fnew);
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			ImageIO.write(originalImage, "png", baos );
			byte[] imageInByte=baos.toByteArray();
			
			pdf.writeImageWatermark(imageInByte, rectPostiontion, 1, 1);
			
			if (pdf != null)
				pdf.close();
			
			System.out.println("QR Code added to PDF successfully");
			status="Attached";	
			//byte[] generatedPDFByteData = CommonFunctions.getByteArrayFromLocalPath(tempraroryPathForPDF);

		} 
		catch (NGIMException ex) 
		{
			System.out.println("NGIMException Encountered in writeImageWatermark()");
			status="NotAttached";
		} 
		catch (InterruptedException e) 
		{
			System.out.println("InterruptedException Encountered in writeImageWatermark()");
			status="NotAttached";
		}
		catch (IOException e) 
		{
			System.out.println("IOException Encountered in writeImageWatermark()");
			status="NotAttached";
		} 
		finally 
		{
			if (pdf != null)
				pdf.close();
		}
		return status;
	}
	
	/*public static void readQRcodePdf(String pdffilepath) throws IOException{
		
		java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
		try{
			File pdfFile = new File(pdffilepath);
			System.out.println("inside readQRcodePdf");
			FileInputStream input = new FileInputStream(pdfFile);
			PDDocument testDoc = PDDocument.load(input);
			List<PDPage> pages = testDoc.getDocumentCatalog().getAllPages();
				
			PDPage page = (PDPage)pages.get(0);
			System.out.println("inside for get all pages");		
			BufferedImage image = page.convertToImage();
			System.out.println("after convert to image");
				
			 BufferedImage cropedImage = image.getSubimage(0, 0, 914, 400);
			 System.out.println("after cropedImage");
		     // using the cropedImage instead of image
		     LuminanceSource source1 = new BufferedImageLuminanceSource(cropedImage);
		     BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source1));
		     // bar code decoding
		     QRCodeReader reader1 = new QRCodeReader();
		     Result result1 = null;		    
		     try 
		     {
		    	 System.out.println("before decode ");
		         result1 = reader1.decode(bitmap);
		         System.out.println("after decode ");
		       			     } 
		     catch (ReaderException e) 
		     {
		         System.out.println("reader error to decode"); 
		     }
		     
		    if(result1.getText()!=null){
		    	 System.out.println("The data in the qr cod is  :"+result1.getText());
		    	System.out.println("ready to make directory");
//		    	return result1;
		    }
			
		    
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
		
	}*/
	
	public static void readpdfscanned(String pdffilepath,Map hintMap) throws IOException, NotFoundException, ChecksumException, FormatException{
		
		java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
		Result result = null;
		QRCodeMultiReader reader = new QRCodeMultiReader();
		
		File pdfFile = new File(pdffilepath);
		System.out.println("inside readQRcodePdf");
		FileInputStream input = new FileInputStream(pdfFile);
		PDDocument testDoc = PDDocument.load(input);
		List<PDPage> pages = testDoc.getDocumentCatalog().getAllPages();
		PDPage page = (PDPage)pages.get(0);
		BufferedImage image = page.convertToImage();
		
		 BufferedImage imagToBeDecoded = image.getSubimage(100, 10,  400, 400);
		//BufferedImage imagToBeDecoded = image.getSubimage(800, 60,  300, 200);
		File imgfilepath = new File("D:\\rakdocument\\pdf qr code\\image.png");
		
		ImageIO.write(imagToBeDecoded, "png", imgfilepath);
		System.out.println("done creating image");
		BinaryBitmap binaryBitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(imagToBeDecoded)));
		 Result qrCodeResult = null;
		 try{
			 	System.out.println("inside try");
			 	qrCodeResult = new com.google.zxing.qrcode.QRCodeReader().decode(binaryBitmap,hintMap);
			 	System.out.println("data is: "+qrCodeResult.getText()); 
			 	//imgfile.delete();
		 }
		 catch(NotFoundException | FormatException e){ //attempt without hints
			 System.out.println("inside catch");
			 	qrCodeResult = new com.google.zxing.qrcode.QRCodeReader().decode(binaryBitmap);
			 	System.out.println("exception"+e.toString());
	     }
			
		
	}
		
	public static void readtiff(String tiffpath,Map hintMap) throws IOException, ChecksumException, NotFoundException, FormatException{
		try{
		InputStream barCodeInputStream = new FileInputStream(tiffpath);  
	    BufferedImage barCodeBufferedImage = ImageIO.read(barCodeInputStream);  
	    
		File imgfile = new File(tiffpath);
		System.out.println("imgfile  "+imgfile);
		BufferedImage image=ImageIO.read(imgfile);
		 BufferedImage cropedImage = image.getSubimage(1654,-800,100,100 );
		 System.out.println("fatra");
         LuminanceSource source = new BufferedImageLuminanceSource(cropedImage);
		    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
		    Reader reader = new MultiFormatReader();  
		    Result result;
		
		        result = reader.decode(bitmap);
		        System.out.println("Barcode text is " + result.getText());
		    //  byte[] b = result.getRawBytes();
		    //  System.out.println(ByteHelper.convertUnsignedBytesToHexString(result.getText().getBytes("UTF8")));
		        //System.out.println(ByteHelper.convertUnsignedBytesToHexString(b));
		    } catch (NotFoundException e) {
		        // TODO Auto-generated catch block
		    	System.out.println("NotFoundException"+e.toString());
		        //e.printStackTrace();
		    } catch (ChecksumException e) {
		        // TODO Auto-generated catch block
		    	System.out.println("ChecksumException"+e.toString());
		       // e.printStackTrace();
		    } catch (FormatException e) {
		        // TODO Auto-generated catch block
		    	System.out.println("FormatException"+e.toString());
		       // e.printStackTrace();
		    }
		}
}
