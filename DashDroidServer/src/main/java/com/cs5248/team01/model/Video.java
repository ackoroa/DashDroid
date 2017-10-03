package com.cs5248.team01.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cs5248.team01.persistent.DBCall;

public final class Video {
	
	public static Integer newVideo(String name) throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement(INSERT_STATEMENT)
				.setString(name)
				.setString("F")
				.executeInsert();
	}
	
	public static List<Video> getAllVideo() throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement("SELECT * FROM Video")
				.executeQuery(multipleResultMapper);
	}
	
	public static Video getById(int id) throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement("SELECT * FROM Video")
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
			// TODO Auto-generated method stub
			return null;
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
	


	
}
