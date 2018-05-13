package bi.zum.lab3;

import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;
import java.util.ArrayList;
import java.util.Random;


/**
 * @author Ladislav Macoun
 */
public class Individual extends AbstractIndividual {

    private double fitness = Double.NaN;
    private AbstractEvolution evolution;
    
    int[] genome;
    //StateSpaceOptimized myStateSpace = new StateSpaceOptimized();
    
    /**
     * Creates a new individual
     * 
     * @param evolution The evolution object
     * @param randomInit <code>true</code> if the individial should be
     * initialized randomly (we do wish to initialize if we copy the individual)
     */
    public Individual(AbstractEvolution evolution, boolean randomInit) {
        this.evolution = evolution;
        
        /* Inicialize genome for whole statespace */
        this.genome = new int[StateSpace.nodesCount()];
        
        if(randomInit) {
            
            for (int i = 0; i < StateSpace.nodesCount(); i++) {
                genome[i] = 0;
            }
          
            /**
             * Heuristic initialization of genome 
             * */
            ArrayList<Edge> covred_edges = new ArrayList<>();

            for (Edge e : StateSpace.getEdges()) {
                if (!covred_edges.contains(e)) {
                    Node from = StateSpace.getNode(e.getFromId());
                    Node to = StateSpace.getNode(e.getToId());

                    // If node is leaf assign it always value
                    if (from.getEdges().size() == 1) {
                        genome[from.getId()] = 2;
                        genome[to.getId()] = 3;
                    } else if (to.getEdges().size() == 1) {
                         genome[from.getId()] = 3;
                        genome[to.getId()] = 2;
                    }
                    if (genome[from.getId()] != 2 || genome[from.getId()] != 3 ||
                        genome[to.getId()] != 2 || genome[to.getId()] != 3) {
                        if (from.getEdges().size() > to.getEdges().size()) {
                            genome[e.getFromId()] = 1;
                        } else {
                            genome[e.getToId()] = 1;
                        }
                        covred_edges.add(e);
                    
                    
                    }
         
                }
            } 
        }
    }

    @Override
    public boolean isNodeSelected(int j) {
        
        switch (genome[j]) {
            case 0 :
                return false;
            case 1 : 
                return true;
            case 2 : 
                return false;
            case 3 : 
                return true;
         default:
             return false;
        }
    }

    /**
     * Evaluate the value of the fitness function for the individual. After
     * the fitness is computed, the <code>getFitness</code> may be called
     * repeatedly, saving computation time.
     */
    @Override
    public void computeFitness() {
        
        int bonus = StateSpace.nodesCount();      
        int penalization = 0;
        
        repair();
        
        /* Penalize invalid solution */
        //result = result - repair();
               
        /* Fitness: less covered vertices is better */
        for (int i = 0; i < genome.length; i++) {
            if (genome[i] > 0) {
                penalization ++;
            } else {
                bonus++;
            }
        }
        
        fitness = Math.exp(Math.log(bonus) - Math.log(penalization));
        //fitness = bonus - penalization;
    }

    /**
     * Only return the computed fitness value
     *
     * @return value of fitness fucntion
     */
    @Override
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Does random changes in the individual's genotype, taking mutation
     * probability into account.
     * 
     * @param mutationRate Probability of a bit being inverted, i.e. a node
     * being added to/removed from the vertex cover.
     */
    @Override
       public void mutate(double mutationRate) {
        for (int i = 0;  i < StateSpace.nodesCount(); i ++) {
            if ( mutationRate < Math.random() ) {
                genome[i] = genome[i] > 0 ? 0 : 1;
            } 
        }
    }

    
    /**
     * Crosses the current individual over with other individual given as a
     * parameter, yielding a pair of offsprings.
     * 
     * @param other The other individual to be crossed over with
     * @return A couple of offspring individuals
     */
    @Override
    public Pair crossover(AbstractIndividual other) {

        Pair<Individual,Individual> result = new Pair();

        Individual indi_a = (Individual)other;
        Individual indi_b = (Individual)this;
        
        result.a = new Individual(evolution, false);
        result.b = new Individual(evolution, false);
        
        
        int cut = StateSpace.nodesCount() / 2;
        int rest = StateSpace.nodesCount() - cut;
        
        for (int i = 0; i < cut; i++) {
            result.a.genome[i] = indi_a.genome[i];
            result.b.genome[i] = indi_b.genome[i];
        }
        
        for (int i = cut; i < rest; i++) {
            result.a.genome[i] = indi_b.genome[i];
            result.b.genome[i] = indi_a.genome[i];
        }
        
        return result;
    }


    
    /**
     * When you are changing an individual (eg. at crossover) you probably don't
     * want to affect the old one (you don't want to destruct it). So you have
     * to implement "deep copy" of this object.
     *
     * @return identical individual
     */
    @Override
    public Individual deepCopy() {
        Individual newOne = new Individual(evolution, false);         
        newOne.genome = new int[this.genome.length];
        System.arraycopy(this.genome, 0, newOne.genome, 0, this.genome.length);   
        newOne.fitness = this.fitness;
        return newOne;
    }

    /**
     * Return a string representation of the individual.
     *
     * @return The string representing this object.
     */
    @Override
    public String toString() {
        String ret = "[";
        for (int i = 0; i < StateSpace.nodesCount(); i++) {
            ret += genome[i] + ", ";
        }
        ret += "]";
        return ret;
    }
    
    /**
    * Repairs the genotype to make it valid, i.e. ensures all the edges
    * are in the vertex cover.
    */
    private void repair() {
        /* We iterate over all the edges */
        for(Edge e : StateSpace.getEdges()) {
            
            /* If there is not either one of the vertecies from 
             * the edge add random  one */
            if ((genome[e.getFromId()] == 0 || genome[e.getFromId()] == 2 ) 
                    && (genome[e.getToId()] == 0 || genome[e.getToId()] == 2)) {
                if (Math.random() > 0.5) {
                    genome[e.getFromId()] =  1;              
                } else {
                    genome[e.getToId()] = 1;
                }               
            }
        }
    }    
}
