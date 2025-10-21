package com.example.aura;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Mantener padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Conectar el fragment del mapa del XML
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Botón recenter (opcional)
        findViewById(R.id.btnRecenter).setOnClickListener(v -> {
            if (gmap != null) {
                LatLng asuncion = new LatLng(-25.281, -57.635);
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(asuncion, 14f));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gmap = googleMap;

        // Punto inicial temporal (hasta tener permisos + API key)
        LatLng asuncion = new LatLng(-25.281, -57.635);
        gmap.addMarker(new MarkerOptions().position(asuncion).title("Asunción"));
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(asuncion, 13f));

        // NO activar ubicación real todavía (esperamos a Ruth):
        // enableMyLocationIfGranted();
    }
}

