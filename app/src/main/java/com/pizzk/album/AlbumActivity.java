package com.pizzk.album;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.pizzk.media.source.core.MediaSource;
import com.pizzk.media.source.impl.CombMediaSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumActivity extends AppCompatActivity {
    private TextView tvBucket;
    private GridView mGridView;
    private final AlbumAdapter adapter = new AlbumAdapter();
    //
    private CombMediaSource mediaSource;
    private final List<Map.Entry<String, String>> buckets = new ArrayList<>();
    private String wholeBucketText = "";
    //
    private int index = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        adapter.setContext(getBaseContext());
        //
        tvBucket = findViewById(R.id.tvBucket);
        Button btNext = findViewById(R.id.btNext);
        btNext.setOnClickListener(v -> {
            index = (index + 1) % buckets.size();
            updateSelectBucket();
        });
        Button btPrevious = findViewById(R.id.btPrevious);
        btPrevious.setOnClickListener(v -> {
            index = (index - 1 + buckets.size()) % buckets.size();
            updateSelectBucket();
        });
        //
        mGridView = findViewById(R.id.vGrid);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {
                if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL) {
                    Glide.with(AlbumActivity.this).resumeRequests();
                } else {
                    Glide.with(AlbumActivity.this).pauseRequests();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mGridView.setAdapter(adapter);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int width = mGridView.getWidth();
                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                final int numCount = width / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
                adapter.setItemSize(columnWidth);
                mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        //create media source
        new Thread(() -> {
            mediaSource = new CombMediaSource(getBaseContext());
            //active other children source query
            int count = mediaSource.count();
            Log.d(MediaSource.TAG, "CombMediaSource prepared, total count=" + count);
            runOnUiThread(() -> {
                fillBuckets(mediaSource.getBucketIds());
                adapter.setSource(mediaSource);
                updateSelectBucket();
            });
        }).start();
    }

    private void fillBuckets(Map<String, String> bucketIds) {
        buckets.clear();
        bucketIds = null == bucketIds ? new HashMap<>(0) : bucketIds;
        Map<String, String> whole = new HashMap<>(1);
        whole.put("", "全部");
        buckets.addAll(whole.entrySet());
        buckets.addAll(bucketIds.entrySet());
        String symbol = " | ";
        StringBuilder sbf = new StringBuilder();
        for (Map.Entry<String, String> e : buckets) {
            sbf.append(e.getValue()).append(symbol);
        }
        wholeBucketText = sbf.subSequence(0, sbf.length() - symbol.length()).toString();
    }

    private void updateSelectBucket() {
        try {
            Map.Entry<String, String> e = buckets.get(index);
            String bucketId = e.getKey();
            String bucketName = e.getValue();
            int sIndex = wholeBucketText.indexOf(bucketName);
            SpannableString s = new SpannableString(wholeBucketText);
            s.setSpan(new ForegroundColorSpan(Color.RED), sIndex, sIndex + bucketName.length(), SpannedString.SPAN_INCLUSIVE_INCLUSIVE);
            tvBucket.setText(s);
            mediaSource.use(bucketId);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaSource.close();
    }
}
