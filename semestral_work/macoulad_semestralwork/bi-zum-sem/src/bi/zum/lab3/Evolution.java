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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
    public boolean mask[] = initStateSpace();
    private int temperature;

    /**
     * The population to be used in the evolution
     */
    public Population population;

    public Evolution() {
        isFinished = false;
        avgFitness = new Pair<Double, Double>();
        bestFitness = new Pair<Double, Double>();
        time = new Pair<Long, Long>();
        convergenceCnt = 0;
        temperature = 10;

    }

    @Override
    public String getName() {
        return "AGA with Catastrophe, Deterministic crowding and Simulated annealing";
    }
    
    @Override
    public void run() {
        this.convergenceCnt = 0;
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("log.txt")) ) {


        // Initialize the population
        Population population = new Population(this, populationSize);


        //simulated annealing
        /*for (int i = temperature; i > 0; i--) {
            population.AnnealPopulation(i);
            avgFitness.a = population.getAvgFitness();
            AbstractIndividual best = population.getBestIndividual();
            bestFitness.a = best.getFitness();
        }*/

        Random random = new Random();

        // Collect initial system time, average fitness, and the best fitness
        time.a = System.currentTimeMillis();
        avgFitness.a = population.getAvgFitness();
        AbstractIndividual best = population.getBestIndividual();
        bestFitness.a = best.getFitness();

        // Show on map
        updateMap(best);
        //System.out.println(population);

        // Run evolution cycle for the number of generations set in GUI
        for (int g = 0; g < generations; g++) {

            // the evolution may be terminate from the outside using GUI button
            if (isFinished) {
                break;
            }

            //get best fitness from prev
            AbstractIndividual bestOfPrevGen = population.getBestIndividual();
            double best_prev = bestOfPrevGen.getFitness();
            // initialize the next generation's population
            ArrayList<AbstractIndividual> newInds = new ArrayList<>();

            // elitism: Preserve the best individual
            // (this is quite exploatory and may lead to premature convergence!)
            newInds.add(population.getBestIndividual().deepCopy());


            // keep filling the new population while not enough individuals in there
            while (newInds.size() < populationSize) {

                // select 2 parents
                List<AbstractIndividual> parents = population.selectIndividuals(2);

                Pair<AbstractIndividual, AbstractIndividual> offspring;
                
                double pc = getPc(g, generations, crossoverProbability, 0.3);
                //System.err.println("pc:" + pc);

                // with some probability, perform crossover
                if (pc < random.nextDouble()) {
                    offspring = parents.get(0).deepCopy().crossover(
                            parents.get(1).deepCopy());
                }
                // otherwise, only copy the parents
                else {
                    offspring = new Pair<>();
                    offspring.a = parents.get(0).deepCopy();
                    offspring.b = parents.get(1).deepCopy();
                }

                // mutate first offspring, add it to the new population
                offspring.a.computeFitness();
                double pm = getPm(g, generations, Math.log(populationSize), mutationProbability, 0.035, offspring.a.getFitness());
                offspring.a.mutate(pm);
                //System.err.println("pm:" + pm);
                newInds.add(offspring.a);

                // if there is still space left in the new population, add also
                // the second offspring
                if (newInds.size() < populationSize) {
                    offspring.b.computeFitness();
                    pm = getPm(g, generations, Math.log(populationSize), mutationProbability, 0.035, offspring.b.getFitness());
                    offspring.b.mutate(pm);
                    //System.err.println("pm:" + pm);                 
                    newInds.add(offspring.b);
                }
                
                
                // Deterministic crowding
                if (Dist(offspring.a, parents.get(0)) + Dist(offspring.b, parents.get(1)) <= Dist(offspring.b, parents.get(0)) + Dist(offspring.a, parents.get(1))) {
                    if (offspring.a.getFitness() > parents.get(0).getFitness()) {
                        newInds.add(offspring.a);
                    } else {
                        newInds.add(parents.get(0));
                    }
                    
                    if (newInds.size() < populationSize && offspring.b.getFitness() > parents.get(1).getFitness()) {
                        newInds.add(offspring.b);
                    
                    } else if (newInds.size() < populationSize) {
                        newInds.add(parents.get(1));
                    }
                } else {
                    
                    if (offspring.a.getFitness() > parents.get(1).getFitness()) {
                        newInds.add(offspring.a);
                    } else {
                        newInds.add(parents.get(1));
                    }
                    
                    if (newInds.size() < populationSize && offspring.b.getFitness() > parents.get(0).getFitness()) {
                        newInds.add(offspring.b);
                    
                    } else if (newInds.size() < populationSize) {
                        newInds.add(parents.get(0));
                    }            
                }
            }

            // replace the current population with the new one
            for (int i = 0; i < newInds.size(); i++) {
                population.setIndividualAt(i, newInds.get(i));
            }

            if (best_prev == population.getBestIndividual().getFitness()) {
                convergenceCnt++;
            } else {
                 convergenceCnt = 0;
            }

            // Catastrophe
            if (hasConverge()) {
                
                this.convergenceCnt = 0;
                List<AbstractIndividual> elites = population.selectIndividuals(populationSize * 2/20);
                List<AbstractIndividual> survivors = RegeneratePopulation(elites, populationSize);
                
                for (int i = 0; i < populationSize; i++) {
                    population.setIndividualAt(i, survivors.get(i).deepCopy());
                }  
             }
            
            // print statistic
            System.out.println("gen: " + g + "\t bestFit: " + population.getBestIndividual().getFitness() + "\t avgFit: " + population.getAvgFitness());
            bw.write(g + "," + population.getBestIndividual().getFitness() + "," + population.getAvgFitness() +"\n");


            if (g % debugLimit == 0) {
                best = population.getBestIndividual();
                updateMap(best);
            }
            updateGenerationNumber(g);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasConverge() {
        /**
         * If there are the same fitness value for 15 generations return true 
         */
        return convergenceCnt > 15;
    }

    private int getAmount(int t, int Tgen, int n0) {
        int amount = (int) ( Math.exp( -10 * t / Tgen) * n0);
        System.err.println("Amount" + amount);
        return amount = amount < 1 ? 1 : amount;
    }

    /**
     * Adaptive crossover probability
     * */
    private double getPc(int t, int Tgen, double PcMax, double PcMin) {
        
        return ((PcMax - PcMin) / (1 + Math.exp(-9.903438 * (Tgen - 2 * t) / Tgen))) + PcMin;
    }

    /**
     * Adaptive mutation probability
     * */
    private double getPm(int t, int Tgen, double fmax, double PmMax, double PmMin, double fitness) {
        
        return Math.exp((fmax - fitness) / fmax) * ((PmMax - PmMin) / (1 + Math.exp((-9.903438 * (Tgen - 2 * t)) / Tgen))) + PmMin;
    }
    
    public List<AbstractIndividual> selectElites(int count) {
        this.population.sortByFitness();
        ArrayList<AbstractIndividual> elites = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            elites.add(this.population.getIndividual(i));
        }
        
        return elites;
    }
      
    private List<AbstractIndividual> RegeneratePopulation(List<AbstractIndividual> elites, int pSize) {
        List<AbstractIndividual> survivors = new ArrayList<>();
        //List<AbstractIndividual> elites = population.selectIndividuals((int) (pSize * 0.90));
        
        for (AbstractIndividual e : elites) {
            survivors.add(e.deepCopy());
        }
        
        Population rngPopulation = new Population(this, pSize);
        List<AbstractIndividual> rest = rngPopulation.selectIndividuals(pSize - survivors.size());
        
        for (AbstractIndividual r : rest) {
            survivors.add(r.deepCopy());
        }
        
        return survivors;
    }     
    
    /** 
     * Computes the hamming distance between two individual's genomes 
     * */
    public int Dist(AbstractIndividual _a, AbstractIndividual _b) {
        int dist = 0;
        Individual a = (Individual) _a;
        Individual b = (Individual) _b;
        for (int i = 0; i < StateSpace.nodesCount(); i++) {
            if (a.getGenomeAt(i) != b.getGenomeAt(i)) {
                dist++;
            }
        }
        return dist;
    }    
    
    /**
     * 
     * Iterative leaf cutting
     * Goes through the statespace and looks for leaf. If leaf is found add it 
     * to the genome mask as permanet false, and permanent true for its predaccesor
     * 
     * */
    public boolean [] initStateSpace() {
    boolean[] result = new boolean[StateSpace.nodesCount()];
        Set<Node> removedNodes = new HashSet<>(); // Keep track of visited nodes
        Set<Edge> removedEdges = new HashSet<>(); // Keep track of visited edges
        int count = 0;
        do {
            count = 0;
            for (Node node : StateSpace.getNodes()) {
                if (!removedNodes.contains(node)) {
                    for (Edge edge : node.getEdges()) {
                        List<Edge> ajdVertices = new ArrayList<>();

                        if (!removedEdges.contains(edge)) {
                            ajdVertices.add(edge);
                        }
                        // If the vertex weight is 1 - its a leaf.
                        if (ajdVertices.size() == 1) {
                            Node candidate1 = StateSpace.getNode(ajdVertices.get(0).getFromId());
                            Node candidate2 = StateSpace.getNode(ajdVertices.get(0).getToId());
                            if (candidate1.equals(node)) {
                                result[candidate2.getId()] = true;
                            } else {
                                result[candidate1.getId()] = true;
                            }   
                            removedEdges.addAll(candidate1.getEdges());
                            removedEdges.addAll(candidate2.getEdges());

                            removedNodes.add(candidate1);
                            removedNodes.add(candidate2);

                            count++;
                        }   
                    }   
                }   
            }   
        } while (count != 0); 
        return result;
    }
}
