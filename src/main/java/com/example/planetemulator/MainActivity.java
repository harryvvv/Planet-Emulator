package com.example.planetemulator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SpaceView spaceView;
    private Button btnStart, btnStop, btnClear;
    private Button btnZoomIn, btnZoomOut, btnResetZoom;
    private Button btnAddPlanet, btnPresets;
    private Button btnConfirmAdd, btnCancelAdd, btnSetLimit;
    private EditText etName, etX, etY, etVx, etVy, etMass, etRadius;
    private EditText etPlanetLimit;
    private TextView tvZoom, tvPlanetCount;
    private LinearLayout panelManual;

    private int planetLimit = 10;
    private boolean isManualPanelOpen = false;

    private final Object[][] planetPresets = {
            {"Sun", 0.0, 0.0, 0.0, 0.0, 1989000.0, 25.0, Color.YELLOW, true},
            {"Mercury", 57.9, 0.0, 0.0, 47.4, 330.0, 2.4, 0xFFB5B5B5, false},
            {"Venus", 108.2, 0.0, 0.0, 35.0, 4870.0, 6.0, 0xFFFFE4B5, false},
            {"Earth", 149.6, 0.0, 0.0, 29.8, 5970.0, 8.0, 0xFF4FC3F7, false},
            {"Mars", 227.9, 0.0, 0.0, 24.1, 642.0, 5.0, 0xFFE57373, false},
            {"Jupiter", 778.5, 0.0, 0.0, 13.1, 1898000.0, 18.0, 0xFFFFB74D, false},
            {"Saturn", 1432.0, 0.0, 0.0, 9.7, 568000.0, 15.0, 0xFFAED581, false},
            {"Uranus", 2867.0, 0.0, 0.0, 6.8, 86800.0, 12.0, 0xFF81D4FA, false},
            {"Neptune", 4515.0, 0.0, 0.0, 5.4, 102400.0, 11.0, 0xFF5C6BC0, false}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        addSun();
        updatePlanetCount();
    }

    private void initViews() {
        spaceView = findViewById(R.id.spaceView);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnClear = findViewById(R.id.btnClear);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnResetZoom = findViewById(R.id.btnResetZoom);
        btnAddPlanet = findViewById(R.id.btnAddPlanet);
        btnPresets = findViewById(R.id.btnPresets);
        btnConfirmAdd = findViewById(R.id.btnConfirmAdd);
        btnCancelAdd = findViewById(R.id.btnCancelAdd);
        btnSetLimit = findViewById(R.id.btnSetLimit);

        etName = findViewById(R.id.etName);
        etX = findViewById(R.id.etX);
        etY = findViewById(R.id.etY);
        etVx = findViewById(R.id.etVx);
        etVy = findViewById(R.id.etVy);
        etMass = findViewById(R.id.etMass);
        etRadius = findViewById(R.id.etRadius);
        etPlanetLimit = findViewById(R.id.etPlanetLimit);

        tvZoom = findViewById(R.id.tvZoom);
        tvPlanetCount = findViewById(R.id.tvPlanetCount);
        panelManual = findViewById(R.id.panelManual);
    }

    private void setupListeners() {
        btnAddPlanet.setOnClickListener(v -> toggleManualPanel());
        btnPresets.setOnClickListener(v -> showPresetsDialog());
        btnConfirmAdd.setOnClickListener(v -> addPlanetFromFields());
        btnCancelAdd.setOnClickListener(v -> closeManualPanel());

        btnStart.setOnClickListener(v -> {
            spaceView.startSimulation();
            btnStart.setText("⏯️ ИДЁТ");
        });

        btnStop.setOnClickListener(v -> {
            spaceView.stopSimulation();
            btnStart.setText("▶️ СТАРТ");
        });

        btnClear.setOnClickListener(v -> {
            spaceView.clear();
            btnStart.setText("▶️ СТАРТ");
            addSun();
        });

        btnZoomIn.setOnClickListener(v -> {
            spaceView.zoomIn();
            updateZoomText();
        });

        btnZoomOut.setOnClickListener(v -> {
            spaceView.zoomOut();
            updateZoomText();
        });

        btnResetZoom.setOnClickListener(v -> {
            spaceView.resetZoom();
            updateZoomText();
        });

        btnSetLimit.setOnClickListener(v -> {
            try {
                planetLimit = Integer.parseInt(etPlanetLimit.getText().toString().trim());
                if (planetLimit < 1) planetLimit = 1;
                if (planetLimit > 50) planetLimit = 50;
                Toast.makeText(this, "Лимит: " + planetLimit, Toast.LENGTH_SHORT).show();
                updatePlanetCount();
            } catch (Exception e) {
                Toast.makeText(this, "Неверное число!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleManualPanel() {
        if (isManualPanelOpen) {
            closeManualPanel();
        } else {
            openManualPanel();
        }
    }

    private void openManualPanel() {
        panelManual.setVisibility(View.VISIBLE);
        btnAddPlanet.setText("🔽 Закрыть");
        isManualPanelOpen = true;
    }

    private void closeManualPanel() {
        panelManual.setVisibility(View.GONE);
        btnAddPlanet.setText("➕ Планета");
        isManualPanelOpen = false;
        clearInputFields();
    }

    private void addPlanetFromFields() {
        if (!areFieldsFilled()) {
            Toast.makeText(this, "❌ Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spaceView.getPlanetCount() >= planetLimit) {
            Toast.makeText(this, "❌ Достигнут лимит планет (" + planetLimit + ")!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) name = "Planet";

            Planet planet = new Planet(
                    name,
                    parseDouble(etX) * 1e9,
                    parseDouble(etY) * 1e9,
                    parseDouble(etVx) * 1000,
                    parseDouble(etVy) * 1000,
                    parseDouble(etMass) * 1e24,
                    parseDouble(etRadius),
                    generateColor(),
                    name.equalsIgnoreCase("sun")
            );

            spaceView.addPlanet(planet);
            Toast.makeText(this, "✅ " + name + " добавлена!", Toast.LENGTH_SHORT).show();
            closeManualPanel();
            updatePlanetCount();

        } catch (Exception e) {
            Toast.makeText(this, "❌ Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPresetsDialog() {
        String[] planetNames = new String[planetPresets.length];
        for (int i = 0; i < planetPresets.length; i++) {
            planetNames[i] = (String) planetPresets[i][0];
        }

        new AlertDialog.Builder(this)
                .setTitle("🌟 Выберите планету")
                .setItems(planetNames, (dialog, which) -> {
                    addPresetPlanet(which);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void addPresetPlanet(int index) {
        if (spaceView.getPlanetCount() >= planetLimit) {
            Toast.makeText(this, "❌ Достигнут лимит планет (" + planetLimit + ")!", Toast.LENGTH_SHORT).show();
            return;
        }

        Object[] preset = planetPresets[index];
        String name = (String) preset[0];
        double x = (Double) preset[1];
        double y = (Double) preset[2];
        double vx = (Double) preset[3];
        double vy = (Double) preset[4];
        double mass = (Double) preset[5];
        double radius = (Double) preset[6];
        int color = (Integer) preset[7];
        boolean isSun = (Boolean) preset[8];

        if (isSun && spaceView.hasSun()) {
            Toast.makeText(this, "⚠️ Солнце уже есть!", Toast.LENGTH_SHORT).show();
            return;
        }

        Planet planet = new Planet(
                name,
                x * 1e9,
                y * 1e9,
                vx * 1000,
                vy * 1000,
                mass * 1e24,
                radius,
                color,
                isSun
        );

        spaceView.addPlanet(planet);
        Toast.makeText(this, "✅ " + name + " добавлена!", Toast.LENGTH_SHORT).show();
        updatePlanetCount();
    }

    private void addSun() {
        if (!spaceView.hasSun()) {
            spaceView.addPlanet(new Planet(
                    "Sun", 0, 0, 0, 0,
                    1989000 * 1e24, 25, Color.YELLOW, true
            ));
            updatePlanetCount();
        }
    }

    private boolean areFieldsFilled() {
        return !etX.getText().toString().trim().isEmpty() &&
                !etY.getText().toString().trim().isEmpty() &&
                !etVx.getText().toString().trim().isEmpty() &&
                !etVy.getText().toString().trim().isEmpty() &&
                !etMass.getText().toString().trim().isEmpty() &&
                !etRadius.getText().toString().trim().isEmpty();
    }

    private void clearInputFields() {
        etName.setText("");
        etX.setText("");
        etY.setText("");
        etVx.setText("");
        etVy.setText("");
        etMass.setText("");
        etRadius.setText("");
    }

    private double parseDouble(EditText et) {
        String val = et.getText().toString().trim();
        return val.isEmpty() ? 0 : Double.parseDouble(val);
    }

    private int generateColor() {
        int[] colors = {0xFF4FC3F7, 0xFF81C784, 0xFFBA68C8, 0xFFFFB74D,
                0xFFE57373, 0xFF64B5F6, 0xFFAED581, 0xFFFF8A65};
        return colors[(int) (Math.random() * colors.length)];
    }

    private void updateZoomText() {
        tvZoom.setText("Zoom: " + String.format("%.1f", spaceView.getZoom()) + "x");
    }

    private void updatePlanetCount() {
        int count = spaceView.getPlanetCount();
        tvPlanetCount.setText("Планет: " + count + "/" + planetLimit);
    }

    @Override
    protected void onPause() {
        super.onPause();
        spaceView.stopSimulation();
    }
}