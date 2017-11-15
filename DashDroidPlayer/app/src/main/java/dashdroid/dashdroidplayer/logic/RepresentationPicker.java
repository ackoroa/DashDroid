package dashdroid.dashdroidplayer.logic;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dashdroid.dashdroidplayer.model.Representation;
import dashdroid.dashdroidplayer.util.Properties;

public class RepresentationPicker {
    private final static double SWITCH_PCT_BUFFER = Properties.SWITCH_PCT_BUFFER;

    private final static int B_MIN = Properties.B_MIN;
    private final static double B_LOW = (int) (VideoBuffer.BUFFER_TOTAL_DURATION * 0.3);
    private final static double B_HIGH = (int) (VideoBuffer.BUFFER_TOTAL_DURATION * 0.7);
    private final static double B_OPT = (B_LOW + B_HIGH) / 2;

    private List<Integer> REP_BANDWIDTHS;
    private int SEGMENT_DURATION;

    private final static int PAST_WINDOW_SIZE = Properties.PAST_WINDOW_SIZE;
    private volatile List<Double> pastThroughputs;
    private volatile List<Integer> pastBufferLevels;

    private volatile RepLevel lastRep = RepLevel.LOW;
    private volatile boolean runningFastStart = true;

    public RepresentationPicker(List<Representation> representations, int segmentDuration) {
        REP_BANDWIDTHS = new ArrayList<>();
        REP_BANDWIDTHS.add(representations.get(RepLevel.LOW.ordinal()).bandwidth);
        REP_BANDWIDTHS.add(representations.get(RepLevel.MID.ordinal()).bandwidth);
        REP_BANDWIDTHS.add(representations.get(RepLevel.HIGH.ordinal()).bandwidth);

        SEGMENT_DURATION = segmentDuration;

        pastThroughputs = new LinkedList<>();
        pastBufferLevels = new LinkedList<>();
    }

    public double getBandwidthEstimate() {
        return pastAverage(pastThroughputs);
    }

    public String getLastRepString() {
        return lastRep.toString();
    }

    public RepLevel chooseRepresentation(int bufferLevel, double bandwidth) {
        Log.i("trace", "Buffer Level: " + bufferLevel);
        Log.i("trace", "Latest Bandwith: " + String.format("%.2f", bandwidth / 1000) + " kb/s");

        if (pastThroughputs.isEmpty() && bandwidth == 0) {
            Log.i("trace", "First segment, download LOW");
            return RepLevel.LOW;
        }

        pastBufferLevels.add(bufferLevel);
        if (pastBufferLevels.size() > PAST_WINDOW_SIZE) {
            pastBufferLevels.remove(0);
        }
        pastThroughputs.add(bandwidth);
        if (pastThroughputs.size() > PAST_WINDOW_SIZE) {
            pastThroughputs.remove(0);
        }

        RepLevel pickedRep;
        if (runningFastStart) {
            Log.i("trace", "Performing fast start");
            pickedRep = fastStart(bufferLevel, bandwidth);
        } else {
            Log.i("trace", "Performing adaptation");
            pickedRep = adapt(bufferLevel, bandwidth);
        }
        lastRep = pickedRep;
        Log.i("trace", "Download " + lastRep);

        return pickedRep;
    }

    private RepLevel fastStart(int bufferLevel, double bandwidth) {
        if (lastRep == RepLevel.HIGH
                || !monotonicalyIncreasing(pastBufferLevels)
                || repBandwidth(lastRep) > SWITCH_PCT_BUFFER * pastAverage(pastThroughputs)) {
            Log.i("trace", "Stopping fast start");
            runningFastStart = false;
            return adapt(bufferLevel, bandwidth);
        }

        if (bufferLevel > B_HIGH) {
            try {
                double delayDuration = bufferLevel - B_HIGH + SEGMENT_DURATION;
                Log.i("trace", "Delay download during fast start: " + delayDuration);
                Thread.sleep((long) (1000 * delayDuration));
            } catch (Exception e) {
                Log.e("trace", e.getMessage(), e);
            }
        }

        if (repBandwidth(lastRep.higher()) <= SWITCH_PCT_BUFFER * pastAverage(pastThroughputs)) {
            return lastRep.higher();
        }

        return lastRep;
    }

    private boolean monotonicalyIncreasing(List<Integer> pastHistory) {
        for (int i = 1; i < pastHistory.size(); i++) {
            if (pastHistory.get(i) < pastHistory.get(i-1)) {
                return false;
            }
        }
        return true;
    }

    private RepLevel adapt(int bufferLevel, double availBandwidth) {
        if (bufferLevel < B_MIN) {
            return RepLevel.LOW;
        }

        if (bufferLevel < B_LOW) {
            if (repBandwidth(lastRep) >= availBandwidth) {
                return lastRep.lower();
            }
            return  lastRep;
        }

        if (bufferLevel < B_HIGH) {
            return delayOrUpgrade(bufferLevel, availBandwidth, false);
        }

        return delayOrUpgrade(bufferLevel, availBandwidth, true);
    }

    private RepLevel delayOrUpgrade(int bufferLevel, double availBandwidth, boolean allowUpgrade) {
        if (lastRep == RepLevel.HIGH ||
                repBandwidth(lastRep.higher()) >= SWITCH_PCT_BUFFER * pastAverage(pastThroughputs)) {
            try {
                double delayDuration = Math.max(SEGMENT_DURATION, bufferLevel - B_OPT);
                Log.i("trace", "Delay download: " + delayDuration);
                Thread.sleep((long) (1000 * delayDuration));
            } catch (Exception e) {
                Log.e("trace", e.getMessage(), e);
            }
        }

        if (allowUpgrade) {
            return lastRep.higher();
        } else {
            return lastRep;
        }
    }

    private double pastAverage(List<Double> pastHistory) {
        if (pastHistory.size() <= 0) {
            return 0;
        }

        double total = 0;
        for (double e : pastHistory) {
            total += e;
        }
        return total / pastHistory.size();
    }

    private int repBandwidth(RepLevel rep) {
        return REP_BANDWIDTHS.get(rep.ordinal());
    }
}
