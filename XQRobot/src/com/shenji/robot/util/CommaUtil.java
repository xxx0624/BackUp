package com.shenji.robot.util;


public class CommaUtil {
	public static String replaceChineseComma(String s){
		if(s != null){
			return s.replace("，", ",").replace(" ", "");
		}
		else{
			return s;
		}
	}
}
