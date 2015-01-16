package pl.edu.pw.elka.spdb.servlet;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.pw.elka.spdb.common.DatabaseHelper;
import pl.edu.pw.elka.spdb.common.Utils;
import pl.edu.pw.elka.spdb.route.GeoPoint;
import pl.edu.pw.elka.spdb.route.Route;
import pl.edu.pw.elka.spdb.route.RouteResponse;
import pl.edu.pw.elka.spdb.route.RouteResponse.Status;

/**
 * Servlet which serves driving routes.
 * 
 * @author Jan Zarzycki
 *
 */
@SuppressWarnings("serial")
public class DrivingRouteServlet extends AbstractRouteServlet {

    /**
     * Google api url.
     */
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(DrivingRouteServlet.class);
    
    // database helper object
    private final DatabaseHelper databaseHelper = new DatabaseHelper();

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
        LOG.debug("Google geocode request url: " + url.toString());
        URLConnection connection = url.openConnection();
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(5000);
        String response = IOUtils.toString(connection.getInputStream());
        JSONObject jsonObject = new JSONObject(response);
        JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
        JSONObject geometry = result.getJSONObject("geometry");
        JSONObject location = geometry.getJSONObject("location");
        
        double lat = location.getDouble("lat");
        double lng = location.getDouble("lng");
        LOG.debug("Geocode response lat " + lat +" lng " + lng);
        return new GeoPoint(lat, lng);
    }

    @Override
    public RouteResponse getRouteResponse(String origin, String destination,
            Timestamp departureTime, Timestamp arrivalTime) {
        RouteResponse routeResponse = new RouteResponse();
        try {
            Route route = getRoute(origin, destination);
            routeResponse.setStatus(Status.OK);
            routeResponse.setRoute(route);
            if (arrivalTime != null) {
                routeResponse.setArrivalTime(arrivalTime);
                Timestamp departureTs = new Timestamp(arrivalTime.getTime() - (long)(route.getTime()*3600*1000));
                routeResponse.setDepartureTime(departureTs);
            } else {
                routeResponse.setDepartureTime(departureTime);
                Timestamp arrivalTs = new Timestamp(departureTime.getTime() + (long)(route.getTime()*3600*1000));
                routeResponse.setArrivalTime(arrivalTs);
            }
        } catch (SQLException e) {
            LOG.error(e);
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("SQLException");
        } catch (IOException | JSONException e) {
            LOG.error(e);
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("Error while getting lat,lng given location.");
        }
        
        return routeResponse;
    }

}
