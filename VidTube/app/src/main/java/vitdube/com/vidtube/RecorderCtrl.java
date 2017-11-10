package vitdube.com.vidtube;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Exchanger;

import processing.ffmpeg.videokit.Command;
import processing.ffmpeg.videokit.LogLevel;
import processing.ffmpeg.videokit.VideoKit;
import processing.ffmpeg.videokit.VideoProcessingResult;

/**
 * Created by xingjia.zhang on 11/9/17.
 */

public class RecorderCtrl
        extends SurfaceView implements SurfaceHolder.Callback{

    private Context context;
    private Camera camera;
    private SurfaceHolder holder;
    private MediaRecorder recorder;
    private boolean recording;
    private Boolean isLive = false;
    private Boolean liveEnd = false;
    private int chunk;
    private String title;
    private String lastFilePath;
    private String filePathPrefix;
    private final VideoKit videoKit = new VideoKit();
    private Long startTime;
    private Long endTime;
    private Uploader uploader;
    private Integer liveVideoId;

    VideoClipDbHelper videoClipDbHelper;
    SQLiteDatabase db;
    SQLiteDatabase readableDb;


    public RecorderCtrl(Context context, MediaRecorder recorder, Camera camera) {
        super(context);
        this.recorder = recorder;
        this.camera = camera;
        this.context = context;
        videoKit.setLogLevel(LogLevel.FULL);
        this.holder = getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);


        videoClipDbHelper = new VideoClipDbHelper(context);
        db = videoClipDbHelper.getWritableDatabase();
        readableDb = videoClipDbHelper.getReadableDatabase();
    }

    public boolean getRecording() {
        return recording;
    }

    public String getTitle() { return title; }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
    }

    public void setLiveVideoId(Integer id) {
        this.liveVideoId = id;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        if (this.camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.recorder != null) {
            recorder.setPreviewDisplay(getHolder().getSurface());
        }

    }

    public void initVideoPrefix() {
        Toast.makeText(context, "Update title!", Toast.LENGTH_SHORT).show();
        DateFormat format = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String now = format.format(new Date());
        this.chunk = 0;
        this.title = "Vid_" + now;

        this.filePathPrefix = "/sdcard/vidTube_file_" + now + "_";
        this.lastFilePath = "/sdcard/vidTube_file_" + now + ".mp4";
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        camera.stopPreview();
    }

    class SocketSetupTask extends AsyncTask<String, Object, FileDescriptor> {
        private PostTaskListener<FileDescriptor> postTaskListener;
        LocalSocket sender;
        LocalSocket receiver;
        LocalServerSocket socket;

        protected SocketSetupTask(PostTaskListener<FileDescriptor> postTaskListener){
            this.postTaskListener = postTaskListener;
        }

        @Override
        protected void onPostExecute(FileDescriptor result) {
            super.onPostExecute(result);

            if (result != null && postTaskListener != null) {
                try {
                    Log.i("Uploader", "asyncTask pass result: " + result);
                    postTaskListener.onPostTask(result);
                    receiver = socket.accept();
                    int ret = 0;
                    while ((ret = receiver.getInputStream().read()) != -1) {
                        System.out.println("ret =" + ret);
                    }

                    System.out.println("ret =" + ret);
                } catch (Exception e) {

                }
            }
        }

        @Override
        protected FileDescriptor doInBackground(String... strings) {
            try {
                Log.i("Recorder", "Setting up socket....");
                socket = new LocalServerSocket("/sdcard/fake");

                sender = new LocalSocket();
                sender.connect(new LocalSocketAddress("/sdcard/fake"));

            } catch (Exception e) {
                Log.e("Recorder","Can't get socket working...");
                e.printStackTrace();
            }

            Log.i("Recorder", socket.toString() + " fd: " + socket.getFileDescriptor());
            return sender.getFileDescriptor();
        }
    }

    public void initRecorder() {

        liveEnd = false;
        camera.unlock();
        recorder.setCamera(camera);

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);

        recorder.setOutputFile(lastFilePath);
