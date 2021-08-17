package com.pizzk.media.source.core;

import android.net.Uri;

import java.util.Map;

public interface IMediaSource {
    Map<String, String> getBucketIds();

    /**
     * Returns the count of image objects.
     *
     * @return the number of images
     */
    int count();

    /**
     * @return true if the count of image objects is zero.
     */
    boolean isEmpty();

    /**
     * Returns the image at the ith position.
     *
     * @param i the position
     * @return the image at the ith position
     */
    IMedia get(int i);

    /**
     * Returns the image with a particular Uri.
     *
     * @param uri
     * @return the image with a particular Uri. null if not found.
     */
    IMedia get(Uri uri);

    /**
     * @param image
     * @return true if the image was removed.
     */
    boolean remove(IMedia image);

    /**
     * Removes the image at the ith position.
     *
     * @param i the position
     */
    boolean removeAt(int i);

    int getIndex(IMedia image);

    /**
     * Closes this list to release resources, no further operation is allowed.
     */
    void close();
}
