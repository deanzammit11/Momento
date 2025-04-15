package com.example.momento.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.momento.R;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.models.Event;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private EditText titleEditText, dateEditText, locationEditText, descriptionEditText, categoryEditText;
    private ImageView imageView;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        titleEditText = findViewById(R.id.titleEditText);
        dateEditText = findViewById(R.id.dateEditText);
        locationEditText = findViewById(R.id.locationEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        imageView = findViewById(R.id.imageView);

        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                });
        imageView.setOnClickListener(v -> openImageChooser());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
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
        String category = categoryEditText.getText().toString();

        if (title.isEmpty() || date.isEmpty() || location.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;

        Event event = new Event(0, title, date, location, description, category, imageUriString);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertEvent(event);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
