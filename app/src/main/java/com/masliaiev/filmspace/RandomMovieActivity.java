package com.masliaiev.filmspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.masliaiev.filmspace.data.FavouriteMovie;
import com.masliaiev.filmspace.data.MainViewModel;
import com.masliaiev.filmspace.data.Movie;

import java.util.ArrayList;
import java.util.List;

public class RandomMovieActivity extends AppCompatActivity implements SensorEventListener{

    private BottomNavigationView bottomNavigationViewRandom;
    private MainViewModel viewModel;

    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private SensorManager sensorManager;

    private static final float SHAKE_THRESHOLD = 3.25f;
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLIS = 1000;
    private long mLastShakeTime;

    private Sensor shakeSensor;


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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        shakeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        //List<Movie> movieForRandom = new ArrayList<>();
        //LiveData<List<FavouriteMovie>> favouriteMovies = viewModel.getFavouriteMovies();
        //favouriteMovies.observe(this, new Observer<List<FavouriteMovie>>() {
        //    @Override
        //    public void onChanged(List<FavouriteMovie> favouriteMovies) {
        //        if (favouriteMovies != null) {
        //            List<Movie> movies = new ArrayList<>(favouriteMovies);
        //            movieForRandom.addAll(movies);
        //        }
        //    }
        //});

            //if (movieForRandom.size() > 0) {
            //    Movie movie = movieForRandom.get((int) Math.random() + movieForRandom.size());
            //    Intent intent = new Intent(RandomMovieActivity.this, DetailActivity.class);
            //    intent.putExtra("id", movie.getId());
            //    startActivity(intent);
            //} else {

            //}

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLIS) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;


                if (acceleration > SHAKE_THRESHOLD) {
                    mLastShakeTime = curTime;
                    Toast.makeText(this, "No film in favourite, but device has shaken.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}