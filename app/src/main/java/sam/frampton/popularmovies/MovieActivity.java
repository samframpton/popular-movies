package sam.frampton.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sam.frampton.popularmovies.api.MovieDetails;
import sam.frampton.popularmovies.api.MovieReview;
import sam.frampton.popularmovies.api.MovieVideo;
import sam.frampton.popularmovies.room.MoviesDatabase;

/**
 * Displays details about a selected movie.
 */
public final class MovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        MovieDetailsViewModel viewModel =
                new ViewModelProvider(this).get(MovieDetailsViewModel.class);

        parseIntent(viewModel);
        setTabListener();
        setFabListener(viewModel);
        addDefaultFragment();
    }

    private void parseIntent(MovieDetailsViewModel viewModel) {
        Intent intent = getIntent();
        if (intent != null) {
            String id = intent.getStringExtra(getString(R.string.extra_movie_id));
            String title = intent.getStringExtra(getString(R.string.extra_movie_title));
            String releaseDate =
                    intent.getStringExtra(getString(R.string.extra_movie_release_date));
            String voteAverage =
                    intent.getStringExtra(getString(R.string.extra_movie_vote_average));
            String overview =
                    intent.getStringExtra(getString(R.string.extra_movie_overview));
            String posterUrl =
                    intent.getStringExtra(getString(R.string.extra_movie_poster_url));
            MovieDetails movieDetails = new MovieDetails(
                    id, title, overview, releaseDate, voteAverage, posterUrl);
            viewModel.setMovieDetails(movieDetails);
        }
    }

    private void setTabListener() {
        TabLayout tabLayout = findViewById(R.id.tab_layout_movie);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Class<? extends Fragment> fragment;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = MovieDetailsFragment.class;
                        break;
                    case 1:
                        fragment = MovieVideosFragment.class;
                        break;
                    case 2:
                        fragment = MovieReviewsFragment.class;
                        break;
                    default:
                        return;
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container_movie, fragment, null);
                transaction.commit();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setFabListener(final MovieDetailsViewModel viewModel) {
        final MovieDetails movieDetails = viewModel.getMovieDetails();
        final FloatingActionButton fabUnselected = findViewById(R.id.fab_favourite_unselected);
        fabUnselected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoviesDatabase moviesDatabase = MoviesDatabase.getInstance(getApplicationContext());
                moviesDatabase.insertFavouriteMovie(movieDetails);
                Toast.makeText(MovieActivity.this,
                        getString(R.string.added_to_favourites), Toast.LENGTH_SHORT).show();
            }
        });
        final FloatingActionButton fabSelected = findViewById(R.id.fab_favourite_selected);
        fabSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoviesDatabase moviesDatabase = MoviesDatabase.getInstance(getApplicationContext());
                moviesDatabase.deleteFavouriteMovie(movieDetails.getId());
                Toast.makeText(MovieActivity.this,
                        getString(R.string.removed_from_favourites), Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.isFavourite(movieDetails.getId()).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isFavourite) {
                if (isFavourite) {
                    fabSelected.setVisibility(View.VISIBLE);
                    fabUnselected.setVisibility(View.GONE);
                } else {
                    fabSelected.setVisibility(View.GONE);
                    fabUnselected.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addDefaultFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container_movie, MovieDetailsFragment.class, null);
        transaction.commit();
    }

    public static class MovieDetailsFragment extends Fragment {

        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_movie_details, container, false);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setViewData();
        }

        private void setViewData() {
            FragmentActivity activity = getActivity();
            MovieDetails movieDetails =
                    new ViewModelProvider(activity).get(MovieDetailsViewModel.class)
                            .getMovieDetails();
            TextView title = activity.findViewById(R.id.tv_title);
            title.setText(formatString(movieDetails.getTitle()));
            TextView releaseDate = activity.findViewById(R.id.tv_release_date);
            releaseDate.setText(formatDate(movieDetails.getReleaseDate()));
            TextView voteAverage = activity.findViewById(R.id.tv_vote_average);
            voteAverage.setText(formatVoteAverage(movieDetails.getVoteAverage()));
            TextView overview = activity.findViewById(R.id.tv_overview);
            overview.setText(formatString(movieDetails.getOverview()));
            ImageView poster = activity.findViewById(R.id.iv_details_movie_poster);
            if (movieDetails.getPosterUrl() != null) {
                Picasso.get().load(movieDetails.getPosterUrl()).into(poster);
            }
        }

        private String formatString(String string) {
            return string == null? getString(R.string.default_text) : string;
        }

        private String formatDate(String date) {
            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
            java.text.DateFormat dateFormat =
                    android.text.format.DateFormat.getDateFormat(this.getContext());
            try {
                Date parsedDate = sdf.parse(date);
                if (parsedDate != null) {
                    return dateFormat.format(parsedDate);
                }
            } catch (ParseException ignored) {
            }
            return getString(R.string.default_text);
        }

        private String formatVoteAverage(String voteAverage) {
            return voteAverage == null? getString(R.string.default_text) :
                    voteAverage + getString(R.string.vote_average_suffix);
        }

    }

    public static class MovieVideosFragment extends Fragment {

        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_movie_videos, container, false);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            MovieDetails movieDetails =
                    new ViewModelProvider(getActivity()).get(MovieDetailsViewModel.class)
                            .getMovieDetails();
            MovieVideosFragment.VideoAdapter videoAdapter = setupVideoRecyclerView();
            observeLiveData(videoAdapter, movieDetails.getId());
        }

        private MovieVideosFragment.VideoAdapter setupVideoRecyclerView() {
            RecyclerView recyclerView = getView().findViewById(R.id.recycler_view_movie_videos);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
            recyclerView.setLayoutManager(layoutManager);
            MovieVideosFragment.VideoAdapter videoAdapter = new MovieVideosFragment.VideoAdapter(
                    new MovieVideosFragment.VideoAdapter.OnVideoSelectedListener() {
                        @Override
                        public void onVideoSelected(MovieVideo movieVideo) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(movieVideo.getUrl()));
                            startActivity(intent);
                        }
                    });
            recyclerView.setAdapter(videoAdapter);
            return videoAdapter;
        }

        private void observeLiveData(final MovieVideosFragment.VideoAdapter videoAdapter,
                                     String id) {
            LiveData<List<MovieVideo>> liveData =
                    new ViewModelProvider(this).get(MovieDetailsViewModel.class)
                            .getMovieVideos(id);
            liveData.observe(getViewLifecycleOwner(), new Observer<List<MovieVideo>>() {
                @Override
                public void onChanged(List<MovieVideo> movieVideos) {
                    videoAdapter.updateVideos(movieVideos);
                    videoAdapter.notifyDataSetChanged();
                }
            });
        }

        private static final class VideoAdapter
                extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

            private final VideoAdapter.OnVideoSelectedListener mVideoSelectedListener;
            private List<MovieVideo> mMovieVideos;

            VideoAdapter(MovieVideosFragment.VideoAdapter.OnVideoSelectedListener listener) {
                mVideoSelectedListener = listener;
                mMovieVideos = Collections.emptyList();
            }

            void updateVideos(List<MovieVideo> movieVideos) {
                mMovieVideos = movieVideos;
            }

            @Override
            public VideoAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_movie_video, parent, false);
                return new VideoAdapter.VideoViewHolder(cardView);
            }

            @Override
            public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder,
                                         int position) {
                MovieVideo movieVideo = mMovieVideos.get(position);
                Picasso.get().load(movieVideo.getThumbnailUrl())
                        .into(holder.getThumbnailImageView());
                holder.getNameTextView().setText(movieVideo.getName());
            }

            @Override
            public int getItemCount() {
                return mMovieVideos.size();
            }

            private final class VideoViewHolder extends RecyclerView.ViewHolder
                    implements View.OnClickListener {

                private final ImageView mThumbnailImageView;
                private final TextView mNameTextView;

                private VideoViewHolder(CardView cardView) {
                    super(cardView);
                    cardView.setOnClickListener(this);
                    mThumbnailImageView = cardView.findViewById(R.id.iv_video_thumbnail);
                    mNameTextView = cardView.findViewById(R.id.tv_video_name);
                }

                private ImageView getThumbnailImageView() {
                    return mThumbnailImageView;
                }

                private TextView getNameTextView() {
                    return mNameTextView;
                }

                @Override
                public void onClick(View view) {
                    MovieVideo movieVideo = mMovieVideos.get(getAdapterPosition());
                    mVideoSelectedListener.onVideoSelected(movieVideo);
                }
            }

            interface OnVideoSelectedListener {
                void onVideoSelected(MovieVideo movieVideo);
            }

        }

    }

    public static class MovieReviewsFragment extends Fragment {

        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_movie_reviews, container, false);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            MovieDetails movieDetails =
                    new ViewModelProvider(getActivity()).get(MovieDetailsViewModel.class)
                            .getMovieDetails();
            ReviewAdapter reviewAdapter = setupReviewRecyclerView();
            observeLiveData(reviewAdapter, movieDetails.getId());
        }

        private ReviewAdapter setupReviewRecyclerView() {
            RecyclerView recyclerView = getView().findViewById(R.id.recycler_view_movie_reviews);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext());
            recyclerView.setLayoutManager(layoutManager);
            ReviewAdapter reviewAdapter = new ReviewAdapter(
                    new ReviewAdapter.OnReviewSelectedListener() {
                        @Override
                        public void onReviewSelected(MovieReview movieReview) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(movieReview.getUrl()));
                            startActivity(intent);
                        }
                    });
            recyclerView.setAdapter(reviewAdapter);
            return reviewAdapter;
        }

        private void observeLiveData(final ReviewAdapter reviewAdapter, String id) {
            LiveData<List<MovieReview>> liveData =
                    new ViewModelProvider(this).get(MovieDetailsViewModel.class)
                            .getMovieReviews(id);
            liveData.observe(getViewLifecycleOwner(), new Observer<List<MovieReview>>() {
                @Override
                public void onChanged(List<MovieReview> movieReviews) {
                    reviewAdapter.updateReviews(movieReviews);
                    reviewAdapter.notifyDataSetChanged();
                }
            });
        }

        private static final class ReviewAdapter
                extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

            private final OnReviewSelectedListener mReviewSelectedListener;
            private List<MovieReview> mMovieReviews;

            ReviewAdapter(OnReviewSelectedListener listener) {
                mReviewSelectedListener = listener;
                mMovieReviews = Collections.emptyList();
            }

            void updateReviews(List<MovieReview> movieReviews) {
                mMovieReviews = movieReviews;
            }

            @Override
            public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_movie_review, parent, false);
                return new ReviewViewHolder(cardView);
            }

            @Override
            public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
                MovieReview movieReview = mMovieReviews.get(position);
                holder.getAuthorTextView().setText(movieReview.getAuthor());
                holder.getContentTextView().setText(movieReview.getContent());
            }

            @Override
            public int getItemCount() {
                return mMovieReviews.size();
            }

            private final class ReviewViewHolder extends RecyclerView.ViewHolder
                    implements View.OnClickListener {

                private final TextView mAuthorTextView;
                private final TextView mContentTextView;

                private ReviewViewHolder(CardView cardView) {
                    super(cardView);
                    cardView.setOnClickListener(this);
                    mAuthorTextView = cardView.findViewById(R.id.tv_review_author);
                    mContentTextView = cardView.findViewById(R.id.tv_review_content);
                }

                private TextView getAuthorTextView() {
                    return mAuthorTextView;
                }

                private TextView getContentTextView() {
                    return mContentTextView;
                }

                @Override
                public void onClick(View view) {
                    MovieReview movieReview = mMovieReviews.get(getAdapterPosition());
                    mReviewSelectedListener.onReviewSelected(movieReview);
                }
            }

            interface OnReviewSelectedListener {
                void onReviewSelected(MovieReview movieReview);
            }

        }

    }

}
