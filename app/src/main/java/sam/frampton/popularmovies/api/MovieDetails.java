package sam.frampton.popularmovies.api;

public final class MovieDetails {

    private final String mId;
    private final String mTitle;
    private final String mOverview;
    private final String mReleaseDate;
    private final String mVoteAverage;
    private final String mPosterUrl;

    public MovieDetails(String id,
                        String title,
                        String overview,
                        String releaseDate,
                        String voteAverage,
                        String posterUrl) {
        mId = id;
        mTitle = title;
        mOverview = overview;
        mReleaseDate = releaseDate;
        mVoteAverage = voteAverage;
        mPosterUrl = posterUrl;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getVoteAverage() {
        return mVoteAverage;
    }

    public String getPosterUrl() {
        return mPosterUrl;
    }

}
