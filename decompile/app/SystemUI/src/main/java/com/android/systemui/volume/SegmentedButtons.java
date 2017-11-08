package com.android.systemui.volume;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.systemui.R;
import java.util.Objects;

public class SegmentedButtons extends LinearLayout {
    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", 0);
    private static final Typeface REGULAR = Typeface.create("sans-serif", 0);
    private Callback mCallback;
    private final OnClickListener mClick = new OnClickListener() {
        public void onClick(View v) {
            SegmentedButtons.this.setSelectedValue(v.getTag(), true);
        }
    };
    private final Context mContext;
    protected final LayoutInflater mInflater;
    protected Object mSelectedValue;
    private final SpTexts mSpTexts;

    public interface Callback extends com.android.systemui.volume.Interaction.Callback {
        void onSelected(Object obj, boolean z);
    }

    public SegmentedButtons(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        setOrientation(0);
        this.mSpTexts = new SpTexts(this.mContext);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public Object getSelectedValue() {
        return this.mSelectedValue;
    }

    public void setSelectedValue(Object value, boolean fromClick) {
        if (!Objects.equals(value, this.mSelectedValue)) {
            this.mSelectedValue = value;
            for (int i = 0; i < getChildCount(); i++) {
                TextView c = (TextView) getChildAt(i);
                boolean selected = Objects.equals(this.mSelectedValue, c.getTag());
                c.setSelected(selected);
                setSelectedStyle(c, selected);
            }
            fireOnSelected(fromClick);
        }
    }

    protected void setSelectedStyle(TextView textView, boolean selected) {
        textView.setTypeface(selected ? MEDIUM : REGULAR);
    }

    public Button inflateButton() {
        return (Button) this.mInflater.inflate(R.layout.segmented_button, this, false);
    }

    public void addButton(int labelResId, int contentDescriptionResId, Object value) {
        Button b = inflateButton();
        b.setTag(R.id.label, Integer.valueOf(labelResId));
        b.setText(labelResId);
        b.setContentDescription(getResources().getString(contentDescriptionResId));
        LayoutParams lp = (LayoutParams) b.getLayoutParams();
        if (getChildCount() == 0) {
            lp.rightMargin = 0;
            lp.leftMargin = 0;
        }
        b.setLayoutParams(lp);
        addView(b);
        b.setTag(value);
        b.setOnClickListener(this.mClick);
        Interaction.register(b, new com.android.systemui.volume.Interaction.Callback() {
            public void onInteraction() {
                SegmentedButtons.this.fireInteraction();
            }
        });
        this.mSpTexts.add(b);
    }

    public void updateLocale() {
        for (int i = 0; i < getChildCount(); i++) {
            Button b = (Button) getChildAt(i);
            b.setText(((Integer) b.getTag(R.id.label)).intValue());
        }
    }

    private void fireOnSelected(boolean fromClick) {
        if (this.mCallback != null) {
            this.mCallback.onSelected(this.mSelectedValue, fromClick);
        }
    }

    private void fireInteraction() {
        if (this.mCallback != null) {
            this.mCallback.onInteraction();
        }
    }
}
