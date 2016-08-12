package com.shenji.search.core.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;

import com.shenji.common.log.Log;

public class FileService {

	private static char md5Chars[] = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String read(String fileName, String encoding) {
		StringBuffer fileContent = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(fis, encoding);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				fileContent.append(line + "\n");
			}
			br.close();
			isr.close();
			fis.close();
			return fileContent.toString();
		} catch (Exception e) {
			Log.getLogger(FileService.class).error(e.getMessage(), e);
			return null;
		}
	}

	public static boolean write(String fileContent, String fileName,
			String encoding) throws Exception {
		try {
			File f = new File(fileName);
			if (f.exists())
				return false;
			FileOutputStream fos = new FileOutputStream(fileName);
			OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
			osw.write(fileContent);
			osw.flush();
			osw.close();
			fos.close();
			return true;
		} catch (Exception e) {
			throw e;
		}
	}

	public static String getStringMD5String(String str) {
		try {
			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(str.getBytes());
			return bufferToHex(messagedigest.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = md5Chars[(bt & 0xf0) >> 4];
		char c1 = md5Chars[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}
	
	public static boolean addFile(String text, String path, String fileName,
			String suffixes, String code) throws Exception {
		if (!FileService.write(text, path + File.separator + fileName
				+ suffixes, code))
			return false;
		else
			return true;

	}

	public static boolean deleteFile(String path, String fileName,
			String suffixes) {
		File file = new File(path + File.separator + fileName + suffixes);
		if (file.exists()) {
			file.delete();
			return true;
		}
		return false;
	}

	public static void deleteDir(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
				return;
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteDir(files[i]);
				}
				file.delete();
			}
		} else {
			System.out.println("文件不存在" + '\n');
		}
	}
}
