package com.shenji.search.threadTool;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.format.datetime.joda.DateTimeParser;

import com.ibm.icu.text.SimpleDateFormat;
import com.shenji.robot.action.DBUserManager;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.web.bean.QALogBean;

public class InsertThread implements Callable<List<? extends XQSearchBean>> {
	private String sentence;
	private List<XQSearchBean> beans;
	
	public InsertThread(String sentence, List<XQSearchBean> beans) {
		this.sentence = sentence;
		this.beans = beans;
	}

	@Override
	public List<? extends XQSearchBean> call() throws Exception {
		DBUserManager dbUserManager = new DBUserManager();
		int sortNum = 0;
		int qaType = 1;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String logIdString = sdf.format(new Date());
		for(Iterator<XQSearchBean> iterator = beans.iterator(); iterator.hasNext(); ){
			XQSearchBean xqSearchBean = iterator.next();
			String robotQuestion = xqSearchBean.getQuestion();
			String robotAnswer = xqSearchBean.getAnswer();
			String score = String.valueOf(xqSearchBean.getScore());
			QALogBean bean = new QALogBean(sentence, robotQuestion, robotAnswer, sortNum, score, qaType, logIdString);
			dbUserManager.insertQA(bean);
			sortNum += 1;
			if(sortNum >= 3){
				break;
			}
		}
		return null;
	}
	
}
