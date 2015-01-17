package elka.pw.edu.pl.spdb;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

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
    private TextView timeTextView;
    private TextView distanceTextView;
    private TextView departureTextView;
    private TextView arrivalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        driveButton = (ToggleButton) findViewById(R.id.driveButton);
        transitButton = (ToggleButton) findViewById(R.id.transitButton);
        timeTextView = (TextView) findViewById(R.id.timeText);
        distanceTextView = (TextView) findViewById(R.id.distanceText);
        departureTextView = (TextView) findViewById(R.id.departureText);
        arrivalTextView = (TextView) findViewById(R.id.arrivalText);

        setUpMapIfNeeded();

        fromAddress = getIntent().getStringExtra(FROM_PARAM);
        toAddress = getIntent().getStringExtra(TO_PARAM);
        driveResponse = new Gson().fromJson(getIntent().getStringExtra(DRIVE_PARAM), ServerResponse.class);
        transitResponse = new Gson().fromJson(getIntent().getStringExtra(TRANSIT_PARAM), ServerResponse.class);

        List<Section> sections = driveResponse.getRoute().getParts();
        GeoBoundaries boundaries = new GeoBoundaries(sections.get(0).getPolyline().get(0));
        for (Section section : sections) {
            for (GeoPoint point : section.getPolyline()) {
                boundaries.update(point);
            }
        }
        driveBounds = boundaries.getLatLngBounds();

        sections = transitResponse.getRoute().getParts();
        boundaries = new GeoBoundaries(sections.get(0).getPolyline().get(0));
        for (Section section : sections) {
            for (GeoPoint point : section.getPolyline()) {
                boundaries.update(point);
            }
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
                    drawRoute(true);
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
     * @param driveResponse true - driveResponse, false - transitResponse
     */
    private void drawRoute(boolean driveResponse) {
        map.clear();
        int[] colors = new int[] {Color.BLUE, Color.MAGENTA, Color.GREEN, Color.RED};
        float[] markerColors = new float[] {BitmapDescriptorFactory.HUE_BLUE,
                BitmapDescriptorFactory.HUE_MAGENTA,
                BitmapDescriptorFactory.HUE_GREEN,
                BitmapDescriptorFactory.HUE_RED};

        ServerResponse response;
        if (driveResponse) {
            response = this.driveResponse;
        }
        else {
            response = transitResponse;
        }

        if (driveResponse) {
            PolylineOptions lineOptions = new PolylineOptions()
                    .width(5)
                    .color(colors[0]);
            List<GeoPoint> polyline = response.getRoute().getParts().get(0).getPolyline();
            for (GeoPoint point : polyline) {
                lineOptions.add(new LatLng(point.getLat(), point.getLng()));
            }
            map.addPolyline(lineOptions);
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(polyline.get(0).getLat(),
                            polyline.get(0).getLng()))
                    .title(fromAddress)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColors[0])));
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(polyline.get(polyline.size() - 1).getLat(),
                            polyline.get(polyline.size() - 1).getLng()))
                    .title(toAddress)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColors[0])));
        }
        else {
            int counter = 0;
            for (Section section : response.getRoute().getParts()) {
                PolylineOptions lineOptions = new PolylineOptions()
                        .width(5)
                        .color(colors[counter]);
                List<GeoPoint> polyline = section.getPolyline();
                for (GeoPoint point : polyline) {
                    lineOptions.add(new LatLng(point.getLat(), point.getLng()));
                }
                map.addPolyline(lineOptions);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(polyline.get(0).getLat(),
                        polyline.get(0).getLng()));
                if (counter == 0) {
                    markerOptions.title(fromAddress);
                }
                else {
                    markerOptions.title(getString(R.string.transfer));
                }
                markerOptions.snippet(section.getDescription());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(markerColors[counter]));
                map.addMarker(markerOptions);
                counter = (counter + 1) % colors.length;
            }
            //Add last marker
            List<Section> sections = response.getRoute().getParts();
            List<GeoPoint> polyline = sections.get(sections.size() - 1).getPolyline();
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(polyline.get(polyline.size() - 1).getLat(),
                            polyline.get(polyline.size() - 1).getLng()))
                    .title(toAddress)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColors[counter])));
        }

        CameraUpdate cameraUpdate;
        if (driveResponse) {
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(driveBounds, 50);
        }
        else {
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(transitBounds, 50);
        }
        map.animateCamera(cameraUpdate);

        int timeMinutes = (int)(response.getRoute().getTime() * 60);
        timeTextView.setText(String.valueOf(timeMinutes) + " " + getString(R.string.minutes));
        distanceTextView.setText(String.format("%.2f", response.getRoute().getLength()) + " " + getString(R.string.km));
        arrivalTextView.setText(response.getArrivalTime());
        departureTextView.setText(response.getDepartureTime());
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
                drawRoute(true);
                break;
            case R.id.transitButton:
                driveButton.setChecked(false);
                drawRoute(false);
                break;
        }
    }

}
