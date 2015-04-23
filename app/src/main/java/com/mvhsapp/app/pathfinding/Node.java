package com.mvhsapp.app.pathfinding;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Node
 */
public class Node implements Comparable {

    /**
     * The x coordinate of the node
     */
    private double mLong;
    /**
     * The y coordinate of the node
     */
    private double mLat;
    /**
     * The path cost for this node
     */
    private double mG;
    /**
     * The mParent of this node, how we reached it in the search
     */
    private Node mParent;
    /**
     * The double cost of this node
     */
    private double mH;
    /**
     * The search mDepth of this node
     */
    private int mDepth;

    private Set<Node> mConnected;

    /**
     * Create a new node
     *
     * @param longitude The x coordinate of the node
     * @param latitude  The y coordinate of the node
     */
    public Node(double latitude, double longitude) {
        this.mLat = latitude;
        this.mLong = longitude;
        mConnected = new HashSet<>();
    }

    public static double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.getLong() - b.getLong(), 2) + Math.pow(a.getLat() - b.getLat(), 2));
    }

    public void removeConnected(Node node) {
        mConnected.remove(node);
    }

    public void addConnected(Node node) {
        mConnected.add(node);
    }

    public Set<Node> getConnected() {
        return mConnected;
    }

    public Node getParent() {
        return mParent;
    }

    public int setParent(Node parent) {
        mDepth = parent.mDepth + 1;
        this.mParent = parent;

        return mDepth;
    }

    public double getLong() {
        return mLong;
    }

    public double getLat() {
        return mLat;
    }

    @Override
    public int hashCode() {
        return (int) (mLong * mLat * 1000);
    }

    public void updateGH(Node target) {
        if (mParent != null) {
            double distance = distance(this, mParent);
            mG = mParent.mG + distance;
        }
        mH = distance(this, target);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node other = (Node) o;

            return other.getLat() == mLat && other.getLong() == mLong;
        } else {
            return false;
        }
    }

    public Node nodeLiesOnConnectedPath(Node check) {
        for (Node connected : mConnected) {
            Log.e("Text", check + "/" + connected + "/" + this);
            if (distance(this, check) + distance(check, connected) == distance(this, connected)) {
                return connected;
            }
        }
        return null;
    }

    public double getG() {
        return mG;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(@NonNull Object other) {
        Node o = (Node) other;

        double f = mH + mG;
        double of = o.mH + o.mG;

        if (f < of) {
            return -1;
        } else if (f > of) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return mLat + " " + mLong;
    }
}
