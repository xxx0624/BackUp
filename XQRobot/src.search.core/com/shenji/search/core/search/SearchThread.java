package com.shenji.search.core.search;

import java.util.List;
import java.util.concurrent.Callable;

import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.bean.SearchParamsBean;
import com.shenji.search.core.exception.SearchException;

/**
 * 查询线程（带返回值）
 * 
 * @author sj
 * 
 */
public class SearchThread implements Callable<List<? extends SearchBean>> {
	private AbsBooleanSearch booleanSearch;

	public SearchThread(SearchParamsBean paramsBean,AbsBooleanSearch booleanSearch) {
		this.booleanSearch=booleanSearch;
		this.booleanSearch.setParamsBean(paramsBean);
	}

	public List<? extends SearchBean> call() throws Exception {
		// 构建布尔查询对象并返回结果
		List<? extends SearchBean> result = null;
		try {
			result = this.booleanSearch.getResult();
		} catch (SearchException e) {
			// 这里发生严重错误，没有查询结果，需要子线程抛给主线程进行处理
			throw e;// 这里线程池会包装成ExecutionException异常
		}
		return result;

	}

}
