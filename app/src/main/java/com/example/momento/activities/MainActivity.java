package com.example.momento.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.momento.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge rendering so layouts can extend behind navigation bar
        EdgeToEdge.enable(this);
        // Inflate activity_main.xml which hosts the NavHostFragment and BottomNavigationView
        setContentView(R.layout.activity_main);
        // Retrieve the NavController from nav_host_fragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // Find the BottomNavigationView in the layout
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        // Connect the BottomNavigationView with NavController:
        NavigationUI.setupWithNavController(bottomNav, navController);
    }
}