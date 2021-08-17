package com.pizzk.media.source.core;

import android.net.Uri;

public class Media implements IMedia {
    protected final MediaSource mContainer;
    protected final long mId;
    protected final Uri mUri;
    protected final String mBucketId;
    protected final int mIndex;
    private final String mTitle;
    private final long mDateTaken;
    private final long mDuration;
    protected String mMimeType;
    protected int mMediaType;

    public Media(MediaSource container, long id, String bucketId,
                 int index, String name, long dateTaken,
                 long duration, String mimeType, int mediaType) {
        mContainer = container;
        mId = id;
        mUri = container.contentUri(id);
        mBucketId = bucketId;
        mIndex = index;
        mTitle = name;
        mDateTaken = dateTaken;
        mDuration = duration;
        mMimeType = mimeType;
        mMediaType = mediaType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Media)) return false;
        return mUri.equals(((Media) other).mUri);
    }

    @Override
    public int hashCode() {
        return mUri.hashCode();
    }

    @Override
    public IMediaSource container() {
        return mContainer;
    }

    @Override
    public long id() {
        return mId;
    }

    @Override
    public Uri uri() {
        return mUri;
    }

    @Override
    public String bucketId() {
        return mBucketId;
    }

    @Override
    public long index() {
        return mIndex;
    }

    @Override
    public String title() {
        return mTitle;
    }

    @Override
    public long dateTaken() {
        return mDateTaken;
    }

    @Override
    public long duration() {
        return mDuration;
    }

    @Override
    public String mimeType() {
        return mMimeType;
    }

    @Override
    public int mediaType() {
        return mMediaType;
    }
}
