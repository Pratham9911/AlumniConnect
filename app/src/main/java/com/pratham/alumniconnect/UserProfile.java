package com.pratham.alumniconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

public class UserProfile extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    TextView nameText, roleText, workplaceText, addressText, bioText;
    ImageView profileImage, backgroundImage;

    ImageView whatsappIcon, linkedinIcon, facebookIcon, twitterIcon;
    ImageView editProfileIcon;

    View loadingContainer;
    View realContent;
    private boolean isActivityActive = false;

    private final String CLOUD_NAME = "dvt0ac7op";
    private final String UPLOAD_PRESET = "alumni_unsigned";

    private Uri selectedBgUri;
    ActivityResultLauncher<Intent> bgPickerLauncher;

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
        editProfileIcon = findViewById(R.id.edit_profile_icon);
        //icons
        whatsappIcon = findViewById(R.id.whatsappIcon);
        linkedinIcon = findViewById(R.id.linkedinIcon);
        facebookIcon = findViewById(R.id.facebookIcon);
        twitterIcon = findViewById(R.id.twitterIcon);


        ImageView editBgIcon = findViewById(R.id.edit_bg_icon);

        editBgIcon.setOnClickListener(v -> pickBackgroundImage());

        bgPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedBgUri = result.getData().getData();
                        uploadBackgroundToCloudinary();
                    }
                }
        );


        editProfileIcon.setOnClickListener(v -> {
            startActivity(new Intent(UserProfile.this, Edit_Profile.class));
        });

        loadingContainer = findViewById(R.id.loadingContainer); // FrameLayout with ProgressBar
        realContent = findViewById(R.id.usermain); // ScrollView with real profile layout

        // Initial UI state
        loadingContainer.setVisibility(View.VISIBLE);
        realContent.setVisibility(View.GONE);

      //  fetchAndShowUserData();
    }

    private void fetchAndShowUserData() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && isActivityActive) {
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
                        // Load profile image with cache disabled
                        Glide.with(this)
                                .load(document.getString("profileImageUrl"))
                                .skipMemoryCache(true) // disable memory cache
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // disable disk cache
                                .dontAnimate()
                                .placeholder(R.drawable.default_user)
                                .into(profileImage);

// Load background image (optional: skip cache here too)
                        Glide.with(this)
                                .load(document.getString("backgroundImageUrl"))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .dontAnimate()
                                .placeholder(R.drawable.default_bg)
                                .into(backgroundImage);

                        // icons logic
                        String whatsapp = document.getString("whatsapp");
                        String linkedin = document.getString("linkedin");
                        String facebook = document.getString("facebook");
                        String twitter = document.getString("twitter");

// WhatsApp
                        if (!TextUtils.isEmpty(whatsapp)) {
                            whatsappIcon.setVisibility(View.VISIBLE);
                            whatsappIcon.setOnClickListener(v -> {
                                Uri uri = Uri.parse("https://wa.me/" + whatsapp);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            });
                        } else {
                            whatsappIcon.setVisibility(View.GONE);
                        }

// LinkedIn
                        if (!TextUtils.isEmpty(linkedin)) {
                            linkedinIcon.setVisibility(View.VISIBLE);
                            linkedinIcon.setOnClickListener(v -> {
                                Uri uri = Uri.parse(linkedin);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            });
                        } else {
                            linkedinIcon.setVisibility(View.GONE);
                        }

// Facebook
                        if (!TextUtils.isEmpty(facebook)) {
                            facebookIcon.setVisibility(View.VISIBLE);
                            facebookIcon.setOnClickListener(v -> {
                                Uri uri = Uri.parse(facebook);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            });
                        } else {
                            facebookIcon.setVisibility(View.GONE);
                        }

// Twitter
                        if (!TextUtils.isEmpty(twitter)) {
                            twitterIcon.setVisibility(View.VISIBLE);
                            twitterIcon.setOnClickListener(v -> {
                                Uri uri = Uri.parse(twitter);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            });
                        } else {
                            twitterIcon.setVisibility(View.GONE);
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    if (isActivityActive) {
                        loadingContainer.setVisibility(View.GONE);
                        realContent.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        Log.e("UserProfile", "Error loading user data", e);
                    }

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
    private void pickBackgroundImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        bgPickerLauncher.launch(intent);
    }

    private void uploadBackgroundToCloudinary() {
        if (selectedBgUri == null) return;

        Toast.makeText(this, "Uploading background...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(selectedBgUri)
                .unsigned(UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String bgUrl = (String) resultData.get("secure_url");
                        updateBackgroundUrlInFirestore(bgUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(UserProfile.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        Log.e("Cloudinary", "Upload error: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();

    }

    private void updateBackgroundUrlInFirestore(String bgUrl) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .update("backgroundImageUrl", bgUrl)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Background updated", Toast.LENGTH_SHORT).show();
                    // 🔄 Refresh UI immediately after update
                    loadingContainer.setVisibility(View.VISIBLE);
                    realContent.setVisibility(View.GONE);
                    fetchAndShowUserData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update background", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error updating background URL", e);
                });
    }



    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        loadingContainer.setVisibility(View.VISIBLE);
        realContent.setVisibility(View.GONE);
        fetchAndShowUserData();
    }
    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
    }

}
