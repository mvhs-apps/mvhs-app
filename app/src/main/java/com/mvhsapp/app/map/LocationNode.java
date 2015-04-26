package com.mvhsapp.app.map;

/**
 * location node for rooms
 */
public class LocationNode extends Node {

    private final String name;
    private final Node pathNode;

    public LocationNode(double lat, double lon, double latOnPath, double longOnPath, String locationName) {
        super(lat, lon);
        pathNode = new Node(latOnPath, longOnPath);
        name = locationName;
        addConnected(pathNode);
        pathNode.addConnected(this);
    }

    public String getName() {
        return name;
    }

    public Node getPathNode() {
        return pathNode;
    }
}
