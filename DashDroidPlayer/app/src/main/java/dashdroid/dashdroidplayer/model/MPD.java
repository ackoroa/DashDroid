package dashdroid.dashdroidplayer.model;

import java.util.ArrayList;
import java.util.List;


public class MPD {
    private static final String BASE_URL = "http://monterosa.d2.comp.nus.edu.sg:32768/dash-server/rest/video/";
    public enum VideoType { STATIC, DYNAMIC }

    public VideoType videoType;
    public int videoLength;
    public List<Representation> representations;

    public static String getSourceUrl(String videoId) {
        return BASE_URL + videoId + "/MPD";
    }

    public int getLastSegmentIdx() {
        return representations.get(0).numberOfSegments;
    }

    public boolean isFinishedVideo() {
        return videoType.equals(VideoType.STATIC);
    }

    public String getVideoBaseUrl() {
        return BASE_URL + videoId + "/";
    }
}
