package com.example.aura;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.core.Prefs;
import com.example.aura.databinding.ActivityMainBinding;
// No necesitarás RegisterActivity aquí, pero no hace daño dejarlo.
import com.example.aura.ui.RegisterActivity;
import com.example.aura.ui.AddContactActivity;
import com.example.aura.ui.ContactListActivity;
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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.util.Log;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMainBinding binding;
    private GoogleMap gmap;
    private FusedLocationProviderClient fused;
    private static final int REQ_LOCATION = 1001;

    // ... (El resto de tus variables y métodos de reportes no cambian) ...

    // ===== Reportes simulados =====
    private static class Report {
        final LatLng pos;
        final String title;
        Report(double lat, double lon, String t) { pos = new LatLng(lat, lon); title = t; }
    }

    private final List<Report> mockReports = Arrays.asList(
            new Report(-25.2815, -57.6358, "Alerta 1"),
            new Report(-25.2899, -57.6281, "Alerta 2"),
            new Report(-25.3002, -57.6405, "Alerta 3 (lejos)")
    );

    private void showNearbyReports(LatLng me) {
        int count = 0;
        for (Report r : mockReports) {
            double d = HaversineUtils.distanceKm(
                    me.latitude, me.longitude, r.pos.latitude, r.pos.longitude);
            if (d <= 1.0) {
                count++;
                gmap.addMarker(new MarkerOptions()
                        .position(r.pos)
                        .title(r.title + " • " + String.format(Locale.US, "%.2f km", d)));
            }
        }
        android.util.Log.d("REPORTS", "Cercanos dibujados: " + count);
    }


    // ===== onCreate =====
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ===================== BLOQUE A ELIMINAR =====================
        /*
        // ✅ Verifica si el usuario ya tiene perfil guardado
        if (!com.example.aura.core.Prefs.isUserLoggedIn(getApplicationContext())){
            // Si no tiene perfil → ir a pantalla de registro
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }
        */
        // =================== FIN DEL BLOQUE ELIMINADO ==================

        // Ahora, esta actividad simplemente carga su layout sin ninguna condición.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ==================== PRUEBA DE CONEXIÓN A FIREBASE ====================
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("test_connection");

        ref.setValue("Firebase OK")
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseTest", "✅ Conexión exitosa con Firebase");
                    Toast.makeText(this, "Conectado a Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseTest", "Error al conectar con Firebase", e);
                    Toast.makeText(this, "Error al conectar con Firebase", Toast.LENGTH_SHORT).show();
                });
// =======================================================================


        // Botones del menú principal (tu parte)
        binding.btnAddContact.setOnClickListener(v ->
                startActivity(new Intent(this, AddContactActivity.class)));

        binding.btnViewContacts.setOnClickListener(v ->
                startActivity(new Intent(this, ContactListActivity.class)));

//        binding.btnEmergencyModule.setOnClickListener(v ->
//                Toast.makeText(this, "Módulo de emergencia (Sofi)", Toast.LENGTH_SHORT).show());

        // Configuración del mapa (Ana)
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

    // ... (El resto de tus métodos: onMapReady, onRequestPermissionsResult, etc., se quedan igual) ...
    // ===== Mapa listo =====
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
                showNearbyReports(target);
            }
        });
    }

    // ===== Crear reportes =====
    private void showCreateReportDialog(LatLng pos) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Describe por qué es peligroso…");
        input.setMinLines(2);
        input.setMaxLines(4);

        String[] levels = {"Bajo", "Medio", "Alto"};
        final int[] selected = {1};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nuevo reporte")
                .setView(input)
                .setSingleChoiceItems(levels, 0, (d, which) -> selected[0] = which + 1)
                .setPositiveButton("Guardar", (d, w) -> {
                    String comment = input.getText().toString().trim();
                    saveReport(pos, comment, selected[0]);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveReport(LatLng pos, String comment, int severity) {
        if (comment.isEmpty()) comment = "(sin comentario)";
        float hue = BitmapDescriptorFactory.HUE_YELLOW;
        if (severity == 2) hue = BitmapDescriptorFactory.HUE_ORANGE;
        if (severity == 3) hue = BitmapDescriptorFactory.HUE_RED;

        // 1. Mostrar el marcador localmente
        gmap.addMarker(new MarkerOptions()
                .position(pos)
                .title(comment)
                .snippet("Riesgo: " + severity)
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));

        // 2. Subir a Firebase
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reportsRef = db.getReference("reports");

        String reportId = reportsRef.push().getKey(); // genera ID único
        if (reportId != null) {
            ReportData report = new ReportData(
                    pos.latitude,
                    pos.longitude,
                    comment,
                    severity,
                    String.valueOf(Prefs.getLoggedInUserId(this))
            );

            reportsRef.child(reportId).setValue(report)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Reporte guardado.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al guardar reporte.", Toast.LENGTH_SHORT).show());
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
