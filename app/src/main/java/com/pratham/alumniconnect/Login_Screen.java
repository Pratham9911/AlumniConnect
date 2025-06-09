package com.pratham.alumniconnect;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable night mode globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Animating the rotating icon
        ImageView switchIcon = findViewById(R.id.switchIcon);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        // UI Elements
        TextView loginText = findViewById(R.id.loginText);
        TextView registerText = findViewById(R.id.registerText);
        LinearLayout loginForm = findViewById(R.id.formLayout);
        LinearLayout registerForm = findViewById(R.id.registerFormLayout);

        // Set default: Login is active
        loginText.setTextColor(Color.WHITE);
        registerText.setTextColor(Color.parseColor("#80FFFFFF")); // semi-transparent

        loginForm.setVisibility(View.VISIBLE);
        registerForm.setVisibility(View.GONE);

        // Switch to Login
        loginText.setOnClickListener(view -> {
            loginText.setTextColor(Color.WHITE);
            registerText.setTextColor(Color.parseColor("#80FFFFFF"));
            loginForm.setVisibility(View.VISIBLE);
            registerForm.setVisibility(View.GONE);
            switchIcon.startAnimation(rotate);
        });

        // Switch to Register
        registerText.setOnClickListener(view -> {
            loginText.setTextColor(Color.parseColor("#80FFFFFF"));
            registerText.setTextColor(Color.WHITE);
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
            switchIcon.startAnimation(rotate);
        });



        //Password show/hide logic

        // LOGIN Password Toggle
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        ImageView passwordToggle = findViewById(R.id.passwordToggle);
        final boolean[] isLoginVisible = {false};

        passwordToggle.setOnClickListener(v -> {
            if (isLoginVisible[0]) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_remove_red_eye_24);
            } else {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            }
            passwordEditText.setSelection(passwordEditText.length());
            isLoginVisible[0] = !isLoginVisible[0];
        });

// REGISTER Password Toggle
        EditText regPasswordEditText = findViewById(R.id.regpasswordEditText);
        ImageView regPasswordToggle = findViewById(R.id.regpasswordToggle);
        final boolean[] isRegVisible = {false};

        regPasswordToggle.setOnClickListener(v -> {
            if (isRegVisible[0]) {
                regPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                regPasswordToggle.setImageResource(R.drawable.baseline_remove_red_eye_24);
            } else {
                regPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                regPasswordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            }
            regPasswordEditText.setSelection(regPasswordEditText.length());
            isRegVisible[0] = !isRegVisible[0];


        });


        //Login email validation
        Button loginButton = findViewById(R.id.loginButton);
        EditText emailEditText = findViewById(R.id.emailEditText);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login_Screen.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Login_Screen.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(Login_Screen.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                // Proceed with login logic (Firebase / local check)
                Toast.makeText(Login_Screen.this, "Login Success", Toast.LENGTH_SHORT).show();
            }
        });


        Button regButton = findViewById(R.id.registerButton);
        EditText regEmailEditText = findViewById(R.id.regEmailEditText);

        regButton.setOnClickListener(v -> {
            String regEmail = regEmailEditText.getText().toString().trim();
            String regPassword = regPasswordEditText.getText().toString().trim();

            if (regEmail.isEmpty() || regPassword.isEmpty()) {
                Toast.makeText(Login_Screen.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(regEmail).matches()) {
                Toast.makeText(Login_Screen.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (regPassword.length() < 6) {
                Toast.makeText(Login_Screen.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                // Proceed with register logic (Firebase or DB)
                Toast.makeText(Login_Screen.this, "Registration Success", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