//        final SocketSetupTask socketSetup = new SocketSetupTask(new PostTaskListener<FileDescriptor>() {
//            @Override
//            void onPostTask(FileDescriptor result) {
//                try {
//                    Log.i("Uploader", "Got fd result - stream file:" + result);
//
//                    recorder.setOutputFile(result);
//
//                    prepareRecorder();
//                    startRecording();
//                } catch (Exception e) {
//
//                }
//            }
//        });
//
//        socketSetup.execute(new String[]{""});

    }

    public void prepareRecorder() {
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRecording() {
        if (recording == true) {
            return;
        }

        Log.i("Camera", "Starts recording...");
        recording = true;
        recorder.start();
        startTime = new Date().getTime();
//        startSplitting();
    }

    public void startSplitting() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("Splitter", "Splitter started....");
                    Thread.sleep(2000);
                    int segmentIdx = 1;

                    if (isLive) {
                        Uploader.initNewVideo(title, new PostTaskListener<String>() {
                            @Override
                            void onPostTask(String result) {
                                if (result.equals("-1")) {
                                    Uploader.initNewVideo(title, this);
                                }
                                setLiveVideoId(Integer.valueOf(result));
                                startLiveStream();
                            }
                        });
                    }
                    long videoDuration = ((endTime == null ? new Date().getTime() : endTime)  - startTime) / 1000;
                    do {
                        videoDuration = ((endTime == null ? new Date().getTime() : endTime)  - startTime) / 1000;
                        while (!new File(lastFilePath).exists() && !new File(lastFilePath).canRead()) {
                            Log.i("Splitter", "Waiting for " + lastFilePath + "to be created/readable.");
                            Thread.sleep(1000);
                        }

                        Log.i("Recorder", "Video" + " duration:" + videoDuration + " Starting: " + (segmentIdx - 1) * 2);
                        String segFilePath = filePathPrefix + "s" + segmentIdx + ".mp4";
                        Command command = videoKit.createCommand()
                                .overwriteOutput()
                                .inputPath(lastFilePath)
                                .outputPath(segFilePath)
                                .customCommand("-ss " + (segmentIdx - 1) * 3 + " -t " + segmentIdx * 2)
                                .copyVideoCodec()
                                .build();
                        VideoProcessingResult result = command.execute();
                        Log.i("Recorder", "Finished trimming! " + result.getCode() + result.getPath());
                        insertClipInfoIntoDB(segmentIdx, segFilePath);
                        segmentIdx++;
                        Thread.sleep(3000);
                    } while(videoDuration - (segmentIdx - 1) * 2 > 0.5);

                    Log.e("Recorder", "Finished trimming. total segments:" + segmentIdx);
                    liveEnd = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopRecording() {
        if (recording == false) {
            return;
        }
        try {
            recorder.stop();
            endTime = new Date().getTime();
            startSplitting();
        } catch (RuntimeException e) {
            Log.w("MediaRecorder", "Error stop the recorder...");
        }

        recorder.reset();
        recording = false;
    }

    private void insertClipInfoIntoDB(int chunk, String filePath) {
        ContentValues clipContent = new ContentValues();
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_TITLE, getTitle());
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH, filePath);
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID, chunk);
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED, 0);
        db.insert(VideoClipContract.VideoClip.TABLE_NAME, null, clipContent);
        Log.i("VideoClipDB", "Saved " + clipContent.toString());
    }

    private List<VideoClip> getVideoClips() {
        Cursor cursor = readableDb.rawQuery("SELECT * FROM " + VideoClipContract.VideoClip.TABLE_NAME
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_TITLE
                + " = '" + title
                + "' AND " + VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED
                + " = 0", null);

        List<VideoClip> clips = new ArrayList<>();

        while(cursor.moveToNext()) {
            VideoClip clip = new VideoClip();
            clip.setChunkId(cursor.getInt(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID)));
            clip.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH)));
            clips.add(clip);
        }
        return clips;
    }

    private void updateDBClipUploadStatus(String filePath, boolean uploaded) {
        db.execSQL("UPDATE " + VideoClipContract.VideoClip.TABLE_NAME
                + " SET " + VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED
                + " = " + (uploaded ? "1" : "0")
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH
                + "= '" + filePath + "'");
    }

    private void startLiveStream() {
        try {
            do {
                List<VideoClip> clips = getVideoClips();
                for (final VideoClip clip : clips) {
                    Uploader.uploadSingleClip(liveVideoId.toString(),
                            clip.getChunkId(),
                            clip.getFilePath(),
                            new PostTaskListener<Boolean>() {
                                @Override
                                void onPostTask(Boolean result) {
                                    updateDBClipUploadStatus(clip.getFilePath(), result);
                                }
                            });
                }
                Thread.sleep(2000);
            } while (!liveEnd);

            endLive();

        } catch ( Exception e) {

        }
    }

    private void endLive() {
        Uploader.endVideo(liveVideoId.toString(), title, new PostTaskListener<Boolean>() {
            @Override
            void onPostTask(Boolean result) {
                Log.i("Recorder", "Ending video.");
                if (result == false) {
                    endLive();
                }
            }
        });
    }
}
