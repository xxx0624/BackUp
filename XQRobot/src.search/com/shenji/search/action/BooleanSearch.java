package com.shenji.search.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import com.shenji.common.log.Log;
import com.shenji.common.util.StringMatching;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.control.Configuration;
import com.shenji.search.control.Parameters;
import com.shenji.search.core.bean.ESearchRelation;
import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.dic.BusinessDic;
import com.shenji.search.core.dic.CustomWordDic;
import com.shenji.search.core.engine.CustomWordEngine;
import com.shenji.search.core.exception.EngineException;
import com.shenji.search.core.search.AbsBooleanSearch;

public class BooleanSearch extends AbsBooleanSearch {
	public BooleanSearch() {
		super();
	}

	/**
	 * 关键词、同义词标记
	 * 
	 * @param str
	 *            HTML文本
	 * @return HTML文本
	 * @throws SearchProcessException
	 */
	private String markContent(String str) {
		for (String s : paramsBean.getQueryFiledArray()) { // if()
			String[] strings = null;
			try {
				str = str.replace(s, "<em>" + s + "</em>");
				if (engine_Custom != null)
					if ((strings = engine_Custom.getSynonyms(s)) != null) {
						for (int i = 0; i < strings.length; i++) {
							str = str.replace(strings[i], "<em>" + strings[i]
									+ "</em>");
						}
					}
				if (engine_Common != null) {
					if ((strings = engine_Common.getSynonyms(s)) != null) {
						for (int i = 0; i < strings.length; i++) {
							str = str.replace(strings[i], "<b>" + strings[i]
									+ "</b>");
						}
					}
				}
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				// 同义词标记失败，这个异常不应该抛出去
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
		return str;
	}

	@Override
	protected List<? extends SearchBean> doDecorate(Map<Document, Float> map) {
		// TODO Auto-generated method stub
		List<XQSearchBean> tmplistBean = new ArrayList<XQSearchBean>();// 构造结果集
		Iterator<Map.Entry<Document, Float>> iterator = map.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Map.Entry<Document, Float> entry = iterator.next();
			Document doc = entry.getKey();
			float score = entry.getValue();
			String question = doc.get(XQSearchBean.Field.QUESTION);
			String answer = doc.get(XQSearchBean.Field.ANSWER);
			String uri = doc.get(XQSearchBean.Field.URI);
			String tag1 = doc.get(XQSearchBean.Field.TAG1);
			String tag2 = doc.get(XQSearchBean.Field.TAG2);
			String shortAnswer = doc.get(XQSearchBean.Field.ShortAnswer);
			XQSearchBean bean = new XQSearchBean();
			bean.setQuestion(question);
			bean.setAnswer(answer);
			bean.setUri(uri);
			bean.setTag1(tag1);
			bean.setTag2(tag2);
			bean.setShortAnswer(shortAnswer);
			// 固有相似度
			String[] simWords = paramsBean.getCoarseGrainedParticipleArray()
					.toArray(
							new String[paramsBean
									.getCoarseGrainedParticipleArray().size()]);
			double similarity = StringMatching.getInherentSimilarity(simWords,
					question + answer);
			bean.setSimilarity(similarity);
			bean.setScore(score);
			/*
			 * // 显示最大文本 String showContent = content .substring( 0,
			 * content.length() > Parameters.maxTestShow ?
			 * Parameters.maxTestShow : content.length());
			 */
			String url = Configuration.webPath + "/" + uri;// +
															// XQSearchBean.Field.FileSuffixes;
			// 构造FAQ内容
			String content = "<div class=\"" + XQSearchBean.Field.QUESTION
					+ "\">" + question + "</div><div class=\""
					+ XQSearchBean.Field.ANSWER + "\">" + "<font size=\"2\">"
					+ answer + "</font><br></div>";
			content = markContent(content);
			content = "<a href=\"" + url + "\">" + content + "</a>";
			bean.setHtmlContent(content);
			tmplistBean.add(bean);
		}
		return tmplistBean;
	}

	@Override
	protected float getCustomWeight(String word) throws EngineException {
		// TODO Auto-generated method stub
		float weight = 1;
		// 业务词典设置权重
		float weight_bussiness = 1;
		try {
			weight_bussiness = BusinessDic.getInstance().getWeight(word);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		if (weight_bussiness != -1) {
			weight = weight * weight_bussiness;
		}
		// 最长匹配词典设置权重
		if (paramsBean.getCoarseGrainedParticipleArray().contains(word))
			weight = weight * Parameters.maxMatchWeight;
		CustomWordEngine engine = CustomWordDic.getInstance();
		// 自建词典设置权重
		if (engine.isCustomWord(word))
			weight = weight * Parameters.myIkdictWeight * word.length();
		return weight;
	}

	@Override
	protected List<Query> getQueriese(String filedValue, float weight) {
		// TODO Auto-generated method stub
		List<Query> queries = new ArrayList<Query>();
		Query query_q = this.createQuery(XQSearchBean.Field.QUESTION,
				filedValue, weight);
		queries.add(query_q);
		if (paramsBean.getRelation().equals(ESearchRelation.OR_SEARCH)) {
			Query query_a = this.createQuery(XQSearchBean.Field.ANSWER,
					filedValue, weight / Parameters.qaProportion);
			queries.add(query_a);
		}
		return queries;
	}

}
