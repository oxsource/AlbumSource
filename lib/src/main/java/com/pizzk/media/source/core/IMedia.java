package com.pizzk.media.source.core;

import android.net.Uri;

public interface IMedia {
    IMediaSource container();

    long id();

    Uri uri();

    String bucketId();

    int index();

    String title();

    long dateTaken();

    long duration();

    String mimeType();

    int mediaType();
}
