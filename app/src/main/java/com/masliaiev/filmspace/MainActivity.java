package com.masliaiev.filmspace;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private RecyclerView recyclerViewPosters;
    private MovieAdapter movieAdapter;
    private TextView textViewPopularity;
    private TextView textViewTopRated;
    private BottomNavigationView bottomNavigationView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkConnection();
        if (!connection) {
            Toast.makeText(this, "Отсутствует Интернет соединение", Toast.LENGTH_SHORT).show();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        swipeRefreshLayoutData = findViewById(R.id.swipeToRefreshData);
        swipeRefreshLayoutData.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkConnection();
                if (!connection) {
                    Toast.makeText(MainActivity.this, "Данные не обновлены", Toast.LENGTH_SHORT).show();
                } else {
                    page = 1;
                    isLoading = false;
                    downloadData(methodOfSort, page);
                    Toast.makeText(MainActivity.this, "Данные обновлены", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayoutData.setRefreshing(false);
            }
        });
        lang = Locale.getDefault().getLanguage();
        loaderManager = LoaderManager.getInstance(this);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.bottomHome).setIcon(R.drawable.home_white);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottomHome:
                        break;
                    case R.id.bottomFavourites:
                        Intent intentFavourites = new Intent(MainActivity.this, FavouriteActivity.class);
                        startActivity(intentFavourites);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.bottomRandom:
                        Intent intentRandom = new Intent(MainActivity.this, RandomActivity.class);
                        startActivity(intentRandom);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    case R.id.bottomSearch:
                        Intent intentSearch = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(intentSearch);
                        overridePendingTransition(0,0);
                        finish();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "errorM", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        viewModel = new  ViewModelProvider(this).get(MainViewModel.class);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        textViewPopularity = findViewById(R.id.textViewPopularity);
        textViewTopRated = findViewById(R.id.textViewTopRated);
        recyclerViewPosters = findViewById(R.id.recyclerViewPosters);
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();
        recyclerViewPosters.setAdapter(movieAdapter);
        page = 1;
        setMethodOfSort(false);
        textViewTopRated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 1;
                setMethodOfSort(true);
            }
        });
        textViewPopularity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 1;
                setMethodOfSort(false);
            }
        });

        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Movie movie = movieAdapter.getMovies().get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("main", "main");
                intent.putExtra("id", movie.getId());
                startActivity(intent);
            }
        });
        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                if (!isLoading) {
                    downloadData(methodOfSort, page);
                }
            }
        });

        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (page == 1) {
                    movieAdapter.setMovies(movies);
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
            textViewPopularity.setTextColor(getResources().getColor(R.color.white));
            textViewTopRated.setTextColor(getResources().getColor(R.color.teal_200));
        } else {
            methodOfSort = NetworkUtils.POPULARITY;
            textViewPopularity.setTextColor(getResources().getColor(R.color.teal_200));
            textViewTopRated.setTextColor(getResources().getColor(R.color.white));
        }
        downloadData(methodOfSort, page);
    }

    private int getColumnCount () {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width / 185 > 2 ? width / 185 : 2;
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                progressBarLoading.setVisibility(View.VISIBLE);
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
}