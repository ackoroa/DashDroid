package vitdube.com.vidtube;

import android.app.ListActivity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xingjia.zhang on 13/11/17.
 */

public class ViewLocalActivity extends ListActivity {

    Toolbar toolbar;
    VideoClipDbHelper dbHelper;
    SQLiteDatabase db;
    List<File> videoInfos = new ArrayList<>();
    private static String TAG = "ServerView";
    ArrayAdapter<String> adapter;
    Segmenter segmenter;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_local);

        context = this;

        dbHelper = new VideoClipDbHelper(this);
        db = dbHelper.getWritableDatabase();

        String path = Environment.getExternalStorageDirectory().getPath() + "/Movies";
//        String path = "/sdcard/Movies";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);

        List<String> filePaths = new ArrayList<>();

        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            filePaths.add(files[i].getAbsolutePath());
            videoInfos.add(files[i]);
        }


        adapter = new ArrayAdapter<>(this, R.layout.db_list_item, filePaths);

        setListAdapter(adapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        final File videoFile = videoInfos.get(position);

        segmenter = new Segmenter();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.fromFile(videoFile));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);

        retriever.release();

        final Map<String, Object> config = new HashMap<>();
        config.put("isPostProcessing", true);
        config.put("originFilePath", videoFile.getAbsolutePath());
        config.put("splitFilePrefix", "/sdcard/" + videoFile.getName() + "_");
        config.put("startTime", 0l);
        config.put("endTime", timeInMillisec);

        Uploader.initNewVideo(videoInfos.get(position).getName(), new PostTaskListener<String>() {
            @Override
            void onPostTask(String result) {
                int videoId = Integer.valueOf(result);
                segmenter.splitFixedVideo(config,
                        new PostTaskListener<String>() {
                            @Override
                            void onPostTask(String result) {
                                Toast.makeText(context, "Finished segmenting", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new PostTaskListener<Map<String, Object>>() {
                            @Override
                            void onPostTask(Map<String, Object> segmentResult) {
                                File[] segmentedFiles = new File("/sdcard/").listFiles();
                                for(int i = 0; i < segmentedFiles.length; i++)
                                {
                                    File file = segmentedFiles[i];
                                    String filePath = file.getName();
                                    if(filePath.indexOf(videoFile.getName()) > -1) {
                                        String chunkId = filePath.substring(filePath.lastIndexOf("s") + 1,
                                                filePath.lastIndexOf("."));
                                        dbHelper.insertClipInfoIntoDB(db, "desinated test video",
                                                Integer.valueOf(chunkId),
                                                file.getAbsolutePath());
                                    }
                                }

                            }
                        });
            }
        });

    }
}
