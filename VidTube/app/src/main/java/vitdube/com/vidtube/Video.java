package vitdube.com.vidtube;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xingjia.zhang on 29/10/17.
 */

public class Video implements Comparable<Video> {

    private String name;
    private List<VideoClip> clips;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VideoClip> getClips() {
        return clips;
    }

    public void setClips(List<VideoClip> clips) {
        this.clips = clips;
    }

    public void addToClips(VideoClip clip) {
        if (this.clips == null) {
            this.clips = new ArrayList<>();
        }
        clips.add(clip);
    }

    public boolean hasBeenUploaded() {
        for (VideoClip clip : this.getClips()) {
            if (clip.getUploaded() != 1) {
                return false;
            }
        }
        return true;
    }

    public VideoClip getClipFromFilePath(String filePath) {
        for (VideoClip clip: this.getClips()) {
            if (clip.getFilePath().equalsIgnoreCase(filePath)) {
                return clip;
            }
        }
        return null;
    }

    public int compareTo(Video o)
    {
        return(name.compareTo(o.name));
    }

}
