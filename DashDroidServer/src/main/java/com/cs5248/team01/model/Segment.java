package com.cs5248.team01.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.cs5248.team01.jobs.SegmentTask;
import com.cs5248.team01.jobs.ThreadExecutor;
import com.cs5248.team01.persistent.DBCall;

public class Segment {
	
	final static Logger logger = Logger.getLogger(Segment.class.getSimpleName());
	
	public static final char SEGMENT_TYPE_ORIGINAL = 'O'; //original
	public static final char SEGMENT_TYPE_240 = 'L';  //low
	public static final char SEGMENT_TYPE_360 = 'M';  //medium
	public static final char SEGMENT_TYPE_480 = 'H';  //high
	public static final char SEGMENT_TYPE_HLS_240 = 'X';
	public static final char SEGMENT_TYPE_HLS_360 = 'Y';
	public static final char SEGMENT_TYPE_HLS_480 = 'Z';
	
	public int id;
	private Video video;
	private int videoId;
	private int sequenceNum;
	private char segmentType;
	private String filePath;
	private Date creationDateTime;
	private Date lastModifiedDateTime;
	
	private static final String INSERT_STATEMENT = "INSERT INTO segment (video_id, file_path, sequence_num, segment_type, creation_datetime, last_modified_datetime) values (?, ?, ?, ?, now(), now())";
	
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_VIDEO_ID = "video_id";
	private static final String COLUMN_FILE_PATH = "file_path";
	private static final String COLUMN_SEQUENCE_NUM = "sequence_num";
	private static final String COLUMN_SEGMENT_TYPE = "segment_type";
	private static final String COLUMN_CREATION_DATETIME = "creation_datetime";
	private static final String COLUMN_LAST_MODIFIED_DATETIME = "last_modified_datetime";
	
	public static synchronized Integer newTranscodedSegment(int videoId, char segmentType, String filePath, int sequenceNum) throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement(INSERT_STATEMENT)
				.setInt(videoId)
				.setString(filePath)
				.setInt(sequenceNum)
				.setChar(segmentType)
				.executeInsert();
	}
	
	public static Integer newOriginalSegment(final Video v, String filePath, final int sequenceNum) throws ClassNotFoundException, SQLException {
		
		logger.info("Creating new original segment");
		int segmentId = new DBCall().createStatement(INSERT_STATEMENT)
				.setInt(v.getId())
				.setString(filePath)
				.setInt(sequenceNum)
				.setChar(SEGMENT_TYPE_ORIGINAL)
				.executeInsert();
		
		logger.debug("original segment: " + segmentId + " for video: " + v.getId() + " created");
		
		Segment result = new Segment(segmentId, v, sequenceNum, filePath);
		ThreadExecutor.submitTask(new SegmentTask(result));
		
		return segmentId;
	}
	
	public static List<Segment> getSegmentByVideoIdSegmentType(int videoId, char segmentType) throws ClassNotFoundException, SQLException {
		logger.info("getting segment list for video: " + videoId + " segmentType: " + segmentType);
		
		return new DBCall().createStatement("SELECT * FROM segment where video_id = ? and segment_type = ? order by sequence_num")
				.setInt(videoId)
				.setChar(segmentType)
				.executeQuery(new DBCall.ResultSetMapper<List<Segment>>() {

					@Override
					public List<Segment> map(ResultSet rs) throws SQLException, RuntimeException {
						List<Segment> results = new ArrayList<Segment>();
						while(rs.next()) {
							int id = rs.getInt(COLUMN_ID);
							Segment segment = new Segment(id);
							segment.setVideoId(rs.getInt(COLUMN_VIDEO_ID));
							segment.setFilePath(rs.getString(COLUMN_FILE_PATH));
							segment.setSequenceNum(rs.getInt(COLUMN_SEQUENCE_NUM));
							segment.setSegmentType(rs.getString(COLUMN_SEGMENT_TYPE).charAt(0));
							segment.setCreationDateTime(rs.getDate(COLUMN_CREATION_DATETIME));
							segment.setLastModifiedDateTime(rs.getDate(COLUMN_LAST_MODIFIED_DATETIME));
							segment.getVideo();
							results.add(segment);
						}
						return results;
					}
					
				});
	}
	
	private static final DBCall.ResultSetMapper<Segment> oneResultMapper = new DBCall.ResultSetMapper<Segment>() {

		@Override
		public Segment map(ResultSet rs) throws SQLException, RuntimeException {
			
			if(!rs.next()) {
				return null;
			}
			int id = rs.getInt(COLUMN_ID);
			
			Segment result = new Segment(id);
			result.setVideoId(rs.getInt(COLUMN_VIDEO_ID));
			result.setFilePath(rs.getString(COLUMN_FILE_PATH));
			result.setSequenceNum(rs.getInt(COLUMN_SEQUENCE_NUM));
			result.setSegmentType(rs.getString(COLUMN_SEGMENT_TYPE).charAt(0));
			result.setCreationDateTime(rs.getDate(COLUMN_CREATION_DATETIME));
			result.setLastModifiedDateTime(rs.getDate(COLUMN_LAST_MODIFIED_DATETIME));
			return result;
		}
		
	};
	
	public Segment(int id) {
		this.id = id;
	}
	public Segment(int id, final Video video, int sequenceNum, String filePath) {
		this.id = id;
		this.video = video;
		this.videoId = video.getId();
		this.sequenceNum = sequenceNum;
		this.filePath = filePath;
		this.segmentType = SEGMENT_TYPE_ORIGINAL;
	}
	
	public String getRepFilePath(char targetSegmentType) {
		switch(targetSegmentType) {
			case SEGMENT_TYPE_240:
				return filePath + "_240.mp4";
			case SEGMENT_TYPE_360:
				return filePath + "_360.mp4";
			case SEGMENT_TYPE_480:
				return filePath + "_480.mp4";
			case SEGMENT_TYPE_HLS_240:
				return filePath + "_hls_240.ts";
			case SEGMENT_TYPE_HLS_360:
				return filePath + "_hls_360.ts";
			case SEGMENT_TYPE_HLS_480:
				return filePath + "_hls_480.ts";
			default:
				return "";
		}
	}

	public int getId() {
		return id;
	}

	public Video getVideo() {
		if(this.video == null) {
			try {
				this.video = Video.getById(this.videoId);
			}
			catch(Exception e) {
				logger.error("unable to get video: " + videoId + "from db");
				logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			}
		}
		return video;
	}

	public void setVideo(Video video) {
		this.video = video;
		this.videoId = video.getId();
	}
	
	public void setVideoId(int videoId) {
		this.videoId = videoId;
	}
	public int getVideoId() {
		return this.videoId;
	}

	public int getSequenceNum() {
		return sequenceNum;
	}

	public void setSequenceNum(int sequenceNum) {
		this.sequenceNum = sequenceNum;
	}

	public char getSegmentType() {
		return segmentType;
	}

	public void setSegmentType(char segmentType) {
		this.segmentType = segmentType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Date getCreationDateTime() {
		return creationDateTime;
	}

	public void setCreationDateTime(Date creationDateTime) {
		this.creationDateTime = creationDateTime;
	}

	public Date getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}

	public void setLastModifiedDateTime(Date lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	
	public static Segment findSegment(int videoId, int sequenceNum, char segmentType) throws ClassNotFoundException, SQLException {
		Segment result = new DBCall().createStatement("SELECT * from segment where video_id = ? and segment_type = ? and sequence_num = ?")
					.setInt(videoId)
					.setString(String.valueOf(segmentType))
					.setInt(sequenceNum)
					.executeQuery(oneResultMapper);
		return result;
	}
		
}
