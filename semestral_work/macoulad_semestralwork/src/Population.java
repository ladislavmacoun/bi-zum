package bi.zum.lab3;

import cz.cvut.fit.zum.api.Node;
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
        AbstractIndividual individual = this.individuals[r.nextInt(this.individuals.length)];
        AbstractIndividual fittestIndividual = individual;
        this.populationUpdate();
        
        while (selected.size() != count) {
            
            ArrayList<AbstractIndividual> tournament = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                tournament.add(this.individuals[r.nextInt(this.individuals.length)]);
            }
            
            for (AbstractIndividual fighter : tournament) {
                if (fighter.getFitness() > fittestIndividual.getFitness()) {
                    fittestIndividual = fighter;
                }
            }
            
            selected.add(fittestIndividual);
                                 
        }       
        return selected;
    }
    
    void AnnealPopulation(int temperature) {
        for (int j = 0; j < individuals.length; j++) {
            Individual individual = (Individual) individuals[j].deepCopy();
            individuals[j] = individual.SimulatedAnnealing(temperature).deepCopy();
        }   
    }
    
    public void populationUpdate() {
        for (int i = 0; i < individuals.length; i++)
            individuals[i].computeFitness();
    }
    
}
