package com.shenji.common.util;

import java.util.Iterator;
import java.util.List;

import com.shenji.robot.data.ResultShowBean;

public class XMLUtil {
	public static String generateXML(String userQuestion, ResultShowBean resultShowBean){
		String res = "";
		int index = 1;
		Iterator<String> qaIterator = resultShowBean.getResult().iterator();
		while(qaIterator.hasNext()){
			String qString = qaIterator.next();
			String aString = "";
			if(qaIterator.hasNext()){
				aString = qaIterator.next();
			}
			res += "<QA id=\""+String.valueOf(index)+"\"><q>"+qString+"</q><a>"+aString+"</a></QA>";
			index += 1;
		}
		res = "<userQuestion time=\""+GetTimeUtil.getYMDHMS()+"\">"
				+ "<content>"
				+userQuestion+"</content>"
				+ "<robotQA>"+res+"</robotQA>"
				+ "</userQuestion>";
		return res;
	}
}
