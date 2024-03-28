package com.newgen.DBO.SignatureCrop;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.newgen.Common.CommonConnection;
import com.newgen.Common.CommonMethods;
import com.newgen.DBO.AWB_Delivery.DBO_AWB_Logs;
import com.newgen.niplj.NIPLJ;
import com.google.zxing.BinaryBitmap;
import com.newgen.niplj.codec.DecodeParam;
import com.newgen.niplj.codec.bmp.BmpEncodeParam;
import com.newgen.niplj.codec.gif.GifEncodeParam;
import com.newgen.niplj.codec.jpeg.JpegEncodeParam;
import com.newgen.niplj.codec.tif6.Tif6EncodeParam;
import com.newgen.niplj.fileformat.Tif6;
import com.newgen.niplj.generic.NGIMException;
import com.newgen.niplj.generic.NG_BufferedImageOperations;
import com.newgen.niplj.io.RandomInputStream;
import com.newgen.niplj.io.RandomInputStreamSource;
import com.newgen.niplj.io.RandomOutputStreamSource;
import com.newgen.niplj.operations.NG_SimpleImageProducer;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;

import org.apache.pdfbox.rendering.PDFRenderer;

public class DBO_SignatureCrop implements Runnable {
	
	static Map<String, String> DBO_SignCrop_cofigParamMap= new HashMap<String, String>();
	private static  String SegregatedDocument=null;
	private static  String SignedDeclarationDocs_Input=null;
	private static  String SignedDeclarationDocs_Error=null;
	private static  String SignedDeclarationDocs_temp=null;
	private static  String ReadyToAttach_Error=null;
	private static  String ReadyToAttach_Input=null;
	private static  String ReadyToAttach_Success=null;
	private static  String tempSignaturePath="";
	private static  String docTypeSignedDeclaration="";
	private static  String docTypeSignature="";
	private static  String cabinetName;
	private static  String jtsIP;
	private static String jtsPort;
	private static String smsPort;
	private int sleepIntervalInMin;
	private String queueID;
	private static String volumeID;
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
	private String RID=null;
	private String ws_name=null;
	public static String newFilename=null;
	private static String activityName = "Sys_Signature_Crop";
	Date now=null;
	Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>(); 
	private String QRFileReadFinalStatus="";
		
