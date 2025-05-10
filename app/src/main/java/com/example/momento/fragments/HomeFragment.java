package com.example.momento.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.momento.R;
import com.example.momento.activities.AddEventActivity;
import com.example.momento.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;

// HomeFragment showing the total number of events and an add event button
public class HomeFragment extends Fragment {
    private MaterialButton buttonEventCount; // Button displaying the current event count
    private MaterialButton buttonAddEvent; // Button which launches the AddEventActivity when clicked to create a new event

    // AddEventActivity launcher which returns RESULT_OK and refreshes the displayed event count
    private final ActivityResultLauncher<Intent> addEventActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateEventCount();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflates fragment_home to create the UI
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Binds UI elements
        buttonEventCount = view.findViewById(R.id.buttonEventCount);
        buttonAddEvent = view.findViewById(R.id.buttonAddEvent);

        // When the Add Event button is tapped the AddEventActivity is launched
        buttonAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            addEventActivityResultLauncher.launch(intent);
        });

        // Initializes the event count display
        updateEventCount();

        return view;
    }

    // Queries the database for the total number of events and updates the event count
    private void updateEventCount() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        int count = dbHelper.getAllEvents().size();
        buttonEventCount.setText(count + (count == 1 ? " Event" : " Events"));
    }
}
