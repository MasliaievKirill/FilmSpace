package com.masliaiev.filmspace;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.masliaiev.filmspace.adapters.MovieAdapter;
import com.masliaiev.filmspace.adapters.PreviouslySearchedAdapter;
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
    private ImageView imageViewDeleteQuery;

    private MainViewModel viewModel;

    private static final int LOADER_ID = 134;
    private LoaderManager loaderManager;
    private static boolean isLoading = false;

    private String query;
    private static int page = 1;

    private static String lang;


    private TextView textViewPreviouslySearched;
    private RecyclerView recyclerViewPreviouslySearched;
    private SharedPreferences preferences;
    private SharedPreferences preferencesCount;
    private PreviouslySearchedAdapter previouslySearchedAdapter;
    List<String> moviesPreviouslySearched;


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
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.bottomFavourites:
                        Intent intentFavourites = new Intent(SearchActivity.this, FavouriteActivity.class);
                        startActivity(intentFavourites);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.bottomRandom:
                        Intent intentRandom = new Intent(SearchActivity.this, RandomActivity.class);
                        startActivity(intentRandom);
                        overridePendingTransition(0, 0);
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
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferencesCount = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferencesCount.getInt("count", -1) == -1) {
            preferencesCount.edit().putInt("count", 1).apply();
        }
        textViewPreviouslySearched = findViewById(R.id.textViewPreviouslySearched);
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
        imageViewDeleteQuery = findViewById(R.id.imageViewDeleteQuery);
        if (editTextSearchQuery.getText().equals("")){
            imageViewDeleteQuery.setVisibility(View.INVISIBLE);
        }
        editTextSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                imageViewDeleteQuery.setVisibility(View.VISIBLE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imageViewDeleteQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSearchQuery.getText().clear();
                getTitlesFromPreferences();
                imageViewDeleteQuery.setVisibility(View.INVISIBLE);
                recyclerViewSearchedMovies.setVisibility(View.INVISIBLE);
                textViewPreviouslySearched.setVisibility(View.VISIBLE);
                recyclerViewPreviouslySearched.setVisibility(View.VISIBLE);
            }
        });
        recyclerViewSearchedMovies = findViewById(R.id.recyclerViewSearchedMovies);
        recyclerViewSearchedMovies.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();

        recyclerViewPreviouslySearched = findViewById(R.id.recyclerViewPreviouslySearched);
        recyclerViewPreviouslySearched.setLayoutManager(new LinearLayoutManager(this));
        moviesPreviouslySearched = new ArrayList<>();
        previouslySearchedAdapter = new PreviouslySearchedAdapter();
        recyclerViewPreviouslySearched.setAdapter(previouslySearchedAdapter);
        getTitlesFromPreferences();

        previouslySearchedAdapter.setOnTitleClickListener(new PreviouslySearchedAdapter.OnTitleClickListener() {
            @Override
            public void onTitleClick(int position) {
                String title = previouslySearchedAdapter.getMoviesTitles().get(position);
                if (title != null) {
                    editTextSearchQuery.setText(title);
                    search();
                }
            }
        });


        loaderManager = LoaderManager.getInstance(this);
        recyclerViewSearchedMovies.setAdapter(movieAdapter);
        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Movie movie = movieAdapter.getMovies().get(position);
                if (movie != null) {
                    if (!preferences.getString("movie1", "null").equals(movie.getTitle()) && !preferences.getString("movie2", "null").equals(movie.getTitle())
                            && !preferences.getString("movie3", "null").equals(movie.getTitle()) && !preferences.getString("movie4", "null").equals(movie.getTitle())
                            && !preferences.getString("movie5", "null").equals(movie.getTitle()) && !preferences.getString("movie6", "null").equals(movie.getTitle())) {
                        switch (preferencesCount.getInt("count", 0)) {
                            case 1:
                                preferences.edit().putString("movie1", movie.getTitle()).apply();
                                preferencesCount.edit().putInt("count", 2).apply();
                                break;
                            case 2:
                                preferences.edit().putString("movie2", movie.getTitle()).apply();
                                preferencesCount.edit().putInt("count", 3).apply();
                                break;
                            case 3:
                                preferences.edit().putString("movie3", movie.getTitle()).apply();
                                preferencesCount.edit().putInt("count", 4).apply();
                                break;
                            case 4:
                                preferences.edit().putString("movie4", movie.getTitle()).apply();
                                preferencesCount.edit().putInt("count", 5).apply();
                                break;
                            case 5:
                                preferences.edit().putString("movie5", movie.getTitle()).apply();
                                preferencesCount.edit().putInt("count", 6).apply();
                                break;
                            case 6:
                                preferences.edit().putString("movie6", movie.getTitle()).apply();
                                preferencesCount.edit().putInt("count", 1).apply();
                                break;
                        }
                    }

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

        LiveData<List<FavouriteMovie>> favouriteMovies = viewModel.getFavouriteMovies();
        favouriteMovies.observe(this, new Observer<List<FavouriteMovie>>() {
            @Override
            public void onChanged(List<FavouriteMovie> favouriteMovies) {
                List<Movie> movies = new ArrayList<>();
                if (favouriteMovies != null) {
                    movies.addAll(favouriteMovies);
                    movieAdapter.setFavouriteMovies(movies);
                }
            }
        });
        if (savedInstanceState != null) {
            editTextSearchQuery.setText(savedInstanceState.getString("query"));
            if (savedInstanceState.getString("query") != null) {
                search();
            }
            textViewPreviouslySearched.setVisibility(savedInstanceState.getInt("textViewPreviouslySearched"));
            recyclerViewPreviouslySearched.setVisibility(savedInstanceState.getInt("recyclerViewPreviouslySearched"));
            recyclerViewSearchedMovies.setVisibility(savedInstanceState.getInt("recyclerViewSearchedMovies"));
        }

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

    private void downloadData(String query, String lang, int page) {
        URL url = NetworkUtils.buildURLToSearch(query, lang, page);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(buttonSearch.getWindowToken(), 0);
    }

    private void search() {
        textViewPreviouslySearched.setVisibility(View.INVISIBLE);
        recyclerViewPreviouslySearched.setVisibility(View.INVISIBLE);
        recyclerViewSearchedMovies.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        Intent intentMainActivity = new Intent(SearchActivity.this, MainActivity.class);
        startActivity(intentMainActivity);
        overridePendingTransition(0, 0);
    }

    private void getTitlesFromPreferences () {
        moviesPreviouslySearched.clear();
        for (int i = 1; i <= 6; i++) {
            if (preferences.getString("movie" + i, null) != null) {
                moviesPreviouslySearched.add(preferences.getString("movie" + i, null));
            }
        }
        if (moviesPreviouslySearched.size() > 0) {
            previouslySearchedAdapter.setMoviesTitles(moviesPreviouslySearched);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("query", query);
        outState.putInt("textViewPreviouslySearched", textViewPreviouslySearched.getVisibility());
        outState.putInt("recyclerViewPreviouslySearched", recyclerViewPreviouslySearched.getVisibility());
        outState.putInt("recyclerViewSearchedMovies", recyclerViewSearchedMovies.getVisibility());
    }
}
