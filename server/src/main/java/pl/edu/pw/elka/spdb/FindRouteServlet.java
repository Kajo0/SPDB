package pl.edu.pw.elka.spdb;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.google.gson.Gson;

/**
 * Servlet which serves routes.
 * 
 * @author Jan Zarzycki
 *
 */
@SuppressWarnings("serial")
public class FindRouteServlet extends HttpServlet {

    /**
     * Origin parameter.
     */
    private static final String ORIGIN_PARAM = "origin";
    /**
     * Destination parameter.
     */
    private static final String DESTINATION_PARAM = "destination";
    /**
     * Arrival time parameter.
     */
    private static final String ARRIVAL_TIME_PARAM = "arrival_time";
    
    /**
     * Google api url.
     */
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    
    
    // database helper object
    private final DatabaseHelper databaseHelper = new DatabaseHelper();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String result = process(request);
        response.setContentType("application/json");
        response.getWriter().print(result);
    }

    private String process(HttpServletRequest request) {
        String origin = request.getParameter(ORIGIN_PARAM);
        String destination = request.getParameter(DESTINATION_PARAM);

        if (origin == null && destination == null) {
            return "error";
        }

        String arrivalTimeParam = request.getParameter(ARRIVAL_TIME_PARAM);
        Timestamp arrivalTime = arrivalTimeParam != null ? new Timestamp(
                Long.valueOf(arrivalTimeParam)) : null;

        return new Gson().toJson(null);
    }
    
    public List<GeoPoint> getRoute(String origin, String destination) {
        // TODO api google pobieranie lat lng origin i dst
//        GeoPoint originGeo = getGeoPoint(origin);
//        GeoPoint destGeo = getGeoPoint(destination);
//        
        return null;
    }
    
    public GeoPoint getGeoPoint(String address) {
        try {
            URL url = Utils.createUrl(GEOCODE_URL, Collections.singletonMap("address", address));
            URLConnection connection = url.openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            String response = IOUtils.toString(connection.getInputStream());
            JSONObject jsonObject = new JSONObject(response);
            JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
            JSONObject geometry = result.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            return new GeoPoint(location.getDouble("lat"), location.getDouble("lng"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

}
