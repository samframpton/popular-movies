package sam.frampton.popularmovies.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import sam.frampton.popularmovies.api.MovieDetails;

@Entity
public class FavouriteMovie {

    public FavouriteMovie(@NonNull String id) {
        this.id = id;
    }

    public FavouriteMovie(MovieDetails movieDetails) {
        this.id = movieDetails.getId();
        this.title = movieDetails.getTitle();
        this.overview = movieDetails.getOverview();
        this.releaseDate = movieDetails.getReleaseDate();
        this.voteAverage = movieDetails.getVoteAverage();
        this.posterUrl = movieDetails.getPosterUrl();
    }

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo
    public String title;

    @ColumnInfo
    public String overview;

    @ColumnInfo
    public String releaseDate;

    @ColumnInfo
    public String voteAverage;

    @ColumnInfo
    public String posterUrl;
}
