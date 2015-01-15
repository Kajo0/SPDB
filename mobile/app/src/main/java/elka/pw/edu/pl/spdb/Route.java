package elka.pw.edu.pl.spdb;

import java.util.List;

/**
 * Created by Maciej on 2015-01-14.
 */
public class Route {
    private final List<GeoPoint> polyline;
    private final double length;
    private final double time;

    public Route(double length, List<GeoPoint> polyline, double time) {
        this.length = length;
        this.polyline = polyline;
        this.time = time;
    }

    public List<GeoPoint> getPolyline() {
        return polyline;
    }

    public double getLength() {
        return length;
    }

    public double getTime() {
        return time;
    }
}
