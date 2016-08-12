package com.shenji.search.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import com.shenji.common.log.Log;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.control.Configuration;

public class AllSearch {
	public List<XQSearchBean> getAllSearchDoc(String dir) {
		IndexReader indexReader = null;
		List<XQSearchBean> listBean = new ArrayList<XQSearchBean>();
		try {
			indexReader = IndexReader.open(FSDirectory.open(new File(dir)));
			int max = indexReader.maxDoc();
			for (int n = 0; n < max; n++) {
				Document doc = indexReader.document(n);
				if (doc.getBoost() > 1.0)
					System.err.println(doc.getBoost());
				XQSearchBean bean = new XQSearchBean();
				/*
				 * String content = doc.get("content"); content =
				 * content.substring
				 * (0,content.length()>200?200:content.length());
				 */
				/*
				 * content="<div class=\"q\">"+question+"</div><div class=\"a\">"
				 * +answer+"<br></div>";
				 * bean.setContent("<a href=\""+path+"\">"+content+"</a>");
				 */
				String uri = doc.get(XQSearchBean.Field.URI);
				String url = Configuration.webPath + "/" + uri;
				String question = doc.get(XQSearchBean.Field.QUESTION);
				String answer = doc.get(XQSearchBean.Field.ANSWER);
				bean.setAnswer(answer);
				bean.setQuestion(question);
				bean.setUri(uri);
				String content = "<div class=\"" + XQSearchBean.Field.QUESTION
						+ "\">" + question + "</div><div class=\""
						+ XQSearchBean.Field.ANSWER + "\">" + answer
						+ "<br></div>";
				bean.setHtmlContent("<a href=\"" + url + "\">" + content
						+ "</a>");
				listBean.add(bean);
			}
			indexReader.close();
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		return listBean;
	}

}