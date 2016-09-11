package com.shenji.search.threadTool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class InsertWordLogExecutorPool {
	private static int corePoolSize = 25;
	private static int maxPoolSize = 200;
	private static int queueCapacity = 500;
	
	public static ExecutorService createInsertWordLogExecutorPool(){
		ExecutorService pool = new ThreadPoolExecutor(
				corePoolSize,
				maxPoolSize, 
				0L, 
				TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>()) {
			
		};
		return pool;
	}
}
