package com.mvhsapp.app;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mvhsapp.app.map.LocationNode;
import com.mvhsapp.app.map.MapData;
import com.mvhsapp.app.map.Node;
import com.mvhsapp.app.map.TwoNodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class MapActivity extends AppCompatActivity {

    private List<Polyline> mNavPathPolylines;
    private int mStep;
    private boolean mDebug;

    public static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density
                + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        FragmentManager manager = getFragmentManager();
        MapFragment map = (MapFragment) manager.findFragmentById(R.id.activity_map_fragment_container);
        if (map == null) {
            map = new MapFragment();
            manager.beginTransaction().add(R.id.activity_map_fragment_container, map).commit();
        }

        ViewTreeObserver vto = findViewById(R.id.activity_map_fragment_container).getViewTreeObserver();
        final MapFragment finalMap = map;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                finalMap.getMapAsync(new OnMapReadyCallback() {
                    private boolean done;

                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        LatLngBounds mapBounds = new LatLngBounds(
                                new LatLng(37.359014, -122.068730), new LatLng(37.361323, -122.065080));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                mapBounds, convertDpToPx(MapActivity.this, 8)));
                        googleMap.setMyLocationEnabled(true);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        googleMap.getUiSettings().setMapToolbarEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        googleMap.getUiSettings().setCompassEnabled(true);
                        googleMap.getUiSettings().setMapToolbarEnabled(false);

                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                Node node = MapData.pathNodeMap.get(marker.getPosition());
                                if (node == null) {
                                    return false;
                                }
                                String snippet = node.toString() + "\nConnected:";
                                for (Node n2 : node.getConnected()) {
                                    snippet += "\n" + n2;
                                }
                                snippet += "\n" + node.getG();
                                Toast.makeText(MapActivity.this,
                                        snippet,
                                        Toast.LENGTH_SHORT).show();

                                return false;
                            }
                        });

                        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                clearNav();
                                double myLat = googleMap.getMyLocation().getLatitude();
                                double myLong = googleMap.getMyLocation().getLongitude();
                                Node myLocation = new Node(myLat, myLong);

                                if (!MapData.onCampus(myLocation)) {
                                    Toast.makeText(MapActivity.this,
                                            "You are not on the Mountain View High School campus.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Node closestPathNode = null;
                                double closestDistance = -1;
                                List<Node> nodes = new ArrayList<>(MapData.pathNodeMap.values());
                                nodes.addAll(MapData.locationNodeMap.values());
                                for (Node node : nodes) {
                                    double distance = Node.distance(node, myLocation);
                                    if (closestDistance == -1 || distance < closestDistance) {
                                        closestDistance = distance;
                                        closestPathNode = node;
                                    }
                                }
                                if (closestPathNode == null) {
                                    Toast.makeText(MapActivity.this, "Error: closest path node null", Toast.LENGTH_SHORT);
                                    return;
                                }

                                List<Node> navPath = MapData.findPath(closestPathNode.latLng, marker.getPosition());
                                if (navPath == null) {
                                    Toast.makeText(MapActivity.this, "Error: path not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                navPath.add(0, myLocation);

                                //TODO: for debug:
                                if (mDebug) {
                                    updateMapOverlays(googleMap);
                                }

                                mNavPathPolylines = new ArrayList<>();
                                for (int i = 1; i < navPath.size(); i++) {
                                    Node n = navPath.get(i - 1);
                                    Node n2 = navPath.get(i);
                                    Polyline polyline = googleMap.addPolyline(
                                            new PolylineOptions()
                                                    .add(new LatLng(n.latLng.latitude, n.latLng.longitude),
                                                            new LatLng(n2.latLng.latitude, n2.latLng.longitude))
                                                    .color(Color.argb(255, 0, 203, 112))
                                                    .width(10)
                                                    .zIndex(1000)
                                    );
                                    mNavPathPolylines.add(polyline);
                                }
                                mStep = mNavPathPolylines.size();

                                Toast.makeText(MapActivity.this, "Path found to " + ((LocationNode) navPath.get(navPath.size() - 1)).getName(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                            @Override
                            public void onCameraChange(CameraPosition cameraPosition) {
                                if (!done) {
                                    updateMapOverlays(googleMap);
                                    done = true;
                                }
                            }
                        });
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(R.id.activity_map_fragment_container).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    //noinspection deprecation
                    findViewById(R.id.activity_map_fragment_container).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        Button clearNav = (Button) findViewById(R.id.activity_map_button_clear_nav);
        clearNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearNav();
            }
        });
        Button next = (Button) findViewById(R.id.activity_map_button_show_nav);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNavPathPolylines != null && mStep < mNavPathPolylines.size()) {
                    mNavPathPolylines.get(mStep).setColor(Color.argb(255, 0, 203, 112));
                    mStep++;
                } else if (mNavPathPolylines != null) {
                    mStep = 0;
                    for (Polyline p : mNavPathPolylines) {
                        p.setColor(Color.argb(0, 0, 0, 0));
                    }
                }
            }
        });

        CheckBox debug = (CheckBox) findViewById(R.id.activity_map_checkbox_debug);
        debug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDebug = isChecked;
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment_container);
                updateMapOverlays(mapFragment.getMap());
            }
        });
        debug.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    private void clearNav() {
        MapData.cleanTempNodes();
        if (mNavPathPolylines != null) {
            for (Polyline line : mNavPathPolylines) {
                line.remove();
            }
            mNavPathPolylines = null;
        }
        if (mDebug) {
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment_container);
            updateMapOverlays(mapFragment.getMap());
        }
    }

    private void updateMapOverlays(GoogleMap googleMap) {
        googleMap.clear();
        LatLngBounds mapBounds = new LatLngBounds(new LatLng(37.359014, -122.068730),
                new LatLng(37.361323, -122.065080));
        GroundOverlayOptions schoolMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(
                        mDebug ? R.drawable.map2 : R.drawable.map))
                .transparency(0.1f)
                .positionFromBounds(mapBounds);
        googleMap.addGroundOverlay(schoolMap);

        for (LocationNode node : MapData.locationNodeMap.values()) {
            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(node.latLng.latitude, node.latLng.longitude)).title(node.getName());
            googleMap.addMarker(options);
        }

        if (mNavPathPolylines != null) {
            List<Polyline> newPolylines = new ArrayList<>();
            for (Polyline p : mNavPathPolylines) {
                Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                        .addAll(p.getPoints())
                        .color(Color.argb(255, 0, 203, 112))
                        .zIndex(1000)
                        .width(10));
                newPolylines.add(polyline);
            }
            mNavPathPolylines = newPolylines;
            mStep = mNavPathPolylines.size();
        }

        //TODO: for debug:
        if (mDebug) {
            Set<TwoNodes> addedNodeMap = new HashSet<>();
            for (Node n : MapData.pathNodeMap.values()) {
                for (Node connected : n.getConnected()) {
                    TwoNodes path = new TwoNodes(n, connected);
                    if (!addedNodeMap.contains(path)) {
                        addedNodeMap.add(path);
                        googleMap.addPolyline(new PolylineOptions()
                                .add(new LatLng(n.latLng.latitude, n.latLng.longitude), new LatLng(connected.latLng.latitude, connected.latLng.longitude))
                                .color(Color.WHITE)
                                .width(20));
                    }
                }

                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(n.latLng.latitude, n.latLng.longitude));
                googleMap.addMarker(options);
            }
        }
    }
}
