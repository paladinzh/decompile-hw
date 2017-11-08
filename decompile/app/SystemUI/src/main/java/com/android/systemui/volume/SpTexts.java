package com.android.systemui.volume;

import android.content.Context;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.TextView;

public class SpTexts {
    private final Context mContext;
    private final ArrayMap<TextView, Integer> mTexts = new ArrayMap();
    private final Runnable mUpdateAll = new Runnable() {
        public void run() {
            for (int i = 0; i < SpTexts.this.mTexts.size(); i++) {
                SpTexts.this.setTextSizeH((TextView) SpTexts.this.mTexts.keyAt(i), ((Integer) SpTexts.this.mTexts.valueAt(i)).intValue());
            }
        }
    };

    public SpTexts(Context context) {
        this.mContext = context;
    }

    public int add(final TextView text) {
        if (text == null) {
            return 0;
        }
        Resources res = this.mContext.getResources();
        float fontScale = res.getConfiguration().fontScale;
        final int sp = (int) ((text.getTextSize() / fontScale) / res.getDisplayMetrics().density);
        this.mTexts.put(text, Integer.valueOf(sp));
        text.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            public void onViewDetachedFromWindow(View v) {
            }

            public void onViewAttachedToWindow(View v) {
                SpTexts.this.setTextSizeH(text, sp);
            }
        });
        return sp;
    }

    public void update() {
        if (!this.mTexts.isEmpty()) {
            ((TextView) this.mTexts.keyAt(0)).post(this.mUpdateAll);
        }
    }

    private void setTextSizeH(TextView text, int sp) {
        text.setTextSize(2, (float) sp);
    }
}
