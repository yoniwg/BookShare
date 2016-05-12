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
    private TextView title;
    private TextView description;
    private ImageView thumbnail;
    private ImageView icon;
    {
        inflate(getContext(), R.layout.beautiful_list_item, this);
        this.title = (TextView)findViewById(R.id.title);
        this.description = (TextView)findViewById(R.id.description);
        this.thumbnail = (ImageView)findViewById(R.id.thumbnail);
        this.icon = (ImageView)findViewById(R.id.icon);

    }

    public BeautifulListItemView(Context context) {
        super(context);
    }

    public BeautifulListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getDescription() {
        return description;
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }

    public ImageView getIcon() {
        return icon;
    }
}
