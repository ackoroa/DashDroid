package com.cs5248.team01.rest;
import java.io.File;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.cs5248.team01.model.JsonResponse;
import com.cs5248.team01.model.Segment;
import com.cs5248.team01.model.Video;

@Path("video")
public class VideoResource {
	
	final static Logger logger = Logger.getLogger(VideoResource.class.getSimpleName());
	
	@POST
	@Path("new")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonResponse newVideo(String data) {
		logger.info("new (post) data: " + data);
		try {
			String name = "";
			
			if(data != null && !data.equals("")) {
				JSONObject obj = new JSONObject(data);
				name = obj.getString("name");
			}
			
			return JsonResponse.createResponse(Video.newVideo(name));
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return JsonResponse.failedResponse(e.getMessage());
		}
	}
	
	@GET
	@Path("new")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonResponse newVideo2() {
		logger.info("new (GET)");
		try {
			return JsonResponse.createResponse(Video.newVideo(""));
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return JsonResponse.failedResponse(e.getMessage());
		}
	}
	
	@POST
	@Path("{videoId}/{sequenceNum}/upload")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public JsonResponse uploadSegment(@PathParam("videoId") String videoId, @PathParam("sequenceNum") String sequenceNum, InputStream payload) {
		logger.info("UPLOAD video: " + videoId + " sequenceNum: " + sequenceNum);
		try {
			
			Video video = Video.getById(Integer.parseInt(videoId));
			if(video == null) 
				throw new Exception("video id " + videoId + " not found.");
			
			Segment segment = Segment.findSegment(Integer.parseInt(videoId), Integer.parseInt(sequenceNum), Segment.SEGMENT_TYPE_ORIGINAL);
			if(segment != null) {
				throw new Exception("Segment already exist");
			}
			video.addSegment(payload, Integer.parseInt(sequenceNum));

			return JsonResponse.createResponse(payload.available());
		}
		catch(Exception e) {
			logger.info(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return JsonResponse.failedResponse(e.getMessage());
		}

	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public JsonResponse updateVideo(@PathParam("videoId") String videoId, String data) {
		logger.info("UPDATE video");
		try {
			Video video = Video.getById(Integer.parseInt(videoId));
			if(video == null) 
				throw new Exception("video id " + videoId + " not found.");
			
			if(data != null && !data.equals("")) {
				JSONObject obj = new JSONObject(data);
				if(obj.has("name"))
					video.setName(obj.getString("name"));
				if(obj.has("description"))
					video.setDescription(obj.getString("description"));
			}
			
			video.update();
			return JsonResponse.createResponse(null);
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return JsonResponse.failedResponse(e.getMessage());
		}		
	}
	
//	@GET
//	@Path("{videoId}/{sequenceNum}/status")
//	@Produces("application/json")
//	public Response getStatus(@PathParam("videoId") String videoId, @PathParam("sequenceNum") String sequenceNum) {
//		return null;
//	}
	
	@POST
	@Path("{videoId}/end")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JsonResponse completeVideo(@PathParam("videoId") String videoId, String data) {
		logger.info("end video : " + videoId  + " data: " + data);
		try {
			Video video = Video.getById(Integer.parseInt(videoId));
			if(video == null) 
				throw new Exception("video id " + videoId + " not found.");
			if(video.isFullVideo())
				throw new Exception("video id " + videoId + " has already ended before.");
			
			int total = -1;
			if(data != null && !data.equals("")) {
				try {
					JSONObject obj = new JSONObject(data);
					total = obj.getInt("total");
				}
				catch(JSONException je) {
					logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(je));
					return JsonResponse.failedResponse("unexpected json format");
				}
			}
			else {
				throw new Exception("please provide total number of sequence");
			}
			video.completeUpload(total);
			//video.setFullVideo(true);
			//video.update();
			return JsonResponse.createResponse(null);
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return JsonResponse.failedResponse(e.getMessage());
		}
	}
	
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON) 
	public JsonResponse getList() {
		logger.info("GET LIST");
		try {
			return JsonResponse.createResponse(Video.getAllVideo());
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return JsonResponse.failedResponse(e.getMessage());
		}
	}
	
