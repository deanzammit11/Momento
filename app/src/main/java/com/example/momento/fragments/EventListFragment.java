package com.example.momento.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.momento.R;
import com.example.momento.activities.AddEventActivity;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.adapters.EventAdapter;
import com.example.momento.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import java.util.ArrayList;

public class EventListFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private FloatingActionButton fabAddEvent;
    private List<Event> eventList;

    private ActivityResultLauncher<Intent> addEventActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refreshEventList();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(getContext(), eventList);
        recyclerView.setAdapter(eventAdapter);

        fabAddEvent = view.findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            addEventActivityResultLauncher.launch(intent);
        });

        refreshEventList();
        return view;
    }

    public void refreshEventList() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        eventList.clear();
        eventList.addAll(dbHelper.getAllEvents());
        eventAdapter.notifyDataSetChanged();
    }
}
