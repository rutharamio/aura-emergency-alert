// En com/example/aura/ui/WelcomeActivity.java
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
import com.example.aura.core.Prefs;

// En com/example/aura/ui/WelcomeActivity.java

// ... (código anterior) ...

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Esta parte está bien
        if (Prefs.isUserLoggedIn(getApplicationContext())) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        TextView titleAura = findViewById(R.id.textTitleAura); // asegúrate que este id coincida con tu TextView
        Animation fadeInBounce = AnimationUtils.loadAnimation(this, R.anim.fade_in_bounce);
        titleAura.startAnimation(fadeInBounce);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        // ===== CORRECCIÓN AQUÍ =====
        // El botón de Login debe llevar a LoginActivity
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        // Este ya está bien, apunta a RegisterActivity
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