	@GET
	@Path("{videoId}/MPD")
	@Produces(MediaType.APPLICATION_XML)
	public Response getMPD(@PathParam("videoId") String videoId) {
		logger.info("GET MPD video: " + videoId);
		ResponseBuilder response = Response.status(500, "unknown error");
		try {
			Video video = Video.getById(Integer.parseInt(videoId));
			if(video == null) {
				response = Response.status(404, "video: " + videoId + "not found");
			}
			else {
				String path = video.getMPDPath();
				if(path == null || path.equals("")) {
					response = Response.status(404, "video does not have MPD");
				}
				else {
					logger.debug("MPD Path: " + path);
					File f = new File(path);
					String fileName = f.getName();
					response = Response.ok((Object)f);
					response.header("Content-Disposition", "inline; filename=" + fileName);
				}
			}
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			response = Response.status(500, e.getMessage());
		}
		
		return response.build();
	}
	
	@GET
	@Path("{videoId}/HLS")
	@Produces("application/x-mpegURL")
	public Response getHLS(@PathParam("videoId") String videoId) {
		return getHLSHelper(videoId, "");
	}
	
	@GET
	@Path("{videoId}/HLS/{postfix}")
	@Produces("application/x-mpegurl")
	public Response getHLSChild(@PathParam("videoId") String videoId, @PathParam("postfix") String postfix) {
		return getHLSHelper(videoId, postfix);
	}
	
	private Response getHLSHelper(String videoId, String postfix) {
		logger.info("GET HLS video: " + videoId + " postfix: " + postfix);
		ResponseBuilder response = Response.status(500, "unknown error");
		try {
			Video video = Video.getById(Integer.parseInt(videoId));
			if(video == null) {
				response = Response.status(404, "video: " + videoId + "not found");
			}
			else {
				String path = video.getHLSPath();
				if(path == null || path.equals("")) {
					response = Response.status(404, "video does not have HLS");
				}
				else {
					logger.debug("HLS Path: " + path);
					
					if(postfix != null && !postfix.equals(""))
						path = path + "-" + postfix;
					
					logger.info("path: " + path);
					File f = new File(path);
					String fileName = f.getName() + ".m3u8";
					response = Response.ok((Object)f);
					//response.header("Content-Disposition", "inline; filename=" + fileName);
					response.header("Content-Type", "application/x-mpegurl");
				}
			}
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			response = Response.status(500, e.getMessage());
		}
		
		return response.build();
	}
	
	@GET
	@Path("{videoId}/{representation}/{sequenceNo}")
	@Produces("video/mp4,video/mp2t")
	public Response getVideoSegment(
				@PathParam("videoId") String videoId,
				@PathParam("representation") String representation,
				@PathParam("sequenceNo") String sequenceNo
			) {
		
		ResponseBuilder response = Response.status(500, "unknown error");
		try {
			Segment segment = Segment.findSegment(Integer.parseInt(videoId), Integer.parseInt(sequenceNo),  representation.charAt(0));
			
			if(segment == null) {
				response = Response.status(404, "segment not found for video: " + videoId + " sequence: " + sequenceNo + " rep: " + representation);
			}
			else {
				File f = new File(segment.getFilePath());
				String fileName = f.getName();
				response = Response.ok((Object)f);
				response.header("Content-Disposition", "inline; filename=" + fileName);
				if(fileName.endsWith(".ts")) {
					response.header("Content-type", "video/mp2t");
				}
				else {
					response.header("Content-type", "video/mp4");
				}
				
			}
		} catch (Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			Response.status(500, e.getMessage());
		}
    	return response.build();
	}
}
