package com.example.labubicaciongps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final String PREFS = "student_locations";
    private static final String KEY_RECORDS = "records";

    private FusedLocationProviderClient fusedLocationClient;

    private EditText editStudentName;
    private TextView tvLatitude, tvLongitude, tvZone, tvAddress, tvStatus, tvSummary;
    private Button btLocation, btRegister, btClear;

    private double currentLatitude;
    private double currentLongitude;
    private String currentZone = "Zona no identificada";
    private String currentAddress = "Sin dirección";
    private boolean hasLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editStudentName = findViewById(R.id.edit_student_name);
        tvLatitude = findViewById(R.id.text_view1);
        tvLongitude = findViewById(R.id.text_view2);
        tvZone = findViewById(R.id.text_view3);
        tvAddress = findViewById(R.id.text_view4);
        tvStatus = findViewById(R.id.text_view5);
        tvSummary = findViewById(R.id.tv_summary);
        btLocation = findViewById(R.id.bt_location);
        btRegister = findViewById(R.id.bt_register);
        btClear = findViewById(R.id.bt_clear);

        btLocation.setOnClickListener(v -> validatePermissionAndLocate());
        btRegister.setOnClickListener(v -> registerStudent());
        btClear.setOnClickListener(v -> clearRecords());

        refreshSummary();
    }

    private void validatePermissionAndLocate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
            return;
        }
        obtainLastKnownLocation();
    }

    private void obtainLastKnownLocation() {
        tvStatus.setText("Estado: consultando GPS...");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            hasLocation = false;
                            btRegister.setEnabled(false);
                            tvStatus.setText("Estado: no hay una ubicación disponible. Activa el GPS y vuelve a intentar.");
                            return;
                        }

                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        hasLocation = true;

                        tvLatitude.setText(String.format(Locale.US, "Latitud: %.6f", currentLatitude));
                        tvLongitude.setText(String.format(Locale.US, "Longitud: %.6f", currentLongitude));

                        resolveAddress(currentLatitude, currentLongitude);
                        btRegister.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    hasLocation = false;
                    btRegister.setEnabled(false);
                    tvStatus.setText("Estado: error obteniendo ubicación: " + e.getMessage());
                });
    }

    private void resolveAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address a = addresses.get(0);
                currentAddress = a.getAddressLine(0) != null ? a.getAddressLine(0) : "Dirección no disponible";

                // Se intenta formar una zona útil con localidad/subLocalidad/ciudad.
                if (a.getSubLocality() != null && !a.getSubLocality().trim().isEmpty()) {
                    currentZone = a.getSubLocality();
                } else if (a.getLocality() != null && !a.getLocality().trim().isEmpty()) {
                    currentZone = a.getLocality();
                } else if (a.getAdminArea() != null && !a.getAdminArea().trim().isEmpty()) {
                    currentZone = a.getAdminArea();
                } else {
                    currentZone = "Zona no identificada";
                }

                String city = a.getLocality() != null ? a.getLocality() : "";
                tvZone.setText("Ciudad / Localidad: " + currentZone + (city.isEmpty() || city.equals(currentZone) ? "" : " · " + city));
                tvAddress.setText("Dirección: " + currentAddress);
                tvStatus.setText("Estado: ubicación obtenida correctamente");
            } else {
                currentZone = "Zona no identificada";
                currentAddress = "Dirección no disponible";
                tvZone.setText("Ciudad / Localidad: " + currentZone);
                tvAddress.setText("Dirección: " + currentAddress);
                tvStatus.setText("Estado: coordenadas obtenidas; Geocoder no devolvió dirección");
            }
        } catch (IOException e) {
            currentZone = "Zona no identificada";
            currentAddress = "Dirección no disponible";
            tvZone.setText("Ciudad / Localidad: " + currentZone);
            tvAddress.setText("Dirección: " + currentAddress);
            tvStatus.setText("Estado: coordenadas obtenidas; no fue posible resolver la dirección");
        }
    }

    private void registerStudent() {
        String name = editStudentName.getText().toString().trim();
        if (name.isEmpty()) {
            editStudentName.setError("Escribe el nombre del estudiante");
            return;
        }
        if (!hasLocation) {
            Toast.makeText(this, "Primero obtén la ubicación actual", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray records = loadRecords();
            JSONObject record = new JSONObject();
            record.put("name", name);
            record.put("latitude", currentLatitude);
            record.put("longitude", currentLongitude);
            record.put("zone", currentZone);
            record.put("address", currentAddress);
            record.put("timestamp", System.currentTimeMillis());
            records.put(record);

            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_RECORDS, records.toString())
                    .apply();

            Toast.makeText(this, "Ubicación registrada para " + name, Toast.LENGTH_SHORT).show();
            editStudentName.setText("");
            refreshSummary();
        } catch (JSONException e) {
            Toast.makeText(this, "No fue posible guardar el registro", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray loadRecords() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_RECORDS, "[]");
        try {
            return new JSONArray(raw);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private void refreshSummary() {
        JSONArray records = loadRecords();
        if (records.length() == 0) {
            tvSummary.setText("Aún no hay estudiantes registrados.\n\nObtén la ubicación, escribe el nombre y registra cada participante.");
            return;
        }

        Map<String, Integer> zoneCount = new HashMap<>();
        for (int i = 0; i < records.length(); i++) {
            try {
                String zone = records.getJSONObject(i).optString("zone", "Zona no identificada");
                zoneCount.put(zone, zoneCount.getOrDefault(zone, 0) + 1);
            } catch (JSONException ignored) {
            }
        }

        List<Map.Entry<String, Integer>> ranking = new ArrayList<>(zoneCount.entrySet());
        Collections.sort(ranking, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });

        StringBuilder summary = new StringBuilder();
        summary.append("Participantes registrados: ").append(records.length()).append("\n\n");
        for (int i = 0; i < ranking.size(); i++) {
            Map.Entry<String, Integer> entry = ranking.get(i);
            summary.append(i + 1)
                    .append(". ")
                    .append(entry.getKey())
                    .append(" — ")
                    .append(entry.getValue())
                    .append(entry.getValue() == 1 ? " estudiante" : " estudiantes")
                    .append("\n");
        }

        if (!ranking.isEmpty()) {
            Map.Entry<String, Integer> top = ranking.get(0);
            summary.append("\nZona con mayor concentración: ")
                    .append(top.getKey())
                    .append(" (")
                    .append(top.getValue())
                    .append(")");
        }

        tvSummary.setText(summary.toString());
    }

    private void clearRecords() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_RECORDS).apply();
        refreshSummary();
        Toast.makeText(this, "Registros eliminados", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtainLastKnownLocation();
            } else {
                tvStatus.setText("Estado: permiso de ubicación denegado");
                Toast.makeText(this, "El permiso GPS es necesario para el laboratorio", Toast.LENGTH_LONG).show();
            }
        }
    }
}
