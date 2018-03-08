package bi.zum.lab2;

import bi.zum.lab1.PathRecSearch;
import bi.zum.lab2.util.Euclidean;
import bi.zum.lab2.util.ZumPriorityQueue;
import cz.cvut.fit.zum.api.AbstractAlgorithm;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.UninformedSearch;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomáš Řehořek
 */
@ServiceProvider(service = AbstractAlgorithm.class)
public class Dijkstra extends PathRecSearch implements UninformedSearch {

    private ZumPriorityQueue<Node> open;
    private HashSet<Node> closed;
    
    @Override
    public String getName() {
        return "Dijkstra";
    }
    
    @Override
    public List<Node> findPath(Node startNode) {
        
        open = new ZumPriorityQueue<Node>();
        closed = new HashSet<Node>();
        prev = new HashMap<Node, Node>();
        
        Map<Node, Double> dist = new HashMap<Node, Double>();
        
        open.enqueue(startNode, 0);
        dist.put(startNode, 0.0);
        
        while (!open.isEmpty()) {
            
            Node curNode = open.dequeue();
            
            if (curNode.isTarget())
                return buildPath(curNode);
            
            for (Node adj : curNode.expand()) {
                
                double _dist = dist.get(curNode) + Euclidean.distance(curNode, adj);
                
                if (!dist.containsKey(adj) || _dist < dist.get(adj)) {
                    
                    if (!dist.containsKey(adj)) {
                        open.enqueue(adj, _dist);
                    } else {
                        dist.put(adj, _dist);
                    }
                    
                    dist.put(adj, _dist);
                    prev.put(adj, curNode);
                    
                }         
            }               
        }                
        return null;
    }
}
