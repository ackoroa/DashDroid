package com.cs5248.team01.jobs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecutor {
	
	private static ExecutorService executorService;
	
	public static void init() {
		executorService = Executors.newFixedThreadPool(10);
	}
	
	public static void shutdown() {
		executorService.shutdown();
	}
	
	public static void submitTask(Runnable r) {
		executorService.submit(r);
	}
	
}	
