package com.cs5248.team01.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.cs5248.team01.jobs.ThreadExecutor;
import com.cs5248.team01.jobs.TranscoderTask;
import com.cs5248.team01.persistent.DBCall;
import com.cs5248.team01.persistent.FileManager;
import com.cs5248.team01.rest.VideoResource;

public final class Video {
	
	final static Logger logger = Logger.getLogger(Video.class.getSimpleName());
	
	public static Integer newVideo(String name) throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement(INSERT_STATEMENT)
				.setString(name)
				.setString("F")
				.executeInsert();
	}
	
	public static List<Video> getAllVideo() throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement("SELECT * FROM video")
				.executeQuery(multipleResultMapper);
	}
	
	public static Video getById(int id) throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement("SELECT * FROM video where id = ?")
				.setInt(id)
				.executeQuery(oneResultMapper);
	}
	
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_IS_FULL_VIDEO = "is_full_video";
	private static final String COLUMN_CREATION_DATETIME = "creation_datetime";
	private static final String COLUMN_LAST_MODIFIED_DATETIME = "last_modified_datetime";
	
	private static final DBCall.ResultSetMapper<List<Video>> multipleResultMapper = new DBCall.ResultSetMapper<List<Video>>() {

		@Override
		public List<Video> map(ResultSet rs) throws SQLException, RuntimeException {
			List<Video> result = new ArrayList<Video>();
			while(rs.next()) {
				int id = rs.getInt(COLUMN_ID);
				Video v = new Video(id);
				v.setName(rs.getString(COLUMN_NAME));
				v.setFullVideo(rs.getString(COLUMN_IS_FULL_VIDEO) == FULL_VIDEO_TRUE);
				v.setCreationDateTime(rs.getDate(COLUMN_CREATION_DATETIME));
				v.setLastModifiedDateTime(rs.getDate(COLUMN_LAST_MODIFIED_DATETIME));
				result.add(v);
			}
			return result;
		}
		
	};
	
	private static final DBCall.ResultSetMapper<Video> oneResultMapper = new DBCall.ResultSetMapper<Video>() {

		@Override
		public Video map(ResultSet rs) throws SQLException, RuntimeException {
			rs.next();
			int id = rs.getInt(COLUMN_ID);
			
			Video v = new Video(id);
			v.setName(rs.getString(COLUMN_NAME));
			v.setFullVideo(rs.getString(COLUMN_IS_FULL_VIDEO) == FULL_VIDEO_TRUE);
			v.setCreationDateTime(rs.getDate(COLUMN_CREATION_DATETIME));
			v.setLastModifiedDateTime(rs.getDate(COLUMN_LAST_MODIFIED_DATETIME));
			return v;
		}
		
	};
	
	private static final String FULL_VIDEO_TRUE = "T";
	private static final String FULL_VIDEO_FALSE = "F";
	
	private static final String INSERT_STATEMENT = "INSERT INTO video (name, is_full_video, creation_datetime, last_modified_datetime) values (?, ?, now(), now())";
	private static final String UPDATE_STATEMENT = "UPDATE video SET name = ?, is_full_video = ?, last_modified_datetime = now() where id = ?";
	
	private final int id;
	private String name;
	private boolean isFullVideo;
	private Date creationDateTime;
	private Date lastModifiedDateTime;
	
	public Video(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isFullVideo() {
		return isFullVideo;
	}
	public void setFullVideo(boolean isFullVideo) {
		this.isFullVideo = isFullVideo;
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
	public int getId() {
		return id;
	}
	
	public void addSegment(InputStream data, int sequenceNum) throws IOException, ClassNotFoundException, SQLException {
		String filePath = FileManager.writeVideoFile(data, this.id, sequenceNum);
		Segment.newSegment(this, filePath, sequenceNum);
		logger.info("Running task");
		ThreadExecutor.submitTask(new TranscoderTask(filePath));
		logger.info("task triggered");
		
	}

	
}
