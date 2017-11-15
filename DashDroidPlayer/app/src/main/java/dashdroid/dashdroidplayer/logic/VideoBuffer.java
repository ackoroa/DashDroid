package dashdroid.dashdroidplayer.logic;

import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dashdroid.dashdroidplayer.util.FileUtils;
import dashdroid.dashdroidplayer.util.Properties;

public class VideoBuffer {
    public final static int BUFFER_TOTAL_DURATION = Properties.BUFFER_TOTAL_DURATION;
    private volatile int bufferContentDuration = 0;

    private volatile Queue<File> playQueue = new ConcurrentLinkedQueue<>();
    private volatile Queue<File> delQueue = new ConcurrentLinkedQueue<>();
    private volatile Queue<RepLevel> playLevelQueue = new ConcurrentLinkedQueue<>();

    public boolean isEmpty() {
        return playQueue.isEmpty();
    }

    public int level() {
        return bufferContentDuration;
    }

    public double getFillRatio() {
        return (double) bufferContentDuration / BUFFER_TOTAL_DURATION;
    }

    public int getBufferContentDuration() {
        return bufferContentDuration;
    }

    public Pair<String, RepLevel> poll() {
        File video = playQueue.poll();
        Log.i("trace", "poll from buffer " + video.getPath());
        delQueue.offer(video);
        return Pair.create(video.getPath(), playLevelQueue.poll());
    }

    public void offer(File video, int duration) {
        Log.i("trace", "offer to buffer " + video.getPath());
        playQueue.offer(video);
        updateBufferSize(duration);
    }

    public void offerLevel(RepLevel level) {
        Log.i("trace", "offer level to buffer " + level);
        playLevelQueue.offer(level);
    }

    public void cleanOne(int duration) {
        File videoToDelete = delQueue.poll();
        updateBufferSize(-duration);
        FileUtils.delete(videoToDelete);
    }

    public void cleanAll() {
        bufferContentDuration = 0;
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
        bufferContentDuration += delta;
        Log.i("trace", "Buffer Level: " + String.format("%.2f", (double) bufferContentDuration / BUFFER_TOTAL_DURATION));
        Log.i("perfTest", System.currentTimeMillis() + ",bufferLevel,"
                + String.format("%.2f", (double) bufferContentDuration / BUFFER_TOTAL_DURATION));
    }
}
