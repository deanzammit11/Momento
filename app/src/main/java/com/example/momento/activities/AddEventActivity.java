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

    private EditText titleEditText, dateEditText, locationEditText;
    private AutoCompleteTextView spinnerCategory;
    private ImageView imageView;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private List<Category> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        ImageButton btnCloseAdd = findViewById(R.id.btnCloseAdd);
        btnCloseAdd.setOnClickListener(v -> finish());

        titleEditText = findViewById(R.id.titleEditText);
        dateEditText = findViewById(R.id.dateEditText);
        locationEditText = findViewById(R.id.locationEditText);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imageView = findViewById(R.id.imageView);

        db = new DatabaseHelper(this);
        loadCategoriesIntoSpinner();

        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri pickedImageUri = result.getData().getData();
                        try {
                            selectedImageUri = copyImageToInternalStorage(pickedImageUri);
                            imageView.setImageURI(selectedImageUri);
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
        Button selectPhotoBtn = findViewById(R.id.buttonSelectPhoto);
        selectPhotoBtn.setOnClickListener(v -> openImageChooser());
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
        spinnerCategory.setDropDownBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.spinner_dropdown_background));
        spinnerCategory.post(() -> spinnerCategory.setDropDownWidth(spinnerCategory.getWidth()));
        spinnerCategory.setOnClickListener(v -> spinnerCategory.showDropDown());
        spinnerCategory.setText(names.get(0), false);
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String picked = categoryAdapter.getItem(position);
            spinnerCategory.setText(picked, false);
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDatePickerDialog() {
        CalendarConstraints constraints = new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").setCalendarConstraints(constraints).build();

        picker.addOnPositiveButtonClickListener(selection -> {
            String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(selection));
            dateEditText.setText(formatted);
        });

        picker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    public void saveEvent(View view) {
        String title = titleEditText.getText().toString().trim();
        String date  = dateEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();

        if (title.isEmpty()
                || date.isEmpty()
                || location.isEmpty()
                || category.isEmpty()
                || category.equals("Select Category")
        ) {
            Toast.makeText(this,
                    "Please fill all required fields and pick a category",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;
        Event event = new Event(0, title, date, location, category, imageUriString);
        db.insertEvent(event);
        setResult(RESULT_OK);
        finish();
    }

    private Uri copyImageToInternalStorage(Uri sourceUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(sourceUri);
        File file = new File(getFilesDir(), "event_" + System.currentTimeMillis() + ".jpg");
        OutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();

        return Uri.fromFile(file);
    }
}
