package com.example.momento.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.momento.R;
import com.example.momento.activities.AddEventActivity;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.adapters.EventAdapter;
import com.example.momento.models.Category;
import com.example.momento.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.ArrayList;

public class EventListFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private FloatingActionButton fabAddEvent;
    private List<Event> eventList;
    private Spinner spinnerFilterCategory;
    private DatabaseHelper db;
    private List<Category> categoryList;
    private ArrayAdapter<String> categoryAdapter;

    private ActivityResultLauncher<Intent> addEventActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refreshEventList();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        db = new DatabaseHelper(getContext());

        spinnerFilterCategory = view.findViewById(R.id.spinner_filter_category);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(getContext(), eventList, event -> {
            Bundle bundle = new Bundle();
            bundle.putInt("eventId", event.getId());
            Navigation.findNavController(requireView()).navigate(R.id.eventDetailsFragment, bundle);
        });
        recyclerView.setAdapter(eventAdapter);

        fabAddEvent = view.findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            addEventActivityResultLauncher.launch(intent);
        });

        setupCategoryFilter();

        return view;
    }

    private void setupCategoryFilter() {
        categoryList = db.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");

        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }

        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(categoryAdapter);

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterEventList(categoryAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void filterEventList(String categoryName) {
        eventList.clear();

        if (categoryName.equals("All Categories")) {
            eventList.addAll(db.getAllEvents());
        } else {
            for (Event e : db.getAllEvents()) {
                if (e.getCategory().equals(categoryName)) {
                    eventList.add(e);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    public void refreshEventList() {
        if (spinnerFilterCategory != null && spinnerFilterCategory.getSelectedItem() != null) {
            filterEventList(spinnerFilterCategory.getSelectedItem().toString());
        } else {
            eventList.clear();
            eventList.addAll(db.getAllEvents());
            eventAdapter.notifyDataSetChanged();
        }
    }
}
