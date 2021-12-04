package bearmaps.proj2c;

import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;
import bearmaps.proj2ab.KDTree;
import bearmaps.proj2ab.Point;

import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

    private KDTree kdTree;
    // key is a clean name, value is a list of all the nodes with the same clean name
    private Map<String, List<Node>> cleanNameMap;
    // a trie of clean names of all the nodes
    private MyTrieSet cleanNameTrie;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        // You might find it helpful to uncomment the line below:
        List<Node> nodes = this.getNodes();
        List<Point> nodesWithNbs = new ArrayList<>();
        cleanNameMap = new HashMap<>();
        cleanNameTrie = new MyTrieSet();

        for (Node node : nodes) {
            if (node.name() != null) {
                String cleanName = cleanString(node.name());
                cleanNameMap.putIfAbsent(cleanName, new ArrayList<>());
                cleanNameMap.get(cleanName).add(node);
                cleanNameTrie.add(cleanName);
            }

            if (!this.neighbors(node.id()).isEmpty()) {
                nodesWithNbs.add(node);
            }
        }
        kdTree = new KDTree(nodesWithNbs);


    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude. Only consider vertices that have neighbors.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        Node nearest = (Node) kdTree.nearest(lon, lat);
        return nearest.id();
    }


    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        List<String> cleanNames = cleanNameTrie.keysWithPrefix(cleanString(prefix));

        // Use a set to store the original names and remove duplicate names
        Set<String> names = new HashSet<>();
        for (String cleanName : cleanNames) {
            List<Node> nodes = cleanNameMap.get(cleanName);
            for (Node n : nodes) {
                names.add(n.name());
            }
        }
        List<String> namesList = new ArrayList<>(names);
        Collections.sort(namesList);
        return namesList;
    }

    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        List<Node> locNodes = cleanNameMap.getOrDefault(cleanString(locationName), new ArrayList<>());
        List<Map<String, Object>> locList = new ArrayList<>();
        for (Node n : locNodes) {
            Map<String, Object> map = new HashMap<>();
            map.put("lat", n.lat());
            map.put("lon", n.lon());
            map.put("name", n.name());
            map.put("id", n.id());
            locList.add(map);
        }
        return locList;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
