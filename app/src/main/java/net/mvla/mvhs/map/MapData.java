package net.mvla.mvhs.map;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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


    private final static Set<LocationNode> tempAddedNodes = new HashSet<>();
    public static Map<LatLng, LocationNode> locationNodeMap = new HashMap<>();
    public static Map<LatLng, Node> pathNodeMap = new HashMap<>();

    public static void init(String string) throws JSONException {
        JSONObject object = new JSONObject(string);

        JSONArray locations = object.getJSONArray("locationNodes");
        locationNodeMap = new HashMap<>();
        for (int i = 0; i < locations.length(); i++) {
            JSONArray location = locations.getJSONArray(i);
            String name = location.getString(0);
            JSONArray coord = location.getJSONArray(1);
            JSONArray pathCoord = location.getJSONArray(2);

            List<String> tags = new ArrayList<>();
            if (location.length() == 4) {
                JSONArray tagsArray = location.getJSONArray(3);
                for (int j = 0; j < tagsArray.length(); j++) {
                    tags.add(tagsArray.getString(j));
                }
            }
            LocationNode node = new LocationNode(
                    coord.getDouble(0), coord.getDouble(1),
                    pathCoord.getDouble(0), pathCoord.getDouble(1),
                    name,
                    tags);
            locationNodeMap.put(node.latLng, node);
        }

        JSONArray paths = object.getJSONArray("pathNodes");
        pathNodeMap = new HashMap<>();
        for (int j = 0; j < paths.length(); j++) {
            //Iterate through each path
            List<Node> added = new ArrayList<>();

            JSONArray path = paths.getJSONArray(j);

            for (int k = 0; k < path.length(); k++) {
                JSONArray pathNode = path.getJSONArray(k);
                Node node = new Node(pathNode.getDouble(0), pathNode.getDouble(1));
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
                    int r = i + 1;
                    added.get(i).addConnected(added.get(r));
                    added.get(r).addConnected(added.get(i));
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

    public static boolean nodesLiesOnOnePath(List<Node> check) {
        if (check.size() == 2) {
            return true;
        }
        boolean lies = true;
        Node start = check.get(0);
        Node end = check.get(check.size() - 1);
        for (int i = 1; i < check.size() - 1; i++) {
            Node checking = check.get(i);
            if (!((Node.distance(start, checking) + Node.distance(checking, end) - Node.distance(start, end)) < 0.000001)) {
                lies = false;
            }
        }
        return lies;
    }
}
