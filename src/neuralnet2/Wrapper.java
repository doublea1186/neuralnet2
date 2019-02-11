package neuralnet2;

//much of the implementation design inspired by an online resource

import java.awt.EventQueue;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Wrapper extends JFrame {

    public static final int FRAMESIZE = 900; 	//sizing of the window
    public static final int BTNSPACE = 100;    //room for the buttons
    public static final int HRZSPACE = 8;		//a bit of side padding

    //in theory, all of the parameters could go here and controller's constructor could be expanded to take them all in

    public Wrapper() {					//normal stuff for a timer based simulation
        setSize(FRAMESIZE+HRZSPACE, FRAMESIZE+BTNSPACE);
        add(new ControllerMS(FRAMESIZE, FRAMESIZE));
        setResizable(false);
        setTitle("Neural net agents");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Wrapper go = new Wrapper();
                go.setVisible(true);
            }
        });
    }
}
