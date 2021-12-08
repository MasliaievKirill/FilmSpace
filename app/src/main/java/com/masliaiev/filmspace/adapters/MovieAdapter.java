package com.masliaiev.filmspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masliaiev.filmspace.R;
import com.masliaiev.filmspace.data.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter <MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private OnPosterClickListener onPosterClickListener;
    private OnReachEndListener onReachEndListener;

    private List<Movie> favouriteMovies = null;

    public MovieAdapter () {
        movies = new ArrayList<>();
    }

    public interface OnPosterClickListener {
        void onPosterClick (int position);
    }

    public interface OnReachEndListener {
        void onReachEnd ();
    }

    public void setOnPosterClickListener(OnPosterClickListener onPosterClickListener) {
        this.onPosterClickListener = onPosterClickListener;
    }

    public void setOnReachEndListener(OnReachEndListener onReachEndListener) {
        this.onReachEndListener = onReachEndListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        if (movies.size() >= 20 && position > movies.size() - 4 && onReachEndListener != null) {
            onReachEndListener.onReachEnd();
        }
        Movie movie = movies.get(position);
        Picasso.get().load(movie.getPosterPath()).into(holder.imageViewSmallPoster);
        if (favouriteMovies != null) {
            for (Movie favouriteMovie : favouriteMovies) {
                if (favouriteMovie.getId() == movie.getId()) {
                    holder.imageViewInFavourite.setImageResource(R.drawable.favourite);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewSmallPoster;
        private ImageView imageViewInFavourite;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSmallPoster = itemView.findViewById(R.id.imageViewSmallPoster);
            imageViewInFavourite = itemView.findViewById(R.id.imageViewInFavourite);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPosterClickListener != null) {
                        onPosterClickListener.onPosterClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public void clear () {
        this.movies.clear();
        notifyDataSetChanged();
    }

    public void clearFavourite () {
        this.favouriteMovies.clear();
        notifyDataSetChanged();
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public void setFavouriteMovies(List<Movie> favouriteMovies) {
        this.favouriteMovies = favouriteMovies;
        notifyDataSetChanged();
    }

    public List<Movie> getFavouriteMovies() {
        return favouriteMovies;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void addMovies (List<Movie> movies) {
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    public void addFavouriteMovies (List<Movie> movies) {
        this.favouriteMovies.addAll(movies);
        notifyDataSetChanged();
    }
}
