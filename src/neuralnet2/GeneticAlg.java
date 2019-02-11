package neuralnet2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeneticAlg {

    class Genome implements Comparable<Genome> {			//a simple class to handle a single genome
        private ArrayList<Double> weights;					//since no other class needs to know how Genome is implemented
        private double fitness;								//it is a subclass of the genetic algorithm class

        public Genome() {
            weights = new ArrayList<Double>();				//here the 'chromosomes' for a genetic alg influenced by a neural net are the weights of the neuron's inputs
            fitness = 0;									//fitness increases as the genome becomes more fit
        }

        public Genome(ArrayList<Double> w, double f) {
            weights = new ArrayList<Double>();
            for (Double d : w) {
                weights.add(d);
            }
            fitness = f;
        }

        public Genome clone() {								//mmm, cloning genomes
            return new Genome(weights, fitness);
        }

        public ArrayList<Double> getWeights() { return weights; }
        public double getFitness() { return fitness; }
        public void setFitness(double f) { fitness = f; }

        @Override
        public int compareTo(Genome o) {					//the comparable interface needs a definition of compareTo
            if (this.fitness > o.getFitness()) {			//the interface is being used so that the genomes can be sorted by fitness
                return 1;
            } else if (this.fitness < o.getFitness()) {
                return -1;
            }
            return 0;
        }
    }

    private ArrayList<Genome> pop;		//the genomes (weights for neural nets) who are the members of the genetic algorithm's gene pool
    private int popSize;				//the pools' size
    private int chromosomeLength;		//the length of the weights list
    private double totalFitness;		//the summation of all the genomes' fitnesses
    private double bestFitness;			//the best fitness of all the genomes, then the average, then the worst
    private double avgFitness;			//could be used for plotting fitnesses
    private double worstFitness;
    private int fittestGenome;			//the index of the most fit genome in the population
    private int genCount;				//what generation the pool has made it to
    private double mutationRate;		//how often mutation (for each entry in a weight list) and crossover occurs
    private double crossoverRate;
    private ArrayList<Double> child1;
    private ArrayList<Double> child2;

    public GeneticAlg(int populationSize, double mutRate, double crossRate, int numWeights) {
        popSize = populationSize;
        mutationRate = mutRate;
        crossoverRate = crossRate;
        chromosomeLength = numWeights;
        totalFitness = 0;
        genCount = 0;
        fittestGenome = 0;
        bestFitness = 0;
        worstFitness = 99999999;
        avgFitness = 0;
        //initialize population with randomly generated weights
        pop = new ArrayList<Genome>();
        Random rnd = new Random();
        for (int i = 0; i < popSize; i++) {
            pop.add(new Genome());
            for (int j = 0; j < chromosomeLength; j++) {
                pop.get(i).weights.add(rnd.nextDouble()*2 - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void crossover(ArrayList<Double> parent1, ArrayList<Double> parent2) {
        //implement crossover, similar to the previous project
        child1 = new ArrayList<Double>();
        child2 = new ArrayList<Double>();
        double x = Math.random();
        if (parent1.size() != parent2.size())
            System.out.println("Parent sizes are not the same");
        for (int i = 0; i < (int) (parent1.size() * x); i++) {
            child1.add(parent1.get(i));
            child2.add(parent2.get(i));
        }

        for(int i = (int) (parent2.size() * x); i < parent2.size(); i++) {
            child1.add(parent2.get(i));
            child2.add(parent1.get(i));
        }
    }

    public void mutate(ArrayList<Double> chromo) {
        //mutate each weight dependent upon the mutation rate
        //the weights are bounded by the maximum allowed perturbation
        double x = Math.random();
        int y = (int) (Math.random() * chromo.size());
        if (x <= Params.MUTATION_RATE && y < chromo.size()) {
            double weight = chromo.get(y) + (Params.MAX_PERTURBATION * (Math.random()* 2 - 1));
            chromo.set(y, weight);
        }
    }

    public Genome getChromoByRoulette() {		//random parent selection using a roulette approach
        Random rnd = new Random();
        double stop = Math.random() * totalFitness;	//pick a random fitness value at which to stop
        double fitnessSoFar = 0;
        Genome result = new Genome();
        int i = 0;
        while (fitnessSoFar <= stop && i != pop.size()) {
            fitnessSoFar += pop.get(i).fitness;
            result = pop.get(i).clone();
            i++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Genome> epoch(ArrayList<Genome> oldpop) { //get the new generation from the old generation
        for (int i = 0; i < oldpop.size(); i++) {
            pop.set(i, oldpop.get(i));
        }
        reset();											//reinitialize fitness stats
        Collections.sort(pop,new Comparator<Genome>() {
            @Override
            public int compare(Genome x, Genome y) {
                return Integer.compare((int)x.getFitness(), (int)y.getFitness());
            }
        });
        calculateBestWorstAvgTot();							//calculate the fitness stats
        ArrayList<Genome> newPop = new ArrayList<Genome>();
        if (Params.NUM_COPIES_ELITE * Params.NUM_ELITE % 2 == 0) {			//take the top NUM_ELITE performers and add them to the new population
            newPop = grabNBest(Params.NUM_ELITE, Params.NUM_COPIES_ELITE, newPop);
        }
        while (newPop.size() < popSize) {					//fill the rest of the new population by children from parents using the classic genetic algorithm
            //your 9-ish lines of code goes here
            double x = Math.random();
            child1 = new ArrayList<Double>();
            child2 = new ArrayList<Double>();
            if (x < Params.CROSSOVER_RATE) {
                Genome p1 = getChromoByRoulette();
                Genome p2 = getChromoByRoulette();
                crossover(p1.getWeights(), p2.getWeights());
                mutate(child1);
                mutate(child2);
                newPop.add(new Genome(child1, 0));
                if (newPop.size() < popSize)
                    newPop.add(new Genome(child2, 0));
                //			System.out.println(child1);
            }
        }
        pop = (ArrayList<Genome>) newPop.clone();
        return pop;											//this probably could have been written better, why return a class variable?
    }

    public ArrayList<Genome> grabNBest(int nBest, int numCopies, ArrayList<Genome> popList) { //hopefully the population is sorted correctly...
        Collections.sort(pop,new Comparator<Genome>() {
            @Override
            public int compare(Genome x, Genome y) {
                return Integer.compare((int)x.getFitness(), (int)y.getFitness());
            }
        });
        int k = 0;
        while (nBest != 0) {
            for (int i = 0; i < numCopies; i++) {
                popList.add(pop.get(k));
            }
            k++;
            nBest--;
        }
        return popList;
    }

    public void calculateBestWorstAvgTot() { //fairly self-explanatory, try commenting it
        totalFitness = 0;
        double highestSoFar = 0;
        double lowestSoFar = 99999999;
        for (int i = 0; i < popSize; i++) { //goes through the population
            if (pop.get(i).fitness > highestSoFar) {
                highestSoFar = pop.get(i).fitness; //gets the highest value
                fittestGenome = i;   //records the index of the fittest genome
                bestFitness = highestSoFar; //sets the class statistics of the highest fitness
            }
            if (pop.get(i).fitness < lowestSoFar) {  //same things except for the lowest
                lowestSoFar = pop.get(i).fitness;
                worstFitness = lowestSoFar;
            }
            totalFitness += pop.get(i).fitness;
        }
        avgFitness = totalFitness / popSize;
    }

    public void reset() {		//reset fitness stats
        totalFitness = 0;
        bestFitness = 0;
        worstFitness = 99999999;
        avgFitness = 0;
    }

    //self-explanatory
    public ArrayList<Genome> getChromosomes() { return pop; }
    public double avgFitness() { return totalFitness / popSize; }
    public double bestFitness() { return bestFitness; }

}
