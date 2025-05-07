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
                if (db.isCategoryUsed(category.getId())) {
                    Toast.makeText(getContext(), "Cannot Edit: Category is used by existing events", Toast.LENGTH_SHORT).show();
                } else {
                    showCategoryDialog(category);
                }
            }

            @Override
            public void onDelete(Category category) {
                if (db.isCategoryUsed(category.getId())) {
                    Toast.makeText(getContext(), "Cannot Delete: Category is used by existing events", Toast.LENGTH_SHORT).show();
                } else {
                    db.deleteCategory(category.getId());
                    refreshCategories();
                }
            }
        });

        recyclerView.setAdapter(adapter);

        Button addCategory = view.findViewById(R.id.buttonAddCategory);
        addCategory.setOnClickListener(v -> showCategoryDialog(null));

        return view;
    }

    private void showCategoryDialog(@Nullable Category categoryToEdit) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_category, null, false);

        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText input   = dialogView.findViewById(R.id.dialogInput);
        Button btnCancel = dialogView.findViewById(R.id.buttonCancel);
        Button btnSave   = dialogView.findViewById(R.id.buttonSave);

        boolean isEdit = (categoryToEdit != null);
        tvTitle.setText(isEdit ? "Rename Category" : "Add Category");
        if (isEdit) input.setText(categoryToEdit.getName());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext()).setView(dialogView).create();

        dialog.setOnShowListener(d -> {
            int tint = ContextCompat.getColor(requireContext(), R.color.dark_blue);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(tint);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(tint);
        });

        dialog.show();

        btnCancel.setOnClickListener(v -> {
            hideKeyboard();
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            hideKeyboard();
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isEdit) db.updateCategory(categoryToEdit.getId(), name);
            else db.insertCategory(name);
            refreshCategories();
            dialog.dismiss();
        });
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
