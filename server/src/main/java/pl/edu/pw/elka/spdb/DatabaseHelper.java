package pl.edu.pw.elka.spdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Database helper object.
 * 
 * @author Jan Zarzycki
 */
public class DatabaseHelper {
    
    /**
     * Database parameters
     */
    private static final String USER = "spdb_routing";
    private static final String PASSWORD = "spdb_routing";
    private static final String DB_NAME = "spdb_routing_warsaw";
    private static final String DB_IP = "mmarkiew.no-ip.biz";
    private static final int DB_PORT = 8888;
    
    /**
     * Forbidden ways classes:
     * 119 - footway
     * 118 - cycleway
     * 114 - pedestrian
     */
    private static final String FORBIDDEN_WAYS_CLASS_IDS = "119, 118, 114";
    /**
     * SQL query which finds source which is the nearest neighbor of given (lat,lng)
     * arg1 - lat
     * arg2 - lng
     */
    private static final String FIND_NEAREST_SOURCE_SQL = "SELECT source from WAYS where class_id not in (" + FORBIDDEN_WAYS_CLASS_IDS +") order by st_distance(st_makepoint(?,?), st_makepoint(y1,x1)) limit 1;";
    /**
     * SQL query which finds route between two points.
     * arg1 - origin source
     * arg2 - destination source
     */
    private static final String FIND_ROUTE_SQL = "SELECT y1 as lat, x1 as lng, cost / maxspeed_forward as time, cost as length FROM pgr_astar('"+
                "SELECT gid AS id,"+
                         "source::integer,"+
                         "target::integer,"+
                         "length::double precision AS cost,"+
                         "x1, y1, x2, y2, reverse_cost "+
                        "FROM ways WHERE class_id not in (" + FORBIDDEN_WAYS_CLASS_IDS + ")',"+
                "?, ?, true, true) res join ways w on res.id2 = w.gid;";
    
    /**
     * Returns connection to database
     * 
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        Connection connection = null;
        connection = DriverManager.getConnection(
                    String.format("jdbc:postgresql://%s:%d/%s",DB_IP, DB_PORT, DB_NAME)
                    ,USER,PASSWORD);
        connection.setAutoCommit(true);
        return connection;
    }
    
    /**
     * Returns node id which is nearest to given point.
     * 
     * @param point
     * @return
     * @throws SQLException
     */
    public Integer getNearestSourceId(GeoPoint point) throws SQLException {
        try (Connection connection = getConnection()){
            PreparedStatement prepareStatement = connection.prepareStatement(FIND_NEAREST_SOURCE_SQL);
            prepareStatement.setDouble(1, point.getLat());
            prepareStatement.setDouble(2, point.getLng());
            
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("source");
                }
                return null;
            }
        }
    }
    
    /**
     * Finds route between origin and destination.
     * 
     * @param origin
     * @param destination
     * @return
     * @throws SQLException
     */
    public Route findRoute(GeoPoint origin, GeoPoint destination) throws SQLException {
        Integer originSourceId = getNearestSourceId(origin);
        Integer destinationSourceId = getNearestSourceId(destination);
        
        try (Connection connection = getConnection()) {
            PreparedStatement prepareStatement = connection.prepareStatement(FIND_ROUTE_SQL);
            prepareStatement.setInt(1, originSourceId);
            prepareStatement.setInt(2, destinationSourceId);
            double length = 0.0;
            double time = 0.0;
            List<GeoPoint> polyline = Lists.newArrayList();
            try (ResultSet resultSet = prepareStatement.executeQuery()) {
                while (resultSet.next()) {
                    GeoPoint geoPoint = new GeoPoint(resultSet.getDouble("lat"), resultSet.getDouble("lng"));
                    polyline.add(geoPoint);
                    length += resultSet.getDouble("length");
                    time += resultSet.getDouble("time");
                }
            }
            Route route = new Route();
            route.setPolyline(polyline);
            route.setLength(length);
            route.setTime(time);
            
            return route;
        }
    }
    
    public static void main(String[] args) {
        DatabaseHelper helper = new DatabaseHelper();
        
        FindRouteServlet findRouteServlet = new FindRouteServlet();
        
        try {
            GeoPoint politechnika = findRouteServlet.getGeoPoint("Politechnika,Warszawa");
            GeoPoint dom = findRouteServlet.getGeoPoint("Polnego wiatru 24,Warszawa");
            System.out.println("Dom:\t" + dom.toString());
            System.out.println("Politechnika:\t" + politechnika.toString());
            
            
            System.out.println("Dom:\t" + helper.getNearestSourceId(dom));
            System.out.println("Politechnika:\t" + helper.getNearestSourceId(politechnika));
            
            Route route = helper.findRoute(dom, politechnika);
            System.out.println(Arrays.toString(route.getPolyline().toArray()));
            
            System.out.println("Distance= "+route.getLength()+"km");
            System.out.println("Time= " + route.getTime());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
