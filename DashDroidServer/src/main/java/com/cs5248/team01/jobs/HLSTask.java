package com.cs5248.team01.jobs;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.cs5248.team01.model.HLS;
import com.cs5248.team01.model.Video;
import com.cs5248.team01.persistent.FileManager;

public class HLSTask implements Runnable {

	Logger logger = Logger.getLogger(HLSTask.class.getSimpleName());

	private Video video;
	
	public HLSTask(Video video) {
		this.video = video;
		
	}
	
	@Override
	public void run() {

		try {
			HLS hls = new HLS(this.video);
			
			String filePath = FileManager.getHLSFilePath(this.video.getId(), this.video.getHLSPathDB());
			if(hls.writeHLS(filePath)) {
				this.video.setHLSPath(filePath);
				this.video.updateHLSPath();
			}
			else {
				throw new Exception("Unable to write MPD");
			}
		} catch (ClassNotFoundException e) {
			logger.error("Unable to save hls of video: " + this.video.getId());
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} catch (SQLException e) {
			logger.error("Unable to save hls of video: " + this.video.getId());
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
		catch (Exception e) {
			logger.error("Unknown hls exception, video: " + this.video.getId());
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
		finally {
			//logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
		
	}
}
