/*---------------------------------------------------------------------------------------------------------------------------
NEWGEN SOFTWARE TECHNOLOGIES LIMITED


Group                        : Application Project 2
Project/Product              : RAK
Application                  : iBPS
Module                       : FPU
File Name                    : PropertyFileLoaderUtil.java
Author                       : om.tiwari
Date (DD/MM/YYYY)            : 07/12/2021
Description                  : For loading FPU config propety file.

------------------------------------------------------------------------------------------------------
 CHANGE HISTORY
 Date       Bug ID / Ticket Number      Change by       Change Description
  
------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------*/
package com.newgen.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.newgen.iforms.user.DBO;

public class PropertyFileLoaderUtil {
	private static String filePath = System.getProperty("user.dir") + System.getProperty("file.separator")
			+ "CustomConfig" + System.getProperty("file.separator") + "DBO" + System.getProperty("file.separator");
	private static Properties prop;

	static {
		load();
	}
	private static void load() {
		prop = new Properties();
		InputStream input = null;
		try {
			String propertyFile = null;
			File folderDirectory = new File(filePath);
			File fileDirectory = null;
			DBO.mLogger.info("[Method - PropertyFileLoaderUtil ] file Folder Directory : " + folderDirectory);
			if (folderDirectory.exists()) {
				propertyFile = filePath + File.separatorChar + "Config" + ".properties";
				fileDirectory = new File(propertyFile);
				if (!fileDirectory.exists()) {
					DBO.mLogger.info("[Method - PropertyFileLoaderUtil ] folder not exist ");
				}
				if (fileDirectory.exists()) {
					input = new FileInputStream(fileDirectory);
					prop.load(input);
				}
			}
		} catch (IOException ex) {
			DBO.mLogger.error("[Method - PropertyFileLoaderUtil ] enter in catch block "+ex);
		}
	}

	public static String getProperty(String key) {
		return prop.getProperty(key);
	}

}
