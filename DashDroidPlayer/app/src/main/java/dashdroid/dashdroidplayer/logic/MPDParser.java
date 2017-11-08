package dashdroid.dashdroidplayer.logic;

import android.util.Log;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;

import dashdroid.dashdroidplayer.model.MPD;

public class MPDParser {
    public static MPD parse(String mpdUrl) {
        try {
            SAXBuilder sb= new SAXBuilder();
            Document doc = sb.build(mpdUrl);

            doc.
        } catch (Exception e) {
            Log.e("error", e.getMessage(), e);
            return null;
        }
    }
}
