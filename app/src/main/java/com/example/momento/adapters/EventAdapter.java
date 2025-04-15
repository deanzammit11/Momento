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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView, dateTextView, locationTextView;
        public ImageView eventImageView;

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
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(event.getDate());
        holder.locationTextView.setText(event.getLocation());

        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(event.getImageUri()))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.eventImageView);
        } else {
            holder.eventImageView.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
