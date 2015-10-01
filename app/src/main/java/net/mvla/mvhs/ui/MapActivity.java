package net.mvla.mvhs.ui;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.mvla.mvhs.BuildConfig;
import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;
import net.mvla.mvhs.map.LocationNode;
import net.mvla.mvhs.map.MapData;
import net.mvla.mvhs.map.Node;
import net.mvla.mvhs.map.TwoNodes;
import net.mvla.mvhs.widget.SearchView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapActivity extends DrawerActivity {

    public static final String STATE_MAP_MODE = "mapMode";
    public static final String FRAGMENT_LIST = "List";

    private boolean mDebugMode;

    private boolean mListShowing;
    private boolean mChoosingStart;
    private boolean mChoosingDestination;
    private LatLng mStartingLocation;

    private boolean mNavigating;
    private Marker mDestinationMarker;
    private List<Polyline> mNavPathPolylines;
    private int mNavPathStep;
    private List<String> mNavTexts;
    //TODO: Cache zoom, only change if changed

    private Map<LocationNode, Marker> mMarkers;
    private SearchView mSearchView;
    private DownloadInfoTask mTask;
    private FloatingActionButton mNavSelectionFab;
    private View mNavigationSelectionAppBar;
    private Button mStartingLocationButton;
    private Toolbar mNavToolbar;

    @Override
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MAP;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_MAP_MODE, !mListShowing);
    }

    private void showNav() {
        final View listFrame = findViewById(R.id.navigation_appbar);
        ObjectAnimator animator = new ObjectAnimator();
        animator.setProperty(View.TRANSLATION_Y);
        animator.setFloatValues(0f);
        animator.setTarget(listFrame);
        animator.setDuration(250);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();
    }

    private void hideNav() {
        final View listFrame = findViewById(R.id.navigation_appbar);
        ObjectAnimator animator = new ObjectAnimator();
        animator.setProperty(View.TRANSLATION_Y);
        animator.setFloatValues(-listFrame.getMeasuredHeight());
        animator.setTarget(listFrame);
        animator.setDuration(250);
        animator.setInterpolator(new FastOutLinearInInterpolator());
        animator.start();
    }

    private void showSelection() {
        final View listFrame = findViewById(R.id.navigation_selection_appbar);
        ObjectAnimator animator = new ObjectAnimator();
        animator.setProperty(View.TRANSLATION_Y);
        animator.setFloatValues(0f);
        animator.setTarget(listFrame);
        animator.setDuration(250);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();
    }

    private void hideSelection() {
        final View listFrame = findViewById(R.id.navigation_selection_appbar);
        ObjectAnimator animator = new ObjectAnimator();
        animator.setProperty(View.TRANSLATION_Y);
        animator.setFloatValues(-listFrame.getMeasuredHeight());
        animator.setTarget(listFrame);
        animator.setDuration(250);
        animator.setInterpolator(new FastOutLinearInInterpolator());
        animator.start();
    }

    private void showList(int listMarginTop) {
        final FrameLayout listFrame = (FrameLayout) findViewById(R.id.activity_map_list_fragment_container);
        ObjectAnimator animator = new ObjectAnimator();
        animator.setProperty(View.Y);
        animator.setFloatValues(Utils.convertDpToPx(this, listMarginTop));
        animator.setTarget(listFrame);
        animator.setDuration(250);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();

        ObjectAnimator animator2 = new ObjectAnimator();
        animator2.setProperty(View.ALPHA);
        animator2.setFloatValues(1f);
        animator2.setTarget(findViewById(R.id.activity_map_searchbox_background));
        animator2.setDuration(250);
        animator2.setInterpolator(new LinearOutSlowInInterpolator());
        animator2.start();

        listFrame.setPadding(0, 0, 0, Utils.convertDpToPx(this, listMarginTop));

        mListShowing = true;
        mSearchView.setDrawerIconState(false, true);
    }

    private void hideList() {
        final FrameLayout layout = (FrameLayout) findViewById(R.id.activity_map_list_fragment_container);
        ObjectAnimator animator = new ObjectAnimator();
        animator.setProperty(View.Y);
        animator.setTarget(layout);
        animator.setFloatValues(layout.getMeasuredHeight());
        animator.setDuration(250);
        animator.setInterpolator(new FastOutLinearInInterpolator());
        animator.start();

        ObjectAnimator animator2 = new ObjectAnimator();
        animator2.setProperty(View.ALPHA);
        animator2.setFloatValues(0f);
        animator2.setTarget(findViewById(R.id.activity_map_searchbox_background));
        animator2.setDuration(250);
        animator2.setInterpolator(new LinearOutSlowInInterpolator());
        animator2.start();

        mListShowing = false;
        mSearchView.setDrawerIconState(true, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (savedInstanceState == null) {
            mListShowing = false;
            downloadMapData();
        } else {
            mListShowing = !savedInstanceState.getBoolean(STATE_MAP_MODE);
        }

        //ADD MAP AND LIST FRAGMENTS
        FragmentManager manager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) manager.findFragmentById(R.id.activity_map_fragment_container);
        boolean initCamera = false;
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            manager.beginTransaction().add(R.id.activity_map_fragment_container, mapFragment).commit();
            initCamera = true;
        }
        initMap(mapFragment, initCamera);

        MapListFragment listFragment = (MapListFragment) manager.findFragmentByTag(FRAGMENT_LIST);
        if (listFragment == null) {
            listFragment = new MapListFragment();
            manager.beginTransaction()
                    .add(R.id.activity_map_list_fragment_container, listFragment, FRAGMENT_LIST)
                    .commit();
        }

        CheckBox debug = (CheckBox) findViewById(R.id.activity_map_checkbox_debug);
        debug.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mDebugMode = isChecked;
            MapFragment mapFragment1 = (MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment_container);
            updateMapOverlays(mapFragment1.getMap());
        });
        debug.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        LinearLayout openListBar = (LinearLayout) findViewById(R.id.activity_map_showlist_linearlayout);
        openListBar.setOnClickListener(v -> {
            showList(80);
            ((MapListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_LIST)).updateDataAndSearch("");
        });

        mSearchView = (SearchView) findViewById(R.id.activity_map_searchbox);
        mSearchView.setCallback(new SearchView.SearchViewCallback() {

            @Override
            public void onQueryTextChange(String newText) {
                ((MapListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_LIST))
                        .updateDataAndSearch(newText);
            }

            @Override
            public void onFocusChange(boolean focused) {
                if (focused) {
                    showList(80);
                }
            }

            @Override
            public void onDrawerIconClicked() {
                if (mChoosingDestination || mChoosingStart || mListShowing) {
                    onBackPressed();
                    mSearchView.clearFocus();
                    mSearchView.clearText();
                } else {
                    openDrawer();
                }
            }
        });
        mSearchView.setDrawerIconState(true, false);

        mNavigationSelectionAppBar = findViewById(R.id.navigation_selection_appbar);

        mStartingLocationButton = (Button) findViewById(R.id.navigation_appbar_startingloc_button);
        mStartingLocationButton.setOnClickListener(v -> enterChoosingStart());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mNavigationSelectionAppBar.getLayoutParams();
        params.height += getStatusBarHeight();
        mNavigationSelectionAppBar.setLayoutParams(params);

        Toolbar navSelection = (Toolbar) findViewById(R.id.navigation_selection_toolbar);
        navSelection.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        navSelection.setNavigationOnClickListener(v -> exitChoosingDestination());

        mNavSelectionFab = (FloatingActionButton) findViewById(R.id.activity_map_nav_fab);
        mNavSelectionFab.setOnClickListener(v -> {
            if (!mNavigating) {
                enterChoosingDestination();
            } else {
                //nav step = how many lines travelled

                if (mNavPathStep < mNavPathPolylines.size()) {
                    //nav step = how many lines travelled-1
                    mNavPathPolylines.get(mNavPathStep).setColor(Color.argb(255, 255, 255, 0));
                    mNavToolbar.setTitle(mNavTexts.get(mNavPathStep));
                    mNavPathStep++;
                } else {
                    mNavPathStep = 0;
                    mNavToolbar.setTitle(mNavTexts.get(0));
                    for (Polyline p : mNavPathPolylines) {
                        if (p != null) {
                            p.setColor(Color.argb(127, 255, 255, 0));
                        }
                    }
                    mNavPathStep++;
                }
            }
        });
        mNavSelectionFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));

        FloatingActionButton myLocFab = (FloatingActionButton) findViewById(R.id.activity_map_my_loc_fab);
        myLocFab.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        myLocFab.setOnClickListener(v -> {
            GoogleMap map = getMap();
            Location myLocation = map.getMyLocation();
            if (myLocation != null) {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                map.animateCamera(cameraUpdate);
            }
        });

        mNavToolbar = (Toolbar) findViewById(R.id.navigation_toolbar);
        mNavToolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
        mNavToolbar.setNavigationOnClickListener(v -> onBackPressed());

        ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) mNavToolbar.getLayoutParams();
        params2.topMargin = getStatusBarHeight();
        mNavToolbar.setLayoutParams(params2);


        //RESTORE MAP MODE AND STUFF
        if (mListShowing) {
            showList(80);
            ((MapListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_LIST)).updateDataAndSearch("");
        } else {
            Utils.addOnGlobalLayoutListener(findViewById(R.id.activity_map_list_fragment_container), () -> {
                final FrameLayout listFragmentFrame = (FrameLayout) findViewById(R.id.activity_map_list_fragment_container);
                listFragmentFrame.setTranslationY(listFragmentFrame.getMeasuredHeight());
            });
            mSearchView.setDrawerIconState(true, true);
        }


        Utils.addOnGlobalLayoutListener(findViewById(R.id.navigation_selection_appbar), () -> {
            final View listFragmentFrame = findViewById(R.id.navigation_selection_appbar);
            listFragmentFrame.setTranslationY(-listFragmentFrame.getMeasuredHeight());
            final View listFragment1 = findViewById(R.id.navigation_appbar);
            listFragment1.setTranslationY(-listFragment1.getMeasuredHeight());
        });

        mMarkers = new HashMap<>();


        overridePendingTransition(0, 0);
    }

    private void enterChoosingStart() {
        mStartingLocation = null;
        mSearchView.clearText();
        hideSelection();
        mChoosingDestination = false;

        mChoosingStart = true;
        showList(80);
    }

    private void exitChoosingDestination() {
        hideSelection();
        hideList();
        mChoosingDestination = false;
    }

    private void enterChoosingDestination() {
        mChoosingStart = false;
        mSearchView.clearFocus();
        mSearchView.clearText();
        showSelection();
        mChoosingDestination = true;
        showList(176);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initMap(MapFragment map, final boolean initCamera) {
        final MapFragment finalMap = map;
        Utils.addOnGlobalLayoutListener(findViewById(R.id.activity_map_fragment_container), () ->
                finalMap.getMapAsync(googleMap -> MapActivity.this.onMapReady(googleMap, initCamera)));
    }

    private void onMapReady(final GoogleMap googleMap, boolean initCamera) {
        final boolean[] done = new boolean[1];
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setPadding(0, getResources().getDimensionPixelSize(R.dimen.searchbox_height), 0, 0);

        if (initCamera) {
            LatLngBounds mapBounds = new LatLngBounds(
                    new LatLng(37.359014, -122.068730), new LatLng(37.361323, -122.065080));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    mapBounds, Utils.convertDpToPx(MapActivity.this, 8)));
        }

        googleMap.setOnMarkerClickListener(marker -> {
            MapActivity.this.onMarkerClick(marker);
            return false;
        });

        googleMap.setOnInfoWindowClickListener(marker -> MapActivity.this.startNavigation(marker, googleMap));

        googleMap.setOnCameraChangeListener(cameraPosition -> {
            if (mNavigating) {
                for (Polyline line : mNavPathPolylines) {
                    if (line != null) {
                        line.setWidth((cameraPosition.zoom - 14.7f) * 6f);
                    }
                }
            }
            Log.e("CameraZoom", "" + cameraPosition.zoom);
            if (!done[0]) {
                updateMapOverlays(googleMap);
                done[0] = true;
            }
        });
    }

    public void onMapListItemClicked(LocationNode node) {
        Marker marker = mMarkers.get(node);
        if (mChoosingDestination) {
            onBackPressed();
            startNavigation(marker, getMap());
        } else if (mChoosingStart) {
            onBackPressed();
            mStartingLocationButton.setText(node.getName());
            mStartingLocation = node.latLng;
        } else {
            if (mListShowing) {
                hideList();
            }
            mSearchView.clearFocus();
            marker.showInfoWindow();
            getMap().animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            //TODO: Clear nav?
            //clearNav();
        }
    }

    private void startNavigation(Marker marker, GoogleMap googleMap) {

        Node startPlace;
        if (mStartingLocation == null) {
            Location myLocation = googleMap.getMyLocation();
            if (myLocation == null) {
                Toast.makeText(MapActivity.this,
                        "Your location cannot be obtained",
                        Toast.LENGTH_LONG).show();
                enterChoosingDestination();
                return;
            }
            double myLat = myLocation.getLatitude();
            double myLong = myLocation.getLongitude();
            startPlace = new Node(myLat, myLong);
        } else {
            startPlace = MapData.locationNodeMap.get(mStartingLocation);
        }

        clearNav();

        if (!MapData.onCampus(startPlace)) {
            Toast.makeText(MapActivity.this,
                    R.string.not_on_campus,
                    Toast.LENGTH_LONG).show();
            enterChoosingDestination();
            return;
        }

        Node closestPathNode = null;
        double closestDistance = -1;
        List<Node> nodes = new ArrayList<>(MapData.pathNodeMap.values());
        nodes.addAll(MapData.locationNodeMap.values());
        for (Node node : nodes) {
            double distance = Node.distance(node, startPlace);
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
        navPath.add(0, startPlace);

        if (mDebugMode) {
            updateMapOverlays(googleMap);
        }

        int totalDistance = 0;
        mNavPathPolylines = new ArrayList<>();
        for (int i = 1; i < navPath.size(); ) {
            Node n = navPath.get(i - 1);

            int index = i;
            while (index < navPath.size() - 1) {
                int test = index + 1;
                if (!MapData.nodesLiesOnOnePath(navPath.subList(i - 1, test + 1))) {
                    break;
                }
                Log.e("Nav Succeeded", navPath.subList(i - 1, test + 1).toString());
                index = test;
            }

            Polyline polyline = googleMap.addPolyline(
                    new PolylineOptions()
                            .add(n.latLng, navPath.get(index).latLng)
                            .color(Color.argb(255, 255, 255, 0))
                            .width((googleMap.getCameraPosition().zoom - 14.7f) * 6f)
                            .zIndex(1000)
            );

            float[] results = new float[3];
            Location.distanceBetween(n.latLng.latitude, n.latLng.longitude,
                    navPath.get(index).latLng.latitude, navPath.get(index).latLng.longitude, results);
            totalDistance += results[0];
            mNavPathPolylines.add(polyline);
            i += index - i + 1;
        }
        mNavPathPolylines.add(0, null);
        mNavPathStep = mNavPathPolylines.size();

        mNavTexts = new ArrayList<>();
        double bearingPrevious = 0;
        for (int i = 1; i < mNavPathPolylines.size(); i++) {
            List<LatLng> latLngs = mNavPathPolylines.get(i).getPoints();
            LatLng n = latLngs.get(0);
            LatLng n2 = latLngs.get(1);

            double bearing = Utils.bearing(n.latitude, n.longitude, n2.latitude, n2.longitude);
            Log.e("NavCalc", "Bearing " + bearing + " for points " + n + ", " + n2);
            String nav = null;
            if (i == 1) {
                if (bearing < 45 || bearing >= 315) {
                    nav = getString(R.string.north);
                } else if (bearing < 135) {
                    nav = getString(R.string.east);
                } else if (bearing < 225) {
                    nav = getString(R.string.south);
                } else if (bearing < 315) {
                    nav = getString(R.string.west);
                }
            } else {
                double difference = bearing - bearingPrevious;
                difference = (difference + 360) % 360;
                if (difference < 45 || difference >= 315) {
                    nav = getString(R.string.forward);
                } else if (difference < 135) {
                    nav = getString(R.string.right);
                } else if (difference < 225) {
                    nav = getString(R.string.around);
                } else if (difference < 315) {
                    nav = getString(R.string.left);
                }
            }
            bearingPrevious = bearing;
            mNavTexts.add(nav);
        }

        LocationNode endLocationNode = (LocationNode) navPath.get(navPath.size() - 1);
        mNavTexts.add(getString(R.string.arrived_at) + endLocationNode.getName());

        LatLngBounds bounds = new LatLngBounds.Builder().include(startPlace.latLng).include(marker.getPosition()).build();
        getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Utils.convertDpToPx(this, 128)));

        mNavigating = true;
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        mDestinationMarker = marker;
        showNav();
        mNavSelectionFab.setImageResource(R.drawable.ic_chevron_right_black_24dp);

        if (startPlace instanceof LocationNode) {
            mNavToolbar.setTitle(String.format(getString(R.string.path_to), ((LocationNode) startPlace).getName(), endLocationNode.getName()));
        } else {
            mNavToolbar.setTitle(String.format(getString(R.string.path_to), "your location", endLocationNode.getName()));
        }

        mNavToolbar.setSubtitle(String.format(getString(R.string.total), totalDistance * 3.28084, totalDistance * 0.001 / 5 * 60));
        Toast.makeText(this, getString(R.string.proceed), Toast.LENGTH_SHORT).show();
    }

    private boolean onMarkerClick(Marker marker) {
        Node node = MapData.pathNodeMap.get(marker.getPosition());
        if (node == null) {
            return true;
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

    @Override
    public void onBackPressed() {
        if (mNavigating) {
            clearNav();
        } else if (mChoosingDestination) {
            exitChoosingDestination();
        } else if (mChoosingStart) {
            enterChoosingDestination();
        } else if (!mListShowing) {
            super.onBackPressed();
        } else {
            hideList();
        }
    }

    private void clearNav() {
        mNavigating = false;
        mStartingLocation = null;
        mStartingLocationButton.setText(R.string.your_location);

        hideNav();
        mNavSelectionFab.setImageResource(R.drawable.ic_directions_black_24dp);

        if (mDestinationMarker != null)
            mDestinationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));

        MapData.cleanTempNodes();
        if (mNavPathPolylines != null) {
            for (Polyline line : mNavPathPolylines) {
                if (line != null) {
                    line.remove();
                }
            }
            mNavPathPolylines = null;
        }
        if (mDebugMode) {
            updateMapOverlays(getMap());
        }
    }

    private GoogleMap getMap() {
        return ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment_container)).getMap();
    }

    private void updateMapOverlays(GoogleMap googleMap) {
        googleMap.clear();
        LatLngBounds mapBounds = new LatLngBounds(new LatLng(37.359014, -122.068730),
                new LatLng(37.361323, -122.065080));
        GroundOverlayOptions schoolMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(
                        mDebugMode ? R.drawable.map_debug : R.drawable.map))
                .transparency(0.1f)
                .positionFromBounds(mapBounds);
        googleMap.addGroundOverlay(schoolMap);

        for (LocationNode node : MapData.locationNodeMap.values()) {
            MarkerOptions options = new MarkerOptions().position(new LatLng(node.latLng.latitude, node.latLng.longitude))
                    .title(node.getName())
                    .snippet("Press for navigation");
            if (!mDebugMode) {
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
            }
            mMarkers.put(node, googleMap.addMarker(options));
        }

        if (mNavPathPolylines != null) {
            List<Polyline> newPolylines = new ArrayList<>();
            for (Polyline p : mNavPathPolylines) {
                if (p != null) {
                    Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                            .addAll(p.getPoints())
                            .color(Color.argb(255, 255, 255, 0))
                            .zIndex(1000)
                            .width((googleMap.getCameraPosition().zoom - 14.7f) * 6f));
                    newPolylines.add(polyline);
                }
            }
            mNavPathPolylines = newPolylines;
            mNavPathStep = mNavPathPolylines.size();
        }

        if (mDebugMode) {
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

    private void downloadMapData() {
        if (mTask == null || mTask.getStatus() != AsyncTask.Status.RUNNING) {
            /*ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {*/
            mTask = new DownloadInfoTask();
            mTask.execute("http://pluscubed.github.io/mvhs-map-data/production-mapdata.json");
            /*} else {
                new MaterialDialog.Builder(this)
                        .content("Not connected to the Internet.")
                        .positiveText("Dismiss")
                        .negativeText("Retry")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                downloadMapData();
                            }
                        })
                        .showSelection();
            }*/
        }
    }

    private class DownloadInfoTask extends AsyncTask<String, Void, Void> {
        private Dialog mLoadingDialog;

        @Override
        protected void onPreExecute() {
            mLoadingDialog = new MaterialDialog.Builder(MapActivity.this)
                    .title("Loading...")
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Void doInBackground(String... urls) {
            String thing = null;

            InputStream stream = null;
            try {
                // Instantiate the parser
                /*URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                Log.e("", "Connection opened");
                stream = conn.getInputStream();*/
                stream = getAssets().open("production-mapdata.json");
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                thing = responseStrBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                //return getResources().getString(R.string.connection_error);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (thing != null) {
                try {
                    MapData.init(thing);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            mLoadingDialog.dismiss();
            updateMapOverlays(getMap());
        }
    }
}
