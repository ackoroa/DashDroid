package vitdube.com.vidtube;

/**
 * Created by xingjia.zhang on 27/9/17.
 */

public class VideoClip {
    String title;
    String filePath;
    int videoId;
    int chunkId;
    int uploaded;
    int toUpload;

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
    }

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public int getToUpload() { return toUpload; }

    public void setToUpload(int toUpload) { this.toUpload = toUpload; }

    public int getVideoId() { return videoId; }

    public void setVideoId(int videoId) { this.videoId = videoId; }

    @Override
    public String toString() {
        return super.toString();
    }
}
