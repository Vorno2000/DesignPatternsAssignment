/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processapplication;

import java.io.Serializable;

//Task Class
/**
 * Task class is an object which stores information from both the client and the user. It allows the Clients class to add values based on the user's input
 * of shape name and length of an edge. It also allows the ProcessServer to get these values and set the values after computing the area and perimeter measurements.
 * 
 * @author
 */
public class Task implements Serializable, Cloneable{
    private double shapeOneSide;
    private double shapePerimeter;
    private double shapeArea;
    private int identifier;
    private String shape;

    public Task(String shape, double shapeOneSide) {
        this.shapeOneSide = shapeOneSide;
        this.shape = shape;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public double getShapeOneSide() {
        return shapeOneSide;
    }

    public void setShapeOneSide(double shapeOneSide) {
        this.shapeOneSide = shapeOneSide;
    }

    public double getShapePerimeter() {
        return shapePerimeter;
    }

    public void setShapePerimeter(double shapePerimeter) {
        this.shapePerimeter = shapePerimeter;
    }

    public double getShapeArea() {
        return shapeArea;
    }

    public void setShapeArea(double shapeArea) {
        this.shapeArea = shapeArea;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
