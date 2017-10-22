package dashdroid.dashdroidplayer.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dashdroid.dashdroidplayer.R;
import dashdroid.dashdroidplayer.provider.VideoListProvider;

public class VideoListActivity extends ListActivity {
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
        String videoName = videos.get(position);
    }

    public void refreshVideoList() {
        Log.i("trace", "refresh video list");
        List<String> vidList = VideoListProvider.getVideoList();

        videos.clear();
        videos.addAll(vidList);
        adapter.notifyDataSetChanged();
    }
}
