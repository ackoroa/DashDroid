package dashdroid.dashdroidplayer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoListItem {
    @JsonProperty
    public Integer id;

    @JsonProperty
    public String Name;

    @JsonProperty
    public Boolean fullVideo;
}
