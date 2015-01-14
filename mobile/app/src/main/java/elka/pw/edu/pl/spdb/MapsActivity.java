package elka.pw.edu.pl.spdb;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.internal.ge;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String DRIVE_PARAM = "drive_param";
    public static final String TRANSIT_PARAM = "transit_param";
    public static final String FROM_PARAM = "from_param";
    public static final String TO_PARAM = "to_param";

    private static final int INIT_ZOOM = 14;

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private ToggleButton driveButton;
    private ToggleButton transitButton;
    private ServerResponse driveResponse;
    private ServerResponse transitResponse;
    private String fromAddress;
    private String toAddress;
    private LatLngBounds driveBounds;
    private LatLngBounds transitBounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        driveButton = (ToggleButton) findViewById(R.id.driveButton);
        transitButton = (ToggleButton) findViewById(R.id.transitButton);

        setUpMapIfNeeded();

        fromAddress = getIntent().getStringExtra(FROM_PARAM);
        toAddress = getIntent().getStringExtra(TO_PARAM);
        driveResponse = new Gson().fromJson(getIntent().getStringExtra(DRIVE_PARAM), ServerResponse.class);
        transitResponse = new Gson().fromJson(getIntent().getStringExtra(TRANSIT_PARAM), ServerResponse.class);

        GeoBoundaries boundaries = new GeoBoundaries(driveResponse.getRoute().getPolyline().get(0));
        for (GeoPoint point : driveResponse.getRoute().getPolyline()) {
            boundaries.update(point);
        }
        driveBounds = boundaries.getLatLngBounds();

        boundaries = new GeoBoundaries(transitResponse.getRoute().getPolyline().get(0));
        for (GeoPoint point : transitResponse.getRoute().getPolyline()) {
            boundaries.update(point);
        }
        transitBounds = boundaries.getLatLngBounds();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(driveBounds, 50));
                    drawRoute(driveResponse, true);
                }
            });
            if (map != null) {
                buildGoogleApiClient();
                googleApiClient.connect();
            }
        }
    }

    /**
     * Refreshes map and draws route
     *
     * @param response response from server
     * @param type true - driveResponse, false - transitResponse
     */
    private void drawRoute(ServerResponse response, boolean type) {
        map.clear();
        int color;
        if (type) {
            color = Color.BLUE;
        }
        else {
            color = Color.CYAN;
        }
        PolylineOptions lineOptions = new PolylineOptions()
                .width(5)
                .color(color);
        List<GeoPoint> polyline = response.getRoute().getPolyline();
        for (GeoPoint point : polyline) {
            lineOptions.add(new LatLng(point.getLat(), point.getLng()));
        }
        map.addPolyline(lineOptions);

        map.addMarker(new MarkerOptions()
                .position(new LatLng(polyline.get(0).getLat(),
                        polyline.get(0).getLng()))
                .title(fromAddress));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(polyline.get(polyline.size() - 1).getLat(),
                        polyline.get(polyline.size() - 1).getLng()))
                .title(toAddress));

        CameraUpdate cameraUpdate;
        if (type) {
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(driveBounds, 50);
        }
        else {
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(transitBounds, 50);
        }
        map.animateCamera(cameraUpdate);

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

        /*LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, INIT_ZOOM);
        map.animateCamera(cameraUpdate);*/
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

    public void onToggleClicked(View view) {
        if (!driveButton.isChecked() && !transitButton.isChecked()) {
            if (view.getId() == R.id.driveButton) {
                driveButton.setChecked(true);
            }
            else {
                transitButton.setChecked(true);
            }
            return ;
        }
        switch (view.getId()) {
            case R.id.driveButton:
                transitButton.setChecked(false);
                drawRoute(driveResponse, true);
                break;
            case R.id.transitButton:
                driveButton.setChecked(false);
                drawRoute(transitResponse, false);
                break;
        }
    }

}
