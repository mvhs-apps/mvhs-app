package net.mvla.mvhs.map;

import java.util.List;

/**
 * location node for rooms
 */
public class LocationNode extends Node {

    private final String mName;
    private final Node mPathNode;
    private final List<String> mTags;

    public LocationNode(double lat, double lon, double latOnPath, double longOnPath, String locationName, List<String> tags) {
        super(lat, lon);
        mTags = tags;
        mPathNode = new Node(latOnPath, longOnPath);
        mName = locationName;

        addConnected(mPathNode);
        mPathNode.addConnected(this);
    }

    public String getName() {
        return mName;
    }

    public Node getPathNode() {
        return mPathNode;
    }

    public boolean matchFilter(String filter) {
        boolean match = false;
        for (String string : mTags) {
            if (string.toLowerCase().contains(filter.toLowerCase())) {
                match = true;
            }
        }
        return match || mName.toLowerCase().contains(filter.toLowerCase());
    }

    public List<String> getTags() {
        return mTags;
    }
}
