package com.example.momento.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.momento.R;
import com.example.momento.activities.EditEventActivity;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.models.Event;

public class EventDetailsFragment extends Fragment {

    private ImageView eventImage;
    private TextView title, description, date, location, category;
    private int eventId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        ImageButton btnCloseDetails = view.findViewById(R.id.btnCloseDetails);
        btnCloseDetails.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .popBackStack(R.id.eventListFragment, false));

        eventImage = view.findViewById(R.id.eventDetailImage);
        title = view.findViewById(R.id.eventDetailTitle);
        description = view.findViewById(R.id.eventDetailDescription);
        date = view.findViewById(R.id.eventDetailDate);
        location = view.findViewById(R.id.eventDetailLocation);
        category = view.findViewById(R.id.eventDetailCategory);

        eventId = getArguments().getInt("eventId", -1);
        loadEvent();

        view.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditEventActivity.class);
            intent.putExtra("eventId", eventId);
            editLauncher.launch(intent);
        });

        view.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(getContext());
            boolean ok = db.deleteEvent(eventId);
            Toast.makeText(getContext(),
                    ok ? "Event deleted" : "Failed to delete event",
                    Toast.LENGTH_SHORT).show();
            if (ok) Navigation.findNavController(view).popBackStack();
        });

        view.findViewById(R.id.buttonCheckWeather).setOnClickListener(v -> {

        });

        return view;
    }

    private void loadEvent() {
        DatabaseHelper db = new DatabaseHelper(getContext());
        Event event = db.getEventById(eventId);
        if (event == null) return;

        title.setText(event.getTitle());
        description.setText(event.getDescription());
        date.setText(event.getDate());
        location.setText(event.getLocation());
        category.setText(event.getCategory());

        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            Glide.with(this)
                    .load(event.getImageUri())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            loadEvent();
        }
    });
}
