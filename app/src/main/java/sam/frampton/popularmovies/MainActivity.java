package sam.frampton.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import sam.frampton.popularmovies.api.MovieDetails;

public final class MainActivity extends AppCompatActivity {

    private Destination mDestination;

    private enum Destination {
        POPULAR,
        TOP_RATED,
        FAVOURITES
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDestination = Destination.POPULAR;
        MoviesViewModel moviesViewModel =
                new ViewModelProvider(this).get(MoviesViewModel.class);

        MoviePosterAdapter moviePosterAdapter = setupMoviePostersRecyclerView();
        setupBottomNavigation(moviesViewModel, moviePosterAdapter);
        observeLiveData(moviesViewModel, moviePosterAdapter);
    }

    private MoviePosterAdapter setupMoviePostersRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_movie_posters);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        MoviePosterAdapter moviePosterAdapter = new MoviePosterAdapter(
                new MoviePosterAdapter.OnMovieSelectedListener() {
                    @Override
                    public void onMovieSelected(MovieDetails movieDetails) {
                        Intent intent = new Intent(MainActivity.this,
                                MovieActivity.class);
                        intent.putExtra(getString(R.string.extra_movie_id), movieDetails.getId());
                        intent.putExtra(getString(R.string.extra_movie_title),
                                movieDetails.getTitle());
                        intent.putExtra(getString(R.string.extra_movie_overview),
                                movieDetails.getOverview());
                        intent.putExtra(getString(R.string.extra_movie_release_date),
                                movieDetails.getReleaseDate());
                        intent.putExtra(getString(R.string.extra_movie_vote_average),
                                movieDetails.getVoteAverage());
                        intent.putExtra(getString(R.string.extra_movie_poster_url),
                                movieDetails.getPosterUrl());
                        MainActivity.this.startActivity(intent);
                    }
                });
        recyclerView.setAdapter(moviePosterAdapter);
        return moviePosterAdapter;
    }

    private void setupBottomNavigation(final MoviesViewModel moviesViewModel,
                                       final MoviePosterAdapter moviePosterAdapter) {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation_main);
        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.page_popular:
                                mDestination = Destination.POPULAR;
                                moviePosterAdapter.updateMovieDetails(
                                        moviesViewModel.getPopularMovies().getValue());
                                break;
                            case R.id.page_top_rated:
                                mDestination = Destination.TOP_RATED;
                                moviePosterAdapter.updateMovieDetails(
                                        moviesViewModel.getTopRatedMovies().getValue());
                                break;
                            case R.id.page_favourites:
                                mDestination = Destination.FAVOURITES;
                                moviePosterAdapter.updateMovieDetails(
                                        moviesViewModel.getFavouriteMovies().getValue());
                                break;
                            default:
                                return false;
                        }
                        moviePosterAdapter.notifyDataSetChanged();
                        return true;
                    }
                });
    }

    private void observeLiveData(MoviesViewModel moviesViewModel,
                                 final MoviePosterAdapter moviePosterAdapter) {
        moviesViewModel.getPopularMovies().observe(this,
                new Observer<List<MovieDetails>>() {
                    @Override
                    public void onChanged(List<MovieDetails> movieDetails) {
                        if (mDestination == Destination.POPULAR) {
                            moviePosterAdapter.updateMovieDetails(movieDetails);
                            moviePosterAdapter.notifyDataSetChanged();
                        }
                    }
                });
        moviesViewModel.getTopRatedMovies().observe(this,
                new Observer<List<MovieDetails>>() {
                    @Override
                    public void onChanged(List<MovieDetails> movieDetails) {
                        if (mDestination == Destination.TOP_RATED) {
                            moviePosterAdapter.updateMovieDetails(movieDetails);
                            moviePosterAdapter.notifyDataSetChanged();
                        }
                    }
                });
        moviesViewModel.getFavouriteMovies().observe(this,
                new Observer<List<MovieDetails>>() {
                    @Override
                    public void onChanged(List<MovieDetails> movieDetails) {
                        if (mDestination == Destination.FAVOURITES) {
                            moviePosterAdapter.updateMovieDetails(movieDetails);
                            moviePosterAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private static final class MoviePosterAdapter
            extends RecyclerView.Adapter<MoviePosterAdapter.MoviePosterViewHolder> {

        private final OnMovieSelectedListener mMovieSelectedListener;
        private List<MovieDetails> mMovieDetails;

        MoviePosterAdapter(OnMovieSelectedListener listener) {
            mMovieSelectedListener = listener;
            mMovieDetails = Collections.emptyList();
        }

        void updateMovieDetails(List<MovieDetails> movieDetails) {
            if (movieDetails == null) {
                mMovieDetails = Collections.emptyList();
            } else {
                mMovieDetails = movieDetails;
            }
        }

        @Override
        public MoviePosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView moviePoster = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_poster, parent, false);
            return new MoviePosterViewHolder(moviePoster);
        }

        @Override
        public void onBindViewHolder(MoviePosterViewHolder holder, int position) {
            MovieDetails movieDetails = mMovieDetails.get(position);
            Picasso.get().load(movieDetails.getPosterUrl()).into(holder.getPosterImageView());
            holder.getPosterImageView().setContentDescription(movieDetails.getTitle());
        }

        @Override
        public int getItemCount() {
            return mMovieDetails.size();
        }

        private final class MoviePosterViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {

            private final ImageView mMoviePosterImageView;

            private MoviePosterViewHolder(ImageView itemView) {
                super(itemView);
                mMoviePosterImageView = itemView;
                mMoviePosterImageView.setOnClickListener(this);
            }

            private ImageView getPosterImageView() {
                return mMoviePosterImageView;
            }

            @Override
            public void onClick(View view) {
                MovieDetails movieDetails = mMovieDetails.get(getAdapterPosition());
                mMovieSelectedListener.onMovieSelected(movieDetails);
            }
        }

        interface OnMovieSelectedListener {
            void onMovieSelected(MovieDetails movieDetails);
        }

    }

}
