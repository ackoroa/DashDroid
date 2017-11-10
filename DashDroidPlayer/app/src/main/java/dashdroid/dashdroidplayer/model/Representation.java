package dashdroid.dashdroidplayer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import dashdroid.dashdroidplayer.util.JsonUtil;

public class Representation {
    @JsonProperty
    public String id;
    @JsonProperty
    public int bandwidth;
    @JsonProperty
    public int frameRate;

    @JsonProperty
    public String baseUrl;
    @JsonProperty
    public String segmentTemplate;
    @JsonProperty
    public int numberOfSegments;
    @JsonProperty
    public int segmentDuration;

    public String getSegmentUrl(int idx) {
        if (idx > numberOfSegments) {
            throw new IllegalArgumentException("Trying to download non-existent video segment");
        }
        return baseUrl + segmentTemplate.replace("$Number$", Integer.toString(idx));
    }

    public String toString() {
        return JsonUtil.toJson(this);
    }
}
