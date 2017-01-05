package com.shenji.search.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.shenji.common.util.FileUtil;
import com.shenji.common.util.MD5Util;

public class NormalQAFileName {
	private static String getTAG(String htmlStr, String tag) {
		org.jsoup.nodes.Document document = null;
		document = Jsoup.parse(htmlStr);
		Elements meta = document.select(tag);
		// System.out.println(meta.text());
		return meta.text();
	}
	public static void main(String[] args) throws IOException{
		String filePath = "D:\\software\\tomcat\\apache-tomcat-7.0.63\\webapps\\XQDoc\\document\\faq";
		File file = new File(filePath);
		if(file.isDirectory()){
			File[] files = file.listFiles();
			int cnt = 0;
			for(File f: files){
				System.out.println("["+cnt+"]"+f.getName());
				cnt += 1;
				String fileContent = FileUtil.readFileContent(f.getAbsolutePath());
				String q = getTAG(fileContent, ".q");
				String a = getTAG(fileContent, ".a");
				String tag1 = getTAG(fileContent, ".tag1");
				String tag2 = getTAG(fileContent, ".tag2");
				String newFileName = MD5Util.md5(q + a) + ".html";
				//File newFile = new File(filePath + "\\" + newFileName + ".html");
				File newFile = new File(filePath + "\\" + newFileName);
				if(newFile.exists()){
					System.out.println("exist="+newFileName);
					continue;
				}
				FileUtil.copyFile(filePath, f.getName(), filePath, newFileName);
				if(f.exists()){
					//System.out.println(f.getAbsolutePath());
					if(f.delete()==false){
						System.out.println("fail");
					}
				}
			}
		}
	}
}
