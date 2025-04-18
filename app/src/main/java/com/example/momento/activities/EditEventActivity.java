package com.example.momento.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.momento.R;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.models.Event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {

    private EditText titleEt, dateEt, locationEt, descEt, categoryEt;
    private ImageView imageView;
    private int eventId;
    private String originalImageUri;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri picked = result.getData().getData();
                            try {
                                selectedImageUri = copyImageToInternalStorage(picked, originalImageUri);
                                Glide.with(this)
                                        .load(selectedImageUri)
                                        .placeholder(R.drawable.ic_image_placeholder)
                                        .into(imageView);
                            } catch (IOException e) {
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        titleEt    = findViewById(R.id.titleEditText);
        dateEt     = findViewById(R.id.dateEditText);
        locationEt = findViewById(R.id.locationEditText);
        descEt     = findViewById(R.id.descriptionEditText);
        categoryEt = findViewById(R.id.categoryEditText);
        imageView  = findViewById(R.id.imageView);

        ImageButton btnClose = findViewById(R.id.btnCloseAdd);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        eventId = getIntent().getIntExtra("eventId", -1);
        DatabaseHelper db = new DatabaseHelper(this);
        Event event = db.getEventById(eventId);

        if (event == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titleEt.setText(event.getTitle());
        dateEt.setText(event.getDate());
        locationEt.setText(event.getLocation());
        descEt.setText(event.getDescription());
        categoryEt.setText(event.getCategory());

        originalImageUri = event.getImageUri();
        if (originalImageUri != null && !originalImageUri.isEmpty()) {
            selectedImageUri = Uri.parse(originalImageUri);
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageView);
        }

        dateEt.setOnClickListener(v -> showDatePicker());
        imageView.setOnClickListener(v -> openImageChooser());
    }

    public void saveEvent(android.view.View view) {
        String title = titleEt.getText().toString();
        String date  = dateEt.getText().toString();
        String loc   = locationEt.getText().toString();
        String desc  = descEt.getText().toString();
        String cat   = categoryEt.getText().toString();

        if (title.isEmpty() || date.isEmpty() || loc.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Event updated = new Event(
                eventId,
                title,
                date,
                loc,
                desc,
                cat,
                selectedImageUri != null ? selectedImageUri.toString() : null
        );

        DatabaseHelper db = new DatabaseHelper(this);
        boolean ok = db.updateEvent(updated);

        if (ok) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageChooser() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        imagePicker.launch(i);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(y, m, d);
                    if (chosen.before(Calendar.getInstance())) {
                        Toast.makeText(this, "Date can't be in the past!", Toast.LENGTH_SHORT).show();
                    } else {
                        dateEt.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                .format(chosen.getTime()));
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /**
     * @param sourceUri       picked image from the gallery
     * @param uriToOverwrite  existing image Uri to overwrite, or null to create a new file
     */
    private Uri copyImageToInternalStorage(Uri sourceUri, String uriToOverwrite) throws IOException {
        File destFile;

        if (uriToOverwrite != null && !uriToOverwrite.isEmpty()) {
            destFile = new File(Uri.parse(uriToOverwrite).getPath());
            if (destFile.exists()) destFile.delete();
        } else {
            destFile = new File(getFilesDir(), "event_" + System.currentTimeMillis() + ".jpg");
        }
        try (
                InputStream  in  = getContentResolver().openInputStream(sourceUri);
                OutputStream out = new FileOutputStream(destFile)
        ) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
        }
        return Uri.fromFile(destFile);
    }
}
