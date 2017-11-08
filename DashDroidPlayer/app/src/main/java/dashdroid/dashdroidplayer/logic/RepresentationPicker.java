package dashdroid.dashdroidplayer.logic;

import android.util.Log;

import java.util.List;

import dashdroid.dashdroidplayer.model.Representation;

public class RepresentationPicker {
    /**
     *
     * @return Representation to be downloaded or
     * null if nothing is to be downloaded at the currrent time
     */
    public Representation chooseRepresentation(
            List<Representation> representations,
            long bufferContentSize,
            double latestBandwidth) {
        Log.i("trace", "bufferContentSize: " + bufferContentSize);
        Log.i("trace", "latestBandwidth: " + latestBandwidth);

        return representations.get(0);
    }
}
