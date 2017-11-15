package dashdroid.dashdroidplayer.util;

import android.util.Log;

import java.util.Random;

public class Properties {
    public static String VIDEO_FILE_EXTENSION = ".mp4";

    // adaptation algo and buffer parameters
    public final static int BUFFER_TOTAL_DURATION = 50;
    public final static int B_MIN = 6;
    public final static double SWITCH_PCT_BUFFER = 0.975;
    public final static int PAST_WINDOW_SIZE = 5;
    public static final double PLAYER_BUFFERING_DELAY = 0.2;

    // live stream parameters
    public final static int LIVE_OFFSET = 4;
    public static final double CHECK_MPD_DELAY = 1;

    // artificial download delays for testing
    private static double DELAY_MEAN = 0;
    private static double DELAY_STD_DEV = 0;
    private static Random rand = new Random(System.currentTimeMillis());
    public static double dlThrottleDuration() {
        double delay = Math.abs(rand.nextGaussian()) * DELAY_STD_DEV + DELAY_MEAN;
        Log.i("trace", "artificial dl delay: " + delay);
        return delay;
    }
}
