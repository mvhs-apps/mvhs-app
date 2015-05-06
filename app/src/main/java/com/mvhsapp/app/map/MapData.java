package com.mvhsapp.app.map;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map Data
 */
public class MapData {


    public static final Map<LatLng, LocationNode> locationNodeMap;
    public static final Map<LatLng, Node> pathNodeMap;
    private static final double[][][] LOCATIONS = {
            //600
            {{37.361031, -122.067937}, {37.360991, -122.067937}},
            {{37.361031, -122.067104}, {37.360991, -122.067104}},

            //100
            {{37.360911, -122.067933}, {37.360911, -122.068003}},
            {{37.360784, -122.067930}, {37.360784, -122.068003}},

            //200
            {{37.360480, -122.067557}, {37.360480, -122.067525}},
            {{37.360651, -122.067375}, {37.360651, -122.067411}},
            {{37.360465, -122.067396}, {37.360465, -122.067411}},
            {{37.360640, -122.067219}, {37.360640, -122.067411}},

            //300
            {{37.360328, -122.067042}, {37.360328, -122.066982}},
            {{37.360200, -122.067042}, {37.360200, -122.066982}},
            {{37.360331, -122.066916}, {37.360331, -122.066982}},
            {{37.360210, -122.066915}, {37.360210, -122.066982}},


            //500
            {{37.359297, -122.066672}, {37.359297, -122.066713}},
            {{37.359381, -122.066430}, {37.359381, -122.066387}},
            {{37.359243, -122.066430}, {37.359381, -122.066387}},
            {{37.359340, -122.066339}, {37.359340, -122.066387}},
            {{37.359250, -122.066339}, {37.359250, -122.066387}},

            //MISC
            {{37.360164, -122.068172}, {37.360164, -122.068003}},
            {{37.359823, -122.068175}, {37.359823, -122.068003}},
            {{37.359699, -122.068172}, {37.359699, -122.068003}},
            {{37.359504, -122.068189}, {37.359504, -122.068003}},
            {{37.359724, -122.067753}, {37.359724, -122.068003}},
            {{37.359683, -122.067890}, {37.359724, -122.068003}},
            {{37.359560, -122.067905}, {37.359724, -122.068003}},
            {{37.359531, -122.067726}, {37.359724, -122.068003}},
            {{37.359637, -122.067526}, {37.359724, -122.068003}},
            {{37.359964, -122.067123}, {37.359964, -122.067411}},
            {{37.359961, -122.066750}, {37.359961, -122.067411}},
    };
    private static final String[] LOCATION_NAMES = {
            "601", "612",
            "101", "102",
            "208", "209", "210", "211",
            "315", "316", "317", "318",
            "520", "521", "522", "523", "524",
            "Theater", "Cafeteria ", "Food Service", "Packard Hall", "Library", "Conf Room", "Coll & Career ", "TBC", "Tutorial Center", "Main Gym", "Weight Room"
    };

    private static final double[][][] PATHS = {
            //lat long, y x

            //Main path
            {{37.360991, -122.068003}, {37.360720, -122.068003}, {37.360408, -122.068003},
                    {37.360150, -122.068003}, {37.359889, -122.068003}, {37.359485, -122.068003},
                    {37.359218, -122.068003}},

            //Second vertical path
            {{37.360991, -122.067745}, {37.360720, -122.067745}, {37.360408, -122.067745},
                    {37.360150, -122.067745}},

            //Third vertical
            {{37.360991, -122.067525}, {37.360720, -122.067525}, {37.360408, -122.067525}},

            //Fourth vertical
            {{37.360720, -122.067411}, {37.360408, -122.067411}, {37.360150, -122.067411}, {37.359889, -122.067411}, {37.359781, -122.067411}, {37.359485, -122.067411}, {37.359218, -122.067411}},

            //Fifth vertical
            {{37.360991, -122.066982}, {37.360720, -122.066982}, {37.360408, -122.066982}, {37.360150, -122.066982}},

            //Sixth vertical
            {{37.360991, -122.066300}, {37.360408, -122.066300}, {37.360150, -122.066300}},

            //600
            {{37.360991, -122.068003}, {37.360991, -122.067745}, {37.360991, -122.067525}, {37.360991, -122.066982}, {37.360991, -122.066300}},

            //100
            {{37.360720, -122.068003}, {37.360720, -122.067745}, {37.360720, -122.067525}, {37.360720, -122.067411}, {37.360720, -122.066982}, {37.360408, -122.066580}},

            //200
            {{37.360408, -122.068003}, {37.360408, -122.067745}, {37.360408, -122.067525}, {37.360408, -122.067411}, {37.360408, -122.066982}, {37.360408, -122.066580}, {37.360408, -122.066300}},

            //300
            {{37.360150, -122.068003}, {37.360150, -122.067745}, {37.360150, -122.067411}, {37.360150, -122.066982}, {37.360150, -122.066300}},

            //400
            {{37.359889, -122.068003}, {37.359889, -122.067411}, {37.359781, -122.067411},
                    {37.359781, -122.066713}, {37.359781, -122.066267}, {37.359674, -122.066267}},

            //500
            {{37.359485, -122.068003}, {37.359485, -122.067411}, {37.359485, -122.066713},
                    {37.359532, -122.066713}, {37.359532, -122.066387}, {37.359532, -122.066267},
                    {37.359470, -122.066267}},

            //400/500 end connection
            {{37.359674, -122.066267}, {37.359532, -122.066267}},

            //Side
            {{37.359218, -122.068003}, {37.359218, -122.067411}, {37.359218, -122.066713}, {37.359218, -122.066387}},

            //4/5/side 419 connection
            {{37.359781, -122.066713}, {37.359532, -122.066713}, {37.359485, -122.066713}, {37.359218, -122.066713}},

            //5/side 519 connection
            {{37.359532, -122.066387}, {37.359218, -122.066387}}
    };

