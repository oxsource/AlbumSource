package com.pizzk.media.source.impl;

import android.database.Cursor;
import android.database.MergeCursor;
import android.text.TextUtils;

import com.pizzk.media.source.core.MediaSource;

import java.util.List;

public class CombMediaCursor extends MergeCursor {
    private final Cursor[] cursors;
    private final List<? extends MediaSource> sources;
    private Cursor cursor = null;

    public CombMediaCursor(List<? extends MediaSource> sources, Cursor[] cursors) {
        super(cursors);
        this.cursors = cursors;
        this.sources = sources;
    }

    public int counts() {
        return null == cursor ? super.getCount() : cursor.getCount();
    }

    public int moveToBucket(String id, int defaultVal) {
        if (TextUtils.isEmpty(id)) {
            this.cursor = null;
            return 0;
        }
        final int length = null == cursors ? 0 : cursors.length;
        for (int i = 0, x = 0; i < length; i++) {
            Cursor cursor = cursors[i];
            if (TextUtils.equals(sources.get(i).mBucketId, id)) {
                this.cursor = cursor;
                return x;
            }
            x += cursor.getCount();
        }
        return defaultVal;
    }
}
