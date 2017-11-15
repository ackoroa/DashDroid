package com.cs5248.team01.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.cs5248.team01.model.Segment;

public class TranscoderTask implements Runnable {

	Logger logger = Logger.getLogger(TranscoderTask.class.getSimpleName());
	private String filePath;
	private int videoId;
	private String targetPath;
	private char segmentType;
	private int sequenceNumber;

	public TranscoderTask(int videoId, String path, String targetPath, char segmentType, int sequenceNumber) {
		this.filePath = path;
		this.videoId = videoId;
		this.targetPath = targetPath;
		this.segmentType = segmentType;
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public void run() {
		logger.info("Transcoder task run");
		try {
			String[] h240 = { "ffmpeg", "-i", this.filePath, "-vcodec", "libx264", "-profile:v", "main", "-level:v", "3.0", "-acodec", "aac", "-b:v", "700K",
					"-b:a", "64K", "-vf", "scale=426:240", "-f", "mpegts", "-strict", "-2",this.targetPath, "-loglevel", "quiet" };

			String[] h360 = { "ffmpeg", "-i", this.filePath, "-vcodec", "libx264", "-profile:v", "main", "-level:v", "3.0", "-acodec", "aac", "-b:v", "1M",
					"-b:a", "128k", "-vf", "scale=640:360", "-f", "mpegts", "-strict", "-2",this.targetPath, "-loglevel", "quiet" };

			String[] h480 = { "ffmpeg", "-i", this.filePath, "-vcodec", "libx264", "-profile:v", "main", "-level:v", "3.0", "-acodec", "aac", "-b:v", "2M",
					"-b:a", "128k", "-vf", "scale=854:480", "-f", "mpegts", "-strict", "-2", this.targetPath, "-loglevel", "quiet" };
			
			String[] c240 = { "ffmpeg", "-i", this.filePath, "-vcodec", "libx264", "-acodec", "aac", "-b:v", "700K",
					"-b:a", "64K", "-vf", "scale=426:240", "-f", "mp4", "-strict", "-2",this.targetPath, "-loglevel", "quiet" };

			String[] c360 = { "ffmpeg", "-i", this.filePath, "-vcodec", "libx264", "-acodec", "aac", "-b:v", "1M",
					"-b:a", "128k", "-vf", "scale=640:360", "-f", "mp4", "-strict", "-2",this.targetPath, "-loglevel", "quiet" };

			String[] c480 = { "ffmpeg", "-i", this.filePath, "-vcodec", "libx264", "-acodec", "aac", "-b:v", "2M",
					"-b:a", "128k", "-vf", "scale=854:480", "-f", "mp4", "-strict", "-2", this.targetPath, "-loglevel", "quiet" };
			
			switch (segmentType) {
			case Segment.SEGMENT_TYPE_240:
				transcodeProcess(c240);
				break;
			case Segment.SEGMENT_TYPE_360:
				transcodeProcess(c360);
				break;
			case Segment.SEGMENT_TYPE_480:
				transcodeProcess(c480);
				break;
			case Segment.SEGMENT_TYPE_HLS_240:
				transcodeProcess(h240);
				break;
			case Segment.SEGMENT_TYPE_HLS_360:
				transcodeProcess(h360);
				break;
			case Segment.SEGMENT_TYPE_HLS_480:
				transcodeProcess(h480);
				break;
			default:
				throw new Exception("unknown segment type");
			}
		} catch (Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		}
	}

	public void transcodeProcess(String[] command) {
		logger.info("running: " + Arrays.toString(command));
		ProcessBuilder pb = new ProcessBuilder(command);
		Process ffmpeg = null;
		try {
			pb.redirectErrorStream();
			ffmpeg = pb.start();
			ffmpeg.waitFor();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(ffmpeg.getInputStream()));

			String line;
			while ((line = stdout.readLine()) != null)
				logger.info(line);

			Segment.newTranscodedSegment(this.videoId, segmentType, this.targetPath, this.sequenceNumber);
		} catch (IOException e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} catch (InterruptedException e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} catch (ClassNotFoundException e) {
			logger.error("Unable to save transcoded segment video: " + this.videoId + " sequence: "
					+ this.sequenceNumber + "segmentType: " + segmentType);
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} catch (SQLException e) {
			logger.error("Unable to save transcoded segment video: " + this.videoId + " sequence: "
					+ this.sequenceNumber + "segmentType: " + segmentType);
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} finally {
			ffmpeg.destroy();
		}
		logger.info("end running");
	}
}
