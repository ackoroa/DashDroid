package com.cs5248.team01.model;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MPD {
	
	final static Logger logger = Logger.getLogger(MPD.class.getSimpleName());
	public static final String MPD_TYPE_STATIC = "static";
	public static final String MPD_TYPE_DYNAMIC = "dynamic";
	private static int duration = 3;
	//public static final String 
	
	private Video video;
	
	public MPD(Video v) {
		this.video = v;
	}
	
	public boolean writeXML(String filePath) {
		try
		{
		  DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		  
		  int numberOfSegments = video.getNumberOfSegments();

		  //root elements
		  Document doc = docBuilder.newDocument();
		  
		  Element rootMPD = this.createMPDElement(doc, numberOfSegments * duration, video.isFullVideo() ? MPD_TYPE_STATIC : MPD_TYPE_DYNAMIC);
		  
		  Element period = this.createPeriodElement(doc, this.video.getId());
		  
		  Element adaptationSet = this.createAdaptationSet(doc);
		  
		  
		  
		  Element representation240 = this.createRepresentation(doc, String.valueOf(Segment.SEGMENT_TYPE_240), 764000, 60, numberOfSegments, duration);
		  Element representation360 = this.createRepresentation(doc, String.valueOf(Segment.SEGMENT_TYPE_360), 1128000, 60, numberOfSegments, duration);
		  Element representation480 = this.createRepresentation(doc, String.valueOf(Segment.SEGMENT_TYPE_480), 2128000, 60, numberOfSegments, duration);
		  
		  adaptationSet.appendChild(representation240);
		  adaptationSet.appendChild(representation360);
		  adaptationSet.appendChild(representation480);
		  
		  period.appendChild(adaptationSet);
		  rootMPD.appendChild(period);
		  doc.appendChild(rootMPD);
		  
		  TransformerFactory transformerFactory = TransformerFactory.newInstance();
		  Transformer transformer = transformerFactory.newTransformer();
		  DOMSource source = new DOMSource(doc);

		  StreamResult result =  new StreamResult(new File(filePath));
		  transformer.transform(source, result);
		  return true;

		}catch(ParserConfigurationException e){
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return false;
		}catch(TransformerException e){
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return false;
		}
		catch(Exception e) {
			logger.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			return false;
		}
	}
	
	private Element createMPDElement(Document doc, int duration, String type) {
		Element result = doc.createElement("MPD");
		result.setAttribute("minBufferTime", "PT1.5s");
		result.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		result.setAttribute("xsi:schemaLocation", "urn:mpeg:dash:schema:mpd:2011 http://standards.iso.org/ittf/PubliclyAvailableStandards/MPEG-DASH_schema_files/DASH-MPD.xsd");
		result.setAttribute("xmlns", "urn:mpeg:dash:schema:mpd:2011");
		result.setAttribute("mediaPresentationDuration", "PT" + duration + "S");
		result.setAttribute("profiles", "urn:mpeg:dash:profile:isoff-main:2011");
		result.setAttribute("type", type);
		
		return result;
	}
	
	private Element createPeriodElement(Document doc, int videoId) {
		Element result = doc.createElement("Period");
		result.setAttribute("start", "PT0s");
		result.setAttribute("id", String.valueOf(videoId));
		
		Element baseUrl = doc.createElement("BaseURL");
		baseUrl.setTextContent(String.valueOf(videoId) + "/");
		result.appendChild(baseUrl);
		
		return result;
	}
	
	private Element createAdaptationSet(Document doc) {
		Element adaptationSet = doc.createElement("AdaptationSet");
		adaptationSet.setAttribute("mimeType", "video/mp4");
		return adaptationSet;
	}
	
	private Element createRepresentation(Document doc, String representationId, int bandwidth, int frameRate, int numberOfSegments, int segmentDuration) {
		Element representation = doc.createElement("Representation");
		representation.setAttribute("id", representationId);
		representation.setAttribute("bandwidth", String.valueOf(bandwidth));
		representation.setAttribute("frameRate", String.valueOf(frameRate));
		
		Element baseUrl = doc.createElement("BaseURL");
		baseUrl.setTextContent(representationId + "/");
		representation.appendChild(baseUrl);
		
		Element segmentTemplate = doc.createElement("SegmentTemplate");
		segmentTemplate.setAttribute("media", "$Number$");
		segmentTemplate.setAttribute("startNumber", "0");
		segmentTemplate.setAttribute("timescale", "1");
		
		Element segmentTimeline = doc.createElement("SegmentTimeline");
		Element s = doc.createElement("S");
		s.setAttribute("t", "0");
		s.setAttribute("r", String.valueOf(numberOfSegments));
		s.setAttribute("d", String.valueOf(segmentDuration));
		
		segmentTimeline.appendChild(s);
		segmentTemplate.appendChild(segmentTimeline);
		representation.appendChild(segmentTemplate);
		
		return representation;
		
	}
	
	
}