    private final static Set<LocationNode> tempAddedNodes = new HashSet<>();

    static {
        locationNodeMap = new HashMap<>();
        int count = 0;
        for (double[][] room : LOCATIONS) {
            LocationNode node = new LocationNode(
                    room[0][0], room[0][1],
                    room[1][0], room[1][1],
                    LOCATION_NAMES[count]);
            locationNodeMap.put(node.latLng, node);
            count++;
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
                    added.set(i, pathNodeMap.get(nodeAdd.latLng));
                }
            }

            //TODO: remove duplicate nodes (after using the ones from the path already added)

            //Connect all added nodes in the path up, add it to node map
            for (int i = 0; i < added.size(); i++) {
                if (i != added.size() - 1) {
                    int j = i + 1;
                    added.get(i).addConnected(added.get(j));
                    added.get(j).addConnected(added.get(i));
                }
                pathNodeMap.put(added.get(i).latLng, added.get(i));
            }
        }

    }

    /**
     * Finds path.
     *
     * @param start Node from path node map
     * @param end   Node with co-ords of end (a location node)
     * @return path
     */
    public static List<Node> findPath(LatLng start, LatLng end) {
        cleanTempNodes();
        LocationNode endNode = MapData.addTempLocationNodeToPaths(end);
        Node startNode = pathNodeMap.get(start);
        if (locationNodeMap.containsKey(start)) {
            startNode = MapData.addTempLocationNodeToPaths(start);
        }
        SortedNodeList openList = new SortedNodeList();
        List<Node> closedList = new ArrayList<>();

        openList.setTarget(endNode);
        openList.add(startNode);
        while (true) {
            if (openList.size() == 0) {
                return null;
            }
            Node lowestF = openList.first();
            openList.remove(lowestF);
            closedList.add(lowestF);
            if (lowestF.equals(endNode)) {
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
        Node child = endNode;
        path.add(endNode);
        while (!child.equals(startNode)) {
            Node parent = child.getParent();
            path.add(parent);
            child = parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static LocationNode addTempLocationNodeToPaths(LatLng latLng) {
        Map<LatLng, Node> added = new HashMap<>();
        LocationNode locationNode = locationNodeMap.get(latLng);

        if (locationNode == null) {
            return null;
        } else if (pathNodeMap.containsKey(locationNode.latLng)) {
            return locationNode;
        }

        added.put(locationNode.latLng, locationNode);
        added.put(locationNode.getPathNode().latLng, locationNode.getPathNode());

        //If the added node is on the line of two other nodes on node map, connect it up

        for (Node pathNode : pathNodeMap.values()) {
            for (Node nodeAdded : added.values()) {
                Node pathNodeConnected = pathNode.nodeLiesOnConnectedPath(nodeAdded);
                if (pathNodeConnected != null && !pathNodeConnected.equals(nodeAdded)) {
                    nodeAdded.addConnected(pathNode);
                    nodeAdded.addConnected(pathNodeConnected);
                    pathNodeConnected.addConnected(nodeAdded);
                    pathNodeConnected.removeConnected(pathNode);
                    pathNode.addConnected(nodeAdded);
                    pathNode.removeConnected(pathNodeConnected);
                    break;
                }
            }
        }

        pathNodeMap.putAll(added);
        tempAddedNodes.add(locationNode);
        return locationNode;
    }

    public static void cleanTempNodes() {
        for (Node node : tempAddedNodes) {
            removeLocationNode((LocationNode) node);
        }
        tempAddedNodes.clear();
    }

    private static void removeLocationNode(LocationNode node) {
        Node locationPathNode = node.getPathNode();
        List<Node> locationPathNodeConnected = new ArrayList<>(locationPathNode.getConnected());
        locationPathNodeConnected.remove(node);
        if (locationPathNodeConnected.size() == 2) {
            locationPathNodeConnected.get(0).addConnected(locationPathNodeConnected.get(1));
            locationPathNodeConnected.get(1).addConnected(locationPathNodeConnected.get(0));
            locationPathNodeConnected.get(0).removeConnected(locationPathNode);
            locationPathNodeConnected.get(1).removeConnected(locationPathNode);
            locationPathNode.removeConnected(locationPathNodeConnected.get(0));
            locationPathNode.removeConnected(locationPathNodeConnected.get(1));
        }
        pathNodeMap.remove(locationPathNode.latLng);
        pathNodeMap.remove(node.latLng);
    }

    public static boolean onCampus(Node node) {
        return node.latLng.latitude > 37.356813 && node.latLng.latitude < 37.361323
                && node.latLng.longitude > -122.068730 && node.latLng.longitude < -122.065080;
    }
}
