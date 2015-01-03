package pl.edu.pw.elka.spdb.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.pw.elka.spdb.common.DatabaseHelper;
import pl.edu.pw.elka.spdb.common.Utils;
import pl.edu.pw.elka.spdb.route.GeoPoint;
import pl.edu.pw.elka.spdb.route.Route;
import pl.edu.pw.elka.spdb.route.RouteResponse;
import pl.edu.pw.elka.spdb.route.RouteResponse.Status;

import com.google.gson.Gson;

/**
 * Servlet which serves driving routes.
 * 
 * @author Jan Zarzycki
 *
 */
@SuppressWarnings("serial")
public class DrivingRouteServlet extends HttpServlet {

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

        RouteResponse routeResponse = new RouteResponse();
        
        if (origin == null && destination == null) {
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("Invalid parameters.");
            return new Gson().toJson(routeResponse);
        }

        String arrivalTimeParam = request.getParameter(ARRIVAL_TIME_PARAM);
        Timestamp arrivalTime = arrivalTimeParam != null ? new Timestamp(
                Long.valueOf(arrivalTimeParam)) : null;

        try {
            Route route = getRoute(origin, destination);
            routeResponse.setStatus(Status.OK);
            routeResponse.setRoute(route);
            if (arrivalTime != null) {
                routeResponse.setArrivalTime(arrivalTime);
                
                Timestamp departureTime = new Timestamp(arrivalTime.getTime() - (long)(route.getTime()*3600*1000));
                routeResponse.setDepartureTime(departureTime);
            } else {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"));
                Timestamp departureTime = new Timestamp(calendar.getTimeInMillis());
                routeResponse.setDepartureTime(departureTime);
                
                arrivalTime = new Timestamp(departureTime.getTime() + (long)(route.getTime()*3600*1000));
                routeResponse.setArrivalTime(arrivalTime);
            }
        } catch (SQLException e) {
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("SQLException");
        } catch (IOException | JSONException e) {
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("Error while getting lat,lng given location.");
        }
        return new Gson().toJson(routeResponse);
    }
    
    /**
     * Returns route from origin to destination.
     * 
     * @param origin
     * @param destination
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public Route getRoute(String origin, String destination) throws SQLException, IOException {
        GeoPoint originGeo = getGeoPoint(origin);
        GeoPoint destGeo = getGeoPoint(destination);
        
        return databaseHelper.findRoute(originGeo, destGeo);
    }
    
    /**
     * Returns given addres location (lat,lng), using google geocoding api.
     * 
     * @param address
     * @return
     * @throws IOException 
     */
    public GeoPoint getGeoPoint(String address) throws IOException {
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
    }

}
