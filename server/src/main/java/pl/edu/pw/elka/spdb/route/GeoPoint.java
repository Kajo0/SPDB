package pl.edu.pw.elka.spdb.route;

public class GeoPoint {

    private final static double AVERAGE_RADIUS_OF_EARTH = 6371;

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

    public double distance(GeoPoint other) {
        
        double latDistance = Math.toRadians(lat - other.lat);
        double lngDistance = Math.toRadians(lng - other.lng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat))
                * Math.cos(Math.toRadians(other.lat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = AVERAGE_RADIUS_OF_EARTH * c;
        if (Double.isNaN(dist)) {
            return 0.0;
        }
        
        return dist;
    }

}
