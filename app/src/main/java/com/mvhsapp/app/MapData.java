package com.mvhsapp.app;

import com.mvhsapp.app.pathfinding.LocationNode;
import com.mvhsapp.app.pathfinding.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map Data
 */
public class MapData {


    public static final Set<LocationNode> locationNodeSet;
    public static final Map<Node, Node> pathNodeMap;
    private static final double[][] WING_600 = {
            {37.361031, -122.067937}
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

    static {
        locationNodeSet = new HashSet<>();
        for (int i = 0; i < WING_600.length; i++) {
            LocationNode node = new LocationNode(WING_600[i][0], WING_600[i][1], "600", WING_600_NAMES[i]);
            locationNodeSet.add(node);
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
}
