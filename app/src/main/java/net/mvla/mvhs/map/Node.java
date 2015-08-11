package net.mvla.mvhs.map;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.Set;

/**
 * Node
 */
public class Node implements Comparable {

    /**
     * The x coordinate of the node
     */
    public LatLng latLng;
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

    private Node mTarget;

    private Set<Node> mConnected;

    /**
     * Create a new node
     *
     * @param longitude The x coordinate of the node
     * @param latitude  The y coordinate of the node
     */
    public Node(double latitude, double longitude) {
        latLng = new LatLng(latitude, longitude);
        mConnected = new HashSet<>();
    }

    public static double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.latLng.longitude - b.latLng.longitude, 2) + Math.pow(a.latLng.latitude - b.latLng.latitude, 2));
    }

    void removeConnected(Node node) {
        mConnected.remove(node);
    }

    void addConnected(Node node) {
        mConnected.add(node);
    }

    public Set<Node> getConnected() {
        return mConnected;
    }

    public Node getParent() {
        return mParent;
    }

    public void setParent(Node parent) {
        this.mParent = parent;
    }

    @Override
    public int hashCode() {
        return latLng.hashCode();
    }

    void updateGH(Node target) {
        if (mParent != null) {
            double distance = distance(this, mParent);
            mG = mParent.mG + distance;
        }
        if (mTarget == null || !mTarget.equals(target)) {
            mH = distance(this, target);
            mTarget = target;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Node) {
            Node other = (Node) o;
            return other.latLng.equals(latLng);
        } else {
            return false;
        }
    }

    public Node nodeLiesOnConnectedPath(Node check) {
        for (Node connected : mConnected) {
            if ((distance(this, check) + distance(check, connected) - distance(this, connected)) < 0.000001) {
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
        return latLng.toString();
    }
}
