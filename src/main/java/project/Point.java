package project;

import java.util.ArrayList;
import java.lang.Math;

public class Point {
    private ArrayList<Point> connections;
    private float xCoordinate;
    private float yCoordinate;
    private float zCoordinate;

    private double tolerance=Math.pow(10,-4);

    /**
     * Generic constructor - most likely will not see use
     */
    public Point() {
        connections=new ArrayList<>();
        xCoordinate=0;
        yCoordinate=0;
        zCoordinate=0;
    }

    /**
     * specific constructor for a Point, god help me for determining where those points are
     * @param x - x coordinate in 3d space
     * @param y - y coordinate in 3d space
     * @param z - z coordinate in 3d space
     */
    public Point(float x, float y, float z) {
        connections=new ArrayList<>();
        xCoordinate=x;
        yCoordinate=y;
        zCoordinate=z;
    }

    /**
     * lets this point know it has a connection to the other point
     * @param p - other point
     * @return - returns true if the point was not previously in the array, else false
     */
    public boolean makeConnection(Point p) {
        if(connections.contains(p)) return false;
        connections.add(p);
        return true;
    }

    public void setX (float x) {xCoordinate=x;}
    public void setY (float y) {yCoordinate=y;}
    public void setZ (float z) {zCoordinate=z;}

    public float getX() {return xCoordinate;}
    public float getY() {return yCoordinate;}
    public float getZ() {return zCoordinate;}

    public boolean basicallyEqual(float x, float y, float z){
        return tolerance>Math.sqrt(Math.pow(xCoordinate-x,2)+Math.pow(yCoordinate-y,2)+Math.pow(zCoordinate-z,2));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point p=(Point)obj;
            return p.getX()==xCoordinate&&p.getY()==yCoordinate&&p.getZ()==zCoordinate;
        }
        return false;
    }

    @Override
    public int hashCode(){
            int hash=7;
            hash=31*hash+Float.floatToIntBits(xCoordinate);
            hash=31*hash+Float.floatToIntBits(yCoordinate);
            hash=31*hash+Float.floatToIntBits(zCoordinate);
            return hash;
    }
}
