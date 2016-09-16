package com.shenji.search.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;



import com.hp.hpl.jena.sparql.algebra.BeforeAfterVisitor;
import com.shenji.common.exception.ConnectionPoolException;
import com.shenji.common.log.Log;
import com.shenji.common.util.HttpUtils;
import com.shenji.robot.action.DBUserManager;
import com.shenji.robot.data.ResultShowBean;
import com.shenji.robot.exception.OntoReasonerException;
import com.shenji.robot.exception.OntoReasonerException.ErrorCode;
import com.shenji.robot.inter.IComReasonerServer;
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
import com.shenji.search.core.exception.EngineException;
import com.shenji.search.core.exception.SearchException;
import com.shenji.search.core.inter.ISearchFolder;
import com.shenji.search.core.search.AbsBooleanSearch;
import com.shenji.search.core.search.Search;
import com.shenji.search.core.search.SearchThread;
import com.shenji.search.threadTool.InsertLogExecutorPool;
import com.shenji.search.threadTool.InsertThread;
import com.shenji.web.bean.QALogBean;

public class SearchControl extends Search {
	
	private static ExecutorService insertLogPool;
	
	private boolean pretreatmentResult = false;

	// 判断用户的意图，声明7种搜索所可能包含的关键词
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
	String[] faqQ = {};

