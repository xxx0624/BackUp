package com.shenji.robot.util;


public class CommaUtil {
	public static String replaceChineseComma(String s){
		return s.replace("，", ",").replace(" ", "");
	}
}
