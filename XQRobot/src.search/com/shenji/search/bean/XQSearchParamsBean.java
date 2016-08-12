package com.shenji.search.bean;

import java.util.Set;

import com.shenji.search.core.bean.ESearchRelation;
import com.shenji.search.core.bean.SearchParamsBean;

public class XQSearchParamsBean extends SearchParamsBean {
	public XQSearchParamsBean(String sentence, ESearchRelation relation,
			Set<String> fineGrainedParticipleArray,
			Set<String> coarseGrainedParticipleArray) {
		super(sentence, relation, fineGrainedParticipleArray,
				coarseGrainedParticipleArray);
	}

}
