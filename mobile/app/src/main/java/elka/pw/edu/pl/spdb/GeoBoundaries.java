package elka.pw.edu.pl.spdb;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Maciej on 2015-01-14.
 */
public class GeoBoundaries {
    private double minLat;
    private double maxLat;
    private double minLng;
    private double maxLng;

    public GeoBoundaries(GeoPoint point) {
        minLat = maxLat = point.getLat();
        minLng = maxLng = point.getLng();
    }

    public void update(GeoPoint point) {
        minLat = Math.min(minLat, point.getLat());
        maxLat = Math.max(maxLat, point.getLat());
        minLng = Math.min(minLng, point.getLng());
        maxLng = Math.max(maxLng, point.getLng());
    }

    public LatLngBounds getLatLngBounds() {
        return new LatLngBounds(new LatLng(minLat,minLng),
                new LatLng(maxLat, maxLng));
    }
}
