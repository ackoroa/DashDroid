package vitdube.com.vidtube;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by xingjia.zhang on 28/10/17.
 */

public class Uploader implements View.OnClickListener {

    VideoClipDbHelper dbHelper;
    SQLiteDatabase readableDb;
    SQLiteDatabase writableDb;
    Context context;
    String videoName;
    List<VideoClip> clipsToUpload;
    Integer videoId;
    Integer clipsUploaded;
    Button buttonView;
    Video video;
    CustomArrayAdapter adapter;

    public static final String ENDPOINT = "http://monterosa.d2.comp.nus.edu.sg:32768/dash-server/rest";

    public Uploader(Context context, String videoName, Video video) {
        this.context = context;
        this.videoName = videoName;
        this.video = video;
        dbHelper = new VideoClipDbHelper(context);
        readableDb = dbHelper.getReadableDatabase();
        writableDb = dbHelper.getWritableDatabase();
    }

    @Override
    public void onClick(View view) {
        Log.i("Uploader", "Click received.");
        this.clipsToUpload = dbHelper.getVideoClipsByName(readableDb, videoName);
        this.clipsUploaded = 0;

        dbHelper.updateDBClipToUploadStatus(writableDb, videoName, true);

        Toast.makeText(this.context, "Uploading "
                + videoName + ". Clips: " + String.valueOf(clipsToUpload.size()), Toast.LENGTH_SHORT).show();

        this.buttonView = (Button) view;
        updateButtonToUpdating();
        initNewVideo();

    }

    private void setId(Integer id) {
        this.videoId = id;
    }

    private void updateButtonToUpdating() {
        this.buttonView.setClickable(false);
        this.buttonView.setText("Uploading...");
    }

    private void updateButtonToUpdated() {
        this.buttonView.setClickable(false);
        this.buttonView.setText("Uploaded");
    }
    private void updateButtonToUpdate() {
        this.buttonView.setClickable(true);
        this.buttonView.setText("Upload");
    }

    private void closeDb() {
        readableDb.close();
        writableDb.close();
    }

    static class InitTask extends AsyncTask<String, Object, String> {
        private PostTaskListener<String> postTaskListener;

        protected InitTask(PostTaskListener<String> postTaskListener){
            this.postTaskListener = postTaskListener;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null && postTaskListener != null) {
                Log.i("Uploader", "asyncTask pass result: " + result);
                postTaskListener.onPostTask(result);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String videoName = strings[0];
            Log.i("Uploader", "Initiating new video:" + videoName);
            HttpURLConnection httpConn = null;
            try {
                URL url = new URL(ENDPOINT + "/video/new");
                Log.i("Uploader", "URL:" + url);

                JSONObject json = new JSONObject();
                json.put("name", videoName);

                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setDoInput(true);
                httpConn.setRequestMethod("GET");
                httpConn.setRequestProperty("Accept","*/*");
                httpConn.setRequestProperty("Content-type", "application/json");
                httpConn.setChunkedStreamingMode(0);

                InputStream in = new BufferedInputStream(httpConn.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder resultBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultBuilder.append(line);
                }
                JSONObject result = new JSONObject(resultBuilder.toString());

                if (httpConn.getResponseCode() == 200) {
                    Log.i("Uploader", "Initiated video name.");
                    Log.i("Uploader", httpConn.getResponseMessage());
                    return String.valueOf((Integer) result.get("data"));
                }
                Log.wtf("Uploader", "Got response code " + httpConn.getResponseCode());
                httpConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Uploader", "Upload failed: " + e.getMessage());
            }
            return "-1";
        }

    }

    private void initNewVideo() {
        InitTask initTask = new InitTask(new PostTaskListener<String>() {
            @Override
            void onPostTask(String result) {
                Log.i("Uploader", "onPostTask got result: " + result);
                if (result.equals("-1")) {
                    updateButtonToUpdate();
                    Toast.makeText(context, "Failed to init new video...", Toast.LENGTH_SHORT).show();
                    return;
                }
                setId(Integer.valueOf(result));
                Toast.makeText(context, "Inited new video with id:" + result, Toast.LENGTH_SHORT).show();

                dbHelper.updateDBClipVideoId(writableDb, videoName, Integer.valueOf(result));

                if (clipsToUpload.size() < 1) {
                    endVideo();
                }

                for (VideoClip clip: clipsToUpload) {
                    uploadClip(clip.getFilePath(), clip.chunkId);
                }

            }
        });
        initTask.execute(new String[]{videoName});
    }

    static class UploadTask extends AsyncTask<String, Object, Boolean> {
        private PostTaskListener<Boolean> postTaskListener;

        protected UploadTask(PostTaskListener<Boolean> postTaskListener){
            this.postTaskListener = postTaskListener;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);


