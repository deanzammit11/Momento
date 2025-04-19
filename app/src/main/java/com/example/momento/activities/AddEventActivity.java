package com.example.momento.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.momento.R;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.models.Category;
import com.example.momento.models.Event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private EditText titleEditText, dateEditText, locationEditText, descriptionEditText;
    private Spinner spinnerCategory;
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
        descriptionEditText = findViewById(R.id.descriptionEditText);
        spinnerCategory = findViewById(R.id.spinner_category);
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
        imageView.setOnClickListener(v -> openImageChooser());
    }

    private void loadCategoriesIntoSpinner() {
        categoryList = db.getAllCategories();
        List<String> categoryNames = new ArrayList<>();

        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    if (selectedDate.before(Calendar.getInstance())) {
                        Toast.makeText(this, "Date can't be in the past!", Toast.LENGTH_SHORT).show();
                    } else {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        dateEditText.setText(formatter.format(selectedDate.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    public void saveEvent(View view) {
        String title = titleEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";

        if (title.isEmpty() || date.isEmpty() || location.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;

        Event event = new Event(0, title, date, location, description, category, imageUriString);

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
