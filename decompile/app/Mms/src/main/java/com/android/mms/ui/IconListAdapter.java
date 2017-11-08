package com.android.mms.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.mms.ui.EmuiMenuText;
import java.util.List;

public class IconListAdapter extends ArrayAdapter<IconListItem> {
    private static int mResource;
    private int mBackgroundColor;
    protected LayoutInflater mInflater;
    private int mTextColor;
    private ViewHolder mViewHolder;

    public static class IconListItem {
        private final String mContentDesCription;
        private final int mResource;
        private final String mTitle;

        public IconListItem(String title, String contentDesCription, int resource) {
            this.mResource = resource;
            this.mTitle = title;
            this.mContentDesCription = contentDesCription;
        }

        public String getContentDesCription() {
            return this.mContentDesCription;
        }

        public String getTitle() {
            return this.mTitle;
        }

        public int getResource() {
            return this.mResource;
        }
    }

    static class ViewHolder {
        private TextView mEmojiTextView;
        private ImageView mImageView;
        private EmuiMenuText mTextView;
        private View mView;

        public ViewHolder(View view) {
            this.mView = view;
        }

        public EmuiMenuText getTextView() {
            if (this.mTextView == null) {
                this.mTextView = (EmuiMenuText) this.mView.findViewById(R.id.text1);
            }
            return this.mTextView;
        }

        public ImageView getImageView() {
            if (this.mImageView == null) {
                this.mImageView = (ImageView) this.mView.findViewById(R.id.icon);
            }
            return this.mImageView;
        }

        public TextView getEmojiTextView() {
            if (this.mEmojiTextView == null) {
                this.mEmojiTextView = (TextView) this.mView.findViewById(R.id.iconEmoji);
            }
            return this.mEmojiTextView;
        }
    }

    private static void setResource(int resourceId) {
        mResource = resourceId;
    }

    public IconListAdapter(Context context, int resourceId, List<IconListItem> date) {
        super(context, resourceId, date);
        setResource(resourceId);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mBackgroundColor = context.getResources().getColor(R.color.attach_panel_item_color);
        this.mTextColor = context.getResources().getColor(R.color.mms_icon_list_item_text_color);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = this.mInflater.inflate(mResource, parent, false);
            view.setBackgroundColor(this.mBackgroundColor);
            this.mViewHolder = new ViewHolder(view);
            view.setTag(this.mViewHolder);
        } else {
            view = convertView;
            this.mViewHolder = (ViewHolder) convertView.getTag();
        }
        IconListItem iconListItem = (IconListItem) getItem(position);
        if (mResource == R.layout.icon_list_item || mResource == R.layout.layout_selector_list) {
            EmuiMenuText text = this.mViewHolder.getTextView();
            if (!(text == null || iconListItem == null)) {
                text.setText(iconListItem.getTitle());
                text.setTextColor(this.mTextColor);
            }
        }
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            TextView text2 = this.mViewHolder.getEmojiTextView();
            if (!(text2 == null || iconListItem == null)) {
                text2.setText(iconListItem.getTitle());
                text2.setContentDescription(iconListItem.getContentDesCription());
            }
        }
        ImageView image = this.mViewHolder.getImageView();
        if (!(image == null || iconListItem == null)) {
            image.setImageResource(iconListItem.getResource());
            image.setContentDescription(iconListItem.getContentDesCription());
        }
        return view;
    }
}
