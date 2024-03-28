package com.newgen.iforms.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.xmlapi.IFormXmlResponse;


public class iRBL_Click extends iRBL_Common {

	public String clickEvent(IFormReference iform, String controlName, String data) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException
	{
		iRBL.mLogger.debug("WINAME : "+getWorkitemName()+", WSNAME: "+getActivityName()+", controlName "+controlName+", data "+data);
		// TODO Auto-generated method stub
		if(controlName.equals("PDFGenerate"))
	        return new iRBL_GeneratePDF().onclickevent(iform, controlName, data);
		else if(controlName.equals("DecTechCall"))
	        return new iRBL_DecTechCall().onevent(iform, controlName, data);
		else if(controlName.equals("ViewAECBReport") || controlName.equals("btn_View_Signature"))
			return new iRBL_Integration().onclickevent(iform, controlName, data);		
	    else
	    	return "";	
	}
}
