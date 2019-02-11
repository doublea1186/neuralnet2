package neuralnet2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import neuralnet2.GeneticAlg.Genome;

@SuppressWarnings("serial")
public class ControllerMS extends JPanel implements ActionListener {

    private Timer timer;					//timer runs the simulation
    private int ticks;						//tick counter for a run of a generation's agents
    private int numMines;					//the number of mines to use in the simulator
    private int generations;				//the counter for which generation the sim's on
    private int numAgents;					//how many agents
    private int numWeights;					//how many weights an agent has
    private ArrayList<Double> avgFitness; 	//useful if you were plotting the progression of fitness
	private ArrayList<Double> bestFitness;
    private GeneticAlg GA;					//the genetic algorithm that manages the genome weights
    private ArrayList<Genome> pop;			//the weights of the neural nets for each of the agents
    private ArrayList<AgentMS> agents;		//the agents themselves (the sweepers)
    private ArrayList<Mines> mines;		//the mines
    private BufferedImage pic;				//the image in which things are drawn
    private JLabel picLabel;				//the label that holds the image
    private JLabel dataLabel;				//the label that holds the fitness information

    //these are specific to the mine sweeping scenario
    //for the controller to run the whole simulation
    public static final int MINES = 60;
    public static final int SWEEPERS = 30;
    public static final int TICKS = 1600;				//how long agents have a chance to gain fitness
    public static final double MINE_SIZE = 4;

    //for the mine sweepers
    public static final double MAX_TURN_RATE = 0.2;		//how quickly they may turn
    public static final double MAX_SPEED = 2;			//how fast they can go
    public static final int SCALE = 15;					//the size of the sweepers

    public ControllerMS(int xDim, int yDim) {
        setBackground(Color.LIGHT_GRAY);
        //addMouseListener(new MAdapter());
        //addMouseMotionListener(new MAdapter());
        setFocusable(true);
        setDoubleBuffered(true);
        //create the things to display, then add them
        pic = new BufferedImage(xDim, yDim, BufferedImage.TYPE_INT_RGB);
        picLabel = new JLabel(new ImageIcon(pic));
        dataLabel = new JLabel("Info");
        dataLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(picLabel);
        add(dataLabel);
        //initialize all of the variables!
        numMines = MINES;
        numAgents = SWEEPERS;
        ticks = 0;
        generations = 0;
		avgFitness = new ArrayList<Double>();
		bestFitness = new ArrayList<Double>();

        //make up agents
        agents = new ArrayList<AgentMS>(numAgents);
        for (int i = 0; i < numAgents; i++) {
            agents.add(new AgentMS());
        }
        numWeights = agents.get(0).getNumberOfWeights();

        //give agent neural nets their weights
        GA = new GeneticAlg(numAgents, Params.MUTATION_RATE, Params.CROSSOVER_RATE, numWeights);
        pop = GA.getChromosomes();
        for (int i = 0; i < numAgents; i++) {
            agents.get(i).putWeights(pop.get(i).getWeights());
        }

        //set up the mines
        mines = new ArrayList<Mines>(numMines);
        Random rnd = new Random();
        for (int i = 0; i < numMines; i++) {
            mines.add(new Mines (true, new Point2D.Double(rnd.nextDouble() * xDim, rnd.nextDouble() * yDim)));
//            if(Math.random() <= .5)
//                mines.get(i).setType(false);
        }
        //start it up!
        timer = new Timer(1, this);
        timer.start();
    }

    public void drawThings(Graphics2D g) {
        //cover everything with a blank screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, pic.getWidth(), pic.getHeight());
        //draw agents
        for (AgentMS a : agents) {
            a.draw(g);
        }
        //draw mines
        g.setColor(Color.RED);
        for (Mines m : mines) {
            if (m.getType()) g.setColor(Color.RED);
            else g.setColor(Color.GREEN);
            g.fillRect((int)(m.getPosition().getX()-MINE_SIZE/2), (int)(m.getPosition().getY()-MINE_SIZE/2), (int)MINE_SIZE, (int)MINE_SIZE);
        }
        g.setColor(Color.BLUE);
        for(AgentMS a: agents){
            Point2D x = a.getClosestMine(mines);
            g.drawLine((int) a.getPos().getX(), (int)a.getPos().getY(), (int) x.getX(), (int) x.getY());

        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
//		drawThings(pic.getGraphics());
    }

    public void updateAgents() {
        ticks = (ticks + 1);		//count ticks to set the length of the generation run
        if (ticks < TICKS) { //do another tick toward finishing a generation
            Random rnd = new Random();
            //update each agent by calling their update function and checking to see if they got a mine
            for (int i = 0; i < numAgents; i++) {
                if (!agents.get(i).update(mines)) {
                    System.out.println("Error: Wrong amount of neural net inputs.");
                    break;
                }
                //did it find a mine
                int foundMine = agents.get(i).checkForMine(mines, MINE_SIZE);
                //if it found a mine, add to that agent's fitness and make a new mine
                if (foundMine >= 0) {
                    if(mines.get(foundMine).getType())
                    agents.get(i).incrementFitness();
                    else
                        agents.get(i).decrementFitness();
                    mines.get(foundMine).setPosition(new Point2D.Double(rnd.nextDouble() * pic.getWidth(), rnd.nextDouble() * pic.getHeight()));
                }
                //keep track of that agent's fitness in the GA as well as the NN
                pop.get(i).setFitness(agents.get(i).getFitness());
            }
        } else { //a generation has completed, run the genetic algorithm and update the agents
			avgFitness.add(GA.avgFitness());
			bestFitness.add(GA.bestFitness());
            generations++;
            dataLabel.setText("Previous generation " + generations + ":  Avg. fitness of " + GA.avgFitness() + ".  Best fitness of " + GA.bestFitness() + ".");
            ticks = 0;
            pop = GA.epoch(pop); //the big genetic algorithm process line
            for (int i = 0; i < numAgents; i++) { //give the agents all the new weights information
                agents.get(i).putWeights(pop.get(i).getWeights());
                agents.get(i).reset();
            }
        }
    }

    //@Override
    public void actionPerformed(ActionEvent e) {
        updateAgents();
        drawThings((Graphics2D) pic.getGraphics());
        repaint();
    }

}
