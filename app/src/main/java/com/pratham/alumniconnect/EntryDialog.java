package com.pratham.alumniconnect;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

public class EntryDialog extends Dialog {

    private EditText titleInput, subtitleInput, linkInput;
    private ImageView imagePreview;
    private Button saveBtn;

    private Uri selectedImageUri;
    private EntryModel preloadedEntry;

    private final ActivityResultLauncher<Intent> imagePickerLauncher;

    public interface OnEntrySavedListener {
        void onEntrySaved(EntryModel entry);
    }

    public EntryDialog(@NonNull Context context, OnEntrySavedListener listener, ActivityResultLauncher<Intent> launcher) {
        super(context, android.R.style.Theme_Material_Light_Dialog_Alert);

        setContentView(R.layout.dialog_entry);
        setCancelable(true);

        this.imagePickerLauncher = launcher;

        // View bindings
        titleInput = findViewById(R.id.titleInput);
        subtitleInput = findViewById(R.id.subtitleInput);
        linkInput = findViewById(R.id.linkInput);
        imagePreview = findViewById(R.id.entryImagePreview);
        saveBtn = findViewById(R.id.saveEntryBtn);

        imagePreview.setVisibility(View.GONE);  // Initially hidden

        // Save button logic
        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String subtitle = subtitleInput.getText().toString().trim();
            String link = linkInput.getText().toString().trim();

            // ✅ Validate title and description
            if (title.isEmpty()) {
                Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (subtitle.isEmpty()) {
                Toast.makeText(context, "Description is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Create entry model
            EntryModel entry = new EntryModel(
                    title,
                    subtitle,
                    selectedImageUri != null ? selectedImageUri.toString() : null,
                    link
            );

            listener.onEntrySaved(entry);
            dismiss();
        });
    }

    /**
     * Preload data into the dialog for editing an entry.
     */
    public void setPreloadedEntry(EntryModel entry) {
        this.preloadedEntry = entry;

        if (entry != null) {
            titleInput.setText(entry.getTitle());
            subtitleInput.setText(entry.getSubtitle());
            linkInput.setText(entry.getLink());

            if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
                selectedImageUri = Uri.parse(entry.getImageUrl());
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageURI(selectedImageUri);
            }
        }
    }

    /**
     * Set the selected image URI from the image picker launcher.
     */
    public void setSelectedImageUri(Uri uri) {
        selectedImageUri = uri;
        imagePreview.setVisibility(View.VISIBLE);
        imagePreview.setImageURI(uri);
    }

    /**
     * Optional: Open gallery to pick an image (if you enable it later).
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
}
