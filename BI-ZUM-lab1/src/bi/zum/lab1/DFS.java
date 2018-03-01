package bi.zum.lab1;

import cz.cvut.fit.zum.api.AbstractAlgorithm;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.UninformedSearch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.openide.util.lookup.ServiceProvider;

/**
 * Depth-first search
 *
 * @see http://en.wikipedia.org/wiki/Depth-first_search
 */
@ServiceProvider(service = AbstractAlgorithm.class, position = 10)
public class DFS extends AbstractAlgorithm implements UninformedSearch {

    private LinkedList<Node> opened;
    private HashSet<Node> closed;
    private Map<Node, Node> prev;
    private List<Node> path;

    @Override
    public String getName() {
        return "DFS";
    }
    
    private List<Node> buildPath(Node start, Node target) {
        path = new ArrayList<Node>();
        path.add(target);
        
        Node path_step = prev.get(target);
        while (path_step != start) {
            path.add(path_step);
            path_step = prev.get(path_step);
        }       
        return path;
    }   
    
    
    @Override
    public List<Node> findPath(Node startNode) 
    {
        opened = new LinkedList<Node>();
        closed = new HashSet<Node>();
        prev = new HashMap<Node, Node>();
        path = null;
     
        Stack<Node> stack = new Stack<Node>();
        stack.push(startNode); 
        
        while (!stack.isEmpty()) {
            Node v = stack.pop(); 
            
            if (v.isTarget()) {               
                return buildPath(startNode, v);
            }
            
            opened.add(v);
            
            for (Node adj : v.expand()) {
                if (!opened.contains(adj) && !closed.contains(adj)) {
                    prev.put(adj, v);
                    stack.push(adj);
                }
            }            
            closed.add(v);
        }
        
        return path;
    }
    
    
    public void DFS_find(Node v)
    {
        if (v.isTarget()) {
            return;
        }
        
        /* If is already opened or closed skip */
        if (closed.contains(v) || opened.contains(v)) {
            return;
        }
        
        path.add(v);       
        opened.add(v);
        
        for (Node adj : v.expand()) {
            DFS_find(adj);
        }
        
        closed.add(v);
    }
}
