package com.shenji.search.core.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.shenji.search.core.action.ScoreComparator;
import com.shenji.search.core.bean.ESearchRelation;
import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.bean.SearchParamsBean;
import com.shenji.search.core.control.Fenci;
import com.shenji.search.core.exception.SearchException;
import com.shenji.search.core.inter.ISearchFolder;

public class Search {
	private static ExecutorService pool;

	public Search() {
		if(pool==null||pool.isTerminated())
			pool = ExecutorPool.createExcutorService();
	}

	protected List<? extends SearchBean> search(String sentence,
			ISearchFolder iSearchFolder, ESearchRelation relation,AbsBooleanSearch booleanSearch)
			throws SearchException {
		// 存放带返回值的线程列表
		List<Future<List<? extends SearchBean>>> list = new ArrayList<Future<List<? extends SearchBean>>>();
		// 存放查询结果的结果集
		List<SearchBean> result = new ArrayList<SearchBean>();
		try {
			String[] searchFolder = iSearchFolder.getSearchFolder();
			SearchParamsBean paramsBean = this.getSearchParamBean(sentence,
					relation);
			for (int i = 0; i < searchFolder.length; i++) {
				// 新建线程
				paramsBean.setFromFolder(searchFolder[i]);
				Callable<List<? extends SearchBean>> c = new SearchThread(paramsBean,booleanSearch);
				// 提交带返回值的线程给线程池
				Future<List<? extends SearchBean>> f = pool.submit(c);
				list.add(f);
			}
			for (Future<List<? extends SearchBean>> f : list) {
				// 阻塞方法，得到线程中的结果
				List<? extends SearchBean> subList = f.get();
				// 普通打分排序
				if (subList != null && subList.size() > 0) {
					Collections.sort(subList, new ScoreComparator<SearchBean>());
					result.addAll(subList);
				}
			}
			if (paramsBean != null)
				paramsBean.clear();
			return result;
		} catch (ExecutionException e) {
			// 判断ExecutionException包装的异常是否为自定义异常
			if (e.getCause() instanceof SearchException) {// 自定义的异常
				throw ((SearchException) e.getCause());
			} else {// 其他可能出现的异常
				throw new SearchException("Unknow Error in Search!",
						e.getCause(), SearchException.ErrorCode.UnKnowError);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			throw new SearchException(
					"Unknow Error in Search by interRuptedException!",
					e.getCause(), SearchException.ErrorCode.UnKnowError);
		}
	}

	private SearchParamsBean getSearchParamBean(String sentence,
			ESearchRelation relation) {
		SearchParamsBean paramsBean = new SearchParamsBean(sentence, relation,
				getFineGrainedParticipleArray(sentence),
				getCoarseGrainedParticipleArray(sentence));
		return paramsBean;
	}

	private static Set<String> getFineGrainedParticipleArray(String sentence) {
		return getParticipleArray(sentence, false);
	}

	private static Set<String> getCoarseGrainedParticipleArray(String sentence) {
		return getParticipleArray(sentence, true);
	}

	private static Set<String> getParticipleArray(String sentence,
			boolean isCoarseGrained) {
		Set<String> participleArray = new LinkedHashSet<String>();
		Fenci fenci = new Fenci();
		String participleStr;
		if (isCoarseGrained)
			participleStr = fenci.iKAnalysisMax(sentence);
		else
			participleStr = fenci.iKAnalysis(sentence);
		participleArray.addAll(Arrays.asList(participleStr.split("/")));
		return participleArray;
	}

}