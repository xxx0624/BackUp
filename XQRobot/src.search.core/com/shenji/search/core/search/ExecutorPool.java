package com.shenji.search.core.search;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorPool {
	private static int corePoolSize = 20;
	private static int maxPoolSize = 1000;

	public static ExecutorService createExcutorService() {
		// 相当于定义一个newFixedThreadPool线程池
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
