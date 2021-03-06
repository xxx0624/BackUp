package com.shenji.search.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;



import com.ctc.wstx.util.StringUtil;
import com.hp.hpl.jena.sparql.algebra.BeforeAfterVisitor;
import com.shenji.common.exception.ConnectionPoolException;
import com.shenji.common.log.Log;
import com.shenji.common.util.HttpUtils;
import com.shenji.robot.action.DBUserManager;
import com.shenji.robot.data.ResultShowBean;
import com.shenji.robot.exception.OntoReasonerException;
import com.shenji.robot.exception.OntoReasonerException.ErrorCode;
import com.shenji.robot.inter.IComReasonerServer;
import com.shenji.robot.util.CommaUtil;
import com.shenji.search.action.BooleanSearch;
import com.shenji.search.action.DBPhraseManager;
import com.shenji.search.action.DividingLineServer;
import com.shenji.search.action.MaxAndMyDictSimilarity;
import com.shenji.search.action.SearchNonBusinessMatching;
import com.shenji.search.action.SearchPatternMatching;
import com.shenji.search.action.SimilarityComparator;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.control.IEnumSearch.ResultCode;
import com.shenji.search.core.bean.ESearchRelation;
import com.shenji.search.core.bean.SearchBean;
import com.shenji.search.core.control.Fenci;
import com.shenji.search.core.dic.CommonSynonymDic;
import com.shenji.search.core.engine.SynonymEngine;
import com.shenji.search.core.exception.EngineException;
import com.shenji.search.core.exception.SearchException;
import com.shenji.search.core.inter.ISearchFolder;
import com.shenji.search.core.search.AbsBooleanSearch;
import com.shenji.search.core.search.Search;
import com.shenji.search.core.search.SearchThread;
import com.shenji.search.enums.LogType.LogTypeEnum;
import com.shenji.search.threadTool.InsertLocalLogExecutorPool;
import com.shenji.search.threadTool.InsertLocalLogThread;
import com.shenji.search.threadTool.InsertLogExecutorPool;
import com.shenji.search.threadTool.InsertThread;
import com.shenji.web.bean.QALogBean;

public class SearchControl extends Search {
	
	private static ExecutorService insertLogPool;
	
	private static ExecutorService insertLocalLogPool;
	
	private boolean pretreatmentResult = false;
	
	private double topSimilarity = 5.01;
	private double lowSimilarity = 0.01;

	// 判断用户的意图，声明7种搜索所可能包含的关键词
	/*
	String[] howQ = { "有问题", "写错", "有错误", "有误", "帮我", "请帮忙", "怎", "如何", "解决办法",
			"咋", "解决方法", "什么情况" };// 包括怎么样，怎么，怎么做，怎的，怎么办等一系列包括“怎”的疑问词
	String[] whyQ = { "想问", "为什么", "为啥", "为嘛", "究竟", "难道", "什么原因", "何原因",
			"原因？", "原因?", "为何", "什么问题？", "怎么回事", "咋回事", "什么情况" };
	String[] whereQ = { "在那", "那里", "哪", "何地", "什么地方", "下载地址？", "下载网址？" };// 包括哪里，在哪，哪儿
	String[] whenQ = { "几时", "多长时间", "什么时", "啥时", "何时", "几月", "何月", "何年",
			"时间是？", "多久", "多少天", "时间是？" };
	String[] whatQ = { "几位", "什么文档", "什么情况", "多少张", "填什么", "什么意思", "是什么",
			"什么是", "是啥", "啥是", "是多少", "是几个", "有什么", "有何", "什么样的", "什么汇率",
			"什么东西", "多少？", "是什么", "谁", "有什么", "什么事情", "多少是" };
	String[] orQ = { "好不好", "吧？", "吧?", "是不是", "吗", "难道", "好不好", "是否", "能不能",
			"可以不", "可否", "有没有", "了么", "了吗", "能否", "可不可以", "会不会", "还是" };// 包括是吗
	*/	
	String[] faqQ = {};

	public SearchControl() {
		super();
		if(insertLogPool == null || insertLogPool.isTerminated()){
			insertLogPool =  InsertLogExecutorPool.createInsertLogExecutorPool();
		}
		if(insertLocalLogPool == null || insertLocalLogPool.isTerminated()){
			insertLocalLogPool = InsertLocalLogExecutorPool.createInsertLocalLogExecutorPool();
		}
	}

	/*
	 * private ExecutorService getExcutorService(int nThreads) { //
	 * 相当于定义一个newFixedThreadPool线程池 ExecutorService pool = new
	 * ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new
	 * LinkedBlockingQueue<Runnable>()) { protected void afterExecute(Runnable
	 * r, Throwable t) { super.afterExecute(r, t);// 空方法,这里不做了,异常处理做过了 } };
	 * return pool; }
	 */

