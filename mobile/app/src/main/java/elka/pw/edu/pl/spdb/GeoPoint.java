package elka.pw.edu.pl.spdb;

public class GeoPoint {

    private double lat;
    private double lng;

    public GeoPoint(double lat, double lng) {
        super();
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "GeoPoint [lat=" + lat + ", lng=" + lng + "]";
    }

}
