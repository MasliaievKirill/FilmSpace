package com.masliaiev.filmspace.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.masliaiev.filmspace.R;
import com.masliaiev.filmspace.data.Movie;

import java.util.ArrayList;
import java.util.List;

public class PreviouslySearchedAdapter extends RecyclerView.Adapter <PreviouslySearchedAdapter.PreviouslySearchedViewHolder> {

    private List<String> moviesTitles;
    private OnTitleClickListener onTitleClickListener;

    public PreviouslySearchedAdapter() {
        this.moviesTitles = new ArrayList<>();
    }

    public interface OnTitleClickListener {
        void onTitleClick (int position);
    }

    public void setOnTitleClickListener(OnTitleClickListener onTitleClickListener) {
        this.onTitleClickListener = onTitleClickListener;
    }

    @NonNull
    @Override
    public PreviouslySearchedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.previously_searched_item, parent, false);
        return new  PreviouslySearchedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviouslySearchedViewHolder holder, int position) {
        String title = moviesTitles.get(position);
        if (title != null) {
            holder.textViewPreviouslySearchedTitle.setText(title);
        }
    }

    @Override
    public int getItemCount() {
        return moviesTitles.size();
    }

    class PreviouslySearchedViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewPreviouslySearchedTitle;

        public PreviouslySearchedViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPreviouslySearchedTitle = itemView.findViewById(R.id.textViewPreviouslySearchedTitle);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onTitleClickListener != null) {
                        onTitleClickListener.onTitleClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public void setMoviesTitles(List<String> moviesTitles) {
        this.moviesTitles = moviesTitles;
        notifyDataSetChanged();
    }

    public List<String> getMoviesTitles() {
        return moviesTitles;
    }

    public void addMoviesTitles (List<String> moviesTitles) {
        this.moviesTitles.addAll(moviesTitles);
        notifyDataSetChanged();
    }
}
