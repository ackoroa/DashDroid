package dashdroid.dashdroidplayer.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import dashdroid.dashdroidplayer.R;

//https://code.tutsplus.com/tutorials/streaming-video-in-android-apps--cms-19888
public class VideoPlayerActivity extends AppCompatActivity {
    Queue<String> vidFiles = new LinkedList<>();

    VideoView vidView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        vidView = (VideoView)findViewById(R.id.dashPlayer);

        String videoId = getIntent().getStringExtra("vidId");
        Log.i("trace", "start player for video " + videoId);

        new VideoDownloader().execute("http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4");
    }

    private class VideoDownloader extends AsyncTask<String, Void, File> {
        @Override
        protected File doInBackground(String... params) {
            String vidUrl = params[0];
            File vidFile = getTempFile(getApplicationContext(), vidUrl);

            Log.i("trace", "Start download of " + vidUrl);
            try {
                URL url = new URL(vidUrl);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());

                FileOutputStream writer = new FileOutputStream(vidFile);
                writer.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (Exception e) {
                Log.e("error", e.getMessage(), e);
            }
            Log.i("trace", vidUrl + " downloaded");
            return vidFile;
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            if (result != null) {
                vidFiles.offer(result.getPath());
            }
            vidView.setVideoPath(vidFiles.poll());
            vidView.start();
        }
    }

    public File getTempFile(Context context, String url) {
        File file;
        Uri vidUri = Uri.parse(url);

        try {
            String fileName = vidUri.getLastPathSegment();
            Log.i("trace", "Creating file " + fileName);
            file = File.createTempFile(fileName, null, context.getCacheDir());
            Log.i("trace", "File created at " + file.getPath());
        } catch (IOException e) {
            Log.e("error", e.getMessage(), e);
            file = null;
        }
        return file;
    }
}
