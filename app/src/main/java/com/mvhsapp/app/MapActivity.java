package com.mvhsapp.app;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mvhsapp.app.pathfinding.LocationNode;
import com.mvhsapp.app.pathfinding.Node;
import com.mvhsapp.app.pathfinding.Path;
import com.mvhsapp.app.pathfinding.SortedNodeList;

import java.util.ArrayList;
import java.util.Collections;
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

    private static List<Node> findPath(Node start, Node end) {
        SortedNodeList openList = new SortedNodeList();
        List<Node> closedList = new ArrayList<>();

        openList.setTarget(end);
        openList.add(start);
        while (true) {
            Node lowestF = openList.first();
            openList.remove(lowestF);
            closedList.add(lowestF);
            if (lowestF.equals(end)) {
                break;
            }
            for (Node n : lowestF.getConnected()) {
                if (!closedList.contains(n)) {
                    if (!openList.contains(n)) {
                        n.setParent(lowestF);
                        openList.add(n);
                    } else {
                        double gFromLowestFToN = lowestF.getG()
                                + Node.distance(lowestF, n);
                        if (gFromLowestFToN < n.getG()) {
                            n.setParent(lowestF);
                        }
                        openList.sort();
                    }
                }
            }
        }

        List<Node> path = new ArrayList<>();
        Node child = end;
        path.add(end);
        while (!child.equals(start)) {
            Node parent = child.getParent();
            path.add(parent);
            child = parent;
        }
        Collections.reverse(path);
        return path;
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
                    private boolean done;

                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        LatLng sw = new LatLng(37.356799, -122.068738);
                        LatLng ne = new LatLng(37.361262, -122.065098);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                new LatLngBounds(sw, ne), convertDpToPx(MapActivity.this, 8)));
                        googleMap.setMyLocationEnabled(true);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        googleMap.getUiSettings().setMapToolbarEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        googleMap.getUiSettings().setCompassEnabled(true);

                        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                Node node = new Node(37.359545, -122.067985);
                                Node node2 = new Node(37.360720, -122.066975);
                                List<Node> path = findPath(MapData.pathNodeMap.get(node), MapData.pathNodeMap.get(node2));
                                for (int i = 1; i < path.size(); i++) {
                                    Node n = path.get(i - 1);
                                    Node n2 = path.get(i);
                                    Log.e("Tag", n + " " + n2);
                                    googleMap.addPolyline(
                                            new PolylineOptions()
                                                    .add(new LatLng(n.getLat(), n.getLong()),
                                                            new LatLng(n2.getLat(), n2.getLong()))
                                                    .color(Color.BLUE)
                                                    .width(10)
                                    );
                                }
                                Log.e("Tag", "done");
                            }
                        });

                        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                            @Override
                            public void onCameraChange(CameraPosition cameraPosition) {
                                if (!done) {
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
                                    for (LocationNode node : MapData.locationNodeSet) {
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
                                    done = true;
                                }
                            }
                        });
                    }
                });
                findViewById(android.R.id.content).getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }
}
