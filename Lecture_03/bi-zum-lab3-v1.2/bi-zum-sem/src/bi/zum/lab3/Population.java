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
        ArrayList<AbstractIndividual> selected = new ArrayList<AbstractIndividual>();

        // example of random selection of N individuals
        Random r = new Random();
        AbstractIndividual individual = individuals[r.nextInt(individuals.length)];
        while (selected.size() < count) {
            selected.add(individual);
            individual = individuals[r.nextInt(individuals.length)];
        }
        
        /* Get the fittest idividual */
        AbstractIndividual fittestIndividual = individuals[0];
        
        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i].getFitness() > fittestIndividual.getFitness()) {
                fittestIndividual = individuals[i];
            }
        }
        
        selected.add(fittestIndividual);
        
        return selected;
    }
}
