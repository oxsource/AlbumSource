package com.pizzk.media.source.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.pizzk.media.source.core.IMedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CombMediaSource extends SimpleMediaSource {
    private final Map<String, String> buckets;
    private final List<SimpleMediaSource> sources = new ArrayList<>();
    private int offset = 0;

    public CombMediaSource(Context context) {
        super(context.getContentResolver(), MediaStore.Files.getContentUri("external"), false, null, new SimpleMediaFactory());
        ContentResolver resolver = context.getContentResolver();
        buckets = super.getBucketIds();
        for (Map.Entry<String, String> e : buckets.entrySet()) {
            String bucketId = e.getKey();
            sources.add(new SimpleMediaSource(resolver, mBaseUri, true, bucketId, new SimpleMediaFactory()));
        }
    }

    @Override
    public Map<String, String> getBucketIds() {
        return buckets;
    }

    @Override
    public int count() {
        Object obj = getCursor();
        if (!(obj instanceof CombMediaCursor)) return super.count();
        CombMediaCursor cursor = (CombMediaCursor) obj;
        return cursor.counts();
    }

    @Override
    protected Cursor createCursor() {
        Cursor[] cursors = new Cursor[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            cursors[i] = sources.get(i).createCursor();
        }
        return new CombMediaCursor(sources, cursors);
    }

    @Override
    public void close() {
        super.close();
        buckets.clear();
        for (SimpleMediaSource e : sources) {
            e.close();
        }
        sources.clear();
    }

    @Override
    public IMedia get(int i) {
        return super.get(i + offset);
    }

    /**
     * 通过id指定当前MediaSource并计算偏移量
     *
     * @param id bucket id
     */
    public void use(String id) {
        Object obj = getCursor();
        if (!(obj instanceof CombMediaCursor)) return;
        CombMediaCursor cursor = (CombMediaCursor) obj;
        offset = cursor.moveToBucket(id, offset);
    }
}
