package com.mvhsapp.app;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 *
 */
public class MapActivity extends ActionBarActivity {

    private static final double[] MAP_LATITUDES = {37.361031, 37.361031, 37.361031, 37.361031, 37.361031};
    private static final double[] MAP_LONGITUDES = {-122.067937, -122.067818, -122.067695, -122.067590};
    private static final String[] MAP_TITLES = {"601", "602", "603", "604"};

    public static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density
                + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager manager = getFragmentManager();
        MapFragment map = (MapFragment) manager.findFragmentById(android.R.id.content);
        if (map == null) {
            map = new MapFragment();
            manager.beginTransaction().add(android.R.id.content, map).commit();
        }

        ViewTreeObserver vto = findViewById(android.R.id.content).getViewTreeObserver();
        final MapFragment finalMap = map;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                finalMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng sw = new LatLng(37.356799, -122.068738);
                        LatLng ne = new LatLng(37.361262, -122.065098);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                new LatLngBounds(sw, ne), convertDpToPx(MapActivity.this, 8)));
                        googleMap.setMyLocationEnabled(true);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        googleMap.getUiSettings().setMapToolbarEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        googleMap.getUiSettings().setCompassEnabled(true);

                        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                            @Override
                            public void onCameraChange(CameraPosition cameraPosition) {
                                googleMap.clear();
//                                if(cameraPosition.zoom>23){
                                LatLngBounds mapBounds = new LatLngBounds(new LatLng(37.359014, -122.068730),
                                        new LatLng(37.361323, -122.065080));
                                GroundOverlayOptions schoolMap = new GroundOverlayOptions()
                                        .image(BitmapDescriptorFactory.fromResource(
                                                BuildConfig.DEBUG ? R.drawable.map2 : R.drawable.map))
                                        .transparency(0.1f)
                                        .positionFromBounds(mapBounds);
                                googleMap.addGroundOverlay(schoolMap);
//                                }else{
                                    /*int numberOfPlaces = MAP_LONGITUDES.length;
                                    for (int i = 0; i < numberOfPlaces; i++) {
                                        LatLng latLng = new LatLng(MAP_LATITUDES[i], MAP_LONGITUDES[i]);
                                        googleMap.addMarker(new MarkerOptions()
                                                        .position(latLng)
                                                        .title(MAP_TITLES[i])
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon))
                                        );
                                    }*/
//                                }
                            }
                        });
                    }
                });
                findViewById(android.R.id.content).getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }
}
