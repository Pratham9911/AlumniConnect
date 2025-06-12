package com.pratham.alumniconnect;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class Login_Screen extends AppCompatActivity {

    private static final String TAG = "Login_Screen";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable night mode globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Auto login if already signed in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(Login_Screen.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);

        // Google Sign-In Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI Elements
        ImageView switchIcon = findViewById(R.id.switchIcon);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        TextView loginText = findViewById(R.id.loginText);
        TextView registerText = findViewById(R.id.registerText);
        LinearLayout loginForm = findViewById(R.id.formLayout);
        LinearLayout registerForm = findViewById(R.id.registerFormLayout);

        loginText.setTextColor(Color.WHITE);
        registerText.setTextColor(Color.parseColor("#80FFFFFF"));
        loginForm.setVisibility(View.VISIBLE);
        registerForm.setVisibility(View.GONE);

        loginText.setOnClickListener(view -> {
            loginText.setTextColor(Color.WHITE);
            registerText.setTextColor(Color.parseColor("#80FFFFFF"));
            loginForm.setVisibility(View.VISIBLE);
            registerForm.setVisibility(View.GONE);
            switchIcon.startAnimation(rotate);
        });

        registerText.setOnClickListener(view -> {
            loginText.setTextColor(Color.parseColor("#80FFFFFF"));
            registerText.setTextColor(Color.WHITE);
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
            switchIcon.startAnimation(rotate);
        });

        // Password toggle for login
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        ImageView passwordToggle = findViewById(R.id.passwordToggle);
        final boolean[] isLoginPasswordVisible = {false};

        passwordToggle.setOnClickListener(v -> {
            if (isLoginPasswordVisible[0]) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_remove_red_eye_24);
            } else {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            }
            passwordEditText.setSelection(passwordEditText.length());
            isLoginPasswordVisible[0] = !isLoginPasswordVisible[0];
        });

        // Password toggle for registration
        EditText regPasswordEditText = findViewById(R.id.regpasswordEditText);
        ImageView regPasswordToggle = findViewById(R.id.regpasswordToggle);
        final boolean[] isRegPasswordVisible = {false};

        regPasswordToggle.setOnClickListener(v -> {
            if (isRegPasswordVisible[0]) {
                regPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                regPasswordToggle.setImageResource(R.drawable.baseline_remove_red_eye_24);
            } else {
                regPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                regPasswordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            }
            regPasswordEditText.setSelection(regPasswordEditText.length());
            isRegPasswordVisible[0] = !isRegPasswordVisible[0];
        });

        // Login logic
        Button loginButton = findViewById(R.id.loginButton);
        EditText emailEditText = findViewById(R.id.emailEditText);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email is registered
            Log.d(TAG, "Checking if email is registered: " + email);
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean isEmailRegistered = task.getResult() != null && task.getResult().getSignInMethods() != null && !task.getResult().getSignInMethods().isEmpty();
                            Log.d(TAG, "Email registered: " + isEmailRegistered);
                            if (!isEmailRegistered) {
                                Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Proceed with login
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(this, authTask -> {
                                        if (authTask.isSuccessful()) {
                                            Log.d(TAG, "Login successful for email: " + email);
                                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);

                                        } else {
                                            Exception e = authTask.getException();
                                            Log.e(TAG, "Login failed: " + e.getMessage());
                                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                            } else if (e instanceof FirebaseAuthInvalidUserException) {
                                                Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Email check failed: " + task.getException().getMessage());
                            Toast.makeText(this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Register logic
        Button regButton = findViewById(R.id.registerButton);
        EditText regNameEditText = findViewById(R.id.regNameEditText);
        EditText regEmailEditText = findViewById(R.id.regEmailEditText);

        regButton.setOnClickListener(v -> {
            String regName = regNameEditText.getText().toString().trim();
            String regEmail = regEmailEditText.getText().toString().trim();
            String regPassword = regPasswordEditText.getText().toString().trim();

            if (regName.isEmpty() || regEmail.isEmpty() || regPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(regEmail).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (regPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email is already registered
            Log.d(TAG, "Checking if email is already registered: " + regEmail);
            mAuth.fetchSignInMethodsForEmail(regEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean isEmailRegistered = task.getResult() != null && task.getResult().getSignInMethods() != null && !task.getResult().getSignInMethods().isEmpty();
                            Log.d(TAG, "Email already registered: " + isEmailRegistered);
                            if (isEmailRegistered) {
                                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Proceed with registration
                            mAuth.createUserWithEmailAndPassword(regEmail, regPassword)
                                    .addOnCompleteListener(this, authTask -> {
                                        if (authTask.isSuccessful()) {
                                            Log.d(TAG, "Registration successful for email: " + regEmail);
                                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null) {
                                                createUserInFirestore(user);
                                            }
                                            Intent intent = new Intent(this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);

                                        } else {
                                            Exception e = authTask.getException();
                                            Log.e(TAG, "Registration failed: " + e.getMessage());
                                            if (e instanceof FirebaseAuthUserCollisionException) {
                                                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Email check failed: " + task.getException().getMessage());
                            Toast.makeText(this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Google Sign-In Buttons for both login and registration
        ImageView googleIconLogin = findViewById(R.id.google_icon1);
        ImageView googleIconRegister = findViewById(R.id.google_icon);

        googleIconLogin.setOnClickListener(v -> {
            Log.d(TAG, "Google Sign-In (Login) clicked");
            signInWithGoogle();
        });

        googleIconRegister.setOnClickListener(v -> {
            Log.d(TAG, "Google Sign-In (Register) clicked");
            signInWithGoogle();
        });


        // GitHub Sign-In Buttons for both login and registration
        ImageView githubIconLogin = findViewById(R.id.github_icon);
        ImageView githubIconRegister = findViewById(R.id.github1_icon);

        githubIconLogin.setOnClickListener(v -> {
            Log.d(TAG, "GitHub Sign-In (Login) clicked");
            signInWithGitHub();
        });

        githubIconRegister.setOnClickListener(v -> {
            Log.d(TAG, "GitHub Sign-In (Register) clicked");
            signInWithGitHub();
        });

    }

    //github sigin method
    private void signInWithGitHub() {
        Log.d(TAG, "Starting GitHub Sign-In");

        OAuthProvider.Builder provider = OAuthProvider.newBuilder("github.com");

        // Optional: You can add custom scopes here
        // provider.addCustomParameter("login", "your-email@example.com");
        provider.setScopes(Arrays.asList("user:email")); // Keep minimal for now

        // Check if already pending (Firebase needs this check)
        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener(authResult -> {
                        Log.d(TAG, "GitHub Sign-In success (pending)");
                        Toast.makeText(this, "GitHub Sign-In Successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            createUserInFirestore(user);
                        }
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "GitHub Sign-In failed (pending): " + e.getMessage());
                        Toast.makeText(this, "GitHub Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            mAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener(authResult -> {
                        Log.d(TAG, "GitHub Sign-In success");
                        Toast.makeText(this, "GitHub Sign-In Successful", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            createUserInFirestore(user);
                        }
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "GitHub Sign-In failed: " + e.getMessage());
                        Toast.makeText(this, "GitHub Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    // Start Google Sign-In
    private void signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign-In");
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    // Handle Google Sign-In result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount account = task.getResult();
                            Log.d(TAG, "Google Sign-In successful, email: " + account.getEmail());
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                            mAuth.signInWithCredential(credential)
                                    .addOnCompleteListener(this, authTask -> {
                                        if (authTask.isSuccessful()) {
                                            Log.d(TAG, "Google authentication successful");
                                            Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show();
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null) {
                                                createUserInFirestore(user);
                                            }

                                            Intent intent = new Intent(this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);

                                        } else {
                                            Log.e(TAG, "Google authentication failed: " + authTask.getException().getMessage());
                                            Toast.makeText(this, "Google Authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Google Sign-In failed: " + task.getException().getMessage());
                            Toast.makeText(this, "Google Sign-In failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createUserInFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {

                    // Get name from registration EditText if available
                    String name = "";
                    EditText regNameEditText = findViewById(R.id.regNameEditText);
                    if (regNameEditText != null) {
                        name = regNameEditText.getText().toString().trim();
                    }

                    // Fallback to Firebase user's display name
                    if (name.isEmpty() && user.getDisplayName() != null) {
                        name = user.getDisplayName();
                    }

                    UserModel newUser = new UserModel(
                            uid,
                            name,
                            user.getEmail(),
                            user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "",
                            "", // backgroundImageUrl
                            "", // role
                            "", // workplace
                            "", // address
                            ""  // bio
                    );

                    userRef.set(newUser)
                            .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "New user added"))
                            .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to add user", e));
                }
            } else {
                Log.e("FIREBASE", "Failed to check user", task.getException());
            }
        });
    }

}

