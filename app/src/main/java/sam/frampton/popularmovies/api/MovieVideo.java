package sam.frampton.popularmovies.api;

public final class MovieVideo {

    private final String mId;
    private final String mName;
    private final String mUrl;
    private final String mThumbnailUrl;
    private final String mType;

    public MovieVideo(String id,
               String name,
               String url,
               String thumbnailUrl,
               String type) {
        mId = id;
        mName = name;
        mUrl = url;
        mThumbnailUrl = thumbnailUrl;
        mType = type;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public String getType() {
        return mType;
    }

}
