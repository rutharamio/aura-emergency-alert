package com.example.aura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.MainActivity;
import com.example.aura.R;
import com.example.aura.core.AuthManager;
import com.example.aura.core.Prefs;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Doble verificación (local y Firebase)
        if (Prefs.isUserLoggedIn(getApplicationContext()) || AuthManager.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Mostrar pantalla de bienvenida
        setContentView(R.layout.activity_welcome);

        // Animación para el título
        TextView titleAura = findViewById(R.id.textTitleAura);
        Animation fadeInBounce = AnimationUtils.loadAnimation(this, R.anim.fade_in_bounce);
        titleAura.startAnimation(fadeInBounce);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Ir a LoginActivity
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        // Ir a RegisterActivity
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
