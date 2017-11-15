package com.cs5248.team01.model;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

public class HLS {
	final static Logger logger = Logger.getLogger(HLS.class.getSimpleName());
	
	private Video video;
	
	
	
	public HLS(Video video) {
		this.video = video;
	}
	
	private void addLine(StringBuilder b, String text) {
		b.append(text).append("\n");
	}
	public boolean writeHLS(String filePath) {
		try {
			StringBuilder builder = new StringBuilder();
			addLine(builder, "#EXTM3U");
			addLine(builder, "#EXT-X-VERSION:5");
			addLine(builder, "");
			
			addLine(builder, "#EXT-X-STREAM-INF:BANDWIDTH=764000,CODECS=\"mp4a.40.2,avc1.4d401e\",RESOLUTION=426x240");
			String part240 = createStreamFile(this.video.getId(), Segment.SEGMENT_TYPE_HLS_240);
			addLine(builder, "/dash-server/rest/video/" + this.video.getId() + "/HLS/" + Segment.SEGMENT_TYPE_HLS_240);
			String filePath240 = filePath + "-" + Segment.SEGMENT_TYPE_HLS_240;
			
			addLine(builder, "#EXT-X-STREAM-INF:BANDWIDTH=1128000,CODECS=\"mp4a.40.2,avc1.4d401e\",RESOLUTION=640x360");
			String part360 = createStreamFile(this.video.getId(), Segment.SEGMENT_TYPE_HLS_360);
			addLine(builder, "/dash-server/rest/video/" + this.video.getId() + "/HLS/" + Segment.SEGMENT_TYPE_HLS_360);
			String filePath360 = filePath + "-" + Segment.SEGMENT_TYPE_HLS_360;
			
			addLine(builder, "#EXT-X-STREAM-INF:BANDWIDTH=2128000,CODECS=\"mp4a.40.2,avc1.4d401e\",RESOLUTION=854x480");
			String part480 = createStreamFile(this.video.getId(), Segment.SEGMENT_TYPE_HLS_480);
			addLine(builder, "/dash-server/rest/video/" + this.video.getId() + "/HLS/" + Segment.SEGMENT_TYPE_HLS_480);
			String filePath480 = filePath + "-" + Segment.SEGMENT_TYPE_HLS_480;

			File file = new File(filePath);
			FileWriter fw = new FileWriter(file);
			fw.write(builder.toString());
			fw.flush();
			fw.close();
			
			file = new File(filePath240);
			fw = new FileWriter(file);
			fw.write(part240);
			fw.flush();
			fw.close();
			
			file = new File(filePath360);
			fw = new FileWriter(file);
			fw.write(part360);
			fw.flush();
			fw.close();
			
			file = new File(filePath480);
			fw = new FileWriter(file);
			fw.write(part480);
			fw.flush();
			fw.close();
			return true;
			
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return false;
		}
		//write to file
		
	}
	
	public String createStreamFile(int videoId, char segmentType) throws ClassNotFoundException, SQLException {
		List<Segment> segments = Segment.getSegmentByVideoIdSegmentType(videoId, segmentType);
		
		StringBuilder builder = new StringBuilder();
		addLine(builder,  "#EXTM3U");
		addLine(builder, "");
		addLine(builder, "#EXT-X-VERSION:3");
		addLine(builder, "#EXT-X-MEDIA-SEQUENCE:0");
		addLine(builder, "#EXT-X-TARGETDURATION:3");
		addLine(builder, "");
		
		for(Segment s : segments) {
			addLine(builder, "#EXTINF:3");
			addLine(builder, "/dash-server/rest/video/" + videoId + "/" + segmentType + "/" + s.getSequenceNum());
		}
		addLine(builder, "#EXT-X-ENDLIST");
		
		return builder.toString();
		
	}
	
	
	
	
}
