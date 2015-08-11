package net.mvla.mvhs.map;

/**
 * path aka 2 nodes
 */
public class TwoNodes {
    private Node[] mNodes;

    public TwoNodes(Node node1, Node node2) {
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
        } else if (o instanceof TwoNodes) {
            TwoNodes other = (TwoNodes) o;
            return other.mNodes[0].equals(mNodes[0]) && other.mNodes[1].equals(mNodes[1]) ||
                    other.mNodes[0].equals(mNodes[1]) && other.mNodes[1].equals(mNodes[0]);
        } else {
            return false;
        }
    }
}
