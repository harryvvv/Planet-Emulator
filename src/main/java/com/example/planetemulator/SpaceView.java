package com.example.planetemulator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SpaceView extends View {

    private final Paint starPaint;
    private final Paint sunPaint;
    private final Paint planetPaint;
    private final Paint textPaint;
    private final Paint trailPaint;

    private final List<Planet> planets = new ArrayList<>();
    private final List<List<double[]>> trails = new ArrayList<>();
    private boolean isRunning = false;
    private double zoom = 1.0;
    private boolean autoZoom = true;

    public SpaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        starPaint = new Paint();
        starPaint.setColor(Color.WHITE);

        sunPaint = new Paint();
        sunPaint.setColor(Color.YELLOW);
        sunPaint.setStyle(Paint.Style.FILL);

        planetPaint = new Paint();
        planetPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);

        trailPaint = new Paint();
        trailPaint.setColor(Color.parseColor("#40FFFFFF"));
        trailPaint.setStrokeWidth(1);
    }

    public void addPlanet(Planet planet) {
        planets.add(planet);
        trails.add(new ArrayList<>());
        calculateAutoZoom();
        invalidate();
    }

    public void clear() {
        planets.clear();
        trails.clear();
        zoom = 1.0;
        invalidate();
    }

    public void startSimulation() {
        isRunning = true;
        startLoop();
    }

    public void stopSimulation() {
        isRunning = false;
    }

    public void zoomIn() {
        autoZoom = false;
        zoom = Math.min(zoom * 1.3, 10.0);
        invalidate();
    }

    public void zoomOut() {
        autoZoom = false;
        zoom = Math.max(zoom / 1.3, 0.01);
        invalidate();
    }

    public void resetZoom() {
        autoZoom = true;
        calculateAutoZoom();
        invalidate();
    }

    public double getZoom() {
        return zoom;
    }

    public int getPlanetCount() {
        return planets.size();
    }

    public boolean hasSun() {
        for (Planet p : planets) {
            if (p.isSun) return true;
        }
        return false;
    }

    private void calculateAutoZoom() {
        if (planets.isEmpty()) {
            zoom = 1.0;
            return;
        }

        double maxDistance = 0;
        for (Planet p : planets) {
            double dist = Math.sqrt(p.x * p.x + p.y * p.y);
            if (dist > maxDistance) maxDistance = dist;
        }

        if (maxDistance > 0) {
            double screenWidth = Math.min(getWidth(), getHeight()) / 2.5;
            zoom = screenWidth / (maxDistance / 1e9);
            zoom = Math.max(0.01, Math.min(zoom, 5.0));
        }
    }

    private void startLoop() {
        post(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    update();
                    if (autoZoom) calculateAutoZoom();
                    invalidate();
                    postDelayed(this, 30);
                }
            }
        });
    }

    private void update() {
        Planet sun = null;
        for (Planet p : planets) {
            if (p.isSun) {
                sun = p;
                break;
            }
        }
        if (sun == null) return;

        double dt = 3600;

        for (int i = 0; i < planets.size(); i++) {
            Planet p = planets.get(i);
            if (p.isSun) continue;

            double dx = sun.x - p.x;
            double dy = sun.y - p.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            double G = 6.674e-11;
            double force = G * sun.mass * p.mass / (dist * dist);

            double ax = force * dx / dist / p.mass;
            double ay = force * dy / dist / p.mass;

            p.vx += ax * dt;
            p.vy += ay * dt;

            p.x += p.vx * dt;
            p.y += p.vy * dt;

            if (trails.get(i).size() < 200) {
                trails.get(i).add(new double[]{p.x, p.y});
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;

        canvas.drawColor(Color.parseColor("#000033"));

        for (int i = 0; i < 100; i++) {
            canvas.drawCircle((i * 137) % w, (i * 263) % h, 1, starPaint);
        }

        for (int i = 0; i < planets.size(); i++) {
            Planet p = planets.get(i);

            float px = cx + (float)(p.x / 1e9 * zoom);
            float py = cy + (float)(p.y / 1e9 * zoom);
            float r = (float)p.radius;

            if (!p.isSun && trails.get(i).size() > 1) {
                for (int j = 1; j < trails.get(i).size(); j++) {
                    double[] prev = trails.get(i).get(j-1);
                    double[] curr = trails.get(i).get(j);
                    float x1 = cx + (float)(prev[0] / 1e9 * zoom);
                    float y1 = cy + (float)(prev[1] / 1e9 * zoom);
                    float x2 = cx + (float)(curr[0] / 1e9 * zoom);
                    float y2 = cy + (float)(curr[1] / 1e9 * zoom);
                    canvas.drawLine(x1, y1, x2, y2, trailPaint);
                }
            }

            planetPaint.setColor(p.color);
            canvas.drawCircle(px, py, r, planetPaint);

            if (p.isSun) {
                sunPaint.setAlpha(100);
                canvas.drawCircle(px, py, r * 3, sunPaint);
                sunPaint.setAlpha(255);
            }

            canvas.drawText(p.name, px + r + 5, py, textPaint);
        }
    }
}