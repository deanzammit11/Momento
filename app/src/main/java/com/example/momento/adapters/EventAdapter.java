package com.example.momento.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.momento.R;
import com.example.momento.models.Event;
import java.util.List;

// RecyclerView.Adapter displaying a list of events
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context; // Used to inflate views and initialize Glide
    private List<Event> eventList; // List of events to display
    private OnItemClickListener listener; // Callback for item click events

    // Constructor
    public EventAdapter(Context context, List<Event> eventList, OnItemClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    // Viewholder caching references to the views for each event item
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView, dateTextView, locationTextView; // Displays title, date and location
        public ImageView eventImageView; // Displays event image or placeholder image

        public EventViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitle);
            dateTextView = itemView.findViewById(R.id.eventDate);
            locationTextView = itemView.findViewById(R.id.eventLocation);
            eventImageView = itemView.findViewById(R.id.eventImageView);
        }
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate layout for event
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        // Bind data from event object at this position
        Event event = eventList.get(position);

        // Populate text fields with title, date and location
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(event.getDate());
        holder.locationTextView.setText(event.getLocation());

        // If there is a URI load the event image else use the placeholder image
        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            // Fetch and display image using Glide
            Glide.with(context).load(Uri.parse(event.getImageUri())).placeholder(R.drawable.ic_image_placeholder).error(R.drawable.ic_image_placeholder).into(holder.eventImageView);
        } else {
            // Display placeholder image
            holder.eventImageView.setImageResource(R.drawable.ic_image_placeholder);
        }
        // Handle event click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(event));
    }

    // Returns total number of events to display
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // Callback interface for clicking events
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }
}
