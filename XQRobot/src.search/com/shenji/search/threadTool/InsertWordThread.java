package com.shenji.search.threadTool;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.format.datetime.joda.DateTimeParser;

import com.ibm.icu.text.SimpleDateFormat;
import com.shenji.robot.action.DBUserManager;
import com.shenji.search.bean.WordLogBean;

public class InsertWordThread implements Callable<List<WordLogBean>> {
	
	private String sentence;
	private List<WordLogBean> beans;
	
	public InsertWordThread(String sentence, List<WordLogBean> beans) {
		this.sentence = sentence;
		this.beans = beans;
	}

	@Override
	public List<WordLogBean> call() throws Exception {
		DBUserManager dbUserManager = new DBUserManager();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String add_time = sdf.format(new Date());
		for(Iterator<WordLogBean> iterator = beans.iterator(); iterator.hasNext(); ){
			WordLogBean wordLogBean = iterator.next();
			wordLogBean.setUser_question(sentence);
			wordLogBean.setAdd_time(add_time);
			dbUserManager.insertQALogWordScore(wordLogBean);
		}
		return null;
	}
	
}
