package sam.frampton.popularmovies.api;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sam.frampton.popularmovies.BuildConfig;

/**
 * Provides methods for obtaining movie details from TBDb.
 */
public final class NetworkUtils {

    private static final String API_SCHEME = "https";
    private static final String API_AUTHORITY = "api.themoviedb.org";
    private static final String API_VERSION = "3";
    private static final String API_MOVIE_METHOD = "movie";
    private static final String API_MOVIES_POPULAR = "popular";
    private static final String API_MOVIES_TOP_RATED = "top_rated";
    private static final String API_VIDEOS_METHOD = "videos";
    private static final String API_REVIEWS_METHOD = "reviews";

    private static final String API_KEY_PARAM = "api_key";

    private static final String IMAGE_SCHEME = "https";
    private static final String IMAGE_AUTHORITY = "image.tmdb.org";
    private static final String IMAGE_T = "t";
    private static final String IMAGE_P = "p";
    private static final String IMAGE_SIZE = "w185";

    private static final String RESULTS_KEY = "results";

    private static final String MOVIE_ID_KEY = "id";
    private static final String MOVIE_ORIGINAL_TITLE_KEY = "original_title";
    private static final String MOVIE_OVERVIEW_KEY = "overview";
    private static final String MOVIE_RELEASE_DATE_KEY = "release_date";
    private static final String MOVIE_VOTE_AVERAGE_KEY = "vote_average";
    private static final String MOVIE_POSTER_PATH_KEY = "poster_path";

    private static final String VIDEO_ID_KEY = "id";
    private static final String VIDEO_NAME_KEY = "name";
    private static final String VIDEO_KEY_KEY = "key";
    private static final String VIDEO_SITE_KEY = "site";
    private static final String VIDEO_TYPE_KEY = "type";
    private static final String VIDEO_SITE_YOUTUBE = "YouTube";

    private static final String YOUTUBE_SCHEME = "https";
    private static final String YOUTUBE_AUTHORITY = "youtube.com";
    private static final String YOUTUBE_WATCH_PATH = "watch";
    private static final String YOUTUBE_VIDEO_PARAM = "v";
    private static final String YOUTUBE_IMAGE_AUTHORITY = "img.youtube.com";
    private static final String YOUTUBE_VIDEO_IMAGE_PATH = "vi";
    private static final String YOUTUBE_DEFAULT_IMAGE_PATH = "mqdefault.jpg";

    private static final String REVIEW_ID_KEY = "id";
    private static final String REVIEW_AUTHOR_KEY = "author";
    private static final String REVIEW_CONTENT_KEY = "content";
    private static final String REVIEW_URL_KEY = "url";

    public static List<MovieDetails> getPopularMovieDetails() {
        return getMovieDetails(API_MOVIES_POPULAR);
    }

    public static List<MovieDetails> getTopRatedMovieDetails() {
        return getMovieDetails(API_MOVIES_TOP_RATED);
    }

    private static List<MovieDetails> getMovieDetails(String order) {
        try {
            URL url = new URL(new Uri.Builder()
                    .scheme(API_SCHEME)
                    .authority(API_AUTHORITY)
                    .appendPath(API_VERSION)
                    .appendPath(API_MOVIE_METHOD)
                    .appendPath(order)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                    .build()
                    .toString());
            String json = performHttpRequest(url);
            if (json != null) {
                return parseMovieDetails(json);
            }
        } catch (IOException | JSONException e) {
            // Empty list is returned if exception occurs
        }
        return Collections.emptyList();
    }

    private static List<MovieDetails> parseMovieDetails(String json) throws JSONException {
        List<MovieDetails> movieDetails = new ArrayList<>();
        JSONArray results = new JSONObject(json).getJSONArray(RESULTS_KEY);
        for (int i = 0; i < results.length(); i++) {
            JSONObject movie = results.getJSONObject(i);
            String id = movie.optString(MOVIE_ID_KEY);
            String title = movie.optString(MOVIE_ORIGINAL_TITLE_KEY);
            String overview = movie.optString(MOVIE_OVERVIEW_KEY);
            String releaseDate = movie.optString(MOVIE_RELEASE_DATE_KEY);
            String voteAverage = movie.optString(MOVIE_VOTE_AVERAGE_KEY);
            String posterPath = movie.optString(MOVIE_POSTER_PATH_KEY);
            String posterUrl = new Uri.Builder()
                    .scheme(IMAGE_SCHEME)
                    .authority(IMAGE_AUTHORITY)
                    .appendPath(IMAGE_T)
                    .appendPath(IMAGE_P)
                    .appendPath(IMAGE_SIZE)
                    .appendPath(posterPath.substring(1))
                    .toString();
            movieDetails.add(
                    new MovieDetails(id, title, overview, releaseDate, voteAverage, posterUrl));
        }
        return movieDetails;
    }

