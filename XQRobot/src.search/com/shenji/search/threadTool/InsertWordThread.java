package com.shenji.search.threadTool;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.format.datetime.joda.DateTimeParser;

import com.ibm.icu.text.SimpleDateFormat;
import com.shenji.robot.action.DBUserManager;
import com.shenji.search.bean.WordBoostLogBean;
import com.shenji.search.bean.WordScoreLogBean;

public class InsertWordThread implements Callable<List<WordBoostLogBean>> {
	
	private String sentence;
	private List<WordBoostLogBean> boostBeans;
	private List<WordScoreLogBean> scoreBeans;
	
	public InsertWordThread(String sentence, 
			List<WordBoostLogBean> boostBeans,
			List<WordScoreLogBean> scoreBeans) {
		this.sentence = sentence;
		this.boostBeans = boostBeans;
		this.scoreBeans = scoreBeans;
	}

	@Override
	public List<WordBoostLogBean> call() throws Exception {
		DBUserManager dbUserManager = new DBUserManager();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String add_time = sdf.format(new Date());
		if(boostBeans != null){
			for(Iterator<WordBoostLogBean> iterator = boostBeans.iterator(); iterator.hasNext(); ){
				WordBoostLogBean wordBoostLogBean = iterator.next();
				wordBoostLogBean.setUser_question(sentence);
				wordBoostLogBean.setAdd_time(add_time);
				dbUserManager.insertQALogWordBoostLog(wordBoostLogBean);
			}
		}
		if(scoreBeans != null){
			for(Iterator<WordScoreLogBean> iterator = scoreBeans.iterator(); iterator.hasNext();){
				WordScoreLogBean wordScoreLogBean = iterator.next();
				wordScoreLogBean.setUser_question(sentence);
				wordScoreLogBean.setAdd_time(add_time);
				dbUserManager.insertQALogWordScoreLog(wordScoreLogBean);
			}
		}
		return null;
	}
	
}
