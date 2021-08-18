package com.pizzk.media.source.core;

import android.database.Cursor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class MediaFactory {
    private final HashMap<Integer, Media> mLruMap;
    private final List<Media> recycles = new LinkedList<>();

    public MediaFactory(int capacity) {
        mLruMap = new LinkedHashMap<Integer, Media>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Media> eldest) {
                if (size() <= capacity) return false;
                if (recycles.size() < capacity) {
                    recycles.add(eldest.getValue());
                }
                return true;
            }
        };
    }

    public Media create(MediaSource source, Cursor cursor) {
        return recycles.isEmpty() ? new Media() : recycles.remove(0);
    }

    public void put(Integer key, Media value) {
        mLruMap.put(key, value);
    }

    public Media get(Integer key) {
        return mLruMap.get(key);
    }

    public void clear() {
        mLruMap.clear();
        recycles.clear();
    }
}
