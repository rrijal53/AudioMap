package com.mantraideas.audiotour;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleMap mMap;
    List<Landmarks> list;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    TextView accuracy;
    LocationRequest mLocationRequest;
    Marker mPositionMarker;
    PolylineOptions op;
    ListView listView;
    ImageView add;
    ArrayAdapter<Landmarks> adapter;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (!checkPermission(this)) {
            showPermissionDialog();
        }
        db = new DatabaseHelper(this);
        listView = (ListView) findViewById(R.id.list);
        add = (ImageView) findViewById(R.id.add);
        add.setOnClickListener(this);
        accuracy = (TextView) findViewById(R.id.accuracy);
        list = new ArrayList<>();
        list.addAll(db.getLandmarks());
        adapter = new ArrayAdapter<Landmarks>(this, android.R.layout.simple_list_item_1, android.R.id.text1, list) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
               TextView v = (TextView) super.getView(position, convertView, parent);
                v.setText(list.get(position).title +"");
                return v;
            }
        };
        listView.setAdapter(adapter);
        op = new PolylineOptions();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        mLocationRequest = createLocationRequest();
    }

    public boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }


    public void showPermissionDialog() {
        if (!checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    112);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        for (int i = 0; i < list.size(); i++) {
            op.add(new LatLng(list.get(i).geo_lat, list.get(i).geo_lng));
        }
        op.width(10);
        op.color(Color.BLUE);
        for (int i = 0; i < list.size(); i++) {
            MarkerOptions o = new MarkerOptions().title((i + 1) + "").position(new LatLng(list.get(i).geo_lat, list.get(i).geo_lng));
            o.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            Marker m = mMap.addMarker(o);
            m.setTitle("Title");
            m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(list.get(i).geo_lat, list.get(i).geo_lng), 18));
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog();
        }

        mMap.addPolyline(op);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }


    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            status.startResolutionForResult(
                                    MapsActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

        return locationRequest;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                buildGoogleApiClient();
                if (mGoogleApiClient.isConnecting()) {
                }
            }
        }

    }

    private void startLocationUpdates() {
        if (mGoogleApiClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showPermissionDialog();
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            mLastLocation = location;

                            if (location == null)
                                return;
                            accuracy.setText("Location Accuracy : " + location.getAccuracy() + "M");
                            Log.d("ROWSUN", "onLocationChanged: :" + location.getLatitude() + " " + location.getLongitude());
                            if (mPositionMarker == null) {

                                mPositionMarker = mMap.addMarker(new MarkerOptions()
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_arrow))
                                        .anchor(0.5f, 0.5f)
                                        .position(
                                                new LatLng(location.getLatitude(), location
                                                        .getLongitude())));
                            }

                            animateMarker(mPositionMarker, location);

                            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location
                                    .getLatitude(), location.getLongitude())));

                        }
                    });
        } else {
            buildGoogleApiClient();
        }
    }


    public void animateMarker(final Marker marker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final double startRotation = marker.getRotation();
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                double lng = t * location.getLongitude() + (1 - t)
                        * startLatLng.longitude;
                double lat = t * location.getLatitude() + (1 - t)
                        * startLatLng.latitude;

                float rotation = (float) (t * location.getBearing() + (1 - t)
                        * startRotation);

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(rotation);

                if (t < 1.0) {
                    handler.postDelayed(this, 100);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog();
            return;
        }
        startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add) {
            showAlert();
        }
    }

    private void showAlert() {
        if (mLastLocation != null) {
            View v_alert = LayoutInflater.from(this).inflate(R.layout.alert_input, null);
            final EditText t = (EditText) v_alert.findViewById(R.id.title);
            final EditText t1 = (EditText) v_alert.findViewById(R.id.lat);
            final EditText t2 = (EditText) v_alert.findViewById(R.id.lng);
            t1.setText(mLastLocation.getLatitude() + "");
            t2.setText(mLastLocation.getLongitude() + "");
            new AlertDialog.Builder(this).setTitle("Add Landmark:").setView(v_alert).setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String title = t.getText().toString();
                    Double lat = Double.parseDouble(t1.getText().toString());
                    Double lng = Double.parseDouble(t2.getText().toString());
                    if (title.isEmpty()) {
                        Utilities.toast(MapsActivity.this, "Invalid title");
                        return;
                    }
                    if (lat.isNaN()) {
                        Utilities.toast(MapsActivity.this, "Invalid Lat");
                        return;

                    }
                    if (lat.isNaN()) {
                        Utilities.toast(MapsActivity.this, "Invalid Lng");
                        return;

                    }
                    Landmarks l = new Landmarks(title, lat, lng);
                    db.addLandmark(l);
                    list.add(l);
                    op.add(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    mMap.addPolyline(op);
                    adapter.notifyDataSetChanged();
                }
            }).setNegativeButton("Cancel", null).show();
        }
    }
}
