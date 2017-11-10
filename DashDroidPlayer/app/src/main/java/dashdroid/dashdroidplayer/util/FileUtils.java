package dashdroid.dashdroidplayer.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileUtils {
    private FileUtils(){}

    public static File download(Context context, String url) {
        File file = getTempFile(context, url);
        Log.i("trace", "Start download of " + url);
        try {;
            ReadableByteChannel rbc = Channels.newChannel(new URL(url).openStream());

            FileOutputStream writer = new FileOutputStream(file);
            writer.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            Log.e("trace", e.getMessage(), e);
        }
        Log.i("trace", url + " downloaded");
        return file;
    }

    public static void delete(File file) {
        boolean deleted = file.delete();
        if (deleted) {
            Log.i("trace", file.getPath() + " deleted");
        }
    }

    private static File getTempFile(Context context, String url) {
        File file;
        Uri vidUri = Uri.parse(url);

        try {
            String fileName = vidUri.getLastPathSegment();
            Log.i("trace", "Creating file " + fileName);
            file = File.createTempFile(fileName, null, context.getFilesDir());
            Log.i("trace", "File created at " + file.getPath());
        } catch (IOException e) {
            Log.e("trace", e.getMessage(), e);
            file = null;
        }
        return file;
    }
}
