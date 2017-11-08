package dashdroid.dashdroidplayer.util;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JsonUtil {
    private static ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> fromJson(String jsonString) {
        try {
            return mapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            Log.e("error", e.getMessage(), e);
            return null;
        }
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            Log.e("error", e.getMessage(), e);
            return null;
        }
    }
}
