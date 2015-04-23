package com.mvhsapp.app.pathfinding;

/**
 * path aka 2 nodes
 */
public class Path {
    private Node[] mNodes;

    public Path(Node node1, Node node2) {
        mNodes = new Node[2];
        mNodes[0] = node1;
        mNodes[1] = node2;
    }

    @Override
    public int hashCode() {
        return mNodes[0].hashCode() * mNodes[1].hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Path) {
            Path other = (Path) o;
            if (other.mNodes[0].equals(mNodes[0]) && other.mNodes[1].equals(mNodes[1]) ||
                    other.mNodes[0].equals(mNodes[1]) && other.mNodes[1].equals(mNodes[0])) {
                return true;
            }
        }
        return super.equals(o);
    }
}
