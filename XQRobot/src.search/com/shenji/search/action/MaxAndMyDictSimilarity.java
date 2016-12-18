package com.shenji.search.action;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

import com.shenji.common.log.Log;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.control.Parameters;
import com.shenji.search.core.dic.CustomWordDic;
import com.shenji.search.core.engine.CustomWordEngine;
import com.shenji.search.core.exception.EngineException;

public class MaxAndMyDictSimilarity extends SimilarityStrategy {
	// private SynonymEngine engine;
	private List<String> matchList;
	private Set<String> maxMatchSet;
	
	private double eps = 0.00000000001;
	
	private double tfidfPart = 0.7;

	public MaxAndMyDictSimilarity(String args) throws EngineException {
		this.matchList = new ArrayList<String>();
		this.maxMatchSet = new HashSet<String>();
		initMachingList(args);

	}

	public void setSimilarity(List<? extends XQSearchBean> beans)
			throws EngineException {
		CustomWordEngine customWordEngine = null;
		customWordEngine = CustomWordDic.getInstance();
		if (beans == null || beans.size() == 0)
			return;
		double qaProportion = Parameters.qaProportion;
		List<String> inIkDictList = new ArrayList<String>();
		Set<String> leftIkDictSet = new HashSet<String>();
		for (String s : matchList) {
			if (customWordEngine.isCustomWord(s.trim().toLowerCase())){
				inIkDictList.add(s.trim().toLowerCase());
			}
			else if (customWordEngine.isCustomWord(s.trim())){
				inIkDictList.add(s.trim().toLowerCase());
			}
			else{
				leftIkDictSet.add(s.trim().toLowerCase());
			}
		}
		double num = inIkDictList.size();
		for (XQSearchBean searchBean : beans) {
			double similarity = 0;
			String answer = searchBean.getAnswer().toLowerCase();
			String question = searchBean.getQuestion().toLowerCase();
			String answerMatch = "_";
			String questionMatch = "_";
			for (String s : inIkDictList) {
				if ((!answerMatch.contains(s)) && answer.contains(s)) {
					similarity += 0.1 * qaProportion;
					answerMatch = answerMatch + "_" + s;
				}
				if ((!questionMatch.contains(s)) && question.contains(s)) {
					similarity += 1;
					questionMatch = questionMatch + "_" + s;
				}
			}
			for (String s:leftIkDictSet){
				if(answer.contains(s)){
					similarity += 0.01;
				}
				if(question.contains(s)){
					similarity += 0.01 * qaProportion;
				}
			}
			
			 //before
			similarity = similarity / num + searchBean.getScore();
			
			searchBean.setSimilarity(similarity);
		}
		if (customWordEngine != null)
			customWordEngine.close();
	}

	private void initMachingList(String args) {
		StringReader reader = new StringReader(args);
		IKSegmentation iks = new IKSegmentation(reader, false);
		Lexeme t;
		try {
			while ((t = iks.next()) != null) {
				String word = t.getLexemeText();
				if (word.length() > 1)
					this.matchList.add(word);
			}
		} catch (IOException e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		StringReader reader_max = new StringReader(args);
		IKSegmentation iks_max = new IKSegmentation(reader_max, true);
		Lexeme t_max;
		try {
			while ((t_max = iks_max.next()) != null) {
				String word = t_max.getLexemeText();
				if (word.length() > 1) {
					this.maxMatchSet.add(word);
					// System.err.println(word);
				}
			}
		} catch (IOException e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}

}
