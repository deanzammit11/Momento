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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private final List<Category> categoryList;
    private final OnCategoryActionListener listener;

    public CategoryAdapter(List<Category> categoryList, OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.nameText.setText(category.getName());

        holder.renameBtn.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(category);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(category);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        Button renameBtn, deleteBtn;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_view_category_name);
            renameBtn  = itemView.findViewById(R.id.btn_rename_category);
            deleteBtn= itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
