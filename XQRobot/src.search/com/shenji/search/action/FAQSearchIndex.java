package com.shenji.search.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.shenji.common.log.Log;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.control.Configuration;
import com.shenji.search.core.action.FileService;
import com.shenji.search.core.action.SearchIndex;
import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.exception.SearchException;

public class FAQSearchIndex extends SearchIndex {
	private int count = 0;
	// 朱
	/*
	 * public static String[] indexPaths = new String[] {
	 * Configuration.indexPath + File.separator + Configuration.faqFolder };
	 */
	// xxx0624
	public static String[] indexPaths = new String[] {
			Configuration.indexPath + File.separator + Configuration.faqFolder
			,Configuration.indexPath + File.separator
					+ Configuration.faqFolderHowQ,
			Configuration.indexPath + File.separator
					+ Configuration.faqFolderWhyQ,
			Configuration.indexPath + File.separator
					+ Configuration.faqFolderWhereQ,
			Configuration.indexPath + File.separator
					+ Configuration.faqFolderWhenQ,
			Configuration.indexPath + File.separator
					+ Configuration.faqFolderWhatQ,
			Configuration.indexPath + File.separator
					+ Configuration.faqFolderOrQ 
					};

	private FAQSearchIndex(String[] indexFolders) throws Exception {
		super(indexFolders);
		// TODO Auto-generated constructor stub
	}

	public FAQSearchIndex() throws Exception {
		this(indexPaths);
	}

	@Override
	protected List<Field> getFileds(SearchBean bean) throws Exception {
		// TODO Auto-generated method stub
		XQSearchBean searchBean = null;
		if (bean instanceof XQSearchBean) {
			searchBean = (XQSearchBean) bean;
		} else {
			throw new SearchException("传入的bean数据非法！",
					SearchException.ErrorCode.SearchBeanError);
		}
		List<Field> fields = new ArrayList<Field>(3);
		Field q_field = new Field(XQSearchBean.Field.QUESTION,
				searchBean.getQuestion(), Field.Store.YES,
				Field.Index.ANALYZED, TermVector.NO);
		Field a_field = new Field(XQSearchBean.Field.ANSWER,
				searchBean.getAnswer(), Field.Store.YES, Field.Index.ANALYZED,
				TermVector.NO);
		Field uri_field = new Field(XQSearchBean.Field.URI,
				searchBean.getUri(), Field.Store.YES, Field.Index.NOT_ANALYZED,
				TermVector.NO);
		/*
		 * Field uri_field = new Field(XQSearchBean.Field.URI, "faq/" +
		 * searchBean.getUri() + ".htm", Field.Store.YES,
		 * Field.Index.NOT_ANALYZED, TermVector.NO);
		 */
		fields.add(q_field);
		fields.add(a_field);
		fields.add(uri_field);
		return fields;
	}

	public String judgeContainTag3(String sentence) {
		if (sentence.contains("how"))
			return Configuration.faqFolderHowQ;
		else if (sentence.contains("why"))
			return Configuration.faqFolderWhyQ;
		else if (sentence.contains("when"))
			return Configuration.faqFolderWhenQ;
		else if (sentence.contains("where"))
			return Configuration.faqFolderWhereQ;
		else if (sentence.contains("what"))
			return Configuration.faqFolderWhatQ;
		else if (sentence.contains("or"))
			return Configuration.faqFolderOrQ;
		else if (sentence.contains("faq"))
			return Configuration.faqFolder;
		return Configuration.faqFolder;
	}

