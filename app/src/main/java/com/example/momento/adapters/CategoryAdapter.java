package com.example.momento.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.momento.R;
import com.example.momento.models.Category;

import java.util.List;

// RecyclerView.Adapter displaying a list of category items with an option to rename or delete each one
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    // Listener interface for edit and delete action
    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    // List of category objects to display
    private final List<Category> categoryList;
    // Callback to host activity/fragment for handling user actions
    private final OnCategoryActionListener listener;

    // Adapter with initial list of categories to show and handling of edit and delete actions
    public CategoryAdapter(List<Category> categoryList, OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item layout for each ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Bind category data at position to ViewHolder
        Category category = categoryList.get(position);
        holder.nameText.setText(category.getName());

        // When rename button is tapped onEdit callback is invoked
        holder.renameBtn.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(category);
        });

        // When delete button is tapped onDelete callback is invoked
        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(category);
        });
    }

    // Returns number of categories to display
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // ViewHolder for category items
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameText; // Displays category name
        Button renameBtn, deleteBtn; // Button to trigger renaming and deletion

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views inside the item_category layout
            nameText = itemView.findViewById(R.id.text_view_category_name);
            renameBtn  = itemView.findViewById(R.id.btn_rename_category);
            deleteBtn= itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