	public SearchControl() {
		super();
		if(insertLogPool == null || insertLogPool.isTerminated()){
			insertLogPool =  InsertLogExecutorPool.createInsertLogExecutorPool();
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
		ISearchFolder iSearchFolderHowQ = new ISearchFolder() {
			@Override
			public String[] getSearchFolder() {
				// TODO Auto-generated method stub
				return Configuration.searchIndexDirsHowQ;
			}
		};
		ISearchFolder iSearchFolderWhyQ = new ISearchFolder() {
			@Override
			public String[] getSearchFolder() {
				// TODO Auto-generated method stub
				return Configuration.searchIndexDirsWhyQ;
			}
		};
		ISearchFolder iSearchFolderWhereQ = new ISearchFolder() {
			@Override
			public String[] getSearchFolder() {
				// TODO Auto-generated method stub
				return Configuration.searchIndexDirsWhereQ;
			}
		};
		ISearchFolder iSearchFolderWhenQ = new ISearchFolder() {
			@Override
			public String[] getSearchFolder() {
				// TODO Auto-generated method stub
				return Configuration.searchIndexDirsWhenQ;
			}
		};
		ISearchFolder iSearchFolderWhatQ = new ISearchFolder() {
			@Override
			public String[] getSearchFolder() {
				// TODO Auto-generated method stub
				return Configuration.searchIndexDirsWhatQ;
			}
		};
		ISearchFolder iSearchFolderOrQ = new ISearchFolder() {
			@Override
			public String[] getSearchFolder() {
				// TODO Auto-generated method stub
				return Configuration.searchIndexDirsOrQ;
			}
		};
		// 针对可能包含的分类进行搜索，最后在汇总结果
		List<XQSearchBean> res = null;
		int classifyTag = 0;
		if (judgeContainTag(sentence, howQ)) {
			System.out.println("how");
			// resHow = (List<XQSearchBean>) super.search(sentence,
			// iSearchFolderHowQ, relation, booleanSearch);
			if (classifyTag == 0)
				classifyTag = 1;
			else
				classifyTag = -1;
		} else if (judgeContainTag(sentence, whyQ)) {
			System.out.println("why");
			// resWhy = (List<XQSearchBean>) super.search(sentence,
			// iSearchFolderWhyQ, relation, booleanSearch);
			if (classifyTag == 0)
				classifyTag = 2;
			else
				classifyTag = -1;
		} else if (judgeContainTag(sentence, whereQ)) {
			System.out.println("where");
			// resWhere= (List<XQSearchBean>) super.search(sentence,
			// iSearchFolderWhereQ, relation, booleanSearch);
			if (classifyTag == 0)
				classifyTag = 3;
			else
				classifyTag = -1;
		} else if (judgeContainTag(sentence, whenQ)) {
			System.out.println("when");
			// resWhen = (List<XQSearchBean>) super.search(sentence,
			// iSearchFolderWhenQ, relation, booleanSearch);
			if (classifyTag == 0)
				classifyTag = 4;
			else
				classifyTag = -1;
		} else if (judgeContainTag(sentence, whatQ)) {
			System.out.println("what");
			// resWhat = (List<XQSearchBean>) super.search(sentence,
			// iSearchFolderWhatQ, relation, booleanSearch);
			if (classifyTag == 0)
				classifyTag = 5;
			else
				classifyTag = -1;
		} else if (judgeContainTag(sentence, orQ)) {
			System.out.println("or");
			// resOr = (List<XQSearchBean>) super.search(sentence,
			// iSearchFolderOrQ, relation, booleanSearch);
			if (classifyTag == 0)
				classifyTag = 6;
			else
				classifyTag = -1;
		} else {
			System.out.println("faq");
			// resFaq = (List<XQSearchBean>) super.search(sentence,
			// iSearchFolder, relation, booleanSearch);
			if (classifyTag == 0)
				classifyTag = 7;
			else
				classifyTag = -1;
		}
		classifyTag = 7;
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
			sentence = errorCodeList[errorIndex]+"报错";
		}
		if (classifyTag != -1) {
			if (classifyTag == 1) {
				res = (List<XQSearchBean>) super.search(sentence,
						iSearchFolderHowQ, relation, booleanSearch);
			} else if (classifyTag == 2) {
				res = (List<XQSearchBean>) super.search(sentence,
						iSearchFolderWhyQ, relation, booleanSearch);
			} else if (classifyTag == 3) {
				res = (List<XQSearchBean>) super.search(sentence,
						iSearchFolderWhereQ, relation, booleanSearch);
			} else if (classifyTag == 4) {
				res = (List<XQSearchBean>) super.search(sentence,
						iSearchFolderWhenQ, relation, booleanSearch);
			} else if (classifyTag == 5) {
				res = (List<XQSearchBean>) super.search(sentence,
						iSearchFolderWhatQ, relation, booleanSearch);
			} else if (classifyTag == 6) {
				res = (List<XQSearchBean>) super.search(sentence,
						iSearchFolderOrQ, relation, booleanSearch);
			} else {
				res = (List<XQSearchBean>) super.search(sentence,
						iSearchFolder, relation, booleanSearch);
			}
		} else {
			/*
			 * XQSearchBean tempBean = new XQSearchBean();
			 * tempBean.setQuestion("hehe"); tempBean.setAnswer("haha");
			 * tempBean.setSimilarity(1); tempBean.setScore(1);
			 * tempBean.setUri("faq/a.htm"); tempBean.setHtmlContent(
			 * "亲~小琼没有明白您的意思，请适当的加上疑问词重新提问吧~(比如加上：为什么，怎么办呀之类的)"); List
			 * tempBeanList = new ArrayList(); tempBeanList.add(tempBean); res =
			 * (List<XQSearchBean>) tempBeanList;
			 */
			// return (List<XQSearchBean>)tempBeanList;
			res = (List<XQSearchBean>) super.search(sentence, iSearchFolder,
					relation, booleanSearch);
		}
		// modify
		System.out.println("总匹配条数=" + res.size());
		/*
		 * 1.一问一答，则只取出一问一答(自己设定上限)。 2.剔除匹配度太低的问答对(自己设定下限)
		 */
		Iterator<XQSearchBean> it = res.iterator();
		boolean noAnswerFlag = true;
		int cntBean = 0;
		boolean oneAnswerFlag = false;
		double up_score = 1.30;
		double low_score = 0.01;
		int print_cnt = 3;
		while (it.hasNext()) {
			XQSearchBean cur = it.next();
			if(cur.getScore()>low_score){
				noAnswerFlag = false;
			}
			if (cntBean < print_cnt) {
				/*System.out.println("current print No:" + cntBean);
				System.out.println("score:" + cur.getScore());
				System.out.println("uri:" + cur.getUri());
				System.out.println("question:" + cur.getQuestion());
				System.out.println("answer:" + cur.getAnswer());*/
			}
			if (cntBean == 0 && oneAnswerFlag == false
					&& cur.getScore() > up_score) {
				oneAnswerFlag = true;
				continue;
			}
			if (oneAnswerFlag == true || cur.getScore() <= low_score) {
				it.remove();
			}
			cntBean++;
		}
		if(noAnswerFlag==true && res.size()>0){
			XQSearchBean tempAns = new XQSearchBean();
			tempAns.setAnswer("由于新系统启用，新的知识库在完善扩展中，您的问题我们已经记录下来，该问题请关注神计报税公众号中相关解答.");
			tempAns.setQuestion("");
			tempAns.setScore(res.get(0).getScore());
			tempAns.setSimilarity(res.get(0).getSimilarity());
			tempAns.setHtmlContent(res.get(0).getHtmlContent());	
			res = new ArrayList<XQSearchBean>();
			res.add(tempAns);
		}
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
			tempAns.setScore(res.get(0).getScore());
			tempAns.setSimilarity(res.get(0).getSimilarity());
			tempAns.setHtmlContent(res.get(0).getHtmlContent());	
			res = new ArrayList<XQSearchBean>();
			res.add(tempAns);
		}
		else if(sentence.contains("8001")){
			XQSearchBean tempAns = new XQSearchBean();
			tempAns.setAnswer("点击【错误详情】，查看具体错误。8001是与您在网站登记情况相关，主要集中未开户或者序列号变更有关。"
					+ "遇到这种情况的话，请联系您的电子报税服务商，由他们为您在网站端进行开户、修改CA或者查询。");
			tempAns.setQuestion("8001 报错的错误详情");
			tempAns.setScore(res.get(0).getScore());
			tempAns.setSimilarity(res.get(0).getSimilarity());
			tempAns.setHtmlContent(res.get(0).getHtmlContent());	
			res = new ArrayList<XQSearchBean>();
			res.add(tempAns);
		}
		else if(sentence.contains("4001")){
			res = new ArrayList<XQSearchBean>();
		}
		System.out.println("现匹配条数" + res.size());
		//add qa log
		Callable<List<? extends XQSearchBean>> c = new InsertThread(sentence,res);
		insertLogPool.submit(c);
		return res;
	}

	// 判断用户意图1
	public boolean judgeContainTag(String sentence, String[] tags) {
		for (int i = 0; i < tags.length; i++) {
			if (sentence.contains(tags[i]))
				return true;
		}
		return false;
	}

	// 判断用户意图2
	public boolean judgeContainTag2(String sentence) {
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
	}

	public String searchBasic(String sentence, ESearchRelation relation)
			throws SearchException {
		List<XQSearchBean> beans = this.search(sentence, relation);
		// 转化为普通HTML文档
		String html = DividingLineServer.simpleSort(beans);
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
		// 判断用户意图2
		/*
		 * if(judgeContainTag2(args)==false){ XQSearchBean tempBean = new
		 * XQSearchBean(); tempBean.setQuestion("hehe");
		 * tempBean.setAnswer("haha"); tempBean.setSimilarity(1);
		 * tempBean.setScore(1); tempBean.setUri("faq/a.htm");
		 * tempBean.setHtmlContent
		 * ("亲~小琼没有明白您的意思，请适当的加上疑问词重新提问吧~(比如加上：为什么，怎么办呀之类的)"); List tempBeanList
		 * = new ArrayList(); tempBeanList.add(tempBean); beans =
		 * (List<XQSearchBean>) tempBeanList; MaxAndMyDictSimilarity
		 * maxAndMyDictSimilarity = null; try { maxAndMyDictSimilarity = new
		 * MaxAndMyDictSimilarity(args); } catch (EngineException e) { // TODO
		 * Auto-generated catch block
		 * Log.getLogger(this.getClass()).error(e.getMessage(), e); return
		 * DividingLineServer.cutlineSort(beans); } // 设置相似度 try {
		 * maxAndMyDictSimilarity.setSimilarity(beans); } catch (EngineException
		 * e) { // TODO Auto-generated catch block
		 * Log.getLogger(this.getClass()).error(e.getMessage(), e); } // 自定义排序
		 * important maxAndMyDictSimilarity.sort(comparator, beans); String html
		 * = DividingLineServer.cutlineSort(beans); return html; }
		 */
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
	
	private String aftertreatmentBydeepLearning(String args,
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
		// 自定义排序 important
		maxAndMyDictSimilarity.sort(comparator, beans);
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
			pretreatmentResult = true;
			return "答:" + answer;
		} else {
			// 非业务问题直接提取答案
			try {
				if ((mattchingStr = SearchNonBusinessMatching.matching(args)) != null) {
					pretreatmentResult = true;
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
			System.err.println("searchOrdinary" + beans.size());
			return aftertreatmentBydeepLearning(sentence, beans,
					new SimilarityComparator<XQSearchBean>(), sentence);
		}

	}
	
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

	public String searchFilterByOnto(String sentence, ESearchRelation relation,
			IComReasonerServer reasonerServer,
			Comparator<? extends XQSearchBean> comparator)
			throws SearchException, OntoReasonerException {
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
				exBeans = reasonerServer.reasoning(new Object[] { sentence,
						beans });
				System.err.println(basicSearchNum + ":" + exBeans.size());
				// 这段我加的是因为图谱定位错误，当普通搜索结果大于10个时，防止万一没有结果，这里就走普通搜索好了
				if (exBeans != null & exBeans.size() == 0
						&& basicSearchNum >= 10)
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

	public ResultShowBean searchOrdinaryNum(String sentence, int number,
			ESearchRelation relation) throws SearchException, SearchException {
		String html = this.searchOrdinary(sentence, relation);
		/*//add deepleanring
		String html = this.searchOrdinaryByxxx0624(sentence, relation);*/
		// System.out.println(html);
		return this.convertHtmlToBean(html, number);
	}

	public ResultShowBean searchFilterByOntoNum(String sentence, int number,
			ESearchRelation relation, IComReasonerServer reasonerServer,
			Comparator<? extends XQSearchBean> comparator)
			throws SearchException, OntoReasonerException, SearchException {
		String html = this.searchFilterByOnto(sentence, relation,
				reasonerServer, comparator);
		System.out.println("onto's html = " + html);
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
			// xxx0624
			if (judgeUserConversation(html) == false) {
				code = ResultCode.Tips;
				reList.add("友情提示：");
				reList.add(html);
				ResultShowBean resultShowBean = new ResultShowBean(code, reList);
				return resultShowBean;
			} else {
				String[] answerList = {
						"您好，我是机器人小琼，您的提问方式有点小问题，请您重新提问才可能能得到新答案哟！",
						"您好，小琼机器人不理解您的问题，请您重新提问题吧~~~谢谢您的合作",
						"亲，小琼机器人没有理解您的意思，请您重新提问题吧~~~",
						"尊敬的客户您好，我是机器人小琼，我没有理解您的意思，请您重新提问吧！" };
				code = ResultCode.Tips;
				reList.add("友情提示：");
				int randomAnswer = ((int) (Math.random() * 10))
						% answerList.length;
				System.out.println(randomAnswer);
				reList.add(answerList[randomAnswer]);
				ResultShowBean resultShowBean = new ResultShowBean(code, reList);
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
					e.printStackTrace();
					continue;
				}
				if (result == null || result.length == 0)
					continue;
				reList.add(result[0]);
				reList.add(result[1]);
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

	public boolean judgeUserConversation(String html) {
		if (html.contains("html") && html.contains("div"))
			return true;
		else
			return false;
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

	public static void main(String[] str) throws SearchException {
		System.out.println(new SearchControl().searchOrdinary("网上认证",
				ESearchRelation.OR_SEARCH));
	}

}