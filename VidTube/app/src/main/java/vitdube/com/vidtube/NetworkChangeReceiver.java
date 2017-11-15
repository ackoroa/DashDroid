package vitdube.com.vidtube;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by xingjia.zhang on 12/11/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    Uploader uploader;
    private static String TAG = "Network Receiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {

        uploader = new Uploader(context, "", null);

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifi.isWifiEnabled();
        if (!wifiEnabled) {
            return;
        }
        Log.i(TAG, "Trying to get wifi...");
        int status = 0;

        do {

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            Log.i(TAG, "Trying to get wifi status");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            // Note: 1 means WIFI Connection
            if (activeNetwork != null && activeNetwork.getType() == 1) {

                status = 1;
                uploader.uploadFailedClips();
            }
        } while (status == 0);

        Toast.makeText(context, "Network status change: " +
                (status == 1 ? "WIFI connected" : "No connection"), Toast.LENGTH_SHORT).show();
    }
}