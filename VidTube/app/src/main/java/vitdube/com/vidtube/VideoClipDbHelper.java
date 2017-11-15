package vitdube.com.vidtube;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingjia.zhang on 27/9/17.
 */

public class VideoClipDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "VideoClip.db";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + VideoClipContract.VideoClip.TABLE_NAME + " (" +
                    VideoClipContract.VideoClip._ID + " INTEGER PRIMARY KEY," +
                    VideoClipContract.VideoClip.COLUMN_NAME_TITLE + " TEXT," +
                    VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH + " TEXT," +
                    VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID + " INT," +
                    VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED + " TINYINT," +
                    VideoClipContract.VideoClip.COLUMN_NAME_TOUPLOAD + " TINYINT," +
                    VideoClipContract.VideoClip.COLUMN_NAME_VIDEO_ID + " INT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + VideoClipContract.VideoClip.TABLE_NAME;

    public VideoClipDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void insertClipInfoIntoDB(SQLiteDatabase db, String videoName, int chunk, String filePath) {
        ContentValues clipContent = new ContentValues();
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_TITLE, videoName);
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH, filePath);
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID, chunk);
        clipContent.put(VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED, 0);
        db.insert(VideoClipContract.VideoClip.TABLE_NAME, null, clipContent);
        Log.i("VideoClipDB", "Saved " + clipContent.toString());
    }

    public void updateDBClipUploadStatus(SQLiteDatabase db, String filePath, boolean uploaded) {
        db.execSQL("UPDATE " + VideoClipContract.VideoClip.TABLE_NAME
                + " SET " + VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED
                + " = " + (uploaded ? "1" : "0")
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH
                + "= '" + filePath + "'");
    }

    public void updateDBClipToUploadStatus(SQLiteDatabase db, String videoName, boolean toUpload) {
        db.execSQL("UPDATE " + VideoClipContract.VideoClip.TABLE_NAME
                + " SET " + VideoClipContract.VideoClip.COLUMN_NAME_TOUPLOAD
                + " = " + (toUpload ? "1" : "0")
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_TITLE
                + "= '" + videoName + "'");
    }

    public void updateDBClipVideoId(SQLiteDatabase db, String videoName, int videoId) {
        db.execSQL("UPDATE " + VideoClipContract.VideoClip.TABLE_NAME
                + " SET " + VideoClipContract.VideoClip.COLUMN_NAME_VIDEO_ID
                + " = " + videoId
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_TITLE
                + "= '" + videoName + "'");
    }

    public List<VideoClip> getVideoClipsByName(SQLiteDatabase db, String videoName) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + VideoClipContract.VideoClip.TABLE_NAME
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_TITLE
                + " = '" + videoName
                + "' AND " + VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED
                + " = 0", null);

        return getVideoClipsFromCursor(cursor);
    }

    public List<VideoClip> getAllVideoClipsByName(SQLiteDatabase db, String videoName) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + VideoClipContract.VideoClip.TABLE_NAME
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_TITLE
                + " = '" + videoName
                + "'", null);

        return getVideoClipsFromCursor(cursor);
    }

    @NonNull
    private List<VideoClip> getVideoClipsFromCursor(Cursor cursor) {
        List<VideoClip> clips = new ArrayList<>();

        while(cursor.moveToNext()) {
            VideoClip clip = new VideoClip();
            clip.setChunkId(cursor.getInt(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_CHUNK_ID)));
            clip.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(VideoClipContract.VideoClip.COLUMN_NAME_FILEPATH)));
            clip.setVideoId(cursor.getInt(cursor.getColumnIndexOrThrow((VideoClipContract.VideoClip.COLUMN_NAME_VIDEO_ID))));
            clip.setTitle(cursor.getString(cursor.getColumnIndexOrThrow((VideoClipContract.VideoClip.COLUMN_NAME_TITLE))));
            clips.add(clip);
        }
        return clips;
    }

    public List<VideoClip> getVideoClipsByIncompleteUpload(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + VideoClipContract.VideoClip.TABLE_NAME
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_TOUPLOAD
                + " = 1 "
                + " AND " + VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED
                + " = 0", null);

        return getVideoClipsFromCursor(cursor);
    }

    public List<VideoClip> getVideoClipsByIncompleteUploadAndVideoName(SQLiteDatabase db, String videoName) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + VideoClipContract.VideoClip.TABLE_NAME
                + " WHERE " + VideoClipContract.VideoClip.COLUMN_NAME_TOUPLOAD
                + " = 1 "
                + " AND " + VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED
                + " = 0"
                + " AND " + VideoClipContract.VideoClip.COLUMN_NAME_TITLE
                + " = '" + videoName + "'", null);

        return getVideoClipsFromCursor(cursor);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}