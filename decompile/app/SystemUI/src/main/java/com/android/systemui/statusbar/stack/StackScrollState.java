package com.android.systemui.statusbar.stack;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.WeakHashMap;

public class StackScrollState {
    private final int mClearAllTopPadding;
    private final ViewGroup mHostView;
    private WeakHashMap<ExpandableView, StackViewState> mStateMap = new WeakHashMap();

    public StackScrollState(ViewGroup hostView) {
        this.mHostView = hostView;
        this.mClearAllTopPadding = hostView.getContext().getResources().getDimensionPixelSize(R.dimen.clear_all_padding_top);
    }

    public ViewGroup getHostView() {
        return this.mHostView;
    }

    public void resetViewStates() {
        int numChildren = this.mHostView.getChildCount();
        for (int i = 0; i < numChildren; i++) {
            ExpandableView child = (ExpandableView) this.mHostView.getChildAt(i);
            resetViewState(child);
            if (child instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow row = (ExpandableNotificationRow) child;
                List<ExpandableNotificationRow> children = row.getNotificationChildren();
                if (row.isSummaryWithChildren() && children != null) {
                    for (ExpandableNotificationRow childRow : children) {
                        resetViewState(childRow);
                    }
                }
            }
        }
    }

    private void resetViewState(ExpandableView view) {
        boolean z;
        StackViewState viewState = (StackViewState) this.mStateMap.get(view);
        if (viewState == null) {
            viewState = new StackViewState();
            this.mStateMap.put(view, viewState);
        }
        viewState.height = view.getIntrinsicHeight();
        if (view.getVisibility() == 8) {
            z = true;
        } else {
            z = false;
        }
        viewState.gone = z;
        viewState.alpha = 1.0f;
        viewState.shadowAlpha = 1.0f;
        viewState.notGoneIndex = -1;
        viewState.hidden = false;
    }

    public StackViewState getViewStateForView(View requestedView) {
        return (StackViewState) this.mStateMap.get(requestedView);
    }

    public void removeViewStateForView(View child) {
        this.mStateMap.remove(child);
    }

    public void apply() {
        int numChildren = this.mHostView.getChildCount();
        for (int i = 0; i < numChildren; i++) {
            ExpandableView child = (ExpandableView) this.mHostView.getChildAt(i);
            StackViewState state = (StackViewState) this.mStateMap.get(child);
            if (applyState(child, state)) {
                boolean visible;
                boolean z;
                if (child instanceof DismissView) {
                    DismissView dismissView = (DismissView) child;
                    if (state.clipTopAmount < this.mClearAllTopPadding) {
                        visible = true;
                    } else {
                        visible = false;
                    }
                    if (!visible || dismissView.willBeGone()) {
                        z = false;
                    } else {
                        z = true;
                    }
                    dismissView.performVisibilityAnimation(z);
                } else if (child instanceof EmptyShadeView) {
                    EmptyShadeView emptyShadeView = (EmptyShadeView) child;
                    if (state.clipTopAmount <= 0) {
                        visible = true;
                    } else {
                        visible = false;
                    }
                    if (!visible || emptyShadeView.willBeGone()) {
                        z = false;
                    } else {
                        z = true;
                    }
                    emptyShadeView.performVisibilityAnimation(z);
                }
            }
        }
    }

    public boolean applyState(ExpandableView view, StackViewState state) {
        if (state == null) {
            Log.wtf("StackScrollStateNoSuchChild", "No child state was found when applying this state to the hostView");
            return false;
        } else if (state.gone) {
            return false;
        } else {
            applyViewState(view, state);
            int height = view.getActualHeight();
            int newHeight = state.height;
            if (height != newHeight) {
                view.setActualHeight(newHeight, false);
            }
            float shadowAlpha = view.getShadowAlpha();
            float newShadowAlpha = state.shadowAlpha;
            if (shadowAlpha != newShadowAlpha) {
                view.setShadowAlpha(newShadowAlpha);
            }
            view.setDimmed(state.dimmed, false);
            view.setHideSensitive(state.hideSensitive, false, 0, 0);
            view.setBelowSpeedBump(state.belowSpeedBump);
            view.setDark(state.dark, false, 0);
            view.setOverlap(state.overlap);
            if (((float) view.getClipTopAmount()) != ((float) state.clipTopAmount)) {
                view.setClipTopAmount(state.clipTopAmount);
            }
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow row = (ExpandableNotificationRow) view;
                if (state.isBottomClipped) {
                    row.setClipToActualHeight(true);
                }
                row.applyChildrenState(this);
            }
            return true;
        }
    }

    public void applyViewState(View view, ViewState state) {
        float alpha = view.getAlpha();
        float yTranslation = view.getTranslationY();
        float xTranslation = view.getTranslationX();
        float zTranslation = view.getTranslationZ();
        float newAlpha = state.alpha;
        float newYTranslation = state.yTranslation;
        float newZTranslation = state.zTranslation;
        boolean z = newAlpha != 0.0f ? state.hidden : true;
        if (alpha != newAlpha && xTranslation == 0.0f) {
            boolean z2;
            int newLayerType;
            boolean becomesFullyVisible = newAlpha == 1.0f;
            if (z || becomesFullyVisible) {
                z2 = false;
            } else {
                z2 = view.hasOverlappingRendering();
            }
            int layerType = view.getLayerType();
            if (z2) {
                newLayerType = 2;
            } else {
                newLayerType = 0;
            }
            if (layerType != newLayerType) {
                view.setLayerType(newLayerType, null);
            }
            view.setAlpha(newAlpha);
        }
        int oldVisibility = view.getVisibility();
        int newVisibility = z ? 4 : 0;
        if (!(newVisibility == oldVisibility || ((view instanceof ExpandableView) && ((ExpandableView) view).willBeGone()))) {
            view.setVisibility(newVisibility);
        }
        if (yTranslation != newYTranslation) {
            view.setTranslationY(newYTranslation);
        }
        if (zTranslation != newZTranslation) {
            view.setTranslationZ(newZTranslation);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("StackScrollState:");
        for (StackViewState s : this.mStateMap.values()) {
            s.dump(fd, pw, args);
        }
        for (ExpandableView s2 : this.mStateMap.keySet()) {
            s2.dump(fd, pw, args);
        }
    }
}
