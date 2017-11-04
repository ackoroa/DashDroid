package dashdroid.dashdroidplayer.logic;

import android.util.Log;

import java.util.List;

import dashdroid.dashdroidplayer.model.Representation;

public class RepresentationPicker {
    /**
     *
     * @return URL of representation to be downloaded or
     * null if nothing is to be downloaded at the currrent time
     */
    public String chooseRepresentation(
            List<Representation> representations,
            long bufferContentSize,
            double latestBandwidth) {
        Log.i("trace", "bufferContentSize: " + bufferContentSize);
        Log.i("trace", "latestBandwidth: " + latestBandwidth);

        return "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4";
    }
}
