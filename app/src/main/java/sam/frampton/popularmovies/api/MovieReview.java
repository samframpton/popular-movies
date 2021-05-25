package sam.frampton.popularmovies.api;

public final class MovieReview {

    private final String mId;
    private final String mAuthor;
    private final String mContent;
    private final String mUrl;

    public MovieReview(String id,
                String author,
                String content,
                String url) {
        mId = id;
        mAuthor = author;
        mContent = content;
        mUrl = url;
    }

    public String getId() {
        return mId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public String getUrl() {
        return mUrl;
    }

}
