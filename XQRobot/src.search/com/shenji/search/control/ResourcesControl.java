package com.shenji.search.control;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.shenji.common.exception.ConnectionPoolException;
import com.shenji.common.log.Log;
import com.shenji.search.action.AllSearch;
import com.shenji.search.action.DBPhraseManager;
import com.shenji.search.action.FAQSearchIndex;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.core.action.FileService;
import com.shenji.search.core.dic.SynonmIndexServer;
import com.shenji.search.old.QAControl;

public class ResourcesControl {
	public int addNewFAQ(String question[], String answer[], String tag) {
		if (question == null || question.length == 0 || answer == null
				|| answer.length == 0)
			return 0;// 参数错误
		FAQSearchIndex searchIndex = null;
		try {
			try {
				searchIndex = new FAQSearchIndex();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				return -2;// 创建失败
			}
			int reFlag = 1;
			try {
				for (int i = 0; i < question.length; i++) {
					searchIndex.addIndex(question[i], answer[i], tag);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				reFlag = -1;// 添加失败
			}
			return reFlag;
		} finally {
			if (searchIndex != null)
				searchIndex.close();
		}

	}

	public String listAllFaq() {
		StringBuilder html = new StringBuilder(
				"<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><head><title>Search</title>");
		html.append("<style>em{font-style:normal;color:#cc0000}</style></head><body>");
		try {
			List<XQSearchBean> result = new AllSearch()
					.getAllSearchDoc(Configuration.searchIndexDirs[0]);
			for (XQSearchBean bean : result) {
				html.append("<div>" + bean.getHtmlContent() + "</div><br>");
			}
			// return html.toString();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			html.append("<div>FAQ为空!</div>");
		}
		html.append("</body></html>");
		return html.toString();
	}

	public int deleteFAQ(String[] url) {
		if (url == null || url.length == 0)
			return 0;// 参数错误

		FAQSearchIndex searchIndex = null;
		try {
			try {
				searchIndex = new FAQSearchIndex();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				return -2;// 创建失败
			}
			int reFlag = 1;
			try {
				for (int i = 0; i < url.length; i++) {
					searchIndex.delIndex(url[i]);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				reFlag = -1;// 删除失败
			}
			return reFlag;
		} finally {
			if (searchIndex != null)
				searchIndex.close();
		}
	}

	public int changeFAQ(String url[], String question[], String answer[],
			String tag) {
		if (url == null || url.length == 0 || question == null
				|| question.length == 0 || answer == null || answer.length == 0)
			return 0;// 参数错误
		FAQSearchIndex searchIndex = null;
		try {
			try {
				searchIndex = new FAQSearchIndex();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				return -2;// 创建失败
			}
			int reFlag = 1;
			try {
				for (int i = 0; i < question.length; i++) {
					searchIndex
							.modifyIndex(url[i], question[i], answer[i], tag);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				reFlag = -1;// 删除失败
			}
			return reFlag;
		} finally {
			if (searchIndex != null)
				searchIndex.close();
		}
	}

	public synchronized boolean rebuildIndex() {
		DBPhraseManager dbManager = new DBPhraseManager();
		String operation = "重建索引";
		dbManager.modifyLog(operation, operation);
		boolean b = false;
		b = new SynonmIndexServer().createIndex();
		// 删除索引目录
		File indexFile = null;
		for (int i = 0; i < FAQSearchIndex.indexPaths.length; i++) {
			indexFile = new File(FAQSearchIndex.indexPaths[i]);
			if (indexFile.exists()) {
				FileService.deleteDir(indexFile);
			}
			indexFile.mkdir();
		}

		FAQSearchIndex searchIndex = null;
		try {
			try {
				searchIndex = new FAQSearchIndex();
				System.out.println("ResourceControl开始重建索引");
				b = searchIndex.createIndex() && b;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
				return false;// 创建失败
			}
		} finally {
			if (searchIndex != null)
				searchIndex.close();
		}
		return b;
	}

	public String getPhrase(String Question) {
		Question = Question.trim();
		DBPhraseManager dbManager = new DBPhraseManager();
		try {
			return dbManager.getAnswer(Question);
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return null;
		}
	}

	public int addPhrase(String Question, String Answer) {
		Question = Question.trim();
		Answer = Answer.trim();
		DBPhraseManager dbManager = new DBPhraseManager();
		try {
			return dbManager.addQA(Question, Answer);
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return -1;
		}
	}

	public int delPhrase(String Question) {
		Question = Question.trim();
		DBPhraseManager dbManager = new DBPhraseManager();
		try {
			return dbManager.delQA(Question);
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return -1;
		}
	}

	// 修改常用语
	public int modifyPhrase(String Question, String Answer) {
		Question = Question.trim();
		Answer = Answer.trim();
		DBPhraseManager dbManager = new DBPhraseManager();
		try {
			return dbManager.modifyQA(Question, Answer);
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return -1;
		}
	}

	public String[][] listAllPhrase() {
		HashMap<String, String> qaMap = null;
		DBPhraseManager dbManager = new DBPhraseManager();
		try {
			qaMap = (HashMap<String, String>) dbManager.listAllQA();
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return null;
		}
		Iterator<Map.Entry<String, String>> iterator = qaMap.entrySet()
				.iterator();
		String[][] reStrs = new String[qaMap.size()][2];
		int count = 0;
		while (iterator.hasNext()) {
			Map.Entry<String, String> map = iterator.next();
			reStrs[count][0] = map.getKey();
			reStrs[count][1] = map.getValue();
			count++;
		}
		return reStrs;
	}

	/*
	 * public static void main(String[] str) { System.out.println(new
	 * ResourcesControl().addNewFAQ("Test", "Test")); }
	 */
}
