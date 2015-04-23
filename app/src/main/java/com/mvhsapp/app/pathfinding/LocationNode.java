package com.mvhsapp.app.pathfinding;

/**
 * location node for rooms
 */
public class LocationNode extends Node {

    private String mName;
    private String mPath;

    public LocationNode(double longitude, double latitude, String pathName, String locationName) {
        super(longitude, latitude);
        mPath = pathName;
        mName = locationName;
    }

    public String getPath() {
        return mPath;
    }

    public String getName() {
        return mName;
    }

}
