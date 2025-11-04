package com.example.aura;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager; // IMPORT NECESARIO
import android.os.Bundle;import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // IMPORT NECESARIO
import androidx.core.content.ContextCompat;

import com.example.aura.core.Prefs;
import com.example.aura.databinding.ActivityMainBinding;
import com.example.aura.services.EmergencyService; // IMPORT NECESARIO
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

import java.util.ArrayList; // IMPORT NECESARIO
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMainBinding binding;
    private GoogleMap gmap;
    private FusedLocationProviderClient fused;
    private static final int REQ_LOCATION_AND_SMS = 101; // Cambiamos el nombre para que sea más claro

    private Button btnEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ================== SOLUCIÓN AÑADIDA AQUÍ (PARTE 1) ==================
        // Llamamos al método para solicitar permisos al iniciar la actividad
        solicitarPermisosNecesarios();
        // =====================================================================

        // ==================== PRUEBA DE CONEXIÓN A FIREBASE ====================
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("test_connection");

        ref.setValue("Firebase OK")
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseTest", "Conexión exitosa con Firebase");
                    // Toast.makeText(this, "Conectado a Firebase", Toast.LENGTH_SHORT).show(); // Opcional, lo comento para no molestar
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseTest", "Error al conectar con Firebase", e);
                    // Toast.makeText(this, "Error al conectar con Firebase", Toast.LENGTH_SHORT).show(); // Opcional
                });
        // =======================================================================

        // Botones de contactos
        binding.btnAddContact.setOnClickListener(v ->
                startActivity(new Intent(this, AddContactActivity.class)));

        binding.btnViewContacts.setOnClickListener(v ->
                startActivity(new Intent(this, ContactListActivity.class)));

        // Botón emergencia (Tu código ya está correcto aquí)
        btnEmergency = findViewById(R.id.btnEmergency);
        btnEmergency.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirmar Emergencia")
                    .setMessage("¿Estás seguro de que deseas enviar una alerta de emergencia a tus contactos?")
                    .setPositiveButton("SÍ, ENVIAR ALERTA", (dialog, which) -> {
                        Intent serviceIntent = new Intent(this, EmergencyService.class);
                        ContextCompat.startForegroundService(this, serviceIntent);
                        Toast.makeText(this, "Activando módulo de emergencia...", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("CANCELAR", null)
                    .show();
        });


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
        // showNearbyReports(asuncion); // Comentado para no mezclar con los de Firebase

        gmap.setOnMapLongClickListener(this::showCreateReportDialog);

        enableMyLocationIfGranted(); // Este método ahora depende del nuevo sistema de permisos
        loadReportsFromFirebase();
    }

    // ================== SOLUCIÓN AÑADIDA AQUÍ (PARTE 2) ==================
    // La definición del método va junto a los otros métodos de permisos.

    // ===== Permisos =====

    private void solicitarPermisosNecesarios() {
        // Lista de permisos peligrosos que la app necesita para funcionar al 100%
        String[] permisos = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.SEND_SMS
        };

        // Filtramos la lista para pedir solo los que aún no han sido concedidos
        List<String> permisosPorPedir = new ArrayList<>();
        for (String permiso : permisos) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                permisosPorPedir.add(permiso);
            }
        }

        // Si hay permisos sin conceder, se los pedimos al usuario
        if (!permisosPorPedir.isEmpty()) {
            Log.d("Permissions", "Pidiendo permisos al usuario...");
            ActivityCompat.requestPermissions(
                    this,
                    permisosPorPedir.toArray(new String[0]),
                    REQ_LOCATION_AND_SMS // Usamos nuestro código de solicitud
            );
        }
    }


    @SuppressLint("MissingPermission")
    private void enableMyLocationIfGranted() {
        if (gmap == null) return;
        // Solo verificamos el permiso de ubicación aquí, el de SMS no es necesario para el mapa
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gmap.setMyLocationEnabled(true);
            fetchLastLocationAndCenter();
        } else {
            // Si el permiso no está, la función solicitarPermisosNecesarios() ya lo pidió.
            // O podemos ser más insistentes y pedirlo de nuevo si es crucial para esta acción.
            Log.d("Permissions", "Permiso de ubicación no concedido aún.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOCATION_AND_SMS) {
            // Revisamos los resultados de nuestra solicitud de permisos
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "Permiso de UBICACIÓN concedido.");
                        // Si se concedió el permiso de ubicación, intentamos activar la capa del mapa
                        enableMyLocationIfGranted();
                    }
                    if (permissions[i].equals(android.Manifest.permission.SEND_SMS)
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "Permiso de SMS concedido.");
                        // No se necesita una acción inmediata, pero el servicio ya podrá usarlo.
                    }
                }
            }
        }
    }

    // El resto de tus métodos (fetchLastLocationAndCenter, showNearbyReports, etc.) se quedan igual
    // ...
    // [COPIA Y PEGA EL RESTO DE TUS MÉTODOS DESDE AQUÍ]
    // ...
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
