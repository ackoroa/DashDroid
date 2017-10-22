package dashdroid.dashdroidplayer.provider;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoListProvider {
    private static ObjectMapper mapper = new ObjectMapper();

    public static List<String> getVideoList() {
        try {
//        URL url = new URL("http://monterosa.d1.comp.nus.edu.sg/~team01/rest/video/list")
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        String response  = conn.getResponseMessage();
            String response = "{\"success\":true, \"message\":\"\", \"videos\":[\"video1\",\"video2\"]}";
            Log.i("trace", "response received: " + response);

            Map<String, Object> map = mapper.readValue(
                    response,
                    new TypeReference<Map<String, Object>>(){}
            );
            Log.i("trace", "response parsed: " + map);

            return (List<String>) map.get("videos");
        } catch (Exception e) {
            Log.e("error", e.getMessage(), e);
            return new ArrayList<String>();
        }
    }
}
