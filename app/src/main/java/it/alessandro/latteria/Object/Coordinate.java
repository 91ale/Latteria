package it.alessandro.latteria.Object;

import java.io.Serializable;

public class Coordinate implements Serializable {

    private String x;
    private String y;

    public Coordinate (String x, String y) {
       this.x = x;
       this.y = y;
    }

    public String getX() {return x;}
    public String getY() {return y;}

}
