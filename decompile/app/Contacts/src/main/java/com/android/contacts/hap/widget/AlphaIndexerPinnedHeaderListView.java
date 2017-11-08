package com.android.contacts.hap.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.list.ContactEntryListFragment.OnOverLayActionListener;
import com.android.contacts.widget.PinnedHeaderListView;

public class AlphaIndexerPinnedHeaderListView extends PinnedHeaderListView {
    private boolean includeStar = false;
    private AlphaScroller mAlphaScroller;
    private Context mContext;

    public void setIncludeStar(boolean includeStar) {
        this.includeStar = includeStar;
    }

    public AlphaIndexerPinnedHeaderListView(Context context) {
        super(context);
        this.mContext = context;
        this.mIsAlphaIndexerEnabled = true;
        setFastScrollEnabled(true);
    }

    public AlphaIndexerPinnedHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mIsAlphaIndexerEnabled = true;
        setFastScrollEnabled(true);
    }

    private AlphaScroller getScroller() {
        if (this.mAlphaScroller == null) {
            Object fieldValue = CommonUtilMethods.getFastScrollerFieldValueFromListView(this.mContext, this);
            if (fieldValue != null && (fieldValue instanceof AlphaScroller)) {
                this.mAlphaScroller = (AlphaScroller) fieldValue;
            }
        }
        return this.mAlphaScroller;
    }

    public AlphaIndexerPinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.mIsAlphaIndexerEnabled = true;
        setFastScrollEnabled(true);
    }

    public void setFastScrollEnabled(boolean aIsFastScrollerEnabled) {
        Object fieldValue = CommonUtilMethods.getFastScrollerFieldValueFromListView(this.mContext, this);
        if (!aIsFastScrollerEnabled) {
            try {
                CommonUtilMethods.setFastScrollerFieldValueInListView(this.mContext, null, this);
            } catch (Exception e) {
            }
        } else if (fieldValue == null) {
            scroller = new AlphaScroller(getContext(), this, this.includeStar);
            CommonUtilMethods.setFastScrollerFieldValueInListView(this.mContext, scroller, this);
            scroller.remove();
        } else if (fieldValue instanceof AlphaScroller) {
            scroller = (AlphaScroller) fieldValue;
            scroller.setIncludeStar(this.includeStar);
            scroller.init(getContext());
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 0) {
            CommonUtilMethods.hideSoftKeyboard(this.mContext, this);
        }
        return super.onTouchEvent(event);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    public void populateIndexerArray(Object[] aSections) {
        AlphaScroller fieldValue = CommonUtilMethods.getFastScrollerFieldValueFromListView(this.mContext, this);
        if (fieldValue != null && (fieldValue instanceof AlphaScroller)) {
            fieldValue.populateIndexerArray(aSections);
        }
    }

    public void setOverLayIndexerListener(OnOverLayActionListener listener) {
        Object fieldValue = CommonUtilMethods.getFastScrollerFieldValueFromListView(this.mContext, this);
        if (fieldValue != null) {
            try {
                if (fieldValue instanceof AlphaScroller) {
                    ((AlphaScroller) fieldValue).setOverLayIndexerListener(listener);
                }
            } catch (Exception e) {
            }
        }
    }

    public void setOverLayIndexer(int index) {
        Object fieldValue = CommonUtilMethods.getFastScrollerFieldValueFromListView(this.mContext, this);
        if (fieldValue != null) {
            try {
                if (fieldValue instanceof AlphaScroller) {
                    ((AlphaScroller) fieldValue).setOverLayIndexer(index);
                }
            } catch (Exception e) {
            }
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        AlphaScroller fieldValue = CommonUtilMethods.getFastScrollerFieldValueFromListView(this.mContext, this);
        if (fieldValue != null && (fieldValue instanceof AlphaScroller)) {
            AlphaScroller alpha = fieldValue;
            if (alpha.needAnimation()) {
                if (!alpha.isAnimationFinished()) {
                    postInvalidateDelayed(5);
                }
                alpha.drawTextWithAnimation(canvas);
                return;
            }
            alpha.draw(canvas);
        }
    }

    public boolean isPointInsideAlphaScroller(float x, float y) {
        if (getScroller() == null) {
            return true;
        }
        return getScroller().isPointInside(x, y);
    }
}
