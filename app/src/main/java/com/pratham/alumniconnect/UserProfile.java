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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserProfile extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    TextView nameText, roleText, workplaceText, addressText, bioText;
    ImageView profileImage, backgroundImage;

    ImageView whatsappIcon, linkedinIcon, facebookIcon, twitterIcon;
    ImageView editProfileIcon;

    View loadingContainer;
    View realContent;

    private List<SectionModel> sectionList;
    private SectionAdapter adapter;

    private ActivityResultLauncher<Intent> entryImagePickerLauncher;
    private EntryDialog currentEntryDialog; // to pass back image URI

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

        entryImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (currentEntryDialog != null) {
                            currentEntryDialog.setSelectedImageUri(uri);
                        }
                    }
                }
        );


        editProfileIcon.setOnClickListener(v -> {
            startActivity(new Intent(UserProfile.this, Edit_Profile.class));
        });

        loadingContainer = findViewById(R.id.loadingContainer);
        realContent = findViewById(R.id.usermain);

        loadingContainer.setVisibility(View.VISIBLE);
        realContent.setVisibility(View.GONE);

        setupProfileSections();






    }

    private void setupProfileSections() {
        RecyclerView sectionRecyclerView = findViewById(R.id.sectionRecyclerView);
        sectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sectionList = new ArrayList<>();
        sectionList.add(new SectionModel(UUID.randomUUID().toString(), "Education"));

        adapter = new SectionAdapter(sectionList, this, new SectionAdapter.OnSectionActionListener() {
            @Override
            public void onAddEntryClicked(SectionModel section) {
                EntryDialog dialog = new EntryDialog(UserProfile.this, entry -> {
                    section.getEntries().add(entry);
                    adapter.notifyDataSetChanged();
                    saveSectionsToFirestore();  // ✅ Save after entry added
                }, entryImagePickerLauncher);

                currentEntryDialog = dialog;
                dialog.show();
            }

            @Override
            public void onEditSection(SectionModel section) {
                Toast.makeText(UserProfile.this, "Edit section: " + section.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteSection(SectionModel section) {
                sectionList.remove(section);
                adapter.notifyDataSetChanged();
                Toast.makeText(UserProfile.this, "Section deleted", Toast.LENGTH_SHORT).show();
                saveSectionsToFirestore();  // ✅ Save after delete
            }
        });

        sectionRecyclerView.setAdapter(adapter);

        ImageView addSectionBtn = findViewById(R.id.addSectionBtn);
        addSectionBtn.setOnClickListener(v -> showAddSectionDialog());
    }


    private void showAddSectionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add New Section");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_section, null);
        builder.setView(customLayout);

        TextView titleInput = customLayout.findViewById(R.id.sectionTitleInput);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            if (!title.isEmpty()) {
                sectionList.add(new SectionModel(UUID.randomUUID().toString(), title));
                adapter.notifyItemInserted(sectionList.size() - 1);
                saveSectionsToFirestore();
            } else {
                Toast.makeText(this, "Section title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }





    private void pickBackgroundImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        bgPickerLauncher.launch(intent);
    }

    private void uploadBackgroundToCloudinary() {
        if (selectedBgUri == null) return;

        Toast.makeText(this, "Uploading background...", Toast.LENGTH_SHORT).show();

        String uid = FirebaseAuth.getInstance().getUid();
        String publicId = "bg_" + uid;

        MediaManager.get().upload(selectedBgUri)
                .option("public_id", publicId)
                .option("overwrite", true)
                .option("resource_type", "image")
                .option("folder", "user_backgrounds/")
                .option("transformation", new com.cloudinary.Transformation()
                        .width(1280).height(720).crop("limit")
                        .quality("auto")
                        .fetchFormat("auto"))
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
                    loadingContainer.setVisibility(View.VISIBLE);
                    realContent.setVisibility(View.GONE);
                    fetchAndShowUserData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update background", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error updating background URL", e);
                });
    }

    private void fetchAndShowUserData() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && isActivityActive) {
                        loadingContainer.setVisibility(View.GONE);
                        realContent.setVisibility(View.VISIBLE);

                        setOrHide(nameText, document.getString("name"));
                        setOrHide(roleText, document.getString("role"));
                        setOrHide(workplaceText, document.getString("workplace"));
                        setOrHide(addressText, document.getString("country"));
                        setOrHide(bioText, document.getString("bio"));

                        Glide.with(this)
                                .load(document.getString("profileImageUrl"))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .dontAnimate()
                                .placeholder(R.drawable.default_user)
                                .into(profileImage);

                        Glide.with(this)
                                .load(document.getString("backgroundImageUrl"))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .dontAnimate()
                                .placeholder(R.drawable.default_bg)
                                .into(backgroundImage);

                        setupSocialIcons(document.getString("whatsapp"),
                                document.getString("linkedin"),
                                document.getString("facebook"),
                                document.getString("twitter"));
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

    private void setupSocialIcons(String whatsapp, String linkedin, String facebook, String twitter) {
        setupSocialIcon(whatsappIcon, "https://wa.me/" + whatsapp, whatsapp);
        setupSocialIcon(linkedinIcon, linkedin, linkedin);
        setupSocialIcon(facebookIcon, facebook, facebook);
        setupSocialIcon(twitterIcon, twitter, twitter);
    }

    private void setupSocialIcon(ImageView iconView, String url, String check) {
        if (!TextUtils.isEmpty(check)) {
            iconView.setVisibility(View.VISIBLE);
            iconView.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        } else {
            iconView.setVisibility(View.GONE);
        }
    }

    private void saveSectionsToFirestore() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        var userDoc = db.collection("users").document(uid);
        var sectionColl = userDoc.collection("profileSections");

        for (SectionModel section : sectionList) {
            var sectionDoc = sectionColl.document(section.getSectionId());

            // Step 1: Delete all existing entries in this section
            sectionDoc.collection("entries")
                    .get()
                    .addOnSuccessListener(entrySnapshots -> {
                        for (var doc : entrySnapshots) {
                            doc.getReference().delete(); // Delete each old entry
                        }

                        // Step 2: Now (re)save section
                        sectionDoc.set(section)
                                .addOnSuccessListener(unused -> Log.d("Firestore", "Section saved: " + section.getTitle()))
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to save section", e));

                        // Step 3: Save all entries under it
                        for (EntryModel entry : section.getEntries()) {
                            sectionDoc.collection("entries")
                                    .document()  // Auto ID
                                    .set(entry)
                                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to save entry", e));
                        }
                    });
        }

        Toast.makeText(this, "Sections saved to Firestore", Toast.LENGTH_SHORT).show();
    }

    private void loadSectionsFromFirestore() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("profileSections")
                .get()
                .addOnSuccessListener(sectionSnapshots -> {
                    sectionList.clear();

                    for (var doc : sectionSnapshots) {
                        SectionModel section = doc.toObject(SectionModel.class);
                        section.setSectionId(doc.getId());

                        // Now fetch entries under this section
                        db.collection("users")
                                .document(uid)
                                .collection("profileSections")
                                .document(doc.getId())
                                .collection("entries")
                                .get()
                                .addOnSuccessListener(entrySnapshots -> {
                                    List<EntryModel> entries = new ArrayList<>();
                                    for (var entryDoc : entrySnapshots) {
                                        EntryModel entry = entryDoc.toObject(EntryModel.class);
                                        entries.add(entry);
                                    }
                                    section.setEntries(entries);
                                    adapter.notifyDataSetChanged(); // Refresh for each section once entries are loaded
                                });

                        sectionList.add(section);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load sections", Toast.LENGTH_SHORT).show();
                    Log.e("UserProfile", "Error loading sections", e);
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

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        loadingContainer.setVisibility(View.VISIBLE);
        realContent.setVisibility(View.GONE);
        fetchAndShowUserData();
        loadSectionsFromFirestore();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
    }
}
