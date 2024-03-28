/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: DAC Document
File Name				: WorkItem.java
Author 					: Nikita Singhal
Date (DD/MM/YYYY)		: 15/06/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DBO.AttachDocument;

import java.util.HashMap;
import java.util.Map;

public class WorkItem
{
	public String processInstanceId;
	public String workItemId;

	public Map map = new HashMap();

	public String getAttribute(String attributeName)
	{
		return (String)map.get(attributeName);
	}
}
