package vitdube.com.vidtube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String title;
    private String outputFilePath;
    private String filePathPrefix;
    private Long startTime;
    private Long endTime;
    private Integer liveVideoId;
    private Segmenter segmenter = new Segmenter();

    private boolean liveEnded = false;

    private int liveVideoSeqNumber = 0;

    private static String TAG = "Recorder";

    VideoClipDbHelper videoClipDbHelper;
    SQLiteDatabase db;
    SQLiteDatabase readableDb;


    public RecorderCtrl(Context context, MediaRecorder recorder, Camera camera) {
        super(context);
        this.recorder = recorder;
        this.camera = camera;
        this.context = context;
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

    public void setLiveEnded(Boolean liveEnded) {
        this.liveEnded = liveEnded;
    }

    public void resetLiveVideo() {
        this.liveVideoId = null;
        this.liveVideoSeqNumber = 0;
        this.liveEnded = false;
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
        this.title = "Vid_" + now;

        this.filePathPrefix = context.getExternalFilesDir(null).getAbsolutePath() + "vidTube_file_" + now + "_";
        this.outputFilePath = context.getExternalFilesDir(null).getAbsolutePath() + "vidTube_file_" + now + ".mp4";
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



    public void initRecorder() {

        camera.unlock();
        recorder.setCamera(camera);

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        recorder.setProfile(cpHigh);

        Log.e(TAG, "Output file path:" + outputFilePath);
        recorder.setOutputFile(outputFilePath);

        Log.e(TAG, "is live:" + isLive);
        if (isLive) {
            Log.e(TAG, "set max duration!");
            recorder.setMaxDuration(5000);
        }

        try {
            recorder.prepare();
        } catch (Exception e) {
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
    }

    public void stopRecording() {
        if (recording == false) {
            return;
        }
        try {
            recorder.stop();
            endTime = new Date().getTime();

            final Map<String, Object> config = new HashMap<>();
            config.put("isPostProcessing", true);
            config.put("originFilePath", outputFilePath);
            config.put("splitFilePrefix", filePathPrefix);
            config.put("startTime", startTime);
            config.put("endTime", endTime);

            if (!isLive) {

                segmenter.startSplitting(config, new PostTaskListener<String>() {
                    @Override
                    void onPostTask(String result) {
                        return;
                    }
                }, new PostTaskListener<Map<String, Object>>() {
                    @Override
                    void onPostTask(Map<String, Object> result) {
                        videoClipDbHelper.insertClipInfoIntoDB(db, title, (int) result.get("segmentIdx"),
                                (String) result.get("segmentFilePath"));

                    }
                });
            } else {
                if (liveVideoId == null) {
                    Uploader.initNewVideo(title, new PostTaskListener<String>() {
                        @Override
                        void onPostTask(String result) {
                            liveVideoId = Integer.valueOf(result);
                            splitAndUpload();
                        }

                        private void splitAndUpload() {
                            segmenter.startSplitting(config, new PostTaskListener<String>() {
                                @Override
                                void onPostTask(String result) {
                                    liveVideoSeqNumber ++;
                                    if (liveEnded) {
                                        endLive();
                                    }
                                }
                            }, new PostTaskListener<Map<String, Object>>() {
                                @Override
                                void onPostTask(Map<String, Object> result) {
                                    int segmentIdx = (int) result.get("segmentIdx");
                                    final String path = (String) result.get("segmentFilePath");

                                    videoClipDbHelper.insertClipInfoIntoDB(db, title, segmentIdx, path);
                                    videoClipDbHelper.updateDBClipToUploadStatus(db, title, true);
                                    videoClipDbHelper.updateDBClipVideoId(db, title, liveVideoId);

                                    Uploader.uploadSingleClip(liveVideoId.toString(), segmentIdx, path,
                                            new PostTaskListener<Boolean>() {
                                                @Override
                                                void onPostTask(Boolean result) {
                                                    videoClipDbHelper.updateDBClipUploadStatus(db, path, true);
                                                }
                                            });

                                }
                            });
                        }
                    });
                } else {
                    segmenter.startSplitting(config, new PostTaskListener<String>() {
                        @Override
                        void onPostTask(String result) {
                            liveVideoSeqNumber ++;
                            if (liveEnded) {
                                endLive();
                            }
                        }
                    }, new PostTaskListener<Map<String, Object>>() {
                        @Override
                        void onPostTask(Map<String, Object> result) {
                            int segmentIdx = (int) result.get("segmentIdx");
                            final String path = (String) result.get("segmentFilePath");

                            videoClipDbHelper.insertClipInfoIntoDB(db, title, segmentIdx, path);
                            videoClipDbHelper.updateDBClipToUploadStatus(db, title, true);
                            videoClipDbHelper.updateDBClipVideoId(db, title, liveVideoId);

                            Uploader.uploadSingleClip(liveVideoId.toString(), segmentIdx + liveVideoSeqNumber * 2, path,
                                    new PostTaskListener<Boolean>() {
                                        @Override
                                        void onPostTask(Boolean result) {
                                            videoClipDbHelper.updateDBClipUploadStatus(db, path, true);
                                        }
                                    });
                        }
                    });
                }


            }
        } catch (RuntimeException e) {
            Log.w("MediaRecorder", "Error stop the recorder...");
        }

        recorder.reset();
        recording = false;
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

    private void endLive() {
        Log.e(TAG, "End live video! videoId:" + liveVideoId);
        Uploader.endVideo(liveVideoId.toString(), title,
                videoClipDbHelper.getAllVideoClipsVideoId(readableDb, liveVideoId).size(),
                new PostTaskListener<Boolean>() {
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