	@SuppressWarnings("unchecked")
	private List<XQSearchBean> search(String sentence, ESearchRelation relation)
			throws SearchException {
		AbsBooleanSearch booleanSearch = new BooleanSearch();
		// 指定7个搜索文件的位置
		ISearchFolder iSearchFolder = new ISearchFolder() {
			@Override
			public String[] getSearchFolder() {
				// TODO Auto-generated method stub
				return Configuration.searchIndexDirs;
			}
		};
		// 针对可能包含的分类进行搜索，最后在汇总结果
		List<XQSearchBean> res = null;
		//special case 
		String[] errorCodeList = { "3001", "5001", "6001", "7001", "8001", "9001"};
		int errorIndex = -1;
		for(int i = 0; i < errorCodeList.length; i ++){
			if(sentence.contains(errorCodeList[i])){
				errorIndex = i;
				break;
			}
		}
		if(errorIndex!=-1){
			sentence = sentence + "报错";
		}
		res = (List<XQSearchBean>) super.search(sentence, iSearchFolder, relation, booleanSearch);
		Log.getLogger().info("总匹配条数=" + res.size());
		//special case
		/*
		if(sentence.contains("7001")){
			XQSearchBean tempAns = new XQSearchBean();
			tempAns.setAnswer("您可以点开【错误详情】具体查看。7001是与CA证书有关的报错，主要集中在驱动安装不完整，U棒没有认出来。"
					+ "遇到这种情况，重新插拔U棒，或者重新去网站上下载协卡助手的驱动，重新安装一下，安装的过程中，U棒不要插在电脑上。"
					+ "（1）卸载原ca驱动：开始--程序--控制面板--添加或删除程序，卸载掉包含以下内容的： 证书管理器（或证书助手）；"
					+ "明华（明华USB驱动、mingwah、m&w ekey driver)；华大（华大智能USB驱动、cidc)；握奇（握奇USB驱动、watch)；"
					+ "文鼎创（uniagent）协卡助手；一证通证书升级客户端。"
					+ "（2）CA驱动下载：登录http://62111929.net--下载 协卡助手，下载安装时U棒不能插在电脑上，"
					+ "安装前在控制面板中把原来的驱动卸载。");
			tempAns.setQuestion("7001 报错的错误详情");
			tempAns.setScore((float)(topSimilarity));
			tempAns.setHtmlContent(tempAns.getQuestion()
					+"<br>"
					+tempAns.getAnswer()
					+"");	
			//res = new ArrayList<XQSearchBean>();
			res.add(tempAns);
		}
		else if(sentence.contains("8001")){
			XQSearchBean tempAns = new XQSearchBean();
			tempAns.setAnswer("点击【错误详情】，查看具体错误。8001是与您在网站登记情况相关，主要集中未开户或者序列号变更有关。"
					+ "遇到这种情况的话，请联系您的电子报税服务商，由他们为您在网站端进行开户、修改CA或者查询。");
			tempAns.setQuestion("8001 报错的错误详情");
			tempAns.setScore((float)(topSimilarity));
			tempAns.setHtmlContent(tempAns.getQuestion()
					+"<br>"
					+tempAns.getAnswer()
					+"");	
			//res = new ArrayList<XQSearchBean>();
			res.add(tempAns);
		}
		else if(sentence.contains("4001")){
			res = new ArrayList<XQSearchBean>();
		}
		*/
		//add qa log
		Callable<List<? extends XQSearchBean>> c = new InsertThread(sentence,res);
		insertLogPool.submit(c);
		return res;
	}

	// 判断用户意图1
	/*public boolean judgeContainTag(String sentence, String[] tags) {
		for (int i = 0; i < tags.length; i++) {
			if (sentence.contains(tags[i]))
				return true;
		}
		return false;
	}*/

	// 判断用户意图2
	/*public boolean judgeContainTag2(String sentence) {
		if (judgeContainTag(sentence, howQ))
			return true;
		else if (judgeContainTag(sentence, whyQ))
			return true;
		else if (judgeContainTag(sentence, whenQ))
			return true;
		else if (judgeContainTag(sentence, whereQ))
			return true;
		else if (judgeContainTag(sentence, whatQ))
			return true;
		else if (judgeContainTag(sentence, orQ))
			return true;
		else if (judgeContainTag(sentence, faqQ))
			return true;
		return false;
	}*/

	public String searchBasic(String sentence, ESearchRelation relation)
			throws SearchException {
		List<XQSearchBean> beans = this.search(sentence, relation);
		// 转化为普通HTML文档
		String html = DividingLineServer.simpleSortWithoutLink(beans);
		return html;
	}

