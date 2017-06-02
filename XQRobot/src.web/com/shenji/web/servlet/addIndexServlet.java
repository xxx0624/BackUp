package com.shenji.web.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.control.Configuration;
import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.engine.SynonymEngine;
import com.shenji.search.core.exception.SearchException;
import com.shenji.common.exception.ConnectionPoolException;
import com.shenji.common.log.Log;
import com.shenji.search.action.AllSearch;
import com.shenji.search.action.DBPhraseManager;
import com.shenji.search.action.FAQSearchIndex;
import com.shenji.search.core.action.FileService;
import com.shenji.search.core.dic.SynonmIndexServer;
import com.shenji.search.old.QAControl;
import com.sun.tools.jxc.gen.config.Config;

public class addIndexServlet extends HttpServlet {
	
	/*private List<IndexWriter> writers = null;
	private List<Directory> directorys = null;
	private Analyzer analyzer = null;
	private SynonymEngine engine = null;*/
	//判断用户的意图，声明7种搜索所可能包含的关键词
	String[] howQ = {"有问题","写错","有错误","有误","帮我","请帮忙","怎","如何","解决办法","咋","解决方法","什么情况"};//包括怎么样，怎么，怎么做，怎的，怎么办等一系列包括“怎”的疑问词
	String[] whyQ = {"想问","为什么","为啥","为嘛","究竟","难道","什么原因","何原因","原因？","原因?","为何","什么问题？","怎么回事","咋回事","什么情况"};
	String[] whereQ = {"在那","那里","哪","何地","什么地方","下载地址？","下载网址？"};//包括哪里，在哪，哪儿
	String[] whenQ = {"几时","多长时间","什么时","啥时","何时","几月","何月","何年","时间是？","多久","多少天","时间是？"};
	String[] whatQ = {"几位","什么文档","什么情况","多少张","填什么","什么意思","是什么","什么是","是啥","啥是","是多少","是几个","有什么","有何","什么样的","什么汇率","什么东西","多少？","是什么","谁","有什么","什么事情","多少是"};
	String[] orQ = {"好不好","吧？","吧?","是不是","吗","难道","好不好","是否","能不能","可以不","可否","有没有","了么","了吗","能否","可不可以","会不会","还是"};//包括是吗
	String[] faqQ = {};
	
	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws IllegalAccessException,
			ClassNotFoundException, Exception {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		String html1 = "<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><head><title>Search</title></head><body><div class=\"q\">";
		String html2 = "</div><div class=\"a\">";
		String html3 = "</div></body></html>";
		
		try {
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			
			String qText = URLDecoder.decode(request.getParameter("qText"),"utf-8");
			String aText = URLDecoder.decode(request.getParameter("aText"),"utf-8");
			String[] tagList = request.getParameter("tag").split(";");
			System.out.println(qText);
			System.out.println(aText);
			System.out.println(tagList[0]);
			
			XQSearchBean bean = new XQSearchBean();
			String question = qText;
			String answer = aText;
			String uri = "";
			bean.setAnswer(answer);
			bean.setQuestion(question);
			bean.setUri(uri);
			
			FAQSearchIndex searchIndex = null;
			try {
				searchIndex = new FAQSearchIndex();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				out.write("no");
			}
			for( int i=0;i<tagList.length;i++ ){
				System.out.println("current add index:"+tagList[i]);
				if(searchIndex.addIndex(question, answer,tagList[i])==1){
					String folderString = judgeContainTag3(tagList[i]);
					String filePath = Configuration.notesPath + File.separator
							+ folderString + File.separator;
					String fileName = FileService.getStringMD5String(qText + aText)+".htm";
					System.out.println(filePath);
					System.out.println(fileName);
					String fileContent = html1+qText+html2+aText+html3;
					File file = new File(filePath+fileName);
					if(!file.exists()){
				         FileOutputStream fout=new FileOutputStream(file,true);
				         StringBuffer sb=new StringBuffer();
				         sb.append(fileContent);
				         fout.write(sb.toString().getBytes("utf-8"));   
				         fout.close();
				         System.out.println("new file is ok");
					}
				}
			}
			searchIndex.close();
			
			out.write("yes");
			out.flush();
		} finally {
			out.close();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}
	
	public String judgeContainTag3(String sentence){
		/*if( sentence.contains("how") )
			return Configuration.faqFolderHowQ;
		else if( sentence.contains("why") )
			return Configuration.faqFolderWhyQ;
		else if( sentence.contains("when") )
			return Configuration.faqFolderWhenQ;
		else if( sentence.contains("where") )
			return Configuration.faqFolderWhereQ;
		else if( sentence.contains("what") )
			return Configuration.faqFolderWhatQ;
		else if( sentence.contains("or") )
			return Configuration.faqFolderOrQ;
		else if( sentence.contains("faq") )
			return Configuration.faqFolder;
		*/
		return Configuration.faqFolder;
	}
	
}