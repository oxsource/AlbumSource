package com.pizzk.media.source.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.pizzk.media.source.core.MediaSource;

import java.util.HashMap;
import java.util.Map;

public class CombMediaSource extends SimpleMediaSource {
    private final Map<String, String> buckets;
    private final Map<String, SimpleMediaSource> sources = new HashMap<>();

    public CombMediaSource(Context context) {
        super(context.getContentResolver(), MediaStore.Files.getContentUri("external"), false, null);
        ContentResolver resolver = context.getContentResolver();
        buckets = super.getBucketIds();
        for (Map.Entry<String, String> e : buckets.entrySet()) {
            String bucketId = e.getKey();
            sources.put(bucketId, new SimpleMediaSource(resolver, mBaseUri, false, bucketId));
        }
    }

    @Override
    public Map<String, String> getBucketIds() {
        return buckets;
    }

    @Override
    protected Cursor createCursor() {
        Cursor[] cursors = new Cursor[sources.size()];
        int index = 0;
        for (Map.Entry<String, SimpleMediaSource> e : sources.entrySet()) {
            cursors[index] = e.getValue().createCursor();
            index += 1;
        }
        return new MergeCursor(cursors);
    }

    @Override
    public void close() {
        super.close();
        buckets.clear();
        for (Map.Entry<String, SimpleMediaSource> e : sources.entrySet()) {
            e.getValue().close();
        }
        sources.clear();
    }

    /**
     * 通过id获取指定的MediaSource
     *
     * @param id bucket id
     * @return MediaSource
     */
    public MediaSource bucket(String id) {
        return TextUtils.isEmpty(id) ? this : sources.get(id);
    }
}
