package com.masliaiev.filmspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
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

    private RecyclerView recyclerViewFavouriteMovies;
    private MovieAdapter adapter;
    private MainViewModel viewModel;
    private BottomNavigationView bottomNavigationViewFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        bottomNavigationViewFavourite = findViewById(R.id.bottomNavigationViewFavourite);
        Menu menu = bottomNavigationViewFavourite.getMenu();
        menu.findItem(R.id.bottomFavourites).setIcon(R.drawable.favourite_white);
        bottomNavigationViewFavourite.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottomHome:
                        Intent intentMainActivity = new Intent(FavouriteActivity.this, MainActivity.class);
                        startActivity(intentMainActivity);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.bottomFavourites:
                        break;
                    case R.id.bottomRandom:
                        Intent intentRandom = new Intent(FavouriteActivity.this, RandomActivity.class);
                        startActivity(intentRandom);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.bottomSearch:
                        Intent intentSearch = new Intent(FavouriteActivity.this, SearchActivity.class);
                        startActivity(intentSearch);
                        overridePendingTransition(0,0);
                        break;
                    default:
                        Toast.makeText(FavouriteActivity.this, "errorF", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        recyclerViewFavouriteMovies = findViewById(R.id.recyclerViewFavouriteMovies);
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
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        adapter.clear();
        LiveData<List<FavouriteMovie>> favouriteMovies = viewModel.getFavouriteMovies();
        favouriteMovies.observe(this, new Observer<List<FavouriteMovie>>() {
            @Override
            public void onChanged(List<FavouriteMovie> favouriteMovies) {
                List<Movie> movies = new ArrayList<>();
                if (favouriteMovies != null) {
                    movies.addAll(favouriteMovies);
                    adapter.setMovies(movies);
                }
            }
        });


    }

    private int getColumnCount () {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width / 185 > 2 ? width / 185 : 2;
    }
}