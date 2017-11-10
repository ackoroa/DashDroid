package com.cs5248.team01.model;

import java.sql.SQLException;
import java.util.Date;

import com.cs5248.team01.persistent.DBCall;

public class Segment {
	
	public static final char SEGMENT_TYPE_ORIGINAL = 'O'; //original
	public static final char SEGMENT_TYPE_240 = 'L';  //low
	public static final char SEGMENT_TYPE_360 = 'M';  //medium
	public static final char SEGMENT_TYPE_480 = 'H';  //high
	
	private Video video;
	private int sequenceNum;
	private char sequenceType;
	private String filePath;
	private Date creationDateTime;
	private Date lastModifiedDateTime;
	
	private static final String INSERT_STATEMENT = "INSERT INTO SEGMENT (video_id, file_path, sequence_num, segment_type, creation_datetime, last_modified_datetime) values (?, ?, ?, ?, now(), now())";
	
	public static Integer newSegment(Video v, String filePath, int sequenceNum) throws ClassNotFoundException, SQLException {
		return new DBCall().createStatement(INSERT_STATEMENT)
				.setInt(v.getId())
				.setString(filePath)
				.setInt(sequenceNum)
				.setChar(SEGMENT_TYPE_ORIGINAL)
				.executeInsert();
	}
}
