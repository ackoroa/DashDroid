package vitdube.com.vidtube;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewDBActivity extends ActionBarActivity {

    Toolbar toolbar;
    VideoClipDbHelper dbHelper;
    SQLiteDatabase db;

    String[] projection = {
            VideoClipContract.VideoClip.COLUMN_NAME_TITLE,
            VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID,
            VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH,
            VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED,
            VideoClipContract.VideoClip.COLUMN_NAME_VIDEO_ID
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_db);

        dbHelper = new VideoClipDbHelper(this);
        db = dbHelper.getReadableDatabase();

        List<VideoClip> clips = getVideoClipsFromDB();

        Map<String, Video> videos = getStringVideoMapFromClips(clips);

        Log.i("VideoClipDB", "Total videos:" + String.valueOf(videos.size()));

        List<Video> videoList = new ArrayList<>(videos.values());
        Collections.sort(videoList);

        CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.video_list_item ,
                videoList);

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.video_list);

        listView.setAdapter(adapter);
    }

    @NonNull
    private List<VideoClip> getVideoClipsFromDB() {
        Cursor cursor = db.rawQuery("SELECT "
                + projection[0]
                + ", " + projection[1]
                + ", " + projection[2]
                + ", " + projection[3]
                + ", " + projection[4]
                + " FROM "
                + VideoClipContract.VideoClip.TABLE_NAME, null);

        List<VideoClip> clips = new ArrayList<>();


        while(cursor.moveToNext()) {
            VideoClip clip = new VideoClip();
            clip.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_TITLE)));
            clip.setChunkId(cursor.getInt(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID)));
            clip.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH)));
            clip.setUploaded(cursor.getInt(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED)));
            clip.setVideoId(cursor.getInt(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_VIDEO_ID)));
            clips.add(clip);
        }
        return clips;
    }

    @NonNull
    private Map<String, Video> getStringVideoMapFromClips(List<VideoClip> clips) {
        Map<String, Video> videos = new HashMap();
        List<String> titles = new ArrayList<>();
        for (VideoClip clip : clips) {
            Video video;
            if (titles.indexOf(clip.getTitle()) < 0) {
                video = new Video();
                video.setName(clip.getTitle());
            } else {
                video = videos.get(clip.getTitle());
            }
            video.addToClips(clip);
            titles.add(clip.getTitle());
            videos.put(clip.getTitle(), video);
        }
        return videos;
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!db.isOpen()) {
            db = dbHelper.getReadableDatabase();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

}
