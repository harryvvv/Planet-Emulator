package com.example.planetemulator;

public class Physics {

    // Гравитационная постоянная
    public static final double G = 6.674e-11;

    // ✅ Масштаб: 1 пиксель = 1 миллион км = 1e9 метров
    public static final double PIXEL_TO_METER = 1e9;

    // Временной шаг: 1 час = 3600 секунд
    // В приложении проходит примерно 33.3 часа за 1 секунду
    public static final double TIME_STEP = 3600;

    public static double calculateForce(double m1, double m2, double distance) {
        if (distance < 1e6) return 0;
        return G * m1 * m2 / (distance * distance);
    }

    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static void updatePlanet(Planet planet, Planet sun) {
        double distance = calculateDistance(planet.x, planet.y, sun.x, sun.y);
        double force = calculateForce(planet.mass, sun.mass, distance);
        double acceleration = force / planet.mass;

        double dx = sun.x - planet.x;
        double dy = sun.y - planet.y;
        double dirX = dx / distance;
        double dirY = dy / distance;

        planet.vx += acceleration * dirX * TIME_STEP;
        planet.vy += acceleration * dirY * TIME_STEP;

        planet.x += planet.vx * TIME_STEP;
        planet.y += planet.vy * TIME_STEP;
    }
}