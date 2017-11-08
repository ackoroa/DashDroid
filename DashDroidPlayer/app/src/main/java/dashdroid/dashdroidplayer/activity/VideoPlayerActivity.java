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
import dashdroid.dashdroidplayer.logic.RepresentationPicker;
import dashdroid.dashdroidplayer.logic.VideoBuffer;
import dashdroid.dashdroidplayer.util.FileUtils;
import dashdroid.dashdroidplayer.R;
import dashdroid.dashdroidplayer.model.MPD;
import dashdroid.dashdroidplayer.util.StringDownloader;

public class VideoPlayerActivity extends AppCompatActivity {
    VideoBuffer buffer = new VideoBuffer();
    RepresentationPicker repPicker = new RepresentationPicker();

    String videoId;
    VideoView vidView;
    ProgressBar spinnerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        vidView = (VideoView)findViewById(R.id.dashPlayer);
        spinnerView = (ProgressBar) findViewById(R.id.spinner);
        spinnerView.setVisibility(View.VISIBLE);

        videoId = getIntent().getStringExtra("vidId");
        Log.i("trace", "start player for video " + videoId);

        vidView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
                new VideoPlayer().execute();
                new VideoDeleter().execute();
            }
        });

        new DashManager().execute(); //TODO download MPD first then call this
    }

    @Override
    public void onBackPressed() {
        buffer.cleanAll();
        super.onBackPressed();
    }

    private boolean started = false;
    private int curIdx = 0;
    private double latestBandwidth = 0;

    private MPD mpd;

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
            new DashManager().execute();
        }
    }

    private class DashManager extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (curIdx <= mpd.getLastSegmentIdx()) {
                return mpd.getVideoBaseUrl() +
                        repPicker.chooseRepresentation(
                                mpd.representations,
                                buffer.getBufferContentSize(),
                                latestBandwidth)
                                .getSegmentUrl(curIdx);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String videoToDownload) {
            if (!started && okToGo()) {
                started = true;
                new VideoPlayer().execute();
            }

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

        private boolean okToGo() {
            return buffer.filledTo(0.5) || (curIdx >= mpd.getLastSegmentIdx());
        }
    }

    private class VideoDownloader extends AsyncTask<String, Void, File> {
        long downloadStartTime;

        @Override
        protected File doInBackground(String... params) {
            downloadStartTime = System.nanoTime();
            return FileUtils.download(getApplicationContext(), params[0]);
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            buffer.offer(result);

            double downloadTime = (double) (System.nanoTime() - downloadStartTime) / 1000000000;
            latestBandwidth = result.length() / downloadTime;

            curIdx++;
            new DashManager().execute();
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
                    new VideoPlayer().execute();
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
            buffer.cleanOne();
            return null;
        }
    }
}
