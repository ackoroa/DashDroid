package vitdube.com.vidtube;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import processing.ffmpeg.videokit.Command;
import processing.ffmpeg.videokit.LogLevel;
import processing.ffmpeg.videokit.VideoKit;
import processing.ffmpeg.videokit.VideoProcessingResult;

/**
 * Created by xingjia.zhang on 12/11/17.
 */

public class Segmenter {

    private VideoKit videoKit = new VideoKit();
    private static int SEGMENT_LENGTH_MS = 3000;
    private static String TAG = "Segmenter";

    public Segmenter() {
        super();
        videoKit.setLogLevel(LogLevel.FULL);
    }

    public class SegmentTask extends AsyncTask<Map<String, Object>, Object, String> {

        private PostTaskListener<String> listener;

        private PostTaskListener<Map<String, Object>> segmentListener;

        public SegmentTask(PostTaskListener<String> listener, PostTaskListener<Map<String, Object>> segmentListener) {
            this.listener = listener;
            this.segmentListener = segmentListener;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            listener.onPostTask(result);
        }

        @Override
        protected String doInBackground(Map... params) {
            Map<String, Object> config = params[0];
            Boolean isPostProcessing = (Boolean) config.get("isPostProcessing");
            String originFilePath = (String) config.get("originFilePath");
            String splitFilePrefix = (String) config.get("splitFilePrefix");
            Long startTime = (Long) config.get("startTime");
            Long endTime = (Long) config.get("endTime");

            try {
                Log.e(TAG, "Splitter started....");
                if (!isPostProcessing) { Thread.sleep(SEGMENT_LENGTH_MS); }

                int segmentIdx = 0;
                long videoDuration = ((endTime == null ? new Date().getTime() : endTime)  - startTime) / 1000;
                do {
                    videoDuration = ((endTime == null ? new Date().getTime() : endTime)  - startTime) / 1000;
                    while (!new File(originFilePath).exists() && !new File(originFilePath).canRead()) {
                        Log.i(TAG, "Waiting for " + originFilePath + "to be created/readable.");
                        Thread.sleep(1000);
                    }

                    Log.i(TAG, "Video" + " duration:" + videoDuration +
                            " Starting: " + segmentIdx * SEGMENT_LENGTH_MS/1000);

                    String segFilePath = splitFilePrefix + "s" + segmentIdx + ".mp4";
                    Command command = videoKit.createCommand()
                            .overwriteOutput()
                            .inputPath(originFilePath)
                            .outputPath(segFilePath)
                            .customCommand("-ss " + segmentIdx * SEGMENT_LENGTH_MS/1000
                                    + " -t " + (segmentIdx + 1) * SEGMENT_LENGTH_MS/1000)
                            .copyVideoCodec()
                            .build();
                    VideoProcessingResult result = command.execute();
                    Log.i(TAG, "Finished trimming! " + result.getCode() + result.getPath());

                    Map<String, Object> segmentResult = new HashMap<>();
                    segmentResult.put("segmentIdx", segmentIdx);
                    segmentResult.put("segmentFilePath", result.getPath());
                    segmentListener.onPostTask(segmentResult);

                    segmentIdx++;

                    if (!isPostProcessing) { Thread.sleep(SEGMENT_LENGTH_MS); }

                } while(videoDuration - segmentIdx * SEGMENT_LENGTH_MS/1000 > 0.5);

                Log.e(TAG, "Finished trimming. total segments:" + segmentIdx);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

    }

    public void startSplitting(Map<String, Object> config,
                               PostTaskListener<String> postSegmentListener,
                               PostTaskListener<Map<String, Object>> segmentTaskListener) {

        SegmentTask task = new SegmentTask(postSegmentListener, segmentTaskListener);
        task.execute(config);

    }
}
