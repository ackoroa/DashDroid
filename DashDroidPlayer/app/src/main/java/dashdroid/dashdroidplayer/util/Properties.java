package dashdroid.dashdroidplayer.util;

import android.util.Log;

import java.util.Random;

public class Properties {
    public static String VIDEO_FILE_EXTENSION = ".mp4";

    public final static int BUFFER_TOTAL_DURATION = 30;
    public final static int B_MIN = 4;
    public final static double SWITCH_PCT_BUFFER = 0.90;

    private static double DELAY_MEAN = 0.2;
    private static double DELAY_STD_DEV = 1;
    private static Random rand = new Random(System.currentTimeMillis());
    public static double dlThrottleDuration() {
        double delay = Math.abs(rand.nextGaussian()) * DELAY_STD_DEV + DELAY_MEAN;
        Log.i("trace", "artificial dl delay: " + delay);
        return delay;
    }
}