            if (result != null && postTaskListener != null) {
                Log.i("Uploader", "asyncTask pass result: " + result);
                postTaskListener.onPostTask(result);
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String videoId = strings[0];
            String filePath =  strings[1];
            Integer sequenceNumber = Integer.valueOf(strings[2]);
            Log.i("Uploader", "Uploading " + filePath);

            try {
                URL url = new URL(ENDPOINT + "/video/" + videoId + "/" + sequenceNumber + "/upload");

                Log.i("Uploader", "URL:" + url);

                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                String contentType = "application/octet-stream";
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Accept", "*/*");
                httpConn.setRequestProperty("Content-type", contentType);
                httpConn.setChunkedStreamingMode(0);

                OutputStream outputStream = httpConn.getOutputStream();
                FileInputStream inputStream = new FileInputStream(new File(filePath));

                byte[] buffer = new byte[4096];
                int bytesRead = -1;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                InputStream responseInput = new BufferedInputStream(httpConn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseInput));
                StringBuilder resultBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultBuilder.append(line);
                }
                JSONObject result = new JSONObject(resultBuilder.toString());

                outputStream.close();
                inputStream.close();

                if (httpConn.getResponseCode() == 200) {
                    if ((boolean) result.get("success")) {
                        Log.i("Uploader", "Upload success.");
                        return true;
                    }
                    Log.e("Uploader", "Upload failed: " + resultBuilder.toString());

                }
                Log.wtf("HTTP", "Response status: " + httpConn.getResponseCode());
                Log.wtf("HTTP", "Response message: " + httpConn.getResponseMessage());
                return false;

            } catch (Exception e) {
                Log.i("Uploader", "Upload failed(E): " + e.getMessage());
                return false;
            }
        }
    }

    private void uploadClip(final String filePath, final Integer sequenceNumber) {
        UploadTask uploadTask = new UploadTask(new PostTaskListener<Boolean>() {
            @Override
            void onPostTask(Boolean result) {
                if (result == false) {
                    updateButtonToUpdate();
                    dbHelper.updateDBClipUploadStatus(writableDb, filePath, false);
                    Toast.makeText(context, "Failed to upload video clip " + sequenceNumber, Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.updateDBClipUploadStatus(writableDb, filePath, true);
                Toast.makeText(context, "Uploaded video clip " + sequenceNumber, Toast.LENGTH_SHORT).show();
                clipsUploaded++;
                Toast.makeText(context, "Uploaded " + clipsUploaded + "/" + clipsToUpload.size()
                        , Toast.LENGTH_SHORT).show();
                if (clipsToUpload.size() == clipsUploaded) {
                    Toast.makeText(context, "End video!", Toast.LENGTH_SHORT).show();
                    endVideo();
                }
            }
        });
        uploadTask.execute(new String[]{videoId.toString(), filePath, sequenceNumber.toString()});
    }

    public void uploadFailedClips() {
        List<VideoClip> clips = dbHelper.getVideoClipsByIncompleteUpload(writableDb);
        for (final VideoClip clip : clips) {
            uploadSingleClip(String.valueOf(clip.getVideoId()),
                    clip.getChunkId(),
                    clip.getFilePath(),
                    new PostTaskListener<Boolean>() {
                        @Override
                        void onPostTask(Boolean result) {
                            dbHelper.updateDBClipUploadStatus(writableDb, clip.getFilePath(), true);
                        }
                    });
        }
    }

    public static void initNewVideo(String videoName, PostTaskListener<String> listener) {
        InitTask initTask = new InitTask(listener);
        initTask.execute(new String[]{videoName});
    }

    public static void uploadSingleClip (final String videoId, final Integer sequenceNumber,
                                         final String filePath, PostTaskListener<Boolean> listener) {
        UploadTask uploadTask = new UploadTask(listener);
        uploadTask.execute(new String[] {videoId, filePath, sequenceNumber.toString()});
    }

    static class EndVideoTask extends AsyncTask<String, Object, Boolean> {
        private PostTaskListener<Boolean> postTaskListener;

        protected EndVideoTask(PostTaskListener<Boolean> postTaskListener){
            this.postTaskListener = postTaskListener;
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            String videoId = strings[0];
            String videoName = strings[1];
            String total = strings[2];
            Log.i("Uploader", "End video:" + videoName);
            HttpURLConnection httpConn = null;
            try {
                URL url = new URL(ENDPOINT + "/video/" + videoId + "/end");
                Log.i("Uploader", "URL:" + url);

                JSONObject json = new JSONObject();
                json.put("name", videoName);
                json.put("total", Integer.valueOf(total));

                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setDoInput(true);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Accept","*/*");
                httpConn.setRequestProperty("Content-type", "application/json");
                httpConn.setChunkedStreamingMode(0);

                try {
                    DataOutputStream printout = new DataOutputStream(httpConn.getOutputStream ());
                    printout.writeBytes(jsonParam.toString());
                    printout.flush ();
                    printout.close ();
                } catch(Exception e) {
                    //do nothing
                }
                
                InputStream in = new BufferedInputStream(httpConn.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder resultBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultBuilder.append(line);
                }
                JSONObject result = new JSONObject(resultBuilder.toString());

                if (httpConn.getResponseCode() == 200) {
                    Log.i("Uploader", "Initiated video name.");
                    Log.i("Uploader", httpConn.getResponseMessage());
                    return true;
                }
                Log.wtf("Uploader", "Got response code " + httpConn.getResponseCode());
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Uploader", "Upload failed: " + e.getMessage());
                return false;
            } finally {
                httpConn.disconnect();
            }
        }
    }

    private void endVideo() {
        EndVideoTask endTask = new EndVideoTask(new PostTaskListener<Boolean>() {
            @Override
            void onPostTask(Boolean result) {
                closeDb();
                if (result == false) {
                    updateButtonToUpdate();
                    Toast.makeText(context, "Failed to end video " + videoName, Toast.LENGTH_SHORT).show();
                    return;
                }
                updateButtonToUpdated();
                Toast.makeText(context, "Ended video " + videoName, Toast.LENGTH_SHORT).show();
            }
        });
        endTask.execute(new String[]{videoId.toString(),
                videoName,
                String.valueOf(clipsToUpload.size())});
    }

    public static void endVideo(String id, String videoName, int totalClips, PostTaskListener<Boolean> listener) {
        EndVideoTask endTask = new EndVideoTask(listener);
        endTask.execute(new String[]{id, videoName, String.valueOf(totalClips)});
    }

}
