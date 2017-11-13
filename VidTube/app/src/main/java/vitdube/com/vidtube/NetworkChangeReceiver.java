package vitdube.com.vidtube;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by xingjia.zhang on 12/11/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    Uploader uploader;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        uploader = new Uploader(context, "", null);

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        int status = activeNetwork.getType();
        // Note: 1 means WIFI Connection
        if ( status == 1) {
            uploader.uploadFailedClips();
        }

        Toast.makeText(context, "Network status change: " +
                (status == 1 ? "WIFI connected" : "No connection"), Toast.LENGTH_SHORT).show();
    }
}