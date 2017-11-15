package vitdube.com.vidtube;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements MediaRecorder.OnInfoListener{

    private View mContentView;

    private FrameLayout layout;

    private Button recordingButton;
    private Button viewDBButton;
    private Button viewServerButton;
    private Button viewLocalButton;
    private Switch liveSwitch;
    private Boolean isLive = false;

    Camera camera;
    MediaRecorder recorder;

    private RecorderCtrl recorderCtrl;

    Context appContext;

    VideoClipDbHelper videoClipDbHelper;
    SQLiteDatabase db;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestForPermissions();
        appContext = this;

        videoClipDbHelper = new VideoClipDbHelper(this);
        db = videoClipDbHelper.getWritableDatabase();

//        db.execSQL(videoClipDbHelper.SQL_DELETE_ENTRIES);
//        db.execSQL(videoClipDbHelper.SQL_CREATE_ENTRIES);
        setContentView(R.layout.activity_main);

        layout = (FrameLayout) findViewById(R.id.inner_frame_layout);

        recorder = new MediaRecorder();
        recorder.setOnInfoListener(this);

        requestQueue = Volley.newRequestQueue(this);

        prepareRecordingButton();
        prepareViewDBButton();
        prepareServerButton();
        prepareViewLocalButton();
        prepareSwitch();
    }

    private void requestForPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.getApplicationContext(),
                    "Need audio permission",
                    Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    200);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("Permission result:", Integer.toString(grantResults[0]));
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(requestCode == 200)
            {
                Log.i("Permission", "Permission granted");
            }
        } else {
            Log.d("Permission", "Permission failed");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (safeCameraOpen()) {
            recorderCtrl = new RecorderCtrl(this, recorder, camera);
            layout.addView(recorderCtrl);
        }

    }

    private void prepareServerButton() {
        viewServerButton = (Button) findViewById(R.id.server_list_button);
        viewServerButton.setClickable(true);
        viewServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainToServerViewIntent = new Intent();
                mainToServerViewIntent.setClass(appContext, ViewUploadedActivity.class);
                startActivity(mainToServerViewIntent);
            }
        });
    }

    private void prepareViewLocalButton() {
        viewLocalButton = (Button) findViewById(R.id.local_list_button);
        viewLocalButton.setClickable(true);
        viewLocalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainToServerViewIntent = new Intent();
                mainToServerViewIntent.setClass(appContext, ViewLocalActivity.class);
                startActivity(mainToServerViewIntent);
            }
        });
    }

    private void prepareViewDBButton() {
        viewDBButton = (Button) findViewById(R.id.button);
        viewDBButton.setClickable(true);
        viewDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainToDBViewIntent = new Intent();
                mainToDBViewIntent.setClass(appContext, ViewDBActivity.class);
                startActivity(mainToDBViewIntent);
            }
        });
    }

    private void prepareRecordingButton() {
        recordingButton = (Button) findViewById(R.id.button4);
        recordingButton.setClickable(true);
        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recorderCtrl.getRecording()) {
                    Log.i("Main", "Start Recording... live:" + isLive);
                    recorderCtrl.setIsLive(isLive);
                    recorderCtrl.resetLiveVideo();
                    recorderCtrl.initVideoPrefix();
                    recorderCtrl.initRecorder();
//                    recorder.setOnInfoListener();
                    recordingButton.setText("Stop Recording");
                    recorderCtrl.startRecording();

                } else {
                    recordingButton.setText("Start Recording");
                    recorderCtrl.setLiveEnded(true);
                    Log.i("MediaRecorderCtrl", "Stop Recording...");
                    recorderCtrl.stopRecording();

                }
            }
        });
    }

    private void prepareSwitch() {
        liveSwitch = (Switch) findViewById(R.id.live_switch);
        liveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Toast.makeText(appContext, "Live mode " + (isChecked ? "on" : "off") + "!",
                        Toast.LENGTH_SHORT).show();
                Log.i("Uggg", "is live:" + isChecked);
                isLive = isChecked;
            }
        });
    }

    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
            this.camera = Camera.open();
            qOpened = (camera != null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getBaseContext(), "Failed to connect to camera...",
                    Toast.LENGTH_LONG).show();
        }

        return qOpened;
    }

    private void updateVideoClipInfoIntoDB(String title, String chunkId) {
        ContentValues clipContent = new ContentValues();
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED, 1);
        db.update(VideoClipContract.VideoClip.TABLE_NAME, clipContent,
                (VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH + "= ? AND " + VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID + " = " + chunkId),
                new String[]{title});
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
        Toast.makeText(appContext, "Info!", Toast.LENGTH_SHORT).show();
        Log.e("UGGG", "Max duration reached!!");

        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Toast.makeText(appContext, "Chop!", Toast.LENGTH_SHORT).show();
            recorderCtrl.stopRecording();
            recorderCtrl.initVideoPrefix();
            recorderCtrl.initRecorder();
            recorder.setOnInfoListener(this);
            recorderCtrl.startRecording();
        }

    }
}
