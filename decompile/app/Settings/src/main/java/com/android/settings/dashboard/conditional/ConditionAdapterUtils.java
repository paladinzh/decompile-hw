package com.android.settings.dashboard.conditional;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.dashboard.DashboardAdapter;
import com.android.settings.dashboard.DashboardAdapter.DashboardItemHolder;

public class ConditionAdapterUtils {
    public static void addDismiss(final RecyclerView recyclerView) {
        new ItemTouchHelper(new SimpleCallback(0, 48) {
            public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
                return true;
            }

            public int getSwipeDirs(RecyclerView recyclerView, ViewHolder viewHolder) {
                return viewHolder.getItemViewType() == 2130968680 ? super.getSwipeDirs(recyclerView, viewHolder) : 0;
            }

            public void onSwiped(ViewHolder viewHolder, int direction) {
                Object item = ((DashboardAdapter) recyclerView.getAdapter()).getItem(viewHolder.getItemId());
                if (item instanceof Condition) {
                    ((Condition) item).silence();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    public static void bindViews(final Condition condition, DashboardItemHolder view, boolean isExpanded, OnClickListener onClickListener, OnClickListener onExpandListener) {
        View card = view.itemView.findViewById(2131886388);
        card.setTag(condition);
        card.setOnClickListener(onClickListener);
        view.icon.setImageIcon(condition.getIcon());
        view.title.setText(condition.getTitle());
        ImageView expand = (ImageView) view.itemView.findViewById(2131886391);
        expand.setTag(condition);
        expand.setImageResource(isExpanded ? 2130838238 : 2130838239);
        expand.setContentDescription(expand.getContext().getString(isExpanded ? 2131627115 : 2131627114));
        expand.setOnClickListener(onExpandListener);
        View detailGroup = view.itemView.findViewById(2131886392);
        CharSequence[] actions = condition.getActions();
        if (isExpanded != (detailGroup.getVisibility() == 0)) {
            animateChange(view.itemView, view.itemView.findViewById(2131886388), detailGroup, isExpanded, actions.length > 0);
            setViewVisibility(view.itemView, 16908304, isExpanded);
        }
        if (isExpanded) {
            view.summary.setText(condition.getSummary());
            int i = 0;
            while (i < 2) {
                TextView button = (TextView) detailGroup.findViewById(i == 0 ? 2131886394 : 2131886395);
                if (actions.length > i) {
                    button.setVisibility(0);
                    button.setText(actions[i]);
                    final int index = i;
                    button.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            MetricsLogger.action(v.getContext(), 376, condition.getMetricsConstant());
                            condition.onActionClick(index);
                        }
                    });
                } else {
                    button.setVisibility(8);
                }
                i++;
            }
        }
    }

    private static void animateChange(View view, final View content, final View detailGroup, final boolean visible, boolean hasButtons) {
        int i;
        setViewVisibility(detailGroup, 2131886538, hasButtons);
        setViewVisibility(detailGroup, 2131886393, hasButtons);
        final int beforeBottom = content.getBottom();
        if (visible) {
            i = -2;
        } else {
            i = 0;
        }
        setHeight(detailGroup, i);
        detailGroup.setVisibility(0);
        view.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int afterBottom = content.getBottom();
                v.removeOnLayoutChangeListener(this);
                ObjectAnimator animator = ObjectAnimator.ofInt(content, "bottom", new int[]{beforeBottom, afterBottom});
                animator.setDuration(250);
                final boolean z = visible;
                final View view = detailGroup;
                animator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        if (!z) {
                            view.setVisibility(8);
                        }
                    }
                });
                animator.start();
            }
        });
    }

    private static void setHeight(View detailGroup, int height) {
        LayoutParams params = detailGroup.getLayoutParams();
        params.height = height;
        detailGroup.setLayoutParams(params);
    }

    private static void setViewVisibility(View containerView, int viewId, boolean visible) {
        View view = containerView.findViewById(viewId);
        if (view != null) {
            view.setVisibility(visible ? 0 : 8);
        }
    }
}