	public String searchBasicWithoutLink(String sentence, ESearchRelation relation)
			throws SearchException {
		List<XQSearchBean> beans = this.search(sentence, relation);
		if (beans.size() <= 0) {
			return "语料中无答案";
		}
		if (beans.size() == 1){
			return beans.get(0).getAnswer();
		}
		/*
		 * convert question & answer then put into shenji_qa_predict
		 */
		Fenci fenciUtil = new Fenci();
		final String splitWordTag = "@@";
		final String splitSentenceTag = "$$";
		final String splitIDTag = "SENTENCEID";
		// convert question
		StringBuilder question = new StringBuilder();
		question.append(fenciUtil.iKAnalysisMax(sentence).replace("/",
				splitWordTag));
		// convert answer
		StringBuilder answerList = new StringBuilder();
		for (int i = 0; i < beans.size() && i < 5; i++) {
			if (i > 0)
				answerList.append(splitSentenceTag);
			answerList.append(fenciUtil.iKAnalysisMax(beans.get(i).getAnswer())
					.replace("/", splitWordTag));
			answerList.append(splitIDTag + String.valueOf(i));
		}
		// put into shenji-qa-predict
		System.out.println("a=" + question.toString() + "&b="
				+ answerList.toString());
		String html = HttpUtils.sendGet(
				"http://192.168.0.122:5555/shenjiSearch",
				"a=" + question.toString() + "&b=" + answerList.toString());
		if(html == null || html.equals(HttpUtils.flagError) || html.equals("")){
			return beans.get(0).getAnswer();
		}
		// parse the result from shenji_qa_predict
		String[] sentenceIdList = html.split(splitIDTag);
		if(sentenceIdList.length > 0){
			String ansString = "";
			//return beans.get(Integer.valueOf(sentenceId).intValue()).getAnswer();
			for(int i = 0; i < sentenceIdList.length; i ++){
				if(sentenceIdList[i].equals("") || sentenceIdList[i] == null){
					continue;
				}
				ansString = ansString + "第" + String.valueOf(i+1) + "个答案：" + beans.get(Integer.valueOf(sentenceIdList[i]).intValue()).getAnswer();
				if(i!=sentenceIdList.length - 1)
					ansString += "<br><br>";
			}
			System.out.println(ansString);
			return ansString;
		}
		else{
			return beans.get(0).getAnswer();
		}
	}

