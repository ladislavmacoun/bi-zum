package bi.zum.lab3;

import cz.cvut.fit.zum.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.data.Edge;
import java.util.Random;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Ladislav Macoun
 */
@ServiceProvider(service = AbstractEvolution.class)
public class Evolution extends AbstractEvolution<Individual> implements Runnable {

    private int convergenceCnt;
    
    /**
     * start and final average fitness
     */
    private Pair<Double, Double> avgFitness;
    /**
     * start and final best fitness in whole population
     */
    private Pair<Double, Double> bestFitness;
    /**
     * start and final time
     */
    private Pair<Long, Long> time;
    /**
     * How often to print status of evolution
     */
    private int debugLimit = 100;
    private Random rand = new Random();
    
    /**
     * The population to be used in the evolution
     */
    Population population;

    public Evolution() {
        isFinished = false;
        avgFitness = new Pair<Double, Double>();
        bestFitness = new Pair<Double, Double>();
        time = new Pair<Long, Long>();    
        convergenceCnt = 0;
        
    }

    @Override
    public String getName() {
        return "My evolution";
    }

    @Override
    public void run() {
        
        // Initialize the population
        population = new Population(this, populationSize); 
        double prev_fitness;
       
        Random random = new Random();
  
        // Collect initial system time, average fitness, and the best fitness
        time.a = System.currentTimeMillis();
        avgFitness.a = population.getAvgFitness();
        AbstractIndividual best = population.getBestIndividual();
        bestFitness.a = best.getFitness();   
        prev_fitness = best.getFitness();

        // Show on map
        updateMap(best);
        //System.out.println(population);

        
        // Run evolution cycle for the number of generations set in GUI
        for(int g=0; g < generations; g++) {
          
            // the evolution may be terminate from the outside using GUI button
            if (isFinished) {
                break;
            } 
            
            if (hasConverge()) {
                 System.out.println("Castastrophe cleaning");
                int i = (int) (StateSpace.nodesCount() * 0.1);
                population.sortByFitness();
                while (i != StateSpace.nodesCount()) {
                    population.getIndividuals()[i] = null;
                }
            }
                
            // initialize the next generation's population
            ArrayList<AbstractIndividual> newInds = new ArrayList<AbstractIndividual>();
            
            // elitism: Preserve the best individual
            // (this is quite exploatory and may lead to premature convergence!)
            newInds.add(population.getBestIndividual().deepCopy());

            double pc = getPc(g, generations, crossoverProbability, 0.5);
            double pm = getPm(g, generations, Math.log(StateSpace.nodesCount()), mutationProbability, 0.01, population.getBestIndividual().getFitness());
            // keep filling the new population while not enough individuals in there
            while(newInds.size() < populationSize) {
                
                // select 2 parents
                List<AbstractIndividual> parents = population.selectIndividuals(2);
                
                Pair<AbstractIndividual,AbstractIndividual> offspring;
                
                // with some probability, perform crossover
                if(pc < random.nextDouble()) {
                    offspring = parents.get(0).deepCopy().crossover(
                                    parents.get(1).deepCopy());
                }
                // otherwise, only copy the parents
                else {
                    offspring = new Pair<AbstractIndividual, AbstractIndividual>();
                    offspring.a = parents.get(0).deepCopy();
                    offspring.b = parents.get(1).deepCopy();
                }
                
                // mutate first offspring, add it to the new population
                offspring.a.mutate(pm);
                offspring.a.computeFitness();
                newInds.add(offspring.a);
                
                // if there is still space left in the new population, add also
                // the second offspring
                if(newInds.size() < populationSize) {
                    offspring.b.mutate(pm);
                    offspring.b.computeFitness();
                    newInds.add(offspring.b);
                }
            }
            
            // replace the current population with the new one
            for(int i=0; i<newInds.size(); i++) {
                population.setIndividualAt(i, newInds.get(i));
            }

            // print statistic
            System.out.println("gen: " + g + "\t bestFit: " + population.getBestIndividual().getFitness() + "\t avgFit: " + population.getAvgFitness());
            System.out.println("pc:" + pc + "\t pm:" + pm);
            // for very long evolutions print best individual each 1000 generations

            if (g % debugLimit == 0) {
                best = population.getBestIndividual();
                updateMap(best);
            }
            
            if (Math.abs(prev_fitness - population.getBestIndividual().getFitness()) < 0.1) {
                convergenceCnt++;
            }
            updateGenerationNumber(g);
            prev_fitness = population.getBestIndividual().getFitness();
            
        }

        // === END ===
        time.b = System.currentTimeMillis();
        population.sortByFitness();
        avgFitness.b = population.getAvgFitness();
        best = population.getBestIndividual();
        bestFitness.b = best.getFitness();
        updateMap(best);
        System.out.println("Evolution has finished after " + ((time.b - time.a) / 1000.0) + " s...");
        System.out.println("avgFit(G:0)= " + avgFitness.a + " avgFit(G:" + (generations - 1) + ")= " + avgFitness.b + " -> " + ((avgFitness.b / avgFitness.a) * 100) + " %");
        System.out.println("bstFit(G:0)= " + bestFitness.a + " bstFit(G:" + (generations - 1) + ")= " + bestFitness.b + " -> " + ((bestFitness.b / bestFitness.a) * 100) + " %");
        System.out.println("bestIndividual= " + population.getBestIndividual());
        //System.out.println(pop);

        isFinished = true;
        System.out.println("========== Evolution finished =============");
    }
    
    public boolean hasConverge() { 
        /**
         * If there are the same fitness value for 50 generations return true 
         * */
        return convergenceCnt > 50;
    }
    
    private int getAmount(int t, int a, int Tgen, int n0) {
        return (int) Math.exp(((-a * t) / Tgen) * n0);  
    }
    
    private double getPc(int t, int Tgen, double PcMax, double PcMin) {
        return ((PcMax - PcMin) / (1 + Math.exp(-9.903438 * (Tgen - 2*t) / Tgen))) + PcMin;
    }
    
    private double getPm(int t, int Tgen, double fmax, double PmMax, double PmMin, double fitness) {
    
        return Math.exp((fmax - fitness) / fmax ) * ((PmMax - PmMin) / (1 + Math.exp((-9.903438*(Tgen - 2*t))/Tgen))) + PmMin;
    }
}
