package com.example.momento.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import androidx.core.content.ContextCompat;

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

public class AddEventActivity extends AppCompatActivity {

    // Add Event Form Fields
    private EditText titleEditText, dateEditText, locationEditText;
    private AutoCompleteTextView spinnerCategory;
    private ImageView imageView;
    private Uri selectedImageUri; // Image URI

    // Image Picker Intent Launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Data Sources
    private List<Category> categoryList; // Category List loaded from the database
    private ArrayAdapter<String> categoryAdapter; // Adapter for the dropdown
    private DatabaseHelper db; // Database Helper for CRUD operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Close Button which simply finishes the activity
        ImageButton btnCloseAdd = findViewById(R.id.btnCloseAdd);
        btnCloseAdd.setOnClickListener(v -> finish());

        // Link UI Elements
        titleEditText = findViewById(R.id.titleEditText);
        dateEditText = findViewById(R.id.dateEditText);
        locationEditText = findViewById(R.id.locationEditText);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imageView = findViewById(R.id.imageView);

        // Initialise database helper and populate the categories into the spinner
        db = new DatabaseHelper(this);
        loadCategoriesIntoSpinner();

        // When date fields is clicked MaterialDatePicker with only future dates is shown
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Prepare image picker callback to handle user image
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    // If user picked an image
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri pickedImageUri = result.getData().getData();
                        try {
                            // Copy image into app's internal storage
                            selectedImageUri = copyImageToInternalStorage(pickedImageUri);
                            // Display image in image view
                            imageView.setImageURI(selectedImageUri);
                        } catch (IOException e) {
                            // Notify user if there is a failure in loading the image
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
        // Button for launching image picker
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
        // Apply custom background drawable for dropdown
        spinnerCategory.setDropDownBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.spinner_dropdown_background));
        // Ensure dropdown width matches spinner width
        spinnerCategory.post(() -> spinnerCategory.setDropDownWidth(spinnerCategory.getWidth()));
        // Show dropdown when text fields is selected
        spinnerCategory.setOnClickListener(v -> spinnerCategory.showDropDown());
        // Initialize text to placeholder without filtering
        spinnerCategory.setText(names.get(0), false);
        // Update text when a category is selected
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String picked = categoryAdapter.getItem(position);
            spinnerCategory.setText(picked, false);
        });
    }

    // Launches the image picker
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // Sets up and shows a MaterialDatePicker which only allows picking today or future dates
    private void showDatePickerDialog() {
        CalendarConstraints constraints = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setCalendarConstraints(constraints).build();

        // When user confirms selection apply date format and set it into EditText
        picker.addOnPositiveButtonClickListener(selection -> {
            String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
            dateEditText.setText(formatted);
        });

        picker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    // Called when save button is clicked and it validates input, constructs the Event object and add it to the database
    public void saveEvent(View view) {
        String title = titleEditText.getText().toString().trim();
        String date  = dateEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();

        // Checks that no fields are empty and that a valid category is chosen
        if (title.isEmpty() || date.isEmpty() || location.isEmpty() || category.isEmpty() || category.equals("Select Category")) {
            Toast.makeText(this, "Please fill all required fields and pick a category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sets image URI as a string or null if no image is selected
        String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;
        // Creates Event model, id will be auto generated by the database
        Event event = new Event(0, title, date, location, category, imageUriString);
        // Insert event with RESULT_OK so list can be refreshed with new event
        db.insertEvent(event);
        setResult(RESULT_OK);
        finish();
    }

    // Copies the user-selected image into the app's internal storage
    private Uri copyImageToInternalStorage(Uri sourceUri) throws IOException {
        // Open streams
        InputStream inputStream = getContentResolver().openInputStream(sourceUri);
        File file = new File(getFilesDir(), "event_" + System.currentTimeMillis() + ".jpg");
        OutputStream outputStream = new FileOutputStream(file);

        // Copy bytes in chunks of 1KB each
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        // Close streams
        inputStream.close();
        outputStream.close();

        return Uri.fromFile(file);
    }
}
