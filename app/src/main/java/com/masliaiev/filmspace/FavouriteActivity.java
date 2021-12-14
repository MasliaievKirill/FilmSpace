package com.masliaiev.filmspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.masliaiev.filmspace.adapters.MovieAdapter;
import com.masliaiev.filmspace.data.FavouriteMovie;
import com.masliaiev.filmspace.data.MainViewModel;
import com.masliaiev.filmspace.data.Movie;

import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private MovieAdapter adapter;
    private TextView textViewFavouriteWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.favouriteActivity);
        textViewFavouriteWarning = findViewById(R.id.textViewFavouriteWarning);
        bottomNavigationView.getMenu().findItem(R.id.favouriteActivity).setIcon(R.drawable.favourite_white);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mainActivity:
                        Intent intentMainActivity = new Intent(FavouriteActivity.this, MainActivity.class);
                        startActivity(intentMainActivity);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.favouriteActivity:
                        return true;
                    case R.id.randomActivity:
                        Intent intentRandom = new Intent(FavouriteActivity.this, RandomActivity.class);
                        startActivity(intentRandom);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.searchActivity:
                        Intent intentSearch = new Intent(FavouriteActivity.this, SearchActivity.class);
                        startActivity(intentSearch);
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        RecyclerView recyclerViewFavouriteMovies = findViewById(R.id.recyclerViewFavouriteMovies);
        recyclerViewFavouriteMovies.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        adapter = new MovieAdapter();
        adapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                if (position < 0) {
                    Toast.makeText(FavouriteActivity.this, "Error", Toast.LENGTH_SHORT).show();
                } else {
                    Movie movie = adapter.getMovies().get(position);
                    Intent intent = new Intent(FavouriteActivity.this, DetailActivity.class);
                    intent.putExtra("favourite", "favourite");
                    intent.putExtra("id", movie.getId());
                    startActivity(intent);
                }
            }
        });
        recyclerViewFavouriteMovies.setAdapter(adapter);
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        adapter.clear();
        LiveData<List<FavouriteMovie>> favouriteMovies = viewModel.getFavouriteMovies();
        favouriteMovies.observe(this, new Observer<List<FavouriteMovie>>() {
            @Override
            public void onChanged(List<FavouriteMovie> favouriteMovies) {
                if (favouriteMovies != null) {
                    List<Movie> movies = new ArrayList<>(favouriteMovies);
                    if (movies.size() > 0) {
                        textViewFavouriteWarning.setVisibility(View.INVISIBLE);
                    } else {
                        textViewFavouriteWarning.setVisibility(View.VISIBLE);
                    }
                    adapter.setMovies(movies);
                    adapter.setFavouriteMovies(movies);
                }
            }
        });
    }

    private int getColumnCount () {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return Math.max(width / 185, 2);
    }

    @Override
    public void onBackPressed() {
        Intent intentMainActivity = new Intent(FavouriteActivity.this, MainActivity.class);
        startActivity(intentMainActivity);
        overridePendingTransition(0,0);
    }
}