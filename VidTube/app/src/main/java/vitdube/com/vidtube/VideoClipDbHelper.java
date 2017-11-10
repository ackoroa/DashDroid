package vitdube.com.vidtube;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
                    VideoClipContract.VideoClip.COLUMN_NAME_UPLOADED + " TINYINT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + VideoClipContract.VideoClip.TABLE_NAME;

    public VideoClipDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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