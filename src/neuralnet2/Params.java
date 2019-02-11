package neuralnet2;

public class Params {
    //general parameters
    public static final int WIN_WIDTH = Wrapper.FRAMESIZE;			//width of world map
    public static final int WIN_HEIGHT = Wrapper.FRAMESIZE;			//height of world map
    public static final int FPS = 60;								//frames per second for drawing...not used

    //for the neural network
    public static final int INPUTS = 5;					//number of inputs
    public static final int HIDDEN = 1;					//number of hidden layers
    public static final int NEURONS_PER_HIDDEN = 6; 	//number of neurons in each hidden layer
    public static final int OUTPUTS = 2;				//number of outputs
    public static final double BIAS =-1;				//the threshold (bias) value
    public static final double ACT_RESPONSE = 1;		//adjusts the sigmoid function

    //for the genetic algorithm
    public static final double CROSSOVER_RATE = 0.5;	//the chance of crossover happening
    public static final double MUTATION_RATE = 0.05;		//the chance of a particular value in a genome changing
    public static final double MAX_PERTURBATION = 0.3;	//maximum magnitude of the new value from mutation
    public static final int NUM_ELITE = 3;				//how many of the top performers advance to the next generation
    public static final int NUM_COPIES_ELITE = 1;		//and how many copies of those performers we'll use

}