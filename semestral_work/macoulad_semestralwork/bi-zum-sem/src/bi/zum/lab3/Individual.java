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
    int cnt;  
    boolean[] genome;
    boolean[] genome_mask;
    Random rand = new Random();
    
    /**
     * Creates a new individual
     * 
     * @param evolution The evolution object
     * @param randomInit <code>true</code> if the individial should be
     * initialized randomly (we do wish to initialize if we copy the individual)
     */
     public Individual(AbstractEvolution evolution, boolean randomInit) {
         this.evolution = evolution;
         this.genome = new boolean[StateSpace.nodesCount()];
         Evolution evo = (Evolution) evolution;
         this.genome_mask = evo.mask;
         
         if (randomInit) {
            Random random = new Random();
            for (int i = 0 ; i < StateSpace.nodesCount(); i++) {
                genome[i] = random.nextBoolean();
                genome[i] |= genome_mask[i];
            }
            this.repair();
        } 
     }
     
    /**
     * Simulated SimulatedAnnealing
     * @param temp defines the pressure caused on the individual 
     *  We use the temperature tu mutate the surroudings 
     *  according to the temperature
     *
     * @referece http://www.theprojectspot.com/tutorial-post/simulated-SimulatedAnnealing-algorithm-for-beginners/6 
     */ 
    public Individual SimulatedAnnealing(int temp) {
        if (temp > 0){ 
            ArrayList<Individual> surrounding = new ArrayList<>();
            // Create a surrouding a neigborhood of 10 members
            for (int i = 0; i < 10; i++) {
                Individual neighbor = this.deepCopy();
                // Perform mutation accordingly to the temperature
                neighbor.mutate(temp);
                neighbor.repair();
                neighbor.computeFitness();
                surrounding.add(neighbor);
            }   
    
            double prev_fitness = this.fitness;
            int max_index = 0;
            
            for (int i = 0; i < 10; i++) {
                if(surrounding.get(max_index).fitness < surrounding.get(i).fitness)
                    max_index = i;
            }   

            if (prev_fitness > surrounding.get(max_index).fitness){
                cnt++;
                return this;
            } else {
                cnt = 0;
                Individual newOne = surrounding.get(max_index).deepCopy();
                return newOne;
            }   
    
        }   
        return this;
    }   


    @Override
    public boolean isNodeSelected(int j) {
        return genome[j];
    }

    /**
     * Evaluate the value of the fitness function for the individual. After
     * the fitness is computed, the <code>getFitness</code> may be called
     * repeatedly, saving computation time.
     */
    @Override
    public void computeFitness() {
        
        int bonus = 0;    
        int penalization = 0;
               
        /* Fitness: less covered vertices is better */
        for (int i = 0; i < genome.length; i++) {
            if (!genome[i]) {
                bonus ++;
            } else {
                penalization++;
            }
        }        
        fitness = Math.exp(Math.log(bonus) - Math.log(penalization));
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
        int numberOfMutations = (int) ((this.genome.length/40) * mutationRate);
        for (int i = 0; i < numberOfMutations; i++) {
            int pos = rand.nextInt(genome.length);
            genome[pos] = !genome[pos];
        } 

        this.repair();
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
        
        this.repair();
        result.a.repair();
        result.b.repair();
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
        newOne.genome = new boolean[this.genome.length];
        System.arraycopy(this.genome, 0, newOne.genome, 0, this.genome.length);
        System.arraycopy(this.genome_mask, 0, newOne.genome_mask, 0, this.genome_mask.length); 
        newOne.fitness = this.fitness;
        newOne.cnt = this.cnt;
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
            ret += genome[i] ? 1 + ", " : 0 + ", ";
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
            Node from = StateSpace.getNode(e.getFromId());
            Node to = StateSpace.getNode(e.getToId());

            if (!genome[from.getId()] && !genome[to.getId()]) {
                 genome[ (from.expand().size() > to.expand().size())
                        ? from.getId()
                        : to.getId() ] = true;
            }
            
        }
    } 
    
    public int HammingDistance(Individual other) {
        int dist = 0;
        for (int i = 0; i < StateSpace.nodesCount(); i++) {
            
            if( genome[i] != other.getGenomeAt(i)) {
                dist++;
            }
        }
        return dist;
    }
    
    public boolean getGenomeAt(int idx) {
        return this.genome[idx];
    }
}
