package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.api.ga.AbstractPopulation;
import cz.cvut.fit.zum.data.StateSpace;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author Ladislav Macoun
 */
public class Population extends AbstractPopulation {

    public Population(AbstractEvolution evolution, int size) {
        individuals = new Individual[size];
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(evolution, true);
            individuals[i].computeFitness();
        }
    }

    /**
     * Method to select individuals from population
     *
     * @param count The number of individuals to be selected
     * @return List of selected individuals
     */
    public List<AbstractIndividual> selectIndividuals(int count) {
        ArrayList<AbstractIndividual> selected = new ArrayList<>();

        // example of random selection of N individuals
        Random r = new Random();
        AbstractIndividual individual = individuals[r.nextInt(individuals.length)];
        AbstractIndividual fittestIndividual = individuals[0];
        
        while (selected.size() < count) {
            if (individual.getFitness() > fittestIndividual.getFitness()) {
                fittestIndividual = individual;
            }
            
            individual = individuals[r.nextInt(individuals.length)];
            selected.add(fittestIndividual);
                                 
        }
        
        return selected;
    }
}
