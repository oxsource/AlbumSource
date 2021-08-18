package com.pizzk.media.source.core;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MediaSource implements IMediaSource {
    private static final String TAG = "MediaSource";
    private static final int CACHE_CAPACITY = 512;
    private final LruCache<Integer, Media> mCache = new LruCache<>(CACHE_CAPACITY);

    protected ContentResolver mResolver;
    public boolean isAsc;
    protected final Uri mBaseUri;
    public final String mBucketId;
    protected Cursor mCursor;

    public MediaSource(ContentResolver resolver, Uri uri, boolean asc, String bucketId) {
        isAsc = asc;
        mBaseUri = uri;
        mBucketId = bucketId;
        mResolver = resolver;
        // list. After we implement the media list state, we can remove this
        // kind of usage.
        mCache.clear();
    }

    public void close() {
        invalidateCursor();
        mResolver = null;
    }

    protected Uri contentUri(long id) {
        try {
            // does our uri already have an id (single media query)?
            // if so just return it
            long existingId = ContentUris.parseId(mBaseUri);
            if (existingId != id) Log.e(TAG, "id mismatch");
            return mBaseUri;
        } catch (NumberFormatException ex) {
            // otherwise tack on the id
            return ContentUris.withAppendedId(mBaseUri, id);
        }
    }

    public int count() {
        Cursor cursor = getCursor();
        if (cursor == null) return 0;
        synchronized (this) {
            return cursor.getCount();
        }
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    protected Cursor getCursor() {
        synchronized (this) {
            if (null != mCursor) return mCursor;
            if (null == mResolver) return null;
            mCursor = createCursor();
            return mCursor;
        }
    }

    public IMedia get(int i) {
        Media result = mCache.get(i);
        if (null != result) return result;
        Cursor cursor = getCursor();
        if (cursor == null) return null;
        synchronized (this) {
            result = cursor.moveToPosition(i) ? loadFromCursor(cursor) : null;
            mCache.put(i, result);
        }
        return result;
    }

    public boolean remove(IMedia media) {
        if (mResolver.delete(media.uri(), null, null) > 0) {
            invalidateCursor();
            invalidateCache();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeAt(int i) {
        return remove(get(i));
    }

    protected abstract Cursor createCursor();

    protected abstract Media loadFromCursor(Cursor cursor);

    protected abstract long getMediaId(Cursor cursor);

    protected void invalidateCursor() {
        if (mCursor == null) return;
        try {
            if (!mCursor.isClosed()) mCursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while close cursor.", e);
        }
        mCursor = null;
    }

    protected void invalidateCache() {
        mCache.clear();
    }

    private static final Pattern sPathWithId = Pattern.compile("(.*)/\\d+");

    private static String getPathWithoutId(Uri uri) {
        String path = uri.getPath();
        Matcher matcher = sPathWithId.matcher(path);
        return matcher.matches() ? matcher.group(1) : path;
    }

    private boolean isChildUri(Uri uri) {
        // Sometimes, the URI of an media contains a query string with key
        // "bucketId" inorder to restore the media list. However, the query
        // string is not part of the mBaseUri. So, we check only other parts
        // of the two Uri to see if they are the same.
        Uri base = mBaseUri;
        return Objects.equals(base.getScheme(), uri.getScheme())
                && Objects.equals(base.getHost(), uri.getHost())
                && Objects.equals(base.getAuthority(), uri.getAuthority())
                && Objects.equals(base.getPath(), getPathWithoutId(uri));
    }

    public IMedia get(Uri uri) {
        if (!isChildUri(uri)) return null;
        long matchId;
        try {
            matchId = ContentUris.parseId(uri);
        } catch (NumberFormatException ex) {
            Log.i(TAG, "fail to get id in: " + uri, ex);
            return null;
        }
        Cursor cursor = getCursor();
        if (cursor == null) return null;
        synchronized (this) {
            cursor.moveToPosition(-1);
            for (int i = 0; cursor.moveToNext(); ++i) {
                if (getMediaId(cursor) != matchId) continue;
                Media media = mCache.get(i);
                if (media == null) {
                    media = loadFromCursor(cursor);
                    mCache.put(i, media);
                }
                return media;
            }
            return null;
        }
    }

    public int getIndex(IMedia media) {
        return ((Media) media).mIndex;
    }

    // This provides a default sorting order string for subclasses.
    // The list is first sorted by date, then by id. The order can be ascending
    // or descending, depending on the mSort variable.
    // The date is obtained from the "datetaken" column. But if it is null,
    // the "date_modified" column is used instead.
    protected String sortOrder() {
        String ascending = isAsc ? " ASC" : " DESC";
        // Use DATE_TAKEN if it's non-null, otherwise use DATE_MODIFIED.
        // DATE_TAKEN is in milliseconds, but DATE_MODIFIED is in seconds.
        String dateExpr =
                "case ifnull(datetaken,0)" +
                        " when 0 then date_modified*1000" +
                        " else datetaken" +
                        " end";
        // Add id to the end so that we don't ever get random sorting
        // which could happen, I suppose, if the date values are the same.
        return dateExpr + ascending + ", _id" + ascending;
    }
}