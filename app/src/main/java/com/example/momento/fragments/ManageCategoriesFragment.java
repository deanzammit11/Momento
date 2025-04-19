package com.example.momento.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.momento.R;
import com.example.momento.adapters.CategoryAdapter;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ManageCategoriesFragment extends Fragment {

    private DatabaseHelper db;
    private CategoryAdapter adapter;
    private List<Category> categories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_categories, container, false);

        db = new DatabaseHelper(getContext());
        categories = db.getAllCategories();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                showCategoryDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                db.deleteCategory(category.getId());
                refreshCategories();
            }
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_category);
        fab.setOnClickListener(v -> showCategoryDialog(null));

        return view;
    }

    private void showCategoryDialog(@Nullable Category categoryToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(categoryToEdit == null ? "Add Category" : "Edit Category");

        final EditText input = new EditText(getContext());
        if (categoryToEdit != null) input.setText(categoryToEdit.getName());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            hideKeyboard();
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryToEdit == null) {
                db.insertCategory(name);
            } else {
                db.updateCategory(categoryToEdit.getId(), name);
            }

            refreshCategories();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            hideKeyboard();
            dialog.dismiss();
        });

        builder.show();
    }

    private void refreshCategories() {
        categories.clear();
        categories.addAll(db.getAllCategories());
        adapter.notifyDataSetChanged();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        View currentFocus = getActivity() != null ? getActivity().getCurrentFocus() : null;
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}
