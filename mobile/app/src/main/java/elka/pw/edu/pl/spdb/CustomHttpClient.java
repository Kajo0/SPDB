package elka.pw.edu.pl.spdb;

import android.os.Build;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class CustomHttpClient {

    public static final int HTTP_TIMEOUT = 60 * 1000; // milliseconds
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private static final String PARAMETER_ENCONDING = "UTF-8";
    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_QUERY = '?';
    private static final char PARAMETER_EQUALS_CHAR = '=';

    static {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO)
            System.setProperty("http.keepAlive", "false");
    }

    public static String doPostRequest(final String address, final Map<String, Object> parameters) {
        String result = "";

        BufferedReader reader = null;
        try {
            URL url = new URL(address);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(HTTP_TIMEOUT);
            conn.setRequestMethod(HTTP_POST);
            conn.setRequestProperty(HTTP_CONTENT_TYPE, HTTP_APPLICATION_X_WWW_FORM_URLENCODED);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(createQueryStringForParameters(parameters));
            wr.flush();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null)
                sb.append(line + "");

            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore
            }
        }

        return result;
    }

    public static String doGetRequest(final String address, final Map<String, Object> parameters) {
        String result = "";

        BufferedReader reader = null;
        try {
            URL url = new URL(address + PARAMETER_QUERY + createQueryStringForParameters(parameters));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(HTTP_TIMEOUT);
            conn.setRequestMethod(HTTP_GET);

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null)
                sb.append(line + "");

            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // ignore
            }
        }

        return result;
    }

    private static String createQueryStringForParameters(final Map<String, Object> parameters)
            throws UnsupportedEncodingException {
        StringBuilder queryStringBuilder = new StringBuilder();

        if (parameters != null) {
            Iterator<Entry<String, Object>> it = parameters.entrySet().iterator();
            boolean first = true;

            while (it.hasNext()) {
                if (!first)
                    queryStringBuilder.append(PARAMETER_DELIMITER);
                else
                    first = false;

                Entry<String, Object> entry = it.next();

                queryStringBuilder.append(entry.getKey()).append(PARAMETER_EQUALS_CHAR)
                        .append(URLEncoder.encode("" + entry.getValue(), PARAMETER_ENCONDING));
            }
        }

        return queryStringBuilder.toString();
    }

    public static Map<String, Object> createParameterMap() {
        return new HashMap<String, Object>();
    }

}