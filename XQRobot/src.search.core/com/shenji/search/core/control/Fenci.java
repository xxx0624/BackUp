package com.shenji.search.core.control;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

import com.shenji.common.log.Log;
import com.shenji.robot.inter.IComFenciServer;
import com.shenji.search.core.dic.CommonSynonymDic;
import com.shenji.search.core.dic.CustomWordDic;
import com.shenji.search.core.engine.CustomWordEngine;
import com.shenji.search.core.engine.SynonymEngine;
import com.shenji.search.core.exception.EngineException;

public class Fenci{
	public String iKAnalysis(String str) {
		return iKAnalysis(str, false);
	}

	public String iKAnalysisMax(String str) {
		String result = iKAnalysis(str, true);
		if (result != null && result.length() != 0) {
			int position = -1;
			String[] fencis = result.split("/");
			String maxFencis = "";
			for (String fenci : fencis) {
				int p = str.indexOf(fenci);
				while (p <= position) {
					fenci = fenci.substring(1);
					if (fenci == null || fenci.length() == 0)
						break;
					p = str.indexOf(fenci);
				}
				if (fenci != null && fenci.length() != 0) {
					position = p + fenci.length() - 1;
					maxFencis = maxFencis + fenci + "/";
				}
			}
			return maxFencis;
		}
		return null;
	}

	private String iKAnalysis(String str, boolean isMaxWordLength) {
		str = str.trim().toLowerCase();
		StringBuffer sb = new StringBuffer();
		CustomWordEngine engine = null;
		try {
			engine = CustomWordDic.getInstance();
		} catch (EngineException ex) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(ex);
		}
		try {	
			StringReader reader = new StringReader(str);
			IKSegmentation iks = new IKSegmentation(reader, isMaxWordLength,
					str);
			Lexeme t;
			while ((t = iks.next()) != null) {
				sb.append(t.getLexemeText() + "/");
			}
			String[] chEnDict = engine.mixCuttingEnCh(str);
			for (String s : chEnDict)
				sb.append(s + "/");
			//sb.delete(sb.length() - 1, sb.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(),e);
		} finally {
			if (engine != null)
				engine.close();
		}

		return sb.toString();
	}

	// 获得分词和分词的同义词
	public String iKAnalysisAndSyn(String str) throws EngineException {
		return iKAnalysisAndSyn(str, false);

	}

	// 最大分词、同义词
	public String iKAnalysisMaxAndSyn(String str) throws EngineException {
		return iKAnalysisAndSyn(str, true);

	}

	private String iKAnalysisAndSyn(String str, boolean isMaxWordLength)
			throws EngineException {
		String fenci;
		if (isMaxWordLength) {
			fenci = iKAnalysisMax(str);
		} else {
			fenci = iKAnalysis(str);
		}
		String[] fencis = fenci.split("/");
		List<String> list = Arrays.asList(fencis);
		;
		StringBuilder sb = new StringBuilder();
		SynonymEngine engine = new CommonSynonymDic();
		for (String word : list) {
			sb.append(word + "/");
			// 这里有点问题，应该多分点
			String[] syns = engine.getSynonyms(word);
			for (String syn : syns) {
				sb.append(syn + "/");
			}
		}
		if (engine != null)
			engine.close();
		return sb.toString();
	}


	public static void main(String[] args){
		String sentence = "网上认证怎么办";
		System.out.println(new Fenci().iKAnalysisMax(sentence));
	}
}
