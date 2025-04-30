package com.example.momento.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.momento.R;
import com.example.momento.activities.EditEventActivity;
import com.example.momento.database.DatabaseHelper;
import com.example.momento.models.Event;

public class EventDetailsFragment extends Fragment {

    private ImageView eventImage;
    private TextView title, date, location, category;
    private int eventId;
    private TextView weatherText;
    private ImageView weatherIcon;
    private LinearLayout weatherLayout;
    private Button buttonCheckWeather;
    private String apiKey = "0b1ccab07a6bb42adf87a9b3d949014d";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        ImageButton btnCloseDetails = view.findViewById(R.id.btnCloseDetails);
        btnCloseDetails.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .popBackStack(R.id.eventListFragment, false));

        eventImage = view.findViewById(R.id.eventDetailImage);
        title = view.findViewById(R.id.eventDetailTitle);
        date = view.findViewById(R.id.eventDetailDate);
        location = view.findViewById(R.id.eventDetailLocation);
        category = view.findViewById(R.id.eventDetailCategory);

        eventId = getArguments().getInt("eventId", -1);
        loadEvent();

        weatherText = view.findViewById(R.id.weatherText);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        weatherLayout = view.findViewById(R.id.weatherLayout);
        buttonCheckWeather = view.findViewById(R.id.buttonCheckWeather);

        view.findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditEventActivity.class);
            intent.putExtra("eventId", eventId);
            editLauncher.launch(intent);
        });

        view.findViewById(R.id.buttonDelete).setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(getContext());
            boolean ok = db.deleteEvent(eventId);
            Toast.makeText(getContext(),
                    ok ? "Event deleted" : "Failed to delete event",
                    Toast.LENGTH_SHORT).show();
            if (ok) Navigation.findNavController(view).popBackStack();
        });

        buttonCheckWeather.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(getContext());
            Event event = db.getEventById(eventId);
            if (event != null) {
                fetchWeather(event.getLocation());
            }
        });

        return view;
    }

    private void loadEvent() {
        DatabaseHelper db = new DatabaseHelper(getContext());
        Event event = db.getEventById(eventId);
        if (event == null) return;

        if (event.getImageUri() != null && !event.getImageUri().isEmpty()) {
            Glide.with(this)
                    .load(event.getImageUri())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        title.setText(event.getTitle());

        try {
            java.text.SimpleDateFormat dbFmt =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date d = dbFmt.parse(event.getDate());
            java.text.SimpleDateFormat outFmt =
                    new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            date.setText(outFmt.format(d));
        } catch (Exception e) {
            date.setText(event.getDate());
        }
        location.setText(event.getLocation());
        category.setText(event.getCategory());
    }

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            loadEvent();
        }
    });

    private void fetchWeather(String location) {
        if (location == null || location.trim().length() < 3) {
            Toast.makeText(getContext(), "Please enter a valid city or country.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userInput = location.trim();

        new Thread(() -> {
            try {
                String geoUrl = "https://api.openweathermap.org/geo/1.0/direct?q=" + java.net.URLEncoder.encode(userInput, "UTF-8") + "&limit=1&appid=" + apiKey;

                java.net.URL url = new java.net.URL(geoUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                java.io.InputStream in = conn.getInputStream();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in));
                StringBuilder geoResult = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) geoResult.append(line);

                org.json.JSONArray geoArray = new org.json.JSONArray(geoResult.toString());

                if (geoArray.length() == 0) {
                    throw new Exception("No matching place found");
                }

                org.json.JSONObject place = geoArray.getJSONObject(0);
                String resolvedName = place.optString("name", "").trim();
                String country = place.optString("country", "").trim();
                double lat = place.getDouble("lat");
                double lon = place.getDouble("lon");

                String lowerUserInput = userInput.toLowerCase();
                String lowerResolved = resolvedName.toLowerCase();

                if (!lowerResolved.equals(lowerUserInput) && !lowerResolved.contains(lowerUserInput) && !lowerUserInput.contains(lowerResolved)) {
                    throw new Exception("Resolved location '" + resolvedName + "' does not match input '" + userInput + "'");
                }

                String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";

                java.net.URL weatherApi = new java.net.URL(weatherUrl);
                java.net.HttpURLConnection weatherConn = (java.net.HttpURLConnection) weatherApi.openConnection();
                weatherConn.setRequestMethod("GET");

                java.io.InputStream weatherIn = weatherConn.getInputStream();
                java.io.BufferedReader weatherReader = new java.io.BufferedReader(new java.io.InputStreamReader(weatherIn));
                StringBuilder weatherResult = new StringBuilder();
                while ((line = weatherReader.readLine()) != null) weatherResult.append(line);

                org.json.JSONObject weatherObj = new org.json.JSONObject(weatherResult.toString());
                String description = weatherObj.getJSONArray("weather").getJSONObject(0).getString("description");
                String icon = weatherObj.getJSONArray("weather").getJSONObject(0).getString("icon");
                double temp = weatherObj.getJSONObject("main").getDouble("temp");

                String weatherInfo = resolvedName + ", " + country + " • " + description + " • " + temp + "°C";
                String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";

                requireActivity().runOnUiThread(() -> {
                    weatherLayout.setVisibility(View.VISIBLE);
                    weatherText.setText(weatherInfo);
                    Glide.with(requireContext()).load(iconUrl).placeholder(R.drawable.ic_image_placeholder).error(R.drawable.ic_image_placeholder).into(weatherIcon);
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Could not fetch weather for: " + location + ". Try a valid city or country name.",
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
