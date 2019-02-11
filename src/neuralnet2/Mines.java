package neuralnet2;
import java.awt.geom.Point2D;

public class Mines { //class to characterize the type of mines
    private boolean type; //good or bad
    private Point2D position; //position of the mine

    public Mines(boolean type, Point2D position){
        this.type = type;
        this.position = position;
    }
    public boolean getType() {return type;}
    public Point2D getPosition() {return position;}
    public void setType(boolean x) {type = x;}
    public void setPosition(Point2D x) {position = x;}
}
