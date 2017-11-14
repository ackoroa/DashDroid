package com.cs5248.team01.persistent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class FileManager {
	//private static final String rootDirectory = "D:/myfiles/temp";
	
	private static final String rootDirectory = "/dashserver";
	private static int READ_SIZE = 8 * 1024;
	
	public static String getMPDFilePath(int videoId, String previousPath) {
		String directory = rootDirectory + "/" + videoId;
		int numbering = 0;
		if(previousPath != null && !previousPath.equals("")) {
			File f = new File(previousPath);
			String name = f.getName();
			
			String[] splits = name.split("_");
			numbering = Integer.parseInt(splits[1]) + 1;
		}
		
		return directory + "/mpd_" + numbering;
	}
	
	public static String writeVideoFile(InputStream data, int videoId, int sequenceNum) throws IOException {
	    String directory = rootDirectory + "/" + videoId;
	    String filePath = directory + "/" + sequenceNum;

	    File dir = new File(directory);
	    if (! dir.exists()){
	    	dir.mkdirs();
	    }

	    File file = new File(filePath);
	    OutputStream outStream = null;
	    try{
	    	
	    	outStream = new FileOutputStream(file);
	    	byte[] buffer = new byte[READ_SIZE];
	    	int bytesRead;
	    	while((bytesRead = data.read(buffer)) != -1) {
	    		outStream.write(buffer, 0, bytesRead);
	    	}
	    	IOUtils.closeQuietly(outStream);
	    }
	    catch (IOException e){
	        throw e;
	    }
	    finally {
	    	if(outStream != null)
	    		IOUtils.closeQuietly(outStream);
	    }
	    
	    return filePath;
		
	}
}
