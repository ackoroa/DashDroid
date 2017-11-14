package com.cs5248.team01.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.cs5248.team01.model.Segment;

public class SegmentTask implements Runnable {
	
	Logger logger = Logger.getLogger(SegmentTask.class.getSimpleName());
	
	List<TranscoderTask> transcoderTask;
	
	private Segment segment;
	public SegmentTask(Segment segment) {
		this.segment = segment;
	} 



	@Override
	public void run() {
		
		ExecutorService es  = null;
		
		try {
			es = Executors.newFixedThreadPool(4);
			List<Callable<Object>> transcodes = new ArrayList<Callable<Object>>();
		
			for(char c : new char[] {Segment.SEGMENT_TYPE_240, Segment.SEGMENT_TYPE_360, Segment.SEGMENT_TYPE_480}) {
				TranscoderTask task = new TranscoderTask(
						segment.getVideo().getId(), 
						segment.getFilePath(), 
						segment.getRepFilePath(c), 
						c, 
						segment.getSequenceNum());
				transcodes.add(Executors.callable(task));
			}
			
			List<Future<Object>> result = es.invokeAll(transcodes);
			
			es.submit(new MPDTask(segment.getVideo()));
			
		}
		catch(Exception e) {
			logger.error("Exception in segment task");
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
		finally {
			if(es != null) {
				try {
					es.shutdown();
				}
				catch(Exception e2) {
					logger.error("Exception in segment task shutting down executor service");
					logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e2));
				}
			}
		}
		
		
	}
}
