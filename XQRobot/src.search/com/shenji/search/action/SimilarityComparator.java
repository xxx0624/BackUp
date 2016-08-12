package com.shenji.search.action;

import java.util.Comparator;

import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.core.action.ScoreComparator;

public class SimilarityComparator<T extends XQSearchBean> implements
		Comparator<T> {

	public int compare(T o1, T o2) {
		// TODO Auto-generated method stub
		XQSearchBean bean1 = (XQSearchBean) o1;
		XQSearchBean bean2 = (XQSearchBean) o2;
		// 从大到小排列
		if (bean1.getSimilarity() == bean2.getSimilarity()) {
			return new ScoreComparator<XQSearchBean>().compare(bean1, bean2);
		} else if (bean1.getSimilarity() > bean2.getSimilarity())
			return -1;
		else
			return 1;
	}

}
