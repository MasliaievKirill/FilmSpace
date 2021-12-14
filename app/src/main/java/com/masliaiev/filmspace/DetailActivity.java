package com.masliaiev.filmspace;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.masliaiev.filmspace.adapters.ReviewAdapter;
import com.masliaiev.filmspace.adapters.TrailerAdapter;
import com.masliaiev.filmspace.data.FavouriteMovie;
import com.masliaiev.filmspace.data.MainViewModel;
import com.masliaiev.filmspace.data.Movie;
import com.masliaiev.filmspace.data.Review;
import com.masliaiev.filmspace.data.Trailer;
import com.masliaiev.filmspace.utils.JSONUtils;
import com.masliaiev.filmspace.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewAddToFavourite;

    private int id;
    private Movie movie;
    private FavouriteMovie favouriteMovie;
    private boolean fromSearchActivity = false;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        String lang = Locale.getDefault().getLanguage();
        ImageView imageViewButtonToBack = findViewById(R.id.imageViewButtonToBack);
        imageViewButtonToBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ScrollView scrollViewDetail = findViewById(R.id.scrollViewDetail);
        TextView textViewTopTitle = findViewById(R.id.textViewTopTitle);
        ImageView imageViewBigPoster = findViewById(R.id.imageViewBigPoster);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        TextView textViewRating = findViewById(R.id.textViewRating);
        TextView textViewReleaseDate = findViewById(R.id.textViewReleaseDate);
        TextView textViewOverview = findViewById(R.id.textViewOverview);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id") && intent.hasExtra("main")) {
            id = intent.getIntExtra("id", -1);
            movie = viewModel.getMovieById(id);
        } else if (intent != null && intent.hasExtra("id") && intent.hasExtra("favourite")) {
            id = intent.getIntExtra("id", -1);
            movie = viewModel.getFavouriteMovieById(id);
        } else if (intent != null && intent.hasExtra("search") && intent.hasExtra("id") && intent.hasExtra("voteCount")
                && intent.hasExtra("title") && intent.hasExtra("originalTitle") && intent.hasExtra("overview") && intent.hasExtra("posterPath")
                && intent.hasExtra("bigPosterPath") && intent.hasExtra("backdropPath")&& intent.hasExtra("voteAverage") && intent.hasExtra("releaseDate")){
            id = intent.getIntExtra("id", -1);
            movie = new Movie(intent.getIntExtra("id", -1), intent.getIntExtra("voteCount", 1), intent.getStringExtra("title"),intent.getStringExtra("originalTitle"),
                    intent.getStringExtra("overview"), intent.getStringExtra("posterPath"), intent.getStringExtra("bigPosterPath"), intent.getStringExtra("backdropPath"),
                    intent.getDoubleExtra("voteAverage", -1), intent.getStringExtra("releaseDate"));
        } else if (intent != null &&  intent.hasExtra("random") && intent.hasExtra("id") && intent.hasExtra("voteCount")
                && intent.hasExtra("title") && intent.hasExtra("originalTitle") && intent.hasExtra("overview") && intent.hasExtra("posterPath")
                && intent.hasExtra("bigPosterPath") && intent.hasExtra("backdropPath")&& intent.hasExtra("voteAverage") && intent.hasExtra("releaseDate")){
            id = intent.getIntExtra("id", -1);
            movie = new Movie(intent.getIntExtra("id", -1), intent.getIntExtra("voteCount", 1), intent.getStringExtra("title"),intent.getStringExtra("originalTitle"),
                    intent.getStringExtra("overview"), intent.getStringExtra("posterPath"), intent.getStringExtra("bigPosterPath"), intent.getStringExtra("backdropPath"),
                    intent.getDoubleExtra("voteAverage", -1), intent.getStringExtra("releaseDate"));
        }
        else {
            finish();
        }
        imageViewAddToFavourite = findViewById(R.id.imageViewAddToFavourite);
        imageViewAddToFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favouriteMovie == null) {
                    viewModel.insertFavouriteMovie(new FavouriteMovie(movie));
                    Toast.makeText(DetailActivity.this, R.string.add_to_favourite, Toast.LENGTH_SHORT).show();
                } else {
                    viewModel.deleteFavouriteMovie(favouriteMovie);
                    Toast.makeText(DetailActivity.this, R.string.delete_from_favourite, Toast.LENGTH_SHORT).show();
                    if (fromSearchActivity) {
                        favouriteMovie = null;
                    }
                }
                setFavourite();
            }
        });
        Picasso.get().load(movie.getBigPosterPath()).placeholder(R.drawable.placeholder_large).into(imageViewBigPoster);
        textViewTitle.setText(movie.getTitle());
        textViewTopTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewRating.setText(Double.toString(movie.getVoteAverage()));
        textViewReleaseDate.setText(movie.getReleaseDate());
        textViewOverview.setText(movie.getOverview());
        setFavourite();
        RecyclerView recyclerViewTrailers = findViewById(R.id.recyclerViewTrailers);
        RecyclerView recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        TrailerAdapter trailerAdapter = new TrailerAdapter();
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentToTrailer);
            }
        });
        ReviewAdapter reviewAdapter = new ReviewAdapter();
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewReviews.setAdapter(reviewAdapter);
        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideos(movie.getId(), lang);
        JSONObject jsonObjectReviews = NetworkUtils.getJSONForReviews(movie.getId(), lang);
        ArrayList<Trailer> trailers = JSONUtils.getTrailersFromJSON(jsonObjectTrailers);
        ArrayList<Review> reviews = JSONUtils.getReviewsFromJSON(jsonObjectReviews);
        trailerAdapter.setTrailers(trailers);
        reviewAdapter.setReviews(reviews);
        scrollViewDetail.smoothScrollTo(0,0);
    }


    private void setFavourite () {
            favouriteMovie = viewModel.getFavouriteMovieById(id);
            if (favouriteMovie == null) {
                imageViewAddToFavourite.setImageResource(R.drawable.favourite_add_star_white_small);
            } else {
                imageViewAddToFavourite.setImageResource(R.drawable.favourite_add_star_gold_small);
            }

    }
}