package com.example.momento.fragments;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.momento.R;
import com.example.momento.adapters.CategoryAdapter;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.models.Category;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

// Fragment for viewing and managing event categories displaying existing categories in a recycler view with options to rename and delete
public class ManageCategoriesFragment extends Fragment {

    private DatabaseHelper db; // Database helper for categories CRUD operations
    private CategoryAdapter adapter; // Adapter for displaying categories in a RecyclerView
    private List<Category> categories; // List of category models

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout containing the RecyclerView and add button
        View view = inflater.inflate(R.layout.fragment_manage_categories, container, false);

        // Initialize DatabaseHelper and load all the categories from the database
        db = new DatabaseHelper(getContext());
        categories = db.getAllCategories();

        // Set up the RecyclerView with a LinearLayoutManager
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create adapter with callbacks for edit and delete actions
        adapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                // Do not allow editing if category is used by another event
                if (db.isCategoryUsed(category.getId())) {
                    Toast.makeText(getContext(), "Cannot Edit: Category is used by existing events", Toast.LENGTH_SHORT).show();
                } else {
                    // Show dialog prefilled with category to edit
                    showCategoryDialog(category);
                }
            }

            @Override
            public void onDelete(Category category) {
                // Do not allow deletion if category is used by another event
                if (db.isCategoryUsed(category.getId())) {
                    Toast.makeText(getContext(), "Cannot Delete: Category is used by existing events", Toast.LENGTH_SHORT).show();
                } else {
                    // Delete from database and refresh the list
                    db.deleteCategory(category.getId());
                    refreshCategories();
                }
            }
        });

        recyclerView.setAdapter(adapter);

        // Bind the Add Category button to open blank dialog to add a category
        Button addCategory = view.findViewById(R.id.buttonAddCategory);
        addCategory.setOnClickListener(v -> showCategoryDialog(null));

        return view;
    }

    private void showCategoryDialog(@Nullable Category categoryToEdit) {
        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_category, null, false);

        // Bind views inside the dialog
        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText input   = dialogView.findViewById(R.id.dialogInput);
        Button btnCancel = dialogView.findViewById(R.id.buttonCancel);
        Button btnSave   = dialogView.findViewById(R.id.buttonSave);

        // Check if an edit or addition is underway
        boolean isEdit = (categoryToEdit != null);
        tvTitle.setText(isEdit ? "Rename Category" : "Add Category");
        // Prefill existing name
        if (isEdit) input.setText(categoryToEdit.getName());

        // Build AlertDialog with custom view
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext()).setView(dialogView).create();

        // Tint action buttons after dialog is shown
        dialog.setOnShowListener(d -> {
            int tint = ContextCompat.getColor(requireContext(), R.color.dark_blue);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(tint);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(tint);
        });
        dialog.show();

        // Cancel button hides keyboard and dismisses dialog
        btnCancel.setOnClickListener(v -> {
            hideKeyboard();
            dialog.dismiss();
        });

        // Save button validating input then inserts or updates category
        btnSave.setOnClickListener(v -> {
            hideKeyboard();
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isEdit) db.updateCategory(categoryToEdit.getId(), name);
            else db.insertCategory(name);
            // Refresh RecyclerView and close dialog
            refreshCategories();
            dialog.dismiss();
        });
    }

    // Refreshes categories in database and notifies the adapter to refresh the RecyclerView
    private void refreshCategories() {
        categories.clear();
        categories.addAll(db.getAllCategories());
        adapter.notifyDataSetChanged();
    }

    // Hide keyboard if it is shown on screen
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        View currentFocus = getActivity() != null ? getActivity().getCurrentFocus() : null;
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}
