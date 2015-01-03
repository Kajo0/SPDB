package pl.edu.pw.elka.spdb.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.pw.elka.spdb.common.Utils;
import pl.edu.pw.elka.spdb.route.Route;
import pl.edu.pw.elka.spdb.route.RouteResponse;
import pl.edu.pw.elka.spdb.route.RouteResponse.Status;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

/**
 * Servlet which serves public transit routes.
 * 
 * @author Jan Zarzycki
 *
 */
@SuppressWarnings("serial")
public class TransitRouteServlet extends HttpServlet {

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
	 * 
	 */
    private static final String GOOGLE_TRANSIT_URL = "https://maps.googleapis.com/maps/api/directions/json";

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

        RouteResponse transitResponse = null;
        try {
            String routeJson = getGoogleTransitRouteJson(origin, destination, arrivalTime);
            transitResponse = getTransitResponse(routeJson);
        } catch (IOException | JSONException e) {
            transitResponse = new RouteResponse();
            transitResponse.setStatus(Status.ERROR);
            transitResponse.setDescription("Find transit error");
        }
        
        return new Gson().toJson(transitResponse);
    }

    /**
     * Returns json string with route from origin to destination using public
     * transport. If arrivalTime is null then departureTime is set to actual
     * time.
     * 
     * @param origin
     * @param destination
     * @param arrivalTime
     * @return
     * @throws IOException 
     */
    private String getGoogleTransitRouteJson(String origin, String destination,
        Timestamp arrivalTime) throws IOException {
        Map<String, String> params = Maps.newHashMap();
        params.put("origin", origin);
        params.put("destination", destination);
        if (arrivalTime == null) {
            Calendar calendar = Calendar.getInstance(TimeZone
                    .getTimeZone("Europe/Warsaw"));
            // departure_time is in seconds
            params.put("departure_time",
                    String.valueOf(calendar.getTimeInMillis() / 1000));
        } else {
            params.put("arrival_time",
                    String.valueOf(arrivalTime.getTime() / 1000));
        }
        params.put("mode", "transit");
        params.put("region", "pl");
        params.put("language", "pl");
        URL url = Utils.createUrl(GOOGLE_TRANSIT_URL, params);

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
        routeResponse.setArrivalTime(new Timestamp(firstLeg.getJSONObject("arrival_time").getLong("value")*1000));
        routeResponse.setDepartureTime(new Timestamp(firstLeg.getJSONObject("departure_time").getLong("value")*1000));
        routeResponse.setStatus(Status.OK);
        
        return routeResponse;
    }
    
    public static void main(String[] args) {
        TransitRouteServlet servlet = new TransitRouteServlet();
        try {
            String routeJson = servlet.getGoogleTransitRouteJson("Polnego wiatru 24, Warszawa", "Politechnika, Warszawa", null);
            System.out.println(servlet.getTransitResponse(routeJson));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
