package dashdroid.dashdroidplayer.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import dashdroid.dashdroidplayer.R;

//https://code.tutsplus.com/tutorials/streaming-video-in-android-apps--cms-19888
public class VideoPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
    }
}
