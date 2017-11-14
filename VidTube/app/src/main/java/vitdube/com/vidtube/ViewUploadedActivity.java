package vitdube.com.vidtube;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xingjia.zhang on 13/11/17.
 */

public class ViewUploadedActivity extends ActionBarActivity {

    Toolbar toolbar;
    VideoClipDbHelper dbHelper;
    SQLiteDatabase db;
    List<VideoInfo> videoInfos = new ArrayList<>();
    private static String TAG = "ServerView";
    ArrayAdapter<String> adapter;

    //public static final String ENDPOINT = "http://monterosa.d2.comp.nus.edu.sg:32768/dash-server/rest";
    public static final String ENDPOINT = "http://192.168.1.5:8080/dash-server/rest";

    public class GetVideoListTask extends AsyncTask<String, Object, List<VideoInfo>> {

        @Override
        protected void onPostExecute(List<VideoInfo> videos) {
            super.onPostExecute(videos);
            videoInfos = videos;
            for (VideoInfo vid : videoInfos) {
                Log.i("adapter count", adapter.getCount() + "");
                adapter.insert("Heh", adapter.getCount());
                adapter.insert(String.valueOf(vid.getId()) + " - " + String.valueOf(vid.getSegments()), adapter.getCount());
            }
        }

        @Override
        protected List<VideoInfo> doInBackground(String... strings) {
            List<VideoInfo> videoInfos = new ArrayList<>();

            HttpURLConnection httpConn = null;
            try {
                URL url = new URL(ENDPOINT + "/video/list");
                Log.i("ServerView", "URL:" + url);

                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setDoInput(true);
                httpConn.setRequestMethod("GET");
                httpConn.setRequestProperty("Accept", "*/*");
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
                    JSONArray videos = result.getJSONArray("data");

                    for (int i = 0; i < videos.length(); i++) {
                        JSONObject videoJson = videos.getJSONObject(i);

                        VideoInfo videoInfo = new VideoInfo();
                        videoInfo.setName((String) videoJson.get("name"));
                        videoInfo.setId((Integer) videoJson.get("id"));
                        videoInfo.setSegments((Integer) videoJson.get("numberOfSegments"));
                        videoInfo.setHasEnded((Boolean) videoJson.get("fullVideo"));

                        videoInfos.add(videoInfo);
                    }
                }
                Log.wtf(TAG, "Got response code " + httpConn.getResponseCode());
                httpConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Uploader", "Upload failed: " + e.getMessage());
            }
            return videoInfos;
        }
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_server);
        ListView videoListView = (ListView) findViewById(R.id.server_list);

        List<String> info = new ArrayList<>();
        for (VideoInfo vid : videoInfos) {
            info.add(String.valueOf(vid.getId()) + " - " + String.valueOf(vid.getSegments()));
        }

        adapter = new ArrayAdapter(this,
                R.layout.db_list_item,
                R.id.chunk_info,
                info);
        videoListView.setAdapter(adapter);

        GetVideoListTask getVideoListTask = new GetVideoListTask();
        getVideoListTask.execute(new String[]{});

        try {
            Thread.sleep(2000);
        } catch (Exception e) {

        }




    }
}
