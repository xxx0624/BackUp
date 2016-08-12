package com.shenji.search.control;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.shenji.robot.inter.IComFenciServer;
import com.shenji.search.core.control.Fenci;
import com.shenji.search.core.dic.CommonSynonymDic;
import com.shenji.search.core.engine.SynonymEngine;
import com.shenji.search.core.exception.EngineException;

public class FenciControl extends Fenci implements IComFenciServer {
	public String[] iKAnalysisWithPossibility(String str)
			throws EngineException {
		String fenci = iKAnalysis(str);
		String[] fencis = fenci.split("/");
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		SynonymEngine engine = new CommonSynonymDic();
		for (String word : fencis) {
			map.put(word, 2);
			String[] syns = engine.getSynonyms(word);
			if (syns != null && syns.length != 0) {
				for (String syn : syns) {
					if (!map.containsKey(syn)) {
						map.put(syn, 1);
					}
				}
			}
		}
		if (engine != null)
			engine.close();
		String maxFenci = iKAnalysisMax(str);
		if (maxFenci != null) {
			for (String mWord : maxFenci.split("/")) {
				map.put(mWord, 3);
			}
		}
		String[] result = new String[map.size()];
		Iterator<Map.Entry<String, Integer>> iterator = map.entrySet()
				.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			result[count++] = entry.getKey() + "#" + entry.getValue();
		}
		return result;
	}

}
