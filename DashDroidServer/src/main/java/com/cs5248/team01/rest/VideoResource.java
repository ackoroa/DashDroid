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

import com.cs5248.team01.model.JsonResponse;
import com.cs5248.team01.model.Video;

@Path("video")
public class VideoResource {
	
	final static Logger logger = Logger.getLogger(VideoResource.class.getSimpleName());
	
	@GET
	@Path("new")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonResponse newVideo(String data) {
		try {
			return JsonResponse.createResponse(Video.newVideo(""));
		}
		catch(Exception e) {
			return JsonResponse.failedResponse(e.getMessage());
		}
	}
	
	@POST
	@Path("{videoId}/{sequenceNum}/upload")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public JsonResponse uploadSegment(@PathParam("videoId") String videoId, @PathParam("sequenceNum") String sequenceNum, InputStream payload) {
		try {
			Video video = Video.getById(Integer.parseInt(videoId));
			//video.addVideoSegment(Integer.parseInt(sequenceNum), payload);
				    //for testing
			return JsonResponse.createResponse(payload.available());
		}
		catch(Exception e) {
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
	public JsonResponse completeVideo(@PathParam("videoId") String videoId) {
		try {
			Video video = Video.getById(Integer.parseInt(videoId));
			//video.allUploaded()
			
			return JsonResponse.createResponse(null);
		}
		catch(Exception e) {
			return JsonResponse.failedResponse(e.getMessage());
		}
	}
	
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON) 
	public JsonResponse getList() {
		try {
			return JsonResponse.createResponse(Video.getAllVideo());
		}
		catch(Exception e) {
			return JsonResponse.failedResponse(e.getMessage());
		}
	}
	
	@GET
	@Path("{videoId}/MPD")
	@Produces(MediaType.APPLICATION_XML)
	public Response getMPD(@PathParam("videoId") String videoId) {
		return null;
	}
	
	
	@GET
	@Path("{videoId}/{representationId}/{segmentId}")
	@Produces("video/mp4")
	public Response getVideoSegment(
				@PathParam("videoId") String videoId,
				@PathParam("representationId") String representationId,
				@PathParam("segmentId") String segmentId
			) {
		
		
    	File f = new File("D:/myfiles/temp/minion.mp4");
    	
    	ResponseBuilder response = Response.ok((Object)f);
    	
    	response.header("Content-Disposition", "inline; filename=minion.mp4");
    	
    	return response.build();
	}
}
