package com.masliaiev.filmspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class RandomMovieActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationViewRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_movie);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        bottomNavigationViewRandom = findViewById(R.id.bottomNavigationViewRandom);
        Menu menu = bottomNavigationViewRandom.getMenu();
        menu.findItem(R.id.bottomRandom).setIcon(R.drawable.random_selection_white);
        bottomNavigationViewRandom.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottomHome:
                        Intent intentMainActivity = new Intent(RandomMovieActivity.this, MainActivity.class);
                        startActivity(intentMainActivity);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.bottomFavourites:
                        Intent intentFavourites = new Intent(RandomMovieActivity.this, FavouriteActivity.class);
                        startActivity(intentFavourites);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.bottomRandom:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                return false;
            }
        });
    }
}