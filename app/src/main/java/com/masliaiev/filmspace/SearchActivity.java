package com.masliaiev.filmspace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.masliaiev.filmspace.adapters.MovieAdapter;
import com.masliaiev.filmspace.data.FavouriteMovie;
import com.masliaiev.filmspace.data.MainViewModel;
import com.masliaiev.filmspace.data.Movie;
import com.masliaiev.filmspace.utils.JSONUtils;
import com.masliaiev.filmspace.utils.NetworkUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private EditText editTextSearchQuery;
    private RecyclerView recyclerViewSearchedMovies;
    private MovieAdapter movieAdapter;
    private ProgressBar progressBarLoadingSearchedMovies;
    private BottomNavigationView bottomNavigationSearch;
    private Button buttonSearch;

    private MainViewModel viewModel;

    private static final int LOADER_ID = 134;
    private LoaderManager loaderManager;
    private static boolean isLoading = false;

    private String query;
    private static int page = 1;

    private static String lang;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        lang = Locale.getDefault().getLanguage();
        progressBarLoadingSearchedMovies = findViewById(R.id.progressBarLoadingSearchedMovies);
        bottomNavigationSearch = findViewById(R.id.bottomNavigationViewSearch);
        Menu menu = bottomNavigationSearch.getMenu();
        menu.findItem(R.id.bottomSearch).setIcon(R.drawable.search_white);
        bottomNavigationSearch.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottomHome:
                        Intent intentHome = new Intent(SearchActivity.this, MainActivity.class);
                        startActivity(intentHome);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.bottomFavourites:
                        Intent intentFavourites = new Intent(SearchActivity.this, FavouriteActivity.class);
                        startActivity(intentFavourites);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.bottomRandom:
                        Intent intentRandom = new Intent(SearchActivity.this, RandomActivity.class);
                        startActivity(intentRandom);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.bottomSearch:
                        break;
                    default:
                        Toast.makeText(SearchActivity.this, "error 1", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        editTextSearchQuery = findViewById(R.id.editTextTextSearchQuery);
        editTextSearchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                }
                return false;
            }
        });
        recyclerViewSearchedMovies = findViewById(R.id.recyclerViewSearchedMovies);
        recyclerViewSearchedMovies.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();
        loaderManager = LoaderManager.getInstance(this);
        recyclerViewSearchedMovies.setAdapter(movieAdapter);
        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                    Movie movie = movieAdapter.getMovies().get(position);
                    if (movie != null) {
                        Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                        intent.putExtra("search", "search");
                        intent.putExtra("id", movie.getId());
                        intent.putExtra("voteCount", movie.getVoteCount());
                        intent.putExtra("title", movie.getTitle());
                        intent.putExtra("originalTitle", movie.getOriginalTitle());
                        intent.putExtra("overview", movie.getOverview());
                        intent.putExtra("posterPath", movie.getPosterPath());
                        intent.putExtra("bigPosterPath", movie.getBigPosterPath());
                        intent.putExtra("backdropPath", movie.getBackdropPath());
                        intent.putExtra("voteAverage", movie.getVoteAverage());
                        intent.putExtra("releaseDate", movie.getReleaseDate());
                        startActivity(intent);
                    } else {
                        Toast.makeText(SearchActivity.this, "error 2", Toast.LENGTH_SHORT).show();
                    }
            }
        });
        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                if (!isLoading) {
                    downloadData(query, lang, page);
                }
            }
        });
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                progressBarLoadingSearchedMovies.setVisibility(View.VISIBLE);
                isLoading = true;
            }
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(data);
        if (movies != null && !movies.isEmpty()) {
            if (page == 1) {
                movieAdapter.setMovies(movies);
            } else {
                movieAdapter.addMovies(movies);
            }
            page++;
        } else {
            Toast.makeText(this, "Nothing", Toast.LENGTH_SHORT).show();
        }
        progressBarLoadingSearchedMovies.setVisibility(View.INVISIBLE);
        isLoading = false;
        loaderManager.destroyLoader(LOADER_ID);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }

    private int getColumnCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width / 185 > 2 ? width / 185 : 2;
    }

    private void downloadData (String query, String lang, int page) {
        URL url = NetworkUtils.buildURLToSearch(query, lang, page);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(buttonSearch.getWindowToken(), 0);
    }
    private void search () {
        hideKeyboard();
        movieAdapter.clear();
        query = null;
        page = 1;
        query = editTextSearchQuery.getText().toString().trim();
        if (!query.isEmpty()) {
            downloadData(query, lang, page);
        } else {
            Toast.makeText(SearchActivity.this, "Введите название фильма, или часть названия для поиска ", Toast.LENGTH_LONG).show();
        }
    }
}
