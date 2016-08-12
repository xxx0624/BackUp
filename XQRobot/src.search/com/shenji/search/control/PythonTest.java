package com.shenji.search.control;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.bcel.generic.NEW;

public class PythonTest {
	public static void main(String[] args) {
		test2();
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String str2Hex(String str)
			throws UnsupportedEncodingException {
		String hexRaw = String.format("%x",
				new BigInteger(1, str.getBytes("UTF-8")));
		char[] hexRawArr = hexRaw.toCharArray();
		StringBuilder hexFmtStr = new StringBuilder();
		final String SEP = "\\x";
		for (int i = 0; i < hexRawArr.length && i + 1 < hexRawArr.length; i++) {
			hexFmtStr.append(SEP).append(hexRawArr[i]).append(hexRawArr[++i]);
		}
		return hexFmtStr.toString();
	}

	public static String hex2Str(String str)
			throws UnsupportedEncodingException {
		String strArr[] = str.split("\\\\"); // 分割拿到形如 xE9 的16进制数据
		byte[] byteArr = new byte[strArr.length - 1];
		for (int i = 1; i < strArr.length; i++) {
			Integer hexInt = Integer.decode("0" + strArr[i]);
			byteArr[i - 1] = hexInt.byteValue();
		}

		return new String(byteArr, "UTF-8");
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static void test2() {
		try {
			System.out.println("start");
			String question = "认证\t回执\t提示\t这个\t发行\t代理\t未\t初始化\t。\t怎么办\t？";
			String[] answer = {
					"只能\t去\t大厅\t认证",
					"打开\t报表\t后\t点\t带征\t按钮\t查看\t，\t或者\t按\tF12\"\t\"答\t：\t软件\t没有\t重装\t过\t的\t情况\t下\t，\t点\t视图\t-\t-\t已\t发件\t，\t按照\t发送\t的\t时间\t查看\t报表\t内容\t。",
					"重新\t从\t网站\t上面\t下载\t扫描仪\t驱动" };
			Process pr = Runtime.getRuntime().exec(
					"cmd /c python D:\\XQRobotLog\\PythonTest\\pythonTest.py "
							+ "\"" + "你是我的小苹果" + "\""
			/*
			 * + "\t" +"\""+ question +"\"" +"\t" + "\""+answer[0] +"\"" + "\t"
			 * +"\""+ answer[1] + "\"" +"\t" +"\""+ answer[2]+"\""
			 */
			);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
			pr.waitFor();
			System.out.println("end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void test1() {
		try {
			System.out.println("start");
			String question = "认证\t回执\t提示\t这个\t发行\t代理\t未\t初始化\t。\t怎么办\t？";
			String[] answer = {
					"只能\t去\t大厅\t认证",
					"打开\t报表\t后\t点\t【\t带征\t】\t按钮\t查看\t，\t或者\t按\tF12\"\t\"答\t：\t软件\t没有\t重装\t过\t的\t情况\t下\t，\t点\t【\t视图\t】\t-\t-\t【\t已\t发件\t】\t，\t按照\t发送\t的\t时间\t查看\t报表\t内容\t。",
					"重新\t从\t网站\t上面\t下载\t扫描仪\t驱动" };
			// Process pr =
			// Runtime.getRuntime().exec("cmd /c python D:\\TJ\\shenji_qa\\shenji_qa_predict.py "
			// + question + "\t" + answer[0] + "\t" + answer[1] + "\t" +
			// answer[2]);
			Process pr = Runtime
					.getRuntime()
					.exec("cmd /c python D:\\TJ\\shenji_qa\\shenji_qa_predict.py 认证\t回执\t提示\t这个\t发行\t代理\t未\t初始化\t。\t怎么办\t");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					pr.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
			pr.waitFor();
			System.out.println("end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
