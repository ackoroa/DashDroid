package dashdroid.dashdroidplayer.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dashdroid.dashdroidplayer.R;
import dashdroid.dashdroidplayer.util.JsonUtil;
import dashdroid.dashdroidplayer.util.StringDownloader;

public class VideoListActivity extends ListActivity {
    List<Pair<String, String>> videos = new ArrayList<>();
    ArrayList<String> videoNames = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, videoNames);
        setListAdapter(adapter);

        View refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshVideoList();
            }
        });

        Log.i("trace", "initial video list GET");
        refreshVideoList();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String videoId = videos.get(position).first;
        String videoName = videos.get(position).second;
        Log.i("trace", videoName + " selected");

        Intent intent = new Intent(VideoListActivity.this, VideoPlayerActivity.class);
        intent.putExtra("vidId", videoId);
        startActivity(intent);
    }

    public void refreshVideoList() {
        Log.i("trace", "refresh video list");
        new VideoListRetriever().execute();
    }

    private class VideoListRetriever extends AsyncTask<Void, Void, List<Pair<String, String>>> {
        @Override
        protected List<Pair<String, String>> doInBackground(Void... params) {
            try {
                String url = "http://monterosa.d2.comp.nus.edu.sg:32768/dash-server/rest/video/list";
                String response = StringDownloader.download(url);
                Log.i("trace", "response received: " + response);

                Map<String, Object> map = JsonUtil.fromJson(response);
                List<Map<String, Object>> videoData =  (List<Map<String, Object>>) map.get("data");
                Log.i("trace", "response parsed: " + videoData);

                List<Pair<String, String>> newVideos = new ArrayList<>();
                for (Map<String, Object> videoDatum : videoData) {
                    String id = videoDatum.get("id").toString();
                    String name = videoDatum.get("name").toString();
                    newVideos.add(new Pair<String, String>(id, name));
                }

                return newVideos;
            } catch (Exception e) {
                Log.e("error", e.getMessage(), e);
                return new ArrayList<Pair<String, String>>();
            }
        }

        @Override
        protected void onPostExecute(List<Pair<String, String>> result) {
            super.onPostExecute(result);
            if (!result.isEmpty()) {
                Log.i("trace", "Videos found. Refresh video list");

                videos = result;
                videoNames.clear();
                for (Pair<String, String>video : videos) {
                    videoNames.add(video.first);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }
}
