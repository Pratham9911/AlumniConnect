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

    private EditText titleInput, subtitleInput;
    private ImageView imagePreview;
    private Button selectImageBtn, saveBtn;
    private Uri selectedImageUri;
    private final ActivityResultLauncher<Intent> imagePickerLauncher;

    public interface OnEntrySavedListener {
        void onEntrySaved(EntryModel entry);
    }

    public EntryDialog(@NonNull Context context, OnEntrySavedListener listener, ActivityResultLauncher<Intent> launcher) {
        super(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        setContentView(R.layout.dialog_entry);

        this.imagePickerLauncher = launcher;

        titleInput = findViewById(R.id.titleInput);
        subtitleInput = findViewById(R.id.subtitleInput);
        imagePreview = findViewById(R.id.entryImagePreview);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        saveBtn = findViewById(R.id.saveEntryBtn);

        imagePreview.setVisibility(View.GONE);  // Initially hidden

        selectImageBtn.setOnClickListener(v -> openImagePicker());

        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String subtitle = subtitleInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            EntryModel entry = new EntryModel(title, subtitle, selectedImageUri != null ? selectedImageUri.toString() : null);
            listener.onEntrySaved(entry);
            dismiss();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    public void setSelectedImageUri(Uri uri) {
        selectedImageUri = uri;
        imagePreview.setVisibility(View.VISIBLE);
        imagePreview.setImageURI(uri);
    }
}
