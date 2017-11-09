package dashdroid.dashdroidplayer.activity;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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

public class VideoPlayerActivity extends AppCompatActivity {
    private RepresentationPicker repPicker;

    volatile boolean running;
    private VideoBuffer buffer = new VideoBuffer();

    String videoId;
    VideoView vidView;
    ProgressBar spinnerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        running = true;

        vidView = (VideoView)findViewById(R.id.dashPlayer);
        spinnerView = (ProgressBar) findViewById(R.id.spinner);
        spinnerView.setVisibility(View.VISIBLE);

        videoId = getIntent().getStringExtra("vidId");
        Log.i("trace", "start player for video " + videoId);

        vidView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (running) {
                    new VideoPlayer().execute();
                    new VideoDeleter().execute();
                }
            }
        });

        new MPDDownloader().execute();
    }

    @Override
    public void onBackPressed() {
        buffer.cleanAll();
        running = false;

        super.onBackPressed();
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
            }

            if (running) {
                new DashManager().execute();
            }
        }
    }

    private class DashManager extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (curIdx <= mpd.getLastSegmentIdx()) {
                RepLevel repLevel = repPicker.chooseRepresentation(
                        buffer.getBufferContentDuration(),
                        latestBandwidth
                );
                Representation rep = mpd.representations.get(repLevel.ordinal());
                return mpd.getVideoBaseUrl() + rep.getSegmentUrl(curIdx);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String videoToDownload) {
            if (!started && !buffer.isEmpty()) {
                started = true;
                new VideoPlayer().execute();
            }

            if (running) {
                if (videoToDownload != null) {
                    new VideoDownloader().execute(videoToDownload);
                } else if (!videoFinished()) {
                    if (!mpd.isFinishedVideo()) {
                        new MPDDownloader().execute();
                    } else {
                        new DashManager().execute();
                    }
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

            curIdx++;

            if (running) {
                new DashManager().execute();
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
            if (buffer.isEmpty()) {
                if (videoFinished()) {
                    return;
                } else {
                    spinnerView.setVisibility(View.VISIBLE);

                    if (running) {
                        new VideoPlayer().execute();
                    }
                }
            } else {
                String vidPath = buffer.poll();

                vidView.setVideoPath(vidPath);
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
