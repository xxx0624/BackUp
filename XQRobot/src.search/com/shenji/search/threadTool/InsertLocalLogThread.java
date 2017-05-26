package com.shenji.search.threadTool;

import java.util.List;
import java.util.concurrent.Callable;

import com.shenji.common.util.*;
import com.shenji.robot.data.ResultShowBean;

public class InsertLocalLogThread implements Callable<List<? extends ResultShowBean>>{
	private String userQuestion;
	private ResultShowBean resultShowBean;
	
	public InsertLocalLogThread(String userQuestion, ResultShowBean resultShowBean){
		this.userQuestion = userQuestion;
		this.resultShowBean = resultShowBean;
	}

	@Override
	public List<? extends ResultShowBean> call() throws Exception {
		//log写入本地
		String logContent = XMLUtil.generateXML(this.userQuestion, this.resultShowBean)+"\r\n";
		String filename = GetTimeUtil.getYMD() + ".xml";
		String filePath = PathUtil.getWebInFAbsolutePath()+"log";
		FileUtil.writeOrExtendFile(filePath, filename, logContent);
		return null;
	}
	
}
