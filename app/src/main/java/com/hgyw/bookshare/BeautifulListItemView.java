package com.hgyw.bookshare;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * TODO: document your custom view class.
 */
public class BeautifulListItemView extends RelativeLayout {
    private final TextView titleView;
    private final TextView descriptionView;
    private final ImageView thumbnailView;
    private final TextView moreTextView;

    {
        inflate(getContext(), R.layout.beautiful_list_item, this);
        this.titleView = (TextView)findViewById(R.id.supplierName);
        this.descriptionView = (TextView)findViewById(R.id.description);
        this.thumbnailView = (ImageView)findViewById(R.id.userThumbnail);
        this.moreTextView = (TextView) findViewById(R.id.button);

    }

    public BeautifulListItemView(Context context) {
        super(context);
    }

    public BeautifulListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView findTitleView() {
        return titleView;
    }

    public TextView findDescriptionView() {
        return descriptionView;
    }

    public ImageView findThumbnailView() {
        return thumbnailView;
    }

    public TextView findMoreTextView() {
        return moreTextView;
    }
}
