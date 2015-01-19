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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 *
 */
public class MapActivity extends ActionBarActivity {

    private static final double[] MAP_LATITUDES = {37.360268};
    private static final double[] MAP_LONGITUDES = {-122.068058};
    private static final int[] MAP_TITLES = {R.string.entrance};
    private static final int[] MAP_ICONS = {R.drawable.map_icon};

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
                        googleMap.setPadding(0, getStatusBarHeight(), 0, getNavBarHeight());
                        googleMap.setMyLocationEnabled(true);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        googleMap.getUiSettings().setMapToolbarEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        googleMap.getUiSettings().setCompassEnabled(true);

                        int numberOfPlaces = MAP_ICONS.length;
                        for (int i = 0; i < numberOfPlaces; i++) {
                            LatLng latLng = new LatLng(MAP_LATITUDES[i], MAP_LONGITUDES[i]);
                            googleMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title(getString(MAP_TITLES[i]))
                                            .icon(BitmapDescriptorFactory.fromResource(MAP_ICONS[i]))
                            );
                        }
                    }
                });
                findViewById(android.R.id.content).getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
