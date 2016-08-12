package com.shenji.common.util;

import java.security.MessageDigest;

import com.shenji.common.log.Log;

/** 
 * 工具类
 * MD5加密
 * @author zhq
 */
public abstract class MD5Util {
	/**
	 * MD5加密
	 * @param str 用于加密字符串
	 * @return 加密后的字符串
	 */
	public static String md5(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			Log.getLogger(MD5Util.class).error(e.getMessage(), e);
			return "";
		}
		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	// 测试主函数
	public static void main(String args[]) {
		String s = new String("idggnoix881");
		System.out.println("原始：" + s);
		System.out.println("MD5后：" + md5(s));

	}
}
