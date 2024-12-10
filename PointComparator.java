package project;

import java.util.Comparator;

//was for making tolerances in hashmaps- but uhh i dont think you can do that
public class PointComparator implements Comparator<Point> {
    double tolerance=Math.pow(10,-4);
    public int compare(Point p1, Point p2) {
        if(tolerance>Math.sqrt(Math.pow(p1.getX()- p2.getX(),2)+Math.pow(p1.getY()-p2.getY(),2)+Math.pow(p1.getZ()- p2.getZ(),2)))
            return 0;
        return (int) Math.round(tolerance-Math.sqrt(Math.pow(p1.getX()- p2.getX(),2)+Math.pow(p1.getY()-p2.getY(),2)+Math.pow(p1.getZ()- p2.getZ(),2)));
    }
}
