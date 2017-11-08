package dashdroid.dashdroidplayer.logic;

import android.util.Log;

import java.util.List;

import dashdroid.dashdroidplayer.model.Representation;

public class RepresentationPicker {
    public Representation chooseRepresentation(
            VideoBuffer buffer,
            List<Representation> representations,
            double latestBandwidth) {
        Log.i("trace", "bufferRatio: " + buffer.fillRatio());
        Log.i("trace", "latestBandwidth: " + latestBandwidth);

        try {
            if (buffer.fillRatio() > 0.75) {
                Thread.sleep(1000);
                return null;
            }
        } catch (Exception e) {
            Log.e("error", e.getMessage(), e);
            return null;
        }

        return representations.get(0);
    }
}