	private String aftertreatment(String args,
			List<? extends XQSearchBean> beans, Comparator comparator) {
		MaxAndMyDictSimilarity maxAndMyDictSimilarity = null;
		try {
			maxAndMyDictSimilarity = new MaxAndMyDictSimilarity(args);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return DividingLineServer.cutlineSort(beans);
		}
		// 设置相似度
		try {
			maxAndMyDictSimilarity.setSimilarity(beans);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		// 自定义排序 important
		maxAndMyDictSimilarity.sort(comparator, beans);
		// 添加分割线
		String html = DividingLineServer.cutlineSort(beans);
		return html;
	}
	
	private List<? extends XQSearchBean> aftertreatment_SortAndFilterAndWithoutCutline(String args,
			List<? extends XQSearchBean> beans, Comparator comparator) {
		MaxAndMyDictSimilarity maxAndMyDictSimilarity = null;
		try {
			maxAndMyDictSimilarity = new MaxAndMyDictSimilarity(args);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return new ArrayList();
		}
		// 设置相似度
		try {
			maxAndMyDictSimilarity.setSimilarity(beans);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		//针对与问答库中问题一模一样的用户问题特殊处理
		if(filterOnlyOneQA(beans, args)){
			return beans;
		}
		// 自定义排序 important
		maxAndMyDictSimilarity.sort(comparator, beans);
		System.out.println("[after sort] bean size=" + beans.size());
		filterByTopBetweenLowSimilarity(beans);
		System.out.println("[after filter by similarity] bean size=" + beans.size());
		filterByQATags(beans, args);
		System.out.println("[after filter by QATags] bean size=" + beans.size());
		return beans;
	}
	
	private String aftertreatment_SortAndFilter(String args,
			List<? extends XQSearchBean> beans, Comparator comparator, String sentence) {
		MaxAndMyDictSimilarity maxAndMyDictSimilarity = null;
		try {
			maxAndMyDictSimilarity = new MaxAndMyDictSimilarity(args);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
			return DividingLineServer.cutlineSort(beans);
		}
		// 设置相似度
		try {
			maxAndMyDictSimilarity.setSimilarity(beans);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		//针对与问答库中问题一模一样的用户问题特殊处理
		filterOnlyOneQA(beans, args);
		// 自定义排序 important
		maxAndMyDictSimilarity.sort(comparator, beans);
		System.out.println("[after sort] bean size=" + beans.size());
		filterByTopBetweenLowSimilarity(beans);
		System.out.println("[after filter by similarity] bean size=" + beans.size());
		filterByQATags(beans, args);
		System.out.println("[after filter by QATags] bean size=" + beans.size());
		/*
		//add deep learning
        try{
        	System.err.println("after treatment:" + beans.size());
	        //start shenji qa deep learning
	        if (beans.size() <= 1) {
	            return DividingLineServer.cutlineSort(beans);
	        }
	        
	         // convert question & answer then put into shenji_qa_predict
	         
	        Fenci fenciUtil = new Fenci();
	        final String splitWordTag = "@@";
	        final String splitSentenceTag = "$$";
	        final String splitIDTag = "SENTENCEID";
	        // convert question
	        StringBuilder question = new StringBuilder();
	        question.append(fenciUtil.iKAnalysisMax(sentence).replace("/",
	                splitWordTag));
	        // convert answer
	        StringBuilder answerList = new StringBuilder();
	        for (int i = 0; i < beans.size() && i < 5; i++) {
	            if (i > 0)
	                answerList.append(splitSentenceTag);
	            answerList.append(fenciUtil.iKAnalysisMax(beans.get(i).getAnswer())
	                    .replace("/", splitWordTag));
	            answerList.append(splitIDTag + String.valueOf(i));
	        }
	        // put into shenji-qa-predict
	        System.out.println("add deeplearning" + "a=" + question.toString() + "&b="
	                + answerList.toString());
	        String html = HttpUtils.sendGet(
	                "http://192.168.0.122:5555/shenjiSearch",
	                "a=" + question.toString() + "&b=" + answerList.toString());
	        if(html == null || html.equals(HttpUtils.flagError) || html.equals("")){
	            return DividingLineServer.cutlineSort(beans);
	        }
	        // parse the result from shenji_qa_predict
	        String[] sentenceIdList = html.split(splitIDTag);
	        List<XQSearchBean> ansBeans = new ArrayList<XQSearchBean>();
	        if(sentenceIdList.length > 0){
	            String ansString = "";
	            //return beans.get(Integer.valueOf(sentenceId).intValue()).getAnswer();
	            for(int i = 0; i < sentenceIdList.length; i ++){
	                if(sentenceIdList[i].equals("") || sentenceIdList[i] == null){
	                    continue;
	                }
	                ansBeans.add(beans.get(i));
	                ansString = ansString + "第" + String.valueOf(i+1) + "个答案：" + beans.get(Integer.valueOf(sentenceIdList[i]).intValue()).getAnswer();
	                if(i!=sentenceIdList.length - 1)
	                    ansString += "<br><br>";
	            }
            System.out.println("after treatment:" + ansString);
            //return ansString;
            return DividingLineServer.cutlineSort(beans);
			//end deep learning
	        }
	    }
        catch (Exception ee){
        	System.err.println("after treatment" + ee);
        	return DividingLineServer.cutlineSort(beans);
        }
        */
		// 添加分割线
		return DividingLineServer.cutlineSort(beans);
	}
	
	private boolean filterOnlyOneQA(List<? extends XQSearchBean> beans, String userQuestion){
		userQuestion = com.shenji.robot.util.StringUtil.formatUserQuestion(userQuestion);
		Iterator<? extends XQSearchBean> iterator = beans.iterator();
		boolean onlyOneQAFlag = false;
		while(iterator.hasNext()){
			XQSearchBean bean = iterator.next();
			if(userQuestion.equals(com.shenji.robot.util.StringUtil.formatUserQuestion(bean.getQuestion()))){
				onlyOneQAFlag = true;
				break;
			}
		}
		if(onlyOneQAFlag == true){
			iterator = beans.iterator();
			while(iterator.hasNext()){
				XQSearchBean bean = iterator.next();
				if(userQuestion.equals(com.shenji.robot.util.StringUtil.formatUserQuestion(bean.getQuestion()))){
					continue;
				}
				else{
					iterator.remove();
				}
			}
		}
		return onlyOneQAFlag;
	}

	private void filterByTopBetweenLowSimilarity(List<? extends XQSearchBean> beans){
		Iterator<? extends XQSearchBean> iterator = beans.iterator();
		boolean oneAnswerFlag = false;
		int cnt = 0;
		int print_cnt = 3;
		while(iterator.hasNext()){
			XQSearchBean bean = iterator.next();
			if(cnt < print_cnt){
				System.out.println("["+cnt+"][Similarity="+bean.getSimilarity()+"][Score="+bean.getScore()+"]");
				System.out.println(bean.getQuestion());
				System.out.println(bean.getAnswer());
			}
			cnt += 1;
			if(oneAnswerFlag == true){
				iterator.remove();
				continue;
			}
			if(bean.getSimilarity() >= topSimilarity){
				oneAnswerFlag = true;
				continue;
			}
			if(bean.getSimilarity() <= lowSimilarity){
				iterator.remove();
				continue;
			}
		}
	}
	
	private void filterByQATags(List<? extends XQSearchBean> beans, String userQuestion){
		String extendUserQuestion= extendSynWords(userQuestion);
		System.out.println("filterByQATags extend ("+userQuestion+") tobe ("+extendUserQuestion+")");
		Iterator<? extends XQSearchBean> iterator = beans.iterator();
		int tags2Count = 0;
		int tags1Count = 0;
		//Count
		while(iterator.hasNext()){
			XQSearchBean bean = iterator.next();
			//remove chinese comma & whitespace
			String tag1 = CommaUtil.replaceChineseComma(bean.getTag1());
			String tag2 = CommaUtil.replaceChineseComma(bean.getTag2());
			//确保tag1 tag2不能为null
			if(tag1 != null && tag2 != null){
				//qa with 2 tags
				if(!tag1.equals("") && !tag2.equals("")){
					int existCnt = 0;
					String[] tag1List = tag1.trim().split(",");
					for(String t:tag1List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					String[] tag2List = tag2.trim().split(",");
					for(String t:tag2List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					if(existCnt == 2){
						tags2Count += 1;
					}
				}
				//qa with 1 tags
				else if(!tag1.equals("") || !tag2.equals("")){
					int existCnt = 0;
					String[] tag1List = tag1.trim().split(",");
					for(String t:tag1List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					String[] tag2List = tag2.trim().split(",");
					for(String t:tag2List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					if(existCnt == 1){
						tags1Count += 1;
					}
				}
			}
		}
		//Remove
		iterator = beans.iterator();
		while( iterator.hasNext() && ((tags1Count + tags2Count) > 0) ){
			XQSearchBean bean = iterator.next();
			//remove chinese comma & whitespace
			String tag1 = CommaUtil.replaceChineseComma(bean.getTag1());
			String tag2 = CommaUtil.replaceChineseComma(bean.getTag2());
			//确保tag1 tag2不能为null
			if(tag1 != null && tag2 != null){
				//qa with 2 tags
				if(!tag1.equals("") && !tag2.equals("")){
					int existCnt = 0;
					String[] tag1List = tag1.trim().split(",");
					for(String t:tag1List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					String[] tag2List = tag2.trim().split(",");
					for(String t:tag2List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					if(existCnt == 2){
						continue;
					}
				}
				//qa with 1 tags
				else if(!tag1.equals("") || !tag2.equals("")){
					int existCnt = 0;
					String[] tag1List = tag1.trim().split(",");
					for(String t:tag1List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					String[] tag2List = tag2.trim().split(",");
					for(String t:tag2List){
						if(extendUserQuestion.contains(t) && !t.equals("")){
							existCnt += 1;
							break;
						}
					}
					if(existCnt == 1){
						continue;
					}
				}
			}
			iterator.remove();
		}
	}
	
	/*
	 * 扩展用户问题的同义词
	 * 1. 堆用户问题进行分词
	 * 2. 针对每个词扩展相应的同义词
	 * 3. /进行分隔
	 * */
	private String extendSynWords(String sentence){
		String extendSentenceString = sentence.toLowerCase();
		// 同义词引擎
		SynonymEngine synonymEngine = null;
		try {
			synonymEngine = new CommonSynonymDic();
		} catch (EngineException e) {
			System.err.println("extendSynWords("+sentence+") error...");
			e.printStackTrace();
			return extendSentenceString;
		}
		String words = new FenciControl().iKAnalysis(sentence);
		String[] wordList = words.split("/");
		Set<String> tempSynWordListSet = new HashSet<>();
		tempSynWordListSet.add(extendSentenceString);
		for(String w:wordList){
			try {
				String[] synWordList = synonymEngine.getSynonyms(w.toLowerCase());
				for(String synW:synWordList){
					if (synW == null || synW.length() == 0)
						continue;
					tempSynWordListSet.add(synW);
				}
			} catch (EngineException e) {
				System.err.println("extendSynWords("+sentence+") error...");
				e.printStackTrace();
				if (synonymEngine != null)
					synonymEngine.close();
				return extendSentenceString;
			}
		}
		if (synonymEngine != null)
			synonymEngine.close();
		extendSentenceString = "";
		for(String s:tempSynWordListSet){
			extendSentenceString += "/" + s.toLowerCase();
		}
		return extendSentenceString;
	}
	
	private String pretreatment(String args) {
		String mattchingStr = null;
		// 大小写转换
		args = args.toLowerCase();
		// 模式匹配问句
		if ((mattchingStr = SearchPatternMatching.questionMatching(args)) != null) {
			args = mattchingStr;
		}
		// 搜索数据库
		DBPhraseManager dbManager = new DBPhraseManager();
		String answer = null;
		try {
			answer = dbManager.getAnswer(args);
		} catch (ConnectionPoolException e) {
			// TODO Auto-generated catch block
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		if (answer != null && answer.length() > 0) {
			Log.getLogger().info("mysql中问候语句");
			pretreatmentResult = true;
			return "答:" + answer;
		} else {
			// 非业务问题直接提取答案
			try {
				if ((mattchingStr = SearchNonBusinessMatching.matching(args)) != null) {
					pretreatmentResult = true;
					Log.getLogger().info("用户问题中不存在业务词");
					return "答:" + mattchingStr;
				}
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				// 这里有问题，不应该抛到这层
				Log.getLogger(this.getClass()).error(e.getMessage(), e);
			}
		}
		return args;

	}

	public String searchOrdinary(String sentence, ESearchRelation relation)
			throws SearchException {
		// 预处理
		String str = pretreatment(sentence);
		if (pretreatmentResult)
			return str;
		else {
			List<XQSearchBean> beans = search(sentence, relation);
			Log.getLogger().info("searchOrdinary(html) result size = " + beans.size());
			return aftertreatment_SortAndFilter(sentence, beans,new SimilarityComparator<XQSearchBean>(), sentence);
		}

	}
	
	//not used
	public String searchOrdinaryByxxx0624(String sentence, ESearchRelation relation)
			throws SearchException {
		// 预处理
		String str = pretreatment(sentence);
		if (pretreatmentResult)
			return str;
		else {
			List<XQSearchBean> beans = search(sentence, relation);
			try{
			System.err.println(beans.size());
			//start shenji qa deep learning
			if (beans.size() <= 1) {
				return aftertreatment(sentence, beans,
						new SimilarityComparator<XQSearchBean>());
			}
			/*
			 * convert question & answer then put into shenji_qa_predict
			 */
			Fenci fenciUtil = new Fenci();
			final String splitWordTag = "@@";
			final String splitSentenceTag = "$$";
			final String splitIDTag = "SENTENCEID";
			// convert question
			StringBuilder question = new StringBuilder();
			question.append(fenciUtil.iKAnalysisMax(sentence).replace("/",
					splitWordTag));
			// convert answer
			StringBuilder answerList = new StringBuilder();
			for (int i = 0; i < beans.size() && i < 5; i++) {
				if (i > 0)
					answerList.append(splitSentenceTag);
				answerList.append(fenciUtil.iKAnalysisMax(beans.get(i).getAnswer())
						.replace("/", splitWordTag));
				answerList.append(splitIDTag + String.valueOf(i));
			}
			// put into shenji-qa-predict
			System.out.println("a=" + question.toString() + "&b="
					+ answerList.toString());
			String html = HttpUtils.sendGet(
					"http://192.168.0.122:5555/shenjiSearch",
					"a=" + question.toString() + "&b=" + answerList.toString());
			if(html == null || html.equals(HttpUtils.flagError) || html.equals("")){
				return aftertreatment(sentence, beans,
						new SimilarityComparator<XQSearchBean>());
			}
			// parse the result from shenji_qa_predict
			String[] sentenceIdList = html.split(splitIDTag);
			List<XQSearchBean> ansBeans = new ArrayList<XQSearchBean>();
			if(sentenceIdList.length > 0){
				String ansString = "";
				//return beans.get(Integer.valueOf(sentenceId).intValue()).getAnswer();
				for(int i = 0; i < sentenceIdList.length; i ++){
					if(sentenceIdList[i].equals("") || sentenceIdList[i] == null){
						continue;
					}
					ansBeans.add(beans.get(i));
					ansString = ansString + "第" + String.valueOf(i+1) + "个答案：" + beans.get(Integer.valueOf(sentenceIdList[i]).intValue()).getAnswer();
					if(i!=sentenceIdList.length - 1)
						ansString += "<br><br>";
				}
				System.out.println(ansString);
				//return ansString;
				return aftertreatment(sentence, ansBeans,
						new SimilarityComparator<XQSearchBean>());
			}
			else{
				return aftertreatment(sentence, beans,
						new SimilarityComparator<XQSearchBean>());
			}
			//end deep leanring
			}
			catch (Exception e){
				System.out.println("hahaha");
				return aftertreatment(sentence, beans,
						new SimilarityComparator<XQSearchBean>());
			}
		}

	}

	public String searchFilterByOnto(
			String sentence, 
			ESearchRelation relation,
			IComReasonerServer reasonerServer,
			Comparator<? extends XQSearchBean> comparator)throws SearchException, OntoReasonerException {
		// 预处理
		String str = pretreatment(sentence);
		if (pretreatmentResult)
			return str;
		else {
			List<XQSearchBean> beans = null;
			List<? extends XQSearchBean> exBeans = null;
			int basicSearchNum = 0;
			try {
				beans = search(sentence, relation);
				basicSearchNum = beans.size();
				exBeans = reasonerServer.reasoning(new Object[] { sentence, beans });
				System.err.println(basicSearchNum + ":" + exBeans.size());
				// 这段我加的是因为图谱定位错误，当普通搜索结果大于10个时，防止万一没有结果，这里就走普通搜索好了
				if (exBeans != null && exBeans.size() == 0 && basicSearchNum >= 10)
					return aftertreatment(sentence, beans,
							new SimilarityComparator<XQSearchBean>());
				else
					return aftertreatment(sentence, exBeans, comparator);
			} catch (OntoReasonerException e) {
				// 这段我加的是因为图谱可能不稳定，当普通搜索结果大于20个时，防止万一没有结果，这里就走普通搜索好了
				if (e.getErrorCode().equals(ErrorCode.UserTreeIsNull)
						&& basicSearchNum >= 20) {
					return aftertreatment(sentence, beans,
							new SimilarityComparator<XQSearchBean>());
				} else {
					throw e;
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.getLogger(this.getClass()).error(
						"图谱发生错误，请尽快重启，现在结果为普通搜索结果!", e);
				return aftertreatment(sentence, beans,
						new SimilarityComparator<XQSearchBean>());
			} finally {
				return aftertreatment(sentence, beans,
						new SimilarityComparator<XQSearchBean>());
				/*if (beans != null && beans.size() > 0) {
					beans.clear();
					beans = null;
				}
				if (exBeans != null && exBeans.size() > 0) {
					exBeans.clear();
					exBeans = null;
				}*/
			}
		}
	}

	public ResultShowBean searchBasicNum(String sentence, int number,
			ESearchRelation relation) throws SearchException, SearchException {
		String html = this.searchBasic(sentence, relation);
		return this.convertHtmlToBean(html, number);
	}

	public ResultShowBean searchOrdinaryNum_Log(
			String sentence, 
			int number,
			ESearchRelation relation, 
			int logType) 
			throws SearchException, SearchException {
		String str = pretreatment(sentence);
		ResultShowBean resultShowBean = null;
		if (pretreatmentResult){
			//直接数据库中抽取答案
			IEnumSearch.ResultCode code = ResultCode.Tips;
			List<String> reList = new ArrayList<>();
			reList.add("友情提示:");
			reList.add(str.length() >= 2 ? str.substring(2):str);
			resultShowBean = new ResultShowBean(code, reList);
		}
		else {
			List<XQSearchBean> beans = (List<XQSearchBean>) aftertreatment_SortAndFilterAndWithoutCutline(
					sentence,
					search(sentence, relation),
					new SimilarityComparator<XQSearchBean>()
					);
			Log.getLogger().info("searchOrdinary(no proxy) result size = " + beans.size());
			if(beans.size() <= 0){
				IEnumSearch.ResultCode code = ResultCode.Tips;
				List<String> reList = new ArrayList<>();
				String[] answerList = {
						"您好，我是机器人小琼，您的提问方式有点小问题，请您重新提问才可能能得到新答案哟！",
						"您好，小琼机器人不理解您的问题，请您重新提问题吧~~~谢谢您的合作",
						"亲，小琼机器人没有理解您的意思，请您重新提问题吧~~~",
						"尊敬的客户您好，我是机器人小琼，我没有理解您的意思，请您重新提问吧！"
						};
				reList.add("友情提示：");
				int randomAnswer = ((int) (Math.random() * 10)) % answerList.length;
				reList.add(answerList[randomAnswer]);
				resultShowBean = new ResultShowBean(code, reList);
			}
			else{
				IEnumSearch.ResultCode code = null;
				List<String> reList = new ArrayList<>();
				int count = 0;
				Iterator<XQSearchBean> iterator = beans.iterator();
				while(iterator.hasNext()) {
					XQSearchBean em = iterator.next();
					if (count >= number) {
						break;
					}
					reList.add(em.getQuestion());
					reList.add(em.getAnswer());
					count++;
				}
				if(count <= 1){
					code = ResultCode.Exact;
				}
				else{
					//count >= 2
					code = ResultCode.NunExact;
				}
				resultShowBean = new ResultShowBean(code, reList);
			}
		}
		//log qa to local file
		if(logType ==  LogTypeEnum.Log_local_file.value()){
			Callable<List<? extends ResultShowBean>> c = new InsertLocalLogThread(sentence, resultShowBean);
			insertLocalLogPool.submit(c);
		}
		return resultShowBean;
	}
	
	public ResultShowBean searchOrdinaryNum(String sentence, int number,
			ESearchRelation relation) throws SearchException, SearchException {
		String str = pretreatment(sentence);
		if (pretreatmentResult){
			//直接数据库中抽取答案
			IEnumSearch.ResultCode code = ResultCode.Tips;
			List<String> reList = new ArrayList<>();
			reList.add("友情提示:");
			reList.add(str.length() >= 2 ? str.substring(2):str);
			ResultShowBean resultShowBean = new ResultShowBean(code, reList);
			System.out.println("Type 1:code = " + resultShowBean.getCode().value());
			System.out.println("Type 1:list size = " + resultShowBean.getResult().size());
			System.out.println("Type 1:" + resultShowBean.getResult().get(0));
			System.out.println("Type 1:" + resultShowBean.getResult().get(1));
			return resultShowBean;
		}
		else {
			List<XQSearchBean> beans = (List<XQSearchBean>) aftertreatment_SortAndFilterAndWithoutCutline(
					sentence,
					search(sentence, relation),
					new SimilarityComparator<XQSearchBean>()
					);
			Log.getLogger().info("searchOrdinary(no proxy) result size = " + beans.size());
			if(beans.size() <= 0){
				IEnumSearch.ResultCode code = ResultCode.Tips;
				List<String> reList = new ArrayList<>();
				String[] answerList = {
						"您好，我是机器人小琼，您的提问方式有点小问题，请您重新提问才可能能得到新答案哟！",
						"您好，小琼机器人不理解您的问题，请您重新提问题吧~~~谢谢您的合作",
						"亲，小琼机器人没有理解您的意思，请您重新提问题吧~~~",
						"尊敬的客户您好，我是机器人小琼，我没有理解您的意思，请您重新提问吧！"
						};
				reList.add("友情提示：");
				int randomAnswer = ((int) (Math.random() * 10)) % answerList.length;
				reList.add(answerList[randomAnswer]);
				ResultShowBean resultShowBean = new ResultShowBean(code, reList);
				System.out.println("Type 3:code = " + resultShowBean.getCode().value());
				System.out.println("Type 3:list size = " + resultShowBean.getResult().size());
				System.out.println("Type 3:" + resultShowBean.getResult().get(0));
				System.out.println("Type 3:" + resultShowBean.getResult().get(1));
				return resultShowBean;
			}
			IEnumSearch.ResultCode code = null;
			List<String> reList = new ArrayList<>();
			int count = 0;
			Iterator<XQSearchBean> iterator = beans.iterator();
			while(iterator.hasNext()) {
				XQSearchBean em = iterator.next();
				if (count >= number) {
					code = ResultCode.NunExact;
					break;
				}
				reList.add(em.getQuestion());
				reList.add(em.getAnswer());
				System.out.println("(" + count + ")Type 4 question:" + em.getQuestion());
				System.out.println("(" + count + ")Type 4 answer:" + em.getAnswer());
				count++;
			}
			if(count <= 1){
				code = ResultCode.Exact;
			}
			else{
				//count = 2
				code = ResultCode.NunExact;
			}
			ResultShowBean resultShowBean = new ResultShowBean(code, reList);
			System.out.println("Type 4:code = " + resultShowBean.getCode().value());
			System.out.println("Type 4:list size = " + resultShowBean.getResult().size());
			return resultShowBean;
		}
	}

	
	public ResultShowBean searchFilterByOntoNum(
			String sentence, 
			int number,
			ESearchRelation relation, 
			IComReasonerServer reasonerServer,
			Comparator<? extends XQSearchBean> comparator)throws SearchException, OntoReasonerException, SearchException {
		String html = this.searchFilterByOnto(sentence, relation, reasonerServer, comparator);
		Log.getLogger().info("onto's html = " + html);
		return this.convertHtmlToBean(html, number);
	}

	private ResultShowBean convertHtmlToBean(String html, int number)
			throws SearchException {
		List<String> reList = new ArrayList<String>();
		IEnumSearch.ResultCode code = null;
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		Iterator<Element> iterator = doc.select("a").iterator();
		if (!iterator.hasNext()) {
			// 没有查询结果
			/*
			 * throw new SearchException("No search Result",
			 * SearchException.ErrorCode.NoSearchResult);
			 */
			// add some faq from mysql
			if (judgeUserConversation(html) == 1) {
				code = ResultCode.Tips;
				reList.add("友情提示:");
				reList.add(parseA(html));
				System.out.println("Type 1:" + reList.get(0));
				System.out.println("Type 1:" + reList.get(1));
				ResultShowBean resultShowBean = new ResultShowBean(code, reList);
				return resultShowBean;
			} else if (judgeUserConversation(html) == 2){
				code = ResultCode.Tips;
				reList.add("友情提示:");
				reList.add(parseA(html));
				ResultShowBean resultShowBean = new ResultShowBean(code, reList);
				System.out.println("Type 2:" + reList.get(0));
				System.out.println("Type 2:" + reList.get(1));
				return resultShowBean;
			}
			else {
				String[] answerList = {
						"您好，我是机器人小琼，您的提问方式有点小问题，请您重新提问才可能能得到新答案哟！",
						"您好，小琼机器人不理解您的问题，请您重新提问题吧~~~谢谢您的合作",
						"亲，小琼机器人没有理解您的意思，请您重新提问题吧~~~",
						"尊敬的客户您好，我是机器人小琼，我没有理解您的意思，请您重新提问吧！"
						};
				code = ResultCode.Tips;
				reList.add("友情提示：");
				int randomAnswer = ((int) (Math.random() * 10)) % answerList.length;
				reList.add(answerList[randomAnswer]);
				ResultShowBean resultShowBean = new ResultShowBean(code, reList);
				System.out.println("Type 3:" + reList.get(0));
				System.out.println("Type 3:" + reList.get(1));
				return resultShowBean;
			}
		}
		try {
			int count = 0;
			while (iterator.hasNext()) {
				Element em = (Element) iterator.next();
				if (count >= number) {
					code = ResultCode.NunExact;
					break;
				}
				String url = em.attr("href");
				String[] result = null;
				try {
					result = copeOneHtml(url);
				} catch (IOException e) {
					// TODO: handle exception
					// [NeedToDo]这里不合理
					//add my solution by xxx0624
					result = new String[2];
					result[0] = parseQ(html).equals(html)?"":html;
					result[1] = parseA(html).equals(html)?"":html;
					System.err.println(e.getMessage());
					//e.printStackTrace();
					//continue;
				}
				if (result == null || result.length == 0)
					continue;
				reList.add(result[0]);
				reList.add(result[1]);
				System.out.println("(" + count + ")question:" + result[0]);
				System.out.println("(" + count + ")answer:" + result[1]);
				count++;
			}
			int index = Parameters.maxAccurat - number;
			while (index >= 0) {
				if (iterator.hasNext()) {
					index--;
					iterator.next();
				} else {
					code = ResultCode.Exact;
					break;
				}
			}
		} catch (Exception e) {
			Log.getLogger(this.getClass()).error(e.getMessage(), e);
		}
		ResultShowBean resultShowBean = new ResultShowBean(code, reList);
		return resultShowBean;
	}

	public int judgeUserConversation(String html) {
		if (html.contains("html") && html.contains("div"))
			return 1;
		else if (html.contains("答:"))
			return 2;
		else
			return 3;
	}

	private String[] copeOneHtml(String url) throws IOException {
		org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
		String[] str = new String[2];
		String q = doc.getElementsByClass("q").get(0).html();
		String a = doc.getElementsByClass("a").get(0).html();
		str[0] = q;
		str[1] = a;
		return str;
	}
	
	private String parseQ(String sen){
		int pos1 = sen.indexOf("=\"q\">");
		if(pos1>0){
			int pos2 = sen.indexOf("</div>", pos1);
			if (pos2 > pos1){
				return sen.substring(pos1 + 5, pos2).replace("\\", "/").replace("'", " ").replace("‘", " ");
			}
		}
		return sen;
	}
	
	private String parseA(String sen){
		int pos1 = sen.indexOf("=\"a\">");
		if(pos1>0){
		int pos2 = sen.indexOf("</div>", pos1);
			if (pos2 > pos1){
				return sen.substring(pos1 + 5, pos2).replace("\\", "/").replace("'", " ").replace("‘", " ");
			}
		}
		return sen;
	}

	public static void main(String[] str) throws SearchException {
		System.out.println(new SearchControl().searchOrdinary("网上认证",
				ESearchRelation.OR_SEARCH));
	}

}