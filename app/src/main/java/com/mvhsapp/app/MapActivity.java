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
import com.google.android.gms.maps.model.PolylineOptions;
import com.mvhsapp.app.pathfinding.LocationNode;
import com.mvhsapp.app.pathfinding.MapData;
import com.mvhsapp.app.pathfinding.Node;
import com.mvhsapp.app.pathfinding.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class MapActivity extends AppCompatActivity {

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

                        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                double myLat = googleMap.getMyLocation().getLatitude();
                                double myLong = googleMap.getMyLocation().getLongitude();
                                Node myLocation = new Node(myLat, myLong);

                                if (MapData.onCampus(myLocation)) {
                                    Toast.makeText(MapActivity.this,
                                            "You are not in the Mountain View High School campus.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Node closestMapNode = null;
                                double closestDistance = -1;
                                List<Node> nodes = new ArrayList<>(MapData.pathNodeMap.values());
                                nodes.addAll(MapData.locationNodeMap.values());
                                for (Node node : nodes) {
                                    double distance = Node.distance(node, myLocation);
                                    if (closestDistance == -1 || distance < closestDistance) {
                                        closestDistance = distance;
                                        closestMapNode = node;
                                    }
                                }
                                if (closestMapNode == null) {
                                    Toast.makeText(MapActivity.this, "Error: closest map node null", Toast.LENGTH_SHORT);
                                    return;
                                }

                                Node end = new Node(marker.getPosition().latitude, marker.getPosition().longitude);
                                List<Node> path = MapData.findPath(closestMapNode, end);
                                if (path == null) {
                                    Toast.makeText(MapActivity.this, "Error: path not found or not location node", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                for (int i = 1; i < path.size(); i++) {
                                    Node n = path.get(i - 1);
                                    Node n2 = path.get(i);
                                    googleMap.addPolyline(
                                            new PolylineOptions()
                                                    .add(new LatLng(n.getLat(), n.getLong()),
                                                            new LatLng(n2.getLat(), n2.getLong()))
                                                    .color(Color.BLUE)
                                                    .width(10)
                                                    .zIndex(1000)
                                    );
                                }
                                if (!myLocation.equals(closestMapNode)) {
                                    googleMap.addPolyline(
                                            new PolylineOptions()
                                                    .add(new LatLng(closestMapNode.getLat(), closestMapNode.getLong()),
                                                            new LatLng(myLocation.getLat(), myLocation.getLong()))
                                                    .color(Color.CYAN)
                                                    .width(10)
                                                    .zIndex(1000)
                                    );
                                }

                                //TODO: for debug:
                                updateMapOverlays(googleMap);
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
                    findViewById(R.id.activity_map_fragment_container).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        Button clearNav = (Button) findViewById(R.id.activity_map_button_clear_nav);
        clearNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapData.cleanTempNodes();
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment_container);
                updateMapOverlays(mapFragment.getMap());
            }
        });
    }

    private void updateMapOverlays(GoogleMap googleMap) {
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

        for (LocationNode node : MapData.locationNodeMap.values()) {
            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(node.getLat(), node.getLong())).title(node.getName());
            googleMap.addMarker(options);
        }

        Set<Path> addedNodeMap = new HashSet<>();

        for (Node n : MapData.pathNodeMap.values()) {
            for (Node connected : n.getConnected()) {
                Path path = new Path(n, connected);
                if (!addedNodeMap.contains(path)) {
                    addedNodeMap.add(path);
                    googleMap.addPolyline(new PolylineOptions().add(new LatLng(n.getLat(), n.getLong()),
                            new LatLng(connected.getLat(), connected.getLong())).color(Color.GREEN).width(10));
                }
            }

            MarkerOptions options = new MarkerOptions();
            String snippet = "Connected:";
            for (Node n2 : n.getConnected()) {
                snippet += "\n" + n2;
            }
            options.position(new LatLng(n.getLat(), n.getLong())).title(n.toString()).snippet(snippet);
            googleMap.addMarker(options);
        }
    }
}
