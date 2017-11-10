package com.cs5248.team01.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.cs5248.team01.persistent.DBCall;

public class TranscoderTask implements Runnable {

	Logger logger = Logger.getLogger(TranscoderTask.class.getSimpleName());
	private String filePath;
	
	public TranscoderTask(String path) {
		this.filePath = path;
	}
	@Override
	public void run() {
		logger.info("Transcoder task run");
		//String template = "ffmpeg -i {0} -vcodec mpeg2video -acodec mp2 -b:v {1} -b:a {2} -vf scale={3} -f mpegts {4}";
		String file240 = filePath + "_240.ts";
		String file360 = filePath + "_360.ts";
		String file480 = filePath + "_480.ts";
		
		String[] c240 = {"ffmpeg", "-i", filePath, "-vcodec", "mpeg2video", "-acodec", "mp2", "-b:v", "700K", "-b:a", "64K", "-vf", "scale=426:240", "-f", "mpegts", file240, "-loglevel", "quiet"};
		String[] c360 = {"ffmpeg", "-i", filePath, "-vcodec", "mpeg2video", "-acodec", "mp2", "-b:v", "1M", "-b:a", "128k", "-vf", "scale=640:360", "-f", "mpegts", file360, "-loglevel", "quiet"};
		String[] c480 = {"ffmpeg", "-i", filePath, "-vcodec", "mpeg2video", "-acodec", "mp2", "-b:v", "2M", "-b:a", "128k", "-vf", "scale=854:480", "-f", "mpegts", file480, "-loglevel", "quiet"};
		
		//String[] c240 = {"ffmpeg", "-i", filePath, "-vcodec", "mpeg2video", "-acodec", "mp2", "-b:v", "700K", "-b:a", "64K", "-vf", "scale=426:240", "-f", "mpegts", file240};
		//String[] c360 = {"ffmpeg", "-i", filePath, "-vcodec", "mpeg2video", "-acodec", "mp2", "-b:v", "1M", "-b:a", "128k", "-vf", "scale=640:360", "-f", "mpegts", file360};
		//String[] c480 = {"ffmpeg", "-i", filePath, "-vcodec", "mpeg2video", "-acodec", "mp2", "-b:v", "2M", "-b:a", "128k", "-vf", "scale=854:480", "-f", "mpegts", file480};
		
		
		//String c240 = MessageFormat.format(template, filePath, "700K", "64K", "426:240", file240);
		//String c360 = MessageFormat.format(template, filePath, "1M", "128k", "640:360", file360);
		//String c480 = MessageFormat.format(template, filePath, "2M", "128k", "854:480", file480);
		
		transcodeProcess(c240);
		transcodeProcess(c360);
		transcodeProcess(c480);
		
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
			while((line = stdout.readLine())!= null) 
				logger.info(line);
		} catch (IOException e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
		} finally {
			ffmpeg.destroy();
		}
		logger.info("end");
	}
}