	public int addIndex(String q, String a, String tag) throws Exception {
		q = q.toLowerCase();
		a = a.toLowerCase();
		String fileText = this.createSearchText(q, a);
		String fileName = FileService.getStringMD5String(q + a);
		if (fileName == null)
			throw new RuntimeException("文件名创建失败！");
		String folderString = judgeContainTag3(tag);
		String path = Configuration.notesPath + File.separator + folderString
				+ File.separator;
		if (FileService.addFile(fileText, path, fileName, ".htm", "UTF-8") == false) {
			Log.getLogger(this.getClass()).info("文件已存在:" + fileName);
			return -1;// 文件已存在
		}
		XQSearchBean bean = new XQSearchBean();
		bean.setAnswer(a);
		bean.setQuestion(q);
		// 这里URI要并结成URL
		String uri = folderString + "/" + fileName
				+ XQSearchBean.Field.FileSuffixes;
		bean.setUri(uri);
		try {
			if (super.addIndex(bean, tag)) {
				super.commit();
				return 1;// 添加成功
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		return -2;// 其他问题
	}

	public int delIndex(String url) throws Exception {
		String fileName = url.replace(Configuration.webPath + "/"
				+ Configuration.faqFolder + "/", "");
		fileName = fileName.replace(XQSearchBean.Field.FileSuffixes, "");
		String filePath = Configuration.notesPath + "/"
				+ Configuration.faqFolder;
		if (!FileService.deleteFile(filePath, fileName,
				XQSearchBean.Field.FileSuffixes)) {
			Log.getLogger(this.getClass()).info("文件不存在:" + fileName);
			return -1;
		}
		try {
			String uri = Configuration.faqFolder + "/" + fileName
					+ XQSearchBean.Field.FileSuffixes;
			if (super.deleteIndex(uri)) {
				super.commit();
				return 1;// 添加成功
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
		return -2;// 其他问题
	}

	public int modifyIndex(String url, String q, String a, String tag)
			throws Exception {
		if (this.delIndex(url) != 1)
			return -1;// 删除失败
		else {
			return this.addIndex(q, a, tag);
		}
	}

	private String createSearchText(String q, String a) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("<head><title>Search</title></head><body>");
		q = q.replace("\n", "<br>");
		sb.append("<div class=\"q\">" + q + "</div><br>");
		a = a.replace("\n", "<br>");
		sb.append("<div class=\"a\">" + a + "</div>");
		sb.append("</body></html>");
		return sb.toString();
	}

	@Override
	public boolean createIndex() {
		// TODO Auto-generated method stub
		File htmlFile = new File(Configuration.searchDocmentDirs[0]);
		try {
			System.out.println("FAQ库faq开始重建...");
			createIndexDir(htmlFile, "faq");
			super.commit();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		Log.getLogger(this.getClass()).info("FAQ库faq重建索引完成！");
		// 分类
		/*
		File htmlFileHowQ = new File(Configuration.searchDocmentDirsHowQ[0]);
		try {
			System.out.println("FAQ库how开始重建...");
			createIndexDir(htmlFileHowQ, "how");
			super.commit();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		Log.getLogger(this.getClass()).info("FAQ库how重建索引完成！");
		File htmlFileWhyQ = new File(Configuration.searchDocmentDirsWhyQ[0]);
		try {
			System.out.println("FAQ库why开始重建...");
			createIndexDir(htmlFileWhyQ, "why");
			super.commit();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		Log.getLogger(this.getClass()).info("FAQ库why重建索引完成！");
		File htmlFileWhereQ = new File(Configuration.searchDocmentDirsWhereQ[0]);
		try {
			System.out.println("FAQ库where开始重建...");
			createIndexDir(htmlFileWhereQ, "where");
			super.commit();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		Log.getLogger(this.getClass()).info("FAQ库where重建索引完成！");
		File htmlFileWhenQ = new File(Configuration.searchDocmentDirsWhenQ[0]);
		try {
			System.out.println("FAQ库when开始重建...");
			createIndexDir(htmlFileWhenQ, "when");
			super.commit();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		Log.getLogger(this.getClass()).info("FAQ库when重建索引完成！");
		File htmlFileWhatQ = new File(Configuration.searchDocmentDirsWhatQ[0]);
		try {
			System.out.println("FAQ库what开始重建...");
			createIndexDir(htmlFileWhatQ, "what");
			super.commit();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		Log.getLogger(this.getClass()).info("FAQ库what重建索引完成！");
		File htmlFileOrQ = new File(Configuration.searchDocmentDirsOrQ[0]);
		try {
			System.out.println("FAQ库or开始重建...");
			createIndexDir(htmlFileOrQ, "or");
			super.commit();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		Log.getLogger(this.getClass()).info("FAQ库or重建索引完成！");
		*/
		return true;

	}

	private void createIndexDir(File dir, String tag) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				createIndexDir(files[i], tag);
			else
				createIndexFile(files[i], tag);
		}
	}

	private void createIndexFile(File file, String tag) {
		try {
			String html = FileService.read(file.getCanonicalPath(), "utf-8");
			System.out.println(html);
			// String content = delelteHTMLTag(html);
			XQSearchBean bean = new XQSearchBean();
			String question = getQuestion(html);
			String answer = getAnswer(html);
			String uri = getURI(file);
			bean.setAnswer(answer);
			// bean.setContent(content);
			bean.setQuestion(question);
			bean.setUri(uri);
			System.out.println("uri=" + uri);
			super.addIndex(bean, tag);
			System.out.print(count++ + " ");
			if (count % 200 == 0)
				System.out.println("");

		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}

	private String delelteHTMLTag(String htmlStr) {
		htmlStr = htmlStr.replaceAll(".*?<body.*?>(.*?)", "$1"); // match body
		htmlStr = htmlStr.replaceAll("(?is)<script.*?>.*?</script>", ""); // remove
																			// //
																			// javascript
		htmlStr = htmlStr.replaceAll("(?is)<style.*?>.*?</style>", ""); // remove
																		// // //
																		// //
																		// css
		htmlStr = htmlStr.replaceAll("(?is)<.*?>", ""); // remove all
		return htmlStr.trim();
	}

	private String getQuestion(String htmlStr) {
		org.jsoup.nodes.Document document = null;
		document = Jsoup.parse(htmlStr);
		Elements meta = document.select(".q");
		// System.out.println(meta.text());
		return meta.text();
	}

	private String getAnswer(String htmlStr) {
		org.jsoup.nodes.Document document = null;
		document = Jsoup.parse(htmlStr);
		Elements meta = document.select(".a");
		// System.out.println(meta.text());
		return meta.text();
	}

	private String getURI(File file) {
		try {
			String path = file.getCanonicalPath();
			path = path.replace("\\", "/");
			path = path.replace(Configuration.notesPath, "");
			path = path.substring(1, path.length());
			return path.replace('\\', '/');
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return null;
		}
	}

}
