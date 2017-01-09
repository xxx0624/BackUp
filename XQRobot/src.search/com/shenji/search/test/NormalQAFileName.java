package com.shenji.search.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.shenji.common.util.FileUtil;
import com.shenji.common.util.MD5Util;

public class NormalQAFileName {
	private static String getTAG1(String htmlStr, String tag) {
		org.jsoup.nodes.Document document = null;
		document = Jsoup.parse(htmlStr);
		Elements meta = document.select(tag);
		// System.out.println(meta.text());
		return meta.text();
	}
	private static String getTAG2(String htmlStr, String tag) {
        org.jsoup.nodes.Document document = null;
        document = Jsoup.parse(htmlStr);
        Elements meta = document.getElementsByAttributeValue("name", tag);
        // System.out.println(meta.text());
        return meta.attr("content");
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
				f.delete();
				String q = getTAG1(fileContent, ".q");
				String a = getTAG1(fileContent, ".a");
				String tag1 = getTAG2(fileContent, "tag1");
				String tag2 = getTAG2(fileContent, "tag2");
				String newFileName = MD5Util.md5(q + a) + ".html";
				//File newFile = new File(filePath + "\\" + newFileName + ".html");
				File newFile = new File(filePath + "\\" + newFileName);
				String content = "<html>" +
		                "<head>" +
		                "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">" +
		                "<meta name=\"tag1\" content=\""+tag1+"\"/>" +
		                "<meta name=\"tag2\" content=\""+tag2+"\"/>" +
		                "</head>" +
		                "<body>" +
		                "<div class=\"q\">"+q+"</div>" +
		                "<br><div class=\"a\">"+a+"</div>" +
		                "</body>" +
		                "</html>";
				FileUtil.writeFile(filePath, newFileName, content);
			}
		}
	}
}
