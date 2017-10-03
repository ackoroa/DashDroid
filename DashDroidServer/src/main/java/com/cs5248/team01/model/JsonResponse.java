package com.cs5248.team01.model;

import java.io.Serializable;

public class JsonResponse {
	private Object data;
	private String message;
	private boolean success;
	
	public static JsonResponse failedResponse(String message) {
		JsonResponse result = new JsonResponse();
		
		result.message = message;
		result.success = false;
		
		return result;
		
	}
	
	public static JsonResponse createResponse(Object data) {
		return createResponse(data, "");
	}
	
	public static JsonResponse createResponse(Object data, String message) {
		JsonResponse result = new JsonResponse();
		
		result.message = message;
		result.data = data;
		result.success = true;
		
		return result;
	}
	
	public Object getData() {
		return this.data;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public boolean isSuccess() {
		return this.success;
	}
}
