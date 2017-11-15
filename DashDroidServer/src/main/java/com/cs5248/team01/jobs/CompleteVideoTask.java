package com.cs5248.team01.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.cs5248.team01.model.Segment;
import com.cs5248.team01.model.Video;

public class CompleteVideoTask implements Runnable {

	Logger logger = Logger.getLogger(CompleteVideoTask.class.getSimpleName());

	private static final int TIMEOUT_SECOND = 120;
	
	private Video video;
	private int segmentCount;
	public CompleteVideoTask(Video v, int segmentCount) {
		
		this.video = v;
		this.segmentCount = segmentCount;
	}
	@Override
	public void run() {
		try {
			int timewait = 0;
			
			boolean completed = false;
			while(timewait < TIMEOUT_SECOND) {
				if(video.getSegmentCount() >= segmentCount *4) {
					completed = true;
					break;
				}
				else {
					Thread.sleep(5000);
					timewait+=5;
				}
			}
			if(completed) {
				this.video.setFullVideo(true);
				this.video.updateIsFullVideo();
				ThreadExecutor.submitTask(new MPDTask(this.video));
				
				ExecutorService es = null;
				List<Segment> segmentList = Segment.getSegmentByVideoIdSegmentType(this.video.getId(), Segment.SEGMENT_TYPE_ORIGINAL);
				try {
					es = Executors.newFixedThreadPool(3 * segmentList.size() + 1);
					List<Callable<Object>> transcodes = new ArrayList<Callable<Object>>();
					
					for(Segment s : segmentList) {
						for(char c: new char[] {Segment.SEGMENT_TYPE_HLS_240, Segment.SEGMENT_TYPE_HLS_360, Segment.SEGMENT_TYPE_HLS_480}) {
							TranscoderTask task = new TranscoderTask(
									s.getVideoId(),
									s.getFilePath(),
									s.getRepFilePath(c),
									c,
									s.getSequenceNum());
							transcodes.add(Executors.callable(task));
						}
					}
					
					//this line will execute all the task and wait for them to return
					List<Future<Object>> result = es.invokeAll(transcodes);
					
					es.submit(new HLSTask(this.video));
				}
				catch(Exception e) {
					
				}
				finally {
					
				}
			}
			else {
				throw new Exception("video transcoding job error.");
			}
		}
		catch(Exception e) {
			logger.error("Unknown exception, video: " + this.video.getId());
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
		
		
		
	}
}
