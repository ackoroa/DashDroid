package com.cs5248.team01.rest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.cs5248.team01.model.SimpleVideoMeta;

@Path("video")
public class VideoResource {
	

	@POST
	@Path("new")
	@Produces(MediaType.APPLICATION_JSON)
	public Response newVideo(String data) {
		return null;
	}
	
	@POST
	@Path("{videoId}/{sequenceNum}/upload")
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadSegment(@PathParam("videoId") String videoId, @PathParam("sequenceNum") String sequenceNum, String data) {
		return null;
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
	public Response completeVideo(@PathParam("videoId") String videoId) {
		return null;
	}
	
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON) 
	public List<SimpleVideoMeta> getList() {
		List<SimpleVideoMeta> resultList = new ArrayList<SimpleVideoMeta>();
		resultList.add(new SimpleVideoMeta("abc", ""));
		resultList.add(new SimpleVideoMeta("def", ""));
		
		return resultList;
	}
	
	@GET
	@Path("{videoId}/MPD")
	@Produces(MediaType.APPLICATION_XML)
	public Response getMPD(@PathParam("videoId") String videoId) {
		return null;
	}
	
	
	@GET
	@Path("{videoId}/{representationId}/{segmentId}")
	@Produces("video/3gp")
	public Response getVideoSegment(
				@PathParam("videoId") String videoId,
				@PathParam("representationId") String representationId,
				@PathParam("segmentId") String segmentId
			) {
		
		
    	File f = new File("D:/myfiles/temp/v.3gp");
    	
    	ResponseBuilder response = Response.ok((Object)f);
    	
    	response.header("Content-Disposition", "attachment; filename=v.3gp");
    	
    	return response.build();
	}
}
