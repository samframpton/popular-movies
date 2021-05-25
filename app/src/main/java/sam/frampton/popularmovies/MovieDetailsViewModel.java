package sam.frampton.popularmovies;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import sam.frampton.popularmovies.api.MovieDetails;
import sam.frampton.popularmovies.api.MovieReview;
import sam.frampton.popularmovies.api.MovieVideo;
import sam.frampton.popularmovies.api.NetworkUtils;
import sam.frampton.popularmovies.room.FavouriteMovie;
import sam.frampton.popularmovies.room.MoviesDatabase;

public final class MovieDetailsViewModel extends AndroidViewModel {

    private MovieDetails mMovieDetails;
    private LiveData<Boolean> mFavourite;
    private MutableLiveData<List<MovieVideo>> mMovieVideos;
    private MutableLiveData<List<MovieReview>> mMovieReviews;

    public MovieDetailsViewModel(Application application) {
        super(application);
    }

    public MovieDetails getMovieDetails() {
        return mMovieDetails;
    }

    public void setMovieDetails(MovieDetails movieDetails) {
        mMovieDetails = movieDetails;
    }

    public LiveData<Boolean> isFavourite(String movieId) {
        if (mFavourite == null) {
            LiveData<FavouriteMovie> favouriteMovieLiveData =
                    MoviesDatabase.getInstance(getApplication().getApplicationContext())
                            .favouriteMovieDao().getMovie(movieId);
            mFavourite = Transformations.map(favouriteMovieLiveData,
                    new Function<FavouriteMovie, Boolean>() {
                @Override
                public Boolean apply(FavouriteMovie input) {
                    return input!=null;
                }
            });
        }
        return mFavourite;
    }

    public LiveData<List<MovieVideo>> getMovieVideos(String movieId) {
        if (mMovieVideos == null) {
            mMovieVideos = new MutableLiveData<>();
            loadMovieVideos(movieId);
        }
        return mMovieVideos;
    }

    public LiveData<List<MovieReview>> getMovieReviews(String movieId) {
        if (mMovieReviews == null) {
            mMovieReviews = new MutableLiveData<>();
            loadMovieReviews(movieId);
        }
        return mMovieReviews;
    }

    private void loadMovieVideos(final String movieId) {
        new Thread() {
            @Override
            public void run() {
                mMovieVideos.postValue(NetworkUtils.getMovieVideos(movieId));
            }
        }.start();
    }

    private void loadMovieReviews(final String movieId) {
        new Thread() {
            @Override
            public void run() {
                mMovieReviews.postValue(NetworkUtils.getMovieReviews(movieId));
            }
        }.start();
    }

}
