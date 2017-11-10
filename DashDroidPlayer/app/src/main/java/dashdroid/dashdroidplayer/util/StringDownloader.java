package dashdroid.dashdroidplayer.util;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.net.URL;

public class StringDownloader {
    public static String download(String url) {
        String response = null;

        try {
            response = IOUtils.toString(new URL(url));
        } catch (Exception e) {
            Log.e("trace", e.getMessage(), e);
        }

        return response;
    }
}
