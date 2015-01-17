package elka.pw.edu.pl.spdb;

import java.util.List;

/**
 * Created by Maciej on 2015-01-14.
 */
public class Route {
    private final List<Section> parts;
    private final double length;
    private final double time;

    public Route(List<Section> parts, double length, double time) {
        this.parts = parts;
        this.length = length;
        this.time = time;
    }

    public List<Section> getParts() {
        return parts;
    }

    public double getLength() {
        return length;
    }

    public double getTime() {
        return time;
    }
}
