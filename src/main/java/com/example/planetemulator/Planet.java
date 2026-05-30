package com.example.planetemulator;

public class Planet {
    public String name;
    public double x, y;
    public double vx, vy;
    public double mass;
    public double radius;
    public int color;
    public boolean isSun;

    public Planet(String name, double x, double y, double vx, double vy,
                  double mass, double radius, int color, boolean isSun) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.mass = mass;
        this.radius = radius;
        this.color = color;
        this.isSun = isSun;
    }
}