	public void run(){
		DBO_SignCrop_Logs.setLogger();
		try{
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();
		} catch(NGException e){
			e.printStackTrace();
		}
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Connecting to Cabinet.");
		int configReadStatus = readConfig();
		
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("configReadStatus "+configReadStatus);
		if(configReadStatus !=0){
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
		
		docTypeSignedDeclaration=DBO_SignCrop_cofigParamMap.get("DocTypeInProcessSignedDeclaration");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("docTypeSignedDeclaration: " + docTypeSignedDeclaration);
		
		docTypeSignature=DBO_SignCrop_cofigParamMap.get("DocTypeInProcessSignature");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("docTypeSignature: " + docTypeSignature);
				
		SignedDeclarationDocs_Input=DBO_SignCrop_cofigParamMap.get("SignedDeclarationDocs_Input");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SignedDeclarationDocs_Input: " + SignedDeclarationDocs_Input);
		
		SegregatedDocument=DBO_SignCrop_cofigParamMap.get("SegregatedDocument");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SegregatedDocument: " + SegregatedDocument);
		
		SignedDeclarationDocs_Error=DBO_SignCrop_cofigParamMap.get("SignedDeclarationDocs_Error");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SignedDeclarationDocs_Error: " + SignedDeclarationDocs_Error);
		
		SignedDeclarationDocs_temp=DBO_SignCrop_cofigParamMap.get("Temp");
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SignedDeclarationDocs_temp: " + SignedDeclarationDocs_temp);
		
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
		else
		{
			while (true) 
			{
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Session ID found: " + sessionId);
				DBO_SignCrop_Logs.setLogger();
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("AWB_Gen_file...123.");
				try 
				{
					DocMoveToSegregatedLocation();
					processCases();
					System.out.println("No More cases to Process, Sleeping...!");
					Thread.sleep(sleepIntervalInMin*60*1000);
				} 
				catch (IOException | InterruptedException e) 
				{
					e.printStackTrace();
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception in DocMoveToSegregatedLocation "+e.getMessage());
					e.printStackTrace();
					System.out.println("Exception in DocMoveToSegregatedLocation."+e.toString());
				}
				System.out.println("No More workitems to Process, Sleeping!");
				
			}
		}
		
	}
    private void DocMoveToSegregatedLocation() throws IOException
	{
		String errorPath = "";
		String tempPath="";
		try
		{
			
			String DeclarationRequired ="0";
			String SignedDeclFormRecvdStatus ="0";
			String SignCropAttachStatus ="0";
			now = new Date();
			Format formatter = new SimpleDateFormat("dd-MMM-yy");
			DATE = formatter.format(now);
			
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("DATE "+DATE);
			
			File folder = new File(SignedDeclarationDocs_Input);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("folder "+folder);
			File filesinFolder[] = folder.listFiles();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("files "+filesinFolder);
	
			if(filesinFolder.length == 0)
			{
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("No Files in the Signed Document folder from Control-M to move.");
			}
			else
			{
				for(int i=filesinFolder.length-1;i>=0;i--)
				{
					String file = filesinFolder[i].getName();
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file Name: "+filesinFolder[i].getName());
					String sourceFilePath = "";
					tempPath=SignedDeclarationDocs_temp+File.separator+DATE;
					sourceFilePath = SignedDeclarationDocs_Input + File.separator + file;
					errorPath = SignedDeclarationDocs_Error +File.separator+DATE;
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file TempsourcePath: "+tempPath);
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("file TempfailPath: "+errorPath);
					
					String newNameWithExt=Move(tempPath, sourceFilePath, "", false);
					if(!("".equalsIgnoreCase(newNameWithExt) || newNameWithExt == null ))
					{
						tempPath=tempPath+File.separator+newNameWithExt;
						hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); 
						String QR_Result= "";  
						String extension = file.substring(file.lastIndexOf(".")+1);
						if("pdf".equalsIgnoreCase(extension))
						{
							QR_Result=readQRcodePdf(tempPath);
						}
						else if("tif".equalsIgnoreCase(extension))
						{
							QR_Result=readtiff(tempPath,hintMap);
						}
						else
						{
							QR_Result=readQRcode(tempPath, "UTF-8", hintMap);
							
						}
						
						if(!"Error".equalsIgnoreCase(QR_Result))
						{
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("QR_Result: "+QR_Result);
							String[] QR_CodeData = QR_Result.split("~");
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("QR_CodeData: "+QR_CodeData.toString());
							if(QR_CodeData.length>1)
							{
								processInstanceID=QR_CodeData[0].trim();
								AWBNo=QR_CodeData[1].trim();
								RID=QR_CodeData[2].trim();
								CustomerName=QR_CodeData[3].trim();
								String finalPathofSignedDeclaration=SegregatedDocument+File.separator+processInstanceID+File.separator+AWBNo;
								String nameSignedDeclaration="";
								String nameSignature="";
								String parentFolderIndex = getFolderIndex(processInstanceID);
								if(CustomerName != null && !"".equalsIgnoreCase(CustomerName))
								{
									nameSignedDeclaration=RID+"_"+CustomerName.replace(" ", "")+"_SignedDeclarartion."+extension;
									nameSignature=RID+"_"+CustomerName.replace(" ", "")+"_Signature";
									if(CopyFile(tempPath,finalPathofSignedDeclaration+File.separator+nameSignedDeclaration))
									{
										String attachStatus=attachDocument(tempPath,nameSignedDeclaration,processInstanceID,parentFolderIndex,docTypeSignedDeclaration);
										String colName="SignedDeclFormRecvdStatus";
										String where = "RelatedPartyID='"+RID+"' and WINAME='"+processInstanceID+"'";
										String values="";
										String updateTabelStatus="";
										if("S".equalsIgnoreCase(attachStatus))
										{
											values="'R'";
											updateTabelStatus=updateTableData("USR_0_DBO_RelatedPartyGrid",colName,values,where);
										}
										else
										{
											QRFileReadFinalStatus="Error";
										}
										if("Success".equalsIgnoreCase(updateTabelStatus))
										{
											if("jpg".equalsIgnoreCase(extension))
											{
												tempSignaturePath=cropedSignatureJPG(tempPath);
											}
											File cropedSign = new File (tempSignaturePath!=null?tempSignaturePath:"");
											//String AutoCropStatus=AutoCrop(tempPath,tempPath,processInstanceID,file);
											if(cropedSign.length()!=0)
											{
												String newFileName=Move(finalPathofSignedDeclaration,tempSignaturePath,nameSignature,true);
												
												finalPathofSignedDeclaration+=File.separator+newFileName;
												attachStatus=attachDocument(finalPathofSignedDeclaration,newFileName,processInstanceID,parentFolderIndex,docTypeSignature);
												if("S".equalsIgnoreCase(attachStatus) && !"".equalsIgnoreCase(newFileName))
												{
													colName="SignCropAttachStatus";
													values="'D'";
													updateTabelStatus=updateTableData("USR_0_DBO_RelatedPartyGrid",colName,values,where);
												}
												else
												{
													colName="SignCropAttachStatus";
													values="'E'";
													updateTableData("USR_0_DBO_RelatedPartyGrid",colName,values,where);
													QRFileReadFinalStatus="Error";
												}
												
											}
											else
											{
												colName="SignCropAttachStatus";
												values="'E'";
												updateTableData("USR_0_DBO_RelatedPartyGrid",colName,values,where);
												QRFileReadFinalStatus="Error";
											}
										}
									}
									else
									{
										QRFileReadFinalStatus="Error";
									}
								}
								else
									QRFileReadFinalStatus="Error";
							}
							else
								QRFileReadFinalStatus="Error";
							
						}
						else
						{
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Error in QR Code Read: ");
							QRFileReadFinalStatus="Error";
							
						}
						if("Error".equalsIgnoreCase(QRFileReadFinalStatus))
							Move(errorPath, tempPath, "", false);
					}
					else
					{
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Error in moving to temp location: ");
					}
					
					//QR_Result= QRCodePOC.readtiff(TempsourcePath, hintMap); // read TIF
					//QR_Result= QRCodePOC.readQRcode(TempsourcePath, "UTF-8", hintMap); // Read from JPG
					//QR_Result= QRCodePOC.readQRcodePdf(TempsourcePath); // Read from PDF
					
					
					
				}
			}
		}
		catch (Exception e) {
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception in DocMoveToSegregatedLocation "+e.getMessage());
			e.printStackTrace();
			System.out.println("Exception in DocMoveToSegregatedLocation.");
			Move(errorPath, tempPath, "", false);
			
		}
	}
	// strInputFileName -  Pass the path of the file to be picked after movement of SignedDoc to SegregatedFolder.
	// strInputFileName -  Pass the path where the auto crop file will be saved i.e. Segregated folder->WIName->AWB->SignatureFile.
	public static String readQRcode(String path, String charset, Map map) throws FileNotFoundException, IOException, NotFoundException, ChecksumException, FormatException  
	{  
		String locationofFile=path.substring(0,path.lastIndexOf(File.separator));
		String FileName=path.substring(path.lastIndexOf(File.separator)+1);
		String cropedQR =locationofFile+File.separator+"CropedQR_"+FileName;
		BufferedImage image = ImageIO.read(new FileInputStream(path));
		BufferedImage cropedImage = image.getSubimage(700, 0, 1200, 400);//300, 0, 1000, 500
		File outputfile = new File(cropedQR);
		ImageIO.write(cropedImage, "jpg", outputfile);
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(cropedImage)));  
		QRCodeReader reader = new QRCodeReader();
		Result rslt = reader.decode(binaryBitmap); //new MultiFormatReader().decode(binaryBitmap);  
		return rslt.getText();  
	}
	public static String cropedSignatureJPG(String path) throws FileNotFoundException, IOException, NotFoundException, ChecksumException, FormatException  
	{  
		String locationofFile=path.substring(0,path.lastIndexOf(File.separator));
		String FileName=path.substring(path.lastIndexOf(File.separator)+1);
		String cropedSign =locationofFile+File.separator+"CropedSignature_"+FileName;
		BufferedImage image = ImageIO.read(new FileInputStream(path));
		BufferedImage cropedImage = image.getSubimage(640, 2970, 1250, 370);//x,y,w,h//600, 2750, 1200, 500
		File outputfile = new File(cropedSign);
		ImageIO.write(cropedImage, "jpg", outputfile);
		return cropedSign;  
	}
	public static String readtiff(String tiffpath,Map hintMap) 
			throws IOException, ChecksumException, NotFoundException, FormatException
	{
		try
		{
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
		    Result result= null;;
		    result = reader.decode(bitmap);
		    System.out.println("Barcode text is " + result.getText());
		    if (result.getText() != null){
				System.out.println("The data in the qr cod is  :: "+ result.getText());
				return result.getText();
		    }
		    //  byte[] b = result.getRawBytes();
		    //  System.out.println(ByteHelper.convertUnsignedBytesToHexString(result.getText().getBytes("UTF8")));
		    //System.out.println(ByteHelper.convertUnsignedBytesToHexString(b));
		    } 
		catch (NotFoundException e)
		{  
		  System.out.println("NotFoundException"+e.toString());
		} 
		catch (ChecksumException e)
		{ 
		  System.out.println("ChecksumException"+e.toString()); 
		} 
		catch (FormatException e) 
		{
		  System.out.println("FormatException"+e.toString());
		}
		return "Error";
		    
	}
