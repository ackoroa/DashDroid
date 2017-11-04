package dashdroid.dashdroidplayer.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dashdroid.dashdroidplayer.R;

public class VideoListActivity extends ListActivity {
    //TODO this needs to be (id, name)
    ArrayList<String> videos = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("trace", "creating video list activity");
        setContentView(R.layout.activity_video_list);

        Log.i("trace", "creating list adapter");
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, videos);
        setListAdapter(adapter);

        Log.i("trace", "creating button listener");
        View refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshVideoList();
            }
        });

        Log.i("trace", "initial vido list GET");
        refreshVideoList();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String videoId = videos.get(position);
        String videoName = videos.get(position);
        Log.i("trace", videoName + " selected");

        Intent intent = new Intent(VideoListActivity.this, VideoPlayerActivity.class);
        intent.putExtra("vidId", videoId);
        startActivity(intent);
    }

    public void refreshVideoList() {
        Log.i("trace", "refresh video list");
        new VideoListRetriever().execute();
    }

    private class VideoListRetriever extends AsyncTask<Void, Void, List<String>> {
        ObjectMapper mapper = new ObjectMapper();

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                URL url = new URL("http://monterosa.d2.comp.nus.edu.sg:32768/dash-server/rest/video/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) conn.getContent()));
                String response = reader.readLine();

                Log.i("trace", "response received: " + response);

                Map<String, Object> map = mapper.readValue(
                        response,
                        new TypeReference<Map<String, Object>>(){}
                );
                Log.i("trace", "response parsed: " + map.get("data"));

                List<String> videoNames = new ArrayList<>();
                for (Map<String, Object> videoData : (List<Map<String, Object>>) map.get("data")) {
                    videoNames.add(videoData.get("id").toString());
                }

                return videoNames;
            } catch (Exception e) {
                Log.e("error", e.getMessage(), e);
                return new ArrayList<String>();
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            if (!result.isEmpty()) {
                Log.i("trace", "Videos found. Refresh video list");

                videos.clear();
                videos.addAll(result);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
