package com.example.aura;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.core.Prefs;
import com.example.aura.databinding.ActivityMainBinding;
import com.example.aura.services.PowerButtonService;
import com.example.aura.ui.AddContactActivity;
import com.example.aura.ui.ContactListActivity;
import com.example.aura.ui.EmergencyModuleActivity;
import com.example.aura.utils.HaversineUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMainBinding binding;
    private GoogleMap gmap;
    private FusedLocationProviderClient fused;
    private static final int REQ_LOCATION = 1001;

    private Button btnEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Iniciar servicio de detección de botón Power
        Intent powerService = new Intent(this, PowerButtonService.class);
        startService(powerService);

        // ==================== PRUEBA DE CONEXIÓN A FIREBASE ====================
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("test_connection");

        ref.setValue("Firebase OK")
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseTest", "Conexión exitosa con Firebase");
                    Toast.makeText(this, "Conectado a Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseTest", "Error al conectar con Firebase", e);
                    Toast.makeText(this, "Error al conectar con Firebase", Toast.LENGTH_SHORT).show();
                });
        // =======================================================================

        // Botones de contactos
        binding.btnAddContact.setOnClickListener(v ->
                startActivity(new Intent(this, AddContactActivity.class)));

        binding.btnViewContacts.setOnClickListener(v ->
                startActivity(new Intent(this, ContactListActivity.class)));

        // Botón emergencia
        btnEmergency = findViewById(R.id.btnEmergency);
        btnEmergency.setOnClickListener(v ->
                startActivity(new Intent(this, EmergencyModuleActivity.class)));

        // Configuración del mapa
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        fused = LocationServices.getFusedLocationProviderClient(this);

        // Botón para recentrar mapa
        findViewById(R.id.btnRecenter).setOnClickListener(v -> {
            if (gmap != null) {
                LatLng asuncion = new LatLng(-25.281, -57.635);
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(asuncion, 14f));
            }
        });
    }

    // ===== Mapa =====
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gmap = googleMap;

        gmap.getUiSettings().setZoomControlsEnabled(true);
        gmap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng asuncion = new LatLng(-25.281, -57.635);
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(asuncion, 13f));
        showNearbyReports(asuncion);

        gmap.setOnMapLongClickListener(this::showCreateReportDialog);

        enableMyLocationIfGranted();
        loadReportsFromFirebase();
    }

    // ===== Permisos =====
    @SuppressLint("MissingPermission")
    private void enableMyLocationIfGranted() {
        if (gmap == null) return;
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            gmap.setMyLocationEnabled(true);
            fetchLastLocationAndCenter();
        } else {
            androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0
                && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            enableMyLocationIfGranted();
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLastLocationAndCenter() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) return;

        fused.getLastLocation().addOnSuccessListener(loc -> {
            LatLng target = (loc != null)
                    ? new LatLng(loc.getLatitude(), loc.getLongitude())
                    : new LatLng(-25.281, -57.635);
            if (gmap != null) {
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 16f));
            }
        });
    }

    private void showNearbyReports(LatLng me) {
        int count = 0;
        for (LatLng pos : Arrays.asList(
                new LatLng(-25.2815, -57.6358),
                new LatLng(-25.2899, -57.6281),
                new LatLng(-25.3002, -57.6405)
        )) {
            double d = HaversineUtils.distanceKm(me.latitude, me.longitude, pos.latitude, pos.longitude);
            if (d <= 1.0) {
                gmap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title("Reporte cercano (" + String.format(Locale.US, "%.2f km", d) + ")"));
                count++;
            }
        }
        Log.d("REPORTS", "Cercanos dibujados: " + count);
    }

    private void showCreateReportDialog(LatLng pos) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Describe por qué es peligroso…");
        String[] levels = {"Bajo", "Medio", "Alto"};
        final int[] selected = {1};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nuevo reporte")
                .setView(input)
                .setSingleChoiceItems(levels, 0, (d, which) -> selected[0] = which + 1)
                .setPositiveButton("Guardar", (d, w) -> {
                    saveReport(pos, input.getText().toString().trim(), selected[0]);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveReport(LatLng pos, String comment, int severity) {
        if (comment.isEmpty()) comment = "(sin comentario)";
        float hue = BitmapDescriptorFactory.HUE_YELLOW;
        if (severity == 2) hue = BitmapDescriptorFactory.HUE_ORANGE;
        if (severity == 3) hue = BitmapDescriptorFactory.HUE_RED;

        gmap.addMarker(new MarkerOptions()
                .position(pos)
                .title(comment)
                .snippet("Riesgo: " + severity)
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reportsRef = db.getReference("reports");
        String reportId = reportsRef.push().getKey();

        if (reportId != null) {
            ReportData report = new ReportData(
                    pos.latitude, pos.longitude, comment, severity,
                    String.valueOf(Prefs.getLoggedInUserId(this))
            );
            reportsRef.child(reportId).setValue(report)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Reporte guardado ✅", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al guardar reporte ❌", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadReportsFromFirebase() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reportsRef = db.getReference("reports");

        reportsRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot child : snapshot.getChildren()) {
                ReportData r = child.getValue(ReportData.class);
                if (r != null) {
                    float hue = BitmapDescriptorFactory.HUE_YELLOW;
                    if (r.severity == 2) hue = BitmapDescriptorFactory.HUE_ORANGE;
                    if (r.severity == 3) hue = BitmapDescriptorFactory.HUE_RED;
                    LatLng pos = new LatLng(r.lat, r.lon);
                    gmap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(r.comment)
                            .snippet("Riesgo: " + r.severity)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                }
            }
        }).addOnFailureListener(e ->
                Log.e("FirebaseLoad", "Error cargando reportes", e));
    }
}