public static String readQRcodePdf(String pdffilepath) throws IOException
{
		java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
		try{
			
			File pdfFile = new File(pdffilepath);
			System.out.println("inside readQRcodePdf");
			FileInputStream input = new FileInputStream(pdfFile);
			PDDocument testDoc = PDDocument.load(input);
			List<PDPage> pages = testDoc.getDocumentCatalog().getAllPages();
			// for(int i=0; i<pages.size(); i++) {//for loop starts if multiple
			// pages
			PDPage page = (PDPage) pages.get(0);
			System.out.println("inside for get all pages");
			BufferedImage image = page.convertToImage();
			System.out.println("after convert to image");

			BufferedImage cropedImage = image.getSubimage(0, 0, 914, 400);
			System.out.println("after cropedImage");
			// using the cropedImage instead of image
			LuminanceSource source1 = new BufferedImageLuminanceSource(cropedImage);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source1));
			// barcode decoding
			QRCodeReader reader1 = new QRCodeReader();
			Result result1 = null;
			try {
				System.out.println("before decode ");
				result1 = reader1.decode(bitmap);
				System.out.println("after decode ");
			} catch (ReaderException e){
				System.out.println("reader error to decode");
			}
			if (result1.getText() != null) {
				System.out.println("The data in the qr cod is  :: "+ result1.getText());
				return result1.getText();
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
		return "Error";
	}
	
	public static void readpdfscanned(String pdffilepath,Map hintMap) throws IOException, NotFoundException, ChecksumException, FormatException{
		java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
		Result result=null;
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
			 System.out.println("inside try");
			qrCodeResult = new com.google.zxing.qrcode.QRCodeReader().decode(binaryBitmap,hintMap);
			System.out.println("data is: "+qrCodeResult.getText()); 
			//imgfile.delete();
	}
	
	private  void processCases()
	{
		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(DBO_SignCrop_Logs.DBO_SignCropLogger, false);

			if (sessionId == null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				DBO_SignCrop_Logs.DBO_SignCropLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Fetching all Workitems for Signatur Crop Queue ");
			System.out.println("Fetching all Workitems on queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,jtsIP,jtsPort,1);

			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Number of workitems retrieved on DBO_Sys_Signature_Crop : "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DBO_Sys_Signature_Crop  : "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ActivityName: "+ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ProcessDefId: "+ProcessDefId);
					//Complete wi if files corresponding to all deliverable generated
					DoneWI(processInstanceID,ActivityID,ActivityType,entryDateTime,WorkItemID);
					
				}
			}
		}
		catch(Exception e)
		{
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Inside process cases exception--"+e.getMessage()));
		}
	}
	private String DoneWI(String Wi_name,String ActivityID,String ActivityType,String entryDateTime,String WorkItemID)
	{
		String status="";
		String decisionValue="";
		String completeWIFlag="";
		try
		{
			String query = "select Count(*) as PendingDeclarationAndCropSign from USR_0_DBO_RelatedPartyGrid with (nolock) "
					+ "where winame='"+Wi_name+"' and DeclarationRequired='Y' and (SignCropAttachStatus != 'D' or SignCropAttachStatus is null)";
			
			String InputXML=CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionId);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Query to get count of pending rows: "+InputXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(InputXML,jtsIP,jtsPort,1);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug(" Output to validate pending file data "+ extTabDataOPXML);
			
				
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			String mainCode = xmlParserData.getValueOf("MainCode");
			
			if("0".equalsIgnoreCase(mainCode))
			{
				String recRetrived = xmlParserData.getValueOf("TotalRetrieved");
				int noOfRecords = (!"".equalsIgnoreCase(recRetrived) && recRetrived != null)?Integer.parseInt(recRetrived):0;
				if(noOfRecords>0)
				{
					String DeclarationSignPendingCount = xmlParserData.getValueOf("PendingDeclarationAndCropSign");
					if("0".equalsIgnoreCase(DeclarationSignPendingCount.trim()))
					{
						decisionValue="Success";
						String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, Wi_name,WorkItemID);
						String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,jtsIP,jtsPort,1);
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

						XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
						String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
						if ("0".equals(getWorkItemMainCode.trim()))
						{
							
							String attributesTag="<Decision>"+decisionValue+"</Decision>";
							completeWIFlag="D";
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
							//Move Workitem to next Workstep 
							String completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId, Wi_name, WorkItemID,ActivityID,ActivityType, attributesTag,completeWIFlag);
							//completeWorkItemInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, Wi_name, WorkItemID, attributesTag,completeWIFlag);
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

							String completeWorkItemOutputXML = WFNGExecute(completeWorkItemInputXML,jtsIP,jtsPort,1);
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);

							XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
							String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
							DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);

							if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) 
							{
								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("assignWorkitemAttributeInput successful: "+completeWorkitemMaincode);

								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								//SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime=inputDateformat.format(entryDatetimeFormat);
								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

								Date actionDateTime= new Date();
								String formattedActionDateTime=inputDateformat.format(actionDateTime);
								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("FormattedActionDateTime: "+formattedActionDateTime);

								//Insert in WIHistory Table.
								String columnNames="WINAME,ACTION_DATE_TIME,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
								String columnValues="'"+Wi_name+"','"+formattedActionDateTime+"','"+activityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','Successfully attached Declaration and Signature'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"USR_0_DBO_WIHISTORY"); // toDo
								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,jtsIP,jtsPort,1);
								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("APInsertOutputXML: "+ apInsertOutputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Completed On "+ activityName);

								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ApInsert successful: "+apInsertMaincode);
									DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							
							}
							else 
							{
								completeWorkitemMaincode="";
								DBO_SignCrop_Logs.DBO_SignCropLogger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception in  DoneWI:- "+ e.toString());
		}
		return status;
	}
	private static String updateTableData(String tablename, String columnname,String values, String sWhere)
	{
		
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		String status="";
		
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,values,sWhere,cabinetName,sessionId);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!"0".equalsIgnoreCase(mainCodeforCheckUpdate)){
					
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Error in executing update on "+tablename+" :maincode"+mainCodeforCheckUpdate));
					status = "Error";
				}
				else
				{
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Succesfully updated "+tablename+" table"));
					return "Success";
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionId  = CommonConnection.getSessionID(DBO_SignCrop_Logs.DBO_SignCropLogger, false);
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
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
				status = "Error";
			}
		}
		return status;
	}
	public String AutoCrop(String strInputFileName,String strOutputFileName,String processInstanceID,String file){
		try {
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("processInstanceID AutoCrop "+processInstanceID);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("strInputFileName AutoCrop "+strInputFileName);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("strOutputFileName AutoCrop "+strOutputFileName);
			String strOutputFileNameGIF = strOutputFileName;
			strInputFileName = strInputFileName+"\\"+file;
			double X1 = 1.95;    // left
			double Y1 = 9.5;     // height
			double X2 = 6.44;    // breadth
			double Y2 = 10.62;   // height from below
			//convert(strInputFileName, strInputFileNameConverted, 150);
			String ext = "jpg";
			int iPageNo = 1;
			int ImageData[] = null;
			boolean bJPEG = false;
			RandomInputStreamSource riss = new RandomInputStreamSource(strInputFileName);
			File output_temp = new File(strOutputFileName);
			byte[] bytes = Tif6.isTifJpegCompressed(riss.getStream(), iPageNo);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("after bytes" + bytes);
			if (bytes != null){
				riss = new RandomInputStreamSource(bytes);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Changing output stream for JPEG Handling");
				bJPEG = true;
			}
			File f = null;
			if (bJPEG == true) {
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("after inside bjpeg");
				f = File.createTempFile("Signature", ".tif", output_temp);
			} else {
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("inside tif making");
				f = File.createTempFile("Signature", ".tif", output_temp);
			}
			String strOutputFile = f.getAbsolutePath();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Output File: "+ strOutputFile);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("strInputFileName --"+ strInputFileName);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("iPageNo --" + iPageNo);
			ImageData = GetImageDPI(strInputFileName, iPageNo);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After getting image data" + ImageData);
			if (ImageData == null) {
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("image is null"+ ImageData);
				return "Error";
			}
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After getting image data that is not null");
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ImageData[0]"+ ImageData[0]);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ImageData[1]"+ ImageData[1]);
			int iX1 = (int) (X1 * ImageData[0]);
			int iY1 = (int) (Y1 * ImageData[1]);
			int iX2 = (int) (X2 * ImageData[0]);
			int iY2 = (int) (Y2 * ImageData[1]);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Dimensions: iX1 " + iX1+ "iY1 " + iY1 + "iX2 " + iX2 + "iY2 " + iY2);
			if (iX2 > ImageData[2]) {
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("error--1");
				return "Error";
			}
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After 1");
			if (iY2 > ImageData[3]) {
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("error--2");
				return "Error";
			}
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After 2");
			RandomOutputStreamSource rout = new RandomOutputStreamSource(strOutputFile);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After 3");
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug(riss + " Page no: "+ iPageNo + " Dimensions: " + iX1 + " " + iY1 + " " + iX2+ " " + iY2 + " rout: " + rout);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Latest strOutputFile:  " + strOutputFile+ " strInputFileName: " + strInputFileName);
			riss.getStream().setCurrentPosition(0);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("sanal1:");

			NIPLJ.getRegion(riss, iPageNo, iX1, iY1, iX2, iY2, rout);

			File justTotest = new File(strOutputFile);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Size of temp file"+ justTotest.length());
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After 4");
			riss.getStream().close();
			rout.getStream().close();
/*			
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After 5");
			int iRetVal = 1;
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Before Concatenation");
			if (bJPEG == true) {
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Inside JPEG Code");
				File f1 = File.createTempFile("OSW", "." + ext + "");
				String strOutputFile1 = f1.getAbsolutePath();
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Path of Tif created from Jpeg: "+ strOutputFile1);
				NIPLJ.convertJPEGIntoTif6(strOutputFile, strOutputFile1);
				iRetVal = ConcatenateFiles(strOutputFile1, strInputFileName);
			} else {
				iRetVal = ConcatenateFiles(strOutputFile, strInputFileName);
			}
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("After Concatenation with retuen value: " + iRetVal);
			File ftodel = new File(strOutputFile);
			ConvertImageInDifferentType(strOutputFile, 1, 2,"SignatureCompress." + ext, processInstanceID, sessionId,strOutputFileNameGIF + "SignatureCompress", ext, jtsIP,jtsPort, cabinetName);
*/
		} catch (Exception e) {
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception in AutoCrop "+ e.getMessage());
			return "Error";
		}
		return "true";
	}
	
	public int ConcatenateFiles(String sSourceFile, String sDestFile) {
		try {
			com.newgen.niplj.fileformat.Tif6.appendTif6IntoTif6(sSourceFile, sDestFile);
			return 1;
		} catch (NGIMException e) {
			return e.getErrorCode();
		} catch (Exception ex) {
			return -1;
		}
	}
	
	public  int ConvertImageInDifferentType(String strInputFileName, int PageNo, int lConvertImageType, String CompressionFilename,String wi_name, String WD_UID,String OutputPathForGIF,String ext,String sJtsIp,String jtsPort,String sCabName) throws IOException {
		try {
			String sTempJPGFile = "";
			if (strInputFileName == null) {
				throw new NGIMException(-7055);
			}
			RandomInputStreamSource riss = null;
			riss = new RandomInputStreamSource(strInputFileName);
			RandomInputStream ris = riss.getStream();
			int m_FileType = NIPLJ.getFileFormat(riss);
			System.out.println("ConvertImageInDifferentType filetype "
					+ m_FileType);
			java.awt.Image m_image = null;
			int iNoOfPages = 0;
			com.newgen.niplj.codec.EncodeParam encdParam = null;
			JpegEncodeParam jpegEncParam = null;
			DecodeParam decode = null;
			DecodeParam decodetemp = null;
			java.awt.Image m_OutImage = null;
			String strTemp = System.getProperty("user.dir")+ System.getProperty("file.separator") + "crop"+ System.getProperty("file.separator") + wi_name
			+ System.getProperty("file.separator");
			File fTempFile1 = new File(strTemp + CompressionFilename);
			strTemp = fTempFile1.getAbsolutePath();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("strTemp---" + strTemp);
			NG_BufferedImageOperations m_ngBuffImage = null;
			switch (m_FileType) {
			case 1: 
				iNoOfPages = Tif6.getPageCount(ris);
				if (iNoOfPages <= 0 || iNoOfPages < PageNo) {
					throw new NGIMException(-7082);
				}
				if (iNoOfPages < 1)
					;
				int m_CompType = Tif6.getCompressionType(ris, PageNo);
				System.out.println("Compression type of ris in convertdifferentimage "+ m_CompType);
				if (m_CompType >= 1 && m_CompType <= 5 || m_CompType == 32773) {
					encdParam = new Tif6EncodeParam(m_CompType);
				} else {
					File fle = new File("~rotated.tif");
					fle.delete();
					byte jpeg[] = null;
					jpeg = Tif6.isTifJpegCompressed(ris, PageNo);
					FileOutputStream fos = new FileOutputStream(
							strInputFileName);
					if (jpeg == null) {
					} else {
						fos.write(jpeg);
					}
					fos.close();
					m_FileType = 0;
					m_image = Toolkit.getDefaultToolkit().getImage("~orig.tif");
					switch (lConvertImageType) {
					case 1: 
						m_OutImage = NIPLJ.convertAnyToBW(m_image);
						break;
					case 2: 
						m_OutImage = NIPLJ.convertTo4bitGray(m_image);
						break;
					case 4: 
						m_OutImage = NIPLJ.convertTo8bitGray(m_image);
						break;
					case 5: 
						m_OutImage = NIPLJ.reducePixelDepth(m_image, 8);
						break;
					case 3: 
					default:
						System.out
								.println("Incorrect value for Conversion Type exists. No Conversion would take place.");
						m_OutImage = m_image;
						break;
					}
					jpegEncParam = new JpegEncodeParam();
					NIPLJ.encodeFromImage(m_OutImage, sTempJPGFile,
							jpegEncParam);
					NIPLJ.convertJPEGIntoTif6(sTempJPGFile, strTemp);
					ris.close();
					File f1 = new File("~orig.tif");
					f1.delete();
				}
				break;

			case 2:
				encdParam = new BmpEncodeParam();
				break;
			case 3:
				encdParam = new JpegEncodeParam();
				break;
			case 5: 
				encdParam = new GifEncodeParam();
				break;
			case 4:
			default:
				throw new NGIMException(-7070);
			}
			if (m_FileType != 0) {
				decode = new DecodeParam(PageNo, 0);
				m_image = NIPLJ.decodeToImage(strInputFileName, decode);
				switch (lConvertImageType) {
				case 1:
					m_OutImage = NIPLJ.convertAnyToBW(m_image);
					break;
				case 2: 
					m_OutImage = NIPLJ.convertTo4bitGray(m_image);
					break;
				case 4: 
					m_OutImage = NIPLJ.convertTo8bitGray(m_image);
					break;
				case 5:
					m_OutImage = NIPLJ.reducePixelDepth(m_image, 8);
					break;
				case 3: 
				default:
					System.out.println("Incorrect value for Conversion Type exists. No Conversion would take place.");
					m_OutImage = m_image;
					break;
				}
			}
			switch (m_FileType) {
			case 1:
			case 2:
			case 3:
			case 5:
				NIPLJ.encodeFromImage(m_OutImage, strTemp, encdParam);
			case 4:
			default:
				NIPLJ.freeMemory();
				break;
			}
			riss.getStream().close();
			ris.close();
			ris = null;
			updateAUSSignatureBase64("gif", strTemp, OutputPathForGIF, wi_name,WD_UID, ext, sJtsIp, Integer.parseInt(jtsPort), sCabName);
		//	addDocumentToWorkitem(strTemp, wi_name, WD_UID, ext, sJtsIp,iJtsPort, sCabName);
			return 1;
		} catch (Exception e) {
			System.out.println("Exception");
			return 0;
		}
	}
	

	private String checkSize4Conversion(String sFilePath){
			String LOG_FILE_NAME1 = "Logs";
			String callingFile = "CreateLogs";
			File f = new File(sFilePath);
			if (!f.exists()){
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("File to be converted does not exist");
				return "Fail";
			} else {
				long sizeInBytes = f.length();
				float sizeInMB = sizeInBytes / 1024f / 1024f;
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Allowed size is 1 MB");
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Actual size of file: " + sizeInMB);
				int allowedSizeInMB = 1;
				long allowedSizeInBytes = allowedSizeInMB * 1024 * 1024;
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Compared values: Size of file- " + sizeInBytes+ ", Allowed size- " + allowedSizeInBytes);
				if (sizeInBytes > allowedSizeInBytes) {
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Allowed size limit for conversion is: "+ allowedSizeInMB + " MB and actual size of file is: "+ sizeInMB + " MB");
					return "Fail";
				}
			}
			return "success";
	    }
	
	public String updateAUSSignatureBase64(String sDocExt, String outPutPath, String sDestPath,String wi_name,String WD_UID,String ext,String sJtsIp,int iJtsPort,String sCabName){
		RandomInputStreamSource riss=null;
		try{
			NGEjbClient ejbOb = NGEjbClient.getSharedInstance();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Inside convertTifToGif_Binary"); 
			String res=checkSize4Conversion(outPutPath);
			if(!res.equalsIgnoreCase("success")){
				return "fail";
			}
			riss = new RandomInputStreamSource(outPutPath);
			RandomInputStream ris = riss.getStream();
			int pagecount = 0;
			pagecount = Tif6.getPageCount(ris);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Page Count: "+pagecount);
			if(pagecount>1){	
				return "fail";
			}
			java.awt.Image imageObj = null;
			byte[] bytes = Tif6.isTifJpegCompressed(ris, 1);
			if (bytes != null) {
				imageObj = Toolkit.getDefaultToolkit().createImage(bytes);
			}else{

				DecodeParam decode1 = new DecodeParam(1, DecodeParam.DECODEAS_DEFAULT);
				imageObj = NIPLJ.decodeToImage(riss, decode1);
			}
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("here"); 
			NG_BufferedImageOperations ngbuff = new NG_BufferedImageOperations();
			ngbuff.FillInImage(imageObj, true);
			java.awt.Image image=null;
			if(ngbuff.BitsPerPixel == 24){
				ngbuff = NIPLJ.reducePixelDepth(ngbuff, 8);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("exportInside 24 bit");
			}
			if(ngbuff.BitsPerPixel == 1){
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("exportInside one bit");
				image = NIPLJ.convertOneTo8bitGray(ngbuff);
				if ((ngbuff.m_ImageProperties.iCompression != 3 && ngbuff.m_ImageProperties.iCompression != 4) && (ngbuff.m_ImageProperties.PhotometricInterpretation == 0))
					ngbuff.negateImageBuffer();
			}
			else{
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("exportInside 8 bit");
				NG_SimpleImageProducer ngsim = new NG_SimpleImageProducer(ngbuff);
				image = Toolkit.getDefaultToolkit().createImage(ngsim);
			}			
			GifEncodeParam enc = new GifEncodeParam();
			String outFolder="";
			outFolder= sDestPath+"."+sDocExt;
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Output Image Path: "+outFolder);
			NIPLJ.encodeFromImage(image,outFolder, enc);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("convertTifToGif Done");
			
			FileInputStream fis = new FileInputStream(outFolder);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			long lSize=0;
			try {
				for (int readNum; (readNum = fis.read(buf)) != -1;){
					bos.write(buf, 0, readNum);
					lSize=lSize+readNum;
				}
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Total size in export modified: "+lSize);
				byte[] encodedBytes = Base64.encodeBase64(bos.toByteArray()); 
				String sEncodedBytes = new String(encodedBytes);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Binary Conversion done : "+sEncodedBytes);
				double signatureSize = sEncodedBytes.length();
					signatureSize = 3*Math.ceil(signatureSize/4) - ((sEncodedBytes.indexOf("==")) != -1 ? 2 : 1);
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SignatureUpload signatureSize: "+signatureSize);
					if(signatureSize > 16281){
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("greater than 16 kb ");
					} else {
						DBO_SignCrop_Logs.DBO_SignCropLogger.debug("less than 16 kb");
					} 
			} catch (IOException ex) { 
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("IOException in convertTifToGif_Binary: "+ex);
				return "fail";
			}
		}
		catch(Exception e){
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception in convertTifToGif_Binary: "+e);	
			return "fail";
		}
		finally{
			try {
				if(riss != null && riss.getStream() != null)
				riss.getStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "success";
	}
	
	public int[] GetImageDPI(String filename, int pageNo) {
		int ImageData1[] = new int[4];
		DecodeParam decode = new DecodeParam(pageNo,
				DecodeParam.DECODEAS_DEFAULT);
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug(" decode created");
		java.awt.Image m_img = null;
		NG_BufferedImageOperations obj = new NG_BufferedImageOperations();
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("buffres object decode created");
		boolean isBlank = false;
		try {
			RandomInputStreamSource riss2 = new RandomInputStreamSource(
					filename);
			RandomInputStream ris = riss2.getStream();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("get stream done decode created");

			byte[] jpeg = null;
			jpeg = Tif6.isTifJpegCompressed(ris, pageNo);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("got jpeg ");

			if (jpeg == null) {
				try {
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("jpeg not null");

					m_img = NIPLJ.decodeToImage(filename, decode);
				} catch (Exception e) {
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("exception while reading jpeg" + e);

				}

			} else {
				RandomInputStreamSource riss1 = new RandomInputStreamSource(
						jpeg);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("riss1 created");

				m_img = NIPLJ.decodeToImage(riss1, decode);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("m_img created");

				// m_img=Toolkit.getDefaultToolkit().createImage(jpeg);
			}
			obj.FillInImage(m_img, true);
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("after FillInImage");

			while (obj.NoOfRowsSet != obj.ImageDimension.height) {
				try{
					Thread mainThread = Thread.currentThread();
					mainThread.sleep(3000);
				} catch (Exception ex) {}
			}

			NIPLJ.freeMemory();
			int x = obj.XDPI;
			int y = obj.YDPI;
			int img_height = obj.ImageDimension.height;
			int img_width = obj.ImageDimension.width;
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("dimension defined");

			ImageData1[0] = x;
			ImageData1[1] = y;
			ImageData1[2] = img_width;
			ImageData1[3] = img_height;
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("dimension set");

			obj = null;
			m_img = null;
			ris.close();
			return ImageData1;
		} catch (FileNotFoundException err) {
			err.printStackTrace();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("exception first" + err);
		} catch (Exception ex) {
			ex.printStackTrace();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("exception second" + ex);
		}
		return null;
	 }
	
	  public static void convert(String pdfInputPath, String tiffOutputPath,int default_dpi) {
			try{
				PDDocument pdf = PDDocument.load(new File(pdfInputPath));
				PDFRenderer ren = new PDFRenderer(pdf);
				ImageWriter writer = ImageIO.getImageWritersBySuffix("jpeg").next();
				writer.setOutput(ImageIO.createImageOutputStream(new File(tiffOutputPath)));
				ImageWriteParam params = writer.getDefaultWriteParam();
				params.setCompressionMode(ImageWriteParam.MODE_DEFAULT);	
				writer.prepareWriteSequence(null);
				/*for(int page=0; page<pdf.getNumberOfPages(); page++){
					writer.writeToSequence(new IIOImage(ren.renderImageWithDPI(page, default_dpi), null, null), params);
				}*/
				writer.endWriteSequence();
			}
			catch (Exception e){
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception in conversion of pdf--"+e.toString());
			}
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
					//newFilename=orgFileName.substring(0,orgFileName.indexOf("."))+"_"+append+orgFileName.substring(orgFileName.lastIndexOf("."));
					newFilename=append+orgFileName.substring(orgFileName.lastIndexOf("."));
					lobjFileTemp = new File(pstrDestFolderPath + File.separator + newFilename);
				}
				else
				{
					// if file is not to move then no change in file name 
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug.info("orgFileName::"+orgFileName);
					newFilename=orgFileName;
					lobjFileTemp = new File(pstrDestFolderPath+ File.separator + newFilename );
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug.info("lobjFileTemp::"+lobjFileTemp);
				}
				if (lobjFileTemp.exists()) 
				{
					// to ask  om bhai about it
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug.info("lobjFileTemp exists");
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
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug.info("lobjFileTemp dont exists");
					// lobjFileTemp = null;
				}
				
				// make a file in destination folder
				File lobjNewFolder = new File(lobjDestFolder, newFilename);
				
				boolean lbSTPuccess = false;
				try 
				{
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug.info("lobjFileToMove::"+lobjFileToMove);
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug.info("lobjNewFolder::"+lobjNewFolder);
					// To confirm as this line is to move a file
					lbSTPuccess = lobjFileToMove.renameTo(lobjNewFolder);
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug.info("lbSTPuccess::"+lbSTPuccess);
				} 
				catch (SecurityException lobjExp) 
				{

					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SecurityException " + lobjExp.toString());
				} 
				catch (NullPointerException lobjNPExp) 
				{

					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("NullPointerException " + lobjNPExp.toString());
				} 
				catch (Exception lobjExp) 
				{

					DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception " + lobjExp.toString());
				}
				if (!lbSTPuccess) 
				{
					// File was not successfully moved


					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Failure while moving " + lobjFileToMove.getAbsolutePath() + "===" +
					//	lobjFileToMove.canWrite());
				} 
				else 
				{

					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Success while moving " + lobjFileToMove.getName() + "to" + pstrDestFolderPath);
					//DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Success while moving " + lobjFileToMove.getName() + "to" + lobjNewFolder);
				}
				lobjDestFolder = null;
				lobjFileToMove = null;
				lobjNewFolder = null;
			} 
			catch (Exception lobjExp) 
			{
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(lstrExceptionId + " : " + "Exception occurred while moving " + pstrFilePathToMove + " to " +
						":" + lobjExp.toString());
				newFilename="";
			}

			return newFilename;
		}
	  private static boolean CopyFile(String sourceFile,String destFolder)
		{
			File FileToBecopied = new File(sourceFile);
			File destinationFolder = new File(destFolder);
			/*if (!destinationFolder.exists())
			{
				destinationFolder.mkdirs();
			}*/
			//File rename = new File(destFolder+File.separator+nameOfFile);
			try 
			{
				 FileUtils.copyFile(FileToBecopied,destinationFolder);
				 return true;
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Exception in CopyFile:- "+e.getMessage()));
				return false;
			}
		}
	  
	  private static String attachDocument(String filetobeaddedpath,String fileName,String workItemName,String parentFolderIndex,String DocTypeAsProcess)
		 {
		        try 
		        {
		            DBO_SignCrop_Logs.DBO_SignCropLogger.info("attachDocument volumeID : " + volumeID + " filetobeaddedpath :" + filetobeaddedpath + " DocName :" + fileName);
		            if (filetobeaddedpath.equalsIgnoreCase("") || parentFolderIndex.equalsIgnoreCase("")) {
		                return "N";
		            }
		           // String parentFolderIndex= RelatedPartyData.get("PARENTFOLDERINDEX");
		            String sDocsize = "";
		            JPISIsIndex IsIndex = new JPISIsIndex();
		            JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
		            JPISDEC.m_cDocumentType = 'N';
		            JPISDEC.m_sVolumeId = Short.parseShort(volumeID);
		            File fppp = new File(filetobeaddedpath);
		            long lgvDocSize;
		            File obvFile = fppp;
		            lgvDocSize = obvFile.length();
		            sDocsize = Long.toString(lgvDocSize);
		            String DocAttach = "Y";
		            if (fppp.exists()) {
		                DBO_SignCrop_Logs.DBO_SignCropLogger.info("fpp exists : " + fppp.getPath());
		            } else {
		                DBO_SignCrop_Logs.DBO_SignCropLogger.info("fpp does not exists");
		                DocAttach = "N";
		            }
		            if (!DocAttach.equalsIgnoreCase("N")) {
		                if (fppp.isFile()) {
		                    DBO_SignCrop_Logs.DBO_SignCropLogger.info("fpp is file");
		                } else {
		                    DBO_SignCrop_Logs.DBO_SignCropLogger.info("fpp is not file");
		                    DocAttach = "N";
		                }
		            }
		            if (!DocAttach.equalsIgnoreCase("N")) 
		            {
		                DBO_SignCrop_Logs.DBO_SignCropLogger.info("Before AddDocument_MT Completion");
		                try 
		                {
		                    if(smsPort.startsWith("33"))
							{
								CPISDocumentTxn.AddDocument_MT(null, jtsIP , Short.parseShort(smsPort), cabinetName, Short.parseShort(volumeID), filetobeaddedpath, JPISDEC, "",IsIndex);
							}
							else
							{
								CPISDocumentTxn.AddDocument_MT(null, jtsIP , Short.parseShort(smsPort), cabinetName, Short.parseShort(volumeID), filetobeaddedpath, JPISDEC, null,"JNDI", IsIndex);
							}
		                    DBO_SignCrop_Logs.DBO_SignCropLogger.info("AddDocument_MT Completed successfully");
		                    //fppp.delete();
		                    DBO_SignCrop_Logs.DBO_SignCropLogger.info("Generated File deleted successfully");
		                } 
		                catch (Exception e) 
		                {
		                    DBO_SignCrop_Logs.DBO_SignCropLogger.info("Exception in CPISDocumentTxn");
		                    DocAttach = "N";
		                    //updateRecordData(ReferenceNumber, iD2, "E", "", RETRY, e.toString().replace(",", "").replace("'", "''"));
		                    return "N";
		                } 
		                catch (JPISException e)
		                {
		                    DBO_SignCrop_Logs.DBO_SignCropLogger.info("Exception in CPISDocumentTxn : " + e);
		                    StringWriter sw = new StringWriter();
		                    PrintWriter pw = new PrintWriter(sw);
		                    e.printStackTrace(pw);
		                    DBO_SignCrop_Logs.DBO_SignCropLogger.info("Exception in CPISDocumentTxn 2 : " + sw);
		                    DocAttach = "N";
		                    //updateRecordData(ReferenceNumber, iD2, "E", "", RETRY, e.toString().replace(",", "").replace("'", "''"));
		                    return "N";
		                }
		            }

		            if (!DocAttach.equalsIgnoreCase("N"))
		            {
						
						String sISIndex = IsIndex.m_nDocIndex + "#" + IsIndex.m_sVolumeId;
						DBO_SignCrop_Logs.DBO_SignCropLogger.info("workItemName: "+workItemName+" sISIndex: "+sISIndex);
						String strExtension = "pdf";
		                String DocumentType = "pdf";
		               
		                strExtension= fileName.substring(fileName.lastIndexOf(".")+1);
		                
		                if(strExtension.equalsIgnoreCase("JPG") || strExtension.equalsIgnoreCase("TIF") || strExtension.equalsIgnoreCase("JPEG") || strExtension.equalsIgnoreCase("TIFF"))
						{
							DocumentType = "I";
						}
						else
						{
							DocumentType = "N";
						}
		                
		                fileName = fileName.substring(0,fileName.lastIndexOf("."));
		               
		                String sMappedInputXml = CommonMethods.getNGOAddDocument(parentFolderIndex,DocTypeAsProcess,fileName,DocumentType,strExtension,sISIndex,sDocsize,volumeID,cabinetName,sessionId);
		                DBO_SignCrop_Logs.DBO_SignCropLogger.info("workItemName: " + workItemName + " sMappedInputXml " + sMappedInputXml);
		               
		                String sOutputXml =WFNGExecute(sMappedInputXml, jtsIP, jtsPort, 1);
		                sOutputXml = sOutputXml.replace("<Document>", "");
		                sOutputXml = sOutputXml.replace("</Document>", "");
		                DBO_SignCrop_Logs.DBO_SignCropLogger.info("workItemName: " + workItemName + " Output xml For NGOAddDocument Call: " + sOutputXml);
		                XMLParser objXMLParser = new XMLParser(sOutputXml);
						String Status = objXMLParser.getValueOf("Status");/*
						XMLParser xmlParserData1 = new XMLParser(extTabDataOPXML1);
						String mainCode = xmlParserData1.getValueOf("MainCode");*/
						DBO_SignCrop_Logs.DBO_SignCropLogger.info("Status of AddDocument_MT:" + Status);
		                if ("0".equalsIgnoreCase(Status))
	                    {
		                	DocAttach = "S";
	                    } 
	                    else
	                    {
	                    	DBO_SignCrop_Logs.DBO_SignCropLogger.info("Error of AddDocument_MT:" + objXMLParser.getValueOf(sOutputXml, "Error"));
	                        DocAttach = "N";
	                    }
		                				
		            }
		            return DocAttach;
		        } catch (Exception e)
		        {
		            return "N";
		        }
		    }
	  public static String getFolderIndex(String WINo)
		{
			String parentFolderIndex="";
			XMLParser objXMLParser = new XMLParser();
			String sInputXML="";
			String sOutputXML="";
			String RecordCount="";
			

			try
			{
				String query="SELECT ItemIndex FROM RB_DBO_EXTTABLE WITH(nolock) WHERE WINAME='"+WINo+"'";
				sInputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionId);
				DBO_SignCrop_Logs.DBO_SignCropLogger.info("Get FolderIndex InputXML = "+sInputXML);
				sOutputXML = WFNGExecute(sInputXML, jtsIP, jtsPort, 0 );
				DBO_SignCrop_Logs.DBO_SignCropLogger.info("Get FolderIndex OutputXML = "+sOutputXML);
				objXMLParser.setInputXML(sOutputXML);
				String MaincodeTemp=objXMLParser.getValueOf("MainCode");
				DBO_SignCrop_Logs.DBO_SignCropLogger.info("Get FolderIndex MainCode = "+MaincodeTemp);
				RecordCount=objXMLParser.getValueOf("TotalRetrieved");
				if("0".equalsIgnoreCase(MaincodeTemp) && Integer.parseInt(RecordCount)>0)
					parentFolderIndex=objXMLParser.getValueOf("ItemIndex");
			}
			catch(Exception e)
			{
				DBO_SignCrop_Logs.DBO_SignCropLogger.error("Exception in getFolderIndex...");
			}
			
		
			
			return parentFolderIndex;
		}
	/*public String Move(String pstrDestFolderPath, String pstrFilePathToMove,String append,boolean flag )
	{
		String newFilename="";
		String lstrExceptionId = "Text_Read.Move";
		try 
		{
			File lobjDestFolder = new File(pstrDestFolderPath);

			if (!lobjDestFolder.exists()) {
				lobjDestFolder.mkdirs();
			}
			File lobjFileTemp;
			File lobjFileToMove = new File(pstrFilePathToMove);
			String orgFileName=lobjFileToMove.getName();
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug("orgFileName "+orgFileName);
			if(flag){
				newFilename=orgFileName;
				lobjFileTemp = new File(pstrDestFolderPath + File.separator + newFilename);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("IF lobjFileTemp "+lobjFileTemp);
			}
			else{
				newFilename=orgFileName;
				lobjFileTemp = new File(pstrDestFolderPath+ File.separator + newFilename );
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("ELSE lobjFileTemp "+lobjFileTemp);
			}
			if (lobjFileTemp.exists()){
				if (!lobjFileTemp.isDirectory()){
					lobjFileTemp.delete();
				} 
				else{
					deleteDir(lobjFileTemp);
				}
			}
			else{}
			
			File lobjNewFolder = new File(lobjDestFolder, newFilename);
			boolean lbSTPuccess = false;
			try{
				lbSTPuccess = lobjFileToMove.renameTo(lobjNewFolder);
			} 
			catch (SecurityException lobjExp){
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("SecurityException " + lobjExp.toString());
				return "false";
			} 
			catch (NullPointerException lobjNPExp){
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("NullPointerException " + lobjNPExp.toString());
				return "false";
			} 
			catch (Exception lobjExp){
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Exception " + lobjExp.toString());
				return "false";
			}
			if (!lbSTPuccess) 
			{} 
			else 
			{}
			lobjDestFolder = null;
			lobjFileToMove = null;
			lobjNewFolder = null;
		} 
		catch (Exception lobjExp) 
		{
			DBO_SignCrop_Logs.DBO_SignCropLogger.debug(lstrExceptionId + " : " + "Exception occurred while moving " + pstrFilePathToMove + " to " +":" + lobjExp.toString());
			return "false";
		}
		return "true";
	}*/
	
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
				// DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Base64 string..:" +retValue);
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
	
	private void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere, String jtsIP, String jtsPort, String cabinetName){
		
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		
		DBO_SignCrop_Logs.DBO_SignCropLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount){
			
			try{

				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionId);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0")){
					
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Exception in ExecuteQuery_APUpdate updating "+tablename+" table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else
				{
					DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Succesfully updated "+tablename+" table"));
					System.out.println("Succesfully updated "+tablename+" table");
					//ThreadConnect.addToTextArea("Successfully updated transaction table");
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionId  = CommonConnection.getSessionID(DBO_SignCrop_Logs.DBO_SignCropLogger, false);
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
				DBO_SignCrop_Logs.DBO_SignCropLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
			}
		}
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