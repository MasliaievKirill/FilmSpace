package com.masliaiev.filmspace.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainViewModel extends AndroidViewModel {

    private static MovieDatabase database;
    private LiveData<List<Movie>> movies;

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(getApplication());
        movies = database.movieDao().getAllMovies();
    }

    public Movie getMovieById (int id) {
        try {
            return new GetMovieTask().execute(id).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    private static class GetMovieTask extends AsyncTask <Integer, Void, Movie> {
        @Override
        protected Movie doInBackground(Integer... integers) {
            if(integers != null && integers.length > 0) {
                return database.movieDao().getMovieById(integers[0]);
            }
            return null;
        }
    }

    public void deleteAllMovies () {
        new DeleteAllMoviesTask().execute();
    }

    private static class DeleteAllMoviesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteAllMovies();
            return null;
        }

    }

    public void insertMovie (Movie movie) {
        new InsertTask().execute(movie);
    }

    private static class InsertTask extends AsyncTask <Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if(movies != null && movies.length > 0) {
                database.movieDao().insertMovie(movies[0]);
            }
            return null;
        }
    }

    public void deleteMovie (Movie movie) {
        new DeleteTask().execute(movie);
    }

    private static class DeleteTask extends AsyncTask <Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if(movies != null && movies.length > 0) {
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }
    }


}
