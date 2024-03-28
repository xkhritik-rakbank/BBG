package com.newgen.iforms.user;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MapXML 
{
	public static String getTagValue(String xml,String tag) throws ParserConfigurationException, SAXException, IOException  
	{
		Document doc=getDocument(xml);
		NodeList nodeList = doc.getElementsByTagName(tag);
		
		int length = nodeList.getLength();
		
		if (length > 0) 
		{
			Node node =  nodeList.item(0);
//			System.out.println("Node : " + node);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				NodeList childNodes = node.getChildNodes();
				String value = "";
				int count = childNodes.getLength();
				for (int i = 0; i < count; i++) 
				{
					Node item = childNodes.item(i);
					if (item.getNodeType() == Node.TEXT_NODE) 
					{
						value += item.getNodeValue();
					}
				}
				return value;
			} 
			else if (node.getNodeType() == Node.TEXT_NODE) 
			{
				return node.getNodeValue();
			}
		}
		return "";
	}
	public static Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException  
	{
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		return doc;
		
		 
	}
	public static NodeList getNodeListFromDocument(Document doc,String identifier)
	{
		NodeList records = doc.getElementsByTagName(identifier);
		return records;
	}
	public static Map getKeyValueMapFromNode(Node record,String [] keys)
	{
		Map map = new HashMap();
		NodeList columnList = record.getChildNodes();
		int columnLength = columnList.getLength();
		//System.out.println("columnLength "+columnLength);
		for (int col = 0, i = 0; col  < columnLength; ++col) 
		{
			Node columnItem = columnList.item(col);
			if (columnItem.getNodeType() == Node.ELEMENT_NODE && columnItem.getNodeName().equalsIgnoreCase("td")) 
			{
				if( columnItem.getTextContent()==null)
					map.put(keys[i++].trim(),"");
				else if( columnItem.getTextContent().equalsIgnoreCase("null"))
					map.put(keys[i++].trim(),"");
				else
					map.put(keys[i++].trim(), columnItem.getTextContent());
			}
		}
		return map;
	}
	

	public static String getTagValueFromNode(Node record,String tagName)
	{
		String tagValue="";
		NodeList columnList = record.getChildNodes();
		int columnLength = columnList.getLength();
		//System.out.println("columnLength "+columnLength);
		for (int col = 0, i = 0; col  < columnLength; ++col) 
		{
			Node columnItem = columnList.item(col);
			if (columnItem.getNodeType() == Node.ELEMENT_NODE && columnItem.getNodeName().equalsIgnoreCase(tagName)) 
			{
				if( columnItem.getTextContent()==null)
					tagValue="";
				else if( columnItem.getTextContent().equalsIgnoreCase("null"))
					tagValue="";
				else
					tagValue=columnItem.getTextContent();
			}
		}
		return tagValue;
	}
}

