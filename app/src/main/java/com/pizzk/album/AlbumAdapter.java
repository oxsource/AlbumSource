package com.pizzk.album;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.pizzk.media.source.core.IMedia;
import com.pizzk.media.source.core.MediaSource;

class AlbumAdapter extends BaseAdapter {
    private Context context;
    private int mItemSize;
    private GridView.LayoutParams mItemLayoutParams;
    private MediaSource source;

    public AlbumAdapter() {
        mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void swapSource(MediaSource source) {
        this.source = source;
        notifyDataSetChanged();
    }

    /**
     * 重置每个Column的Size
     */
    public void setItemSize(int columnWidth) {
        if (mItemSize == columnWidth) {
            return;
        }
        mItemSize = columnWidth;
        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return null == source ? 0 : source.count();
    }

    @Override
    public IMedia getItem(int i) {
        return null == source ? null : source.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Object tag = null == view ? null : view.getTag();
        ViewHolder holder = tag instanceof ViewHolder ? (ViewHolder) tag : null;
        if (null == holder) {
            if (null == context) return view;
            view = LayoutInflater.from(context).inflate(R.layout.photo_list_item_image, viewGroup, false);
            holder = new ViewHolder(view);
        }
        holder.bindData(getItem(i), mItemSize);
        /*Fixed View Size */
        GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
        if (lp.height != mItemSize) {
            view.setLayoutParams(mItemLayoutParams);
        }
        return view;
    }

    private static class ViewHolder {
        ImageView image;
        TextView durationView;
        TextView indicator;
        View mask;
        View videoMask;

        private ViewHolder(View view) {
            image = view.findViewById(R.id.image);
            indicator = view.findViewById(R.id.checkmark);
            durationView = view.findViewById(R.id.tv_duration);
            mask = view.findViewById(R.id.mask);
            videoMask = view.findViewById(R.id.video_mask);
            view.setTag(this);
        }

        @SuppressLint("SetTextI18n")
        void bindData(final IMedia media, int size) {
            if (media == null) return;
            indicator.setVisibility(View.GONE);
            if (MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE != media.mediaType()) {
                indicator.setVisibility(View.GONE);
                mask.setVisibility(View.GONE);
                durationView.setVisibility(View.VISIBLE);
                durationView.setText(media.duration() / 1000 + "");
                videoMask.setVisibility(View.VISIBLE);
            } else {
                durationView.setVisibility(View.GONE);
                videoMask.setVisibility(View.GONE);
                durationView.setText("");
            }
            RequestOptions options = new RequestOptions();
            if (size > 0) {
                options = options.centerCrop().override(size, size).placeholder(android.R.drawable.gallery_thumb);
            }
            Glide.with(this.image).load(media.uri()).apply(options).into(image);
        }
    }
}
