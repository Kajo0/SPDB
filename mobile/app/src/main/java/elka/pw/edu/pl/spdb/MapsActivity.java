package elka.pw.edu.pl.spdb;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String EXTRA_PARAM = "extra_param";

    private static final int INIT_ZOOM = 14;

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private List<GeoPoint> route = Lists.newArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        route.clear();
        ServerResponse serverResponse = new Gson().fromJson(getIntent().getStringExtra(EXTRA_PARAM), ServerResponse.class);
        if (serverResponse != null) {
            route.addAll(serverResponse.getRoute().getPolyline());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        drawRoute();
    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (map != null) {
                setUpMap();
                buildGoogleApiClient();
                googleApiClient.connect();
            }
        }
    }

    private void setUpMap() {
//        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void drawRoute() {
        PolylineOptions lineOptions = new PolylineOptions()
                .width(5)
                .color(Color.RED);
        for (GeoPoint point : route) {
            lineOptions.add(new LatLng(point.getLat(), point.getLng()));
        }
        map.addPolyline(lineOptions);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        map.setMyLocationEnabled(true);

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        // To avoid timing with google play service
        if (location == null)
            return;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, INIT_ZOOM);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.error_unknown), Toast.LENGTH_LONG).show();

        finish();
    }

}
