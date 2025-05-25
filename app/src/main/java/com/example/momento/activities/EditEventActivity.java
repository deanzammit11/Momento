package com.example.momento.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.example.momento.models.Category;
import com.example.momento.models.Event;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {
    // Edit Event Form Fields
    private EditText titleEt, dateEt, locationEt;
    private AutoCompleteTextView spinnerCategory; // Editable dropdown for categories
    private ImageView imageView; // Shows current or new event image

    // Data for the Event being edited
    private int eventId; // Event primary key
    private String originalImageUri; // URI string for the existing image
    private Uri selectedImageUri; // URI string for the new image

    // Data Sources
    private List<Category> categoryList; // Category List loaded from the database
    private ArrayAdapter<String> categoryAdapter; // Adapter for the dropdown
    private DatabaseHelper db; // Database Helper for CRUD operations

    // Handles image picker intent result where it copies picked image to internal storage overwriting old one if needed and loads it into the ImageView
    private final ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri picked = result.getData().getData();
            try {
                // Copy into app storage
                selectedImageUri = copyImageToInternalStorage(picked, originalImageUri);
                // Load Image with glide
                Glide.with(this).load(selectedImageUri).placeholder(R.drawable.ic_image_placeholder).into(imageView);
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

        // Link UI Elements
        titleEt = findViewById(R.id.titleEditText);
        dateEt = findViewById(R.id.dateEditText);
        locationEt = findViewById(R.id.locationEditText);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imageView = findViewById(R.id.imageView);

        // Close Button which simply finishes the activity
        ImageButton btnClose = findViewById(R.id.btnCloseAdd);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // Initialise database helper and populate the categories into the spinner
        db = new DatabaseHelper(this);
        loadCategoriesIntoSpinner();

        // Retrieve the event ID
        eventId = getIntent().getIntExtra("eventId", -1);
        Event event = db.getEventById(eventId);
        if (event == null) {
            // If no events with that ID exist show error message
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pre-fill fields with existing event details
        titleEt.setText(event.getTitle());
        dateEt.setText(event.getDate());
        locationEt.setText(event.getLocation());

        // Load existing image URI if any into ImageView
        originalImageUri = event.getImageUri();
        if (originalImageUri != null && !originalImageUri.isEmpty()) {
            selectedImageUri = Uri.parse(originalImageUri);
            Glide.with(this).load(selectedImageUri).placeholder(R.drawable.ic_image_placeholder).into(imageView);
        }
        // Pre0fill category with event category
        spinnerCategory.setText(event.getCategory(), false);
        // Show date picker when date fields is clicked
        dateEt.setOnClickListener(v -> showDatePicker());
        // Open image picker when selecting a new image
        Button selectPhotoBtn = findViewById(R.id.buttonSelectPhoto);
        selectPhotoBtn.setOnClickListener(v -> openImageChooser());
    }

    // Populates spinnerCategory with names from Database
    private void loadCategoriesIntoSpinner() {
        categoryList = db.getAllCategories(); // Fetch all categories from database
        List<String> names = new ArrayList<>();
        names.add("Select Category"); // Placeholder for dropdown
        for (Category c : categoryList) {
            names.add(c.getName()); // Add each category
        }

        // Use customer dropdown layout for spinner items
        categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, names);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerCategory.setAdapter(categoryAdapter);
        // Update text when selecting a new category
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String picked = categoryAdapter.getItem(position);
            spinnerCategory.setText(picked, false);
        });
        // Initialize text to placeholder without filtering
        spinnerCategory.setText(names.get(0), false);
        // Ensure dropdown width matches spinner width
        spinnerCategory.post(() -> spinnerCategory.setDropDownWidth(spinnerCategory.getWidth()));
    }

    // Called when save button is clicked and it validates input, constructs the Event object and adds it to the database
    public void saveEvent(android.view.View view) {
        String title = titleEt.getText().toString().trim();
        String date  = dateEt.getText().toString().trim();
        String loc   = locationEt.getText().toString().trim();
        String cat   = spinnerCategory.getText().toString().trim();

        // Checks that no fields are empty and that a valid category is chosen
        if (title.isEmpty() || date.isEmpty() || loc.isEmpty() || cat.isEmpty() || cat.equals("Select Category")) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Updated event is built and if new image is chosen the selectedImageURI remains as it was
        Event updated = new Event(eventId, title, date, loc, cat, selectedImageUri != null ? selectedImageUri.toString() : null);

        // Update event in database and error message if it fails
        boolean ok = db.updateEvent(updated);
        if (ok) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Launches the image picker
    private void openImageChooser() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        imagePicker.launch(i);
    }

    // Sets up and shows a MaterialDatePicker which only allows picking today or future dates
    private void showDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setCalendarConstraints(constraints).build();

        picker.addOnPositiveButtonClickListener(selection -> {
            String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
            dateEt.setText(formatted);
        });

        picker.show(getSupportFragmentManager(), "EDIT_DATE_PICKER");
    }

    // Copies the user-selected image into the app's internal storage and overwrites the existing image if needed
    private Uri copyImageToInternalStorage(Uri sourceUri, String uriToOverwrite) throws IOException {
        File destFile;
        if (uriToOverwrite != null && !uriToOverwrite.isEmpty()) {
            // Overwrite existing file
            destFile = new File(Uri.parse(uriToOverwrite).getPath());
            if (destFile.exists()) destFile.delete();
        } else {
            // Create a new file name
            destFile = new File(getFilesDir(), "event_" + System.currentTimeMillis() + ".jpg");
        }
        // Copy stream with automatic resource management
        try (InputStream in = getContentResolver().openInputStream(sourceUri); OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
        }
        return Uri.fromFile(destFile);
    }
}
