/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: CMP Document
File Name				: WorkItem.java
Author 					: Sivakumar P
Date (DD/MM/YYYY)		: 26/07/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.iRBL.AttachDocument;

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
