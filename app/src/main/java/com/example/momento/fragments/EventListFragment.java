package com.example.momento.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.momento.R;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.adapters.EventAdapter;
import com.example.momento.models.Category;
import com.example.momento.models.Event;

import java.util.List;
import java.util.ArrayList;

// Fragment displaying a scrollable list of events with support for filtering new events by category
public class EventListFragment extends Fragment {

    private RecyclerView recyclerView; // RecyclerView showing the list of events
    private EventAdapter eventAdapter; // Adapter backing the RecyclerView
    private List<Event> eventList; // List of events being displayed
    private AutoCompleteTextView spinnerFilterCategory; // Dropdown for filtering events bt category
    private DatabaseHelper db; // Database Helper for retrieving events and categories
    private List<Category> categoryList; // List of categories in the database

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for fragment
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // Initialize database helper
        db = new DatabaseHelper(getContext());

        // Bind category filter dropdown
        spinnerFilterCategory = (AutoCompleteTextView) view.findViewById(R.id.spinner_filter_category);
        // Bind and configure RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the event list and adapter to navigate to the details on tap
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(getContext(), eventList, event -> {
            // When an event is tapped navigate to the EventDetailsFragment using the eventId
            Bundle bundle = new Bundle();
            bundle.putInt("eventId", event.getId());
            Navigation.findNavController(requireView()).navigate(R.id.eventDetailsFragment, bundle);
        });
        recyclerView.setAdapter(eventAdapter);

        // Populate the category filter
        setupCategoryFilter();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re‐load the spinner & event list whenever you return here
        setupCategoryFilter();
    }

    // Loads categories in dropdown
    private void setupCategoryFilter() {
        // Loads all categories from the database
        categoryList = db.getAllCategories();

        // Builds a list of category names starting with the 'Filter by Category' placeholder
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.filter_by_category));
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }

        // Create and set the ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_dropdown_item, categoryNames);
        spinnerFilterCategory.setAdapter(adapter);

        // When a category is selected filter by category
        spinnerFilterCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = adapter.getItem(position);
            filterEventList(selectedCategory);
        });

        // Initialize the dropdown text with the placeholder
        spinnerFilterCategory.setText(getString(R.string.filter_by_category), false);

        // Style the dropdown background and match its width to the view
        spinnerFilterCategory.setDropDownBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.spinner_dropdown_background)
        );
        spinnerFilterCategory.post(() -> spinnerFilterCategory.setDropDownWidth(spinnerFilterCategory.getWidth()));

        // Perform the initial unfiltered load
        filterEventList(categoryNames.get(0));
    }

    // Filter Event list by the category
    private void filterEventList(String categoryName) {
        eventList.clear();

        String placeholder = getString(R.string.filter_by_category);
        if (categoryName.equals(placeholder)) {
            // If the placeholder is selected add all events from database
            eventList.addAll(db.getAllEvents());
        } else {
            for (Event e : db.getAllEvents()) {
                if (e.getCategory().equals(categoryName)) {
                    // Add only events which match the selected category
                    eventList.add(e);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
    }
}
