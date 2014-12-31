package pl.edu.pw.elka.spdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class DatabaseHelper {
    
    private static final String USER = "spdb_routing";
    private static final String PASSWORD = "spdb_routing";
    private static final String DB_NAME = "spdb_routing_warsaw";
    private static final String DB_IP = "mmarkiew.no-ip.biz";
    private static final int DB_PORT = 8888;
    
    private static final String FIND_SOURCE_LAT_LNG_SQL = "SELECT y1, x1 from WAYS where source = ?";
    
    private static final String FIND_NEAREST_SOURCE_SQL = "SELECT source from WAYS order by st_distance(st_makepoint(?,?), st_makepoint(y1,x1)) limit 1;";
    
    private static final String FIND_ROUTE_STATEMENT = "SELECT seq, id1 AS source_id, y1 as lat, x1 as lng FROM pgr_astar('"+
                "SELECT gid AS id,"+
                         "source::integer,"+
                         "target::integer,"+
                         "length::double precision AS cost,"+
                         "x1, y1, x2, y2 "+
                        "FROM ways',"+
                "?, ?, false, false) res join ways w on res.id1 = w.source;";
    
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
        Connection connection = getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement(FIND_NEAREST_SOURCE_SQL);
        prepareStatement.setDouble(1, point.getLat());
        prepareStatement.setDouble(2, point.getLng());
        
        ResultSet resultSet = prepareStatement.executeQuery();
        Integer id = null;
        if (resultSet.next()) {
            id = resultSet.getInt("source");
        }
        
        connection.close();
        return id;
    }
    
    /**
     * Returns location of source with given id.
     * 
     * @param sourceId
     * @return
     * @throws SQLException
     */
    public GeoPoint getSourceGeoPoint(Integer sourceId) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement(FIND_SOURCE_LAT_LNG_SQL);
        prepareStatement.setLong(1, sourceId);
        
        ResultSet resultSet = prepareStatement.executeQuery();
        GeoPoint result = null;
        if (resultSet.next()) {
            result = new GeoPoint(resultSet.getDouble("y1"), resultSet.getDouble("x1"));
        }
        connection.close();
        
        return result;
    }
    
    /**
     * Finds route between origin and destination.
     * 
     * @param origin
     * @param destination
     * @return
     * @throws SQLException
     */
    public List<GeoPoint> findRoute(GeoPoint origin, GeoPoint destination) throws SQLException {
        Integer originSourceId = getNearestSourceId(origin);
        Integer destinationSourceId = getNearestSourceId(destination);
        
        Connection connection = getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement(FIND_ROUTE_STATEMENT);
        prepareStatement.setInt(1, originSourceId);
        prepareStatement.setInt(2, destinationSourceId);
        ResultSet resultSet = prepareStatement.executeQuery();
        
        List<GeoPoint> route = Lists.newArrayList();
        while (resultSet.next()) {
            GeoPoint geoPoint = new GeoPoint(resultSet.getDouble("lat"), resultSet.getDouble("lng"));
            route.add(geoPoint);
        }
        connection.close();

        return route;
    }
    
    public static void main(String[] args) {
        DatabaseHelper helper = new DatabaseHelper();
        
        FindRouteServlet findRouteServlet = new FindRouteServlet();
        GeoPoint politechnika = findRouteServlet.getGeoPoint("Politechnika,Warszawa");
        GeoPoint dom = findRouteServlet.getGeoPoint("Polnego wiatru 24,Warszawa");
        System.out.println("Dom:\t" + dom.toString());
        System.out.println("Politechnika:\t" + politechnika.toString());
        
        try {
            System.out.println("Dom:\t" + helper.getNearestSourceId(dom));
            System.out.println("Politechnika:\t" + helper.getNearestSourceId(politechnika));
            
            System.out.println(Arrays.toString(helper.findRoute(dom, politechnika).toArray()));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
