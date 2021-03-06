package com.masliaiev.filmspace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private MovieAdapter movieAdapter;
    private TextView textViewPopularity;
    private TextView textViewTopRated;
    private ProgressBar progressBarLoading;
    private SwipeRefreshLayout swipeRefreshLayoutData;
    private MainViewModel viewModel;

    private static final int LOADER_ID = 133;
    private LoaderManager loaderManager;

    private static int page;
    private static int methodOfSort;
    private static boolean isLoading = false;

    private static String lang;

    private static boolean connection = false;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkConnection();
        if (!connection) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        swipeRefreshLayoutData = findViewById(R.id.swipeToRefreshData);
        swipeRefreshLayoutData.setOnRefreshListener(() -> {
            checkConnection();
            if (!connection) {
                Toast.makeText(MainActivity.this, R.string.data_not_updated, Toast.LENGTH_SHORT).show();
            } else {
                page = 1;
                isLoading = false;
                downloadData(methodOfSort, page);
                Toast.makeText(MainActivity.this, R.string.data_updated, Toast.LENGTH_SHORT).show();
            }
            swipeRefreshLayoutData.setRefreshing(false);
        });
        lang = Locale.getDefault().getLanguage();
        loaderManager = LoaderManager.getInstance(this);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.mainActivity);
        bottomNavigationView.getMenu().findItem(R.id.mainActivity).setIcon(R.drawable.home_white);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.mainActivity:
                    return true;
                case R.id.favouriteActivity:
                    Intent intentFavourites = new Intent(MainActivity.this, FavouriteActivity.class);
                    startActivity(intentFavourites);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.randomActivity:
                    Intent intentRandom = new Intent(MainActivity.this, RandomActivity.class);
                    startActivity(intentRandom);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.searchActivity:
                    Intent intentSearch = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intentSearch);
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });
        viewModel = new  ViewModelProvider(this).get(MainViewModel.class);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        textViewPopularity = findViewById(R.id.textViewPopularity);
        textViewTopRated = findViewById(R.id.textViewTopRated);
        RecyclerView recyclerViewPosters = findViewById(R.id.recyclerViewPosters);
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();
        recyclerViewPosters.setAdapter(movieAdapter);
        page = 1;
        setMethodOfSort(false);
        textViewTopRated.setOnClickListener(v -> {
            checkConnection();
            page = 1;
            setMethodOfSort(true);
            if (!connection) {
                Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        });
        textViewPopularity.setOnClickListener(v -> {
            checkConnection();
            page = 1;
            setMethodOfSort(false);
            if (!connection) {
                Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        });

        movieAdapter.setOnPosterClickListener(position -> {
            Movie movie = movieAdapter.getMovies().get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("main", "main");
            intent.putExtra("id", movie.getId());
            startActivity(intent);
        });
        movieAdapter.setOnReachEndListener(() -> {
            if (!isLoading) {
                downloadData(methodOfSort, page);
            }
        });

        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, movies -> {
            if (page == 1) {
                movieAdapter.setMovies(movies);
            }
        });

        LiveData<List<FavouriteMovie>> favouriteMovies = viewModel.getFavouriteMovies();
        favouriteMovies.observe(this, favouriteMovies1 -> {
            if (favouriteMovies1 != null) {
                List<Movie> movies = new ArrayList<>(favouriteMovies1);
                movieAdapter.setFavouriteMovies(movies);
            }
        });


    }

    private void downloadData (int methodOfSort, int page) {
        URL url = NetworkUtils.buildURL(methodOfSort, page, lang);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }


    private void setMethodOfSort (boolean isTopRated) {
        if (isTopRated) {
            methodOfSort = NetworkUtils.TOP_RATED;
            textViewPopularity.setBackgroundResource(R.drawable.background_gray);
            textViewTopRated.setBackgroundResource(R.drawable.background_light_gray);
        } else {
            methodOfSort = NetworkUtils.POPULARITY;
            textViewPopularity.setBackgroundResource(R.drawable.background_light_gray);
            textViewTopRated.setBackgroundResource(R.drawable.background_gray);
        }
        downloadData(methodOfSort, page);
    }

    private int getColumnCount () {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return Math.max(width / 185, 2);
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(() -> {
            progressBarLoading.setVisibility(View.VISIBLE);
            isLoading = true;
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(data);
        if (!movies.isEmpty()) {
            if (page == 1) {
                viewModel.deleteAllMovies();
                movieAdapter.clear();
            }
            for (Movie movie : movies) {
                viewModel.insertMovie(movie);
            }
            movieAdapter.addMovies(movies);
            page++;
        }
        isLoading = false;
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }

    private void checkConnection () {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connection = true;
        }
    }
    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}