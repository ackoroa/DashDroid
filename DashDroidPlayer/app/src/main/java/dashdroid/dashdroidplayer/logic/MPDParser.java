package dashdroid.dashdroidplayer.logic;

import android.util.Log;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.util.ArrayList;
import java.util.List;

import dashdroid.dashdroidplayer.model.MPD;
import dashdroid.dashdroidplayer.model.Representation;

public class MPDParser {
    public static MPD parse(String mpdUrl) {
        Log.i("trace", "Start parsing MPD for " + mpdUrl);
        SAXBuilder sb= new SAXBuilder();

        try {
            Element mpdElement = sb.build(mpdUrl).getRootElement();
            MPD mpd = getMpdBasicInfo(mpdElement);
            mpd.representations = getRepresentations(mpdElement);

            Log.i("trace", "Parsed MPD: " + mpd.toString());
            return mpd;
        } catch (Exception e) {
            Log.e("trace", e.getMessage(), e);
            return null;
        }
    }

    private static MPD getMpdBasicInfo(Element mpdElement) {
        Namespace ns = mpdElement.getNamespace();

        MPD mpd = new MPD();
        mpd.videoBaseUrl = mpdElement
                .getChild("Period", ns)
                .getChildText("BaseURL", ns);
        mpd.videoType = MPD.VideoType.valueOf(
                mpdElement.getAttributeValue("type").toUpperCase()
        );
        mpd.videoLength = Integer.valueOf(
                mpdElement.getAttributeValue("mediaPresentationDuration")
                        .replaceAll("[a-zA-Z]*", "")
        );
        return mpd;
    }

    private static List<Representation> getRepresentations(Element mpdElement) throws DataConversionException {
        Namespace ns = mpdElement.getNamespace();

        List<Element> repElements = mpdElement
                .getChild("Period", ns)
                .getChild("AdaptationSet", ns).getChildren();

        List<Representation> reps = new ArrayList<>();
        for (Element repElement : repElements) {
            Representation rep = new Representation();

            rep.id = repElement.getAttributeValue("id");
            rep.bandwidth = repElement.getAttribute("bandwidth").getIntValue();
            rep.frameRate = repElement.getAttribute("frameRate").getIntValue();

            rep.baseUrl = repElement.getChildText("BaseURL", ns);
            rep.segmentTemplate = repElement
                    .getChild("SegmentTemplate", ns)
                    .getAttributeValue("media");

            rep.numberOfSegments = repElement
                    .getChild("SegmentTemplate", ns)
                    .getChild("SegmentTimeline", ns)
                    .getChild("S", ns).getAttribute("r").getIntValue();
            rep.segmentDuration = repElement
                    .getChild("SegmentTemplate", ns)
                    .getChild("SegmentTimeline", ns)
                    .getChild("S", ns).getAttribute("d").getIntValue() /
                    repElement.getChild("SegmentTemplate", ns).getAttribute("timescale").getIntValue();

            reps.add(rep);
        }

        return reps;
    }
}
