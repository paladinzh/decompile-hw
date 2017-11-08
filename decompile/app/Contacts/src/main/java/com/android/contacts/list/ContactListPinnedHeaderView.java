package com.android.contacts.list;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.contacts.R$styleable;
import com.android.contacts.hap.CommonUtilMethods;
import com.google.android.gms.R;

public class ContactListPinnedHeaderView extends ViewGroup {
    private final int mContactsCountTextColor;
    protected final Context mContext;
    private TextView mCountTextView = null;
    private int mHeaderBackgroundHeight;
    private View mHeaderDivider;
    private final int mHeaderTextColor;
    private final int mHeaderTextIndent;
    private final int mHeaderTextSize;
    private TextView mHeaderTextView;
    private final int mHeaderTopmargin;
    private final int mHeaderUnderlineColor;
    private final int mHeaderUnderlineHeight;
    private final int mPaddingLeft;
    private final int mPaddingRight;

    public ContactListPinnedHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        Resources lres = getResources();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.ContactListItemView);
        this.mHeaderTextIndent = a.getDimensionPixelOffset(23, 0);
        this.mHeaderTextColor = lres.getColor(R.color.people_app_theme_color);
        this.mHeaderTextSize = a.getDimensionPixelSize(26, 12);
        this.mHeaderUnderlineHeight = a.getDimensionPixelSize(28, 1);
        this.mHeaderUnderlineColor = lres.getColor(R.color.people_app_theme_color);
        this.mHeaderBackgroundHeight = a.getDimensionPixelSize(27, 30);
        this.mPaddingLeft = a.getDimensionPixelOffset(9, 0);
        this.mPaddingRight = a.getDimensionPixelOffset(7, 0);
        a.recycle();
        this.mContactsCountTextColor = lres.getColor(R.color.list_item_contacts_count_text_color);
        this.mHeaderTextView = new TextView(this.mContext);
        this.mHeaderTextView.setTextColor(this.mHeaderTextColor);
        this.mHeaderTextView.setTextSize(0, (float) this.mHeaderTextSize);
        this.mHeaderTextView.setTypeface(this.mHeaderTextView.getTypeface(), 1);
        this.mHeaderTextView.setGravity(16);
        this.mHeaderTextView.setAllCaps(true);
        addView(this.mHeaderTextView);
        this.mHeaderDivider = new View(this.mContext);
        this.mHeaderDivider.setBackgroundColor(this.mHeaderUnderlineColor);
        addView(this.mHeaderDivider);
        this.mHeaderTopmargin = Float.valueOf(getResources().getDimension(R.dimen.contact_list_header_top_padding)).intValue();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(0, widthMeasureSpec);
        this.mHeaderTextView.measure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(this.mHeaderBackgroundHeight, 1073741824));
        if (isViewMeasurable(this.mCountTextView)) {
            this.mCountTextView.measure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(this.mHeaderBackgroundHeight, 1073741824));
        }
        setMeasuredDimension(width, (this.mHeaderTopmargin + this.mHeaderBackgroundHeight) + this.mHeaderUnderlineHeight);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        if (CommonUtilMethods.isLayoutRTL()) {
            this.mHeaderTextView.layout(((width - this.mPaddingRight) - this.mHeaderTextView.getMeasuredWidth()) - this.mHeaderTextIndent, this.mHeaderTopmargin, width - this.mPaddingRight, this.mHeaderTopmargin + this.mHeaderBackgroundHeight);
            if (isViewMeasurable(this.mCountTextView)) {
                this.mCountTextView.layout(this.mHeaderTextIndent + this.mPaddingLeft, this.mHeaderTopmargin, (this.mCountTextView.getMeasuredWidth() + this.mHeaderTextIndent) + this.mPaddingLeft, this.mHeaderTopmargin + this.mHeaderBackgroundHeight);
            }
        } else {
            this.mHeaderTextView.layout(this.mHeaderTextIndent + this.mPaddingLeft, this.mHeaderTopmargin, (this.mHeaderTextView.getMeasuredWidth() + this.mHeaderTextIndent) + this.mPaddingLeft, this.mHeaderTopmargin + this.mHeaderBackgroundHeight);
            if (isViewMeasurable(this.mCountTextView)) {
                this.mCountTextView.layout((width - this.mPaddingRight) - this.mCountTextView.getMeasuredWidth(), this.mHeaderTopmargin, width - this.mPaddingRight, this.mHeaderTopmargin + this.mHeaderBackgroundHeight);
            }
        }
        this.mHeaderDivider.layout(this.mPaddingLeft, this.mHeaderTopmargin + this.mHeaderBackgroundHeight, width - this.mPaddingRight, (this.mHeaderTopmargin + this.mHeaderBackgroundHeight) + this.mHeaderUnderlineHeight);
    }

    public void setSectionHeader(String title) {
        if (TextUtils.isEmpty(title)) {
            this.mHeaderTextView.setVisibility(8);
            this.mHeaderDivider.setVisibility(8);
            return;
        }
        this.mHeaderTextView.setText(title);
        this.mHeaderTextView.setVisibility(0);
        this.mHeaderDivider.setVisibility(0);
    }

    public void requestLayout() {
        forceLayout();
    }

    public void setCountView(String count) {
        if (this.mCountTextView == null) {
            this.mCountTextView = new TextView(this.mContext);
            this.mCountTextView.setTextColor(this.mContactsCountTextColor);
            this.mCountTextView.setTextSize(0, getResources().getDimension(R.dimen.contact_count_text_size));
            this.mCountTextView.setGravity(16);
            addView(this.mCountTextView);
        }
        this.mCountTextView.setText(count);
        if (count == null || count.isEmpty()) {
            this.mCountTextView.setVisibility(8);
        } else {
            this.mCountTextView.setVisibility(0);
        }
    }

    private boolean isViewMeasurable(View view) {
        return view != null && view.getVisibility() == 0;
    }
}
