package sam.frampton.popularmovies.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import sam.frampton.popularmovies.R;
import sam.frampton.popularmovies.api.MovieDetails;

@Database(entities = {FavouriteMovie.class}, version = 1)
public abstract class MoviesDatabase extends RoomDatabase {

    private static MoviesDatabase dbInstance;

    public abstract FavouriteMovieDao favouriteMovieDao();

    public void insertFavouriteMovie(MovieDetails movie) {
        final FavouriteMovie favouriteMovie = new FavouriteMovie(movie);
        new Thread() {
            @Override
            public void run() {
                favouriteMovieDao().insert(favouriteMovie);
            }
        }.start();
    }

    public void deleteFavouriteMovie(final String id) {
        new Thread() {
            @Override
            public void run() {
                favouriteMovieDao().delete(id);
            }
        }.start();
    }

    public static MoviesDatabase getInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = Room.databaseBuilder(context, MoviesDatabase.class,
                    context.getString(R.string.movies_database_name)).build();
        }
        return dbInstance;
    }

}
