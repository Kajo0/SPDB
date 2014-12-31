package pl.edu.pw.elka.spdb;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class Utils {
    
    /**
     * Decodes polyline from google route.
     * 
     * @param encoded
     * @return
     */
    public static List<GeoPoint> decodePoly(String encoded) {

        List<GeoPoint> poly = Lists.newArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            GeoPoint p = new GeoPoint(((double) lat / 1E5),
                 ((double) lng / 1E5));
            poly.add(p);
        }

        return poly;
    }
    
    /**
     * Creates url with parameters.
     * 
     * @param baseUrl
     * @param params
     * @return
     * @throws MalformedURLException 
     */
    public static URL createUrl(String baseUrl, Map<String, String> params) throws MalformedURLException {
        if (params.isEmpty()) {
            return new URL(baseUrl);
        }
        List<String> keyVal = Lists.newArrayList();
        for (Entry<String, String> entry : params.entrySet()) {
            try {
                keyVal.add(entry.getKey() + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // it wont happen
            }
        }

        return new URL(baseUrl + "?" + StringUtils.join(keyVal, "&"));
    }
}
