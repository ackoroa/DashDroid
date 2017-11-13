package com.cs5248.team01.jobs;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.cs5248.team01.model.MPD;
import com.cs5248.team01.model.Video;
import com.cs5248.team01.persistent.FileManager;

public class MPDTask implements Runnable {

	Logger logger = Logger.getLogger(MPDTask.class.getSimpleName());
	private Video video;
	
	public MPDTask(final Video video) {
		this.video = video;
	}
	@Override
	public void run() {
		try {
			MPD mpd = new MPD(this.video);
			
			String filePath = FileManager.getMPDFilePath(this.video.getId(), this.video.getMPDPath());
			mpd.writeXML(filePath);
			
			this.video.setMPDPath(filePath);
			this.video.updateMPDPath();
		} catch (ClassNotFoundException e) {
			logger.error("Unable to save mpd of video: " + this.video.getId());
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} catch (SQLException e) {
			logger.error("Unable to save mpd of video: " + this.video.getId());
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
		catch (Exception e) {
			logger.error("Unknown MPD exception, video: " + this.video.getId());
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
		finally {
			//logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
	}

}
