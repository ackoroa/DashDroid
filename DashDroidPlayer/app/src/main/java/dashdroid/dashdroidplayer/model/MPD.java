package dashdroid.dashdroidplayer.model;

import java.util.ArrayList;
import java.util.List;

public class MPD {
    public int getLastSegmentIdx() {
        return 3;
    }

    public boolean isFinishedVideo() {
        return true;
    }

    public List<Representation> getRepresentations(int idx) {
        return new ArrayList<>();
    }
}
