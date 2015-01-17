package pl.edu.pw.elka.spdb.route;

import java.util.List;

/**
 * Object with route data.
 * 
 * @author Jan Zarzycki
 *
 */
public class RoutePart {
    /**
     * Polyline with route points.
     */
    private List<GeoPoint> polyline;
    /**
     * Route length in km.
     */
    private Double length;
    /**
     * Travel time in hours.
     */
    private Double time;
    /**
     * Route description.
     */
    private String description;
    
    public List<GeoPoint> getPolyline() {
        return polyline;
    }
    public void setPolyline(List<GeoPoint> polyline) {
        this.polyline = polyline;
    }
    public Double getLength() {
        return length;
    }
    public void setLength(Double length) {
        this.length = length;
    }
    public Double getTime() {
        return time;
    }
    public void setTime(Double time) {
        this.time = time;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
