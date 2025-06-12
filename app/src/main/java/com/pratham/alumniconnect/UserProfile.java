package com.pratham.alumniconnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class UserProfile extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    TextView nameText, roleText, workplaceText, addressText, bioText;
    ImageView profileImage, backgroundImage;

    View loadingContainer;
    View realContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Views
        nameText = findViewById(R.id.nameText);
        roleText = findViewById(R.id.roleText);
        workplaceText = findViewById(R.id.workplaceText);
        addressText = findViewById(R.id.addressText);
        bioText = findViewById(R.id.bioText);
        profileImage = findViewById(R.id.profile_image);
        backgroundImage = findViewById(R.id.bg_image);

        loadingContainer = findViewById(R.id.loadingContainer); // FrameLayout with ProgressBar
        realContent = findViewById(R.id.usermain); // ScrollView with real profile layout

        // Initial UI state
        loadingContainer.setVisibility(View.VISIBLE);
        realContent.setVisibility(View.GONE);

        fetchAndShowUserData();
    }

    private void fetchAndShowUserData() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Hide loader, show actual profile
                        loadingContainer.setVisibility(View.GONE);
                        realContent.setVisibility(View.VISIBLE);

                        // Set texts
                        setOrHide(nameText, document.getString("name"));
                        setOrHide(roleText, document.getString("role"));
                        setOrHide(workplaceText, document.getString("workplace"));
                        setOrHide(addressText, document.getString("address"));
                        setOrHide(bioText, document.getString("bio"));

                        // Load images
                        Glide.with(this)
                                .load(document.getString("profileImageUrl"))
                                .dontAnimate()
                                .placeholder(R.drawable.ic_user)
                                .into(profileImage);

                        Glide.with(this)
                                .load(document.getString("backgroundImageUrl"))
                                .dontAnimate()
                                .placeholder(R.drawable.bg_placeholder)
                                .into(backgroundImage);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingContainer.setVisibility(View.GONE);
                    realContent.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Error loading user data", e);
                });
    }

    private void setOrHide(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(text);
            view.setVisibility(View.VISIBLE);
        }
    }
}
