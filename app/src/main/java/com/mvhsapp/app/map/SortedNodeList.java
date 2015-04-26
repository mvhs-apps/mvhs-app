package com.mvhsapp.app.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortedNodeList {

    private List<Node> list = new ArrayList<>();
    private Node mTarget;

    public Node first() {
        return list.get(0);
    }

    public void clear() {
        list.clear();
    }

    public void setTarget(Node target) {
        mTarget = target;
    }

    public void add(Node o) {
        o.updateGH(mTarget);
        list.add(o);
        Collections.sort(list);
    }

    public void remove(Node o) {
        list.remove(o);
    }

    public int size() {
        return list.size();
    }

    public boolean contains(Node o) {
        return list.contains(o);
    }

    public void sort() {
        for (Node n : list) {
            n.updateGH(mTarget);
        }
        Collections.sort(list);
    }
}