package pl.edu.pw.elka.spdb.servlet;

import java.io.IOException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.edu.pw.elka.spdb.common.Utils;
import pl.edu.pw.elka.spdb.route.RouteResponse;
import pl.edu.pw.elka.spdb.route.RouteResponse.Status;

/**
 * Servlet which serves routes.
 * 
 * @author Jan Zarzycki
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractRouteServlet extends HttpServlet {

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
     * Departure time parameter.
     */
    private static final String DEPARTURE_TIME_PARAM = "departure_time";
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String result = process(request);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().print(result);
    }

    private String process(HttpServletRequest request) {
        String origin = request.getParameter(ORIGIN_PARAM);
        String destination = request.getParameter(DESTINATION_PARAM);
        String arrivalTimeParam = request.getParameter(ARRIVAL_TIME_PARAM);
        String departureTimeParam = request.getParameter(DEPARTURE_TIME_PARAM);
        
        RouteResponse routeResponse = new RouteResponse();
        
        if (origin == null && destination == null) {
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("Invalid parameters.");
            return Utils.getGson().toJson(routeResponse);
        }

        if (arrivalTimeParam == null && departureTimeParam == null) {
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("Invalid parameters.");
            return Utils.getGson().toJson(routeResponse);
        }
        
        if (arrivalTimeParam != null && departureTimeParam != null) {
            routeResponse.setStatus(Status.ERROR);
            routeResponse.setDescription("Invalid parameters.");
            return Utils.getGson().toJson(routeResponse);
        }
        
        Timestamp departureTime = null;
        Timestamp arrivalTime = null;
        if (departureTimeParam != null) {
            departureTime = new Timestamp(Long.valueOf(departureTimeParam));
        } else {
            arrivalTime = new Timestamp(Long.valueOf(arrivalTimeParam));
        }
        
        return Utils.getGson().toJson(getRouteResponse(origin, destination, departureTime, arrivalTime));
    }
    
    /**
     * Finds route from origin to destination.
     * 
     * DepartureTime or ArrivalTime must be null.
     * 
     * @param origin
     * @param destination
     * @param departureTime
     * @param arrivalTime
     * @return
     */
    public abstract RouteResponse getRouteResponse(String origin, String destination,
            Timestamp departureTime, Timestamp arrivalTime);

}
