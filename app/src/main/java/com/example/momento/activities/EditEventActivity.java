package com.example.momento.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

    private EditText titleEt, dateEt, locationEt;
    private AutoCompleteTextView spinnerCategory;
    private ImageView imageView;
    private int eventId;
    private String originalImageUri;
    private Uri selectedImageUri;
    private List<Category> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private DatabaseHelper db;

    private final ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri picked = result.getData().getData();
            try {
                selectedImageUri = copyImageToInternalStorage(picked, originalImageUri);
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

        titleEt = findViewById(R.id.titleEditText);
        dateEt = findViewById(R.id.dateEditText);
        locationEt = findViewById(R.id.locationEditText);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imageView = findViewById(R.id.imageView);

        ImageButton btnClose = findViewById(R.id.btnCloseAdd);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        db = new DatabaseHelper(this);
        loadCategoriesIntoSpinner();

        eventId = getIntent().getIntExtra("eventId", -1);
        Event event = db.getEventById(eventId);
        if (event == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titleEt.setText(event.getTitle());
        dateEt.setText(event.getDate());
        locationEt.setText(event.getLocation());
        originalImageUri = event.getImageUri();
        if (originalImageUri != null && !originalImageUri.isEmpty()) {
            selectedImageUri = Uri.parse(originalImageUri);
            Glide.with(this).load(selectedImageUri).placeholder(R.drawable.ic_image_placeholder).into(imageView);
        }
        spinnerCategory.setText(event.getCategory(), false);
        dateEt.setOnClickListener(v -> showDatePicker());
        imageView.setOnClickListener(v -> openImageChooser());
    }

    private void loadCategoriesIntoSpinner() {
        categoryList = db.getAllCategories();
        List<String> names = new ArrayList<>();
        names.add("Select Category");
        for (Category c : categoryList) {
            names.add(c.getName());
        }

        categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, names);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String picked = categoryAdapter.getItem(position);
            spinnerCategory.setText(picked, false);
        });
        spinnerCategory.setText(names.get(0), false);
        spinnerCategory.post(() ->
                spinnerCategory.setDropDownWidth(spinnerCategory.getWidth())
        );
    }

    public void saveEvent(android.view.View view) {
        String title = titleEt.getText().toString().trim();
        String date  = dateEt.getText().toString().trim();
        String loc   = locationEt.getText().toString().trim();
        String cat   = spinnerCategory.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || loc.isEmpty() || cat.isEmpty() || cat.equals("Select Category")) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Event updated = new Event(eventId, title, date, loc, cat, selectedImageUri != null ? selectedImageUri.toString() : null);

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
        CalendarConstraints constraints = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setCalendarConstraints(constraints).build();

        picker.addOnPositiveButtonClickListener(selection -> {
            String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
            dateEt.setText(formatted);
        });

        picker.show(getSupportFragmentManager(), "EDIT_DATE_PICKER");
    }

    private Uri copyImageToInternalStorage(Uri sourceUri, String uriToOverwrite) throws IOException {
        File destFile;
        if (uriToOverwrite != null && !uriToOverwrite.isEmpty()) {
            destFile = new File(Uri.parse(uriToOverwrite).getPath());
            if (destFile.exists()) destFile.delete();
        } else {
            destFile = new File(getFilesDir(), "event_" + System.currentTimeMillis() + ".jpg");
        }
        try (InputStream in = getContentResolver().openInputStream(sourceUri); OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
        }
        return Uri.fromFile(destFile);
    }
}
