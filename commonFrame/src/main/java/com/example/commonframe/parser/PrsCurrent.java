package com.example.commonframe.parser;

import java.io.StringReader;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.example.commonframe.model.CurrentResult;
import com.example.commonframe.model.base.BaseResult;
import com.example.commonframe.util.Utils;

public class PrsCurrent extends BaseParser {

	@Override
	protected BaseResult parseXML(String content) {
		CurrentResult data = null;
		try {
			XmlPullParser parser = Xml.newPullParser();
			data = new CurrentResult();
			parser.setInput(new StringReader((String) content));
			int eventType = parser.getEventType();
			String nodeName = null;
			String nodeVal = null;
			boolean isResponse = false;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.TEXT:
					nodeVal = parser.getText();
					if (isResponse && !Utils.isEmpty(nodeName)
							&& !Utils.isEmpty(nodeVal)) {
						// success data
					}
					nodeName = null;
					nodeVal = null;
					break;
				case XmlPullParser.END_TAG:
					isResponse = false;
					nodeName = parser.getName();
					break;
				case XmlPullParser.START_TAG:
					isResponse = true;
					nodeName = parser.getName();
					if (nodeName == null)
						break;
					break;
				}
				eventType = parser.next();
			}

		} catch (Exception e) {
			return null;
		}
		return data;
	}

	@Override
	protected BaseResult parseJSON(String content) {
		CurrentResult data = null;
		try {
			JSONObject root = new JSONObject(content);
			data = new CurrentResult();
			root.getString("");
		} catch (Exception e) {
			return null;
		}
		return data;
	}

	@Override
	protected BaseResult parseSTRING(String content) {
		CurrentResult data = null;
		return data;
	}
}