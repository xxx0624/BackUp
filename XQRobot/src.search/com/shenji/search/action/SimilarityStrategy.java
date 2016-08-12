package com.shenji.search.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.core.exception.EngineException;

abstract class SimilarityStrategy {
	public abstract void setSimilarity(List<? extends XQSearchBean> result)
			throws EngineException;

	public void sort(Comparator<? super XQSearchBean> comparator,
			List<? extends XQSearchBean> beans) {
		if (beans != null) {
			Collections.sort(beans, comparator);
		}
	}
}
