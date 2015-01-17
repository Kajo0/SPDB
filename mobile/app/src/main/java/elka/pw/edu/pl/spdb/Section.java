package elka.pw.edu.pl.spdb;

import java.util.List;

/**
 * Created by Maciej on 2015-01-17.
 */
public class Section {
    private final List<GeoPoint> polyline;
    private final double length;
    private final double time;
    private final String description;

    public Section(List<GeoPoint> polyline, double length, double time, String description) {
        this.polyline = polyline;
        this.length = length;
        this.time = time;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}
