package com.mvhsapp.app.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Map Data
 */
public class MapData {


    public static final Map<Node, LocationNode> locationNodeMap;
    public static final Map<Node, Node> pathNodeMap;
    private static final double[][][] WING_600 = {
            //601
            {{37.361031, -122.067937}, {37.360991, -122.067937}}
    };
    private static final String[] WING_600_NAMES = {"601"};
    private static final double[][][] PATHS = {
            //lat long, y x
            //600
            {{37.360991, -122.067985},
                    {37.360991, -122.066269}},

            //Main path
            {{37.361234, -122.067985},
                    {37.359545, -122.067985}},
            //100
            {{37.360720, -122.067985},
                    {37.360720, -122.066975}}

    };

    private static Node tempAddedNode;

    static {
        locationNodeMap = new HashMap<>();
        for (int i = 0; i < WING_600.length; i++) {
            LocationNode node = new LocationNode(WING_600[i][0][0], WING_600[i][0][1], WING_600[i][1][0], WING_600[i][1][1], WING_600_NAMES[i]);
            locationNodeMap.put(node, node);
        }

        pathNodeMap = new HashMap<>();
        for (double[][] PATH : PATHS) {
            //Iterate through each path
            List<Node> added = new ArrayList<>();

            for (double[] nodeCoords : PATH) {
                Node node = new Node(nodeCoords[0], nodeCoords[1]);
                added.add(node);
            }

            //If node already exists on node map, discard current one
            for (int i = 0; i < added.size(); i++) {
                Node nodeAdd = added.get(i);
                if (pathNodeMap.containsValue(nodeAdd)) {
                    added.set(i, pathNodeMap.get(nodeAdd));
                }
            }

            //Connect all added nodes up, add it to node map
            for (int i = 0; i < added.size(); i++) {
                for (int j = 0; j < added.size(); j++) {
                    if (i != j) {
                        added.get(i).addConnected(added.get(j));
                        added.get(j).addConnected(added.get(i));
                    }
                }
                pathNodeMap.put(added.get(i), added.get(i));
            }

            Collection<Node> oldPathNodes = new HashSet<>(pathNodeMap.values());
            oldPathNodes.removeAll(added);
            //If the added node is on the line of two other nodes on node map, connect it up
            for (Node nodeAdded : added) {
                for (Node pathNode : oldPathNodes) {
                    Node pathNodeConnected = pathNode.nodeLiesOnConnectedPath(nodeAdded);
                    if (pathNodeConnected != null && !pathNodeConnected.equals(nodeAdded)) {
                        nodeAdded.addConnected(pathNode);
                        nodeAdded.addConnected(pathNodeConnected);
                        pathNodeConnected.addConnected(nodeAdded);
                        pathNodeConnected.removeConnected(pathNode);
                        pathNode.addConnected(nodeAdded);
                        pathNode.removeConnected(pathNodeConnected);
                    }
                }
            }
            //If any node is on the line of two of the added nodes, connect it up
            for (Node pathNode : oldPathNodes) {
                for (Node nodeAdded : added) {
                    Node nodeAddedConnected = nodeAdded.nodeLiesOnConnectedPath(pathNode);
                    if (nodeAddedConnected != null && !nodeAddedConnected.equals(pathNode)) {
                        pathNode.addConnected(nodeAdded);
                        pathNode.addConnected(nodeAddedConnected);
                        nodeAdded.addConnected(pathNode);
                        nodeAdded.removeConnected(nodeAddedConnected);
                        nodeAddedConnected.addConnected(pathNode);
                        nodeAddedConnected.removeConnected(nodeAdded);
                    }
                }
            }
        }

    }

    public static List<Node> findPath(Node start, Node end) {
        cleanTempNodes();
        end = MapData.addLocationNodeToPaths(end);
        if (end == null) {
            return null;
        }
        tempAddedNode = end;
        start = MapData.pathNodeMap.get(start);
        SortedNodeList openList = new SortedNodeList();
        List<Node> closedList = new ArrayList<>();

        openList.setTarget(end);
        openList.add(start);
        while (true) {
            if (openList.size() == 0) {
                return null;
            }
            Node lowestF = openList.first();
            openList.remove(lowestF);
            closedList.add(lowestF);
            if (lowestF.equals(end)) {
                break;
            }
            for (Node n : lowestF.getConnected()) {
                if (!closedList.contains(n)) {
                    if (!openList.contains(n)) {
                        n.setParent(lowestF);
                        openList.add(n);
                    } else {
                        double gFromLowestFToN = lowestF.getG()
                                + Node.distance(lowestF, n);
                        if (gFromLowestFToN < n.getG()) {
                            n.setParent(lowestF);
                        }
                        openList.sort();
                    }
                }
            }
        }

        List<Node> path = new ArrayList<>();
        Node child = end;
        path.add(end);
        while (!child.equals(start)) {
            Node parent = child.getParent();
            path.add(parent);
            child = parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static LocationNode addLocationNodeToPaths(Node node) {
        Map<Node, Node> added = new HashMap<>();
        LocationNode locationNode = locationNodeMap.get(node);
        if (locationNode == null) {
            return null;
        }
        added.put(locationNode, locationNode);
        added.put(locationNode.getPathNode(), locationNode.getPathNode());

        //If the added node is on the line of two other nodes on node map, connect it up
        for (Node nodeAdded : added.values()) {
            for (Node pathNode : pathNodeMap.values()) {
                Node pathNodeConnected = pathNode.nodeLiesOnConnectedPath(nodeAdded);
                if (pathNodeConnected != null && !pathNodeConnected.equals(nodeAdded)) {
                    nodeAdded.addConnected(pathNode);
                    nodeAdded.addConnected(pathNodeConnected);
                    pathNodeConnected.addConnected(nodeAdded);
                    pathNodeConnected.removeConnected(pathNode);
                    pathNode.addConnected(nodeAdded);
                    pathNode.removeConnected(pathNodeConnected);
                }

            }
        }

        pathNodeMap.putAll(added);
        return locationNode;
    }

    public static void cleanTempNodes() {
        if (tempAddedNode != null) {
            removeLocationNode((LocationNode) tempAddedNode);
            tempAddedNode = null;
        }
    }

    private static void removeLocationNode(LocationNode node) {
        Node pathNode = node.getPathNode();
        List<Node> connected = new ArrayList<>(pathNode.getConnected());
        connected.remove(node);
        connected.get(0).addConnected(connected.get(1));
        connected.get(1).addConnected(connected.get(0));
        pathNode.removeConnected(connected.get(0));
        pathNode.removeConnected(connected.get(1));
        pathNodeMap.remove(pathNode);
        pathNodeMap.remove(node);
    }

    public static boolean onCampus(Node node) {
        return node.getLat() > 37.356813 && node.getLat() < 37.361323 && node.getLong() > -122.068730 && node.getLong() < -122.065080;
    }
}
