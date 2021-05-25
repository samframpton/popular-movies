package sam.frampton.popularmovies;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import sam.frampton.popularmovies.api.MovieDetails;
import sam.frampton.popularmovies.api.NetworkUtils;
import sam.frampton.popularmovies.room.FavouriteMovie;
import sam.frampton.popularmovies.room.MoviesDatabase;

public final class MoviesViewModel extends AndroidViewModel {

    public MoviesViewModel(Application application) {
        super(application);
    }

    private MutableLiveData<List<MovieDetails>> mPopularMovies;
    private MutableLiveData<List<MovieDetails>> mTopRatedMovies;
    private LiveData<List<MovieDetails>> myFavouriteMovies;

    public LiveData<List<MovieDetails>> getPopularMovies() {
        if (mPopularMovies == null) {
            mPopularMovies = new MutableLiveData<>();
            loadPopularMovies();
        }
        return mPopularMovies;
    }

    public LiveData<List<MovieDetails>> getTopRatedMovies() {
        if (mTopRatedMovies == null) {
            mTopRatedMovies = new MutableLiveData<>();
            loadTopRatedMovies();
        }
        return mTopRatedMovies;
    }

    public LiveData<List<MovieDetails>> getFavouriteMovies() {
        if (myFavouriteMovies == null) {
            LiveData<List<FavouriteMovie>> favouriteMovieLiveData =
                    MoviesDatabase.getInstance(getApplication().getApplicationContext())
                            .favouriteMovieDao().getAll();
            myFavouriteMovies = Transformations.map(favouriteMovieLiveData,
                    new Function<List<FavouriteMovie>, List<MovieDetails>>() {
                        @Override
                        public List<MovieDetails> apply(List<FavouriteMovie> input) {
                            List<MovieDetails> movieDetails = new ArrayList<>();
                            for (FavouriteMovie movie : input) {
                                movieDetails.add(new MovieDetails(
                                        movie.id, movie.title, movie.overview, movie.releaseDate,
                                        movie.voteAverage, movie.posterUrl));
                            }
                            return movieDetails;
                        }
                    });
        }
        return myFavouriteMovies;
    }

    private void loadPopularMovies() {
        new Thread() {
            @Override
            public void run() {
                mPopularMovies.postValue(NetworkUtils.getPopularMovieDetails());
            }
        }.start();
    }

    private void loadTopRatedMovies() {
        new Thread() {
            @Override
            public void run() {
                mTopRatedMovies.postValue(NetworkUtils.getTopRatedMovieDetails());
            }
        }.start();
    }

}
