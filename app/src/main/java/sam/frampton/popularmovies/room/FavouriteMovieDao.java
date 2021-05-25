package sam.frampton.popularmovies.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavouriteMovieDao {
    @Query("SELECT * FROM favouriteMovie")
    LiveData<List<FavouriteMovie>> getAll();

    @Query("SELECT * FROM favouriteMovie WHERE id = :id")
    LiveData<FavouriteMovie> getMovie(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FavouriteMovie favouriteMovie);

    @Query("DELETE FROM favouritemovie WHERE id = :id")
    void delete(String id);
}
