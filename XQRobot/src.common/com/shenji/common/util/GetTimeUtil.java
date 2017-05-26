package com.shenji.common.util;

import java.util.Date;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

public class GetTimeUtil {
	private final static DateFormat format_ymd = new SimpleDateFormat("yyyyMMdd");
	private final static DateFormat format_ymdhms = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public static String getYMD(){
		return format_ymd.format(new Date());
	}
	
	public static String getYMDHMS(){
		return format_ymdhms.format(new Date());
	}
	
	public static void main(String[] args){
		System.out.println(getYMD());
		System.out.println(getYMDHMS());
	}
}