    public static List<MovieVideo> getMovieVideos(String movieId) {
        try {
            URL url = new URL(new Uri.Builder()
                    .scheme(API_SCHEME)
                    .authority(API_AUTHORITY)
                    .appendPath(API_VERSION)
                    .appendPath(API_MOVIE_METHOD)
                    .appendPath(movieId)
                    .appendPath(API_VIDEOS_METHOD)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                    .build()
                    .toString());
            String json = performHttpRequest(url);
            if (json != null) {
                return parseMovieVideos(json);
            }
        } catch (IOException | JSONException e) {
            // Empty list is returned if exception occurs
        }
        return Collections.emptyList();
    }

    private static List<MovieVideo> parseMovieVideos(String json) throws JSONException {
        List<MovieVideo> movieVideos = new ArrayList<>();
        JSONArray results = new JSONObject(json).getJSONArray(RESULTS_KEY);
        for (int i = 0; i < results.length(); i++) {
            JSONObject video = results.getJSONObject(i);
            String site = video.optString(VIDEO_SITE_KEY);
            if (!site.equalsIgnoreCase(VIDEO_SITE_YOUTUBE)) continue;
            String id = video.optString(VIDEO_ID_KEY);
            String name = video.optString(VIDEO_NAME_KEY);
            String key = video.optString(VIDEO_KEY_KEY);
            String type = video.optString(VIDEO_TYPE_KEY);
            String url = new Uri.Builder()
                    .scheme(YOUTUBE_SCHEME)
                    .authority(YOUTUBE_AUTHORITY)
                    .appendPath(YOUTUBE_WATCH_PATH)
                    .appendQueryParameter(YOUTUBE_VIDEO_PARAM, key)
                    .toString();
            String thumbnailUrl = new Uri.Builder()
                    .scheme(YOUTUBE_SCHEME)
                    .authority(YOUTUBE_IMAGE_AUTHORITY)
                    .appendPath(YOUTUBE_VIDEO_IMAGE_PATH)
                    .appendPath(key)
                    .appendPath(YOUTUBE_DEFAULT_IMAGE_PATH)
                    .toString();
            movieVideos.add(new MovieVideo(id, name, url, thumbnailUrl, type));
        }
        return movieVideos;
    }

    public static List<MovieReview> getMovieReviews(String movieId) {
        try {
            URL url = new URL(new Uri.Builder()
                    .scheme(API_SCHEME)
                    .authority(API_AUTHORITY)
                    .appendPath(API_VERSION)
                    .appendPath(API_MOVIE_METHOD)
                    .appendPath(movieId)
                    .appendPath(API_REVIEWS_METHOD)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                    .build()
                    .toString());
            String json = performHttpRequest(url);
            if (json != null) {
                return parseMovieReviews(json);
            }
        } catch (IOException | JSONException e) {
            // Empty list is returned if exception occurs
        }
        return Collections.emptyList();
    }

    private static List<MovieReview> parseMovieReviews(String json) throws JSONException {
        List<MovieReview> movieReviews = new ArrayList<>();
        JSONArray reviews = new JSONObject(json).getJSONArray(RESULTS_KEY);
        for (int i = 0; i < reviews.length(); i++) {
            JSONObject review = reviews.getJSONObject(i);
            String id = review.optString(REVIEW_ID_KEY);
            String author = review.optString(REVIEW_AUTHOR_KEY);
            String content = review.optString(REVIEW_CONTENT_KEY);
            String url = review.optString(REVIEW_URL_KEY);
            movieReviews.add(new MovieReview(id, author, content, url));
        }
        return movieReviews;
    }

    private static String performHttpRequest(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            if (urlConnection.getResponseCode() == 200) {
                return result.toString("UTF-8");
            }
            return null;
        } finally {
            urlConnection.disconnect();
        }
    }

}
