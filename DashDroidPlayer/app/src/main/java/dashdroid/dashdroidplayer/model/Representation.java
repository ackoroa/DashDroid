package dashdroid.dashdroidplayer.model;

public class Representation {
    public String id;
    public int bandwidth;
    public int frameRate;

    public String baseUrl;
    public String segmentTemplate;
    public int numberOfSegments;

    public String getSegmentUrl(int idx) {
        if (idx > numberOfSegments) {
            throw new IllegalArgumentException("Trying to download non-existent video segment");
        }
        return baseUrl + segmentTemplate.replace("$Number$", Integer.toString(idx));
    }
}
