package com.cs5248.team01.jobs;

import org.apache.log4j.Logger;

import com.cs5248.team01.model.Video;
import com.cs5248.team01.persistent.DBCall;

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
				
				//submit for hls transcoding and generation of playlist here
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
