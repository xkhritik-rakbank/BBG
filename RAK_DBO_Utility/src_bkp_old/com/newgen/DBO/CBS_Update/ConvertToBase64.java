/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP CBS
File Name				: ConvertToBase64.java
Author 					: Sajan Soda
Date (DD/MM/YYYY)		: 15/06/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DBO.CBS_Update;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

public class ConvertToBase64 {

	public static String convertToBase64(String filePath)
    {
		String retValue="";

		try
		{
			//System.out.println("inside base64 ");
			File file = new File(filePath);

			FileInputStream fis = new FileInputStream(file);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			long size=0;

			try
			{
				for (int readNum; (readNum = fis.read(buf)) != -1;)
				{
					bos.write(buf, 0, readNum);
					size=size+readNum;
				}
				byte[] encodedBytes = Base64.encodeBase64(bos.toByteArray());
				String sEncodedBytes=new String(encodedBytes);

				retValue=sEncodedBytes;
			}
			catch (IOException ex)
			{
			  //
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println("retValue base64: "+retValue);
		return retValue;
    }

}
