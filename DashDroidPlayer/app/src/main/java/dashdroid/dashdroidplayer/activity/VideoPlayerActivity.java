package dashdroid.dashdroidplayer.activity;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

import dashdroid.dashdroidplayer.logic.MPDParser;
import dashdroid.dashdroidplayer.logic.RepLevel;
import dashdroid.dashdroidplayer.logic.RepresentationPicker;
import dashdroid.dashdroidplayer.logic.VideoBuffer;
import dashdroid.dashdroidplayer.model.Representation;
import dashdroid.dashdroidplayer.util.FileUtils;
import dashdroid.dashdroidplayer.R;
import dashdroid.dashdroidplayer.model.MPD;
import dashdroid.dashdroidplayer.util.Properties;

public class VideoPlayerActivity extends AppCompatActivity {
    private RepresentationPicker repPicker;

    volatile boolean running;
    private volatile VideoBuffer buffer = new VideoBuffer();

    String videoId;
    VideoView vidView;
    ProgressBar spinnerView;
    TextView bandwidthMeter;
    TextView nowPlayingLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        running = true;

        vidView = (VideoView) findViewById(R.id.dashPlayer);
        spinnerView = (ProgressBar) findViewById(R.id.spinner);
        spinnerView.setVisibility(View.VISIBLE);
        bandwidthMeter = (TextView) findViewById(R.id.bandwidthMeter);
        nowPlayingLevel = (TextView) findViewById(R.id.nowPlaying);

        videoId = getIntent().getStringExtra("vidId");
        Log.i("trace", "start player for video " + videoId);

        vidView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("trace", "Complete segment playback");
                if (running) {
                    new VideoPlayer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new VideoDeleter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        new MPDDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        buffer.cleanAll();
        running = false;
        super.onDestroy();
    }

    private volatile boolean started = false;
    private volatile int curIdx = 0;
    private volatile double latestBandwidth = 0;

    private volatile MPD mpd;

    private boolean videoFinished() {
        return curIdx >= mpd.getLastSegmentIdx() && mpd.isFinishedVideo();
    }

    private class MPDDownloader extends AsyncTask<Void, Void, MPD> {
        @Override
        protected MPD doInBackground(Void... params) {
            return MPDParser.parse(MPD.getSourceUrl(videoId));
        }

        @Override
        protected void onPostExecute(MPD result) {
            super.onPostExecute(result);
            if (result != null) {
                mpd = result;
            }

            if (!started) {
                repPicker = new RepresentationPicker(
                        mpd.representations,
                        mpd.getSegmentDuration()
                );
                if (!mpd.isFinishedVideo()) {
                    curIdx = mpd.getLastSegmentIdx() - Properties.LIVE_OFFSET;
                }
            }

            if (running) {
                new DashManager().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private class DashManager extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (curIdx < mpd.getLastSegmentIdx()) {
                RepLevel repLevel = repPicker.chooseRepresentation(
                        buffer.getBufferContentDuration(),
                        latestBandwidth
                );
                Representation rep = mpd.representations.get(repLevel.ordinal());
                buffer.offerLevel(repLevel);
                return mpd.getVideoBaseUrl() + rep.getSegmentUrl(curIdx);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String videoToDownload) {
            String bandwidthText = String.format("%.2f", repPicker.getBandwidthEstimate()/1000)  + " kb/s ("
                    + String.format("%.2f", buffer.getFillRatio()) + " buffer)";
            bandwidthMeter.setText(bandwidthText);

            if (running) {
                if (!started) {
                    started = true;
                    new VideoPlayer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                if (videoToDownload != null) {
                    new VideoDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, videoToDownload);
                } else if (!videoFinished()) {
                    try {
                        Thread.sleep((long) (Properties.CHECK_MPD_DELAY * 1000));
                    } catch (InterruptedException e) {
                        Log.e("trace", e.getMessage(), e);
                    }
                    new MPDDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    bandwidthMeter.setText("Finished Buffering");
                }
            }
        }
    }

    private class VideoDownloader extends AsyncTask<String, Void, File> {
        private volatile long downloadStartTime;

        @Override
        protected File doInBackground(String... params) {
            downloadStartTime = System.nanoTime();
            return FileUtils.download(getApplicationContext(), params[0]);
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            buffer.offer(result, mpd.getSegmentDuration());

            double downloadTime = (double) (System.nanoTime() - downloadStartTime) / 1000000000;
            latestBandwidth = result.length() / downloadTime;
            Log.i("perfTest", System.currentTimeMillis() + ",bandwidth," + String.format("%.2f", latestBandwidth / 1000));

            curIdx++;

            if (running) {
                new DashManager().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                result.delete();
            }
        }
    }

    private class VideoPlayer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i("trace", "Start player instance");
            if (buffer.isEmpty()) {
                Log.i("trace", "Player: buffer empty");
                if (videoFinished()) {
                    Log.i("trace", "Player: video finished");
                    nowPlayingLevel.setText("Finished Playback");
                    return;
                } else {
                    Log.i("trace", "Player: showing spinner");
                    spinnerView.setVisibility(View.VISIBLE);

                    Log.i("perfTest", System.currentTimeMillis() + ",interrupt,1");
                    try {
                        Thread.sleep((long) (Properties.PLAYER_BUFFERING_DELAY * 1000));
                    } catch (InterruptedException e) {
                        Log.e("trace", e.getMessage(), e);
                    }

                    if (running) {
                        new VideoPlayer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            } else {
                Pair<String, RepLevel> vidPathLevel = buffer.poll();
                Log.i("trace", "Start playback of " + vidPathLevel.first);
                Log.i("perfTest", System.currentTimeMillis() + ",playLevel," + vidPathLevel.second);

                vidView.setVideoPath(vidPathLevel.first);
                nowPlayingLevel.setText(vidPathLevel.second.toString());
                spinnerView.setVisibility(View.GONE);
                vidView.start();
            }
        }
    }

    private class VideoDeleter extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            buffer.cleanOne(mpd.getSegmentDuration());
            return null;
        }
    }
}
