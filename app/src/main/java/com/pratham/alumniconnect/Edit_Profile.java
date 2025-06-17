package com.pratham.alumniconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Edit_Profile extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText etName, etRole, etWorkplace, etDob, etCountry,
            etBio, etWhatsApp, etLinkedin, etFacebook, etTwitter;

    private ImageView profileImage;
    private Button btnSave;

    private String profileImageUrl; // Store existing or uploaded URL
    private Uri selectedImageUri;

    private final String CLOUD_NAME = "dvt0ac7op";
    private final String UPLOAD_PRESET = "alumni_unsigned";

    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.et_name);
        etRole = findViewById(R.id.et_role);
        etWorkplace = findViewById(R.id.et_workplace);
        etDob = findViewById(R.id.et_dob);
        etCountry = findViewById(R.id.et_country);

        etBio = findViewById(R.id.et_bio);
        etWhatsApp = findViewById(R.id.et_whats);
        etLinkedin = findViewById(R.id.et_linkedin);
        etFacebook = findViewById(R.id.et_facebook);
        etTwitter = findViewById(R.id.et_twitter);

        profileImage = findViewById(R.id.profile_image);
        btnSave = findViewById(R.id.btn_save);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        profileImage.setImageURI(selectedImageUri);
                    }
                });

        profileImage.setOnClickListener(v -> pickImageFromGallery());
        btnSave.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadToCloudinaryAndSave();
            } else {
                saveProfile(); // no image update
            }
        });

        fetchUserData();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void fetchUserData() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("name"));
                        etRole.setText(doc.getString("role"));
                        etWorkplace.setText(doc.getString("workplace"));
                        etDob.setText(doc.getString("dob"));
                        etCountry.setText(doc.getString("country"));
                        etBio.setText(doc.getString("bio"));
                        etWhatsApp.setText(doc.getString("whatsapp"));
                        etLinkedin.setText(doc.getString("linkedin"));
                        etFacebook.setText(doc.getString("facebook"));
                        etTwitter.setText(doc.getString("twitter"));

                        profileImageUrl = doc.getString("profileImageUrl");
                        if (!TextUtils.isEmpty(profileImageUrl)) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.default_user)
                                    .into(profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.e("EditProfile", "Error loading", e);
                });
    }


    private void uploadToCloudinaryAndSave() {
        if (selectedImageUri == null) return;

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(selectedImageUri)
                .option("public_id", "user_profiles/profile_" + FirebaseAuth.getInstance().getUid())
                .option("overwrite", true)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        profileImageUrl = Objects.requireNonNull(resultData.get("secure_url")).toString();
                        saveProfile();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        String err = "Upload failed: " + error.getDescription();
                        Toast.makeText(Edit_Profile.this, err, Toast.LENGTH_LONG).show();
                        Log.e("Cloudinary", err);  // 🔥 This will print the real error
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }


    private void saveProfile() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etName.getText().toString().trim());
        updates.put("role", etRole.getText().toString().trim());
        updates.put("workplace", etWorkplace.getText().toString().trim());
        updates.put("dob", etDob.getText().toString().trim());
        updates.put("country", etCountry.getText().toString().trim());
        updates.put("bio", etBio.getText().toString().trim());
        updates.put("whatsapp", etWhatsApp.getText().toString().trim());
        updates.put("linkedin", etLinkedin.getText().toString().trim());
        updates.put("facebook", etFacebook.getText().toString().trim());
        updates.put("twitter", etTwitter.getText().toString().trim());

        // Only update profileImageUrl if it's available (not null or empty)
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            updates.put("profileImageUrl", profileImageUrl);
        }

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                    Log.e("EditProfile", "Update error", e);
                });
    }

}
