package com.pizzk.media.source.impl;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.pizzk.media.source.core.MediaFactory;
import com.pizzk.media.source.core.MediaSource;

import java.util.HashMap;
import java.util.Map;

public class SimpleMediaSource extends MediaSource {
    private final String[] whereArgs;

    public SimpleMediaSource(ContentResolver resolver, Uri uri, boolean asc, String bucketId, MediaFactory factory) {
        super(resolver, uri, asc, bucketId, factory);
        whereArgs = new String[2 + (TextUtils.isEmpty(mBucketId) ? 0 : 1)];
        whereArgs[0] = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "";
        whereArgs[1] = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + "";
        if (whereArgs.length > 2) {
            whereArgs[2] = mBucketId;
        }
    }

    private static final String WHERE_CLAUSE = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + " in (?, ?)) AND " + MediaStore.MediaColumns.SIZE + " > 0";
    private static final String WHERE_CLAUSE_WITH_BUCKET_ID = WHERE_CLAUSE + " AND " + MediaStore.Images.Media.BUCKET_ID + " = ?";

    public Map<String, String> getBucketIds() {
        Map<String, String> buckets = new HashMap<>();
        if (!TextUtils.isEmpty(mBucketId)) return buckets;
        Uri uri = mBaseUri.buildUpon().appendQueryParameter("distinct", "true").build();
        String[] projects = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        try (Cursor cursor = MediaStore.Images.Media.query(mResolver, uri, projects, whereClause(), whereClauseArgs(), null)) {
            while (cursor.moveToNext()) {
                buckets.put(cursor.getString(1), cursor.getString(0));
            }
            return buckets;
        } catch (Exception e) {
            return buckets;
        }
    }

    protected String whereClause() {
        return TextUtils.isEmpty(mBucketId) ? WHERE_CLAUSE : WHERE_CLAUSE_WITH_BUCKET_ID;
    }

    protected String[] whereClauseArgs() {
        return whereArgs;
    }

    @Override
    protected Cursor createCursor() {
        return MediaStore.Images.Media.query(mResolver, mBaseUri, PROJECTION, whereClause(), whereClauseArgs(), sortOrder());
    }

    private static final String[] PROJECTION = new String[]{
            MediaStore.MediaColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.MEDIA_TYPE};

    public static final int INDEX_ID = 0;
    public static final int INDEX_BUCKET_ID = 1;
    public static final int INDEX_DISPLAY_NAME = 2;
    public static final int INDEX_DATE_ADDED = 3;
    public static final int INDEX_DURATION = 4;
    public static final int INDEX_MIME_TYPE = 5;
    public static final int INDEX_MEDIA_TYPE = 6;

    @Override
    protected long getMediaId(Cursor cursor) {
        return cursor.getLong(INDEX_ID);
    }
}