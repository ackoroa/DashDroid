package vitdube.com.vidtube;

import android.provider.BaseColumns;

/**
 * Created by xingjia.zhang on 27/9/17.
 */

public class VideoClipContract {
    private VideoClipContract() {};

    public static class VideoClip implements BaseColumns {
        public static final String TABLE_NAME = "videoClips";
        public static final String COLUMN_NAME_TITLE = "videoTitle";
        public static final String COLUMN_NAME_FILEPATH = "filePath";
        public static final String COLUMN_NAME_CHUNK_ID = "chunkId";
        public static final String COLUMN_NAME_UPLOADED = "uploaded";
        public static final String COLUMN_NAME_TOUPLOAD = "toUpload";
        public static final String COLUMN_NAME_VIDEO_ID = "videoId";
    }
}
