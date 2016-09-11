package com.shenji.search.threadTool;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.context.annotation.Primary;

import com.shenji.robot.action.DBUserManager;
import com.shenji.search.bean.XQSearchBean;
import com.shenji.search.core.search.ExecutorPool;
import com.shenji.web.bean.QALogBean;

public class InsertLogExecutorPool {
	private static int corePoolSize = 25;
	private static int maxPoolSize = 200;
	private static int queueCapacity = 500;
	
	public static ExecutorService createInsertLogExecutorPool(){
		ExecutorService pool = new ThreadPoolExecutor(corePoolSize,
						maxPoolSize, 0L, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>()) {
					protected void afterExecute(Runnable r, Throwable t) {
						super.afterExecute(r, t);// 空方法,这里不做了,异常处理做过了
					}
				};
		return pool;
	}
	
}
