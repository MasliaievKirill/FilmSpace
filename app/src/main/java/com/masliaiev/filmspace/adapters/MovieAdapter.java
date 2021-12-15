package com.masliaiev.filmspace.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masliaiev.filmspace.R;
import com.masliaiev.filmspace.data.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private OnPosterClickListener onPosterClickListener;
    private OnReachEndListener onReachEndListener;
    private List<Movie> favouriteMovies;

    public MovieAdapter() {
        movies = new ArrayList<>();
        favouriteMovies = new ArrayList<>();
    }

    public interface OnPosterClickListener {
        void onPosterClick(int position);
    }

    public interface OnReachEndListener {
        void onReachEnd();
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
        Picasso.get().load(movie.getPosterPath()).placeholder(R.drawable.placeholder_large).into(holder.imageViewSmallPoster);
        holder.textViewTitleIfPosterDoNotExist.setText(movie.getTitle());
        if (movie.getPosterPath().contains(".jpg")) {
            holder.textViewTitleIfPosterDoNotExist.setVisibility(View.INVISIBLE);
        } else {
            holder.textViewTitleIfPosterDoNotExist.setVisibility(View.VISIBLE);
        }
        if (favouriteMovies != null) {
            boolean inFavourite = false;
            for (int i = 0; i < favouriteMovies.size(); i++) {
                String originalTitle = favouriteMovies.get(i).getOriginalTitle();
                if (originalTitle.equals(movie.getOriginalTitle())) {
                    inFavourite = true;
                }
            }
            if (inFavourite) {
                holder.imageViewFavouriteIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.imageViewFavouriteIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewSmallPoster;
        private final ImageView imageViewFavouriteIndicator;
        private final TextView textViewTitleIfPosterDoNotExist;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSmallPoster = itemView.findViewById(R.id.imageViewSmallPoster);
            imageViewFavouriteIndicator = itemView.findViewById(R.id.imageViewFavouriteIndicator);
            textViewTitleIfPosterDoNotExist = itemView.findViewById(R.id.textViewTitleIfPosterDoNotExist);
            itemView.setOnClickListener(v -> {
                if (onPosterClickListener != null) {
                    onPosterClickListener.onPosterClick(getAdapterPosition());
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        this.movies.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFavouriteMovies(List<Movie> favouriteMovies) {
        this.favouriteMovies = favouriteMovies;
        notifyDataSetChanged();
    }

    public List<Movie> getMovies() {
        return movies;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addMovies(List<Movie> movies) {
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }



}
