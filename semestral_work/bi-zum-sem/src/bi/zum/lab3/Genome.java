/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bi.zum.lab3;

/**
 *
 * @author Ladislav Macoun
 */
public class Genome {
    // 0 always false, 1 false, 2 always true, 3 true
    private int genome[];
    
    public Genome(int size) {
        this.genome = new int[size];
    }
    
    public Genome(Genome other) {
        System.arraycopy(this.genome, 0, other.genome, 0, this.genome.length);
    }
    
    public boolean getGenomeAt(int idx) {
        return genome[idx] > 1;
    }
    
    public void setGenomeAt(int idx, int value) {
        this.genome[idx] = value;
    }
    
    public void setAlwaysTrue(int idx) {
        this.genome[idx] = 2;
    }
    
    public void setAlwaysFalse(int idx) {
        this.genome[idx] = 0;
    }
    
    public void setRandom(int idx) {
        if (0.50 < Math.random()) {
            this.genome[idx] = 3;
        } else {
            this.genome[idx] = 2;
        }
    }
    
    public void mutateGenome(int idx) {
        if (genome[idx] != 0 && genome[idx] != 2) {  
            if (genome[idx] == 1) { genome[idx] = 3;}
            else { genome[idx] = 1;}
        } 
    }
    
    public void genomeCorrection(int idx) {
        genome[idx] = 3;
    }
    
    public int size() {
        return genome.length;
    }
    
    public int genomeAt(int idx) {
        return this.genome[idx];
    }
    
    public void setTrue(int idx) {
        this.genome[idx] = 3;
    }
    
    public void setFalse(int idx) {
        this.genome[idx] = 1;
    }
    
}
