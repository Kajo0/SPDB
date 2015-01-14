package elka.pw.edu.pl.spdb;

import java.util.List;

/**
 * Created by Maciej on 2015-01-14.
 */
public class Route {
    private final List<GeoPoint> polyline;
    private final double lenght;
    private final double time;

    public Route(double lenght, List<GeoPoint> polyline, double time) {
        this.lenght = lenght;
        this.polyline = polyline;
        this.time = time;
    }

    public List<GeoPoint> getPolyline() {
        return polyline;
    }

    public double getLenght() {
        return lenght;
    }

    public double getTime() {
        return time;
    }
}
