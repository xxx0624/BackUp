package com.shenji.search.core.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.shenji.common.log.Log;
import com.shenji.search.bean.WordBoostLogBean;
import com.shenji.search.bean.WordScoreLogBean;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.control.Parameters;
import com.shenji.search.core.bean.ESearchRelation;
import com.shenji.search.core.bean.ExplainBean;
import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.bean.SearchParamsBean;
import com.shenji.search.core.dic.CommonSynonymDic;
import com.shenji.search.core.dic.CustomSynonymDic;
import com.shenji.search.core.engine.SynonymEngine;
import com.shenji.search.core.exception.EngineException;
import com.shenji.search.core.exception.SearchException;
import com.shenji.search.threadTool.InsertThread;
import com.shenji.search.threadTool.InsertWordLogExecutorPool;
import com.shenji.search.threadTool.InsertWordThread;

public abstract class AbsBooleanSearch {
	protected SearchParamsBean paramsBean = null;
	protected SynonymEngine engine_Custom = null;
	protected SynonymEngine engine_Common = null;
	private ESearchRelation relation;
	
	private static ExecutorService insertWordLogPool;

	public AbsBooleanSearch() {
		try {
			// 我的同义词引擎
			this.engine_Custom = new CustomSynonymDic();
			// 哈工大词林同义词引擎
			this.engine_Common = new CommonSynonymDic();
			//init thread pool
			if(insertWordLogPool == null || insertWordLogPool.isTerminated()){
				insertWordLogPool =  InsertWordLogExecutorPool.createInsertWordLogExecutorPool();
			}
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
	}

	public void setParamsBean(SearchParamsBean bean) {
		this.paramsBean = bean;
	}

	public List<? extends SearchBean> getResult() throws Exception {
		try {
			Map<Document, Float> map = search();
			List<? extends SearchBean> result = doDecorate(map);
			return result;
		} finally {
			// 关闭两类同义词引擎
			if (this.engine_Custom != null)
				this.engine_Custom.close();
			if (this.engine_Common != null)
				this.engine_Common.close();
			if (this.paramsBean != null) {
				this.paramsBean.clear();
			}
		}
	}

	protected abstract List<? extends SearchBean> doDecorate(
			Map<Document, Float> map);

	protected float getCustomWeight(String word) throws EngineException {
		return 1;
	}

	protected abstract List<Query> getQueriese(String filedValue, float weight);

	private Map<Document, Float> search() throws Exception {
		// 布尔查询向量
		IndexReader reader = null;
		Directory dir = null;
		Searcher searcher = null;
		File file = new File(paramsBean.getFromFolder());
		// 判断索引目录存在长度不为0
		if (file.isDirectory() && file.listFiles().length == 0) {
			throw new SearchException("Lucene Index is Null!",
					SearchException.ErrorCode.LuceneError);
		}
		// 打开索引目录
		try {
			dir = FSDirectory.open(file);
			reader = IndexReader.open(dir);
		} catch (Exception ex) {
			try {
				if (reader != null)
					reader.close();
				if (dir != null)
					dir.close();
			} catch (IOException e) {
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
			}
			throw new SearchException("Lucene Index Open failed!", ex,
					SearchException.ErrorCode.LuceneError);
		}
		try {
			searcher = new IndexSearcher(reader);
			BooleanQuery booleanQuery = new BooleanQuery();
			Iterator<String> iterator = paramsBean.getQueryFiledArray()
					.iterator();
			List<WordBoostLogBean> wordBoostLogs = new ArrayList<>();
			while (iterator.hasNext()) {
				float weight = 1;
				String filedValue = iterator.next();
				weight = this.getCustomWeight(filedValue);
				//System.out.println("text:" + filedValue + "; value:" + String.valueOf(weight));
				wordBoostLogs.add(new WordBoostLogBean(filedValue, String.valueOf(weight)));
				List<Query> queries = getQueriese(filedValue, weight);
				Occur occur = null;
				if (relation == ESearchRelation.AND_SEARCH) {
					occur = BooleanClause.Occur.MUST;
				} else if (relation == ESearchRelation.OR_SEARCH) {
					occur = BooleanClause.Occur.SHOULD;
				}
				for (Query query : queries) {
					booleanQuery.add(query, occur);
				}
				if (queries != null)
					queries.clear();
			}
			//add qa word boost log
			Callable<List<WordBoostLogBean>> c1 = new InsertWordThread(paramsBean.getSentence(), wordBoostLogs, null);
			insertWordLogPool.submit(c1);
			TopDocs topDocs = searcher.search(booleanQuery, Parameters.maxResult);
			ScoreDoc[] docs = topDocs.scoreDocs;
			// 构造查询结果集
			Map<Document, Float> map = new LinkedHashMap<Document, Float>();
			int cnt = 0;
			for (ScoreDoc doc : docs) {
				map.put(searcher.doc(doc.doc), doc.score);
				//add qa word score log
				if(cnt <= 2){
					Explanation explanation = searcher.explain(booleanQuery, doc.doc);
					List<WordScoreLogBean> wordScoreLogBeans = analyseExplanation(explanation, cnt);
					Callable<List<WordBoostLogBean>> c2 = new InsertWordThread(paramsBean.getSentence(), null, wordScoreLogBeans);
					insertWordLogPool.submit(c2);
					cnt += 1;
				}
			}
			Log.getLogger(this.getClass()).info("共找到：" + topDocs.totalHits);
			return map;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new SearchException(
					"Search ScoreDocs has IoException!", e,
					SearchException.ErrorCode.SearchDocError);
		} catch (Exception ex) {
			// TODO: handle exception
			throw new SearchException(
					"Search ScoreDocs has UnKnow Exception!", ex,
					SearchException.ErrorCode.UnKnowError);
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (dir != null)
					dir.close();
				if (searcher != null)
					searcher.close();
			} catch (Exception e) {
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
	}

	protected Query createQuery(String filedKey, String filedValue, float weight) {
		Term term = new Term(filedKey, filedValue);
		Query query = new TermQuery(term);
		query.setBoost(weight);
		return query;
	}
	
	private List<WordScoreLogBean> analyseExplanation(Explanation explanation, int sort_num){
		List<WordScoreLogBean> wordScoreLogBeans = new ArrayList<>();
		//System.out.println("Description:" + explanation.getDescription());
		//System.out.println("Value:" + explanation.getValue());
		for(int i = 0; i < explanation.getDetails().length; i ++){
			//System.out.println("	description("+ i +"):" + explanation.getDetails()[i].getDescription());
			//System.out.println("	value("+ i +"):" + explanation.getDetails()[i].getValue());
			if(explanation.getDetails()[i].getDetails() != null){
				for(int j = 0; j < explanation.getDetails()[i].getDetails().length; j ++){
					wordScoreLogBeans.add(new WordScoreLogBean(
							explanation.getDetails()[i].getDetails()[j].getDescription(), 
							String.valueOf(explanation.getDetails()[i].getDetails()[j].getValue()), 
							sort_num)
							);
					//System.out.println("		description("+ i +")("+ j +"):" +  explanation.getDetails()[i].getDetails()[j].getDescription());
					//System.out.println("		value("+ i +")("+ j +"):" + explanation.getDetails()[i].getDetails()[j].getValue());
				}
			}
		}
		return wordScoreLogBeans;
	}

}
