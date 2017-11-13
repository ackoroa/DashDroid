package dashdroid.dashdroidplayer.util;

import android.util.Log;

import java.util.Random;

public class Properties {
    public static String VIDEO_FILE_EXTENSION = ".mp4";
    public final static int BUFFER_TOTAL_DURATION = 20;

    private static Random rand = new Random(System.currentTimeMillis());
    private static int DELAY_STD_DEV = 0;

    public static double dlThrottleDuration() {
        double delay = Math.abs(rand.nextGaussian()) * DELAY_STD_DEV;
        Log.i("trace", "artificial dl delay: " + delay);
        return delay;
    }
}
