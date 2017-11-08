package com.android.contacts.widget;

import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.BitSet;

public abstract class AbstractExpandableViewAdapter extends BaseAdapter implements WrapperListAdapter {
    private OnItemExpandListener expandListener;
    protected ListAdapter expandWrappedAdapter;
    private View lastOpen;
    private int lastOpenPosition;
    private OnClickListener mPrimaryActionListener;
    private BitSet openItems;
    private final SparseIntArray viewHeights;

    public static class ExpandCollapseAnimation extends Animation {
        private View mAnimatedView;
        private int mEndHeight = this.mAnimatedView.getMeasuredHeight();
        private LayoutParams mLayoutParams;
        private int mType;

        public ExpandCollapseAnimation(View view, int type) {
            this.mAnimatedView = view;
            if (this.mEndHeight == 0) {
                this.mEndHeight = this.mAnimatedView.getContext().getResources().getDimensionPixelSize(R.dimen.detail_sim_line_item_height);
            }
            this.mLayoutParams = (LayoutParams) view.getLayoutParams();
            this.mType = type;
            if (this.mType == 0) {
                this.mLayoutParams.bottomMargin = -this.mEndHeight;
            } else {
                this.mLayoutParams.bottomMargin = 0;
            }
            view.setVisibility(0);
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0f) {
                if (this.mType == 0) {
                    this.mLayoutParams.bottomMargin = (-this.mEndHeight) + ((int) (((float) this.mEndHeight) * interpolatedTime));
                } else {
                    this.mLayoutParams.bottomMargin = -((int) (((float) this.mEndHeight) * interpolatedTime));
                }
                if (HwLog.HWDBG) {
                    HwLog.d("ExpandCollapseAnimation", "anim height " + this.mLayoutParams.bottomMargin);
                }
                this.mAnimatedView.requestLayout();
            } else if (this.mType == 0) {
                this.mLayoutParams.bottomMargin = 0;
                this.mAnimatedView.requestLayout();
            } else {
                this.mLayoutParams.bottomMargin = -this.mEndHeight;
                this.mAnimatedView.setVisibility(8);
                this.mAnimatedView.requestLayout();
            }
        }
    }

    public interface OnItemExpandListener {
        void onExpand(View view, View view2, int i);
    }

    public abstract ArrayList<View> getExpandToggleButton(View view);

    public abstract View getExpandableView(View view);

    private void notifiyExpandListener(View parent, int type, View view, int position) {
        if (this.expandListener != null && type == 0) {
            this.expandListener.onExpand(parent, view, position);
        }
    }

    public View getView(int position, View view, ViewGroup viewGroup) {
        view = this.expandWrappedAdapter.getView(position, view, viewGroup);
        initExpandView(view, position);
        return view;
    }

    public boolean isAnyItemExpanded() {
        return this.lastOpenPosition != -1;
    }

    public void initExpandView(View parent, int position) {
        ArrayList<View> more = getExpandToggleButton(parent);
        View itemToolbar = getExpandableView(parent);
        if (itemToolbar != null) {
            itemToolbar.measure(parent.getWidth(), parent.getHeight());
            initExpandView(parent, more, itemToolbar, position);
            itemToolbar.requestLayout();
        }
    }

    private void initExpandView(final View parent, ArrayList<View> buttons, final View target, final int position) {
        if (target == this.lastOpen && position != this.lastOpenPosition) {
            this.lastOpen = null;
        }
        if (position == this.lastOpenPosition) {
            this.lastOpen = target;
        }
        if (this.viewHeights.get(position, -1) == -1) {
            this.viewHeights.put(position, target.getMeasuredHeight());
            updateExpandable(parent, target, position);
        } else {
            updateExpandable(parent, target, position);
        }
        OnClickListener expandColloseListener = new OnClickListener() {
            public void onClick(View view) {
                int type = 0;
                if (target.getVisibility() == 0) {
                    type = 1;
                }
                if (type == 0) {
                    AbstractExpandableViewAdapter.this.expand(parent, target, position, type);
                } else {
                    AbstractExpandableViewAdapter.this.collapse(target, position);
                }
            }
        };
        for (int i = 0; i < buttons.size(); i++) {
            View view = (View) buttons.get(i);
            if (view != null && isItemCanExpanded(position)) {
                view.setOnClickListener(expandColloseListener);
            } else if (!(view == null || this.mPrimaryActionListener == null)) {
                view.setOnClickListener(this.mPrimaryActionListener);
            }
        }
    }

    protected boolean isItemCanExpanded(int position) {
        return true;
    }

    private void updateExpandable(View parent, View target, int position) {
        LayoutParams params = (LayoutParams) target.getLayoutParams();
        if (this.openItems.get(position)) {
            target.setVisibility(0);
            params.bottomMargin = 0;
            return;
        }
        target.setVisibility(8);
        params.bottomMargin = 0 - this.viewHeights.get(position);
    }

    public static void animateView(View target, int type) {
        Animation anim = new ExpandCollapseAnimation(target, type);
        anim.setDuration(280);
        target.startAnimation(anim);
    }

    public void expand(View parent, View target, int position, int type) {
        this.openItems.set(position, true);
        if (type == 0) {
            if (!(this.lastOpenPosition == -1 || this.lastOpenPosition == position)) {
                if (this.lastOpen != null) {
                    animateView(this.lastOpen, 1);
                    notifiyExpandListener(parent, 1, this.lastOpen, this.lastOpenPosition);
                }
                this.openItems.set(this.lastOpenPosition, false);
            }
            this.lastOpen = target;
            this.lastOpenPosition = position;
        } else if (this.lastOpenPosition == position) {
            this.lastOpenPosition = -1;
        }
        animateView(target, type);
        notifiyExpandListener(parent, type, target, position);
    }

    public void collapse(View target, int position) {
        if (isAnyItemExpanded() && target != null) {
            this.openItems.set(position, false);
            if (this.lastOpenPosition == position) {
                this.lastOpenPosition = -1;
            }
            animateView(target, 1);
        }
    }

    public int getCount() {
        return this.expandWrappedAdapter.getCount();
    }

    public Object getItem(int i) {
        return this.expandWrappedAdapter.getItem(i);
    }

    public long getItemId(int i) {
        return this.expandWrappedAdapter.getItemId(i);
    }

    public int getItemViewType(int i) {
        return this.expandWrappedAdapter.getItemViewType(i);
    }

    public boolean isEmpty() {
        return this.expandWrappedAdapter.isEmpty();
    }
}
