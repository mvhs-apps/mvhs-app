package com.mvhsapp.app;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.maps.MapFragment;

/**
 *
 */
public class MapActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager manager = getFragmentManager();
        MapFragment map = (MapFragment) manager.findFragmentById(android.R.id.content);
        if (map == null) {
            map = new MapFragment();
        }
        manager.beginTransaction().add(android.R.id.content, map).commit();
    }
}
