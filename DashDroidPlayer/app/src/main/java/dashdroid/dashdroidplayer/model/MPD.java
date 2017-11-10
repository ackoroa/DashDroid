package dashdroid.dashdroidplayer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import dashdroid.dashdroidplayer.util.JsonUtil;


public class MPD {
    private static final String BASE_URL = "http://monterosa.d2.comp.nus.edu.sg:32768/dash-server/rest/video/";
    public enum VideoType { STATIC, DYNAMIC }

    @JsonProperty
    public String videoBaseUrl;

    @JsonProperty
    public VideoType videoType;

    @JsonProperty
    public int videoLength;

    @JsonProperty
    public List<Representation> representations;

    public static String getSourceUrl(String videoId) {
        return "https://raw.githubusercontent.com/ackoroa/DashDroid/master/DashDroidPlayer/app/src/test/resources/sample.mpd" +
                "?token=ACZVKdKVAApM9iMpsbOLo9gMbGvm5ac7ks5aC-q_wA%3D%3D";
        //return BASE_URL + videoId + "/MPD";
    }

    @JsonProperty
    public int getLastSegmentIdx() {
        return representations.get(0).numberOfSegments;
    }

    @JsonProperty
    public int getSegmentDuration() {
        return representations.get(0).segmentDuration;
    }

    @JsonProperty
    public boolean isFinishedVideo() {
        return videoType.equals(VideoType.STATIC);
    }

    @JsonIgnore
    public String getVideoBaseUrl() {
        return BASE_URL + videoBaseUrl;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
