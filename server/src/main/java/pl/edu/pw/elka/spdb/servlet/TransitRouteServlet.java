package pl.edu.pw.elka.spdb.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.pw.elka.spdb.common.Utils;
import pl.edu.pw.elka.spdb.route.Route;
import pl.edu.pw.elka.spdb.route.RouteResponse;
import pl.edu.pw.elka.spdb.route.RouteResponse.Status;

import com.google.common.collect.Maps;

/**
 * Servlet which serves public transit routes.
 * 
 * @author Jan Zarzycki
 *
 */
@SuppressWarnings("serial")
public class TransitRouteServlet extends AbstractRouteServlet {
    /**
	 * Google transit API URL
	 */
    private static final String GOOGLE_TRANSIT_URL = "https://maps.googleapis.com/maps/api/directions/json";
    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(TransitRouteServlet.class);
    
    
    /**
     * Returns json string with route from origin to destination using public
     * transport. 
     * time.
     * 
     * @param origin
     * @param destination
     * @param departureTime
     * @param arrivalTime
     * @return
     * @throws IOException 
     */
    private String getGoogleTransitRouteJson(String origin, String destination,
        Timestamp departureTime, Timestamp arrivalTime) throws IOException {
        Map<String, String> params = Maps.newHashMap();
        params.put("origin", origin);
        params.put("destination", destination);
        if (arrivalTime == null) {
            // departure_time is in seconds
            params.put("departure_time",
                    String.valueOf(departureTime.getTime() / 1000));
        } else {
            // arrival_time is in seconds
            params.put("arrival_time",
                    String.valueOf(arrivalTime.getTime() / 1000));
        }
        params.put("mode", "transit");
        params.put("region", "pl");
        params.put("language", "pl");
        URL url = Utils.createUrl(GOOGLE_TRANSIT_URL, params);
        LOG.debug("Google transit request url: " + url.toString());

        URLConnection connection = url.openConnection();
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(5000);
        String response = IOUtils.toString(connection.getInputStream());

        return response;
    }
    
    /**
     * Returns RouteResponse object from routeJson.
     * 
     * @param routeJson
     * @return
     */
    private RouteResponse getTransitResponse(String routeJson) {
        JSONObject jsonObject = new JSONObject(routeJson);
        JSONObject firstRoute = jsonObject.getJSONArray("routes").getJSONObject(0);
        
        JSONObject firstLeg = firstRoute.getJSONArray("legs").getJSONObject(0);
        JSONObject overviewPolyline = firstRoute.getJSONObject("overview_polyline");
        
        RouteResponse routeResponse = new RouteResponse();
        Route route = new Route();
        route.setPolyline(Utils.decodePoly(overviewPolyline.getString("points")));
        route.setLength(firstLeg.getJSONObject("distance").getInt("value")/1000.0);
        route.setTime(firstLeg.getJSONObject("duration").getInt("value")/3600.0);
        routeResponse.setRoute(route);
        routeResponse.setDescription(getTransitDescription(firstLeg.getJSONArray("steps")));
        routeResponse.setArrivalTime(new Timestamp(firstLeg.getJSONObject("arrival_time").getLong("value")*1000));
        routeResponse.setDepartureTime(new Timestamp(firstLeg.getJSONObject("departure_time").getLong("value")*1000));
        routeResponse.setStatus(Status.OK);
        
        return routeResponse;
    }
    
    private String getTransitDescription(JSONArray steps) {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i< steps.length(); i++) {
            JSONObject step = steps.getJSONObject(i);
            builder.append(step.getString("html_instructions"));
            if (step.has("transit_details")) {
                JSONObject transitDetails = step.getJSONObject("transit_details");
                String arrivalStopName = transitDetails.getJSONObject("arrival_stop").getString("name");
                String arrivalTime = transitDetails.getJSONObject("arrival_time").getString("text");
                String departureStopName = transitDetails.getJSONObject("departure_stop").getString("name");
                String departureTime = transitDetails.getJSONObject("departure_time").getString("text");
                String line = transitDetails.getJSONObject("line").getString("short_name");
                
                builder.append(String.format(": linia %s z przystanku %s o godzinie %s do przystanku %s o godzinie %s",
                        line,
                        departureStopName, departureTime,
                        arrivalStopName, arrivalTime));
            }
            
            builder.append('\n');
        }
        
        return builder.toString();
    }

    @Override
    public RouteResponse getRouteResponse(String origin, String destination,
            Timestamp departureTime, Timestamp arrivalTime) {
        RouteResponse transitResponse = new RouteResponse();
        try {
            String routeJson = getGoogleTransitRouteJson(origin, destination, departureTime, arrivalTime);
            transitResponse = getTransitResponse(routeJson);
        } catch (IOException | JSONException e) {
            LOG.error(e);
            transitResponse = new RouteResponse();
            transitResponse.setStatus(Status.ERROR);
            transitResponse.setDescription("Find transit error");
        }
        return transitResponse;
    }

}
