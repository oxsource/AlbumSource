package com.pizzk.media.source.impl;

import android.database.Cursor;

import com.pizzk.media.source.core.Media;
import com.pizzk.media.source.core.MediaFactory;
import com.pizzk.media.source.core.MediaSource;

public class SimpleMediaFactory extends MediaFactory {
    public SimpleMediaFactory(int capacity) {
        super(capacity);
    }

    public SimpleMediaFactory() {
        this(512);
    }

    @Override
    public Media create(MediaSource source, Cursor cursor) {
        Media value = super.create(source, cursor);
        long id = cursor.getLong(SimpleMediaSource.INDEX_ID);
        String bucketId = cursor.getString(SimpleMediaSource.INDEX_BUCKET_ID);
        String name = cursor.getString(SimpleMediaSource.INDEX_DISPLAY_NAME);
        long date = cursor.getLong(SimpleMediaSource.INDEX_DATE_ADDED);
        date = date == 0 ? cursor.getLong(SimpleMediaSource.INDEX_DATE_ADDED) * 1000 : date;
        long duration = cursor.getLong(SimpleMediaSource.INDEX_DURATION);
        String mineType = cursor.getString(SimpleMediaSource.INDEX_MIME_TYPE);
        int index = cursor.getPosition();
        int mediaType = cursor.getInt(SimpleMediaSource.INDEX_MEDIA_TYPE);
        return value.of(source, id, bucketId, index, name, date, duration, mineType, mediaType);
    }
}
