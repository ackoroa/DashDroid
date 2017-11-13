package vitdube.com.vidtube;

/**
 * Created by xingjia.zhang on 13/11/17.
 */

public class VideoInfo {
    String name;
    int id;
    String created;
    String lastModified;
    Boolean hasEnded;
    int segments;
    String mpdPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Boolean getHasEnded() {
        return hasEnded;
    }

    public void setHasEnded(Boolean hasEnded) {
        this.hasEnded = hasEnded;
    }

    public int getSegments() {
        return segments;
    }

    public void setSegments(int segments) {
        this.segments = segments;
    }

    public String getMpdPath() {
        return mpdPath;
    }

    public void setMpdPath(String mpdPath) {
        this.mpdPath = mpdPath;
    }

}
