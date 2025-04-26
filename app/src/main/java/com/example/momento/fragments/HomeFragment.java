package com.example.momento.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.momento.R;
import com.example.momento.activities.AddEventActivity;
import com.example.momento.database.DatabaseHelper;

public class HomeFragment extends Fragment {

    private TextView textViewEventCount;
    private Button buttonAddEvent;

    private final ActivityResultLauncher<Intent> addEventActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateEventCount();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textViewEventCount = view.findViewById(R.id.textViewEventCount);
        buttonAddEvent = view.findViewById(R.id.buttonAddEvent);

        buttonAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            addEventActivityResultLauncher.launch(intent);
        });

        updateEventCount();

        return view;
    }

    private void updateEventCount() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        int count = dbHelper.getAllEvents().size();
        textViewEventCount.setText(count + (count == 1 ? " Event Added" : " Events Added"));
    }
}
