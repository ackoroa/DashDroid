package dashdroid.dashdroidplayer.logic;

import android.util.Log;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dashdroid.dashdroidplayer.util.FileUtils;

public class VideoBuffer {
    public final static long BUFFER_TOTAL_SIZE = 100 * 1024 * 1024;
    private volatile long bufferContentSize = 0;

    private volatile Queue<File> playQueue = new ConcurrentLinkedQueue<>();
    private volatile Queue<File> delQueue = new ConcurrentLinkedQueue<>();

    public boolean isEmpty() {
        return playQueue.isEmpty();
    }

    public double fillRatio() {
        return (double) bufferContentSize / BUFFER_TOTAL_SIZE;
    }

    public boolean filledTo(double fillRatio) {
        return fillRatio() > fillRatio;
    }

    public long getBufferContentSize() {
        return bufferContentSize;
    }

    public String poll() {
        File video = playQueue.poll();
        delQueue.offer(video);
        return video.getPath();
    }

    public void offer(File video) {
        playQueue.offer(video);
        updateBufferSize(video.length());
    }

    public void cleanOne() {
        File videoToDelete = delQueue.poll();
        updateBufferSize(-videoToDelete.length());
        FileUtils.delete(videoToDelete);
    }

    public void cleanAll() {
        bufferContentSize = 0;
        for (File videoToDelete : delQueue) {
            FileUtils.delete(videoToDelete);
        }
        delQueue.clear();
        for (File videoToDelete : playQueue) {
            FileUtils.delete(videoToDelete);
        }
        playQueue.clear();
    }

    private synchronized void updateBufferSize(long delta) {
        bufferContentSize += delta;
    }
}
